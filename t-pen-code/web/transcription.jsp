<%@page import="com.hp.hpl.jena.sparql.function.library.substr"%>
<%@page import="textdisplay.Archive"%>
<%@page import="servlets.annotation"%>
<%@page import="user.*"%>
<%@page import="net.sf.saxon.functions.Collection"%>
<%@page import="utils.Tool"%>
<%@page import="utils.UserTool"%>
<%@page import="org.apache.commons.lang.StringEscapeUtils"%>
<%@page import="java.text.SimpleDateFormat"%>
<%@page import="java.text.DateFormat"%>
<%@page import="java.util.*"%>
<%@page import="com.hp.hpl.jena.util.cache.Cache"%>
<%@page import="textdisplay.ArchivedTranscription"%>
<%@page import="org.owasp.esapi.tags.EncodeForHTMLTag"%>
<%@page import="textdisplay.AbbreviationPage"%>
<%@page import ="textdisplay.Transcription" %>
<%@page import ="textdisplay.Folio"  %>
<%@page import ="textdisplay.Line" %>
<%@page import ="textdisplay.*" %>
<%@page import ="textdisplay.TagButton" %>
<%@page import="org.owasp.esapi.ESAPI" %>

<%@page contentType="text/html; charset=UTF-8"  %>
<%

//You have to be logged in to transcribe
    int UID = 0;
    if (session.getAttribute("UID") == null) {
%><%@ include file="loginCheck.jsp" %><%                
    } else {
        UID = Integer.parseInt(session.getAttribute("UID").toString());
%>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN"
    "http://www.w3.org/TR/html4/strict.dtd">
<html>
    <head>
        <%
            int projectID = 0;
            String projectAppend = "";
            textdisplay.Project thisProject = null;
            if (request.getParameter("projectID") != null) {
                projectID = Integer.parseInt(request.getParameter("projectID"));
                thisProject = new textdisplay.Project(projectID);
            } else {
                //gotta find the project
                if (UID > 0 && request.getParameter("ms")!=null) {
                    textdisplay.Manuscript mss=new textdisplay.Manuscript(Integer.parseInt(request.getParameter("ms")),true);
                    int [] msIDs=new int[0];
                    User u = new User(UID);
                    textdisplay.Project[] p = u.getUserProjects();
                    msIDs = new int[p.length];
                    for (int i = 0; i < p.length; i++) {
                        try {
                            msIDs[i] = new textdisplay.Manuscript(p[i].firstPage()).getID();
                        } catch (Exception e) {
                            msIDs[i] = -1;
                        }
                    }
                    for (int l = 0; l < msIDs.length; l++) {
                        if (msIDs[l] == mss.getID()) {
                            projectID=p[l].getProjectID();
                            thisProject=p[l];
                        }
                    }
                    if(projectID<1) {
                        //create a project for them
                        String tmpProjName = mss.getShelfMark()+" project";
                        if (request.getParameter("title") != null) {
                            tmpProjName = request.getParameter("title");
                        }
                        user.Group newgroup = new user.Group(tmpProjName, UID);
                        textdisplay.Project newProject = new textdisplay.Project(tmpProjName, newgroup.getGroupID());
                        newProject.setFolios(mss.getFolios(), newProject.getProjectID());
                        newProject.addLogEntry("<span class='log_manuscript'></span>Added manuscript " + mss.getShelfMark(), UID);
                        thisProject=newProject;
                        projectID=thisProject.getProjectID();
                        newProject.importData(UID);
                    }
                }    
            }
        %>
        <title>TPEN Transcription</title>
        <link rel="stylesheet" href="css/tpen.css" type="text/css" media="screen, projection">
        <link rel="stylesheet" href="css/print.css" type="text/css" media="print">
        <!--[if lt IE 8]><link rel="stylesheet" href="css/ie.css" type="text/css" media="screen, projection"><![endif]-->
        <link type="text/css" href="css/custom-theme/jQuery.css" rel="Stylesheet" />
        <link  href="http://fonts.googleapis.com/css?family=Nova+Cut:regular&v1" rel="stylesheet" type="text/css" >
        <script type="text/javascript" src="https://ajax.googleapis.com/ajax/libs/jquery/1.7.1/jquery.js"></script>
        <script type="text/javascript" src="https://ajax.googleapis.com/ajax/libs/jqueryui/1.8.16/jquery-ui.js"></script> 
        <script type="text/javascript" src="js/tpen.js"></script>
        <script type="text/javascript" src="js/transcription.js"></script>
        
<!--<link rel="stylesheet" type="text/css" href="jwysiwyg/jquery.wysiwyg.css" />
<script type="text/javascript" src="jwysiwyg/jquery.wysiwyg.js"></script>        -->
        
        <style type="text/css">
            body,#wrapper{height: 100%;width:100%;opacity:1;}
            #parsingLoader {z-index: 50;visibility: visible !important;position: fixed; width: 100%;height:100%;background-color: #69ACC9;top:33%;}
            #parsingLoader img {position: absolute;top:0;left:0;bottom:0;right:0;margin:auto;}
            #imgTop, #imgBottom {display:block; height:35%; width:100%;overflow:hidden;z-index: 1;position: relative;}
            #imgBottom {z-index: 2;height:100%;}
            #imgTopImg, #imgBottom img {display:block;position:absolute;width:100%;left: 0px; top: 0px;z-index: inherit;}
            #workspace {position:relative; width:100%;z-index: 3;display: block;padding:12px 4px;
            box-shadow: 0 8px 5px -5px black, 0 -8px 5px -5px black;
            background: url(images/linen.png);
            background: -moz-linear-gradient(top,  rgba(0,0,0,1) 0%, rgba(255,255,255,0.4) 10%, rgba(255,255,255,0.4) 50%, rgba(255,255,255,0.4) 90%, rgba(0,0,0,1) 100%),url(images/linen.png); /* FF3.6+ */
            background: -webkit-gradient(linear, left top, left bottom, color-stop(0%,rgba(0,0,0,1)), color-stop(10%,rgba(255,255,255,0.4)), color-stop(50%,rgba(255,255,255,0.4)), color-stop(90%,rgba(255,255,255,0.4)), color-stop(100%,rgba(0,0,0,1))),url(images/linen.png); /* Chrome,Safari4+ */
            background: -webkit-linear-gradient(top,  rgba(0,0,0,1) 0%,rgba(255,255,255,0.4) 10%,rgba(255,255,255,0.4) 50%,rgba(255,255,255,0.4) 90%,rgba(0,0,0,1) 100%),url(images/linen.png); /* Chrome10+,Safari5.1+ */
            background: -o-linear-gradient(top,  rgba(0,0,0,1) 0%,rgba(255,255,255,0.4) 10%,rgba(255,255,255,0.4) 50%,rgba(255,255,255,0.4) 90%,rgba(0,0,0,1) 100%),url(images/linen.png); /* Opera 11.10+ */
            background: -ms-linear-gradient(top,  rgba(0,0,0,1) 0%,rgba(255,255,255,0.4) 10%,rgba(255,255,255,0.4) 50%,rgba(255,255,255,0.4) 90%,rgba(0,0,0,1) 100%),url(images/linen.png); /* IE10+ */
            background: linear-gradient(top,  rgba(0,0,0,1) 0%,rgba(255,255,255,0.4) 10%,rgba(255,255,255,0.4) 50%,rgba(255,255,255,0.4) 90%,rgba(0,0,0,1) 100%),url(images/linen.png); /* W3C */
            filter: progid:DXImageTransform.Microsoft.gradient( startColorstr='#FFFFFF', endColorstr='#FFFFFF',GradientType=0 ); /* IE6-9 */
            }       
            #workspace {
              background: white url(images/linen.png);
              box-shadow: 0 6px 32px 4px black;
            }
            #wrapper {position:relative;float:left;z-index: 3;}
            #fullscreenBtn {cursor:pointer;display: none;position:absolute;z-index: 2;height:100%;width:16px;right:0;top:0;
                -moz-box-shadow:4px 0 10px 0px black;
                -webkit-box-shadow:4px 0 10px 0px black;
                box-shadow:4px 0 10px 0px black;
                background: rgb(171,183,188); /* Old browsers */
                background: -moz-linear-gradient(left,  rgba(171,183,188,1) 0%, rgba(232,233,234,1) 100%); /* FF3.6+ */
                background: -webkit-gradient(linear, left top, right top, color-stop(0%,rgba(171,183,188,1)), color-stop(100%,rgba(232,233,234,1))); /* Chrome,Safari4+ */
                background: -webkit-linear-gradient(left,  rgba(171,183,188,1) 0%,rgba(232,233,234,1) 100%); /* Chrome10+,Safari5.1+ */
                background: -o-linear-gradient(left,  rgba(171,183,188,1) 0%,rgba(232,233,234,1) 100%); /* Opera 11.10+ */
                background: -ms-linear-gradient(left,  rgba(171,183,188,1) 0%,rgba(232,233,234,1) 100%); /* IE10+ */
                background: linear-gradient(left,  rgba(171,183,188,1) 0%,rgba(232,233,234,1) 100%); /* W3C */
                filter: progid:DXImageTransform.Microsoft.gradient( startColorstr='#abb7bc', endColorstr='#e8e9ea',GradientType=1 ); /* IE6-9 */
                -moz-transition: all .5s; /* Firefox 4 */
                -webkit-transition: all .5s; /* Safari and Chrome */
                -o-transition: all .5s; /* Opera */             
                transition: all .5s;
            }
            #fullscreenBtn:hover{
                -moz-box-shadow:6px 0 8px 2px black;
                -webkit-box-shadow:6px 0 8px 2px black;
                box-shadow:6px 0 8px 2px black;
                background: rgb(200,215,220); /* Old browsers */
                background: -moz-linear-gradient(left,  rgba(200,215,220,1) 0%, rgba(227,234,237,1) 63%, rgba(242,245,246,1) 100%); /* FF3.6+ */
                background: -webkit-gradient(linear, left top, right top, color-stop(0%,rgba(200,215,220,1)), color-stop(63%,rgba(227,234,237,1)), color-stop(100%,rgba(242,245,246,1))); /* Chrome,Safari4+ */
                background: -webkit-linear-gradient(left,  rgba(200,215,220,1) 0%,rgba(227,234,237,1) 63%,rgba(242,245,246,1) 100%); /* Chrome10+,Safari5.1+ */
                background: -o-linear-gradient(left,  rgba(200,215,220,1) 0%,rgba(227,234,237,1) 63%,rgba(242,245,246,1) 100%); /* Opera 11.10+ */
                background: -ms-linear-gradient(left,  rgba(200,215,220,1) 0%,rgba(227,234,237,1) 63%,rgba(242,245,246,1) 100%); /* IE10+ */
                background: linear-gradient(left,  rgba(200,215,220,1) 0%,rgba(227,234,237,1) 63%,rgba(242,245,246,1) 100%); /* W3C */
                filter: progid:DXImageTransform.Microsoft.gradient( startColorstr='#c8d7dc', endColorstr='#f2f5f6',GradientType=1 ); /* IE6-9 */
            }
            #fullscreenBtn span {display: block;float: left;clear: left;}
            #tools {float: left;width:0%;position: relative;z-index: 1;}
            #tools > div {width:100%;height:100%;}
            #lineInfo,#annotationInstructions {font-size: 14px;line-height: 16px;position:fixed;margin:3px;padding:3px;
                -moz-box-shadow:-4px -4px 8px -4px black;
                -webkit-box-shadow:-4px -4px 8px -4px black;
                box-shadow:-4px -4px 8px -4px black;
                z-index: 3;
                bottom:18px;left:20px;font-family: sans-serif;max-height:30%;overflow: hidden;
                background: rgb(152,190,222);
                background: -moz-linear-gradient(top,  rgba(222,239,255,0.5) 0%, rgba(152,190,222,1) 100%); /* FF3.6+ */
                background: -webkit-gradient(linear, left top, left bottom, color-stop(0%,rgba(222,239,255,0.5)), color-stop(100%,rgba(152,190,222,1))); /* Chrome,Safari4+ */
                background: -webkit-linear-gradient(top,  rgba(222,239,255,0.5) 0%,rgba(152,190,222,1) 100%); /* Chrome10+,Safari5.1+ */
                background: -o-linear-gradient(top,  rgba(222,239,255,0.5) 0%,rgba(152,190,222,1) 100%); /* Opera 11.10+ */
                background: -ms-linear-gradient(top,  rgba(222,239,255,0.5) 0%,rgba(152,190,222,1) 100%); /* IE10+ */
                background: linear-gradient(top,  rgba(222,239,255,0.5) 0%,rgba(152,190,222,1) 100%); /* W3C */
                filter: progid:DXImageTransform.Microsoft.gradient( startColorstr='#80deefff', endColorstr='#bf98bede',GradientType=0 ); /* IE6-9 */
                }
            #annotationInstructions{bottom: auto;top:18px;}
            #lineInfo div,#annotationInstructions span{color:#394753;text-shadow:0 1px 0 #ccdfef;}
            .line {
                position:absolute;
                background-color: white;
                border-style:solid;
                border-width:1px;
                border-color: black;
                z-index          : 3 ;
                filter:alpha(opacity=0);-moz-opacity:0;-khtml-opacity: 0;opacity: 0;
            }
            .jumpLine {background-color: goldenrod !important;
                overflow:visible;
                color:#618797;
                filter:alpha(opacity=50);-moz-opacity:0.5;-khtml-opacity: 0.5;opacity: 0.3;
            }
            #buttonList {position: relative; top: 0; height: auto; margin:0 auto; right:0; left:0;padding:0 30px 18px;z-index: 5;}
