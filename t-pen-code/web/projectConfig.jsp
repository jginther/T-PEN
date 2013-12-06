<%-- 
FILE NOT IN DIRECT USE
    Document   : projectConfig
    Created on : Nov 10, 2010, 2:06:34 PM
    Author     : jdeerin1
--%>
<%@page import ="textdisplay.FolioSet"%>
<%@page import ="textdisplay.Folio"%>
<%@page import ="textdisplay.Project"%>
<%@page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<jsp:useBean id="proj" class="textdisplay.Project" scope="page" />
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN"
   "http://www.w3.org/TR/html4/loose.dtd">
<%         
            if (session.getAttribute("UID") == null) {
        %><%@ include file="loginCheck.jsp" %><%
                   } else {
%><%
if(request.getParameter("submitted")!=null)
    {
        request.getParameterMap();
        Project p=new Project(1);
        Folio [] f=p.makeFolioArray(request.getParameterMap());
        p.setFolios(f, 1);
    }
%>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title>Remove Images</title>
        <link rel="stylesheet" href="css/tpen.css" type="text/css" media="screen, projection">
        <link rel="stylesheet" href="css/print.css" type="text/css" media="print">
        <!--[if lt IE 8]><link rel="stylesheet" href="css/ie.css" type="text/css" media="screen, projection"><![endif]-->
        <link type="text/css" href="css/custom-theme/jQuery.css" rel="Stylesheet" />
        <script type="text/javascript" src="https://ajax.googleapis.com/ajax/libs/jquery/1.7.1/jquery.js"></script>
        <script type="text/javascript" src="https://ajax.googleapis.com/ajax/libs/jqueryui/1.8.16/jquery-ui.js"></script> 
    </head>
    <body>
        <div id="wrapper">
            <div id="header"><p align="center" class="tagline">transcription for paleographical and editorial notation</p></div>
            <div id="content">
        <h1><script>document.write(document.title); </script></h1>
                <div class="ui-widget ui-widget-content ui-corner-all ui-tabs ui-helper-reset ui-helper-clearfix">
        <form action="projectConfig.jsp" method="POST">
            <c:if test="${not empty param.projectname}">
                <c:set target="${proj}" property="groupID" value="1"/>
                <c:set target="${proj}" property="projectName" value="${param.projectname}"/>
                <%int newProjID= proj.build(); %>
            </c:if>
            <c:choose>
                <c:when test="${empty param.projectID}">
                    <input type="text" name="projectname" value="Choose a project name"><br>
                </c:when>
                <c:otherwise>
                    <input type="hidden" name="projectID" value="<c:out value="${param.projectID}" />"/>
                </c:otherwise>
            </c:choose>

         Uncheck a page if you don't want to transcribe it:<br>
        <%
        int pageno=0;
        Project p;
        if(request.getParameter("folio")!=null)
        {
            pageno= Integer.parseInt(request.getParameter("folio"));
             Folio fol=new Folio(pageno);
        FolioSet f=new FolioSet(fol.getCollectionName());
        out.print(f.listAll());
        }
        if(request.getParameter("projectID")!=null)
        {
             p=new Project(Integer.parseInt(request.getParameter("projectID")));
             out.print(p.checkBoxes());

        }
        
       
        %>
        <input type="hidden" name="folio" value="<%out.print(""+pageno);%>">
        <input type="submit" id="submitted" name="submitted"/>
        </form>
                </div>
                <a href="project.jsp?projectID=<c:out value="${param.projectID}" />">Return to project page</a><br>
            </div></div>
    </body>
</html>
<%}%>