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

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.OutputStream;
import java.sql.SQLException;
import javax.imageio.ImageIO;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import textdisplay.Folio;

/**
 *
 * @author jdeerin1
 */
public class PageImage extends HttpServlet {
   
   /** 
    * Handles the HTTP <code>GET</code> method.
    *
    * @param request servlet request
    * @param response servlet response
    * @throws ServletException if a servlet-specific error occurs
    * @throws IOException if an I/O error occurs
    */
   @Override
   protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
      try (OutputStream os = response.getOutputStream()) {
         String folioParam = request.getParameter("folio");
         if (folioParam != null) {
            Folio f = new Folio(Integer.parseInt(folioParam));
            BufferedImage img = f.loadLocalImage();
            if (img != null) {
               response.setContentType("image/jpeg");
               ImageIO.write(img, "jpg", os);
            } else {
               response.sendError(400, "Unknown image archive");
            }
         } else {
            response.sendError(400, "Missing \"folio=\" parameter");
         }
      } catch(SQLException e) {
         response.sendError(503);
      }
   } 

   /** 
    * Returns a short description of the servlet.
    * @return a String containing servlet description
    */
   @Override
   public String getServletInfo() {
      return "T-PEN Page Image Servlet";
   }
}