/*            #previous {position:relative;width: 100%;z-index: 3;overflow: visible;display: block;background-color: white;padding-top: 8px;}*/
            #texts, #notes {display: block;font-family: "Cambria","Georgia", serif; text-align: center;font-size:16px;text-overflow: ellipsis;width:100%;position:relative;white-space: nowrap;overflow: hidden;}
            #previous img {position:absolute;top:-29px;z-index: 4;height:49px;left:0;}
            #captions {position:relative; min-height: 2em;}
            #following img {position:absolute;bottom:-62px;height:82px;left:0;}
/*            #following {position:relative;width: 100%;overflow: visible;display: block; background-color: white;}*/
            #entry {margin:0; position:relative;width:auto;}
            .transcriptlet{display:block;position: relative;width:auto;margin:0 12.5%; opacity:1;z-index: 3;left:0px;top:0;visibility: visible;}
            .transcriptletBefore {top:-60px;opacity:0;position:absolute;visibility: hidden;}
            .transcriptletAfter {top:100px;opacity:0;position:absolute;visibility: hidden;}
            .counter,.addNotes,.previousLine,.nextLine {position: absolute;width:150px;cursor: pointer;min-height:18px;}
            .counter,.previousLine{left:-105px;z-index:2;text-align: left;}
            .nextLine,.addNotes{text-align: right;right: -105px;}
            .counter{top:30px; cursor: default !important;}
            .addNotes{top: 30px;}
            .previousLine,.nextLine{top:4px;}
