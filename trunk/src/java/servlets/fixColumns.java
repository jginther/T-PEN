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
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import textdisplay.Folio;
import textdisplay.Project;
import user.Group;

public class fixColumns extends HttpServlet {

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
        int UID = 0;
        HttpSession session=request.getSession();
    if (session.getAttribute("UID") == null) {
        response.sendError(403);
        return;
    } else {
    UID = Integer.parseInt(session.getAttribute("UID").toString());
    String url=request.getRequestURI();
    user.User thisUser = new user.User(UID);
        int projectID=Integer.parseInt(request.getParameter("projectID"));
        int folioNum=Integer.parseInt(request.getParameter("folioNum"));
        Project thisProject=new Project(projectID);
        Group g=new user.Group(thisProject.getGroupID());
        if(false && !(g.isMember(UID)))
        {
            response.sendError(403);
            return;
        }
            int[] lines = new int[999];
        int[] lines2 = new int[999];
        int[] lines3 = new int[999];
        int[] lines4 = new int[999];
        int ctr = 0;//how many actual values are in here
        for (int i = 0; i < 999; i++) {
            if (request.getParameter("t" + i) != null) {
                try {
                    int decimalPoint=request.getParameter("t" + i).indexOf('.');
                                int tmp;
                                if(decimalPoint>0)
                                {
                                    tmp= Integer.parseInt(request.getParameter("t" + i).substring(0,decimalPoint));
                                }
                                else
                                {
                                tmp= Integer.parseInt(request.getParameter("t" + i));
                                }
                                int tmp2;
                                decimalPoint=request.getParameter("l" + i).indexOf('.');
                                if(decimalPoint>0)
                                {
                                    tmp2= Integer.parseInt(request.getParameter("l" + i).substring(0,decimalPoint));
                                }
                                else
                                {
                                tmp2= Integer.parseInt(request.getParameter("l" + i));
                                }
                                int tmp3;
                                decimalPoint=request.getParameter("w" + i).indexOf('.');
                                if(decimalPoint>0)
                                {
                                    tmp3= Integer.parseInt(request.getParameter("w" + i).substring(0,decimalPoint));
                                }
                                else
                                {
                                tmp3= Integer.parseInt(request.getParameter("w" + i));
                                }
                                int tmp4;
                                decimalPoint=request.getParameter("b" + i).indexOf('.');
                                if(decimalPoint>0)
                                {
                                    tmp4= Integer.parseInt(request.getParameter("b" + i).substring(0,decimalPoint));
                                }
                                else
                                {
                                tmp4= Integer.parseInt(request.getParameter("b" + i));
                                }
                    lines[i] = tmp;
                    lines2[i] = tmp2;
                    lines3[i] = tmp3;
                    lines4[i] = tmp4 ;
                    ctr++;
                } catch (NumberFormatException e) {
                }
            }
        }
      
        try {
            int[] linePositions = new int[ctr + 1];
            int[] linePositions2 = new int[ctr + 1];
            int[] linePositions3 = new int[ctr + 1];
            int[] linePositions4 = new int[ctr + 1];
            int nonSequentialCounter = 0;
            for (int i = 0; i < linePositions.length && nonSequentialCounter < 998; i++) {
                while (lines[nonSequentialCounter] == 0 && lines2[nonSequentialCounter] == 0 & lines3[nonSequentialCounter] == 0 && nonSequentialCounter < 998) {
                    nonSequentialCounter++;
                }
                linePositions[i] = lines[nonSequentialCounter];
                linePositions2[i] = lines2[nonSequentialCounter];
                linePositions3[i] = lines3[nonSequentialCounter];
                linePositions4[i] = lines4[nonSequentialCounter];
                nonSequentialCounter++;
                if (nonSequentialCounter == 998) {
                    break;
                }
            }
textdisplay.Transcription [] t=textdisplay.Transcription.getProjectTranscriptions(projectID, folioNum);
for(int i=0;i<t.length;i++)
    t[i].remove();
            Folio thisFolio = new Folio(folioNum, true);
            
            //Folio thisFolio=new Folio(folioNum,true);
            if (projectID > 0) {
                thisProject.update(linePositions, linePositions2, linePositions3, linePositions4, folioNum);
                textdisplay.Manuscript ms = new textdisplay.Manuscript(folioNum);
                thisProject.addLogEntry("<span class='log_parsing'></span>Corrected line parsing of page  " + ms.getShelfMark() + " " + thisFolio.getPageName(), UID); // ,"parsing"
            }
           thisProject.detectInColumns(thisFolio.getFolioNumber());
           out.print("success");
        } finally {            
            out.close();
     
        }}
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
            Logger.getLogger(fixColumns.class.getName()).log(Level.SEVERE, null, ex);
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
            Logger.getLogger(fixColumns.class.getName()).log(Level.SEVERE, null, ex);
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
