
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
import java.util.logging.Level;
import java.util.logging.Logger;
import org.owasp.esapi.ESAPI;
import user.User;


/**This class provides a mechanism for standardizing certain aspects of a set of projects as well as providing a mechanism for sending notification
 to a chosen user when a particular project is complete and ready for export. */
public class PartnerProject {
private String url;
private int controllingUser;
private int templateProject;
private int id;
private String description;
private String name;
/**Fetch a partner project by its unique ID. That id is stored in the project object for all associated projects.*/
public PartnerProject(int id) throws SQLException
{
    this.id=id;
    String query="select * from partnerProject where id=?";
    Connection j=null;
PreparedStatement ps=null;
    try{
        j=DatabaseWrapper.getConnection();
        ps=j.prepareStatement(query);
        ps.setInt(1, id);
        ResultSet rs=ps.executeQuery();
        if(rs.next())
        {
            this.name=rs.getString("name");
            this.description =rs.getString("description");
            this.controllingUser=rs.getInt("user");
            this.url=rs.getString("url");
            this.templateProject=rs.getInt("projectID");
        }
        else
        {
            this.id=-1;
        }
    }
    finally
    {
DatabaseWrapper.closeDBConnection(j);
DatabaseWrapper.closePreparedStatement(ps);
    }
}
/**Create a new pipeline based on a template project. The group leader of that project will recieve notifications and be added to projects which associate with
 this pipeline.*/
public PartnerProject(String name, String description, String url, int controllingUser, int projectID) throws SQLException
{
    String query="insert into partnerProject(name,url,user,description,projectID) values(?,?,?,?,?)";
    Connection j=null;
PreparedStatement ps=null;
    try{
        j=DatabaseWrapper.getConnection();
        ps=j.prepareStatement(query, PreparedStatement.RETURN_GENERATED_KEYS);
        ps.setString(1, name);
        ps.setString(2, url);
        ps.setInt(3, controllingUser);
        ps.setString(4, description);
        ps.setInt(5, projectID);
        ps.execute();
        ResultSet rs=ps.getGeneratedKeys();
        rs.next();
        this.id=rs.getInt(1);
        this.controllingUser=controllingUser;
        this.name=name;
        this.description=description;
        this.url=url;
        this.templateProject=projectID;

    }
    finally{
DatabaseWrapper.closeDBConnection(j);
DatabaseWrapper.closePreparedStatement(ps);
    }
}

    public void setControllingUser(int controllingUser) throws SQLException {
        Connection j=null;
PreparedStatement ps=null;
        try{
            String query="update partnerProject set user=? where id=?";
            j=DatabaseWrapper.getConnection();
            ps=j.prepareStatement(query);
            ps.setInt(1, controllingUser);
            ps.setInt(2, this.id);
            ps.execute();
        }
        finally{
DatabaseWrapper.closeDBConnection(j);
DatabaseWrapper.closePreparedStatement(ps);
        }
        this.controllingUser = controllingUser;
    }

    public void setDescription(String description) throws SQLException {
         Connection j=null;
PreparedStatement ps=null;
        try{
            String query="update partnerProject set description=? where id=?";
            j=DatabaseWrapper.getConnection();
            ps=j.prepareStatement(query);
            ps.setString(1, description);
            ps.setInt(2, this.id);
            ps.execute();
        }
        finally{
DatabaseWrapper.closeDBConnection(j);
DatabaseWrapper.closePreparedStatement(ps);
        }
        this.description = description;
    }

    public void setName(String name) throws SQLException {
        Connection j=null;
PreparedStatement ps=null;
        try{
            String query="update partnerProject set name=? where id=?";
            j=DatabaseWrapper.getConnection();
            ps=j.prepareStatement(query);
            ps.setString(1, name);
            ps.setInt(2, this.id);
            ps.execute();
        }
        finally{
DatabaseWrapper.closeDBConnection(j);
DatabaseWrapper.closePreparedStatement(ps);
        }
        this.name = name;
    }

    public void setTemplateProject(int templateProject) throws SQLException {
        Connection j=null;
PreparedStatement ps=null;
        try{
            String query="update partnerProject set projectID=? where id=?";
            j=DatabaseWrapper.getConnection();
            ps=j.prepareStatement(query);
            ps.setInt(1, templateProject);
            ps.setInt(2, this.id);
            ps.execute();
        }
        finally{
DatabaseWrapper.closeDBConnection(j);
DatabaseWrapper.closePreparedStatement(ps);
        }
        this.templateProject = templateProject;
    }

    public void setUrl(String url) throws SQLException {
        Connection j=null;
PreparedStatement ps=null;
        try{
            String query="update partnerProject set url=? where id=?";
            j=DatabaseWrapper.getConnection();
            ps=j.prepareStatement(query);
            ps.setString(1, url);
            ps.setInt(2, this.id);
            ps.execute();
        }
        finally{
DatabaseWrapper.closeDBConnection(j);
DatabaseWrapper.closePreparedStatement(ps);
        }
        this.url = url;
    }
/**Destroy the pipeline record, and remove all associations with this pipeline*/
    public static String [] removeTemplateProject(int id) throws SQLException {
        String [] toret = new String[3];
        toret[2] = new PartnerProject(id).getName();
        //remove associated projects
        Project [] associates = Project.getAllAssociatedProjects(id);
        int numOfAssoc = (associates != null) ? associates.length : 0;
        for (int i=0;i<numOfAssoc;i++){
            associates[i].setAssociatedPartnerProject(0);
        }
        if (numOfAssoc == 1){
            toret[0] = "1 project";
        } else {
            toret[0] = numOfAssoc + " projects";
        }
        //disconnect PartnerProject
        Connection j=null;
PreparedStatement ps=null;
        try{
            String query="delete from partnerProject where id=?";
            j=DatabaseWrapper.getConnection();
            ps=j.prepareStatement(query);
            ps.setInt(1, id);
            ps.execute();
            toret[1] = "was successfully detached from T-PEN";
            return toret;
        }
        catch (Error e) {
            // TODO log error?
            toret[1] = "encountered an error: "+e.getMessage();
            return toret;
        }
        finally {
DatabaseWrapper.closeDBConnection(j);
DatabaseWrapper.closePreparedStatement(ps);
        }
    }
public User getControllingUser() throws SQLException
{
    return new User(controllingUser);
}
public String getURL()
{
    return ESAPI.encoder().encodeForHTML(url);
}
public int getID()
{
    return id;
}
public Project getTemplateProject() throws SQLException
{
    return new Project(templateProject);
}
public String getDescription()
{
    return ESAPI.encoder().encodeForHTML(description);
}
public String getName()
{
    return ESAPI.encoder().encodeForHTML(name);
}
/**Get all existing pipelines*/
public static PartnerProject [] getAllPartnerProjects() throws SQLException
{
    PartnerProject [] all;
    String query="select id from partnerProject";
    Connection j=null;
PreparedStatement ps=null;
    Stack<PartnerProject> tmp=new Stack();
    try{
        j=DatabaseWrapper.getConnection();
        ps=j.prepareStatement(query);
        ResultSet rs=ps.executeQuery();
        while(rs.next())
        {
            tmp.push(new PartnerProject(rs.getInt(1)));
        }
    }
    finally{
DatabaseWrapper.closeDBConnection(j);
DatabaseWrapper.closePreparedStatement(ps);
    }
    all=new PartnerProject [tmp.size()];
    //odd looking way of doing this copy, I know, but it was convenient
    while(!tmp.empty())
        all[tmp.size()-1]=tmp.pop();
    return all;
}

}
