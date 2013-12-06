/*
 * Copyright 2011-2013 Saint Louis University. Licensed under the
 *	Educational Community License, Version 2.0 (the "License"); you may
 * not use this file except in compliance with the License. You may
 * obtain a copy of the License at
 *
 * http://www.osedu.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an "AS IS"
 * BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing
 * permissions and limitations under the License.
 * 
 * @author Jon Deering
 */
package match;

import detectimages.blob;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import textdisplay.DatabaseWrapper;
import textdisplay.Folio;
import textdisplay.Manuscript;

/**
 *
 * @author jdeerin1
 */
public class matchLocater {

   String url;
   int matchCount;
   int[] charCounts = new int[5000];
   int[] maxLevels = new int[5000];

   public matchLocater(String img1, int blob1) throws SQLException {
      matchCount = 0;
      url = "";
      String query = "select img2,blob2 from blobmatches where img1=? and blob1=? order by img2";
      Connection j = DatabaseWrapper.getConnection();
      PreparedStatement ps;
      ps = j.prepareStatement(query);
      ps.setString(1, img1);
      ps.setInt(2, blob1);
      ResultSet rs = ps.executeQuery();
      String img2 = "";
      while (rs.next()) {

         if (img2.compareTo(rs.getString(1)) != 0) {
            img2 = rs.getString(1);
            url += ("<br>" + img2 + ":");
         }
         int folioNum = Integer.parseInt(rs.getString(1).replace(".jpg", "").split("/")[rs.getString(1).replace(".jpg", "").split("/").length - 1]);
         Folio f = new Folio(folioNum);
         if (f.isCached()) {
            url += "<span href=\"paleo.jsp?highlightblob=true&page=" + rs.getString(1).replace(".jpg", "") + "&orig=1&blob=" + rs.getInt(2) + "\"><img src=\"characterImage?page=" + rs.getString(1).replace(".jpg", "") + "&orig=1&blob=" + rs.getInt(2) + "\"/></span>\n";
            matchCount++;
         } else {
            url += "not cached";
         }
      }
      j.close();
   }

   public matchLocater(int img1, int blob1, String express) throws FileNotFoundException, IOException, SQLException {
      url = "";
      charCounts = new int[5000];
      for (int i = 0; i < charCounts.length; i++) {
         charCounts[i] = 0;
         maxLevels[i] = 0;
      }
      textdisplay.Folio fol = new textdisplay.Folio(img1);
      Manuscript ms = new Manuscript(img1);
      File f = new File(textdisplay.Folio.getRbTok("PALEOTEMPDIR") + textdisplay.Folio.getRbTok("PALEODATADIR") + "/" + ms.getID() + "/" + img1 + ".txt .txt");
      BufferedReader b = new BufferedReader(new FileReader(f));
      String[] lines = blob.readFileIntoArray(f.getAbsolutePath());
      String oldpage = "";
      Pattern colon = Pattern.compile(":");
      Pattern semicolon = Pattern.compile(";");
      Pattern slash = Pattern.compile("/");
      Pattern quote = Pattern.compile("\"");
      for (int i = 0; i < lines.length; i++) {

         String buff = lines[i];
         buff = buff.replace("\"", "");
         //System.out.print(buff+"\n");
         String parts1 = colon.split(semicolon.split(buff)[0])[1];
         String parts2 = colon.split(semicolon.split(buff)[1])[0];
         //String parts2=buff.replace("\"", "").split("\\;")[1].split(":")[0];
         parts2 = parts2.replace(".jpg", "").split("/")[parts2.replace(".jpg", "").split("/").length - 1].replace(".txt", "");
         String parts3 = colon.split(semicolon.split(buff)[1])[1];
         int blob = Integer.parseInt(parts1);
         if (blob1 == 0 || blob1 == blob) {
            if (oldpage.compareTo(parts2) != 0) {
               Folio tmp = new Folio(Integer.parseInt(parts2));
               url += ("<br>" + tmp.getPageName() + ":");
               oldpage = parts2;
            }
            //this is what they wanted
            //int folioNum=Integer.parseInt(parts2);
            //System.out.print("folio:"+folioNum+"\n");

            charCounts[blob]++;
         }


      }
      b.close();
   }

