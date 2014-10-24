<%-- 
    Document   : buttons
    Created on : Oct 26, 2010, 1:38:08 PM
    Author     : jdeerin1
--%> 
<%@page import="textdisplay.Project"%>

<%
            int UID = 0;
            if (session.getAttribute("UID") == null) {
%>              <%@ include file="loginCheck.jsp" %><%
            } else {
                UID = Integer.parseInt(session.getAttribute("UID").toString());
            }%>
<%@page import ="textdisplay.Hotkey"%>
<%@page import ="textdisplay.TagButton"%>
<%@page import ="user.*"%>
<%@page contentType="text/html; charset=UTF-8"  pageEncoding="UTF-8"%>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN"
    "http://www.w3.org/TR/html4/loose.dtd">
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title>Button Management</title>
        <link rel="stylesheet" href="css/tpen.css" type="text/css" media="screen, projection">
        <link rel="stylesheet" href="css/print.css" type="text/css" media="print">
        <!--[if lt IE 8]><link rel="stylesheet" href="css/ie.css" type="text/css" media="screen, projection"><![endif]-->
        <link type="text/css" href="css/custom-theme/jQuery.css" rel="Stylesheet" />
        <link type="text/css" href="css/jquery.simple-color-picker.css" rel="Stylesheet" />
        <script type="text/javascript" src="https://ajax.googleapis.com/ajax/libs/jquery/1.7.1/jquery.js"></script>
        <script type="text/javascript" src="https://ajax.googleapis.com/ajax/libs/jqueryui/1.8.16/jquery-ui.js"></script> 
        <script type="text/javascript" src="js/tpen.js"></script>
<!--        <script src="js/jquery.simple-color-picker.js" type="text/javascript"></script>-->
        <style type="text/css" >
                input[type=text]{font-size: 14px;width: 200px;}
                #sortable1 .ui-sortable-helper{padding: 0.4em; padding-left: 1.5em; font-size: 1.4em; width: 180px !important; height:44px !important;}
                #sortable2 .ui-sortable-helper{padding: 0.4em; padding-left: 1.5em; font-size: 1.4em; width: 430px !important; height:36px !important;}
                #sortable1 .ui-state-highlight{min-height:44px;}
                #sortable2 .ui-state-highlight{min-height:36px;}
                #sortable { margin: 0; padding: 0; width: 30%; }
                #sortable1 li, #sortable1b li { margin: 0 3px 3px 3px; padding: 0.4em; padding-left: 1.5em; font-size: 1.4em; width: 180px; position:relative;}
                #sortable1 input[type=text] {width: 80px;}
                #sortable2 li { margin: 0 3px 3px 3px; padding: 0.4em; padding-left: 1.5em; width:430px; position:relative;}
                span.tag {position:relative;}
                span.tag:before {
                    content:"\003c";
                    position: absolute;
                    left:5px;
                }
                span.tag:after {
                    content:"\003e";
                    position:absolute;
                    right:5px;
                }
                span.tag input {width: 160px;font-weight: bold;padding:1px 20px;}
                a.ui-icon-closethick:hover {background-image: url(css/custom-theme/images/ui-icons_cd0a0a_256x240.png);}
                .disabled { margin: 0 3px 3px 23px; padding: 0.4em; padding-left: 1.5em; font-size: 1.4em; height: 18px;width:650px;}
