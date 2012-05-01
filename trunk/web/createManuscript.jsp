<%-- 
    Document   : createManuscript
    Created on : Sep 16, 2011, 9:12:57 AM
    Author     : ijrikgnmd
--%>

<%@page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title>JSP Page</title>
    </head>
    <body>
        <%if(request.getParameter("city")!=null)
                               {
        String repository="unknown";
            String archive="Stanford";
            String city="unknown";
            String collection="unknown";
            
                city=request.getParameter("city");
            
            if(request.getParameter("collection")!=null)
                               {
                collection=request.getParameter("collection");
            }
            if(request.getParameter("repository")!=null)
                               {
                repository=request.getParameter("repository");
            }
        textdisplay.Manuscript m=new textdisplay.Manuscript(repository, archive, collection, city);
        
               
        String urls=request.getParameter("urls");
        String [] seperatedURLs=urls.split(";");
        String names=request.getParameter("names");
        String [] seperatedNames=names.split(",");
        for(int i=0;i<seperatedURLs.length;i++)
        {
            textdisplay.Folio.createFolioRecord(collection, seperatedNames[i], seperatedURLs[i], archive, m.getID());
        }
                   }
        
        %>
        <form action="createManuscript.jsp" method="POST">
            city<input type="text" name="city"/>
            repository<input type="text" name="repository"/>
            collection<input type="text" name="collection"/>
            semicolon separate list of image urls<textarea name="urls"></textarea>
            comma separate list of image names<textarea name="names"></textarea>
            <input type="submit"/> 
            
        </form>
    </body>
</html>
