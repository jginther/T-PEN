
/**@deprecated */
package textdisplay;

import com.lowagie.text.Chunk;
import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.Paragraph;
import com.lowagie.text.pdf.BaseFont;
import com.lowagie.text.pdf.PdfWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Stack;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.owasp.esapi.ESAPI;


/**Handles the display/rendering of a page of transcribed text.*/
public class transcriptionPage
{
    private String pageName;
    private String content;
    private String comments;
    private String contentWithNotes="";
    private int uid;
    /**Get the content of the page*/
    public String getContent()
    {
        return content;
    }
    /**Get the coments for this page*/
    public String getComments()
    {
        return comments;
    }
    
    /**Write the page of Transcription as PDF content to the provided output stream, should be used by a servlet*/
    public void PDFify(OutputStream os, Boolean notes) throws FileNotFoundException, DocumentException, IOException
    {
        BaseFont bf = BaseFont.createFont("/usr/share/fonts/truetype/msttcorefonts/arial.ttf", BaseFont.IDENTITY_H, BaseFont.EMBEDDED);
        Document doc=new Document();
        PdfWriter p= PdfWriter.getInstance(doc, os);
        doc.open();
        Paragraph para=new Paragraph();
        para.setLeading(20);
        String pdfContent=this.getContent();
        pdfContent=ESAPI.encoder().decodeForHTML(content);
        if(!notes)
        {
        para=new Paragraph("/"+this.pageName+"/\n"+pdfContent.replace("\t","").replace("<br/>", "\n"),new Font(bf));
        doc.add(para);
        }else
        {

            {
            String toWrite=ESAPI.encoder().decodeForHTML(this.contentWithNotes);
            String [] lines=toWrite.split("\n--    ");
            para=new Paragraph("",new Font(bf));
             Chunk c=new Chunk("/"+this.pageName+"/\n",new Font(bf));
             para.add(c);
            for(int ctr=0;ctr<lines.length;ctr++)
            {
                if(ctr%2==0)
                {
                    c=new Chunk(lines[ctr]+"\n",new Font(bf,12));

                para.add(c);
                }
                else
                {
                    if(lines[ctr].length()>0)
                    {
                    c=new Chunk("    "+lines[ctr].replace("\n", "\n     ")+"\n",new Font(bf,10,Font.ITALIC));
                    para.add(c);
                    }
                }
            }

            doc.add(para);
        }
           
        }
        doc.newPage();
        doc.close();
        p.close();
    }

       /**Build a pdf containing all of the pages of this manuscript in order. There wont be any blanks for pages that havent been transcribed
     they just get skipped*/
    public void PDFifyMultiplePages(OutputStream os,String archive, String collection, Boolean notes, Boolean project) throws FileNotFoundException, DocumentException, IOException, SQLException
    {

         BaseFont bf = BaseFont.createFont("/usr/share/fonts/truetype/msttcorefonts/arial.ttf", BaseFont.IDENTITY_H, BaseFont.EMBEDDED);
        Document doc=new Document();
        PdfWriter p= PdfWriter.getInstance(doc, os);
        doc.open();
        Paragraph intropara=new Paragraph(textdisplay.Archive.getShelfMark(archive)+" "+collection+"\n");
        intropara.setAlignment(Element.ALIGN_CENTER);
        doc.add(intropara);
        Connection j=null;
        try{
        String toret="";
        j = DatabaseWrapper.getConnection();
        Folio [] f=new Project(uid).getFolios();
 int [] pageNumbersArray=new int [f.length];
 String [] paddedPageNameArray=new String[f.length];
 for(int i=0;i<paddedPageNameArray.length;i++)
 {
     paddedPageNameArray[i]=Folio.zeroPadLastNumberFourPlaces(f[i].getPageName());
     pageNumbersArray[i]=f[i].folioNumber;
 }

for(int i=0;i<pageNumbersArray.length;i++)
{

        Paragraph para=new Paragraph();
        para.setLeading(20);
        transcriptionPage nextPage=new transcriptionPage(pageNumbersArray[i],this.uid, true);
        String toWrite=nextPage.getContent();
        toWrite=ESAPI.encoder().decodeForHTML(toWrite);
        if(!notes)
        {
        para=new Paragraph("/"+nextPage.pageName+"/\n"+toWrite.replace("\t","").replace("<br/>", "\n"),new Font(bf));
        doc.add(para);
        }else
        {
            toWrite=ESAPI.encoder().decodeForHTML(nextPage.contentWithNotes);
            String [] lines=toWrite.split("\n--    ");
            para=new Paragraph("",new Font(bf));
            Chunk c=new Chunk("/"+nextPage.pageName+"/\n",new Font(bf));
            para.add(c);
            for(int ctr=0;ctr<lines.length;ctr++)
            {
                if(ctr%2==0)
                {
                     c=new Chunk(lines[ctr]+"\n",new Font(bf,12));

                para.add(c);
                }
                else
                {
                    if (lines[ctr].length() > 0)
                        {
                         c = new Chunk("    "+lines[ctr].replace("\n", "\n     ") + "\n", new Font(bf, 10, Font.ITALIC));
                        para.add(c);
                        }
                }
            }

            doc.add(para);
        }
        doc.newPage();

}
        doc.close();
        p.close();

    }
        finally{
        if(j!=null)
                j.close();
            else
                    System.err.print("Attempt to close DB connection failed, connection was null"+this.getClass().getName()+"\n");
        }
    
    }

