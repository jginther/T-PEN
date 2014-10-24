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
import java.io.InputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import static edu.slu.util.LangUtils.getMessage;
import static edu.slu.util.ServletUtils.reportInternalError;
import textdisplay.Folio;
import textdisplay.Archive;


/**
 * A servlet to resize an image from an Archive to a size requested.
 */
public class ImageResize extends HttpServlet {

   /**
    * Processes requests for both HTTP
    * <code>GET</code> and
    * <code>POST</code> methods.
    *
    * @param milliSeconds
    * @return
    */
   private static String getGMTTimeString(long milliSeconds) {
      SimpleDateFormat sdf = new SimpleDateFormat("E, d MMM yyyy HH:mm:ss 'GMT'");
      return sdf.format(new Date(milliSeconds));
   }

   /**
    * This handles delivering properly sized images to both end users and parts of tpen that need to access
    * images, such as the line parser.
    *
    * @param request
    * @param response
    * @throws ServletException
    * @throws IOException
    * @throws SQLException
    */
   @Override
   protected void doGet(HttpServletRequest request, HttpServletResponse response)
           throws ServletException, IOException {
      try {
         String uidString = null;
         if (request.getSession().getAttribute("UID") != null) {
            uidString = request.getSession().getAttribute("UID").toString();
         }
         if (uidString == null) {
            if (request.getParameter("code") == null) {
               response.sendError(403);
               return;
            }
            if (request.getParameter("code").compareTo(Folio.getRbTok("imageCode")) != 0) {
               response.sendError(403);
               return;
            }
         }
         response.addHeader("Cache-Control", "max-age=3600");
         long relExpiresInMillis = System.currentTimeMillis() + (1000 * 2600);
         response.addHeader("Expires", getGMTTimeString(relExpiresInMillis));
         response.setContentType("image/jpeg");

         if (request.getParameter("folioNum") != null) {
            BufferedImage toResize;
            Iterator iter = ImageIO.getImageWritersByFormatName("jpeg");
            ImageWriter writer = (ImageWriter) iter.next();
   // instantiate an ImageWriteParam object with default compression options
            ImageWriteParam iwp = writer.getDefaultWriteParam();
            iwp.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
            float quality = 0.5f;
            if (request.getParameter("quality") != null) {
               try {
                  int qual = Integer.parseInt(request.getParameter("quality"));
                  quality = qual / 100.0f;
               } catch (NumberFormatException e) {
               }
            }
            iwp.setCompressionQuality(quality);   // an integer between 0 and 1
            int folioNum = Integer.parseInt(request.getParameter("folioNum"));

            ImageRequest ir = null;
            if (uidString != null) {
               try {
                  int uid = Integer.parseInt(uidString);
                  ir = new ImageRequest(folioNum, uid);
               } catch (NumberFormatException | SQLException e) {
               }
            }
            if (ir == null) {
               ir = new ImageRequest(folioNum, 0);
            }

            try {
               toResize = ImageCache.getImage(folioNum);
               if (toResize != null) {
                  LOG.log(Level.INFO, "Loaded image {0} from cache", folioNum);
               } else {
                  LOG.log(Level.INFO, "Cache load failed, loading from source.");
                  Folio f = new Folio(folioNum);
						try (InputStream stream = f.getUncachedImageStream(false)) {
                     toResize = ImageIO.read(stream);
                     LOG.log(Level.INFO, "Loaded {0}", toResize);
                     Archive a = new Archive(f.getArchive());
                     if (!a.getName().equals("private") && a.getConnectionMethod() != Archive.connectionType.local) {
                        LOG.log(Level.INFO, "Adding image {0} to cache", folioNum);
                        ImageCache.setImage(folioNum, toResize);
                     }
                     ir.completeSuccess();
						}
               }

               int height = Integer.parseInt(request.getParameter("height"));
               int width = (int) ((height / (double)toResize.getHeight()) * toResize.getWidth());
               toResize = ImageHelpers.scale(toResize, height, width);

               OutputStream os = response.getOutputStream();
               IIOImage image = new IIOImage(toResize, null, null);
               writer.setOutput(ImageIO.createImageOutputStream(os));
               writer.write(null, image, iwp);
            } catch (SQLException | IOException | IllegalArgumentException ex) {
               ir.completeFail(getMessage(ex));
               reportInternalError(response, ex);
            }
         } else {
            // Folio not provided in URL, throw a 400.
            response.sendError(HttpServletResponse.SC_BAD_REQUEST);
         }
      } catch (SQLException | IllegalArgumentException ex) {
         reportInternalError(response, ex);
      }
   }

   /**
    * Returns a short description of the servlet.
    *
    * @return a String containing servlet description
    */
   @Override
   public String getServletInfo() {
      return "T-PEN Image Resize servlet";
   }
   
   private static final Logger LOG = Logger.getLogger(ImageResize.class.getName());
}
