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

import java.awt.image.BufferedImage;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Random;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;
import imageLines.ImageHelpers;


/**
 * This class represents a folio with lines and possibly columns,identified within
 */
public class folio {

   line[] linePositions;
   int[] colStarts;
   int[] colWidths;
   int folioNumber;
   private String imageName;
   int msPage; //We have a different ordering of the folios of the MS than the folioation indicates, which will be stored here
   Boolean zoom = false;

   public folio(int id) throws SQLException {
      String q1 = "select imageName from pages where id=?";

      folioNumber = id;
      String query = "select * from imagepositions where imageName=? order by width,y";
      Connection j = textdisplay.DatabaseWrapper.getConnection();
      PreparedStatement p = j.prepareStatement(query);
      PreparedStatement p2 = j.prepareStatement(q1);
      p2.setInt(1, id);
      ResultSet name = p2.executeQuery();
      name.next();
      imageName = name.getString(1);
      Vector<line> linePosVector = new Vector<line>();
      p.setString(1, imageName);
      ResultSet rs = p.executeQuery();
      int i = 0;
      int pTop = 0;
      while (rs.next()) {
         int y = rs.getInt("x");
         int x = rs.getInt("y");
         if (x != 0) {
            int width = rs.getInt("width");
            int top = x - rs.getInt("height");

            pTop = top;

            line tmp = new line(y, y + width, top, x);
            linePosVector.add(tmp);

         }
      }
      linePositions = new line[linePosVector.size()];
      for (int k = 0; k < linePosVector.size(); k++) {
         linePositions[k] = linePosVector.get(k);
      }
      j.close();

   }

   public static int randomPage() throws SQLException {
      String query = "select id from pages where finding=0 order by Rand() limit 1";

      // String query="select min(id) from pages";


      //String query="SELECT * FROM `pages` WHERE id >= (SELECT FLOOR( MAX(id) * RAND()) FROM `pages` ) ORDER BY id LIMIT 1;";
      Connection j = textdisplay.DatabaseWrapper.getConnection();
      PreparedStatement p = j.prepareStatement(query);

      ResultSet rs;
      rs = p.executeQuery(query);
      rs.next();
      int min = rs.getInt(1);
      if (true) {
         j.close();
         return min;

      }
      rs = p.executeQuery("select max(id) from pages");
      rs.next();
      int max = rs.getInt(1);
      int count = max - min;
      Random generator = new Random();
      int rec = generator.nextInt(count);
      rec += min;



      j.close();

      return rec;

   }

   public String getImageName() {
      return imageName;
   }

   public static int userCount(String user) throws SQLException {

      String query = "select count(id) from pages where finder=?";
      System.out.print(query);
      Connection j = textdisplay.DatabaseWrapper.getConnection();
      PreparedStatement p = j.prepareStatement(query);
      p.setString(1, user);
      ResultSet rs = p.executeQuery();
      rs.next();
      int amount = rs.getInt(1);

      query = "select * from leaders where uname=?";
      p = j.prepareStatement(query);

      p.setString(1, user);
      rs = p.executeQuery();
      if (!rs.next()) {
         query = "insert into leaders (uname,count) values(?,0)";
         p = j.prepareStatement(query);
         p.setString(1, user);
         p.execute();
      }

      query = "update leaders set count=? where uname=?";
      p = j.prepareStatement(query);
      p.setInt(1, amount);
      p.setString(2, user);
      p.execute();
      j.close();
      return amount;
   }

   public static String leaderBoard() throws SQLException {
      String toret = "<table cellpadding=\"5\" cellspacing=\"5\" border=\"2\"><tr><td>Standing<td >Name<td>Images Evaluated</tr>";
      String query = "select * from leaders order by count desc";
      System.out.print(query);
      Connection j = textdisplay.DatabaseWrapper.getConnection();
      PreparedStatement p = j.prepareStatement(query);

      ResultSet rs = p.executeQuery();
      int count = 0;
      while (rs.next() && count < 10) {
         count++;
         toret += "<tr><td>" + count + "<td>" + rs.getString("uname") + "</td><td>" + rs.getInt("count") + "</td></tr>";

      }
      toret += "</table>";
      j.close();
      return toret;
   }

   public void setFinding(int finding, String user, int id) {
      try {
         String query = "update `pages` set `finding`=?, `finder`=? where `id`=?";
         System.out.print(query);
         Connection j = textdisplay.DatabaseWrapper.getConnection();
         PreparedStatement p = j.prepareStatement(query);
         p.setInt(1, finding);

         p.setString(2, user);
         p.setInt(3, id);
         int i = p.executeUpdate();
         //p.execute();
         // p.execute(query);
         j.close();
      } catch (SQLException ex) {
         Logger.getLogger(folio.class.getName()).log(Level.SEVERE, null, ex);
      }
   }

