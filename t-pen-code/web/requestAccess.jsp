<%-- 
    Document   : requestAccess
    Created on : Mar 15, 2011, 2:30:31 PM
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
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title>Request Access</title>
        <link rel="stylesheet" href="css/tpen.css" type="text/css" media="screen, projection">
        <link rel="stylesheet" href="css/print.css" type="text/css" media="print">
        <!--[if lt IE 8]><link rel="stylesheet" href="css/ie.css" type="text/css" media="screen, projection"><![endif]-->
        <link type="text/css" href="css/custom-theme/jQuery.css" rel="Stylesheet" />
        <link type="text/css" href="css/jquery.simple-color-picker.css" rel="Stylesheet" />
        <script type="text/javascript" src="https://ajax.googleapis.com/ajax/libs/jquery/1.7.1/jquery.js"></script>
        <script type="text/javascript" src="https://ajax.googleapis.com/ajax/libs/jqueryui/1.8.16/jquery-ui.js"></script> 
        <script type="text/javascript" src="js/tpen.js"></script>
    </head>
    <body>
        <div id="wrapper">
        <div id="header"><p align="center" class="tagline">transcription for paleographical and editorial notation</p></div>
        <div id="content">
<%
        if(request.getParameter("submitted")!=null)
            {
            textdisplay.Manuscript ms=new textdisplay.Manuscript(Integer.parseInt(request.getParameter("ms")), true);
            if(ms.contactControllingUser(request.getParameter("reason"),new user.User(UID)))
                {
                //give success message and redirect
                out.print("Message sent, you will be redirected to the T&#8209;PEN homepage in a moment.");
                out.print("<script>setTimeout('document.location=\"index.jsp\"', 3000);</script>");
                return;
                }
            else
                {
                //sending the email failed, give failure message and redirect
                out.print("An error occured sending the message, the TPEN team has been notified.");
                out.print("<script>setTimeout('document.location=\"index.jsp\"', 3000);</script>");
                return;
                }
            }
        %>
        <form action="requestAccess.jsp" method="POST">
            This will send an email to the TPEN user who controls access to this document. Your name and email address will be included in your request automatically. Please add details of why you want access below.<br>
            <textarea name="reason"></textarea>
            <input type="hidden" name="ms" value="<%out.print(request.getParameter("ms"));%>">
            <input type="submit" name="submitted">
        </form>
        </div>
        </div>
    </body>
</html>
