<%-- 
    Document   : manuscriptAdmin
    Created on : Mar 15, 2011, 6:11:25 PM
    Author     : jdeerin1
--%>
<%@page import="org.apache.commons.lang.StringEscapeUtils"%>
<%@page import="sun.nio.cs.MS1250"%>
<%@page import="textdisplay.Folio"%>
<%@page import="org.owasp.esapi.ESAPI" %>
<%         
        if (session.getAttribute("UID") == null) {
        %><%@ include file="loginCheck.jsp" %><%
                   } else {
%>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title>Manuscript Administration</title>
        <link rel="stylesheet" href="css/tpen.css" type="text/css" media="screen, projection">
        <link rel="stylesheet" href="css/print.css" type="text/css" media="print">
        <!--[if lt IE 8]><link rel="stylesheet" href="css/ie.css" type="text/css" media="screen, projection"><![endif]-->
        <link type="text/css" href="css/custom-theme/jQuery.css" rel="Stylesheet" />
        <script type="text/javascript" src="https://ajax.googleapis.com/ajax/libs/jquery/1.7.1/jquery.js"></script>
        <script type="text/javascript" src="https://ajax.googleapis.com/ajax/libs/jqueryui/1.8.16/jquery-ui.js"></script> 
        <script type="text/javascript" src="js/tpen.js"></script>
        <style type="text/css">
            #updateMetadata,#grantAccess,#removeUser,#updateImgName {width:475px;padding: 5px;margin: 5px;overflow: hidden;}
            #removeUser a {display: block;border:solid thin transparent;}
            label {font-weight: normal;width: 100%;padding: 2px;}
            #results {font-weight: bold;font-size: 16px;text-align: center;}
            #imagePreview,#imageSelect {width:100%}
       </style>
        <script type="text/javascript">
            $(function(){
//                $("#updateMetadata,#grantAccess").find("input[type='submit']")
//                    .addClass("ui-corner-all ui-state-default ui-button")
//                    .hover(function(){$(this).toggleClass("ui-state-hover")});
//                $( ".returnButton" ).addClass("ui-state-default ui-corner-bl ui-corner-br ui-helper-clearfix")
//                    .prepend("<span class='ui-icon ui-icon-arrowreturnthick-1-w right'></span>")
//                    .css("display","inline-block")
//                    .hover(function(){$(this).toggleClass("ui-state-hover")});
                $("#removeUser").children("a").prepend("<span class='left ui-icon ui-icon-circle-close'></span>")
                    .addClass("clear-left ui-corner-all")
                    .hover(function(){$(this).toggleClass("ui-state-error")});
                $("#results").show("pulsate","fast");
                $("#imageSelect").change(function(){
                    // copy value to text input
                    var selectedImage = $("#imageSelect").find(":selected");
                    $("#imageNameNew").unbind("keyup").val(selectedImage.text()).keyup(function(){
                        // Set value to equal and update attribute to POST
                        selectedImage.text($(this).val()).attr({
                            "imgName":$(this).val()
                        });
                    });
                    // update img
                    $("#imagePreview").addClass("ui-state-disabled").load(function(){
                        $(this).removeClass("ui-state-disabled");
                    }).attr("src",selectedImage.val());
                }).change();
                $("#renameImages").submit(function(){
                    var thisForm = $(this);
                    $(this).find("[imgName]").each(function(){
                        var imgname = $(this).attr('imgName').replace("'","&#39;");
                        if (imgname.length < 1){
                            var cfrm = confirm('You are saving an image with a blank name.\n\nContinue?');
                            if (cfrm) imgname = ' ';
                            else return false;
                        } 
                        var newName = "<input type='hidden' name='imgName[]' value='"+$(this).attr("folio")+"TPENIMGNAME"+imgname+"'/>";
                        thisForm.append(newName);
                    })
                });
//                $("#imageUpdate").click(function(){
//                    // build and POST image names
//                    var params = new Array({name:"ms",value:msID});
//                    $("#imageSelect").find("option").each(function(){
//                        if ($(this).is("[updated]")){
//                            if ($(this).text().indexOf("TPENIMGNAME") != -1) return false;
//                            params.push({name:"imgName[]",value:$(this).attr("folio")+"TPENIMGNAME"+$(this).text()});
//                        }
//                    });
//                    if(params.length > 0){
//                        $.post("manuscriptAdmin.jsp",$.param(params));
//                    } else {
//                        alert("No changes were recorded.");
//                        return false;
//                    }
//                });
            });
        </script>
    </head>
    <body>
        <%
        int UID=0;
        if(session.getAttribute("UID")==null)
     {
    %><%@ include file="loginCheck.jsp" %><%
            }
else
    {
        UID=Integer.parseInt(session.getAttribute("UID").toString());
    }
        if(request.getParameter("ms")==null)
        {
            out.print("ms id not specified!");
            return;
        }
        int msID=Integer.parseInt(request.getParameter("ms"));
        user.User thisUser=new user.User(UID);

        textdisplay.Manuscript ms=new textdisplay.Manuscript(msID,true);
        boolean isRestricted = true;
        if(request.getParameter("unrestricted") != null && thisUser.isAdmin()) 
                       {isRestricted = false;}
        if(isRestricted){ //prevent NPE if ms has no controlling user
        if(ms.getControllingUser().getUID()!=UID && !thisUser.isAdmin())
                {
                String errorMessage = thisUser.getFname() + ", you are not the controlling user for this manuscript.";
            %><%@include file="WEB-INF/includes/errorBang.jspf" %><%
                return;
            }
           }
   %>
                <div id="wrapper">
            <div id="header"><p align="center" class="tagline">transcription for paleographical and editorial notation</p></div>
            <div id="content">
                <h1><script>document.write(document.title); </script></h1>
                <div id="main" class="ui-tabs ui-widget ui-widget-content ui-corner-all">
        <h3 class="ui-widget-header ui-tabs ui-corner-all ui-state-default"><%
        String headerText = ms.getShelfMark();
        if (ms.getControllingUser() != null) {
            User controllingUser = ms.getControllingUser();
            String cUser = controllingUser.getFname() + " " 
                    + controllingUser.getLname() 
                    + " (" + controllingUser.getUname() + ")";
            headerText += " controlled by "+cUser;
        }
        out.print(headerText);
   %></h3>
                    <div id="results"></div>
 <%
         if(request.getParameter("submitted")!=null) {           
            if(request.getParameter("city")!=null)
                {
                String city=request.getParameter("city");
                String repo=request.getParameter("repository");
                String collection=request.getParameter("identifier");
                ms.update(city, repo, collection);
                //update the ms object for later output
                ms=new textdisplay.Manuscript(ms.getID(),true);
                out.print("<script>");
                out.print("$('#results').addClass('ui-state-active ui-corner-all').html('Successfully updated manuscript.')");
                out.print("</script>");
                }
            }
        if(request.getParameter("grant")!=null) {
            String grantAll = (request.getParameter("grantAll")!=null)? request.getParameter("grantAll") : "none";
            textdisplay.Manuscript[] mssToControl = null;
            if (grantAll == "none"){
                // ms is used without change
                //TODO learn how to fake casting manuscript to manuscript[]
            } else if (grantAll.length() == 10){ //"repository"
                mssToControl = textdisplay.Manuscript.getManuscriptsByRepository(ms.getRepository());
            } else if (grantAll.length() == 4){ //"user"
                mssToControl = thisUser.getUserControlledManuscripts();
            }
//            try{
                user.User toAdd=new user.User(request.getParameter("email"));
                // Grant access to just one MS
                if((toAdd.getUID()>0 && !ms.isAuthorized(toAdd) && mssToControl == null)||thisUser.isAdmin()) {
                    ms.authorizeUser(toAdd.getUID());
                    out.print("<script>");
                    out.print("$('#results').addClass('ui-state-active ui-corner-all').html('"+toAdd.getFname()+" "+toAdd.getLname()+" added successfully.')");
                    out.print("</script>");
                // Grant access to all in repository or user
                } else {
                    for(int i=0;i<mssToControl.length;i++){
                        if(mssToControl[i].isRestricted() && ((mssToControl[i].getControllingUser().getUID() == thisUser.getUID())||thisUser.isAdmin())) {
                            mssToControl[i].authorizeUser(toAdd.getUID());
                        }
                    }
                    out.print("<script>");
                    out.print("$('#results').addClass('ui-state-active ui-corner-all').html('"+toAdd.getFname()+" "+toAdd.getLname()+" added successfully.')");
                    out.print("</script>");
                }
//            }
//            catch (Exception e)
//                    {
//                out.print("<script>");
//                out.print("$('#results').addClass('ui-state-active ui-corner-all').html('Failed to add user.')");
//                out.print("</script>");
//                }
            }
        if(request.getParameter("deactivateUser")!=null){
            int userToRemove=Integer.parseInt(request.getParameter("deactivateUser"));
            ms.deauthorizeUser(userToRemove);
                out.print("<script>");
                out.print("$('#results').addClass('ui-state-active ui-corner-all').html('Removed user.')");
                out.print("</script>");
            }
        if(request.getParameterValues("imgName[]") != null){
            // change names of images
            if ((ms.getControllingUser().getUID() == thisUser.getUID()) || thisUser.isAdmin()){
                String [] imageNames = request.getParameterValues("imgName[]");
//                int imageNum = -1;
//                String imageName = "undefined";
                String thisName[] = {"-1","undefined"};
                for (int i = 0; i<imageNames.length; i++){
                    thisName = imageNames[i].split("TPENIMGNAME");
                    textdisplay.Folio changeFolio = new Folio(Integer.parseInt(thisName[0]));
                    String newName = thisName[1];
                    changeFolio.setPageName(newName);
                }
                out.print("<script>");
                out.print("$('#results').addClass('ui-state-active ui-corner-all').html('Image names updated.')");
                out.print("</script>");
             } else {
                String errorMessage = thisUser.getFname() + ", you are not the controlling user for this manuscript.";
            %><%@include file="WEB-INF/includes/errorBang.jspf" %><%
                return;
             }
        }
        %>
        <form id="updateMetadata" action="manuscriptAdmin.jsp?ms=<%out.print(ms.getID());%>&unrestricted=<%out.print(isRestricted);%>" method="post" class="left ui-widget-content ui-corner-br ui-corner-tl">
            <h3>Change Manuscript Metadata</h3> 
            <label for="city" class="clear-left">City:</label>   <input class="left" type="text" name="city" value="<%out.print(ms.getCity()); %>"><br>
        <label for="repository" class="clear-left">Repository:</label>   <input class="left" type="text" name="repository" value="<%out.print(ESAPI.encoder().decodeFromURL(ms.getRepository())); %>"><br>
        <label for="identifier" class="clear-left">Manuscript Identifier:</label>   <input class="left" type="text" name="identifier" value="<%out.print(ESAPI.encoder().decodeFromURL(ms.getCollection())); %>"><br>
            <input class="clear-left right tpenButton ui-button" type="submit" name="submitted" value="Update Metadata">
        </form>
            <%if(isRestricted){ //prevent NPE if ms has no controlling user
            if (!isRestricted || (ms.getControllingUser().getUID() == thisUser.getUID())||thisUser.isAdmin()){%>
        <div id="updateImgName" class="right ui-widget-content ui-corner-br ui-corner-tl">
            <h3>Modify Image Names</h3>
            <form id="renameImages" action="manuscriptAdmin.jsp?ms=<%out.print(msID);%>" method="POST">
                <span class="left">Select a page from the list and enter a new title.</span>
                <select id="imageSelect" class="left clear-left">
                    <%
                    textdisplay.Folio [] allFolios = ms.getFolios();
                    int msLength = allFolios.length;
                    for (int i=0;i<msLength;i++){
                        out.print("<option class='left clear-left' folio='"+allFolios[i].getFolioNumber()+"' value='"+allFolios[i].getImageURL()+"'>"+allFolios[i].getPageName()+"</option>");
                    }
                    %>
                </select>
                <label for="imageNameNew">New Label:<input id="imageNameNew" type="text" placeholder="Image Name" value="" /></label>
                <input type="submit" id="imageUpdate" class="right clear-left tpenButton ui-button" value="Commit Changes" />
            </form>
            <img id="imagePreview" src="css/custom-theme/images/loadingImg.gif" />
        </div>
        <form id="grantAccess" action="manuscriptAdmin.jsp?ms=<%out.print(ms.getID());%>" method="post" class="left clear-left ui-widget-content ui-corner-br ui-corner-tl">
            <h3>Grant Access</h3>
                Select the person you wish to grant access to:<br />
                <select name="email" class="combobox">
                <%
user.User [] activeUsers = user.User.getAllActiveUsers();
                               for (int i = 0; i < activeUsers.length; i++) {
                    out.print("<option value=" + activeUsers[i].getUname() + ">" + activeUsers[i].getFname() + " " + activeUsers[i].getLname() + " (" + activeUsers[i].getUname() + ")" + "</option>");
                }

                %>
            </select><br/>
            Select an option to also grant access to related manuscripts:
            <label class="clear"><input type="radio" name="grantAll" value="none" checked />Just this manuscript</label>
            <label class="clear"><input type="radio" name="grantAll" value="repository" />All controlled <span class="loud"><%out.print(ms.getRepository());%></span> manuscripts</label>
            <label class="clear"><input type="radio" name="grantAll" value="user" />All manuscripts controlled by <span class="loud"><%out.print(thisUser.getUname());%></span></label>
            <input class="clear right tpenButton ui-button" type="submit" name="grant" value="Grant Access"/>
        </form><br/>
        <div id="removeUser" class="left ui-widget-content ui-corner-br ui-corner-tl">
            <h3>Remove Access</h3>
        <%
                user.User [] allUsers=ms.getAuthorizedUsers();
        for(int i=0;i<allUsers.length;i++){
         //   if (i>0){
         //       if (allUsers[i].getUID()==allUsers[i-1].getUID()) continue;
         //   }
            out.print("<a href=\"manuscriptAdmin.jsp?deactivateUser="+allUsers[i].getUID()+"&ms="+ms.getID()+"&submitted=true\">Remove access for user "+allUsers[i].getFname()+" "+allUsers[i].getLname()+" ("+allUsers[i].getUname()+")</a>");
        }
        %>
        </div>
        <%}}%>
                </div>
                             <a class="returnButton" href="admin.jsp?selecTab=1">Return to Administration Page</a>
                           <a class="returnButton" href="index.jsp">Return to TPEN Homepage</a>
            </div>
                </div>
   </body>
</html>
<%}%>