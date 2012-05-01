<%-- 
    Document   : pagelisting
    Created on : Nov 3, 2010, 7:54:23 AM
    Author     : jdeerin1
--%>
<%@page import ="textdisplay.Archive"%>
<%@page contentType="text/html; charset=UTF-8"  pageEncoding="UTF-8"%>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN"
   "http://www.w3.org/TR/html4/loose.dtd">

<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title>JSP Page</title>
    </head>
    <body>
        <%
        Archive a= new Archive("ecodices");
        int num=Integer.parseInt(request.getParameter("id"));
        int cnum=Integer.parseInt(request.getParameter("cid"));
        out.print(a.getAvailableCollectionsFromSite("Oregon State University",num,cnum));
        %>
    </body>
</html>
