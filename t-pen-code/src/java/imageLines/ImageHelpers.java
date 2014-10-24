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

import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Transparency;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.awt.image.renderable.ParameterBlock;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import javax.media.jai.Histogram;
import javax.media.jai.JAI;
import javax.media.jai.PlanarImage;
import org.apache.http.HttpResponse;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpHead;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import static edu.slu.util.ImageUtils.convertToGreyscale;
import javax.media.jai.RenderedOp;


/**
 * Provides a bunch of helper functions for dealing with BufferedImages.
 */
public class ImageHelpers {

   /**
    * Scale the image to have the specified height and width in pixels.
    */
   public static BufferedImage scale(BufferedImage img, int height, int width) {
      BufferedImage bdest = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
      Graphics2D g = bdest.createGraphics();
      AffineTransform at = AffineTransform.getScaleInstance((double)width / img.getWidth(),
              (double)height / img.getHeight());
      g.drawRenderedImage(img, at);
      g.dispose();
      return bdest;
   }

   /**
    * Scale the image to have the specified height in pixels, maintaining aspect ratio
    */
   public static BufferedImage scale(BufferedImage img, int height) {
      double scale = (double)height / img.getHeight();
      int width = (int)(img.getWidth() * scale);
      return scale(img, height, width);
   }

   public static BufferedImage getScaledInstance(BufferedImage img,
           int targetWidth,
           int targetHeight,
           Object hint,
           boolean higherQuality) {
      int type = (img.getTransparency() == Transparency.OPAQUE)
              ? BufferedImage.TYPE_INT_RGB : BufferedImage.TYPE_INT_ARGB;
      BufferedImage ret = (BufferedImage) img;
      int w, h;
      if (higherQuality) {
         // Use multi-step technique: start with original size, then
         // scale down in multiple passes with drawImage()
         // until the target size is reached
         w = img.getWidth();
         h = img.getHeight();
      } else {
         // Use one-step technique: scale directly from original
         // size to target size with a single drawImage() call
         w = targetWidth;
         h = targetHeight;
      }

      do {
         if (higherQuality && w > targetWidth) {
            w /= 2;
            if (w < targetWidth) {
               w = targetWidth;
            }
         }

         if (higherQuality && h > targetHeight) {
            h /= 2;
            if (h < targetHeight) {
               h = targetHeight;
            }
         }

         BufferedImage tmp = new BufferedImage(w, h, type);
         Graphics2D g2 = tmp.createGraphics();
         g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, hint);
         g2.drawImage(ret, 0, 0, w, h, null);
         g2.dispose();

         ret = tmp;
      } while (w != targetWidth || h != targetHeight);

