<%-- 
    Document   : project
    Created on : Aug 6, 2009, 11:44:14 AM
    Author     : jdeerin1
--%>

<%@page import="textdisplay.ProjectPermissions"%>
<%@page import="org.apache.commons.lang.StringEscapeUtils"%>
<%@page import="utils.Tool"%>
<%@page import="utils.UserTool"%>
<%@page import="textdisplay.Metadata"%>
<%@page import="textdisplay.PartnerProject"%>
<%@page import="textdisplay.Project" contentType="text/html; charset=UTF-8"  pageEncoding="UTF-8"%>
<%@page import="user.User" import="user.Group"%>
<%@page import="textdisplay.Manuscript" %>
<%@page import ="textdisplay.Hotkey" %>
<%@page import ="textdisplay.TagButton"%>
<%@page import="org.owasp.esapi.ESAPI" %>

<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN"
    "http://www.w3.org/TR/html4/loose.dtd">
<%
    if (session.getAttribute("UID") == null) {
%><%@ include file="loginCheck.jsp" %><%        } else {
%>
<%
    int UID = 0;
    user.User thisUser = null;
    if (session.getAttribute("UID") != null) {
        UID = Integer.parseInt(session.getAttribute("UID").toString());
        thisUser = new user.User(UID);
    }
%>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title>Transcription Project Management</title>
        <link rel="stylesheet" href="css/tpen.css" type="text/css" media="screen, projection">
        <link rel="stylesheet" href="css/print.css" type="text/css" media="print">
        <!--[if lt IE 8]><link rel="stylesheet" href="css/ie.css" type="text/css" media="screen, projection"><![endif]-->
        <link type="text/css" href="css/custom-theme/jQuery.css" rel="Stylesheet" />
        <script type="text/javascript" src="https://ajax.googleapis.com/ajax/libs/jquery/1.7.1/jquery.js"></script>
        <script type="text/javascript" src="https://ajax.googleapis.com/ajax/libs/jqueryui/1.8.16/jquery-ui.js"></script> 
        <script src="js/manuscriptFilters.js" type="text/javascript"></script>
        <script src="js/tpen.js" type="text/javascript"></script>
        <style>
            #footer { width: 1010px; position: fixed;left:0;right:0; bottom:0;margin: 0 auto;}
            #foot { background: url(images/footer.png) top left no-repeat;position:relative;padding:50px 125px; }
            #button { text-decoration: none; }
        </style>
        <style type="text/css" >
            body,#wrapper {height: 100%;max-height: 100%;overflow: visible;}
            #project, #ms, #team, #options, #export { margin: 0; padding: 0;}
            #project li, #ms li, #team li, #options li,#export li {padding: 0.4em; padding-left: 1.5em; height: 100%;overflow: hidden; float:left; width:32%; position: relative;margin-right: 1%;}
            #project li ul,#ms li ul,#team li ul, #options li ul, #export li ul {padding: 0;margin: 0;}
            #project li ul li,#ms li ul li,#team li ul li, #options li ul li, #export li ul li {list-style:outside none;padding:0;width:100%;}
            #project div.tall {position: absolute;right:0;width: 29%;}
            #project div.tall li {position: absolute;right:20px;height:48%;margin:0;width:100%;min-height: 120px;}
            #project div.tall li:last-child {bottom:-13px;min-height: 80px;}
            #projectList {max-height: 300px;overflow: auto;}
            #projectList li {margin: 0;}
            a.tpenButton,button.tpenButton {padding: 2px; margin:2px; display:block;text-align: center; text-decoration: none;min-width: 90px;min-height:16px;}
            .stretch {font-size: 0.8em !important;}
            .shrink {width: 3em !important;font-size: 0.8em !important;}
            .grow {width:7em !important;}
            .bold, span.bold input {font-weight: bold; border: none;}
            span.bold input {width: 60px; font-size: .8em;}
            .ui-icon-closethick:hover {background-image: url(css/custom-theme/images/ui-icons_cd0a0a_256x240.png);}
            .disabled { margin: 0 3px 3px 23px; padding: 0.4em; padding-left: 1.5em; font-size: 1.4em; height: 18px;width:650px;}
            #addToProject,#addingTools {position: fixed; top:25%; left:25%; width:50%; display: none;z-index: 501;}
            #addingTools {
                box-shadow: 0 0 0 2000px rgba(0,0,0,.4);
                padding: 10px;
            }
            #addToolCommit, #addToolPreview {
                display: block;
                float: left;
            }
            #addToolName, #addToolURL {
                display: block;
                width: 50%;
            }
            #addToolFrame{
                float: right;
                height: 350px;
                width: 50%;
                position: relative;}
            #addToProject:after {
                content: "";
                width: 100%;
                height: 100%;
                position: fixed;
                pointer-events:none;
                z-index: 500;
                box-shadow: 0 0 200px black inset;
                top: 0;
                left: 0;}
            #form {height: 425px; margin: 0 auto; width:515px;border-width: 1px; 
                   -webkit-box-shadow: -1px -1px 12px black;
                   -moz-box-shadow: -1px -1px 12px black;
                   box-shadow: -1px -1px 12px black;
            }
            .delete {position:absolute;top:6px;right:6px;}
            input.text {width:100%;}
            #logFilter a:not(#openNote) {width:46%;float:left;margin-left: 1%;}
            #closePopup:hover {color: crimson;cursor: pointer;}
            .label {text-align: right; margin-right: 10px;}
            #inviteFeedback span {display:block;}
            #currentSetting {font-weight: bold;}
            #currentSetting:before {content:'Currently set to: "';color:black;font-weight: normal;}
            #currentSetting:after {content:'"';color:black;font-weight: normal;}
            .loadingBook {background-position: center center !important;}
            #xmlImport,#xmlValidate,#linebreaking,#wordbreak,#inviteUser,#template {margin:-4px 5px 2px;background: url(images/linen.png);padding:6px 2px 2px;display:none;overflow: hidden;z-index: 1; border: 1px solid #A68329;
                                                                                    -moz-box-shadow: -1px -1px 2px black;
                                                                                    -webkit-box-shadow: -1px -1px 2px black; 
                                                                                    box-shadow:-1px -1px 2px black;}
            #xmlImportBtn,#xmlValidateBtn,#linebreakingBtn,#wordbreakBtn,#inviteUserBtn,#templateBtn {padding:2px;text-align: center;position: relative;z-index: 2;cursor: pointer;width:100%;-o-box-sizing:border-box;-webkit-box-sizing:border-box;-moz-box-sizing:-ms-border-box;box-sizing:border-box;}
            #template label {width: 100%;}
            #template textarea {height: 3em;max-width: 100%;}
            .format{padding:2px;text-align: center;position: relative;z-index: 2;cursor: pointer;}
            .formatDiv{margin:-4px 5px 2px;clear:left;background: url(images/linen.png);padding:6px 2px 2px;display:none;overflow: hidden;z-index: 1; border: 1px solid #A68329;
                       -moz-box-shadow: -1px -1px 2px black;
                       -webkit-box-shadow: -1px -1px 2px black; 
                       box-shadow:-1px -1px 2px black;}
            #pdfOptions {display:block;}
            #exportLinebreakString,#exportWordbreak{width: 40px;}
            #quickExport span.choices {width:32%;float:left;position:relative;padding:15px 0;margin-bottom: -4px;}
            #quickExport {position:relative;}
            #quickExport input[type=radio] {visibility:visible;position: absolute;}
            .xmlRemove {display: none;clear: left;}
            .xmlShow {clear:left;}
            #xmlTags {display:block;}
            #quickExport h4 {position: relative;margin-bottom: 0;}
            .xmlFormat span {display: block;float:left;clear:left;width:40%;max-height: 18px;overflow: hidden;}
            #pageRange select {max-width: 175px;position: absolute;left:110px;}
            #exportTags select {position: absolute;right: 90px;}
            #samplePreview {overflow: hidden;position: relative;}
            #samplePreview img {width: auto;height: auto; position: absolute;left:0;top:30px;}
            #samplePreview h3 {position:relative;z-index: 2;}
            #samplePreview img, #samplePreview h3 {cursor: -moz-zoom-in, move;}
            #export label {font-weight: normal;width:auto;margin:0; padding:0;line-height: 24px; float: none;} /* override defaults */
            #export h3 {margin-top: 1em;margin-bottom: 0;}
            #export button {padding:8px 4px;width:33%;margin:0px;height:50px;}
            .xmlDisclaimer {display:none;float: left;clear: left;}
            #exportAlert {display: none;width:100%;}
            .partnerListing {width:100%;padding:2px;}
            .partnerName {width:100%;font-size: 125%;font-family: serif;display: block;}
            .partnerDescription {width:100%;padding:3px;margin:2px;display: block;white-space: normal;}
            .partnerSelected {background: green;}
            #QENI {overflow: auto;width: 100%;}
            #tpenSubmit,#disconnect {width:100%;margin-bottom: 1.25em;}
            #deleteConnection{display: none;}
            span + h4[id] {margin-top:1.25em;}
            .projectTools,.userTools {font-weight: normal;width: auto;padding: 0;clear: left;}
            #customHeader {margin: 1em;background-color: rgba(255,255,255,.5);padding: .5em;display: block;color: gray;border:thin solid gray;overflow: auto;text-overflow:ellipsis;max-height: 300px;font-size: smaller;}
            #projectOrdering {position: fixed !important;width:50% !important;right:auto !important;} /* battling weird Chrome bug */ 
        </style>
        <%
            String projectAppend = "";
            Boolean isMember,permitOACr,permitOACw,permitExport,permitCopy,permitModify,permitAnnotation,permitButtons,permitParsing,permitMetadata,permitNotes,permitRead;
            isMember=permitOACr=permitOACw=permitExport=permitCopy=permitModify=permitAnnotation=permitButtons=permitParsing=permitMetadata=permitNotes=permitRead=false;
            int projectID = 0;
            if (request.getParameter("projectID") != null) {
                projectID = Integer.parseInt(request.getParameter("projectID"));
                projectAppend = "&projectID=" + projectID;
                Project thisProject = new Project(projectID);
                Group thisGroup = new Group(thisProject.getGroupID());
                isMember = thisGroup.isMember(UID);
                ProjectPermissions permit = new ProjectPermissions(projectID);
                permitOACr = permit.getAllow_OAC_read();
                permitOACw = permit.getAllow_OAC_write();
                permitExport = permit.getAllow_export();
                permitCopy = permit.getAllow_public_copy();
                permitModify = permit.getAllow_public_modify();
                permitAnnotation = permit.getAllow_public_modify_annotation();
                permitButtons = permit.getAllow_public_modify_buttons();
                permitParsing = permit.getAllow_public_modify_line_parsing();
                permitMetadata = permit.getAllow_public_modify_metadata();
                permitNotes = permit.getAllow_public_modify_notes();
                permitRead = permit.getAllow_public_read_transcription();
                Boolean permitManage = permitExport || permitCopy || permitRead || permitButtons || permitMetadata;
                if (!isMember && !permitManage) {
                String errorMessage = thisUser.getFname() + ", you are not a member of this project.";
            %><%@include file="WEB-INF/includes/errorBang.jspf" %><%
                return;
                }
            } else {
                if (request.getParameter("delete") != null) {
                    //do they have permission to do that?
                    int projectNumToDelete = Integer.parseInt(request.getParameter("projDelete"));
                    textdisplay.Project todel = new textdisplay.Project(projectNumToDelete);
                    user.Group projectGroup = new user.Group(todel.getGroupID());
                    if (projectGroup.isAdmin(UID)) {
                        if (todel.delete()) {
                            //redirect to first project
                            out.print("<script>document.location=\"project.jsp\";</script>");
                            return;
                            //                   textdisplay.project[] p=thisUser.getUserProjects();
                            //                   if(p.length>0)
                            //                       projectID=p[0].getProjectID();
                        }
                        //              else
                        //                  {
                        //                   //failed deleting due to some error..shoudlnt happen
                        //                   }
                        textdisplay.Project[] p = thisUser.getUserProjects();
                        if (p.length > 0) {
                            projectID = p[0].getProjectID();
                        }
        %>
        <script>
            document.location = "project.jsp?projectID=<%out.print(projectID);%>";
        </script>
        <%                        //response.sendRedirect("project.jsp?projectID=" + projectID);
            } else {
                //couldnt delete, you arent the project creator. You can remove yourself from the group working on this project by visting ...
            }
        } else {
            textdisplay.Project[] p = thisUser.getUserProjects();
            if (p.length > 0) {
                projectID = p[0].getProjectID();
            }
            if (projectID > 0) {
        %>
        <script>
            document.location = "project.jsp?projectID=<%out.print(projectID);%>";
        </script>
        <%                         //response.sendRedirect("project.jsp?projectID=" + projectID);
                    } else {
                        out.print("<div class=\"error\">No project specified!</div>");
                    }
                    return;
                }
            }
            textdisplay.Project thisProject = new textdisplay.Project(projectID);
            //have they invited a new user?
            if (request.getParameter("invite") != null) {
                int result = thisUser.invite(request.getParameter("uname"), request.getParameter("fname"), request.getParameter("lname"));
                if (result == 0) {
                    user.Group g = new user.Group(thisProject.getGroupID());
                    if (g.isAdmin(thisUser.getUID())) {

                        user.User newUser = new user.User(request.getParameter("uname"));
                        g.addMember(newUser.getUID());
        %><script>
            $(function() {$('#inviteFeedback').html('<span class=\"ui-state-highlight ui-corner-all\"><%out.print(request.getParameter("fname"));%> has been invited to the project. A notification has been sent to an administrator for activation.<span>');
            });
            //alert('User has been invited, but must be activated by an administrator. The administrator has been notified');
        </script>
        <%
        } else {
        %><script>
            $(function() {$('#inviteFeedback').html('<span class=\"ui-state-error ui-corner-all\"><%out.print(request.getParameter("fname"));%> has been invited to TPEN, but only the project leader may add someone to a project. A notification has been sent to an administrator for activation.<span>');
                $('#inviteFeedback').effect('pulsate',500);
            });
            //alert('User has been invited to use TPEN, but the project leader must invite them to join the project. Also, the account must be activated by an administrator. The administrator has been notified');
        </script>
        <%
                }

            }
            if (result == 1) {
                //failed to create new user account! Likely because it already exists
%>
        <script>
            $(function() {$('#inviteFeedback').html('<span class=\"ui-state-error ui-corner-all\"><%out.print(request.getParameter("fname"));%> seems to have an account already, please use the "Add to Project" option to include them on your team. If you have any further trouble, please contact TPEN.<span>');
                $('#inviteFeedback').effect('pulsate',500);
            });
            //alert('Failed to invite new user, likely because they are already using TPEN!');
        </script>
        <%
            }
            if (result == 2) {
                //account created but email issue occured, usually happens in dev environments with no email server.
                user.Group g = new user.Group(thisProject.getGroupID());
                if (g.isAdmin(thisUser.getUID())) {

                    user.User newUser = new user.User(request.getParameter("uname"));
                    g.addMember(newUser.getUID());
        %><script>
            $(function() {$('#inviteFeedback').html('<span class=\"ui-state-error ui-corner-all\"><%out.print(request.getParameter("fname"));%> was successfully invited, but an e-mail error occurred. Please contact TPEN.<span>');
                $('#inviteFeedback').effect('pulsate',500);
            });
            //alert('The user invitation was successful, but a possible email error occured. Please contact the TPEN team.');
        </script>
        <%
        } else {
        %><script>
            $(function() { $('#inviteFeedback').html('<span class=\"ui-state-error ui-corner-all\"><%out.print(request.getParameter("fname"));%> was successfully invited, but an e-mail error occurred. Please contact TPEN. To add <%out.print(request.getParameter("fname"));%> to this project, the request must be made by the project leader.<span>');
                $('#inviteFeedback').effect('pulsate',500);
            });
            //alert('The user invitation was successful, but a possible email error occured. Please contact the TPEN team. Also, your project leader will need to invite them to join the project');
        </script>
        <%
                    }


                }

            }
        %>
        <script type="text/javascript">
            var minWidth = 10;
            function equalWidth(){
                $("#allXML").children("span").each(function(){
                    minWidth = ($(this).width()>minWidth) ? $(this).width() : minWidth;
                }).css({"min-width":minWidth+"px"});
            }
            var selecTab<%if (request.getParameter("selecTab") != null) {
                    out.print("=" + request.getParameter("selecTab"));
                }%>;
                    function deleteProject (user, project, title) {     //Confirm and delete project when user clicks x icon
                        var bDelete = confirm('This action will delete \"'+unescape(title)+'\" for all members. Are you sure?','Confirm Project Delete');
                        if(bDelete){
                            document.location = 'project.jsp?UID=' + user + '&delete=true&projDelete=' + project;
                        }
                    }
                    function simpleFormValidation (){
                        var field1=document.forms["invite"]["fname"].value;
                        var field2=document.forms["invite"]["lname"].value;
                        var field3=document.forms["invite"]["uname"].value;
                        if (field1==null || field1=="")
                        {
                            alert("First name must be filled out");
                            return false;
                        }
                        if (field2==null || field2=="" || field2.length<2)
                        {
                            alert("Last name must be filled out");
                            return false;
                        }
                        var atpos=field3.indexOf("@");
                        var dotpos=field3.lastIndexOf(".");
                        if (atpos<1 || dotpos<atpos+2 || dotpos+2>=field3.length)
                        {
                            alert("Not a valid e-mail address");
                            return false;
                        }
                    }
                    $(window).load(function(){
                        equalHeights("tall",100);
                        //$("body").removeClass("ui-state-disabled",500);
                        $("a:contains('Resume Transcribing')").parent().each(function(){
                            $(this).removeClass('loadingBook').css("background","url('<%
                int pageno = 501;
                try {
                    if (request.getParameter("p") != null) {
                        pageno = Integer.parseInt(request.getParameter("p"));
                    } else {
                        pageno = thisProject.firstPage();
                    }
                } catch (NumberFormatException e) {
                }
                textdisplay.Folio thisFolio = new textdisplay.Folio(pageno, true);
                                out.print(thisFolio.getImageURLResize(600));%>&quality=30') -30px -60px no-repeat");
                                        });
                                        //cache the page image for the project after loading the page
                                        //                msImage = new Image();
                                        //                msImage.src = <%out.print(thisFolio.getImageURLResize(2000));%>;
                                        //                $("#samplePreview").append("<span style='z-index:5;position:absolute;bottom:0'></span>");
                                           
        });
        </script>
    </head>
    <body>
        <div id="wrapper">
            <div id="header"><p align="center" class="tagline">transcription for paleographical and editorial notation</p></div>
            <div id="content">
                <h1><script>document.write(document.title); </script></h1>
                <p>Use this page to coordinate your team, design customized projects from available manuscripts, and make fine adjustments to individual pages.</p>
                <div id="outer-barG">
                    <div id="front-barG" class="bar-animationG">
                        <div id="barG_1" class="bar-lineG">
                        </div>
                        <div id="barG_2" class="bar-lineG">
                        </div>
                        <div id="barG_3" class="bar-lineG">
                        </div>
                    </div>
                </div>
                <div id="tabs">
                    <ul>
                        <li><a title="Switch between projects or manage pages" href="#tabs-1">Projects</a></li>
                        <li><a title="Alter linebreaks and parsings" href="#tabs-2">Manuscripts</a></li>
                        <li><a title="Organize your team" href="#tabs-3">Collaboration</a></li>
                        <li><a title="Project Options" href="#tabs-4">Options</a></li>
                        <li><a title="Export Options" href="#tabs-5">Export</a></li>
                    </ul>
                    <div id="tabs-1">
                        <ul id="project" class="ui-helper-reset">
                            <li class="ui-widget-content ui-corner-tr ui-corner-bl tall">
                                <h3>Project Selection</h3>
                                <%
                                user.Group thisGroup = new user.Group(thisProject.getGroupID());
                                User[] leader = thisGroup.getLeader();
                                if (!isMember){
                                %>
                                <p class="ui-state-highlight">You are viewing public project, <span class="loud bold"><%out.print(ESAPI.encoder().decodeFromURL(thisProject.getProjectName()));%></span>. To manage one of your own projects, click below.</p>
                                <%} else {%>
                                <p>Active project is <span class="loud"><%out.print(ESAPI.encoder().decodeFromURL(thisProject.getProjectName()));%></span>.</p>
                                <p> To manage a different project, select below. Clicking the X will delete your project.</p>
                                <%}%>
                                <h6>All Projects</h6>
                                <% if (session.getAttribute("UID") != null) {
                                        int puid;
                                        String puname = "" + session.getAttribute("UID").toString();
                                        try {
                                            puid = Integer.parseInt(puname);
                                        } catch (Exception e) {
                                            puid = 1;
                                        }
                                        if (puid > 1) {
                                            textdisplay.Project[] userProjects = thisUser.getUserProjects();
                                            if (userProjects.length > 0) {
                                            %><%@include file="WEB-INF/includes/projectPriority.jspf" %><%
                                                out.print("<ul id='projectList'>");
                                            }
                                            for (int i = 0; i < userProjects.length; i++) {
                                                textdisplay.Metadata mList = userProjects[i].getMetadata();
                                                out.print("<li><a title=\"" + userProjects[i].getProjectName() + "\" class=\"tpenButton projectTitle\" href=\"project.jsp?projectID=" + userProjects[i].getProjectID() + "\">" + ESAPI.encoder().decodeFromURL(mList.getTitle()) + "</a>");
                                                //  Get project Leader and offer a delete option.
                                                textdisplay.Project iProject = new textdisplay.Project(userProjects[i].getProjectID());
                                                user.Group projectGroup = new user.Group(iProject.getGroupID());
                                                if (projectGroup.isAdmin(UID)) {
                                                    out.print("<span class=\"delete ui-icon ui-icon-closethick right\" onclick=\"deleteProject(" + UID + "," + userProjects[i].getProjectID() + "," + "'" + StringEscapeUtils.escapeJavaScript(mList.getTitle()) + "'" + ");\">delete</span></li>");
                                                } else {
                                                    out.print("</li>");
                                                }
                                            }
                                            out.print("</ul>");
                                        }
                                    } else {
                                        out.print("<p>You have no active projects.</p>");
                                    }
                                    Metadata m = new Metadata(projectID);
                                if (permitModify || isMember){
                                %>
                                <h3>Add to Project</h3>
                                <a id="addManuscript" class="tpenButton" href="#"><span class="ui-icon ui-icon-plus right"></span>Find Manuscript to Add</a>
                                <div id="uploadImagesDiv"></div>
                                <%}%>
                            </li>
                            <li class="left ui-widget-content ui-corner-tr ui-corner-bl tall">
                                <h3>Current Metadata Summary</h3>
<%
                                if (thisProject.getHeader().length()>0){
                                    String header = thisProject.getHeader();
                                    if(permitMetadata || isMember){%>
                                    <a class="tpenButton ui-button" href="projectMetadata.jsp?resetHeader=true&projectID=<%out.print(projectID);%>" id="emptyHeader">Remove Custom Header</a>
                                    <p>You have uploaded a custom header for this project, which is displayed below. This header is read-only. To change it, remove the current header and upload a new file.</p>
                                    <%}
                                    out.println("<span id='customHeader'>"+header+"</span>");
                                    if (header.length()>thisProject.getLinebreakCharacterLimit()-1){
                                        out.print("<span class='ui-state-error-text'>Preview limited to first "+thisProject.getLinebreakCharacterLimit()+" characters.</span>");
                                    }
                                } else {
%>
                                    <p><span class="label">Title: </span><%out.print(m.getTitle());%><br />
                                    <span class="label">Subtitle: </span><%out.print(m.getSubtitle());%><br />
                                    <span class="label">MS&nbsp;Identifier: </span><%out.print(m.getMsIdentifier());%><br />
                                    <span class="label">MS&nbsp;Settlement: </span><%out.print(m.getMsSettlement());%><br />
                                    <span class="label">MS&nbsp;Repository: </span><%out.print(m.getMsRepository());%><br />
                                    <span class="label">MS&nbsp;Collection: </span><%out.print(m.getMsCollection());%><br />
                                    <span class="label">MS&nbsp;ID&nbsp;number: </span><%out.print(m.getMsIdNumber());%><br />
                                    <span class="label">Group&nbsp;Leader: </span><%
                                        for (int i = 0; i < leader.length; i++) {
                                            if (i > 0) {
                                                out.print(", ");
                                            }
                                            out.print(leader[i].getFname() + " " + leader[i].getLname());
                                        }%><br />
                                    <span class="label">Description: </span><%out.print(m.getDescription());%>
                                </p>
                                <%if(permitMetadata || isMember){%>
                                <p>To update any of this information:</p>
                               
                                <a class="tpenButton" href="projectMetadata.jsp?projectID=<%out.print("" + projectID);%>"><span class="ui-icon ui-icon-tag right"></span>Update Metadata</a>
<%}}%>                             </li> 
                            <div class="tall">
                                <li class="left ui-widget-content ui-corner-tr ui-corner-bl loadingBook">
                                <%
                                int recentFolio = thisProject.getLastModifiedFolio();
                                String projectLink = (recentFolio>0) ? 
                                    "<a class='tpenButton' href='transcription.jsp?projectID=" + projectID + "&p=" + recentFolio + "'><span class='ui-icon ui-icon-pencil right'></span>Resume Transcribing</a>": 
                                    "<a class='tpenButton' href='transcription.jsp?projectID=" + projectID + "'><span class='ui-icon ui-icon-pencil right'></span>First Page</a>"; 
                                if(permitRead || permitModify || permitNotes || isMember){%>
                                    <%out.print(projectLink);%>
                                    <select class="clear folioDropdown" style="margin: 10% 10%;text-align: center;max-width: 80%;" onchange="navigateTo(this);">
                                        <option SELECTED>Jump to page</option>
                                    </select>
                                    <%}%>
                                </li>
                                <%if(permitModify || isMember){%>
                                <li class="left ui-widget-content ui-corner-tr ui-corner-bl"><a class="tpenButton" href="projSequence.jsp?projectID=<%out.print("" + projectID);%>"><span class="ui-icon ui-icon-image right"></span>Modify image sequence</a>
                                    <p>Change the order of or remove pages within this project.</p></li>
                                    <%}%>
                            </div>
                        </ul>
                    </div>
                    <div id="tabs-2">
                        <ul id="ms" class="ui-helper-reset">
                            <li class="left ui-widget-content ui-corner-tr ui-corner-bl tall loadingBook">
                                <%if(permitRead || permitModify || permitNotes || isMember){%>
                                    <%out.print(projectLink);%>
                                    <select class="clear folioDropdown" style="margin: 10% 10%;text-align: center;max-width: 80%;" onchange="navigateTo(this);">
                                        <option SELECTED>Jump to page</option>
                                    </select>
                                                              <%}%>
  </li>
                                <%if(permitModify || isMember){%>
                            <li class="left ui-widget-content ui-corner-tr ui-corner-bl tall">
                                <a class="tpenButton" href="transcription.jsp?tool=linebreak&projectID=<%out.print("" + projectID);%>"><span class="ui-icon ui-icon-clipboard right"></span>Linebreak and proofread existing text</a>
                                <p>Adjust the linebreaking or make changes to uploaded text. Also revise previously saved transcriptions.</p>
                                <a class="tpenButton" href="uploadText.jsp?projectID=<%out.print("" + projectID);%>"><span class="ui-icon ui-icon-folder-open right"></span>Upload a file to linebreak</a>
                                <p>Upload a file to get started. The text will be available to any group member in this project.</p>
                            </li>
                                                               <%}
                                    if(permitParsing || isMember){%>
 <li class="left ui-widget-content ui-corner-tr ui-corner-bl tall"><a class="tpenButton" href="transcription.jsp?tool=parsing&projectID=<%out.print("" + projectID);%>"><span class="ui-icon ui-icon-note right"></span>Check line parsings</a>
                                <p>Verify the automatic line detection for the project or define the columns and lines manually. Access independent control over each page in the project.</p>
                            </li>
<%}%>
                        </ul>
                    </div>
                    <div id="tabs-3">
                        <ul id="team" class="ui-helper-reset">
                            <li class="left ui-widget-content ui-corner-tr ui-corner-bl tall">
                                <div id="inviteFeedback"></div>
                                <%
                                    User[] groupMembers = thisGroup.getMembers();
                                    User[] groupLeader = thisGroup.getLeader();
                                if ((UID == groupLeader[0].getUID()) && !thisProject.containsUserUploadedManuscript()){
                                %>
                                <a class="tpenButton" href="#publicOptions" onclick="$('#publicOptions').fadeIn('normal');return false;"><span class="ui-icon ui-icon-unlocked right"></span>Share Publicly</a>
                                <%}
                                    if(isMember){%>
                                <a class="tpenButton" href="groups.jsp?projectID=<%out.print("" + projectID);%>"><span class="ui-icon ui-icon-person right"></span>Modify Project Team</a>
                                <p>Add, delete, or just check your group membership on this project.</p>
                                <%}
                                    out.print("<h4>" + thisGroup.getTitle() + " Group Members:</h4>");
                                    //now list the users
                                    if(isMember){
                                        for (int i = 0; i < groupMembers.length; i++) {
                                            if (groupLeader[0].getUID() == groupMembers[i].getUID()) {
                                                out.print("<div class='loud'>" + groupMembers[i].getFname().substring(0,1) +"&nbsp;"+groupMembers[i].getLname() + "&nbsp;(" + groupMembers[i].getUname() + ")&nbsp;Group&nbsp;Leader</div>");
                                            } else {
                                                out.print("<div>" + groupMembers[i].getFname().substring(0,1) +"&nbsp;"+groupMembers[i].getLname() + "&nbsp;(" + groupMembers[i].getUname() + ")</div>");
                                            }
                                        }
                                   }else{ // e-mail username hidden from non-members
                                        for (int i = 0; i < groupMembers.length; i++) {
                                            if (groupLeader[0].getUID() == groupMembers[i].getUID()) {
                                                out.print("<div class='loud'>" + groupMembers[i].getFname().substring(0,1) +"&nbsp;"+groupMembers[i].getLname()+",&nbsp;Group&nbsp;Leader</div>");
                                            } else {
                                                out.print("<div>" + groupMembers[i].getFname().substring(0,1) +"&nbsp;"+groupMembers[i].getLname() + "</div>");
                                            }
                                        }
                                    }
                                %>
                                <br />
                                <div id="inviteUserBtn" class="tpenButton"><span class="ui-icon ui-icon-person right"></span>Invite to T-PEN</div>
                                <form id="inviteUser" class="ui-corner-all" name="invite" action="project.jsp" onsubmit="return simpleFormValidation();" method="get">
                                    <input type="hidden" name="projectID" value="<%out.print("" + projectID);%>">
                                    <input type="hidden" name="selecTab" value="2">
                                    <label for="uname">Email</label><input class="text" type="text" name="uname"/><br/>
                                    <label for="fname">First Name</label><input class="text" type="text" name="fname" /><br/>
                                    <label for="lname">Last Name</label><input class="text" type="text" name="lname"/><br/>
                                    <button type="submit" value="Register" name="invite" class="ui-button tpenButton"><span class="ui-icon ui-icon-person right"></span>Invite</button>
                                </form>
                            </li>
                            <li class="left ui-widget-content ui-corner-tr ui-corner-bl tall">
                                <h3>Switchboard</h3>
                                <p>
                                    Import recommended XML tag sets, connect to remote XML schema, and enable simple submission with a click.
                                </p>
                                <%
                                if(isMember || permitModify){
                                    if (request.getParameter("partnerConnect") != null) {
                                %>
                                <h4 class="clear-left" id="QENIresults">Results</h4>
                                <%
                                        //remove any connection
                                        if (Integer.parseInt(request.getParameter("template")) == -1) {
                                            try {
                                                PartnerProject exTemplate = thisProject.getAssociatedPartnerProject();
                                                User exController = exTemplate.getControllingUser();
                                                thisProject.setAssociatedPartnerProject(0);
                                                out.println("<span class='left clear-left'><span class='ui-icon-circle-minus ui-icon left'></span>Disconnected successfully.</span>");
                                                out.println("<span class='left clear-left'><span class='ui-icon-info ui-icon left'></span>" + exController.getFname() + " " + exController.getLname() + " has not been removed from your group.</span>");
                                                out.println("<a id='removeUser' class='right clear-left ui-button tpenButton' href='groups.jsp?act=rem&projectID=" + projectID + "&usr=" + exController.getUID() + "'>Remove " + exController.getFname().toString().substring(0, 1) + " " + exController.getLname() + "</a>");
                                            } catch (NullPointerException npe) {
                                                out.println("<span class='left clear-left ui-state-error-text'><span class='ui-icon-alert ui-icon left'></span>Sorry, there was a problem disconnecting. Please <a href='admin.jsp?selecTab=3' target='_blank'>Contact T&#8209;PEN</a>. " + npe.getLocalizedMessage() + "</span>");
                                            }
                                        } //attach to partner project
                                        else if (request.getParameter("template") != null) {
                                            thisProject.setAssociatedPartnerProject(Integer.parseInt(request.getParameter("template")));
                                            out.println("<span class='left clear-left'><span class='ui-icon-check ui-icon left'></span>Connected successfully.</span>");
                                            PartnerProject theTemplate = new PartnerProject(Integer.parseInt(request.getParameter("template")));
                                            thisProject.copyButtonsFromProject(theTemplate.getTemplateProject());
                                            thisProject.copyHotkeysFromProject(theTemplate.getTemplateProject());
                                            out.println("<span class='left clear-left'><span class='ui-icon-check ui-icon left'></span>Tags copied successfully.</span>");
                                            User controller = theTemplate.getControllingUser();
                                            if (!thisGroup.isMember(controller.getUID())) {
                                                thisGroup.addMember(controller.getUID());
                                                out.println("<span class='left clear-left'><span class='ui-icon-check ui-icon left'></span>" + theTemplate.getControllingUser().getFname() + " " + theTemplate.getControllingUser().getLname() + " added to group.</span>");
                                            } else {
                                                out.println("<span class='left clear-left'><span class='ui-icon-info ui-icon left'></span>" + theTemplate.getControllingUser().getFname() + " " + theTemplate.getControllingUser().getLname() + " is already in this group.</span>");
                                            }
                                            String schema = theTemplate.getTemplateProject().getSchemaURL();
                                            if (schema.length() > 2) {
                                                if (thisProject.setSchemaURL(schema)) {
                                                    out.println("<span class='left clear-left'><span class='ui-icon-check ui-icon left'></span>Schema URL: " + schema + "</span>");
                                                } else {
                                                    out.println("<span class='left clear-left ui-state-error-text'><span class='ui-icon-alert ui-icon left'></span>Schema URL copy failed.</span>");
                                                }
                                            } else {
                                                out.println("<span class='left clear-left'><span class='ui-icon-info ui-icon left'></span>Template has no schema URL.</span>");
                                            }
                                        }
                                    }
                                    //create as template
                                    if (request.getParameter("makeTemplate") != null) {
                                        String tName = (request.getParameter("templateName") != null) ? request.getParameter("templateName") : "unnamed";
                                        String tDesc = (request.getParameter("templateDesc") != null) ? request.getParameter("templateDesc") : "no description";
                                        String url = "#"; //dummy for now
                                        PartnerProject tPartner = new PartnerProject(tName, tDesc, url, UID, projectID);
                                    }
                                    //remove template
                                    if (request.getParameter("disconnectTemplate") != null) {
                                %>
                                <h4 class="clear-left" id="QENIresults">Results</h4>
                                <%
                                        String[] msg = PartnerProject.removeTemplateProject(Integer.parseInt(request.getParameter("disconnectTemplate")));
                                        out.print(msg[0] + " disconnected from this template and "
                                                + msg[2] + " " + msg[1] + ".");
                                    }
                                    if (thisUser.isAdmin() || (groupLeader[0].getUID() == thisUser.getUID())) {
                                        PartnerProject[] partners = PartnerProject.getAllPartnerProjects();
                                        int partnersLength = partners.length;
                                        boolean isTemplate = false;
                                        int thisPartnerID = 0;
                                        for (int i = 0; i < partnersLength; i++) {
                                            if (partners[i].getTemplateProject().getProjectID() == projectID) {
                                                isTemplate = true;
                                                thisPartnerID = partners[i].getID();
                                                break;
                                            }
                                        }
                                        if (isTemplate) {
                                %>
                                <h4 class="clear-left" id="QENItemplate">Conduit Summary</h4>
                                <%
                                    //show a template control panel
                                    PartnerProject thisPartner = new PartnerProject(thisPartnerID);
                                    user.User thisController = thisPartner.getControllingUser();
                                    int numOfConnections = Project.getAllAssociatedProjects(thisPartnerID).length;
                                    String connections = (numOfConnections == 1) ? "1 connected project" : numOfConnections + " connected projects";
                                %>
                                <p class="loud">This project is a <span class="bold" title="The group leader has designated this project as a Switchboard template">template project</span>.</p>
                                <dl>
                                    <dt>Name</dt>
                                    <dd><%out.print(thisPartner.getName());%></dd>
                                    <dt>Description</dt>
                                    <dd><%out.print(thisPartner.getDescription());%></dd>
                                    <dt>Controlled By</dt>
                                    <dd><%out.print(thisController.getFname() + " " + thisController.getLname());%></dd>
                                    <dt>Conduit URL</dt>
                                    <dd><%out.print(thisPartner.getURL());%></dd>
                                    <dt>Connections</dt>
                                    <dd><%out.print(connections);%></dd>
                                </dl>
                                <a class="tpenButton" href="project.jsp?selecTab=2&disconnectTemplate=<%out.print(thisPartner.getID() + projectAppend);%>">Disconnect Template from T&#8209;PEN</a>
                                <p class="small clear"><span class="ui-icon ui-icon-info" style="float:left;height:14px;"></span>
                                    This project will not be deleted, but will no longer appear on the Switchboard list and all associated projects will be disconnected. If you decide to reconnect in the future, all leaders of associated projects will have to reattach their projects.
                                </p>
                                <%
                                } else {
                                %>
                                <div id="QENIx" class="clear-left">
                                    <form method="GET" action="project.jsp" class="clear">
                                        <h4 class="clear-left" id="QENIconnected">Currently Connected</h4>
                                        <%
                                            if (thisProject.getAssociatedPartnerProject() != null) {
                                                textdisplay.PartnerProject connect = new PartnerProject(thisProject.getAssociatedPartnerProject().getID());
                                                if (connect.getURL().length() > 4) {
                                                    out.print("Click to submit to this conduit");
                                                }
                                                out.print("<div id='disconnect' class='clear-left left tpenButton' data-partnerid='none'><span class='partnerName'>" + connect.getName() + ""
                                                        + "<span class='ui-icon ui-icon-closethick right' title='Remove this connection. You must manually delete any collaborators.'></span></span>"
                                                        + "<input class='hide' type='radio' name='template' value='-1' />"
                                                        + "<span id='deleteConnection' class='partnerDescription clear-left left'>Remove this connection. You must manually delete any collaborators.</span>"
                                                        + "</div><span></span>");
                                            } else {
                                                out.print("<p>No current connections.</p>");
                                            }
                                        %>
                                        <!--                                List in fancy checkboxes each project with maybe lists of available stuff with each-->
                                        <h4 class="clear-left" id="QENIconnections">Available Connections</h4>
                                        <div id="QENI" style='height:<%
                                            int listHeight;
                                            listHeight = 16 + 54 * partnersLength; // padding + each
                                            listHeight = (listHeight > 300) ? 300 : listHeight; // 300px max
                                            out.print(listHeight);%>px;'>
                                            <input type="hidden" value="<%out.print(projectID);%>" name="projectID" />
                                            <input type="hidden" value="2" name="selecTab" />
                                            <%//if(thisProject.getAssociatedPartnerProject() != null){%>
                                            <!--   <div id="disconnect" class="partnerListing tpenButton" data-partnerid="none">
                                                       <input class="hide" type="radio" name="template" value="-1" />
                                                       <span class="partnerName"><span class="ui-icon ui-icon-circle-minus left"></span>Disconnect</span>
                                                       <span class="partnerDescription">Remove any connections. You must manually delete any collaborators.</span>
                                               </div> -->
                                            <%//}
                                                for (int i = 0; i < partnersLength; i++) {
                                            %>
                                            <div class="partnerListing tpenButton" data-template="<%out.print(partners[i].getTemplateProject().getProjectID());%>">
                                                <input class="hide" type="radio" name="template" value="<%out.print(partners[i].getID());%>" />
                                                <span class="partnerName"><%out.print(partners[i].getName());%></span>
                                                <span class="partnerDescription"><%out.print(partners[i].getDescription());%></span>
                                            </div>
                                            <%
                                                }
                                            %>
                                        </div>
                                        <input class="right tpenButton ui-button hide" type="submit" value="Submit Changes" name="partnerConnect" />
                                    </form>
                                    <p class="small clear"><span class="ui-icon ui-icon-info" style="float:left;height:14px;"></span>
                                        By connecting this project to a Switchboard, TPEN automatically adds the appropriate individuals to this project and enables direct submission to the collaborating entity. This connection grants the entity access to your project, but you will still retain control of available customizations. 
                                    </p>
                                </div>
                                <%if (!isTemplate) {%>
                                <h4 class="clear-left" id="QENItemplate">Create a New Template</h4>
                                <div id="templateBtn" class='tpenButton ui-button' title="Allow others to connect to this project and use its button sets"><span class="right ui-icon-star ui-icon"></span>Use Current Project as Template</div>
                                <form id="template" class='ui-corner-all' name="template" action="project.jsp" method="get">
                                    <input type="hidden" name="projectID" value="<%out.print("" + projectID);%>">
                                    <input type="hidden" name="selecTab" value="2">
                                    <div id="sampleTemplate" class="partnerListing tpenButton">
                                        <span class="partnerName">Name</span>
                                        <span class="partnerDescription">This description will update as you type below</span>
                                    </div>
                                    <label for="templateName">Name
                                        <input name="templateName" id="templateName" type="text" placeholder="Brief Title"/>
                                    </label>
                                    <label for="templateDesc">Description
                                        <textarea name="templateDesc" id="templateDesc" placeholder="Enter a short description to appear on the linking button"></textarea>
                                    </label>
                                    <label for="templateDesc">URL
                                    </label>
                                    <p class="small clear"><span class="ui-icon ui-icon-info" style="float:left;height:14px;"></span>
                                        Setting up a web service and providing a URL allows TPEN users to submit transcriptions directly to non-TPEN services. <a href="admin.jsp?selecTab=3" target="_blank">Contact T&#8209;PEN</a> for more information.
                                    </p>
                                    <input type="submit" class="right tpenButton ui-button" name="makeTemplate" value="Create Template" />
                                </form>
                                <%     }
                                    }
                                } else {%>
                                <span class="ui-corner-all ui-state-error left"><span class="ui-icon-alert ui-icon left"></span>This feature is limited to the Group Leader.</span>
                                <!--                                List any connected projects-->
                                <%
                                        if (thisProject.getAssociatedPartnerProject() != null) {
                                            textdisplay.PartnerProject connect = new PartnerProject(thisProject.getAssociatedPartnerProject().getID());
                                            out.print("<p class='clear-left left'>Connected to " + connect.getName() + ".</p>");
                                        } else {
                                            out.print("<p class='clear-left left'>No current connections.</p>");
                                        }
                                    }
                                } else {
                                            out.print("<p class='clear-left left'>Public projects can only be connected to the Group Leader.</p>");
                                                                                       }
                            %>
                            </li>
                            <li class="left ui-widget-content ui-corner-tr ui-corner-bl tall">
                                <h3>Recent Activity on this Project</h3>
                                <a id="popupNote" class="tpenButton" href="#"><span class="ui-icon ui-icon-comment right"></span>Add a note to the log</a>
                                <div id="projectLog" class="ui-corner-all">
                                    <%
                                        if (request.getParameter("submitted") != null) {
                                            String content = request.getParameter("logContent");
                                            thisProject.addLogEntry("<span class='log_user'></span>"+content, UID); // ,"userAdded"
                                        }
                                        out.print(thisProject.getProjectLog());
                                    %>
                                </div>
                                    <div id="noteForm" class="ui-corner-all" style="display:none;">
                                        <form action="project.jsp?selecTab=2&projectID=<%out.print(projectID);%>" method="POST">
                                            <textarea name="logContent" id="logContent"></textarea><br>
                                            <button class="tpenButton left" type="submit" name="submitted" value="add comment" >Submit</button>
                                            <button class="tpenButton left" id="clearNote" name="cancel">Cancel</button>
                                        </form>
                                    </div>
                                            <div id="logFilter">
                                    <!--    Filters: userAdded, transcription, addMS, parsing           -->
                                    <a href="#" id="userAdded" filter="log_user" class="tpenButton" title="Filter notes added by users"><span class="right ui-icon ui-icon-person"></span>User Comment</a>
                                    <a href="#" id="transcription" filter="log_transcription" class="tpenButton" title="Filter automatic notes about new transcriptions"><span class="right ui-icon ui-icon-note"></span>Transcription</a>
                                    <a href="#" id="addMS" filter="log_manuscript" class="tpenButton" title="Filter automatic notes about additions to project"><span class="right ui-icon ui-icon-plus"></span>New Manuscript</a>
                                    <a href="#" id="parsing" filter="log_parsing" class="tpenButton" title="Filter automatic notes about changes in parsing"><span class="right ui-icon ui-icon-wrench"></span>Parsing Update</a>
                                    <a href="projectlog.jsp?projectID=<%out.print(projectID);%>" target="_blank" class="tpenButton clear" id="openNote"><span class="right ui-icon ui-icon-newwin"></span>View Log in a new window</a>
                                </div></li>
                        </ul>
                    </div>
                    <div id="tabs-4">
                        <ul id="options" class="ui-helper-reset">
                            <li class="left ui-widget-content ui-corner-tr ui-corner-bl tall">
                                <h3>Project Options</h3>
                                <%if(isMember || permitModify){%>
                                Import XML schema, validate projects, and customize buttons.
                                <div id="xmlImportDiv">
                                    <div id="xmlImportBtn" class="tpenButton"><span class="ui-icon ui-icon-script right"></span>
                                        Link a schema to this project
                                    </div>
                                    <form id="xmlImport" name="xmlImport" class="ui-corner-all" action="project.jsp" method="POST">
                                        <input type="hidden" name="selecTab" value="3"/>
                                        <input type="hidden" name="projectID" value="<%out.print("" + projectID);%>"/>
                                        URL of schema:<input name="url" style="width:100%;margin:2px 0px 2px -1px" title="Link directly to a valid file" type="text" placeholder="enter complete file URL"/>
                                        <input class="tpenButton right" name="xmlImport" value="Link XML" type="submit" />
                                    </form>
                                </div>
                                <%}
    if (thisProject.getSchemaURL().length() > 5) {%>
                                <div id="xmlValidateDiv">
                                    <div id="xmlValidateBtn" class="tpenButton"><span class="ui-icon ui-icon-script right"></span>
                                        Validate XML for this project
                                    </div>
                                    <form id="xmlValidate" class="ui-corner-all" action="validate" method="POST">
                                        <input type="hidden" name="selecTab" value="3"/>
                                        <input type="hidden" name="projectID" value="<%out.print("" + projectID);%>"/>
                                        URL of schema for validation: <div style="width:100%;margin:2px 0px 2px -1px"><%out.print(thisProject.getSchemaURL());%></div>
                                        <input class="tpenButton right" name="xmlValidate" value="Validate" type="submit" />
                                    </form>
                                </div>
                                        <%if (isMember || permitButtons){%>
                                <a id="importBtnFromSchemaBtn" class="tpenButton" href="buttonSchemaImport.jsp?projectID=<%out.print("" + projectID);%>"><span class="ui-icon ui-icon-transfer-e-w right"></span>Import Buttons from Linked Schema</a>
                                <%}
                                }
                                if (isMember || permitButtons){%>
                                <a class="tpenButton" href="buttons.jsp?projectID=<%out.print("" + projectID);%>"><span class="ui-icon ui-icon-gear right"></span>Button Management</a>
                                <p>The <span title="Any unicode character can be attached to one of these buttons for use in your project." class="loud">Special Character</span> and <span title="Multiple custom tags with parameters can be added to this project." class="loud">Custom xml Tags</span> you define will remain specific to each project. These buttons are accessible on the transcription pages. Characters assigned to the numbered buttons can be inserted simply by holding CTRL and pressing the corresponding number on the keyboard.</p>
                           <%} else {%>
                               <p>Button management is restricted to group members on this public project. The current button pallete is displayed to the right.</p>
                           <%}
                           if ((isMember || permitCopy) && !thisProject.containsUserUploadedManuscript()){
         %>
                                <a id="copyProjectBtn" class="tpenButton" href="index.jsp?projectID=<%out.print("" + projectID);%>&makeCopy=true"><span class="ui-icon ui-icon-copy right"></span>Create a Copy</a>
                                <p>Create a new project with the same set of images, transcriptions, and buttons. Once copied, the projects will not synchronize cannot be recombined in T&#8209;PEN.</p>
                               <%}%>
                            </li>
                            <li class="left ui-widget-content ui-corner-tr ui-corner-bl tall">
                                <h3>Current Button Summary</h3>
                                <h6>Special Character Buttons</h6><%
                                    Hotkey ha;
                                    ha = new Hotkey(projectID, true);
                                    /**Retrieve stored button information*/
                                    out.print(ha.javascriptToAddProjectButtons(projectID));
                                %><br class="clear-left" />
                                <h6>XML Tags</h6>
                                <div id="allXML"><%
                                    /**Retrieve stored button information*/
                                    out.print(TagButton.getAllProjectButtons(projectID));
                                                                   %></div>
                                <%if(isMember || permitButtons){%>
                               <p class="clear-left">If you wish, you can copy XML tags between projects: <a href="buttonProjectImport.jsp?a=1<%out.print(projectAppend);%>" class="ui-button tpenButton">Copy XML Tags</a></p>
                            <%}%>
                            </li>
                                <%if(isMember || permitModify){%>
                            <li class="left ui-widget-content ui-corner-tr ui-corner-bl tall">
                                <h3>Transcription Tools</h3>
<!--                                <h4>Automatic Coding</h4>
                                <div class="clear-left ui-state-disabled" id="linebreakingDiv">
                                    <div id="linebreakingBtn" class="tpenButton">Linebreak String</div>
                                    <div id="linebreaking" class="ui-corner-all"><p>What string is used to indicate a new line?</p>
                                                                            TODO build out this feature and add information about what a default means
                                        <span id="currentSettingLinebreak" class="loud left clear-left">Currently set to: <script type="text/javascript">document.write("selectLinebreak");</script></span>
                                        <br />Enter custom string here:<input style="width:50px;" title="Type any valid string." maxlength="6" type="text" id="linebreak" placeholder="custom" value=""/>   
                                        <input class="button ui-corner-all" onclick="return false;" type="button" name="Save" value="save" title="Click to save your choice"/>
                                    </div>
                                </div>
                                <div class="clear-left ui-state-disabled" id="wordbreakDiv">
                                    <div id="wordbreakBtn" class="tpenButton">Breaking Words Between Lines</div>
                                    <div id="wordbreak" class="ui-corner-all"><p>What string will you use to indicate that a word has been separated across lines?</p>
                                        <span id="currentSetting" class="loud left clear-left"><script type="text/javascript">document.write(selectWordbreak);</script></span>
                                        <br />Enter custom string here:<input style="width:50px;" title="Type any valid string." maxlength="6" type="text" id="userWord" placeholder="custom" value=""/>   
                                        <input class="button ui-corner-all" onclick="customWordbreak();" type="button" name="Save" value="save" title="Click to save your choice"/>
                                    </div>
                                </div>
                                <br />-->
                                <%
                                    if (request.getParameter("tools") != null) {
                                        // remove current tools to replace with new
                                        Tool.removeAll(UID);
                                        UserTool.removeAll(projectID);
                                        String[] projectTools = request.getParameterValues("projectTool[]");
                                        String[] userTools = request.getParameterValues("userTool[]");
                                        // add requested user tools
                                        if (userTools != null) {
                                            for (int i = 0; i < userTools.length; i++) {
                                                Tool.tools newTool = null;
                                                if (userTools[i].compareTo("abbreviations") == 0) {
                                                    newTool = Tool.tools.abbreviation;
                                                } else if (userTools[i].compareTo("compare") == 0) {
                                                    newTool = Tool.tools.compare;
                                                } else if (userTools[i].compareTo("parsing") == 0) {
                                                    newTool = Tool.tools.parsing;
                                                } else if (userTools[i].compareTo("preview") == 0) {
                                                    newTool = Tool.tools.preview;
                                                } else if (userTools[i].compareTo("history") == 0) {
                                                    newTool = Tool.tools.history;
                                                } else if (userTools[i].compareTo("linebreak") == 0) {
                                                    newTool = Tool.tools.linebreak;
//                                                } else if (userTools[i].compareTo("annotation") == 0) {
//                                                    newTool = Tool.tools.annotation;
                                                } else if (userTools[i].compareTo("paleography") == 0) {
                                                    newTool = Tool.tools.paleography;
//                                                } else if (userTools[i].compareTo("sciat") == 0) {
//                                                    newTool = Tool.tools.sciat;
                                                } else {
                                                    continue;
                                                }
                                                if (newTool != null) {
                                                    new Tool(newTool, UID);
                                                }
                                            }
                                        }
                                        if (projectTools != null) {
                                            for (int i = 0; i < projectTools.length; i++) {
                                                String[] thisTool = projectTools[i].split("TPENTOOLURL");
                                                new UserTool(thisTool[0], thisTool[1], projectID);
                                            }
                                        }
                                    }
                                %>
                                <form id="toolSelection" action="project.jsp" method="GET">
                                    <h4 class="clear-left" title="These options are unique to each user">User Tools</h4>
                                    <%
                                    // User Tools
                                        String[] toolCheck = new String[11];
                                        String[] toolName = new String[11];
                                        Tool.tools[] TOOLS = {Tool.tools.abbreviation, Tool.tools.compare, Tool.tools.parsing, Tool.tools.preview, Tool.tools.history, Tool.tools.linebreak, Tool.tools.paleography};
                                        for (int i = 0; i < 6; i++) {
                                            toolCheck[i] = (Tool.isToolActive(TOOLS[i], UID)) ? "checked=true" : "";
                                        }
                                    %>
                                    <label class='userTools'><input name="userTool[]" type="checkbox" <%out.print(toolCheck[0]);%> value="abbreviations"/>Cappelli Abbreviations</label>
                                    <label class='userTools'><input name="userTool[]" type="checkbox" <%out.print(toolCheck[1]);%> value="compare" />Compare Pages</label>
                                    <label class='userTools'><input name="userTool[]" type="checkbox" <%out.print(toolCheck[2]);%> value="parsing" />Parsing Adjustment</label>
                                    <label class='userTools'><input name="userTool[]" type="checkbox" <%out.print(toolCheck[3]);%> value="preview" />Preview Tool</label>
                                    <label class='userTools'><input name="userTool[]" type="checkbox" <%out.print(toolCheck[4]);%> value="history" />History Tool</label>
                                    <label class='userTools'><input name="userTool[]" type="checkbox" <%out.print(toolCheck[5]);%> value="linebreak" />Linebreaking Tool</label>
                                    <label class='userTools'><input name="userTool[]" type="checkbox" <%out.print(toolCheck[6]);%> value="paleography" />Glyph Matching</label>
                                    <!-- <label class='userTools'><input name="userTool[]" type="checkbox" < %out.print(toolCheck[7]);% > value="sciat" />Annotations</label> -->
                                    <span class="ui-helper-clearfix"></span><h4 id="projectTools" class="clear-left" title="These options are tied to each project">Project Tools
                                        <a class="ui-icon ui-icon-plusthick" id="addTool" title="Add a Tool" onclick="$('#addingTools').fadeIn();">Add a Tool</a>
                                        <span class="left clear-left small">(Click on a label to edit the button name)</span></h4>
                                    <%
                                        //Project Tools
                                        UserTool[] projectTools = UserTool.getUserTools(projectID);
                                        StringBuilder toolOptions = new StringBuilder();
                                        for (int i = 0; i < projectTools.length; i++) {
                                            String toolLabel = projectTools[i].getName();
                                            String toolURL = projectTools[i].getUrl();
                                            if (toolURL.endsWith("vulsearch")) {
                                                toolCheck[6] = "checked=true";
                                                toolName[6] = toolLabel;
                                                continue;
                                            }
                                            if (toolURL.endsWith("morph.jsp")) {
                                                toolCheck[7] = "checked=true";
                                                toolName[7] = toolLabel;
                                                continue;
                                            }
                                            if (toolURL.endsWith("lookup.html")) {
                                                toolCheck[8] = "checked=true";
                                                toolName[8] = toolLabel;
                                                continue;
                                            }
                                            if (toolURL.endsWith("morphologie/")) {
                                                toolCheck[9] = "checked=true";
                                                toolName[9] = toolLabel;
                                                continue;
                                            }
                                            if (toolURL.endsWith("production.pl")) {
                                                toolCheck[10] = "checked=true";
                                                toolName[10] = toolLabel;
                                                continue;
                                            }
                                            toolOptions.append("<label class='projectTools'><input type='checkbox' checked=true name='projectTool[]' ").append("value='").append(toolURL).append("'/><span contentEditable=true>").append(toolLabel).append("</span></label>");
                                        }
                                        if (toolName[6] == null) {
                                            toolName[6] = "Latin Vulgate Search";
                                        }
                                        if (toolName[7] == null) {
                                            toolName[7] = "Latin Dictionary";
                                        }
                                        if (toolName[8] == null) {
                                            toolName[8] = "Middle English Dictionary";
                                        }
                                        if (toolName[9] == null) {
                                            toolName[9] = "French Dictionary";
                                        }
                                        if (toolName[10] == null) {
                                            toolName[10] = "Dictionary of Old English";
                                        }
                                        out.print(toolOptions.toString());
                                    %>
                                    <label class='projectTools'><input name="projectTool[]" type="checkbox" <%out.print(toolCheck[6]);%> value="http://vulsearch.sourceforge.net/cgi-bin/vulsearch" title="Latin Vulgate Search"/><span contentEditable="true"><%out.print(toolName[6]);%></span></label>
                                    <label class='projectTools'><input name="projectTool[]" type="checkbox" <%out.print(toolCheck[7]);%> value="http://t-pen.org/hopper/morph.jsp" title="Latin Dictionary"/><span contentEditable="true"><%out.print(toolName[7]);%></span></label>
                                    <label class='projectTools'><input name="projectTool[]" type="checkbox" <%out.print(toolCheck[8]);%> value="http://quod.lib.umich.edu/m/med/lookup.html" title="Middle English Dictionary"/><span contentEditable="true"><%out.print(toolName[8]);%></span></label>
                                    <label class='projectTools'><input name="projectTool[]" type="checkbox" <%out.print(toolCheck[9]);%> value="http://www.cnrtl.fr/morphologie/" title="French Dictionary"/><span contentEditable="true"><%out.print(toolName[9]);%></span></label>
                                    <label class='projectTools'><input name="projectTool[]" type="checkbox" <%out.print(toolCheck[10]);%> value="http://tapor.library.utoronto.ca/cgi-bin/doe/production.pl" title="Dictionary of Old English"/><span contentEditable="true"><%out.print(toolName[10]);%></span></label>
                                    <input type="hidden" name="p" value="<%out.print(pageno);%>"/>
                                    <input type="hidden" name="projectID" value="<%out.print(projectID);%>"/>
                                    <input type="hidden" name="selecTab" value="3"/>
                                    <input type="submit" name="tools" value="Save Tool Preferences" class="ui-button tpenButton right clear-left"/>
                                </form>
                            </li>
<%}%>
                        </ul>
                    </div>
                    <%if (isMember || permitExport){%>
                        <div id="tabs-5">
                            <%@include file="WEB-INF/includes/switchboardSubmit.jspf" %>
                        <%
                                }
                            }%>
                        <form action="export" method="get" onsubmit="return Export.validForm();">
                            <ul id="export" class="ui-helper-reset">
                                <li class="left ui-widget-content ui-corner-tr ui-corner-bl tall">
                                    <button class="ui-state-default ui-button listBegin left" type="submit" value="Download File"><span class="ui-icon ui-icon-disk right"></span>Download File</button>
                                    <button class="ui-state-default ui-button left" type="submit" value="Open File"><span class="ui-icon ui-icon-document right"></span>Open File</button>
                                    <button class="ui-state-default ui-button listEnd" type="button" onclick="window.location.href='exportUI.jsp?projectID=<%out.print(projectID);%>&p=<%out.print(pageno);%>';return false;" title="Adjust settings in the browser for a highly customizable and portable document"><span class="ui-icon ui-icon-suitcase right"></span>HTML Export</button>
                                    <h3>File Format</h3>
                                    <label for="pdf" title="Portable Document Format"><input id="pdf" type="radio" checked name="type" value="pdf"/>PDF</label><br />
                                    <label for="rtf" title="Rich Text Format"><input id="rtf" type="radio" name="type" value="rtf">RTF</label><br />
                                    <label for="xml" title="XML/Plaintext"><input id="xml" type="radio" name="type" value="xml">XML/Plaintext</label><br />
                                    <!--                        </span>
                                                        <span class='label left clear-left'>Colors:</span>
                                                            <span class="left">
                                                                <label for="bw" title="Standard output"><input id="bw" type="radio" checked name="color" value="bw"/>Black &amp; White</label><br />
                                                                <label for="color" title="Colors are defined by the project buttons"><input disabled id="color" type="radio" name="color" value="color">Color Tags</label><br />-->
                                    <h3 class="clear">Metadata and Page Labels</h3>
                                    <p class="xmlDisclaimer">Metadata export and page labels are not supported in plaintext.</p>
                                    <span class="xmlHide">
                                        <label class="clear" id="paginationOption" for="paginationSelect" title="Include page labels in the exported document"><input id="paginationSelect" type="checkbox" name="pageLabels" checked />Check to Include Page Labels</label><br />