   public String getImageUrl() throws SQLException {
      String t = "pageImage?page=";
      String query = "select ms from pages where imageName=?";


      t += this.folioNumber;

      return t;
   }

   public BufferedImage getImg() {
      return ImageHelpers.scale(ImageHelpers.readAsBufferedImage("/database/web/parker/" + this.getImageName()), 1000);
   }

   public static BufferedImage highlightPortion(BufferedImage img, int x, int y, int height, int width) {

      for (int i = (int) (x - width); i < x + width * 2; i++) {
         try {
            img.setRGB(i, y - width, 0xff0000);
            img.setRGB(i, y - width - 1, 0xff0000);
            img.setRGB(i, y + 2 * width, 0xff0000);
            img.setRGB(i, y + 2 * width - 1, 0xff0000);
         } //if there was an out of bounds its because of the size doubling, ignore it
         catch (ArrayIndexOutOfBoundsException e) {
         }
      }
      for (int j = (int) (y - height); j < y + height * 2; j++) {
         try {
            img.setRGB(x - width, j, 0xff0000);
            img.setRGB(x - width - 1, j, 0xff0000);
            img.setRGB(x + 2 * width, j, 0xff0000);
            img.setRGB(x + 2 * width - 1, j, 0xff0000);
         } //if there was an out of bounds its because of the size doubling, ignore it
         catch (ArrayIndexOutOfBoundsException e) {
         }
      }

      return img;
   }

   public String getLinesAsDivs() {
      String toret = "";
      String onClick = "onclick=\"deleteme(this,event);\"";
      for (int i = 0; i < linePositions.length; i++) {
         if (i % 2 == 1) {
            toret += "<div class=\"line\" style=\"position:absolute;top:" + linePositions[i].top + "px;width:100%;height:" + (linePositions[i].bottom - linePositions[i].top) + "px;background-color:aqua;\" " + onClick + " >";
            toret += "</div>";
         } else {
            toret += "<div class=\"line\" style=\"position:absolute;top:" + linePositions[i].top + "px;width:100%;height:" + (linePositions[i].bottom - linePositions[i].top) + "px;background-color:red;\" " + onClick + " >";
            toret += "</div>";
         }
      }
      return toret;
   }

   /**
    * Returns each line of the ms represented by a properly sized div accounting for columns
    */
   public String getLinesAsDivsWithCols() {
      if (true) {
         return "";
      }
      String toret = "";
      String onClick = "onclick=\"deleteme(this,event);\"";
      for (int i = 0; i < linePositions.length; i++) {
         if (i % 2 == 1 && linePositions[i] != null) {
            toret += "<div class=\"line\" style=\"position:absolute;top:";
            toret += linePositions[i].top;
            toret += "px;left:" + linePositions[i].left;
            if (linePositions[i].getWidth() > 0) {
               toret += "px;width:" + linePositions[i].getWidth();
            } else {
               toret += "px;width:200";
            }
            toret += "px;height:" + (linePositions[i].bottom - linePositions[i].top);
            toret += "px;background-color:aqua;\" " + onClick + " >";
            toret += "</div>";
         } else {
            if (linePositions[i] != null) {
               toret += "<div class=\"line\" style=\"position:absolute;top:" + linePositions[i].top + "px;width:100%;left:" + linePositions[i].left;
               if (linePositions[i].getWidth() > 0) {
                  toret += "px;width:" + linePositions[i].getWidth() + "px;height:" + (linePositions[i].bottom - linePositions[i].top) + "px;background-color:red;\" " + onClick + " >";
               } else {
                  toret += "px;width:200" + "px;height:" + (linePositions[i].bottom - linePositions[i].top) + "px;background-color:red;\" " + onClick + " >";
               }
               toret += "</div>";
            }
         }
      }
      return toret;
   }

   public String parkerURL() {
      String[] nameBits = this.imageName.split("_");

      String toret = "";
      while (nameBits[0].charAt(0) == '0') {
         nameBits[0] = nameBits[0].substring(1);
      }
      while (nameBits[1].charAt(0) == '0') {
         nameBits[1] = nameBits[1].substring(1);
      }
      toret = "http://parkerweb.stanford.edu/parker/actions/page_turner?ms_no=" + nameBits[0] + "&pageNumber=" + nameBits[1] + "&pageType=";
      if (imageName.contains("V")) {
         toret += "V";
      }

      if (imageName.contains("R")) {
         toret += "R";
      }

      return toret;
   }
}
