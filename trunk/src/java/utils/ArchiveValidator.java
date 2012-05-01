/*
 * @author Jon Deering
Copyright 2011 Saint Louis University. Licensed under the Educational Community License, Version 2.0 (the "License"); you may not use
this file except in compliance with the License.

You may obtain a copy of the License at http://www.osedu.org/licenses/ECL-2.0

Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions
and limitations under the License.
 */
package utils;

import detectimages.imageHelpers;
import imageLines.ImageHelpers;
import java.awt.image.BufferedImage;
import java.net.MalformedURLException;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.http.client.ClientProtocolException;
import textdisplay.Archive;
import textdisplay.DatabaseWrapper;
import textdisplay.Folio;
/**Provides mechanisms for checking that the images on record for an archive are loading correctly.*/

public class ArchiveValidator {
    private String archive;
public ArchiveValidator(String a){
    this.archive=a;
}
/**Attempt to load a single image from each manuscript hosted by a particular archive, return a String with error information if there were failures*/
public String checkRandomFolioInEachManuscript() throws SQLException, MalformedURLException
    {
    String toret="";
    String query="select id from manuscript where archive=?";
    Connection j = null;
        PreparedStatement stmt = null;
        PreparedStatement folioStmt=null;
        try {
            j = DatabaseWrapper.getConnection();
            stmt = j.prepareStatement(query);
            String folioQuery="select pageNumber from folios where msID=? order by RAND() limit 1";
            folioStmt=j.prepareStatement(folioQuery);
            stmt.setString(1,this.archive);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                try {
                    //sleep 10 seconds
                    System.out.print("sleeping 5\n");
                    Thread.sleep(5000);
                    
                } catch (InterruptedException ex) {
                    Logger.getLogger(ArchiveValidator.class.getName()).log(Level.SEVERE, null, ex);
                }

            folioStmt.setInt(1, rs.getInt(1));
            ResultSet rs2=folioStmt.executeQuery();
            if(rs2.next())
            {
                Folio f=new Folio(rs2.getInt(1));
                long time=System.currentTimeMillis();
            Boolean check=this.checkImage(rs2.getInt(1));
            time=System.currentTimeMillis()-time;
            System.out.print("Checked in "+time+" ms\n");
            if(!check){
                System.out.print("Failed to load folio "+rs2.getInt(1)+" "+f.getImageURL()+"\n");
                }
 else
            {
                System.out.print("Successfully checked folio "+rs2.getInt(1)+" "+f.getImageURL()+"\n");
 }
            }
            }
        }
        finally
        {
            DatabaseWrapper.closeDBConnection(j);
        DatabaseWrapper.closePreparedStatement(stmt);
        }

    return toret;
}
private Boolean checkImage(int folio) throws SQLException
    {

   
        Folio f=new Folio(folio);
            try {
                System.out.print(f.getImageURL()+"\n");
                return ImageHelpers.checkImageHeader(f.getImageURL(), "usr", "pass");
            } catch (ClientProtocolException ex) {
                Logger.getLogger(ArchiveValidator.class.getName()).log(Level.SEVERE, null, ex);
            }

    
    return false;
}
/**Attempt to load a single image random hosted by a particular archive, return a String with error information if there were failures*/
public String checkRandomFolio() throws SQLException, MalformedURLException
    {
    String toret="";
    String query="select pageNumber from folios where archive=? order by RAND() limit 1";
    Connection j = null;
        PreparedStatement stmt = null;
        try {
            j = DatabaseWrapper.getConnection();
            stmt = j.prepareStatement(query);
            stmt.setString(1,this.archive);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
            //request that image using imageResize
                BufferedImage img = imageHelpers.readAsBufferedImage(new URL(Folio.getRbTok("SERVERCONTEXT")+ "imageResize?folioNum="+ rs.getInt(1)+"&code="+Folio.getRbTok("imageCode")));
                if(img!=null)
                    return "no problems";
                Folio f=new Folio(rs.getInt(1));
                toret+="Failed to load folio "+rs.getInt(1)+" "+f.getImageURL()+"\n";

            }
        }
        finally{
        DatabaseWrapper.closeDBConnection(j);
        DatabaseWrapper.closePreparedStatement(stmt);
        }
    return toret;
}
/**Attempt to load a every hosted by a particular archive, return a String with error information if there were failures. Not likely to see much use...*/
public String checkAllFolios() throws SQLException, MalformedURLException
    {
    String toret="";
    String query="select pageNumber from folios where archive=?";
    Connection j = null;
        PreparedStatement stmt = null;
        try {
            j = DatabaseWrapper.getConnection();
            stmt = j.prepareStatement(query);
            stmt.setString(1,this.archive);
            ResultSet rs = stmt.executeQuery();
           while (rs.next()) {
            //request that image using imageResize
               try {
                    //sleep 10 seconds
                    Thread.sleep(10000);

                } catch (InterruptedException ex) {
                    Logger.getLogger(ArchiveValidator.class.getName()).log(Level.SEVERE, null, ex);
                }
                BufferedImage img = imageHelpers.readAsBufferedImage(new URL(Folio.getRbTok("SERVERCONTEXT")+ "imageResize?folioNum="+ rs.getInt(1)+"&code="+Folio.getRbTok("imageCode")));
                if(img==null)
                {
                Folio f=new Folio(rs.getInt(1));
                toret+="Failed to load folio "+rs.getInt(1)+" "+f.getImageURL()+"\n";
                }

            }
        }
        finally{
        DatabaseWrapper.closeDBConnection(j);
        DatabaseWrapper.closePreparedStatement(stmt);
        }
    return toret;
}
}