   public matchLocater(int img1, int blob1, String express, int requiredMatchLevel) throws FileNotFoundException, IOException, SQLException {
      url = "";
      charCounts = new int[5000];
      maxLevels = new int[5000];
      for (int i = 0; i < charCounts.length; i++) {

         maxLevels[i] = 0;
         charCounts[i] = 0;
      }
      textdisplay.Folio fol = new textdisplay.Folio(img1);
      Manuscript ms = new Manuscript(img1);
      File f = new File(textdisplay.Folio.getRbTok("PALEOTEMPDIR") + textdisplay.Folio.getRbTok("PALEODATADIR") + "/" + ms.getID() + "/" + img1 + ".txt .txt");
      BufferedReader b = new BufferedReader(new FileReader(f));
      String[] lines = blob.readFileIntoArray(f.getAbsolutePath());
      String oldpage = "";
      Pattern colon = Pattern.compile(":");
      Pattern semicolon = Pattern.compile(";");
      Pattern slash = Pattern.compile("/");
      Pattern quote = Pattern.compile("\"");
      int maxMatchLevel = 0;
      for (int i = 0; i < lines.length; i++) {

         String buff = lines[i];
         buff = buff.replace("\"", "");
         //System.out.print(buff+"\n");
         String parts1 = colon.split(semicolon.split(buff)[0])[1];
         String parts2 = colon.split(semicolon.split(buff)[1])[0];
         //String parts2=buff.replace("\"", "").split("\\;")[1].split(":")[0];
         parts2 = parts2.replace(".jpg", "").split("/")[parts2.replace(".jpg", "").split("/").length - 1].replace(".txt", "");
         String parts3 = buff.replace("\"", "").split("\\;")[1].split(":")[1].split("/")[0];
         String parts4 = buff.replace("\"", "").split("\\;")[1].split(":")[1].split("/")[1];
         int matchLevel = Integer.parseInt(parts4);
         int blob = Integer.parseInt(parts1);
         if (blob1 == 0 || blob1 == blob) {

            if (oldpage.compareTo(parts2) != 0) {
               Folio tmp = new Folio(Integer.parseInt(parts2));
               url += ("<br>" + tmp.getPageName() + ":");
               oldpage = parts2;
            }
            //this is what they wanted
            //int folioNum=Integer.parseInt(parts2);
            //System.out.print("folio:"+folioNum+"\n");
            if (matchLevel >= requiredMatchLevel) {
               if (matchLevel > maxLevels[blob]) {
                  maxLevels[blob] = matchLevel;
               }
               charCounts[blob]++;
            }
         }


      }
      b.close();
   }

   public matchLocater(int img1, int blob1, Boolean noSQL) throws FileNotFoundException, IOException, SQLException {
      url = "";
      charCounts = new int[5000];
      for (int i = 0; i < charCounts.length; i++) {
         charCounts[i] = 0;
      }
      textdisplay.Folio fol = new textdisplay.Folio(img1);
      Manuscript ms = new Manuscript(img1);
      File f = new File(textdisplay.Folio.getRbTok("PALEOTEMPDIR") + textdisplay.Folio.getRbTok("PALEODATADIR") + "/" + ms.getID() + "/" + img1 + ".txt .txt");
      BufferedReader b = new BufferedReader(new FileReader(f));
      String oldpage = "";
      while (b.ready()) {
         String buff = b.readLine();
         //System.out.print(buff+"\n");
         String parts1 = buff.replace("\"", "").split("\\;")[0].split(":")[1];
         String parts2 = buff.replace("\"", "").split("\\;")[1].split(":")[0];
         parts2 = parts2.replace(".jpg", "").split("/")[parts2.replace(".jpg", "").split("/").length - 1].replace(".txt", "");
         String parts3 = buff.replace("\"", "").split("\\;")[1].split(":")[1].split("/")[0];
         String parts4 = buff.replace("\"", "").split("\\;")[1].split(":")[1].split("/")[1];
         int matchLevel = Integer.parseInt(parts4);
         int blob = Integer.parseInt(parts1);
         if (blob1 == 0 || blob1 == blob) {
            if (oldpage.compareTo(parts2) != 0) {
               Folio tmp = new Folio(Integer.parseInt(parts2));
               url += ("<br>" + tmp.getPageName() + ":");
               oldpage = parts2;
            }
            //this is what they wanted
            int folioNum = Integer.parseInt(parts2);
            System.out.print("folio:" + folioNum + "\n");
            Folio folio = new Folio(folioNum);
            if (folio.isCached()) {
               String folioNumber = parts2;

               url += "<span href=\"paleo.jsp?highlightblob=true&p=" + folioNumber + "&orig=1&blob=" + parts3 + "\"><img src=\"characterImage?page=" + folio.getFolioNumber() + "&blob=" + parts3 + "\"/></span>\n";
            } else {
               url += "not cached";
            }
            charCounts[blob]++;
         }


      }
      b.close();
   }

