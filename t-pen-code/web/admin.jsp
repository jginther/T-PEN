<%-- 
    Document   : admin
    Created on : Feb 25, 2011, 6:09:12 PM
    Author     : jdeerin1
--%>
<%@page import="textdisplay.WelcomeMessage"%>
<%@page import="textdisplay.Project"%>
<%@page import="java.sql.Timestamp"%>
<%@page import="java.util.*"%>
<%@page import="org.owasp.esapi.ESAPI" %>
<%@page import="utils.Tool"%>
<%@page import="utils.UserTool"%>
<%@page import="textdisplay.Manuscript" %>
<%@page import="textdisplay.CityMap" %>
<%@page import ="user.*"%>
<%@page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN"
    "http://www.w3.org/TR/html4/loose.dtd">
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title>User Account Management</title>
        <link rel="stylesheet" href="css/tpen.css" type="text/css" media="screen, projection">
        <link rel="stylesheet" href="css/print.css" type="text/css" media="print">
        <!--[if lt IE 8]><link rel="stylesheet" href="css/ie.css" type="text/css" media="screen, projection"><![endif]-->
        <link type="text/css" href="css/custom-theme/jQuery.css" rel="Stylesheet" />
        <script type="text/javascript" src="https://ajax.googleapis.com/ajax/libs/jquery/1.7.1/jquery.js"></script>
        <script type="text/javascript" src="https://ajax.googleapis.com/ajax/libs/jqueryui/1.8.16/jquery-ui.js"></script> 
        <script type="text/javascript" src="js/tpen.js"></script>  
        <script type="text/javascript" src="js/manuscriptFilters.js"></script>  
        <style type="text/css">
            #userAccount, #ms, #manageUsers, #options, #about, #reportsTab { margin: 0; padding: 0;}
            #reportsTab li,#userAccount li, #manageUsers li, #ms li, #options li, #about li { margin: 0 4px 3px 3px; padding: 0.4em; padding-left: 1.5em; height: 100%;overflow: hidden; float:left; width:29%; position: relative;}
            #manageUsers li {max-height: 350px;overflow: auto;}
            #tabs-3 {padding-bottom: 120px;}
            #ms li {width:98% !important; height:auto !important;}
            #ms textarea {width:600px;height:8em;}
            #userAccount li ul,#ms li ul,#manageUsers li ul, #options li ul, #about li ul {padding: 0;margin: 0;}
            #userAccount li ul li,#ms li ul li,#manageUsers li ul li, #options li ul li, #about li ul li {list-style:outside none;padding:0;width:100%;}
            .approved, .deactivated {display: block;clear:both;}
            .approved:after, .deactivated:after {content:attr(title);}
            .deactivated {color:red;}
            #userSummary span {display: block;width: 100%;}
            label {float: none; font-weight: normal;padding: 0;width: auto;display: block;} /* reset formatting */
            #newUserApproval label:hover {color:green;}
            #userDeactivation label:hover,#denyUsers label:hover {color:red;}
            #manageUsers label {display: block;width:100%;position: relative;} /*Keep checkboxes lined up*/
            #newUserApproval .approved {color: darkgreen;}
            #manageUsersBtn, #emailAlert {width:50%;margin: 0 auto 10px; padding: 5px 0;display: block;text-align: center;text-decoration: none;}
            a.ui-corner-all {text-align: center;text-decoration: none;}
            .iprs,.archAlert {display: none;}
            #closePopup:hover {color: crimson;cursor: pointer;}
            #adminMS,#userEmailList {position: absolute; top:25%; left:25%; width:50%; display: none;z-index: 500;}
            #form {height: 425px; margin: 0 auto; width:515px;border-width: 1px; 
                   -webkit-box-shadow: -1px -1px 12px black;
                   -moz-box-shadow: -1px -1px 12px black;
                   box-shadow: -1px -1px 12px black;
            }
            #listings a {position: relative;float:left;padding: 0 4px;}
            #adminManuscript {max-width: 120px;padding: 15px;margin:0 15%;position: relative;}
            #about li {height: 400px;overflow: auto;}
            #contact,#FBextra {width:100%;height:6em;}
            .contactCOMMENT{padding:2px;text-align: center;position: relative;z-index: 2;cursor: pointer;}
            .contactDiv{margin:-4px 5px 2px;clear:left;background: url(images/linen.png);padding:6px 2px 2px;
/*                        display:none;*/
                        overflow: hidden;z-index: 1; border: 1px solid #A68329;
                       -moz-box-shadow: -1px -1px 2px black;
                       -webkit-box-shadow: -1px -1px 2px black; 
                       box-shadow:-1px -1px 2px black;}
            #overlay {display: none;}
            #overlayNote{position: fixed;top:2%;right:2%;white-space: nowrap;font-size: large;font-weight: 700;font-family: monospace;text-shadow:1px 1px 0 white;}
            .mapCheck {display: none;}
            .mapCheck[checked],.mapCheck:checked {display: inline-block;}
            .msSelect {display: none;padding: 3px;background-color: rgba(66,66,00,.3);position: relative;cursor: pointer;white-space: nowrap;overflow: hidden;width: 100%;text-overflow:ellipsis;}
            .msSelect.newCity {display:block;}
            .msSelect:hover {background-color: rgba(166,166,90,.1)}
            .mapCheck[checked]:after,.mapCheck:checked:after{
                content: '';
                background-color: green;
                position: absolute;
                top:0;left:0;
                width:100%;height:100%;
                opacity:.2;
            }
            #cityMapContain {float: right;width:300px;height:200px;position: relative;}
            #cityMap,#cityMapZoom {position: absolute;}
            #cityMap {top:0;left:0;width:100%;}
            #cityMapZoom {bottom:-5px;right:-3px;width: 30%;height:30%;overflow: hidden;box-shadow:-1px -1px 3px black;}
            #cityMapZoom img {position:relative;top:-100%;left:0;}
            #mapCities {position: relative;float: left;clear:left;max-height: 200px;overflow: auto;max-width: 300px;}
            #updateCityMap button,#updateCityMap input {padding: .4em;float: left;
            }
            #updateCityMap p {max-width: 300px;}
            #cityString {width:70%;}
            #updateMap {width: 25%;float:right !important;}
            #clearMap {width:100%;clear:left;}
            #mapSearch {
                width:300px;
            }
            #welcomeMsg textarea {
                height:2em;
            }
            #welcomeMsg textarea, welcomeForm {
                -o-transition: height, .5s;
                -moz-transition: height .5s;
                -webkit-transition: height .5s;
                transition: height .5s;
            }
            #welcomeForm:hover textarea {
                height:18em;
            }
            #welcomeForm {
                position:absolute !important;
                width:96% !important;
                height:auto !important;
                bottom:0;
            }
        </style>
        <script>
            var selecTab<%if (request.getParameter("selecTab") != null) {
                out.print("=" + request.getParameter("selecTab"));
            }%>;
                var userList = '';
                function scrubListings (){
                    $("#listings").ajaxStop(function(){
                        $("#listings")
                        .find("a[href *= 'transcription']").remove().end()
                        .find("a").attr("href",function(){
                            var oldLink = $(this).attr("href");
                            $(this).attr('href',oldLink.replace("addMStoProject","manuscriptAdmin")+"&unrestricted=true");
                        }).html("<span class='ui-icon ui-icon-wrench left'></span>Modify");
                    });
                }
                function overlay (text) {
                    $("#userEmailList").val(text).add("#overlay").show('fade',500);
                }
                $(function() {
                    if (selecTab) $('#tabs').tabs('option','selected',selecTab);
                    /* Interface Feedback Handlers */
                    var awaitingApproval = $('#newUserApproval>input:checkbox').size()
                    if(awaitingApproval>0){
                        var tasklist = "Click on the 'Manage Users' tab to complete these pending tasks:<span class='approved'><span class='left ui-icon ui-icon-check'></span>" + awaitingApproval + " users waiting for approval.</span>";
                        if (awaitingApproval==1) tasklist.replace("users","user")
                        $('#taskList').prepend(tasklist)
                        .parent('li').fadeIn(2000);
                    };
                    $("#newUserApproval").find("input:checkbox").change(function(){
                        $("#manageUsersBtn,#userAlert").slideDown();
                        $(this).parent("label").toggleClass("approved");
                    });
                    $("#userDeactivation,#denyUsers").find("input:checkbox").on('change',function(){
                        $("#manageUsersBtn").slideDown();
                        $("#userAlert").show();
                        $(this).parent("label").toggleClass("deactivated");
                    });
                    $('#overlay').on("click",function(event){
                        $(this).hide(250);
                        $(".popover").hide();
                    });     
                    $("input:submit").addClass("ui-state-default ui-corner-all ui-button")
                    .add("#manageUsersBtn").hover(
                    function(){$(this).addClass     ("ui-state-hover");},
                    function(){$(this).removeClass  ("ui-state-hover");}
                );
                    $("#iprs").change(function(){
                        if ($(".iprs").is(":visible")){
                            $(".iprs:visible").hide("slide",{direction:"left"},"fast",function(){
                                $("#ipr"+$("#iprs").val()).show("slide",{direction:"right"},"fast");
                            });
                        } else {
                            $("#ipr"+$("#iprs").val()).show("slide",{direction:"right"});
                        }   
                    });
                    $("#archiveAlert").change(function(){
                        if ($(".archAlert").is(":visible")){
                            $(".archAlert:visible").hide("slide",{direction:"left"},"fast",function(){
                                $("#archAlert"+$("#archiveAlert").val()).show("slide",{direction:"right"},"fast");
                            });
                        } else {
                            $("#archAlert"+$("#archiveAlert").val()).show("slide",{direction:"right"});
                        }   
                    });
                    $("#adminManuscript").click(function(){
                        $("div#adminMS,#overlay").show('fade',500);
                    });
                    $("#closePopup").click(function(){
                        $("#overlay").click();
                    });
                    $(".contactDiv").addClass('ui-corner-all');
//                    $(".contact").click(function(){
//                        $(this).siblings(".contactDiv").slideToggle(500);
//                    });
                });
                function manageUsers(){
                    $("#manageUsersBtn").children("span").switchClass("ui-icon-alert","ui-icon-check");
                    var saveChangesURL = ["admin.jsp?selecTab=2"];
                    var $changes = $("input:checked");
                    var approve = new Array();
                    var deactivate = new Array();
                    var deny = new Array();
                    $changes.filter("[name^='approve']").each(function(index){
                        approve[index] = "approveUser[]="+this.value;
                    });
                    $changes.filter("[name^='deactivate']").each(function(index){
                        deactivate[index] = "deactivateUser[]="+this.value;
                    });
                    $changes.filter("[name^='eliminate']").each(function(index){
                        deny[index] = "denyUser[]="+this.value;
                    });
                    if(approve.length>0)    saveChangesURL.push(approve.join("&"));
                    if(deactivate.length>0) saveChangesURL.push(deactivate.join("&"));
                    if(deny.length>0)       saveChangesURL.push(deny.join("&"));
                    window.location.href = saveChangesURL.join("&");
                }
        </script>
    </head>
    <body>
        <div id="wrapper">
            <div id="header"><p align="center" class="tagline">transcription for paleographical and editorial notation</p></div>
            <div id="content">
                <h1><script>document.write(document.title); </script></h1>
                <p>Use this page to manage your account, change your password, or manage manuscripts and user privileges.</p>
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
                    <%
                        user.User thisUser = null;
                        if (session.getAttribute("UID") != null) {
                            thisUser = new user.User(Integer.parseInt(session.getAttribute("UID").toString()));
                    %>
                    <ul>
                        <li><a title="Reset or change your password; view account privileges" href="#tabs-1">User Account</a></li>
                        <li><a title="Alter IPR statements, restrict access, update global metadata" href="#tabs-2">Manuscripts</a></li>
                        <%if (thisUser.isAdmin()) { //hiding non-Admin tab%>
                        <li><a title="Manage Users" href="#tabs-3">Manage Users</a><div id="userAlert" class='ui-icon-alert ui-icon right' style="display:none;margin: 8px 8px 0 0;"></div></li>
                        <li><a title="Reports" href="#reportsTab">Reports</a></li>
                        <%}%>
                        <li><a title="About the T-PEN project" href="#aboutTab">About T&#8209;PEN</a></li>
                    </ul>
                    <div id="tabs-1">
                        <% } else {%>
                        <div id="userUnknown2">
                            <div class="left inline" style="width:300px;"> <form id="login" action="login.jsp" method="POST" >
                                    <fieldset>
                                        <legend>Login Here:</legend>
                                        <label for="uname">Email</label><input class="text" type="text" name="uname"/><br/>
                                        <label for="password">Password</label><input  class="text" type="password" name="password"/><br/>
                                        <input type="hidden" name="ref" value="admin.jsp"/>
                                        <span class='buttons right'><button type="submit" title="Log In" value="log in">Log In</button></span>
                                    </fieldset>
                                </form></div>
                        </div>
                        <%                        }
                            //process any submitted requests
                            user.User[] unapprovedUsers = user.User.getUnapprovedUsers();
                            user.User[] allUsers = user.User.getAllActiveUsers();
                            if (thisUser != null) {
                                //String to return completion information to user on Tabs-3
                                StringBuilder manageUserFeedback = new StringBuilder("");
                                //a request to approve a new user, generate a password and email it to them
                                if (request.getParameterValues("approveUser[]") != null) {
                                    String[] approveUser = request.getParameterValues("approveUser[]");
                                    if (thisUser.isAdmin()) {
                                        for (int i = 0; i < approveUser.length; i++) {
                                            user.User newUser = new user.User(Integer.parseInt(approveUser[i]));
                                            if (newUser.requiresApproval()) {
                                                newUser.resetPassword();
                                                // load tools
                                                Tool.initializeTools(newUser.getUID());
                                                manageUserFeedback.append("<span title=' has been notified of account approval.' class='approved'><span class='left ui-icon ui-icon-check'></span>").append(newUser.getFname()).append(" ").append(newUser.getLname()).append("</span>");
                                            } else {
                                                //out.print("That user is not awaiting your approval!");
                                                manageUserFeedback.append("<span title=' no longer required approval.' class='approved'><span class='left ui-icon ui-icon-check'></span>").append(newUser.getFname()).append(" ").append(newUser.getLname()).append("</span>");
                                                return;
                                            }
                                        }
                                    }
                                }
                                //a request to deactivate a user so they can no longer log in. Does NOT delete any associated content
                                if (request.getParameterValues("deactivateUser[]") != null) {
                                    String[] deactivateUser = request.getParameterValues("deactivateUser[]");
                                    for (int i = 0; i < deactivateUser.length; i++) {
                                        if (thisUser.isAdmin()) {
                                            user.User newUser = new user.User(Integer.parseInt(deactivateUser[i]));
                                            newUser.deactivate();
                                            manageUserFeedback.append("<span title=' has been deactivated.' class='deactivated'>").append(newUser.getFname()).append(" ").append(newUser.getLname()).append("</span>");
                                        }
                                    }
                                }
                                if (request.getParameterValues("denyUser[]") != null) {
                                    String[] denyUser = request.getParameterValues("denyUser[]");
                                    for (int i = 0; i < denyUser.length; i++) {
                                        if (thisUser.isAdmin()) {
                                            user.User newUser = new user.User(Integer.parseInt(denyUser[i]));
                                            newUser.deactivate();
                                            manageUserFeedback.append("<span title=' has been removed.' class='deactivated'>").append(newUser.getFname()).append(" ").append(newUser.getLname()).append("</span>");
                                        }
                                    }
                                }
                                if (manageUserFeedback.length() > 3) {
                                    out.println("<script>");
                                    out.println("$(document).ready(function() {");
                                    out.println("$('#manageUserFeedback').html(\"" + manageUserFeedback.toString() + "\").fadeIn(2000);");
                                    out.println("});");
                                    out.println("</script>");
                                }


                                //a password update request
                                if (request.getParameter("newPassword") != null) {
                                    String pass = request.getParameter("newPassword");
                                    String conf = request.getParameter("confirmPassword");
                                    if (pass.compareTo(conf) == 0) {
                                        thisUser.updatePassword(pass);
                                        out.print("<br><br><h3>Password updated!</h3><br><br>");
                                    } else {
                                        out.print("<br><br><ul><h3>Passwords did not match; no change has been made.</h3></ul><br><br>");
                                    }
                                }
                                if (request.getParameter("welcome") != null) {
                                    if (!thisUser.isAdmin()) {
                String errorMessage = thisUser.getFname() + ", you have attempted something limited to administrators.";
            %><%@include file="WEB-INF/includes/errorBang.jspf" %><%
                return;
                                    }
                                    textdisplay.WelcomeMessage nwm = new WelcomeMessage();
                                    nwm.SetMessage(request.getParameter("welcome"));
                                }
                                if (request.getParameter("eula") != null) {
                                    if (!thisUser.isAdmin()) {
                String errorMessage = thisUser.getFname() + ", you have attempted something limited to administrators.";
            %><%@include file="WEB-INF/includes/errorBang.jspf" %><%
                return;
                                    }
                                    String archiveName = request.getParameter("name");
                                    textdisplay.Archive a = new textdisplay.Archive(archiveName);
                                    a.setIPRAgreement(request.getParameter("eula"));
                                }
                                if (request.getParameter("alert") != null) {
                                    if (!thisUser.isAdmin()) {
                String errorMessage = thisUser.getFname() + ", you have attempted something limited to administrators.";
            %><%@include file="WEB-INF/includes/errorBang.jspf" %><%
                return;
                                    }
                                    String archiveMsg = request.getParameter("alert");
                                    textdisplay.Archive a = new textdisplay.Archive(request.getParameter("name"));
                                    a.setMessage(archiveMsg);
                                }
                            }
                            //reinitiate the user lists for use on the rest of the page
                            unapprovedUsers = user.User.getUnapprovedUsers();
                            allUsers = user.User.getAllActiveUsers();
                            //if the person isnt logged in, only show them the 'reset my password via email' div
                            if (thisUser == null) { //reset a user's password based on their email address
                                if (request.getParameter("emailSubmitted") != null) {
                                    user.User toReset = new user.User(request.getParameter("email"));
                                    if (toReset.getUID() > 0) {
                                        if (!toReset.requiresApproval()) {
                                            toReset.resetPassword();
                                            out.print("<br><br><h3>Password reset!</h3><br>Please check your e-mail from TPEN@T&#8209;PEN.org for a new password.  If your e-mail does not arrive, please verify that it has not been caught by a spam filter.<br>");
                                        } else {
                                            out.print("This user does not exist or needs administrator approval before they can log in!");
                                            return;
                                        }
                                    } else {
                                        out.print("This user does not exist or needs administrator approval before they can log in!");
                                        return;
                                    }
                                }

                        %>
                        <div class="right" id="resetPassword" style="width:45%;">
                            <h3>Reset your Password</h3>
                            To reset your password and have the new password sent to your email address, please enter the email address associated with your account.
                            <form action="admin.jsp" method="POST">
                                <input type="text" name="email">
                                <input type="submit" name="emailSubmitted" value="Reset Password"/>
                            </form>
                        </div>
                        <%
                                //if they arent logged in, dont bother with showing them any of the other stuff
                                out.print("</div></div>\n<a class='returnButton' href='index.jsp'>Return to TPEN Homepage</a>\n</div>"); //close up the tab                                   
                                return;
                            }
                        %>
                        <ul id="userAccount" class="ui-helper-reset"> 
                            <li class="gui-tab-section">
                                <%if (request.getParameter("pleaseReset") != null) {
                                        System.out.print("<h3>Due to a server migration, we ask you to change your password below:<br></h3>");
                                    }
                                %>
                                <h3>Change your password</h3>
                                <div>
                                    <form action="admin.jsp" method="POST">
                                        <label>New Password
                                            <input type="password" name="newPassword" /></label>
                                        <label>Confirm Password
                                            <input type="password" name="confirmPassword" /></label>
                                        <input class="tpenButton right" type="submit">
                                    </form></div>
                            </li>
                            <li class="gui-tab-section">
                                <a class="tpenButton" href="index.jsp"><span class="ui-icon ui-icon-home right"></span>TPEN Homepage</a>
                            </li>
                            <li class="gui-tab-section" style="display:none;">
                                <h3>Task List</h3>
                                <div id="taskList"></div>
                            </li>
                            <li class="gui-tab-section" id="userSummary">
                                <h3>Account Information</h3>
                                Name: <%out.print(thisUser.getFname() + " " + thisUser.getLname());%> <br />
                                E-mail Login: <%out.print(thisUser.getUname());%> <br />
                                Status:<%if (thisUser.isAdmin()) {
                                        out.print("Administrator, ");
                                    }%>Contributor<%if (thisUser.requiresApproval()) {
                                                out.print(" (pending approval)");
                                            }%><br />
                                <%
                                    Project[] userProjects = thisUser.getUserProjects();
                                    if (userProjects.length > 0) {
                                        out.print("You are a member of " + userProjects.length + " project");
                                    }
                                    if (userProjects.length == 1) {
                                        out.print(", " + userProjects[0].getProjectName() + ".");
                                    } else {
                                        out.print("s:");
                                        for (int i = 0; i < userProjects.length; i++) {
                                            out.println("<span>" + (i + 1) + ". " + userProjects[i].getProjectName() + "</span>");
                                        }
                                    }
                                %>

                            </li>
                        </ul>
                    </div>
                    <!--                end of tab-1, user accounts-->

                    <div id="tabs-2">
                        <ul id="ms" class="ui-helper-reset"> 

                            <%                           //this request seeks to remove access restrictions for this manuscript.
                                if (request.getParameter("unrestrict") != null) {
                                    int msID = Integer.parseInt(request.getParameter("ms"));
                                    if (thisUser.isAdmin()) {
                                        textdisplay.Manuscript ms = new textdisplay.Manuscript(msID, true);
                                        ms.makeUnresricted();
                                    } else {
                                        out.print("Only admins can do that!");
                                        return;
                                    }
                                }
                                if (request.getParameter("restrict") != null) {


                                    int msID = Integer.parseInt(request.getParameter("ms"));
                                    if (thisUser.isAdmin()) {
                                        textdisplay.Manuscript ms = new textdisplay.Manuscript(msID, true);
                                        int controllingUID = Integer.parseInt(request.getParameter("uid"));
                                        ms.makeRestricted(controllingUID);
                                        ms.authorizeUser(controllingUID);
                                    }
                                }

                                if (thisUser.isAdmin()) { //hide non-Admin items%>
                                <li class="gui-tab-section">
                                <h3>Update IPR agreements </h3>
                                <select id="iprs">
                                    <option value="-1" selected>Select an archive</option>
                                    <%
                                        //allow admins to edit archive IPR agreements
                                        String[] archives = textdisplay.Archive.getArchives();
                                        for (int i = 0; i < archives.length; i++) {
                                            out.print("<option value='" + i + "'>" + archives[i] + "</option>");
                                            //out.print(new textdisplay.archive(archives[i]).getIPRAgreement());
                                            //out.print("<a href=\"?archive="+archives[i]+"\">Edit IPR agreement</a>");
                                        }%>
                                </select>
                                <%  for (int i = 0; i < archives.length; i++) {%>
                                <form id="ipr<%out.print(i);%>" class="iprs" action="admin.jsp" method="post">
                                    <textarea name="eula"><% out.print(new textdisplay.Archive(archives[i]).getIPRAgreement());%></textarea>
                                    <input type="hidden" name="name" value="<% out.print(archives[i]);%>"><br />
                                    <input type="hidden" name="selecTab" value="1">
                                    <input type="submit" name="submitted" value="Update <% out.print(archives[i]);%>">
                                </form>
                                <%
                                    }
                                %>
                            </li>
                            <li class="gui-tab-section" id="modifyArchiveAlerts">
                                <h3>Modify Archive Alert</h3>
                                <div class="ui-state-error-text"><span class="ui-icon ui-icon-alert left"></span>
                                    This message is intrusive and should be left blank unless a disruption or important message is needed.
                                </div>
                                <script>
                                    $("#iprs").clone().attr("id","archiveAlert").appendTo($("#modifyArchiveAlerts"));
                                    function escapeTextarea(textareaToEscape){
                                        textareaToEscape.value = escape(textareaToEscape.value);
                                    }
                                </script>
                                <%  for (int i = 0; i < archives.length; i++) {%>
                                <form id="archAlert<%out.print(i);%>" class="archAlert" onsubmit="escapeTextarea(this.getElementsByTagName('textarea')[0]);" action="admin.jsp" method="post">
                                    <textarea id="msg<%out.print(i);%>" name="alert"></textarea>
                                    <script>
                                              var msg<%out.print(i);%> = <% out.print("unescape('" + new textdisplay.Archive(archives[i]).message() + "')");%>;
                                              $("#msg<%out.print(i);%>").val(msg<%out.print(i);%>);
                                    </script>
                                    <input type="hidden" name="name" value="<% out.print(archives[i]);%>"><br />
                                    <input type="hidden" name="selecTab" value="1">
                                    <input type="submit" name="submitted" value="Post <% out.print(archives[i]);%>">
                                </form>
                                <%
                                    }
                                %>
                            </li>
                            <li class="gui-tab-section" id="modifyCityMap">
                                <script>
                                    function mapFilter(){
                                        $("#updateCityMap").find("input[type='checkbox']").not(':checked').remove();
                                    }
                                    $(function(){
                                        $("#updateCityMap").find(".msSelect").on({
                                            click: function(event){
                                                if(event.target!=this)return true;
                                                var checkbox = $(this).find("input[type='checkbox']");
                                                //                        console.log(event.target);
                                                if (!checkbox.prop("checked")){
                                                    checkbox.prop("checked",true);
                                                }
                                                $(this).addClass('activeMap').siblings().removeClass("activeMap");
                                                var city = $(this).attr('data-map');
                                                if (city == "na") {
                                                    //No reliable map data in lookup table
                                                    city = $(this).attr('title'); 
                                                    $(this).attr('data-map',city);
                                                }
                                                $("#cityString").val(city);
                                                var src = [
                                                    "https://maps.googleapis.com/maps/api/staticmap?",
                                                    "center=",city,
                                                    "&markers=icon:http://www.t-pen.org/TPENFRESH/images/quillpin.png|",city,
                                                    "&sensor=false&scale=1&zoom=3&visibility=simplified&maptype=terrain",
                                                    "&size=",$("#cityMap").width(),"x",$("#cityMap").height()
                                                ].join("");
                                                var src2 = [
                                                    "https://maps.googleapis.com/maps/api/staticmap?",
                                                    "center=",city,
                                                    "&sensor=false&scale=1&zoom=10&visibility=simplified&maptype=terrain",
                                                    "&size=",$("#cityMap").width()*.3,"x",$("#cityMap").height()*.9
                                                ].join("");
                                                $("#cityMap").attr("src",src).show();
                                                $("#cityMapZoom img").attr("src",src2).show();
                                            }
                                        });
                                        $("#updateMap").on({
                                            click: function(){
                                                $(".activeMap").attr("data-map",$("#cityString").val()).click()
                                                .children('input[type="checkbox"]').val($("#cityString").val()).prop('checked',true);
                                            }
                                        });
                                        $("#clearMap").on({
                                            click: function(){
                                                $(".activeMap").attr("data-map","").children('input[type="checkbox"]').add("#cityString").val("");
                                            }
                                        });
                                        $("#cityString").on({
                                            keydown: function(event){
                                                if(!event)event=window.event;
                                                if(event.which==13){
                                                    // Enter pressed
                                                    event.preventDefault();
                                                    $("#updateMap").click();
                                                }
                                            }
                                        });
                                    });

                                </script>
                                <h3>Display City Map</h3>
                                <form id="updateCityMap" class="cityMap" action="admin.jsp" method="post" onsubmit="mapFilter();">
                                    <div id="cityMapContain">
                                        <img id="cityMap" src="https://maps.googleapis.com/maps/api/staticmap?center=St.%20Louis&zoom=3&sensor=false&scale=1&size=300x200&maptype=terrain&visibility=simplified&markers=icon:http://www.t-pen.org/TPENFRESH/images/quillpin.png%257St.%20Louis" />
                                        <div id="cityMapZoom">
                                            <img src="https://maps.googleapis.com/maps/api/staticmap?center=St.%20Louis&zoom=10&sensor=false&scale=1&size=100x140&maptype=terrain&visibility=simplified&markers=icon:http://www.t-pen.org/TPENFRESH/images/quillpin.png%257St.%20Louis" />
                                        </div>
                                    </div>
                                    <div class="right" id="mapSearch">
                                        <input id="cityString" class="left" placeholder="test values here" />
                                        <button class="tpenButton ui-button" id="updateMap" type="button"><span class="ui-icon ui-icon-search left"></span>Search</button>
                                        <button class="tpenButton ui-button" id="clearMap" type="button"><span class="ui-icon ui-icon-close left"></span>Remove City Map</button>
                                    </div>
                                    <p class="left">Select a city to view the map. Update the search string to change the associated map. Any checked city will be updated.</p>
                                    <button class="tpenButton left clear-left" id="mapAll" onclick="$('.msSelect').show();return false;" type="button">Show All Cities</button>
                                    <div id="mapCities">
                                        <%
                                            if (request.getParameter("updateCityMap") != null) {
                                                // Process updates to citymap
                                                Map<String, String[]> mapCities = request.getParameterMap();
                                                for (Map.Entry<String, String[]> entry : mapCities.entrySet()) {
                                                    if (entry.getKey().startsWith("city")) {
                                                        CityMap updateCity = new CityMap(entry.getKey().substring(4));
                                                        updateCity.setValue(entry.getValue()[0]);
                                                    }
                                                }
                                            }
                                            String[] cities = Manuscript.getAllCities();
                                            for (int i = 0; i < cities.length; i++) {
                                                CityMap thisCity = new CityMap(cities[i]);
                                                String mapped = thisCity.getValue();
                                                if (mapped.length() < 2) { // New city listing
                                                    mapped = "na";
                                                    out.print("<span class='msSelect newCity left' title='" + cities[i] + "' data-map='" + mapped + "'><input type='checkbox' class='mapCheck' value='" + cities[i] + "' id='city" + i + "' name='city" + cities[i] + "' />" + cities[i] + "</span> ");
                                                } else {
                                                    // no newCity flag
                                                    out.print("<span class='msSelect left' title='" + cities[i] + "' data-map='" + mapped + "'><input type='checkbox' class='mapCheck' value='" + cities[i] + "' id='city" + i + "' name='city" + cities[i] + "' />" + cities[i] + "</span> ");
                                                }
                                        %>          <%
    }
                                        %></div>
                                    <input type="hidden" name="selecTab" value="1">
                                    <input type="submit" name="updateCityMap" value="Save All Map Updates" class="left clear">
                                </form>

                            </li>
                            <li class="gui-tab-section">
                                <h3>Restrict access to a manuscript</h3>
                                Select a manuscript to restrict access and the user who will be in charge of controlling access to it. As an administrator, you will
                                always be able to control access to it as well.
                                <form action="admin.jsp" method="POST">

                                    <input type="hidden" name="restrict" value="true">
                                    <select name="ms" class="combobox">
                                        <%
                                            for (int i = 0; i < cities.length; i++) {
                                                textdisplay.Manuscript[] cityMSS = textdisplay.Manuscript.getManuscriptsByCity(cities[i]);
                                                for (int j = 0; j < cityMSS.length; j++) {
                                                    if (!cityMSS[j].isRestricted()) {
                                                        out.print("<option value=" + cityMSS[j].getID() + ">" + cityMSS[j].getShelfMark() + "</option>");
                                                    }
                                                }
                                            }
                                        %>
                                    </select>
                                    <select name="uid" class="combobox">
                                        <%
                                            for (int i = 0; i < allUsers.length; i++) {
                                                out.print("<option value=" + allUsers[i].getUID() + ">" + allUsers[i].getFname() + " " + allUsers[i].getLname() + " (" + allUsers[i].getUname() + ")" + "</option>");
                                            }

                                        %>
                                    </select>
                                    <input type="submit" name="submitted" value="restrict">
                                </form>

                            </li>
                            <% } //end of hiding Admin items
                                textdisplay.Manuscript[] mss = thisUser.getUserControlledManuscripts();
                                if (mss.length > 0) {%>
                            <li class="ui-widget-content ui-corner-tr ui-corner-bl">
                                <h3>Manuscript Administration</h3>
                                <%if (thisUser.isAdmin()) {%>   <a class="ui-button tpenButton right" id="adminManuscript">Select an unrestricted manuscript to administer</a><%}%>
                                <h6>Restricted Manuscripts</h6>
                                <%
                                        out.print("Click a manuscript to administer access to it or modify the shelfmark<br>");
                                        for (int i = 0; i < mss.length; i++) {
                                            out.print("<a href=\"manuscriptAdmin.jsp?ms=" + mss[i].getID() + "\">" + ESAPI.encoder().decodeFromURL(mss[i].getShelfMark()) + "</a> <a href=\"admin.jsp?unrestrict=true&ms=" + mss[i].getID() + "&submitted=true\">remove access restrictions</a><br>");
                                        }
                                    }
                                %></li>
                        </ul>
                    </div>
                    <!--                end of tab-2, manuscripts-->
                    <%if (thisUser.isAdmin()) { //hiding non-Admin tab%>
                    <div id="tabs-3">
                        <ul id="manageUsers" class="ui-helper-reset"> 
                            <li class="left">
                                <a id="manageUsersBtn" class="tpenButton" href="#" onclick="manageUsers();return false;" style="display:none;">Save Changes</a>
                            </li>               
                            <li class="left">
                                <a id="emailAlert" class="tpenButton" href="#" onclick="overlay(userList);return false;">User Emails</a><br />
                            </li>
                            <li id="manageUserFeedback" class="gui-tab-section" style="display:none;"></li>
                            <%
                                //if this is an administrator, allow them to approve new users
                                if (thisUser.isAdmin()) {
                            %>
                            <li class="gui-tab-section clear-left">
                                <div id="newUserApproval">
                                    <h3>Activations</h3>
                                    <%
                                        for (int i = 0; i < unapprovedUsers.length; i++) {
                                    %><label for="approve<%out.print(i);%>"><input type="checkbox" name="approve<%out.print(i);%>" id="approve<%out.print(i);%>" value="<%out.print(unapprovedUsers[i].getUID());%>" /><%out.print(unapprovedUsers[i].getFname() + " " + unapprovedUsers[i].getLname() + " (" + unapprovedUsers[i].getUname() + ")");%></label>
                                        <%
                                            }
                                        %>
                                </div></li>
                            <li class="gui-tab-section">
                                <div id="denyUsers">
                                    <h3>Deny Requests</h3>
                                    <%
                                        for (int i = 0; i < unapprovedUsers.length; i++) {
                                    %><label for="eliminate<%out.print(i);%>"><input type="checkbox" name="eliminate<%out.print(i);%>" id="eliminate<%out.print(i);%>" value="<%out.print(unapprovedUsers[i].getUID());%>" /><%out.print(unapprovedUsers[i].getFname() + " " + unapprovedUsers[i].getLname() + " (" + unapprovedUsers[i].getUname() + ")");%></label>
                                        <%
                                            }
                                        %>
                                </div>
                            </li>
                            <li class="gui-tab-section">
                                <div id="userDeactivation">
                                    <h3>Deactivate User</h3>
                                    <%
                                        StringBuilder userEmails = new StringBuilder();
                                        for (int i = 0; i < allUsers.length; i++) {
                                            int lastActive = allUsers[i].getMinutesSinceLastActive();
                                            String ago = "Activity unknown";
                                            if (lastActive == -1) {
                                                //never
                                                ago = "This is an inactive user";
                                            } else if (lastActive < 60) {
                                                // minutes
                                                ago = (lastActive == 1) ? "Active 1 minute ago" : "Active " + lastActive + " minutes ago";
                                            } else if (lastActive < 1440) {
                                                // hours
                                                ago = (lastActive < 120) ? "Active in the last couple hours" : "Active " + Math.floor(lastActive / 60) + " hours ago";
                                            } else {
                                                //days
                                                ago = (lastActive < 2880) ? "Active yesterday" : "Active " + Math.floor(lastActive / 1440) + " days ago";
                                            }
                                            userEmails.append(", " + allUsers[i].getUname());
                                    %><label for="deactivate<%out.print(i);%>" title="<%out.print(ago);%>"><input type="checkbox" name="deactivate<%out.print(i);%>" id="deactivate<%out.print(i);%>" data-lastactive="<%out.print(lastActive);%>" value="<%out.print(allUsers[i].getUID());%>" /><%out.print(allUsers[i].getFname() + " " + allUsers[i].getLname() + " (" + allUsers[i].getUname() + ")");%></label>
                                        <%
                                            }
                                        %>
                                    <script>userList = '<%out.print(userEmails.toString().substring(2));%>';</script>
                                </div></li>
                                <%
                                textdisplay.WelcomeMessage welcome = new WelcomeMessage();
                                String wMsg = welcome.getMessagePlain();
                                %>
                                <li class="gui-tab-section" id="welcomeForm">
                                    <h3>Update Welcome Message </h3>
                                    <form id="welcomeMsg" class="ui-corner-all" action="admin.jsp" method="post">
                                        <textarea name="welcome" cols="78" rows="6"><%out.print(wMsg);%></textarea>
                                        <input type="hidden" name="selecTab" value="2">
                                        <input type="submit" name="submitted" style="display:block;" value="Update Welcome">
                                    </form>
                                </li>
                        </ul>
                    </div>
                                        <div id="reportsTab">
                                            <ul id="reports">
                                                <li class="gui-tab-section">
                                                    <h3>User Reports</h3>
                                                    <form action="reports.jsp" method="GET" target="_blank">
                      <select name="u" class="combobox">
                                        <%
                                            for (int i = 0; i < allUsers.length; i++) {
                                                out.print("<option value=" + allUsers[i].getUID() + ">" + allUsers[i].getFname() + " " + allUsers[i].getLname() + " (" + allUsers[i].getUname() + ")" + "</option>");
                                            }

                                        %>
                                    </select>
                                    <input class="tpenButton ui-button" type="submit" />
                                                    </form>

                                                </li>
                                                <li class="gui-tab-section">
                                                    <h3>Active Users and Projects</h3>
                                                    <a class="tpenButton ui-button" target="_blank" href="reports.jsp?active=true">Run Report</a>
                                                </li>
                                                <li class="gui-tab-section">
                                                    <h3>T-PEN Totals</h3>
                                                    <a class="tpenButton ui-button" target="_blank" href="reports.jsp?totals=true">Run Report</a>
                                                </li>
                                            </ul>
                                        </div>
                    <%} // end of isAdmin()
                                    }%>
                    <!--                end of tabs-3, manage users-->
                    <div id="aboutTab">
                        <ul id="about">
                            <li class="gui-tab-section">
                                <h3>T&#8209;PEN</h3>
                                <p>The Transcription for Paleographical and Editorial Notation (T&#8209;PEN) project is coordinated by the <a href="http://www.slu.edu/x27122.xml" target="_blank">Center for Digital Theology</a> at <a href="www.slu.edu" target="_blank">Saint Louis University</a> (SLU) and funded by the <a href="http://www.mellon.org/" target="_blank">Andrew W. Mellon Foundation</a> and the <a title="National Endowment for the Humanities" target="_blank" href="http://www.neh.gov/">NEH</a>. The <a target="_blank" href="ENAP/">Electronic Norman Anonymous Project</a> developed several abilities at the core of this project's functionality.</p>
                                <p>T&#8209;PEN is released under <a href="http://www.opensource.org/licenses/ecl2.php" title="Educational Community License" target="_blank">ECL v.2.0</a> as free and open-source software (<a href="https://github.com/jginther/T-PEN/tree/master/trunk" target="_blank">git</a>), the primary instance of which is maintained by SLU at <a href="www.T&#8209;PEN.org" target="_blank">T&#8209;PEN.org</a>.
                                </p>
                            </li>
                            <li id="contactForm" class="gui-tab-section">
                                <h3>More T&#8209;PEN</h3>
                                <div id='sharing'>
                                    <a id="shareFacebook" class="share" 
                                       href="http://www.facebook.com/pages/The-T-Pen-project/155508371151230"
                                       sharehref="http://www.facebook.com/sharer/sharer.php?u=http%3A%2F%2Fwww.t-pen.org"
                                       title="facebook"
                                       target="_blank">
                                        <img alt="facebook"
                                             src="images/sharing/facebook.png"/>
                                    </a>
                                    <a id="shareGoogle" class="share" 
                                       href="https://plus.google.com/104676239440224157170"
                                       share-href="https://plus.google.com/share?url=http%3A%2F%2Fwww.t-pen.org&hl=en-US"
                                       title="google+"
                                       target="_blank">
                                        <img alt="google+"
                                             src="images/sharing/google+.png"/>
                                    </a>
                                    <a id="shareTwitter" class="share" 
                                       href="https://twitter.com/intent/tweet?text=Well%20done%2C%20%23TPEN"
                                       title="twitter"
                                       target="_blank">
                                        <img alt="twitter"
                                             src="images/sharing/twitter.png"/>
                                    </a>
                                    <a id="shareYoutube" class="share" 
                                       href="http://www.youtube.com/user/tpentool"
                                       title="youtube"
                                       target="_blank">
                                        <img alt="youtube"
                                             src="images/sharing/youtube-128.png"/>
                                    </a>
                                    <a id="shareBlogger" class="share" 
                                       href="http://digital-editor.blogspot.com/"
                                       title="blogger"
                                       target="_blank">
                                        <img alt="blogger"
                                             src="images/sharing/blogger-128.png"/>
                                    </a>
                                    <a id="shareGithub" class="share" 
                                       href="https://github.com/jginther/T-PEN/tree/master/trunk"
                                       title="github"
                                       target="_blank">
                                        <img alt="github"
                                             src="images/sharing/github.png"/>
                                    </a>
                                </div>
                                <div>
<!--                                    <div class="tpenButton contact">Directed Communication</div>
                                    <div class="contactDiv" style="display:block;">
                                        <form id="bugForm" onsubmit="$('#FBextra').change();" method="POST" action="http://165.134.241.72/ScoutSubmit.asp" target="_blank">
                                            <input type="hidden" value="James Ginther" name="ScoutUserName" />
                                            <input type="hidden" value="T-PEN" name="ScoutProject" />
                                            <input type="hidden" value="Use Cases" name="ScoutArea" />
                                            <input type="hidden" value="Thank you. A new case has been submitted. You can close this tab to resume your work." name="ScoutDefaultMessage" />
                                            <input type="hidden" value="We are aware of this problem and are working to fix it. Thank you." name="ScoutMessage" />
                                            <input type="hidden" value="cubap@slu.edu" name="ScoutPersonAssignedTo" />
                                            <input type="hidden" value="1" name="Priority" />
                                            <input id="extraSubmit" type="hidden" value="" name="Extra" />
                                            <input id="FBemail" type="hidden" value="<%out.print(thisUser.getUname());%>" name="Email" />
                                            <input type="hidden" name="FriendlyResponse" value="1" />
                                                    Category is not supported in Scout at this time and will be added to the description.
                                            <select id="FBcategory" name="Category">
                                                <option value="Inquiry">Ask a Question</option>
                                                <option value="Feature">Request a Feature</option>
                                                <option value="Bug">Report a Bug</option>
                                            </select>
                                            <input type="text" value="Brief Description" name="Description" />
                                            <textarea id="FBextra" placeholder="Include any additional information" name="FBExtra"></textarea>
                                            <input type="submit" value="Submit" />
                                        </form>
                                    </div>-->
                                    <h4>Contact Us</h4>
                                    <div>
                                        <%
                                            if (request.getParameter("contactTPEN") != null) {
                                                String msg = "Message was not successfully received.";
                                                if (request.getParameter("contact") != null) {
                                                    msg = request.getParameter("contact");
                                                }
                                                int msgSent = thisUser.contactTeam(msg);
                                                switch (msgSent) {
                                                    case 0:
                                                        out.print("<span class='loud'><span class='ui-icon ui-icon-check left'></span>Message sent</span>");
                                                        break;
                                                    case 1:
                                                        out.print("<span class='ui-state-error-text'><span class='ui-icon ui-icon-alert left'></span>You must log in to send a message</span>");
                                                        break;
                                                    case 2:
                                                        out.print("<span class='ui-state-error-text'><span class='ui-icon ui-icon-close left'></span>Server failed to send your message</span>");
                                                        break;
                                                }
                                            }
                                        %>
                                        <form action="admin.jsp" method="POST" onsubmit="return Message.isValid();">
                                            <script type="text/javascript">
                                                var Message = {
                                                    isValid:    function(){
                                                        var contact = $("#contact");
                                                        var msgLength = contact.val().length
                                                        var maxLength = 10000;
                                                        if (msgLength > maxLength) {
                                                            contact.addClass("ui-state-error-text")
                                                            .change(function(){
                                                                var maxLength = 10000;
                                                                var msgLength = $("#contact").val().length
                                                                if (msgLength < maxLength) contact.removeClass("ui-state-error-text");
                                                            });
                                                            alert ("Please limit your message to "+maxLength+" characters.");
                                                            return false;
                                                        }
                                                        if (msgLength === 0) {
                                                            alert ("No message to send");
                                                            return false;
                                                        }
                                                        return true;
                                                    }
                                                };
                                            </script>
                                            <input type="hidden" value="3" name="selecTab" />
                                            <textarea id="contact" name="contact" placeholder="User information will be included automatically with this message"></textarea>
                                            <input type="submit" name="contactTPEN" value="Send Message" />
                                        </form> </div>
                                </div>
                            </li>
                            <li class="gui-tab-section">
                                <h3>User Agreement</h3>
                                <p><b>Conditions of Use</b></p>
                                <p>As a T&#8209;PEN user, you agree 
                                    to use T&#8209;PEN, its tools and services, for their intended purpose.  
                                    You will not use T&#8209;PEN for illegal purposes.  You will not use 
                                    T&#8209;PEN to obtain digital images or transcription data without permission 
                                    or to void any Intellectual Property Rights (IPR) governing one or more 
                                    of the digital collections to which T&#8209;PEN provides access.  Furthermore, 
                                    you agree not to infringe on the rights of other T&#8209;PEN users through 
                                    your own use of T&#8209;PEN.  You also agree that any action that does 
                                    contravene these conditions of use may result in the suspension and 
                                    even deletion of your T&#8209;PEN account.  </p>
                                <p>You agree to abide by the 
                                    IPR conditions that govern access to, and use of, digital images in 
                                    each individual Digital Repository that have their manuscripts listed 
                                    and displayed in T&#8209;PEN. Those notices are displayed when you request 
                                    access to a manuscript of that repository for the first time. </p>
                                <p><b>Intellectual Property and 
                                        Permissions</b></p>
                                <p>You grant permission to 
                                    Saint Louis University (SLU) to store your transcription data on a SLU 
                                    server.  Even if you elect to keep your work completely private, 
                                    you give permission to SLU to use your work as an index for searching 
                                    the manuscripts that T&#8209;PEN has processed.  Your transcription data 
                                    will never be displayed without your express permission, but instead 
                                    will be used to search and display the image of the line of the manuscript 
                                    that matches the search query.  When search results are displayed, your 
                                    username will be cited as the transcription used in the search, but 
                                    no other personal data you have provided for your T&#8209;PEN account will 
                                    ever be displayed.  Your username is defined as the initial letter 
                                    of your first name and your surname.</p>
                                <p>You grant permission for 
                                    SLU to share your transcription data with the Digital Repository where 
                                    the digital manuscript resides.  The Digital Repository is prohibited 
                                    form using any transcription data for commercial purposes nor can they 
                                    distribute it without obtaining your permission. </p>
                                <p>You grant permission to 
                                    the Andrew W. Mellon Foundation to have access to, keep copies of, and 
                                    distribute your transcription data.  This IPR transfer is necessary 
                                    should SLU, for some unforeseen reason, be unable to provide access 
                                    to your transcription in the future; at which point the Mellon Foundation 
                                    would be have the permissions to provide access to your data stored 
                                    in T&#8209;PEN&#39;s server.  This IPR transfer, however, prohibits the Mellon 
                                    Foundation from using your transcription data for commercial purposes. </p>
                                <p><b>License to Use Transcription 
                                        Data</b></p>
                                <p>SLU grants you an unlimited 
                                    license to use any and all transcription data created under your username 
                                    for non-commercial purposes.  You may export your transcription 
                                    data using T&#8209;PEN&#39;s export functions and disseminate it in any electronic 
                                    or print format.</p>
                                <p>This unlimited license 
                                    cannot be interpreted as a license to gain access to repositories or 
                                    individual manuscript images that are protected by subscription or conditions 
                                    external to T&#8209;PEN functionality.     </p>
                                <p><b>Privacy</b></p>
                                <p>SLU will never share your 
                                    personal information that you provide to T&#8209;PEN without your express 
                                    written permission.  This includes your full name, complete email 
                                    address and list of projects and/or transcriptions.  Users who 
                                    elect to collaborate  with other users on projects agree to share 
                                    their full name and email address with their collaborators.  </p>
                                <p><b>Indemnification</b></p>
                                <p>As a T&#8209;PEN user, you indemnify  
                                    SLU, its affiliates and employees from any liability for damage to your 
                                    computer and/or any information stored therein because of your use of 
                                    T&#8209;PEN as a web-based application.</p>
                                <p>  </p>
                            </li>
                            <li class="gui-tab-section">
                                <h3>Development Team</h3>
                                <dl>
                                    <dt>Dr. Jim Ginther, Principal Investigator</dt>
                                    <dd>Director, Center&nbsp;for&nbsp;Digital&nbsp;Theology, Saint&nbsp;Louis&nbsp;University</dd>
                                    <dt>Dr. Abigail Firey, co-Principal Investigator</dt>
                                    <dd><a href="http://ccl.rch.uky.edu" target="_blank" title="Carolingian Canon Law">CCL</a>&nbsp;Project&nbsp;Director, University&nbsp;of&nbsp;Kentucky</dd>
                                    <dt>Dr. Tomás O’Sullivan, Research Fellow (2010-11)</dt>
                                    <dd>Center&nbsp;for&nbsp;Digital&nbsp;Theology, Saint&nbsp;Louis&nbsp;University</dd>
                                    <dt>Dr. Alison Walker, Research Fellow (2011-12)</dt>
                                    <dd>Center&nbsp;for&nbsp;Digital&nbsp;Theology, Saint&nbsp;Louis&nbsp;University</dd>
                                    <dt>Michael Elliot, Research Assistant</dt>
                                    <dd>University&nbsp;of&nbsp;Toronto</dd>
                                    <dt>Meredith Gaffield, Research Assistant</dt>
                                    <dd>University&nbsp;of&nbsp;Kentucky</dd>
                                    <dt>Jon Deering, Senior Developer</dt>
                                    <dd>Center&nbsp;for&nbsp;Digital&nbsp;Theology, Saint&nbsp;Louis&nbsp;University</dd>
                                    <dt>Patrick Cuba, Web Developer</dt>
                                    <dd>Center&nbsp;for&nbsp;Digital&nbsp;Theology, Saint&nbsp;Louis&nbsp;University</dd>
                                </dl>
                            </li>
                            <li class="gui-tab-section">
                                <h3>Contributors</h3>
                                <h5>Repositories</h5>
                                <dl>
                                    <dt><a target="_blank" href="http://parkerweb.stanford.edu/">Parker Library on the Web</a></dt>
                                    <dt><a target="_blank" href="http://www.e-codices.unifr.ch/">e-codices</a></dt>
                                    <dt><a target="_blank" href="http://www.ceec.uni-koeln.de/">Codices Electronici Ecclesiae Coloniensis</a></dt>
                                    <dt><a target="_blank" href="http://hcl.harvard.edu/libraries/houghton/collections/early.cfm">Harvard Houghton Library</a></dt>
                                    <dt><a target="_blank" href="http://www.sisf-assisi.it/" title="Società internazionale di Studi francescani">SISF - Assisi</a></dt>
                                </dl>
                                <h5>Institutions</h5>
                                <dl>
                                    <dt><a target="_blank" href="http://www-sul.stanford.edu/">Stanford University Libraries</a></dt>
                                </dl>
                            </li>
                            <li class="gui-tab-section">
                                <h3>Site Elements</h3>
                                <h5>Tools</h5>
                                <p>T&#8209;PEN will continue to add tools for transcription as regular feature releases. Please contact us if you would like to see a particular tool integrated with the transcription interface.</p>
                                <h5>Images</h5>
                                <p>Manuscript images displayed on T&#8209;PEN are not the property of T&#8209;PEN, but are linked through agreement from hosting repositories. The User Agreement describes the users' rights to these images.</p>
                                <p>All images used or composited in the design of T&#8209;PEN originated in the public domain. Individuals who wish to use portions of this site's design are encouraged to seek out the original source file to adapt. Using the T&#8209;PEN logo or any of its design elements with the purposes of deceiving, defrauding, defaming, phishing, or otherwise misrepresenting the T&#8209;PEN project is prohibited.</p>
                                <h5>Logo</h5>
                                <p>The T&#8209;PEN logo displayed on each page and the variant on the home page was assembled by committee and is an identifying mark. The tyrannosaurus is used with permission from Mineo Shiraishi at <a href="http://www.dinosaurcentral.com/" target="_blank">Dinosaur Central.com</a>.</p>
                                <h5>Source Code</h5>
                                <p>All code generated by the T&#8209;PEN Development Team is covered by license as described in the User Agreement. This project makes use of several public libraries.</p>
                            </li>
                        </ul>
                    </div>

                </div>
                <!--                close up tabs panels-->
                <a class="returnButton" href="index.jsp">Return to TPEN Homepage</a>
            </div>
        </div>
        <div id="adminMS" class="popover"> <!-- container for managing unrestricted MSs -->
            <div class="callup" id="form"> <!-- add to project -->
                <span id="closePopup" class="right caps">close<a class="right ui-icon ui-icon-closethick" title="Close this window">cancel</a></span>
                <%    //Attach arrays of AllCities and AllRepositories represented on T&#8209;PEN
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
        <div id="overlay" class="ui-widget-overlay">
            <div id="overlayNote">Click the page to return</div>
        </div>
        <textarea id="userEmailList" class="popover"></textarea>
        <script type="text/javascript">
            <%
                    if (mss.length > 0) {%>
                        $("#taskList").append("<p title='Click on the \"Manuscript\" tab'>Update information or restrict access to manuscripts you control (<%out.print(mss.length);%> total).</p>")
            <%}%>
        </script>
    </body>
</html>
