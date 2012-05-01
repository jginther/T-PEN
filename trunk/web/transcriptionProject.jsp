<%@page import ="textdisplay.Transcription"%>
<%@page import ="textdisplay.Folio"%>
<%@page import ="textdisplay.Line"%>
<%@page import ="textdisplay.Hotkey"%>
<%@page import ="textdisplay.TagButton"%>
<%@page import ="textdisplay.Project"%>

<%@page contentType="text/html; charset=UTF-8"  %>
<%

//You have to be logged in to transcribe
int UID=0;
if(session.getAttribute("UID")==null)
     {
    %><%@ include file="loginCheck.jsp" %><%
            }
else
    {
        UID=Integer.parseInt(session.getAttribute("UID").toString());
    }
%>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN"
   "http://www.w3.org/TR/html4/strict.dtd">
<html>
    <head>
        <script type="text/javascript" src="js/transcription.js"></script>
        <%

       request.setCharacterEncoding("UTF-8");
        int pageno=501;
                try
                {
                    pageno=Integer.parseInt(request.getParameter("p"));
                }
                catch (NumberFormatException e)
                {

                }
        int projID=0;
        if(session.getAttribute("project")!=null)
            {
            projID=(Integer)session.getAttribute("project");
            //out.print(""+projID);
            }
        else
            if(request.getParameter("proj")!=null)
            {
        projID=Integer.parseInt(request.getParameter("proj"));
        
        session.setAttribute("project", projID);
            }
        
        Project thisProject=new Project(projID);
        


        if(request.getParameter("save")!=null||request.getParameter("preview")!=null)
        for(int i=0;i<150;i++)
            {
            if(request.getParameter(""+i)!=null)
            {
            String comment=""+request.getParameter("comment"+i);
            String tmpstr=""+request.getParameter(""+i);
            Transcription thisLine=new Transcription(pageno,i,request.getParameter(""+i),comment,UID,thisProject.getProjectID());
            thisLine.commit();
            }

            }
        if(request.getParameter("preview")!=null)
            {
                out.print("<script>document.location=\"viewTranscription.jsp?p="+(pageno)+"\";</script>");
            }
        String archive="ENAP";
        /*if(request.getParameter("archive")!=null)
            {
                archive=request.getParameter("archive");
            }*/
        Folio thisFolio=new Folio(pageno,true);
        archive=thisFolio.getArchive();
        if(thisFolio.hasIPRRestrictions())
            {
            //out.print(thisFolio.getIPRRestrictions());
            }
        out.print("<title>TPEN</title>");
        %>
<style>
    .footer{
        position:fixed;
        bottom:0px;
        height:40px;
        width:100%;
        left:0px;
        border-style:solid;
        border-color:maroon;
        z-index:2;
        background:maroon;
        text-align:center;

    }
    .footer a
    {
        color:white;
    }
    .lookLikeButtons{border-style:solid;background-color:gray;cursor:pointer;padding-left:4px;padding-right:4px;padding-top:4px;}
    .lookLikeButtons2{border-style:solid;background-color:gray;cursor:pointer;padding-left:4px;padding-right:4px;}
</style>
         </head>
    <body>
        <%

        Boolean zoom=false;
        if(request.getParameter("zoom")!=null)
            {
            zoom=true;
            thisFolio.setZoom();
            }
        Line [] linepositions=thisFolio.getlines();
        //this should never happen with the current system but may be used in the future
            /*if(zoom)
            {
            for(int i=0;i<linepositions.length;i++)
                {
                linepositions[i].setZoom();
                }
            }*/
        int mean=thisFolio.getMeanHeight();