<%if (thisProject.getHeader().length()>0){%>
<label class="clear" id="metadataOption" for="metadataSelect" title="Include custom header in the exported document"><input id="metadataSelect" type="checkbox" name="metadata" checked />Check to Include Custom Header</label><br />
<%}else{%>
<label class="clear" id="metadataOption" for="metadataSelect" title="Include project metadata in the exported document"><input id="metadataSelect" type="checkbox" name="metadata" checked />Check to Include Project Metadata</label><br />
<%}%>
                                    </span>
                                    <label class="clear xmlDisclaimer" id="imageWrapOption" for="imageWrapSelect" title="Place an XML tag to indicate the reference image at each pagebreak"><input id="imageWrapSelect" type="checkbox" name="imageWrap" checked />Check to Include Image Tags</label><br />
                                    <h3 class="clear">Export Range</h3>
                                    <div id="pageRange">
                                        <label class="clear" for="beginFolio">Start with folio:<select id='beginFolio' class="beginFolio folioDropdown" name='beginFolio'></select></label><br/>
                                        <label class="clear" for="endFolio">End with folio:<select id='endFolio' class='endFolio folioDropdown' name='endFolio'></select></label>
                                    </div>
                                    <script type="text/javascript">
                                        var folioDropdown = $('<div />').append('<%
                                        String fdrop = "";
                                        try {
                                            fdrop = ESAPI.encoder().decodeFromURL(thisProject.getFolioDropdown());
                                                    } catch (Error e) {
                                                        fdrop = "<option>" + e.getLocalizedMessage() + "</option>";
                                                    } finally {
                                        out.print(fdrop.replace("'", "&apos;"));
                                        }%>');
                                        $(".folioDropdown").append(folioDropdown.html());
                                        $("#pageRange").find("select")
                                        .children("option").val(function(){
                                            return parseInt($(this).val(),10);
                                        }).end()
                                        .filter(":first").children("option:first").attr("selected",true).end().end()
                                        .filter(":last").children("option:last").attr("selected",true);
                                    </script>
                                </li>
                                <li id="exportTags" class="left ui-widget-content ui-corner-tr ui-corner-bl tall">
                                    <h3>XML Tag Options</h3>
                                    <p class="xmlDisclaimer">Formatting XML tags is not possible with plaintext export.</p>
                                    <%
                                        textdisplay.TagFilter f = new textdisplay.TagFilter(textdisplay.Manuscript.getFullDocument(thisProject, true));
                                        String[] tags = f.getTags();
                                        out.print(tags.length + "</span> XML tags were detected in this text:<br />");
                                        out.print("<input type='hidden' name='projectID' value=" + projectID + " />");
                                        out.print("<span class='ui-button tpenButton xmlDisclaimer' state='off' id='xmlSelectAll'>Select All</span>");                                       
                                        for (int i = tags.length - 1; i > -1; i--) {
                                            out.print("<span class='xmlDisclaimer'><label for='removeTag" + (i + 1) + "' title='Check this box to remove the tag and its contents'><input type='checkbox' name='removeTag" + (i + 1) + "' />Remove &lt;" + tags[i] + "&gt;</label></span>");
                                            out.print("<span class='xmlHide'><label>" + tags[i] + "");
                                            out.print("<select name='style" + (i + 1) + "' >");
                                            out.print("<option value='none' selected>No Change</option>");
                                            out.print("<option value='italic' >Italic</option>");
                                            out.print("<option value='bold' >Bold</option>");
                                            out.print("<option value='underlined'>Underlined</option>");
                                            out.print("<option value='paragraph'>New Paragraph</option>");
                                            out.print("<option value='remove' >Remove</option>");
                                            out.print("</select></label><label for='stripTag" + (i + 1) + "' title='Check this box to strip this tag from around its content' class='right'><input type='checkbox' checked id='stripTag" + (i + 1) + "' name='stripTag" + (i + 1) + "' />Hide " + ESAPI.encoder().encodeForHTML("<tag>") + "</label><br><input type='hidden' name='tag" + (i + 1) + "' value='" + tags[i] + "'></span>");
                                        }
                                    %>
                                </li>
                                <li class="left ui-widget-content ui-corner-tr ui-corner-bl tall">
                                    <div class="ui-state-error ui-corner-all" id="exportAlert"><span class="left ui-icon ui-icon-alert"></span>Notes cannot be displayed in continuous text at this time.</div>
                                    <h3>Notes</h3>
                                    <p class="xmlDisclaimer">Notes are not supported with plaintext export.</p>
                                    <span class="xmlHide">
                                        <!--                        <label id="sideBySide" for="notesSideBySide" title="Show notes to the side of each line"><input id="notesSideBySide" type="radio" name="notes" value="sideBySide"  />Side-by-side</label><br />-->
                                        <label id="noteLine" for="notesLine" title="Show notes underneath each line"><input id="notesLine" type="radio" name="notes" value="line" checked />Beneath each line</label><br />
                                        <label id="endnote" for="notesEndnote" title="Show notes at the end of the document"><input id="notesEndnote" type="radio" name="notes" value="endnote" />Endnotes</label><br />
                                        <!--<label id="footnote" for="notesFootnote" title="Show notes after each page of the manuscript"><input id="notesFootnote" type="radio" name="notes" value="footnote" />Footnotes</label><br />-->
                                        <label id="noteRemove" for="notesRemove" title="Remove notes from exported document"><input id="notesRemove" type="radio" name="notes" value="remove" />Remove</label>
                                    </span>
                                    <h3 class="clear">Linebreaking Layout</h3>
                                    <label id="linebreakLine" class="xmlHide" title="Start each line of transcription on a new line" for="newline"><input id="newline" type="radio" name="linebreak" value="newline" checked />Start a new line</label><br />
                                    <!--                    <label id="linebreakPage" class="xmlHide" title="Linebreak only at a new page" for="pageonly"><input id="pageonly" type="radio" name="linebreak" value="pageonly" />Page break only</label><br />-->
                                    <label id="linebreakContinuous" title="Remove all linebreaks" for="inline"><input id="inline" type="radio" name="linebreak" value="inline" />Continuous text</label><br />
                <!--                    <label id="exportHyphenation" class="" title="Use hyphenation to join words broken across lines" for="exportWordbreak"><input id="exportWordbreak" name="exportWordbreak" type="text" placeholder="/-/" value="<%//out.print(p.getWordbreakString);%>" /> custom word break string</label><br />
                                    <label id="exportLinebreak" class="" title="Use this string with linebreaking" for="exportLinebreakString"><input id="exportLinebreakString" name="exportLinebreakString" type="text" placeholder="&lsaquo;lb /&rsaquo;" value="<%//out.print(p.getLinebreakString);%>" /> custom line break string</label><br />
                                    <label id="exportUseLinebreakString" class="" for="useLinebreakingString" title="Use this string in exported document"><input id="useLinebreakingString" type="checkbox"  value="true"/>Use linebreak string in exported document</label>-->
                                </li>
                            </ul>   
                        </form>
                    </div>
                                    <%}%>
                </div>
                <a class="returnButton" href="index.jsp?projectID=<%out.print("" + projectID);%>">Return to TPEN Homepage</a>
            </div>
        </div>
