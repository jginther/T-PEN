 <%--
    Document   : requestcode
    Created on : Nov 1, 2010, 12:10:42 PM
    Author     : jdeerin1
--%>

<%@page contentType="text/html; charset=UTF-8"  pageEncoding="UTF-8"%>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN"
   "http://www.w3.org/TR/html4/loose.dtd">
<%@page import="user.User"%>
<%@page import="user.Group"%>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN"
   "http://www.w3.org/TR/html4/loose.dtd">

<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title>TPEN beta code request</title>
        <style>
             body{background-color:#dccdc8;}
        </style>
    </head>
    <body>
        

    
    <%
   
   /*
       if(request.getParameter("msg")!=null)
           {
           textdisplay.mailer m=new textdisplay.mailer();
           m.sendMail("smtp.mail.yahoo.com", "jonthab@yahoo.com", "jdeerin1@slu.edu", "TPEN beta request", "Here is a beta request from  "+request.getParameter("email")+":\n"+request.getParameter("msg"));
           out.print("Your request has been sent.");
           return;
           }

*/
       %>
        <!--
 <div style="width:400px;">
       <form action="requestcode.jsp">
           Please provide you email address <br><input type="text" id="email"/><br>
          Please provide any details about your interest in the beta. If you would like us to add iamges from a source not currently included in the beta, a url will
           be most helpful.<br>
           <textarea  name="msg" cols="35" rows="15"></textarea><br>
           <input type="submit" name="Send" value="submit request" >
       </form>
 </div>
-->
    </body>
</html>