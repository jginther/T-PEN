<%-- 
    Document   : compare
    Created on : Jan 6, 2012, 10:16:06 AM
    Author     : ijrikgnmd
--%>

<%@page import="java.util.Stack"%>
<%@page import="textdisplay.Transcription"%>
<%@page import="textdisplay.Folio"%>
<%@page import="textdisplay.Project"%>
<%@page import="user.User"%>
<%@page import="textdisplay.Manuscript"%>
<%int UID = 0;
    if (session.getAttribute("UID") == null) {
%><%@ include file="loginCheck.jsp" %><%                
    } else {
        UID = Integer.parseInt(session.getAttribute("UID").toString());
               }
        int folio=0;
        if(request.getParameter("folio")!=null)
            folio=Integer.parseInt(request.getParameter("folio"));
        else
            response.sendError(500);
        int projectID=0;
        if(request.getParameter("projectID")!=null)
        {
            projectID=Integer.parseInt(request.getParameter("projectID"));
        }
        Manuscript ms=new Manuscript(folio);
        User u= new User(UID);
        if(projectID==0)
                       {
        Project [] proj=u.getUserProjects();
        for(int i=0;i<proj.length;i++)
        {
            Project p=proj[i];
            Folio [] f=p.getFolios();
            for(int j=0;j<f.length;j++)
            {
            if(f[j].getFolioNumber()==folio)
            {
                //get the associated transcriptions
                projectID=p.getProjectID();
                
            }                   
            }
        }
               }
        Transcription [] t=Transcription.getProjectTranscriptions(projectID, folio);
        
%>
<!DOCTYPE html>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title>JSP Page</title>
    </head>
    <style>
        .text{width:50%;position:absolute;left:50%;}
        .line{overflow:hidden;}
        #image{width:50%;left:0px;}
    </style>
    <body><!--.line{border-style: solid;border-width:1px;}-->
        <div class="text">
            <%
            
            if(t.length>0)
                               {
                
                Stack<Integer> heights=new Stack();
            int left=t[0].getX();
            Boolean top=true;
            for(int i=0;i<t.length;i++)
               {
                    heights.push(t[i].getHeight());
                  if(left!=t[i].getX())
                  {
                   left=t[i].getX();
                   out.print("</div><div class=\"text\">");
                    top=true;
                  }
                  if(top)
                                           {top=false;
                out.print("<div class=\"line\" style=\""+"position:relative ;"+"height:"+(t[i].getHeight()+t[i].getY())+"px;width:"+t[i].getWidth()*2+"px;left:"+t[i].getX()*2+"px;\"><span style=\"position:absolute;bottom:0;\">"+t[i].getText()+"</span></div>");
                               }
                  else
                                           {
                      out.print("<div class=\"line\" style=\"position:relative ;height:"+t[i].getHeight()+"px;width:"+t[i].getWidth()*2+"px;left:"+t[i].getX()*2+"px;\"><span style=\"position:absolute;bottom:0;\">"+t[i].getText()+"</span></div>");
                  }
            }
            int []nums=new int[heights.size()];
            for(int i=0;i<nums.length;i++)
                nums[i]=heights.pop();
            
            out.print("<div style=\"display:hidden\"></div>");
            out.print("<script>var smallest="+(nums[nums.length/2])+";</script>");
            out.print("<style>.line{font-size:"+nums[nums.length/2]/2+"px;}</style>");
                       }
            %>
        </div>
        <div id="image">
            <img src="<% out.print(new Folio(folio).getImageURLResize()); %>"/>
        </div> 
    </body>
</html>