    /**Build a pdf containing all of the pages of this manuscript in order. There wont be any blanks for pages that havent been transcribed
     they just get skipped*/
    public void PDFifyMultiplePages(OutputStream os,String archive, String collection, Boolean notes) throws FileNotFoundException, DocumentException, IOException, SQLException
    {
        
         BaseFont bf = BaseFont.createFont("/usr/share/fonts/truetype/msttcorefonts/arial.ttf", BaseFont.IDENTITY_H, BaseFont.EMBEDDED);
        Document doc=new Document();
        PdfWriter p= PdfWriter.getInstance(doc, os);
        doc.open();
        Paragraph intropara=new Paragraph(textdisplay.Archive.getShelfMark(archive)+" "+collection+"\n");
        intropara.setAlignment(Element.ALIGN_CENTER);
        doc.add(intropara);



    {
        Connection j=null;
PreparedStatement stmt=null;
        try{
        String toret="";
        j = DatabaseWrapper.getConnection();
        String qry="select distinct (folio),pageName,pageNumber from folios inner join transcription on pageNumber=folio where archive=? and collection=? ";
        stmt = j.prepareStatement(qry);
        stmt.setString(1, archive);
        stmt.setString(2, collection);

        ResultSet rs=stmt.executeQuery();
        Stack <String>pageNames=new Stack();
        Stack <Integer>pageNumbers=new Stack();
        while(rs.next())
        {
        //toret+="<option value=\""+rs.getInt("pageNumber")+"\">"+textdisplay.Archive.getShelfMark(rs.getString("Archive"))+" "+rs.getString("collection")+" "+rs.getString("pageName")+"</option>";
            //pageNames.add(rs.getString("pageName"));
        pageNames.add(Folio.zeroPadLastNumberFourPlaces(rs.getString("pageName").replace("-000", "")));
        pageNumbers.add(rs.getInt("pageNumber"));
        }
 int [] pageNumbersArray=new int [pageNumbers.size()];
 String [] paddedPageNameArray=new String[pageNames.size()];

 for(int i=0;i<paddedPageNameArray.length;i++)
 {
     paddedPageNameArray[i]=pageNames.elementAt(i);
     pageNumbersArray[i]=pageNumbers.get(i);
 }

 for(int i=0;i<paddedPageNameArray.length;i++)
     for(int k=0;k<paddedPageNameArray.length-1;k++)
     {
         if(paddedPageNameArray[k].compareTo(paddedPageNameArray[k+1])>0)
         {
             String tmpStr=paddedPageNameArray[k];
             paddedPageNameArray[k]=paddedPageNameArray[k+1];
             paddedPageNameArray[k+1]=tmpStr;
             int tmpInt=pageNumbersArray[k];
             pageNumbersArray[k]=pageNumbersArray[k+1];
             pageNumbersArray[k+1]=tmpInt;

         }
     }
for(int i=0;i<pageNumbersArray.length;i++)
{
   
        Paragraph para=new Paragraph();
        para.setLeading(20);
        transcriptionPage nextPage=new transcriptionPage(pageNumbersArray[i],uid);
        String toWrite=nextPage.getContent();
        toWrite=ESAPI.encoder().decodeForHTML(toWrite);
        if(!notes)
        {
        para=new Paragraph("/"+nextPage.pageName+"/\n"+toWrite.replace("\t","").replace("<br/>", "\n"),new Font(bf));
        doc.add(para);
        }else
        {
            toWrite=ESAPI.encoder().decodeForHTML(nextPage.contentWithNotes);
            String [] lines=toWrite.split("\n--    ");
            para=new Paragraph("",new Font(bf));
            Chunk c=new Chunk("/"+nextPage.pageName+"/\n",new Font(bf));
            para.add(c);
            for(int ctr=0;ctr<lines.length;ctr++)
            {
                if(ctr%2==0)
                {
                     c=new Chunk(lines[ctr]+"\n",new Font(bf,12));

                para.add(c);
                }
                else
                {
                    if (lines[ctr].length() > 0)
                        {
                         c = new Chunk("    "+lines[ctr].replace("\n", "\n     ") + "\n", new Font(bf, 10, Font.ITALIC));
                        para.add(c);
                        }
                }
            }
            
            doc.add(para);
        }
        doc.newPage();
       
}
        doc.close();
        p.close();

    }
        finally{
DatabaseWrapper.closeDBConnection(j);
DatabaseWrapper.closePreparedStatement(stmt);
        }
    }
    }

