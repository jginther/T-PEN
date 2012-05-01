<%-- 
    Document   : schemaTest
    Created on : Apr 15, 2011, 1:35:47 PM
    Author     : jim
--%>

<%@page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN"
   "http://www.w3.org/TR/html4/loose.dtd">

<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title>JSP Page</title>
    </head>
    <body>
        <%
        if(request.getParameter("projectID")!=null)
            {
            textdisplay.Project p=new textdisplay.Project(Integer.parseInt(request.getParameter("projectID")));
            p.setSchemaURL(request.getParameter("url"));
                }
            
        %>
        Set schema url
        <form action="schemaTest.jsp" method="POST">
        project id    <input type="text" name="projectID"><br>
            url<input type="text" name="url"><br>
            <input type="submit"><br>

        </form>
        Validate project
        <form action="validate" method="GET">
            Project ID<input type="text" name="projectID"><br>
            <input type="submit">
        </form>

    </body>
</html>
