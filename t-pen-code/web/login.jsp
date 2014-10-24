<%-- 
    Document   : login
    Created on : Apr 24, 2009, 8:52:50 AM
    Author     : jdeerin1
--%>
<%@page import ="java.sql.*"%>
<%@page import ="user.*"%>
<%@page contentType="text/html; charset=UTF-8"  pageEncoding="UTF-8" %>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN"
   "http://www.w3.org/TR/html4/loose.dtd">
   <%
   if(request.getParameter("uname")!=null&&request.getParameter("password")!=null)
       {
       user.User thisOne=new user.User(request.getParameter("uname"), request.getParameter("password"));
            if(thisOne.getUID()>0)
                {
                
                  
                session.setAttribute("UID", ""+thisOne.getUID());
                String ref="";
                String tmpref=request.getHeader("referer");

                if(request.getHeader("referer")==null || request.getHeader("referer").compareTo("")==0 || request.getHeader("referer").contains("login")){
                    %>
                    <script>
                        document.location = "index.jsp";
                    </script>
        <%}
                //response.sendRedirect("index.jsp");
                else
                    {
                    if(request.getHeader("referer").contains("authenticate.jsp"))
                        {
                    %>
                    <script>
                        document.location = "index.jsp";
                    </script>
        <%                       // response.sendRedirect("index.jsp");
                        }
                    else
                        {
                    %>
                    <script>
                        document.location = "<%out.print(request.getHeader("referer"));%>";
                    </script>
        <%//                        response.sendRedirect(request.getHeader("referer"));
                        }
                    }
                }
                
            else
                {
                String errorMessage = "Incorrect log in. Try again or <a href='login.jsp'>Request an Account</a>.";
            %><%@include file="WEB-INF/includes/errorBang.jspf" %><%
                return;
                }
           
       }
   %>
<%
            user.User thisUser = null;
            if (session.getAttribute("UID") != null)
                {
                thisUser = new user.User(Integer.parseInt(session.getAttribute("UID").toString()));
                }%>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title>Login or Register a New Account</title>
        <link rel="stylesheet" href="css/tpen.css" type="text/css" media="screen, projection">
        <link rel="stylesheet" href="css/print.css" type="text/css" media="print">
        <!--[if lt IE 8]><link rel="stylesheet" href="css/ie.css" type="text/css" media="screen, projection"><![endif]-->
        <link type="text/css" href="css/custom-theme/jQuery.css" rel="Stylesheet" />
        <script type="text/javascript" src="https://ajax.googleapis.com/ajax/libs/jquery/1.7.1/jquery.js"></script>
        <script type="text/javascript" src="https://ajax.googleapis.com/ajax/libs/jqueryui/1.8.16/jquery-ui.js"></script> 
        <script src="js/manuscriptFilters.js" type="text/javascript"></script>
        <script src="js/tpen.js" type="text/javascript"></script>
        <style>
        #login, #register { width: 45%;padding:10px;}
        #content {max-width: 800px;}
        #forgetForm {margin:-4px 5px 2px;background: url(images/linen.png);padding:10px 15px;display:none;overflow: hidden;z-index: 1; border: 1px solid #A68329;
            box-shadow:-1px -1px 2px black;}
        #forgetFormBtn {position: relative;z-index: 2;cursor: pointer;margin: 0 0 5px 0;}
	</style>
        <script type="text/javascript">
        $(function(){
            $("input:submit").hover(function(){$(this).toggleClass("ui-state-hover")});
        });
</script>
    </head>
<!--    <script type="text/javascript" src="menu.js"></script>-->
    <body>
        <div id="wrapper">
            <div id="header"><p align="center" class="tagline">transcription for paleographical and editorial notation</p>
            </div>
            <div id="content">
                    <h1><script>document.write(document.title); </script></h1>
            <!--<a href="authenticate?use=yahoo">openID using Yahoo</a><br/>-->     
        <%if(  request.getParameter("referer")==null ||request.getParameter("referer").contains("authenticate.jsp"))
        {
           // out.print("url:"+request.getRequestURL().toString());
            if(request.getRequestURL().toString().contains("authenticate") || request.getRequestURL().toString().contains("landing") )
                {
              //  out.print("setting toc");
                if(session.getAttribute("ref")==null)
                 session.setAttribute("ref","ToC.jsp");
                
                }
            else
                {
             //   out.print("setting url");
            session.setAttribute("ref",request.getRequestURL().toString());
            
            }
        }
   else
{
       
session.setAttribute("ref",request.getParameter("referrer"));

}%>
            <div id="main" class="ui-widget ui-widget-content ui-corner-all ui-tabs ui-helper-reset ui-helper-clearfix">
                <div id="login" class="left">
                    <h3 class="ui-widget-header ui-tabs ui-corner-all ui-state-default">Log In</h3>
            <p> You may log into your account to start transcribing or to manage your projects.</p>
                        <div id="resetPassword">
                            <h6 id="forgetFormBtn" class="clear-right">Forgot your Password?<span class="left ui-icon ui-icon-arrowstop-1-s"></span></h6>
                            <form id="forgetForm" action="admin.jsp" method="POST" class="ui-corner-all">
                            <span>Enter the email address associated with your account to have your password reset.</span>
                                <input id="email" type="text" class="text" style="width:220px;" placeholder="Forgot your password?" name="email">
                                <input class="right ui-corner-all ui-state-default" type="submit" name="emailSubmitted" value="Reset Password"/>
                            </form>
                        </div>
            <form id="login" action="login.jsp" method="POST" >
                            <fieldset>
                                <legend>Login Here:</legend>
                                <label for="uname">Email</label><input class="text" type="text" name="uname"/><br/>
                                <label for="password">Password</label><input  class="text" type="password" name="password"/><br/>
                            <input type="hidden" name="ref" value="<%out.print(session.getAttribute("ref"));%>"/>
                            <input class="ui-button ui-state-default ui-corner-all right" type="submit" title="Log In" value="Log In">
                            </fieldset>
                            </form>
                </div>
                <div id="register" class="right">
                    <h3 class="ui-widget-header ui-tabs ui-corner-all ui-state-default">Register a New Account</h3>
               <form action="signup.jsp" method="POST">
               <fieldset>
                                <legend>or Register as a New User:</legend>
                                Note: You will receive your password via email after your account is activated by an administrator
                                <label for="uname">Email</label><input class="text" type="text" name="uname"/><br/>
                                <label for="fname">First Name</label><input class="text" type="text" name="fname"/><br/>
                                <label for="lname">Last Name</label><input class="text" type="text" name="lname"/><br/>
               <input type="submit" value="Register" class="ui-button ui-state-default ui-corner-all right"/>
               </fieldset></form>
                </div>
            </div>
        <a class="returnButton" href="index.jsp">Return to T&#8209;PEN Home</a>
        </div>
        </div>
    </body>
</html>
