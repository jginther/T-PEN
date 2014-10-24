/*
 * Copyright 2011-2013 Saint Louis University. Licensed under the
 *	Educational Community License, Version 2.0 (the "License"); you may
 * not use this file except in compliance with the License. You may
 * obtain a copy of the License at
 *
 * http://www.osedu.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an "AS IS"
 * BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing
 * permissions and limitations under the License.
 * 
 * @author Jon Deering
 */
package utils;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Stack;
import textdisplay.DatabaseWrapper;
import user.User;


/**
 * This is a mechanism for controlling the display of tools internal to TPEN in the transcription UI. You
 * can do things like hide the parsing correction tool if you have already finished checking parsing for the
 * whole project.
 */
public class Tool {

   public enum tools {
      preview, compare, parsing, abbreviation, history, linebreak, annotation, paleography, sciat
   };

   public Tool(tools toolName, int UID) throws SQLException {
      User u = new User(UID);//make sure the user exists
      String query = "insert into tools(uid,tool) values(?,?)";
      Connection j = null;
      PreparedStatement ps = null;
      try {
         j = DatabaseWrapper.getConnection();
         ps = j.prepareStatement(query);
         ps.setInt(1, UID);
         ps.setString(2, toolName.toString());
         ps.execute();
      } finally {
         DatabaseWrapper.closeDBConnection(j);
         DatabaseWrapper.closePreparedStatement(ps);
      }
   }

   public static void removeTool(tools toolName, int UID) throws SQLException {
      String query = "delete from tools where UID=? and tool=?";
      Connection j = null;
      PreparedStatement ps = null;
      try {
         j = DatabaseWrapper.getConnection();
         ps = j.prepareStatement(query);
         ps.setInt(1, UID);
         ps.setString(2, toolName.name());
         ps.execute();
      } finally {
         DatabaseWrapper.closeDBConnection(j);
         DatabaseWrapper.closePreparedStatement(ps);
      }
   }

   public static void initializeTools(int uid) throws SQLException {
      for (Tool.tools iter : tools.values()) {
         if (iter != tools.sciat && iter != tools.annotation) {
            Tool t = new Tool(iter, uid);
         }
      }
   }

   public static Boolean isToolActive(tools t, int uid) throws SQLException {

      String query = "select * from tools where uid=? and tool=?";
      Connection j = null;
      PreparedStatement ps = null;
      try {
         j = DatabaseWrapper.getConnection();
         ps = j.prepareStatement(query);
         ps.setInt(1, uid);
         ps.setString(2, t.toString());
         ResultSet rs = ps.executeQuery();
         if (rs.next()) {
            return true;
         }
      } finally {
         DatabaseWrapper.closeDBConnection(j);
         DatabaseWrapper.closePreparedStatement(ps);
      }
      return false;
   }

   public static tools[] getTools(int UID) throws SQLException {
      tools[] toret = null;
      String query = "select tool from tools where UID=?";
      Stack<String> res = new Stack();
      Connection j = null;
      PreparedStatement ps = null;
      try {
         j = DatabaseWrapper.getConnection();
         ps = j.prepareStatement(query);
         ps.setInt(1, UID);
         ResultSet rs = ps.executeQuery();
         while (rs.next()) {
            res.push(rs.getString(1));
         }
         toret = new tools[res.size()];
         int ctr = 0;
         while (!res.empty()) {
            String tmp = res.pop();
            if (tmp.compareTo("preview") == 0) {
               toret[ctr] = tools.preview;
            }
            if (tmp.compareTo("compare") == 0) {
               toret[ctr] = tools.compare;
            }
            if (tmp.compareTo("parsing") == 0) {
               toret[ctr] = tools.parsing;
            }
            if (tmp.compareTo("abbreviation") == 0) {
               toret[ctr] = tools.abbreviation;
            }
            if (tmp.compareTo("history") == 0) {
               toret[ctr] = tools.history;
            }
            if (tmp.compareTo("linebreak") == 0) {
               toret[ctr] = tools.linebreak;
            }
            ctr++;
         }
      } finally {
         DatabaseWrapper.closeDBConnection(j);
         DatabaseWrapper.closePreparedStatement(ps);
      }
      return toret;
   }

   public static void removeAll(int uid) throws SQLException {
      String query = "delete from tools where uid=?";
      Connection j = null;
      PreparedStatement ps = null;
      try {
         j = DatabaseWrapper.getConnection();
         ps = j.prepareStatement(query);
         ps.setInt(1, uid);
         ps.execute();
      } finally {
         DatabaseWrapper.closeDBConnection(j);
         DatabaseWrapper.closePreparedStatement(ps);
      }
   }
}
