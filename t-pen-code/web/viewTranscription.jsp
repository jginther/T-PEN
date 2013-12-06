<%-- 
    Document   : viewTranscription
    Created on : Jun 19, 2009, 10:23:32 AM
    Author     : jdeerin1
--%>
<%@page import="textdisplay.Project"%>
<%@page import ="textdisplay.transcriptionPage"%>
<%@page import ="textdisplay.Folio"%>
<%@page import="org.owasp.esapi.ESAPI" %>


<%@page contentType="text/html; charset=UTF-8"  pageEncoding="UTF-8"%>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN"
   "http://www.w3.org/TR/html4/loose.dtd">

<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title>Transcription Preview</title>
        <link rel="stylesheet" href="css/tpen.css" type="text/css" media="screen, projection">
        <link rel="stylesheet" href="css/print.css" type="text/css" media="print">
        <!--[if lt IE 8]><link rel="stylesheet" href="css/ie.css" type="text/css" media="screen, projection"><![endif]-->
        <link type="text/css" href="css/custom-theme/jQuery.css" rel="Stylesheet" />
        <script type="text/javascript" src="https://ajax.googleapis.com/ajax/libs/jquery/1.7.1/jquery.js"></script>
        <script type="text/javascript" src="https://ajax.googleapis.com/ajax/libs/jqueryui/1.8.16/jquery-ui.js"></script>   
        <script type="text/javascript" src="js/tpen.js"></script>   
        <script>
            function pageWidth() {return window.innerWidth != null? window.innerWidth: document.body != null? document.body.clientWidth:null;}
            function pageHeight() {return window.innerHeight != null? window.innerHeight: document.body != null? document.body.clientHeight:null;} 
            document.write("<style type='text/css'>#fullText {height:" + (pageHeight()-200) + "px; width:" + (pageWidth()-405) +"px;overflow:auto;}</style>");

        function navigateTo(dropdown)
        {
            document.location='?uid='+dropdown.value;

        }
        $(function(){
            $( ".returnButton" )
                .appendTo("#content");
        });
        </script>
        <style>
    .line {border-style:dotted;
           border-width:1px;
            border-top:none;
            border-left:none;
            border-right:none;
           }
    #location {bottom:0;top:auto;left:auto;right:0;border-right-width: 0;border-bottom-width: 0;}
    a.export {float: left;padding:.5em;}
        </style>
    </head>
    
    <body>
        <div id="wrapper">
            <div id="header"><p align="center" class="tagline">transcription for paleographical and editorial notation</p></div>
            <div id="content">
                <h1><script>document.write(document.title); </script></h1>
                <div id="main" class="ui-widget ui-widget-content ui-corner-all ui-tabs ui-helper-reset ui-helper-clearfix">
        <%
        int pageno=Integer.parseInt(request.getParameter("p"));
        int projectID=0;
        String projectAppend="";
        if(request.getParameter("projectID")!=null) {
            projectID=Integer.parseInt(request.getParameter("projectID"));
            projectAppend="&projectID="+projectID;
        }
        if(request.getParameter("collection")!=null && request.getParameter("archive")!=null && request.getParameter("page")!=null)
           {
            //This is a view request comming from an external source
            String collection=request.getParameter("collection");
            String archive=request.getParameter("archive");
            String pagea=request.getParameter("page");
            //use the page collection and archive to get the unique page id of this page, then use that to build a folio
            Folio guestViewFolio=new Folio(Folio.getPageNum(pagea, collection, archive));
            int folioNum=Folio.getPageNum(pagea, collection, archive);
            TranscriptionPage myPage=new TranscriptionPage(Folio.getPageNum(pagea, collection, archive));

            if(request.getParameter("uid")!=null)
                {
                myPage=new transcriptionPage(folioNum,Integer.parseInt(request.getParameter("uid")),guestViewFolio);
                }
            out.print(myPage.getContent());
            if(request.getParameter("uid")!=null)
            out.print("<img src=\""+guestViewFolio.getImageURLResize(guestViewFolio.getFolioNumber())+"\" style=\"position:absolute;right:0px;top:0px;z-index:-1;\"/>");
            %><div class="footer">
        <%
        out.print("<span style=\"float:left;color:white;\">Transcription by "+myPage.getUID()+"</span><br/>");
        out.print("<select name=\"p\" id=\"p\" onchange=\"document.location=document.getElementById('p').value+'?'+projectAppend;\">");
        out.print("<option SELECTED>Change Page</option>");
        out.print(guestViewFolio.getTranscriptionFolioDropDown());
        out.print("</select>");
        out.print("<select id=\"u\" onchange=\"document.location=document.getElementById('u').value+'?'+projectAppend;\"><option>Choose Transcriber</option>");
        out.print(guestViewFolio.getTranscriberDropDown(Folio.getPageNum(pagea, collection, archive)));
        out.print("</select>");
        out.print("&nbsp;<a href=\"transcription.jsp?p="+guestViewFolio.getPageNum(pagea, collection, archive)+"\">Transcribe</a>");
        out.print("<span style=\"float:right;color:white;\">Viewing: "+guestViewFolio.getArchiveShelfMark()+" "+guestViewFolio.getCollectionName()+" "+guestViewFolio.getPageName()+"&nbsp;</span>");
        %>
        </div>
          <%  
            } else {%>
        <%@ include file="loginCheck.jsp" %>
        <%
        user.User thisUser=new user.User(uid);
        if(thisUser.isAdmin()) {
//                out.print("<a href=\"compareTranscriptions.jsp?p="+pageno+"\">Compare Transcriptions</a>");
        }
        transcriptionPage myPage;
        if(projectID>0)
            {
            myPage=new transcriptionPage(pageno,projectID,false);
            }
        else
            {
            myPage=new transcriptionPage(pageno,uid);
            }
        //myPage.getOAC();
        String archive; //archive name
        Folio thisFolio = new Folio(pageno, true);
        archive = thisFolio.getArchive();
        //thisFolio.getOAC(uid);
        out.print("<a class=\"export ui-button tpenButton\" href=\"exportUI.jsp?pageNum="+pageno+projectAppend+"\">Export Options</a>");
        %>
         <div class="footer">
        <%
        out.print("<form action=\"viewTranscription.jsp\" method=\"GET\">");
        out.print("<select name=\"p\" id=\"p\" onchange=\"document.location='?p='+document.getElementById('p').value+'?'+projectAppend;\"><option>Change Page</option>");
        out.print(thisFolio.getFolioDropDown());
        out.print("</select>");
        out.print("</form>");
        %>
        </div>
        <div id="fullText">
        <div style="width:45%;float:left;">
        <%
        out.print(""+myPage.getContent().replace("&amp;", "&"));
        %>
        </div>
        <div style="width:45%;float:right;">
            <%
            out.print(""+myPage.getComments());
            %>
        </div>
        </div>
            <div id="location" class="location ui-widget-content ui-corner-tl"><%
                out.print("<span>Viewing: " + ESAPI.encoder().decodeFromURL(thisFolio.getArchiveShelfMark() + " " + thisFolio.getCollectionName() + " " + thisFolio.getPageName()) + "&nbsp;</span>");
                %>
                <span><%out.print(thisFolio.getCopyrightNotice(archive, thisFolio.getCollectionName(), thisFolio.getPageName()));
                String archiveLink = thisFolio.getArchiveLink();
                if (archiveLink != null && archiveLink.compareTo("") != 0) {
                    out.print("<a href=\"" + archiveLink + "\" target=\"_blank\" title=\"View Image\">Source Image</a>");
                }%>&nbsp;
                </span>
            </div>
        <%}%>
                </div>
        <a class="returnButton" href="transcription.jsp?p=<%out.print(pageno+projectAppend);%>">Return to Transcribing</a>
        <%if (projectID>0) {%>
            <a class="returnButton" href="project.jsp?view=true<%out.print(request.getParameter("projectID"));%>" >Return to project page</a>
        <%}%>
            </div>
            <div id="space"></div>
    <%@include file="WEB-INF/includes/projectTitle.jspf" %>
        </div>
    </body>
</html>
