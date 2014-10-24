<%-- 
    Document   : search
    Created on : Nov 10, 2011, 3:04:13 PM
    Author     : cubap
--%>

<%@page import="textdisplay.Folio"%>
<%@page import="textdisplay.Transcription"%>
<%@page import="textdisplay.Manuscript"%>
<%@page import="java.text.DateFormat"%>
<%@page import = "user.User"%>
<%@page import = "Search.*" %>
<%@page import = "java.io.*" %>
<%@page import = "java.util.*" %>
<%@page import = "java.sql.SQLException"%>
<%@page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%

//Login check
    int UID = 0;
    if (session.getAttribute("UID") == null) {
%><%@ include file="loginCheck.jsp" %><%                
    } else {
        UID = Integer.parseInt(session.getAttribute("UID").toString());
        user.User thisUser = new User(Integer.parseInt(session.getAttribute("UID").toString()));
%>
<!DOCTYPE html>
<html>
    <head>
<%
    //Initialize user and project IDs, populate if available
    int projectID = 0;
    int manuscript=0;
    int project=0;
    textdisplay.Project thisProject = null;
    if (request.getParameter("projectID") != null) {
        try{
        projectID = Integer.parseInt(request.getParameter("projectID"));
        thisProject = new textdisplay.Project(projectID);
               }
        catch(NumberFormatException e)
                               {
                       }
    }
    if(request.getParameter("manuscript")!=null)
    {
        try{
        manuscript=Integer.parseInt(request.getParameter("manuscript"));
               }
        catch(NumberFormatException e)
                               {
                       }
    }
    
    //Initialize search parameters, populate if available
    String searchWord="";                                   //query
    if (request.getParameter("searchWord") != null) {
        searchWord = request.getParameter("searchWord");
        if(manuscript>0)
            searchWord += " AND manuscript:"+manuscript;
        if(projectID>0)
                       {
            searchWord+=" AND projectID:"+projectID;
        }
    }
    System.out.print("query:"+searchWord+"\n");
    String language="LA";                                //language
    if (request.getParameter("language") != null) {
        language = request.getParameter("language");
    }
    int order=1;                                //order of results, default is by line
    if (request.getParameter("order") != null) {
        order = Integer.parseInt(request.getParameter("order"));
    }
    boolean paged = true;                   //page of results
    if (request.getParameter("paged") != null) {
        paged = Boolean.parseBoolean(request.getParameter("paged"));
    }
    int pageNumber=1;                           //page of the results to return
    if (request.getParameter("pageNumber") != null) {
        pageNumber = Integer.parseInt(request.getParameter("pageNumber"));
    }
%> 
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title>Search T-PEN</title>
        <link rel="stylesheet" href="css/tpen.css" type="text/css" media="screen, projection">
        <link rel="stylesheet" href="css/print.css" type="text/css" media="print">
        <!--[if lt IE 8]><link rel="stylesheet" href="css/ie.css" type="text/css" media="screen, projection"><![endif]-->
        <link type="text/css" href="css/custom-theme/jQuery.css" rel="Stylesheet" />
        <link  href="http://fonts.googleapis.com/css?family=Nova+Cut:regular&v1" rel="stylesheet" type="text/css" >
        <script type="text/javascript" src="https://ajax.googleapis.com/ajax/libs/jquery/1.7.1/jquery.js"></script>
        <script type="text/javascript" src="https://ajax.googleapis.com/ajax/libs/jqueryui/1.8.16/jquery-ui.js"></script> 
        <script type="text/javascript" src="js/tpen.js"></script>
<!--        <script type="text/javascript" src="js/transcription.js"></script>-->
        <style type="text/css">
            .resultImg {position:relative;height: 1000px;}
            .resultImgDiv {position:relative; overflow: hidden;box-shadow:-1px 1px 5px gray inset;}
            .resultCredit {position: relative;}
            .resultShelfmark {position: relative;color: brown;}
            .resultText {position: relative; float: left;font-family: "Georgia",serif;text-shadow:0 -1px 0 silver;}
            .searchResult {position: relative;width:100%;overflow: auto;background: whitesmoke;padding: 4px;box-shadow:gray -1px 1px 5px;margin:4px 0;}
            #wrapper {max-width:95%;}
            #content,#main {overflow: auto;}
            #imgLoading {border:thin solid #A64129;text-align: center;height: 125px; width:400px;position: fixed;top:50%;left:50%;margin:-75px 0 0 -200px;padding:20px;background: url('images/linen.png') repeat;overflow: auto;z-index: 1;display: none;}
            #imagePreview {height:100%;position:absolute;display:none;left:0;right:0;margin:0 auto;}
            #imagePreviewDiv {height:100%;width:100%;position:fixed;display:none;top:0;left:0;right:0;bottom:0;margin:auto;}
            #highlight {border: red solid thin;box-shadow:0 0 80px black;position: absolute;}
            #searchWord {position: relative;
                         width: 50%;
                         -moz-box-sizing: border-box;
                         -webkit-box-sizing: border-box;
                         box-sizing: border-box;
                         margin: 15px 25%;
                         padding: 1em;
                         font-size: 150%;
            }
            #searchBtn {float: right;margin: -10px 25% 0;}
            select {float: left;clear: both;}
            #main {clear: both;}
        </style>
    </head>
    <body>
        <div id="wrapper">
            <div id="header"><p align="center" class="tagline">transcription for paleographical and editorial notation</p></div>
            <div id="content">
                <h1><script>document.write(document.title); </script></h1>
