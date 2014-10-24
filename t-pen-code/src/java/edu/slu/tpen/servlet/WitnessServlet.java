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
import java.sql.SQLException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import edu.slu.tpen.transfer.JsonImporter;
import edu.slu.tpen.transfer.JsonLDExporter;
import static edu.slu.util.ServletUtils.*;
import textdisplay.Project;
import user.Group;
import user.User;


/**
 * Servlet which is responsible for exporting the requested witness as JSON-LD.
 *
 * @author tarkvara
 */
public class WitnessServlet extends HttpServlet {

   /**
    * Handles the HTTP <code>GET</code> method, returning a JSON-LD serialisation of the
    * requested T-PEN project.
    *
    * @param req servlet request
    * @param resp servlet response
    * @throws ServletException if a servlet-specific error occurs
    * @throws IOException if an I/O error occurs
    */
   @Override
   protected void doGet(HttpServletRequest req, HttpServletResponse resp)
           throws ServletException, IOException {
      if (verifyHostInList(req.getRemoteHost(), "TRADAMUS")) {
         try {
            User u = new User(req.getParameter("user"));
            if (u.getUID() >= 0) {
               try {
                  int projID = Integer.parseInt(req.getPathInfo().substring(1));
                  Project proj = new Project(projID);
                  if (proj.getProjectID() > 0) {
                     if (new Group(proj.getGroupID()).isMember(u.getUID())) {
                        resp.setContentType("application/ld+json; charset=UTF-8");
								resp.getWriter().write(new JsonLDExporter(proj).export());
								resp.setStatus(HttpServletResponse.SC_OK);
                        resp.setStatus(HttpServletResponse.SC_OK);
                     } else {
                        resp.sendError(HttpServletResponse.SC_UNAUTHORIZED);
                     }
                  } else {
                     resp.sendError(HttpServletResponse.SC_NOT_FOUND);
                  }
               } catch (NumberFormatException | SQLException | IOException ex) {
                  throw new ServletException(ex);
               }
            } else {
               resp.sendError(HttpServletResponse.SC_UNAUTHORIZED);
            }
         } catch (SQLException ex) {
            throw new ServletException(ex);
         }
      } else {
         resp.sendError(HttpServletResponse.SC_FORBIDDEN);
      }
   }

   /**
    * Handles the HTTP <code>PUT</code> method, updating a project from a plain JSON serialisation.
    * 
    *
    * @param req servlet request
    * @param resp servlet response
    * @throws ServletException if a servlet-specific error occurs
    * @throws IOException if an I/O error occurs
    */
   @Override
   protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
      if (verifyHostInList(req.getRemoteHost(), "TRADAMUS")) {
         try {
            User u = new User(req.getParameter("user"));
            if (u.getUID() >= 0) {
               try {
                  int projID = Integer.parseInt(req.getPathInfo().substring(1));
                  Project proj = new Project(projID);
                  if (proj.getProjectID() > 0) {
                     if (new Group(proj.getGroupID()).isMember(u.getUID())) {
                        if (getBaseContentType(req).equals("application/json")) {
                           new JsonImporter(proj, u).update(req.getInputStream());
                           resp.setStatus(HttpServletResponse.SC_OK);
                        } else {
                           resp.sendError(HttpServletResponse.SC_UNSUPPORTED_MEDIA_TYPE, "Expecting application/json");
                        }
                     } else {
                        resp.sendError(HttpServletResponse.SC_UNAUTHORIZED);
                     }
                  } else {
                     resp.sendError(HttpServletResponse.SC_NOT_FOUND);
                  }
               } catch (NumberFormatException | SQLException | IOException ex) {
                  reportInternalError(resp, ex);
               }
            } else {
               resp.sendError(HttpServletResponse.SC_UNAUTHORIZED);
            }
         } catch (SQLException ex) {
            throw new ServletException(ex);
         }
      } else {
         resp.sendError(HttpServletResponse.SC_FORBIDDEN);
      }
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public String getServletInfo() {
      return "JSON Transfer Servlet";
   }
}
