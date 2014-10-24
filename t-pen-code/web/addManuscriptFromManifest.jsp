<%-- 
    Document   : addManuscriptFromManifest
    Created on : Dec 15, 2011, 2:26:34 PM
    Author     : ijrikgnmd
--%>

<%@page import="java.net.URL"%>
<%          
        if (session.getAttribute("UID") == null) {
        %><%@ include file="loginCheck.jsp" %><%
                   return;} 
        int UID=Integer.parseInt(session.getAttribute("UID").toString());
        user.User thisOne=new user.User(UID);
        if(!thisOne.isAdmin())
                       {
            out.print("You must be an administrator to use this page");
            return;
                       }
%> 
<!DOCTYPE html>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title>JSP Page</title>
    </head>
    <body>
        <%
        if(request.getParameter("manifest")!=null )
        {
                
                String manifestURL = request.getParameter("manifest");
                String city = request.getParameter("city");
                String archive = request.getParameter("archive");
                String repository = request.getParameter("repository");
                String collection = request.getParameter("collection");
                String format=request.getParameter("format");
                textdisplay.Manuscript ms = new textdisplay.Manuscript(repository, archive, collection, city);
                URL[] urls = new URL[1];
                urls[0] = new URL(manifestURL);
                DMSTech.sequence s = new DMSTech.sequence(urls, format, ms.getID());
                DMSTech.canvas[] allCanvases = s.getSequenceItems();
                out.print("Created MS "+ ms.getID()+" with shelfmark "+ms.getShelfMark()+"<br>");
                for (int i = 0; i < allCanvases.length; i++) {
                    DMSTech.canvas c = allCanvases[i];
                    int folID=textdisplay.Folio.createFolioRecord(ms.getCollection(), c.getTitle(), c.getImageURL()[0].getImageURL(), ms.getArchive(), ms.getID(), c.getPosition(), c.getCanvas());
                    textdisplay.Folio f=new textdisplay.Folio(folID);
                    out.print("Created folio "+f.getPageName()+" with image "+f.getImageURL()+"<br>");
                }
            }
                %>
                <form target="addManuscriptFromManifest.jsp" method="POST">
                    Repository<input type="text" id="repository" name="repository"/><br>
                    City<input type="text" id="city" name="city"/><br>
                    Archive<input type="text" id="archive" name="archive"/><br>
                    Collection<input type="text" id="collection" name="collection"/><br>
                    Manifest URL<input type="manifest" id="city" name="manifest"/><br>
                    
                    Format<input type="text" id="format" name="format"/><br>
                    <input type="submit" value="Ingest"/>
                </form>
    </body>
</html>
