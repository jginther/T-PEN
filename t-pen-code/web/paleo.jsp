<%-- 
    Document   : paleoTool
    Created on : Apr 30, 2012, 3:55:33 PM
    Author     : cubap
--%>

<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title>Repeated Glyph Detection</title>
        <link rel="stylesheet" href="css/tpen.css" type="text/css" media="screen, projection">
        <link rel="stylesheet" href="css/print.css" type="text/css" media="print">
        <link type="text/css" href="css/custom-theme/jQuery.css" rel="Stylesheet" />
        <script type="text/javascript" src="https://ajax.googleapis.com/ajax/libs/jquery/1.7.1/jquery.js"></script>
        <script type="text/javascript" src="https://ajax.googleapis.com/ajax/libs/jqueryui/1.8.16/jquery-ui.js"></script> 
        <script type="text/javascript" src="js/tpen.js"></script>
        <style type="text/css">
            #waiting {
                color:white;
                font-family: serif;
                font-size: 150%;
                width: 360px;
                margin: 120px auto;
            }
            #circular3dG{
                position:relative;
                width:128px;
                height:128px;
                left:116px;
            }

            .circular3dG{
                position:absolute;
                background-color:#ffffff;
                width:36px;
                height:36px;
                -webkit-border-radius:38px;
                -moz-border-radius:38px;
                border-radius:38px;
                -webkit-animation-name:bounce_circular3dG;
                -webkit-animation-duration:1.8399999999999999s;
                -webkit-animation-iteration-count:infinite;
                -webkit-animation-direction:linear;
                -moz-animation-name:bounce_circular3dG;
                -moz-animation-duration:1.8399999999999999s;
                -moz-animation-iteration-count:infinite;
                -moz-animation-direction:linear;
                -o-animation-name:bounce_circular3dG;
                -o-animation-duration:1.8399999999999999s;
                -o-animation-iteration-count:infinite;
                -o-animation-direction:linear;
                -ms-animation-name:bounce_circular3dG;
                -ms-animation-duration:1.8399999999999999s;
                -ms-animation-iteration-count:infinite;
                -ms-animation-direction:linear;
            }

            #circular3d_1G{
                left:52px;
                top:8px;
                -webkit-animation-delay:0.69s;
                -moz-animation-delay:0.69s;
                -o-animation-delay:0.69s;
                -ms-animation-delay:0.69s}

            #circular3d_2G{
                left:78px;
                top:30px;
                -webkit-animation-delay:0.9199999999999999s;
                -moz-animation-delay:0.9199999999999999s;
                -o-animation-delay:0.9199999999999999s;
                -ms-animation-delay:0.9199999999999999s;
            }

            #circular3d_3G{
                left:94px;
                top:58px;
                -webkit-animation-delay:1.15s;
                -moz-animation-delay:1.15s;
                -o-animation-delay:1.15s;
                -ms-animation-delay:1.15s;
            }

            #circular3d_4G{
                left:88px;
                top:86px;
                -webkit-animation-delay:1.38s;
                -moz-animation-delay:1.38s;
                -o-animation-delay:1.38s;
                -ms-animation-delay:1.38s}

            #circular3d_5G{
                left:54px;
                top:94px;
                -webkit-animation-delay:1.6099999999999999s;
                -moz-animation-delay:1.6099999999999999s;
                -o-animation-delay:1.6099999999999999s;
                -ms-animation-delay:1.6099999999999999s}

            #circular3d_6G{
                left:10px;
                top:62px;
                -webkit-animation-delay:1.8399999999999999s;
                -moz-animation-delay:1.8399999999999999s;
                -o-animation-delay:1.8399999999999999s;
                -ms-animation-delay:1.8399999999999999s;
            }

            #circular3d_7G{
                left:0px;
                top:18px;
                -webkit-animation-delay:2.07s;
                -moz-animation-delay:2.07s;
                -o-animation-delay:2.07s;
                -ms-animation-delay:2.07s;
            }

            #circular3d_8G{
                left:22px;
                top:0px;
                -webkit-animation-delay:2.3s;
                -moz-animation-delay:2.3s;
                -o-animation-delay:2.3s;
                -ms-animation-delay:2.3s;
            }

            @-webkit-keyframes bounce_circular3dG{
                0%{
                -webkit-transform:scale(1)}

            100%{
                -webkit-transform:scale(.3)}

            }

            @-moz-keyframes bounce_circular3dG{
                0%{
                -moz-transform:scale(1)}

            100%{
                -moz-transform:scale(.3)}

            }

            @-o-keyframes bounce_circular3dG{
                0%{
                -o-transform:scale(1)}

            100%{
                -o-transform:scale(.3)}

            }

            @-ms-keyframes bounce_circular3dG{
                0%{
                -ms-transform:scale(1)}

            100%{
                -ms-transform:scale(.3)}

            }
        </style>
    </head>
    <body>
        <div id="waiting">
        <div id="circular3dG">
            <div id="circular3d_1G" class="circular3dG">
            </div>
            <div id="circular3d_2G" class="circular3dG">
            </div>
            <div id="circular3d_3G" class="circular3dG">
            </div>
            <div id="circular3d_4G" class="circular3dG">
            </div>
            <div id="circular3d_5G" class="circular3dG">
            </div>
            <div id="circular3d_6G" class="circular3dG">
            </div>
            <div id="circular3d_7G" class="circular3dG">
            </div>
            <div id="circular3d_8G" class="circular3dG">
            </div>
        </div>
            <p>
                Please be patient while your page is analyzed.
                This process may take several minutes.
            </p>
        </div>
    <script type="text/javascript">
        var params = location.href.match(/\jsp\?(.*)/)[1];
        document.location.href = "paleoTool.jsp?" + params;
    </script>
    </body>
</html>
