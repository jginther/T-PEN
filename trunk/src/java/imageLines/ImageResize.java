/*
 * @author Jon Deering
Copyright 2011 Saint Louis University. Licensed under the Educational Community License, Version 2.0 (the "License"); you may not use
this file except in compliance with the License.

You may obtain a copy of the License at http://www.osedu.org/licenses/ECL-2.0

Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions
and limitations under the License.
 */
package imageLines;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.URL;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import javax.mail.MessagingException;
import javax.mail.internet.AddressException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import textdisplay.Folio;
import textdisplay.mailer;
import java.io.File;
import java.util.Iterator;
import javax.imageio.*;
import javax.imageio.stream.*;
import textdisplay.Archive;
import textdisplay.Manuscript;

/**
A servlet to resize an image from an Archive to a size requested.
 */
public class ImageResize extends HttpServlet {

    /**
     * Processes requests for both HTTP <code>GET</code> and <code>POST</code> methods.
     * @param milliSeconds
     * @return
     */
    public static String getGMTTimeString(long milliSeconds) {
        SimpleDateFormat sdf = new SimpleDateFormat("E, d MMM yyyy HH:mm:ss 'GMT'");
        return sdf.format(new Date(milliSeconds));
    }

    /**
     * This handles delivering properly sized images to both end users and parts of tpen that need to access images, such as the line parser.
     * @param request
     * @param response
     * @throws ServletException
     * @throws IOException
     * @throws SQLException
     */
    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException, SQLException {
        String uidString = null;
        ;
        if (request.getSession().getAttribute("UID") != null) {
            uidString = request.getSession().getAttribute("UID").toString();
        }
        if (uidString == null) {
            if (request.getParameter("code") == null) {
                response.sendError(403);
                return;
            }
            if (request.getParameter("code").compareTo(Folio.getRbTok("imageCode")) != 0) {
                response.sendError(403);
                return;
            }
        }
        response.addHeader("Cache-Control", "max-age=3600");
        long relExpiresInMillis = System.currentTimeMillis() + (1000 * 2600);
        response.addHeader("Expires", getGMTTimeString(relExpiresInMillis));
        response.setContentType("image/jpeg");

        if (request.getParameter("folioNum") != null) {
            BufferedImage toResize = null;
            Iterator iter = ImageIO.getImageWritersByFormatName("jpeg");
            ImageWriter writer = (ImageWriter) iter.next();
// instantiate an ImageWriteParam object with default compression options
            ImageWriteParam iwp = writer.getDefaultWriteParam();
            iwp.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
            float quality = (float) 0.5;
            if (request.getParameter("quality") != null) {
                try {
                    int qual = Integer.parseInt(request.getParameter("quality"));
                    quality = (float) qual / 100;
                } catch (NumberFormatException e) {
                }
            }
            iwp.setCompressionQuality(quality);   // an integer between 0 and 1
            int folioNum = Integer.parseInt(request.getParameter("folioNum"));
            ImageRequest ir = null;


            int uid;
            String uname = uidString;

            if (uidString != null) {
                try {
                    uid = Integer.parseInt(uname);
                    ir = new ImageRequest(folioNum, uid);
                } catch (Exception e) {
                    uid = 1;

                }
            } else {
                ir = new ImageRequest(folioNum, 0);
            }

            try {
                toResize = ImageCache.getImage(folioNum);
                Logger.getLogger(ImageResize.class.getName()).log(Level.SEVERE, "Loaded image " + folioNum + " from cache\n");
            } catch (Exception e) {
                Logger.getLogger(ImageResize.class.getName()).log(Level.SEVERE, null, e);
            }
            String heightNumString = request.getParameter("height");
                String methodString=request.getParameter("method");
            int height=Integer.parseInt(heightNumString);
            Folio f = new Folio(Integer.parseInt(request.getParameter("folioNum")));
            Archive a = new Archive(f.getArchive());
            Manuscript m = new Manuscript(f.getFolioNumber());
            URL imageURL = null;
            String url = "none";
            url = textdisplay.Archive.getURL(folioNum);
            //small hack for one repository we use, not needed generally
            url = url.replace("64.19.142.12/88.48.84.154", "88.48.84.154");
            System.out.print("requesting image " + url + "\n");
            imageURL = new URL(url);
            //if the image wasnt in the cache, use the appropriate method to fetch is from wherever it is hosted
            if (toResize == null) {
                if (a.getConnectionMethod() == Archive.connectionType.local) {
                    try {
                        Logger.getLogger(ImageResize.class.getName()).log(Level.SEVERE, "using local method\n");
                        response.setContentType("image/jpeg");
                        System.out.print("loading image " + Folio.getRbTok("LOCALIMAGESTORE") + f.getPageName() + ".jpg\n");
                        toResize = ImageHelpers.readAsBufferedImage(Folio.getRbTok("LOCALIMAGESTORE") + f.getPageName() + ".jpg");

                    } catch (Exception e) {
                        try {
                            ir.completeFail(e.getMessage());
                        } catch (Exception ex) {
                            Logger.getLogger(ImageResize.class.getName()).log(Level.SEVERE, null, ex);
                        }
                        mailer mail = new mailer();
                        try {
                            String body = "Failed to load image " + url + " which is folio " + folioNum + " from " + new Manuscript(folioNum).getShelfMark() + "\n";
                            mail.sendMail(Folio.getRbTok("EMAILSERVER"), "TPEN@t-pen.org", Folio.getRbTok("NOTIFICATIONEMAIL"), "TPEN Image Issue", body);
                            response.sendError(500);
                            return;
                        } catch (Exception ex) {
                            Logger.getLogger(ImageResize.class.getName()).log(Level.SEVERE, null, ex);
                            response.sendError(500);
                            return;
                        }


                    }
                }

                if (f.getArchive().compareTo("private") == 0) {
                    try {
                        response.setContentType("image/jpeg");
                        toResize = ImageHelpers.readAsBufferedImage(f.getImageName() + ".jpg");
                    } catch (Exception e) {
                        try {
                            ir.completeFail(e.getMessage());
                        } catch (Exception ex) {
                            Logger.getLogger(ImageResize.class.getName()).log(Level.SEVERE, null, ex);
                        }
                        mailer mail = new mailer();
                        try {
                            String body = "Failed to load image " + url + " which is folio " + folioNum + " from " + new Manuscript(folioNum).getShelfMark() + "\n";
                            mail.sendMail(Folio.getRbTok("EMAILSERVER"), "TPEN@t-pen.org", Folio.getRbTok("NOTIFICATIONEMAIL"), "TPEN Image Issue", body);
                            response.sendError(500);
                            return;
                        } catch (Exception ex) {
                            Logger.getLogger(ImageResize.class.getName()).log(Level.SEVERE, null, ex);
                            response.sendError(500);
                            return;
                        }


                    }
                }

                if (a.getConnectionMethod() == Archive.connectionType.cookie) {
                    try {
                        Logger.getLogger(ImageResize.class.getName()).log(Level.SEVERE, "using cookie method with url "+a.getCookieURL()+"\n");
                        String cookiegetter = a.getCookieURL(); //"";
                        String val = ImageHelpers.getCookie(cookiegetter);
                        System.out.print(val+" "+imageURL.toString()+"\n");
                        toResize = ImageHelpers.readAsBufferedImage(imageURL, val);
                    } catch (Exception e) {
                        try {
                            ir.completeFail(e.getMessage());
                        } catch (Exception ex) {
                            Logger.getLogger(ImageResize.class.getName()).log(Level.SEVERE, null, ex);
                        }
                        mailer mail = new mailer();
                        try {
                            String body = "Failed to load image " + url + " which is folio " + folioNum + " from " + new Manuscript(folioNum).getShelfMark() + "\n";
                            mail.sendMail(Folio.getRbTok("EMAILSERVER"), "TPEN@t-pen.org", Folio.getRbTok("NOTIFICATIONEMAIL"), "TPEN Image Issue", body);
                            response.sendError(500);
                            return;
                        } catch (Exception ex) {
                            Logger.getLogger(ImageResize.class.getName()).log(Level.SEVERE, null, ex);
                            response.sendError(500);
                            return;
                        }


                    }
                }
                if (a.getConnectionMethod() == Archive.connectionType.none) {
                    try {
                        Logger.getLogger(ImageResize.class.getName()).log(Level.SEVERE, "using no special method\n");
                        toResize = ImageHelpers.readAsBufferedImage(imageURL);
                    } catch (Exception e) {
                        try {
                            ir.completeFail(e.getMessage());
                        } catch (Exception ex) {
                            Logger.getLogger(ImageResize.class.getName()).log(Level.SEVERE, null, ex);
                        }
                        mailer mail = new mailer();
                        try {
                            String body = "Failed to load image " + url + " which is folio " + folioNum + " from " + new Manuscript(folioNum).getShelfMark() + "\n";
                            mail.sendMail(Folio.getRbTok("EMAILSERVER"), "TPEN@t-pen.org", Folio.getRbTok("NOTIFICATIONEMAIL"), "TPEN Image Issue", body);
                            response.sendError(500);
                            return;
                        } catch (Exception ex) {
                            Logger.getLogger(ImageResize.class.getName()).log(Level.SEVERE, null, ex);
                            response.sendError(500);
                            return;
                        }
                        

                    }
                }
                if (a.getConnectionMethod() == Archive.connectionType.httpAuth) {
                    try {
                        Logger.getLogger(ImageResize.class.getName()).log(Level.SEVERE, "using http auth method\n");
                        toResize = ImageHelpers.readAsBufferedImage(url, a.getUname(), a.getPass());
                    } catch (Exception e) {
                        try {
                            ir.completeFail(e.getMessage());
                        } catch (Exception ex) {
                            Logger.getLogger(ImageResize.class.getName()).log(Level.SEVERE, null, ex);
                        }
                        mailer mail = new mailer();
                        try {
                            String body = "Failed to load image " + url + " which is folio " + folioNum + " from " + new Manuscript(folioNum).getShelfMark() + "\n";
                            mail.sendMail(Folio.getRbTok("EMAILSERVER"), "TPEN@t-pen.org", Folio.getRbTok("NOTIFICATIONEMAIL"), "TPEN Image Issue", body);
                            response.sendError(500);
                            return;
                        } catch (Exception ex) {
                            Logger.getLogger(ImageResize.class.getName()).log(Level.SEVERE, null, ex);
                            response.sendError(500);
                            return;
                        }
                        

                    }

                }



            }
        if(toResize==null)
        {
             try {
                            ir.completeFail("Image was null");
                        } catch (Exception ex) {
                            Logger.getLogger(ImageResize.class.getName()).log(Level.SEVERE, null, ex);
                        }
                        mailer mail = new mailer();
                        try {
                            String body = "Failed to load image " + url + " which is folio " + folioNum + " from " + new Manuscript(folioNum).getShelfMark() + "\n";
                            mail.sendMail(Folio.getRbTok("EMAILSERVER"), "TPEN@t-pen.org", Folio.getRbTok("NOTIFICATIONEMAIL"), "TPEN Image Issue", body);
                            response.sendError(500);
                            return;
                        } catch (Exception ex) {
                            Logger.getLogger(ImageResize.class.getName()).log(Level.SEVERE, null, ex);
                            response.sendError(500);
                            return;
                        }
        }
        try {
            ir.completeSuccess();
        } catch (Exception ex) {
            Logger.getLogger(ImageResize.class.getName()).log(Level.SEVERE, null, ex);
        }
        int width = (int) ((height / (double) toResize.getHeight()) * toResize.getWidth());
        /*if(height==1000 && width>700)
        {
        width=700;
        height=(int) ((width / (double) toResize.getWidth()) * toResize.getHeight());
        }
        else
        if(height==2000 && width>1400)
        {
        width=1400;
        int resizeWidth=toResize.getWidth();
        int oldh=toResize.getHeight();
        height=(int) (((width / (double) toResize.getWidth()) * toResize.getHeight()));
        }*/
        ImageCache.setImage(folioNum, toResize);
        toResize = ImageHelpers.scale(toResize, height, width);
        //ImageHelpers.writeImage(toResize, "/usr/web/tosend/debug.jpg");
        OutputStream os = response.getOutputStream();
        IIOImage image = new IIOImage(toResize, null, null);

        writer.setOutput(ImageIO.createImageOutputStream(os));
        writer.write(null, image, iwp);

        Logger.getLogger(ImageResize.class.getName()).log(Level.SEVERE, "adding image " + folioNum + " to cache\n");

//            ImageIO.write(toResize,"jpeg",os);
//            os.close();
        //now parse the image

        //Folio f=new Folio(folioNum,true);
    
  
    return;

}
        //This is no longer used, throw a 404
        response.sendError(response.SC_NOT_FOUND);
            return;




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
        protected void

doGet(HttpServletRequest request, HttpServletResponse response)
    throws ServletException, IOException {
        try {
            processRequest(request, response);



}

catch (Exception ex) {
            Logger.getLogger(ImageResize.class.getName()).log(Level.SEVERE, null, ex);
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
        protected void

doPost(HttpServletRequest request, HttpServletResponse response)
    throws ServletException, IOException {
        try {
            processRequest(request, response);



}

catch (Exception ex) {
            Logger.getLogger(ImageResize.class.getName()).log(Level.SEVERE, null, ex);
        }


}

    /**
     * Returns a short description of the servlet.
     * @return a String containing servlet description
     */

        @Override
        public String

getServletInfo() {
        return "Short description";

}// </editor-fold>

}
