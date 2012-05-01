/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package match;

import detectimages.blob;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import textdisplay.Manuscript;

/**
 *
 * @author jdeerin1
 */
public class blobGetter
{
 int h,y,x,w;
 public static blob getRawBlob(int folio, int blob) throws SQLException
    {

     Manuscript ms=new Manuscript(folio);
     blob b;
      String path=textdisplay.Folio.getRbTok("PalographyDataDir")+"/"+ms.getID()+"/";
     b=detectimages.blob.getBlob(path,folio+".txt", blob);
     return b;

 }
public blobGetter(String img,int blob) throws SQLException
{
     Connection j=null;
     int folioNum=Integer.parseInt(img.split("\\.")[0]);
     Manuscript ms=new Manuscript(folioNum);
    String path=textdisplay.Folio.getRbTok("PalographyDataDir")+"/"+ms.getID()+"/";
     try{
         j=textdisplay.DatabaseWrapper.getConnection();

         String selectQuery="select * from `blobs` where `img`=? and `blob`=?";
         PreparedStatement select=j.prepareStatement(selectQuery);
         if(img.contains("."))
            select.setString(1, img);
         else
             select.setString(1, img+".jpg");
         select.setInt(2, blob);
         ResultSet rs=select.executeQuery();
         if(rs.next())
         {
             y=rs.getInt("y");
             x=rs.getInt("x");
             h=rs.getInt("h");
             w=rs.getInt("w");
         }
         else
         {
             blob b=null;
             if(img.contains("."))
            b=detectimages.blob.getBlob(path,img, blob);
         else
                 b=detectimages.blob.getBlob(path,img+".txt", blob);

if(b==null)
{
    System.out.print("missing blob "+img+":"+blob+"\n");

}
             y=b.getY();
             x=b.getX();
             h=b.getHeight();
             w=b.getWidth();
         }
     }
     finally
     {
         j.close();
     }

}
public int getX()
{
        return x;
}
public int getY()
{
    return y;
}
public int getHeight()
{
    return h;
}
public int getWidth()
{
    return w;
}
}
