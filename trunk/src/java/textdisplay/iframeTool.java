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

import java.net.MalformedURLException;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Vector;

/**
 * This class represents a tool like  a dictionary, abbreviation list, or common source (vulgate) that can be embedded in an iframe in TPEN
 * 
 */
public class iframeTool {
   String url;
   String name;
   int projectID;
   int id;
    /**
     * Constructor to add a new tool to a Project
     * @param url url to embed in the iframe
     * @param name label for the tool
     * @param p the Project to which the tool is being added
     */
    public iframeTool(String url, String name, Project p) throws SQLException
    {
        try{
            //dont add the record if the url is malformed
        URL testurl=new URL(url);
        }
        catch(MalformedURLException ex)
        {
            return;
        }
        String insertQuery="insert into iframeTools(url, name, projectID) values(?,?,?)";
        Connection j=null;
PreparedStatement ps=null;
        try{
            j=DatabaseWrapper.getConnection();
            ps=j.prepareStatement(insertQuery, PreparedStatement.RETURN_GENERATED_KEYS);
            ps.setString(1, url);
            ps.setString(2, name);
            ps.setInt(3, p.projectID);
        ps.execute();
        id=ps.getGeneratedKeys().getInt(1);
        //if that went well, set the class fields accordingly.
        this.url=url;
        this.name=name;
        this.projectID=p.projectID;
        }
    finally{
DatabaseWrapper.closeDBConnection(j);
DatabaseWrapper.closePreparedStatement(ps);
    }
}
    /**
     * Fetch a tool based on its unique ID. Used internally but made accessible for future use
     * @param id
     * @throws SQLException 
     */
    public iframeTool(int id) throws SQLException
    {
        String selectQuery="select * from iframeTools where id=?";
        
        Connection j=null;
PreparedStatement ps=null;
        try{
            j=DatabaseWrapper.getConnection();
            ps=j.prepareStatement(selectQuery);
            ps.setInt(1, id);
        ResultSet rs=ps.executeQuery();
        if(rs.next())
        {
            this.name=rs.getString("name");
            this.url=rs.getString("url");
            this.projectID=rs.getInt("projectID");
            this.id=id;
        }
        }
        finally{
DatabaseWrapper.closeDBConnection(j);
DatabaseWrapper.closePreparedStatement(ps);
        }
    }
    /**
     * get all tools associated with a Project
     * @param p the Project
     * @return array of iframeTools for the Project
     * @throws SQLException 
     */
    public iframeTool [] getAllProjectTools(Project p) throws SQLException
    {
        String query="select id from iframeTools where projectID=?";
        Connection j=null;
PreparedStatement ps=null;
        try{
            j=DatabaseWrapper.getConnection();
            ps=j.prepareStatement(query);
            ps.setInt(1,projectID);
            ResultSet rs=ps.executeQuery();
            Vector<Integer> uniqueIDs=new Vector();
            while(rs.next())
            {
                uniqueIDs.add(rs.getInt(1));
            }
            iframeTool [] toret=new iframeTool[uniqueIDs.size()];
            for(int i=0;i<toret.length;i++)
                toret[i]=new iframeTool(uniqueIDs.get(i));
            return toret;
        }
        finally{
DatabaseWrapper.closeDBConnection(j);
DatabaseWrapper.closePreparedStatement(ps);
        }
    }
    /**
     * Delete this tool from its Project
     * @throws SQLException 
     */
    public void delete() throws SQLException
    {
        if(this.id>0)
        {
            String delQuery="delete from iframeTools where id=?";
            Connection j=null;
PreparedStatement ps=null;
            try{
                j=DatabaseWrapper.getConnection();
                ps=j.prepareStatement(delQuery);
                ps.setInt(1, id);
                ps.execute();
            }
            finally
            {
DatabaseWrapper.closeDBConnection(j);
DatabaseWrapper.closePreparedStatement(ps);
            }
        }
    }
}
