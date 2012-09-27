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

public class CEECParser {
    /**
     * This parser begins by loading the apache directory listing,
     * which contains a folder for each manuscript hosted by the CEEC.
     * It then follows the link to the folder containing the images
     * for each manuscript.  From that page, it is able to construct
     * the needed folio records for each image. This is an old
     * parser that does not create manuscript records.
     * 
     * @param archive
     * @throws MalformedURLException
     * @throws IOException 
     */
    public static void parse(String archive) throws MalformedURLException, IOException
    {
        String toret = "junk";
        String urls = "";
        Boolean added = false;
        if (archive.compareTo("CEEC") == 0) {
            String url = "http://www.ceec.uni-koeln.de/projekte/CEEC/manuscripts/indiv/";
            URL mslisting = new URL(url);
            BufferedReader b = new BufferedReader(new InputStreamReader(mslisting.openConnection().getInputStream()));
            Connection dbConn =null;
            
            String buf = "";
            Boolean in = false;
            int ctr = 0;
            
            while (b.ready()) {
                
                // if(ctr>3)
                //   return urls;
                buf = b.readLine();
                // urls+=buf;
                
                if (buf.contains("alt=\"[DIR]\"")) {
                    try {
                        //if(!buf.contains("#"))
                        {
                            ctr++;
                            added = true;
                            String s = buf.split("href=\"")[1].split("\"")[0];
                            toret = "junk," + s;
                            //urls+=s+"/";
                        }
                    } catch (Exception e) {
                    }
                }
                
                if (added) {
                    added = false;
                    
                    String[] msses = toret.split(",");
                    for (int i = msses.length - 1; i < msses.length; i++) {
                        System.err.print("Starting " + msses[i] + "\n");
                        dbConn = DatabaseWrapper.getConnection();
                        try {
                            //urls+=msses[i];
                            // String firstPart=msses[i].split("-")[0];
                            //String secondPart=msses[i].split("-")[1];
                            //urls+=firstPart+secondPart+"<br>";
                            String url2 = url + msses[i];
                            
                            try {
                                Thread.sleep(500);//sleep for 1000 ms
                            } catch (InterruptedException ie) {
                                //If this thread was intrrupted by nother thread
                            }
                            
                            //urls+=url2+"<br>";
                            URL msUrl = new URL(url2);
                            BufferedReader msPage = new BufferedReader(new InputStreamReader(msUrl.openStream()));
                            Boolean inPageSelect = false;
                            
                            String checkDupe = "select * from folios where collection=? and pageName=? and imageName=? and archive=?";
                            PreparedStatement dupe = dbConn.prepareStatement(checkDupe);
                            String query = "insert into folios (collection,pageName,imageName,archive) values(?,?,?,?)";
                            
                            PreparedStatement stmt = dbConn.prepareStatement(query, PreparedStatement.RETURN_GENERATED_KEYS);
                            while (msPage.ready()) {
                                String buffer = msPage.readLine();
                                // urls+=buffer;
                                
                                if (buffer.contains("alt=\"[IMG]\"")) {
                                    
                                    buffer = buffer.split("href=\"")[1].split("\"")[0];
                                    String pageID = buffer;
                                    int numberCount = 0;
                                    for (int j = 0; j < pageID.length(); j++) {
                                        
                                        if (Character.isDigit(pageID.charAt(j))) {
                                            numberCount++;
                                        }
                                    }
                                    /* if(numberCount==2)
                                    pageID="0"+pageID;
                                    if(numberCount==1)
                                    pageID="00"+pageID;
                                     *
                                     */
                                    String imagename = buffer;
                                    String imageurl = url + msses[i] + imagename;
                                    //urls+="<a href=\""+imageurl+"\">"+imageurl+"</a><br>\n";
                                    
                                    int folionum = Folio.createFolioRecord(msses[i].replace("csg-", ""), pageID.replace(".jpg", ""), imageurl, "CEEC", dupe, stmt);
                                }
                            }
                        } catch (Exception e) {
                        } finally {
                            DatabaseWrapper.closeDBConnection(dbConn);
                        }
                    }
                }
            }
        }
    }
}
