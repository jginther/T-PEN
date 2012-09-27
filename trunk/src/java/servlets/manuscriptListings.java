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
import textdisplay.Manuscript;
import user.User;

/**
 *
 * Generate html listing all the manuscripts in the city, repository, or city and repository specified. 
 * Used to populate the ms listing using the city/repository dropdowns.
 */
public class manuscriptListings extends HttpServlet {

    /** 
     * Processes requests for both HTTP <code>GET</code> and <code>POST</code> methods.
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("text/html;charset=UTF-8");
        PrintWriter out = response.getWriter();
        HttpSession session = request.getSession();
        try {
            if (request.getParameter("city") != null) {
                if (request.getParameter("repository") != null) {
                    String city = request.getParameter("city");
                    String repo = request.getParameter("repository");
                    Manuscript[] mss = Manuscript.getManuscriptsByCityAndRepository(city, repo);
                    out.print("<div id=\"count\" class=\"right\">");
                    out.print("<h3>" + mss.length + " total manuscripts available</h3>");
                    out.print("</div><br>");
                    int UID = 0;
                    if (session.getAttribute("UID") != null) {
                        UID = Integer.parseInt(session.getAttribute("UID").toString());
                    }
                    int[] msIDs = new int[0];
                    if (UID > 0) {
                        User u = new User(UID);
                        textdisplay.Project[] p = u.getUserProjects();
                        msIDs = new int[p.length];
                        for (int i = 0; i < p.length; i++) {
                            try {
                                msIDs[i] = new Manuscript(p[i].firstPage()).getID();
                            } catch (Exception e) {
                                msIDs[i] = -1;
                            }
                        }
                    }
                    
                    for (int i = 0; i < mss.length; i++) {
                        out.print(mss[i].getCity() + ", " + mss[i].getRepository() + " " + mss[i].getCollection() + " (hosted by " + mss[i].getArchive() + ")\n");
                        if (mss[i].isRestricted()) {
                            out.print("<span class=\"restricted\">Restricted Access</span>\n");
                        }
                        Boolean resume = false;
                        for (int l = 0; l < msIDs.length; l++) {
                            if (msIDs[l] == mss[i].getID()) {
                                resume = true;
                            }
                        }
                        if (resume) {
                            out.print("<a href=transcription.jsp?ms=" + mss[i].getID() + ">Resume transcribing</a>" + "\n");
                        } else {
                            out.print("(Not marked)<a href=transcription.jsp?ms=" + mss[i].getID() + ">Start transcribing</a>" + "\n");
                        }
                        
                        
                        out.print("<a href=addMStoProject.jsp?ms=" + mss[i].getID() + ">Add to project</a>" + "\n<br>");
                    }
                } else {
                    String city = request.getParameter("city");
                    Manuscript[] mss = Manuscript.getManuscriptsByCity(city);
                    out.print("<div id=\"count\" class=\"right\">");
                    out.print("<h3>" + mss.length + " total manuscripts available</h3>");
                    out.print("</div><br>");
                    int UID = 0;
                    if (session.getAttribute("UID") != null) {
                        UID = Integer.parseInt(session.getAttribute("UID").toString());
                    }
                    
                    int[] msIDs = new int[0];
                    if (UID > 0) {
                        User u = new User(UID);
                        textdisplay.Project[] p = u.getUserProjects();
                        msIDs = new int[p.length];
                        for (int i = 0; i < p.length; i++) {
                            try {
                                msIDs[i] = new Manuscript(p[i].firstPage()).getID();
                            } catch (Exception e) {
                                msIDs[i] = -1;
                            }
                        }
                    }
                    
                    for (int i = 0; i < mss.length; i++) {
                        out.print(mss[i].getCity() + ", " + mss[i].getRepository() + " " + mss[i].getCollection() + " (hosted by " + mss[i].getArchive() + ")\n");
                        if (mss[i].isRestricted()) {
                            out.print("<span class=\"restricted\">Restricted Access</span>\n");
                        }
                        Boolean resume = false;
                        for (int l = 0; l < msIDs.length; l++) {
                            if (msIDs[l] == mss[i].getID()) {
                                resume = true;
                            }
                        }
                        if (resume) {
                            out.print("<a href=transcription.jsp?ms=" + mss[i].getID() + " class=\"resume\">Resume transcribing</a>" + "\n");
                        } else {
                            out.print("<a href=transcription.jsp?ms=" + mss[i].getID() + " class=\"unmarked\">Start transcribing</a>" + "\n");
                        }
                        out.print("<a href=addMStoProject.jsp?ms=" + mss[i].getID() + ">Add to project</a>" + "\n<br>");
                    }
                }
            } else {
                if (request.getParameter("repository") != null) {
                    String repo = request.getParameter("repository");
                    Manuscript[] mss = Manuscript.getManuscriptsByRepository(repo);
                    out.print("<div id=\"count\" class=\"right\">");
                    out.print("<h3>" + mss.length + " total manuscripts available</h3>");
                    out.print("</div><br>");
                    int UID = 0;
                    if (session.getAttribute("UID") != null) {
                        UID = Integer.parseInt(session.getAttribute("UID").toString());
                    }
                    int[] msIDs = new int[0];
                    if (UID > 0) {
                        User u = new User(UID);
                        textdisplay.Project[] p = u.getUserProjects();
                        msIDs = new int[p.length];
                        for (int i = 0; i < p.length; i++) {
                            try {
                                msIDs[i] = new Manuscript(p[i].firstPage()).getID();
                            } catch (Exception e) {
                                msIDs[i] = -1;
                            }
                        }
                    }
                    for (int i = 0; i < mss.length; i++) {
                        out.print(mss[i].getCity() + ", " + mss[i].getRepository() + " " + mss[i].getCollection() + " (hosted by " + mss[i].getArchive() + ")\n");
                        if (mss[i].isRestricted()) {
                            out.print("<span class=\"restricted\">Restricted Access</span>\n");
                        }
                        Boolean resume = false;
                        for (int l = 0; l < msIDs.length; l++) {
                            if (msIDs[l] == mss[i].getID()) {
                                resume = true;
                            }
                        }
                        if (resume) {
                            out.print("<a href=transcription.jsp?ms=" + mss[i].getID() + ">Resume transcribing</a>" + "\n");
                        } else {
                            out.print("<a href=transcription.jsp?ms=" + mss[i].getID() + ">(Not marked)Start transcribing</a>" + "\n");
                        }
                        //out.print("<a href=transcription.jsp?ms=" + mss[i].getID() + ">Start transcribing</a>" + "\n");
                        out.print("<a href=addMStoProject.jsp?ms=" + mss[i].getID() + ">Add to project</a>" + "\n<br>");
                    }
                }
            }
        } catch (SQLException ex) {
            Logger.getLogger(manuscriptListings.class.getName()).log(Level.SEVERE, null, ex);
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
        processRequest(request, response);
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
        processRequest(request, response);
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
