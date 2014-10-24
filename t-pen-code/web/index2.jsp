<%--
    Document   : index
    Created on : Oct 26, 2010, 12:08:31 PM
    Author     : cubap,jdeerin1
--%>
<%@page import = "java.sql.SQLException"%>
<%@page import = "textdisplay.Project"%>
<%@page import = "textdisplay.Archive" %>
<%@page import = "textdisplay.Folio" %>
<%@page import = "textdisplay.Manuscript" %>
<%@page import = "org.owasp.esapi.ESAPI" %>

<%@page contentType="text/html; charset=UTF-8"  pageEncoding="UTF-8"%>

<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN"
    "http://www.w3.org/TR/html4/loose.dtd">
<%
    user.User thisUser = null;

    if (session.getAttribute("UID") != null) {
        thisUser = new user.User(Integer.parseInt(session.getAttribute("UID").toString()));
        if (request.getParameter("accept") != null) {
            int UID = thisUser.getUID();
            thisUser.acceptUserAgreement();
        }
    }%>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <LINK REL="SHORTCUT ICON" HREF="logo.ico">
        <title>TPEN <%out.println("Version " + Folio.getRbTok("VERSION"));%></title>
        <link rel="stylesheet" href="css/tpen.css" type="text/css" media="screen, projection">
        <link rel="stylesheet" href="css/print.css" type="text/css" media="print">
        <!--[if lt IE 8]><link rel="stylesheet" href="css/ie.css" type="text/css" media="screen, projection"><![endif]-->
        <link type="text/css" href="css/custom-theme/jQuery.css" rel="stylesheet" />
        <link href='http://fonts.googleapis.com/css?family=Stardos+Stencil:700' rel='stylesheet' type='text/css'>
        <script type="text/javascript" src="https://ajax.googleapis.com/ajax/libs/jquery/1.7.1/jquery.js"></script>
        <script type="text/javascript" src="https://ajax.googleapis.com/ajax/libs/jqueryui/1.8.16/jquery-ui.js"></script> 
        <script src="js/manuscriptFilters.js" type="text/javascript"></script>
        <script src="js/tpen.js" type="text/javascript"></script>
        <style>
            #footer { width: 1010px; position: fixed;left:0;right:0; bottom:0;margin: 0 auto;}
            #foot { background: url(images/footer.png) top left no-repeat;position:relative;padding:50px 125px; }
            #button { text-decoration: none; }
            #cities, #repositories {min-width: 40px;}
            #pagename {text-decoration: none;font-weight: bold;}
            #yourPage, ul li a {text-decoration: none;}
            ul li {list-style:outside none;padding:3px;width:100%;}
            ul li a {padding:2px;}
            /*                #listings {padding-top: 15px;}*/
            #userKnown2, #userUnknown2,#blogEntries,#browseListings {margin: 8px 0; overflow: hidden; padding: 5px;}
            #userUnknown2 a,#userUnknown2 input[type="submit"] {margin: 3px 0; width: 150px;}
            #forgetForm {margin:-4px 5px 2px;background: url(images/linen.png);padding:6px 2px 2px;display:none;overflow: hidden;z-index: 1; border: 1px solid #A68329;
                         -moz-box-shadow: -1px -1px 2px black;
                         -webkit-box-shadow: -1px -1px 2px black; 
                         box-shadow:-1px -1px 2px black;}
            #forgetFormBtn {position: relative;z-index: 2;cursor: pointer;margin: 0 0 5px 0;}
            .section {width:50%; padding: 20px; float: left;}
            #beta,#titleBeta {
                font-family: "Stardos Stencil",monospace;
                color: rgb(142,11,0);
                -ms-transform:rotate(-20deg);
                -moz-transform:rotate(-20deg);
                -webkit-transform:rotate(-14deg);
                transform: rotate(-20deg);
                text-shadow: -1px 1px 2px rgba(0, 0, 0, 0.5);
                display: inline-block;
                font-variant: normal;
            }
            #beta {font-size: 6em;
                   color: rgba(142,11,0,0.5);
                   text-shadow: 1px 1px 1px rgba(255, 255, 255, 0.3),0px 0px 9px rgba(0, 0, 0, 0.5);
                   position: fixed;
                   left:0;
                   top:0;
            }
            #overlay {width:600%;height:600%;z-index: 1;}
            #acceptAgreement,#betaNews {display: none;z-index: 6;width:70%;max-width: 675px;min-width: 200px; position: fixed;margin: 0 auto;left:0;right:0;min-height: 200px;height:70%;top:15%;}
            #betaNews,#browseMSPanel,#userAgreementPanel {overflow: hidden;
                             -moz-box-shadow: 0px 0px 100px black;
                             -webkit-box-shadow: 0px 0px 100px black; 
                             box-shadow:0px 0px 100px black;
            }
            #browseMSPanel {height: 100%;}
            #betaNewsText,#userAgreementPanel {overflow: auto;width: 100%;height:95%;position: relative;}
            h1>a{font-family: sans-serif;font-size: 14px;text-shadow:none; text-decoration: none;font-variant: normal;position: relative;display: inline-block;}
            h1>a:hover{color: black;text-decoration: underline;}
            #agreement {max-height: 300px;overflow: auto; background: whitesmoke; border: inset gray thin;padding: 10px;}
            #browseMS {position: fixed; top:25%; left:15%; width:70%; display: none;height:60%;z-index: 6;}
            #browseListings,#blogEntries {width:49%;}
            #count {display: none;}
            #toc {text-shadow:0px -1px 0px #DDEAFF; margin-right: 3px;}
            #toc a{padding: 3px;margin:-3px 0;}
            #toc a:hover {background:whitesmoke;
                          -moz-box-shadow:1px -1px 6px silver inset;
                          -webkit-box-shadow:1px -1px 6px silver inset;
                          box-shadow:1px -1px 6px silver inset;
            }
            .blogEntry {width:100%;float: left;position: relative; margin-bottom: 1em;}
            .blogTitle {position: relative; line-height: 1; margin:0;}
            .blogTitle a {text-decoration: none;color: #A64129;}
            .blogTitle a:hover {text-decoration: underline;}
            .blogSnippet {font-size: 90%;clear:left;position: relative;}
            .blogDate {float: right;}
        </style>
        <script>
            $(function() {
                // Set #listings to the height of the cover
                //        $('#listings').height(function(){
                //            var ListingsOffset = $("#listings").offset().top - $("#content").offset().top;
                //            return $("#content").height()-ListingsOffset-15;
                //        });
//                $("#betaNewsBtn").click(function(){
//                    $("#betaNews,#overlay").show('fade',500);
//                });
                // Allow #yourPage to fill the unused space to the left of the list of user projects
                $("#yourPage span:first-child").height(($("ul .ui-state-default").height()+8)*$("ul .ui-state-default").size());
                if($("#yourPage span:first-child").height() > 380)$("#yourPage span:first-child").height(380);
                $("#browseListings").click(function(){
                    $("#browseMS").css({
                        "top"   : Page.height() * .15,
                        "left"  : Page.width() * .15,
                        "width" : Page.width() * .7,
                        "height": Page.height() * .75
                    });
                $("#listings").height($("#browseMS").height()-109);
                    $("#browseMS,#overlay").show('fade',500);
                });
                $("#closePopup").click(function(){
                    $("#browseMS,#overlay").hide('fade',500,function(){
                    });
                });
                $("#toc a").click(function(event){
                    event.preventDefault();
                    var aTarget = $("[name='"+$(this).attr("href").substr(1)+"']");
                    $("#betaNewsText").scrollTop(0);
                    var scrollTo = aTarget.position().top - 18;
                    $("#betaNewsText").scrollTop(scrollTo);
                });
                blogPosts("http://digital-editor.blogspot.com/feeds/posts/default");
                var a=(new Date).getTime();
                if(a>=1326891600000 && a<=1326934800000 || window.location.hash=="#stopsopa"){
                    $("#landing").addClass('blackout');
                }
            });
            function blogPosts(url){
                //http://digital-editor.blogspot.com/feeds/posts/default
                $.ajax({
                    url:document.location.protocol + '//ajax.googleapis.com/ajax/services/feed/load?v=1.0&num=10&callback=?&q=' + encodeURIComponent(url),
                    dataType: 'json',
                    success: function(data){
                        var posts = data.responseData.feed;
                        console.log(posts);
                        var addResult = new Array();
                        // sticky for beta
                            addResult.push("<div class='blogEntry boxsizingBorder'><h4 class='blogTitle'><a href='#' id='betaNewsBtn' onclick='$(\"#betaNews,#overlay\").show(\"fade\",500);'>T&#8209;PEN has hit <span id='titleBeta'>&beta;</span>!</a></h4><div class='blogSnippet'>Read more about this project. Learn about the features and benefits of this new digital tool.</div></div>");
                        for (var i=0;i<3;i++){
                            addResult.push("<div class='blogEntry boxsizingBorder'><div class='blogDate small'>",
                                posts.entries[i].publishedDate.substr(0, 16),
                                "</div>",
                                "<h4 class='blogTitle'><a href='",posts.entries[i].link,"' target='_blank'>",
                                    posts.entries[i].title,
                                "</a></h4>",
                                "<div class='blogSnippet'>",
                                    posts.entries[i].contentSnippet,
                                "</div></div>");
                        }
                        $("#blogEntries").append(addResult.join(""));
                    }
                });
            }
        </script>
    </head>
    <body id="landing">
        <div id="wrapper">
            <div id="header">
                <div align="center" class="tagline">transcription for paleographical and editorial notation</div>
                <div class="login ui-widget-content ui-corner-bl">
                    <%out.println("Version " + Folio.getRbTok("VERSION") + " Build " + Folio.getRbTok("BUILD") + " (Built " + Folio.getRbTok("DATE") + ")");%> <br />
                    <span id="userKnown">
                        <%if (session.getAttribute("UID") != null) {%>
                        Welcome back,<span style="text-transform: capitalize;font-weight: bold;"><%
                            String usernm = thisUser.getFname() + " " + thisUser.getLname();
                            out.print(usernm + ". ");
                            %></span>(<a href="login.jsp">Change User</a> or <a href="login.jsp" onclick="logout();return false;">Logout</a>)<br>
                    <%
                    thisUser.updateLastActive();
                    User [] currentUsers = User.getLoggedInUsers();
                    int numberTranscribing = currentUsers.length;
                    String userCount = (numberTranscribing==1) ? "is 1 user" : "are "+numberTranscribing+" users";
                    StringBuilder usersTranscribing = new StringBuilder();
                    for (int i = 0 ; i<numberTranscribing;i++){
                        usersTranscribing.append(currentUsers[i].getFname().substring(0,1)+" "+currentUsers[i].getLname()+" | ");
                    }
                    if(numberTranscribing==0)usersTranscribing.append("No one is transcribing at the moment...   ");
                    int sbLength = usersTranscribing.length();
                    %>
                        There <%out.print("<span title='"+usersTranscribing.toString().substring(0,sbLength-3) +"' id=userList>"+userCount+"</span>");%> transcribing right now
                    </span>
                    <%} else {%>
                    <span id="userUnknown">
                        Please login below or <a href="login.jsp">register</a>.<br>
                    </span>
                    <%}%>
                    <a class="right" href="admin.jsp"><span class="ui-icon ui-icon-gear left"></span>Account Management</a>
                </div>
            </div>
            <div id="beta">BETA</div>
            <div id="content">
                <script>
                    function maintenanceDate(){
                        var today = new Date();
                        while (today.getDay() !== 2){
                            today.setDate(today.getDate()+1);
                        }
                        return(today.toLocaleDateString());
                    }
                </script>
                <div class="loud"><span class="ui-icon ui-icon-info left"></span>Scheduled Maintenance: <script>document.write(maintenanceDate());</script> from 9am - 10am CST</div>
<!--                <h1>T&#8209;PEN has hit <span id="titleBeta">&beta;</span>! <a href="#" id="betaNewsBtn" class=""><span class="ui-icon ui-icon-comment right"></span>Learn More</a></h1>-->
                <%if (thisUser != null) {%>
                <div id="userKnown2" class="ui-widget-content ui-corner-all">
                    <%
                        if (thisUser.hasAcceptedUserAgreement()) {
                    %>
                    <div class='left' style='width:50%;'>
                        <span class='caps'>Recent Page</span>
                        <%
                            if (thisUser.getAnyLastModifiedFolio() != "-1") {
                                String lastFolio[] = thisUser.getAnyLastModifiedFolio().split(",");
                                String lastProject = (Integer.parseInt(lastFolio[1]) > 0) ? "&projectID=" + lastFolio[1] : "";
                                textdisplay.Folio thisFolio = new textdisplay.Folio(Integer.parseInt(lastFolio[0]));
                                out.println("<a id=\"yourPage\" href=\"transcription.jsp?p=" + lastFolio[0] + lastProject + "\"><span class=\"ui-corner-all\" style=\"width:320px;min-height:100px;text-decoration:none;background:-10px -70px url('" + thisFolio.getImageURLResize(600) + "'); border:thin solid; display:block;\">");
                                String projectTitle = "This work is not part of a project";
                                //find the last project title
                                if (lastProject.length() > 1) {
                                    Project lastProj = new Project(Integer.parseInt(lastFolio[1]));
                                    projectTitle = "Working on project: " + lastProj.getProjectName();
                                    }%>
                        <span title="<%out.print(projectTitle);%>" style='position:relative;top:-14px; left:15px; z-index:5;'>
                            <img src='images/ribbon.png' alt="bookmark" />
                        </span>
                        <%
                                //find the full name of the recent manuscript
                                textdisplay.Manuscript ms = new textdisplay.Manuscript(thisFolio.getFolioNumber());
                                String recentPage = ESAPI.encoder().decodeFromURL(ms.getShelfMark() + " " + thisFolio.getPageName());
                                out.println("</span><span id='pagename'>" + recentPage + "</span></a>");
                            } else {
                                out.println("<a href=\"project.jsp\"><img src=\"images/notfound.jpg\" /></span></a>");
                                    }%>
                    </div>
                    <span class='left' style='width:50%;max-height: 400px;overflow: auto;'>
                        <span class='caps'>Current Projects:</span>
                        <%
                            try {
                                textdisplay.Project[] userProjects = thisUser.getUserProjects();
                                out.print("<ul>");
                                if (userProjects.length > 0) {
                                    int projectID = 0;
                                    String projectTitle;
                                    for (int i = 0; i < userProjects.length; i++) {
                                        projectID = userProjects[i].getProjectID();
                                        projectTitle = userProjects[i].getProjectName();
                                        out.print("<li><a title=\"" + projectTitle + "\" class=\"tpenButton projectTitle\" href=\"project.jsp?projectID=" + projectID + "\"><strong>" + projectTitle + "</strong></a></li>");
                                    }
                                } else {
                                    out.print("Getting Started:<br/><iframe src='http://www.youtube.com/embed/KZWIlzD9H_o' allowfullscreen></iframe>");
                                    out.print("</ul>");
                                }
                            } catch (SQLException err) {
                                out.print("<p class=ui-state-error-text>Error retreiving list of projects.</p>");
                            }%>
                    </span>
                </div>
                <%} else {%>
                <script>
                    $(function(){
                        $("#wrapper").append("<div class='ui-widget-overlay' id='overlay' style='display:none;'></div>");
                        $("#acceptAgreement,#overlay").show('fade',500);
                    });
                </script> 
                <%                    }
                } else {%>
                <div class="ui-widget-content ui-corner-all" id="userUnknown2">
                    <div class="left inline clear boxsizingBorder" style="width:100%;"> 
                        <form id="loginLanding" action="login.jsp" method="POST" >
                            <fieldset>
                                <legend>Login Here:</legend>
                                <label for="uname">Email</label><input class="text" type="text" name="uname"/><br/>
                                <label for="password">Password</label><input  class="text" type="password" name="password"/><br/>
                                <input type="hidden" name="ref" value="<%out.print(session.getAttribute("ref"));%>"/>
                                <input class="right tpenButton" type="submit" title="Log In" value="Log In" />
                            </fieldset>
                        </form>
                    </div>
                    <div class="section boxsizingBorder">
                        <p>The Transcription for Paleographical and Editorial Notation (T&#8209;PEN) project is coordinated by the <a href="http://www.slu.edu/x27122.xml" target="_blank">Center for Digital Theology</a> at <a href="www.slu.edu" target="_blank">Saint Louis University</a> (SLU) and funded by the <a href="http://www.mellon.org/" target="_blank">Andrew W. Mellon Foundation</a> and the <a title="National Endowment for the Humanities" target="_blank" href="http://www.neh.gov/">NEH</a>. The <a target="_blank" href="ENAP/">Electronic Norman Anonymous Project</a> developed several abilities at the core of this project's functionality.</p>
                        <p>T&#8209;PEN will be released under <a href="http://www.opensource.org/licenses/ecl2.php" title="Educational Community License" target="_blank">ECL v.2.0</a> as free and open-source software, the primary instance of which will be maintained by SLU at <a href="www.T&#8209;PEN.org" target="_blank">T&#8209;PEN.org</a>.
                        </p>
                    </div>
                    <div class="section boxsizingBorder">
                        <h6 class="loud caps">Register as a New User</h6>
                        <span>You are not currently logged in. Once you sign up you will be able to design personalized projects.</span>
                        <a class="right ui-button tpenButton" href="login.jsp">Request an account<span class="ui-icon ui-icon-mail-closed right"></span></a>
                        <div id="resetPassword">
                            <h6 id="forgetFormBtn" class="loud caps clear-right">Reset your Password<span class="left ui-icon ui-icon-arrowstop-1-s"></span></h6>
                            <form id="forgetForm" action="admin.jsp" method="POST" class="ui-corner-all">
                                <span>Enter the email address associated with your account to have your password reset.</span>
                                <input id="email" type="text" class="text" style="width:220px;" placeholder="Forgot your password?" name="email">
                                <input class="right tpenButton" type="submit" name="emailSubmitted" value="Reset Password"/>
                            </form>
                        </div>
                    </div>
                </div>
                <%}%>
                <div id="blogEntries" class="ui-widget-content ui-corner-all boxsizingBorder right">
                    <h2>T&#8209;PEN Updates</h2>
                </div>
                <div id="spacer" class="right boxsizingBorder" style="width:2%;height:100px;"></div>
                <div>
                    <span id="browseListings" class="tpenButton large boxsizingBorder right"><span class="ui-icon ui-icon-extlink left"></span>Browse Available Manuscripts</span>
                </div>
            </div> 
            <div class='ui-widget-overlay' id='overlay' style='display:none;'></div>
            <div id="space"></div>
        </div>
        <div id="acceptAgreement">
            <div class="callup" id="userAgreementPanel">
                <h1>User Agreement</h1>
                You must accept the most recent T&#8209;PEN User Agreement to continue to access this site.
                <div id="agreement" class="boxsizingBorder ui-corner-all">
                    <p><font size="3" face="Times New Roman">Please read this notice carefully 
                            as it describes the terms and conditions for using T&#8209;PEN.  </font></p>
                    <p><font size="3" face="Times New Roman"><b>Conditions of Use</b></font></p>
                    <p><font size="3" face="Times New Roman">As a T&#8209;PEN user, you agree 
                            to use T&#8209;PEN, its tools and services, for their intended purpose.  
                            You will not use T&#8209;PEN for illegal purposes.  You will not use 
                            T&#8209;PEN to obtain digital images or transcription data without permission 
                            or to void any Intellectual Property Rights (IPR) governing one or more 
                            of the digital collections to which T&#8209;PEN provides access.  Furthermore, 
                            you agree not to infringe on the rights of other T&#8209;PEN users through 
                            your own use of T&#8209;PEN.  You also agree that any action that does 
                            contravene these conditions of use may result in the suspension and 
                            even deletion of your T&#8209;PEN account.  </font></p>
                    <p><font size="3" face="Times New Roman">You agree to abide by the 
                            IPR conditions that govern access to, and use of, digital images in 
                            each individual Digital Repository that have their manuscripts listed 
                            and displayed in T&#8209;PEN. Those notices are displayed when you request 
                            access to a manuscript of that repository for the first time. </font></p>
                    <p><font size="3" face="Times New Roman"><b>Intellectual Property and 
                                Permissions</b></font></p>
                    <p><font size="3" face="Times New Roman">You grant permission to 
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
                            of your first name and your surname.</font></p>
                    <p><font size="3" face="Times New Roman">You grant permission for 
                            SLU to share your transcription data with the Digital Repository where 
                            the digital manuscript resides.  The Digital Repository is prohibited 
                            form using any transcription data for commercial purposes nor can they 
                            distribute it without obtaining your permission. </font></p>
                    <p><font size="3" face="Times New Roman">You grant permission to 
                            the Andrew W. Mellon Foundation to have access to, keep copies of, and 
                            distribute your transcription data.  This IPR transfer is necessary 
                            should SLU, for some unforeseen reason, be unable to provide access 
                            to your transcription in the future; at which point the Mellon Foundation 
                            would be have the permissions to provide access to your data stored 
                            in T&#8209;PEN&#39;s server.  This IPR transfer, however, prohibits the Mellon 
                            Foundation from using your transcription data for commercial purposes. </font></p>
                    <p><font size="3" face="Times New Roman"><b>License to Use Transcription 
                                Data</b></font></p>
                    <p><font size="3" face="Times New Roman">SLU grants you an unlimited 
                            license to use any and all transcription data created under your username 
                            for non-commercial purposes.  You may export your transcription 
                            data using T&#8209;PEN&#39;s export functions and disseminate it in any electronic 
                            or print format.</font></p>
                    <p><font size="3" face="Times New Roman">This unlimited license 
                            cannot be interpreted as a license to gain access to repositories or 
                            individual manuscript images that are protected by subscription or conditions 
                            external to T&#8209;PEN functionality.     </font></p>
                    <p><font size="3" face="Times New Roman"><b>Privacy</b></font></p>
                    <p><font size="3" face="Times New Roman">SLU will never share your 
                            personal information that you provide to T&#8209;PEN without your express 
                            written permission.  This includes your full name, complete email 
                            address and list of projects and/or transcriptions.  Users who 
                            elect to collaborate  with other users on projects agree to share 
                            their full name and email address with their collaborators.  </font></p>
                    <p><font size="3" face="Times New Roman"><b>Indemnification</b></font></p>
                    <p><font size="3" face="Times New Roman">As a T&#8209;PEN user, you indemnify  
                            SLU, its affiliates and employees from any liability for damage to your 
                            computer and/or any information stored therein because of your use of 
                            T&#8209;PEN as a web-based application.</font></p>
                    <p><font size="3" face="Times New Roman">  </font></p>
                </div>
                <form id="acceptAgrForm" action="index.jsp" method="POST">
                    <input type="submit" id="accept" name="accept" class="ui-button tpenButton right" value="I have read and agree" />
                </form>
            </div>
        </div>
        <div id="betaNews" class="callup ui-widget-content">
            <div class="closeBetaNews right tpenButton" onclick="$('#betaNews,#overlay').hide('fade',500);">Close<span class="ui-icon ui-icon-closethick right"></span></div>
            <div id="toc" class="right ui-state-default ui-corner-all">
                <a class="ui-corner-all" href="#bbeta">The Beta</a>
                <a class="ui-corner-all" href="#bhelp">Help Out</a>
                <a class="ui-corner-all" href="#bfeat">Features</a>
                <a class="ui-corner-all" href="#bcost">Cost</a>
            </div>
            <div id="betaNewsText" class="clear boxsizingBorder">
                <h3>Welcome to the T&#8209;PEN Beta</h3>
                <p>T&#8209;PEN is a digital tool designed to help scholars and researchers transcribe unpublished manuscripts. T&#8209;PEN connects users to remote repositories, automatically parses the images to find lines and columns of text, and displays them for transcription and basic encoding.</p>
                <h5><a name="bbeta"></a>What Does Beta Mean?</h5>
                <p>T&#8209;PEN is a collection of interactions and tools, so every aspect of the web application is evolving. Our Beta release means you can find manuscripts on our currently available repositories and start your project. Data you create will not be destroyed without specific user request and (at this time) abundant warnings and chances to cancel. The tools we offer have some rough edges, pages may not always be stable, and helpful documentation and instructions are a work in progress. However, this Beta is exciting because of the new perspective it can offer on existing or evolving projects in the digital humanities. Of course, this is all offered without warranty according to terms in the User Agreement, etc. etc. . .</p>
                <h5><a name="bhelp"></a>How Can I Help?</h5>
                <p>As a Beta-tester, you will need to understand that you may run up against small and annoying bugs or a nasty bug that could stop you in your tracks. When this happens, use the BETA bug reporting form at the bottom right of most pages to let the T&#8209;PEN Team know. We resolve most bugs within one day and you will receive a notification that it has been handled.</p>
                <p>Additionally, if you like T&#8209;PEN, you can use the bug reporting form to request features such as new tool integration, connections with more repositories, or interoperability with your favorite web services. Encourage developers of your favorite tools to contact T&#8209;PEN and learn how we can integrate our services. Feel free to advertise for us and tell all your friends how +1 #TPEN is.</p>
                <h5><a name="bfeat"></a>Features (at Beta)</h5>
                <dl>
                    <dt>Automatic Line Parsing</dt>
                    <dd>T&#8209;PEN not only detects columns and lines within remote manuscript images, but will remember corrections and adjustments you have made to your project. Accuracy depends greatly on the quality and structure of each image, but T&#8209;PEN enjoys an 85% success rate with current manuscripts.</dd>
                    <dt>Line by Line Transcription</dt>
                    <dd>Make a meaningful connection between transcription data and the original manuscript. The helpful interface not only displays the manuscript line by line for simple text entry, encoding, and proofreading, but connects that coordinate data to the transcription for easy reference, portability, or annotation.</dd>
                    <dt>Customized Interface</dt>
                    <dd>Work towards your purpose by selecting the layout and tools that work best. Assign key shortcuts to special characters, build buttons for easy XML tag entry, or add freeform notes to specific lines of transcription.</dd>
                    <dt>Project Management</dt>
                    <dd>Combine, rearrange, and reduce manuscripts virtually by creating projects out of one or more manuscript image collections, even across different repositories. Add group members to the project to have a team of collaborators all working with the same custom schema, image parsing, button sets, and transcription.</dd>
                    <dt>Import and Export</dt>
                    <dd>Link an XML schema for quick validation or to import buttons for the included tags. Upload a file that already contains transcription data and easily (even automatically) linebreak the text. At any point in the project, export your work to a pdf, rtf, or plaintext file. </dd>
                    <dt>Integrated Tools</dt>
                    <dd>Split the screen with tools such as Capelli Abbreviations, a Latin dictionary, or compare with another page image. As more customizable tools are added, you will be able to select your toolset for each project, hiding tools you do not need access to.</dd>
                    <dt>Keyboard Shortcuts</dt>
                    <dd>Support for most browsers includes the following simple shortcuts:
                        <ul>
                            <li><span class="loud">CTRL + 1-9</span>: Insert the corresponding special character at the cursor location</li>
                            <li><span class="loud">CTRL + HOME</span>: Quickly jump to the first line of the current page.</li>
                            <li><span class="loud">ESC</span>: Close any open tool and return to fullscreen transcription; also works to reset the screen after resizing your browser window</li>
                            <li><span class="loud">SHIFT</span>: Hold to shift the interface. Slide the main workspace up and down to see more of the manuscript; move the manuscript image freely behind the main workspace; or resize the bookmark bounding box to precisely bound an odd or skewed line.</li>
                            <li><span class="loud">CTRL</span>: Hold to get a better view of the important parts of your manuscript. The bookmark bounding box, location flag, and bug report form will all fade out and let you see the manuscript or tool behind it.</li>
                            <li><span class="loud">SHIFT + CTRL</span>: Hold both to use the peek-zoom, which scales the bounded area to fit your screen and increases the quality of the image, if possible.</li>
                            <li><span class="loud">+ or - while magnifying</span>: Each keystroke will adjust the magnification of the tool by .4x to fine tune the image result.</li>
                        </ul></dd></dl>
                <h5><a name="bcost"></a>Cost</h5>
                <p>Though not often considered a feature, the T&#8209;PEN team is excited to offer T&#8209;PEN free. Though this tool does not provide unauthorized access to restricted manuscript images, approved users can still access secure manuscripts.</p>
                <p class="loud large">Thank you for participating, <span style="font-family: 'Stardos Stencil',monospace;">The T&#8209;PEN Team</span></p>
                <br /><br /><br /><br /><br /><br /><br />
            </div>
        </div>
        <div id="browseMS"> <!-- container for browsing listings -->
            <div class="callup" id="browseMSPanel"> <!-- select -->
                <span id="closePopup" class="tpenButton right" title="Close this window">close<span class="ui-icon ui-icon-closethick right"></span></span>
                <%    //Attach arrays of AllCities and AllRepositories represented on T&#8209;PEN
                    String[] cities = Manuscript.getAllCities();
                    String[] repositories = Manuscript.getAllRepositories();
                    if (thisUser == null) {
                %>
                <script>
                    function scrubListings (){
                        $("#listings").ajaxStop(function(){
                            $("#listings a[href *= 'MStoProject']").hide();
                            $("#countListings").html($("#count").html());
                        });
                    }
                </script>
                <div class="ui-state-error-text left boxsizingBorder" style="width:100%;"><a href="#" onclick="$('#closePopup').click();">Log in</a> to start a project.</div>
                <%  } else {%>
                <script>
                    function scrubListings (){
                        $("#listings").ajaxStop(function(){
                            //no scrub
                            $("#countListings").html($("#count").html());
                        });
                    }
                </script>
                <%}
                %>
                <div id="countListings" class="right clear-right"></div>
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
                <div id="listings" class="clear"  style="overflow: auto;">
                    <div class="ui-state-active ui-corner-all" align="center">
                        Select a city or repository above to view available manuscripts.
                    </div>
                </div>
            </div>
        </div>
                <%
if(thisUser!=null){
    %><%@include file="WEB-INF/includes/bugReport.jspf" %><%
}                
%>
    </body>
</html>
