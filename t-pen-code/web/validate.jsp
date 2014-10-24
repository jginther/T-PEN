<%-- 
    Document   : validate
    Created on : Apr 18, 2012, 1:09:59 PM
    Author     : obi1one
--%>

<%@page import="utils.DatastoreValidator"%>
<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN"
   "http://www.w3.org/TR/html4/loose.dtd">

<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title>JSP Page</title>
    </head>
    <body>
        <%
        user.User thisUser = null;
                        if (session.getAttribute("UID") != null) {
                            thisUser = new user.User(Integer.parseInt(session.getAttribute("UID").toString()));
                            if(!thisUser.isAdmin())
                                response.sendError(403);
                            }
        else
            {
                                response.sendError(403);
            }
        if(request.getParameter("checkEmptyMSS")!=null)
        {
        //manuscripts with no images
        DatastoreValidator v=new DatastoreValidator();
        out.print(v.checkEmptyManuscripts().replace("\n", "<br>"));
        out.print("<a href=\"validate.jsp?deleteEmptyMSS=true\">Delete all of these manuscripts</a>");
        }
        if(request.getParameter("deleteEmptyMSS")!=null)
        {
        DatastoreValidator v=new DatastoreValidator();
        out.print(v.deleteEmptyManuscripts().replace("\n", "<br>"));
        }
        //projects with no images
        if(request.getParameter("checkEmptyProjects")!=null)
        {
        DatastoreValidator v=new DatastoreValidator();
        out.print(v.checkEmptyProjects().replace("\n", "<br>"));
        out.print("<a href=\"validate.jsp?deleteEmptyProjects=true\">Delete all of these projects</a>");
        }
        if(request.getParameter("deleteEmptyProjects")!=null)
        {
        DatastoreValidator v=new DatastoreValidator();
        out.print(v.deleteEmptyProjects().replace("\n", "<br>"));
        }
        //Images attached to a non existant manuscript
        if(request.getParameter("checkOrphanedImages")!=null)
        {
        DatastoreValidator v=new DatastoreValidator();
        out.print(v.checkOrphanedImages().replace("\n", "<br>"));
        out.print("<a href=\"validate.jsp?deleteOrphanedImages=true\">Delete all of these images</a>");
        }
        if(request.getParameter("deleteOrphanedImages")!=null)
        {
        DatastoreValidator v=new DatastoreValidator();
        out.print(v.deleteOrphanedImages().replace("\n", "<br>"));
        }
        //project folios attached to delted projects
        if(request.getParameter("checkOrphanedProjectImages")!=null)
        {
        DatastoreValidator v=new DatastoreValidator();
        out.print(v.checkOrphanedProjectImages().replace("\n", "<br>"));
        out.print("<a href=\"validate.jsp?deleteOrphanedProjectImages=true\">Delete all of these records</a>");
        }
        if(request.getParameter("deleteOrphanedProjectImages")!=null)
        {
        DatastoreValidator v=new DatastoreValidator();
        out.print(v.deleteOrphanedProjectImages().replace("\n", "<br>"));
        }
        //Transcriptions based on a non existant image
        if(request.getParameter("checkTranscriptionsOnOrphanedImages")!=null)
        {
        DatastoreValidator v=new DatastoreValidator();
        out.print(v.checkTranscriptionsOnOrphanedImages().replace("\n", "<br>"));
        out.print("<a href=\"validate.jsp?deleteTranscriptionsOnOrphanedImages=true\">Delete all of these records</a>");
        }
        if(request.getParameter("deleteTranscriptionsOnOrphanedImages")!=null)
        {
        DatastoreValidator v=new DatastoreValidator();
        out.print(v.deleteTranscriptionsOnOrphanedImages().replace("\n", "<br>"));
        }
        //Transcriptions that belogn to a non existant project
        if(request.getParameter("checkTranscriptionsOnOrphanedProjects")!=null)
        {
        DatastoreValidator v=new DatastoreValidator();
        out.print(v.checkTranscriptionsOnOrphanedProjects().replace("\n", "<br>"));
        out.print("<a href=\"validate.jsp?deleteTranscriptionsOnOrphanedProjects=true\">Delete all of these records</a>");
        }
        if(request.getParameter("deleteTranscriptionsOnOrphanedProjects")!=null)
        {
        DatastoreValidator v=new DatastoreValidator();
        out.print(v.deleteTranscriptionsOnOrphanedProjects().replace("\n", "<br>"));
        }
        %>
        <a href="validate.jsp?checkEmptyMSS=true">Check for manuscripts with no images</a><br>
        <a href="validate.jsp?checkEmptyProjects=true">Check for projects with no images</a><br>
        <a href="validate.jsp?checkOrphanedImages=true">Check for images attached to a non existant manuscript</a><br>
        <a href="validate.jsp?checkOrphanedProjectImages=true">Check for images attached to a non existant project</a><br>
        <a href="validate.jsp?checkTranscriptionsOnOrphanedImages=true">Check for transcriptions associated with a non existant image</a><br>
        <a href="validate.jsp?checkTranscriptionsOnOrphanedProjects=true">Check for transcriptions associated with a non existant project</a><br>

        

    </body>
</html>
