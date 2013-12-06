/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package servlets;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.URL;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import user.User;

/**
 *
 * @author jdeerin1
 */

public class proxy extends HttpServlet {

    /**
     * Processes requests for both HTTP
     * <code>GET</code> and
     * <code>POST</code> methods.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String mime;
    String search = request.getParameter("url");
    HttpSession session = request.getSession();
    if (session.getAttribute("UID") == null)
        {
            response.sendError(403);
            return;
        }
     if (request.getParameter("mimeType") == null) {
        mime = "application/rdf+xml";
    } else {
        
        mime = request.getParameter("mimeType");
        if(mime.contains("xml"))
        {
            
        }
    }
    mime = mime.trim();
    InputStream resultInStream = null;
    OutputStream resultOutStream = response.getOutputStream();

 response.setContentType(mime);
 response.addHeader("Access-Control-Allow-Origin", "http://t-pen.org");
 response.addHeader("Access-Control-Allow-Credentials", "true");
    try {
        URL url = new URL(search);
        resultInStream = url.openStream();
        byte[] buffer = new byte[4096];
        int bytes_read;
        while ((bytes_read = resultInStream.read(buffer)) != -1) {
            resultOutStream.write(buffer, 0, bytes_read);
        }
        resultOutStream.flush();
        resultOutStream.close();
        resultInStream.close();
    } catch (Exception e) {e.printStackTrace(); } finally {
            try {
                resultOutStream.flush();
                resultOutStream.close();
                resultInStream.close();
            } catch (Exception e) {e.printStackTrace();}
               }
        
    }

    // <editor-fold defaultstate="collapsed" desc="HttpServlet methods. Click on the + sign on the left to edit the code.">
    /**
     * Handles the HTTP
     * <code>GET</code> method.
     *
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
     * Handles the HTTP
     * <code>POST</code> method.
     *
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
     *
     * @return a String containing servlet description
     */
    @Override
    public String getServletInfo() {
        return "Short description";
    }// </editor-fold>
}
