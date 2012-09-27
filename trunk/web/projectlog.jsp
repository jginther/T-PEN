<%-- 
    Document   : projectlog
    Created on : Feb 16, 2011, 1:57:49 PM
    Author     : cubap
--%>
<%         
            if (session.getAttribute("UID") == null) {
        %><%@ include file="loginCheck.jsp" %><%
                   } else {
%>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title>Project Log</title>
        <link rel="stylesheet" href="css/tpen.css" type="text/css" media="screen, projection">
        <link rel="stylesheet" href="css/print.css" type="text/css" media="print">
        <!--[if lt IE 8]><link rel="stylesheet" href="css/ie.css" type="text/css" media="screen, projection"><![endif]-->
        <link type="text/css" href="css/custom-theme/jQuery.css" rel="Stylesheet" />
        <script type="text/javascript" src="https://ajax.googleapis.com/ajax/libs/jquery/1.7.1/jquery.js"></script>
        <script type="text/javascript" src="https://ajax.googleapis.com/ajax/libs/jqueryui/1.8.16/jquery-ui.js"></script> 
        <script type="text/javascript" src="js/tpen.js"></script>
        <%     int projectID=0;
    if(request.getParameter("projectID")!=null) projectID=Integer.parseInt(request.getParameter("projectID"));
    else{
        out.print("<div class=\"error\">No project specified!</div>");
        return;
        }%>
        <style type="text/css" media="print">
            form, #header, .openNote, #logContent,h1,#close {display:none;}
            a[href] {text-decoration: none;}
            div.logEntry:nth-child(2n) { background-color: whitesmoke; } /* Alternate entry colors (CSS3) */
            div.logEntry:nth-child(2n-1) { background-color: lightgrey; } /* Alternate entry colors (CSS3) */
            .logDate,.logAuthor {font-size:smaller; font-weight:bold;}
            .logDate, .logAuthor, .logContent {color:black;}
            .logEntry:not(:last-child) {border-bottom:thin solid black;}
            .boom:before{content:'Printed Record of Project ID#:<%out.print("" + projectID);%>, Project Title:';}
            .boom {font-size: medium;line-height: normal;color:black;}
        </style>
        <style type="text/css" media="screen">
            #logContent {height: 4em;width: 100%;font-family: sans-serif;}
            .logDate,.logAuthor {color:#226683;font-size:12px;font-weight: bold;}
            .logEntry {clear:both;padding:4px;}
        </style>
    </head>
    <body>
        <div id="wrapper">
            <div id="header"><p align="center" class="tagline">transcription for paleographical and editorial notation</p></div>
            <div id="content">
    <h1><script>document.write(document.title); </script></h1>
    <a id="close" class="right ui-state-default ui-corner-all ui-button" href="javascript:window.close();">Close this Page</a>
    <p class="openNote">This log will print in plain text. To add an entry, type below and click "Submit".</p>
<!--    Filters: userAdded, transcription, addMS, parsing           -->
    <form action="projectlog.jsp?selecTab=2&projectID=<%out.print(projectID);%>" method="POST">
                                    <input type="hidden" name="projectID" value="<%out.print(projectID);%>">
                                    <textarea name="logContent" id="logContent"></textarea><br>
                                    <input class="tpenButton right" type="submit" name="submitted" value="Submit New Entry" />
                                </form>
        <%
        int UID=0;
if(session.getAttribute("UID")==null)
     {
    %><%@ include file="loginCheck.jsp" %><%
            }
else
    {
        UID=Integer.parseInt(session.getAttribute("UID").toString());
    }
        textdisplay.Project p=new textdisplay.Project(projectID);
        if(request.getParameter("submitted")!=null)
            {
            String content=request.getParameter("logContent");
            p.addLogEntry("<span class='log_user'></span>"+content, UID); // ,"userAdded"
            }
        out.print(p.getProjectLog());
        %>
            </div>
        </div>       
        <%@include file="WEB-INF/includes/projectTitle.jspf" %>
    </body>
</html>
<%}%>