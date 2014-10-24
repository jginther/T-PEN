/*
 * Copyright 2011-2013 Saint Louis University. Licensed under the
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
 * 
 * @author Jon Deering
 */

package imageLines;

import java.io.IOException;
import java.io.PrintWriter;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
    perform line detection on the image from an archive and create a Folio object, store it in the db, so the page can be used for
 * transcription
 */
public class ProcessImage extends HttpServlet {

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
        /*
            String collection="";
            String archive="";
            String page="";
            try{
            collection=request.getParameter("ms");
            page=request.getParameter("Folio");
            archive=request.getParameter("archive");
            }
            catch (NullPointerException e)
            {
            response.sendError(response.SC_NOT_FOUND);
            return;
            }
            //Has this page already been processed?
            try
            {
                int pagenum=Folio.getPageNum(page, collection, archive);
                if(pagenum>0)
                {
                    response.sendRedirect("transcription.jsp?p="+pagenum);
                    return;
                }
            }
            catch (Exception e)
            {

            }
            out.print("<HTML><body onload=\"redirectThem();\">");
            out.print("The requested image is being processed. You will be taken to the transcription page in a moment...");
            String baseURL="http://sulwebappdev1.stanford.edu:6789/mappaemundi/getImg";
            baseURL+="?ms="+collection;
            baseURL+="&Folio="+page;
            baseURL=textdisplay.archive.getURL(collection, page, archive);
            URL imageURL=new URL(baseURL);

            BufferedImage img=ImageHelpers.readAsBufferedImage(imageURL);
            int width=(int) (img.getWidth() * .5);
            img=ImageHelpers.scale(img,1000,width);
            BufferedImage flippedImage=ImageHelpers.flipHorizontal(img);
            BufferedImage bin=ImageHelpers.binaryThreshold(img, 0);
            Detector myDetector=new Detector(img,bin);
            myDetector.smeared=bin;

            //ImageHelpers.writeImage(img, "/usr/web/tosend/img.jpg");
            myDetector.graphical=false;
            myDetector.vsmearDist=15;
            myDetector.hsmearDist=15;
            try{
            myDetector.detect();
            }
            catch (ArithmeticException e)
            {

            }
            detectimages.line [] flipped=myDetector.lines.toArray(new detectimages.line[myDetector.lines.size()]);

            bin=ImageHelpers.binaryThreshold(flippedImage, 0);
            myDetector.bin=bin;
            myDetector.img=flippedImage;

            Detector flipDetector=new Detector(flippedImage, bin);
            flipDetector.smeared=bin;

            ImageHelpers.writeImage(flippedImage, "/usr/web/tosend/bin.jpg");
            try{
                flipDetector.detect();
            }

            catch (ArithmeticException e)
            {

            }
            ImageHelpers.writeImage(bin, "/usr/web/tosend/bin2.jpg");
            if(flipDetector.lines.size()>flipped.length)
                flipped=flipDetector.lines.toArray(new detectimages.line[flipDetector.lines.size()]);

            //ImageHelpers.writeImage(bin, "/usr/web/tosend/bin.jpg");
            try {
                //before saving the positions of the lines in the image, create the record to indicate the identifying
                //information for this image in the folios table.

                String imageName=textdisplay.archive.getURL(collection, page, archive);
                int pageNum=Folio.createFolioRecord(collection, page, imageName,archive);
                Folio newFolio=new Folio(flipped,pageNum);
            //response.sendRedirect("transcription.jsp?p="+pageNum);

                /*
            out.print("found "+myDetector.lines.size()+"lines");
               out.print("<img src=\""+baseURL+"\"/>");
            for(int i=0;i<myDetector.lines.size();i++)
            {
                out.print("<div style=\"position:absolute;background-color:red;height:2px;z-index:1;top:"+((int)myDetector.lines.get(i).getStartVertical()*1.25)+"px;\">a</div>");
            }
                out.print("<script>function redirectThem(){document.location=\"transcription.jsp?p="+pageNum+"\";}</script>");
                out.print("The image has been processed, if you arent automatically redirected, please enable javascript and go <a href=\"transcription.jsp?p="+pageNum+"\">here</a>");
                out.print("</body></HTML>");

            } catch (SQLException ex) {
                Logger.getLogger(ProcessImage.class.getName()).log(Level.SEVERE, null, ex);
            }
            */




    }

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
    }
}
