<%-- 
    Document   : showMetadata
    Created on : Oct 27, 2010, 6:26:49 PM
    Author     : jdeerin1
--%>
<%
int UID=0;
if(session.getAttribute("UID")==null)
     {
    %><%@ include file="loginCheck.jsp" %><%
            }
else
    {
         UID=Integer.parseInt(session.getAttribute("UID").toString());
    }%>
<%@page import ="textdisplay.Folio"%>
<%@page contentType="text/html; charset=UTF-8"  pageEncoding="UTF-8"%>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN"
   "http://www.w3.org/TR/html4/loose.dtd">

<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title>Metadata Display</title>
    </head>
    <body>
        title:
        transcribed by:
        settlement:
        repository:
        collection:
        idno:
        msname:

    </body>
</html>
