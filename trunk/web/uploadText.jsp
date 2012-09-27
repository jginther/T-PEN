<%-- 
    Document   : uploadText
    Created on : Jan 16, 2011, 5:36:06 AM
    Author     : jdeerin1
--%>


<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN"
   "http://www.w3.org/TR/html4/loose.dtd">
<%


//You have to be logged in to upload text
            int UID = 0;
            if (session.getAttribute("UID") == null)
                {
%><%@ include file="loginCheck.jsp" %><%                    } else
                    {
                    UID = Integer.parseInt(session.getAttribute("UID").toString());
                    }
int projectID=0;
int p=0;
String location = "";
   if(request.getParameter("projectID")!=null)
        {
        if (request.getParameter("p")!=null) p=Integer.parseInt(request.getParameter("p"));
        projectID=Integer.parseInt(request.getParameter("projectID"));
        location = (p>0) ? 
            "?projectID="+projectID+"&p="+p : 
            "?projectID="+projectID;
        }
    else{
        out.print("no project specified!");
        return;
        }
        %>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title>Upload Text File</title>
        <link rel="stylesheet" href="css/tpen.css" type="text/css" media="screen, projection">
        <link rel="stylesheet" href="css/print.css" type="text/css" media="print">
        <!--[if lt IE 8]><link rel="stylesheet" href="css/ie.css" type="text/css" media="screen, projection"><![endif]-->
        <link type="text/css" href="css/custom-theme/jQuery.css" rel="Stylesheet" />
        <link type="text/css" href="css/jquery.simple-color-picker.css" rel="Stylesheet" />
        <script type="text/javascript" src="https://ajax.googleapis.com/ajax/libs/jquery/1.7.1/jquery.js"></script>
        <script type="text/javascript" src="https://ajax.googleapis.com/ajax/libs/jqueryui/1.8.16/jquery-ui.js"></script> 
        <script type="text/javascript" src="js/tpen.js"></script>
        <style type="text/css">
            #content{max-width: 60%;min-width: 500px;padding:10px;}       
            #fileUpload{position: relative;}
            input[type='file']{position: absolute;top:0;left:0;z-index: 2;cursor: pointer;}
            #pseudo {position: absolute;top:0;left:0;z-index: 1;cursor: pointer;}
        </style>
        <script>
            $(function(){
                var filename = "";
                var nameBegin = 0;
                $("#file")
                    .before('<div id="pseudo" class="ui-corner-all ui-state-default ui-corner-all"><input type="text" placeholder="Select a File"/><span class="right ui-icon-folder-collapsed ui-icon"></span></div>')
                    .change(function(){
                        filename = $("#file").val();
                        nameBegin = filename.lastIndexOf("\\")+1;
                        $("#pseudo").children("input").val(filename.substr(nameBegin));
//                        alert(filename + "\n" + nameBegin);
                    })
                    .bind({
                        mouseenter: function(){
                            $("#pseudo").addClass("ui-state-hover")
                            .children("span").switchClass("ui-icon-folder-collapsed","ui-icon-folder-open",0);
                        },
                        mouseleave: function(){
                            $("#pseudo").removeClass("ui-state-hover")
                            .children("span").switchClass("ui-icon-folder-open","ui-icon-folder-collapsed",0);
                        }
                    })
                    .css("opacity",0);
                $("#pseudo").width($("#file").width());
            });
        </script>
    </head>
    <body>
        <div id="wrapper">
        <div id="header"><p align="center" class="tagline">transcription for paleographical and editorial notation</p></div>
        <div id="content" class="ui-widget">
            <div id="main" class="ui-widget-content ui-corner-all">
            <h3 class="ui-widget-header ui-corner-all">Upload a File</h3>
        <form id="fileUpload" action="uploadTextfile<%out.print(location);%>&selecTab=2" ENCTYPE="multipart/form-data" method="POST">
            <input class="ui-button tpenButton" type="file" id="file" name="file"/><br/>
     <br/><input class="ui-button tpenButton" type="submit" value="Upload" name="Upload"/>
     <input class="ui-button tpenButton" type="reset" value="Cancel" name="cancel" onclick="history.back();return false;" />
        </form>
    </div>
        </div>
        </div>
    </body>
</html>
