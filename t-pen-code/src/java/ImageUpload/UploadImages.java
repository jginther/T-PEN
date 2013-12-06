/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package ImageUpload;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import textdisplay.Folio;
import textdisplay.Manuscript;

/**
 *
 * @author obi1one
 */
public class UploadImages extends HttpServlet {
   
    /** 
     * Processes requests for both HTTP <code>GET</code> and <code>POST</code> methods.
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
    throws ServletException, IOException, FileUploadException, Exception {
        response.setContentType("text/html;charset=UTF-8");
        PrintWriter out = response.getWriter();
        HttpSession session=request.getSession();
        if (session.getAttribute("UID") == null) {
        response.sendError(403);
        return;
    }
    int UID = Integer.parseInt(session.getAttribute("UID").toString());
    String url=request.getRequestURI();
    user.User thisUser = new user.User(UID);
            String city=request.getParameter("city");
            String collection=request.getParameter("collection");
            String repository=request.getParameter("repository");
            String archive="private";
             if (ServletFileUpload.isMultipartContent(request)){
  ServletFileUpload servletFileUpload = new ServletFileUpload(new DiskFileItemFactory());
  List fileItemsList = servletFileUpload.parseRequest(request);

  String optionalFileName = "";
  FileItem fileItem = null;
  Iterator it = fileItemsList.iterator();
  while (it.hasNext()){
    FileItem fileItemTemp = (FileItem)it.next();
    String tmp=fileItemTemp.getFieldName();
    long maxSize=Integer.parseInt(Folio.getRbTok("maxUploadSize")); //200 megs
    if(fileItemTemp.getSize()>maxSize)
    {
        out.print("file is too large!");
        return;
    }
    if (fileItemTemp.getName().toLowerCase().endsWith("zip") ){
        
        File f=new File(Folio.getRbTok("uploadLocation")+"/"+thisUser.getLname()+thisUser.getUID()+".zip");
        fileItemTemp.write(f);
        Manuscript ms=new Manuscript(repository,archive,collection,city);
        UserImageCollection newImageCollection=new UserImageCollection(f,thisUser,ms);

        


        
    }
                 }
        }
        out.close();
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
        } catch (FileUploadException ex) {
            Logger.getLogger(UploadImages.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            Logger.getLogger(UploadImages.class.getName()).log(Level.SEVERE, null, ex);
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
        } catch (FileUploadException ex) {
            Logger.getLogger(UploadImages.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            Logger.getLogger(UploadImages.class.getName()).log(Level.SEVERE, null, ex);
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
