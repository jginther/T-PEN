<%-- 
    Document   : addMStoProject
    Created on : Jan 11, 2011, 5:52:11 PM
    Author     : jdeerin1
--%>

<%@page import="textdisplay.Project"%>
<%@page import="org.owasp.esapi.ESAPI" %>
<%@page import ="user.*"%>

<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title>Add Manuscript to Project</title>
        <link rel="stylesheet" href="css/tpen.css" type="text/css" media="screen, projection">
        <link rel="stylesheet" href="css/print.css" type="text/css" media="print">
        <!--[if lt IE 8]><link rel="stylesheet" href="css/ie.css" type="text/css" media="screen, projection"><![endif]-->
        <link type="text/css" href="css/custom-theme/jQuery.css" rel="Stylesheet" />
        <script type="text/javascript" src="https://ajax.googleapis.com/ajax/libs/jquery/1.7.1/jquery.js"></script>
        <script type="text/javascript" src="https://ajax.googleapis.com/ajax/libs/jqueryui/1.8.16/jquery-ui.js"></script>
        <script src="js/tpen.js" type="text/javascript"></script>
        <script>
            $(function() {
                /* Click handlers*/
                $('#import').click(function(){
                    if($('#import').attr('checked')){
                        $(this).parent().switchClass('ui-state-highlight', 'ui-state-error');
                    } else {
                        $(this).parent().switchClass('ui-state-error', 'ui-state-highlight');
                    }
                });
                /* Equalize heights and widths */
                $("#projectList").ready(function(){         // list of current projects
                    var maxWidth = 120;
                    $("#projectList a").each(function(){
                        maxWidth = ($(this).width()>maxWidth) ? $(this).width() : maxWidth;
                    })
                    $("#projectList a").css({"width":maxWidth+"px","margin":"2px"});
                })
                $("#addMS").find("li").ready(function(){    // bounded action boxes       
                    var minHeight = 120;
                    $("li").each(function(){
                        minHeight = ($(this).height()>minHeight) ? $(this).height() : minHeight;                    
                    })
                    $("li").css({"height":minHeight+"px"});
                })
            });
        </script>
        <style>
            #addMS li {list-style:outside none;padding:3px;margin:3px;width:45%; max-height: 400px; overflow: auto;}
            label {padding:3px;margin:5px;cursor:pointer; float: none;font-weight: normal;width: auto; display: block;} /*escape the default tag formatting*/
            #orText {
                text-align: center;background: url("css/custom-theme/images/ui-bg_inset-hard_75_a8d2e4_1x100.png") repeat scroll 50% 50% transparent;position: absolute;top:-15px; width:40px;height:35px;padding-top:5px;left:43%;color:#618797; font: italic 20px serif;
                -moz-box-shadow: -1px 1px 4px black;
                -webkit-box-shadow:-1px 1px 4px black;
                box-shadow:-1px 1px 4px black;
                -moz-border-radius:20px;
                -webkit-border-radius:20px;
                border-radius:20px 20px 20px 20px;
            }
            button {padding: .4em;}
        </style>
    </head>
    <body>
        <%
            int UID = 0;
            if (session.getAttribute("UID") == null) {
        %>
        <%@ include file="loginCheck.jsp" %>
        <%        } else {
            UID = Integer.parseInt(session.getAttribute("UID").toString());
            user.User thisUser = new user.User(UID);
            //Create a new project and add the ms to it, then allow the user to set the name of the project
            if (request.getParameter("newProject") != null) {
                int folioNum = 0;
                if (request.getParameter("p") != null) {
                    folioNum = Integer.parseInt(request.getParameter("p"));
                    textdisplay.Folio f = new textdisplay.Folio(folioNum);
                }
                if (request.getParameter("ms") != null) {
                    textdisplay.Manuscript ms = new textdisplay.Manuscript(Integer.parseInt(request.getParameter("ms")), true);
                    folioNum = ms.getFirstPage();
                }
                if (folioNum == 0) {
                    out.print("Error:page number not included!");
                    return;
                }
                textdisplay.Folio projectFolio = new textdisplay.Folio(folioNum);
                String tmpProjName = new textdisplay.Manuscript(folioNum).getShelfMark() + " project";
                if (request.getParameter("title") != null) {
                    tmpProjName = request.getParameter("title");
                }
                user.Group newgroup = new user.Group(tmpProjName, UID);
                textdisplay.Project newProject = new textdisplay.Project(tmpProjName, newgroup.getGroupID());
                textdisplay.FolioSet f = new textdisplay.FolioSet(projectFolio.getArchive(), projectFolio.getCollectionName());
                textdisplay.Manuscript ms = new textdisplay.Manuscript(folioNum);
                newProject.setFolios(ms.getFolios(), newProject.getProjectID());

                newProject.addLogEntry("<span class='log_manuscript'></span>Added manuscript " + ms.getShelfMark(), UID);
                if (request.getParameter("import") != null && request.getParameter("import").compareTo("import") == 0) {
                    newProject.importData(UID);
                }
                //Now redirect them to the project config screen so they can modify the included pages and sequence
%>
        <script>
                        document.location = "project.jsp?projectID=<%out.print(newProject.getProjectID());%>";
        </script>
        <%
            //response.sendRedirect("project.jsp?projectID=" + newProject.getProjectID());
        } else {
            //add to an existing project
            if (request.getParameter("projectID") != null) {
                int projectID = Integer.parseInt(request.getParameter("projectID"));
                int folioNum = 0;
                if (request.getParameter("p") != null) {
                    folioNum = Integer.parseInt(request.getParameter("p"));
                }
                if (request.getParameter("ms") != null) {
                    textdisplay.Manuscript ms = new textdisplay.Manuscript(Integer.parseInt(request.getParameter("ms")), true);
                    folioNum = ms.getFirstPage();
                }
                if (folioNum == 0) {
                    out.print("Error:page number not included!");
                    return;
                }
                textdisplay.Folio projectFolio = new textdisplay.Folio(folioNum);
                textdisplay.Project newProject = new textdisplay.Project(projectID);
                textdisplay.FolioSet f = new textdisplay.FolioSet(projectFolio.getCollectionName());
                textdisplay.Folio[] originalFolios = newProject.getFolios();
                textdisplay.Folio[] additionalFolios = f.getAsArray();
                textdisplay.Folio[] allFolios = new textdisplay.Folio[originalFolios.length + additionalFolios.length];
                for (int i = 0; i < originalFolios.length; i++) {
                    allFolios[i] = originalFolios[i];
                }
                for (int i = 0; i < additionalFolios.length; i++) {
                    allFolios[i + originalFolios.length] = additionalFolios[i];
                }
                newProject.setFolios(allFolios, newProject.getProjectID());
                textdisplay.Manuscript ms = new textdisplay.Manuscript(folioNum);
                newProject.addLogEntry("<span class='log_manuscript'></span>Added manuscript " + ms.getShelfMark(), UID);
                //redirect to project page to manage this project
%>
        <script>
                        document.location = "project.jsp?projectID=<%out.print(projectID);%>";
        </script>
        <%
                    //response.sendRedirect("project.jsp?projectID=" + projectID);
                }
            }
            //they are logged in, ask them what project they want to add this MS to, including a new project
            int folioNum = 0;
            String msName = "this manuscript";
            textdisplay.Folio f = new textdisplay.Folio(folioNum);
            if (request.getParameter("p") != null) {
                folioNum = Integer.parseInt(request.getParameter("p"));
                msName = new textdisplay.Manuscript(folioNum).getShelfMark();
            }
            if (request.getParameter("ms") != null) {
                textdisplay.Manuscript ms = new textdisplay.Manuscript(Integer.parseInt(request.getParameter("ms")), true);
                folioNum = ms.getFirstPage();
                msName = ms.getShelfMark();
            }
        %>
        <div id="wrapper">
            <div id="header">
                <p align="center" class="tagline">transcription for paleographical and editorial notation</p>
            </div>
            <div id="content">
                <h1><script>document.write(document.title); </script></h1>
                <div class="ui-widget ui-widget-content ui-corner-all ui-tabs ui-helper-reset ui-helper-clearfix">
                    <h3 class="ui-widget-header ui-tabs ui-corner-all ui-state-default"><%out.print(msName);%></h3>
                    <ul id="addMS" style="position:relative;" class="ui-helper-reset">
                        <span id="orText">-or-</span>
                        <li class="left ui-widget-content ui-corner-bl ui-corner-tr">
                            <h3>Create New Project</h3>
                            <p>This manuscript will appear with the following project title on the <span class="helpText" title="After the project is created, you may rearrange the images, add others to your project team, and more.">Project Management</span> page.</p>
                            <form action="addMStoProject.jsp" method="get">
                                <strong>Project Title:</strong> <input type="text" name="title" value="<%out.print(ESAPI.encoder().decodeFromURL(f.getCollectionName()));%>" style="width: 300px;"/><br>
                                <input type="hidden" name="p" value="<%out.print("" + folioNum);%>" />
                                <button class="tpenButton right" type="submit" name="newProject" value="Create">Create New Project</button>
                            </form>    
                            <h3 class="clear">Coordinating Projects</h3>
                            <p>Each <span title="A project is created whenever you click an 'Add to Project' link and complete the form on this page." class="helpText">project</span> is a virtual collection of manuscripts, organized by the <span title="The creator of each project is the Group Leader by default." class="helpText">Group Leader</span>. Any project can be expanded to multiple manuscripts.</p>
                            <p>When a manuscript is added to a project, all the associated page images are also added. Project images can be rearranged or deleted through the <a href="project.jsp">Project Management</a> page.</p>
                        </li>
                        <li class="left ui-widget-content ui-corner-bl ui-corner-tr">
                            <h3>Add Images to Existing Project</h3>
                            <p>Include the images from this manuscript at the end of an <span class="helpText" title="On the Project Management page, you may rearrange the images, add others to your project team, and more.">existing project</span> creating a <span class="helpText" title="This collection of images will retain all references to the source images and your associated data, but allows the group members to include images from serveral sources in one project.">virtual manuscript</span>.</p>
                            <%
                                textdisplay.Project[] allProjects = thisUser.getUserProjects();
                                if (allProjects.length > 0) {
                                    out.print("<p>Add <em>" + msName + "</em> to an existing project:</p><span id=\"projectList\">");
                                    for (int i = 0; i < allProjects.length; i++) {
                                        out.print("<a title=\"Click to add the manuscript to this project.\" class=\"tpenButton left\" href=addMStoProject.jsp?p=" + folioNum + "&projectID=" + allProjects[i].getProjectID() + ">Add to " + allProjects[i].getProjectName() + "</a>");
                                    }
                                    out.print("</span>");
                                } else {
                                    out.print("<p>You have no projects. Please type a name for this new project to the left.</p>");
                                }
                            %>
                        </li>
                    </ul>                
                </div>
                <a class="returnButton" href="project.jsp">Return to Project Management</a>
            </div>
        </div>
        <%}%>
    </body>
</html>
