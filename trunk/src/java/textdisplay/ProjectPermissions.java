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
import java.util.logging.Level;
import java.util.logging.Logger;


public class ProjectPermissions {
public ProjectPermissions(int projectID) throws SQLException
{
    this.projectID=projectID;
    String query="select * from ProjectPermissions where projectID=?";
    Connection j = null;
PreparedStatement ps=null;
        try
            {
            j = DatabaseWrapper.getConnection();
            ps = j.prepareStatement(query);
            ps.setInt(1, projectID);
            ResultSet rs=ps.executeQuery();
            if(rs.next())
            {
                this.allow_OAC_read=rs.getBoolean("allow_OAC_read");
                this.allow_OAC_write=rs.getBoolean("allow_OAC_write");
                this.allow_public_export=rs.getBoolean("allow_export");
                this.allow_public_copy=rs.getBoolean("allow_public_copy");
                this.allow_public_modify=rs.getBoolean("allow_public_modify");
                this.allow_public_modify_buttons=rs.getBoolean("allow_public_modify_buttons");
                this.allow_public_modify_line_parsing=rs.getBoolean("allow_public_modify_line_parsing");
                this.allow_public_modify_metadata=rs.getBoolean("allow_public_modify_metadata");
                this.allow_public_read_transcription=rs.getBoolean("allow_public_read_transcription");
                this.allow_public_modify_annotation=rs.getBoolean("allow_public_modify_annotation");
                this.allow_public_modify_notes=rs.getBoolean("allow_public_modify_notes");
            }
            else
            {
                query="insert into ProjectPermissions (projectID,allow_OAC_read,allow_OAC_write,allow_export,allow_public_copy,allow_public_modify,allow_public_modify_buttons,allow_public_modify_annotation,allow_public_modify_notes,allow_public_modify_line_parsing,allow_public_modify_metadata,allow_public_read_transcription) values (?,false,false,false,false,false,false,false,false,false,false,false)";
                ps=j.prepareStatement(query);
                ps.setInt(1, projectID);
                ps.execute();
            }
            }
        finally{
            DatabaseWrapper.closeDBConnection(j);
            DatabaseWrapper.closePreparedStatement(ps);
        }
        
}
public int getProjectID()
{
    return projectID;
}
/**Should an export of the entire transcription be permitted*/
    public Boolean getAllow_export() {
        return allow_public_export;
    }

    public void setAllow_export(Boolean allow_export) throws SQLException {
    String query="update ProjectPermissions set allow_export=? where projectID=?";
    Connection j = null;
    PreparedStatement ps=null;
        try
            {
            j = DatabaseWrapper.getConnection();
            ps = j.prepareStatement(query);
            ps.setInt(2, projectID);
            ps.setBoolean(1, allow_export);
            ps.execute();
            }
        finally{
            DatabaseWrapper.closeDBConnection(j);
            DatabaseWrapper.closePreparedStatement(ps);
        }
        this.allow_public_export = allow_export;
    }
/**Should exports to other tools via OAC be permitted*/
    public Boolean getAllow_OAC_read() {
        return allow_OAC_read;
    }

    public void setAllow_OAC_read(Boolean a) throws SQLException {
        String query="update ProjectPermissions set allow_OAC_read=? where projectID=?";
    Connection j = null;
    PreparedStatement ps=null;
        try
            {
            j = DatabaseWrapper.getConnection();
            ps = j.prepareStatement(query);
            ps.setInt(2, projectID);
            ps.setBoolean(1, a);
            ps.execute();
            }
        finally{
            DatabaseWrapper.closeDBConnection(j);
            DatabaseWrapper.closePreparedStatement(ps);
        }
        this.allow_OAC_read = a;
    }
/**Should external tools be permitted to update this transcription*/
    public Boolean getAllow_OAC_write()  {
                return allow_OAC_write;
    }

    public void setAllow_OAC_write(Boolean a) throws SQLException {
        String query="update ProjectPermissions set allow_OAC_write=? where projectID=?";
    Connection j = null;
    PreparedStatement ps=null;
        try
            {
            j = DatabaseWrapper.getConnection();
            ps = j.prepareStatement(query);
            ps.setInt(2, projectID);
            ps.setBoolean(1, a);
            ps.execute();
            }
        finally{
            DatabaseWrapper.closeDBConnection(j);
            DatabaseWrapper.closePreparedStatement(ps);
        }

        this.allow_OAC_write = a;
    }
/**Should users be able to create a copy of this transcription for themselves. Changes arent garnteed to be shared back*/
    public Boolean getAllow_public_copy() {
        return allow_public_copy;
    }

    public void setAllow_public_copy(Boolean a) throws SQLException {
        String query="update ProjectPermissions set allow_public_copy=? where projectID=?";
    Connection j = null;
    PreparedStatement ps=null;
        try
            {
            j = DatabaseWrapper.getConnection();
            ps = j.prepareStatement(query);
            ps.setInt(2, projectID);
            ps.setBoolean(1, a);
            ps.execute();
            }
        finally{
            DatabaseWrapper.closeDBConnection(j);
            DatabaseWrapper.closePreparedStatement(ps);
        }

        this.allow_public_copy = a;
    }
/**Should everyone be permitted to change the transcription*/
    public Boolean getAllow_public_modify() {
        return allow_public_modify;
    }

    public void setAllow_public_modify(Boolean a) throws SQLException {
        String query="update ProjectPermissions set allow_public_modify=? where projectID=?";
    Connection j = null;
    PreparedStatement ps=null;
        try
            {
            j = DatabaseWrapper.getConnection();
            ps = j.prepareStatement(query);
            ps.setInt(2, projectID);
            ps.setBoolean(1, a);
            ps.execute();
            }
        finally{
            DatabaseWrapper.closeDBConnection(j);
            DatabaseWrapper.closePreparedStatement(ps);
        }

        this.allow_public_modify = a;
    }
    /**Should everyone be permitted to modify the xml tags and special character buttons*/
    public Boolean getAllow_public_modify_buttons() {
        return allow_public_modify_buttons;
    }

