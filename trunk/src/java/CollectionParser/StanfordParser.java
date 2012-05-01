/*
 * @author Jon Deering
Copyright 2011 Saint Louis University. Licensed under the Educational Community License, Version 2.0 (the "License"); you may not use
this file except in compliance with the License.

You may obtain a copy of the License at http://www.osedu.org/licenses/ECL-2.0

Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions
and limitations under the License.
 */
package CollectionParser;

import dmstech.canvas;
import dmstech.manifestAggregation;
import dmstech.sequence;
import dmstech.thing;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;
import textdisplay.Archive;
import textdisplay.DatabaseWrapper;
import textdisplay.Folio;
import textdisplay.Manuscript;
import textdisplay.mailer;
/**This class pulls in SharedCanvas manifests and uses them to add information about manuscripts and images to TPEN's datastore.*/
public class StanfordParser {
    public static void parse(String archive) throws IOException
    {
        String [] res=thing.getManifestAggregations("http://dms-data.stanford.edu/Repository.xml");
         for(int i=0;i<res.length;i++)
         {
             if(res[i].contains("arker")){
            String [] p= manifestAggregation.getManifestUrls(res[i]);
            for(int j=0;j<p.length;j++)
            {
                System.out.print(p[j]+"\n");
                try{
                sequence s=new sequence(new URL[]{new URL(p[j])},"");
                String checkDupe = "select * from folios where pageName=?";
                String updateQuery="update folios set uri=?, sequence=?, canvas=?, archive='Stanford' where pageNumber=?";
                Connection dbConn=null;
                try{
                    dbConn=DatabaseWrapper.getConnection();
                            PreparedStatement dupe = dbConn.prepareStatement(checkDupe);
                            PreparedStatement upd = dbConn.prepareStatement(updateQuery);
                               mailer mail=new mailer();
                            if(s==null || s.getSequenceItems()==null || !(s.getSequenceItems().length>0))
                            {
                                mail.sendMail("slumailrelay.slu.edu", "TPEN@t-pen.org", "jdeerin1@slu.edu", "Error loading Stanford MSS",  "Found no canvases (or none with images) for "+p[j] );}

                             dupe.setString(1, s.getSequenceItems()[0].getImageURL()[0].getImageURL().split("\\/")[s.getSequenceItems()[0].getImageURL()[0].getImageURL().split("\\/").length-1].replace("_46", ""));
                            ResultSet rs=dupe.executeQuery();
                            if(true)
                            {

                                if(false)
                                {
                                 System.out.print("no match for "+s.getSequenceItems()[0].getImageURL()[0].getImageURL().split("\\/")[s.getSequenceItems()[0].getImageURL()[0].getImageURL().split("\\/").length-1]+"\n");
                                        }
                                else{
                                canvas [] canvases=s.getSequenceItems();
//this does the importing. The else does some matching of old data that should never need to be done again
                                
if(true)
                                {try{
                                    Manuscript m=new Manuscript(s.getRepository(),"Stanford",s.getCollection(),s.getCity());
                                    String msg="Added manuscript"+  "Manuscript "+ m.getID()+" is now "+m.getShelfMark()+". It has "+canvases.length+" canvases.";
                                    Logger.getAnonymousLogger().log(Level.SEVERE, msg);

                                    mail.sendMail("slumailrelay.slu.edu", "TPEN@t-pen.org", "jdeerin1@slu.edu", "Added manuscript to TPEN",  "Manuscript "+ m.getID()+" is now "+m.getShelfMark()+". It has "+canvases.length+" canvases." );}
                                catch(Exception e)
                                {

                                }
                                }
                                System.out.print(s.getRepository()+"Stanford"+s.getCollection()+s.getCity()+"\n");
                                for(int l=0;l<canvases.length;l++)
                                {
                                    String tmp_name=s.getSequenceItems()[l].getImageURL()[0].getImageURL().split("\\/")[s.getSequenceItems()[l].getImageURL()[0].getImageURL().split("\\/").length-1].replace("TC_46", "TC");
                                    dupe.setString(1, tmp_name);
                                    rs=dupe.executeQuery();
                                    while(rs.next())
                                    {
                                        upd.setString(1, s.getSequenceItems()[l].getImageURL()[0].getImageURL()+"_xlarge");
                                        upd.setInt(2, s.getSequenceItems()[l].getPosition() );
                                        upd.setString(3, s.getSequenceItems()[l].getCanvas() );
                                        upd.setInt(4, rs.getInt("pageNumber"));
                                        upd.execute();
                                        File imgFile=new File("/images/parker/"+rs.getString("imageName").replace("//", "/"));
                                        if(imgFile.exists())
                                            imgFile.delete();

                                    }
                                //Folio.createFolioRecord(s.getCollection(), canvases[l].getTitle(), canvases[l].getImageURL()[0].getImageURL(), "Stanford", m.getID(), canvases[l].getPosition(), canvases[l].getCanvas());

                                }
                                }
                            }
                            else
                            {
                                System.out.print("would change "+rs.getString("uri")+" to "+s.getSequenceElement(1).getImageURL()[0].getImageURL()+"\n");
                            }



                }
                        finally{dbConn.close();}
                }

                catch(Exception e)
                {
                    e.printStackTrace();
                }
            }}
         }


    }
    }


