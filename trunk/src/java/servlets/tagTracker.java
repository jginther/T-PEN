/*
 * @author Jon Deering
Copyright 2011 Saint Louis University. Licensed under the Educational Community License, Version 2.0 (the "License"); you may not use
this file except in compliance with the License.

You may obtain a copy of the License at http://www.osedu.org/licenses/ECL-2.0

Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions
and limitations under the License.
 */
package servlets;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.SQLException;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import textdisplay.Project;
import utils.OpenTagTracker;

/**
 *
 * Allows the transcription UI to keep track of open xml tags and remind the user to close them.
 */
public class tagTracker extends HttpServlet {
   
    /** 
     * Processes requests for both HTTP <code>GET</code> and <code>POST</code> methods.
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
    throws ServletException, IOException, SQLException {
        response.setContentType("text/html;charset=UTF-8");
        PrintWriter out = response.getWriter();
        try {
            int projectID=0;

        if(request.getParameter("projectID")!=null)
        {
            projectID=Integer.parseInt(request.getParameter("projectID"));
        }
            Project thisProject=new Project(projectID);
        if(request.getParameter("addTag")!=null)
        {
            //request to add a tag to the open tag list
            OpenTagTracker t=new OpenTagTracker(thisProject);
            String tag=request.getParameter("tag");
            int folio=Integer.parseInt(request.getParameter("folio"));
            int line=Integer.parseInt(request.getParameter("line"));
            int idNo=t.addTag(tag,folio,line);
            out.print(""+idNo);
            return;
        }
        //request to remove a tag from the tag list
        if(request.getParameter("removeTag")!=null)
        {
            OpenTagTracker t=new OpenTagTracker(thisProject);
            try{
            t.removeTag(Integer.parseInt(request.getParameter("id")));
            //if the id wasnt provided, delete nothing and alert the browser that nothing was deleted.
            }catch(NumberFormatException e)
            {
                out.print("failure");
                return;
            }
            out.print("success");
            return;
        }
        //request to list all tags after folio in the Project
        if(request.getParameter("listTags")!=null)
        {
            int folio=0;
            if(request.getParameter("folio")!=null)
            {
                folio=Integer.parseInt(request.getParameter("folio"));
            }
            OpenTagTracker t=new OpenTagTracker(thisProject);
            Vector<String[]> tags=t.getTagsAfterFolio(folio);
            for(int i=0;i<tags.size();i++)
            {
                String [] tagElements=tags.get(i);
                out.print(tagElements[0]+","+tagElements[1]+","+tagElements[2]+","+tagElements[3]+"\n");
            }
            return;

        }
            if(request.getParameter("checkTags")!=null)
            {
                int folio=0;
            if(request.getParameter("folio")!=null)
            {
                folio=Integer.parseInt(request.getParameter("folio"));
            }
                OpenTagTracker t=new OpenTagTracker(thisProject);
                if(t.checkValidity(folio))
                {
                    out.print("Changed");
                }
                return;
            }

        } finally { 
            out.close();
        }
    } 

    // <editor-fold defaultstate="collapsed" desc="HttpServlet methods. Click on the + sign on the left to edit the code.">
    /** 
     * Handles the HTTP <code>GET</code> method.
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
    throws ServletException, IOException {
        try {
            processRequest(request, response);
        } catch (SQLException ex) {
            Logger.getLogger(tagTracker.class.getName()).log(Level.SEVERE, null, ex);
        }
    } 

    /** 
     * Handles the HTTP <code>POST</code> method.
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
    throws ServletException, IOException {
        try {
            processRequest(request, response);
        } catch (SQLException ex) {
            Logger.getLogger(tagTracker.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /** 
     * Returns a short description of the servlet.
     * @return a String containing servlet description
     */
    @Override
    public String getServletInfo() {
        return "Short description";
    }// </editor-fold>

}
