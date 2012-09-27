<%-- 
    Document   : exportUI
    Created on : May 25, 2011, 1:23:56 PM
    Author     : jim
--%>

<%@page import="textdisplay.Transcription"%>
<%@page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@page import="org.owasp.esapi.ESAPI" %>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN"
   "http://www.w3.org/TR/html4/loose.dtd">
<%
int projectID=0;
if (request.getParameter("projectID")!=null){
    projectID = Integer.parseInt(request.getParameter("projectID"));
    textdisplay.Project p=new textdisplay.Project(projectID);
    textdisplay.TagFilter f=new textdisplay.TagFilter(textdisplay.Manuscript.getFullDocument(p,true));
    String [] tags=f.getTags();
    %>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title>Export Transcription</title>
        <link rel="stylesheet" href="css/tpen.css" type="text/css" media="screen, projection">
        <link rel="stylesheet" href="css/print.css" type="text/css" media="print">
        <!--[if lt IE 8]><link rel="stylesheet" href="css/ie.css" type="text/css" media="screen, projection"><![endif]-->
        <link type="text/css" href="css/custom-theme/jQuery.css" rel="Stylesheet" />
        <script type="text/javascript" src="https://ajax.googleapis.com/ajax/libs/jquery/1.7.1/jquery.js"></script>
        <script type="text/javascript" src="https://ajax.googleapis.com/ajax/libs/jqueryui/1.8.16/jquery-ui.js"></script>   
        <script type="text/javascript" src="js/tpen.js"></script>   
        <style type="text/css">
            .label { min-width: 75px;display:inline-block;}
            #fullText {width:100%;overflow: auto;position: relative; background: url('images/linen.png') fixed 0 0 repeat;z-index: 3;}
            label {float: none;font-weight: normal;width:auto; padding: 0;display: inline-block;}
            input+.ui-icon{background-color: lightgreen;border-radius:50%;background-position: -144px 0;}
            input:checked+.ui-icon{background-color: lightgray;border-radius:50%;background-position: -16px -208px;}
            fieldset,input[type="submit"] {margin-left: 10px; overflow: hidden;}
            #wrapper{width:auto;padding:0 20px;}
            /*Styling for export preview*/
            .exportPage {border:thin solid black;clear: both; padding: 10px;margin: 0 2px 5px 10px; overflow: auto; background: white;
                            -moz-box-shadow: -1px -1px 3px rgba(0,0,0,.4);
                            -webkit-box-shadow: -1px -1px 3px rgba(0,0,0,.4); 
                            box-shadow:-1px -1px 3px rgba(0,0,0,.4);
            }

            .exportFolioNumber,.exportFolioImage {float: left;font-weight: bold; display: block;width:100%;}
            .exportFolioImage {display: none;}
            .exportLine{float: left;clear:left;display:block;width:100%;}
            .exportLine:nth-child(even) { background-color: whitesmoke; }
            .exportLine:nth-child(odd) { background-color: lightgray; }
            .exportLineNumber {font-style: italic;float: left;width:5%;}
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
            .xmlDisclaimer {display:none;float: left;clear: left;}
            span {position:relative;}
            .footnote,.endnote{display: block;float:left;clear:left;}
            .mdata {display: inline-block; width:240px; float: left;}
            #beginFolio,#endFolio{width:60%;position:absolute;left:110px;}
            #pageRange {width: 100%;position:relative;}
            #pageRange strong {line-height: 20px;}
            #formatting {max-width:600px;min-width: 200px;width:33%;position: relative;float: left;}
            .optionPanel{position: relative;width: 100%;}
            #metadataPreview {display: none;}
            .highlight {background-color: yellow;}
            #tagsList {position: relative; max-height: 400px;overflow: auto;}
            #selectOptions a {
                width: 40%;
                padding: 4px;
                text-decoration: none;
                display: inline-block;
                margin: 0 4%;
            }
            #tagTable select {width:100%;}
            #endNotes {display:none;}
            .footNotes {border-top: 1px solid lightgray;display:none;overflow: auto;width:100%;}
            .exportPage {
                -webkit-transition:margin-bottom 1s;
                -moz-transition:margin-bottom 1s;
                -o-transition:margin-bottom 1s;
                transition:margin-bottom 1s;
            }
	</style>
        <script type="text/javascript">
            function pageWidth() {return window.innerWidth != null? window.innerWidth: document.body != null? document.body.clientWidth:null;}
            function pageHeight() {return window.innerHeight != null? window.innerHeight: document.body != null? document.body.clientHeight:null;} 
