<%-- 
    Document   : compareTranscriptions
    Created on : Sep 17, 2009, 11:55:58 AM
    Author     : jdeerin1
UNUSED FILE NON-FUNCTIONAL
--%>

<%@page import ="textdisplay.transcriptionPage"%>

<%@page import ="textdisplay.Folio"%>
<%@page contentType="text/html; charset=UTF-8"  pageEncoding="UTF-8"%>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN"
   "http://www.w3.org/TR/html4/loose.dtd">

<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title>JSP Page</title>
        <script>
        function navigateTo(dropdown)
        {
            document.location='?uid='+dropdown.value;
        }
        function chooseUser(dropDown)
        {
            document.location='?uid='+dropDown.value+'&p=<%out.print(request.getParameter("p"));%>';
        }

        </script>
        <style>
            .footer{
        position:fixed;
        bottom:0px;
        height:40px;
        width:100%;
        left:0px;
        border-style:solid;
        border-color:maroon;
        z-index:2;
        background:maroon;
        text-align:center;
    }
    .footer a
    {
        color:white;
    }
    .lookLikeButtons{border-style:solid;background-color:gray;cursor:pointer;padding-left:4px;padding-right:4px;}
        </style>
    </head>
    <body>
        <%@ include file="loginCheck.jsp" %>
        <%
        user.User thisUser=new user.User(uid);
        if(thisUser.isAdmin())
            {
            }
        int pageno=Integer.parseInt(request.getParameter("p"));
         transcriptionPage myPage=new transcriptionPage(pageno,uid);
        //myPage.PDFify();
        Folio thisFolio=new Folio(pageno,true);
       
        %>
         <div class="footer">
        <%
        out.print("<form action=\"viewTranscription.jsp\" method=\"GET\">");
        out.print("<select name=\"p\">");
        out.print(thisFolio.getFolioDropDown());
        out.print("</select>");
        out.print("<input type=submit value=\"Jump to assignment\">");
        out.print("</form>");
        out.print("<a href=\"transcription.jsp?p="+pageno+"\">Return to Transcribing</a>");
        out.print("<span style=\"float:right;color:white;\">Viewing: "+thisFolio.getCollectionName()+" "+thisFolio.getPageName()+"&nbsp;</span>");
        %>
        </div>
        <div style="width:40%;float:left;"><u>Your transcription</u><br/>
        <%
        out.print(""+myPage.getContent());
        %>
        </div>
            <%
            int secondUID=0;
            if(request.getParameter("uid")!=null)
                {
                    secondUID=Integer.parseInt(request.getParameter("uid"));
                }
            user.User compareUser=new user.User(secondUID);
            out.print("<select onChange=\"chooseUser(this);\">"+compareUser.getAllUsers()+"</select>");
            %><div style="width:40%;float:right;"><u><%out.print(compareUser.getFname()+" "+compareUser.getLname()+"'s");%> transcription</u><br/><%
            myPage=new transcriptionPage(pageno,secondUID);
            out.print(""+myPage.getContent());
            %>
        </div>





    </body>
</html>
