<%-- 
    Document   : buttonTest
    Created on : May 26, 2011, 11:26:20 AM
    Author     : jim
--%>
<%@page import="user.User"%>
<%@page contentType="text/html; charset=UTF-8"  %>
<%        
        int UID = 0;
            if (session.getAttribute("UID") == null) {
        %><%@ include file="loginCheck.jsp" %><%
                   } else {
                UID = Integer.parseInt(session.getAttribute("UID").toString());
            }
%>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN"
   "http://www.w3.org/TR/html4/loose.dtd">

<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title>Import Buttons from XML Schema</title>
        <link rel="stylesheet" href="css/tpen.css" type="text/css" media="screen, projection">
        <link rel="stylesheet" href="css/print.css" type="text/css" media="print">
        <!--[if lt IE 8]><link rel="stylesheet" href="css/ie.css" type="text/css" media="screen, projection"><![endif]-->
        <link type="text/css" href="css/custom-theme/jQuery.css" rel="Stylesheet" />
        <script type="text/javascript" src="https://ajax.googleapis.com/ajax/libs/jquery/1.7.1/jquery.js"></script>
        <script type="text/javascript" src="https://ajax.googleapis.com/ajax/libs/jqueryui/1.8.16/jquery-ui.js"></script> 
        <script type="text/javascript" src="js/tpen.js"></script>
        <style type="text/css">
            #importedTags, #discard, #currentTags {width: 45%; float:left; position: relative; margin-right: 45px;
            }
            #importedTags ul, #discard ul { overflow: hidden;width:100%; position: relative; list-style: outside none; margin:0; min-height: 100px;padding:10px;}
            #importedTags li, #discard li {margin: 0; padding: 2px; float: left; width: auto;min-width: 70px; position: relative;height:18px;}
            .discard {position: absolute; right: 0; top: 0;display: none;}
            .hoverButton {padding: 7px !important;margin: -5px !important; z-index: 1;                    
                    -moz-box-shadow: 0px 0px 2px black;
                    -webkit-box-shadow: 0px 0px 2px black; 
                    box-shadow:0px 0px 2px black;}
        </style>
        <script type="text/javascript">
            $(function() {
//                $( ".returnButton" ).addClass("ui-state-default ui-corner-bl ui-corner-br")
//                    .css("display","inline-block")
//                    .prepend("<span class='ui-icon ui-icon-arrowreturnthick-1-w right'></span>")
//                    .hover(function(){$(this).addClass("ui-state-highlight");},
//                        function(){$(this).removeClass("ui-state-highlight");}
//                );
                $( "#importedTags ul,#discard ul" ).sortable({
                    placeholder: "ui-state-highlight ui-corner-all ui-state-disabled",
                    connectWith: "#importedTags ul,#discard ul",
                    scroll:true,
                    forceHelperSize:true,
                    items:"li",
                    start: function(e,ui){
                        ui.placeholder.css("min-width",minWidth);
                    }
                }).disableSelection()
                    .bind("sort",function(){
                        $(this).find("li").removeClass("hoverButton");
                    })
                    .not("#discard ul").find("li")
                    .hover(
                        function() {
                            $(this)
                                .addClass("hoverButton")
                                .find(".discard").hover(
                                    function(){
                                        $(this).parent().addClass("ui-state-error");
                                    },
                                    function(){
                                        $("#importedTags,#discard").find("li.ui-state-error").removeClass("ui-state-error");
                                })
                                .click(function(){
                                    $(this).parent()
                                        .fadeOut("fast",function(){
                                            $(this)
                                                .removeClass("ui-state-error hoverButton")
                                                .css("display","block")
                                                .appendTo("#discard ul");
                                    })
                            }).show();
                        },function() {
                            $(".discard").hide();
                            $(this).removeClass("hoverButton");
                        }
                );
                $("#trash").css("cursor","pointer").click(function(){
                    $(this).siblings("li").remove();
                });
                $("input:submit").hover(function(){$(this).toggleClass("ui-state-hover")});
            });
            var minWidth = 70;
            $(window).load(function(){
                $("#importedTags").find("li").each(function(){
                    minWidth = ($(this).width()>minWidth) ? $(this).width() : minWidth;
                }).css({"min-width":minWidth+"px"});
                $("#currentTags").children("span").each(function(){
                    currentTagWidth = ($(this).width()>currentTagWidth) ? $(this).width() : currentTagWidth;
                }).css({"min-width":currentTagWidth+"px"});
            });
        </script>
    </head>
    <body>
        <div id="wrapper">
            <div id="header"><p align="center" class="tagline">transcription for paleographical and editorial notation</p></div>
            <div id="content">
                <h1><script>document.write(document.title); </script></h1>
                <div id="main" class="ui-widget ui-widget-content ui-corner-all ui-tabs ui-helper-reset ui-helper-clearfix">
                    <div id="results"></div>
        <%
        if(request.getParameter("projectID")!=null){
            int projectID = Integer.parseInt(request.getParameter("projectID"));
            textdisplay.Project p=new textdisplay.Project(projectID);
            if(request.getParameter("submitted")!=null) {
                String [] tags=request.getParameterValues("xmlTag[]");
                int ctr=0;
               for(int i=0;i<tags.length;i++) {
                    if(tags[i]!=null) {
                        ctr++;
 //              out.print(tags[i]+", ");
                        new textdisplay.TagButton(projectID, ctr, tags[i],true, tags[i]);
                    }
               }
                out.print("<script>");
                out.print("$('#results').addClass('ui-state-active ui-corner-all').html('Buttons imported successfully. Use the links at the bottom of this page to return to your work.')");
                out.print("</script>");
            }
        %>
        <h3 class="ui-widget-header ui-tabs ui-corner-all ui-state-default">Detected Tags for <%out.print(p.getProjectName());%></h3>
        <h3>Linked Schema: <span class="small"><%out.print(p.getSchemaURL());%></span></h3>
        <div id="importedTags">
            <h3>XML Tags to Import</h3>
            <form action="buttonSchemaImport.jsp" method="post" onSubmit="$('#trash').click();">
            <ul>
        <%
        java.util.Vector v=new java.util.Vector();
        String [][] tags=textdisplay.TagButton.getTagsFromSchema(p,v);
        if (tags!=null){
        for(int i=0;i<tags.length;i++)
            {
            //the && j<1 means we only want t print out the tags, no parameters, since the tag is the first item in the string []
            for(int j=0;j<tags[i].length && j<1;j++)
                {
            out.println("<li class=\"ui-corner-all ui-state-default\"><input type=\"hidden\" name=\"xmlTag[]\" value=\""+tags[i][j]+"\"/>");
            out.print(tags[i][j]+"<span class=\"discard ui-icon ui-icon-trash\" ></span></li>");
            }
            }
        } else {
            out.print("No tags detected.");
        }

        %>
        </ul>
        <input type="hidden" value="<%out.print(projectID);%>" name="projectID" />
        <input class="ui-button ui-corner-all ui-state-default clear-left right" type="submit" name="submitted" value="Import Buttons" >
        </form>
            </div>
        <div id="discard">
            <h3>Discard</h3>
            <ul title="Click the trash icon or drop buttons here to remove them from the project" class="ui-corner-all ui-state-error">
                <span class="right ui-icon ui-icon-trash" id="trash" title="Click to remove these buttons now"></span>
            </ul>
        </div>
        <div id="currentTags">
            <h3>Current Project Tags</h3>
            <%
            int position = 1;
            textdisplay.TagButton b;
            out.println(textdisplay.TagButton.getAllProjectButtons(projectID));
            /*while (new textdisplay.tagButton(projectID, position, true).exists()) {
                b = new textdisplay.tagButton(projectID, position, true);
                //out.println("<span class='tagButton' title='"+b.getFullTag()+"'>"+b.getDescription()+"("+b.getTag()+")</span>");
                position++;
            }*/
                       %>
        </div>
        <%} else {      //no projectID passed
            out.print("No project specified!");
        }%>
                </div>
        <a class="returnButton" href="buttons.jsp<%if (request.getParameter("projectID")!=null)out.print("?projectID="+request.getParameter("projectID"));%>">Return to Button Management</a>
        <a class="returnButton" href="project.jsp<%if (request.getParameter("projectID")!=null)out.print("?projectID="+request.getParameter("projectID"));%>">Return to Project Management</a>
        <a class="returnButton" href="index.jsp">Return to T&#8209;PEN Home</a>
            </div>
        </div>
    </body>
</html>
