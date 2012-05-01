/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

import com.lowagie.text.DocumentException;
import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.SQLException;
import java.util.Enumeration;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.owasp.esapi.ESAPI;
import textdisplay.Manuscript;
import textdisplay.Metadata;
import textdisplay.Metadata;
import textdisplay.Project;
import textdisplay.TagFilter;

/**
 *
 * @author jim
 */
public class export extends HttpServlet {

    /** 
     * Processes requests for both HTTP <code>GET</code> and <code>POST</code> methods.
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     * @throws SQLException not likely to happen
     */
    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException, SQLException, FileNotFoundException, DocumentException {
        

       Enumeration paramNames=request.getParameterNames();
       String paramString="";
       while(paramNames.hasMoreElements())
       {
           String name=(String) paramNames.nextElement();
           paramString+=name+"="+request.getParameter(name)+"&";
       }
       System.out.print("Export request params:\n"+paramString+"\n");

            int projectID = 0;
            if (request.getParameter("projectID") != null) {
                projectID = Integer.parseInt(request.getParameter("projectID"));

            }
            Project p = new Project(projectID);
            
            String type = request.getParameter("type");
            String linebreak=request.getParameter("linebreak");

            Boolean lineBreak=true;
            if(linebreak.compareTo("newline")!=0)
            {
                lineBreak=false;
            }
            Boolean pageLabels=false;
            if(request.getParameter("pageLabels")!=null)
            {
                pageLabels=true;
            }
            //figure out if notes should be included in the document when it is built, and if so how they should be rendered. Not all rendering types my be supported by all
            //output types...
            Boolean includeNotes=false;
            TagFilter.noteStyles noteStyle=TagFilter.noteStyles.remove;
            if(request.getParameter("notes")!=null)
            {
                String val=request.getParameter("notes");
                if(val.compareTo("sideBySide")==0)
                {
                noteStyle=TagFilter.noteStyles.sidebyside;
                includeNotes=true;
                }
                if(val.compareTo("line")==0)
                {
                     noteStyle=TagFilter.noteStyles.inline;
                includeNotes=true;
                }
            
            if(val.compareTo("endnote")==0)
            {
                noteStyle=TagFilter.noteStyles.endnote;
                includeNotes=true;
            }
            if(val.compareTo("footnote")==0)
            {
                noteStyle=TagFilter.noteStyles.footnote;
                includeNotes=true;
            }
            if(val.compareTo("remove")==0)
            {
                includeNotes=false;
                noteStyle=TagFilter.noteStyles.remove;
            }
            }


            if (type.compareTo("pdf") == 0) {
                 ByteArrayOutputStream bos = null;
        try {
                response.setContentType("application/pdf");
                //response.setHeader( "Content-Disposition", "filename="+p.getProjectName()+".pdf");
                bos = new ByteArrayOutputStream();
                String metadataString="";
                if(request.getParameter("metadata")!=null)
                {
                    Metadata m=p.getMetadata();
                    metadataString+="Title:"+m.getTitle()+"\n";
                    metadataString+="Subtitle:"+m.getSubtitle() +"\n";
                    metadataString+="MS identifier:"+m.getMsIdentifier()+"\n";
                    metadataString+="MS settlement:"+m.getMsSettlement()+"\n";
                    metadataString+="MS Repository:"+m.getMsRepository()+"\n";
                    metadataString+="MS Collection:"+m.getMsCollection()+"\n";
                    metadataString+="MS id number:"+m.getMsIdNumber()+"\n";
                    metadataString+="Subject:"+m.getSubject()+"\n";
                    metadataString+="Author:"+m.getAuthor()+"\n";
                    metadataString+="Date:"+m.getDate()+"\n";
                    metadataString+="Location:"+m.getLocation()+"\n";
                    metadataString+="Language:"+m.getLanguage()+"\n";
                    metadataString+="Description:"+m.getDescription()+"\n";
                }
                
                String text=metadataString+"\n";
                if(request.getParameter("beginFolio")!=null && request.getParameter("endFolio")!=null)
                {
                    int endFolio=Integer.parseInt(request.getParameter("endFolio"));
                    int beginFolio=Integer.parseInt(request.getParameter("beginFolio"));
                    text+= Manuscript.getPartialDocument(p, noteStyle, lineBreak, pageLabels, beginFolio, endFolio);
                }
                else
                {
                text+= Manuscript.getFullDocument(p,noteStyle,lineBreak,pageLabels,false);
                }
                TagFilter f = new TagFilter(text);
                String tagVariablePrefix = "tag";
                String styleVariablePrefix = "style";
                int totalTags = 0;
                //count the number of tags passed in
                for (int i = 1; i < 50; i++) {
                    if (request.getParameter(tagVariablePrefix + i) != null) {
                        totalTags++;
                    } else {
                        break;
                    }
                }
                //Build an array tags and an array of styles, 1 per tag
                String[] tagArray = new String[totalTags];
                TagFilter.styles[] styleArray = new TagFilter.styles[totalTags];
                for (int i = 1; i <= totalTags; i++) {
                    tagArray[i-1] = request.getParameter(tagVariablePrefix + i);
                    String styleString = request.getParameter(styleVariablePrefix + i);
                    if(tagArray[i-1].compareTo("tpen_note")==0 && noteStyle==TagFilter.noteStyles.endnote)
                        styleArray[i-1]=TagFilter.styles.superscript;
                    //switch statements arent possible with strings, hence all of the if/else
                    if (styleString.compareTo("italic") == 0) {
                        styleArray[i-1] = TagFilter.styles.italic;
                    } else {
                        if (styleString.compareTo("bold") == 0) {
                            styleArray[i-1] = TagFilter.styles.bold;
                        } else if (styleString.compareTo("underlined") == 0) {
                            styleArray[i-1] = TagFilter.styles.underlined;
                        } else {
                            if (styleString.compareTo("none") == 0)
                            styleArray[i-1] = TagFilter.styles.none;
                            else
                                 if(styleString.compareTo("paragraph")==0)
                                {
                                    styleArray[i-1]=TagFilter.styles.paragraph;
                                }
                                else
                                styleArray[i-1] = TagFilter.styles.remove;
                        }
                    }
                    if(tagArray[i-1].compareTo("tpen_note")==0 && noteStyle==TagFilter.noteStyles.endnote)
                        styleArray[i-1]=TagFilter.styles.superscript;
                }
                //p.getMetadata()
                f.replaceTagsWithPDFEncoding(tagArray, styleArray, bos);
                response.getOutputStream().write(bos.toByteArray());
                return;
                } finally {
            if (bos != null) {
                bos.close();
            }
            }
            }
            if (type.compareTo("rtf") == 0) {
                
                response.setContentType("application/rtf");
                response.setHeader( "Content-Disposition", " filename=\""+p.getProjectName().replace(",", "").replace(".", "")+".rtf\"");
                PrintWriter out = response.getWriter();
                String metadataString="";
                if(request.getParameter("metadata")!=null)
                {
                    Metadata m=p.getMetadata();
                    metadataString+="Title:"+m.getTitle()+"\n";
                    metadataString+="Subtitle:"+m.getSubtitle() +"\n";
                    metadataString+="MS identifier:"+m.getMsIdentifier()+"\n";
                    metadataString+="MS settlement:"+m.getMsSettlement()+"\n";
                    metadataString+="MS Repository:"+m.getMsRepository()+"\n";
                    metadataString+="MS Collection:"+m.getMsCollection()+"\n";
                    metadataString+="MS id number:"+m.getMsIdNumber()+"\n";
                    metadataString+="Subject:"+m.getSubject()+"\n";
                    metadataString+="Author:"+m.getAuthor()+"\n";
                    metadataString+="Date:"+m.getDate()+"\n";
                    metadataString+="Location:"+m.getLocation()+"\n";
                    metadataString+="Language:"+m.getLanguage()+"\n";
                    metadataString+="Description:"+m.getDescription()+"\n";
                }
                String text=metadataString;
                if(request.getParameter("beginFolio")!=null && request.getParameter("endFolio")!=null)
                {
                    int endFolio=Integer.parseInt(request.getParameter("endFolio"));
                    int beginFolio=Integer.parseInt(request.getParameter("beginFolio"));
                    text+= Manuscript.getPartialDocument(p, noteStyle, lineBreak, pageLabels, beginFolio, endFolio);
                }
                else
                {
                text+= Manuscript.getFullDocument(p,noteStyle,lineBreak,pageLabels,false);
                }
                TagFilter f = new TagFilter(text);
                String tagVariablePrefix = "tag";
                String styleVariablePrefix = "style";
                int totalTags = 0;
                //count the number of tags passed in
                for (int i = 1; i < 50; i++) {
                    if (request.getParameter(tagVariablePrefix + i) != null) {
                        totalTags++;
                    } else {
                        break;
                    }
                }
                //Build an array tags and an array of styles, 1 per tag
                String[] tagArray = new String[totalTags];
                TagFilter.styles[] styleArray = new TagFilter.styles[totalTags];
                for (int i = 1; i <= totalTags; i++) {
                    tagArray[i-1] = request.getParameter(tagVariablePrefix + i);
                    String styleString = request.getParameter(styleVariablePrefix + i);
                    if (styleString.compareTo("italic") == 0) {
                        styleArray[i-1] = TagFilter.styles.italic;
                    } else {
                        if (styleString.compareTo("bold") == 0) {
                            styleArray[i-1] = TagFilter.styles.bold;
                        } else if (styleString.compareTo("underlined") == 0) {
                            styleArray[i-1] = TagFilter.styles.underlined;
                        } else {
                            if (styleString.compareTo("none") == 0)
                            styleArray[i-1] = TagFilter.styles.none;
                            else
                                if(styleString.compareTo("paragraph")==0)
                                {
                                    styleArray[i-1]=TagFilter.styles.paragraph;
                                }
                                else
                                styleArray[i-1] = TagFilter.styles.remove;
                        }
                    }
                    if(tagArray[i-1].compareTo("tpen_note")==0 && noteStyle==TagFilter.noteStyles.endnote)
                        styleArray[i-1]=TagFilter.styles.superscript;
                }
                f.replaceTagsWithRTFEncoding(tagArray, styleArray, out);
                return;
               
            }
            if (type.compareTo("xml") == 0) {
                
                response.setContentType("application/txt");
                response.setCharacterEncoding("UTF-8");
                response.setHeader( "Content-Disposition", "filename=\""+p.getProjectName()+".txt");
                String text="";
                
                if(request.getParameter("beginFolio")!=null && request.getParameter("endFolio")!=null)
                {
                    Boolean imageWrap=false;
                if(request.getParameter("imageWrap")!=null)
                    imageWrap=true;
                    int endFolio=Integer.parseInt(request.getParameter("endFolio"));
                    int beginFolio=Integer.parseInt(request.getParameter("beginFolio"));
                    text+= Manuscript.getPartialDocument(p, noteStyle, lineBreak, pageLabels, beginFolio, endFolio, imageWrap );
                }
                else
                {
                    Boolean imageWrap=false;
                    if(request.getParameter("imageWrap")!=null)
                    imageWrap=true;
                text+= Manuscript.getFullDocument(p,noteStyle,lineBreak,pageLabels,imageWrap);
                }
                if(request.getParameter("tei")!=null)
                {
                    //stickthe header after the <TEI> tag if it exists
                    if(text.contains ("<TEI>"))
                    {
                        String tmp=text;
                        text=text.substring(0,text.indexOf("<TEI>")+4)+p.getMetadata().getTEI()+tmp.substring(tmp.indexOf("<TEI>")+4);
                    }
                            //if there was no <TEI>, prepend the header. Wdont ahve to wrorry about breaking the document, it is already not valid tei
                    else
                    {
                        text=p.getMetadata().getTEI()+text;
                    }
                }
                TagFilter f = new TagFilter(text);
                String tagVariablePrefix = "tag";
                int totalTags = 0;
                for (int i = 1; i < 50; i++) {
                    if (request.getParameter(tagVariablePrefix + i) != null) {
                        totalTags++;
                    } else {
                        break;
                    }
                }
                //Note that xml stlying only cares about style=none and style=remove, other styles have no impact
                String styleVariablePrefix = "style";
                String[] tagArray = new String[totalTags];
                String[] tagArrayKeepContent = new String[totalTags];
                TagFilter.styles[] styleArray = new TagFilter.styles[totalTags];
                for (int i = 1; i <= totalTags; i++)
                {
                    
                    String styleString = request.getParameter(styleVariablePrefix + i);
                    if (styleString.compareTo("remove")==0)
                    {
                        //this tag and its contents need to be removed
                         tagArray[i-1] = request.getParameter(tagVariablePrefix + i);
                         styleArray[i-1] = TagFilter.styles.remove;
                        }
                    else
                        {
                         tagArray[i-1] = "";
                         styleArray[i-1] = TagFilter.styles.none;
                        }
                    
                    String removeTag=request.getParameter(styleVariablePrefix + i);
                    if(removeTag.compareTo("checked")==0)
                    {
                        //add this tag to the list 
                        tagArrayKeepContent[i-1]=request.getParameter(tagVariablePrefix + i);
                    }
                    }
              
                

                PrintWriter out = response.getWriter();
                String result=f.removeTagsAndContents(tagArray);
                f=new TagFilter(result);
                
                out.append(f.stripTags(tagArrayKeepContent));
            }



        
        


    }

    // <editor-fold defaultstate="collapsed" desc="HttpServlet methods. Click on the + sign on the left to edit the code.">
    /** 
     * Handles the HTTP <code>GET</code> method.
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException, FileNotFoundException {
        try {
            processRequest(request, response);
        } catch (SQLException ex) {
            Logger.getLogger(export.class.getName()).log(Level.SEVERE, null, ex);
        } catch (DocumentException ex) {
            Logger.getLogger(export.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /** 
     * Handles the HTTP <code>POST</code> method.
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException, FileNotFoundException {
        try {
            processRequest(request, response);
        } catch (SQLException ex) {
            Logger.getLogger(export.class.getName()).log(Level.SEVERE, null, ex);
        } catch (DocumentException ex) {
            Logger.getLogger(export.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /** 
     * Returns a short description of the servlet.
     * @return a String containing servlet description
     */
    @Override
    public String getServletInfo() {
        return "Short description";
    }// </editor-fold>
}
