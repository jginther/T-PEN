<%-- 
    Document   : abbreviations
    Created on : Mar 25, 2011, 1:59:07 PM
    Author     : jdeerin1
UNUSED FILE
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
            int id=Integer.parseInt(request.getParameter("id"));
            

            if(request.getParameter("label")!=null)

                {
                String label=request.getParameter("label");
                String group=request.getParameter("group");
                textdisplay.AbbreviationPage.update(id, label, group);
                out.print("updated!<br>");
                }
            textdisplay.AbbreviationPage abbrev=new textdisplay.AbbreviationPage(id);
            
            out.print("<form method=GET action=\"abbreviations.jsp\">");
        out.print("<input type=hidden value=\""+abbrev.getId()+"\" name=\"id\">");
        out.print("Page Label<input type=\"text\" value=\""+abbrev.getLabel()+"\" name=\"label\"><br>");
        out.print("Page starting letter<input type=\"text\" value=\""+abbrev.getGroup()+"\" name=\"group\"><br>");
        out.print("<input type=submit value=\"Update\"></form>");
        
            
            
            out.print("<img style=\"float:left;\" src=\"http://t-pen.org/TPEN/images/cappelli/"+abbrev.getImageName()+"\"</img><br>");

            }
        %>
        <div style="float:right">
        <form action="abbreviations.jsp" method="POST">
        <select name="group">
        <%
        String [] groups=textdisplay.AbbreviationPage.getGroups("capelli");
        for(int i=0;i<groups.length;i++)
            {
            out.print("<option  value=\""+groups[i]+"\">"+groups[i]+"</option>");
            }
        %>
        </select>
        <input type="submit">
        </form>
        <%
        if(request.getParameter("group")!=null)
            {
            String group=request.getParameter("group");
           textdisplay.AbbreviationPage[] a= textdisplay.AbbreviationPage.getLabels(group,"capelli");
           for(int i=0;i<a.length;i++)
               {
               out.print("<a href=abbreviations.jsp?id="+a[i].getId()+"&group="+group+">"+a[i].getLabel()+"</a><br>");
               }
            }
        %>
        </div>
    </body>
</html>
