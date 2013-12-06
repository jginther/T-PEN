<%-- 
    Document   : uploadHeader
    Created on : Mar 7, 2012, 12:01:47 PM
    Author     : obi1one
--%>

<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN"
   "http://www.w3.org/TR/html4/loose.dtd">
<%
int UID = 0;
            if (session.getAttribute("UID") == null)
                {
%><%@ include file="loginCheck.jsp" %><%                    } else
                    {
                    UID = Integer.parseInt(session.getAttribute("UID").toString());
                    }
int projectID=0;
    if(request.getParameter("projectID")!=null)
        {
        projectID=Integer.parseInt(request.getParameter("projectID"));
        }
    else{
        out.print("no project specified!");
        return;
        }




        %>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title>JSP Page</title>
    </head>
    <body>
        <form id="fileUpload" action="uploadHeader?projectID=<%out.print(""+projectID);%>" ENCTYPE="multipart/form-data" method="POST">
            <input class="ui-button tpenButton" type="file" id="file" name="file"/><br/>
     <br/><input class="ui-button tpenButton" type="submit" value="Upload" name="Upload"/>
     <input class="ui-button tpenButton" type="reset" value="Cancel" name="cancel" onclick="history.back();return false;" />
        </form>
    </body>
</html>
