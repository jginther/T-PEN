<%-- 
    Document   : signup.jsp
    Created on : Jun 2, 2009, 10:56:17 AM
    Author     : jdeerin1
--%>
<%@page import ="user.User"%>
<%@page contentType="text/html; charset=UTF-8"  pageEncoding="UTF-8"%>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN"
    "http://www.w3.org/TR/html4/loose.dtd">
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title>Account Creation</title>
        <link rel="stylesheet" href="css/tpen.css" type="text/css" media="screen, projection">
        <link rel="stylesheet" href="css/print.css" type="text/css" media="print">
        <!--[if lt IE 8]><link rel="stylesheet" href="css/ie.css" type="text/css" media="screen, projection"><![endif]-->
        <link type="text/css" href="css/custom-theme/jQuery.css" rel="Stylesheet" />
        <script type="text/javascript" src="https://ajax.googleapis.com/ajax/libs/jquery/1.7.1/jquery.js"></script>
        <script type="text/javascript" src="https://ajax.googleapis.com/ajax/libs/jqueryui/1.8.16/jquery-ui.js"></script> 
        <script type="text/javascript" src="js/tpen.js"></script>
        <style type="text/css">
            .reginput
            {
                position:absolute;
                left:175px;
            }
        </style>
        <script type="text/javascript">
            $(function(){
                //            $( ".returnButton" )
                //                .addClass("ui-state-default ui-corner-bl ui-corner-br")
                //                .css("display","inline-block")
                //                .prepend("<span class='ui-icon ui-icon-arrowreturnthick-1-w right'></span>")
                //                .hover(function(){$(this).addClass("ui-state-highlight");},
                //                    function(){$(this).removeClass("ui-state-highlight");}
                //            );
                //            $("input:submit").hover(function(){$(this).toggleClass("ui-state-hover")});
            });

            function checkEmail () {
                var field3=document.forms["signup"]["uname"].value;
                var atpos=field3.indexOf("@");
                var dotpos=field3.lastIndexOf(".");
                if (atpos<1 || dotpos<atpos+2 || dotpos+2>=field3.length)
                {
                    alert("Not a valid e-mail address");
                    return false;
                }
            }    
        </script>

    </head>

    <body>
        <div id="wrapper">
            <div id="header"><p align="center" class="tagline">transcription for paleographical and editorial notation</p>
            </div>
            <div id="content">
                <h1><script type="text/javascript">document.write(document.title); </script></h1>
                <div id="main" class="ui-widget ui-widget-content ui-corner-all ui-tabs ui-helper-reset ui-helper-clearfix">
                    <%
                        if (request.getParameter("uname") != null
                                && request.getParameter("uname").contains("@") 
                                && request.getParameter("uname").contains(".")
                                && request.getParameter("fname") != null
                                && request.getParameter("lname") != null) {
                            //create a user with a blank password. Their password will be set when they are approved by an admin
                            int result = user.User.signup(request.getParameter("uname"), request.getParameter("lname"), request.getParameter("fname"));
                            //total success
                            if (result == 0) {
                                out.println("<div class=\"success\"><p style=\"font-size:2em;\">Your account was created. You will recieve an email from TPEN@t-pen.org when an administrator has activated your account. If your e-mail does not arrive, please verify that it has not been caught by a spam filter.</p></div>");
                                return;
                            }
                            //failed to create user
                            if (result == 1) {
                                out.println("<div class=\"error\"><p style=\"font-size:2em;\">That username seems to be taken!</p></div>");
                            }
                            //created user but email failed, typical on test systems
                            if (result == 2) {
                                out.println("<div class=\"error\"><p style=\"font-size:2em;\">Account created but the emails could not be sent! Contact the TPEN team.</p></div>");
                            }
                        } else {
                            if (request.getParameter("uname") != null){
                                out.println("<div class=\"error\"><p style=\"font-size:2em;\">There was an error with your submission. Please check the form and try again.</p></div>");
                            }
                    %>                       
                    <form action="signup.jsp" name="signup" onsubmit="return simpleFormValidation();">
                        Email<input class="reginput" type="text" name="uname" <%if (request.getParameter("uname") != null) {
                               out.print("value=\"" + request.getParameter("uname") + "\"");
                           }%>/><br/><br/>
                        first name<input class="reginput" type="text" name="fname" <%if (request.getParameter("fname") != null) {
                               out.print("value=\"" + request.getParameter("fname") + "\"");
                           }%>/><br/><br/>
                        last name<input class="reginput" type="text" name="lname" <%if (request.getParameter("lname") != null) {
                               out.print("value=\"" + request.getParameter("lname") + "\"");
                           }%>/><br/><br/>
                        <input class="ui-button tpenButton" type="submit" value="Register"/>
                    </form>
                    <%}%>
                </div>
                <a class="returnButton" href="index.jsp">Return to T&#8209;PEN Home</a>
            </div>
        </div>
    </body>
</html>
