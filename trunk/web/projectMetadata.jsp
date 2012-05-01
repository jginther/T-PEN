<%-- 
    Document   : projectMetadata
    Created on : Jan 10, 2011, 4:08:07 PM
    Author     : jdeerin1
--%>
<%@page import="textdisplay.Manuscript"%>
<%@page import="user.User"%>
<%@page import="org.owasp.esapi.ESAPI" %>
<%@page import="textdisplay.Project" contentType="text/html; charset=UTF-8"  pageEncoding="UTF-8"%>
<%         
    int UID = 0;
           if (session.getAttribute("UID") == null) {
        %><%@ include file="loginCheck.jsp" %><%
                   } else {
                UID=Integer.parseInt(session.getAttribute("UID").toString());
                User thisUser = new User(UID);
%>
<%if (request.getParameter("projectID") == null) {
                out.print("Missing project identifier!");
                return;
            }
            int projectID = Integer.parseInt(request.getParameter("projectID"));
            Project thisProject = new Project(projectID);
            textdisplay.Metadata m = thisProject.getMetadata();
            if (request.getParameter("save") != null) {

                m.setMsCollection(request.getParameter("collection"));
                m.setMsIdNumber(request.getParameter("idno"));
                m.setMsIdentifier(request.getParameter("MSidentifier"));
                m.setMsSettlement(request.getParameter("settlement"));
                m.setTitle(request.getParameter("title"));
                m.setSubtitle(request.getParameter("subtitle"));
                m.setMsRepository(request.getParameter("repository"));
                m.setDescription(request.getParameter("description"));
                m.setLanguage(request.getParameter("language"));
                m.setLocation(request.getParameter("location"));
                m.setDate(request.getParameter("date"));
                m.setAuthor(request.getParameter("author"));
                m.setSubject(request.getParameter("subject"));
                m.commit();
            }
%>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN"
    "http://www.w3.org/TR/html4/loose.dtd">

<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title>Project Metadata</title>
        <link rel="stylesheet" href="css/tpen.css" type="text/css" media="screen, projection">
        <link rel="stylesheet" href="css/print.css" type="text/css" media="print">
        <!--[if lt IE 8]><link rel="stylesheet" href="css/ie.css" type="text/css" media="screen, projection"><![endif]-->
        <link type="text/css" href="css/custom-theme/jQuery.css" rel="Stylesheet" />
        <script type="text/javascript" src="https://ajax.googleapis.com/ajax/libs/jquery/1.7.1/jquery.js"></script>
        <script type="text/javascript" src="https://ajax.googleapis.com/ajax/libs/jqueryui/1.8.16/jquery-ui.js"></script> 
        <script type="text/javascript" src="js/tpen.js"></script>
        <style type="text/css">
            #customHeader {margin: 1em;background-color: rgba(255,255,255,.5);padding: .5em;display: block;color: gray;border:thin solid gray;}
            a.tpenButton {padding: .4em;}
        </style>
    </head>
    <body>
        <div id="wrapper">
            <div id="header"><p align="center" class="tagline">transcription for paleographical and editorial notation</p></div>
            <div id="content">
                <h1><script>document.write(document.title); </script></h1>
                <div class="ui-widget ui-widget-content ui-corner-all ui-tabs ui-helper-reset ui-helper-clearfix">
                    <h3 class="ui-widget-header ui-tabs ui-corner-all ui-state-default">Update Your Project Metadata</h3>
<%
if(request.getParameter("resetHeader")!=null) thisProject.setHeaderText("");
if (thisProject.getHeader().length()>0){
    String header = thisProject.getHeader();
    %>
    <a class="tpenButton ui-button" href="projectMetadata.jsp?resetHeader=true&projectID=<%out.print(projectID);%>" id="emptyHeader">Remove Custom Header</a>
    <p>You have uploaded a custom header for this project, which is displayed below. This header is read-only. To change it, remove the current header and upload a new file.</p>
    <%
    out.println("<span id='customHeader'>"+header+"</span>");
    if (header.length()>thisProject.getLinebreakCharacterLimit()-1){
        out.print("<span class='ui-state-error-text'>Preview limited to first "+thisProject.getLinebreakCharacterLimit()+" characters.</span>");
    };
} else {
%>
                    <p>Update the metadata for this project below or upload a custom header to override project metadata.</p>
                    <div style="width:40%" class="ui-tabs ui-widget-content ui-corner-bl ui-corner-tr left">
                        <form action="projectMetadata.jsp?projectID=<%out.print(projectID);%>" method="post">
                            <p><span class="label">Title</span><input type="text" name="title" value="<%out.print(ESAPI.encoder().decodeFromURL(m.getTitle()));%>"/><br>
                                <span class="label">Subtitle</span><input type="text" name="subtitle" value="<%out.print(m.getSubtitle());%>"/><br>
                                <span class="label">MS identifier</span><input type="text" name="MSidentifier" value="<%out.print(m.getMsIdentifier());%>"/><br>
                                <span class="label">MS settlement</span><input type="text" name="settlement" value="<%out.print(ESAPI.encoder().decodeFromURL(m.getMsSettlement()));%>"/><br>
                                <span class="label">MS Repository</span><input type="text" name="repository" value="<%out.print(ESAPI.encoder().decodeFromURL(m.getMsRepository()));%>"/><br>
                                <span class="label">MS Collection</span><input type="text" name="collection" value="<%out.print(ESAPI.encoder().decodeFromURL(m.getMsCollection()));%>"/><br>
                                <span class="label">MS id number</span><input type="text" name="idno" value="<%out.print(m.getMsIdNumber());%>"/><br>
                                <span class="label">Subject</span><input type="text" name="subject" value="<%out.print(m.getSubject());%>"/><br>
                                <span class="label">Author</span><input type="text" name="author" value="<%out.print(m.getAuthor());%>"/><br>
                                <span class="label">Date</span><input type="text" name="date" value="<%out.print(m.getDate());%>"/><br>
                                <span class="label">Location</span><input type="text" name="location" value="<%out.print(m.getLocation());%>"/><br>
                                <span class="label">Language</span><input type="text" name="language" value="<%out.print(m.getLanguage());%>"/><br>
                                <span class="label">Description</span><input type="text" name="description" value="<%out.print(m.getDescription());%>"/><br>
                            </p>
                            <span class="right"><input onclick="alert('This is a scheduled feature not yet tested and implemented in T&#8209;PEN.');return false;" id="import" class="tpenButton ui-button ui-state-disabled" type="button" value="Import from Schema" name="import" /><input class="tpenButton ui-button" type="submit" value="Save and Update" id="save" name="save" /></span>
                        </form></div>
                    <div class="left" style="margin-left: 10px;">
        <form id="fileUpload" action="uploadHeader?projectID=<%out.print(""+projectID);%>" ENCTYPE="multipart/form-data" method="POST">
            <input class="ui-button tpenButton" type="file" id="file" name="file"/>
     <br/><input class="ui-button tpenButton" type="submit" value="Upload" name="Upload"/>
     <input class="ui-button tpenButton" type="reset" value="Cancel" name="cancel" onclick="history.back();return false;" />
        </form>
                        <h3>Current Metadata</h3>
                        <p><span class="label">Title: </span><%out.print(m.getTitle());%><br />
                            <span class="label">Subtitle: </span><%out.print(m.getSubtitle());%><br />
                            <span class="label">MS&nbsp;Identifier: </span><%out.print(m.getMsIdentifier());%><br />
                            <span class="label">MS&nbsp;Settlement: </span><%out.print(m.getMsSettlement());%><br />
                            <span class="label">MS&nbsp;Repository: </span><%out.print(m.getMsRepository());%><br />
                            <span class="label">MS&nbsp;Collection: </span><%out.print(m.getMsCollection());%><br />
                            <span class="label">MS&nbsp;ID&nbsp;number: </span><%out.print(m.getMsIdNumber());%><br />
                            <span class="label">Subject</span><%out.print(m.getSubject());%><br>
                            <span class="label">Author</span><%out.print(m.getAuthor());%><br>
                            <span class="label">Date</span><%out.print(m.getDate());%><br>
                            <span class="label">Location</span><%out.print(m.getLocation());%><br>
                            <span class="label">Language</span><%out.print(m.getLanguage());%><br>
                            <span class="label">Description</span><%out.print(m.getDescription());%>
                            </p>
                    </div>
                            <%}%>
                </div>
                <a class="returnButton" href="project.jsp?selecTab=0&projectID=<%out.print(projectID);%>">Return to Project Management</a>
            </div>
        </div>
        <%@include file="WEB-INF/includes/projectTitle.jspf" %>
    </body>
</html>
<%}%>