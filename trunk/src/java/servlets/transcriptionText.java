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

import com.lowagie.text.DocumentException;
import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import textdisplay.Manuscript;
import textdisplay.Project;
import textdisplay.TagFilter;
import textdisplay.transcriptionPage;
import user.User;

/**
 *Old servlet for outputting transcription in PDF format. No longer used.
 * 
 */
public class transcriptionText extends HttpServlet {
   
    /** 
     * Processes requests for both HTTP <code>GET</code> and <code>POST</code> methods.
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
    throws ServletException, IOException, SQLException, FileNotFoundException, DocumentException {
        response.setContentType("text/html;charset=UTF-8");
        PrintWriter out = response.getWriter();
       
            //build the transcription either as entered (possibly including markup) or without markup(strip all tags)
            
                
            if(request.getParameter("uid")!=null && request.getParameter("pageNum")!=null)
            {
            HttpSession session=request.getSession();
            int uid=Integer.parseInt(request.getParameter("uid"));
            if(session.getAttribute("UID")==null || session.getAttribute("UID").toString().compareTo(""+uid)!=0)
            {
                response.sendError(response.SC_FORBIDDEN);
                return;
            }
            int projectID=0;
            if(request.getParameter("projectID")!=null)
            {
                projectID=Integer.parseInt(request.getParameter("projectID"));
            }
            //response.setContentType("application/pdf");
            int pageNum=Integer.parseInt(request.getParameter("pageNum"));
            transcriptionPage thisPage;
            Boolean includeNotes=false;
            if(request.getParameter("notes")!=null)
            {
                includeNotes=true;
            }
            if(projectID>0)
            {
                Manuscript ms=new Manuscript(pageNum);
                out.print(ms.getFullDocument(new Project(projectID),TagFilter.noteStyles.inline,true,false,false));
            }
            else
            {
                Manuscript ms=new Manuscript(pageNum);
                out.print(ms.getFullDocument(new User(uid)));
            }


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
    throws ServletException, IOException, FileNotFoundException {
        try
            {
            processRequest(request, response);
            } catch (SQLException ex)
            {
            Logger.getLogger(transcriptionText.class.getName()).log(Level.SEVERE, null, ex);
            } catch (DocumentException ex)
            {
            Logger.getLogger(transcriptionText.class.getName()).log(Level.SEVERE, null, ex);
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
        try
            {
            processRequest(request, response);
            } catch (SQLException ex)
            {
            Logger.getLogger(transcriptionText.class.getName()).log(Level.SEVERE, null, ex);
            } catch (FileNotFoundException ex)
            {
            Logger.getLogger(transcriptionText.class.getName()).log(Level.SEVERE, null, ex);
            } catch (DocumentException ex)
            {
            Logger.getLogger(transcriptionText.class.getName()).log(Level.SEVERE, null, ex);
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
