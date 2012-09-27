/*
 * @author Jon Deering
Copyright 2011 Saint Louis University. Licensed under the Educational Community License, Version 2.0 (the "License"); you may not use
this file except in compliance with the License.

You may obtain a copy of the License at http://www.osedu.org/licenses/ECL-2.0

Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions
and limitations under the License.
 */

package Paleography;
import detectimages.imageProcessor;
import imageLines.ImageCache;
import imageLines.ImageHelpers;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
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

/**This class extracts feature information from images in the imagecache for paleographic feature comparison*/
public class ImageAnalyzer {
    /**Analyze all images in the image cache that havent been analyzed yet*/
    public ImageAnalyzer () throws SQLException, MalformedURLException
    {
        String query="select folio,pageNumber from folios join imageCache on folios.pageNumber=imageCache.folio where folios.paleography='0000-00-00 00:00:00'";
        String update="update folios set paleography=now() where pageNumber=?";
        Connection j=null;
PreparedStatement ps=null;
        try{
            j=DatabaseWrapper.getConnection();
            ps=j.prepareStatement(query);
            ResultSet rs=ps.executeQuery();
            ps=j.prepareStatement(update);
            while(rs.next())
            {
                Folio f=new Folio(rs.getInt("pageNumber"));
                System.out.print(f.getFolioNumber()+"\n");
                BufferedImage img = ImageCache.getImage(f.getFolioNumber());//ImageHelpers.readAsBufferedImage(new URL(Folio.getRbTok("SERVERCONTEXT") + Folio.getImageURL(rs.getInt("pageNumber"))+"&code="+Folio.getRbTok("imageCode")));
                detectimages.imageProcessor proc=new imageProcessor(img,2000);
                Manuscript ms=new Manuscript(f.getFolioNumber());
                System.out.print(Folio.getRbTok("PalographyDataDir")+"/"+ms.getID()+"/\n");
                File outdir=new File(Folio.getRbTok("PalographyDataDir")+"/"+ms.getID()+"/");
                if(!outdir.exists())
                    outdir.mkdirs();
                proc.setDataPath(Folio.getRbTok("PalographyDataDir")+"/"+ms.getID()+"/");
                proc.run2(""+f.getFolioNumber()+".txt");
                ps.setInt(1, f.getFolioNumber());
                ps.execute();
            }
        }
        catch (IOException ex) {
            Logger.getLogger(ImageAnalyzer.class.getName()).log(Level.SEVERE, null, ex);
        }        finally{
DatabaseWrapper.closeDBConnection(j);
DatabaseWrapper.closePreparedStatement(ps);
        }
    }
    public ImageAnalyzer (String archive) throws SQLException, MalformedURLException
    {
        String query="select pageNumber from folios where folios.paleography='0000-00-00 00:00:00' and archive=?";
        String update="update folios set paleography=now() where pageNumber=?";
        Connection j=null;
PreparedStatement ps=null;
        try{
            j=DatabaseWrapper.getConnection();
            ps=j.prepareStatement(query);
            ps.setString(1, archive);
            ResultSet rs=ps.executeQuery();
            ps=j.prepareStatement(update);
            while(rs.next())
            {
                Folio f=new Folio(rs.getInt("pageNumber"));
                System.out.print(f.getFolioNumber()+"\n");
                BufferedImage img = ImageHelpers.readAsBufferedImage(new URL(Folio.getRbTok("SERVERCONTEXT") + Folio.getImageURL(rs.getInt("pageNumber"))+"&code="+Folio.getRbTok("imageCode")));//ImageCache.getImage(f.getFolioNumber());//
                detectimages.imageProcessor proc=new imageProcessor(img,2000);
                Manuscript ms=new Manuscript(f.getFolioNumber());
                System.out.print(Folio.getRbTok("PalographyDataDir")+"/"+ms.getID()+"/\n");
                File outdir=new File(Folio.getRbTok("PalographyDataDir")+"/"+ms.getID()+"/");
                if(!outdir.exists())
                    outdir.mkdirs();
                proc.setDataPath(Folio.getRbTok("PalographyDataDir")+"/"+ms.getID()+"/");
                proc.run2(""+f.getFolioNumber()+".txt");
                ps.setInt(1, f.getFolioNumber());
                ps.execute();
            }
        }
        catch (IOException ex) {
            Logger.getLogger(ImageAnalyzer.class.getName()).log(Level.SEVERE, null, ex);
        }        finally{
DatabaseWrapper.closeDBConnection(j);
DatabaseWrapper.closePreparedStatement(ps);
        }
    }
public static void main(String [] args)
    {
        try {
            if(args.length==1)
                new ImageAnalyzer(args[0]);
            
            new ImageAnalyzer();
            
        } catch (SQLException ex) {
            Logger.getLogger(ImageAnalyzer.class.getName()).log(Level.SEVERE, null, ex);
        } catch (MalformedURLException ex) {
            Logger.getLogger(ImageAnalyzer.class.getName()).log(Level.SEVERE, null, ex);
        }
}
}