out.print("<div id=\"imagediv\" style=\"border-style:solid;left:800px;position:fixed;height:650px;width:450px;overflow:auto \"><img src=\""+thisFolio.getImageURLResize(thisFolio.getCollectionName(),thisFolio.getPageName(),archive)+"\"");
//if(zoom)out.print("z");
String parkerURL="http://parkerweb.stanford.edu/parker/actions/page.do?forward=page_turner&ms_no=";
String leadingZeroRegex="^0*";
parkerURL+=thisFolio.getCollectionName().replace("Manuscript","").replace(" ", "").replaceAll(leadingZeroRegex, "");
try{

parkerURL+="&pageNo="+thisFolio.getPageName().replace("_", "").replaceAll(leadingZeroRegex, "");
parkerURL+="&pageType="+thisFolio.getPageName().substring(thisFolio.getPageName().length()-1).replaceAll(leadingZeroRegex, "");
}
catch (ArrayIndexOutOfBoundsException e)
        {
    parkerURL="/images/"+thisFolio.getImageName()+".jpg";
    out.print(".jpg onclick=\"window.open('"+parkerURL+"','_new');\"/></div>");
    parkerURL="";
    out.print("<script>alert(\"Zoomed images wont fit on your screen, they are too wide!\");doZoom=false;</script>");
    }
if(parkerURL.compareTo("")!=0)
out.print(".jpg onclick=\"window.open('"+thisFolio.getArchiveLink()+"','_new');\"/></div>");
%>


<form style="width: 650px;position:relative;" action="?p=<%out.print(""+(pageno));%>" onsubmit="replaceChars()" method="POST">

<%
//out.print("<input type=\"hidden\" name=\"proj\" value=\""+thisProject.getProjectID()+"\"/>");
                for (int i=0;i<linepositions.length;i++)
                {
                    if(linepositions[i]!=null)
                        {
                %>
                <%out.print("<span style=\"position:absolute;background:white;z-index:1;\">"+(i+1)+"</span>");%><div style="width: <%out.print(""+(linepositions[i].getWidth()+mean*4));%>px;position:relative;overflow:hidden;height:<%out.print(""+(linepositions[i].getHeight()+(mean*.5)));%>px;"><img class="imgs" id="<%out.print("i"+i);%>" src="<%out.print(thisFolio.getImageURLResize(thisFolio.getCollectionName(),thisFolio.getPageName(),archive));%><%if(zoom)out.print("z");%>" style="position:relative;left:<%out.print("-"+linepositions[i].getLeft());%>px;top:<%out.print("-"+(linepositions[i].getTop()));%>px;\"/></div>
                <%out.print("<textarea class=\"theText\" onfocus=\"newFocus(this);scrollImage("+linepositions[i].getLeft()+","+(linepositions[i].getTop()-mean*2)+");zoomImage(i"+i+");\" onkeydown=\"return keyhandler(event);\" onkeyup=\"keyUpHandler(event);\" id=\""+(i+1)+"\" name=\""+(i+1)+"\" style=\"width:600px;\">");
                                Transcription thisLine2=new Transcription (pageno,i+1,thisProject.getProjectID(),false);
                                out.print (thisLine2.getText().replace("&amp;", "&"));
                                if(thisLine2.getComment()!=null&&thisLine2.getComment().length()>1 && thisLine2.getComment().compareTo("Enter any comments here.")!=0)
                                    {
                                    out.print("</textarea><br/>");
                                out.print("<textarea  onFocus=\"newFocus(this);selectAll(this);\" name=\"comment"+(i+1)+"\" id=\"comment"+(i+2)+"\" style=\"width:600px;\">");
                                    }
                                else
                                    {
                                out.print("</textarea><span class=\"lookLikeButtons2\" onclick=\"document.getElementById('comment"+(i+2)+"').style.display='block';this.style.display='none';\">+note</span><br/>");
                                out.print("<textarea style=\"display:none;width:600px;\" onFocus=\"newFocus(this);selectAll(this);\" name=\"comment"+(i+1)+"\" id=\"comment"+(i+2)+"\" style=\"width:600px;\">");
                                }
                                if(thisLine2.getComment()!=null&&thisLine2.getComment().length()>1)
                                    out.print (thisLine2.getComment());
                                else
                                    out.print("Enter any notes here.");
                                out.print("</textarea><br/>");
                                }

                }
        %>
        <div class="footer">

            <a href="index.jsp">TPEN home</a>&nbsp;
            <a href="correctPage.jsp?<%out.print("folio="+pageno);%>">Correct parsing</a>&nbsp;
        <input type="submit" value="save" name="save"/>
        <input type="submit"  name="preview" value="Save & preview"/>
 <!--       <span class="lookLikeButtons"  onclick="addchar('&para;');">&para;</span>