/*                input.colors {width:16px !important; height: 16px !important;background-color:transparent;background-image: url("images/color-picker.png");position: absolute;left: 55%;}
                input.colors:focus {color:black !important;background: white none !important;width:84px !important;}*/
                .lastRow,.secondRow {display: none;}
                .moreParameters,.ui-icon-closethick {cursor: pointer;margin:2px -5px;}
                .collapseXML{display: none;}
                .importButton {padding:0.4em 1em;position: relative;}
                .badInfo {border: thin solid red !important; box-shadow:inset 0 0 10px red;}
                .colors {display: none;} /* TODO show when colors are supported */
                #codeSearch{padding: .4em 1em;width:145px;}
                .tagWarning {position: absolute;top:0;left:0;}
                .tag .tagWarning {top:-18px;font-size: smaller;white-space: nowrap;}
        </style>
    <%
            user.User thisUser = null;
            if (session.getAttribute("UID") != null) {
                thisUser = new user.User(Integer.parseInt(session.getAttribute("UID").toString()));
                }
            Project thisProject = null;
            String projectAppend = "";
            int projectID = 0;
            if (request.getParameter("projectID") == null){
                textdisplay.Project[] p = thisUser.getUserProjects();
                if (p.length > 0) {
                    projectID = p[0].getProjectID();
                }
                if (projectID > 0) {
                    %>
                    <script type="text/javascript">
                        document.location = "buttons.jsp?projectID=<%out.print(projectID);%>";
                    </script>
        <%
                    //response.sendRedirect("buttons.jsp?projectID=" + projectID);
                } else {
                String errorMessage = "No project has been indicated.";
            %><%@include file="WEB-INF/includes/errorBang.jspf" %><%
                }
                return;
            }
            String p = "";
            if (request.getParameter("p") != null){
                p = request.getParameter("p");
            }
            projectID = Integer.parseInt(request.getParameter("projectID"));
            thisProject = new Project(projectID);
            projectAppend = (p.length()>0)? "&projectID=" + projectID + "&p=" + p : "&projectID=" + projectID;
            out.println("<script>");
            out.println("var projectID="+projectID+";");
            out.println("var projectAppend='"+projectAppend+"';");
            out.println("</script>");                               
%>
    <script type="text/javascript">
	$(function() {
//            $( ".returnButton" ).css("display","inline-block");
            $( ".sortable" ).disableSelection();
            $( "#sortable1, #sortable2" ).sortable({
                connectWith: ".connectedSortable",
                placeholder: "ui-state-highlight ui-state-disabled",
                axis:'y'
            })
                .css("cursor","move");
            var $tabs = $( "#tabs" ).tabs();
            $(".tag").children('input').change(function(){
                unsavedAlert('#tabs-2');
                isValidTag(this);
            });
            $(".moreParameters").click(function(){
                $(this)
                    .hide()
                    .parent().next().slideDown();
            });
            $(".xmlPanel").focusin(function() {
                $(".xmlPanel").not(this).each(function(){collapsePanel(this);});
                expandPanel(this);
            });
            $("#addT").click(function(){
                var addTagData = {projectID:projectID};
                $.post("addTag", $.param(addTagData),function(data){
                    var position = data;    //tag position from servlet
                    $("#sortable2").children("li").eq(-1).clone(true).appendTo($("#sortable2"))
                    .children("input.description").attr("name", "description"+position).val("New Tag").end()
//                    .children("input.colors").attr("name","xmlColor"+position).val("black").end()
                    .children(".xmlParams").find("input").each(function(index,param){
                        var rename = (index > 0) ? "b"+position+"p"+index :  "b"+position;
                        $(param).attr("name", rename).val("");
                    });
                    $("[name='b"+position+"']").val("New Tag");                        
                },"html");           
            });
            $("#addH").click(function(){
                var addHotkeyData = {projectID:projectID};
                $.post("addHotkey", $.param(addHotkeyData),function(data){
                    var position = data;    //tag position from servlet
                    $("#sortable1").children("li").eq(-1).clone(true).appendTo($("#sortable1"))
                    .children("input.label").attr("id", "a"+position+"a").val("-").end()
                    .children("input.shrink").attr({
                        "name":"a"+position,
                        "id":"a"+position
                    }).val("42").end()
                    .children("a").attr("onclick","deleteHotkey("+position+");");
                },"html");           
            });
            $('#tabs').tabs({
                show:equalWidth,
                selected:<%if (request.getParameter("selecTab") != null) {
                out.print(request.getParameter("selecTab"));
            } else {
                out.print('0');
            };%>
            });
            //$('input:text').not(".colors").focus(hideBox(box));
//            $('input.colors')
//                .focusin(function(){$(".color-picker").hide();$(this).select();})
//                .blur(function() {
//                    var newColor = $(this).val();
//                    var bgColor = "#444";
//                    var color = (colorNameToHex(newColor).length == 7) ? colorNameToHex(newColor).substr(1,1)+colorNameToHex(newColor).substr(3,1)+colorNameToHex(newColor).substr(5,1) : colorNameToHex(newColor).substr(1,3);
//                    if(parseInt(color)==NaN)color="FFF";
//                    $(this).css({'color':'black','background':newColor});
//                    if (color<"888"){
//                        //dark color selected
//                        bgColor = "white";
//                        $(this).css({'color':'white'});
//                    }
//                    $(this).prev().css({'color':newColor,'background':bgColor});
//                    $(".color-picker").hide("fade");
//                })
//                .simpleColorPicker({ showEffect: 'slide', hideEffect: 'fade' });
            $('.ui-icon-closethick').hover(function()
                {$(this).parent().addClass("ui-state-error");},
                function(){$(this).parent().removeClass("ui-state-error");}
            );
            $('.toggleXML').click(function(){
                if ($(this).hasClass("ui-icon-arrow-4")) {
                    expandPanel($(this).parent("li"));
                } else {
                    collapsePanel($(this).parent("li"));
                }
            });
            $(".lookLikeButtons").prop("onclick",null);
        });
