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
package imageLines;

import java.awt.Dimension;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import javax.imageio.ImageIO;
import edu.slu.util.ImageUtils;
import textdisplay.DatabaseWrapper;

/**
 * This class manages the caching of images for TPEN, so we arent constantly hitting the image host for the
 * same image. Additionally, the paleographic analysis displays only display content from cached images,
 * because the results often span dozens or hundreds of images, and fetching those from the remote host
 * would be very unkind
 */
public class ImageCache {

   /**
    * The max size for the cache, when it approaches this size, things get dropped out.
    */
   public static final int CACHE_SIZE = 10000;

   /**
    * All of the methods for this class are static
    */
   private ImageCache() {
   }

   /**
    * Attempt to fetch an image from the cache.
    *
    * @param folioNum the associated folio
    * @return the image, or null if we couldnt get it
    * @throws SQLException
    * @throws IOException
    */
   public static BufferedImage getImage(int folioNum) throws SQLException, IOException {
		try (InputStream stream = getImageStream(folioNum)) {
			return stream != null ? ImageIO.read(stream) : null;
		}
   }
   
   public static InputStream getImageStream(int folioNum) throws SQLException, IOException {
      try (Connection j = DatabaseWrapper.getConnection()) {
         try (PreparedStatement ps = j.prepareStatement("select image from imageCache where folio=?")) {
            ps.setInt(1, folioNum);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
               // The image is in the cache
               try (PreparedStatement updateHitCount = j.prepareStatement("update imageCache set count=count+1 where folio=?")) {
                  updateHitCount.setInt(1, folioNum);
                  updateHitCount.execute();
               }

               return rs.getBinaryStream(1);
            } else {
               return null;
            }
         }
      }
   }

  /**
    * Attempt to fetch an image from the cache
    *
    * @param folioNum the associated folio number
    * @return the image, or null if we couldnt get it
    * @throws SQLException
    * @throws IOException
    */
   public static Dimension getImageDimension(int folioNum) throws SQLException, IOException {
      try (Connection j = DatabaseWrapper.getConnection()) {
         try (PreparedStatement ps = j.prepareStatement("select image from imageCache where folio = ?")) {
            ps.setInt(1, folioNum);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
               try (InputStream input = rs.getBinaryStream(1)) {
                  return ImageUtils.getJPEGDimension(input);
               }
            }
         }
      }
      return null;
   }

   /**
    * Remove images beyond the permitted number.
    *
    * @throws SQLException
    * @throws IOException
    */
   private static void cleanup(Connection j) throws SQLException {
      try (PreparedStatement ps = j.prepareStatement("SELECT MAX(id) FROM imageCache")) {
         ResultSet rs = ps.executeQuery();
         if (rs.next()) {
            int maxID = rs.getInt(1);
            try (PreparedStatement ps2 = j.prepareStatement("DELETE FROM imageCache WHERE id < ?")) {
               ps2.setInt(1, maxID - CACHE_SIZE);
               ps2.executeUpdate();
            }
         }
      }
   }

   /**
    * Add an image to the cache
    *
    * @param folioNum the folio number for it
    * @param img the image itself, expect 2000 pixel height rgb
    * @throws SQLException
    * @throws IOException
    */
   public static void setImage(int folioNum, BufferedImage img) throws SQLException, IOException {

      try (Connection j = DatabaseWrapper.getConnection()) {
         try (PreparedStatement ps = j.prepareStatement("select folio from imageCache where folio=?")) {
            ps.setInt(1, folioNum);
            if (!ps.executeQuery().next()) {
               // Not already in cache, so set up the insertion.
               try (PreparedStatement ps2 = j.prepareStatement("insert into imageCache (folio, image) values(?,?)")) {
                  // Write the image to a binary array to send to the DB
                  ByteArrayOutputStream baos = new ByteArrayOutputStream(1000);
                  ImageIO.write(img, "jpg", baos);
                  baos.flush();

                  byte[] result = baos.toByteArray();

                  ps2.setInt(1, folioNum);
                  ps2.setBytes(2, result);

                  ps2.executeUpdate();
               }
            }
         }
         cleanup(j);
      }
   }
}
