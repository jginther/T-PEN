/*
 * @author Jon Deering
Copyright 2011 Saint Louis University. Licensed under the Educational Community License, Version 2.0 (the "License"); you may not use
this file except in compliance with the License.

You may obtain a copy of the License at http://www.osedu.org/licenses/ECL-2.0

Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions
and limitations under the License.
 */
package textdisplay;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Stack;
import java.util.Vector;

/**
 *
 * Used for tasks requiring the management of all of the folios in a collection at once (for building listings of them,
 * showing thumbnails, counting them, and the like)
 * This class was created befor the manuscript class was created, but is still used in some cases for manageing multiple folios.
 */
public class FolioSet {

    private Vector<Integer> folios;
    private Vector<String> imageNames;

    public FolioSet() {
        folios = new Vector();
    }

    /**
     * Build a set of Folio objects for this manuscript
     * @param collection
     * @throws SQLException
     */
    public FolioSet(String collection) throws SQLException {
        folios = new Vector();
        imageNames = new Vector();
        String query = "select distinct(pageNumber), imageName from folios where collection=? order by pageNumber";
        String joinQuery = "select distinct(pageNumber), imageName  from folios join folioScribes on pageName=pageName where collection=? order by pageNumber";
        Connection j = null;
PreparedStatement stmt=null;
        try {
            j=DatabaseWrapper.getConnection();

            stmt = j.prepareStatement(query);
            stmt.setString(1, collection);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                try {
                    folios.add(rs.getInt(1));
                    imageNames.add(rs.getString(2));
                } catch (NullPointerException e) {
                }
            }
        } finally {
DatabaseWrapper.closeDBConnection(j);
DatabaseWrapper.closePreparedStatement(stmt);
        }
    }
    /**
     * Create a FolioSet using the archive and ms identifier
     * @param archive The hosting archive (ex. TPEN, CCL, CEEC)
     * @param collection The ms identifier
     * @throws SQLException
     */
    public FolioSet(String archive,String collection) throws SQLException {
    {
        folios=new Vector();
        Connection j=null;
PreparedStatement stmt=null;
        try{
        String toret="";
        j = DatabaseWrapper.getConnection();
        String qry="select * from folios where archive=? and collection=? ";
        stmt = j.prepareStatement(qry);
        stmt.setString(1, archive);
        stmt.setString(2, collection);

        ResultSet rs=stmt.executeQuery();
        Stack <String>pageNames=new Stack();
        Stack <Integer>pageNumbers=new Stack();
        while(rs.next())
        {
        pageNames.add(zeroPadLastNumberFourPlaces(rs.getString("pageName").replace("-000", "")));
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
 qry="select * from folios where pageNumber=?";
stmt=j.prepareStatement(qry);

for(int i=0;i<pageNumbersArray.length;i++)
{
    this.folios.add(pageNumbersArray[i]);
}
        }
        finally{
            if(j!=null)
DatabaseWrapper.closeDBConnection(j);
DatabaseWrapper.closePreparedStatement(stmt);
        }
    }
    }
    /**
     * Zero page the last number in a string to 4 places, for use in sorting images when the metadata is messy (non uniformnumber of digits)
     * @param name the name (of the image)
     * @return the reformatted name
     */
    public static String zeroPadLastNumberFourPlaces(String name)
    {
        for(int i=name.length()-1;i>=0;i--)
        {
            if(Character.isDigit(name.charAt(i)) )
            {
                //count the number of digits that preceed this one, if it is less than 3, padd with zeros
                int count=0;
                for(int j=i;j>=0;j--)
                {
                    if(Character.isDigit(name.charAt(j)))
                    {
                        count++;
                        if(j==0)
                        {
                            //padd
                            if(count==2)
                            name=name.substring(0, j)+"0"+name.substring(j);
                            if(count==1)
                            name=name.substring(0, j)+"00"+name.substring(j);
                            if(count==0)
                            name=name.substring(0, j)+"000"+name.substring(j);
                             return name;
                        }
                    }
                    else
                    {
                        if(count<3)
                        {
                            //padd
                            if(count==2)
                            name=name.substring(0, j+1)+"0"+name.substring(j+1);
                            if(count==1)
                            name=name.substring(0, j+1)+"00"+name.substring(j+1);
                            if(count==0)
                            name=name.substring(0, j+1)+"000"+name.substring(j+1);
                        }
                        return name;
                    }
                }

            }
        }
        return name;
    }

    /**
     * Builds an set of HTML checkboxes listing all images in the folioset
     * @return HTML for checkboxes, 1 per Folio
     * @throws SQLException
     */
    public String listAll() throws SQLException {
        String toret = "";
        for (int i = 0; i < folios.size(); i++) {
            String pageName = imageNames.elementAt(i);
            Folio f = new Folio(folios.get(i));
            //f.getImageURL(pageName, pageName, pageName);
            toret += "<input type=\"checkbox\" name=\"" + folios.get(i) + "\" id=\"" + folios.get(i) + "\" checked value=\"" + folios.get(i) + "\"/>" + imageNames.elementAt(i) + "<br>\n";
        }
        return toret;
    }
/**
 * Convert the FolioSet to an array
 * @return array of folios representing everything in this FolioSet
 * @throws SQLException
 */
    public Folio[] getAsArray() throws SQLException {
        Folio[] toret = new Folio[folios.size()];
        for (int i = 0; i < toret.length; i++) {
            toret[i] = new Folio(folios.get(i));
        }
        return toret;
    }
}
