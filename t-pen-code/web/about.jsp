<%-- 
    Document   : about
    Created on : Jan 18, 2012, 1:07:43 PM
    Author     : cubap
--%>

<%@page import="user.User"%>
<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title>About T-PEN</title>
        <link rel="stylesheet" href="css/tpen.css" type="text/css" media="screen, projection">
        <link rel="stylesheet" href="css/print.css" type="text/css" media="print">
        <link type="text/css" href="css/custom-theme/jQuery.css" rel="Stylesheet" />
        <script type="text/javascript" src="https://ajax.googleapis.com/ajax/libs/jquery/1.7.1/jquery.js"></script>
        <script type="text/javascript" src="https://ajax.googleapis.com/ajax/libs/jqueryui/1.8.16/jquery-ui.js"></script>
        <script type="text/javascript" src="js/tpen.js"></script>
        <style type="text/css">
            ul {padding: 0;margin: 0;}
            ul li {list-style:outside none;padding:2%;width:32%;margin:.666%;height: 375px;overflow: auto;}
            textarea {width:100%;height:auto;}
            form {padding-right: 5%}
            #contact:focus {height:200px;}
        </style>
    </head>
    <body>
        <div id="wrapper">
            <div id="header"><p align="center" class="tagline">transcription for paleographical and editorial notation</p></div>
            <div id="content">
                <h1><script>document.write(document.title); </script></h1>
                <div id="main">
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
                            <dl>
                                <dt>E-mail</dt>
                                <dd>
                                    <%
                                        if (request.getParameter("contactTPEN") != null) {
                                            String msg = "Message was not successfully received.";
                                            if (request.getParameter("contact") != null) {
                                                msg = request.getParameter("contact");
                                            }
                                            User thisUser = new User("ginthej@slu.edu");
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
                                    <form action="about.jsp" method="POST" onsubmit="return Message.isValid();">
                                        <script>
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
                                        <textarea id="contact" name="contact" placeholder="Include contact information in your message for a response."></textarea>
                                        <input type="submit" name="contactTPEN" value="Send Message" />
                                    </form> </dd>
                            </dl>

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
        </div>
    </body>
</html>
