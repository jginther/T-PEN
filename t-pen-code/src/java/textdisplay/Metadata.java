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
import java.util.Hashtable;
import user.Group;
import user.User;

/**
 *
 *A class for storing Project Metadata.
 */
public class Metadata
{

    int projectID=-1;
    String title;
    String subtitle;

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }
    String msIdentifier;
    String msSettlement;
    String msRepository;
    String msCollection="";
    String msIdNumber;
    String author;
    String date;
    String language;
    String description;
    String location;
    String subject;

    public String getAuthor()
        {
        return author;
        }

    public void setAuthor(String author)
        {
        this.author = author;
        }

    public String getDate()
        {
        return date;
        }

    public void setDate(String date)
        {
        this.date = date;
        }

    public String getDescription()
        {
        return description;
        }

    public void setDescription(String description)
        {
        this.description = description;
        }

    public String getLanguage()
        {
        return language;
        }

    public void setLanguage(String language)
        {
        this.language = language;
        }

    public String getLocation()
        {
        return location;
        }

    public void setLocation(String location)
        {
        this.location = location;
        }

    public void setMsCollection(String msCollection)
        {
        this.msCollection = msCollection;
        }

    public void setMsIdNumber(String msIdNumber)
        {
        this.msIdNumber = msIdNumber;
        }

    public void setMsIdentifier(String msIdentifier)
        {
        this.msIdentifier = msIdentifier;
        }

    public void setMsRepository(String msRepository)
        {
        this.msRepository = msRepository;
        }

    public void setMsSettlement(String msSettlement)
        {
        this.msSettlement = msSettlement;
        }

    public void setProjectID(int projectID)
        {
        this.projectID = projectID;
        }

    public void setSubtitle(String subtitle)
        {
        this.subtitle = subtitle;
        }

    public void setTitle(String title) throws SQLException
        {
        this.title=title;
        this.commit();
        }

    public String getMsCollection()
        {
        return msCollection;
        }

    public String getMsIdNumber()
        {
        return msIdNumber;
        }

    public String getMsIdentifier()
        {
        return msIdentifier;
        }

    public String getMsRepository()
        {
        return msRepository;
        }

    public String getMsSettlement()
        {
        return msSettlement;
        }

    public int getProjectID()
        {
        return projectID;
        }

    public String getSubtitle()
        {
        return subtitle;
        }

    public String getTitle()
        {
        return title;
        }




