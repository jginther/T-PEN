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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import static edu.slu.util.ServletUtils.*;
import textdisplay.Project;
import user.Group;
import user.User;

/**
 * Servlet which returns a JSON list of witness IDs.
 *
 * @author tarkvara
 */
public class WitnessesServlet extends HttpServlet {

   /**
    * Handles the HTTP <code>GET</code> method, returning a list of witness URIs which can be
    * passed to the JsonLDExportServlet.
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
               resp.setContentType("application/json");
               Project[] projs = Project.getAllProjects();
               List<Map<String, Object>> result = new ArrayList<>();
               for (Project p: projs) {
                  if (new Group(p.getGroupID()).isMember(u.getUID())) {
                     result.add(makeQuickMap(p));
                  }
               }
               ObjectMapper mapper = new ObjectMapper();
               mapper.writeValue(resp.getOutputStream(), result);
            } else {
               resp.sendError(HttpServletResponse.SC_UNAUTHORIZED);
            }
         } catch (SQLException | IOException ex) {
            reportInternalError(resp, ex);
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
      return "Witness List Servlet";
   }
   
   private static Map<String, Object> makeQuickMap(Project p) throws SQLException {
      Map<String, Object> result = new LinkedHashMap<>();
      result.put("id", p.getProjectID());
      result.put("name", p.getProjectName());
      return result;
   }
}