var minWidth = 70;
function equalWidth(){
    $("#xmlReview").children("span").each(function(){
        minWidth = ($(this).width()>minWidth) ? $(this).width() : minWidth;
    }).css({"min-width":minWidth+"px"});
}
    function expandPanel(btn) {
        $(btn)
            .find(".collapseXML").switchClass("collapseXML","expandXML").end()
            .find(".ui-icon-arrow-4").switchClass("ui-icon-arrow-4","ui-icon-arrowstop-1-n").end()
            .find(".xmlParams").slideDown();
    }
    function collapsePanel(btn) {
        $(btn)
            .find(".xmlParams").slideUp().end()
            .find(".expandXML").switchClass("expandXML","collapseXML").end()
            .find(".ui-icon-arrowstop-1-n").switchClass("ui-icon-arrowstop-1-n","ui-icon-arrow-4");
    }
    function updatea(obj) {
        var objValue = obj.value;
        var decimalTest =/^[0-9]+(\.[0-9]+)+$/;
        if (isNaN(objValue) || objValue < 32 || objValue > 65518){
            $(obj).addClass('badInfo').prev("input").val(" ").parent().attr("title","Please use a valid unicode decimal (1-65518)\nUse the link on this page to find a complete list.");
        } else {
            if (!objValue.match(decimalTest)){
                obj.value = parseInt(objValue);
            }
            $(obj).removeClass('badInfo').prev("input").val(String.fromCharCode(objValue)).parent().attr("title","");
    //        var idnum=obj.id;
    //        idnum+="a";
    //        document.getElementById(idnum).value=String.fromCharCode(obj.value);
            unsavedAlert("#tabs-1");
        }
    }
    /* Check for self-closing */
    function checkType ($tag) {
        var tagName = $tag.find(".tag").val();
        if (tagName.lastIndexOf("/") == (tagName.length-1)){
            $tag.addClass("selfClosing");
        }
    }
    /* Swap ids to reorder the buttons when saved */
    function rebuildOrder() {
        //interrupt saving if bad data is included
        if($(".badInfo").length > 0){
            var thisValue = $(".badInfo").val();
            alert("\""+thisValue+"\" is not valid unicode.\n\nPlease enter a number from 32-65518.");
            $(".badInfo").show('pulsate',500);
            return false;
        }
        var hotkeyLoop = $("#tabs-1").find("input:text").not(".label");
        var xmlLoop = $("#tabs-2").find("input:text");
        for (i=0;i<hotkeyLoop.length;i++){
            hotkeyLoop[i].name = "a"+(i+1);
        }
        for (i=0;i<xmlLoop.length;i=i+7) {
            xmlLoop[i].name = "description"+(i/7+1);
//            xmlLoop[i+1].name = "xmlColor"+(i/8+1);
            xmlLoop[i+1].name = "b"+(i/7+1);
            for (j=0;j<5;j++){                  //parameters
                xmlLoop[i+2+j].name = "b"+(i/7+1)+"p"+(j+1);
                if (xmlLoop[i+2+j].value == 'null') xmlLoop[i+2+j].value = '';
            }
        }
        return true;
    }
    function unsavedAlert(idRef) {
        $("#tabs").find("a[href='"+idRef+"']").next().stop(true,true).show("pulsate","fast");
    }
    function isValidTag(tag){
        var tagVal = $(tag).val();
        var isIllegalChar  = /[^\w:\.\-]/;
        var hasSpace = /\s/;
        var validStart = /^\w|:/
        var notDigitStart = /^\D/
        var xmlStart = /^xml/i;
        var msg = '';
        if (hasSpace.test(tagVal)){
            msg = ['alert','error','XML does not allow spaces in tag names'];
        } else if (isIllegalChar.test(tagVal)){
            msg = ['alert','error','XML allows only letters, digits, .,-,_, and : in tag names'];
        } else if (!validStart.test(tagVal) || !notDigitStart.test(tagVal)){
            msg = ['alert','error','XML tags must begin with a letter, _, or :'];
        } else if (xmlStart.test(tagVal)){
            msg = ['info','highlight','Beginning tag names with "XML" should be avoided'];
        }
        $(tag).parents('.xmlPanel').find('.tagWarning').remove();
        if (msg.length == 3){
            var warning     = "<div class='tagWarning ui-corner-all ui-state-"+msg[1]+"'><span class='ui-icon left ui-icon-"+msg[0]+"'></span>"+msg[2]+"</div>";
            var warningFlag = "<div class='tagWarning ui-corner-all ui-state-"+msg[1]+"'><span class='ui-icon left ui-icon-"+msg[0]+"' title='"+msg[2]+"'></span></div>";
            $(tag).after(warning)
            .parents('.xmlPanel').find('.description').after(warningFlag);
        }
    }
    function deleteHotkey(position){
        $("#buttonForm")
            .append("<input type='hidden' value=true name='deletehotkey'/><input type='hidden' value="+position+" name='position'/>")
            .submit();
    }
    function deleteTag(position){
        $("#buttonForm")
            .append("<input type='hidden' value=true name='deletetag'/><input type='hidden' value="+position+" name='position'/>")
            .submit();
    }
