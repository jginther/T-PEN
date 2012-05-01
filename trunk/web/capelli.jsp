<%-- 
    Document   : capelli
    Created on : Mar 4, 2011, 5:37:25 PM
    Author     : jdeerin1
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
        if(request.getParameter("id")!=null)
            {
            int id=Integer.parseInt( request.getParameter("id"));
            if(request.getParameter("label")==null)
                {
                //this
                textdisplay.AbbreviationPage.setIrrelevant(id);
                }
            else
                {
                String label=request.getParameter("label");
                String group=request.getParameter("group");
                textdisplay.AbbreviationPage.update(id, label, group);
                }
            }
        int imageNum=textdisplay.AbbreviationPage.getImageNeedingUpdate("french");
        textdisplay.AbbreviationPage abbrev=new textdisplay.AbbreviationPage(imageNum);
        out.print("<form method=POST action=\"capelli.jsp\">");
        out.print("<input type=hidden value=\""+abbrev.getId()+"\" name=\"id\">");
        out.print("Page Label<input type=\"text\" value=\""+"\" name=\"label\"><br>");
        out.print("Page starting letter<input type=\"text\" value=\""+abbrev.getGroup()+"\" name=\"group\"><br>");
        out.print("<input type=submit value=\"Save\"></form>");
        out.print("<form method=POST action=\"capelli.jsp\">");
        out.print("<input type=hidden value=\""+abbrev.getId()+"\" name=\"id\">");
        out.print("<input type=submit name=\"irrelevant page\" value=\"irrelevant page\"></form>");



        out.print("<img src=\"images/cappelli/"+abbrev.getImageName()+"\" alt=\"capelli image\">");

        %>
    </body>
</html>