/*            .lineNav {position:relative;width:100%;clear:both;}
            .lineNav a {top:5px; text-decoration: none;}*/
            /*.notes{height:30px !important;width:100%;}*/
            .previousLine:hover {left:-120px !important;}
            .previousLine.shrink:hover {left:-30px !important;}
            .addNotes:hover,.nextLine:hover{right:-120px !important;}
            .addNotes.shrink:hover,.nextLine.shrink:hover {right:-48px !important;;}
            .theText,.notes {z-index: 5;width:100%;margin:0 auto;left:0; right:0;max-height: 12em;position:relative;font-family: "Cambria","Georgia", serif !important;}
            .theText { min-height: 60px;}
            .notes { min-height: 2.5em;height:2.5em;}
            #options {padding:2px;width:auto;min-width: 50px;margin:0 auto;max-height:200px;overflow: hidden;}
            #options a, #options input, #tools a,#tools .exitPage,#annotation_toolbar button {padding:1px;min-width: 60px;min-height: 18px;margin:0 -2px;font-size: 12px;line-height: 18px;overflow:hidden;}
            #options > div {padding:2px 5px;margin:0 auto;}
            #popin {clear:both;overflow:visible;padding:2px;width:auto;position:relative; border:none !important;}
            #popin div {display: inline-block;}
            #bookmark {z-index: 2;position:absolute;left:-600%;height:0%;border:thin solid #A64129;opacity:1 !important;
                       -moz-box-shadow: 0 0 15px black;
                       -webkit-box-shadow: 0 0 15px black; 
                       box-shadow:0 0 15px black;}
            #bookmarkText {display: none; position: absolute; bottom:-30px;color:#A64129;font-size:24px; right:10px;text-shadow: 0 0 4px black;}/*TODO Hidden for now, can remove later.*/
            .toolLinks {text-align: right;z-index: 4;width:100%;}
            [id$='Split'],[id^='split'] {position:relative; z-index: -3; display: none; padding:0px;}
            #parsingSplit {max-width:600px; min-width: 350px;position: relative;z-index: 9;width:100%;}
            #historySplit {width: auto;}
            [id^='frameSplitDiv']{text-align:right;width:100%;}
            #latinSplit,#vulgateSplit{text-align:right;padding-right: 20px;padding-top:5px;}
            #vulgateDiv {position:absolute;left: 20px} /*shift from under the main window*/
            #abbrevSplit {width:30%;}
            #abbrevImg {position:absolute;right:0;left:0;margin:0 auto;height: 100%;width:auto;}
            #abbreviations {position:relative; top:0;left:0;margin:5px 30px;overflow: visible;z-index: 15;display: inline-block;max-width: 200px;width:100%;
                            -moz-box-shadow: 0 0 5px #A64129;
                            -webkit-box-shadow: 0 0 5px #A64129; 
                            box-shadow:0 0 5px #A64129;}
            #abbreviations select {width:100%;}      
            /* i-frame control */
            #vulgateDiv {display: block; overflow: hidden;right:-20px;top:30px;width:480px !important;height:100%;}
            #vulgateSplit a,#latinSplit a {left:20px;position: relative; text-decoration: none;}
            #vulgateDiv iframe,#latinSplit iframe {background: url('images/linen.png') white scroll repeat; position: relative;}
            iframe {background: url('images/linen.png') white scroll repeat; position: relative;width: 100%;height:100%;}
            #vulgateDiv iframe {left:-181px;top:-40px;width:660px !important;height:100%;}
            #latinSplit iframe {width:743px;padding:5px 0; height: 128%;
                                -moz-transform: scale(0.7, 0.7) translate(-10%,-20%); 
                                -webkit-transform: scale(0.7, 0.7) translate(-10%,-20%); 
                                -o-transform: scale(0.7, 0.7) translate(-10%,-20%); 
                                transform: scale(0.7, 0.7) translate(-10%,-20%); 
            }
            .xmlClosingTags {position: absolute; bottom:0;right:10px;z-index: 20;}
            .tags {font-size: 80%; padding: 1px;margin: 0px -1px 1px 0px;display: inline-block;z-index: 20;cursor: pointer;position: relative;}
            .destroyTag {position:absolute;top:-15px;right:0;}
            .loadingStatus {position:absolute;color: white; top:25%;left:30%;font-family: 'Nova Cut',sans-serif;font-size: 24px;text-shadow:0px 1px 0 #69ACC9;}
            #loadingCompare {position:relative;}
            .previewPage {border:thin solid black; margin-bottom: 5px;clear: both; padding: 10px;overflow: auto; background: white;
                            -moz-box-shadow: -1px -1px 3px rgba(0,0,0,.4);
                            -webkit-box-shadow: -1px -1px 3px rgba(0,0,0,.4); 
                            box-shadow:-1px -1px 3px rgba(0,0,0,.4);
            }
            .previewFolioNumber {float: left;font-weight: bold; display: block;padding-left: 20px;}
            /*.previewAnnotations {float: left;width:100%;background: wheat;}*/
/*            .previewAnnoText {display: block;}*/
            .previewLine{float: left;clear:left;display:block;width:100%;}
            .previewLine:nth-child(even) { background-color: whitesmoke; }
            .previewLine:nth-child(odd) { background-color: lightgray; }
            .previewLineNumber {font-style: italic;float: left;width:10%;color: #707070;}
            .previewText {font-style: normal;float: left;width: 90%;min-height: 14px;}
            .previewLinebreak {float: left;color:gray;}
            .previewNotes {float: left; display: none; color:#4863A0;width: 90%;margin-left: 10%;}
            #previewDiv{position:relative;overflow: auto;clear:both;padding: 0 2px 5px 5px;}
            #previewDiv span{position: relative;}
            #previewSplit {overflow: hidden;height:100%;}
            .previewSave {position: absolute; right:0;font-size: 80%;cursor: pointer;z-index: 4;}
            .previewTag {color:#488AC7;display:inline-block;}
            #saveReport,#contribution {position: absolute; z-index: 4;width:18em;cursor: pointer;bottom:0;right:0;color:#226683;text-transform: capitalize;padding: 1px;margin: 0px;max-height:18px;overflow: hidden;}
            #contribution {right:auto;left:0;}
            #contribution:before {content:'Last edit by: '} 
            .saveLog:first-child {color:#618797;}
            #saveReport:hover {box-shadow: -4px -4px 8px -4px #666666;max-height: 95px;overflow:auto;
                /* IE10 */ 
                background-image: -ms-linear-gradient(top, whitesmoke 25%, rgba(255,255,255,0) 100%);
                /* Mozilla Firefox */ 
                background-image: -moz-linear-gradient(top, whitesmoke 25%, rgba(255,255,255,0) 100%);
                /* Opera */ 
                background-image: -o-linear-gradient(top, whitesmoke 25%, rgba(255,255,255,0) 100%);
                /* Webkit (Safari/Chrome 10) */ 
                background-image: -webkit-gradient(linear, left top, left bottom, color-stop(.25, whitesmoke), color-stop(1, rgba(255,255,255,0)));
                /* Webkit (Chrome 11+) */ 
                background-image: -webkit-linear-gradient(top, whitesmoke 25%, rgba(255,255,255,0) 100%);
                /* Proposed W3C Markup */ 
                background-image: linear-gradient(top, whitesmoke 25%, rgba(255,255,255,0) 100%);
            }
            #clickDivs {position: relative;height:auto;clear:both;}
            .parsing,.adjustable {z-index: 5;opacity:.3;position: absolute;border:solid 1px black;}
            .parsing:nth-child(even){background-color: goldenrod;}
            .parsing:nth-child(odd){background-color: lightgoldenrodyellow;}
            .deletable {background-color: red !important;cursor:url("css/custom-theme/images/deleteDiv.png"),url("css/custom-theme/images/deleteDiv.cur"),auto !important;}
            .mergeable {background-color: blue !important;}
            .adjustable {background-color: white !important;cursor: default;border-right: solid black 4px;}
/*            #toggler {display: none; z-index: 6;position: fixed;right:5px;top:45px;width: auto;}*/
            #imageTip {text-align: center;overflow: visible;display: none;z-index: 6;position:absolute;background-color:#A68329;height: 18px;width:auto; max-width: 100px;color:#FFDB7F;box-shadow:2px 2px 4px black;}
            #ruler1,#ruler2 {display: none;background: black;position: absolute;z-index: 4;}
            #sampleRuler {overflow: hidden;position:relative;background:black;width:80%;margin:0 auto;height:2px;top:-10px;}
            #savedChanges {display: none; opacity:0;position: fixed;right:20px;min-width:150px;top:100%;padding: 4px;text-align: center;z-index: 50;}
            #parsingDiv, #linebreakDiv {height:100%; padding: 4px;overflow: auto;width:auto;}
            #parseOptions > span, #linebreakOptions > span, #columnOptions > span, #allColumnOptions > span, #destroyPage, #reparseColumn {width:100%;height:38px;float:left;padding-top:8px;z-index: 2;}
            #columnOptions > span, #allColumnOptions > span {width: 50%;}
            .parsingColumn {background-image: url('css/custom-theme/images/ui-bg_layered-circles_50_618797_13x13.png');background-attachment: scroll;background-repeat: repeat;position: absolute;border-style: solid; border-width: 2px;border-color: black;z-index: 3;opacity:.3;}
            .parsingColumnSelected {background-image: url('css/custom-theme/images/ui-bg_layered-circles_50_ffc6b9_13x13.png') !important;border-color: red !important;opacity:.5 !important;cursor:url("css/custom-theme/images/deleteDiv.png"),url("css/custom-theme/images/deleteDiv.cur"),auto !important;} /* Visual selection for deleting columns */
            .actions > div {position:relative;z-index: 10;margin: 4px;}
            .actions{
                margin:10px 6px;background: url(images/linen.png);padding:6px 2px 2px;overflow: visible;z-index: 1; border: 1px solid #A68329;
                -moz-box-shadow: -1px 1px 2px black;
                -webkit-box-shadow: -1px 1px 2px black; 
                box-shadow:-1px 1px 2px black;}
            label {width:auto;padding: 0;float: none;}/*fixme should not have to overwrite*/
            #linebreakText {clear:left;width:auto;max-height:120px;height:40%;overflow: auto;background: white fixed repeat; border: 2px inset #FFDB7F;margin-bottom:4px;position:relative;}
            #linebreakSplit {width: 350px;}
            #linebreakString{max-width: 4em;width:auto;}
            .linebreaking {display: block;clear: both;}
            #newColumnCount{position: fixed;text-align: center;left:0;top:0;font-size: 18px;width: 80px;padding:10px;opacity:.7;z-index: 4;}
            #newColumnCount span{font-size: 32px;}
            #createColumnInst .buttons {max-width:246px;margin:0 auto;}
            #createColumnInst .buttons button {min-width: 120px;}
            #iprAccept,#requestAccess {position: fixed;left:0;right:0;top:10%;margin:auto;width:90%;max-width: 600px;max-height: 400px;overflow: hidden;padding: 5px;z-index: 50;
                -moz-box-shadow:0 0 45px black;
                box-shadow:0 0 45px black;}
            #requestAccess textarea {width:100%;height:5em;}
            #requestAccess a.returnButton {position: absolute;bottom: -36px;}
            #iprAgreement {background-color: whitesmoke;padding: 3px;overflow: auto; max-height: 200px;border: thin inset black;}
            #iprAgreement h1,#iprAgreement h2,#iprAgreement h3,#iprAgreement h4,#iprAgreement h5,#iprAgreement h6{
                text-shadow: none; 
                color: #226683;
                font-family: Tahoma, Geneva, sans-serif;
            }
            #lineResizing,#progress{position: fixed;right:10px;z-index: 500;padding: 15px;display: none;
                -moz-box-shadow: 0 0 15px black;
                box-shadow: 0 0 15px black;
            }
            #progress {bottom:0;right:0;}
            #lineResizing {top:50%;width:auto;max-width: 25%;}
            #zoomDiv {display: none;border-radius : 50%; box-shadow :5px 5px 15px 3px black,15px 15px 100px rgba(230,255,255,.8) inset,-15px -15px 100px rgba(0,0,15,.4) inset;width : 300;height:300;border: 3px gold outset;overflow : hidden;position:fixed;left: 450px;top: 65px;background-color: transparent;background-position: 0px 0px;background-size: auto;background-repeat:no-repeat;z-index: 2;}
            #historyListing {height: 100%;overflow: auto;position:relative;}
            .historyEntry,.historyLine{position: relative;overflow: auto;}
            .historyLine{height:100%;}
            .historyEntry {border-width: 0px; padding:5px 0;margin:-5px 0; }
            .historyEntry:hover {box-shadow:rgba(166,65,41,0.7) 0 0 10px,rgba(166,65,41,0.7) 0 0 10px inset;}
            .historyNote,.historyText{position: relative;overflow: auto;padding:3px;background:whitesmoke;margin: 0 10px;clear: left;max-height: 5em;}
            .historyNote {display: none;}
            .historyCreator,.historyDate{position: relative;overflow: hidden;margin: 0 10px;float: left;}
            .historyOptions {position: absolute;right:12px; top:20px;z-index: 10;display: none;}
            #historyViewer {height: 200px;overflow: hidden;position: relative;}
            #historyViewer img {position:absolute;height:auto;width:100%;}
            .historyBookmark {position:absolute;box-shadow:0 0 30px 5px black;border:thin blue solid;z-index: 1;}
            .historyRevert {margin: 0 4px;}
            .previewText:focus,.previewNotes:focus {box-shadow:0 0 5px #A64129;}
            .isUnsaved textarea {box-shadow: 0 0 10px #A64129;}
            body {overflow: hidden;position: fixed;}
            #help {z-index: 40;position: absolute;display: none;left:100%;overflow: hidden;width: 100%; color: rgba(40,40,40,1);-webkit-transition:left 2s;-moz-transition:left 2s;transition:left 2s;font-family: "Palatino Linotype", "Book Antiqua", "Palatino", serif;
                -webkit-box-sizing:border-box;
                -moz-box-sizing: border-box;
                -ms-box-sizing: border-box;
                box-sizing: border-box;
            }
            #help iframe {width:auto;height:auto;}
            #helpPanels {position: relative;width:3000px;overflow: hidden;-webkit-transition:margin 2s;-moz-transition:margin 2s;transition:margin 2s;
                background: rgb(255,255,255); /* Old browsers */
                background: -moz-radial-gradient(center, ellipse cover,  rgba(255,255,255,1) 0%, rgba(204,204,204,1) 100%); /* FF3.6+ */
                background: -webkit-gradient(radial, center center, 0px, center center, 100%, color-stop(0%,rgba(255,255,255,1)), color-stop(100%,rgba(204,204,204,1))); /* Chrome,Safari4+ */
                background: -webkit-radial-gradient(center, ellipse cover,  rgba(255,255,255,1) 0%,rgba(204,204,204,1) 100%); /* Chrome10+,Safari5.1+ */
                background: -o-radial-gradient(center, ellipse cover,  rgba(255,255,255,1) 0%,rgba(204,204,204,1) 100%); /* Opera 12+ */
                background: -ms-radial-gradient(center, ellipse cover,  rgba(255,255,255,1) 0%,rgba(204,204,204,1) 100%); /* IE10+ */
                background: radial-gradient(center, ellipse cover,  rgba(255,255,255,1) 0%,rgba(204,204,204,1) 100%); /* W3C */
                filter: progid:DXImageTransform.Microsoft.gradient( startColorstr='#ffffff', endColorstr='#cccccc',GradientType=1 ); /* IE6-9 fallback on horizontal gradient */
}
            #helpContents {position: relative;width: 100%;cursor: pointer; padding: 5px 41%;
                background: rgb(76,76,76); /* Old browsers */
                background: -moz-linear-gradient(top,  rgba(76,76,76,1) 0%, rgba(89,89,89,1) 12%, rgba(102,102,102,1) 25%, rgba(71,71,71,1) 39%, rgba(44,44,44,1) 50%, rgba(0,0,0,1) 51%, rgba(17,17,17,1) 60%, rgba(43,43,43,1) 76%, rgba(28,28,28,1) 91%, rgba(19,19,19,1) 100%); /* FF3.6+ */
                background: -webkit-gradient(linear, left top, left bottom, color-stop(0%,rgba(76,76,76,1)), color-stop(12%,rgba(89,89,89,1)), color-stop(25%,rgba(102,102,102,1)), color-stop(39%,rgba(71,71,71,1)), color-stop(50%,rgba(44,44,44,1)), color-stop(51%,rgba(0,0,0,1)), color-stop(60%,rgba(17,17,17,1)), color-stop(76%,rgba(43,43,43,1)), color-stop(91%,rgba(28,28,28,1)), color-stop(100%,rgba(19,19,19,1))); /* Chrome,Safari4+ */
                background: -webkit-linear-gradient(top,  rgba(76,76,76,1) 0%,rgba(89,89,89,1) 12%,rgba(102,102,102,1) 25%,rgba(71,71,71,1) 39%,rgba(44,44,44,1) 50%,rgba(0,0,0,1) 51%,rgba(17,17,17,1) 60%,rgba(43,43,43,1) 76%,rgba(28,28,28,1) 91%,rgba(19,19,19,1) 100%); /* Chrome10+,Safari5.1+ */
                background: -o-linear-gradient(top,  rgba(76,76,76,1) 0%,rgba(89,89,89,1) 12%,rgba(102,102,102,1) 25%,rgba(71,71,71,1) 39%,rgba(44,44,44,1) 50%,rgba(0,0,0,1) 51%,rgba(17,17,17,1) 60%,rgba(43,43,43,1) 76%,rgba(28,28,28,1) 91%,rgba(19,19,19,1) 100%); /* Opera 11.10+ */
                background: -ms-linear-gradient(top,  rgba(76,76,76,1) 0%,rgba(89,89,89,1) 12%,rgba(102,102,102,1) 25%,rgba(71,71,71,1) 39%,rgba(44,44,44,1) 50%,rgba(0,0,0,1) 51%,rgba(17,17,17,1) 60%,rgba(43,43,43,1) 76%,rgba(28,28,28,1) 91%,rgba(19,19,19,1) 100%); /* IE10+ */
                background: linear-gradient(top,  rgba(76,76,76,1) 0%,rgba(89,89,89,1) 12%,rgba(102,102,102,1) 25%,rgba(71,71,71,1) 39%,rgba(44,44,44,1) 50%,rgba(0,0,0,1) 51%,rgba(17,17,17,1) 60%,rgba(43,43,43,1) 76%,rgba(28,28,28,1) 91%,rgba(19,19,19,1) 100%); /* W3C */
                filter: progid:DXImageTransform.Microsoft.gradient( startColorstr='#4c4c4c', endColorstr='#131313',GradientType=0 ); /* IE6-9 */
}
            .helpContents {background: aliceblue;display: inline-block;height: 12px;width:12px;margin: 0 6px;border-radius:50%;}
            .helpContents:hover {box-shadow:0 0 4px orangered inset;}
            .helpActive {box-shadow:0 0 6px 2px orangered inset !important;}
            .helpContents:hover:after{
                content: attr(title);
                left:144px;
                color:silver;
                text-shadow:0 0 2px orangered;
                position:relative;
                white-space: nowrap;
                font-family:monospace;
                font-size:14px;
                top:-3px;
            }
            .helpContents:nth-child(2):hover:after{
                left:119px
            }
            .helpContents:nth-child(3):hover:after{
                left:94px
            }
            .helpContents:nth-child(4):hover:after{
                left:69px
            }
            .helpContents:nth-child(5):hover:after{
                left:44px
            }
            .helpPanel {width: 600px;position: relative;max-height: 550px;overflow: auto;float: left;padding:10px;
                        box-sizing:border-box;
/*                        -moz-column-width:400px;-moz-column-gap:20px;-webkit-column-width:400px;-webkit-column-gap:20px;column-width:400px;column-gap:20px;*/
                background: -moz-linear-gradient(left,  rgba(0,0,0,0) 0%, rgba(0,0,0,0.2) 50%, rgba(0,0,0,0) 100%); /* FF3.6+ */
                background: -webkit-gradient(linear, left top, right top, color-stop(0%,rgba(0,0,0,0)), color-stop(50%,rgba(0,0,0,0.2)), color-stop(100%,rgba(0,0,0,0))); /* Chrome,Safari4+ */
                background: -webkit-linear-gradient(left,  rgba(0,0,0,0) 0%,rgba(0,0,0,0.2) 50%,rgba(0,0,0,0) 100%); /* Chrome10+,Safari5.1+ */
                background: -o-linear-gradient(left,  rgba(0,0,0,0) 0%,rgba(0,0,0,0.2) 50%,rgba(0,0,0,0) 100%); /* Opera 11.10+ */
                background: -ms-linear-gradient(left,  rgba(0,0,0,0) 0%,rgba(0,0,0,0.2) 50%,rgba(0,0,0,0) 100%); /* IE10+ */
                background: linear-gradient(left,  rgba(0,0,0,0) 0%,rgba(0,0,0,0.2) 50%,rgba(0,0,0,0) 100%); /* W3C */
                filter: progid:DXImageTransform.Microsoft.gradient( startColorstr='#00000000', endColorstr='#00000000',GradientType=1 ); /* IE6-9 */
}
            .helpPanel h2{
                color: rgba(40,40,40,1);text-shadow:1px 1px 0 rgba(240,240,240,1);}
            #closeHelp {position: absolute;z-index: 1;right:5px;top:5px;}
            #closeHelp:hover {background-image: url(css/custom-theme/images/ui-icons_cd0a0a_256x240.png);}
            #helpMe{cursor: help;}
            .showMe {border: dotted 1px transparent; border-bottom: dotted 1px orangered;cursor: help;display: inline-block;position: relative;}
            .showMe:hover {color: rgb(255,36,0);border: solid 1px rgba(255,36,0,.7);border-bottom: transparent;border-top-left-radius:3px;border-top-right-radius:3px;padding:2px;margin:-2px;}
            .helpDetail{position: absolute;left:0;top:16px;max-width: 350px;border: solid 1px rgba(0,100,0,.7);box-shadow:1px 1px 3px black;overflow: visible;z-index: 1;display: none;background: whitesmoke url(images/linen.png);padding: 5px;text-align: center;font-size: 14px !important;}
            .helpDetail span {margin:4px;}
            .gloss {position: absolute;left:0;top:16px;min-width:325px;max-width: 600px;border: solid 1px rgba(0,100,0,.7);box-shadow:1px 1px 3px black;overflow: visible;z-index: 1;display: none;}
            .gloss dt {font-variant: small-caps;font-size: larger;font-weight: bold;}
            .glossary {position: relative;border: dotted 1px transparent; border-bottom: dotted 1px rgba(0,100,0,.7);cursor: help;display: inline-block;}
            .glossary:hover {color: rgb(0,100,0);border: solid 1px rgba(0,100,0,.7);border-bottom: transparent;border-top-left-radius:3px;border-top-right-radius:3px;padding:2px;margin:-2px;}
            #lbText {white-space: pre;}
/*            #annotations {position: relative;clear: both;}*/
/*            .annotation {position: absolute;min-height: 5px;min-width:5px;box-shadow:0 0 2px 1px black;z-index: 2;}*/
            .newAnno,.ui-resizable-resizing {box-shadow:0 0 4px 2px goldenrod !important;border:none !important;}