<%if(isMember||permitModify){%>
            <div id="addToProject"> <!-- container for adding to projects -->
            <div class="callup" id="form"> <!-- add to project -->
                <span id="closePopup" class="right caps">close<a class="right ui-icon ui-icon-closethick" title="Close this window">cancel</a></span>
                <%    //Attach arrays of AllCities and AllRepositories represented on T-PEN
                    String[] cities = Manuscript.getAllCities();
                    String[] repositories = Manuscript.getAllRepositories();
                %>
                <label class="left" for="cities">City: </label>
                <select class="left" name="cities" onchange="Manuscript.filteredCity();scrubListings();" id="cities">
                    <option selected value="">Select a City</option>
                    <%  //Generate dropdown menus for available cities.
                        for (int i = 0; i < cities.length; i++) {
                            out.print("<option value=\"" + (cities[i]) + "\">" + cities[i] + "</option>");
                        }
                    %>
                </select>
                <label class="left clear" for="repositories">Repository: </label>
                <select class="left" name="repository" onchange="Manuscript.filteredRepository();scrubListings();" id="repositories">
                    <option selected value="">Select a Repository</option>
                    <%  //Generate dropdown menus for available repositories.
                        for (int i = 0; i < repositories.length; i++) {
                            out.print("<option class=\"" + (i + 1) + "\" value=\"" + (repositories[i]) + "\">" + repositories[i] + "</option>");
                        }
                    %>
                </select>
                <div id="listings" class="center clear"  style="height:355px;overflow: auto;">
                    <div class="ui-state-active ui-corner-all" align="center">
                        Select a city or repository above to view available manuscripts.
                    </div>

                </div>
            </div>
        </div>
                <%}
                                                if (UID == groupLeader[0].getUID()){
                %>
                <div id="publicOptions" class="ui-widget-content ui-corner-all">
<a id="closePublicOption" href="#" onclick="$('#publicOptions').fadeOut('normal');return false;" class="right tpenButton ui-button">Close<span class="ui-icon ui-icon-close right"></span></a>
                    <%
textdisplay.ProjectPermissions permit = new ProjectPermissions(projectID);
if(request.getParameter("publicOptions")!=null && UID == thisGroup.getLeader()[0].getUID()){
    // First process the requests for public access
    permitOACr = request.getParameter("OACr")!=null;
    permitOACw = request.getParameter("OACw")!=null;
    permitRead = request.getParameter("readTrans")!=null;
    permitExport = request.getParameter("publicExport")!=null;
    permitCopy = request.getParameter("projectCopy")!=null;
    permitNotes = request.getParameter("modNotes")!=null;
    permitModify = request.getParameter("modTrans")!=null;
    permitParsing = request.getParameter("modParsing")!=null;
    permitAnnotation = request.getParameter("modAnno")!=null;
    permitButtons = request.getParameter("modButton")!=null;
    permitMetadata = request.getParameter("modMetadata")!=null;
    permit.setAllow_OAC_read(permitOACr);
    permit.setAllow_OAC_write(permitOACw);
    permit.setAllow_public_read_transcription(permitRead);
    permit.setAllow_export(permitExport);
    permit.setAllow_public_copy(permitCopy);
    permit.setAllow_public_modify_notes(permitNotes);
    permit.setAllow_public_modify(permitModify);
    permit.setAllow_public_modify_line_parsing(permitParsing);
    permit.setAllow_public_modify_annotation(permitAnnotation);
    permit.setAllow_public_modify_buttons(permitButtons);
    permit.setAllow_public_modify_metadata(permitMetadata);
%>
<span id="publicOptionResult" class="right">Changes saved<span class="ui-icon ui-icon-check right"></span></span>
                    <%
} else {
%>
                    <p class="ui-state-error ui-corner-all">
                        <span class="ui-icon ui-icon-alert"></span>
                        By default, only group members have access to the transcription and related project data. Changing any of these options will place this project on the Public Projects list.<br/>
                        <span class="tpenButton ui-button center" onclick="$(this).parent().slideUp();">Acknowledged, thank you</span>
                    </p>
                    <%}%>
                    <h2>Public Access Options</h2>
                    <div id="publicOptionsSelection">
<!-- Restricted to 40% width to allow for clear explanations of each item -->
<form action="project.jsp" method="POST">
    <input type="hidden" name="projectID" value="<%out.print(projectID);%>">
    <input type="hidden" value="2" name="selecTab" />
    <h6>External Access</h6>
    <label for="OACr" class="publicLabel">
        <input id="OACr" name="OACr" type="checkbox" value="true" <%if(permitOACr)out.print("checked");%>/>OAC Read
        <div class="publicDesc">
            <h3>Open Annotation Collaboration</h3>
                <code>Read Permission</code>
                <p>This setting allows external (not originating from t&#8209;pen.org) tools and services to <strong>read</strong> data from the T&#8209;PEN database. Projects with this permission can load annotations and transcriptions into other tools and interfaces for in-depth processing not available on T&#8209;PEN.</p>
            <span class="ui-state-error-text"><span class="left ui-icon ui-icon-unlocked"></span>This setting exposes your data <em>without authentication</em>.</span>
            <span class=""><span class="left ui-icon ui-icon-info"></span>Original project data cannot be changed with a <code>read</code> permission.</span>
            <span class=""><span class="left ui-icon ui-icon-notice"></span>Allowing this is not recommended unless you have a specific need in mind.</span>
        </div>
    </label>
    <label for="OACw" class="publicLabel">
        <input id="OACw" name="OACw" type="checkbox"  value="true" <%if(permitOACw)out.print("checked");%>/>OAC Write
        <div class="publicDesc">
            <h3>Open Annotation Collaboration</h3>
                <code>Write Permission</code>
                <p>This setting allows external (not originating from t&#8209;pen.org) tools and services to write data from the T&#8209;PEN database. Projects with this permission can load annotations and transcriptions into other tools <em>and changes saved</em> for in-depth processing not available on T&#8209;PEN.</p>
            <span class="ui-state-error-text"><span class="left ui-icon ui-icon-unlocked"></span>This setting exposes your data <em>without authentication</em>.</span>
            <span class="ui-state-error-text"><span class="left ui-icon ui-icon-alert"></span>Original project data can be changed with a <code>write</code> permission.</span>
            <span class=""><span class="left ui-icon ui-icon-notice"></span>Allowing this is not recommended unless you have a specific need in mind.</span>
        </div>
    </label>
    <h6>View and Export</h6>
    <label for="readTrans" class="publicLabel">
        <input id="readTrans" name="readTrans" type="checkbox"  value="true" <%if(permitRead)out.print("checked");%>/>View Project
        <div class="publicDesc">
            <h3>View Project</h3>
                <code>Read Permission</code>
                <p>This setting allows users logged into T&#8209;PEN to <strong>view</strong> this project in the transcription interface. Users cannot make any changes or view any data not revealed in the interface.</p>
            <span class="ui-state-error-text"><span class="left ui-icon ui-icon-unlocked"></span>This setting does not track viewers and provides no protections against manual copying.</span>
            <span class=""><span class="left ui-icon ui-icon-info"></span>Original project data cannot be changed with a <code>read</code> permission.</span>
            <span class=""><span class="left ui-icon ui-icon-check"></span>This is the most restrictive way to make a project public.</span>
        </div>
    </label>
    <label for="publicExport" class="publicLabel">
        <input id="publicExport" name="publicExport" type="checkbox"  value="true" <%if(permitExport)out.print("checked");%>/>Export Project
        <div class="publicDesc">
            <h3>Export Project</h3>
                <code>Read Permission</code>
                <p>This setting allows users logged into T&#8209;PEN to <strong>export</strong> this project through the T&#8209;PEN interface. Users cannot make any changes to original data.</p>
            <span class="ui-state-error-text"><span class="left ui-icon ui-icon-unlocked"></span>This setting does not track exports and provides no protections against copying.</span>
            <span class=""><span class="left ui-icon ui-icon-info"></span>Original project data cannot be changed with a <code>read</code> permission.</span>
            <span class=""><span class="left ui-icon ui-icon-notice"></span>Even without this permission, a clever and malicious user may capture data from any public project.</span>
        </div>
    </label>
    <label for="projectCopy" class="publicLabel">
        <input id="projectCopy" name="projectCopy" type="checkbox"  value="true" <%if(permitCopy)out.print("checked");%> <%if(thisProject.containsUserUploadedManuscript())out.print("disabled title='This project contains private images.'");%>/>Copy Project
        <div class="publicDesc">
            <h3>Copy Project</h3>
                <code>Read Permission</code>
                <p>This setting allows users logged into T&#8209;PEN to <strong>copy</strong> this project and work independently. Users cannot make any changes to original data.</p>
            <span class="ui-state-error-text"><span class="left ui-icon ui-icon-unlocked"></span>This setting does not track copies and does not prevent a user from making the copy more public than the original.</span>
            <span class=""><span class="left ui-icon ui-icon-info"></span>Original project data cannot be changed with a <code>read</code> permission.</span>
            <span class=""><span class="left ui-icon ui-icon-notice"></span>There is no way to merge or collate different branches of an original project.</span>
            <span class=""><span class="left ui-icon ui-icon-notice"></span>Projects containing private images cannot be copied.</span>
        </div>
    </label>
    <h6>Public Modifications</h6>
    <label for="modNotes" class="publicLabel">
        <input id="modNotes" name="modNotes" type="checkbox"  value="true" <%if(permitNotes)out.print("checked");%>/>Modify Notes
        <div class="publicDesc">
            <h3>Modify Notes</h3>
                <code>Write Permission</code>
                <p>This setting allows users logged into T&#8209;PEN to <strong>modify</strong> any data in the notes fields. Changes are not restricted, but are tracked in the History with a username and timestamp.</p>
            <span class="ui-state-error-text"><span class="left ui-icon ui-icon-alert"></span>Original project data can be changed with a <code>write</code> permission.</span>
            <span class=""><span class="left ui-icon ui-icon-info"></span>The transcription and notes fields are controlled by separate permissions.</span>
        </div>
    </label>
    <label for="modTrans" class="publicLabel">
        <input id="modTrans" name="modTrans" type="checkbox"  value="true" <%if(permitModify)out.print("checked");%>/>Modify Transcription
        <div class="publicDesc">
            <h3>Modify Transcription</h3>
                <code>Write Permission</code>
                <p>This setting allows users logged into T&#8209;PEN to <strong>modify</strong> any data in the transcription fields. Changes are not restricted, but are tracked in the History with a username and timestamp.</p>
            <span class="ui-state-error-text"><span class="left ui-icon ui-icon-alert"></span>Original project data can be changed with a <code>write</code> permission.</span>
            <span class=""><span class="left ui-icon ui-icon-info"></span>The transcription and notes fields are controlled by separate permissions.</span>
        </div>
    </label>
    <label for="modAnno" class="publicLabel">
        <input id="modAnno" name="modAnno" type="checkbox"  value="true" <%if(permitAnnotation)out.print("checked");%>/>Modify Annotations
        <div class="publicDesc">
            <h3>Modify Annotations</h3>
                <code>Write Permission</code>
                <p>This setting allows users logged into T&#8209;PEN to <strong>modify</strong> any annotation. This includes insertion, deletion, and modification.</p>
            <span class="ui-state-error-text"><span class="left ui-icon ui-icon-alert"></span>Original project data can be changed with a <code>write</code> permission.</span>
            <span class=""><span class="left ui-icon ui-icon-info"></span>Only annotation modifications are allowed with this permission.</span>
        </div>
    </label>
    <label for="modParsing" class="publicLabel">
        <input id="modParsing" name="modParsing" type="checkbox"  value="true" <%if(permitParsing)out.print("checked");%>/>Modify Line Parsing
        <div class="publicDesc">
            <h3>Modify Line Parsing</h3>
                <code>Write Permission</code>
                <p>This setting allows users logged into T&#8209;PEN to <strong>modify</strong> line parsing. This includes insertion, deletion, and modification.</p>
            <span class="ui-state-error-text"><span class="left ui-icon ui-icon-alert"></span>Original project data can be changed with a <code>write</code> permission.</span>
            <span class="ui-state-error-text"><span class="left ui-icon ui-icon-alert"></span>If a user deletes a column which contains transcription data, the attached data will also be removed.</span>
        </div>
    </label>
    <label for="modButton" class="publicLabel">
        <input id="modButton" name="modButton" type="checkbox"  value="true" <%if(permitButtons)out.print("checked");%>/>Modify Buttons and Tags
        <div class="publicDesc">
            <h3>Modify Buttons</h3>
                <code>Write Permission</code>
                <p>This setting allows users logged into T&#8209;PEN to <strong>modify</strong> any special character or XML tag buttons in the project (including addition and deletion).</p>
            <span class="ui-state-error-text"><span class="left ui-icon ui-icon-alert"></span>Original project data can be changed with a <code>write</code> permission.</span>
            <span class=""><span class="left ui-icon ui-icon-notice"></span>Changes to buttons are not tracked in the project log.</span>
        </div>
    </label>
    <label for="modMetadata" class="publicLabel">
        <input id="modMetadata" name="modMetadata" type="checkbox"  value="true" <%if(permitMetadata)out.print("checked");%>/>Modify Metadata
        <div class="publicDesc">
            <h3>Modify Metadata</h3>
                <code>Write Permission</code>
                <p>This setting allows users logged into T&#8209;PEN to <strong>modify</strong> project metadata or upload a custom header.</p>
            <span class="ui-state-error-text"><span class="left ui-icon ui-icon-alert"></span>Original project data can be changed with a <code>write</code> permission.</span>
            <span class=""><span class="left ui-icon ui-icon-notice"></span>Changes to metadata are not tracked in the project log.</span>
        </div>
    </label>
        <input type="reset" value="Clear All" onclick="$('.publicLabel').children('input').prop('checked',false);return false;" class="tpenButton ui-button" />    
    <input type="submit" name="publicOptions" value="Submit Changes"  class="tpenButton ui-button" />    
</form>
                    </div>
                </div><%}%>
                <div id="addingTools" class="ui-corner-all ui-widget ui-widget-content">
                    <a id="closeAddingTools" href="#" onclick="$('#addingTools').fadeOut('normal');return false;" class="right tpenButton ui-button">Close<span class="ui-icon ui-icon-close right"></span></a>
                    <h3>Add an iFrame Tool</h3>
                    <iframe src="iframe.html" id="addToolFrame"></iframe>
                    <input id="addToolName" type="text" maxlength="25" placeholder="Tool Name" />
                    <input id="addToolURL" type="text" placeholder="Full URL of tool" />
                    <a class="tpenButton" id="addToolPreview" 
                       onclick="$('#addToolFrame').attr('src',$('#addToolURL').val());">
                        Test Link
                    </a>
                    <a class="tpenButton" id="addToolCommit"
                       onclick="addTool();">
                        Add
                    </a>
                    <div id="addToolInfo" class="clear-left">
                        <p>
                            Add a useful name and complete URL of the page you would like loaded as a tool.
                        </p>
                        <p>
                            Some pages will not allow themselves to be loaded into an iFrame, so you should test your tool before adding it.
                        </p>
                        <p>
                            These tools are connected to specific projects and will be available for all group members.
                        </p>
                    </div>
                </div>
        <%@include file="WEB-INF/includes/privateUpload.jspf" %>
        <%@include file="WEB-INF/includes/projectTitle.jspf" %>
                <script type="text/javascript">
                                $(function() {
                                        $("#copyProjectBtn").click(function(){
                                            var cfrm = confirm('All transcriptions, parsings, and buttons will be copied into a new project.\n\nContinue?');
                                            return cfrm;
                                        });
                                        $("#xmlSelectAll").click(function(){
                                            if ($(this).attr('state') == "on"){
                                                $(this).text("Select All").attr('state','off');
                                                $("[name^='removeTag']").attr("checked",false);
                                            } else {
                                                $(this).text("Deselect All").attr('state','on');
                                                $("[name^='removeTag']").attr("checked",true);
                                            }
                                        });
                                        $("#export").find("button").hover(function(){$(this).toggleClass("ui-state-hover")});
                                        $( "#popupNote").click(function() {
                                            $( "#noteForm" ).fadeIn(1000);
                                            return false;
                                        });
                                        $( "#clearNote").click(function() {
                                            $( "#noteForm" ).fadeOut(500);
                                            return false;
                                        });
                                        $('#tabs').tabs({ 
                                            fx: { opacity: 'toggle', duration:250 },
                                            show: function(ui) {equalHeights("tall",100);equalWidth();}
                                        });
                                        if (selecTab) $('#tabs').tabs('option','selected',selecTab);
                                        $('span.delete').hover(
                                        function(){$(this).parent('li').find("a.tpenButton").addClass     ("ui-state-error strikeout");},
                                        function(){$(this).parent('li').find("a.tpenButton").removeClass  ("ui-state-error strikeout");}
                                    );
                                        $(".formatDiv").addClass('ui-corner-all');
                                        $(".format").click(function(){
                                            $(this).next("div.formatDiv").slideToggle(500);
                                        });
                                        $("#addManuscript").click(function(){
                                            $("#wrapper").append("<div class='ui-widget-overlay' id='overlay' style='display:none;'></div>");
                                            $("div#addToProject,#overlay").show('fade',500);
                                        });
                                        $("#closePopup").click(function(){
                                            $("div#addToProject,#overlay").hide('fade',500,function(){
                                                $("#overlay").remove();
                                            });
                                        });
                                        $("#xmlImportBtn,#inviteUserBtn,#templateBtn").click(function(){
                                            $(this).toggleClass("ui-state-active")
                                            .next("form").add("#QENIx").slideToggle(function(){
                                                equalHeights("tall",200);
                                            }); 
                                        });
                                        $("#xmlValidateBtn").click(function(){
                                            $("#xmlValidate").slideToggle(function(){
                                                equalHeights("tall",200);
                                            }); 
                                        });
                                        $("#linebreakingBtn").click(function(){
                                            $("#linebreaking").slideToggle(function(){
                                                equalHeights("tall",200);
                                            }); 
                                        });
                                        $("#wordbreakBtn").click(function(){
                                            $("#wordbreak").slideToggle(function(){
                                                equalHeights("tall",200);
                                            }); 
                                        });
//                                        $("#logFilter a").not($("#openNote")).hide(); //delete this line to enable the filters on the projectLog
                                        $("#logFilter a").not($("#openNote")).click(function(){
                                            $(this).toggleClass('ui-state-disabled');
                                            $("#projectLog div."+$(this).attr("filter")).parent().parent().slideToggle();
                                        });
                                        /* Handlers for Export Options */
                                        $("#inline, #pageonly").click(function(){
                                            if(this.checked) {
                                                $("#exportAlert").slideDown();
                                                $("#exportHyphenation").attr("disabled",false);
                                            }
                                            if($("#notesLine").attr("checked") || $("#notesSideBySide").attr("checked")){
                                                $("#notesRemove").attr("checked", true);
                                                //alert("Notes options were incompatable with this selection and have been changed.");
                                            }
                                            if(this.id=="inline") $("#notesFootnote").attr("disabled",true);
                                            $("#notesSideBySide, #notesLine").attr("disabled",true);
                                        });
                                        $("#newline").click(function(){
                                            if(this.checked) $("#exportHyphenation").attr("disabled",true);
                                            $("#notesSideBySide, #notesLine, #notesFootnote").attr("disabled",false);
                                        });
                                        $("#notesSideBySide,#notesLine").click(function(){
                                            if(this.checked) {
                                                $("#exportAlert").slideDown();
                                                $("#exportWordbreak, #pageonly, #inline").attr("disabled",true);
                                                if($("#pageonly").attr('#checked')||$("#inline").attr('checked')) {
                                                    //alert('Linebreaking options were incompatable with your selection and have been changed.');
                                                }
                                                $("#newline").attr({'checked': true,'disabled':false});
                                            }
                                        });
                                        $("#notesEndnote,#notesFootnote,#notesRemove").click(function(){
                                            if(this.checked) {
                                                $("#exportWordbreak, #newline, #exportLinebreakString, #useLinebreakString, #pageonly, #inline").attr("disabled",false);
                                                $("#exportAlert").slideUp();
                                            }
                                        });
                                        //                $("#metadataOption").click(function(){
                                        //                    if ($("#metadataSelect").is(":checked")) $("#metadataPreview").slideDown();
                                        //                    else $("#metadataPreview").slideUp();
                                        //                })
                                        $("#pdf,#rtf").click(function(){
                                            if(this.checked) {
                                                $(".xmlHide").show();
                                                $(".xmlDisclaimer").hide();
                                                $("#color").attr("disabled",false);
                                            }
                                        });
                                        $("#xml").click(function(){
                                            if(this.checked) {
                                                var areChanges = false;
                                                $(".xmlHide").hide();
                                                $(".xmlDisclaimer").show();
                                                $("#color").attr("disabled",true);
                                                if ($("#color").attr("checked")){
                                                    areChanges = true;
                                                    $("#bw").attr("checked",true);
                                                }
                                                if ($("#notesSideBySide,#notesEndnote,#notesFootnote,#notesLine").attr("checked")) {
                                                    areChanges = true;
                                                    $("#notesRemove").attr("checked",true);
                                                }
                                                if ($("#pageonly,#newline").attr("checked")) {
                                                    areChanges = true;
                                                }
                                                $("#inline").attr("checked",true);
                                                //if (areChanges) alert("Some selections are not supported in plaintext export and have been changed.");
                                            }
                                        });
                                        $(".beginFolio,.endFolio").change(function(){
                                            var thisIs = ($(this).hasClass('beginFolio')) ? "beginFolio" : "endFolio";
                                            $('.'+thisIs).val(this.value);
                                            var first = $(this).parent().find(".beginFolio");
                                            var last = $(this).parent().find(".endFolio");
                                            var firstFolio = first.children("option:selected").index();
                                            var lastFolio = last.children("option:selected").index();
                                            if (firstFolio > lastFolio) {
                                                first.add(last).addClass("ui-state-error").attr("title","Folio range does not include any pages.");
                                                $('#submitLimit').prop('disabled',true).addClass('ui-state-disabled');
                                            } else {
                                                first.add(last).removeClass("ui-state-error").attr("title","");
                                                $('#submitLimit').prop('disabled',false)
                                                    .removeClass('ui-state-disabled')
                                                    .html("Submit Set Range");
                                            }
                                        });

                                        $(".partnerListing").find("input:radio").click(function(event){
                                            event.preventDefault();
                                        });
                                        $("#QENI").find(".partnerListing").click(function(){
                                            $(this).find("input:radio").attr("checked",true);
                                            $("#QENI").scrollTop(0).find("input:checked")
                                            .parent(".partnerListing")
                                            .addClass("ui-state-active")
                                            .fadeOut(250,function(){
                                                $(this).css("opacity", 0)
                                                .prependTo("#QENI").slideDown()
                                                .animate({"opacity":1},250)
                                            })
                                            .siblings().removeClass("ui-state-active");
                                            if($("#QENI").parent("form").find("input[type='submit']").hasClass('hide')){
                                                $("#QENI").parent("form").find("input[type='submit']").slideDown().removeClass('hide');
                                            }
                                        });
                                        $("#disconnect").find(".partnerName").click(function(event){
                                            if(event.target != this){return true;}
                                            $("#tabs").tabs("select",4);
                                            setTimeout('$("#tpenSubmit").click()',350);
                                        })
                                        .find(".ui-icon-closethick").bind({
                                            mouseenter: function(){
                                                $("#deleteConnection").stop(true,true).slideDown();
                                                $("#disconnect").addClass("ui-state-error");
                                            },
                                            mouseleave: function(){
                                                $("#deleteConnection").stop(true,true).slideUp();
                                                $("#disconnect").removeClass("ui-state-error");
                                            },
                                            click: function(){
                                                $("#disconnect").find("input:radio").attr("checked",true);
                                                $("#QENI").find(".partnerListing").removeClass("ui-state-active")
                                                .parents("form").find("input[type='submit']").click();
                                            }
                                        })
                                        $("#template").find("input:text").keyup(function(){
                                            $("#template").find(".partnerName").html($(this).val());
                                        }).end()
                                        .find("textarea").keyup(function(){
                                            $("#template").find(".partnerDescription").html($(this).val());
                                        });
                                        $("#toolSelection").submit(function(){
                                            $(this).find(".projectTools").each(function(){
                                                var thisInput = $(this).find("input");
                                                thisInput.val($(this).find("span").text()+"TPENTOOLURL"+thisInput.val());
                                            });
                                        });
                                        $(".publicLabel").children('input').on('change',function(){$("#publicOptionResult").fadeOut('slow');});
$("#samplePreview").hover(function(){
                                            $(this).find("img").css({
                                                "width" :   "auto",
                                                "height":   "auto"
                                            });
                                            var posX = $(this).offset().left;
                                            var posY = $(this).offset().top;
                                            var sampleX = $(this).width();
                                            var sampleY = $(this).height();
                                            var imgX = $(this).find("img").width();
                                            var imgY = $(this).find("img").height();
                                            var imgLeft = 0;
                                            var imgTop= 0;
                                            $(document).bind('mousemove', function(e){
                                                imgLeft = -(e.pageX-posX) * (imgX-sampleX-68) / sampleX;    // 68 pixel nudge for padding and box-model inconsistencies
                                                imgTop  = -(e.pageY-posY) * (imgY-sampleY-30) / sampleY;    // 30 pixel nudge for padding and box-model inconsistencies
                                                //alert("e.pageX:"+e.pageX+"\nposX:"+posX+"\nposY:"+posY+"\nsampleX:"+sampleX+"\nsampleY:"+sampleY+"\nimgLeft:"+imgLeft+"\nimgTop:"+imgTop);
                                                $("#samplePreview").find("img").css({
                                                    "left"  :   imgLeft,
                                                    "top"   :   imgTop
                                                });
                    //                        $("#samplePreview span").html(Math.round(e.pageY)+", "+Math.round(posY)+", "+Math.round(sampleY));
                                            });
                                        },function(){
                                            $(this).find("img").css({
                                                "width" :   "100%",
                                                "height":   "100%",
                                                "top"   :   "30px",
                                                "left"  :   0
                                            });
                                            $(document).unbind('mousemove');
                                        }).find("img").css({
                                            "width" :   "100%",
                                            "height":   "100%"
                                        });
                                        });
                                        
                                    function navigateTo(dropdown){
                                        $("body").addClass(" ui-state-disabled");
                                        document.location='transcription.jsp?p='+dropdown.value;
                                    }
                                    function equalHeights (eClass, minHeight){
                                        var minHeight = minHeight;
                                        var tabIndex = $("#tabs").tabs().tabs('option','selected')+1;
                                        $("#tabs-"+tabIndex).find("."+eClass).each(function(){
                                            minHeight = ($(this).height()>minHeight) ? $(this).height() : minHeight; 
                                        })
                                        $("#tabs-"+tabIndex).find("."+eClass).css({"min-height":minHeight+"px"});
                                    }
                                    var selectWordbreak = "-";
                                    function customWordbreak(){
                                        selectWordbreak = $('#userWord').val();
                                        $("#currentSetting").html(selectWordbreak);
                                    }
                                    function scrubListings (){
                                        $("#listings").ajaxStop(function(){
                                            $("#listings a[href *= 'transcription']").hide();
                                        });
                                    }
                                    function addTool() {
                                        var name=$("#addToolName").val();
                                        var URL=$("#addToolURL").val();
                                        if(name.length<1){
                                            $("#addToolName").addClass('ui-state-error').one('change',function(){$(this).removeClass('ui-state-error')});
                                            return false;
                                        }
                                        if(URL.length<5){
                                            $("#addToolURL").addClass('ui-state-error').one('change',function(){$(this).removeClass('ui-state-error')});
                                            return false;
                                        }
                                        var newTool = ['<label class="projectTools"><input name="projectTool[]" type="checkbox" checked="true" value="',
                                                URL,'" title="',name,'"><span contenteditable="true">',name,'</span></label>'].join('');
                                            $(".projectTools").eq(0).before(newTool);
                                        $("#addingTools").fadeOut();
                                    }
                </script>
    </body>
    <%    //sent request to link schema
        if (request.getParameter("xmlImport") != null) {
            boolean inviteSuccess = thisProject.setSchemaURL(request.getParameter("url"));
            if (inviteSuccess) {
    %>
    <script>
        $("#xmlImportDiv").prepend("<span class='ui-icon ui-icon-circle-check left'></span>XML successfully linked");
        $("#importBtnFromSchemaBtn, #xmlValidateDiv").show();
    </script>
    <%        } else {
    %><script>$("#xmlImportDiv").prepend("<span class='ui-icon ui-icon-alert left'></span>Failed to import. Please check the link and try again. For more information on compatable file formats, check the wiki documentation.");$("#xmlImportDiv").addClass("ui-corner-all ui-state-error");</script><%                }
        }
    %>
</html>
<%}%>