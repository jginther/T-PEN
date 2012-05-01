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
        <title>TPEN - List Annotations</title>
<!--        <script type="text/javascript" src="js/rison.js"></script>-->
        <script type="text/javascript" src="https://ajax.googleapis.com/ajax/libs/jquery/1.7.1/jquery.js"></script>
        <link type="text/css" href="http://ajax.googleapis.com/ajax/libs/jqueryui/1.7.2/themes/base/jquery-ui.css" rel="stylesheet" />
        <style type="text/css">
            #annoTable {width: 100%;}
            .annotation {position: relative;}
            .annotationImg {position: absolute;overflow: hidden;}
            td {border: thin solid black; overflow: hidden;position: relative;}
            #annotationDims {min-width: 100px;width:20%;}
            .annotationDims {color: gray;font-size: small;}
            #map {min-width: 150px;}
            .map {overflow: visible;min-width: 100px;}
            .mapDiv {position: relative;margin:auto;overflow: visible;background: gray url("data:image/gif;base64,R0lGODlhIAAeAMIEAKutqbq8ucrMyd3g3P///////////////yH5BAEAAAQALAAAAAAgAB4AAAN9OLrc/jDKSau9OEehqQiKAABXEHCOgHbqo3LnucAD+q6M/YmDXAOiz+kDCBRrHF7NtPP1Pj3jCXhsNXTX2Qtp7Xi/4LB4TC6bKzgy0wy93FbpriQJMkKDNQVoUl+3Yi8kTwsgQYFCQFwmRThthkV2hXJZM5ItL05laWecCwkAOw==") repeat scroll;}
            .mapPin {position: absolute;top:50%;left:50%;}
        </style>
        <script type="text/javascript">
            var Annotations = {
                // Direct image link. If there are no images, there may be a problem with access to this address
                imgURL  : '<%out.print(imgURL);%>',
                // Width of annotation thumbnails
                imgWidth: .2,
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
                createRows: function(){
                    var appendable = new Array();
                    for (var i=0;i<Annotations.items.length;i++){
                        appendable.push("<tr>\n<td class='annotation'",
                            " data-id='",   Annotations.items[i].id,
                            "' data-w='",   Annotations.items[i].w,
                            "' data-h='",   Annotations.items[i].h,
                            "' data-x='",   Annotations.items[i].x,
                            "' data-y='",   Annotations.items[i].y,
                            "'></td>\n<td>",  Annotations.items[i].text,
                            "\n</td><td class='annotationDims'>x:",Annotations.items[i].x/10,
                            ", y:",     Annotations.items[i].y/10,
                            ", width:", Annotations.items[i].w/10,
                            ", height:",Annotations.items[i].h/10,
                            " <br/>Heights are relative to an image 100 units tall.</td>\n<td class='map'></td>\n</tr>\n");
                    }
                    return appendable.join('');
                },
                populate: function(){
                    $('.annotation').each(function(index){
                        var imgRatio = $(this).width()/parseInt($(this).attr('data-w'));
                        var imgClip  = 'rect('+parseInt($(this).attr('data-y'))*imgRatio+'px '+(parseInt($(this).attr('data-x'))+parseInt($(this).attr('data-w')))*imgRatio+'px '+(parseInt($(this).attr('data-y'))+parseInt($(this).attr('data-h')))*imgRatio+'px '+parseInt($(this).attr('data-x'))*imgRatio+'px)';
                        var imgHeight   = parseInt($(this).attr('data-h'))*imgRatio+'px;';
                        var imgThumb = $('<img alt="annotation image" class="annotationImg" src='+Annotations.imgURL+' style="clip:'+imgClip+';height:'+1000*imgRatio+'px;top:-'+parseInt($(this).attr('data-y'))*imgRatio+'px;left:-'+parseInt($(this).attr('data-x'))*imgRatio+'px;" />');
                        $(this).attr('height',imgHeight).append(imgThumb);
                    });
                },
                map: function(){
                    if (imgHasNoWidth()){
                        return false;
                    }
                    window.clearInterval(interval);
                    var imgRatio = $('.annotationImg').height()/$('.annotationImg').width();
                    $('.map').each(function(){
                        var map = {
                            height : $(this).width()*imgRatio,
                            width  : $(this).width()
                        }
                        if (map.height > $(this).height()){
                            //taller than width allows
                            map = {
                                height : $(this).height(),
                                width  : $(this).height()/imgRatio
                            }
                        }
                        var mapStyle = 'height:'+map.height+'px;width:'+map.width+'px;margin:auto;';
                        var mapDiv = $('<div class="mapDiv" style='+mapStyle
                            +'><img style="height:100%;" src="'+Annotations.imgURL
                            +'" /><span class="ui-icon ui-icon-pin-s mapPin"></span></div>');
                        $(this).append(mapDiv);
                        Annotations.pin($(this));
                    });
                },
                pin: function(map){
                    var containing = map.siblings('.annotation');
                    var data = containing.children('img');
                    var dims = {
                        x100: (-parseInt(data.css('left'))+containing.width()/2)/data.width(),
                        y100: (-parseInt(data.css('top'))+containing.height()/2)/data.height()
                    };
                    map.find('.mapPin').css({
                        'left'  : map.children('.mapDiv').width()*dims.x100-8,
                        'top'   : map.children('.mapDiv').height()*dims.y100-13
                    });
                }
            }
            var imgHasNoWidth = function(){return $('.annotationImg').width()==0;};
            var interval = window.setInterval(Annotations.map, 200);
            $(function(){
                $('#annoTable tbody').append(Annotations.createRows());
                $('#annotationCol').width(Annotations.imgWidth*100+'%');
                var thisImg = new Image();
                $(thisImg).load(Annotations.populate());
                thisImg.src = Annotations.imgURL;
                $('.annotationImg').one('load', Annotations.map());
            });
        </script>
    </head>
    <body>
        <table id="annoTable">
            <colgroup>
                <col id="annotationCol" />
                <col id="annotationDesc" />
                <col id="annotationDims" />
                <col id="map" />
            </colgroup>
            <thead>
                <tr>
                    <th>Annotation</th>
                    <th>Description</th>
                    <th>Dimensions</th>
                    <th>Location</th>
                </tr>
            </thead>
            <tbody>
            </tbody>
            <tfoot></tfoot>
        </table>
    </body>
</html>
