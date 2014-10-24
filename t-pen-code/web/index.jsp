<%--
    Document   : index
    Created on : Oct 26, 2010, 12:08:31 PM
    Author     : cubap,jdeerin1
--%>
<%@page import="java.util.Date"%>
<%@page import="textdisplay.ProjectPermissions"%>
<%@page import="user.*"%>
<%@page import = "java.sql.SQLException"%>
<%@page import = "textdisplay.Project"%>
<%@page import = "textdisplay.Archive" %>
<%@page import = "textdisplay.Folio" %>
<%@page import = "textdisplay.CityMap" %>
<%@page import = "textdisplay.Manuscript" %>
<%@page import = "org.owasp.esapi.ESAPI" %>

<%@page contentType="text/html; charset=UTF-8"  pageEncoding="UTF-8"%>

<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN"
    "http://www.w3.org/TR/html4/loose.dtd">
<%
    user.User thisUser = null;

    int UID = 0;
    if (session.getAttribute("UID") != null) {
        thisUser = new user.User(Integer.parseInt(session.getAttribute("UID").toString()));
        UID = thisUser.getUID();
        if (request.getParameter("accept") != null) {
            thisUser.acceptUserAgreement();
        }
    }
    if (request.getParameter("makeCopy") != null) {
        Project thisProject;
        if (request.getParameter("projectID") != null) {
            thisProject = new Project(Integer.parseInt(request.getParameter("projectID")));
            thisProject.copyProject(UID);
        }
    }
    %>
