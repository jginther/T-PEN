<%--
    Document   : addPage
    Created on : Dec 21, 2009, 1:06:19 PM
    Author     : jdeerin1
UNUSED FILE
--%>

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
        for(int i=1;i<411;i++)
            {
                out.print("<a href=\"processImage?archive=ecodices&ms=0682&folio=");
                if(i<10)
                {//pad with 2 zeros
                    out.print("00");
                }
                else//pad with 1 zero
                    if(i<100)
                    {
                        out.print("0");
                    }
                else //its over 100, no padding
                    {
                    }
                out.print(""+i+"\">Add Page "+i+"</a>");

                out.print("&nbsp;&nbsp;&nbsp;<a href=\"viewTranscription.jsp?archive=ecodices&collection=0682&page=");
                if(i<10)
                {//pad with 2 zeros
                    out.print("00");
                }
                else//pad with 1 zero
                    if(i<100)
                    {
                        out.print("0");
                    }
                out.print(""+i+"\">View Existing (if there is one)</a><br/>");


            }
        %>
    </body>
</html>