//    function colorNameToHex(color)
//{
//    var colors = {"aliceblue":"#f0f8ff","antiquewhite":"#faebd7","aqua":"#00ffff","aquamarine":"#7fffd4","azure":"#f0ffff",
//    "beige":"#f5f5dc","bisque":"#ffe4c4","black":"#000000","blanchedalmond":"#ffebcd","blue":"#0000ff","blueviolet":"#8a2be2","brown":"#a52a2a","burlywood":"#deb887",
//    "cadetblue":"#5f9ea0","chartreuse":"#7fff00","chocolate":"#d2691e","coral":"#ff7f50","cornflowerblue":"#6495ed","cornsilk":"#fff8dc","crimson":"#dc143c","cyan":"#00ffff",
//    "darkblue":"#00008b","darkcyan":"#008b8b","darkgoldenrod":"#b8860b","darkgray":"#a9a9a9","darkgreen":"#006400","darkkhaki":"#bdb76b","darkmagenta":"#8b008b","darkolivegreen":"#556b2f",
//    "darkorange":"#ff8c00","darkorchid":"#9932cc","darkred":"#8b0000","darksalmon":"#e9967a","darkseagreen":"#8fbc8f","darkslateblue":"#483d8b","darkslategray":"#2f4f4f","darkturquoise":"#00ced1",
//    "darkviolet":"#9400d3","deeppink":"#ff1493","deepskyblue":"#00bfff","dimgray":"#696969","dodgerblue":"#1e90ff",
//    "firebrick":"#b22222","floralwhite":"#fffaf0","forestgreen":"#228b22","fuchsia":"#ff00ff",
//    "gainsboro":"#dcdcdc","ghostwhite":"#f8f8ff","gold":"#ffd700","goldenrod":"#daa520","gray":"#808080","green":"#008000","greenyellow":"#adff2f",
//    "honeydew":"#f0fff0","hotpink":"#ff69b4",
//    "indianred ":"#cd5c5c","indigo ":"#4b0082","ivory":"#fffff0","khaki":"#f0e68c",
//    "lavender":"#e6e6fa","lavenderblush":"#fff0f5","lawngreen":"#7cfc00","lemonchiffon":"#fffacd","lightblue":"#add8e6","lightcoral":"#f08080","lightcyan":"#e0ffff","lightgoldenrodyellow":"#fafad2",
//    "lightgrey":"#d3d3d3","lightgreen":"#90ee90","lightpink":"#ffb6c1","lightsalmon":"#ffa07a","lightseagreen":"#20b2aa","lightskyblue":"#87cefa","lightslategray":"#778899","lightsteelblue":"#b0c4de",
//    "lightyellow":"#ffffe0","lime":"#00ff00","limegreen":"#32cd32","linen":"#faf0e6",
//    "magenta":"#ff00ff","maroon":"#800000","mediumaquamarine":"#66cdaa","mediumblue":"#0000cd","mediumorchid":"#ba55d3","mediumpurple":"#9370d8","mediumseagreen":"#3cb371","mediumslateblue":"#7b68ee",
//    "mediumspringgreen":"#00fa9a","mediumturquoise":"#48d1cc","mediumvioletred":"#c71585","midnightblue":"#191970","mintcream":"#f5fffa","mistyrose":"#ffe4e1","moccasin":"#ffe4b5",
//    "navajowhite":"#ffdead","navy":"#000080",
//    "oldlace":"#fdf5e6","olive":"#808000","olivedrab":"#6b8e23","orange":"#ffa500","orangered":"#ff4500","orchid":"#da70d6",
//    "palegoldenrod":"#eee8aa","palegreen":"#98fb98","paleturquoise":"#afeeee","palevioletred":"#d87093","papayawhip":"#ffefd5","peachpuff":"#ffdab9","peru":"#cd853f","pink":"#ffc0cb","plum":"#dda0dd","powderblue":"#b0e0e6","purple":"#800080",
//    "red":"#ff0000","rosybrown":"#bc8f8f","royalblue":"#4169e1",
//    "saddlebrown":"#8b4513","salmon":"#fa8072","sandybrown":"#f4a460","seagreen":"#2e8b57","seashell":"#fff5ee","sienna":"#a0522d","silver":"#c0c0c0","skyblue":"#87ceeb","slateblue":"#6a5acd","slategray":"#708090","snow":"#fffafa","springgreen":"#00ff7f","steelblue":"#4682b4",
//    "tan":"#d2b48c","teal":"#008080","thistle":"#d8bfd8","tomato":"#ff6347","turquoise":"#40e0d0",
//    "violet":"#ee82ee",
//    "wheat":"#f5deb3","white":"#ffffff","whitesmoke":"#f5f5f5",
//    "yellow":"#ffff00","yellowgreen":"#9acd32"};
//
//    if (typeof colors[color.toLowerCase()] != 'undefined')
//        return colors[color.toLowerCase()];
//
//    return color;
//}
</script>
    </head>
    <%
                if (request.getParameter("update") != null) {
                    for (int i = 1; i < 50; i++) {
                        if (request.getParameter("a" + i) != null) {
                            String val = request.getParameter("a" + i);
                            int key = Integer.parseInt(val);
                            Hotkey h;
                            h = new Hotkey(projectID, i, true);
                            h.setKey(key);
                        }
                    }
                    for (int i = 1; i < 50; i++) {
                        if (request.getParameter("b" + i) != null) {
                            TagButton h;
                            String val = request.getParameter("b" + i);
                            String tag = val;
                            h = new TagButton(projectID, i, true);
                            h.updatePosition(i);
                            h.updateTag(tag);
                            String description=h.getTag();
                            if(request.getParameter("description"+i)!=null)
                                description=request.getParameter("description"+i);
                            h.updateDescription(description);
                            /*    String xmlColor="";
                                if(request.getParameter("xmlColor"+i)!=null)
                                    xmlColor=request.getParameter("xmlColor"+i);
                                h.updateXmlColor(xmlColor);
                            */
                            if (request.getParameter("b" + i + "p1") != null) {
                                String[] params = new String[5];
                                for (int j = 0; j < 5; j++) {
                                    if (request.getParameter("b" + i + "p" + (j + 1)) != null) {
                                        params[j] = request.getParameter("b" + i + "p" + (j + 1));
                                    }
                                }
                                h.updateParameters(params);
                            } else {
                            }
                        }
                    }
           //         out.print("updated!<br>");
//                    if (response.isCommitted() && session.getAttribute("ref") != null && !session.getAttribute("ref").toString().contains("login")) {
//                        String toret = session.getAttribute("ref").toString();
//                        session.setAttribute("ref", null);
//                        response.sendRedirect(toret);
//                        return;
//                    }
                }
                if (request.getParameter("deletetag") != null) {
                    int pos = Integer.parseInt(request.getParameter("position"));
                    TagButton b = new TagButton(projectID, pos, true);
                    b.deleteTag();
                }
                if (request.getParameter("deletehotkey") != null) {
                        int pos = Integer.parseInt(request.getParameter("position"));
                        Hotkey b = new Hotkey(projectID, pos, true);
                        b.delete();
                }
                if (request.getParameter("addT") != null) {
                    int ctr = 1;
                    while (new TagButton(projectID, ctr, true).exists()) {
                        ctr++;
                    }
                    new TagButton(projectID, ctr, "new", false, "description");
                }
                if (request.getParameter("addH") != null) {
                    int ctr = 1;
                    while (new Hotkey(projectID, ctr, true).exists()) {
                        ctr++;
                    }
                    new Hotkey(45, projectID, ctr, true);
                }
    %>
    <body id="buttonPage">
        <div id="wrapper">
        <div id="header"><p align="center" class="tagline">transcription for paleographical and editorial notation</p></div>
            <div id="content">
                <h1><script>document.write(document.title); </script></h1>
        <div class="widget">
            <form id="buttonForm" action="buttons.jsp" onsubmit="return rebuildOrder();" onkeypress="return event.keyCode!=13;" method="POST">
                <input type="hidden" value="<%out.print(projectID);%>" name="projectID" />
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
            <li><a href="#tabs-1">Special Characters</a><span title="There may be unsaved changes on this tab" class="right ui-icon ui-icon-alert" style="display:none;"></span></li>
		<li><a href="#tabs-2">Custom XML</a><span title="There may be unsaved changes on this tab" class="right ui-icon ui-icon-alert" style="display:none;"></span></li>
	</ul>
                <div id="tabs-1">
                    <div class="right" style="width:50%;">
                        <h2>Hotkeys</h2>
                        <p>Enter the unicode value for each character.</p>
                        <a id="codeSearch" target="_blank" class="tpenButton ui-button ui-button-text-only" href="http://www.ssec.wisc.edu/~tomw/java/unicode.html"><span class="left ui-icon ui-icon-search"></span>Search for Codes</a>
                        <p>Buttons are mapped to the digits 1-9 on your keyboard. <span class="loud">Hold CTRL and press the corresponding number key</span> to insert one into your transcription.</p>
                        <p>Rearrange by clicking on the blue box and dragging to a new position. Click the X to remove a button.</p>
                        <p>You may add more than 9, but only the first 9 will be mapped to shortcuts.</p>
                        <div id="characterReview"><h5>Current Special Character Buttons</h5>
    <%
        /**Retrieve stored button information*/
                        Hotkey ha;
                        ha=new Hotkey(projectID,true);
                        out.print(ha.javascriptToAddProjectButtons(projectID));
    %>
                        </div>
                    </div>
                        <ul id="sortable1" class="connectedSortable ui-helper-reset">
            <%
                    int ctr = 1;
                    try {
                        String ref = request.getHeader("referer");
                        if (ref.contains("transcription")) {
                            session.setAttribute("ref", request.getHeader("referer"));
                        }
                    } catch (NullPointerException e) {
                        //They didnt get here from another page, maybe a bookmark. Not a big deal
                    }
                    while (new Hotkey(projectID, ctr, true).exists()) {
                        ha = new Hotkey(projectID, ctr, true);
                        out.print("<li class=\"ui-state-default\"><input readonly class=\"label\" name=\"a"+ctr+"a\" id=\"a"+ctr+"a\" value=\""+(char)Integer.parseInt(ha.getButton())+"\" tabindex=-5>\n");
                        out.print("<input class=\"shrink\" onkeyup=\"updatea(this);\" name=\"a"+ctr+"\" id=\"a"+ctr+"\" type=\"text\" value=\""+ha.getButton()+"\"></input>");
                        out.print("<a class=\"ui-icon ui-icon-closethick right\" onclick=\"deleteHotkey(" + ctr + ");\">delete</a></li>");
                        out.print("\n");
                        ctr++;
                    }
            %>
                    </ul>
            <input type="button" id="addH" name="addH" class="tpenButton ui-button" value="Add a Button"/>
            <input type="submit" id="update" name="update" value="Save Changes" class="tpenButton ui-button"/><br><br>
                </div>
                <div id="tabs-2">
                    <div class="right" style="width:440px;">
                        <h2>Custom XML Tags</h2>
                        <p>Add each tag without &lsaquo;angle brackets&rsaquo;, any parameters including &quot;quotes&quot; in the next 5 fields, and a description in the final field. Text in the <span class="loud">"description"</span> field will become the title of your button. Seek conciseness.</p>
                        <p>Rearrange by clicking on the blue box and dragging. Click the "X" icon to remove a button from the list. Add as many buttons as is useful. You will access them through the footer menu.</p>
                        <div id="xmlReview"><h5>Current Tags</h5>
    <%
        /**Retrieve stored button information*/
        out.print(TagButton.getAllProjectButtons(projectID));
    %>
                        </div>
                    </div>
                    <ul id="sortable2" class="connectedSortable ui-helper-reset">
