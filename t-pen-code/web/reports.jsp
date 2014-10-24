<%-- 
    Document   : reports
    Created on : Aug 30, 2012, 2:12:23 PM
    Author     : cubap
--%>

<%@page import="textdisplay.Transcription"%>
<%@page import="textdisplay.Project"%>
<%@page import="java.util.Arrays"%>
<%@page import="com.sun.org.apache.bcel.internal.generic.AALOAD"%>
<%@page import="user.Group"%>
<%@page import="java.util.List"%>
<%@page import="textdisplay.Manuscript"%>
<%@page import="textdisplay.Folio"%>
<%@page import="user.User"%>
<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title>T-PEN Reports</title>
        <link rel="stylesheet" href="css/tpen.css" type="text/css" media="screen, projection">
        <link rel="stylesheet" href="css/print.css" type="text/css" media="print">
        <!--[if lt IE 8]><link rel="stylesheet" href="css/ie.css" type="text/css" media="screen, projection"><![endif]-->
        <link type="text/css" href="css/custom-theme/jQuery.css" rel="Stylesheet" />
        <script type="text/javascript" src="https://ajax.googleapis.com/ajax/libs/jquery/1.7.1/jquery.js"></script>
        <script type="text/javascript" src="https://ajax.googleapis.com/ajax/libs/jqueryui/1.8.16/jquery-ui.js"></script> 
        <script type="text/javascript" src="js/tpen.js"></script>  
        <script type="text/javascript" src="js/jquery.tablesorter.min.js"></script>  
        <style type="text/css">
            label, .reportSection {
                position: relative;
                float:left;
                clear:left;
                width:auto;
                text-shadow: 0px 1px rgba(255, 255, 255, .4);
            }
            .value {
                font-weight: normal;
            }
            .reportSection {
                position: relative;
                clear:none;
                background-color: rgba(255,255,255,.2);
                border: thin black solid
            }
            .value>img {
                position: fixed;
                left:35%;
                top:5%;
                display:none;
                box-shadow: 3px 3px 15px #000;
                z-index: -1;
                width: 30%;
            }
            .img {
                border-right: rgb(255,228,159) dotted thin;
                position: static;
            }
            .img:hover {
                background-color: rgba(255,228,159,.5)
            }
            .img:hover img {
                display:block;
            }
            .logEntry {
                padding:5px;
            }
            .individualReport {display:none;}
            .individualReport:first-child {display: block;}
            .reportContainer {position: relative;width:400px;}
            .ui-button {
                -webkit-touch-callout: none;
                -webkit-user-select: none;
                -khtml-user-select: none;
                -moz-user-select: none;
                -ms-user-select: none;
                user-select: none;
            }
            tr:nth-child(even) {background: rgba(255,255,255,.3)}
            .constrain {
                max-width:250px;
                overflow: auto;
                white-space: nowrap;
            }
        </style>
    </head>
    <body>
        <%
    int UID = 0;
    boolean isAdmin = false;
    boolean isCompleteReport = false;
    user.User thisUser = null;
    if (session.getAttribute("UID") != null) {
        UID = Integer.parseInt(session.getAttribute("UID").toString());
        thisUser = new user.User(UID);
        isAdmin = thisUser.isAdmin();
    }
    if (request.getParameter("access") != null && isAdmin) {
        // Admin has requested all access
        isCompleteReport = !isCompleteReport;
    }
    if (request.getParameter("r") != null || isCompleteReport){
        // Show relevant repositories
        String [] allRepos = textdisplay.Manuscript.getAllRepositories();
        if (request.getParameter("r") != null && !isCompleteReport){
            //single report
            int rParam = Integer.parseInt(request.getParameter("r"));
            // The repository will be selected by a folio contained within
            // for the sake of brevity and flexibility in spelling.
            textdisplay.Manuscript thisM = new Manuscript(rParam);
            textdisplay.Manuscript [] theseManuscripts = 
                    Manuscript.getManuscriptsByRepository(new Manuscript(rParam).getRepository());
            int numOfManuscripts = theseManuscripts.length;
%>
<div id="manuscriptReport" class="reportSection right">
    <h2>Manuscripts</h2>
        <a id="prevReport" class="left listBegin ui-button ui-state-default">Previous</a>
        <a id="nextReport" class="left listEnd ui-button ui-state-default">Next</a>
        <div class="reportContainer">
<%
            int RepoProjects = 0;
            int RepoUsers = 0;
            int RepoFoliosTranscribed = 0;
            int RepoLinesTranscribed = 0;
            // cycle through manuscripts
            for (int i = 0; i < numOfManuscripts;i++){
                textdisplay.Manuscript thisManuscript = theseManuscripts[i];
                String mShelfmark = thisManuscript.getShelfMark();
                user.User mController = thisManuscript.getControllingUser();
                user.User [] authorizedUsers = thisManuscript.getAuthorizedUsers();
                // Individual manuscript summaries
%>            
        <div class="individualReport">
        <label><%out.print(i+1+" of "+numOfManuscripts);%></label>
        <label>Shelfmark: <span class="value"><%out.print(mShelfmark);%></span></label>
        <%
        if (mController != null) {
            %>
        <label>Controller: <a href="reports.jsp?u=<%out.print(mController.getUID());%>"class="value"><%out.print(mController.getFname()+" "+mController.getLname()+" ("+mController.getUname()+")");%></a></label>
<%      if (isAdmin) {%>
                <label>Authorized&nbsp;Users:<span class="value">
                        <ol>
                            <%
                            for (int j=0;j<authorizedUsers.length;j++) {
%>
        <li><a href="reports.jsp?u=<%out.print(authorizedUsers[j].getUID());%>"class="value"><%out.print(authorizedUsers[j].getFname()+" "+authorizedUsers[j].getLname()+" ("+authorizedUsers[j].getUname()+")");%></a></li>
                       <%}%>
                        </ol>
                    </span></label>
        <%}
        }%>
        </div>
<%
            }
%>
        </div>
</div>
<%
    if (isAdmin) {
%>
<div id="projectsReport" class="reportSection">
    <h2>Projects</h2>
        <table> 
            <tr>
                <th>Name</th>
                <th>Leader</th>
                <th>Folio Count</th>
                <th>Work Completed</th>
            </tr>
<%
    }
    textdisplay.Project [] allProjects = textdisplay.Project.getAllProjects();
    textdisplay.Project thisProject;
    int numOfProjs = allProjects.length;
    for (int i=0; i<numOfProjs;i++) {            
        thisProject = allProjects[i];
        int firstPage = thisProject.firstPage();
        boolean isThisRepo = new Manuscript(firstPage).getRepository().compareTo(new Manuscript(rParam).getRepository()) == 0;
        if (firstPage > 0 && isThisRepo) {
            RepoLinesTranscribed += thisProject.getNumberOfTranscribedLines();
            RepoProjects++;
            RepoFoliosTranscribed += thisProject.getFolios().length;                   
            if (isAdmin) {
                out.println("<tr>");
                user.User[] pLeader = new Group(thisProject.getGroupID()).getLeader();
                StringBuilder projectLeader = new StringBuilder();
                for (int j=0;j<pLeader.length;j++) {
                    if (j>0) projectLeader.append(", ");
                    projectLeader.append(pLeader[j].getFname()+" "+pLeader[j].getLname());
                }
    //                RepoProjects.append(thisProject.getProjectName()+"("+thisProject.getProjectID()+")");
                out.println("<td>"+thisProject.getProjectName()+"</td>");
                out.println("<td>"+projectLeader.toString()+"</td>");
                out.println("<td>"+thisProject.getFolios().length+"</td>");
                out.println("<td>"+thisProject.getProgress()+"</td>");
                out.println("</tr>");
            }
        } else {
            continue;
        }
    }
    if (isAdmin) {
%>
        </table>    
</div>
<%
    }
    %>
    <div id="repositorySummary" class="reportSection">
        <label>Total Projects from Repository: <span class="value"><%out.print(RepoProjects);%></span></label>
        <label>Total Folios in Projects: <span class="value"><%out.print(RepoFoliosTranscribed);%></span></label>
        <label>Total Lines Transcribed: <span class="value"><%out.print(RepoLinesTranscribed);%></span></label>
</div>
<%
        } else {
            //complete report
            if (isAdmin) {
                out.print("Complete Reports are not available yet.");
            } else {
                // non-Admin attempt to access complete reports
                String errorMessage = "This report is limited to administrators.";
            %><%@include file="WEB-INF/includes/errorBang.jspf" %><%
                return;
            }
        }
    }
    if (request.getParameter("u") != null || isCompleteReport){
        // Show relevant users
        user.User [] activeUsers = user.User.getAllActiveUsers();
        if (request.getParameter("u") != null && !isCompleteReport) {
            //single report
            int thisUID = Integer.parseInt(request.getParameter("u"));
            user.User theUser = new User(thisUID);
            boolean isLoggedUser = false;
            if (thisUser != null) isLoggedUser = thisUser.getUID() == thisUID;
            if (isAdmin || isLoggedUser) {
                textdisplay.Project [] userProjects = theUser.getUserProjects();
                int numOfProjs = userProjects.length;
%>
<div id="userSummary" class="reportSection">
    <h2>User</h2>
    <label>Name:<span class="value"><%out.print(theUser.getFname() + " " + theUser.getLname());%></span></label>
    <label>E&#8209;mail:<span class="value"><%out.print(theUser.getUname());%></span></label>
    <label>Last&nbsp;Active:<span class="value"><%out.print(theUser.getLastActiveDate().toString());%></span></label>
    <label>Total Projects: <span class="value"><%out.print(numOfProjs);%></span></label>
    <label>Lines of Transcription: <span class="value"><%out.print(theUser.getUserTranscriptionCount());%></span></label>
</div>
<div id="userProjects" class="reportSection">
    <h2>User Projects</h2>
        <a id="prevReport" class="left listBegin ui-button ui-state-default">Previous</a>
        <a id="nextReport" class="left listEnd ui-button ui-state-default">Next</a>
        <div class="reportContainer">
<%
            for (int i=0;i<numOfProjs;i++) {
                String lastLogEntry = userProjects[i].getProjectLog(1);
                if (lastLogEntry.length() == 0) lastLogEntry = "none recorded";
                int lastFolioID = userProjects[i].getLastModifiedFolio();
                textdisplay.Folio lastFolio = new Folio(lastFolioID);
                String lastFolioImg = "";
                String lastFolioName = "";
                try {
                    lastFolioName = lastFolio.getImageName();
                    lastFolioImg = lastFolio.getImageURLResize(400);
                } catch (Error e) {
                }
                StringBuilder theseLeaders = new StringBuilder();
                user.User[] leaders = new user.Group(userProjects[i].getGroupID()).getLeader();
                for(int j=0;j<leaders.length;j++){
                    if(j>0) theseLeaders.append(", ");
                    theseLeaders.append(leaders[j].getFname()).append(" ").append(leaders[j].getLname());
                }
%>
    <div class="individualReport">
        <label><%out.print(i+1+" of "+numOfProjs);%></label>
        <label>Title:<span class="value"><%out.print(userProjects[i].getProjectName());%></span></label>
        <label>Group&nbsp;Leader: <span class="value"><%out.print(theseLeaders.toString());%></span></label>
        <label class="img">Last&nbsp;Modified&nbsp;Folio: <span class="value"><%out.print(lastFolioName);%>
                <img alt="thumb" src="<%out.print(lastFolioImg);%>&quality=30" /></span></label>
        <label>Last&nbsp;Log&nbsp;Entry: <span class="value"><%out.print(lastLogEntry);%></span></label>
    </div>
<%                
            }
%>
        </div>
</div>

<%
            } else {
                // non-User attempt to access complete reports
                String errorMessage = "Only an administrator or the user can view this report.";
            %><%@include file="WEB-INF/includes/errorBang.jspf" %><%
                return;
            }
        } else {
            //complete report
            if (isAdmin) {
                out.print("Complete Reports are not available yet.");
            } else {
                // non-Admin attempt to access complete reports
                String errorMessage = "This report is limited to administrators.";
            %><%@include file="WEB-INF/includes/errorBang.jspf" %><%
                return;
            }
        }
    }
    if (request.getParameter("totals") != null){
        // Summary of totals
        textdisplay.Project [] allProjects = Project.getAllProjects();
        int projectCount = allProjects.length;
        int userCount = user.User.getAllActiveUsers().length;
        int transcriptionCount = Transcription.getNumberOfTranscribedLines();
        int manuscriptCount = Manuscript.getTotalManuscriptCount();
        %>
<div id="totalSummary" class="reportSection">
    <h2>Summary</h2>
    <p>T-PEN totals, including all active and dormant projects and users.</p>
    <label>Total&nbsp;Manuscripts: <span class="value"><%out.print(manuscriptCount);%></span></label>
    <label>Total&nbsp;Projects: <span class="value"><%out.print(projectCount);%></span></label>
    <label>Total&nbsp;Users: <span class="value"><%out.print(userCount);%></span></label>
    <label>Total&nbsp;Lines: <span class="value"><%out.print(transcriptionCount);%></span></label>
</div>        
<%
    }
    if (request.getParameter("p") != null || isCompleteReport){
        // Show relevant project
        
        if (request.getParameter("p") != null && !isCompleteReport) {
            //single report
            int thisProjectID = Integer.parseInt(request.getParameter("p"));
            out.print("projects");
        } else {
            //complete report
            if (isAdmin) {
                out.print("Complete Reports are not available yet.");
            } else {
                // non-Admin attempt to access complete reports
                String errorMessage = "This report is limited to administrators.";
            %><%@include file="WEB-INF/includes/errorBang.jspf" %><%
                return;
            }
        }
    }
    if (request.getParameter("active") != null) {
        // Show report for active projects and active users
        if (isAdmin) {
            textdisplay.Project [] activeProjects = Project.getAllActiveProjects();
            user.User [] activeUsers = User.getRecentUsers();
            int cntActiveProjects = activeProjects.length;
            int cntActiveUsers = activeUsers.length;
            %>
<div id="recentSummary" class="reportSection">
    <h2>Summary</h2>
    <p>Count of unique contributions in the last 2 months.</p>
    <label>Recent&nbsp;Projects: <span class="value"><%out.print(cntActiveProjects);%></span></label>
    <label>Recent&nbsp;Users: <span class="value"><%out.print(cntActiveUsers);%></span></label>
</div>
<div id="recentProjects" class="reportSection">
    <h2>Recent Projects</h2>
        <table style="width:auto;position: relative;">
            <col width="30%"/>
            <col width="20%"/>
            <col width="20%"/>
            <col width="30%"/>
            <thead>
                <tr>
                    <th>Title</th>
                    <th>Group&nbsp;Leader</th>
                    <th>Last&nbsp;Modified&nbsp;Folio</th>
                    <th>Latest&nbsp;Log&nbsp;Entry</th>
                </tr>
            </thead>
<%
            for (int i=0;i<cntActiveProjects;i++) {
                String lastLogEntry = activeProjects[i].getProjectLog(1);
                if (lastLogEntry.length() == 0) lastLogEntry = "none recorded";
                int lastFolioID = activeProjects[i].getLastModifiedFolio();
                textdisplay.Folio lastFolio = new Folio(lastFolioID);
                String lastFolioImg = "";
                String lastFolioName = "";
                try {
                    lastFolioName = lastFolio.getImageName();
                    lastFolioImg = lastFolio.getImageURLResize(200);
                } catch (Error e) {
                }
                StringBuilder theseLeaders = new StringBuilder();
                user.User[] leaders = new user.Group(activeProjects[i].getGroupID()).getLeader();
                for(int j=0;j<leaders.length;j++){
                    if(j>0) theseLeaders.append(", ");
                    theseLeaders.append(leaders[j].getFname()).append(" ").append(leaders[j].getLname());
                }
%>
    <tr>
        <td><div class="constrain"><%out.print(activeProjects[i].getProjectName());%></div></td>
        <td><%out.print(theseLeaders.toString());%></td>
        <td class="img"><div class="constrain"><%out.print(lastFolioName);%></div>
            <span class="value"><img alt="thumb" src="<%out.print(lastFolioImg);%>" /></span></td>
        <td><%out.print(lastLogEntry);%></td>
    </tr>
<%                
            }
%>
        </table>
</div>
<div id="recentUsers" class="reportSection">
    <h2>Recent Users</h2>
    <table style="width:auto;">
        <thead>
            <tr>
                <th>Name</th>
                <th>E&#8209;mail</th>
                <th>Last&nbsp;Active</th>
                <th>Total&nbsp;Projects</th>
                <th>Lines&nbsp;of&nbsp;Transcription</th>
            </tr>
        </thead>
    <%
            for (int r=0;r<cntActiveUsers;r++) {
                user.User theUser = activeUsers[r];
                textdisplay.Project [] userProjects = theUser.getUserProjects();
                int numOfProjs = userProjects.length;
%>
<tr> 
    <td><a href="reports.jsp?u=<%out.print(theUser.getUID()+"\">"+theUser.getFname() + "&nbsp;" + theUser.getLname());%></a></td>
    <td><%out.print(theUser.getUname());%></td>
    <td><%out.print(theUser.getLastActiveDate().toString());%></td>
    <td><%out.print(numOfProjs);%></label>
    <td><%out.print(theUser.getUserTranscriptionCount());%></td>
</tr>

<%}%>
    </table>
</div>
<%
        } else {
            // non-Admin attempt to access complete reports
            String errorMessage = "This report is limited to administrators.";
            %><%@include file="WEB-INF/includes/errorBang.jspf" %><%
            return;
        }
    }
        %>
        <script type="text/javascript">
            $(function(){
                $('#nextReport').click(function(){
                    var proj = $('.individualReport').filter(':visible');
                    if (proj == null) proj = $('.individualReport').eq(0);
                    if (proj.next('.individualReport') != null) {
                        proj.next().show('fade')
                        .siblings('.individualReport').hide();
                    }
                });
                $('#prevReport').click(function(){
                    var proj = $('.individualReport').filter(':visible');
                    if (proj == null) proj = $('.individualReport').eq(0);
                    if (proj.prev('.individualReport') != null) {
                        proj.prev().show('fade')
                        .siblings('.individualReport').hide();
                    }
                });
                $('#recentUsers').find('table').tablesorter({
                    sortList: [[3,1],[4,1]]
                });
            });
        </script>
    </body>
</html>