<!--                search options-->
<form id="searchFor" action="search.jsp" method="POST">
    <input id="searchWord" name="searchWord" type="text" class="text" placeholder="Enter a phrase for which to search" value="" />
    <input id="searchBtn" name="search" type="submit" class="ui-button tpenButton" value="Search T-PEN" />
    <h3>Advanced Filters</h3>
    Select an option from any of the dropdowns below to limit the scope of your search: 
    <%
    textdisplay.Project [] allProjects = thisUser.getUserProjects();
    Stack<Integer> MSs = new Stack();
    StringBuilder pOptions = new StringBuilder("<option class='project' value='0' selected>&gt;Projects</option>");
    for (int i=0;i<allProjects.length;i++){
        //build an option for each project the user is a member of
        textdisplay.Project iproject = allProjects[i];
        pOptions.append("<option class='project' value='").append(iproject.getProjectID()).append("' >").append(iproject.getProjectName()).append("</option>\n");
        //while in a project, add any unique MSs to the stack for the other filter
        textdisplay.Folio [] ifolios = iproject.getFolios();
        for (int j=0;j<ifolios.length;j++){
            Integer iMS = new Integer(new Manuscript(ifolios[j].getFolioNumber()).getID());
            if(MSs.search(iMS)==-1) MSs.push(iMS);
        }
    }
    StringBuilder msOptions = new StringBuilder("<option class='manuscript' value='0' selected>&gt;Manuscripts</option>");
    while (!MSs.empty()){
        //build an option for each ms in the user projects
        Integer msID = MSs.pop();
        textdisplay.Manuscript imanuscript = new Manuscript(msID,true);
        msOptions.append("<option class='manuscript' value='").append(imanuscript.getID()).append("' >").append(imanuscript.getShelfMark()).append("</option>\n");
    }
    %>
    <select id="projectID" name="projectID">
        <%out.println(pOptions.toString());%>
    </select>
    <select id="manuscript" name="manuscript">
        <%out.println(msOptions.toString());%>
    </select>
</form>
                <div id="main">
                    <div id="hitreport"></div>
                    <div id="pageNavigator"></div>
