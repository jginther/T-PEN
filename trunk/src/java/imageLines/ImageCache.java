/*
 * @author Jon Deering
Copyright 2011 Saint Louis University. Licensed under the Educational Community License, Version 2.0 (the "License"); you may not use
this file except in compliance with the License.

You may obtain a copy of the License at http://www.osedu.org/licenses/ECL-2.0

Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions
and limitations under the License.
 */

package imageLines;


import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import textdisplay.DatabaseWrapper;
/**This class manages the caching of images for TPEN, so we arent constantly hitting the image host for the same image. Additionally, the paleographic analysis displays only
 display content from cached images, because the results often span dozens or hundreds of images, and fetching those from the remote host would be very unkind*/
public class ImageCache {
    /**
     * The max size for the cache, when it approaches this size things get dropped out.
     */
    public static int size=10000;
    /**
     * All of the methods for this class are static
     */
    public ImageCache()
{
    
}
/**
 * Attempt to fetch an image from the cache
 * @param folio the associated folio number
 * @return the image, or null if we couldnt get it
 * @throws SQLException
 * @throws IOException
 */
public static BufferedImage getImage(int folio) throws SQLException, IOException
{
    String query="select folio from imageCache where folio=?";
    Connection j=null;
PreparedStatement ps=null;
PreparedStatement updateHitCount=null;
    try{
        j=DatabaseWrapper.getConnection();
        ps=j.prepareStatement(query);
        ps.setInt(1, folio);
        ResultSet rs=ps.executeQuery();
        if(rs.next())
        {
            updateHitCount=j.prepareStatement("update imageCache set count=count+1 where folio=?");
            updateHitCount.setInt(1, folio);
            updateHitCount.execute();
            
            //the image is in the cache
            query="select image from imageCache where folio=?";
            ps=j.prepareStatement(query);
            ps.setInt(1, folio);
            rs=ps.executeQuery();
            if(rs.next())
            {
            InputStream fis=rs.getBinaryStream(1);
            BufferedImage bi= ImageIO.read(fis);
                 return bi;
            }
            else return null;
        }
    }
    
    finally{
DatabaseWrapper.closeDBConnection(j);
DatabaseWrapper.closePreparedStatement(ps);
DatabaseWrapper.closePreparedStatement(updateHitCount);
    }
    return null;
}

/**
 * Check to see if an image is present in the cache. Very useful for estimating how long it will take to deliver to the client.
 * @param folio the associated folio number
 * @return true if it is in the cache. doesnt guarntee a fetch will work, the item could be removed from the cache, so be aware.
 * @throws SQLException
 * @throws IOException
 */
public static Boolean hasImage(int folio) throws SQLException, IOException
{
    String query="select folio from imageCache where folio=?";
    Connection j=null;
PreparedStatement ps=null;
    try{
        j=DatabaseWrapper.getConnection();
        ps=j.prepareStatement(query);
        ps.setInt(1, folio);
        ResultSet rs=ps.executeQuery();
        if(rs.next())
        {
           return true;
        }
    }

    finally{
DatabaseWrapper.closeDBConnection(j);
DatabaseWrapper.closePreparedStatement(ps);
    }
    return false;
}


/**
 * Remove images beyond the permitted number
 * @throws SQLException
 * @throws IOException
 */
public static void cleanup() throws SQLException, IOException
{
    String query1="select max(id) from imageCache";
    String query="delete from imageCache where id<?";
    Connection j=null;
PreparedStatement ps=null;
    try{
        j=DatabaseWrapper.getConnection();
        ps=j.prepareStatement(query1);
        ResultSet rs=ps.executeQuery();
        if(rs.next())
        {
        int max=rs.getInt(1);
        ps=j.prepareStatement(query);
        //because the ids are sequential, if the max id is 15000 and size is 10000, delete any id <5000
        ps.setInt(1, max-size);
        ps.execute();
        }
    }
    finally{
DatabaseWrapper.closeDBConnection(j);
DatabaseWrapper.closePreparedStatement(ps);
    }
}


/**
 * Add an image to the cache
 * @param folio the folio number for it
 * @param img the image iteself, expect 2000 pixel height rgb
 * @throws SQLException
 * @throws IOException
 */
public static void setImage(int folio, BufferedImage img) throws SQLException, IOException
{

    if(!ImageCache.hasImage(folio)){
    String query="insert into imageCache (folio, image) values(?,?)";
    Connection j=null;
PreparedStatement ps=null;
    try{
        j=DatabaseWrapper.getConnection();
        ps=j.prepareStatement(query);
        //write the image to a binary array to send to the DB
        ByteArrayOutputStream baos = new ByteArrayOutputStream( 1000 );
        ImageIO.write( img, "jpeg", baos );


baos.flush();
byte[] result = baos.toByteArray();
ByteArrayInputStream b=new ByteArrayInputStream(result);

        ps.setInt(1, folio);
        ps.setBytes(2, result);

        ps.execute();
}
    catch(Exception e)
    {
         Logger.getLogger(ImageCache.class.getName()).log(Level.SEVERE, null, e);
    }
DatabaseWrapper.closeDBConnection(j);
DatabaseWrapper.closePreparedStatement(ps);

}
    ImageCache.cleanup();
}
}
