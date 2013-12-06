<%-- 
    Document   : projSequence
    Created on : Nov 26, 2010, 5:46:42 PM
    Author     : jdeerin1
--%>
<%@page import="com.sun.xml.internal.ws.wsdl.writer.document.Message"%>
<jsp:useBean id="proj" class="textdisplay.Project" scope="page" />
<%@page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@page import ="textdisplay.Folio"%>
<%@page import ="textdisplay.Project"%>
<%@page import ="java.util.*"%>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN"
   "http://www.w3.org/TR/html4/loose.dtd">
<%         
        if (session.getAttribute("UID") == null) {
        %><%@ include file="loginCheck.jsp" %><%
                   } else {
 %>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title>TPEN Project image sequencing</title>
        <link rel="stylesheet" href="css/tpen.css" type="text/css" media="screen, projection">
        <link rel="stylesheet" href="css/print.css" type="text/css" media="print">
        <!--[if lt IE 8]><link rel="stylesheet" href="css/ie.css" type="text/css" media="screen, projection"><![endif]-->
        <link type="text/css" href="css/custom-theme/jQuery.css" rel="Stylesheet" />
        <script type="text/javascript" src="https://ajax.googleapis.com/ajax/libs/jquery/1.7.1/jquery.min.js"></script>
        <script type="text/javascript" src="https://ajax.googleapis.com/ajax/libs/jqueryui/1.8.13/jquery-ui.min.js"></script>
        <script type="text/javascript" src="js/tpen.js"></script>
        <script type="text/javascript">
            var mvHeight = ((Page.height()-300)>400) ? (Page.height()-300) : 400;
            document.write("<style type='text/css'>#manuscriptViewer {max-height:" + mvHeight + "px;}</style>");
        </script>
        <style type="text/css">
            .folios {cursor:move;z-index: 1;margin: 0;padding: 0;width: 100%; display: block; overflow: hidden;clear: both;text-overflow:ellipsis;white-space: nowrap;text-shadow:0 1px 0 white;
                -moz-box-sizing:border-box;
                -webkit-box-sizing:border-box;
                box-sizing:border-box;
            }
            #holding .folios {border-radius:5px;}
            .pageName {}
            .collection {color:steelBlue;display: none;} /* Hide collection name unless they are various */
            #manuscriptViewer .folios {cursor: pointer;}
            #manuscriptViewer .folios:nth-child(even) {background: rgba(168,210,228,.35);}
            #manuscriptViewer .folios:hover {background: rgba(168,210,228,.75) !important;}
            .imagePreview,.discard,.clicked {position: relative;z-index: 2;cursor: pointer;}
            #holding .discard,#holding .clicked{display: none;}
            #manuscriptViewer {overflow-y:scroll;overflow-x:visible;}
            #manuscriptViewer .ui-state-highlight {min-height: 18px;}
            #moveAll .ui-state-highlight {min-height: 85px;}
            #manuscriptViewer,#holding,h3 {width:40%;padding: 5px;}
            #holdingArea {position: relative;min-height: 200px;border:1px solid #A64129;overflow: visible;}
            #holdingArea, #discard {width: 100%;margin: 0 0 15px 0;padding: 5px;box-sizing: border-box;}
            #holdingArea li,#discard li{cursor: move;position: relative;z-index: 2;margin: 1px; padding: 2px; padding-left:18px;font-size: 12px; height: 22px;
                border: 1px solid #69acc9;
                background: rgb(168,210,228); /* Old browsers */
                background: -moz-linear-gradient(top,  rgba(168,210,228,1) 0%, rgba(229,229,229,1) 100%); /* FF3.6+ */
                background: -webkit-gradient(linear, left top, left bottom, color-stop(0%,rgba(168,210,228,1)), color-stop(100%,rgba(229,229,229,1))); /* Chrome,Safari4+ */
                background: -webkit-linear-gradient(top,  rgba(168,210,228,1) 0%,rgba(229,229,229,1) 100%); /* Chrome10+,Safari5.1+ */
                background: -o-linear-gradient(top,  rgba(168,210,228,1) 0%,rgba(229,229,229,1) 100%); /* Opera 11.10+ */
                background: -ms-linear-gradient(top,  rgba(168,210,228,1) 0%,rgba(229,229,229,1) 100%); /* IE10+ */
                background: linear-gradient(top,  rgba(168,210,228,1) 0%,rgba(229,229,229,1) 100%); /* W3C */
                filter: progid:DXImageTransform.Microsoft.gradient( startColorstr='#a8d2e4', endColorstr='#e5e5e5',GradientType=0 ); /* IE6-9 */
                font-weight: normal; color: #226683; }
            #holdingArea:after {
                content: 'Rearrange folios here';
                position: absolute;
                bottom: 0;
                right: 0;
                font-size: 24px;
                color: rgba(145, 145, 60, .5);
                z-index: 0;
            }
            #discard {min-height: 35px;}
            #moveAll, #centerConsole a{position: relative;width:100%;height:0px;overflow: hidden;text-decoration: none;box-sizing:border-box; margin:0;padding: 0 5px;text-align: center;
                -webkit-transition:height 1s;
                -moz-transition:height 1s;
                -o-transition:height 1s;
                transition:height 1s;
            }
            #moveAllBtn {list-style: none outside;cursor:move;}
            #rearrangeInstructions {position: relative; z-index: 1;}
            #rearrangeInstructions h3 {width:100%;}
            .imagePreview {background: url(images/pageIcon.png) no-repeat !important;z-index: 2;}
            #imagePreview {z-index: 0;width:100%;position:fixed;display:none;top:0;left:0;right:0;bottom:0;margin:auto;}
            #imgLoading {border:thin solid #A64129;text-align: center;height: 200px; width:400px;position: fixed;top:50%;left:50%;margin:-75px 0 0 -200px;padding:20px;background: url('images/linen.png') repeat;overflow: auto;z-index: -1;display: none;}
            #loadingPreview {border:thin solid #A64129;margin: 0 auto; display: block; clear:both;}
            #wrapper, #content {z-index: 1;position: relative;}
            #centerConsole {width:15%;position: absolute;left: 0;right:0;margin: 0 auto;top:5%;background-image: url(images/linen.png);background-color: #69ACC9;height: 90%;box-shadow:-1px -1px 4px black inset;}
            #centerConsole a,#noneSelected {padding: 0px;border-width: 0;margin: 3px auto;width:90%;}
            #preview{width:100px;position: relative;margin: 15px auto;height: 100px;border: 1px solid #A64129;padding: 0 5px;background: whitesmoke;}
            #previewWide,#previewTall {border: 1px solid #A64129;margin:2px;cursor: pointer;}
            #previewWide:hover,#previewTall:hover {                    
                -moz-box-shadow: 0 0 5px #A64129;
                -webkit-box-shadow: 0 0 5px #A64129; 
                box-shadow:0 0 5px #A64129;}
            #previewWide {position:absolute;bottom:0;left:4px;z-index: 4;}
            #previewTall {position:absolute;bottom:0;right:4px;z-index: 3;}
            #previewText {text-align: center;}
            .ui-state-disabled {list-style: none outside;} /* cleanup ui when dragging */
            #selectOptions a {
                width: 120px;
                height: 22px;
                text-decoration: none;
                display: inline-block;
                margin: 0 10px;
            }
            .selectedFolio{background: #69ACC9 !important;text-shadow:0 1px 0 rgb(168,210,228);box-shadow: 0 0 2px white inset;}
            .selectedFolio .collection {color:rgb(168,210,228);text-shadow:0 1px 0 0 1px 0 rgb(208, 250, 255);}
        </style>
        <script>
            function checkBeforeSave (){
                $("body").addClass("ui-state-disabled");
                if ($("#holdingArea li").length > 0) {
                    var okDiscard = confirm('Folios remaining in the Holding Area will be removed from your project.');
                    if (!okDiscard) {
                        $("body").removeClass("ui-state-disabled");
                        return false;
                    }
                }
                $("#holdingArea,#discard").remove();
                return true;
            }
            $(function() {
                $( "#holdingArea,#manuscriptViewer,#discard" ).sortable({
                    placeholder: "ui-state-highlight ui-corner-all ui-state-disabled",
                    connectWith: "#holdingArea,#manuscriptViewer,#discard",
                    cancel: "#manuscriptViewer",
                    scroll:true,
                    forceHelperSize:true,
                    items:"li",
                    stop: function(){
                        if ($('#holdingArea li').length == 0)$('#moveAll').height(0);
                    }
                }).disableSelection();
            $(".imagePreview").on({
                mouseenter: function(){
                    $("#content").css("opacity",0.1);
                    $("#imgLoading").show();
                    $("#imagePreview")
                        .attr("src",$(this).attr('data-imgUrl'))
                        .fadeIn(250);
                },
                mouseleave: function(){
                    $("#imagePreview").fadeOut(250);
                    $("#content").css("opacity",1);
                    $("#imgLoading").stop().hide();
                }
            });
            $("#moveAll").sortable({
                connectWith: "#manuscriptViewer,#discard",
                placeholder: "ui-state-highlight ui-corner-all ui-state-disabled",
                scroll:true,
                forceHelperSize:true,
                start:function(){
                    $("#holdingArea li").addClass("selectedFolio");
                    $("#moveAll").css('overflow','visible');
                },
                update:function(){
                    $("#holdingArea li").addClass("selectedFolio").show("slide",{direction:"right"},1000,function(){
                        $(this).removeClass("selectedFolio")
                            .attr("title","Click to move to the holding area");
                    });
                    $("#moveAllBtn").after($("#holdingArea").contents());
                    $("#moveAllBtn").prependTo("#moveAll");
                    $("#moveAll").css('overflow','hidden');
                    if ($('#holdingArea li').length == 0)$('#moveAll').height(0);
                },
                stop:function(){$('#holding').find('.selectedFolio').removeClass('selectedFolio');}
            }).disableSelection();
            $("#rearrangeInstructions").disableSelection();
            $("#moveToDiscard").click(function(){
                $("#manuscriptViewer").find('.selectedFolio').removeClass('selectedFolio').appendTo("#discard").attr("title","This image will not be included in this project");
                $("#centerConsole").children("a").height(0).css({'padding':'0px','border-width':'0px'});
                $('#noneSelected').show();
            });
            $("#moveToHold").click(function(){
                $("#manuscriptViewer").find('.selectedFolio').removeClass('selectedFolio').appendTo("#holdingArea").attr("title","Drag to replace in project or discard");
                $("#moveAll").height(125);
                $("#centerConsole").children("a").height(0).css({'padding':'0px','border-width':'0px'});
                $('#noneSelected').show();
            });
            $(".clicked").on({
                click: function(event){
                    event.preventDefault();
                    $(this).parent('.folios').removeClass('selectedFolio').appendTo("#holdingArea").removeClass('selectedFolio').attr("title","Drag to replace in project or discard");
                    $("#moveAll").height(125);
                }
            });
            $("#manuscriptViewer").find('.folios').on({
                click: function(event){
                    if(!event) event = window.event;
                    if($(event.target).hasClass('clicked')||$(event.target).hasClass('discard')){return true;}
                    event.preventDefault();
                    $(this).toggleClass('selectedFolio');
                    if(event.shiftKey){
                        var startIndex = $("#startIndex").index();
                        var thisIndex = $(this).index();
                        var selected = (thisIndex > startIndex)? $("#startIndex").nextUntil($(this).next(),'li'): $("#startIndex").prevUntil($(this).prev(),'li');
                        if ($("#startIndex").hasClass('selectedFolio')){
                            selected.addClass('selectedFolio');
                        } else {
                            selected.removeClass('selectedFolio')
                        }
                    }
                    $("#startIndex").attr('id','');
                    $(this).attr('id','startIndex');
                    if ($(".selectedFolio").length > 0){
                        $("#centerConsole").children("a").height(27).css({'padding':'3px','border-width':'1px'});
                        $('#noneSelected').hide();
                    } else {
                        $("#centerConsole").children("a").height(0).css({'padding':'0px','border-width':'0px'});
                        $('#noneSelected').show();
                    }
                }
            });
            $("#manuscriptViewer").find(".discard").on({
                click:  function(){
                    $(this).parent()
                        .fadeOut("fast",function(){
                            $(this)
                                .removeClass("selectedFolio")
                                .css("display","block")
                                .appendTo("#discard")
                        })
                        .preventDefault();
                },
                mouseenter: function(){$(this).siblings('.pageName').add($(this).parent('li')).css('color','red !important');},
                mouseleave: function(){$(this).siblings('.pageName').add($(this).parent('li')).css('color','');}
            });
            $("#trash").css("cursor","pointer").click(function(){
                $("#discard").children("li").remove();
            });
            $("input:submit").hover(function(){$(this).toggleClass("ui-state-hover")});
            $("#previewWide").css({"height":"45px","bottom":"-6px"}).click(function(){
                $("#previewTall").animate({"height":"32px","z-index":3,"bottom":"0px"},500,"easeOutExpo");
                $(this).animate({"height":"45px","z-index":4,"bottom":"-6px"},500,"easeInExpo");
                $("#imagePreview").css({
                    "width":"100%",
                    "height":"auto"
                });
                $("#previewText").hide("slide",{direction:"right"},250,function(){$(this).html("Full-width preview").show("slide",{direction:"left"},250);});
            });
            $("#previewTall").click(function(){
                $("#previewWide").animate({"height":"32px","z-index":3,"bottom":"0px"},500,"easeOutExpo");
                $(this).animate({"height":"45px","z-index":4,"bottom":"-6px"},500,"easeInExpo");
                $("#imagePreview").css({
                    "height":"100%",
                    "width":"auto"
                });
                $("#previewText").hide("slide",{direction:"left"},function(){$(this).html("Full-page preview").show("slide",{direction:"right"},250);});
            });
            $("#all").click(function(){
                var folioList = $("#manuscriptViewer").find("li");
                if($(this).attr('select')=='true'){
                    folioList.addClass('selectedFolio');
                    $(this).attr('select',false).text('Deselect All');
                    $("#centerConsole").children("a").height(27).css({'padding':'3px','border-width':'1px'});
                    $("#noneSelected").hide();
                } else {
                    folioList.removeClass('selectedFolio');
                    $(this).attr('select',true).text('Select All');
                    $("#centerConsole").children("a").height(0).css({'padding':'0px','border-width':'0px'});
                    $("#noneSelected").show();
                }
            });
            $("#inv").click(function(){
                if ($(".selectedFolio").length == 0){
                    $("#all").attr('select',true).click();
                    return false;
                }
                $("#manuscriptViewer").find("li").each(function(){
                    if ($(this).hasClass('selectedFolio')){
                        $(this).removeClass('selectedFolio')
                    } else {
                        $(this).addClass('selectedFolio')
                    }
                })
            });
        });
        $(window).load(function(){
            var collection = $('.collection').eq(-1).text();
            $('.collection').each(function(){
                if ($(this).text() != collection) {
                    $('.collection').show();
                    return false;
                }
            });
        });
        </script>
    </head>
       <%
    // saving changes
    if(request.getParameter("submit")!=null) {
        int projID=Integer.parseInt(request.getParameter("projectID"));
        proj.setProjectNumber(projID);
        proj.fetch();
        // collect new order and convert to folio[]
        if (request.getParameter("folio[]")!=null){
            String[] folioList = request.getParameterValues("folio[]");
            Stack<Folio> allFolios = new Stack();
            for(int i=0;i<folioList.length;i++){
                try {
                    Folio f = new Folio(Integer.parseInt(folioList[i]));
                    allFolios.add(f);
                } catch (NumberFormatException er) {
                }
            }
            Folio[] sequenced = new Folio[allFolios.size()];
            for (int i = 0; i < sequenced.length; i++) {
                sequenced[i] = allFolios.get(i);
            }
            // save order to project
            proj.setFolios(sequenced, proj.getProjectID());
        }               
    }
    %> <body>
        <div id="wrapper">
            <div id="header"><p align="center" class="tagline">transcription for paleographical and editorial notation</p></div>
            <div id="content">
        <h1><script>document.write(document.title); </script></h1>
                <div id="main">
         <%
        int projID=Integer.parseInt(request.getParameter("projectID"));
        %>
        <form action="projSequence.jsp?projectID=<%out.print(""+projID);%>" onsubmit="return checkBeforeSave();" method="POST" style="position:relative;min-height:400px;">
            <div id="holding" class="right">
                <h3>Holding Area</h3>
                <ul id="holdingArea" class="ui-corner-all">
                </ul>
                <ul title="Click the trash icon or drop pages here to remove them from the project" class="ui-corner-all ui-state-error" id="discard">
                    <span class="right ui-icon ui-icon-trash" id="trash" title="Click to remove these items now"></span>
                    <strong>Discard</strong>
                </ul>
                <div id="rearrangeInstructions" class="right clear-right">
                    <h3>Rearranging Images</h3>
                    <ol>
                        <li>Click the arrow on a misplaced page to move it to the holding area;</li>
                        <li>Click the title to select multiple images;</li>
                        <li>Rearrange pages within the holding area by clicking and dragging;</li>
                        <li>Drag pages back into the list to place them correctly; or</li>
                        <li>Drag the stack of books to place the whole group.</li>
                    </ol>
                </div>
            </div>
            <div class="ui-corner-all" id="centerConsole">
                <div id="preview" class="ui-corner-all">
                    <h6>Preview Options</h6>
                    <img id="previewTall" class="ui-corner-all ui-widget-content" src="images/pageTall.gif" alt="Preview will show the full page" title="Preview will show the full page, scaled down to your screen" />
                    <img id="previewWide" class="ui-corner-all ui-widget-content" src="images/pageWide.gif" alt="Preview will use the full width of the screen" title="Preview will use the full width of the screen and may crop the bottom of the image" />
                    <div id="previewText" class="small">Full-width preview</div>
                </div>
                <ul id="moveAll">
                    <li id="moveAllBtn" title="Drag to move the entire group">
                        <img src="images/moveAll.png" alt="Move All" />
                    </li>
                Drag to place group back into project.</ul>
                <a id="moveToHold" class="tpenButton"><span class="ui-icon ui-icon-seek-next right"></span>Hold selected</a>
                <a id="moveToDiscard" class="tpenButton"><span class="ui-icon ui-icon-trash right"></span>Discard selected</a>
                <div id="noneSelected"><span class="ui-icon ui-icon-info left"></span>Click on an image name to select.</div>
            </div>
            <div>
                <h3>Project Folio List</h3>
                <div id="selectOptions">
                    <a id="all" class="tpenButton" select=true>Select All</a>
                    <a id="inv" class="tpenButton">Invert Selection</a>
                </div>
                <ul id="manuscriptViewer">
                    <%
                    proj.setProjectNumber(projID);
                    proj.fetch();
                    Folio[] folios=proj.getFolios();
                    for(int i=0;i<folios.length;i++) {
                        out.print("<li class='folios' title='"+folios[i].getCollectionName()+" "+folios[i].getPageName()+"'><span class=\"clicked ui-icon ui-icon-arrowthick-1-e right\" title='Move a single image into the holding area'></span><span class=\"discard ui-icon ui-icon-trash right\" title='Discard a single folio'></span><span class=\"imagePreview ui-icon left\" data-imgUrl="+folios[i].getImageURL() +"></span><input type=\"hidden\" name=\"folio[]\" id=\""+folios[i].getFolioNumber()+"\" value=\""+folios[i].getFolioNumber()+"\"/><span class='collection'>"+folios[i].getCollectionName()+"</span> <span class='pageName'>"+folios[i].getPageName()+"</span></li>\n");
                    }%>
                </ul>
                <div class="small">
                    <span class="ui-icon ui-icon-info left"></span>Hold SHIFT to select a group of images.<br/>
                    <span class="ui-icon ui-icon-info left"></span>Hover over the image icon <img align="top" alt="Image Preview Icon" title="Not this one - the icon next to the shelfmark" data-imgurl="" src="images/pageIcon.png" width="16px" height="16px"/> to see a preview of the page.
                </div>
            </div>
            <input class="ui-corner-all ui-state-default ui-button" type="submit" name="submit" value="Save Changes" id="submit"/>
            </form>
                </div>
                <a class="returnButton" href="project.jsp?projectID=<%out.print(projID);%>">Return to project page</a><br>
            </div>
        </div>
                <%@include file="WEB-INF/includes/projectTitle.jspf"%>
        <img alt="Image Preview" src="#" id="imagePreview" />
        <div id="imgLoading" class="ui-corner-all">
            <h1>Requesting Preview...</h1>
            <img alt="Loading Preview" align="center" id="loadingPreview" class="clear ui-corner-all" src="css/custom-theme/images/loadingPreview.gif" />
            <div class="small">If this does not disappear, there may have been a problem loading the image.</div>
        </div>
    </body>
</html>
<%}%>