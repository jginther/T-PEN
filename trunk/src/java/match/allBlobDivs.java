/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package match;

import detectimages.blob;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Vector;
import textdisplay.Manuscript;

/**
 *
 * @author jdeerin1
 */
public class allBlobDivs
{
    private String divs;
    /**@depricated*/
    public allBlobDivs(String pageIdentifier) throws FileNotFoundException, IOException, SQLException
    {
        String onMouseOver="";
        divs="";
        Vector <blob> allblobs=blob.getBlobs("/usr/data/"+pageIdentifier+".txt");
        matchLocater m=new matchLocater(Integer.parseInt(pageIdentifier),0,true);
        //Connection j=dbWrapper.getConnection();
        for(int i=0;i<allblobs.size();i++)
        {
        //if(allblobs.get(i).getSize()>=50)
            int count=m.getBlobMatchCount(i);
            if(count>0)
            divs+="<a title=\""+count+"\" href=\"?b="+i+"&p="+pageIdentifier+"\"><div id=\""+i+"\" style=\"z-index:1;background-color:blue;filter:alpha(opacity=50);opacity: 0.5;-moz-opacity:0.5;position:absolute;left:"+allblobs.get(i).getX()+"px;top:"+allblobs.get(i).getY()+"px;width:"+allblobs.get(i).getWidth()+"px;height:"+allblobs.get(i).getHeight()+"px; \" > "+"</div>"+"</a>";
        }
       // j.close();

    }
    public allBlobDivs(int folioNum) throws FileNotFoundException, IOException, SQLException
    {
        StringBuilder newBlobs = new StringBuilder("");
        Manuscript ms=new Manuscript(folioNum);
        Vector <blob> allblobs=blob.getBlobs(textdisplay.Folio.getRbTok("PalographyDataDir")+"/"+ms.getID()+"/"+folioNum+".txt");
        matchLocater m=new matchLocater(folioNum,0,true);
        //Connection j=dbWrapper.getConnection();
        for(int i=0;i<allblobs.size();i++)
        {
        //if(allblobs.get(i).getSize()>=50)
            int count=m.getBlobMatchCount(i);
            if(count>0){
                newBlobs.append("<a blobid='"+i+"' class='blob' title='"+
                    "' href='?b="+i+"&p="+folioNum+"' blobx='"+
                    allblobs.get(i).getX()+"' bloby='"+allblobs.get(i).getY()+
                    "' blobwidth='"+allblobs.get(i).getWidth()+"' blobheight='"+
                    allblobs.get(i).getHeight()+"'></a>");
            }
            divs=newBlobs.toString();
        }
       // j.close();

    }
    public String getDivs()
    {
        return divs;
    }

}
