
/**This servlet is modified from source code at http://www.javabeat.net/articles/262-asynchronous-file-upload-using-ajax-jquery-progress-ba-1.html*/

package ImageUpload;




import java.util.logging.Level;
import java.util.logging.Logger;
	import javax.servlet.Servlet;
	import javax.servlet.http.HttpServlet;
	import java.io.*;
import java.sql.SQLException;
	import java.util.*;
	import javax.servlet.http.*;
	import org.apache.commons.fileupload.*;
	import javax.servlet.ServletException;
	import org.apache.commons.fileupload.disk.DiskFileItemFactory;
	import org.apache.commons.fileupload.servlet.ServletFileUpload;
import textdisplay.Folio;
import textdisplay.Manuscript;
import textdisplay.Project;
import user.Group;

/**
 *
 * @author obi1one
 */
public class FileUpload extends HttpServlet implements Servlet {

	    private static final long serialVersionUID = 2740693677625051632L;

            /**
             *
             */
            public FileUpload() {
	        super();
	    }

            /**
             *
             * @param request
             * @param response
             * @throws ServletException
             * @throws IOException
             */
            protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
	        PrintWriter out = response.getWriter();
		    HttpSession session = request.getSession();
	        FileUploadListener listener = null;
			StringBuffer buffy = new StringBuffer();
		    long bytesRead = 0, contentLength = 0;

	        if (session == null) {
	            return;
		    } else if (session != null) {
	            listener = (FileUploadListener) session.getAttribute("LISTENER");

		        if (listener == null) {
                            out.print("No active listener");
			        return;
	            } else {
		            bytesRead = listener.getBytesRead();
	                contentLength = listener.getContentLength();
		        }
	        }
		    response.setContentType("text/xml");
		    buffy.append("<?xml version=\"1.0\" encoding=\"ISO-8859-1\"?>\n");
			buffy.append("<response>\n");
	        buffy.append("\t<bytes_read>" + bytesRead + "</bytes_read>\n");
	        buffy.append("\t<content_length>" + contentLength + "</content_length>\n");

	        if (bytesRead == contentLength) {
	            buffy.append("\t<finished />\n");
		        //session.setAttribute("LISTENER", null);
	        } else {
		        long percentComplete = ((100 * bytesRead) / contentLength);
	            buffy.append("\t<percent_complete>" + percentComplete + "</percent_complete>\n");
		    }
	        buffy.append("</response>\n");
		    out.println(buffy.toString());
	        out.flush();
		    out.close();
	    }

                /**
                 *
                 * @param request
                 * @param response
                 * @throws ServletException
                 * @throws IOException
                 */
                protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
HttpSession session = request.getSession();
		    try {
                         
        if (session.getAttribute("UID") == null) {
        response.sendError(403);
        return;
    }
            FileItemFactory factory = new DiskFileItemFactory();
            ServletFileUpload upload = new ServletFileUpload(factory);
            FileUploadListener listener = new FileUploadListener();
            
            
            upload.setProgressListener(listener);
            session.setAttribute("LISTENER", listener);
            List uploadedItems = null;
            FileItem fileItem = null;
            int UID = Integer.parseInt(session.getAttribute("UID").toString());
            String url = request.getRequestURI();
            user.User thisUser = new user.User(UID);
            String city = request.getParameter("city");
            String collection = request.getParameter("collection");
            String repository = request.getParameter("repository");
            String archive = "private";
            String filePath = Folio.getRbTok("uploadLocation") + "/" + thisUser.getLname() + thisUser.getUID() + ".zip";
            try {
                uploadedItems = upload.parseRequest(request);
                Iterator i = uploadedItems.iterator();
                long maxSize=Integer.parseInt(Folio.getRbTok("maxUploadSize")); //200 megs
                while (i.hasNext()) {
                    fileItem = (FileItem) i.next();
                    if (fileItem.isFormField() == false) {
                        if (fileItem.getSize() > 0 && fileItem.getSize()<maxSize && fileItem.getName().toLowerCase().endsWith("zip")) {
        File f=new File(Folio.getRbTok("uploadLocation")+"/"+thisUser.getLname()+thisUser.getUID()+".zip");
fileItem.write(f);
        Manuscript ms=new Manuscript(repository,archive,collection,city);
        ms.makeRestricted(-999);
        UserImageCollection newImageCollection=new UserImageCollection(f,thisUser,ms);
        Group g=new Group (ms.getShelfMark(),thisUser.getUID());
        Project p=new Project(ms.getShelfMark(),g.getGroupID());
        p.setFolios(ms.getFolios(),p.getProjectID());





    }
                            /*File uploadedFile = null;
                            String myFullFileName = fileItem.getName();
                            String myFileName = "";
                            String slashType = (myFullFileName.lastIndexOf("\\") > 0) ? "\\" : "/";
                            int startIndex = myFullFileName.lastIndexOf(slashType);
                            myFileName = myFullFileName.substring(startIndex + 1, myFullFileName.length());
                            uploadedFile = new File(filePath, myFileName);
                            fileItem.write(uploadedFile);*/
                        }
                    }
                
            } catch (FileUploadException e) {
                e.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            }
			} catch (SQLException ex) {
			    Logger.getLogger(FileUpload.class.getName()).log(Level.SEVERE, null, ex);
	        }
                    finally{session.setAttribute("LISTENER", null);}
		}
	}
