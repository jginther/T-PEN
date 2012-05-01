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
import org.owasp.esapi.ESAPI;

/**
 *
 * A class to manage a set of images of some sort of resource (like a dictionary or abbreviation list) that has 
 * head words, so we label then by letter and headword for easy navigation.
 */
public class AbbreviationPage {

    public String getGroup() {
        return ESAPI.encoder().encodeForHTML(group);
    }

    public int getId() {
        return id;
    }

    public String getImageName() {
        return imageName;
    }

    public String getLabel() {
        return ESAPI.encoder().encodeForHTML(label);
    }
    private String imageName;
    private int id;
    private String label;
    private String group;

    /**
     * Pull the data for an image using the unique id. Mostly used when populating the headwords
     * @param id 
     * @throws SQLException 
     */
    public AbbreviationPage(int id) throws SQLException {
        String query = "select * from capelli where id=?";
        Connection j = null;
        PreparedStatement ps = null;
        try {
            j = DatabaseWrapper.getConnection();
            ps = j.prepareStatement(query);
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                this.id = id;
                this.label = rs.getString("label");
                this.imageName = rs.getString("image");
                this.group = rs.getString("group");
            }
        } finally {
            DatabaseWrapper.closeDBConnection(j);
            DatabaseWrapper.closePreparedStatement(ps);
        }
    }

    /**Get all of the groupings of pages. Most will be single letters.*/
    public static String[] getGroups(String collection) throws SQLException {
        String[] toret = new String[0];
        String query = "select distinct(`group`) from capelli where label!='irrelevant' and collection=? order by `label`";
        Connection j = null;
        PreparedStatement ps = null;
        try {
            j = DatabaseWrapper.getConnection();
            ps = j.prepareStatement(query);
            ps.setString(1, collection);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                String[] tmp = new String[toret.length + 1];
                for (int i = 0; i < toret.length; i++) {
                    tmp[i] = toret[i];
                }

                toret = tmp;
                toret[toret.length - 1] = rs.getString("group");
            }
        } finally {
            DatabaseWrapper.closeDBConnection(j);
            DatabaseWrapper.closePreparedStatement(ps);
        }
        //create an array for sorting with any leading zeros removed
        String[] removedZeroPadding = new String[toret.length];
        for (int i = 0; i < toret.length; i++) {
            removedZeroPadding[i] = toret[i];
            while (removedZeroPadding[i].startsWith("0")) {
                removedZeroPadding[i] = removedZeroPadding[i].substring(1);
            }
        }


        return toret;
    }

    /**Save the label and group values for this page*/
    public static void update(int id, String label, String group) throws SQLException {
        String query = "update capelli set label=? , `group`=? where id=?";
        Connection j = null;
        PreparedStatement ps = null;
        try {
            j = DatabaseWrapper.getConnection();
            ps = j.prepareStatement(query);
            ps.setInt(3, id);
            ps.setString(1, label);
            ps.setString(2, group);
            ps.execute();

        } finally {
            DatabaseWrapper.closeDBConnection(j);
            DatabaseWrapper.closePreparedStatement(ps);

        }
    }

    /**Mark a page as irrelevant for future display*/
    public static void setIrrelevant(int id) throws SQLException {
        String query = "update capelli set label='irrelevant' where id=?";
        Connection j = null;
        PreparedStatement ps = null;
        try {
            j = DatabaseWrapper.getConnection();
            ps = j.prepareStatement(query);
            ps.setInt(1, id);
            ps.execute();
        } finally {
            DatabaseWrapper.closeDBConnection(j);
            DatabaseWrapper.closePreparedStatement(ps);
        }
    }

    /**If there are any images that havent had the page label set, this will choose a random one and give the
    unique ID for it*/
    public static int getImageNeedingUpdate(String collection) throws SQLException {
        String query = "select id from capelli where label='none' and collection=? ORDER BY RAND() limit 1";
        Connection j = null;
        PreparedStatement ps = null;
        try {
            j = DatabaseWrapper.getConnection();
            ps = j.prepareStatement(query);
            ps.setString(1, collection);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getInt(1);
            }
            return -1;
        } finally {
            DatabaseWrapper.closeDBConnection(j);
            DatabaseWrapper.closePreparedStatement(ps);
        }
    }

    /**Get a list of html option elements representing each image in the specified group.*/
    public static AbbreviationPage[] getLabels(String group, String collection) throws SQLException {
        AbbreviationPage[] toret = new AbbreviationPage[0];
        String query = "select * from capelli where `group`=? and collection=? order by label";
        Connection j = null;
        PreparedStatement ps = null;
        try {
            j = DatabaseWrapper.getConnection();
            ps = j.prepareStatement(query);
            ps.setString(1, group);
            ps.setString(2, collection);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                AbbreviationPage[] tmp = new AbbreviationPage[toret.length + 1];
                for (int i = 0; i < toret.length; i++) {
                    tmp[i] = toret[i];
                }
                toret = tmp;
                AbbreviationPage a = new AbbreviationPage(rs.getInt("id"));
                toret[toret.length - 1] = a;
            }
        } finally {
            DatabaseWrapper.closeDBConnection(j);
            DatabaseWrapper.closePreparedStatement(ps);
        }
        return toret;

    }
}