    /**Get a contiguous portion of the transcribed text for insertion into a comment, with linebreaks after each ms line*/
    public String getChosenPortion(int beginFolio, int beginLine, int endFolio, int endLine, int uid) throws SQLException
    {

        assert(beginFolio<=endFolio);
        if(beginFolio==endFolio)
            assert(beginLine<=endLine);
        Connection j=null;
        PreparedStatement queryFirstFolio=null;
                PreparedStatement queryLastFolio=null;
                        PreparedStatement queryMiddleFolio=null;
                                PreparedStatement queryOnlyFolio=null;
        try{
        j= DatabaseWrapper.getConnection();
        String toret="";
        queryFirstFolio=j.prepareStatement("select text from transcription where folio=? and line>=? and creator=? order by line");
        queryLastFolio=j.prepareStatement("select text from transcription where folio=? and line<=? and creator=? order by line");
        queryMiddleFolio=j.prepareStatement("select text from transcription where folio=? and creator=? order by line");
        queryOnlyFolio=j.prepareStatement("select text from transcription where folio=? and line<=? and line >=? and creator=? order by line");
        for(int i=beginFolio;i<=endFolio;i++)
        {
            String query;
            ResultSet rs=null;
            //if there is more than one Folio and this is the first Folio, only get lines starting from beginLine
            if(i==beginFolio && endFolio!=i)
            {
                query="select text from transcription where folio=? and line>=? and creator=? order by line";
                queryFirstFolio.setInt(1, i);
                queryFirstFolio.setInt(2, beginLine);
                queryFirstFolio.setInt(3, uid);
                rs=queryFirstFolio.executeQuery();
            }
            //If there is more than one Folio and this is the last Folio, only get lines up to endLine
            if(endFolio==i && beginFolio!=i)
            {
                query="select text from transcription where folio=? and line<=? and creator=? order by line";
                queryLastFolio.setInt(1, i);
                queryLastFolio.setInt(2, endLine);
                queryLastFolio.setInt(3, uid);
                rs=queryLastFolio.executeQuery();
            }
            //If this is a 1 Folio request, only get the lines from this Folio between begin and end
            if(endFolio==i && beginFolio==i)
            {
                query="select text from transcription where folio=? and line<=? and line >=? and creator=? order by line";
                queryOnlyFolio.setInt(1, i);
                queryOnlyFolio.setInt(2, endLine);
                queryOnlyFolio.setInt(3, beginLine);
                queryOnlyFolio.setInt(4, uid);
                rs=queryOnlyFolio.executeQuery();
            }
            if(endFolio!=i && beginFolio!=i)
            {
                query="select text from transcription where folio=? and creator=? order by line";
                queryMiddleFolio.setInt(1, i);
                queryMiddleFolio.setInt(2, uid);
                rs=queryMiddleFolio.executeQuery();
            }
            while(rs.next())
            {
                toret+=rs.getString("text")+"<br/>";
            }
        }
        return toret;
        }
        finally
        {
            DatabaseWrapper.closeDBConnection(j);
            DatabaseWrapper.closePreparedStatement(queryLastFolio);
            DatabaseWrapper.closePreparedStatement(queryFirstFolio);
            DatabaseWrapper.closePreparedStatement(queryMiddleFolio);
            DatabaseWrapper.closePreparedStatement(queryOnlyFolio);
        }


    }
    /**Get a single page*/
    public transcriptionPage (int folio, int uid) throws SQLException
    {
        Connection j=null;
PreparedStatement stmt=null;
        try{
        this.uid=uid;
            j = DatabaseWrapper.getConnection();
            stmt = j.prepareStatement("Select line,comment from transcription where folio=? and creator=? order by line");
            stmt.setInt(1, folio);
            stmt.setInt(2, uid);
            ResultSet rs;
            rs=stmt.executeQuery();
            content="";
            comments="";
            int ctr=1;
            while(rs.next())
            {
               
               
                Transcription thisline=new Transcription(rs.getInt("id"));
               // String linetext=ESAPI.encoder().encodeForHTML(thisline.getText().replaceAll("<lb?+>","<br/>").replace("<?xml version=\"1.0\" encoding=\"UTF-8\"?>",""));
                 String linetext= ESAPI.encoder().encodeForHTML(ESAPI.encoder().decodeForHTML(thisline.getText().replaceAll("<lb?+>","<br/>").replace("<?xml version=\"1.0\" encoding=\"UTF-8\"?>","")));
                //linetext=linetext.replace("¶", "<br/>&nbsp;&nbsp;&nbsp;&nbsp;");
                content+=""+ctr+" "+linetext+"<br/>";
                contentWithNotes+=+ctr+" "+linetext+"\n--    "+rs.getString("comment")+"\n--    ";
                if(rs.getString("comment").compareTo("Enter any comments here.")!=0 && rs.getString("comment").compareTo("Enter any commments here.")!=0)
                    comments+="<div style=\"width:100%;height:1.2em;overflow:hidden;\">"+ctr+" "+rs.getString("comment")+"</div>";
                else
                    comments+="<div style=\"width:100%;height:1.2em;overflow:hidden;\">"+ctr+" [No Comment]</div>";
                ctr++;
            }
            Folio f=new Folio(folio);
            this.pageName=f.getPageName();
    }
        finally
        {
DatabaseWrapper.closeDBConnection(j);
DatabaseWrapper.closePreparedStatement(stmt);
    }
    }

