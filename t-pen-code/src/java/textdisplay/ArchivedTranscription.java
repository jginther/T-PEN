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
package textdisplay;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import org.owasp.esapi.ESAPI;

/**
 * This class is for handling archived (older) versions of a transcription.
 */
public class ArchivedTranscription {

   private String text;
   private String comment;
   private int folio;
   private int line;
   private Date date;
   public int UID;//should use a user object rather than the user id?
   private int projectID = -1;
   //the coordinates this transcription is attached to
   private int x;
   private int y;
   private int height;
   private int width;
   private int lineID;

   /**
    * Initialise an archived transcription from the current result-set row.
    *
    * @param rs result-set positioned to row of interest
    * @param proj ID of project to which this transcription belongs
    * @param line ID of line of which this is an archive
    * @throws SQLException
    */
   private ArchivedTranscription(ResultSet rs, int proj, int line) throws SQLException {
      text = rs.getString("text");
      comment = rs.getString("comment");
      UID = rs.getInt("creator");
      lineID = line;
      x = rs.getInt("x");
      y = rs.getInt("y");
      width = rs.getInt("width");
      height = rs.getInt("height");
      projectID = proj;
      folio = rs.getInt("folio");
      date = rs.getDate("date");
   }

   /**
    * Get a particular archived version by unique ID.
    *
    * @param uniqueID the archived transcription id, not to be confused with the transcription id.
    * @throws SQLException
    */
   private ArchivedTranscription(int uniqueID) throws SQLException {
      String query = "Select * from archivedTranscription where uniqueID=?";
      Connection j = null;
      PreparedStatement ps = null;
      try {
         j = DatabaseWrapper.getConnection();
         ps = j.prepareStatement(query);
         ps.setInt(1, uniqueID);
         ResultSet rs = ps.executeQuery();
         if (rs.next()) {
            text = rs.getString("text");
            comment = rs.getString("comment");
            UID = rs.getInt("creator");
            lineID = rs.getInt("id");
            x = rs.getInt("x");
            y = rs.getInt("y");
            width = rs.getInt("width");
            height = rs.getInt("height");
            this.projectID = rs.getInt("projectID");
            this.folio = rs.getInt("folio");
            date = rs.getDate("date");
         }
      } finally {
         DatabaseWrapper.closeDBConnection(j);
         DatabaseWrapper.closePreparedStatement(ps);
      }
   }

   /**
    * Get all older versions of a transcription created by a particular person.
    */
   public static ArchivedTranscription[] getAllVersionsByCreator(int transcriptionID, int uid) throws SQLException {
      String query = "select uniqueID from archivedTranscription where id=? and creator=?";
      Connection j = null;
      PreparedStatement ps = null;
      try {
         j = DatabaseWrapper.getConnection();
         ps = j.prepareStatement(query);
         ps.setInt(1, transcriptionID);
         ResultSet rs = ps.executeQuery();
         Stack<ArchivedTranscription> tmp = new Stack();
         while (rs.next()) {
            tmp.push(new ArchivedTranscription(rs.getInt(1)));
         }
         ArchivedTranscription[] toret = new ArchivedTranscription[tmp.size()];
         for (int i = 0; i < toret.length; i++) {
            toret[i] = tmp.pop();
         }
         return toret;
      } finally {
         DatabaseWrapper.closeDBConnection(j);
         DatabaseWrapper.closePreparedStatement(ps);
      }
   }

   /**
    * Get all older versions of a particular transcription.
    */
   public static ArchivedTranscription[] getAllVersions(int transcriptionID) throws SQLException {
      String query = "select uniqueID from archivedTranscription where id=?";
      Connection j = null;
      PreparedStatement ps = null;
      try {
         j = DatabaseWrapper.getConnection();
         ps = j.prepareStatement(query);
         ps.setInt(1, transcriptionID);
         ResultSet rs = ps.executeQuery();
         Stack<ArchivedTranscription> tmp = new Stack();
         while (rs.next()) {
            tmp.push(new ArchivedTranscription(rs.getInt(1)));
         }
         ArchivedTranscription[] toret = new ArchivedTranscription[tmp.size()];
         for (int i = 0; i < toret.length; i++) {
            toret[i] = tmp.pop();
         }
         return toret;
      } finally {
         DatabaseWrapper.closeDBConnection(j);
         DatabaseWrapper.closePreparedStatement(ps);
      }
   }

   /**
    * Return the revision that would have been live at the passed in date/time. Return null if none exists.
    *
    * @param transcriptionID
    * @param date yyyy-MM-dd HH:mm:ss formatted date
    */
   public static ArchivedTranscription getVersionAsOf(int transcriptionID, String date) throws SQLException {
      String query = "select uniqueID from archivedTranscription where id=? and date <? order by date desc limit 1";
      Connection j = null;
      PreparedStatement ps = null;
      try {
         j = DatabaseWrapper.getConnection();
         ps = j.prepareStatement(query);

         java.text.SimpleDateFormat df = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
         ps.setInt(1, transcriptionID);
         ps.setString(2, date);
         ResultSet rs = ps.executeQuery();
         Stack<ArchivedTranscription> tmp = new Stack();
         if (rs.next()) {
            return new ArchivedTranscription(rs.getInt(1));
         }
         return null;

      } finally {
         DatabaseWrapper.closeDBConnection(j);
         DatabaseWrapper.closePreparedStatement(ps);
      }
   }

   /**
    * Get all older versions of transcriptions for the given project.
    */
   public static Map<Integer, List<ArchivedTranscription>> getAllVersionsForPage(int projID, int folioID) throws SQLException {
      try (Connection j = DatabaseWrapper.getConnection()) {
         try (PreparedStatement ps = j.prepareStatement("SELECT * FROM archivedTranscription "
                 + "WHERE projectID = ? AND folio = ? "
                 + "ORDER BY x, y")) {
            ps.setInt(1, projID);
            ps.setInt(2, folioID);
            ResultSet rs = ps.executeQuery();
            Map<Integer, List<ArchivedTranscription>> result = new LinkedHashMap<>();
            while (rs.next()) {
               int lineID = rs.getInt("id");
               List<ArchivedTranscription> dest;
               if (!result.containsKey(lineID)) {
                  dest = new ArrayList<>();
                  result.put(lineID, dest);
               } else {
                  dest = result.get(lineID);
               }
               dest.add(new ArchivedTranscription(rs, projID, lineID));
            }

            return result;
         }
      }
   }

   public int getHeight() {
      return height;
   }

   public int getLineID() {
      return lineID;
   }

   public int getProjectID() {
      return projectID;
   }

   public Date getDate() {
      return date;
   }

   public int getWidth() {
      return width;
   }

   public int getX() {
      return x;
   }

   public int getY() {
      return y;
   }

   public String getComment() {
      return comment;
   }

   public int getCreator() {
      return UID;
   }

   public int getFolio() {
      return folio;
   }

   public int getLine() {
      return line;
   }

   public String getText() {
      if (text != null) {
         return ESAPI.encoder().encodeForHTML(ESAPI.encoder().decodeForHTML(text));
      } else {
         return "";
      }
   }
}
