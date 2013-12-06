
/*
 * @author Jon Deering
 Copyright 2011 Saint Louis University. Licensed under the Educational Community License, Version 2.0 (the "License"); you may not use
 this file except in compliance with the License.

 You may obtain a copy of the License at http://www.osedu.org/licenses/ECL-2.0

 Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS,
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions
 and limitations under the License.
 */
package user;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

import textdisplay.DatabaseWrapper;

/**
 *
 * Represents a single group
 */
public class Group
    {

    private String name;
    private int groupID;
    /**
     * Permitted project roles
     */
    public enum roles
        {
        Leader, Editor, Contributor, Suspended, None
        };
        /**
         * get the unique ID for this group
         * @return
         */
    public int getGroupID()
        {
        return groupID;
        }

    /**
     * Populate the group based on the group unique id
     * @param id unique id
     * @throws SQLException
     */
    public Group(int id) throws SQLException
        {
        Connection j = null;
        PreparedStatement ps=null;
        try
            {
            groupID = id;
            j = DatabaseWrapper.getConnection();
            
            ps = j.prepareStatement("select * from groups where GID=?");
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next())
                {
                name = rs.getString("name");

                } else
                {
                groupID = 0;

                }

            } finally
            {
                DatabaseWrapper.closeDBConnection(j);
                DatabaseWrapper.closePreparedStatement(ps);
            }
        }

    /**
     * Create a new group with the give name, the UID is the id of the creator
     * @param gname name for this group
     * @param UID The user id of the creator of this group
     * @throws SQLException
     */
    public Group(String gname, int UID) throws SQLException
        {
        Connection j = null;
PreparedStatement qry=null;
        qry=null;
        try
            {
            name = gname;
            j = DatabaseWrapper.getConnection();
            
            qry = j.prepareStatement("insert into groups (name) value (?)", PreparedStatement.RETURN_GENERATED_KEYS);
            qry.setString(1, name);

            qry.execute();
            ResultSet rs = qry.getGeneratedKeys();

            if (rs.next())
                {
                groupID = rs.getInt(1);
                qry = j.prepareStatement("insert into groupMembers (UID,GID,role) values(?,?,?)");
                qry.setInt(1, UID);
                qry.setInt(2, groupID);
                //The creator of a group gets leader, otherwise noone else could be invited.
                qry.setString(3, roles.Leader.toString());
                qry.execute();
                }
            } finally
            {
                DatabaseWrapper.closeDBConnection(j);
                DatabaseWrapper.closePreparedStatement(qry);
            }
        }

    /**
     * Get the name of this group
     * @return
     */
    public String getTitle()
        {
        return name;
        }

    /**
     * Get all members of the group
     * @return array of all users in this group
     * @throws SQLException
     */
    public User[] getMembers() throws SQLException
        {
        Connection j = null;
PreparedStatement qry=null;
        qry=null;
        try
            {
            j = DatabaseWrapper.getConnection();
            
            qry = j.prepareStatement("select UID from groupMembers where GID=?");
            qry.setInt(1, groupID);
            ResultSet rs = qry.executeQuery();
            int recordCount = 0;
            while (rs.next())
                {
                recordCount++;
                }
            User[] users = new User[recordCount];
            rs.beforeFirst();
            int i = 0;
            while (rs.next())
                {
                users[i] = new User(rs.getInt("UID"));
                i++;
                }
            return users;
            } finally
            {
                DatabaseWrapper.closeDBConnection(j);
                DatabaseWrapper.closePreparedStatement(qry);
            }
        }
/**
 * Return all users with the role Group.roles.leader
 * @return array of users who are leaders for this group
 * @throws SQLException
 */
