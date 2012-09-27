
package CollectionParser;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;
import textdisplay.Archive;
import textdisplay.DatabaseWrapper;
import textdisplay.Folio;
import textdisplay.Manuscript;

/**This parser begins by loading a list of all manuscripts hosted by ecodices. It parses out each shelfmark on the page, and as it does so, checks to see 
 if there is an existing manuscript record for it. if not, a new manuscript record is created, then a page is loaded which contains a list of all images
 in the manuscript. That information is used to create the folio records. Note that this uses an image naming convention that sometimes breaks for the unnamed
 blank pages that are sometimes found in ecodices manuscripts.*/

/**
 * 
 * @author jdeering
 */
public class EcodicesParser {
    
    /**
     * 
     * @param archive
     * @throws MalformedURLException
     * @throws IOException
     * @throws SQLException 
     */
    public static void parse(String archive) throws MalformedURLException, IOException, SQLException
    {
        String toret = "junk";
        String urls = "";
        Boolean added = false;
        
        String url = "http://www.e-codices.unifr.ch/en/list/all/Shelfmark/10000/0";
        URL mslisting = new URL(url);
        BufferedReader b = new BufferedReader(new InputStreamReader(mslisting.openConnection().getInputStream()));
        
        String buf = "";
        Boolean in = false;
        int ctr = 0;
        
        String wholeShelfmark="";
        while (b.ready()) {
            // if(ctr>3)
            //   return urls;
            buf = b.readLine();
            // urls+=buf;
            
            if (in && buf.contains("</select>")) {
                in = false;
            }
            
            if (in) {
                try {
                    if (!buf.contains("#")) {
                        ctr++;
                        added = true;
                        String s = buf.split("value=\"")[1].split("\"")[0];
                        wholeShelfmark=buf.split(">")[1].split("</")[0];
                        System.out.print(wholeShelfmark);
                        
                        toret = " ," + s;
                        //urls+=s;
                    }
                } catch (Exception e) {
                }
            }
            
            if (buf.contains("select name=\"quickselect\"")) {
                in = true;
            }
            
            if (added) {
                added = false;
                
                String[] msses = toret.split(",");
                for (int i = msses.length - 1; i < msses.length; i++) {
                    Connection dbConn = null;
                    
                    try {
                        dbConn = DatabaseWrapper.getConnection();
                        String checkDupe = "select * from manuscript where archive=? and city=? and repository=? and msIdentifier=?";
                        PreparedStatement dupe = dbConn.prepareStatement(checkDupe);
                        String query = "insert into folios (collection,pageName,imageName,archive) values(?,?,?,?)";
                        
                        System.err.print("Starting " + msses[i] + "\n");
                        try {
                            //urls+=msses[i];
                            String firstPart = msses[i].split("-")[0];
                            String secondPart = msses[i].substring(msses[i].indexOf("-")+1);
                            //urls+=firstPart+secondPart+"<br>";
                            
                            String city=wholeShelfmark.split(",")[0];
                            String repo=wholeShelfmark.split(",")[1].trim();
                            String collection=wholeShelfmark.split(",")[2].split("[0-9]")[0]+ msses[i].substring(msses[i].split("[0-9]")[0].length());
                            collection=collection.trim();
                            System.out.print(collection+"\n");
                            String imageBase="<img id=\"msImage\" src=\"";
                            String url2 = "http://www.e-codices.unifr.ch/en/" + firstPart + "/" + secondPart;
                            
                            try {
                                Thread.sleep(500);//sleep for 1000 ms
                            } catch (InterruptedException ie) {
                                //If this thread was intrrupted by nother thread
                            }
                            
                            // urls+=url2+"<br>";
                            
                            URL msUrl = new URL(url2);
                            System.out.print("fetching "+url2+"\n");
                            BufferedReader msPage = new BufferedReader(new InputStreamReader(msUrl.openStream()));
                            while (msPage.ready()) {
                                String buffer = msPage.readLine();
                                if(imageBase.compareTo("<img id=\"msImage\" src=\"")==0&&buffer.contains(imageBase))
                                {
                                    imageBase=buffer.split(imageBase)[1].split("\"")[0].split("fit")[0];
                                    System.out.print("imageBase:"+imageBase+"\n");
                                }
                            }
                            msPage = new BufferedReader(new InputStreamReader(msUrl.openStream()));
                            Boolean inPageSelect = false;
                            dupe.setString(1, archive);
                            dupe.setString(2, city);
                            dupe.setString(3, repo);
                            dupe.setString(4,collection);
                            ResultSet dupeCheck=dupe.executeQuery();
                            if(dupeCheck.next())
                            {
                                System.out.print("Skipping, already know that MSS.\n");
                            }
                            else
                            {
                                Manuscript ms=new Manuscript(repo,archive,collection,city);
                                int seq=1;
                                while (msPage.ready()) {
                                    String buffer = msPage.readLine();
                                    // urls+=buffer;
                                    
                                    if (inPageSelect && buffer.contains("</select>")) {
                                        inPageSelect = false;
                                    }
                                    String pageURL="";
                                    if (inPageSelect) {
                                        pageURL="http://www.e-codices.unifr.ch/en/";
                                        buffer = buffer.split("\"")[1];
                                        pageURL+=buffer;
                                        System.out.print("pageURL:"+pageURL+"\n");
                                        String pageID = buffer.split("/")[2];
                                        int numberCount = 0;
                                        for (int j = 0; j < pageID.length(); j++) {
                                            if (Character.isDigit(pageID.charAt(j))) {
                                                numberCount++;
                                            }
                                        }
                                        if (numberCount == 2) {
                                            pageID = "0" + pageID;
                                        }
                                        if (numberCount == 1) {
                                            pageID = "00" + pageID;
                                        }
                                        String imagename = buffer.split("/")[0] + "-" + buffer.split("/")[1] + "_" + pageID + ".jpg";
                                        String imageurl="";
                                        if(pageID.matches("[0-9]+[r,v]") || pageID.matches("[0-9]+"))
                                        {
                                            System.out.println("regex hit for "+pageID);
                                            imageurl = imageBase+"film/" + msses[i] + "/" + imagename;
                                        }
                                        else
                                        {
                                            imageurl=getEcodicesImageURL( pageURL);
                                        }
                                        
                                        //String
                                        // urls+="<a href=\""+imageurl+"\">"+imageurl+"</a><br>\n";
                                        //public static int createFolioRecord(String collection, String pageName, String imageName, String Archive, int msID, int sequence,String canvas) throws SQLException {
                                        Folio.createFolioRecord(collection, pageID.replace(".jpg", ""), imageurl, archive, ms.getID(), seq, "");
                                        seq++;
                                        //int folionum = Folio.createFolioRecord(msses[i].replace("csg-",""), pageID, imageurl, "ecodices");
                                    }
                                    
                                    if (buffer.contains("id=\"page_select\"")) {
                                        inPageSelect = true;
                                    }
                                }
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    } finally {
                        DatabaseWrapper.closeDBConnection(dbConn);
                    }
                }
            }
        }
    }
    
    
    /**
     * 
     * @param pageURL
     * @return 
     */
    private static String getEcodicesImageURL(String pageURL)
    {
        
        try {
            Thread.sleep(2000);//sleep for 1000 ms
        } catch (InterruptedException ie) {
            //If this thread was intrrupted by nother thread
        }
        
        BufferedReader content = null;
        try {
            String toret="";
            content = new BufferedReader(new InputStreamReader(new URL(pageURL).openConnection().getInputStream()));
            while(content.ready())
            {
                String buff=content.readLine();
                if(buff.contains("<img id=\"msImage\" src=\""))
                {
                    toret=buff.split("<img id=\"msImage\" src=\"")[1].split("\"")[0].replace("/fit/", "/film/");
                }
            }
            content.close();
            return toret;
        } catch (MalformedURLException ex) {
            Logger.getLogger(Archive.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(Archive.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                content.close();
            } catch (IOException ex) {
                Logger.getLogger(Archive.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        
        return "error";
    }
    
}