/**Retrieves existing metadata for the project or creates a new metadata record if nothing exists.*/
    public Metadata(int projectID) throws SQLException
    {
    fetch(projectID);
    //If there was nothing to fetch, create a record wiht empty values
    if(this.projectID==-1)
    {
        String query="Insert into metadata (title, subtitle, msIdentifier, msSettlement, msRepository, msIdNumber,msCollection,projectID, subject, description, `date`, language, location, author) values(?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
        Connection j=null;
PreparedStatement ps=null;
        try
        {
        j=DatabaseWrapper.getConnection();
        ps=j.prepareStatement(query);
        ps.setString(1, " ");
        ps.setString(2, " ");
        ps.setString(3, " ");
        ps.setString(4, " ");
        ps.setString(5, " ");
        ps.setString(6, " ");
        ps.setString(7, " ");
        ps.setInt(8, projectID);
ps.setString(9, " ");
ps.setString(10, " ");
ps.setString(11, "");
ps.setString(12, "");
ps.setString(13, "");
ps.setString(14, "");
        ps.execute();
        this.fetch(projectID);
        }
        finally{
            if(j!=null)
DatabaseWrapper.closeDBConnection(j);
DatabaseWrapper.closePreparedStatement(ps);
        }

    }
    }
    /**netbean constructor, can be used to populate values and commit or set projid and build*/
    public Metadata()
    {

    }
    public void fetch(int projectID) throws SQLException
    {
            String query="select * from metadata where projectID=?";
    Connection j=null;
PreparedStatement ps=null;
    try{
        j=DatabaseWrapper.getConnection();
        ps=j.prepareStatement(query);
        ps.setInt(1, projectID);
        ResultSet rs=ps.executeQuery();
        if(rs.next())
        {
            this.projectID=projectID;
            title=rs.getString("title");
            subtitle=rs.getString("subtitle");
            msIdentifier=rs.getString("msIdentifier");
            msSettlement=rs.getString("msSettlement");
            msRepository=rs.getString("msRepository");
            msIdNumber=rs.getString("msIdNumber");
            subject=rs.getString("subject");
            date=rs.getString("date");
            language=rs.getString("language");
            author=rs.getString("author");
            description=rs.getString("description");
            location=rs.getString("location");
            msCollection=rs.getString("msCollection");
        }
    }
    finally
    {
        if(j!=null)
        {
DatabaseWrapper.closeDBConnection(j);
DatabaseWrapper.closePreparedStatement(ps);
        }
    }
    }
    public void commit() throws SQLException
    {
        String query="update metadata set title=?, subtitle=?, msIdentifier=?, msSettlement=?, msRepository=?, msIdNumber=?, subject=?, author=?, `date`=?, location=?, description=?, language=?, msCollection=? where projectID=? ";
        Connection j=null;
        PreparedStatement updater=null;
        try
        {
            j=DatabaseWrapper.getConnection();
            updater=j.prepareStatement(query);
            updater.setString(1, this.title);
            updater.setString(2, this.subtitle);
            updater.setString(3, this.msIdentifier);
            updater.setString(4, this.msSettlement);
            updater.setString(5, this.msRepository);
            updater.setString(6, this.msIdNumber);
            updater.setString(7, this.subject);
            updater.setString(8, this.author);
            updater.setString(9, this.date);
            updater.setString(10, this.location);
            updater.setString(12, this.language);
            updater.setString(11, this.description);
            updater.setString(13, this.msCollection );
            updater.setInt(14, projectID);
            updater.execute();
        }
        catch(Exception e){
            e.printStackTrace();
        }
        finally
        {
            DatabaseWrapper.closeDBConnection(j);
            DatabaseWrapper.closePreparedStatement(updater);
        }

    }
    /**Generate a TEI header from the Metadata TPEN stores*/
    public String getTEI() throws SQLException
    {
        String toret="";
        toret+="<teiHeader>\n<fileDesc>\n<titleStmt>\n<title type=\"main\">"+this.title+"</title>\n";
        toret+="<title type=\"sub\">"+this.subtitle+"</title>\n";
        Project thisProject=new Project(this.projectID);
        Group g=new Group(thisProject.groupID);
        User [] members=g.getMembers();

        toret+="<respStmt><resp>Contributor</resp>\n";
        for(int i=0;i<members.length;i++)
        {
        toret+="<name>"+members[i].getFname()+" "+members[i].getLname()+"</name>\n";
        }
        toret+="\n</respStmt>\n";

        toret+="</titleStmt>\n";
        
        toret+="<sourceDesc>\n";
        toret+="<msDesc>\n";
        toret+="<msIdentifier>\n";
        if(this.msSettlement.length()>2)
        {
        toret+="<settlement>"+this.msSettlement+"</settlement>\n";
        toret+="<repository>"+this.msRepository+"</repository>\n";
        toret+="<collection>"+this.msCollection+"</collection>\n";
        toret+="<idno>"+this.msIdNumber+"</idno>\n";
        toret+="<altIdentifier>"+this.msIdentifier+"</altIdentifier>\n";
        }
        else
        {
            Manuscript f=new Manuscript(thisProject.firstPage());
            toret+="<settlement>"+f.getCity()+"</settlement>\n";
        toret+="<repository>"+f.getRepository()+"</repository>\n";
        toret+="<collection>"+f.getCollection()+"</collection>\n";
        toret+="<idno>"+f.getShelfMark()+"</idno>\n";
            
        }
        
        toret+="</msIdentifier>\n";
        toret+="</msDesc>\n";
        toret+="</sourceDesc>\n";
        toret+="</fileDesc>\n";
        toret+="</teiHeader>";
        return toret;
    }
    public static void main(String [] args) throws SQLException
    {
        System.out.print(new Metadata(611).getTEI());
    }            
    public String getDublinCore() throws SQLException
    {
        Project thisProject=new Project(this.projectID);
        Group g=new Group(thisProject.groupID);
        User [] members=g.getMembers();
        Manuscript f=new Manuscript(thisProject.firstPage());
        String toret="";
        toret+="<metadata>";
        toret+="<dc:title>"+this.title;
        toret+="</dc:title>\n";
        toret+="<dc:identifier>"+f.getShelfMark();
        toret+="</dc:identifier>\n";
        toret+="</metadata>";
        return toret;
    }
    

}
