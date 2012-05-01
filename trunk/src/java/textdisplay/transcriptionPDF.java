
package textdisplay;

import com.lowagie.text.DocumentException;
import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
Creates a pdf of a transcription
 */

public class transcriptionPDF extends HttpServlet {

    /**
     * Processes requests for both HTTP <code>GET</code> and <code>POST</code> methods.
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
    throws ServletException, IOException, SQLException, FileNotFoundException, DocumentException {


        ByteArrayOutputStream bos=null;
                try {
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
            response.setContentType("application/pdf");
            int pageNum=Integer.parseInt(request.getParameter("pageNum"));
            transcriptionPage thisPage;
            if(projectID>0)
            {
                thisPage=new transcriptionPage(pageNum,projectID,true);
            }
            else
            {
                thisPage=new transcriptionPage(pageNum,uid);
            }
            
            bos=new ByteArrayOutputStream();
            //thisPage.PDFify(bos);
            /**Is this a request for a pdf of all work done on this folio?*/
            if(request.getParameter("archive")!=null && request.getParameter("collection")!=null)
            {
                String archive=request.getParameter("archive");
                String collection=request.getParameter("collection");
                if(request.getParameter("notes")!=null && request.getParameter("notes").compareTo("true")==0)
                {
                    if(projectID>0)
                    {
                        thisPage.PDFifyMultiplePages(bos, archive, collection,true,true);
                    }
                    else
                    {
                    thisPage.PDFifyMultiplePages(bos, archive, collection,true);
                    }
                }
                else
                {
                     if(projectID>0)
                    {
                        thisPage.PDFifyMultiplePages(bos, archive, collection,false,false);
                    }
                    else
                    {
                    thisPage.PDFifyMultiplePages(bos, archive, collection,false);
                    }
                }
            }
            //it is a request for a single page
            else
            {
                if(request.getParameter("notes")!=null && request.getParameter("notes").compareTo("true")==0)
                {
            thisPage.PDFify(bos,true);
                }
                else
                {
                    thisPage.PDFify(bos,false);
                }
            }
            response.getOutputStream().write(bos.toByteArray());
            }
        }
        finally
        {
            if(bos!=null)
                bos.close();
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
            throws ServletException, IOException, FileNotFoundException
    {
        try
        {
            processRequest(request, response);
        } catch (SQLException ex)
        {
            Logger.getLogger(transcriptionPDF.class.getName()).log(Level.SEVERE, null, ex);
        } catch (DocumentException ex)
        {
            Logger.getLogger(transcriptionPDF.class.getName()).log(Level.SEVERE, null, ex);
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
            throws ServletException, IOException, FileNotFoundException
    {
        try
        {
            processRequest(request, response);
        } catch (SQLException ex)
        {
            Logger.getLogger(transcriptionPDF.class.getName()).log(Level.SEVERE, null, ex);
        } catch (DocumentException ex)
        {
            Logger.getLogger(transcriptionPDF.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Returns a short description of the servlet.
     * @return a String containing servlet description
     */
    @Override
    public String getServletInfo()
    {
        return "Short description";
    }

}