<html itemscope itemtype="http://schema.org/Product">
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <meta property="og:title" content="T-PEN" />
        <meta property="og:description" 
              content="Transcription for paleographical and editorial 
              notation" />
        <meta property="og:type" content="website" />
        <meta property="og:url" content="http://www.t-pen.org" />
        <meta property="og:site_name" content="T-PEN" />
        <meta property="fb:admins" content="155508371151230" />
        <meta itemprop="name" content="T-PEN">
        <meta itemprop="description" content="Digital tool for transcription">
        <meta itemprop="image" content="https://lh3.googleusercontent.com/-TysT8pvMcgI/AAAAAAAAAAI/AAAAAAAAADI/PWEsFECiPwE/s250-c-k/photo.jpg">        
        <link rel="shortcut icon" type="image/x-icon" href="/images/favicon.ico">
        <title>TPEN <%out.println("Version " + Folio.getRbTok("VERSION"));%></title>
        <link rel="stylesheet" href="css/tpen.css" type="text/css" media="screen, projection">
        <link rel="stylesheet" href="css/print.css" type="text/css" media="print">
        <!--[if lt IE 8]><link rel="stylesheet" href="css/ie.css" type="text/css" media="screen, projection"><![endif]-->
        <link type="text/css" href="css/custom-theme/jQuery.css" rel="stylesheet" />
        <link href='http://fonts.googleapis.com/css?family=Stardos+Stencil:700' rel='stylesheet' type='text/css'>
        <script type="text/javascript" src="//ajax.googleapis.com/ajax/libs/jquery/1.7.1/jquery.min.js"></script>
        <script type="text/javascript" src="https://ajax.googleapis.com/ajax/libs/jqueryui/1.8.16/jquery-ui.js"></script> 
        <script src="js/manuscriptFilters.js" type="text/javascript"></script>
        <script src="js/tpen.js" type="text/javascript"></script>
        <style type="text/css">
            html, body {
                height: 100%;
                width: 100%;
                padding: 0px;
                margin: 0px;
            }
            body {
                background:#69ACC9
                    url(images/tpen_logo_header.jpg)
                    top left
                    no-repeat
                    scroll;
            }
            #cities, #repositories {
                min-width: 40px;
            }
            #yourPage, ul li a {
                text-decoration: none;
            }
            ul li {
                list-style:outside none;
                padding:3px;
                width:100%;
            }
            ul li a {
                padding:2px;
            }
            #userKnown2, #userUnknown2,#browseListings {
                margin: 8px 0; 
                overflow: hidden; 
                padding: 5px;
            }
            #userUnknown2 a,#userUnknown2 input[type="submit"] {
                margin: 3px 0;
                width: 150px;
            }
            #overlay {
                width:600%;
                height:600%;
            }
            #tabs {
                clear:both;
                height:90%;
                border-radius:10px;
            }
            #manuscripts li a {
                text-shadow: 0 -1px 0px rgba(255, 255, 255, .5);
                font-weight: 700;
            }
            #cityMS,#repositoryMS {
                width: 50%;
                min-width: 150px;
                float: left;
            }
            #login {
                position: absolute;
                left: 0;
                right: 0;
                margin: 0 auto;
                top: 5px;
                background: rgba(105, 172, 201, .4);
                width: 250px;
            }
            #login form{
                text-align: right;
            }
            #login label,#login input.text {
                float: none;width:auto;
            }
            #login input.text,-webkit-autofill {
                background: white !important;
                box-shadow:-1px -1px 10px -4px black inset;
            }
            #menu li {width:auto; z-index: 2;}
            #msCount {position: absolute;right: 5px;top: 10px;z-index: 0;}
            .column {
                min-width: 350px; 
                width: 50%;
                float: left;
            }
            #newArticle {
                display: none;
                position: absolute;
                left: 40%;
                width: 20%;
                top: 15px;
                z-index: 600;
                cursor: pointer;
            }
            #newArticle a {
                color: white;
                text-decoration: none;
                line-height: 1.5;
                font-weight: 700;
                font-size: 125%;
            }
            #newArticle:hover {
                background-color: rgba(255,255,255,.5);
            }
            td > a {
                text-decoration: none;
            }
        </style>
        <script type="text/javascript">
            var recentArticleDate;
            $(function() {
                $( "#tabs" ).tabs({
                    show: tabSize
		});
                tabSize();
                $(window).resize(tabSize);
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
                $("#blogTitles").on("mouseenter",".blogTitle",function(){
                    $(this).addClass("blogTitleHover").siblings().removeClass("blogTitleHover");
                    $(".blogEntry").eq($(this).index()).show().siblings().hide();
                });
                $("#blogTags").change(function(){
                    var selectedCategory = $("#blogTags").find("option").filter(":selected").text();
                    $(".blogTitle").each(function(){
                        $(this).show();
                        if (!$(this).hasClass(selectedCategory)){
                            $(this).hide();
                        }
                    });
                    $(".blogTitle").filter(":visible").eq(0).trigger("mouseover");
                });
                $(".msSelect").on({
                    click:  function(){
                        Manuscript.resetFilters();
                        var $this = $(this);
                        $("#browseMS").css({
                            "top"   : Page.height() * .15,
                            "left"  : Page.width() * .15,
                            "width" : Page.width() * .7,
                            "height": Page.height() * .75
                        });
                        $("#listings").height($("#browseMS").height()-109);
                        $("#browseMS,#overlay").show('fade',500,function(){
                            if($this.parent().parent().is("#cityMS")){
                                $("#cities").find("option").eq($this.index()+1).prop("selected",true);
                                Manuscript.filteredCity();
                            } else if ($this.parent().parent().is("#repositoryMS")){
                                $("#repositories").find("option").eq($this.index()).prop("selected",true);
                                Manuscript.filteredRepository();
                            } else {alert("Apologies, but there was an error with your request.")}
                        });
                    }
                });
                $("#cityMap").error(function(){
                    $(this).hide();
                });
                $("#cityMS").find(".msSelect").on({
                    mouseenter: function(){
                        var city = $(this).attr('data-map');
                        if (city.length < 3) return false; //No reliable map data in lookup table
                        var src = [
                            "https://maps.googleapis.com/maps/api/staticmap?",
                            "center=",city,
                            "&markers=icon:http://www.t-pen.org/TPENFRESH/images/quillpin.png|",city,
                            "&sensor=false&scale=1&zoom=3&visibility=simplified&maptype=terrain",
                            "&size=",$("#cityMapContain").width(),"x",$("#cityMapContain").height()
                        ].join("");
                        $("#cityMap").attr("src",src).parent().show();
                        var src2 = [
                            "https://maps.googleapis.com/maps/api/staticmap?",
                            "center=",city,
                            "&sensor=false&scale=1&zoom=10&visibility=simplified&maptype=terrain",
                            "&size=",Math.round($("#cityMapContain").width()*.3),"x",Math.round($("#cityMapContain").height()*.9)
                        ].join("");
                        $("#cityMapZoom img").attr("src",src2).show();
                    },
                    mouseleave: function(){
                        $("#cityMapContain").hide();
                    }
                });
                $("#closePopup").click(function(){
                    $("#browseMS,#overlay").hide('fade',500,function(){
                    });
                });        
                if (previousLogin < recentArticleDate){
                    $("#newArticle")
                    .show()
                    .click(function(){
                        $("#updateBtn").click();
                        $(this).fadeOut("slow");
                    });
                }
            });
            // Date.prototype.format v1.0
            // John Strickler						
            // http://www.opensource.org/licenses/mit-license.php
            if(!Date.prototype.format){Date.prototype.format=(function(){var a={d:function(){var b=this.getDate().toString();return b.length===1?"0"+b:b},D:function(){return a.l.call(this).slice(0,3)},j:function(){return this.getDate()},l:function(){switch(this.getDay()){case 0:return"Sunday";case 1:return"Monday";case 2:return"Tuesday";case 3:return"Wednesday";case 4:return"Thursday";case 5:return"Friday";case 6:return"Saturday"}},N:function(){return this.getDay()===0?7:this.getDay()},S:function(){if(this.getDate()>3&&this.getDate()<21){return"th"}switch(this.getDate().toString().slice(-1)){case"1":return"st";case"2":return"nd";case"3":return"rd";default:return"th"}},w:function(){return this.getDay()},z:function(){return Math.floor(((this-new Date(this.getFullYear(),0,1))/86400000),0)},W:function(){var b=new Date(this.getFullYear(),0,1);return Math.ceil((((this-b)/86400000)+b.getDay()+1)/7)},F:function(){switch(this.getMonth()){case 0:return"January";case 1:return"February";case 2:return"March";case 3:return"April";case 4:return"May";case 5:return"June";case 6:return"July";case 7:return"August";case 8:return"September";case 9:return"October";case 10:return"November";case 11:return"December"}},m:function(){var b=(this.getMonth()+1).toString();return b.length===1?"0"+b:b},M:function(){return a.F.call(this).slice(0,3)},n:function(){return this.getMonth()+1},t:function(){return 32-new Date(this.getFullYear(),this.getMonth(),32).getDate()},L:function(){return new Date(this.getFullYear(),1,29).getDate()===29?1:0},o:function(){return null},Y:function(){return this.getFullYear()},y:function(){return this.getFullYear().toString().slice(-2)},a:function(){return this.getHours()<12?"am":"pm"},A:function(){return this.getHours()<12?"AM":"PM"},B:function(){return null},g:function(){var b=this.getHours();return b>12?b-12:b},G:function(){return this.getHours()},h:function(){var b=a.g.call(this).toString();return b.length===1?"0"+b:b},H:function(){var b=a.G.call(this).toString();return b.length===1?"0"+b:b},i:function(){return this.getMinutes()<10?"0"+this.getMinutes():this.getMinutes()},s:function(){return this.getSeconds()<10?"0"+this.getSeconds():this.getSeconds()},u:function(){return this.getMilliseconds()},e:function(){return null},I:function(){return null},O:function(){var b=this.getTimezoneOffset()/60;return(b<0?"":"+")+(b<10?"0"+b.toString():b.toString())+"00"},P:function(){var b=a.O.call(this);return b.slice(0,3)+":"+b.slice(-2)},T:function(){return null},Z:function(){return parseInt(a.O.call(this),10)*60},c:function(){function c(d){return d<10?"0"+d.toString():d.toString()}var b="";b+=this.getUTCFullYear()+"-";b+=c(this.getUTCMonth()+1)+"-";b+=c(this.getUTCDate())+"T";b+=c(this.getUTCHours())+":";b+=c(this.getUTCMinutes())+":";b+=c(this.getUTCSeconds())+"Z";return b},r:function(){return this.toUTCString()},U:function(){return this.getTime()}};return function(b){var c="",e="",d;for(d=0;d<=b.length;d+=1){e=b.charAt(d);if(a.hasOwnProperty(e)){c+=a[e].call(this).toString()}else{c+=e}}return c}}())};
            function maintenanceDate(){
                var today = new Date();
                while (today.getDay() !== 4){
                    today.setDate(today.getDate()+1);
                }
                // set to 7pm Central Time
                today.setHours(24 - today.getTimezoneOffset()/60);
                return(today.format('l, F jS, Y g:00a'));
            }
            var blogTags = new Array();
            function blogPostsJson(data) {
                        var posts = data.feed;
                        console.log(posts);
                        recentArticleDate = Date.parse(posts.entry[0].published.$t);
                        var addResult = new Array();
                        var blogContents = new Array();
                        for (var i=0;i<posts.entry.length;i++){
                            var tagsLength = posts.entry[i].category.length;
                            var theseTags = new Array();
                            for (var j=0;j<tagsLength;j++){
                                var thisTag = posts.entry[i].category[j].term;
                                theseTags.push(thisTag);
                                if ($.inArray(thisTag,blogTags) === -1){
                                    blogTags.push(thisTag);
                                }
                            }
                            addResult.push(
                                "<li class='blogTitle ui-state-default ",theseTags.join(" "),"'><a href='",posts.entry[i].link.pop().href,"' target='_blank'>",
                                posts.entry[i].title.$t.replace("T-PEN","T&#8209;PEN"),
                                "</a></li>");
                            blogContents.push(
                                "<li class='blogEntry'><div class='byline'><div class='blogDate small'>",
                                posts.entry[i].published.$t.substr(0, 16),
                                "</div><div class='blogAuthor small'>Written by ",
                                posts.entry[i].author[0].name.$t,
                                "</div></div><div class='blogSnippet'>",
                                posts.entry[i].content.$t,
                                "</div></li>");
                        }
                        $(function(){
                          $("#blogTitles")
                            .append(addResult.join(""));
                        $("#blogContent")
                            .append(blogContents.join(""));
                      $("#blogTags").append(buildBlogTags());
                      $("#blogLoad").remove();
                        });
            };
            function tabSize(){
            if(!!$("#tabs").length && !!$("#projects").length && !!$("#blogTitles").length && !!$("#recentProject").length) {
                var tabsHeight = $("#tabs").height();
                    var maxHeight = tabsHeight-$("#projects").position().top;
                        $("#projects")
                            .css("max-height",maxHeight);
                    maxHeight = tabsHeight-$("#blogTitles").position().top;
                        $("#blogTitles")
                            .css("max-height",maxHeight).attr('class','');
                        $("#blogContent")
                            .css("max-height",maxHeight);
                        $(".blogTitle").eq(0).trigger("mouseover"); //default to first
                    $(".lists").filter(":visible").each(function(){
                        maxHeight = tabsHeight-$(this).position().top;
                            $(this)
                                .css("max-height",maxHeight);
                    });
                    if($("#recentProject").length>0){
                        maxHeight = tabsHeight-$("#recentProject").position().top;
                            $("#recentProject,#projectTable")
                                .css("height",maxHeight);
                    }
            }
            }
            function buildBlogTags(){
                var tagOptions = new Array();
                var tagsLength = blogTags.length;
                for (var i=0;i<tagsLength;i++){
                    if (blogTags[i] == 'T-PEN') continue;
                    tagOptions.push(blogTags[i]);
                }
                return "<option>"+tagOptions.join("</option><option>")+"</option>";
            }