    public void setAllow_public_modify_buttons(Boolean allow_public_modify_buttons) throws SQLException {
        String query="update ProjectPermissions set allow_public_modify_buttons=? where projectID=?";
    Connection j = null;
    PreparedStatement ps=null;
        try
            {
            j = DatabaseWrapper.getConnection();
            ps = j.prepareStatement(query);
            ps.setInt(2, projectID);
            ps.setBoolean(1, allow_public_modify_buttons);
            ps.execute();
            }
        finally{
            DatabaseWrapper.closeDBConnection(j);
            DatabaseWrapper.closePreparedStatement(ps);
        }

        this.allow_public_modify_buttons = allow_public_modify_buttons;
    }
/**Should everyone be permitted to modify the line parsing*/
    public Boolean getAllow_public_modify_line_parsing() throws SQLException {
 
        return allow_public_modify_line_parsing;
    }

    public void setAllow_public_modify_line_parsing(Boolean allow_public_modify_line_parsing) throws SQLException {
               String query="update ProjectPermissions set allow_public_modify_line_parsing=? where projectID=?";
    Connection j = null;
    PreparedStatement ps=null;
        try
            {
            j = DatabaseWrapper.getConnection();
            ps = j.prepareStatement(query);
            ps.setInt(2, projectID);
            ps.setBoolean(1, allow_public_modify_line_parsing);
            ps.execute();
            }
        finally{
            DatabaseWrapper.closeDBConnection(j);
            DatabaseWrapper.closePreparedStatement(ps);
        }
        this.allow_public_modify_line_parsing = allow_public_modify_line_parsing;
    }
/**Should everyone be permitted to modify the metadata*/
    public Boolean getAllow_public_modify_metadata() {
        return allow_public_modify_metadata;
    }

    public void setAllow_public_modify_metadata(Boolean allow_public_modify_metadata) throws SQLException {
               String query="update ProjectPermissions set allow_public_modify_metadata=? where projectID=?";
    Connection j = null;
    PreparedStatement ps=null;
        try
            {
            j = DatabaseWrapper.getConnection();
            ps = j.prepareStatement(query);
            ps.setInt(2, projectID);
            ps.setBoolean(1, allow_public_modify_metadata);
            ps.execute();
            }
        finally{
            DatabaseWrapper.closeDBConnection(j);
            DatabaseWrapper.closePreparedStatement(ps);
        }
        this.allow_public_modify_metadata = allow_public_modify_metadata;
    }
/**Should everyone be permitted to view the transcription*/
    public Boolean getAllow_public_read_transcription() {
        return allow_public_read_transcription;
    }

    public void setAllow_public_read_transcription(Boolean allow_public_read_transcription) throws SQLException {
               String query="update ProjectPermissions set allow_public_read_transcription=? where projectID=?";
    Connection j = null;
    PreparedStatement ps=null;
        try
            {
            j = DatabaseWrapper.getConnection();
            ps = j.prepareStatement(query);
            ps.setInt(2, projectID);
            ps.setBoolean(1, allow_public_read_transcription);
            ps.execute();
            }
        finally{
            DatabaseWrapper.closeDBConnection(j);
            DatabaseWrapper.closePreparedStatement(ps);
        }
        this.allow_public_read_transcription = allow_public_read_transcription;
    }
    private Boolean allow_public_read_transcription=false;
    private Boolean allow_public_modify=false;
    private Boolean allow_OAC_read=false;
    private Boolean allow_OAC_write=false;
    private Boolean allow_public_modify_line_parsing=false;
    private Boolean allow_public_copy=false;
    private Boolean allow_public_export=false;
    private Boolean allow_public_modify_metadata=false;
    private Boolean allow_public_modify_buttons=false;
    private Boolean allow_public_modify_annotation=false;
    private Boolean allow_public_modify_notes=false;

    public Boolean getAllow_public_modify_annotation() {
        return allow_public_modify_annotation;
    }

    public void setAllow_public_modify_annotation(Boolean allow_public_modify_annotation) throws SQLException {
         String query="update ProjectPermissions set allow_public_modify_annotation=? where projectID=?";
    Connection j = null;
    PreparedStatement ps=null;
        try
            {
            j = DatabaseWrapper.getConnection();
            ps = j.prepareStatement(query);
            ps.setInt(2, projectID);
            ps.setBoolean(1, allow_public_modify_annotation);
            ps.execute();
            }
        finally{
            DatabaseWrapper.closeDBConnection(j);
            DatabaseWrapper.closePreparedStatement(ps);
        }
        this.allow_public_modify_annotation = allow_public_modify_annotation;
    }

    public Boolean getAllow_public_modify_notes() {
        return allow_public_modify_notes;
    }

    public void setAllow_public_modify_notes(Boolean allow_public_modify_notes) throws SQLException {
        String query="update ProjectPermissions set allow_public_modify_notes=? where projectID=?";
    Connection j = null;
    PreparedStatement ps=null;
        try
            {
            j = DatabaseWrapper.getConnection();
            ps = j.prepareStatement(query);
            ps.setInt(2, projectID);
            ps.setBoolean(1, allow_public_modify_notes);
            ps.execute();
            }
        finally{
            DatabaseWrapper.closeDBConnection(j);
            DatabaseWrapper.closePreparedStatement(ps);
        }
        this.allow_public_modify_notes = allow_public_modify_notes;
    }
    private int projectID;
    
    
}
