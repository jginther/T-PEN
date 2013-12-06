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

import textdisplay.DatabaseWrapper;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Stack;

/**This class controls tools that can be displayed in an iframe like dictionaries and abbreviation lists.*/
public class UserTool {

    private String name, url;
    private int projectID;

    /**Add a new iframe based tool to this project*/
    public UserTool(String name, String url, int project) throws SQLException {
        String query = "insert into userTools (name, url, projectID) values(?,?,?)";
        Connection j = null;
        PreparedStatement ps = null;
        try {
            j = DatabaseWrapper.getConnection();
            ps = j.prepareStatement(query);
            ps.setString(1, name);
            ps.setString(2, url);
            ps.setInt(3, project);
            ps.execute();
            this.name = name;
            this.url = url;
            this.projectID = project;

        } finally {
            DatabaseWrapper.closeDBConnection(j);
            DatabaseWrapper.closePreparedStatement(ps);
        }
    }

    public int getProjectID() {
        return projectID;
    }

    public void setProjectID(int projectID) {
        this.projectID = projectID;
    }

    public UserTool(String url, String name) {
        this.url = url;
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public String getUrl() {
        return url;
    }

    /**Remove the tool*/
    public static void removeUserTool(String url, int project) throws SQLException {
        String query = "delete from userTools where projectID=? and url=?";
        Connection j = null;
        PreparedStatement ps = null;
        try {
            j = DatabaseWrapper.getConnection();
            ps = j.prepareStatement(query);
            ps.setInt(1, project);
            ps.setString(2, url);
            ps.execute();
        } finally {
            DatabaseWrapper.closeDBConnection(j);
            DatabaseWrapper.closePreparedStatement(ps);
        }
    }

    /**Get all tools assciated with a project*/
    public static UserTool[] getUserTools(int project) throws SQLException {
        UserTool[] toret = null;
        String query = "select url,name from userTools where projectID=?";
        Stack<UserTool> tmp = new Stack();
        Connection j = null;
        PreparedStatement ps = null;
        try {
            j = DatabaseWrapper.getConnection();
            ps = j.prepareStatement(query);
            ps.setInt(1, project);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                UserTool thisOne = new UserTool(rs.getString(1), rs.getString(2));
                thisOne.setProjectID(project);
                tmp.push(thisOne);
            }
            toret = new UserTool[tmp.size()];
            for (int i = 0; i < toret.length; i++) {
                toret[i] = tmp.pop();
            }
        } finally {
            DatabaseWrapper.closeDBConnection(j);
            DatabaseWrapper.closePreparedStatement(ps);
        }
        return toret;
    }

    /**Delete all tools assciated with a project*/
    public static void removeAll(int projectID) throws SQLException {
        String query = "delete from userTools where projectID=?";
        Connection j = null;
        PreparedStatement ps = null;
        try {
            j = DatabaseWrapper.getConnection();
            ps = j.prepareStatement(query);
            ps.setInt(1, projectID);
            ps.execute();
        } finally {
            DatabaseWrapper.closeDBConnection(j);
            DatabaseWrapper.closePreparedStatement(ps);
        }
    }
}
