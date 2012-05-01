/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package imageLines;

import detectimages.Detector;
import java.awt.image.BufferedImage;
import java.net.MalformedURLException;
import java.net.URL;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;
import textdisplay.Archive;
import textdisplay.Folio;

/**
 *
 * Load folios into the DB from either a local or remote repository. local images will be parsed immediately, remote ones will
 * be parsed now or on demand per the Archive's configuration
 */
public class FolioLoader
{/*
   public FolioLoader(String archiveName,String collection, String imageList) throws MalformedURLException, SQLException
    {
       
        Archive a=new Archive(archiveName);
        String [] images=imageList.split(",");
        for(int i=0;i<images.length;i++)
        {
            
                //String collection, String pageName, String imageName, String Archive
                if(a.permitsBatchProcessing())
                {
                
                String baseURL=textdisplay.Archive.getURL(collection, images[i], archiveName);
            URL imageURL=new URL(baseURL);

            BufferedImage img=imageHelpers.readAsBufferedImage(imageURL);
            int width=(int) (img.getWidth() * .5);
            img=imageHelpers.scale(img,1000,width);
            BufferedImage flippedImage=imageHelpers.flipHorizontal(img);
            BufferedImage bin=imageHelpers.binaryThreshold(img, 0);
            Detector myDetector=new Detector(img,bin);
            myDetector.smeared=bin;

            //imageHelpers.writeImage(img, "/usr/web/tosend/img.jpg");
            myDetector.graphical=false;
            myDetector.vsmearDist=15;
            myDetector.hsmearDist=15;
            try{
            myDetector.detect();
            }
            catch (ArithmeticException e)
            {

            }
            detectimages.line [] flipped=myDetector.lines.toArray(new detectimages.line[myDetector.lines.size()]);
            bin=imageHelpers.binaryThreshold(flippedImage, 0);
            myDetector.bin=bin;
            myDetector.img=flippedImage;
            Detector flipDetector=new Detector(flippedImage, bin);
            flipDetector.smeared=bin;

            imageHelpers.writeImage(flippedImage, "/usr/web/tosend/bin.jpg");
            try{
                flipDetector.detect();
            }

            catch (ArithmeticException e)
            {

            }
            imageHelpers.writeImage(bin, "/usr/web/tosend/bin2.jpg");
            if(flipDetector.lines.size()>flipped.length)
                flipped=flipDetector.lines.toArray(new detectimages.line[flipDetector.lines.size()]);

            //imageHelpers.writeImage(bin, "/usr/web/tosend/bin.jpg");

                //before saving the positions of the lines in the image, create the record to indicate the identifying
                //information for this image in the folios table.

                String imageName=textdisplay.Archive.getURL(collection, images[i], archiveName);
                int pageNum=Folio.createFolioRecord(collection, images[i], imageName,archiveName);
                Folio newFolio=new Folio(flipped,pageNum);
                    
                }

                else{

                        int folionum = Folio.createFolioRecord(collection, images[i], images[i], archiveName);
                   
                }

            
        }

    }
   public static void main(String [] args)
   {
       String Archive="ENAP";
       String collection="415_again";
       String imgs="415_231_TC_46.jpg,415_230_TC_46.jpg";
        try {
            FolioLoader l = new FolioLoader(Archive, collection, imgs);
        } catch (MalformedURLException ex) {
            Logger.getLogger(FolioLoader.class.getName()).log(Level.SEVERE, null, ex);
        } catch (SQLException ex) {
            Logger.getLogger(FolioLoader.class.getName()).log(Level.SEVERE, null, ex);
        }
   }*/
}
