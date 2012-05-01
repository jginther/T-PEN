/*
 * @author Jon Deering
Copyright 2011 Saint Louis University. Licensed under the Educational Community License, Version 2.0 (the "License"); you may not use
this file except in compliance with the License.

You may obtain a copy of the License at http://www.osedu.org/licenses/ECL-2.0

Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions
and limitations under the License.
 */
package utils;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Hashtable;
import java.util.Vector;
import textdisplay.DatabaseWrapper;
import textdisplay.Folio;
import textdisplay.Project;
import textdisplay.Transcription;

/**
 *
 * 
 *
 * Keeps track of which tags are open in a Project, and where they were opened.
 *
 *
 */
public class OpenTagTracker
{
    Project thisProject;
    public OpenTagTracker(textdisplay.Project p)
    {
    thisProject=p;
    }
    /**
     * Check whether there are tags on the preceeding pages that had tags opened then deleted, meaning that we 
     * should no longer show a close button for them.
     * @param folioNum
     * @return
     * @throws SQLException 
     */
    public Boolean checkValidity(int folioNum) throws SQLException
    {
        Boolean toret=false;
        //build a list of folios which come after this one in the Folio orderr for the Project.
        Folio [] projectFolios=thisProject.getFolios();
        for(int i=0;i<projectFolios.length;i++)
        {
            //this is the one, everything after this should be included in the list.
            if(projectFolios[i].getFolioNumber()==folioNum || folioNum == 0) //if foliNum==0 the requestor doesnt care to specify a start point, so start with the first Folio.
            {
            Connection j=null;
PreparedStatement ps=null;
            try{
                j=DatabaseWrapper.getConnection();
                ps=null;
                String query="select * from tagTracking where folio in (";
                int count=projectFolios.length-i-1;
                //build a query that says select * from tagTracking where Folio in (projectFolios[i+1].getFolioNumber(),projectFolios[i+2].getFolioNumber(), ...)
                if(count>0)
                {
                    query="select * from tagTracking where folio in (?";
                    while(count>0)
                    {
                        count--;
                        query+=",?";
                    }
                    query+=")";
                }
                count=projectFolios.length-i;
                ps=j.prepareStatement(query);
                while(count>0)
                    {

                    ps.setInt(count, projectFolios[count-1].getFolioNumber());
                     count--;
                }
                ResultSet rs=ps.executeQuery();
                while(rs.next())
                {
                    //check whetehr this is still valid
                    int thisID=rs.getInt("id");
                    String thisTagtag=rs.getString("tag");
                    int folio=rs.getInt("folio");
                    int line=rs.getInt("line");
                    Transcription t=new Transcription(line);
                    if(!t.getText().contains("<"+thisTagtag))
                    {
                        toret=true;
                        this.removeTag(thisID);
                    }

                    //toret.add(new String []{rs.getString("id"),rs.getString("tag"),""+rs.getInt("Folio"),""+rs.getInt("line")});
                }
                return toret;
            }
            finally{
DatabaseWrapper.closeDBConnection(j);
DatabaseWrapper.closePreparedStatement(ps);
            }
            }
        }
        return toret;
    }
    /**Retrieve all tags that occur after the specified Folio.
     * @param folionum the number of the Folio after which you want to begin including tags. Use a value of 0 to easily indicate all folios should be considered
     * Returns a Vector of String arrays, each of which contains 3 elements, the tag, the Folio it occurs on, and the line it occurs on.
     */
    public Vector<String[]> getTagsAfterFolio(int folioNum) throws SQLException
    {
        Vector<String[]> toret=new Vector();
        //build a list of folios which come after this one in the Folio order for the Project.
        Folio [] projectFolios=thisProject.getFolios();
        int count=0;
        for(int i=0;i<projectFolios.length;i++)
        {
            //this is the one, everything after this should be included in the list.
            if(projectFolios[i].getFolioNumber()==folioNum || folioNum == 0) //if foliNum==0 the requestor doesnt care to specify a start point, so start with the first Folio.
            {
                if(projectFolios[i].getFolioNumber()==folioNum)
                count++;
                break;
            }
            count++;
            
        }
            Connection j=null;
PreparedStatement ps=null;
            try{
                j=DatabaseWrapper.getConnection();
                ps=null;
                String query="select * from tagTracking where folio in (";
                
                //build a query that says select * from tagTracking where Folio in (projectFolios[i+1].getFolioNumber(),projectFolios[i+2].getFolioNumber(), ...)
                if(count>0)
                {
                    query="select * from tagTracking where folio in (?";
                    for(int ctr=1;ctr<count;ctr++)
                    {
                        
                        query+=",?";
                    }
                    query+=")";
                
                ps=j.prepareStatement(query);
               for(int ctr=0;ctr<count;ctr++)
                    {
                    ps.setInt(ctr+1, projectFolios[ctr].getFolioNumber());
                    }
                ResultSet rs=ps.executeQuery();
                while(rs.next())
                {
                    //append all items to the return list
                    toret.add(new String []{rs.getString("id"),rs.getString("tag"),""+rs.getInt("folio"),""+rs.getInt("line")});
                }
                }
                return toret;
            }
                    
            finally{
DatabaseWrapper.closeDBConnection(j);
DatabaseWrapper.closePreparedStatement(ps);
            }
            
        
        
    }
    /**Remove the tag entry that occurs earliest in the Project and matches this tag. Returns false if it couldnt find a matching tag to delete, true on success
    @PARAM tag String with the xml tag name, excluding parameters and brackets */
    public Boolean removeTag(int id) throws SQLException
    {
        Connection j=null;
PreparedStatement ps=null;
        try{
            j=DatabaseWrapper.getConnection();
            //find the first occurance of this tag by getting the Folio/line identifiers for all of them, then looking for which Folio occurs first in the Project
            
            //the limit 1 shouldnt be needed, and could be removed after testing.
            ps=j.prepareStatement("delete from tagTracking where id=?");
            ps.setInt(1, id);
            ps.execute();
        }
        finally
        {
DatabaseWrapper.closeDBConnection(j);
DatabaseWrapper.closePreparedStatement(ps);
        }
        return true;
    }
    /**Add a new tag to the list of currently open tags for this Project*/
    public int addTag(String tag, int folio, int line) throws SQLException
    {
        Connection j=null;
PreparedStatement ps=null;
        try{
            j=DatabaseWrapper.getConnection();

            ps=j.prepareStatement("insert into tagTracking(tag,folio,line, projectID) values(?,?,?,?)", PreparedStatement.RETURN_GENERATED_KEYS);
            ps.setString(1, tag);
            ps.setInt(2, folio);
            ps.setInt(3, line);
            ps.setInt(4, thisProject.getProjectID());
            ps.execute();
            ResultSet rs=ps.getGeneratedKeys();
            rs.next();
            return rs.getInt(1);

        }
        finally
        {
DatabaseWrapper.closeDBConnection(j);
DatabaseWrapper.closePreparedStatement(ps);
        }
    }

}
