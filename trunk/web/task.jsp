<%-- 
    Document   : task
    Created on : Aug 14, 2009, 11:03:53 AM
    Author     : jdeerin1
--%>

<%@page import="textdisplay.task" contentType="text/html; charset=UTF-8"  pageEncoding="UTF-8"%>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN"
   "http://www.w3.org/TR/html4/loose.dtd">

<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title>JSP Page</title>
    </head>
    <body>
        <%
        if(request.getParameter("task")!=null)
            {
                textdisplay.folio.main(new String[0]);
                
                

            }
        %>
    </body>
</html>
