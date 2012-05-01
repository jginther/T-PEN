/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package match;


import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.logging.Level;
import java.util.logging.Logger;
import textdisplay.DatabaseWrapper;
import textdisplay.Folio;
import textdisplay.Manuscript;

/**
 *
 * @author jdeerin1
 */
public class matchLocater
{
    String url;
    int matchCount;
    int [] charCounts=new int[5000];
    public matchLocater(String img1, int blob1) throws SQLException
    {
        matchCount=0;
        url="";
    String query="select img2,blob2 from blobmatches where img1=? and blob1=? order by img2";
    Connection j=DatabaseWrapper.getConnection();
    PreparedStatement ps;
    ps=j.prepareStatement(query);
    ps.setString(1, img1);
    ps.setInt(2, blob1);
    ResultSet rs=ps.executeQuery();
    String img2="";
    while(rs.next())
    {
        
        if(img2.compareTo(rs.getString(1))!=0)
        {
            img2=rs.getString(1);
            url+=("<br>"+img2+":");
        }
        int folioNum=Integer.parseInt(rs.getString(1).replace(".jpg", "").split("/")[rs.getString(1).replace(".jpg", "").split("/").length-1]);
        Folio f=new Folio(folioNum);
        if(f.isCached())
        {
        url+="<a href=\"paleo.jsp?highlightblob=true&page="+rs.getString(1).replace(".jpg", "")+"&orig=1&blob="+rs.getInt(2)+"\"><img src=\"characterImage?page="+rs.getString(1).replace(".jpg", "")+"&orig=1&blob="+rs.getInt(2)+"\"/></a>\n";
        matchCount++;
        }
 else
        {
        url+="not cached";
         }
    }
    j.close();
    }
    public matchLocater(int img1, int blob1, Boolean noSQL) throws FileNotFoundException, IOException, SQLException
    {
        url="";
        charCounts=new int[5000];
        for(int i=0;i<charCounts.length;i++)
            charCounts[i]=0;
            textdisplay.Folio fol=new textdisplay.Folio(img1);
            Manuscript ms=new Manuscript(img1);
        File f=new File(textdisplay.Folio.getRbTok("TempPaleoResults")+textdisplay.Folio.getRbTok("PalographyDataDir")+"/"+ms.getID()+"/"+img1+".txt .txt");
        BufferedReader b=new BufferedReader(new FileReader(f));
        String oldpage="";
        while(b.ready())
        {
            String buff=b.readLine();
            //System.out.print(buff+"\n");
            String parts1=buff.replace("\"", "").split("\\;")[0].split(":")[1];
            String parts2=buff.replace("\"", "").split("\\;")[1].split(":")[0];
            parts2=parts2.replace(".jpg", "").split("/")[parts2.replace(".jpg", "").split("/").length-1].replace(".txt", "");
            String parts3=buff.replace("\"", "").split("\\;")[1].split(":")[1];
            int blob=Integer.parseInt(parts1);
            if (blob1==0 ||blob1==blob)
            {
                if(oldpage.compareTo(parts2)!=0)
                {
                    Folio tmp=new Folio(Integer.parseInt(parts2));
                    url+=("<br>"+tmp.getPageName()+":");
                    oldpage=parts2;
                }
                //this is what they wanted
                 int folioNum=Integer.parseInt(parts2);
                 System.out.print("folio:"+folioNum+"\n");
        Folio folio=new Folio(folioNum);
        if(folio.isCached())
        {
            String folioNumber=parts2;
            
                url+="<a href=\"paleo.jsp?highlightblob=true&p="+folioNumber+"&orig=1&blob="+parts3+"\"><img src=\"characterImage?page="+folio.getFolioNumber()+"&blob="+parts3+"\"/></a>\n";
            }
        else
            {
                url+="not cached";
 }
            charCounts[blob]++;
            }
 

        }
    }
    public String getUrls()
    {
        return url;
    }
    public int getMatchCount()
    {
        return matchCount;
    }
    public int getBlobMatchCount(int blob)
    {
        return charCounts[blob];
    }
    public static int getBlobMatchCount(String img1, int blob1, Connection j) throws SQLException
    {
        String query="select count from charactercount where `img`=? and `blob`=?";
        //Connection j=DatabaseWrapper.getConnection();
    PreparedStatement ps;
    ps=j.prepareStatement(query);
    ps.setString(1, img1);
    ps.setInt(2, blob1);
    ResultSet rs=ps.executeQuery();
    if(rs.next())
    {
        int toret=rs.getInt(1);
        //j.close();
        return toret;

    }
    else
    {
       // j.close();
        return 0;
    }

    }
    public static String getBlobMatchCousins(String img1, int blob1) throws SQLException
    {
        int counter=0;
        String toret="";
        String query="select img2,blob2 from blobmatches where img1=? and blob1=? order by img2";
        String cousinQuery="select img2,blob2 from blobmatches where img1=? and blob1=? order by img2";
    Connection j=DatabaseWrapper.getConnection();
    PreparedStatement ps;
    ps=j.prepareStatement(query);
    PreparedStatement ps2=j.prepareStatement(cousinQuery);
    ps.setString(1, img1);
    ps.setInt(2, blob1);
    ResultSet rs=ps.executeQuery();
    String img2="";
    Hashtable <String,String> res=new Hashtable();
    while(rs.next())
    {
        res.put(rs.getString(1)+rs.getInt(2), "");
        ps2.setString(1,rs.getString(1));
        ps2.setInt(2,rs.getInt(2));
        ResultSet rs2=ps2.executeQuery();
        while(rs2.next())
        {
            res.put(rs2.getString(1)+rs2.getInt(2), rs2.getString(1)+rs2.getInt(2));
            counter++;
            if(counter>500)
                break;
        }
        if(counter>500)
                break;

    }
    Enumeration e=res.elements();


    int ctr=0;
    while(e.hasMoreElements())
    {
        e.nextElement();
        ctr++;

    }
    e=res.elements();
    String [] blobs=new String [ctr];
    ctr=0;
    while(e.hasMoreElements())
    {

        blobs[ctr]=(String) e.nextElement();
        ctr++;

    }
    Arrays.sort(blobs);
    String oldimg="";
    for(int i=0;i<blobs.length;i++)
    {
        try{
        String tmp=blobs[i];
        int num=Integer.parseInt(tmp.split(".jpg")[1]);
        tmp=tmp.split(".jpg")[0];
        if(tmp.compareTo(oldimg)!=0)
        {
            toret+="<br>"+tmp;
            oldimg=tmp;
        }
        toret+="<a href=\"paleo.jsp?page="+tmp+"&blob="+num+"&blobhighlight=true\"><img src=\"characterImage?page="+tmp+"&blob="+num+"\"/></a>\n";
        }catch(Exception ex)
        {
            toret+="<br>"+blobs[i];
        }
    }
    return toret;

    }
    public static String getDropdown(int msID) throws SQLException
    {
        String toret="";
        String query="select * from folios where msID=? and paleography!='0000-00-00 00:00:00' order by pageName";
        Connection j=DatabaseWrapper.getConnection();
        PreparedStatement ps=j.prepareStatement(query);
        ps.setInt(1, msID);
        ResultSet rs=ps.executeQuery();


        while(rs.next())
        {
            
            Folio f =new Folio((rs.getInt("pageNumber")));
            toret+="<option value=\"?p="+f.getFolioNumber()+"\">"+f.getPageName()+"</option>";
        }
        return toret;
    }
    public static void main(String [] args)
    {
        try {
            matchLocater m = new matchLocater(1, 100, true);
            for(int i=0;i<100;i++)
                System.out.print(""+i+": "+m.getBlobMatchCount(i)+"\n");
            System.out.print(m.url);
        } catch (SQLException ex) {
            Logger.getLogger(matchLocater.class.getName()).log(Level.SEVERE, null, ex);
        } catch (FileNotFoundException ex) {
            Logger.getLogger(matchLocater.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(matchLocater.class.getName()).log(Level.SEVERE, null, ex);
        }

    }
}
