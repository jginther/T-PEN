<%-- 
    Document   : insertLinks
    Created on : Feb 22, 2011, 6:30:44 PM
    Author     : jdeerin1
--%>

<%@page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN"
   "http://www.w3.org/TR/html4/loose.dtd">

<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title>JSP Page</title>
    <script>
        function save(folio)
{
$.get(
    "manuscriptListings?city="+folio,
    "",
    function(data) {


                document.getElementById('thediv').innerHTML=data;

            }



    ,
    "html"
);
}


        </script>
        <script type="text/javascript" src="https://ajax.googleapis.com/ajax/libs/jquery/1.7.1/jquery.js"></script>


    </head>
    <body>
        <h1>Hello World!</h1>
        <div id="thediv"></div>
        <button onclick="save('St. Gallen');">go!</button>
    </body>
</html>
