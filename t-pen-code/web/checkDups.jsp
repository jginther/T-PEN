<%-- 
    Document   : checkDups
    Created on : Jan 7, 2011, 2:50:38 PM
    Author     : jdeerin1
--%>

<%@page import="textdisplay.archive"%>
<%@page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@page import ="utils.duplicateMerger"%>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN"
   "http://www.w3.org/TR/html4/loose.dtd">

<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title>JSP Page</title>
    </head>
    <body>
        <%
        Search.TranscriptionIndexer.main(new String [] {});
        //Search.FullTextIndexer.main(new String [] {});
//textdisplay.harvardParser.main(new String [] {});
%>
    </body>
</html>
