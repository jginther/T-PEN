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

import java.awt.Rectangle;
import java.io.IOException;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Stack;
import java.util.logging.Level;
import java.util.logging.Logger;
import com.hp.hpl.jena.rdf.model.*;
import org.owasp.esapi.ESAPI;
import Search.TranscriptionIndexer;
import user.User;


/**
 * This class handles saving and retrieving portions of a single Line of a Transcription in progress, as
 * well as storing the coordinates that Transcription is associated with.
 */
public class Transcription {

   private String text;
   private String comment;

   private int folio;
   private int line;
   private Date date;
   private String timestamp;
   public int UID;//should use a user object rather than the user id?
   private int projectID = -1;
   //the coordinates this Transcription is attached to
   private int x;
   private int y;
   private int height;
   private int width;

   private int lineID;

   /**
    * @deprecated in favor of one including creator id Create a new Transcription bounding. The text will be
    * added later. After using this, running getLineID on the resultant object will get you the Line
    * identifier that can be used for setting text.
    * @param projectID
    * @param Folio
    * @param x
    * @param y
    * @param height
    * @param width
    */
   public Transcription(int projectID, int folio, int x, int y, int height, int width, Boolean isProject) throws SQLException, IOException {
      if (isProject) {
         String insertQuery = "insert into transcription(projectID,folio,x,y,height,width,comment,text,line,creator) values(?,?,?,?,?,?,'','',-1,0)";
         Connection j = null;
         PreparedStatement ps = null;
         try {
            this.projectID = projectID;
            this.folio = folio;
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
            this.text = "";
            this.comment = "";
            j = DatabaseWrapper.getConnection();
            ps = j.prepareStatement(insertQuery, PreparedStatement.RETURN_GENERATED_KEYS);
            ps.setInt(1, projectID);
            ps.setInt(2, folio);
            ps.setInt(3, x);
            ps.setInt(4, y);
            ps.setInt(5, height);
            ps.setInt(6, width);
            ps.execute();
            ResultSet rs = ps.getGeneratedKeys();
            //the Line id is an auto increment field, so get that and store it here.
            if (rs.next()) {
               this.lineID = rs.getInt(1);
            }
            this.projectID = projectID;
            this.folio = folio;
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
            TranscriptionIndexer.add(this);
         } finally {
            DatabaseWrapper.closeDBConnection(j);
            DatabaseWrapper.closePreparedStatement(ps);
         }
      } else {
         //in this case projectID is actually a UID
         String insertQuery = "insert into transcription(creator,folio,x,y,height,width,comment,text,line) values(?,?,?,?,?,?,'','',-1)";
         Connection j = null;
         PreparedStatement ps = null;
         try {
            j = DatabaseWrapper.getConnection();
            ps = j.prepareStatement(insertQuery, PreparedStatement.RETURN_GENERATED_KEYS);
            ps.setInt(1, projectID);
            ps.setInt(2, folio);
            ps.setInt(3, x);
            ps.setInt(4, y);
            ps.setInt(5, height);
            ps.setInt(6, width);
            ps.execute();
            ResultSet rs = ps.getGeneratedKeys();
            //the Line id is an auto increment field, so get that and store it here.
            if (rs.next()) {
               this.lineID = rs.getInt(1);
            }
            this.text = "";
            this.comment = "";
            this.projectID = projectID;
            this.folio = folio;
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;

         } finally {
            DatabaseWrapper.closeDBConnection(j);
            DatabaseWrapper.closePreparedStatement(ps);
         }
      }
   }

