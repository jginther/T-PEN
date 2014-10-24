<%-- 
    Document   : exportUI
    Created on : May 25, 2011, 1:23:56 PM
    Author     : jim
--%>

<%@page import="textdisplay.Folio"%>
<%@page import="user.*"%>
<%@page import="textdisplay.FolioSet"%>
<%@page import="textdisplay.Transcription"%>
<%@page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@page import="org.owasp.esapi.ESAPI" %>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN"
   "http://www.w3.org/TR/html4/loose.dtd">
    <%
        int projectID=0;
        int beginFolio = -1;
        int endFolio = -1;
        if (request.getParameter("projectID")!=null){
            projectID = Integer.parseInt(request.getParameter("projectID"));
        }
        textdisplay.Folio [] folios = new textdisplay.Project(projectID).getFolios();
        if (request.getParameter("beginFolio")!=null){
            beginFolio = Integer.parseInt(request.getParameter("beginFolio"));
        }
        if (request.getParameter("endFolio")!=null){
            endFolio = Integer.parseInt(request.getParameter("endFolio"));
        }
        try {
            // Update list of folios to only include the desired range from beginFolio-endFolio (both optional)
            int destPos = 0;
            int i = 0;
            while ((i<folios.length-1) && (folios[i].getFolioNumber() != beginFolio))
            {i++;};
            int srcPos = i;
            while ((i<folios.length-1) && (folios[i].getFolioNumber() != endFolio))
            {i++;};
            int length = i-srcPos+1;
            textdisplay.Folio [] range = new textdisplay.Folio[length];
            System.arraycopy(folios, srcPos, range, destPos, length);
            folios = range;
        } catch (NullPointerException e){
            // oops
        } catch (ArrayIndexOutOfBoundsException e){
            // no parameters
        }
    %>
  
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title>T-PEN Transcription</title>
        <script type="text/javascript" src="https://ajax.googleapis.com/ajax/libs/jquery/1.7.1/jquery.js"></script>
        <link type="text/css" href="http://ajax.googleapis.com/ajax/libs/jqueryui/1.7.2/themes/base/jquery-ui.css" rel="stylesheet" />
        <style type="text/css">
            #wrapper {opacity:0;
            -webkit-transition:opacity 1s;
            -moz-transition:opacity 1s;
            transition:opacity 1s;
            z-index: 1;
            }
            #fullText {width:100%;overflow: visible;position: relative;}
            /*Styling for export preview*/
            .exportPage {box-shadow:-1px -1px 5px black;position: relative;overflow: auto;width: auto;padding: 5px;padding-bottom: 1em;margin-bottom: 1em;}
            .exportFolioNumber,.exportFolioImage {float: left;font-weight: bold; display: none;width:100%;}
            .exportLine{float: left;clear:left;display:block;width:100%;}
            .exportLine:nth-child(even) { background-color: whitesmoke; }
            .exportLine:nth-child(odd) { background-color: lightgray; }
            .exportLineNumber {font-style: italic;float: left;width:5%;display: none;}
            .exportText {font-style: normal;float: left;width: 60%;}
            .exportLinebreak {float: left;color:gray;}
            .exportNotes {float: left; color:steelblue;width: 35%;}
            .format{padding:2px;text-align: center;position: relative;z-index: 2;cursor: pointer;}
            .formatDiv{margin:-4px 5px 2px;background: url(images/linen.png);padding:6px 2px 2px;overflow: hidden;z-index: 1; border: 1px solid #A68329;display:none;
                    -moz-box-shadow: -1px -1px 2px black;
                    -webkit-box-shadow: -1px -1px 2px black; 
                    box-shadow:-1px -1px 2px black;}
            #formatExport{display:block;}
            #exportLinebreakString,#exportWordbreak{width: 40px;}
            #updatePreview {position: absolute;top:-20px;height: 40px;width:120px;right:20%;z-index: 1;-webkit-transition:all .25s;}
            #updatePreview:hover {top:-34px;padding-top: 12px;height: 28px;}
            #fullTextDiv {position: relative;overflow: visible;float: left;width:66%;padding:10px;z-index: 2;position: relative;}
            span {position:relative;}
            .footnote,.endnote{display: block;float:left;clear:left;}
            .mdata {display: inline-block; width:240px; float: left;}
            #beginFolio,#endFolio{width:60%;position:absolute;left:110px;}
            #pageRange {width: 100%;position:relative;}
            #pageRange strong {line-height: 20px;}
            #formatting {max-width:600px;min-width: 200px;width:33%;position: relative;float: left;}
            .optionPanel{position: relative;width: 100%;}
            #metadataPreview,#titlePage,#endNotes {display: none;}
            .pagebreak:after{content: "(pagebreak)";position: absolute;width: 100%;color: gray;font-size: small;bottom:0;left:0;clear: both;text-align: right;}
            .exportPage a {display: none;background: black;font-weight: bold;color:white;position: absolute;top:0;right:0;padding: 3px;cursor: pointer;z-index: 10; border-bottom-left-radius: 8px;font-family: sans-serif;}
            .exportPage:hover a {display: block;}
            .exportPage a:hover {background: red;text-shadow:0 1px 0 darkred;}
            .editNote {
                position: absolute;
                z-index: 11;
                bottom: 0;
                right:0;
                display: block;
                opacity: 1;
                background: silver;
                font-weight: bold;
                color:rgb(120,120,120);
                padding: 3px;
                border-top-left-radius: 8px;
                font-family: sans-serif;
                text-shadow:0 1px 0 lightgray;
                font-size: small;
                width: 300px;
                -webkit-transition:opacity 1s;
                -moz-transition:opacity 1s;
                transition:opacity 1s;
            }
            .exportPage:hover .editNote {
                opacity: 0;
            }
            .editable .editSpace {
                width: 90%;
                min-height: 3em;
                position: relative;
                border: 2px dotted transparent;
            }
            .editable:hover .editSpace {
                border: 2px dotted #ECECEC;
                border-radius: 5px;
                -webkit-transition:opacity 1s;
                -moz-transition:opacity 1s;
                transition:opacity 1s;
            }
            .editable:hover .editSpace:after {
                content: "Click to enter additional information";
                position: absolute;
                left:0;
                right:0;
                color:gray;
                background: #ECECEC;
                z-index: -1;
                text-align: center;
                font-family: sans-serif;
                height: 100%;
                width:100%;
                line-height: 3em;
            }
            .editable:hover .editSpace:hover:after {
                opacity:.2;
            }
            /* Loader */
            #loader {
                position: fixed;
                z-index: 0;
                top:40%;
                width:150px;
                margin:0 auto;
                left:0;
                right:0;
            }
            #circleG{
            width:149.33333333333334px;
            }

            .circleG{
            background-color:#FFFFFF;
            float:left;
            height:32px;
            margin-left:17px;
            width:32px;
            -webkit-animation-name:bounce_circleG;
            -webkit-border-radius:21px;
            -webkit-animation-duration:0.44999999999999996s;
            -webkit-animation-iteration-count:infinite;
            -webkit-animation-direction:linear;
            -moz-animation-name:bounce_circleG;
            -moz-border-radius:21px;
            -moz-animation-duration:0.44999999999999996s;
            -moz-animation-iteration-count:infinite;
            -moz-animation-direction:linear;
            opacity:0.3}

            #circleG_1{
            -webkit-animation-delay:0.09s;
            -moz-animation-delay:0.09s}
            
            #circleG_1:after {
            content: "Building Transcription";
            position: absolute;
            bottom: -2em;
}
            #circleG_2{
            -webkit-animation-delay:0.21s;
            -moz-animation-delay:0.21s}

            #circleG_3{
            -webkit-animation-delay:0.27s;
            -moz-animation-delay:0.27s}

            @-webkit-keyframes bounce_circleG{
            0%{
            opacity:0.3}

            50%{
            opacity:1;
            background-color:#000000}

            100%{
            opacity:0.3}

            }

            @-moz-keyframes bounce_circleG{
            0%{
            opacity:0.3}

            50%{
            opacity:1;
            background-color:#000000}

            100%{
            opacity:0.3}

            }
        @media print {
            .pagebreak{page-break-after:always;min-height: 100%;}
            .pagebreak:after,.noprint,.noprint:hover,.exportPage:hover .noprint{display:none;}
            .exportPage {box-shadow:0 0 0 transparent;border: thin solid black;padding-bottom: 5px;}
            .editSpace:after{display: none;}
            .editSpace:hover, .exportPage:hover .editSpace{background: none;border-color: transparent;}
        }
	</style>
        <script type="text/javascript">
            var Custom = {
                // Values Initialization
                titlePage: false,
                pageNames: false,
                imageTags: false,
                columnIDs: false,
                alternateColors: false,
                pagebreakPrint: false,
                metadata: false,
                beginFolio: null,
                endFolio: null,
                notes: "remove",
                tagStyle: new Array(),
                linebreak: "newline",
                exportWordbreak: "/-/",
                exportLinebreakString: "<lb/>",
                useLinebreakingString: false,
                updateDisplay: function(){
                   // Hide loader, reveal page
                   $("#loader").remove();
                   $("#wrapper").css("opacity","1");
                   // Update Display
                   switch (Custom.linebreak){
                       case "pageonly":
                           linebreakPage(Custom.exportWordbreak);
                           switch (Custom.notes){
                               case "endnote":
                                   endNotes();
                                   break;
                               case "footnote":
                                   footNotes();
                                   break;
                               default:
                                   removeNotes();
                               };
                           break;
                       case "inline":
                           linebreakContinuous(Custom.exportWordbreak);
                           switch (Custom.notes){
                               case "endnote":
                                   endNotes();
                                   break;
                               case "footnote":
                                   footNotes();
                                   break;
                               default:
                                   removeNotes();
                               };
                           break;
                       default:
                           linebreakLine();
                           switch (Custom.notes){
                               case "sideBySide": 
                                   sideNotes();
                                   break;
                               case "line":
                                   lineNotes();
                                   break;
                               case "endnote":
                                   endNotes();
                                   break;
                               case "footnote":
                                   footNotes();
                                   break;
                               default:
                                   removeNotes();
                               };
                   }
                   if (Custom.useLinebreakingString) addLinebreakString(Custom.exportLinebreakString);
                   if (Custom.titlePage) $("#titlePage").show();
                   if (Custom.pageNames) $(".exportFolioNumber").show();
                   if (Custom.imageTags) $(".exportFolioImage").show();
                   if (Custom.columnIDs) $(".exportLineNumber").show();
                   if (!Custom.alternateColors) $(".exportLine").css("background-color","white");
                   if (Custom.pagebreakPrint) $(".exportPage").addClass("pagebreak");
                   if (Custom.metadata) $("#metadataPreview").show();
                   // Prepare and style XML tags
                   XML.process();
                }
            };
            $(function(){
                // Values Customization
                <%
                for (java.util.Enumeration params = request.getParameterNames() ; params.hasMoreElements() ;) {
                    String next = params.nextElement().toString();
                    if(next.startsWith("style")){
                        int index = Integer.parseInt(next.substring(5));
                        out.println("if(!Custom.tagStyle["+index+"])Custom.tagStyle["+index+"] = new Object");
                        out.println("Custom.tagStyle["+index+"].style = '"+request.getParameter(next)+"'");
                    } else if (next.startsWith("stripTag")) {
                        int index = Integer.parseInt(next.substring(8));
                        out.println("if(!Custom.tagStyle["+index+"])Custom.tagStyle["+index+"] = new Object");
                        out.println("Custom.tagStyle["+index+"].strip = '"+request.getParameter(next)+"'");
                    } else if (next.startsWith("tag")) {
                        int index = Integer.parseInt(next.substring(3));
                        out.println("if(!Custom.tagStyle["+index+"])Custom.tagStyle["+index+"] = new Object");
                        out.println("Custom.tagStyle["+index+"].tag = '"+request.getParameter(next).replace("/","") +"'");
                    } else {
                        out.println("Custom."+next+" = '"+request.getParameter(next)+"';");
                    }
               };
                %>
                Custom.updateDisplay();
        });
        function ordinal(n) {
            if (10 < n && n < 14) return n + 'th';
            switch (n % 10) {
                case 1: return n + 'st';
                case 2: return n + 'nd';
                case 3: return n + 'rd';
                default: return n + 'th';
        }
}
            function sideNotes(){
                //align notes side by side
                clearNotes();
                $(".exportText").css({
                    "width":"60%"
                });
                $(".exportNotes").show().css({
                    "width":"35%",
                    "left":"auto"
                });
                $(".exportLineNumber").filter(":visible").css({
                    "display":"block",
                    "width":"5%"
                });
            }
            function lineNotes(){
                //align notes beneath each line
                clearNotes();
                $(".exportText").css({
                    "width":"95%"
                });
                $(".exportNotes").show().css({
                    "display":"block",
                    "width":"95%",
                    "left":"5%"
                });
                $(".exportLineNumber").filter(":visible").css({
                    "width":"5%"
                });
            }
            function endNotes(){
                //collect notes at the end of the document
                clearNotes();
                var endNotes = new Array();
                $(".exportNotes").each(function(){
                    if ($(this).text().length>1) {
                        endNotes.push("<span class='endnote'>",
                            $(this).parents('.exportPage').attr('data-pagenumber'),".",
                            $(this).parents('.exportPage').find('.exportFolioNumber').text(),".",
                            $(this).siblings('.exportLineNumber').text(),": ",
                            $(this).text(),"</span>");
                    }
                });
                $("#endNotes").html(endNotes.join('')).show();
                hideNotes();
            }
            function footNotes(){
                //collect notes at the end of each page
                clearNotes();
                var footNote = new Array();
                $(".exportPage").not("#titlePage,#metadataPreview,#endNotes").each(function(){
                    var $foot = $(this).find(".footNotes");
                    footNote.length = 0;
                    $(this).find(".exportNotes").each(function(){
                        if ($(this).text().length>1) {
                            footNote.push("<span class='footnote'>",
//                                $(this).parents('.exportPage').attr('data-pagenumber'),".",
                                $(this).siblings(".exportLineNumber").text(),": ",
                                $(this).text(),"</span>");
                        }
                    });
                    if(footNote.length>0) $foot.html(footNote.join('')).show();
                });
                hideNotes();
            }
            function hideNotes(){
                //hide the notes and fill the preview with the text
                $(".exportText").css({
                    "width":"95%"
                });
                $(".exportNotes").hide();
                $(".exportLineNumber").filter(":visible").css({
                    "display":"block",
                    "width":"5%"
                });
            }
            function clearNotes(){
                $(".footNotes,#endNotes").empty().hide();
            }
            function removeNotes(){
                clearNotes();
                hideNotes();
            }
            function addLinebreakString(){
                $(".exportLinebreak").html($("#exportLinebreakString").val());
            }
            function linebreakLine(){
                //linebreak at each line of the manuscript
                $(".exportPage,.exportLine,.exportLinebreak,.exportFolioNumber,.exportFolioImage,.exportLineNumber,.exportText,.exportNotes").not("#titlePage,#metadataPreview,#endNotes").attr("style","");
                $("#formatExport").find("input").change();
            }
            function linebreakPage(){
                //linebreak at each page of the manuscript
                linebreakContinuous();
                $(".exportPage").not("#titlePage,#metadataPreview,#endNotes").css("display","block");
            }
            function linebreakContinuous(){
                //remove all linebreaking
                $(".exportLine,.exportLinebreak,.exportPage,.exportText,.exportLineNumber,.exportFolioNumber,.exportFolioImage").not("#titlePage,#metadataPreview,#endNotes").filter(":visible").css({"display":"inline","width":"auto","float":"none","clear":"none","border":"none"});
                $("#formatExport").find("input").change();
                if ($("#pagebreak").prop("checked")); //this will not happen
                //TODO rejoin broken words
            }
            var XML = {
                openTags: new Array(),
                tagName: function(text) {
                    var patt = /^(\w+\b)/;
                    var toret = (patt.exec(text)!=null) ? patt.exec(text)[0].toString() : Infinity;
                    return toret;
                },
                prep: function(){
                    this.openTags.length=0;
                    $(".exportText").each(function(index){
                        var lineContents = $(this).html();
                        var newContent = XML.carryoverTags() + XML.closeAllTags(XML.openAllTags(lineContents)) + XML.endoflineTags();
                        $(this).html(newContent);
                    });
                },
                openAllTags: function(lineContents){
                    var opening = lineContents.split("&lt;");
                    if (opening.length == 1) return lineContents;
                    for (var i=(lineContents.indexOf("&lt;")==0)?0:1;i<opening.length;i++){
                        var thisTag = XML.tagName(opening[i]);
                        if (thisTag == Infinity || (thisTag.length < opening[i].length)){
                            // Close tag name after parameters
                            var endTagName = opening[i].indexOf("&gt;");
                            if(endTagName>0){
                                if(opening[i].charAt(endTagName-1)=="/"){
                                    // self-closing - no content and no tag to add
                                    opening[i] = "<span class='tagName XML_"+thisTag+"'>&lt;"+opening[i].substring(0,endTagName+4)+"</span>"+opening[i].substring(endTagName+4);
                                } else if(thisTag == Infinity){
                                    // Closing tag - let closeAllTags() handle it
                                    opening[i] = "&lt;"+opening[i];
                                } else {
                                    // add to open tags
                                    XML.openTags.push(thisTag);
                                    // close up opening spans, add content tags
                                    opening[i] = "<span class='tagName XML_"+thisTag+"'>&lt;"+opening[i].substring(0,endTagName+4)+"</span><span class='tagContent XML_"+thisTag+"'>"+opening[i].substring(endTagName+4);
                                }
                            } else {
                                // Parsing problem alert
                                opening[i] = "<span class='tagName XML_"+thisTag+"'>&lt;<span class='parseErrorOpen'></span>"+opening[i]+"</span>";
                            }
                        } else {
                            // no tags
                        }
                    }
                    return opening.join("");
                },
                closeAllTags: function(lineContents){
                    var closing = lineContents.split("&lt;");
                    if (closing.length == 1) return lineContents;
                    for (var i=(lineContents.indexOf("&lt;")==0)?0:1;i<closing.length;i++){
                        if(closing[i].charAt(0)=="/"){
                            // This is a standard closing tag
                            var endTagAt = closing[i].indexOf("&gt;",1);
                            if(endTagAt>1){
                                // Get tag name and remove it from the open tag list
                                var endTag = closing[i].substring(1,endTagAt);
                                if (XML.closeTag(endTag)){
                                    // Close open content span, add tag span
                                    closing[i] = "</span><span class='tagName XML_"+endTag+"'>&lt;"+closing[i].substring(0,endTagAt+4)+"</span>"+closing[i].substring(endTagAt+4);
                                } else {
                                    // Parsing error, the tag was not found open
                                    closing[i] = "</span><span class='tagName parseErrorClose XML_"+endTag+"'>&lt;"+closing[i].substring(0,endTagAt+4)+"</span>"+closing[i].substring(endTagAt+4);
                                }
                            } else {
                                // Parsing error, the tag is never closed, skip this one
                                closing[i] = "&lt;"+closing[i];
                            }
                        } else {
                            // Parsing error, this should have been handled by openTags(), skip it
                            closing[i] = "&lt;"+closing[i];
                        }
                    }
                    return closing.join("");
                },
                closeTag: function(tag){
                    for (var i=XML.openTags.length-1;i>-1;i--){
                        // Check from the end to enforce LIFO nesting
                        if (XML.openTags[i]==tag){
                            // Remove and exit
                            XML.openTags.splice(i,1);
                            return true;
                        }
                    }
                    // Exited without finding a match
                    return false;
                },
                carryoverTags: function(){
                    var toret = new Array();
                    for (var i=0;i<XML.openTags.length;i++){
                        // Front to back for FIFO nesting
                        toret.push("<span class='tagContent XML_"+XML.openTags[i]+"'>");
                    }
                    return toret.join("");
                },
                endoflineTags: function(){
                    var toret = new Array();
                    for (var i=0;i<XML.openTags.length;i++){
                        toret.push("</span>");
                    }
                    return toret.join("");
                },
                process: function(){
                    XML.prep();
                    for(var i=0;i<Custom.tagStyle.length;i++){
                        if(Custom.tagStyle){
                            XML.format(Custom.tagStyle[i].tag,Custom.tagStyle[i].style);
                            if(Custom.tagStyle[i].strip!=null) XML.strip(Custom.tagStyle[i].tag)
                        }
                    }
                },
                format: function(tag,format){
                    //apply selected formatting to XML tags
                    if (format=="italic") {
                        $(".XML_"+tag).css("font-style","italic");
                    } else if (format=="bold") {
                        $(".XML_"+tag).css("font-weight","bold");
                    } else if (format=="underlined") {
                        $(".XML_"+tag).css("text-decoration","underline");
                    } else if (format=="paragraph") {
                        $(".XML_"+tag).css({
                            "display":"block",
                            "float":"left",
                            "clear":"left"
                        });
                    } else if (format=="remove"){
                        $(".XML_"+tag).css("display","none"); 
                    }
                },
                reset: function(tag){
                    $(".XML_"+tag).css({
                        "font-style":"",
                        "font-weight":"",
                        "text-decoration":"",
                        "display":"",
                        "float":"",
                        "clear":""
                    });
                },
                strip: function(tag){
                    //strip tag from around content
                    $(".XML_"+tag).filter(".tagName").hide();
                }
            }
    </script>
    </head>
    <body>
        <%
        if (request.getParameter("projectID")!=null){
            textdisplay.Project p=new textdisplay.Project(projectID);
            textdisplay.TagFilter f=new textdisplay.TagFilter(textdisplay.Manuscript.getFullDocument(p,true));
            String [] tags=f.getTags();
            %>
        <div id="wrapper">
                <div id="fullText">
                    <div id="titlePage" class="exportPage editable" contentEditable>
                        <h1 class="ui-corner-all ui-state-default"><%out.print(p.getProjectName());%></h1>
                        <div class="editSpace"></div>
                        <h4>Group Members:</h4>
                            <ul>
                        <%
                            user.Group thisGroup = new user.Group(p.getGroupID());
                            //now list the users
                            User[] groupMembers = thisGroup.getMembers();
                            User[] groupLeader = thisGroup.getLeader();
                            for (int i = 0; i < groupMembers.length; i++) {
                                if (groupLeader[0].getUID() == groupMembers[i].getUID()) {
                                    out.print("<li>" + groupMembers[i].getLname() + "&nbsp;(" + groupMembers[i].getUname() + ")&nbsp;<em>Group&nbsp;Leader</em></li>");
                                } else {
                                    out.print("<li>" + groupMembers[i].getFname() + "&nbsp;" + groupMembers[i].getLname() + "&nbsp;(" + groupMembers[i].getUname() + ")</li>");
                                }
                            }
                            %></ul>
                        <div class="editSpace"></div>
                        <a onclick="$(this).parent().remove();" class="noprint">Remove Title Page</a>
                        <div class="editNote noprint">This section is editable - click to change</div>
                    </div>
                <%
                StringBuilder metadataString = new StringBuilder(120);
                metadataString.append("<span class='mdata clear'>");
                textdisplay.Metadata m=p.getMetadata();
                metadataString.append("Title:"+m.getTitle()+"</span><span class='mdata'>");
                metadataString.append("Subtitle:"+m.getSubtitle() +"</span><span class='mdata'>");
                metadataString.append("MS identifier:"+m.getMsIdentifier()+"</span><span class='mdata'>");
                metadataString.append("MS settlement:"+m.getMsSettlement()+"</span><span class='mdata'>");
                metadataString.append("MS Repository:"+m.getMsRepository()+"</span><span class='mdata'>");
                metadataString.append("MS Collection:"+m.getMsCollection()+"</span><span class='mdata'>");
                metadataString.append("MS id number:"+m.getMsIdNumber()+"</span><span class='mdata'>");
                metadataString.append("Subject:"+m.getSubject()+"</span><span class='mdata'>");
                metadataString.append("Author:"+m.getAuthor()+"</span><span class='mdata'>");
                metadataString.append("Date:"+m.getDate()+"</span><span class='mdata'>");
                metadataString.append("Location:"+m.getLocation()+"</span><span class='mdata'>");
                metadataString.append("Language:"+m.getLanguage()+"</span><span class='mdata'>");
                metadataString.append("Description:"+m.getDescription()+"</span><span class='mdata'>");
                metadataString.append("</span>");
                %><div id="metadataPreview" class="exportPage">
<%
    String header = p.getHeader();
    if(header.length()>0){%>
                    <a onclick="$(this).parent().remove();" class="noprint">Remove Header</a>
                    <h2 class="ui-corner-all ui-state-default">Custom Header</h2><%out.print(header);%></div><%
    } else {
        %>
                    <a onclick="$(this).parent().remove();" class="noprint">Remove Metadata</a>
                    <h2 class="ui-corner-all ui-state-default">Project Metadata</h2><%out.print(metadataString.toString());%></div><%
        }
                Transcription[] exportable;
                textdisplay.Folio eachFolio;
                textdisplay.Folio[] exportFolios = folios;
                int folioLimit = exportFolios.length;
                for (int i=0;i<folioLimit;i++){
                    eachFolio = exportFolios[i];
                  if (!Transcription.projectHasTranscriptions(projectID, eachFolio.getFolioNumber())){
                        out.print("This page has no stored transcription.");
                        continue;
                    }
                    exportable = Transcription.getProjectTranscriptions(projectID, eachFolio.getFolioNumber());
                    int numberOfLines = exportable.length;
                    int columnLineShift = 0;
                    char column='A';
                    int columnPrefix=1; //for more than 26 columns
                %><div class="exportPage" data-pageNumber="<%out.print(i+1);%>">
                    <a onclick="$(this).parent().remove();" class="noprint">Remove This Page</a>
                    <span class="exportFolioNumber"><%out.print(eachFolio.getPageName());%></span>
                    <span class="exportFolioImage"><%out.print(eachFolio.getImageName());%></span><%
                    int oldLeft = exportable[0].getX();
                    for (int line=1;line<numberOfLines+1;line++){
                        if (column>'Z'){
                            columnPrefix++;
                            column -= 26;
                        }
                        StringBuilder lineMarkBuilder = new StringBuilder();
                        for (int iter=columnPrefix;iter>0;iter--){
                            lineMarkBuilder.append(column);
                        }
                        lineMarkBuilder.append(line-columnLineShift).append(" ");
                        String lineMark = lineMarkBuilder.toString();
                    %>
                        <div class="exportLine" data-lineNumber="<%out.print(line);%>">
                            <span class="exportLineNumber" data-lineID="<%out.print(exportable[line-1].getLineID());%>" data-lineNumber="<%out.print(line);%>"  data-column="<%out.print(column);%>"  data-lineOfColumn="<%out.print(line-columnLineShift);%>"><%
                            int columnLeft = exportable[line-1].getX();
                            out.println(lineMark);
                            if (columnLeft > oldLeft){
                                column++;
                                columnLineShift = line;
                                columnLeft = oldLeft;
                            }
                            %>
                            </span>
                            <span class="exportText">
                                <%out.println(exportable[line-1].getText().replace("&amp;", "&"));%>
                            <span class="exportLinebreak"></span>
                            </span>
                            <span class="exportNotes"><%
                            out.print(exportable[line-1].getComment());
                            %></span>
                        </div>
                    <%
                    }%>
                    <span class="footNotes"></span>
                </div>
               <%}
            %>
            <div id="endNotes" class="exportPage"></div>
                </div>
        </div>
            <div id="loader">
                <div id="circleG">
                    <div id="circleG_1" class="circleG"></div>
                    <div id="circleG_2" class="circleG"></div>
                    <div id="circleG_3" class="circleG"></div>
                </div>
            </div>
       <%}else{
            // No projectID passed
            out.print("No project indicated");
        }%>
     </body>
</html>