//            Google and Facebook Buttons
//            (function() {
//                var po = document.createElement('script'); po.type = 'text/javascript'; po.async = true;
//                po.src = 'https://apis.google.com/js/plusone.js';
//                var s = document.getElementsByTagName('script')[0]; s.parentNode.insertBefore(po, s);
//            })();
//            (function(d, s, id) {
//  var js, fjs = d.getElementsByTagName(s)[0];
//  if (d.getElementById(id)) return;
//  js = d.createElement(s); js.id = id;
//  js.src = "//connect.facebook.net/en_US/all.js#xfbml=1";
//  fjs.parentNode.insertBefore(js, fjs);
//}(document, 'script', 'facebook-jssdk'));
$(window).load(function(){gapi.plusone.go();});
        </script>
        <script src="http://digital-editor.blogspot.com/feeds/posts/default?alt=json-in-script&callback=blogPostsJson"></script>
<script type="text/javascript" src="https://apis.google.com/js/plusone.js">
  {parsetags: 'explicit'}
</script>
    </head>
    <body id="landing2">
        <div id="versionFlag">
            <%out.println("Version " + Folio.getRbTok("VERSION") + " Build " + Folio.getRbTok("BUILD") + "<br/>(Built " + Folio.getRbTok("DATE") + ")");%>
            <div id="fb-root"></div>
            <div class="fb-like" data-href="http://www.t-pen.org" data-send="false" data-layout="button_count" data-width="20" data-show-faces="false" data-action="recommend" data-font="segoe ui"></div>
            <div class="g-plusone" data-size="small" data-href="http://www.t-pen.org"></div>
        </div>
        <div id="newArticle">
            <a>
                <img alt="New Article" src="images/sharing/blogger-new.png" class="left" />
                A new article has been published since your last login
            </a>
        </div>
        <div id="wrapper2">