/*            .deleteAnno {box-shadow:0 0 2px 2px red !important;}
            .adjustAnno {box-shadow:0 0 4px navy;}
            .adjustAnno:hover {box-shadow:0 0 4px 2px navy;cursor:move;}
            .activeAnnotation {box-shadow:0 0 4px 2px gold;}
            #annotationInfo{position: absolute;bottom:0;width:90%;margin:0 5%;
                display: none;padding: 4px;background-color: #69ACC9;
                box-shadow: -1px -1px 5px black;text-shadow: 0 1px #367996;}
            #annotationInfo textarea {margin:3px;max-height: 3em;overflow: auto;}
            #annotationInfo h6 {color:white;}*/
            .topAdjust {bottom:auto !important;top:25px !important;}
/*            .showAnno {background-color: rgba(255,255,0,.4)}*/
            .shrink {white-space: nowrap;}
            .shrink.wBtn .ui-icon {
              margin: 0 2px;
            }
            .shrink.wBtn {min-width:18px !important;width:26px !important;color: transparent !important;z-index: 7;}
            .wBtn:hover {z-index: 8;}
            .navigation {
              color: white;
              background: rgb(166,65,41); /* Old browsers */
background: -moz-linear-gradient(top,  rgba(166,65,41,1) 0%, rgba(244,150,129,1) 100%); /* FF3.6+ */
background: -webkit-gradient(linear, left top, left bottom, color-stop(0%,rgba(166,65,41,1)), color-stop(100%,rgba(244,150,129,1))); /* Chrome,Safari4+ */
background: -webkit-linear-gradient(top,  rgba(166,65,41,1) 0%,rgba(244,150,129,1) 100%); /* Chrome10+,Safari5.1+ */
background: -o-linear-gradient(top,  rgba(166,65,41,1) 0%,rgba(244,150,129,1) 100%); /* Opera 11.10+ */
background: -ms-linear-gradient(top,  rgba(166,65,41,1) 0%,rgba(244,150,129,1) 100%); /* IE10+ */
background: linear-gradient(to bottom,  rgba(166,65,41,1) 0%,rgba(244,150,129,1) 100%); /* W3C */
filter: progid:DXImageTransform.Microsoft.gradient( startColorstr='#a64129', endColorstr='#f49681',GradientType=0 ); /* IE6-9 */
            }
            .navigation .ui-icon {
background-image: url(css/custom-theme/images/ui-icons_ffffff_256x240.png);            }
            a.navigation:hover .ui-icon {
background-image: url(css/custom-theme/images/ui-icons_a64129_256x240.png); 
            border-color: #a64129;}
            a.navigation {
              border-color: white;
            }
            #siteNavigation {
              position:relative;
              top:0;left:0;
              width:100%;/*width:100vw;*/
/*              background: #ffebb9;
              background: url(css/custom-theme/images/ui-bg_dots-medium_75_ffebb9_4x4.png) 50% 50% repeat, rgba(166,65,41,.5);*/
/*              background: #a64129;*/
              z-index: 5;
/*              color: white;*/
            }
            #siteNavigation,#imgBottom img,#location {
              box-shadow: 0 0 4px black;
            }
            #siteNavigation a {
              border-color: transparent;
              background: none;
              padding:.25em;
              color: white;
              display: inline-block;
            }
            #siteNavigation a:hover {
              background-color: white;
              background-color: rgba(255,255,255,.5);
              border: solid thin;
              color: #a64129;
            }
            #navOptions .wBtn {
              color: #800306;
              background: rgb(166,65,41); /* Old browsers */
background: -moz-linear-gradient(top,  rgba(166,65,41,1) 0%, rgba(244,150,129,1) 100%); /* FF3.6+ */
background: -webkit-gradient(linear, left top, left bottom, color-stop(0%,rgba(166,65,41,1)), color-stop(100%,rgba(244,150,129,1))); /* Chrome,Safari4+ */
background: -webkit-linear-gradient(top,  rgba(166,65,41,1) 0%,rgba(244,150,129,1) 100%); /* Chrome10+,Safari5.1+ */
background: -o-linear-gradient(top,  rgba(166,65,41,1) 0%,rgba(244,150,129,1) 100%); /* Opera 11.10+ */
background: -ms-linear-gradient(top,  rgba(166,65,41,1) 0%,rgba(244,150,129,1) 100%); /* IE10+ */
background: linear-gradient(to bottom,  rgba(166,65,41,1) 0%,rgba(244,150,129,1) 100%); /* W3C */
filter: progid:DXImageTransform.Microsoft.gradient( startColorstr='#a64129', endColorstr='#f49681',GradientType=0 ); /* IE6-9 */
            }
            .wBtn.tools {}
            .shrink.icon {
              padding: 1em;
            }
            .shrink .ui-icon {position: absolute;top:1px;left:1px;}
            .shrink.wLeft {left:-30px;}
            .shrink.wRight .ui-icon {left:auto;right:1px;}
            .shrink.previousLine {color: transparent !important;}
            .shrink.wRight {right:-48px;color: transparent !important;}
            .btnTitle {position: absolute;z-index: 6;top:-1.2em;white-space: nowrap;background: rgba(255,255,255,.75);box-shadow: .1px 1px 2px black;}
            #videoView{height: 50%;width: 50%;top: 25%;left: 25%;position: fixed;min-width: 560px;min-height: 315px;}
            #overlay{display: none;}
            #overlayNote{position: fixed;top:2%;right:2%;white-space: nowrap;font-size: large;font-weight: 700;font-family: monospace;text-shadow:1px 1px 0 white;}
            #fullImg,#compareDiv,#annotationDiv{height:100%;}
            #aLinkFrame {position: absolute;top:0;left:0;z-index: 0;}
            #aShowLink {width:100px;display: inline-block;}
            #aShowLink:after{display: none;} /* break out of clearfix */
/*            #aLink {clear:left;display:none;}  DEBUG until ready for annotation links */
        </style>
        <script type="text/javascript">document.write('<style type="text/css">body {visibility:hidden;}\n#historyViewer,[id^="split"],[id$="Split"] {width:'+Page.width()*.4+'px;}</style>');</script>

<%
    out.println("<script type='text/javascript'>");
            projectAppend = "&projectID="+projectID;
            out.println("var projectID=" + projectID + ";");
            request.setCharacterEncoding("UTF-8");
            int pageno = 501;
            try {
                if (request.getParameter("p") != null) {
                    pageno = Integer.parseInt(request.getParameter("p"));
                    if (pageno < 0) pageno = thisProject.firstPage();
                } else {
                    // @TODO:  ?? If a user starts to transcribe a manuscript that's not in a project, this call throws an uncaught exception.
                    pageno = thisProject.firstPage();
                }
                out.println("var folio=" + pageno + ";");
            } catch (NumberFormatException e) {
                e.printStackTrace();
            }
            String archive; //archive name
            Folio thisFolio = new Folio(pageno, true);
            archive = thisFolio.getArchive();
            //if this page is ipr restricted, make sure the user has accepted those restrictions
            user.User thisUser = new user.User(UID);
            if (request.getParameter("acceptIPR") != null) {
                thisUser.acceptIPR(thisFolio.getFolioNumber());
            }
            textdisplay.Archive thisArchive;
            thisArchive = new textdisplay.Archive(archive);
            String archiveMsg = thisArchive.message();
            boolean hasMessage = (archiveMsg!=null && archiveMsg.length()>0);
            textdisplay.Manuscript thisMS = new textdisplay.Manuscript(thisFolio.getFolioNumber());
            Group thisGroup = new Group(thisProject.getGroupID());
            Boolean isMember,permitOACr,permitOACw,permitExport,permitCopy,permitModify,permitAnnotation,permitButtons,permitParsing,permitMetadata,permitNotes,permitRead;
            isMember=permitOACr=permitOACw=permitExport=permitCopy=permitModify=permitAnnotation=permitButtons=permitParsing=permitMetadata=permitNotes=permitRead=false;
            isMember = thisGroup.isMember(UID);
            out.println("isMember = "+isMember);
            ProjectPermissions permit = new ProjectPermissions(projectID);
            permitOACr = permit.getAllow_OAC_read();
            out.println("permitOACr = "+permitOACr);
            permitOACw = permit.getAllow_OAC_write();
            out.println("permitOACw = "+permitOACw);
            permitExport = permit.getAllow_export();
            out.println("permitExport = "+permitExport);
            permitCopy = permit.getAllow_public_copy();
            out.println("permitCopy = "+permitCopy);
            permitModify = permit.getAllow_public_modify();
            out.println("permitModify = "+permitModify);
            permitAnnotation = permit.getAllow_public_modify_annotation();
            out.println("permitAnnotation = "+permitAnnotation);
            permitButtons = permit.getAllow_public_modify_buttons();
            out.println("permitButtons = "+permitButtons);
            permitParsing = permit.getAllow_public_modify_line_parsing();
            out.println("permitParsing = "+permitParsing);
            permitMetadata = permit.getAllow_public_modify_metadata();
            out.println("permitMetadata = "+permitMetadata);
            permitNotes = permit.getAllow_public_modify_notes();
            out.println("permitNotes = "+permitNotes);
            permitRead = permit.getAllow_public_read_transcription();
            out.println("permitRead = "+permitRead);
            out.println("</script>");
            if (!thisGroup.isMember(UID) && !permitRead){
                String errorMessage = thisUser.getFname() + ", you are not a member of this project.";
            %><%@include file="WEB-INF/includes/errorBang.jspf" %><%
                return;
            }
