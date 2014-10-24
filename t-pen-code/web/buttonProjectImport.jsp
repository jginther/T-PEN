<%-- 
    Document   : buttonProjectImport
    Created on : May 26, 2011, 11:26:20 AM
    Author     : cubap
--%>

<%@page import="user.Group"%>
<%@page import="textdisplay.Project"%>
<%@page import="textdisplay.TagButton"%>
<%@page import="javax.print.DocFlavor.STRING"%>
<%@page import="org.owasp.esapi.ESAPI"%>
<%@page import="user.User"%>
<%@page contentType="text/html; charset=UTF-8"  %>
<%         
        int UID = 0;
            if (session.getAttribute("UID") == null) {
        %><%@ include file="loginCheck.jsp" %><%
                   } else {
        User thisUser = null;
                UID = Integer.parseInt(session.getAttribute("UID").toString());
                thisUser = new User(UID);
%>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN"
   "http://www.w3.org/TR/html4/loose.dtd">

<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title>Import Buttons from Another Project</title>
        <link rel="stylesheet" href="css/tpen.css" type="text/css" media="screen, projection">
        <link rel="stylesheet" href="css/print.css" type="text/css" media="print">
        <!--[if lt IE 8]><link rel="stylesheet" href="css/ie.css" type="text/css" media="screen, projection"><![endif]-->
        <link type="text/css" href="css/custom-theme/jQuery.css" rel="Stylesheet" />
        <script type="text/javascript" src="https://ajax.googleapis.com/ajax/libs/jquery/1.7.1/jquery.js"></script>
        <script type="text/javascript" src="https://ajax.googleapis.com/ajax/libs/jqueryui/1.8.16/jquery-ui.js"></script> 
        <script src="js/tpen.js" type="text/javascript"></script>
        <style type="text/css">
            #currentTags {width:100%;} /* span bottom of screen */
            #importInstructions,#results,#buttons {font-size: 16px;text-align: center;margin:15px;clear:both;position: relative;}
            #buttons {font-size: .8em;max-height: 60px;overflow: auto;}
            #importInstructions .numbers {height:30px;width:30px;text-align: center;display: inline-block; font-size: 24px;margin:-10px 0; position: relative;bottom: -4px;
            -moz-border-radius: 15px 15px 15px 15px;
            -webkit-border-radius: 15px 15px 15px 15px;
            border-radius: 15px 15px 15px 15px;}
            .activeNumber {box-shadow:3px -3px 15px #69ACC9 inset,-1px 1px 5px gold;}
            .activeInstruction {text-shadow:0 0 5px gold;color:black;}
            .projectSelect {font-size: 1.2em; margin:0;}
            .projectSelectDiv {display: block; margin-bottom: 20px; text-align: center;position:relative;}
            #projectSelectDisclaimer {position:absolute;bottom:-1.3em;right:35%;}
/*            .returnButton {display: inline-block;}*/
            #importButtonsForm {overflow:auto;}
            #results button {}
        </style>
        <script>
            function workFlow(step){
                // indicate current step in the process
                $(".numbers").removeClass("activeNumber ui-state-error").eq(step-1).addClass("activeNumber");
                $(".instruction").removeClass("activeInstruction ui-state-error-text").eq(step-1).addClass("activeInstruction");
                if(step<3) $("#importButtons").addClass("ui-state-disabled");
                switch (step) {
                    case 3:
                        if ($(".projectSelect").eq(1).val()==""){
                            $(".numbers").eq(1).addClass("ui-state-error").next(".instruction").addClass("ui-state-error-text");
                            $("#importButtons").addClass("ui-state-disabled");
                        } else {
                            $("#importButtons").removeClass("ui-state-disabled");
                        }                       
                    case 2: 
                        if ($(".projectSelect").eq(0).val()==""){
                            $(".numbers").eq(0).addClass("ui-state-error").next(".instruction").addClass("ui-state-error-text");
                            $("#importButtons").addClass("ui-state-disabled");
                        }
                        break;
                    default:
                        break;
                }
            }
            $(function(){
                $(".numbers,.instruction").click(function(){
                    workFlow(Math.round(($(this).index()+1)/2));
                });
                $(".projectSelect").eq(0).contents().clone(true).appendTo($(".projectSelect").eq(1));
                $(".projectSelect").change(function(){
                    var selectVals = [$(".projectSelect").eq(0).val(),$(".projectSelect").eq(1).val()];
                    var step = 3;
                    if (selectVals[0] == "") step = 1;
                    else if (selectVals[1] == "") step = 2;
                    workFlow(step);
                });
                $("#importButtons").addClass("ui-state-disabled").click(function(){
                    if ($(this).hasClass("ui-state-disabled")){
                        $(".ui-state-error, .ui-state-error-text").effect("pulsate",250);
                        return false;
                    }
                });
            });
        </script>
    </head>
    <body>
        <div id="wrapper">
            <div id="header"><p align="center" class="tagline">transcription for paleographical and editorial notation</p></div>
            <div id="content">
                <h1><script>document.write(document.title); </script></h1>
                <div id="main">
                    <div id="buttons"></div>
                    <div id="results"></div>
        <%
        int projectID = 0;
        if(request.getParameter("projectID")!=null){
            projectID = Integer.parseInt(request.getParameter("projectID"));
        }
        textdisplay.Project p=new textdisplay.Project(projectID);
        if(request.getParameter("submitted")!=null) {
            int fromProjectID = Integer.parseInt(request.getParameter("fromProject"));
            int toProjectID = Integer.parseInt(request.getParameter("toProject"));
            textdisplay.Project toProject = new Project(toProjectID);
            textdisplay.Project fromProject = new Project(fromProjectID);
            Group thisGroup = new Group(toProject.getGroupID());
            if (thisGroup.isMember(thisUser.getUID())){
                toProject.copyButtonsFromProject(fromProject);
                out.println("<script>");
                out.println("$('#results').addClass('ui-state-active ui-corner-all').html('Buttons imported successfully. Use the links at the bottom of this page to return to your work.');");
                out.println("$('#buttons').append('"+ESAPI.encoder().encodeForJavaScript(TagButton.getAllProjectButtons(toProjectID)) +"');");
                out.println("</script>");
            }else{
                out.print("<script>");
                out.print("$('#results').addClass('ui-state-error ui-corner-all').html('You are not a member of the group to which you are copying buttons.')");
                out.print("</script>");
            }
        }
        %>
        <div id="importInstructions" class="ui-state-active ui-corner-all" title="ALL TAGS will be copied from the first project, REPLACING ALL TAGS in the second project.">
            <span class="ui-state-default numbers activeNumber">1</span>
            <span class="instruction activeInstruction">Select a Project from which to copy tags;</span>
            <span class="ui-state-default numbers">2</span>
            <span class="instruction">Select a Project whose tags will be replaced; and</span>
            <span class="ui-state-default numbers">3</span>
            <span class="instruction">Click "Import Buttons".</span>
        </div>
            <form id="importButtonsForm" action="buttonProjectImport.jsp" method="post" onSubmit="$('#trash').click();"> 
<%
                    textdisplay.Project[] allProjects=thisUser.getUserProjects();
%><div class="projectSelectDiv"><h3>Copy from: </h3><select class="projectSelect" name="fromProject"><%
                    for(int i=0;i<allProjects.length;i++) {
                        out.print("<option value="+allProjects[i].getProjectID()+">"+allProjects[i].getProjectName()+"</option>");
                    }
                    out.print("<option selected='selected' value=''>Select a Project</option>");
                    %></select></div>
 <div class="projectSelectDiv"><h3>Copy to: </h3><select class="projectSelect" name="toProject">
     </select>
<!--                    <span id="projectSelectDisclaimer" class="small restricted">Only the Group Leader can copy buttons into a project.</span>-->
                    <span id="projectSelectDisclaimer" class="small restricted">All buttons will be replaced.</span>
 </div>
<input id="importButtons" class="ui-button tpenButton clear-left right" type="submit" name="submitted" value="Import Buttons" >
<%if (request.getParameter("projectID")!=null)out.print("<input name='projectID' type='hidden' value='"+request.getParameter("projectID")+"'/>");%>
        </form>
                </div>
        <%if (request.getParameter("projectID")!=null){%>
        <a class="returnButton" href="buttons.jsp<%out.print("?projectID="+request.getParameter("projectID"));%>">Return to Button Management</a><%}%>
        <a class="returnButton" href="project.jsp<%if (request.getParameter("projectID")!=null)out.print("?projectID="+request.getParameter("projectID"));%>">Return to Project Management</a>
        <a class="returnButton" href="index.jsp">Return to T&#8209;PEN Home</a>
            </div>
        </div>
    </body>
</html>
<%}%>