    /**Get a single page*/
    public transcriptionPage (int folio, int projectID, Boolean isUser) throws SQLException
    {
        Connection j=null;
PreparedStatement stmt=null;
        try{
        this.uid=projectID;
            j = DatabaseWrapper.getConnection();
            stmt = j.prepareStatement("Select line,comment from transcription where folio=? and projectID=? order by line");
            stmt.setInt(1, folio);
            stmt.setInt(2, projectID);
            ResultSet rs;
            rs=stmt.executeQuery();
            content="";
            comments="";
            int ctr=1;
            while(rs.next())
            {


                Transcription thisline=new Transcription(rs.getInt("line"));
               // String linetext=ESAPI.encoder().encodeForHTML(thisline.getText().replaceAll("<lb?+>","<br/>").replace("<?xml version=\"1.0\" encoding=\"UTF-8\"?>",""));
                 String linetext= ESAPI.encoder().encodeForHTML(ESAPI.encoder().decodeForHTML(thisline.getText().replaceAll("<lb?+>","<br/>").replace("<?xml version=\"1.0\" encoding=\"UTF-8\"?>","")));
                //linetext=linetext.replace("¶", "<br/>&nbsp;&nbsp;&nbsp;&nbsp;");
                content+=""+ctr+" "+linetext+"<br/>";
                this.contentWithNotes+=""+ctr+" "+linetext+"\n--    "+rs.getString("comment")+"\n--    ";
                if(rs.getString("comment").compareTo("Enter any comments here.")!=0 && rs.getString("comment").compareTo("Enter any commments here.")!=0)
                    comments+="<div style=\"width:100%;height:1.2em;overflow:hidden;\">"+ctr+" "+rs.getString("comment")+"</div>";
                else
                    comments+="<div style=\"width:100%;height:1.2em;overflow:hidden;\">"+ctr+" [No Comment]</div>";
                ctr++;
            }
Folio f=new Folio(folio);
            this.pageName=f.getPageName();
    }
         
        finally
        {
DatabaseWrapper.closeDBConnection(j);
DatabaseWrapper.closePreparedStatement(stmt);
        }
    }

