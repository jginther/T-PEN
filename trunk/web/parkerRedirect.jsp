<%-- 
    Document   : parkerRedirect
    Created on : Aug 8, 2011, 12:23:41 PM
    Author     : jim
--%>

<%@page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN"
   "http://www.w3.org/TR/html4/loose.dtd">

<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <%
        String druid=request.getParameter("druid");
        String pageName=request.getParameter("pageName").replace("_46.jp2", "");
        String imageName=textdisplay.Folio.getImageName(pageName)+".jpg";
        textdisplay.Folio f=textdisplay.Folio.getImageNameFolio(imageName);
        if(f!=null)
            {
            if(request.getParameter("project")!=null)
                {
                out.print("<meta http-equiv=\"refresh\" content=\"0;url=addMStoProject.jsp?ms="+new textdisplay.Manuscript(f.getFolioNumber()).getID()+"\" /> ");
                }
            else
                {
            out.print("<meta http-equiv=\"refresh\" content=\"0;url=transcription.jsp?p="+f.getFolioNumber()+"\" /> ");
            }
            }
        else
            {
            out.print("TPEN is unable to find that image! <a href=\"index.jsp\">Continue to TPEN</a>");
            }
        %>
        <title>JSP Page</title>
    </head>
    <body>
        
    </body>
</html>