public User[] getLeader() throws SQLException
        {
        Connection j = null;
PreparedStatement qry=null;
        qry=null;
        try
            {
            j = DatabaseWrapper.getConnection();
            
            qry = j.prepareStatement("select UID from groupMembers where GID=? and role='Leader'");
            qry.setInt(1, groupID);
            ResultSet rs = qry.executeQuery();
            int recordCount = 0;
            while (rs.next())
                {
                recordCount++;
                }
            User[] users = new User[recordCount];
            rs.beforeFirst();
            int i = 0;
            while (rs.next())
                {
                users[i] = new User(rs.getInt("UID"));
                i++;
                }

            return users;
            } finally
            {
                DatabaseWrapper.closeDBConnection(j);
                DatabaseWrapper.closePreparedStatement(qry);
            }
        }

    /**
     * getMembersTable is a convenient way to list group members, use getMembers if you need to do something
     * else display wise
     * @return String containing HTML for a table listing all users in the group
     * @throws SQLException
     */
    public String getMembersTable() throws SQLException
        {
        String toret = "";
        User[] groupMembers = this.getMembers();
        for (int i = 0; i < groupMembers.length; i++)
            {
            User thisUser = groupMembers[i];
            toret += "<tr><td>" + thisUser.getUID() + "</td><td>" + thisUser.getFname() + thisUser.getLname() + "</td></tr>";
            }
        return toret;
        }

    /**
     * Remove a user from this group
     * @param UID unique identifier of the user to remove from the group
     * @throws SQLException
     */
    public void remove(int UID) throws SQLException
        {
        Connection j = null;
PreparedStatement qry=null;
        qry=null;
        try
            {
            j = DatabaseWrapper.getConnection();
            
            qry = j.prepareStatement("Delete from groupMembers where GID=? and UID=? LIMIT 1", PreparedStatement.RETURN_GENERATED_KEYS);
            qry.setInt(1, groupID);
            qry.setInt(2, UID);
            qry.execute();
            } finally
            {
                DatabaseWrapper.closeDBConnection(j);
                DatabaseWrapper.closePreparedStatement(qry);
            }
        }

    /**
     * @deprecated TPEN no longer uses openID
     * Add a group member using openid
     * @param identifier email address
     * @return
     */
    public Boolean addMember(String identifier)
        {
        try
            {
            User u = new User(identifier, true);
            if (u != null && u.getUID() > 0)
                {
                addMember(u.getUID(), Group.roles.Contributor);
                return true;
                }
            else
            {
                u = new User(identifier);
                if (u != null && u.getUID() > 0)
                {
                addMember(u.getUID(), Group.roles.Contributor);
                return true;
                }
            }
            } catch (SQLException ex)
            {
            Logger.getLogger(Group.class.getName()).log(Level.SEVERE, null, ex);
            }
        return false;
        }

    /**
     * Add someone to the group with role Contributor
     * @param UID the unique ID of the user to be added as a contributor
     * @throws SQLException
     */
    public void addMember(int UID) throws SQLException
        {
        Connection j = null;
PreparedStatement qry=null;
        qry=null;
        try
            {
            j = DatabaseWrapper.getConnection();
            
            qry = j.prepareStatement("insert into groupMembers (GID,UID,role) values(?,?,?)");
            qry.setInt(1, groupID);
            qry.setInt(2, UID);
            qry.setString(3, roles.Contributor.toString());
            qry.execute();
            } finally
            {
                DatabaseWrapper.closeDBConnection(j);
                DatabaseWrapper.closePreparedStatement(qry);
            }
        }

    /**
     * Add someone to the group with the specified role
     * @param UID unique ID of the user to add
     * @param thisRole the Group.roles to add this user with
     * @throws SQLException
     */
    public void addMember(int UID, roles thisRole) throws SQLException
        {
        Connection j = null;
PreparedStatement qry=null;
        qry=null;
        try
            {
            j = DatabaseWrapper.getConnection();
           
            qry = j.prepareStatement("insert into groupMembers (GID,UID,role) values(?,?,?)");
            qry.setInt(1, groupID);
            qry.setInt(2, UID);
            qry.setString(3, thisRole.toString());
            qry.execute();
            } finally
            {
                DatabaseWrapper.closeDBConnection(j);
                DatabaseWrapper.closePreparedStatement(qry);
            }
        }

    /**
     * Set someone's role after verifying that the requestor has permission
     * @param requestingUID unique ID of the user attempting to add someone to the group
     * @param targetUID unique id of the user being added to the group
     * @param thisRole Group.roles for the user being added
     * @throws SQLException
     */
    public void setUserRole(int requestingUID, int targetUID, roles thisRole) throws SQLException
        {
        if (isAdmin(requestingUID))
            {
            Connection j = null;
PreparedStatement qry=null;
            qry=null;
            try
                {
                String query = "Update groupMembers set role=? where GID=? and UID=?";
                j = DatabaseWrapper.getConnection();
                
                qry = j.prepareStatement(query);
                qry.setString(1, thisRole.toString());
                qry.setInt(2, groupID);
                qry.setInt(3, targetUID);
                qry.execute();
                } finally
                {
                    DatabaseWrapper.closeDBConnection(j);
                DatabaseWrapper.closePreparedStatement(qry);
                }
            }
        }

    /**
     * True if the requested UID is a group member, false otherwise
     * @param UID unique id of the user
     * @return true if they are a member of the group, false if they are not
     */
    public Boolean isMember(int UID)
        {
        try
            {
            User[] groupMembers = this.getMembers();
            for (int i = 0; i < groupMembers.length; i++)
                {
                User thisUser = groupMembers[i];
                if (thisUser.getUID() == UID)
                    {
                    return true;
                    }
                }
            return false;
            } catch (SQLException e)
            {
            return false;
            }
        }

/**
     * Return the enum item that matches the roles stored in the DB
     * @param UID
     * @return
     * @throws SQLException
     */
    private roles getUserRole(int UID) throws SQLException
        {
        roles thisRole = roles.None;
        Connection j = null; 
PreparedStatement qry=null;
        qry=null;
        try
            {
            j = DatabaseWrapper.getConnection();
           
            qry = j.prepareStatement("select role from groupMembers where UID=? and GID=?");
            qry.setInt(1, UID);
            qry.setInt(2, groupID);
            ResultSet rs = qry.executeQuery();
            if (rs.next())
                {
                String roleText = rs.getString(1);
                //Now match the string with the enum
                roles[] allVals = roles.values();
                for (int i = 0; i < allVals.length; i++)
                    {
                    if (allVals[i].toString().compareTo(roleText) == 0)
                        {
                        thisRole = allVals[i];
                        }
                    }
                }
            return thisRole;
            } finally
            {
                DatabaseWrapper.closeDBConnection(j);
                DatabaseWrapper.closePreparedStatement(qry);
            }
        }

    /**
     * True if the requested UID has admin rights in this group (Groups.roles.Leader), false otherwise. For the moment, only the Leader role gets a true
     * @param UID unique ID of the user in question
     * @return true if they are an admin
     * @throws SQLException
     */
    public Boolean isAdmin(int UID) throws SQLException
        {
        if (getUserRole(UID) == roles.Leader)
            {
            return true;
            }
        return false;
        }
    }