<span class="lookLikeButtons"  onclick="addchar('&#222;');">&#222;<sup>1</sup></span>
<span class="lookLikeButtons"  onclick="addchar('&#254;');">&#254;<sup>2</sup></span>
<span class="lookLikeButtons"  onclick="addchar('&#208;');">&#208;<sup>3</sup></span>
<span class="lookLikeButtons"  onclick="addchar('&#240;');">&#240;<sup>4</sup></span>
<span class="lookLikeButtons"  onclick="addchar('&#503;');">&#503;<sup>5</sup></span>
<span class="lookLikeButtons"  onclick="addchar('&#447;');">&#447;<sup>6</sup></span>

<span class="lookLikeButtons"  onclick="addchar('&#198;');">&#198;<sup>7</sup></span>
<span class="lookLikeButtons"  onclick="addchar('&#230;');">&#230;<sup>8</sup></span>
<span class="lookLikeButtons"  onclick="addchar('&#540;');">&#540;<sup>9</sup></span>
<span class="lookLikeButtons"  onclick="addchar('&#541;');">&#541;<sup>0</sup></span>
<span class="lookLikeButtons"  onclick="addchar('&f#913;');">&#913;</span>
<span class="lookLikeButtons"  onclick="addchar('&#945;');">&#945;</span>
<span class="lookLikeButtons"  onclick="addchar('&#937;');">&#937;</span>
<span class="lookLikeButtons"  onclick="addchar('&#969;');">&#969;</span>
<span class="lookLikeButtons" style="width:auto"  onclick="addchar('<i></i>');">Italic</span>
-->
<%
Hotkey ha;
    ha=new Hotkey(UID);
    out.print(ha.javascriptToAddButtons(UID));
    out.print(TagButton.getAllButtons(UID));
%>
<a href="buttons.jsp">change buttons</a>

<!--<span class="lookLikeButtons"  onclick="addchar('<colophon></colophon>');">colophon</span>
<span class="lookLikeButtons"  onclick="addchar('<rubric><rubric/>');">rubric</span>
<span class="lookLikeButtons"  onclick="addchar('<interlinear></interlinear>');">interlinear</span>
<span class="lookLikeButtons"  onclick="addchar('<marginal></marginal>');">marginal</span>
<button  onclick="document.location='correctPage.jsp?folio=<%out.print(""+pageno);%>'">Correct Parsing</button>-->
<%if(pageno>1 && false){%><a href="?p=<%out.print(""+(pageno-1));%>">Prev Page</a>
<a href="?p=<%out.print(""+(pageno+1));%>">Next Page</a><%}%>
<select onchange="navigateTo(this);">
    <option SELECTED>Change page</option>
    <%out.print(thisFolio.getFolioDropDown()); %>
</select>
<%out.print("<span style=\"float:right;color:white;\">Viewing: "+thisFolio.getArchiveShelfMark()+" "+thisFolio.getCollectionName()+" "+thisFolio.getPageName()+"&nbsp;</span>");%>
<br/>
<span style="text-align:center;color:white;margin-left:auto;"><%out.print(thisFolio.getCopyrightNotice(archive,thisFolio.getCollectionName(),thisFolio.getPageName()));%>&nbsp;</span>

        </div>
</form>
<br><br><br><br>
<script>
  var zoomURL='<%out.print(thisFolio.getImageURL(thisFolio.getCollectionName(),thisFolio.getPageName(),archive));%>';
  var normURL='<%out.print(thisFolio.getImageURLResize(thisFolio.getCollectionName(),thisFolio.getPageName(),archive));%>';
    </script>
    </body>
</html>