      return ret;
   }

   /**
    * Perform a binary thresholding on the image using 1 of 5 methods 0-IterativeThreshold
    * 1-MaxEntropyThreshold 2-Maximum Variance 3-Minimum Error 4-Minimum Fuzziness
    */
   public static BufferedImage binaryThreshold(BufferedImage img, int method) {
//      img = readAsBufferedImage("/Users/zig/Pictures/Aivazovsky_-_Reval_1845.jpg");
//      img = readAsBufferedImage("/Users/zig/Downloads/imageResize.jpg");
      ParameterBlock pb = new ParameterBlock();
      pb.addSource(img);
      pb.add(RGB_COMPONENT_MATRIX);
      RenderedOp greyImage = JAI.create("bandcombine", pb);
      
      pb = new ParameterBlock();
      pb.addSource(greyImage);
      pb.add(null); // region of interest
      pb.add(1);
      pb.add(1);
      pb.add(new int[] { 256 });
      pb.add(new double[] { 0.0 });
      pb.add(new double[] { 256.0 });

      // Calculate the histogram of the image.
      PlanarImage histImage = JAI.create("histogram", pb);
      Histogram h = (Histogram)histImage.getProperty("histogram");
      // Calculate the thresholds based on the selected method.
      double[] thresholds = null;

      switch (method) {
         case 0: // Iterative Bisection
            thresholds = h.getIterativeThreshold();
            break;
         case 1: // Maximum Entropy
            thresholds = h.getMaxEntropyThreshold();
            break;
         case 2: // Maximum Variance
            thresholds = h.getMaxVarianceThreshold();
            break;
         case 3: // Minimum Error
            thresholds = h.getMinErrorThreshold();
            break;
         case 4: // Minimum Fuzziness
            thresholds = h.getMinFuzzinessThreshold();
            break;

      }
      int threshold = (int) thresholds[0];
      BufferedImage bin = binarize(threshold, greyImage);
      return bin;
   }

   /**
    * Binarize a planar image. Only used internally.
    */
   private static BufferedImage binarize(int threshold, PlanarImage image) {
      // Binarizes the original image.
      if (threshold > 5) {
         threshold -= 3;
      }
      ParameterBlock pb = new ParameterBlock();
      pb.addSource(image);
      pb.add(1.0 * threshold);
      // Creates a new, thresholded image and uses it on the DisplayJAI component
      PlanarImage thresholdedImage = JAI.create("binarize", pb);
      return thresholdedImage.getAsBufferedImage();
   }

   /**
    * Read an image from a file into a BufferedImage
    */
   public static BufferedImage readAsBufferedImage(String filename) {
      FileInputStream fis = null;
      try {
         fis = new FileInputStream(filename);
         BufferedImage bi = ImageIO.read(fis);
         return bi;
      } catch (Exception e) {

         System.out.println(e);
         return null;
      } finally {
         try {
            if (fis != null) {
               fis.close();
            }
         } catch (IOException ex) {
         }
      }
   }

   public static BufferedImage readAsBufferedImage(FileInputStream i) {
      try {
         //FileInputStream fis = new FileInputStream(filename);
         BufferedImage bi = ImageIO.read(i);
         return bi;
      } catch (Exception e) {
         System.out.println(e);
         return null;
      }
   }

   /**
    * Read a jpeg image from a url into a BufferedImage
    */
   public static BufferedImage readAsBufferedImage(URL imageURL) {
      InputStream i = null;
      try {
         URLConnection u = imageURL.openConnection();
         u.setRequestProperty("User-Agent", "Mozilla/5.0 (X11; Linux i686) AppleWebKit/535.19 (KHTML, like Gecko) Chrome/18.0.1025.151 Safari/535.19");
         if (u instanceof HttpURLConnection) {
            HttpURLConnection h = (HttpURLConnection) u;
            if (h.getResponseCode() == 403) {
               throw new Exception("403 forbidden");
            }
         }

         i = u.getInputStream();


         BufferedImage bi = ImageIO.read(i);
         return bi;
      } catch (Exception e) {
         System.out.println(e + " for " + imageURL.toString() + "\n");
         return null;
      } finally {
         try {
            i.close();
         } catch (IOException ex) {
         }
      }
   }

   /**
    * Read a jpeg image from a url into a BufferedImage
    */
   public static BufferedImage readAsBufferedImage(URL imageURL, String cookieVal) {
      InputStream i = null;
      try {
         HttpURLConnection conn = (HttpURLConnection) imageURL.openConnection();

         conn.setRequestProperty("Cookie", cookieVal);
         int responseCode = conn.getResponseCode();

         if (responseCode == 200 || responseCode == 304) {
            System.out.print("good response" + responseCode + "\n");

         } else {
            System.out.print("bad response " + responseCode + "\n");

         }

         i = conn.getInputStream();
         //JPEGImageDecoder decoder = JPEGCodec.createJPEGDecoder(i);
         BufferedImage bi = ImageIO.read(i);//decoder.decodeAsBufferedImage();
         return bi;
      } catch (Exception e) {
         System.out.println(e);
         return null;
      } finally {
         try {
            if (i != null) {
               i.close();
            }
         } catch (IOException ex) {
         }
      }
   }

   public static Boolean checkImageHeader(String imageURLString, String uname, String pass) throws ClientProtocolException {


      /*
       URLConnection conn=imageURL.openConnection();
       String userPassword=user+":"+ pass;
       String encoding = new sun.misc.BASE64Encoder().encode (userPassword.getBytes());
       conn.setRequestProperty ("Authorization", "Basic " + encoding);*/
      DefaultHttpClient httpclient = new DefaultHttpClient();

      ResponseHandler<String> responseHandler = new BasicResponseHandler();

      httpclient.getCredentialsProvider().setCredentials(
              new AuthScope(AuthScope.ANY),
              new UsernamePasswordCredentials(uname, pass));
      HttpHead head = new HttpHead(imageURLString);
      //HttpGet httpget = new HttpGet(imageURLString);
      System.out.println("executing head request" + head.getRequestLine());

      HttpResponse response;
      try {
         response = httpclient.execute(head);
         int code = response.getStatusLine().getStatusCode();
         if (code == 200 || code == 304) {
            return true;
         }
      } catch (IOException ex) {
         Logger.getLogger(ImageHelpers.class.getName()).log(Level.SEVERE, null, ex);
      }
      return false;
   }

   
   /**
    * reflect the image over the horizontal axis. This is useful when the border around the image is on the
    * right side and very dark, as this can cause issues when detecting lines.
    */
   public static BufferedImage flipHorizontal(BufferedImage image) {
      PlanarImage j = PlanarImage.wrapRenderedImage(image);
      j = (PlanarImage) JAI.create("transpose", j, javax.media.jai.operator.TransposeDescriptor.FLIP_HORIZONTAL);
      return j.getAsBufferedImage();
   }

   private static final double[][] RGB_COMPONENT_MATRIX = {{0.114, 0.587, 0.299, 0}};

   private static final Logger LOG = Logger.getLogger(ImageHelpers.class.getName());
}
