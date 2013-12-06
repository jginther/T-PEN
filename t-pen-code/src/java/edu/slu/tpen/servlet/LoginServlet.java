/*
 * Copyright 2013 Saint Louis University. Licensed under the
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
 */
package edu.slu.tpen.servlet;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import static edu.slu.util.ServletUtils.reportInternalError;
import user.User;


/**
 * Servlet to retrieve information about all a user's projects.
 *
 * @author tarkvara
 */
public class LoginServlet extends HttpServlet {

   /**
    * Handles the HTTP <code>POST</code> method by logging in using the given credentials.
    *
    * @param req servlet request
    * @param resp servlet response
    * @throws ServletException if a servlet-specific error occurs
    * @throws IOException if an I/O error occurs
    */
   @Override
   protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
      try {
         User u = new User(req.getParameter("uname"), req.getParameter("password"));
         if (u.getUID() > 0) {
            HttpSession sess = req.getSession(true);
            sess.setAttribute("UID", u.getUID());
         } else {
            resp.sendError(HttpServletResponse.SC_UNAUTHORIZED);
         }
      } catch (NoSuchAlgorithmException ex) {
         reportInternalError(resp, ex);
      }
   }

   /**
    * Returns a short description of the servlet.
    *
    * @return a String containing servlet description
    */
   @Override
   public String getServletInfo() {
      return "T-PEN Login Servlet";
   }
}
