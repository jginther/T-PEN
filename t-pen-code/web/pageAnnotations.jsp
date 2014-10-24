<%-- 
    Document   : listAnnotations.jsp
    Created on : Feb 15, 2012, 5:23:47 PM
    Author     : cubap
--%>
<%@page import ="user.Group"%>
<%@page import ="net.sf.saxon.functions.Collection"%>
<%@page import ="utils.Tool"%>
<%@page import ="utils.UserTool"%>
<%@page import ="org.apache.commons.lang.StringEscapeUtils"%>
<%@page import ="java.text.SimpleDateFormat"%>
<%@page import ="java.text.DateFormat"%>
<%@page import ="java.util.Calendar"%>
<%@page import ="com.hp.hpl.jena.util.cache.Cache"%>
<%@page import ="org.owasp.esapi.tags.EncodeForHTMLTag"%>
<%@page import ="textdisplay.*"%>
<%@page import="org.owasp.esapi.ESAPI" %>
<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
    <head>
<%
    int projectID = 0;
    textdisplay.Project thisProject = null;
    if (request.getParameter("projectID") != null) {
        projectID = Integer.parseInt(request.getParameter("projectID"));
        thisProject = new textdisplay.Project(projectID);
    }
    int folioNum = 501;
    request.setCharacterEncoding("UTF-8");
    try {
        if (request.getParameter("p") != null) {
            folioNum = Integer.parseInt(request.getParameter("p"));
        }
    } catch (NumberFormatException e) {
        e.printStackTrace();
    }
    String imgURL = new Folio(folioNum).getImageURL();
    textdisplay.Annotation[] allAnnotations = textdisplay.Annotation.getAnnotationSet(projectID, folioNum);
    int annoLength = allAnnotations.length;
%>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title>TPEN - Annotations</title>
<!--        <script type="text/javascript" src="js/rison.js"></script>-->
        <script type="text/javascript" src="https://ajax.googleapis.com/ajax/libs/jquery/1.7.1/jquery.js"></script>
        <link type="text/css" href="http://ajax.googleapis.com/ajax/libs/jqueryui/1.7.2/themes/base/jquery-ui.css" rel="stylesheet" />
        <style type="text/css">
            * {box-sizing:border-box;}
            #annotations,#annotationImg {position:absolute;top:0;left:0;}
            .annotation {position: absolute;border:thin solid blue;overflow: visible;}
            .annotation:hover:after {
                position: relative;
                display: block;
                background-color: lightsteelblue;
                white-space: pre-line;
                content: attr(data-text)"\00000A Location:(" attr(data-x) "," attr(data-y) ")\00000A Dimensions:(" attr(data-width) "," attr(data-height) ")";
           }
        </style>
        <script type="text/javascript">
            var Annotations = {
                // Direct image link. If there are no images, there may be a problem with access to this address
                imgURL  : '<%out.print(imgURL);%>',
                // Create an object for each annotation
                items: [<%
            for (int i=0;i<annoLength;i++){
                if (i==0) out.print("{");
                else out.print(",{");
                out.print("id: "+ allAnnotations[i].getId()+","
                    +   "w: "   + allAnnotations[i].getW()+","
                    +   "h: "   + allAnnotations[i].getH()+","
                    +   "x: "   + allAnnotations[i].getX()+","
                    +   "y: "   + allAnnotations[i].getY()+","
                    +   "text: '"+ allAnnotations[i].getText()+"'"
                    +   "}");
            }
                %>],
                draw: function(){
                    var appendable = [];
                    for (var i=0;i<Annotations.items.length;i++){
                        appendable.push(Annotations.build(Annotations.items[i]));
                    }
                    Annotations.fillscreen();
                    $('#annotations').empty().append(appendable.join(''));                   
                },
                build: function(a){
                    $("#annotationImg").height("1000px").width("");
                    var newAnno = ["<div class='annotation'",
                        " data-id='",a.id,
                        "' data-width='",a.w,
                        "' data-height='",a.h,
                        "' data-x='",a.x,
                        "' data-y='",a.y,
                        "' data-text='",a.text,
                        "' style='top:",parseInt(a.y)/10,
                        "%;left:",parseInt(a.x)/$("#annotationImg").width()*100,
                        "%;height:",parseInt(a.h)/10,
                        "%;width:",parseInt(a.w)/$("#annotationImg").width()*100,"%;'/>"
                    ];
                    return newAnno.join('');
                },
                fillscreen: function(){
                    $("#annotationImg").width("100%").height("auto");
                    if ($("#annotationImg").height() < $(window).height()) {
                        // switch to full height
                        $("#annotationImg").width("auto").height("100%");
                    }
                    $('#annotations').height($("#annotationImg").height()).width($("#annotationImg").width())
                }
            }
            $(function(){
                var thisImg = new Image();
                thisImg.src = Annotations.imgURL;
                $(thisImg).load(Annotations.draw);
                $(window).resize(function(){
                    clearTimeout(this.id);
                    this.id = setTimeout(Annotations.draw, 500);
                })
            });
        </script>
    </head>
    <body id='body'>
        <img id="annotationImg" alt="manuscript image" src="<%out.print(imgURL);%>" />
        <div id="annotations"></div>
    </body>
</html>
