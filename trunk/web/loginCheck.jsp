<%-- 
    Document   : loginCheck
    Created on : Aug 18, 2009, 10:55:11 AM
    Author     : jdeerin1
    Checks to see if the user is logged in. If they arent, send them to login.jsp after setting their referer to where
    they came from to get to the current page.
--%>

<%@page contentType="text/html; charset=UTF-8"  pageEncoding="UTF-8"%>
<%@page import ="user.*"%>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN"
   "http://www.w3.org/TR/html4/loose.dtd">

<%
   if(session.getAttribute("UID")==null)
       {
           %><%@ include file="login.jsp" %><%
            return;
       }
   int uid;
   String uname=""+session.getAttribute("UID").toString();
   try
      {
      uid=Integer.parseInt(uname);
      }
   catch (Exception e)
           {
       uid=1;
       }
   %>