<!--                    results go here-->
<%
if (searchWord.length()>0){
    SearchExecutor searching = new SearchExecutor();
    Stack<Transcription> searchResults = searching.transcriptionSearch(searchWord, language, order, paged, pageNumber, UID+"");
    int totalHits  = searching.getTotalHits();
    int totalPages = searching.getTotalPages();
    int ctr = 1;
    int firstResult = (pageNumber-1) * 20;
    int lastResult = Math.min(firstResult+19,totalHits);
    %>
    <script type="text/javascript">
        $(function(){
            var firstResult = <%out.print(firstResult);%>;
            var lastResult = <%out.print(lastResult);%>;
            var hits = "Showing results " + firstResult + " to " + lastResult;
            $("#hitreport").html(hits);
            var pages = ["<select id='pages' name='pageNumber'>"];
            for (var i=1;i< <%out.print(totalPages+1);%>;i++){
                pages.push("<option value='",i,"'>",i,"</option>");
            }
            pages.push("</select>");
            $("#pageNavigator").html(pages.join(''));
        });
    </script>
<ol id="searchResults">
    <%
    while (!searchResults.empty()) {
        Transcription oneResult = searchResults.pop();
        try{
            if(oneResult.getCreator()!=0){
        user.User userLookup= new User(oneResult.getCreator());
        String userCredit   = (userLookup != null) ? userLookup.getLname()+", "+userLookup.getFname().substring(0,1): "undefined";
        java.sql.Date timestamp = oneResult.getDate();
        Folio folioLookup   = new Folio(oneResult.getFolio());
        StringBuilder thisResult   = new StringBuilder("<li class='searchResult' ");
                thisResult.append("lineleft='").append(oneResult.getX()).append("' ")
                .append("linetop='").append(oneResult.getY()).append("' ")
                .append("linewidth='").append(oneResult.getWidth()).append("' ")
                .append("lineheight='").append(oneResult.getHeight()).append("' ")
                .append("data-creator='").append(oneResult.getCreator()).append("' ")
                .append("data-project='").append(oneResult.getProjectID()).append("' ")
                .append("data-folio='").append(folioLookup.getFolioNumber()).append("' ")
                .append("data-collection='").append(folioLookup.getCollectionName()).append("' ")
                .append("data-lineid='").append(oneResult.getLineID()).append("' >")
                .append("<span class='resultShelfmark left'>").append(folioLookup.getArchiveShelfMark()).append(" ").append(folioLookup.getCollectionName()).append(" ").append(folioLookup.getPageName()).append("</span>")
                .append("<span class='resultCredit right'>").append(userCredit+" (").append(DateFormat.getDateInstance(DateFormat.LONG).format(timestamp)).append(")</span>")
                .append("<span class='imagePreview ui-icon ui-icon-image ui-button' data-imgUrl=").append(oneResult.getImageURL()).append("></span>")
                .append("<span class='resultImgDiv left clear' style='width:").append(oneResult.getWidth()).append("px;")
                .append("height:").append(oneResult.getHeight()).append("px;'>")
                .append("<img class='resultImg' style='left:-").append(oneResult.getX()).append("px;")
                .append("top:-").append(oneResult.getY()).append("px;' ")
                .append("src='css/custom-theme/images/loadingImg.gif' imgsrc='").append(oneResult.getImageURL()).append("' /></span>");
                if(thisUser.isAdmin() || (userLookup == thisUser)){
                    thisResult.append("<div class='resultText'>").append(oneResult.getText()).append("</div>");
                }
                thisResult.append("</li>");
        out.print(thisResult.toString());
        }
       }
        catch(NullPointerException e)
                {
            System.out.print("Error: user is "+oneResult.getCreator()+"\n");
            }
    }%>
</ol>    
    <%
    // build out the pagination, if needed
}
%>
                </div>
