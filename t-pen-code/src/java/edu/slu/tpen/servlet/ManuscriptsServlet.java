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
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import static edu.slu.util.LangUtils.buildQuickMap;
import static edu.slu.util.ServletUtils.getUID;
import static edu.slu.util.ServletUtils.reportInternalError;
import java.util.HashSet;
import java.util.Set;
import textdisplay.Folio;
import textdisplay.Manuscript;
import textdisplay.Project;
import user.User;


/**
 * Servlet to retrieve information about all a user's projects.
 *
 * @author tarkvara
 */
public class ManuscriptsServlet extends HttpServlet {


   /**
    * Handles the HTTP <code>GET</code> method, returning a list of manuscripts available to the current user.
    *
    * @param req servlet request
    * @param resp servlet response
    * @throws ServletException if a servlet-specific error occurs
    * @throws IOException if an I/O error occurs
    */
   @Override
   protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
      int uid = getUID(req, resp);
      if (uid >= 0) {
         resp.setContentType("application/json");
         try {
            User u = new User(uid);
            Project[] projs = u.getUserProjects();
            Set<Manuscript> mss = new HashSet<>();
            for (Project p: projs) {
               Folio[] folios = p.getFolios();
               for (Folio f: folios) {
                  mss.add(new Manuscript(f.getFolioNumber()));
               }
            }
            List<Map<String, Object>> result = new ArrayList<>();
            for (Manuscript ms: mss) {
               result.add(buildQuickMap("uri", "manuscript/" + ms.getID(), "name", ms.getCollection()));
            }
            ObjectMapper mapper = new ObjectMapper();
            mapper.writeValue(resp.getOutputStream(), result);
         } catch (SQLException ex) {
            reportInternalError(resp, ex);
         }
      }
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public String getServletInfo() {
      return "Manuscript List Servlet";
   }
}