   public matchLocater(int img1, int blob1, Boolean noSQL, int requiredMatchLevel) throws FileNotFoundException, IOException, SQLException {
      url = "";
      charCounts = new int[5000];
      for (int i = 0; i < charCounts.length; i++) {
         charCounts[i] = 0;
      }
      textdisplay.Folio fol = new textdisplay.Folio(img1);
      Manuscript ms = new Manuscript(img1);
      File f = new File(textdisplay.Folio.getRbTok("PALEOTEMPDIR") + textdisplay.Folio.getRbTok("PALEODATADIR") + "/" + ms.getID() + "/" + img1 + ".txt .txt");
      BufferedReader b = new BufferedReader(new FileReader(f));
      String oldpage = "";
      int maxLevel = 0;
      String tmpResult = "";
      int count = 0;
      while (b.ready()) {
         String buff = b.readLine();
         //System.out.print(buff+"\n");
         String parts1 = buff.replace("\"", "").split("\\;")[0].split(":")[1];
         String parts2 = buff.replace("\"", "").split("\\;")[1].split(":")[0];
         parts2 = parts2.replace(".jpg", "").split("/")[parts2.replace(".jpg", "").split("/").length - 1].replace(".txt", "");
         String parts3 = buff.replace("\"", "").split("\\;")[1].split(":")[1].split("/")[0];
         String parts4 = buff.replace("\"", "").split("\\;")[1].split(":")[1].split("/")[1];
         int matchLevel = Integer.parseInt(parts4);

         if (matchLevel >= requiredMatchLevel) {

            int blob = Integer.parseInt(parts1);
            if (blob1 == 0 || blob1 == blob) {
               if (oldpage.compareTo(parts2) != 0) {

                  if (oldpage.compareTo("") != 0) {
                     url += "<div class='result' matchLevel='" + maxLevel + "'>" + new Folio(Integer.parseInt(oldpage)).getPageName() + ":" + tmpResult + "</div>";
                     tmpResult = "";
                  }
                  Folio tmp = new Folio(Integer.parseInt(parts2));

                  oldpage = parts2;
               }
               //this is what they wanted
               int folioNum = Integer.parseInt(parts2);
               System.out.print("folio:" + folioNum + "\n");
               Folio folio = new Folio(folioNum);
               if (matchLevel > maxLevel) {
                  maxLevel = matchLevel;
               }
               if (folio.isCached()) {
                  int x, y, width, height;
                  String folioNumber = parts2;
                  blobGetter thisBlob = new blobGetter(folioNumber, Integer.parseInt(parts3));
                  width = (int) (thisBlob.getHeight() * 2.941);
                  height = (int) (thisBlob.getWidth() * 2.941);
                  x = (int) (thisBlob.getX() * 2.941);
                  y = (int) (thisBlob.getY() * 2.941);
                  width = (int) (thisBlob.getHeight() * 1.47);
                  height = (int) (thisBlob.getWidth() * 1.47);
                  x = (int) (thisBlob.getX() * 1.47);
                  y = (int) (thisBlob.getY() * 1.47);
                  /*width=       (int) (thisBlob.getHeight());
                   height=      (int) (thisBlob.getWidth());
                   x=           (int) (thisBlob.getX());
                   y=           (int) (thisBlob.getY());*/
//                tmpResult+="<img src=\""+folio.getImageURL().replace("_xlarge","")+"?zoom=50&region="+x+","+y+","+width+","+height+"&h=2000\"/>\n";
                  tmpResult += "<span href=\"paleo.jsp?highlightblob=true&p=" + folioNumber + "&orig=1&blob=" + parts3 + "\" matchLevel='" + matchLevel + "'><img src=\"characterImage?page=" + folio.getFolioNumber() + "&blob=" + parts3 + "\"/></span>\n";
               } else {
                  tmpResult += "<span class='match' matchLevel='" + matchLevel + "'>This image is not available for display.</span>";
               }
               charCounts[blob]++;
            }


         }
      }
      b.close();
   }