%>
        <script type="text/javascript" >
            var firstFocus  <%if (request.getParameter("currentFocus") != null) {
            out.print("=\"" + request.getParameter("currentFocus") + "\"");
        }%>;
            var imgURL,prevURL,nextURL = null;
            imgURL = "<%out.print(thisFolio.getImageURLResize(2000));%>";
            var nextFolio = null;
            <%
            if(thisProject.getPreceedingPage(pageno) > 0) {
                Folio prevFolio = new Folio(thisProject.getPreceedingPage(pageno));
                out.println("prevURL = \"" + prevFolio.getImageURLResize(2000) + "\";");
            }
            if (thisProject.getFollowingPage(pageno) > 0) {
                Folio nextFolio = new Folio(thisProject.getFollowingPage(pageno));
                out.println("nextURL = \"" + nextFolio.getImageURLResize(2000) + "\";");
                out.println("nextFolio = \"" + thisProject.getFollowingPage(pageno) + "\";");
            }
            if (request.getParameter("tool") != null){
                out.println("liveTool = '"+request.getParameter("tool")+"';");
            }
            if (request.getParameter("compareIndex") != null){
                out.println("compareIndex = '"+request.getParameter("compareIndex")+"';");
            }%>
  $(function() {
      $(".accordion").children("div").hide();
//      $("#aShowLink").click(Annotation.showLink);
      $('#overlay').on("click",function(event){
          console.log(event.target);
          $(this).hide(250);
          $(".popover").remove();
      });     
      $(".helpContents").click(function(){
          Help.select($(this));
      });
      $(".showMe").each(function(index){
          $(this).append(function(){
          var tip = $("<div/>");
          var ref = $(this).attr("ref");
          var content = "<span class='ui-state-default ui-corner-all lightUp' ref='"+index+"'>Show on Screen</span><span class='ui-state-default ui-corner-all video' ref='"+index+"'>Video Help</span>";
          tip.addClass("helpDetail ui-corner-all").html(content);
          return tip;
          });    
  }).hover(function(){
          var thisWidth = $(this).width()+10;
          var tipWidth = 200;
          var thisPos = ($(this).position().left > tipWidth/1.8) ? Math.min((tipWidth-thisWidth)/2,(Page.width()-tipWidth-5)) : $(this).position().left - 5;
          var thisHeight = $(this).height();
          //if (thisPos+thisWidth > tipWidth) thisPos = tipWidth-thisWidth;
          $(this).find(".helpDetail").css({
              left  : -thisPos+"px",
              width : tipWidth+"px",
              top   : thisHeight+"px"
          }).show();
      },function(){
          $(".helpDetail").hide();
      });
      $(".lightUp").on("click",function(){
          var refIndex = parseInt($(this).attr("ref"));
          Help.lightUp(refIndex);
      });
      $(".video").on("click",function(){
          var refIndex = parseInt($(this).attr("ref"));
          Help.video(refIndex);
      });
      $(".glossary").append(function(){
          var tip = $("<div/>");
          var ref = $(this).attr("ref");
          var content = $("#"+ref).add($("#"+ref).next("dd")).clone();
          tip.addClass("gloss ui-state-default ui-corner-all").html(content);
          return tip;
      }).hover(function(){
          var thisWidth = $(this).width()+10;
          var thisPos = $(this).position().left-$(this).parent().position().left-5;
          var thisHeight = $(this).height();
          var tipWidth = Math.min($(this).parents(".helpColumn").width()-15,600);
          if (thisPos+thisWidth > tipWidth) thisPos = tipWidth-thisWidth;
          $(this).find(".gloss").css({
              left  : -thisPos+"px",
              width : tipWidth+"px",
              top   : thisHeight+"px"
          }).show();
      },function(){
          $(".gloss").hide();
      }).click(function(){
          var ref = $(this).attr("ref");
          $("#helpContents").children(":last").click();
          var def = $("#"+ref);
          def.css('color','orangered');
      });
  });
        </script>
    </head>
    <body id="transcriptionPage">
        <script type="text/javascript">
            var pageLoader = '<div id="parsingLoader"><div id="circleG_1" class="circleG"></div><div id="circleG_2" class="circleG"></div><div id="circleG_3" class="circleG"></div><div id="loadText">Requesting&nbsp;Image...</div></div>';
            document.write(pageLoader);
        </script>
        <%
        boolean isPrivate = thisProject.containsUserUploadedManuscript();
        boolean isPrivateCollaborator = thisGroup.isMember(UID) && isPrivate;
        boolean isAuthorized = thisMS.isAuthorized(thisUser);
            if (thisMS.isRestricted() && !isPrivateCollaborator) { 
                if (!isPrivateCollaborator && isPrivate) {
                %>
            <div id="requestAccess" class="ui-widget ui-corner-all ui-widget-content">
                <h2 class="ui-widget-header ui-corner-all ui-state-error">Private Collection</h2>
                <div class="ui-state-error-text" style="text-align: center;">
                    <span style="display: inline-block;" class="ui-icon ui-icon-alert"></span>Private Collection
                </div>
                <br />
                <p>This image is part of a private user collection and may not 
                    be viewed. Users may invite up to 5 collaborators to each
                    project containing private images.</p>
                <a class="returnButton" href="index.jsp">Return Home</a>
            </div>
                <div id="trexHead"></div>
        <%    return;
                } else if(!isAuthorized) {
                %>
            <div id="requestAccess" class="ui-widget ui-corner-all ui-widget-content">
                <h2 class="ui-widget-header ui-corner-all ui-state-error">Restricted Access</h2>
                <div class="ui-state-error-text" style="text-align: center;"><span style="display: inline-block;" class="ui-icon ui-icon-alert"></span><%out.print(thisUser.getFname());%>, you do not have permission to view this manuscript!</div>
                <br />
                <form action="requestAccess.jsp" method="POST">
                    <%
                    String controllerHint = thisMS.getControllingUser().getUname();
                    controllerHint = controllerHint.substring(controllerHint.indexOf('@'));
                    %>
                    <p>Request access for <%out.print(thisUser.getFname()+" "+thisUser.getLname()+" ("+thisUser.getUname()+") from the TPEN user ("+controllerHint+")");%> who controls access to this document.
                    <textarea name="reason" placeholder="Add any additional request details."></textarea>
                    <input type="hidden" name="ms" value="<%out.print(thisMS.getID());%>">
                    <input type="hidden" name="projectID" value="<%out.print(projectID);%>">
                    <input class="ui-button tpenButton clear right" type="submit" name="submitted" value="Send Request"/>
                    </p>
                </form>               
                <a class="returnButton" href="index.jsp">Return Home</a>
            </div>
                <div id="trexHead"></div>
        <%    return;
                }
            }
            if (!(thisUser.hasAcceptedIPR(thisFolio.getFolioNumber()))) {
                //print the IPR agreement for this MS
                %>
            <div id="iprAccept" class="ui-widget ui-corner-all ui-widget-content">
                <h2 class="ui-widget-header ui-corner-all">Accept IPR Agreement</h2>
                <p>Please read and accept the IPR agreement below to access <span class="loud"><%out.print(thisFolio.getCollectionName() + " at "+ thisFolio.getArchive());%></span>. You only need to do this once.</p>
                <div id="iprAgreement" class="notice"><%out.print(thisFolio.getIPRAgreement());%></div>
                    <span class="right small"><%out.print(thisUser.getFname()+" "+thisUser.getLname()+" ("+thisUser.getUname()+")");%></span>
                    <div class="clear right buttons">
                    <button class="ui-button tpenButton" onclick="document.location='transcription.jsp?p=<%out.print(thisFolio.getFolioNumber() + "&projectID=" + projectID + "&acceptIPR=true");%>'">I Agree</button>
                    <button class="ui-button tpenButton" onclick="document.location='index.jsp'">I do not agree</button>
                </div>
            </div>
                    <div id="trexHead"></div>
        <%  return;
            }
            Transcription[] thisText;
            thisText = Transcription.getProjectTranscriptions(projectID, pageno);
        %>
                                <div id="siteNavigation" class="left right navigation">
                                                      <span class="right">
                              <%out.print(thisUser.getFname() + " " + thisUser.getLname() + " (" + thisUser.getUname() + ")");%>
                    <span id="helpBtns">
                    <a id="helpMe" class="wBtn">Help<span class="ui-icon ui-icon-help right"></span></a>
                    <a id="helpContact" href="admin.jsp#aboutTab" target="_blank" class="wBtn">Contact<span class="ui-icon ui-icon-comment right"></span></a>
                    </span>
                            </span>
                            <a class="exitPage wBtn" title="T-PEN Home" href="index.jsp"><span class="left ui-icon ui-icon-home"></span>T-PEN Home</a>