<a class="returnButton" href="index.jsp">T-PEN Home</a>
            </div>
        </div>
        <div id="imagePreviewDiv">
            <img alt="Image Preview" src="null" id="imagePreview" />
            <div id="highlight"></div>
        </div>
        <script>
            function compareLines(e1,e2){
                var w1 = parseInt($(e1).find(".resultImgDiv").css("width"));
                var h1 = parseInt($(e1).find(".resultImgDiv").css("height"));
                var l1 = parseInt($(e1).find(".resultImg").css("left"));
                var t1 = parseInt($(e1).find(".resultImg").css("top"));
                var w2 = parseInt($(e2).find(".resultImgDiv").css("width"));
                var h2 = parseInt($(e2).find(".resultImgDiv").css("height"));
                var l2 = parseInt($(e2).find(".resultImg").css("left"));
                var t2 = parseInt($(e2).find(".resultImg").css("top"));
                var testW = Math.abs(w1-w2) < 15;
                var testH = Math.abs(h1-h2) < 10;
                var testL = Math.abs(l1-l2) < 10;
                var testT = Math.abs(t1-t2) < 5;
                return (testW && testH && testL && testT);                      
            }
            function ScrubSearchResults(){
                //perhaps this can be moved up the food chain in the future.
                $(".searchResult").each(function(index,e1){
                    var highlander = $("[data-folio='"+$(e1).attr("data-folio")+"']").filter(function(){
                        return compareLines(e1,this);
                    }); //there can be only one
                    if (highlander.size() > 1) {
                        var hoverResults = [highlander.eq(0).find(".resultCredit").html()];
                        highlander.not(highlander.eq(0)).each(function(){
                            hoverResults.push($(this).find(".resultCredit").html());
                            $(this).remove();
                        });
                        highlander.eq(0).find(".resultCredit").html(highlander.size()+" Entries").attr("title",hoverResults.join(", "));
                    }
                });
                //now fix the sizes of the remaining results and load the imgs
                $(".resultImgDiv").each(function(){
                    var result = $(this)
                    var oldWidth = result.width();
                    result.width("100%");
                    var newWidth = result.width();
                    var ratio = oldWidth/newWidth;
                    result.height(result.height()/ratio)
                    .children("img").css("border","solid 3px red").each(function(){
                        var thisImg = $(this);
                        if (thisImg.attr("imgsrc") == "restricted"){
                            thisImg.replaceWith("<div class='restrictedImg ui-state-error'><span class='left ui-icon ui-icon-locked'></span>This image has restricted permissions and cannot be shown in these results.</div>");
                            return false;
                        }
                        thisImg.load(function(){
                            $(this).css({
                                "height"   : 1000/ratio+"px",
                                "top"   : parseInt($(this).css("top"))/ratio+"px",
                                "left"   : parseInt($(this).css("left"))/ratio+"px",
                                "border":"none 0px transparent"
                            });
                        }).attr("src",function(){
                            return $(this).attr("imgsrc");
                        });
                    });
                    $(".resultImgDiv").has(".restrictedImg").height("auto");
                }).has(".restrictedImg").prev(".imagePreview").remove();
            }
            function showHighlight(l,t,w,h){
                // apply highlight over the image preview to indicate region
                var previewRatio = Page.height()/1000;
                $("#highlight").css({
                    "left"  : parseInt(l)*previewRatio+"px", //+ parseInt($("#imagePreview").position().left) + "px",
                    "top"   : t*previewRatio+ "px",
                    "width" : w*previewRatio+ "px",
                    "height": h*previewRatio+ "px"
                });
            }
            $(window).load(ScrubSearchResults);
            $(function(){
                $(".imagePreview").click(
                    function(){
                        var thisImg = $(this);
                        $("#content").css("opacity",0.5);
                        $("#imagePreviewDiv").height(Page.height()).fadeIn(1000).click(function(){
                                $("#imagePreviewDiv").fadeOut(1000);
                                $("#content").css("opacity",1);
                        });
                        $("#imagePreview")
                            .attr("src",$(this).attr('data-imgUrl'))
                            .load(function(){
                                $(this).fadeIn(1000);
                                $("#content").css("opacity",0.1);
                                var r = thisImg.parent(".searchResult");
                                showHighlight(r.attr("lineleft"),r.attr("linetop"),r.attr("linewidth"),r.attr("lineheight"));
                            });
                    }
                );
            });
        </script>
    </body>
</html>
<%}%>