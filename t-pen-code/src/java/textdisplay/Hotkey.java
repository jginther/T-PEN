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

/**customizable hotkeys for transcribing non enlgish texts*/
public class Hotkey {

    int uid;
    int position;
    int key;
    int projectID = 0;

    /**
     * Create a new project Hotkey and store it
     * @param code the integer keycode for the character
     * @param projectID
     * @param position position this button falls in, used to order the output of all buttons
     * @param isProject distinguishes this from a button intended for on-the-fly transcription
     * @throws SQLException
     */
    public Hotkey(int code, int projectID, int position, Boolean isProject) throws SQLException {
        String query = "insert into hotkeys(projectID,uid,position,`key`) values (?,0,?,?)";
        Connection j = null;
        PreparedStatement stmt = null;
        try {
            j = DatabaseWrapper.getConnection();
            stmt = j.prepareStatement(query);
            stmt.setInt(3, code);
            stmt.setInt(1, projectID);
            stmt.setInt(2, position);
            stmt.execute();
        } finally {
            DatabaseWrapper.closeDBConnection(j);
            DatabaseWrapper.closePreparedStatement(stmt);
        }
    }

    /**
     * Add a new Hotkey for use in on-the-fly transcription
     * @param code the integer keycode for the character
     * @param uid user unique id under which this should be stored
     * @param position position this button falls in, used to order the output of all buttons
     * @throws SQLException
     */
    public Hotkey(int code, int uid, int position) throws SQLException {
        String query = "insert into hotkeys(uid,position,`key`) values (?,?,?)";
        Connection j = null;
        PreparedStatement stmt = null;
        try {
            j = DatabaseWrapper.getConnection();
            stmt = j.prepareStatement(query);
            stmt.setInt(3, code);
            stmt.setInt(1, uid);
            stmt.setInt(2, position);
            stmt.execute();
        } finally {
            DatabaseWrapper.closeDBConnection(j);
            DatabaseWrapper.closePreparedStatement(stmt);
        }

    }

    /**Get an existing Hotkey based on the current user and the key position (1-10)*/
    public Hotkey(int uid, int position) throws SQLException {
        String query = "select * from hotkeys where uid=? and position=? and projectID=0";
        Connection j = null;
        PreparedStatement stmt = null;
        try {
            j = DatabaseWrapper.getConnection();

            stmt = j.prepareStatement(query);
            stmt.setInt(1, uid);
            stmt.setInt(2, position);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                this.uid = rs.getInt("uid");
                this.position = rs.getInt("position");
                this.key = rs.getInt("key");
            } else {
                this.uid = 0;
                this.position = 0;
                this.key = 0;
            }
        } finally {
            DatabaseWrapper.closeDBConnection(j);
            DatabaseWrapper.closePreparedStatement(stmt);
        }
    }

    /**Get an existing Hotkey based on the current project and the key position (1-10)*/
    public Hotkey(int projectID, int position, Boolean project) throws SQLException {
        String query = "select * from hotkeys where projectID=? and position=?";
        Connection j = null;
        PreparedStatement stmt = null;
        try {
            j = DatabaseWrapper.getConnection();

            stmt = j.prepareStatement(query);
            stmt.setInt(1, projectID);
            stmt.setInt(2, position);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                this.projectID = rs.getInt("projectID");
                this.uid = 0;
                this.position = rs.getInt("position");
                this.key = rs.getInt("key");
            } else {
                this.uid = 0;
                this.position = position;
                this.key = 0;
            }
        } finally {
            DatabaseWrapper.closeDBConnection(j);
            DatabaseWrapper.closePreparedStatement(stmt);
        }

    }
    /**Check to see if this key was actually populated with useful data when instatiated*/
    public Boolean exists() {
        if (key == 0) {
            return false;
        }
        return true;
    }
    /**Set the value for this hotkey*/
    public void setKey(int newKey) throws SQLException {
        if (!this.exists()) {
            return;
        }
        String query = "update hotkeys set `key`=? where uid=? and position=? and projectID=0";
        if (projectID > 0) {
            query = "update hotkeys set `key`=? where projectID=? and position=?";
        }

        Connection j = null;
        PreparedStatement stmt = null;
        try {
            j = DatabaseWrapper.getConnection();
            stmt = j.prepareStatement(query);
            stmt.setInt(1, newKey);
            if (projectID == 0) {
                stmt.setInt(2, uid);
            } else {
                stmt.setInt(2, projectID);
            }
            stmt.setInt(3, position);
            this.key = newKey;
            stmt.execute();

        } finally {
            DatabaseWrapper.closeDBConnection(j);
            DatabaseWrapper.closePreparedStatement(stmt);
        }
    }

    /**
    @deprecated use Hotkey(int projectID, Boolean isProject)
     */
    public Hotkey(int uid) throws SQLException {
        this.uid = uid;
    }
    /**Constructor for project based buttons. All buttons are project based, but there was a time when that was not the case.*/
    public Hotkey(int projectID, Boolean isProject) throws SQLException {
        this.projectID = projectID;
    }
    /**Return the value for this key. will be a decimal integer, but represented as a String*/
    public String getButton() {
        return "" + key;
    }