<%if(permitModify || permitButtons || permitCopy || permitExport || permitMetadata || isMember){%>
                            <a class="exitPage wBtn" title="My Projects" href="project.jsp?<%out.print(projectAppend);%>">My&nbsp;Projects</a>
<%}%>
                        </div>
        <div id="wrapper" class="ui-corner-all">
            <div id="help">
                <div id="closeHelp" class="ui-icon-closethick ui-icon"></div>
                        <div id="helpPanels">
                            <div class="helpPanel" title="Page Navigation">
                                <h2>Page Navigation</h2>
                                <ul class="helpColumn">
                                    <li>Move through the page with the <span class="showMe">Previous Line</span> and <span class="showMe">Next Line</span> buttons to move from line to line.</li>
                                    <li>The <span class="showMe">Line Indicator</span> identifies the current column and line based on the T&#8209;PEN <span ref="glossParsing" class="glossary">image parsing</span>.</li>
                                    <li>Click to jump to a line on the page within the <span class="showMe">View Full Page</span> or <span class="showMe">Preview</span> tools.</li>
                                </ul>
                            </div>
                            <div class="helpPanel" title="Transcription Tools">
                                <h2>Transcription Tools</h2>
                                <dl class="helpColumn">
                                    <dt>Custom Text Entry</dt>
                                    <dd>Expand the <span class="showMe">Characters</span> or <span class="showMe">XML Tags</span> list to reveal buttons to insert your custom tags or <span ref="glossSpecialCharacters" class="glossary">special characters</span> into the <span ref="glossTranscriptionArea" class="glossary">transcription area</span>.</dd>
                                    <dt class="showMe">View Full Page</dt>
                                    <dd>Show the whole page image. Mouseover the image to highlight individual lines. Click to navigate directly to that line. Use the available <span class="showMe">magnify tool</span> to take a closer look.</dd>
                                    <dt class="showMe">History</dt>
                                    <dd>Lists the previous saved versions of the <span ref="glossParsing" class="glossary">line parsing</span> and <span ref="glossTranscription" class="glossary">transcription text</span>. Mouseover the history entries to reveal options to revert to a previous version.</dd>
                                    <dt class="showMe">Abbreviations</dt>
                                    <dd>Browse A. Capelli's <cite>lexicon abbreviarum</cite> Latin and Italian abbreviations.</dd>
                                    <dt class="showMe">Compare Pages</dt>
                                    <dd>View other pages from this project. Use the available <span class="showMe">magnify tool</span> to take a closer look.</dd>
                                    <dt class="showMe">Linebreak</dt>
                                    <dd>Upload a file for manual or automatic linebreaking.</dd>
                                    <dt class="showMe">Correct Parsing</dt>
                                    <dd>View and adjust the <span ref="glossParsing" class="glossary">line parsing</span> on this page.</dd>
                                    <dt class="showMe">Preview</dt>
                                    <dd>Reveal the <span ref="glossTranscription" class="glossary">transcription text</span> on the previous, current, and following pages. Make changes to text on other pages within the tool.</dd>
                                </dl>
                            </div>
                            <div class="helpPanel" title="Manuscript Navigation">
                                <h2>Manuscript Navigation</h2>
                                <ul class="helpColumn">
                                    <li>The <span class="showMe">Location Flag</span> shows the shelfmark of the current page.</li>
                                    <li>Use the <span class="showMe">Jump to page</span> menu to select any page in the current project.</li>
                                    <li>The <span class="showMe">Previous Page</span> and <span class="showMe">Next Page</span> buttons step through the project one page at a time.</li>
                                </ul>
                            </div>
                            <div class="helpPanel" title="Keyboard Shortcuts">
                                <h2>Keyboard Shortcuts</h2>
                                Support for most browsers includes the following simple shortcuts:
                                    <dl class="helpColumn">
                                        <dt>CTRL + 1-9:</dt>
                                            <dd>Insert the corresponding special character at the cursor location.</dd>
                                        <dt>CTRL + HOME:</dt>
                                            <dd>Quickly jump to the first line of the current page.</dd>
                                        <dt>TAB:</dt>
                                            <dd>Move forward through lines of transcription. Reverse with SHIFT + TAB.</dd>
                                        <dt>ALT + arrows:</dt>
                                            <dd>Move up and down through lines of transcription with the keyboard arrow keys.</dd>
                                        <dt>ESC:</dt>
                                            <dd>Close any open tool and return to fullscreen transcription.</dd>
                                        <dt>ALT:</dt>
                                            <dd>Hold to adjust the interface. Slide the main workspace up and down to see more of the manuscript;<br/>move the manuscript image freely behind the main workspace; or<br/>resize the bookmark bounding box to precisely bound an odd or skewed line.</dd>
                                        <dt>CTRL:</dt>
                                            <dd>Hold to get a better view of the important parts of your manuscript. The bookmark bounding box, location flag, and bug report form will all fade out and let you see the manuscript or tool behind it.</dd>
                                        <dt>F1:</dt>
                                            <dd>On the transcription screen, pulls up the application help.</dd>
                                        <dt>F11:</dt>
                                            <dd>Toggle fullscreen, eliminating browser and desktop toolbars.</dd>
                                        <dt>ALT + CTRL:</dt>
                                            <dd>Hold both to use the peek-zoom, which scales the bounded area to fit your screen and increases the quality of the image, if possible.</dd>
                                        <dt>+ or - while magnifying:</dt>
                                            <dd>Each keystroke will adjust the magnification of the tool by .4x to fine tune the image result.</dd>
                                    </dl>
                            </div>
                            <div class="helpPanel" title="Glossary">
                                <h2>Glossary</h2>
                                Commonly used terms in this application
                                <dl class="helpColumn">
                                        <dt id="glossPage">Page</dt>
                                            <dd>A single image and supporting interface. Some source images may contain partial or multiple folios.</dd>
                                        <dt id="glossProject">Project</dt>
                                            <dd>Collection of images designated for a specific group of users. Defaults to all images within a selected manuscript, but may be altered.</dd>
                                        <dt id="glossManuscript">Manuscript</dt>
                                            <dd>Collection of images on a repository. Often, but not necessarily exemplary of the artifact document.</dd>
                                        <dt id="glossGroup">Group</dt>
                                            <dd>Collection of users associated with a single project. Group membership is controlled by a Group Leader.</dd>
                                        <dt id="glossUser">User</dt>
                                            <dd>Entity intended to represent the human connected to a T&#8209;PEN account. Users login with the e-mail address associated with the T&#8209;PEN account and are displayed to others by a first initial and last name.</dd>
                                        <dt id="glossTranscription">Transcription</dt>
                                            <dd>Usually referring to the textual data associated with an image region. Transcription metadata include specific location, date of creation, and associated image data.</dd>
                                        <dt id="glossTranscriptionArea">Transcription Area</dt>
                                            <dd>Refers to the white textarea region where the user enters transcription text.</dd>
                                        <dt id="glossParsing">Parsing</dt>
                                            <dd>Describes the automatic or manual process of logically splitting a page into rectangular regions to designate individual lines for transcribing. Changes to parsing are saved in the history of a transcription.</dd>
                                        <dt id="glossHistory">History</dt>
                                            <dd>Previous versions of transcriptions. Changes made to textual and parsing data are recorded.</dd>
                                        <dt id="glossSpecialCharacters">Special Characters</dt>
                                            <dd>Custom-coded buttons that automatically enter a unicode character (typically one not included on the user's keyboard) into the transcription area.</dd>
                                        <dt id="glossButtons">Buttons</dt>
                                            <dd>Typically refers to XML Tag objects customized on the Button Management page. May also refer to Special Character objects in the same location. "Buttons" may also used in the generic sense to refer to a graphically distinct, clickable region which invokes an expected action.</dd>
                                    </dl>
                            </div>
                        </div>
                        <div id="helpContents">
                            <span class="helpContents helpActive" title="Page Navigation"></span>
                            <span class="helpContents" title="Transcription Tools"></span>
                            <span class="helpContents" title="Manuscript Navigation"></span>
                            <span class="helpContents" title="Keyboard Shortcuts"></span>
                            <span class="helpContents" title="Glossary"></span>
                        </div>
                    </div>
            <div id="imgTop">
                <div class="ui-corner-all" id="bookmark"><div id="bookmarkText"></div></div>
                <img id="imgTopImg" alt="imgTop" class="preloadImage" src="css/custom-theme/images/loadingImg.gif" />
                   </div>
                <div id="workspace">
                <div id="captions">
                    <span id="texts" class="loud">&nbsp;</span>
                    <span id="notes" class="quiet">&nbsp;</span>
                <div id="saveReport" class="ui-corner-top"></div>
                <div id="contribution" class="ui-corner-top">none</div>
                </div>
            <div id="entry">
                <%
                    int oldleft = -999;
                    char colCounter = 'A' - 1;
                    int linectr = 0;
                    int numberOfLines = thisText.length;
                    if (numberOfLines > 0){
                        for (int i = 0; i < numberOfLines; i++) {
                            if (thisText[i] != null) {
                                //first-run and each change of column increases colCounter by 1 and resets linectr to 1
                                linectr++;
                                if (oldleft != thisText[i].getX()) {
                                    colCounter++;
                                    linectr = 1;
                                    oldleft = thisText[i].getX();
                                }
                                //create 'transcriptlet' which contains the changing inputs for each line
                                out.println("<div class=\"transcriptlet transcriptletAfter\" id=\"t" + (i+1) + "\" data-lineid=\""+thisText[i].getLineID()+"\">");
                                out.println("<span class=\"counter wLeft\">Column:" + colCounter + " Line:" + linectr + "</span>");%>
                    <input class="lineWidth" type="hidden" value="<%out.print((thisText[i].getWidth()));%>" />
                    <input class="lineHeight" type="hidden" value="<%out.print((thisText[i].getHeight()));%>" />
                    <input class="lineLeft" type="hidden" value="<%out.print(thisText[i].getX());%>" />
                    <input class="lineTop" type="hidden" value="<%out.print(thisText[i].getY());%>" />
                    <%
                    out.println("<span class='addNotes wRight'><span class='ui-icon ui-icon-note right'></span>Add Notes</span>");
%><!--                    <span class="lineNav"></span>-->
                    <textarea id="transcription<%out.print((i+1));%>" class="ui-corner-all theText" <%if(!permitModify && !isMember)out.print(" readonly ");%>onkeydown="return Interaction.keyhandler(event);"><%out.print(thisText[i].getText().replace("&amp;", "&"));%></textarea>
                    <div class="xmlClosingTags" id="closeTags<%out.print((i+1));%>">
                    </div>
                    <textarea style="display:none;" id="notes<%out.print((i+1));%>" class="ui-corner-all notes" <%if(!permitModify && !isMember)out.print(" readonly ");%>><%if (thisText[i].getComment() != null && thisText[i].getComment().length() > 0) {out.print(thisText[i].getComment());}%></textarea>                          
                    <%//close 'transcriptlet', or print a suggestion to parse lines
                                out.println("</div>");
                            }
                        }
                    } else {
                    out.println("<div id=\"t1\" class=\"transcriptlet\"><textarea id='transcription1' class='ui-corner-all theText' placeholder='>No lines have been automatically detected on this page, please use the Parsing&nbsp;Tool to identify them.</textarea></div>");
                        }%>
            </div>
                <div id="buttonList">
                    <%
                    Tool.tools[] userTools;
                    UserTool[] projectTools = null;
                    try{
                    userTools = Tool.getTools(UID);
                    projectTools = UserTool.getUserTools(projectID);
                    }catch (NullPointerException e){
                        // npe
                    }
                    // stand in until builder is made for iframe tools TODO
/*                    boolean VulgateIsTool = false;
                    boolean DictionaryIsTool = false;
                    if (projectTools != null){
                        for (int i=0;i<projectTools.length;i++){
                            String toolURL = projectTools[i].getUrl();
                            if(toolURL.endsWith("vulsearch")){
                                VulgateIsTool = true;
                                continue;
                            }
                            if(toolURL.endsWith("morph.jsp")){
                                DictionaryIsTool = true;
                                continue;
                            }
                         }
                    }*/
                    %>
                    <div class="ui-widget-content ui-corner-all" id="options">
                        <div id="msOptions" class="left">
                            <a class="wBtn" title="View the full image on this page" name="imageBtn" id="imageBtn"><span class="left ui-icon-image ui-icon"></span>View Full Page</a>
                            <%
                            if(Tool.isToolActive(Tool.tools.compare, UID)){
                            %>
                            <a class="wBtn" name="compareBtn" target="_blank" id="compareBtn" title="Compare project pages side-by-side">Compare Pages</a>
                            <%}
                            if(Tool.isToolActive(Tool.tools.history, UID)){
                            %>
                            <a class="wBtn" title="View the history for this line" name="historyBtn" id="historyBtn"><span class="left ui-icon-clock ui-icon"></span>History</a>
                            <%}
                            if(Tool.isToolActive(Tool.tools.preview, UID)){
                            %>
                            <a class="wBtn" id="previewBtn" title="Preview transcription" href="#currentPage">Preview</a>
                            <%}
                            if(Tool.isToolActive(Tool.tools.abbreviation, UID)){
                            %>
                            <a class="wBtn" href="http://www.hist.msu.ru/Departments/Medieval/Cappelli/" target="_blank" id="abbrevBtn" title="Lookup frequently used abbreviations">Abbreviations</a>
                            <%}
                            if(false && Tool.isToolActive(Tool.tools.sciat, UID)){
                            %>
<!--                            <a class="wBtn" href="#" target="_blank" id="sciatBtn" title="SharedCanvas annotations viewer and creator">Annotations</a>-->
                            <%}
                    for (int i=0;i<projectTools.length;i++){%>
                            <a class="iframeTools wBtn" href="<%out.print(projectTools[i].getUrl());%>" target="frame<%out.print(i);%>" id="frameBtn<%out.print(i);%>" title="Access the <%out.print(projectTools[i].getName());%> tool"><%out.print(projectTools[i].getName());%></a>
                            <%}%>
                        </div>
                        <div id="projectOptions" class="left right">
                            <%
                            if(Tool.isToolActive(Tool.tools.linebreak, UID) && (isMember || permitModify)){
                            %>
                            <a class="wBtn" name="linebreakBtn" id="linebreakBtn" title="Linebreak Tool">Linebreak</a>
                            <%}
                            if(Tool.isToolActive(Tool.tools.parsing, UID) && (isMember || permitParsing)){
                            %>
                            <a class="wBtn" id="parsingBtn" name="parsingBtn" title="Parsing Tool">Correct Parsing</a>
                            <%}
                            if(isMember || permitButtons){
                            %>
                            <a class="exitPage wBtn" title="Manage Character and XML Tag Buttons" href="buttons.jsp?p=<%out.print(pageno+projectAppend);%>">Change&nbsp;Buttons</a>
                            <%} 
                            if(Tool.isToolActive(Tool.tools.paleography, UID)){
                            %>
                            <a class="wBtn" name="paleographyBtn" isready="<%out.print(thisFolio.isReadyForPaleographicAnalysis());%>" target="_blank" id="paleographyBtn" title="Search for similar glyphs in other images">Glyph Matching<span class="ui-icon ui-icon-search left"></span></a>
                            <%}%>                       
                        </div>
                        <%if (permitModify || isMember) {%>
                        <div id="popinDiv" class="left clear-left">
                          <a id="charactersPopin" title="Special Characters" class="popin wBtn">Characters</a>
                          <a id="xmlTagPopin" class="popin wBtn" title="Custom XML Tags">XML Tags</a>
                        </div>
                        <%}%>                      
                        <div id="navOptions" class="right clear-right">
                            <%if (thisProject.getPreceedingPage(pageno) > 0) {%>
                            <a id="prevPage" title="Previous Page" class="shrink icon exitPage wBtn navigation" href="?p=<%out.print(thisProject.getPreceedingPage(pageno) + "&projectID=" + projectID);%>">Previous&nbsp;Page<span class="ui-icon ui-icon-seek-prev left"></span></a>
                            <%}
                            if (thisProject.getFollowingPage(pageno) > 0) {%>
                            <a id="nextPage" title="Next Page" class="shrink icon exitPage wBtn navigation" href="?p=<%out.print(thisProject.getFollowingPage(pageno) + "&projectID=" + projectID);%>">Next&nbsp;Page<span class="ui-icon ui-icon-seek-next right"></span></a>
                            <%}%>
                        </div>
                        <div id="popin" class="ui-corner-all ui-widget-content">
                            <div id="charactersPopinList">
                                <%
                                if (isMember || permitModify){
                                    /**Retrieve stored Special Character information*/
                                    Hotkey ha;
                                    ha = new Hotkey(projectID, true);
                                    out.print(ha.javascriptToAddProjectButtons(projectID));
                                }
                                %>
                            </div>
                            <div id="xmlTagPopinList">
                                <%
                                if (isMember || permitModify){
                                    /**Retrieve stored XML Tag information*/
                                    out.print(TagButton.getAllProjectButtons(projectID));
                                }
                               %>
                            </div>
                        </div>
                    </div>
                </div>
                </div>
            <div id="imgBottom" class="">
                <img alt="imgBottom" class="preloadImage" src="css/custom-theme/images/loadingImg.gif" />
            </div>
            <div title="Return to fullscreen transcription" id="fullscreenBtn">
                <span class="left ui-icon ui-icon-carat-1-e"></span>
                <span class="left ui-icon ui-icon-carat-1-e"></span>
                <span class="left ui-icon ui-icon-carat-1-e"></span>
                <span class="left ui-icon ui-icon-carat-1-e"></span>
                <span class="left ui-icon ui-icon-carat-1-e"></span>
            </div>
        </div>
        <div id="tools">
<!--            Parsing Tool-->
<% if (isMember || permitParsing){%>
            <div id="parsingSplit" class="ui-widget">
                <div class="toolLinks">
            <!--tool buttons for parsing        -->
                </div>
                <div id="parsingDiv" class="ui-corner-all ui-widget-content">
                    <div id="parseOptions">
                        <h4 class="clear-left">Tool Options</h4>
                        <span id="backToTranscribing" class="tpenButton" onclick="$('#fullscreenBtn').click();"><span class="ui-icon ui-icon-arrowreturn-1-w left"></span>Return to Transcribing</span>
                        <span id="ctrlColumns" class="tpenButton" title="Make adjustments at the column level"><span class='ui-icon ui-icon-carat-1-e left'></span>Column Controls</span>
                        <div id="ctrlColumnsInst" class="actions ui-corner-all">
                            <h4 class="clear-left">Column Controls</h4>
                            <div class="accordion">
                                <p class="loud">Create, destroy, adjust, or reparse columns.</p>
                                <span id="createColumn" class="tpenButton" title="Create new columns by clicking and dragging">Adjust or Create Columns</span>
                                <div id="createColumnInst">
                                    <dl>
                                        <dt>New Column:</dt>
                                        <dd>Click and drag to place</dd>
                                        <dt>Adjustments:</dt>
                                        <dd>Drag the edges of an existing column</dd>
                                    </dl>
                                    <p class="small"><span class="inline ui-icon ui-icon-check"></span>This method does not destroy any <acronym title="Text, markup, and notes saved for lines within this column will remain attached.">transcription information</acronym>.<br />
                                    <span class="ui-state-error-text"><span class="inline ui-icon ui-icon-alert"></span>New column dimensions will overwrite some <acronym title="Custom heights will be retained, but the width will be uniform.">custom line settings</acronym>.</span></p>
                                </div>
                                <span id="destroyColumn" class="tpenButton" title="Remove an entire column and its data with a click">Destroy Columns</span>
                                <div id="destroyColumnInst">
                                    <p class="">Click a column to remove all lines and transcription data it contains from the project.</p>
                                    <span id="destroyPage" class="tpenButton ui-state-error" title="Destroy all columns on this page"><span class="left ui-icon ui-icon-circle-close"></span>Destroy All Lines</span>
                                    <p class="small"><span class="inline ui-icon ui-icon-check"></span>You will be warned before destroying any <acronym title="Text, markup, and notes saved for lines within this column">transcription information</acronym>.<br />
                                    <span class="ui-state-error-text"><span class="inline ui-icon ui-icon-alert"></span>This is a destructive process and cannot be undone.</span></p>
                                </div>
<!--                                <span id="clearColumns" class="tpenButton" title="Clear the page, removing all transcription information">Clear Page</span>
                                <div id="clearColumnsInst">
                                    <p class="">Click to clear the page, removing all transcription information.</p>
                                    <span id="destroyPage" class="tpenButton ui-state-error" title="Destroy all columns on this page"><span class="left ui-icon ui-icon-circle-close"></span>Destroy All Lines</span>
                                    <p class="small"><span class="ui-state-error-text"><span class="inline ui-icon ui-icon-alert"></span>This is a destructive process and cannot be undone.</span><br />
                                        <span class="inline ui-icon ui-icon-info"></span>If you load a page without any parsing information, automatic parsing will occur.<br />
                                        <span class="inline ui-icon ui-icon-check"></span>You will be warned before destroying any <acronym title="Text, markup, and notes saved for lines on this page.">transcription information</acronym>.
                                     </p>
                                </div>-->
                                <span id="reparseColumns" class="tpenButton" title="Reparse all columns, removing any associated lines">Reparse All Columns</span>
                                <div id="reparseColumnsInst">
                                    <p class="">Click to remove all transcription data and lines from this page, automatically parsing based on the columns shown.</p>
                                    <span id="reparseColumn" class="tpenButton ui-state-error" title="Reparse all columns on this page"><span class="left ui-icon ui-icon-refresh"></span>Submit for Reparsing</span>
                                    <p class="small"><span class="inline ui-icon ui-icon-trash"></span>Destroy all manual changes with the alternative <span id="reparsePage" class="loud caps">Reparse Entire Page</span>.<br />
                                    <span class="inline ui-icon ui-icon-check"></span>You will be warned before destroying any <acronym title="Text, markup, and notes saved for lines on this page.">transcription information</acronym>.<br />
                                    <span class="inline ui-icon ui-icon-info"></span>Make any adjustments to your columns before resubmitting.<br />
                                    <span class="ui-state-error-text"><span class="inline ui-icon ui-icon-alert"></span>This is a destructive process and cannot be undone.</span></p>
                                </div>
                            </div>
                        </div>
                        <span id="ctrlLines" class="tpenButton" title="Make adjustments at the line level"><span class='ui-icon ui-icon-carat-1-e left'></span>Line Controls</span>
                        <div id="ctrlLinesInst" class="actions ui-corner-all">
                            <h4 class="clear-left">Line Controls</h4>
                            <div class="accordion">
                                <p class="loud">Make adjustments to the way lines are defined within the columns</p>
                                <span id="addLines" class="tpenButton" title="Split lines to create new ones in a column">Add Lines</span>
                                <div id="addLinesInst">
                                    <p class="">Click within the shaded area to define the bottom of the new line.</p>
                                    <p class="small"><span class="inline ui-icon ui-icon-check"></span>This method retains all your <acronym title="Text, markup, and notes saved for lines within this column will remain attached.">transcription information</acronym>.<br />
                                    <span><span class="inline ui-icon ui-icon-info"></span>Any <acronym title="text, markup, and notes">transcription information</acronym> will remain with the line above where you click.</span><br />
                                    <span><span class="inline ui-icon ui-icon-info"></span>To add a line outside of the defined columns, <span class="ui-button loud" onclick="$('#ctrlColumns').click();$('#createColumn').click();" title="Click to use the Create a Column option">Create a Column</span>.</span></p>
                                    <div align="center">
                                        <p class="clear-left">Trouble seeing the ruler? Change the color below.</p>
                                        <div id="sampleRuler"></div>
                                        <label for="black"><input id="black" CHECKED type="radio" value="black" name="rulerColor"/>Black</label>
                                        <label for="white"><input id="white" type="radio" value="white" name="rulerColor"/>White</label>
                                        <input onchange="Parsing.customRulerColor();" id="customColor" tabindex="-1" type="radio" value=customColor name="rulerColor"/>
                                        <input style="width:80px;" title="Type the name of any valid HTML color or code. If the code is invalid, a default color will be shown." type="text" id="customRuler" onkeyup="Parsing.customRulerColor();" placeholder="transparent" value="transparent"/>
                                    </div>                  
                                </div>
                                <span id="removeLines" class="tpenButton" title="Merge two lines or remove the last line from a column">Delete/Merge Lines</span>
                                <div id="removeLinesInst">
                                    <p class="">Click within the shaded area to merge the highlighted lines or delete the last line of a column.</p>
                                    <p class="small"><span class="inline ui-icon ui-icon-info"></span>This method combines your <acronym title="Text, markup, and notes saved for lines within this column will remain attached.">transcription information</acronym> into the new line.<br />
                                    <span class="ui-state-error-text"><span class="inline ui-icon ui-icon-alert"></span>Deleting lines also removes any associated <acronym title="text, markup, and notes">transcription information</acronym>.</span> <br />
                                    <span><span class="inline ui-icon ui-icon-info"></span>To add a line, use the <span class="ui-button loud" onclick="$('#addLines').click();" title="Click to use the Add Lines option">Add Lines</span> option.</span></p>
                                </div>
                                <span id="adjustLines" class="tpenButton" title="Simple adjustments to individual lines">Adjust Lines</span>                       
                                <div id="adjustLinesInst">
                                    <p class="">Click and drag the right side of the line to contain the line accurately.</p>
                                    <p class="small"><span class="inline ui-icon ui-icon-check"></span>This method retains all your <acronym title="Text, markup, and notes saved for each line will remain attached.">transcription information</acronym>.<br />
                                    <span><span class="inline ui-icon ui-icon-info"></span>Only the right boundary can be moved in this tool.</span><br />
                                    <span><span class="inline ui-icon ui-icon-info"></span>Make precise, individual changes in the <acronym title="Hold ALT while transcribing to resize the line width and height precisely.">transcription view</acronym>.</span></p>
                                </div>
                            </div>
                        </div>
                        <div id="confirmParsingInst">
                            <h4 class="clear-left">Confirm Parsing</h4>
                            <div>
                                <p class="">Return to transcribing after confirming that image parsing is correct. If the automatic parsing is not accurate, use the options above to make corrections.</p>
                                <p class="small"><span class="inline ui-icon ui-icon-check"></span>This will not be required once you have saved transcription data on this page.<br />
                                <span class="ui-state-error-text"><span class="inline ui-icon ui-icon-alert"></span>Changes made here affect all group members on this project.</span></p>
                            </div>
                        </div>
                    </div>
                </div>
                <div id="sidebar">
                    <div id="progress" class="ui-widget-content ui-corner-all">Select an option above</div>
                    <div id="lineResizing" class="ui-widget-content ui-corner-all">Your <span id="originalLine">original</span> pixel line is being resized by <span id="newLine">100</span>%.</div>
                </div>
            </div>
                                                <%}%>
<!--            Full-Page View Tool-->
            <div id="imageSplit">
                <div class="ui-corner-all" id="lineInfo"></div>
                <div class="toolLinks right">
                    <a id="magnify1" class="listBegin right magnifyBtn" magnifyImg="fullImg"><span class="ui-icon ui-icon-zoomin left"></span>Magnify</a>
                </div>
                <div id="clickDivs">
                    <img alt="full-screen image" id="fullImg" class="preloadImage" src="css/custom-theme/images/loadingCompare.gif" />
                <%//out.print("<img height=\"100%\" id=\"fullImg\" class=\"preloadImage\" src=\"css/custom-theme/images/loadingCompare.gif\" />");
                    // there is an available link to the item at the archive site, make that the onclick for the full ms image
             //       if (archiveLink != null && archiveLink.compareTo("") != 0) {
             //           out.print("onclick=\"window.open('" + archiveLink + "','_new');\"/>");
             //       } else {
             //           out.print("/>");
             //       }
                %>
                </div>
            </div>
<!--            Cappelli Abbreviation Lookup Tool-->
            <div id="abbrevSplit">
                <!--                        Image is loaded in Data.postLoader()-->
                <img id="abbrevImg" alt="abbreviation page" src="//t-pen.org/images/cappelli/Scan0064.jpg" />
                <div id="abbreviations"> 
                    <%String[] groups = textdisplay.AbbreviationPage.getGroups("capelli");
                        StringBuilder selectGroups = new StringBuilder();
                        StringBuilder selectLabels = new StringBuilder();
                        for (int i = 0; i < groups.length; i++) {
                            selectGroups.append("<option value='").append(i).append("'>").append(groups[i]).append("</option>");
                            textdisplay.AbbreviationPage[] labels = textdisplay.AbbreviationPage.getLabels(groups[i],"capelli");
                            for (int j = 0; j < labels.length; j++) {
                                textdisplay.AbbreviationPage abbrev = new textdisplay.AbbreviationPage(labels[j].getId());
                                selectLabels.append("<option class='").append(i).append("' value='").append(abbrev.getImageName()).append("' id='").append(labels[j].getId()).append("'>").append(labels[j].getLabel()).append("</option>\n");
                            }
                        }
                    %><select id="abbrevGroups">
                        <option selected>Select One</option>
                        <%out.print(selectGroups);%>
                    </select>
                    <select disabled id="abbrevLabels">
                        <option selected>Select One</option>
                        <%out.print(selectLabels);%>
                    </select>
                </div>
            </div>
<!--                    Compare Pages Tool-->
            <div id="compareSplit">
                <div class="toolLinks right">
                    <a id="magnify2" class="listBegin magnifyBtn" magnifyImg="compareDiv"><span class="ui-icon ui-icon-zoomin left"></span>Magnify</a>
                    <a class="exitPage ui-state-disabled" id="gotoThis"><span class="ui-icon ui-icon-transferthick-e-w left"></span>Go To</a>
                </div>
                <img alt="compare images" id="compareDiv" class="preloadImage" src="css/custom-theme/images/loadingCompare.gif" />
            </div>
<%if(isMember || permitModify){
                        %>
<!--                    Linebreaking Tool-->
            <div id="linebreakSplit" class="ui-widget ui-widget-content">
<!--                <div class="toolLinks right">
                </div>-->
<!--                <div id="linebreakDiv" class="ui-corner-all ui-widget-content">-->
                <h3>Linebreak Existing Transcription</h3>
       <% if (thisProject.getLinebreakText().length()>5) {
           //text is saved
           %>
           <div>
               This project has text stored for linebreaking:
               <div id="linebreakText">
                   <span id="lbText"></span>
                   <%if (thisProject.getLinebreakCharacterLimit()==thisProject.getLinebreakText().length()) out.print("<div id='charLimit' class='ui-corner-all ui-state-error-text small clear'>Showing the first "+thisProject.getLinebreakCharacterLimit()+" characters</div>");%>
               </div>
                   <script type="text/javascript">
//                       var leftovers = "<%--out.print(ESAPI.encoder().encodeForHTML(ESAPI.encoder().decodeForHTML(thisProject.getLinebreakText())));--%>";
                       var leftovers = "<%out.print(StringEscapeUtils.escapeJavaScript(thisProject.getLinebreakText()));%>";
                       $("#lbText").html(unescape(leftovers));
                   </script>
               <div id="linebreakOptions">
                   <span id="useText" class="tpenButton" title="Insert this text at the current line">Insert Text Here</span>
                   <span onclick="alert('Sorry, this feature is not yet implemented.\n\nAny new file you upload will overwrite the old data.')" id="removeText" class="tpenButton ui-state-disabled" title="Remove this text from the database">Remove Text from Database</span>
                   <span onclick="document.location.href='uploadText.jsp?p=<%out.print(pageno+projectAppend);%>';" id="newText" class="tpenButton" title="Upload a new file to linebreak">Upload New File</span>                       
                   <span id="linebreakStringBtn" class="tpenButton" title="Attempt linebreaking using this string">Preview Automatic Linebreaks
    <!--                   TODO replace value with stored value once it is stored-->
                       <input name="linebreakString" id="linebreakString" type="text" placeholder="<lb />" value="" />
                   </span>                       
                   <span id="useLinebreakText" class="tpenButton" title="Automatically linebreak this page">Use Automatic Linebreaking
                       <span id="linesDetected"></span>
                   </span>                       
               </div>
                   <ol class="left">
                       <li>Navigate to a line to start;</li>
                       <li><span class="loud">Insert Text</span> as a block; or</li>
                       <li>Enter a linebreak string to attempt automatic linebreaking; and</li>
                       <li>Press Enter to move all text following the cursor down to the next line.</li>
                   </ol>
                   <span class="small">*At the end of the page, the remaining text will be saved for use on the next page.</span>
           </div>
       <%
}else{
//no text saved
    out.print("Begin by <a href=\"uploadText.jsp?p="+pageno+"&projectID="+thisProject.getProjectID()+"\">uploading some text</a> to linebreak");
}
%>
<!--                </div>-->
            </div>
<%}%>
<%   if(Tool.isToolActive(Tool.tools.preview, UID)){ %>

<!--                    Preview Transcription Tool-->
            <div id="previewSplit" class="ui-widget">
                <div class="toolLinks">
                    <a class="exitPage" id="exportLink" href="project.jsp?selecTab=4&p=<%out.print(pageno+projectAppend);%>"><span class="right ui-icon ui-icon-disk"></span>Export Transcription</a>
                    <a id="previewNotes"><span class="right ui-icon ui-icon-note"></span>Show Notes</a>
<!--                    <a id="previewAnnotations"><span class="right ui-icon ui-icon-pin-s"></span>Show Annotations</a>                  -->
                </div>
                <div id="previewDiv">
                    <%
                        Transcription[] previewText;
                        int[] folioPreview = new int[3];
                        if (projectID >0){
                            folioPreview[0] = thisProject.getPreceedingPage(pageno);
                            folioPreview[1] = pageno;
                            folioPreview[2] = thisProject.getFollowingPage(pageno);
                        } else {
                            folioPreview[0] = -1;
                            folioPreview[1] = pageno; // error: no folios found
                            folioPreview[2] = -1;
                        }
                        for (int i = 0; i < 3; i++) {
                            if (folioPreview[i] < 1) {
                                continue;
                            }
//                            Annotation [] PreviewAnnos = Annotation.getAnnotationSet(projectID, folioPreview[i]);
//                            StringBuilder annos = new StringBuilder();
//                            int pAnnoLength = PreviewAnnos.length;
//                            if (pAnnoLength > 0){
//                                // Build Annotations
//                                annos.append("<div class='previewAnnotations'><span class='ui-icon ui-icon-pin-w left'></span>");
//                                for(int j=0;j<PreviewAnnos.length;j++){
//           //                         String aLink = "<span class='previewAnnoLink'>"+PreviewAnnos[j].getLink()+"</span>";
//                                    String aText = "<span class='previewAnnoText' title='("
//                                            + PreviewAnnos[j].getX()
//                                            + ", "
//                                            + PreviewAnnos[j].getY()
//                                            + ")'>"
//                                            + PreviewAnnos[j].getText()
//                                            + "</span>";
//                                    annos
//          //                              .append(aLink)
//                                        .append(aText);
//                                }
//                                annos.append("</div>");
//                            }
                            previewText = Transcription.getProjectTranscriptions(projectID, folioPreview[i]);
                            if (previewText.length < 1) {
                                continue;
                            }
                            int numberLines = previewText.length;
                            int columnLineShift = 0;
                            char column = 'A';
                            int oldLeftPreview = previewText[0].getX();
                    %>
                    <div class="previewPage" data-pageNumber="<%out.print((pageno - 1) + i);%>">
                        <span class="previewFolioNumber"><%out.print(new Folio(folioPreview[i]).getPageName().replaceAll(" ", "&nbsp;"));if (i == 1)out.print(", <a name='currentPage' id='currentPage' style='text-decoration: none;'>Current&nbsp;Page</a>");%></span><%
                            for (int line = 0; line < numberLines; line++) {
                                // line is zero-based in loop, but starts at 1 in database
                            %>
                        <div class="previewLine" data-lineNumber="<%out.print(line+1);%>">
                            <span class="previewLineNumber" data-lineid="<%out.print(previewText[line].getLineID());%>" data-lineNumber="<%out.print(line+1);%>"  data-column="<%out.print(column);%>"  data-lineOfColumn="<%out.print(line +1 - columnLineShift);%>"><%
                                int columnLeft = previewText[line].getX();
                                if (columnLeft > oldLeftPreview) {
                                    column++;
                                    columnLineShift = line;
                                    oldLeftPreview = columnLeft;
                                }
                                out.print(column + "" + (line +1 - columnLineShift) + " ");
                            %></span>
                            <span contentEditable="<%out.print((permitModify||isMember));%>" class="previewText<%if (i == 1) out.print(" currentPage");%>"><%out.print(previewText[line].getText().replace("&amp;", "&"));%><span class="previewLinebreak"></span></span>
                            <span contentEditable="<%out.print((permitModify||isMember));%>" class="previewNotes<%if (i == 1) out.print(" currentPage");%>"><%out.print(previewText[line].getComment());%></span>
                        </div>
                        <%}
//                        out.print(annos.toString());
%>
<!--                    <div class="previewAnnotations"></div>-->
                    </div>
                    <%}%>             
                </div>
            </div>
                <%}%>
                <%   if(Tool.isToolActive(Tool.tools.history, UID)){
%>
            <div id="historySplit" class="ui-widget">
                <div class="toolLinks">
                    <a href="#" >Parsing Only</a>
                    <a href="#" >Text Only</a>
                    <a href="#" >Show Notes</a>
                </div>
                <div id="historyDiv" class="ui-corner-all ui-widget-content">
                    <div id="historyViewer">
                        <div id="historyBookmark" class="historyBookmark"></div>
                        <img alt="historyViewerImg" class="preloadImage" src="css/custom-theme/images/loadingImg.gif" />
                    </div>
                    <div id="historyListing">
                        <%
                                    Calendar m = Calendar.getInstance(); //midnight
                       //             m.set(Calendar.HOUR_OF_DAY, 0);
                       //             m.set(Calendar.MINUTE, 0);
                       //             m.set(Calendar.SECOND, 0);
                       //             m.set(Calendar.MILLISECOND, 0);
                                    int DOY = m.get(Calendar.DAY_OF_YEAR);
                                    int YEAR= m.get(Calendar.YEAR);
                        Map<Integer, List<ArchivedTranscription>> pageHistory = ArchivedTranscription.getAllVersionsForPage(projectID, pageno);
                        for (Transcription t: thisText) {
                            List<ArchivedTranscription> history = pageHistory.get(t.getLineID());
                            if (history == null){
                                out.print("<div class='historyLine' id='history"+t.getLineID()+"' linewidth='"+t.getWidth()+"' lineheight='"+t.getHeight()+"' lineleft='"+t.getX()+"' linetop='"+t.getY()+"'>No previous versions</div>");
                            } else {
                                out.print("<div class='historyLine' id='history"+t.getLineID()+"'>");
                                for (ArchivedTranscription h: history){%>
                                <div class="historyEntry ui-corner-all" linewidth='<%out.print(h.getWidth());%>' lineheight='<%out.print(h.getHeight());%>' lineleft='<%out.print(h.getX());%>' linetop='<%out.print(h.getY());%>'>
                                    <%
                                    String dateString = "-";
                                    DateFormat dfm;
                                    Calendar historyDate = Calendar.getInstance();
                                    historyDate.setTimeInMillis(h.getDate().getTime());
                                    if ((YEAR == historyDate.get(Calendar.YEAR)) && (DOY == historyDate.get(Calendar.DAY_OF_YEAR))){ // not perfect, but other date comparisons were frustrating
                                        dfm = DateFormat.getTimeInstance(DateFormat.MEDIUM);
                                        dateString = "today";//DateFormat.getTimeInstance(DateFormat.SHORT).format(historyDate);
                                    } else {
                                        dfm = DateFormat.getDateInstance(DateFormat.MEDIUM);
                                        dateString = dfm.format(h.getDate());//DateFormat.getDateInstance(DateFormat.MEDIUM).format(historyDate);
                                    }
                                    dfm.setCalendar(historyDate);
                                    %>
                                    <div class="historyDate"><%out.print(dateString);%></div>
                                    <% if (h.getCreator() > 0){
                                        User creatorUser = new User(h.getCreator());
                                        String creatorName = creatorUser.getFname()+" "+creatorUser.getLname();
                                    %>
                                    <div class="historyCreator"><%out.print(creatorName);%></div>
                                    <%}%>
                                    <div class="right historyRevert"></div>
                                    <div class="right loud historyDims"></div>
                                    <div class="historyText"><%out.print(h.getText());%></div>
                                    <div class="historyNote"><%out.print(h.getComment());%></div>
<%if(isMember || permitModify){%>
                                    <div class="historyOptions">
                                        <span title="Revert image parsing only" class="ui-icon ui-icon-image right"></span>
                                        <span title="Revert text only" class="ui-icon ui-icon-pencil right"></span>
                                        <span title="Revert to this version" class="ui-icon ui-icon-arrowreturnthick-1-n right"></span>
                                    </div>
<%}%>
                                </div>
                                <%}
                                out.print("</div>");
                            }
                        }%>
                    </div>
            </div>
        </div>
                    <% } %>
                    <!--SCIAT tool-->
<%   if(false && Tool.isToolActive(Tool.tools.sciat, UID)){ %>

<!--            <div id="sciatSplit" class="ui-widget">
                <div class="toolLinks">
                    <a href="http://165.134.241.141:80/Annotation/svg-editor.html" target="sciatFrame" title="Reset this tool" class="frameReset">Reset<span class="ui-icon ui-icon-refresh left"></span></a>
                    <a href="http://165.134.241.141:80/Annotation/svg-editor.html" target="_blank" title="Fill the window" class="frameTab">Full size<span class="ui-icon ui-icon-arrow-4-diag left"></span></a>
                </div>
                <%--@include file="WEB-INF/includes/sciat.jspf" --%>
            </div>-->
			<%}%>
                    <%
                    // Loading Loop for iframe tools
                    for(int i=0;i<projectTools.length;i++){
                        String frameLink = projectTools[i].getUrl();
                        String frameName = projectTools[i].getName();
%>
                    <!--  Build Tool-->
            <div id="split<%out.print(i);%>">
                <div class="toolLinks">
                    <a href="<%out.print(frameLink);%>" target="frame<%out.print(i);%>" title="Reset this tool" class="frameReset">Reset<span class="ui-icon ui-icon-refresh left"></span></a>
                    <a href="<%out.print(frameLink);%>" target="_blank" title="Open this tool in a new tab" class="frameTab"><%out.print(frameName);%><span class="ui-icon ui-icon-extlink left"></span></a>
                </div>
                <div id="frameSplitDiv<%out.print(i);%>">
                    <iframe name="frame<%out.print(i);%>" id="frame<%out.print(i);%>" src="iframe.html">
                    </iframe>
                </div>
            </div>
                    <%}%>
        </div>
        <div id="imageTip" class="ui-corner-all">Insert Line</div>
        <div id="zoomDiv" class="ui-corner-all"></div>
        <div id="ruler1"></div><div id="ruler2"></div>
        <div id="savedChanges" class="ui-corner-all ui-state-default"></div>
        <div id="overlay" class="ui-widget-overlay">
            <div id="overlayNote">Click the page to return</div>
        </div>
                    <div id="location" class="ui-widget-content ui-corner-tr"><%
                out.print("<span class='left'>Viewing: " + ESAPI.encoder().decodeFromURL(thisFolio.getArchiveShelfMark() + " " + thisFolio.getCollectionName() + " " + thisFolio.getPageName()) + "&nbsp;</span>");
                    String archiveLink = thisFolio.getArchiveLink();
                    textdisplay.Manuscript ms = new textdisplay.Manuscript(thisFolio.getFolioNumber());
                    if(ms.getRepository().compareTo("Corpus Christi College")==0){
                        out.print("<a id='linkback' target='_blank' href=\"http://dms-dev.stanford.edu/catalog/CCC"+ms.getCollection().replace("MS_", "") + "_keywords\">View in Stanford Search and Discovery<span class='ui-icon ui-icon-extlink right'></span></a>");
                    }
                    if ((ms.getControllingUser() != null) && (ms.getControllingUser().getUID() == thisUser.getUID())){
                        out.print("<a class='left ui-icon ui-icon-wrench' href=\"manuscriptAdmin.jsp?ms=" + ms.getID() + "\" title=\"Change page name or administer manuscript access\">Change Page Names</a>");
                    }
                    if (false && archiveLink != null && archiveLink.compareTo("") != 0) {
                        out.print("<a class='left clear ui-icon ui-icon-image' href=\"" + archiveLink + "\" target=\"_blank\" title=\"View Source Image\">Source Image</a>");
                }%>
                <select class="clear left" onchange="Interaction.navigateTo(this);">
                    <option SELECTED>Jump to page</option>
                    <%out.print(ESAPI.encoder().decodeFromURL(thisProject.getFolioDropdown()));%>
                </select>
            </div>

                <script type="text/javascript">
            $("transcription1").removeClass("transcripletAfter");
//            $("#options").find('a').attr('onclick',"return Screen.restore(event);");
            $("#popin > div").hide(); //hiding here retains display:inline-block
            $("#navOptions").clone(true,true).appendTo("#parsingDiv"); //copy navigation to Parsing tool
                var compareSelect = $("#location").find("select").clone(true);
                compareSelect.clone(true).attr("id","parseLocation").insertAfter("#parseOptions");
                compareSelect.attr({
                    "id":"pageCompare",
                    "onchange":"compareTo(this)"
                }).children("option").eq(0).attr("selected",true).text("Compare to page");
                compareSelect.appendTo("#compareSplit .toolLinks");
                <%
                //TODO replace alert with kinder message
                if (hasMessage) out.print("alert(unescape('Message from"+archive+":\\n\\n"+(archiveMsg)+"'));");                
                %>
        </script>
<%@include file="WEB-INF/includes/noscript.jspf" %>
    </body><%}%>
</html>