   /**
    * Create a new Transcription specifying creator and projectid(if applicable)
    *
    * @param creator user responsible for creating this transcriptions
    * @param projectID project ID (possibly 0)
    * @param folioID ID of folio to which this transcription is being added
    * @param t initial text of transcription 
    * @param c initial text of note/comment 
    * @param r bounding box for transcription
    * @throws SQLException
    */
   public Transcription(User creator, int projID, int folioID, String t, String c, Rectangle r) throws SQLException, IOException {
      try (Connection j = DatabaseWrapper.getConnection()) {
         try (PreparedStatement ps = j.prepareStatement("INSERT INTO transcription (creator, projectID, folio, x, y, width, height, comment, text, line) " +
                 "VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?, -1)", PreparedStatement.RETURN_GENERATED_KEYS)) {
            UID = creator.getUID();
            projectID = projID;
            folio = folioID;
            x = r.x;
            y = r.y;
            width = r.width;
            height = r.height;
            text = t;
            comment = c;
            
            ps.setInt(1, UID);
            ps.setInt(2, projectID);
            ps.setInt(3, folio);
            ps.setInt(4, x);
            ps.setInt(5, y);
            ps.setInt(6, width);
            ps.setInt(7, height);
            ps.setString(8, text);
            ps.setString(9, comment);
            ps.execute();
            ResultSet rs = ps.getGeneratedKeys();
            //the Line id is an auto increment field, so get that and store it here.
            if (rs.next()) {
               lineID = rs.getInt(1);
            }
            TranscriptionIndexer.add(this);
         }
      }
   }

   /**
    * Update a transcription based on values extracted from a JSON import.
    */
   public void update(String cont, Rectangle bounds) throws SQLException {
      if (!cont.equals(text) || bounds.x != x || bounds.y != y || bounds.width != width || bounds.height != height) {
         text = cont;
         x = bounds.x;
         y = bounds.y;
         width = bounds.width;
         height = bounds.height;
         commit();
      }
   }

   public void setComment(String comm) throws SQLException {
      comment = comm;
      commit();
   }

   public void setText(String tt) throws SQLException {
      text = tt;
      commit();
   }

   public void setHeight(int h) throws SQLException {
      height = h;
      commit();
   }

   public void setWidth(int w) throws SQLException {
      width = w;
      commit();
   }

   public void setX(int val) throws SQLException {
      x = val;
      commit();
   }