//            document.write("<style type='text/css'>#fullText {height:" + (pageHeight()-200) + "px; width:" + (pageWidth()-405) +"px;}</style>");
            $(function(){
                $(".format").each(function(){
                    $(this).add($(this).next("div")).wrapAll("<div class='optionPanel left'></div>");
                })
                $("select").change(function(){
                    var $tag = "#fullText "+$(this).prev().text();
                    $($tag).css({
                        "font-style"    :   "normal",
                        "font-weight"   :   "normal",
                        "text-decoration":  "none",
                        "display"       :   "inline"
                    });
                    switch ($(this).attr("selectedIndex")){
                        case 1: $($tag).css("font-style", "italic");break;
                        case 2: $($tag).css("font-weight", "bold");break;
                        case 3: $($tag).css("text-decoration", "underline");break;
                        case 4: $($tag).css("display", "none");break;
                        default: break;
                    }
                }).hover(function(){
                    $("#fullText "+$(this).prev().text()).toggleClass("highlight");
                });
                $(".format").click(function(){
                    $(this).next("div").slideToggle();
                })
                $(".formatDiv").addClass('ui-corner-all');
/* Handlers for Export Options */
            $("#pageNames").change(function(){
                if ($(this).prop('checked')){
                    $(".exportFolioNumber").show();
                } else {
                    $(".exportFolioNumber").hide();
                }
            });
            $("#TPENImageTags").change(function(){
                if ($(this).prop('checked')){
                    $(".exportFolioImage").show();
                } else {
                    $(".exportFolioImage").hide();
                }
            });
            $("#columnIDs").change(function(){
                if ($(this).prop('checked')){
                    $(".exportLineNumber").show();
                } else {
                    $(".exportLineNumber").hide();
                }
            });
            $("#alternateColors").change(function(){
                if ($(this).prop('checked')){
                    $(".exportLine").css("background-color","");
                } else {
                    $(".exportLine").css("background-color","white");
                }
            });
            $("[name='notes']").change(function(){
                var selected = $("[name='notes']").filter(":checked").val();
                switch (selected){
                    case "sideBySide"  : 
                        sideNotes();
                        break;
                    case "line" :
                        lineNotes();
                        break;
                    case "endnote"   :
                        endNotes();
                        break;
                    case "footnote" :
                        footNotes();
                        break;
                    case "remove" :
                        removeNotes();
                        break;
                }
            });
            $("[name='linebreak']").change(function(){
                var selected = $("[name='linebreak']").filter(":checked").val();
                switch (selected){
                    case "newline"  : 
                        linebreakLine();
                        $("#exportHyphenation").attr("disabled",false);
                        break;
                    case "pageonly" :
                        linebreakPage();
                        break;
                    case "inline"   :
                        linebreakContinuous();    
                }
            });
            $("#pagebreak").change(function(){
                if ($(this).prop('checked')){
                    $(".exportPage").css("margin-bottom","");
                } else {
                    $(".exportPage").css("margin-bottom","0px");
                }
            });
            $("#formatXML").find("select").on({
                change: function(){
                    var tag = $(this).parent().next().find("input[name^='tag']").val().replace("/","");
                    var format = $(this).val();
                    if      (format=="none") {
                        XML.reset(tag);
                        if ($(this).next("label").children("input").prop('checked')) XML.strip(tag);
                    }
                    else    XML.format(tag, format);
                },
                mouseenter: function(){
                    var tag = $(this).parent().next().find("input[name^='tag']").val().replace("/","");
                    $(".XML_"+tag).addClass('highlight');
                },
                mouseleave: function(){
                    var tag = $(this).parent().next().find("input[name^='tag']").val().replace("/","");
                    $(".XML_"+tag).removeClass('highlight');
                }
            });
            $("#formatXML").find("input[name^='stripTag']").change(function(){
                var tag = $(this).parent().next("input[name^='tag']").val().replace("/","");
                if ($(this).prop('checked')){
                    $(this).next().attr('title','Show <tag>');
                    XML.strip(tag);
                } else if ($(".XML_"+tag).filter(".tagContent").is(":visible")) {
                    XML.reset(tag);
                    $(this).next().attr('title','Strip <tag>');
                }
            });
            $("#metadataOption").change(function(){
                if ($("#metadataSelect").prop('checked')) $("#metadataPreview").slideDown();
                else $("#metadataPreview").slideUp();
            });
            $("#beginFolio,#endFolio").change(function(){
                var firstFolio = $("#beginFolio").children("option:selected").index();
                var lastFolio = $("#endFolio").children("option:selected").index();
                if (firstFolio > lastFolio) {
                    $("#beginFolio,#endFolio").addClass("ui-state-error").attr("title","Folio range does not include any pages.");
                } else {
                    $("#beginFolio,#endFolio").removeClass("ui-state-error").attr("title","");
                }
            });
            var dropdowns='<%out.print(ESAPI.encoder().decodeFromURL(p.getFolioDropdown()));%>';
            $("#pageRange").find("select").each(function(index){
                $(this).append(dropdowns)
                .children("option").val(function(){
                    return parseInt($(this).val(),10);
                }).eq(-index).attr("selected",true);
            });
            $("#all").click(function(){
                var tagList = $("input[name^='stripTag']");
                if($(this).attr('select')=='false'){
                    tagList.prop('checked',false);
                    $(this).attr('select',true).text('Strip All Tags');
                } else {
                    tagList.prop('checked',true);
                    $(this).attr('select',false).text('Show All Tags');
                }
                tagList.change();
            });
            $("#inv").click(function(){
                var tagList = $("input[name^='stripTag']");
                if (tagList.filter(":checked").length == 0){
                    $("#all").attr('select',true).click();
                    return false;
                }
                tagList.each(function(){
                    if ($(this).prop('checked')){
                        $(this).prop('checked',false);
                    } else {
                        $(this).prop('checked',true);
                    }
                }).change();
            });
            XML.prep();
            $("#formatXML").find("input[name^='stripTag']").change();
        });
        function validForm(){
            var firstFolio = $("#beginFolio").children("option:selected").index();
            var lastFolio = $("#endFolio").children("option:selected").index();
            if (firstFolio <= lastFolio) {
                return true;
            } else {
                alert("The "+ordinal(firstFolio+1)+" page comes after the "+ordinal(lastFolio+1)+" page in this project.\nPlease check your page range.");
                return false;
            }
        }
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
                $(".exportLineNumber").css({
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
                $(".exportLineNumber").css({
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
                    if(footNote.length>0) $foot.html(footNote.join('')).css("display","block");
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
                $(".exportPage,.exportLine,.exportLinebreak,.exportFolioNumber,.exportFolioImage,.exportLineNumber,.exportText,.exportNotes").attr("style","");
                $("#formatExport").find("input").change();
            }
            function linebreakPage(){
                //linebreak at each page of the manuscript
                linebreakContinuous();
                $(".exportPage").css("border","thin dashed black").css("display","block");
            }
            function linebreakContinuous(){
                //remove all linebreaking
                $(".exportLine,.exportLinebreak,.exportPage,.exportText,.exportLineNumber,.exportFolioNumber,.exportFolioImage").css({"display":"inline","width":"auto","float":"none","clear":"none","border":"none"});
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
        <div id="wrapper">
            <div id="header"><p align="center" class="tagline">transcription for paleographical and editorial notation</p></div>
            <div id="content">
                <h1><script>document.write(document.title); </script></h1>
                <div id="main" class="ui-widget ui-widget-content ui-corner-all ui-tabs ui-helper-reset ui-helper-clearfix">
            <h3 class="ui-widget-header ui-tabs ui-corner-all ui-state-default">Exporting <%out.print(p.getProjectName());%></h3>
            <div id="formatting">
                <h3>Export Options</h3>
                <form action="exported.jsp" target="_blank" method="get" onsubmit="return validForm();">
                <div class="format tpenButton">General Formatting</div>
                <div id="formatExport" class="formatDiv">
                    <span class='label left'>Include:</span>
                        <span class="left">
                            <label for="titlePage" title="Show Title Page on exported project"><input id="titlePage" type="checkbox" checked name="titlePage" value="true"/>Title Page</label><br />
                            <label for="pageNames" title="Page Names"><input id="pageNames" type="checkbox" checked name="pageNames" value="true"/>Page Names</label><br />
                            <label for="TPENImageTags" title="Image Location"><input id="TPENImageTags" type="checkbox" name="imageTags" value="true"/>Image Labels</label><br />
                            <label for="columnIDs" title="Sequential Columns and Lines"><input id="columnIDs" type="checkbox" checked name="columnIDs" value="true">Column and Line Marker</label><br />
                            <label for="alternateColors" title="Alternate formatting on each line"><input id="alternateColors" type="checkbox" name="alternateColors" value="true">Alternate Line Colors</label><br />
                            <label for="pagebreak" title="Print each page of transcription on its own page (print only)"><input id="pagebreak" type="checkbox" checked name="pagebreakPrint" value="true">Pagebreaks for Printing</label><br />
<%if (p.getHeader().length()>0){%>
                            <label id="metadataOption" for="metadataSelect" title="Include custom header in the exported document"><input id="metadataSelect" type="checkbox" name="metadata" />Custom Header</label><br />
<%}else{%>
                            <label id="metadataOption" for="metadataSelect" title="Include project metadata in the exported document"><input id="metadataSelect" type="checkbox" name="metadata" />Project Metadata</label><br />
                            <%}%>
                            <!--                        </span>
                    <span class='label left clear-left'>Colors:</span>
                        <span class="left">
                            <label for="bw" title="Standard output"><input id="bw" type="radio" checked name="color" value="bw"/>Black &amp; White</label><br />
                            <label for="color" title="Colors are defined by the project buttons"><input disabled id="color" type="radio" name="color" value="color">Color Tags</label><br />-->
                        </span>
                </div>
                <div class="format tpenButton">Exported Page Range</div>
                <div id="formatMetadata" class="formatDiv">
                    <div id="pageRange">
                        <strong>Start with page:</strong><select id='beginFolio' name='beginFolio'></select><br/>
                        <strong>End with page:</strong><select id='endFolio' name='endFolio'></select><br/>
                    </div>
                </div>
                <div class="format tpenButton">Include Notes</div>
                <div id="formatNotes" class="formatDiv">
                        <label id="sideBySide" for="notesSideBySide" title="Show notes to the side of each line"><input id="notesSideBySide" type="radio" name="notes" checked value="sideBySide"  />Side-by-side</label><br />
                        <label id="noteLine" for="notesLine" title="Show notes underneath each line"><input id="notesLine" type="radio" name="notes" value="line" />Beneath each line</label><br />
                        <label id="endnote" for="notesEndnote" title="Show notes at the end of the document"><input id="notesEndnote" type="radio" name="notes" value="endnote" />Endnotes</label><br />
                        <label id="footnote" for="notesFootnote" title="Show notes after each page of the manuscript"><input id="notesFootnote" type="radio" name="notes" value="footnote" />Footnotes</label><br />
                        <label id="noteRemove" for="notesRemove" title="Remove notes from exported document"><input id="notesRemove" type="radio" name="notes" value="remove" />Remove</label>
                </div>
<!--                <div class="format tpenButton">Include Annotations</div>
                <div id="formatAnnotations" class="formatDiv">
                        <label id="annoList" for="annosList" title="Include list of annotations at the end of each page"><input id="annosList" type="radio" name="annotations" value="list" />List</label><br />
                        <label id="annoText" for="annosText" title="Include text only from annotations at the end of each page"><input id="annosText" type="radio" name="annotations" value="text" />Text</label><br />
                        <label id="annoNone" for="annosNone" title="Exclude annotations from exported document"><input id="annosNone" type="radio" name="annotations" checked value="exclude" />Exclude</label>
                </div>-->
                <div class="format tpenButton">Format XML Tags</div>
                <div id="formatXML" class="formatDiv">
                <%
                if (tags.length < 1){
                    out.print("No XML tags were detected in this text.");
                } else {%>
                    <div id="selectOptions">
                        <a id="all" class="tpenButton" select=false>Show All Tags</a>
                        <a id="inv" class="tpenButton">Invert Selection</a>
                    </div>
                    <div id="tagsList">
                        <table id="tagTable">
                    <%
                    out.print("<input type='hidden' name=projectID value="+request.getParameter("projectID")+">");          
                    for(int i=tags.length-1;i>-1;i--) {
                        out.print("<tr><td class='label'>"+tags[i]+"</td>");
                        out.print("<td><input type='hidden' value='"+tags[i]+"' name='tag"+(i)+"'/>");
                        out.print("<select name='style"+(i)+"' >");
                        out.print("<option value='none' selected>No Change</option>");
                        out.print("<option value='italic' >Italic</option>");
                        out.print("<option value='bold' >Bold</option>");
                        out.print("<option value='underlined'>Underlined</option>");
                        out.print("<option value='paragraph'>New Paragraph</option>");
                        out.print("<option value='remove' >Remove</option>");
                        out.print("</select></td><td><label for='stripTag"+(i+1)+"' title='Show <tag>'><input class='stripTag hide' type='checkbox' checked id='stripTag"+(i+1)+"' name='stripTag"+(i+1)+"' /><span class='ui-icon ui-icon-carat-2-e-w'></span></label><input type='hidden' name='tag"+(i+1)+"' value='"+tags[i]+"'></td></tr>");
                    }%>
                        </table>
                    </div>
                    <%
                }
                %>
                </div>
                <div class="format tpenButton">Linebreaking Layout</div>
                <div id="formatLinebreak" class="formatDiv">
                    <label id="linebreakLine" class="xmlHide" title="Start each line of transcription on a new line" for="newline"><input id="newline" type="radio" name="linebreak" value="newline" checked />Start a new line</label><br />
                    <label id="linebreakPage" class="xmlHide" title="Linebreak only at a new page" for="pageonly"><input id="pageonly" type="radio" name="linebreak" value="pageonly" />Page break only</label><br />
                    <label id="linebreakContinuous" title="Remove all linebreaks" for="inline"><input id="inline" type="radio" name="linebreak" value="inline" />Continuous text</label><br />
                    <label id="exportHyphenation" class="" title="Use hyphenation to join words broken across lines" for="exportWordbreak"><input id="exportWordbreak" name="exportWordbreak" type="text" placeholder="/-/" value="<%//out.print(p.getWordbreakString);%>" /> custom word break string</label><br />
                    <label id="exportLinebreak" class="" title="Use this string with linebreaking" for="exportLinebreakString"><input id="exportLinebreakString" name="exportLinebreakString" type="text" placeholder="&lsaquo;lb /&rsaquo;" value="<%//out.print(p.getLinebreakString);%>" /> custom line break string</label><br />
                    <label id="exportUseLinebreakString" class="" for="useLinebreakingString" title="Use this string in exported document"><input id="useLinebreakingString" type="checkbox" />Use linebreak string in exported document</label>
                </div>
                <input class="tpenButton ui-button clear left" type="submit" value="Build File">
            </form>
                </div>
                    <div>
                        <h3>Preview of Export (first 3 images)</h3>
                    </div>
                    <div id="fullTextDiv">
<!--                        <span id="updatePreview" class="right ui-button ui-state-default ui-corner-all">Update Preview</span>-->
                    <div id="fullText">
                <%
                StringBuilder metadataString = new StringBuilder(120);
                String header = p.getHeader();
if (header.length()>0){
    metadataString.append(p.getHeader());
    if (header.length()>p.getLinebreakCharacterLimit()-1){
        out.print("<span class='ui-state-error-text'>Preview limited to first "+p.getLinebreakCharacterLimit()+" characters.</span>");
    };
       }else{
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
               }%><div id="metadataPreview" class="exportPage"><h4>Project Metadata</h4><%out.print(metadataString.toString());%></div><%
                // Validate the inserted transcription text to be sure there are no dangling <div> tags to throw off the rest of the page
/*                String fullText = textdisplay.manuscript.getFullDocument(p);
               String[] openDiv = fullText.split("<div");
                if (fullText.indexOf("<div>")>fullText.indexOf("</div>")) fullText ="<div>"+fullText;
                for (int i=(fullText.indexOf("<div>")==0)?0:1;i<openDiv.length;i++){        //test for starting position
                    int closeTest = openDiv[i].split("</div>").length;
                    if (closeTest > 2 ) {
                        for (int j=0;j<closeTest-1;j++){
                            fullText = "<div>"+fullText;
                  //          out.println("opened");
                        }
                    } else if (closeTest == 2 ){
                        fullText += "</div>";
                  //          out.println("closed");
                    }
                }
               // out.println(openDiv.length+", "+fullText.split("</div>").length);
                out.print(ESAPI.encoder().encodeForHTML(fullText));//ESAPI.encoder().encodeForHTML(*/
                Transcription[] exportable;
                textdisplay.Folio eachFolio;
                textdisplay.Folio[] exportFolios = p.getFolios();
                int folioLimit = (exportFolios.length>3) ? 3:exportFolios.length;   //prevent runaway previewing
                for (int i=0;i<folioLimit;i++){
                    eachFolio = exportFolios[i];
                    exportable = Transcription.getProjectTranscriptions(projectID, eachFolio.getFolioNumber());
//                    textdisplay.line[] columnCheck = p.getLines(eachFolio.getFolioNumber());
                    int numberOfLines = exportable.length;
                    int columnLineShift = 0;
                    char column='A';
                    int columnPrefix=1; //for more than 26 columns
                %><div class="exportPage" data-pageNumber="<%out.print(i+1);%>">
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
                    //    exportable = new transcription(exportFolios[i].getFolioNumber(), line, p.getProjectID(), false);%>
                        <div class="exportLine" style="background-color:white;" data-lineNumber="<%out.print(line);%>">
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
        <%}else{
            // No projectID passed
            out.print("No project indicated");
        }%>
                </div>
        <a class="returnButton" href="project.jsp?projectID=<%out.print(request.getParameter("projectID"));%>" >Return to project page</a>
        <%if (request.getParameter("p") != null){%>
        <a class="returnButton" href="transcription.jsp?projectID=<%out.print(request.getParameter("projectID")+"&p="+request.getParameter("p"));%>" >Return to transcribing</a>
         <%}%>   </div>
        </div>
    </body>
</html>
