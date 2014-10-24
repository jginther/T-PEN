<%-- 
    Document   : logTest
    Created on : Feb 16, 2011, 1:57:49 PM
    Author     : jdeerin1
--%>



<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title>JSP Page</title>
    </head>
    <body>

        <%
        int UID=0;
if(session.getAttribute("UID")==null)
     {
    %><%@ include file="loginCheck.jsp" %><%
            }
else
    {
        UID=Integer.parseInt(session.getAttribute("UID").toString());
    }
        textdisplay.Project p=new textdisplay.Project(10);
        if(request.getParameter("submitted")!=null)
            {
            String content=request.getParameter("content"); 
            p.addLogEntry(content, UID); // ,"userAdded"
            }
        out.print(p.getProjectLog());
        %>
        <br>
        <form action="logTest.jsp" method="POST">
            <textarea name="content" id="content"></textarea><br>
            <input type="submit" name="submitted" value="add comment" >
        </form>
    </body>
</html>