   public void setY(int val) throws SQLException {
      y = val;
      commit();
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

   public String getTimestamp() {
      return timestamp;
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

   public void setCreator(int uid) {
      this.UID = uid;
      try {
         this.commit();
      } catch (SQLException ex) {
         Logger.getLogger(Transcription.class.getName()).log(Level.SEVERE, null, ex);
      }
   }

   public int getFolio() {
      return folio;
   }

   public int getLine() {
      return line;
   }

   /**
    * Return a count of all lines transcribed
    */
   public static int getNumberOfTranscribedLines() throws SQLException {
      int toret = 0;
      String query = "select count(id) from transcription where text!=''";
      Connection j = null;
      PreparedStatement ps = null;
      try {
         j = DatabaseWrapper.getConnection();
         ps = j.prepareStatement(query);
         ResultSet rs = ps.executeQuery();
         rs.next();
         toret += rs.getInt(1);
      } finally {
         DatabaseWrapper.closeDBConnection(j);
         DatabaseWrapper.closePreparedStatement(ps);
      }
      return toret;
   }

   /**
    * Delete both the Line position data and the transcribed text/comment. Scary thing to do.
    *
    * @throws SQLException
    */
   public void remove() throws SQLException {
      String query = "delete from transcription where id=?";
      Connection j = null;
      PreparedStatement ps = null;
      try {
         j = DatabaseWrapper.getConnection();
         ps = j.prepareStatement(query);
         ps.setInt(1, this.lineID);
         ps.execute();
      } finally {
         DatabaseWrapper.closeDBConnection(j);
         DatabaseWrapper.closePreparedStatement(ps);
      }
   }

   /**
    * Encode the Transcription text for use in html
    *
    * @return encoded text or "" if none available
    */
   public String getText() {
      if (text != null) //return ESAPI.encoder().encodeForHTML(text);
      {
         return text; // DEBUG all trouble. Where's ESAPI?
//         return ESAPI.encoder().encodeForHTML(ESAPI.encoder().decodeForHTML(text));
      } else {
         return "";
      }
   }

   /**
    * Only use this if you are very sure you want the unescaped version. This could have unsafe html in it!
    *
    * @return text without special encoding
    */
   public String getTextUnencoded() {
      return ESAPI.encoder().decodeForHTML(text);
   }

   /**
    * Retrieve a random user's transription of the requested page/Line combination. Used only in a demo.
    *
    * @param Folio Folio unique identifier
    * @param Line number of the Line within this page
    * @throws SQLException
    */
   public Transcription(int uniqueID) throws SQLException {
      Connection j = null;
      PreparedStatement stmt = null;
      try {
         j = DatabaseWrapper.getConnection();

         stmt = j.prepareStatement("Select * from transcription where id=?");
         stmt.setInt(1, uniqueID);
         ResultSet rs;
         rs = stmt.executeQuery();
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
         DatabaseWrapper.closePreparedStatement(stmt);
      }
   }

   /**
    * Build an ordered array of transcriptions for the specified Folio in the specified Project
    *
    * @param projectID
    * @param folioNumber
    * @return
    * @throws SQLException
    */
   public static Transcription[] getProjectTranscriptions(int projectID, int folioNumber) throws SQLException, IOException {
      String query = "select id from transcription where projectID=? and folio=? order by x, y";
      Connection j = null;
      PreparedStatement ps = null;
      Stack<Transcription> orderedTranscriptions = new Stack();
      try {
         j = DatabaseWrapper.getConnection();
         ps = j.prepareStatement(query);
         ps.setInt(1, projectID);
         ps.setInt(2, folioNumber);
         ResultSet transcriptionIDs = ps.executeQuery();
         while (transcriptionIDs.next()) {
            //add a Transcription object built using the unique id
            orderedTranscriptions.add(new Transcription(transcriptionIDs.getInt(1)));
         }
         if (orderedTranscriptions.size() == 0) {
            //create Transcription(s) based on Project settings and Line parsing
            Project p = new Project(projectID);
            Project.imageBounding preferedBounding = p.getProjectImageBounding();
            if (preferedBounding == Project.imageBounding.none) {
               //do nothing
            }
            if (preferedBounding == Project.imageBounding.fullimage) {
               //find the image size, add 1 Transcription to cover the entirety, and done

               int height = 1000;
               Folio f = new Folio(folioNumber, true);
               int width = f.getImageDimension().width;
               Transcription t = new Transcription(projectID, folioNumber, 0, 0, height, width, true);
               orderedTranscriptions.add(t);

            }
            if (preferedBounding == Project.imageBounding.columns) {
               //run the image parsing and make a Transcription for each column
               Folio f = new Folio(folioNumber, true);
               Line[] lines = f.getlines();
               int x = 0;
               int y = 0;
               int w = 0;
               for (int i = 0; i < lines.length; i++) {
                  if (lines[i].getWidth() != w) {
                     if (w != 0 && i != 0) {
                        Transcription t = new Transcription(projectID, folioNumber, x, y, lines[i].getBottom(), w, true);
                        orderedTranscriptions.add(t);
                     }
                     w = lines[i].getWidth();
                     x = lines[i].getTop();
                     y = lines[i].getLeft();

                  }
               }
            }
            if (preferedBounding == Project.imageBounding.lines) {
               //make a Transcription for each Line
               Folio f = new Folio(folioNumber, true);
               Line[] lines = f.getlines();
               for (int i = 0; i < lines.length; i++) {
                  Transcription t = new Transcription(projectID, folioNumber, lines[i].left, lines[i].top, lines[i].getHeight(), lines[i].getWidth(), true);
                  orderedTranscriptions.add(t);
               }
               if (orderedTranscriptions.size() == 0) {
                  int height = 1000;

                  int width = f.getImageDimension().width;
                  Transcription fullPage = new Transcription(projectID, folioNumber, 0, 0, height, width, true);
                  orderedTranscriptions.add(fullPage);
               }
            }

         }
         Transcription[] toret = new Transcription[orderedTranscriptions.size()];
         for (int i = 0; i < orderedTranscriptions.size(); i++) {
            toret[i] = orderedTranscriptions.get(i);
         }
         return toret;
      } finally {
         DatabaseWrapper.closeDBConnection(j);
         DatabaseWrapper.closePreparedStatement(ps);
      }

   }

   /**
    * was used to convert some old records. No longer used
    *
    * @param args
    * @throws SQLException
    */
   private static void main(String[] args) throws SQLException {
      String transcriptionSelect = "select * from transcription";
      String projectImageQuery = "select * from projectimagepositions where folio=? and project=? and line=?";
      String imageQuery = "select * from imagepositions where folio=? and line=?";
      String updateQuery = "update transcription set x=?, y=?, height=?, width=? where id=?";
      Connection j = null;
      PreparedStatement ps = null;
      PreparedStatement ps2 = null;
      PreparedStatement ps3 = null;
      PreparedStatement ps4 = null;
      try {
         j = DatabaseWrapper.getConnection();
         ps = j.prepareStatement(transcriptionSelect);
         ps2 = j.prepareStatement(imageQuery);
         ps3 = j.prepareStatement(projectImageQuery);
         ps4 = j.prepareStatement(updateQuery);
         ResultSet rs = ps.executeQuery();
         while (rs.next()) {
            if (rs.getInt("width") >= 0) {
               int id = rs.getInt("id");
               int folio = rs.getInt("folio");
               int projectID = rs.getInt("projectID");
               int line = rs.getInt("line");
               if (projectID > 0) {
                  ps3.setInt(1, folio);
                  ps3.setInt(2, projectID);
                  ps3.setInt(3, line);
                  ResultSet rs2 = ps3.executeQuery();
                  if (rs2.next()) {
                     //Folio 	Line 	bottom 	top 	id 	colstart 	width 	dummy
                     ps4.setInt(2, rs2.getInt("top"));
                     ps4.setInt(3, (rs2.getInt("bottom") - rs2.getInt("top")));
                     ps4.setInt(1, rs2.getInt("colstart"));
                     ps4.setInt(4, (rs2.getInt("colstart") + rs2.getInt("width")));
                     ps4.setInt(5, id);
                     ps4.execute();
                  }
               } else {
                  ps2.setInt(1, folio);

                  ps2.setInt(2, line);
                  ResultSet rs2 = ps2.executeQuery();
                  if (rs2.next()) {
                     //Folio 	Line 	bottom 	top 	id 	colstart 	width 	dummy
                     ps4.setInt(2, rs2.getInt("top"));
                     ps4.setInt(3, (rs2.getInt("bottom") - rs2.getInt("top")));
                     ps4.setInt(1, rs2.getInt("colstart"));
                     ps4.setInt(4, (rs2.getInt("colstart") + rs2.getInt("width")));
                     ps4.setInt(5, id);
                     ps4.execute();
                  }
               }
            }
         }
      } finally {
         DatabaseWrapper.closeDBConnection(j);
         DatabaseWrapper.closePreparedStatement(ps);
         DatabaseWrapper.closePreparedStatement(ps2);
         DatabaseWrapper.closePreparedStatement(ps3);
         DatabaseWrapper.closePreparedStatement(ps4);
      }
   }

   /**
    * Retrieve transcriptions that are not part of a Project for this user and Folio combination
    *
    * @param UID transcriber uid
    * @param folioNumber Folio unique id
    * @return all the relevant transcriptions
    * @throws SQLException
    */
   public static Transcription[] getPersonalTranscriptions(int UID, int folioNumber) throws SQLException, IOException {
      String query = "select id from transcription where creator=? and folio=? and projectID=0 order by x, y";
      Connection j = null;
      PreparedStatement ps = null;
      Stack<Transcription> orderedTranscriptions = new Stack();
      try {
         j = DatabaseWrapper.getConnection();
         ps = j.prepareStatement(query);
         ps.setInt(1, UID);
         ps.setInt(2, folioNumber);
         ResultSet transcriptionIDs = ps.executeQuery();
         while (transcriptionIDs.next()) {
            //add a Transcription object built using the unique id
            orderedTranscriptions.add(new Transcription(transcriptionIDs.getInt(1)));
         }
         if (orderedTranscriptions.size() == 0) {
            Folio f = new Folio(folioNumber, true);
            Line[] lines = f.getlines();
            for (int i = 0; i < lines.length; i++) {
               Transcription t = new Transcription(UID, folioNumber, lines[i].left, lines[i].top, lines[i].getHeight(), lines[i].getWidth(), false);
               orderedTranscriptions.add(t);
            }
         }
         Transcription[] toret = new Transcription[orderedTranscriptions.size()];
         for (int i = 0; i < orderedTranscriptions.size(); i++) {
            toret[i] = orderedTranscriptions.get(i);
         }
         return toret;
      } finally {
         DatabaseWrapper.closeDBConnection(j);
         DatabaseWrapper.closePreparedStatement(ps);
      }

   }

   /**
    * Build an OAC message for this Transcription, currently generalting a plaintext message rather than
    * serialized rdf for demo purposes
    */
   public String getOAC() throws SQLException {
      String toret = "";
      Folio f = new Folio(this.getFolio());
      toret += "        ex:Anno   a oac:Annotation ,<br>";
      toret += "                  oac:hasTarget ex:" + f.getImageName() + " ,<br>";
      toret += "                  oac:hasBody ex:uuid .<br>";
      toret += "        ex:uuid   a oac:Body ,<br>";
      toret += "                  a cnt:ContentAsText ,<br>";
      toret += "                  cnt:chars \"" + this.text + "\" ,<br>";
      toret += "                  cnt:characterEncoding \"utf-8\" .<br>";

      return toret;
   }

   public static boolean projectHasTranscriptions(int projectID, int folioNumber) throws SQLException {
      String query = "select id from transcription where projectID=? and folio=?";
      Connection j = null;
      PreparedStatement ps = null;
      try {
         j = DatabaseWrapper.getConnection();
         ps = j.prepareStatement(query);
         ps.setInt(1, projectID);
         ps.setInt(2, folioNumber);
         ResultSet transcriptionIDs = ps.executeQuery();
         if (transcriptionIDs.next()) {
            return true;

         }
         return false;
      } finally {
         DatabaseWrapper.closeDBConnection(j);
         DatabaseWrapper.closePreparedStatement(ps);
      }
   }

   /**
    * Archive the current Transcription. This is done before saving changes.
    */
   public void archive() throws SQLException {
      String query = "insert into archivedTranscription (folio,line,comment,text,date,creator,projectID,id,x,y,width,height)  (SELECT * FROM `transcription` WHERE id=?)";
      Connection j = null;
      PreparedStatement ps = null;
      try {
         j = DatabaseWrapper.getConnection();
         ps = j.prepareStatement(query);
         ps.setInt(1, this.lineID);
         ps.execute();
      } finally {
         DatabaseWrapper.closeDBConnection(j);
         DatabaseWrapper.closePreparedStatement(ps);
      }
   }

   /**
    * Save the current data. Dont actually save it unless changes have been made.
    *
    * @return true if an update occured. useful for future versioning
    * @throws SQLException
    */
   public Boolean commit() throws SQLException {
      //this.archive();//archive the old version before saving the changes.
      Connection j = null;
      PreparedStatement stmt = null;
      if (this.text == null) {
         StackTraceElement[] stacktrace = Thread.currentThread().getStackTrace();
         String stackLog = "";
         for (int i = 0; i < stacktrace.length; i++) {
            stackLog += stacktrace[i].toString();
         }
         LOG.log(Level.WARNING, "Transcription text was null {0}", stackLog);
         text = "";
      }
      try {
         j = DatabaseWrapper.getConnection();
         //if this is a group Transcription, update the current one if it exists and set the uid to this user to indicate who modified it most recently
         if (projectID > 0) {
            Project p = new Project(projectID);
            Manuscript m = new Manuscript(folio);
            //modified to now be a link
            p.addLogEntry("<span class='log_transcription'></span>Saved <a href=\"transcription.jsp?projectID=" + projectID + "&folio=" + folio + "\">" + m.getShelfMark() + "</a> " + new Folio(folio).getPageName(), this.UID);// ,"Transcription"
            stmt = j.prepareStatement("Select text,comment from transcription where id=?");
            stmt.setInt(1, lineID);
            ResultSet rs;
            rs = stmt.executeQuery();
            if (rs.next()) {
               stmt = j.prepareStatement("Update transcription set  text=?, comment=?, creator=?, x=?, y=?, height=?, width=? where id=?");

               try {
                  /**
                   * @TODO is this reencoding crap needed, or was this a desperate attempt to solve a server
                   * encoding issue
                   */
                  text = new String(text.getBytes("UTF8"), "UTF8");
               } catch (UnsupportedEncodingException ex) {
                  LOG.log(Level.SEVERE, null, ex);
               }
               stmt.setString(1, text);
               stmt.setString(2, comment);
               stmt.setInt(3, UID);
               stmt.setInt(4, x);
               stmt.setInt(5, y);
               stmt.setInt(6, height);
               stmt.setInt(7, width);
               stmt.setInt(8, lineID);

               stmt.execute();
               try {
                  TranscriptionIndexer.update(this);
               } catch (IOException ex) {
                  LOG.log(Level.SEVERE, null, ex);
               }
               return true;
            }


            stmt = j.prepareStatement("Insert into transcription (text,comment,folio,line,creator,projectID,x,y,height,width) values(?,?,?,?,?,?,?,?,?,?)");
            stmt.setInt(3, folio);
            stmt.setInt(4, line);
            stmt.setString(1, text);
            stmt.setString(2, comment);
            stmt.setInt(5, UID);
            stmt.setInt(6, projectID);
            stmt.setInt(7, x);
            stmt.setInt(8, y);
            stmt.setInt(9, height);
            stmt.setInt(10, width);


            stmt.execute();
            try {
               TranscriptionIndexer.update(this);
            } catch (IOException ex) {
               LOG.log(Level.SEVERE, null, ex);
            }
            return true;

         } else {
            stmt = j.prepareStatement("Select text,comment from transcription where id=?");
            stmt.setInt(1, lineID);



            stmt = j.prepareStatement("Update transcription set  text=?, comment=?, creator=?, x=?, y=?, height=?, width=? where id=?");

            try {
               text = new String(text.getBytes("UTF8"), "UTF8");
            } catch (UnsupportedEncodingException ex) {
               Logger.getLogger(Transcription.class.getName()).log(Level.SEVERE, null, ex);
            }
            stmt.setString(1, text);
            stmt.setString(2, comment);
            stmt.setInt(3, UID);
            stmt.setInt(4, x);
            stmt.setInt(5, y);
            stmt.setInt(6, height);
            stmt.setInt(7, width);
            stmt.setInt(8, lineID);
            stmt.execute();
            try {
               TranscriptionIndexer.update(this);
            } catch (IOException ex) {
               LOG.log(Level.SEVERE, null, ex);
            }
            return true;


         }
      } finally {
         DatabaseWrapper.closeDBConnection(j);
         DatabaseWrapper.closePreparedStatement(stmt);

      }
   }

   /**
    * Make a copy of a Line of non Project Transcription and add it to a Project. Used for converting old
    * data.
    *
    * @param uid transcriber uid
    * @param Project Project id
    * @throws SQLException
    */
   public void makeCopy(int uid, int project) throws SQLException {
      String query = "insert into transcription (text,comment,folio,line,creator,projectID) values(?,?,?,?,?,?)";
      Connection j = null;
      PreparedStatement ps = null;
      try {
         j = DatabaseWrapper.getConnection();
         ps = j.prepareStatement(query);
         ps.setString(1, text);
         ps.setString(2, comment);
         ps.setInt(3, this.folio);
         ps.setInt(4, line);
         ps.setInt(5, uid);
         ps.setInt(6, project);
         ps.execute();
      } finally {
         DatabaseWrapper.closeDBConnection(j);
         DatabaseWrapper.closePreparedStatement(ps);
      }
   }

   /**
    * Update all lines of Transcription with the same left parameter and update them to the new width
    *
    * @param newWidth the new width for all lines in the column
    * @return
    * @throws SQLException
    */
   public boolean updateColumnWidth(int newWidth) throws SQLException, IOException {
      if (projectID > 0) {
         Transcription[] theseTranscriptions = getProjectTranscriptions(projectID, folio);
         for (int i = 0; i < theseTranscriptions.length; i++) {
            if (theseTranscriptions[i].x == this.x) {
               theseTranscriptions[i].setWidth(newWidth);
            }
         }
         return true;
      } else {
         Transcription[] theseTranscriptions = Transcription.getPersonalTranscriptions(UID, folio);
         for (int i = 0; i < theseTranscriptions.length; i++) {
            if (theseTranscriptions[i].x == this.x) {
               theseTranscriptions[i].setWidth(newWidth);
            }
         }
         return true;
      }

   }

   /**
    * Update all lines of Transcription with the same left parameter and update them to the new value
    *
    * @param newLeft
    * @return
    * @throws SQLException
    */
   public boolean updateColumnLeft(int newLeft) throws SQLException, IOException {
      if (projectID > 0) {
         Transcription[] theseTranscriptions = getProjectTranscriptions(projectID, folio);
         for (int i = 0; i < theseTranscriptions.length; i++) {
            if (theseTranscriptions[i].x == this.x) {
               theseTranscriptions[i].setX(newLeft);
            }
         }
         return true;
      } else {
         Transcription[] theseTranscriptions = Transcription.getPersonalTranscriptions(UID, folio);
         for (int i = 0; i < theseTranscriptions.length; i++) {
            if (theseTranscriptions[i].x == this.x) {
               theseTranscriptions[i].setX(newLeft);
            }
         }
         return true;
      }
   }

   /**
    * Build this line of transcription as an OAC annotation and return the N3 serialization
    */
   public String getAsOAC() throws SQLException {
      Model model = ModelFactory.createDefaultModel();
      //model.setNsPrefix("dms", "http://dms.stanford.edu/ns/");
      model.setNsPrefix("oac", "http://www.openannotation.org/ns/");
      model.setNsPrefix("rdf", "http://www.w3.org/1999/02/22-rdf-syntax-ns#");
      model.setNsPrefix("ore", "http://www.openarchives.org/ore/terms/");
      model.setNsPrefix("cnt", "http://www.w3.org/2008/content#");

      model.setNsPrefix("sc", "http://www.shared-canvas.org/ns/");
      //model.setNsPrefix("dcterms", "http://purl.org/dc/terms/");

      Property oacTarget = model.createProperty("http://www.openannotation.org/ns/", "hasTarget");
      Property oacBody = model.createProperty("http://www.openannotation.org/ns/", "hasBody");
      // Property scContentAnnotation=model.createProperty("http://www.openannotation.org/ns/","Annotation");
      Property scContentAnnotation = model.createProperty("http://www.shared-canvas.org/ns/", "ContentAnnotation");
      Property contentChars = model.createProperty("http://www.w3.org/2008/content#", "rest");
      Property encoding = model.createProperty("http://www.w3.org/2008/content#", "characterEncoding");
      Resource item;
      item = model.createResource("http://t-pen.org/transcriptions/" + this.lineID);
      Property rdfType = model.createProperty("http://www.w3.org/1999/02/22-rdf-syntax-ns#", "type");
      Resource thisLine = model.createResource("urn:uuid:" + java.util.UUID.randomUUID().toString());
      Property stringContent = model.createProperty("http://www.w3.org/2008/content#ContentAsText");
      Property parseType = model.createProperty("http://www.w3.org/1999/02/22-rdf-syntax-ns#", "type");
      int folioNumber = this.folio;
      Folio f = new Folio(folioNumber);
      String xyhw = "#xywh=" + this.getX() + "," + this.getY() + "," + this.getWidth() + "," + this.getHeight();
      String image_canvas = f.getCanvas();
      if (image_canvas == null || image_canvas.length() < 10) {
         image_canvas = f.getImageURL();
      }
      Resource target = model.createResource(image_canvas + xyhw);
      Literal textLiteral = model.createLiteral(this.getText());
      Literal literal = model.createLiteral("Literal");
      Literal encodingType = model.createLiteral("utf-8");



      item.addProperty(oacBody, thisLine);
      item.addProperty(oacTarget, target);

      item.addProperty(rdfType, scContentAnnotation);
      //contentChars.addProperty(parseType, literal);
      thisLine.addProperty(contentChars, textLiteral);
      //thisLine.addProperty(parseType, literal);
      thisLine.addProperty(encoding, encodingType);
      thisLine.addProperty(rdfType, stringContent);
      StringWriter tmp = new StringWriter();
      model.write(tmp, "");
      return tmp.toString();
   }

   /**
    * Get the url of the image on which this Transcription is based. This is the scaled version of the
    * image! This abomination is used by search.jsp.
    *
    * @return the url, or "" on error
    */
   public String getImageURL() {
      try {
         Folio f = new Folio(folio);
         Manuscript m = new Manuscript(folio);
         if (!m.isRestricted()) {
            return f.getImageURLResize();
         }
         return "restricted";
      } catch (SQLException ex) {
         LOG.log(Level.SEVERE, null, ex);
      }
      return "";
   }

   public static final Logger LOG = Logger.getLogger(Transcription.class.getName());
}