   public String getUrls() {
      return url;
   }

   public int getMatchCount() {
      return matchCount;
   }

   public int getBlobMatchCount(int blob) {
      return charCounts[blob];
   }

   public int getBlobMaxMatchLevel(int blob) {
      return maxLevels[blob];
   }

   public static int getBlobMatchCount(String img1, int blob1, Connection j) throws SQLException {
      String query = "select count from charactercount where `img`=? and `blob`=?";
      //Connection j=DatabaseWrapper.getConnection();
      PreparedStatement ps;
      ps = j.prepareStatement(query);
      ps.setString(1, img1);
      ps.setInt(2, blob1);
      ResultSet rs = ps.executeQuery();
      if (rs.next()) {
         int toret = rs.getInt(1);
         //j.close();
         return toret;

      } else {
         // j.close();
         return 0;
      }

   }

   public static String getBlobMatchCousins(String img1, int blob1) throws SQLException {
      int counter = 0;
      String toret = "";
      String query = "select img2,blob2 from blobmatches where img1=? and blob1=? order by img2";
      String cousinQuery = "select img2,blob2 from blobmatches where img1=? and blob1=? order by img2";
      Connection j = DatabaseWrapper.getConnection();
      PreparedStatement ps;
      ps = j.prepareStatement(query);
      PreparedStatement ps2 = j.prepareStatement(cousinQuery);
      ps.setString(1, img1);
      ps.setInt(2, blob1);
      ResultSet rs = ps.executeQuery();
      String img2 = "";
      Hashtable<String, String> res = new Hashtable();
      while (rs.next()) {
         res.put(rs.getString(1) + rs.getInt(2), "");
         ps2.setString(1, rs.getString(1));
         ps2.setInt(2, rs.getInt(2));
         ResultSet rs2 = ps2.executeQuery();
         while (rs2.next()) {
            res.put(rs2.getString(1) + rs2.getInt(2), rs2.getString(1) + rs2.getInt(2));
            counter++;
            if (counter > 500) {
               break;
            }
         }
         if (counter > 500) {
            break;
         }

      }
      Enumeration e = res.elements();


      int ctr = 0;
      while (e.hasMoreElements()) {
         e.nextElement();
         ctr++;

      }
      e = res.elements();
      String[] blobs = new String[ctr];
      ctr = 0;
      while (e.hasMoreElements()) {

         blobs[ctr] = (String) e.nextElement();
         ctr++;

      }
      Arrays.sort(blobs);
      String oldimg = "";
      for (int i = 0; i < blobs.length; i++) {
         try {
            String tmp = blobs[i];
            int num = Integer.parseInt(tmp.split(".jpg")[1]);
            tmp = tmp.split(".jpg")[0];
            if (tmp.compareTo(oldimg) != 0) {
               toret += "<br>" + tmp;
               oldimg = tmp;
            }
            toret += "<span href=\"paleo.jsp?page=" + tmp + "&blob=" + num + "&blobhighlight=true\"><img src=\"characterImage?page=" + tmp + "&blob=" + num + "\"/></span>\n";
         } catch (Exception ex) {
            toret += "<br>" + blobs[i];
         }
      }
      return toret;

   }

   public static String getDropdown(int msID) throws SQLException {
      String toret = "";
      String query = "select * from folios where msID=? and paleography!='0000-00-00 00:00:00' order by pageName";
      Connection j = DatabaseWrapper.getConnection();
      PreparedStatement ps = j.prepareStatement(query);
      ps.setInt(1, msID);
      ResultSet rs = ps.executeQuery();


      while (rs.next()) {

         Folio f = new Folio((rs.getInt("pageNumber")));
         toret += "<option value=\"?p=" + f.getFolioNumber() + "\">" + f.getPageName() + "</option>";
      }
      return toret;
   }

   public static void main(String[] args) {
      try {
         matchLocater m = new matchLocater(1, 100, true);
         for (int i = 0; i < 100; i++) {
            System.out.print("" + i + ": " + m.getBlobMatchCount(i) + "\n");
         }
         System.out.print(m.url);
      } catch (SQLException | IOException ex) {
         Logger.getLogger(matchLocater.class.getName()).log(Level.SEVERE, null, ex);
      }
   }
}