/**Return the value for this key as an integer*/
    public int getButtonInteger() {
        return key;
    }

    /**change the position of the button from its current*/
    public void changePosition(int newPos) throws SQLException {
        String query = "update hotkeys set position=? where uid=? and position=? and projectID=0";
        if (projectID > 0) {
            query = "update hotkeys set position=? where projectID=? and position=?";
        }
        Connection j = null;
        PreparedStatement stmt = null;
        try {
            j = DatabaseWrapper.getConnection();
            stmt = j.prepareStatement(query);
            stmt.setInt(1, newPos);
            if (projectID > 0) {
                stmt.setInt(2, projectID);
            } else {
                stmt.setInt(2, uid);
            }
            stmt.setInt(3, position);

            stmt.execute();
            this.position = newPos;
        } finally {
            DatabaseWrapper.closeDBConnection(j);
            DatabaseWrapper.closePreparedStatement(stmt);
        }
    }
    /**@deprecated  use public String javascriptToAddProjectButtons(int projectID)*/
    public String javascriptToAddButtons(int uid) throws SQLException {
        String toret = "";
        String vars = "<script>";
        String query = "select * from hotkeys where uid=? order by position";
        Connection j = null;
        PreparedStatement stmt = null;
        try {
            j = DatabaseWrapper.getConnection();
            stmt = j.prepareStatement(query);
            stmt.setInt(1, uid);
            ResultSet rs = stmt.executeQuery();
            int buttonOffset = 48;
            int ctr = 0;
            while (rs.next()) {
                ctr++;
                char chara = (char) (rs.getInt("key"));
                int button = rs.getInt("position") + buttonOffset;
                //toret+="<script>if(pressedkey=="+(buttonOffset+rs.getInt("position"))+"){addchar('&#"+rs.getInt("key")+";');  return false;}</script>";
                vars += "var char" + button + "=\"" + rs.getInt("key") + "\";\n";
                toret += "<span class=\"lookLikeButtons\"  onclick=\"Interaction.addchar('&#" + rs.getInt("key") + ";');\">&#" + rs.getInt("key") + ";<sup>" + rs.getInt("position") + "</sup></span>";
            }
            if (ctr == 0) {
                Hotkey ha;
                ha = new Hotkey(222, uid, 1);
                ha = new Hotkey(254, uid, 2);
                ha = new Hotkey(208, uid, 3);
                ha = new Hotkey(240, uid, 4);
                ha = new Hotkey(503, uid, 5);
                ha = new Hotkey(447, uid, 6);
                ha = new Hotkey(198, uid, 7);
                ha = new Hotkey(230, uid, 8);
                ha = new Hotkey(540, uid, 9);
                return this.javascriptToAddButtons(uid);
            }
            vars += "</script>";
            return vars + toret;
        } finally {
            DatabaseWrapper.closeDBConnection(j);
            DatabaseWrapper.closePreparedStatement(stmt);
        }
    }
    /**Build the javascript used to drive all hotkeys that are part of this project*/
    public String javascriptToAddProjectButtons(int projectID) throws SQLException {
        String toret = "";
        String vars = "<script>";
        String query = "select * from hotkeys where uid=0 and projectID=? order by position";
        Connection j = null;
        PreparedStatement stmt = null;
        try {
            j = DatabaseWrapper.getConnection();
            stmt = j.prepareStatement(query);
            stmt.setInt(1, projectID);
            ResultSet rs = stmt.executeQuery();
            int buttonOffset = 48;
            int ctr = 0;
            while (rs.next()) {
                ctr++;
                char chara = (char) (rs.getInt("key"));
                int button = rs.getInt("position") + buttonOffset;
                //toret+="<script>if(pressedkey=="+(buttonOffset+rs.getInt("position"))+"){addchar('&#"+rs.getInt("key")+";');  return false;}</script>";
                vars += "var char" + button + "=\"" + rs.getInt("key") + "\";\n";
                toret += "<span class=\"lookLikeButtons\"  onclick=\"Interaction.addchar('&#" + rs.getInt("key") + ";');\">&#" + rs.getInt("key") + ";<sup>" + rs.getInt("position") + "</sup></span>";
            }
            if (ctr == 0) {

                new Hotkey(222, projectID, 1, true);
                new Hotkey(254, projectID, 2, true);
                new Hotkey(208, projectID, 3, true);
                new Hotkey(240, projectID, 4, true);
                new Hotkey(503, projectID, 5, true);
                new Hotkey(447, projectID, 6, true);
                new Hotkey(198, projectID, 7, true);
                new Hotkey(230, projectID, 8, true);
                new Hotkey(540, projectID, 9, true);
                return this.javascriptToAddButtons(uid);
            }
            vars += "</script>";
            return vars + toret;
        } finally {
            DatabaseWrapper.closeDBConnection(j);
            DatabaseWrapper.closePreparedStatement(stmt);
        }
    }

    /**
     * @deprecated 
     * @param uid
     * @return 
     */
    public String keyhandler(int uid) {
        String toret = "";

        return toret;
    }

    /**Remove this key*/
    public void delete() throws SQLException {
        if (this.projectID > 0) {
            String query = "delete from hotkeys where projectID=? and position=?";
            Connection j = null;
            PreparedStatement ps = null;
            PreparedStatement update=null;
            try {
                j = DatabaseWrapper.getConnection();
                ps = j.prepareStatement(query);
                ps.setInt(1, projectID);
                ps.setInt(2, position);
                ps.execute();
                update = j.prepareStatement("update hotkeys set position=? where position=? and projectID=?");
                //Adjust the position of all of the buttons with positions greater than this to be 1 less than they were
                while (true) {
                    Hotkey k = new Hotkey(projectID, position + 1, true);
                    if (k.exists()) {
                        update.setInt(1, position);
                        update.setInt(2, position + 1);
                        update.setInt(3, projectID);
                        update.execute();
                        position++;
                    } else {
                        break;
                    }
                }

            } finally {
                DatabaseWrapper.closeDBConnection(j);
                DatabaseWrapper.closePreparedStatement(ps);
                DatabaseWrapper.closePreparedStatement(update);
                
            }
        } else {
            String query = "delete from hotkeys where uid=? and position=?";
            Connection j = null;
            PreparedStatement ps = null;
            PreparedStatement update=null;
            try {
                j = DatabaseWrapper.getConnection();
                ps = j.prepareStatement(query);
                ps.setInt(1, uid);
                ps.setInt(2, position);
                ps.execute();
                update = j.prepareStatement("update hotkeys set position=? where position=? and uid=?");
                //Adjust the position of all of the buttons with positions greater than this to be 1 less than they were
                while (true) {
                    Hotkey k = new Hotkey(uid, position + 1);
                    if (k.exists()) {
                        update.setInt(1, position);
                        update.setInt(2, position + 1);
                        update.setInt(3, uid);
                        update.execute();
                        position++;
                    } else {
                        break;
                    }
                }
            } finally {
                DatabaseWrapper.closeDBConnection(j);
                DatabaseWrapper.closePreparedStatement(ps);
                DatabaseWrapper.closePreparedStatement(update);
            }
        }
    }
}