<%
String appendProject = "&projectID="+projectID;
ctr = 1;
TagButton b;
while (new TagButton(projectID, ctr, true).exists()) {
    b = new TagButton(projectID, ctr, true);
    out.println("<li class=\"ui-state-default xmlPanel\">");
    out.println("<span class='ui-icon ui-icon-arrow-4 toggleXML left'></span>");
    out.println("<a class=\"ui-icon ui-icon-closethick right\" onclick=\"deleteTag(" + ctr + ");\">delete</a>");
    out.println("<input class=\"description\" onchange=\"unsavedAlert('#tabs-2');\" type=\"text\" placeholder=\"Button Name\" name=description"+(ctr)+" value=\""+b.getDescription()+"\">");
//    out.println("<input class=\"colors\" onchange=\"unsavedAlert('#tabs-2');\" type=\"text\" placeholder=\"black\" name=xmlColor"+(ctr)+" value=\""+"b.getXMLColor"+"\">");
    out.println("<div class='xmlParams'>");
    out.println("<span class=\"firstRow collapseXML\"><span class=\"bold tag\"><input name=\"b"+ctr+"\" id=\"b"+ctr+"\" type=\"text\" class='collapseXML' value=\""+b.getTag()+"\"></input></span>");
    if (b.hasParameters()) {
        String[] params = b.getparameters();
        String parameters = new String();
        //out.print("<script type='text/javascript'>var parameters='';");
        switch (params.length) {
            case 5:     parameters = "<input onchange=\"unsavedAlert('#tabs-2');\" placeholder='parameter' type='text' name='b" + ctr + "p5' value='" + params[4] + "'/>\n</span>" + parameters;
            case 4:     parameters = "<span class='clear-left lastRow collapseXML'>\n<input onchange=\"unsavedAlert('#tabs-2');\" placeholder='parameter' type='text' name='b" + ctr + "p4' value='" + params[3] + "'/>"+parameters;
            case 3:     parameters = "<input onchange=\"unsavedAlert('#tabs-2');\" placeholder='parameter' class='collapseXML' type='text' name='b" + ctr + "p3' value='" + params[2] + "'/><span class='right ui-icon moreParameters ui-icon-plus' title='Add more parameters to this button'>\n</span>\n</span>"+parameters;
            case 2:     parameters = "<span class='clear-left secondRow collapseXML'>\n<input onchange=\"unsavedAlert('#tabs-2');\" placeholder='parameter' type='text' name='b" + ctr + "p2' value='" + params[1] + "'/>"+parameters;
            case 1:     parameters = "<input onchange=\"unsavedAlert('#tabs-2');\" placeholder='parameter' type='text' name='b" + ctr + "p1' value='" + params[0] + "'/><span class='right ui-icon moreParameters ui-icon-plus' title='Add more parameters to this button'>\n</span>\n</span>"+parameters;
            default:    break;
        }
        switch (params.length) {
            case 1:     parameters += "<span class='clear-left secondRow collapseXML'>\n<input onchange=\"unsavedAlert('#tabs-2');\" placeholder='parameter' type='text' name='b"+ ctr + "p2' value=''/>";
            case 2:     parameters += "<input onchange=\"unsavedAlert('#tabs-2');\" placeholder='parameter' type='text' name='b"+ ctr + "p3' value=''/><span class='right ui-icon moreParameters ui-icon-plus' title='Add more parameters to this button'>\n</span>\n</span>";
            case 3:     parameters += "<span class='clear-left lastRow collapseXML'>\n<input onchange=\"unsavedAlert('#tabs-2');\" placeholder='parameter' type='text' name='b"+ ctr + "p4' value=''/>";
            case 4:     parameters += "<input onchange=\"unsavedAlert('#tabs-2');\" placeholder='parameter' type='text' name='b"+ ctr + "p5' value=''/>\n</span>";
            default:     break;
        }
        out.println(parameters);
    } else {%>
                    <input onchange="unsavedAlert('#tabs-2');" placeholder="parameter" type="text" name="b<%out.print(ctr);%>p1" />
                    <span class="right ui-icon moreParameters ui-icon-plus" title="Add more parameters to this button"></span><%out.print("</span>");//close .firstRow%>
                    <span class="clear-left secondRow collapseXML">
                        <input onchange="unsavedAlert('#tabs-2');" placeholder="parameter" type="text" name="b<%out.print(ctr);%>p2" />
                        <input onchange="unsavedAlert('#tabs-2');" placeholder="parameter" type="text" name="b<%out.print(ctr);%>p3" />
                        <span class="right ui-icon moreParameters ui-icon-plus" title="Add more parameters to this button"></span>
                    </span>
                    <span class="clear-left lastRow collapseXML">
                        <input onchange="unsavedAlert('#tabs-2');" placeholder="parameter" type="text" name="b<%out.print(ctr);%>p4" />
                        <input onchange="unsavedAlert('#tabs-2');" placeholder="parameter" type="text" name="b<%out.print(ctr);%>p5" />
                    </span>
<%
    }
        out.println("</div></li>");
        ctr++;
}
%>
                    </ul>
                    <input type="button" id="addT" name="addT" value="Add a Tag" class="tpenButton ui-button" onclick="document.getElementById('selecTab').value = 1;" />
                    <input type="submit" onclick="document.getElementById('selecTab').value = 1;" id="update" name="update" value="Save Changes" class="tpenButton ui-button"/><br><br>
                    <br />
                </div>
                    <div class="right">
                        <a href="buttonProjectImport.jsp?a=1<%out.print(appendProject);%>" class="importButton tpenButton ui-button">Copy Buttons from Another Project</a>
<%
if (thisProject.getSchemaURL().length() > 5){%>
<a href="buttonSchemaImport.jsp?<%out.print(appendProject);%>" class="importButton tpenButton ui-button">Import Buttons from Schema</a>
                    <%}%>
                    </div>
            </div>
            <input type="hidden" name="selecTab" id="selecTab" value="0"/>
<%
        if (p.length() > 0){
            p = request.getParameter("p");
            out.print("<input type='hidden' name='p' value='"+p+"'/>");
        }%>
            </form>
     </div>
<%
        if (p.length() > 0){
            out.print("<a class=\"returnButton\" href=\"transcription.jsp?p=" + request.getParameter("p") + appendProject + "\">Return to transcribing</a>");
        %><a class="returnButton" href="project.jsp?<%out.print(projectAppend);%>">Project Management</a><%
        } else {%>
        <a class="returnButton" href="project.jsp?<%out.print(projectAppend);%>">Return to Project Management</a>
        <%}%>        
        <a class="returnButton" href="index.jsp">T&#8209;PEN Home</a>
            </div>
            <div id="space"></div>
    <%@include file="WEB-INF/includes/projectTitle.jspf" %>
        </div>            
    </body>
</html>