<%
    if (thisUser == null){%>
                        <div id="login">
                        <form action="login.jsp" method="POST">
                            <label for="uname">Email</label><input class="text" type="text" name="uname">
                            <label for="password">Password</label><input class="text" type="password" name="password">
                            <input type="hidden" name="ref" value="http://t-pen.org/TPENFRESH/login.jsp">
                            <input class="ui-state-default ui-button ui-corner-all" type="submit" title="Log In" value="Log In">
                        </form>
                            <h6 id="forgetFormBtn" class="loud caps left">Reset your Password<span class="left ui-icon ui-icon-arrowstop-1-s"></span></h6>
                            <form id="forgetForm" action="admin.jsp" method="POST" class="ui-corner-all left clear-left">
                                <span>Enter the email address associated with your account to have your password reset.</span>
                                <input id="email" type="text" class="text" style="width:220px;" placeholder="Forgot your password?" name="email">
                                <input class="right tpenButton" type="submit" name="emailSubmitted" value="Reset Password"/>
                            </form>
                        </div>
    <%}%>
            <div align="center" class="tagline">
                transcription for paleographical and editorial notation</div>
            <div id="header2">
                <div id="maintenance" class="loud"><span class="ui-icon ui-icon-info left"></span>Scheduled Maintenance: <script type="text/javascript">document.write(maintenanceDate());</script></div>
                    <%
                    if (thisUser != null){
                        Date previousLogin = thisUser.getLastActiveDate();
                        thisUser.updateLastActive();
                        user.User [] currentUsers = user.User.getLoggedInUsers();
                        int numberTranscribing = currentUsers.length;
                        String userCount = (numberTranscribing==1) ? "is 1 user" : "are "+numberTranscribing+" users";
                        StringBuilder usersTranscribing = new StringBuilder();
                        for (int i = 0 ; i<numberTranscribing;i++){
                            usersTranscribing.append(currentUsers[i].getFname().substring(0,1)+" "+currentUsers[i].getLname()+" | ");
                        }
                        if(numberTranscribing==0)usersTranscribing.append("No one is transcribing at the moment...   ");
                        int sbLength = usersTranscribing.length();
                    %>
                    <script type="text/javascript">
                    <%
                        out.print("var previousLogin = '" + previousLogin.getTime() + "';");
                    %>                      
                    </script>
                        <div>
                            Welcome, <%out.print(thisUser.getFname()+" "+thisUser.getLname());%>. <a href="login.jsp" onclick="logout();return false;">Logout/Change User</a>
                            <br/><div>There <%out.print("<span title='"+usersTranscribing.toString().substring(0,sbLength-3) +"' id=userList>"+userCount+"</span>");%> transcribing right now</div>
                        </div>
                    <%} else {%>
                    <%}%>    
                                    <div id='sharing'>
                    <a id="shareFacebook" class="share" 
                       href="http://www.facebook.com/pages/The-T-Pen-project/155508371151230"
                       sharehref="http://www.facebook.com/sharer/sharer.php?u=http%3A%2F%2Fwww.t-pen.org"
                       target="_blank">
                        <img alt="facebook"
                             src="images/sharing/facebook.png"/>
                    </a>
                    <a id="shareGoogle" class="share" 
                       href="https://plus.google.com/104676239440224157170"
                       share-href="https://plus.google.com/share?url=http%3A%2F%2Fwww.t-pen.org&hl=en-US"
                       target="_blank">
                        <img alt="google+"
                             src="images/sharing/google+.png"/>
                    </a>
                    <a id="shareTwitter" class="share" 
                       href="https://twitter.com/intent/tweet?text=Well%20done%2C%20%23TPEN"
                       target="_blank">
                        <img alt="twitter"
                             src="images/sharing/twitter.png"/>
                    </a>
                    <a id="shareYoutube" class="share" 
                       href="http://www.youtube.com/user/tpentool"
                       target="_blank">
                        <img alt="youtube"
                             src="images/sharing/youtube-128.png"/>
                    </a>
                    <a id="shareBlogger" class="share" 
                       href="http://digital-editor.blogspot.com/"
                       target="_blank">
                        <img alt="blogger"
                             src="images/sharing/blogger-128.png"/>
                    </a>
                </div>
            </div>
            <div id="tabs">
              <ul id="menu">
                <%
                if (thisUser == null){
                %>
                        <li><a title="Welcome" href="#projects">Welcome</a></li>
                <%
                               } else {
                %>
                        <li><a title="Active Projects" href="#projects">Active Projects</a></li>
                        <li><a title="Public Projects" href="#publicProjects">Public Projects</a></li>
                        <li><a title="T-PEN Advanced" href="#advancedTpen">Advanced</a></li>
                <%
                               }
                %>
                        <li><a title="T-PEN Team Blog" id="updateBtn" href="#updates">Updates</a></li>
                        <li><a title="Browse all available manuscripts by city or repository" href="#manuscripts">Browse Manuscripts</a></li>
                        <span class="right caps" id="msCount"><%out.print(textdisplay.Manuscript.getTotalManuscriptCount());%> Manuscripts Available</span>
                </ul>
                <div id="manuscripts">
                    <%
                    String [] allCities = Manuscript.getAllCities();
                    String [] allRepositories = Manuscript.getAllRepositories();
                    %>
                    <div id="cityMS">
                        <h3>Available Cities</h3>
                        <div class="lists">
                            <%
                            for (int i=0;i<allCities.length;i++){
                                String cityName = (allCities[i].length() > 1) ? allCities[i] : "&nbsp;";
                                CityMap thisCity = new CityMap(allCities[i]);
                                String mapped = thisCity.getValue();
                                if(mapped.length()<2){ // New city listing
                                    mapped = "na";
                                }
                                out.print("<a class='msSelect' title='"+cityName+"' data-map='"+mapped+"'>"+cityName+"</a> ");
                            }
                            %>
                        </div>
                    </div>
                    <div id="repositoryMS">
                        <h3>Available Repositories</h3>
                        <div class="lists">
                          <div id="cityMapContain">
                                          <img id="cityMap" alt="map" src="https://maps.googleapis.com/maps/api/staticmap?center=St.%20Louis&zoom=3&sensor=false&scale=1&size=300x200&maptype=terrain&visibility=simplified&markers=icon:http://www.t-pen.org/TPENFRESH/images/quillpin.png%257St.%20Louis" />
                                          <div id="cityMapZoom">
                                              <img alt="inset" src="https://maps.googleapis.com/maps/api/staticmap?center=St.%20Louis&zoom=10&sensor=false&scale=1&size=100x140&maptype=terrain&visibility=simplified&markers=icon:http://www.t-pen.org/TPENFRESH/images/quillpin.png%257St.%20Louis" />
                                          </div>
                          </div>
                            <%
                            for (int i=0;i<allRepositories.length;i++){
                                String repoName = (allRepositories[i].length() > 1) ? allRepositories[i] : "&nbsp;";
                                out.print("<a class='msSelect' title='"+repoName+"'>"+repoName+"</a> ");
                            }
                            %>
                        </div>
                    </div>
                </div>
                <div id="updates">
                    <div id="blogEntries">
                    <select id="blogTags" class="right">
                        <option>T-PEN</option>
                    </select>
                    <h2>Updates</h2>
                        <a id="blogLink" href="http://digital-editor.blogspot.com" target="_blank">Visit the T-PEN blog to read more <span class='ui-icon ui-icon-extlink left'>(new window)</span></a>
                        <ul id="blogTitles">
                        </ul>
                        <ul id="blogContent"></ul>
                    </div>
                </div>
                <%
                if (thisUser != null){
                %>
                <div id="projects">
                    <%
                    if (thisUser.getAnyLastModifiedFolio() != "-1") {
                        String lastFolio[] = thisUser.getAnyLastModifiedFolio().split(",");
                        String lastProject = (Integer.parseInt(lastFolio[1]) > 0) ? "&projectID=" + lastFolio[1] : "";
                        textdisplay.Folio thisFolio = new textdisplay.Folio(Integer.parseInt(lastFolio[0]));
                        out.println("<a id=\"yourPage\" href=\"transcription.jsp?p=" + lastFolio[0] + lastProject + "\"><span id=recentProject class=\"ui-corner-all\" style=\"background:-10px -70px url('" + thisFolio.getImageURLResize(600) + "');\">");
                        String projectTitle = "This work is not part of a project";
                        //find the last project title
                        if (lastProject.length() > 1) {
                            Project lastProj = new Project(Integer.parseInt(lastFolio[1]));
                            projectTitle = "Working on project: " + lastProj.getProjectName();
                            }
                    %>
                    <span title="<%out.print(projectTitle);%>" style='position:relative;top:-14px; left:0; z-index:5;'>
                        <img src='images/ribbon.png' alt="bookmark" />
                    </span>
                    <%
                        //find the full name of the recent manuscript
                        textdisplay.Manuscript ms = new textdisplay.Manuscript(thisFolio.getFolioNumber());
                        String recentPage = ESAPI.encoder().decodeFromURL(ms.getShelfMark() + " " + thisFolio.getPageName());
                        out.println("<span id='pagename' class='ui-corner-bl ui-coprner-br'>" + recentPage + "</span></span></a>");
                    }
                    try {
                        textdisplay.Project[] userProjects = thisUser.getUserProjects();
                        if (userProjects.length > 0) {
                            %>
                            <div id="projectTable">
                            <%@include file="WEB-INF/includes/projectPriority.jspf" %>
                    <table id="projectList">
                        <tbody>
<%                      int projectID = 0;
                        String projectTitle;
                        int recentFolio = 0;
                        for (int i = 0; i < userProjects.length; i++) {
                            projectID = userProjects[i].getProjectID();
                            projectTitle = userProjects[i].getProjectName();
                            recentFolio = userProjects[i].getLastModifiedFolio();
                            out.print("<tr title=\"" + projectTitle 
                                    + "\"><td><a href=\"transcription.jsp?projectID=" + projectID + "&p="+ recentFolio +"\">"+ projectTitle + "</a></td>"
                                    + "<td><a href=\"transcription.jsp?projectID=" + projectID + "&p="+ recentFolio +"\" title='Resume Transcribing' class='left'><span class='ui-icon ui-icon-pencil left'></span>Resume</a></td>"
                                    + "<td><a href=\"project.jsp?projectID=" + projectID + "\" title='Manage this Project' class='left'><span class='ui-icon ui-icon-gear left'></span>Manage</a></td>"
                                    + "<td><a href='#' onclick='$(\"#rearrangeProjects\").click();return false;' title='Reorder this List'><span class='ui-icon ui-icon-shuffle left'></span></a></td>");
                        }
                        %> 
                        </tbody>
                    </table>
                        </div><%
                        } else {
                            out.print("Getting Started:<br/><iframe src='http://www.youtube.com/embed/KZWIlzD9H_o' allowfullscreen></iframe>");
                        }
                   } catch (SQLException err) {
                    out.print("<p class=ui-state-error-text>Error retreiving list of projects.</p>");
                   }%>
                </div>
                <div id="publicProjects">
                    <%
                    Project [] publicProjects = Project.getPublicProjects();
                                       if (publicProjects.length > 0) {
                            %>
                            <div id="publicProjectTable">
                    <table id="publicProjectList">
                        <thead>
                            <tr>
                                <th class="publicProjectActions" title="Copy or Export (if available)">Actions</th>
                                <th>Title</th>
                                <th class="publicProjectLeader">Project Leader</th>
                                <th class="publicProjectHeader" title="Transcription"><span class="ui-icon ui-icon-note"></span></th>
                                <th class="publicProjectHeader" title="Manage Options (if available)"><span class="ui-icon ui-icon-gear"></span></th>
                                <th class="publicProjectHeader" title="View Permissions"><span class="ui-icon ui-icon-link"></span></th>
                                <th class="publicProjectHeader" title="Export/Copy Permissions"><span class="ui-icon ui-icon-copy"></span></th>
                                <th class="publicProjectHeader" title="Modification Permissions"><span class="ui-icon ui-icon-pencil"></span></th>
                            </tr>
                        </thead>
                        <tbody>
<%                      int publicProjectID = 0;
                        textdisplay.ProjectPermissions permit;
                        String publicProjectTitle;
                        int recentFolio = 0;
                        String publicGroupLeaderName;
                        User publicGroupLeader;
                        int permitExternal, permitView, permitMod;
                        boolean permitProject, permitTranscription;
                        for (int i = 0; i < publicProjects.length; i++) {
                            publicProjectID = publicProjects[i].getProjectID();
                            permit = new ProjectPermissions(publicProjectID);
                            publicGroupLeader = new user.Group(publicProjects[i].getGroupID()).getLeader()[0];
                            publicGroupLeaderName = publicGroupLeader.getFname().substring(0,1)+" "+publicGroupLeader.getLname();
                            permitExternal = (permit.getAllow_OAC_read())?1:0;
                            permitExternal = (permit.getAllow_OAC_write())?2:permitExternal;
                            permitView = (permit.getAllow_export())?1:0;
                            permitView = (permit.getAllow_public_copy())?2:permitView;
                            permitMod = (permit.getAllow_public_modify_notes()||permit.getAllow_public_modify_annotation()||permit.getAllow_public_modify_buttons()||permit.getAllow_public_modify_line_parsing()||permit.getAllow_public_modify_metadata())?1:0;
                            permitMod = (permit.getAllow_public_modify())?permitMod++:permitMod;
                            permitProject = permit.getAllow_export() || permit.getAllow_public_modify_buttons() || permit.getAllow_public_modify_metadata();
                            permitTranscription = permit.getAllow_public_read_transcription() && (permit.getAllow_public_modify_notes() || permit.getAllow_public_modify_line_parsing() || permit.getAllow_public_modify_annotation() || permit.getAllow_public_modify());
                            recentFolio = publicProjects[i].getLastModifiedFolio();
                            publicProjectTitle = (permitTranscription) ? 
                                "<a href='transcription.jsp?projectID=" 
                                + publicProjectID + "&p="+ recentFolio 
                                + "' title='Most Recent Changes'>" 
                                +publicProjects[i].getProjectName()+"</a>"
                                : "<a href='transcription.jsp?projectID=" 
                                + publicProjectID + "' title='First Page'>" 
                                + publicProjects[i].getProjectName()+"</a>";
                            out.print("<tr title=\"" + publicProjectTitle + "\">"
                                    + "<td>");
                            if(permit.getAllow_export()){
                            out.print("<a href=\"project.jsp?projectID=" + publicProjectID + "&p="+ recentFolio +"&selecTab=4\" title='Export'><span class='ui-icon ui-icon-extlink left'></span></a>");
                            }
                            if(permit.getAllow_public_copy() && !publicProjects[i].containsUserUploadedManuscript()){
                            out.print("<a href=\"index.jsp?projectID=" + publicProjectID + "&p="+ recentFolio +"&makeCopy=true\" title='Create a Copy'><span class='ui-icon ui-icon-copy left'></span></a>");
                            }
                            if(permitTranscription){
                            out.print("<a href=\"transcription.jsp?projectID=" + publicProjectID + "&p="+ recentFolio +"\" title='Most Recent Changes'><span class='ui-icon ui-icon-pencil left'></span></a>");
                            }
                            if(permitProject){
                            out.print("<a href=\"project.jsp?projectID=" + publicProjectID + "\" title='Manage this Project'><span class='ui-icon ui-icon-gear left'></span></a>");
                            }                            
                            out.print("</td>"
                                    + "<div id='project" + publicProjectID + "' class='projectDetails'></div>"
                                    + "<td>"+ publicProjectTitle + "</td>"
                                    + "<td>"+publicGroupLeaderName+"</td>");
                            if(permitTranscription){
                            out.print("<td><a href=\"transcription.jsp?projectID=" + publicProjectID + "&p="+ recentFolio +"\" title='Most Recent Changes'><span class='ui-icon ui-icon-pencil left'></span></a></td>");
                            } else {
                            out.print("<td><a href=\"transcription.jsp?projectID=" + publicProjectID + "\" title='Read Transcription'><span class='ui-icon ui-icon-note left'></span></a></td>");
                            }
                            if(permitProject){
                            out.print("<td><a href=\"project.jsp?projectID=" + publicProjectID + "\" title='Manage this Project'><span class='ui-icon ui-icon-gear left'></span></a></td>");
                            } else {
                            out.print("<td></td>");
                            }
                            StringBuilder permissions = new StringBuilder();
                            switch (permitExternal){
                               case 1: // read only external access
                                  permissions.append("<td title='Read-only External Access' class='permitSome'><span class='ui-icon ui-icon-unlocked'></span></td>");
                                  break;
                               case 2: // read-write external access
                                  permissions.append("<td title='Full External Access' class='permitAll'><span class='ui-icon ui-icon-transferthick-e-w'></span></td>");
                                  break;
                               case 0: default: // no external access
                                  permissions.append("<td title='No External Access' class='permitNone'><span class='ui-icon ui-icon-locked'></span></td>");
                            }
                            switch (permitView){
                               case 1: // export permitted
                                  permissions.append("<td title='Export Permission' class='permitSome'><span class='ui-icon ui-icon-extlink'></span></td>");
                                  break;
                               case 2: // copy permitted
                                  permissions.append("<td title='Copy Permission' class='permitAll'><span class='ui-icon ui-icon-copy'></span></td>");
                                  break;
                               case 0: default: // view only
                                  permissions.append("<td title='View Only' class='permitNone'><span class='ui-icon ui-icon-locked'></span></td>");
                            }
                            switch (permitMod){
                               case 1: // modify aspects permitted
                                  permissions.append("<td title='Some Modification Allowed' class='permitSome'><span class='ui-icon ui-icon-wrench'></span></td>");
                                  break;
                               case 2: // modify transcription permitted
                                  permissions.append("<td title='Transcription Modification Allowed' class='permitAll'><span class='ui-icon ui-icon-pencil'></span></td>");
                                  break;
                               case 0: default: // view only
                                  permissions.append("<td title='View Only' class='permitNone'><span class='ui-icon ui-icon-locked'></span></td>");
                            }
                            permissions.append("</tr>");
                            out.println(permissions.toString());
                        }
                        %> 
                        </tbody>
                    </table>
                        </div><%
                        } else {
                            out.print("There are no publicly accessible projects.");
                        }
                   %>
                </div>
                <div id="advancedTpen" class="lists">
                    <div>
                        <h2>Tools for Advanced Users</h2>
                    </div>
                    <p class="gloss ui-state-active ui-corner-all left">
                        T&#8209;PEN includes several advanced features that 
                        allow a user to accomplish even more. As they are
                        available, these options will be listed here. These
                        useful tools are in good working order, but may require
                        technical knowledge or preparation to use.</p>
                    <div class="clear-left">
                        <h3>Private Collections</h3>
                        <p>
                        Private collections of images can be hosted by 
                        T&#8209;PEN, analyzed by our tools, and transcribed as
                        if loaded from a repository.
                        </p>
                        <p>
                        Project with private images may not be made public or copied.
                            <%@include file="WEB-INF/includes/privateUpload.jspf" %>
                    </div>
                    <div>
                        <h3>Switchboard</h3>
                        <p>
                            Your institution can use T&#8209;PEN as a workspace. 
                            Create a template project for interested users with
                            a customized set of buttons and project tools. After
                            setting up a web service, all users with connected 
                            projects can submit directly with a simple button in
                            the export tool.
                        </p>
                        <p>
                            You can set up a template on the 
                            <a href="project.jsp?selecTab=2">Project
                            Management</a> page, but you will need to contact us
                            to set up a web service. The 
                            <a
                            title="Carolingian Canon Law project" 
                            href="http://ccl.rch.uky.edu/transcribe" 
                            target="_blank">CCL</a> 
                            already has a Switchboard!
                        </p>
                    </div>
                    <div>
                        <h3>Public Projects</h3>
                        <p>
                            Any project that does not contain any private images
                            can become a Public Project, allowing users who are
                            not members access to all or part of the project.
                            The permissions of any project can be changed at any
                            time by the group leader.
                        </p>
                        <p>
                            View all Public Projects on the tab above. Set
                            permissions for your eligible project by using the
                            "Share Publicly" button at the top of the 
                            "Collaboration" tab on the 
                            <a href="project.jsp">Project Management page.</a>
                        </p>
                    </div>
                </div>
                <%} else {%>
                <div id="projects">
                    <div id="requestAccount">
                        <a class="tpenButton right ui-button" href="login.jsp">Request an Account</a>
                    </div>
                    <h2>Welcome to T&#8209;PEN</h2>
                    <div class="lists">
                    <div id="welcome" class="column">
                        <h3>A New Tool for the Digital Humanities</h3>
                        <p>T&#8209;PEN is a web-based tool for working with images of manuscripts. Users attach transcription data (new or uploaded) to the actual lines of the original manuscript in a simple, flexible interface. </p>
                        <h3>Interoperability</h3>
                        <ul>T&#8209;PEN...
                            <li>Is an open and general tool for scholars of <span class="loud" title="Choose to use XML coding assistance or just start typing">any technical expertise level</span></li> 
                            <li>Allows transcriptions to be <span class="loud" title="Use an expanding set of tools or load your own">created, manipulated, and viewed</span> in many ways</li>
                            <li><span class="loud" title="Create teams of scholars to divide up a complex project">Collaborate</span> with others through simple project management</li>
                            <li><span class="loud" title="Take your data with you or let us serve it up as needed">Exports</span> transcriptions as a pdf, XML(plaintext) for further processing, or contribute to a collaborating institution with a click</li>
                            <li>Respects <span class="loud" title="A simple data structure allows for exchange without a hassle">existing and emerging standards</span> for text, image, and annotation data storage</li>
                            <li>Avoids prejudice in data, allowing users to <span class="loud" title="Record only annotations on a map image or use the line detection to make translations of poetry more meaningful . . .">find new ways</span> to work</li>
                        </ul>
                    </div>
                    <div class="column">
                        <p><em>Watch a video of real transcribing in action (11 minutes):</em><br/>
                            <iframe src="http://www.youtube.com/embed/_81fJbOpTcE" frameborder="0" allowfullscreen></iframe>
                        </p>
<!--                        <p><em>Learn more about transcribing in this five minute tour <span class="quiet small">(please note this is for version 0.4, current is <%out.print(Folio.getRbTok("VERSION"));%>)</span>:</em><br/>
                            <iframe src="http://www.youtube.com/embed/sOnJtWtCFZc" frameborder="0" allowfullscreen></iframe>
                        </p>-->
                    </div>
                    </div>
                </div>
<%                        
                }%>
            </div>
            <div id="footer2">
                <%
                if (thisUser != null){
                %>
                <a href="admin.jsp">Account Management</a>
                <a href="admin.jsp?selecTab=2">About T-PEN</a>
                <a href="admin.jsp?selecTab=2">Contact Us</a>
                <%} else {%>
                <a href="about.jsp">About T-PEN</a>
                <a href="about.jsp">Contact Us</a>
                <%}%>
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
                <script type="text/javascript">
                    function scrubListings (){
                        $("#listings").ajaxStop(function(){
                            $("#listings a[href *= 'MStoProject']").hide();
                            $("#countListings").html($("#count").html());
                        });
                    }
                </script>
                <div class="ui-state-error-text left" style="width:100%;"><a href="#" onclick="$('#closePopup').click();">Log in</a> to start a project.</div>
                <%  } else {%>
                <script type="text/javascript">
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
                <script type="text/javascript">
if ( $.browser.msie ) {
    $("html").addClass("IE");
        document.write('<div id="IEflag" class="ui-state-error">T&#8209;PEN has been optimized in webkit and gecko (Chrome, Firefox, Safari, Camino, etc.). <br/><strong>Old versions of Internet Explorer and many mobile browsers are not supported</strong>. <br/>To take advantage of all the tools on T&#8209;PEN, use the latest version of a supported browser.<br/><input onclick="$(this).parent().slideUp();" class="ui-button tpenButton" value="Thanks, got it." /></div>');
}
                </script>
<%@include file="WEB-INF/includes/noscript.jspf" %>
    </body>
</html>
