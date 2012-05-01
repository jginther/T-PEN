<%-- 
    Document   : index
    Created on : Jun 16, 2010, 11:25:25 AM
    Author     : jdeerin1
--%>

<%@page import="detectimages.blob"%>
<%@page import="match.blobGetter"%>
<%@page import="textdisplay.Folio"%>
<%@page import="match.folio"%>
<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN"
    "http://www.w3.org/TR/html4/loose.dtd">
<%@page import ="match.matchLocater"%>
<%@page import ="match.allBlobDivs"%>
<%@page import ="textdisplay.Manuscript"%>
<%@page import ="Paleography.ComparisonRunner"%>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title>Repeated Glyph Detection</title>
        <link rel="stylesheet" href="css/tpen.css" type="text/css" media="screen, projection">
        <link rel="stylesheet" href="css/print.css" type="text/css" media="print">
        <link type="text/css" href="css/custom-theme/jQuery.css" rel="Stylesheet" />
        <link href='http://fonts.googleapis.com/css?family=Stardos+Stencil:700' rel='stylesheet' type='text/css'>
        <script type="text/javascript" src="https://ajax.googleapis.com/ajax/libs/jquery/1.7.1/jquery.js"></script>
        <script type="text/javascript" src="https://ajax.googleapis.com/ajax/libs/jqueryui/1.8.16/jquery-ui.js"></script> 
        <script type="text/javascript" src="js/tpen.js"></script>
        <style type="text/css">
            #blobDisplay {position: relative; overflow: auto; width: 75%; height:100%;}
            #blobDetails {width:25%; padding: 4px;}
            #blobImage {position: relative;left:0;top:0;width:100%;}
            .blob {position:absolute; background: rgba(15,15,255,.5);}
            .blob:hover, .blobHighlight {background: rgba(255,215,0,.5);box-shadow: 0 0 2px rgba(15,15,255,.5);}
            #wrapper {width:90%;}
            #main {padding: 5px;}
            #beta {
                font-family: "Stardos Stencil",monospace;
                color: rgb(142,11,0);
                -ms-transform:rotate(-20deg);
                -moz-transform:rotate(-20deg);
                -webkit-transform:rotate(-14deg);
                transform: rotate(-20deg);
                text-shadow: -1px 1px 2px rgba(0, 0, 0, 0.5);
                display: inline-block;
                font-variant: normal;
                font-size: 6em;
                color: rgba(142,11,0,0.5);
                text-shadow: 1px 1px 1px rgba(255, 255, 255, .5),0px 0px 9px rgba(0, 0, 0, 0.7);
                position: fixed;
                left: 0;
                top: 0;
                z-index: 5;
            }
        </style>
    </head>
    <body>
        <div id="beta">BETA</div>
        <%
            int folioNum = 0;
            int blob = 0;
            if (request.getParameter("p") != null) {
                folioNum = Integer.parseInt(request.getParameter("p"));
            } else {
                // no "p" parameter for folio number
                out.print("No folio specified.");
                return;
            }
%>
        <div id="wrapper">
            <div id="header"><p align="center" class="tagline">transcription for paleographical and editorial notation</p></div>
            <div id="content">
                <h1><script type="text/javascript">document.write(document.title); </script></h1>
        
        <%            if (request.getParameter("b") != null) {
                %>
                <h2>Matching Glyphs</h2>
                <p>
                    Following are the matches<sup>*</sup> found for the glyph 
                    you identified. Click on the image to view the image of 
                    origin.
                </p>
                <div>
                    <p class="small quiet"><sup>*</sup>
                        Image analysis is time-intensive work. At this time, 
                        only cached images from this manuscript are included in
                        the comparison. As more images are analyzed, more 
                        matches will appear.
                    </p>
                </div>
                <div id="main">
        <%
                blob = Integer.parseInt(request.getParameter("b"));
                out.print(new matchLocater(folioNum, blob, true).getUrls());
                %>
                </div>
                    <%
            } else {
                Manuscript m = new Manuscript(folioNum);
                Folio f=new textdisplay.Folio(folioNum);
                if (f.isReadyForPaleographicAnalysis()) {
                    if (request.getParameter("highlightblob") == null){
                    ComparisonRunner comp = new ComparisonRunner(f);
                    }
                    //ComparisonRunner comp = new ComparisonRunner(m);
                } else {
                    // This is not ready for analysis
                    out.print("Sorry, this image has not been analyzed yet.");
                    return;
                    //maybe force an analysis
                }%>
                <h2>Detected Glyphs</h2>
                <div id="main">
        <div id="blobDetails" class="right">
            <h2>Using this page</h2>
            <p>Highlighted are all regions which match<sup>*</sup> other images.</p>
            <p>Click on any highlighted region to view the matches.</p>
            <p>Use the drop-down menu to select a similar page.</p>
            <select onchange="document.location='paleo.jsp'+(this.options[this.selectedIndex].value);">
                <option>Change Page</option>
                <%allBlobDivs a=null;
                if (request.getParameter("highlightblob") == null){
                     a= new allBlobDivs(folioNum);
                    
                    }
                out.print(matchLocater.getDropdown(m.getID()));
                %>
            </select>
                <div>
                    <p class="small quiet"><sup>*</sup>
                        Image analysis is time-intensive work. At this time, 
                        only cached images from this manuscript are included in
                        the comparison. As more images are analyzed, more 
                        matches will appear.
                    </p>
                </div>
        </div>
        <div id="blobDisplay">
            <%
                out.print("<img id='blobImage' class='ui-corner-left' src='imageResize?folioNum=" + folioNum + "&height=2000' />");
                if (request.getParameter("highlightblob") == null){
                out.print(a.getDivs());
                }
            %>
             <%
if (request.getParameter("highlightblob") != null){
    // show the blob(s)
    String blobToHighlight = request.getParameter("blob");

    blob b= blobGetter.getRawBlob(folioNum,Integer.parseInt(blobToHighlight));
    out.print("<a blobid='"+blobToHighlight+"' class='blob' title='"+
                    "' href='paleo.jsp?b="+blobToHighlight+"&p="+folioNum+
                    "' blobx='"+b.getX()+"' bloby='"+b.getY()+
                    "' blobwidth='"+b.getWidth()+"' blobheight='"+
                    b.getHeight()+"'></a>");
    %><script>
        $("[blobid='<%out.print(blobToHighlight);%>']").addClass('blobHighlight');
        <%}

%>
        </script>
        <%
            }
        %>
        </div>
    </div>
        <script type="text/javascript">
            var scrubBlobs = function() {
                var blobImage = $("#blobImage")[0];
                var originalX = blobImage.width/blobImage.height*2000;
                $(".blob").each(function(){
                    var blob = $(this)
                    var bX = parseInt(blob.attr("blobx"))/originalX;
                    var bY = parseInt(blob.attr("bloby"))/2000;
                    var bH = parseInt(blob.attr("blobheight"))/2000;
                    var bW = parseInt(blob.attr("blobwidth"))/originalX;
                    blob.css({
                        'left'  :   Page.convertPercent(bX, 2)+"%",
                        'top'  :   Page.convertPercent(bY, 2)+"%",
                        'height'  :   Page.convertPercent(bH, 2)+"%",
                        'width'  :   Page.convertPercent(bW, 2)+"%"
                    });
                });
            }
            $(window).load(scrubBlobs);
        </script>
           
                </div>
        </div>
    </body>
</html>