    public transcriptionPage(int folio, int uid, Folio imageData) throws SQLException
    {
        Connection j=null;
PreparedStatement stmt=null;
        try{
        this.uid=uid;
            j = DatabaseWrapper.getConnection();
            stmt = j.prepareStatement("Select line,comment from transcription where folio=? and creator=? order by line");
            stmt.setInt(1, folio);
            stmt.setInt(2, uid);
            ResultSet rs;
            rs=stmt.executeQuery();
            content="";
            comments="";
            int ctr=1;
            String divs=imageData.getLinesAsDivs();
            String [] divArray=divs.split("<div");

            while(rs.next())
            {
                Transcription thisline=new Transcription(rs.getInt("line"));
                String linetext=thisline.getText().replaceAll("<lb?+>","<br/>").replace("<?xml version=\"1.0\" encoding=\"UTF-8\"?>","");
                //linetext=linetext.replace("¶", "<br/>&nbsp;&nbsp;&nbsp;&nbsp;");

                if(ctr<divArray.length)
                {
                    content+="<div"+divArray[ctr].replace("</div>", "").replace("background-color:aqua;","").replace("background-color:red;","");
                    content+=""+ctr+" "+linetext+"<br/></div>";

                }
                else
                {
                    content+=""+ctr+" "+linetext+"<br/>";
                }
                if(rs.getString("comment").compareTo("Enter any comments here.")!=0 && rs.getString("comment").compareTo("Enter any commments here.")!=0)
                    comments+="<div style=\"width:100%;height:1.2em;overflow:hidden;\">"+ctr+" "+rs.getString("comment")+"</div>";
                else
                    comments+="<div style=\"width:100%;height:1.2em;overflow:hidden;\">"+ctr+" [No Comment]</div>";
                ctr++;
            }
        }
        finally
        {
DatabaseWrapper.closeDBConnection(j);
DatabaseWrapper.closePreparedStatement(stmt);
        }

    }
    public transcriptionPage(int folio) throws SQLException
    {
        Connection j=null;
PreparedStatement stmt=null;
PreparedStatement stmt2=null;
        try{
        j = DatabaseWrapper.getConnection();
            stmt = j.prepareStatement("Select line,comment from transcription where folio=? and creator=? order by line");
            stmt2=j.prepareStatement("Select creator from transcription where folio=? limit 1");
            ResultSet rs;
            stmt2.setInt(1, folio);
            rs=stmt2.executeQuery();
            int uid=0;
            if(rs.next())
            {
            
            uid=rs.getInt(1);
            this.uid=uid;
            stmt.setInt(1, folio);
            stmt.setInt(2, uid);

            rs=stmt.executeQuery();
            content="";
            comments="";
            while(rs.next())
            {
                Transcription thisline=new Transcription(rs.getInt("line"));
                String linetext=thisline.getText().replaceAll("<lb?+>","<br/>").replace("<?xml version=\"1.0\" encoding=\"UTF-8\"?>","");
                //linetext=linetext.replace("¶", "<br/>&nbsp;&nbsp;&nbsp;&nbsp;");
                content+=linetext+"<br/>";
                if(rs.getString("comment").compareTo("Enter any comments here.")!=0 && rs.getString("comment").compareTo("Enter any commments here.")!=0)
                comments+="<div style=\"width:100%;height:1.2em;overflow:hidden;\">"+rs.getString("comment")+"</div>";
            }
            }
            else
            {
                content="";
                this.comments="";
            }
    }
    finally
        {
DatabaseWrapper.closeDBConnection(j);
DatabaseWrapper.closePreparedStatement(stmt);
DatabaseWrapper.closePreparedStatement(stmt2);
        }
    }
    /**Get the name of the creator of this bit of Transcription*/
    public String getUID()
    {
        try {
            user.User creator = new user.User(uid);
            return creator.getLname()+","+creator.getFname();
        } catch (SQLException ex) {
            Logger.getLogger(transcriptionPage.class.getName()).log(Level.SEVERE, null, ex);
        }
        return "unknown author";
    }
}
