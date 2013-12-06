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

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFList;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import org.owasp.esapi.ESAPI;
import user.Group;
import user.User;


public class Project {

   /**
    * Defines the level of parsing TPEN will attempt to provide for a project.
    */
   public enum imageBounding {
      lines, columns, fullimage, none
   };

   int groupID;
   int projectID;
   String projectName;
   String linebreakSymbol = "-";
   private imageBounding projectImageBounding;

   /**
    * What is the prefered level of parsing for this project.
    */
   public imageBounding getProjectImageBounding() {
      return this.projectImageBounding;
   }

   /**
    * include one of the built in tools in TPEN in the display for this project (ex. preview, correct parsing)
    */
   public void addTool(String name, String url) throws SQLException {
      Connection j = null;
      PreparedStatement ps = null;
      String query = "insert into tools(name,url) values(?,?)";
      try {
         j = DatabaseWrapper.getConnection();
         ps = j.prepareStatement(query);
         ps.setString(1, name);
         ps.setString(2, url);
         ps.execute();
      } finally {
         DatabaseWrapper.closeDBConnection(j);
         DatabaseWrapper.closePreparedStatement(ps);
      }
   }

   /**
    * exclude one of the built in tools in TPEN in the display for this project (ex. preview, correct parsing)
    */
   public void removeTool(String name, String url) throws SQLException {
      Connection j = null;
      PreparedStatement ps = null;
      String query = "delete from tools where name=? and url=?";
      try {
         j = DatabaseWrapper.getConnection();
         ps = j.prepareStatement(query);
         ps.setString(1, name);
         ps.setString(2, url);
         ps.execute();
      } finally {
         DatabaseWrapper.closeDBConnection(j);
         DatabaseWrapper.closePreparedStatement(ps);
      }
   }

   /**
    * Not Implemented
    */
   public String[] getTools() throws SQLException {
      String[] toret = null;
      Connection j = null;
      PreparedStatement ps = null;
      String query = "select * from tools";
      try {
         j = DatabaseWrapper.getConnection();
         ps = j.prepareStatement(query);
         ps.execute();
      } finally {
         DatabaseWrapper.closeDBConnection(j);
         DatabaseWrapper.closePreparedStatement(ps);
      }
      return toret;
   }

   /**
    * Add a tool by url which can be displayed in an iframe
    */
   public void addUserTool(String name, String url) throws SQLException {
      new utils.UserTool(name, url, projectID);
   }

   /**
    * Add the standard latin tools to a project
    */
   public final void initializeTools() throws SQLException {
      this.addUserTool("Latin Vulgate", "http://vulsearch.sourceforge.net/cgi-bin/vulsearch");
      this.addUserTool("Latin Dictionary", "http://t-pen.org/hopper/morph.jsp");
   }

   /**
    * Sets the prefered level of parsing TPEN should attempt. Default is line
    */
   public void setProjectImageBounding(imageBounding projectImageBounding) throws SQLException {
      this.projectImageBounding = projectImageBounding;
      String updateQuery = "update project set imageBounding=? where id=?";
      Connection j = null;
      PreparedStatement ps = null;
      try {
         j = DatabaseWrapper.getConnection();
         ps = j.prepareStatement(updateQuery);
         ps.setString(1, projectImageBounding.name());
         ps.setInt(2, projectID);
         ps.execute();
      } finally {
         DatabaseWrapper.closeDBConnection(j);
         DatabaseWrapper.closePreparedStatement(ps);
      }
   }

   public String getLinebreakSymbol() {
      return linebreakSymbol;
   }

   public void setLinebreakSymbol(String linebreakSymbol) {
      this.linebreakSymbol = linebreakSymbol;
   }
   int linebreakCharacterLimit = 5000;

   public int getLinebreakCharacterLimit() {
      return linebreakCharacterLimit;
   }

   /**
    * Update the number of characters from the uploaded text the linebreak feature will give per page
    */
   public void setLinebreakCharacterLimit(int newLimit) throws SQLException {
      String query = "update project set linebreakCharacterLimit=? where projectID=?";
      Connection j = null;
      PreparedStatement updateQuery = null;
      try {
         j = DatabaseWrapper.getConnection();
         updateQuery = j.prepareStatement(query);
         updateQuery.setInt(1, newLimit);
         updateQuery.setInt(1, projectID);
         updateQuery.execute();
      } finally {
         DatabaseWrapper.closeDBConnection(j);
         DatabaseWrapper.closePreparedStatement(updateQuery);
      }
   }

   /**
    * Set the priority number for this project, which is used to sort the project listing. Higher is
    * displayed first
    */
   public void setProjectPriorty(int priority) throws SQLException {
      String query = "update project set priority=? where id=?";
      Connection j = null;
      PreparedStatement updateQuery = null;
      try {
         j = DatabaseWrapper.getConnection();
         updateQuery = j.prepareStatement(query);
         updateQuery.setInt(1, priority);
         updateQuery.setInt(2, projectID);
         updateQuery.execute();
      } finally {
         DatabaseWrapper.closeDBConnection(j);
         DatabaseWrapper.closePreparedStatement(updateQuery);
      }
   }

   public Project() {
   }

   /**
    * Create a new Project
    */
   public int getProjectID() {
      return projectID;
   }
   /*
    public int addFolio(int folioNum)
    {
    String query="insert into projectFolios";
    }*/

   /**
    * Update the Project image sequence
    */
   public void buildSequence(Folio[] orderedFolios) throws SQLException {
      Connection j = null;
      PreparedStatement qry = null;
      try {
         String query = "update projectFolios set position=? where project=? and folio=?";
         j = DatabaseWrapper.getConnection();
         qry = j.prepareStatement(query);
         //qry.execute("delete from projectSequence where Project="+this.projectID);
         for (int i = 0; i < orderedFolios.length; i++) {
            qry.setInt(1, i + 1);
            qry.setInt(2, this.projectID);
            qry.setInt(3, orderedFolios[i].folioNumber);
            qry.execute();

         }
      } finally {
         if (j != null) {
            DatabaseWrapper.closeDBConnection(j);
            DatabaseWrapper.closePreparedStatement(qry);
         }
      }

   }

   /**
    * Get the full Transcription as a single string
    */
   public String getFullDocument() throws SQLException {

      String toret = "";
      String query = "select transcription.text from transcription join folios on folios.pageNumber=transcription.folio where projectID=? order by folio,line";
      Connection j = null;
      PreparedStatement ps = null;

      try {
         j = DatabaseWrapper.getConnection();
         ps = j.prepareStatement(query);

         ps.setInt(1, this.projectID);
         ResultSet rs = ps.executeQuery();
         while (rs.next()) {
            toret += rs.getString(1);
         }
         return toret;
      } finally {
         if (j != null) {
            DatabaseWrapper.closeDBConnection(j);
            DatabaseWrapper.closePreparedStatement(ps);
         }
      }
   }

   /**
    * Copy the buttons from Project p to this Project, destroying an existing buttons in this Project
    */
   public synchronized void copyButtonsFromProject(Project p) throws SQLException {
      Connection j = null;
      PreparedStatement del = null;
      PreparedStatement table = null;
      PreparedStatement reins = null;
      PreparedStatement upd = null;
      PreparedStatement ins = null;

      try {
         j = DatabaseWrapper.getConnection();
         String deleteQuery = "delete from projectButtons where project=?";
         String tableCreate = "Create TEMPORARY table tmpbuttons like projectButtons";
         String tableDrop = "DROP table tmpbuttons";
         String insertQuery = "insert into tmpbuttons(select * from projectButtons where project=?)";
         String updateQuery = "update tmpbuttons set project=?";
         String reinsertQuery = "insert into projectButtons(select * from tmpbuttons)";
         del = j.prepareStatement(deleteQuery);
         table = j.prepareStatement(tableCreate);
         table.execute();
         ins = j.prepareStatement(insertQuery);
         upd = j.prepareStatement(updateQuery);
         reins = j.prepareStatement(reinsertQuery);
         del.setInt(1, projectID);
         del.execute();
         ins.setInt(1, p.projectID);
         ins.execute();
         //now update the Project ids
         upd.setInt(1, this.projectID);
         upd.execute();
         reins.execute();
         //this doesnt guarntee a drop, because it isnt in a finally, but combined with the table being a temp table, we should be good.
         table.execute(tableDrop);
         table.close();
      } finally {
         DatabaseWrapper.closeDBConnection(j);
         DatabaseWrapper.closePreparedStatement(ins);
         DatabaseWrapper.closePreparedStatement(del);
         DatabaseWrapper.closePreparedStatement(table);
         DatabaseWrapper.closePreparedStatement(reins);
         DatabaseWrapper.closePreparedStatement(upd);
      }
   }

   /**
    * Copy the character hotkeys from another project. Destryoys anything in this project.
    */
   public synchronized void copyHotkeysFromProject(Project p) throws SQLException {
      Connection j = null;
      PreparedStatement del = null;
      PreparedStatement table = null;
      PreparedStatement reins = null;
      PreparedStatement upd = null;
      PreparedStatement ins = null;

      try {
         j = DatabaseWrapper.getConnection();
         String deleteQuery = "delete from hotkeys where projectID=?";
         String tableCreate = "Create TEMPORARY table tmphotkeys like hotkeys";
         String tableDrop = "DROP table tmphotkeys";
         String insertQuery = "insert into tmphotkeys(select * from hotkeys where projectID=?)";
         String updateQuery = "update tmphotkeys set projectID=?";
         String reinsertQuery = "insert into hotkeys(select * from tmphotkeys)";
         del = j.prepareStatement(deleteQuery);
         table = j.prepareStatement(tableCreate);
         table.execute();
         ins = j.prepareStatement(insertQuery);
         upd = j.prepareStatement(updateQuery);
         reins = j.prepareStatement(reinsertQuery);
         del.setInt(1, projectID);
         del.execute();
         ins.setInt(1, p.projectID);
         ins.execute();
         //now update the Project ids
         upd.setInt(1, this.projectID);
         upd.execute();
         reins.execute();
         //this doesnt guarntee a drop, because it isnt in a finally, but combined with the table being a temp table, we should be good.
         table.execute(tableDrop);
         table.close();
      } finally {
         DatabaseWrapper.closeDBConnection(j);
         DatabaseWrapper.closePreparedStatement(ins);
         DatabaseWrapper.closePreparedStatement(del);
         DatabaseWrapper.closePreparedStatement(table);
         DatabaseWrapper.closePreparedStatement(reins);
         DatabaseWrapper.closePreparedStatement(upd);
      }
   }

   /**
    * Modify existing transcriptions belogining to the specified user to be part of this Project
    */
   public void importData(int uid) throws SQLException {
      Connection j = null;
      PreparedStatement ps = null;
      String query = "update transcription set projectID=? where creator=? and folio=? and projectID=0";
      try {
         j = DatabaseWrapper.getConnection();
         ps = j.prepareStatement(query);
         Folio[] theseFolios = this.getFolios();
         for (int i = 0; i < theseFolios.length; i++) {
            ps.setInt(1, projectID);
            ps.setInt(2, uid);
            ps.setInt(3, theseFolios[i].getFolioNumber());
            ps.execute();
         }
      } finally {
         DatabaseWrapper.closeDBConnection(j);
         DatabaseWrapper.closePreparedStatement(ps);
      }

   }

   /**
    * Create a new Project, with the specified name and group
    */
   public Project(String name, int group) throws SQLException {
      Connection j = null;
      PreparedStatement qry = null;
      try {
         if (name == null || name.length() == 0) {
            name = "new project";
         }
         j = DatabaseWrapper.getConnection();
         qry = j.prepareStatement("insert into project (name, grp, schemaURL,linebreakCharacterLimit ) values(?,?,'',5000)", PreparedStatement.RETURN_GENERATED_KEYS);

         qry.setString(1, name);
         qry.setInt(2, group);
         qry.execute();
         ResultSet rs = qry.getGeneratedKeys();
         if (rs.next()) {
            projectID = rs.getInt(1);
            Metadata md = new Metadata(projectID);
            md.setTitle(name);
            this.initializeTools();
            this.groupID = group;
         }
      } finally {
         DatabaseWrapper.closeDBConnection(j);
         DatabaseWrapper.closePreparedStatement(qry);
      }

   }

   /**
    * Get the existing Project based on the Project ID
    */
   public Project(int projectID) throws SQLException {
      Connection j = null;
      PreparedStatement qry = null;
      try {
         j = DatabaseWrapper.getConnection();
         qry = j.prepareStatement("select * from project where id=?");
         qry.setInt(1, projectID);
         ResultSet rs = qry.executeQuery();
         if (rs.next()) {
            projectName = rs.getString("name");
            groupID = rs.getInt("grp");
            this.projectID = projectID;
            this.linebreakCharacterLimit = rs.getInt("linebreakCharacterLimit");
            String imageBoundingStr = rs.getString("imageBounding");
            if (imageBoundingStr.compareTo("columns") == 0) {
               this.projectImageBounding = imageBounding.columns;
            }
            if (imageBoundingStr.compareTo("fullimage") == 0) {
               this.projectImageBounding = imageBounding.fullimage;
            }
            if (imageBoundingStr.compareTo("none") == 0) {
               this.projectImageBounding = imageBounding.none;
            }
            if (imageBoundingStr.compareTo("lines") == 0) {
               this.projectImageBounding = imageBounding.lines;
            }

         }
      } finally {
         if (j != null) {
            DatabaseWrapper.closeDBConnection(j);
            DatabaseWrapper.closePreparedStatement(qry);
         } else {
            System.err.print("Attempt to close DB connection failed, connection was null" + this.getClass().getName() + "\n");
         }
      }
   }

   /**
    * Return a listing of all tasks associated with this Project
    */
   public String listTasks() throws SQLException {
      String query = "select id from tasks where project=?";
      String toret = "";
      Connection j = null;
      PreparedStatement qry = null;
      try {
         j = DatabaseWrapper.getConnection();
         qry = j.prepareStatement(query);
         qry.setInt(1, projectID);
         ResultSet rs = qry.executeQuery();
         while (rs.next()) {
            toret += rs.getInt(1);
         }
         return toret;
      } finally {
         if (j != null) {
            DatabaseWrapper.closeDBConnection(j);
            DatabaseWrapper.closePreparedStatement(qry);
         } else {
            System.err.print("Attempt to close DB connection failed, connection was null" + this.getClass().getName() + "\n");
         }
      }
   }

   /**
    * Retrieve the Project name from the Project Metadata
    */
   public String getProjectName() throws SQLException {
      String toret = new Metadata(this.projectID).title;
      if (toret.compareTo("") == 0) {
         toret = this.projectName;
         if (toret == null || toret.compareTo("") == 0) {
            toret = "unknown project";
         }
         new Metadata(this.projectID).setTitle(toret);

      }
      return toret;
   }

   /**
    * @Depricated no longer implemented, renumbering project IDs isnt allowed
    */
   public void setProjectNumber(int id) {
      projectID = id;
   }

   /**
    * @Depricated in favor of storing this in the Metadata object
    */
   public void setProjectName(String projectName) {
   }

   /**
    * Return Metadata in tei p5 format for CCL
    */
   public Metadata getMetadata() throws SQLException {
      return new Metadata(this.projectID);
   }

   /**
    * take any existing public image parsings and save them as Project specific
    */
   private void storeImagePositions(int folio) throws SQLException {
      Connection j = null;
      PreparedStatement qry = null;
      PreparedStatement insertStatement = null;
      try {
         String query = "select * from imagepositions where folio=?";
         j = DatabaseWrapper.getConnection();
         qry = j.prepareStatement(query);
         qry.setInt(1, folio);
         ResultSet rs = qry.executeQuery();
         String insertQuery = "insert into projectImagePositions(folio, project, line, top, bottom, colstart, width) values(?,?,?,?,?,?,?)";
         insertStatement = j.prepareStatement(insertQuery);
         while (rs.next()) {
            insertStatement.setInt(1, rs.getInt("folio"));
            insertStatement.setInt(3, rs.getInt("line"));
            insertStatement.setInt(4, rs.getInt("top"));
            insertStatement.setInt(5, rs.getInt("bottom"));
            insertStatement.setInt(6, rs.getInt("colstart"));
            insertStatement.setInt(7, rs.getInt("width"));
            insertStatement.setInt(2, rs.getInt("projectID"));
            insertStatement.execute();
         }
      } finally {
         DatabaseWrapper.closeDBConnection(j);
         DatabaseWrapper.closePreparedStatement(qry);
         DatabaseWrapper.closePreparedStatement(insertStatement);

      }
   }

   /**
    * Get all projects with public read access ordered by project name
    */
   public static Project[] getPublicProjects() throws SQLException {
      String query = "select distinct(project.id) from project join ProjectPermissions on project.id=ProjectPermissions.projectID where ProjectPermissions.allow_public_read_transcription=true order by project.name desc";
      Connection j = null;
      PreparedStatement ps = null;
      try {
         j = DatabaseWrapper.getConnection();
         ps = j.prepareStatement(query);

         ResultSet rs = ps.executeQuery();
         Stack<Integer> projectIDs = new Stack();
         while (rs.next()) {
            projectIDs.push(rs.getInt("project.id"));
         }
         Project[] toret = new Project[projectIDs.size()];
         int ctr = 0;
         while (!projectIDs.empty()) {
            toret[ctr] = new Project(projectIDs.pop());
            ctr++;
         }
         return toret;
      } finally {
         DatabaseWrapper.closeDBConnection(j);
         DatabaseWrapper.closePreparedStatement(ps);
      }
   }

   /**
    * Get all projects ordered by project name
    */
   public static Project[] getAllProjects() throws SQLException {
      String query = "select distinct(project.id) from project order by project.name desc";
      Connection j = null;
      PreparedStatement ps = null;
      try {
         j = DatabaseWrapper.getConnection();
         ps = j.prepareStatement(query);

         ResultSet rs = ps.executeQuery();
         Stack<Integer> projectIDs = new Stack();
         while (rs.next()) {
            projectIDs.push(rs.getInt("project.id"));
         }
         Project[] toret = new Project[projectIDs.size()];
         int ctr = 0;
         while (!projectIDs.empty()) {
            toret[ctr] = new Project(projectIDs.pop());
            ctr++;
         }
         return toret;
      } finally {
         DatabaseWrapper.closeDBConnection(j);
         DatabaseWrapper.closePreparedStatement(ps);
      }
   }

   /**
    * Check to see if this project contains user uploaded manuscript images.
    */
   public Boolean containsUserUploadedManuscript() throws SQLException {
      String query = "select * from projectFolios join folios on projectFolios.folio=folios.pageNumber join manuscript on folios.msID=manuscript.id where projectFolios.project=? and manuscript.restricted=-999";
      Connection j = null;
      PreparedStatement ps = null;
      try {
         j = DatabaseWrapper.getConnection();
         ps = j.prepareStatement(query);
         ps.setInt(1, projectID);
         ResultSet rs = ps.executeQuery();
         if (rs.next()) {
            return true;
         }
         return false;
      } finally {
         DatabaseWrapper.closeDBConnection(j);
         DatabaseWrapper.closePreparedStatement(ps);
      }
   }

   /**
    * Create a new Project with the specified leader and copes of the transcriptions, annotations, buttons,
    * and schema
    */
   public int copyProject(int leaderUID) throws SQLException, Exception {
      if (this.containsUserUploadedManuscript()) {
         throw new Exception("Cannot copy a project with user uploaded images!");
      }
      Group g = new Group("Copy of " + this.projectName, leaderUID);
      Project p = new Project("Copy of " + this.projectName, g.getGroupID());
      setFolios(this.getFolios(), p.projectID);
      p.copyButtonsFromProject(this);
      p.copyHotkeysFromProject(this);
      p.setSchemaURL(this.getSchemaURL());
      Connection j = null;
      PreparedStatement qry = null;
      PreparedStatement insertStatement = null;
      try {
         String query = "select * from transcription where projectID=?";
         j = DatabaseWrapper.getConnection();
         qry = j.prepareStatement(query);
         qry.setInt(1, projectID);

         ResultSet rs = qry.executeQuery();
         String insertQuery = "insert into transcription(folio, line, comment, text, creator, projectID,x,y,height,width) values(?,?,?,?,?,?,?,?,?,?)";

         insertStatement = j.prepareStatement(insertQuery);
         while (rs.next()) {
            insertStatement.setInt(1, rs.getInt("folio"));
            insertStatement.setInt(2, rs.getInt("line"));
            insertStatement.setString(3, rs.getString("comment"));
            insertStatement.setString(4, rs.getString("text"));
            insertStatement.setInt(5, rs.getInt("creator"));
            insertStatement.setInt(6, p.projectID);
            insertStatement.setInt(7, rs.getInt("x"));
            insertStatement.setInt(8, rs.getInt("y"));
            insertStatement.setInt(9, rs.getInt("height"));
            insertStatement.setInt(10, rs.getInt("width"));
            insertStatement.execute();
         }
      } finally {
         DatabaseWrapper.closeDBConnection(j);
         DatabaseWrapper.closePreparedStatement(qry);
         DatabaseWrapper.closePreparedStatement(insertStatement);

      }
      return p.projectID;
   }

   /**
    * Copy any transcribed text created by this user outside of this Project into this Project.
    */
   private void copyTranscriptions(int folio, int uid) throws SQLException {
      Connection j = null;
      PreparedStatement qry = null;
      PreparedStatement insertStatement = null;
      try {
         String query = "select * from transcription where folio=? and creator=?";
         j = DatabaseWrapper.getConnection();
         qry = j.prepareStatement(query);
         qry.setInt(1, folio);
         qry.setInt(2, uid);
         ResultSet rs = qry.executeQuery();
         String insertQuery = "insert into transcription(folio, line, comment, text, creator, projectID values(?,?,?,?,?,?)";

         insertStatement = j.prepareStatement(insertQuery);
         while (rs.next()) {
            insertStatement.setInt(1, rs.getInt("folio"));
            insertStatement.setInt(2, rs.getInt("line"));
            insertStatement.setInt(3, rs.getInt("comment"));
            insertStatement.setInt(4, rs.getInt("text"));
            insertStatement.setInt(5, rs.getInt("creator"));
            insertStatement.setInt(6, this.projectID);
            insertStatement.execute();
         }
      } finally {
         DatabaseWrapper.closeDBConnection(j);
         DatabaseWrapper.closePreparedStatement(qry);
         DatabaseWrapper.closePreparedStatement(insertStatement);

      }
   }

   /**
    * Runs line detection on predefined columns. The columns are defined as lines when this is called, so
    * you might have 2 large lines, each being a column.
    */
   public void detectInColumns(int folioNumber) throws SQLException, IOException {
      Folio f = new Folio(folioNumber);
      Line[] t = getLines(folioNumber);
      LOG.log(Level.INFO, "User specified column count {0}", t.length);
      for (int i = 0; i < t.length; i++) {
         LOG.log(Level.INFO, "Column {0},{1},{2},{3}", new Object[]{t[i].left, t[i].right, t[i].top, t[i].bottom});
      }

      Line[] linePositions = f.detectInColumns(t);

      Connection j = null;
      PreparedStatement stmt = null;
      try {
         j = DatabaseWrapper.getConnection();
         stmt = j.prepareStatement("Delete from projectimagepositions where folio=?");
         stmt.setInt(1, folioNumber);
         stmt.execute();
         Transcription[] oldTranscriptions = Transcription.getProjectTranscriptions(projectID, folioNumber);
         for (int i = 0; i < oldTranscriptions.length; i++) {
            oldTranscriptions[i].remove();
         }
         stmt = j.prepareStatement("Insert into projectimagepositions (folio,line,top,bottom,colstart,width,project) values (?,?,?,?,?,?,?)");
         if (linePositions.length == 0) {
            linePositions = t;
         }
         for (int i = 0; i < linePositions.length; i++) {
            stmt.setInt(1, folioNumber);
            stmt.setInt(2, i + 1);
            stmt.setInt(3, linePositions[i].top);
            stmt.setInt(4, linePositions[i].bottom);
            stmt.setInt(5, linePositions[i].left);
            stmt.setInt(6, linePositions[i].right);
            stmt.setInt(7, projectID);
            stmt.execute();
            Transcription tr = new Transcription(projectID, folioNumber, linePositions[i].left, linePositions[i].top, linePositions[i].getHeight(), linePositions[i].right, true);
         }
      } finally {
         if (j != null) {
            DatabaseWrapper.closeDBConnection(j);
            DatabaseWrapper.closePreparedStatement(stmt);
         } else {
            System.err.print("Attempt to close DB connection failed, connection was null" + this.getClass().getName() + "\n");
         }

      }
   }

   /**
    * Set the linebreak text to this value, should only be used by the upload process
    */
   public void setLinebreakText(String txt) throws SQLException {
      String query = "insert into linebreakingText (projectID,remainingText) values (?,?)";
      String deleteQuery = "delete from linebreakingText where projectID=?";
      Connection j = null;
      PreparedStatement ps = null;
      PreparedStatement del = null;
      try {
         j = DatabaseWrapper.getConnection();
         del = j.prepareStatement(deleteQuery);
         del.setInt(1, projectID);
         del.execute();
         ps = j.prepareStatement(query);
         ps.setInt(1, projectID);
         ps.setString(2, txt);
         ps.execute();
      } finally {
         DatabaseWrapper.closeDBConnection(j);
         DatabaseWrapper.closePreparedStatement(ps);
         DatabaseWrapper.closePreparedStatement(del);

      }
   }

   /**
    * Set the linebreak text to this value, should only be used by the upload process
    */
   public void setHeaderText(String txt) throws SQLException {
      String query = "insert into projectHeader (projectID,header) values (?,?)";
      String deleteQuery = "delete from projectHeader where projectID=?";
      Connection j = null;
      PreparedStatement ps = null;
      PreparedStatement del = null;
      try {
         j = DatabaseWrapper.getConnection();
         del = j.prepareStatement(deleteQuery);
         del.setInt(1, projectID);
         del.execute();
         ps = j.prepareStatement(query);
         ps.setInt(1, projectID);
         ps.setString(2, txt);
         ps.execute();
      } finally {
         DatabaseWrapper.closeDBConnection(j);
         DatabaseWrapper.closePreparedStatement(ps);
         DatabaseWrapper.closePreparedStatement(del);

      }
   }

   /**
    * Update the linebreak text by removing the number of characters specified by linebreakCharacterLimit
    * and replacing those with
    returnedText
    */
   public void setLinebreakTextWithReturnedText(String returnedText) throws SQLException {
      String query = "update linebreakingText set remainingText=? where projectID=?";
      String selectQuery = "select remainingText  from linebreakingText where projectID=?";
      Connection j = null;
      PreparedStatement ps = null;
      PreparedStatement selectStatement = null;
      try {

         j = DatabaseWrapper.getConnection();
         ps = j.prepareStatement(query);
         selectStatement = j.prepareStatement(selectQuery);
         selectStatement.setInt(1, projectID);
         ResultSet rs = selectStatement.executeQuery();
         if (rs.next()) {

            String oldValue = "";
            if (rs.getString(1).length() >= linebreakCharacterLimit) {
               oldValue = rs.getString(1).substring(this.linebreakCharacterLimit);
            }
            oldValue = returnedText + oldValue;
            ps.setInt(2, projectID);
            ps.setString(1, oldValue);
            ps.execute();
         }
      } finally {
         DatabaseWrapper.closeDBConnection(j);
         DatabaseWrapper.closePreparedStatement(ps);
         DatabaseWrapper.closePreparedStatement(selectStatement);

      }
   }

   /**
    * Retrieve Project.linebreakCharacterLimit characters of from the uploaded text file
    */
   public String getLinebreakText() throws SQLException {
      String query = "select remainingText  from linebreakingText where projectID=?";
      Connection j = null;
      PreparedStatement ps = null;
      try {
         j = DatabaseWrapper.getConnection();
         ps = j.prepareStatement(query);
         ps.setInt(1, projectID);
         ResultSet rs = ps.executeQuery();
         if (rs.next()) {
            String toret = rs.getString(1);
            if (toret.length() > this.linebreakCharacterLimit) {
               return ESAPI.encoder().encodeForHTML(toret.substring(0, this.linebreakCharacterLimit));
            }
            return toret;
         } else {
            return "";
         }
      } finally {
         if (j != null) {
            DatabaseWrapper.closeDBConnection(j);
            DatabaseWrapper.closePreparedStatement(ps);
         }
      }

   }

   /**
    * Retrieve a stored header that was uploaded by the user.
    */
   public String getHeader() throws SQLException {
      String query = "select header  from projectHeader where projectID=?";
      Connection j = null;
      PreparedStatement ps = null;
      try {
         j = DatabaseWrapper.getConnection();
         ps = j.prepareStatement(query);
         ps.setInt(1, projectID);
         ResultSet rs = ps.executeQuery();
         if (rs.next()) {
            String toret = rs.getString(1);

            return ESAPI.encoder().encodeForHTML(toret);


         } else {
            return "";
         }
      } finally {

         DatabaseWrapper.closeDBConnection(j);
         DatabaseWrapper.closePreparedStatement(ps);

      }

   }

   /**
    * Returns the Folio number of the most recently modified Folio, -1 if non exists
    */
   public int getLastModifiedFolio() throws SQLException {
      String query = "select folio from transcription where projectID=? order by date desc limit 1";
      Connection j = null;
      PreparedStatement ps = null;
      try {
         j = DatabaseWrapper.getConnection();
         ps = j.prepareStatement(query);
         ps.setInt(1, projectID);
         ResultSet rs = ps.executeQuery();
         if (rs.next()) {
            return rs.getInt(1);
         } else {
            return -1;
         }
      } finally {
         if (j != null) {
            DatabaseWrapper.closeDBConnection(j);
            DatabaseWrapper.closePreparedStatement(ps);
         }
      }
   }

   /**
    * Get the parsed lines for this Folio that are specific to this Project
    */
   public Line[] getLines(int folio) throws SQLException, IOException {
      Connection j = null;
      PreparedStatement qry = null;
      try {
         String query = "Select * from projectimagepositions where folio=? and project=? and width>0 order by colstart,top ";
         j = DatabaseWrapper.getConnection();
         qry = j.prepareStatement(query);
         qry.setInt(1, folio);
         qry.setInt(2, this.projectID);
         ResultSet rs = qry.executeQuery();
         Stack<Line> lines = new Stack();
         while (rs.next()) {
            Line tmp = new Line(0, 0, 0, 0);
            tmp.bottom = rs.getInt("bottom");
            tmp.top = rs.getInt("top");
            tmp.left = rs.getInt("colstart");
            tmp.right = tmp.left + rs.getInt("width");
            // i++;
            //Line tmp = new Line(rs.getInt("colstart"), rs.getInt("colstart") + rs.getInt("width"), rs.getInt("top"), rs.getInt("top") + rs.getInt("bottom"));
            lines.add(tmp);
         }
         Line[] toret = new Line[lines.size()];
         for (int i = 0; i < toret.length; i++) {
            toret[i] = lines.get(i);
         }
         //if there wasnt anything specific to this Project, get whatever is public, if that doesnt exist either, itll parse the image, store it public, and pass it here
         if (toret.length == 0) {
            return new Folio(folio, true).getlines();
         }
         return toret;
      } finally {
         if (j != null) {
            DatabaseWrapper.closeDBConnection(j);
            DatabaseWrapper.closePreparedStatement(qry);
         } else {
            System.err.print("Attempt to close DB connection failed, connection was null" + this.getClass().getName() + "\n");
         }
      }
   }

   /**
    * Build a string of option elements, one for each Folio element in the Project
    */
   public String getFolioDropdown() throws SQLException {
      String toret = "";
      Folio[] allFolios = getFolios();
      for (int i = 0; i < allFolios.length; i++) {

         toret += "<option value=\"" + allFolios[i].getFolioNumber() + "&projectID=" + this.projectID + "\">" + allFolios[i].getCollectionName() + " " + allFolios[i].getPageName() + "</option>";
      }
      return toret;
   }

   /**
    * Get the list of folios for this Transcription Project
    */
   public Folio[] getFolios() throws SQLException {
      Connection j = null;
      PreparedStatement qry = null;
      try {
         Folio[] toret = new Folio[0];
         Stack<Folio> t = new Stack();
         String query = "select * from projectFolios where project=? order by position";
         j = DatabaseWrapper.getConnection();
         qry = j.prepareStatement(query);
         qry.setInt(1, this.projectID);
         ResultSet rs = qry.executeQuery();
         while (rs.next()) {
            t.add(new Folio(false, rs.getInt("folio")));
         }
         toret = new Folio[t.size()];
         for (int i = 0; i < t.size(); i++) {
            toret[i] = t.get(i);
         }
         return toret;
      } finally {
         if (j != null) {
            DatabaseWrapper.closeDBConnection(j);
            DatabaseWrapper.closePreparedStatement(qry);
         } else {
            System.err.print("Attempt to close DB connection failed, connection was null" + this.getClass().getName() + "\n");
         }
      }
   }

   /**
    * Publish image parshings from this Project, so anyone else using the images will recieve the corrected parsing.
    */
   public void publishImageParsings() throws SQLException {
      String query = "select * from imagepositions where projectID=?";
      String query2 = "select * from imagepositions where folio=? and line=? and projectID=0";
      Connection j = null;
      PreparedStatement ps = null;
      PreparedStatement ps2 = null;
      try {
         j = DatabaseWrapper.getConnection();
         ps = j.prepareStatement(query);
         ps2 = j.prepareStatement(query2);
         ps.setInt(1, projectID);
         ResultSet rs = ps.executeQuery();
         while (rs.next()) {
            ps2.setInt(1, rs.getInt("folio"));
            ps2.setInt(2, rs.getInt("line"));
            ResultSet rs2 = ps2.executeQuery();
            if (rs2.next()) {
               //update the existing record
            } else {
               //insert a new Line position
            }

         }
      } finally {
         DatabaseWrapper.closeDBConnection(j);
         DatabaseWrapper.closePreparedStatement(ps);
         DatabaseWrapper.closePreparedStatement(ps2);
      }
   }

   /**
    * Update the parsing of an image all at once.
    */
   public void update(int[] linePositions, int[] linePositions2, int[] linePositions3, int[] linePositions4, int pagenumber) {
      // Arrays.sort(linePositions);
      Line[] newLines = new Line[linePositions.length];
      for (int i = 0; i < linePositions.length; i++) {
         Line tmp = new Line(0, 0, 0, 0);
         tmp.top = linePositions[i];
         tmp.bottom = linePositions4[i];
         tmp.left = linePositions2[i];
         tmp.right = linePositions3[i];
         newLines[i] = tmp;
      }

      Connection j = null;
      PreparedStatement stmt = null;
      try {
         j = DatabaseWrapper.getConnection();
         stmt = j.prepareStatement("Delete from projectimagepositions where folio=? and project=?");
         stmt.setInt(1, pagenumber);
         stmt.setInt(2, projectID);
         stmt.execute();
         stmt = j.prepareStatement("Insert into projectimagepositions (folio,line,top,bottom,colstart,width,project, linebreakSymbol) values (?,?,?,?,?,?,?,?)");
         for (int i = 0; i < linePositions.length; i++) {
            stmt.setInt(1, pagenumber);
            stmt.setInt(2, i + 1);
            stmt.setInt(3, newLines[i].top);
            stmt.setInt(4, newLines[i].bottom);
            stmt.setInt(5, newLines[i].left);
            stmt.setInt(6, newLines[i].right);
            stmt.setInt(7, this.projectID);
            stmt.setString(8, this.linebreakSymbol);
            stmt.execute();
         }
      } catch (SQLException ex) {
         Logger.getLogger(Project.class.getName()).log(Level.SEVERE, null, ex);
      } finally {
         DatabaseWrapper.closeDBConnection(j);
         DatabaseWrapper.closePreparedStatement(stmt);
      }

   }

   /**
    * Create an array of folios for the Folio numbers in the map
    */
   public Folio[] makeFolioArray(Map<String, String[]> m) throws SQLException {
      Set<String> e = m.keySet();
      Iterator<String[]> items = m.values().iterator();
      String[] names = new String[e.size()];
      int ctr = 0;
      while (items.hasNext()) {
         names[ctr] = items.next()[0];
         ctr++;
      }
      Stack<Folio> allFolios = new Stack();
      for (int i = 0; i < m.size(); i++) {
         try {
            Folio f = new Folio(Integer.parseInt(names[i]));
            allFolios.add(f);
         } catch (NumberFormatException er) {
         }
      }
      Folio[] toret = new Folio[allFolios.size()];
      for (int i = 0; i < toret.length; i++) {
         toret[i] = allFolios.get(i);
      }
      return toret;
   }

   /**
    * Get a list of images as html checkboxes.
    */
   public String checkBoxes() throws SQLException {
      String toret = "";
      Folio[] folios = this.getFolios();
      for (int i = 0; i < folios.length; i++) {
         toret += "<input type=\"checkbox\" name=\"" + folios[i].getFolioNumber() + "\" id=\"" + folios[i].getFolioNumber() + "\" checked value=\"" + folios[i].getFolioNumber() + "\"/>" + folios[i].getImageName() + "<br>\n";
      }
      return toret;
   }

   /**
    * Store the list of folios for the Project
    */
   public void setFolios(Folio[] f, int project) throws SQLException {
      Connection j = null;
      PreparedStatement qry = null;
      try {
         String query = "insert into projectFolios (project, folio, position) values(?,?,?)";
         String query2 = "Delete from projectFolios where project=?";
         j = DatabaseWrapper.getConnection();
         qry = j.prepareStatement(query2);
         qry.setInt(1, project);
         qry.execute();
         qry = j.prepareStatement(query);
         qry.setInt(1, project);
         for (int i = 0; i < f.length; i++) {
            if (f[i] != null) {
               qry.setInt(2, f[i].getFolioNumber());
               qry.setInt(3, i + 1);
               qry.execute();
            }
         }
      } finally {
         if (j != null) {
            DatabaseWrapper.closeDBConnection(j);
            DatabaseWrapper.closePreparedStatement(qry);
         } else {
            System.err.print("Attempt to close DB connection failed, connection was null" + this.getClass().getName() + "\n");
         }
      }
   }

   /**
    * Number of images in this project
    */
   int folioCount() throws SQLException {
      Connection j = null;
      PreparedStatement qry = null;
      try {
         String query = "select count(folio) from projectFolios where project=?";
         j = DatabaseWrapper.getConnection();
         qry = j.prepareStatement(query);

         return 0;
      } finally {
         if (j != null) {
            DatabaseWrapper.closeDBConnection(j);
            DatabaseWrapper.closePreparedStatement(qry);
         } else {
            System.err.print("Attempt to close DB connection failed, connection was null" + this.getClass().getName() + "\n");
         }
      }
   }

   /**
    * Return a list of Project members with the number of lines transcribed by each
    */
   public String getProgress() throws SQLException {
      String toret = "";
      User[] users = new Group(this.groupID).getMembers();
      String query = "select count(id) from transcription where projectID=? and creator=? and text!=''";
      Connection j = null;
      PreparedStatement ps = null;
      try {
         j = DatabaseWrapper.getConnection();
         ps = j.prepareStatement(query);
         for (int i = 0; i < users.length; i++) {
            ps.setInt(1, projectID);
            ps.setInt(2, users[i].getUID());
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
               toret += "<span class=\"progress\">" + users[i].getFname() + " " + users[i].getLname() + " " + rs.getInt(1) + "<br></span>";
            }
         }
      } finally {
         DatabaseWrapper.closeDBConnection(j);
         DatabaseWrapper.closePreparedStatement(ps);
      }

      return toret;
   }

   /**
    * Return a count of lines transcribed
    */
   public int getNumberOfTranscribedLines() throws SQLException {
      int toret = 0;
      String query = "select count(id) from transcription where projectID=? and text!=''";
      Connection j = null;
      PreparedStatement ps = null;
      try {
         j = DatabaseWrapper.getConnection();
         ps = j.prepareStatement(query);
         ps.setInt(1, projectID);
         ResultSet rs = ps.executeQuery();
         if (rs.next()) {
            toret++;
         }
      } finally {
         DatabaseWrapper.closeDBConnection(j);
         DatabaseWrapper.closePreparedStatement(ps);
      }
      return toret;
   }

   public void setGroupID(int id) {
      groupID = id;
   }

   public int getGroupID() {
      return groupID;
   }

   /**
    * Check for tags used in the transcription that arent part of the schema. This is can be used to warn
    * users during pipeline export that they are including unsupported tags without expecting the document
    * to be fully schema compliant.
    */
   public String[] hasTagsOutsideSchema() throws SQLException, MalformedURLException, IOException {
      Stack<String> badTags = new Stack();
      String[][] res = TagButton.getTagsFromSchema(this, new ArrayList());
      String[] usedTags = new TagFilter(Manuscript.getFullDocument(this, Boolean.FALSE)).getTags();
      for (int i = 0; i < usedTags.length; i++) {
         Boolean found = false;
         for (int j = 0; j < res.length; j++) {
            if (usedTags[i].compareTo(res[j][0]) == 0) {
               found = true;
            }
         }
         if (!found) {
            badTags.push(usedTags[i]);
         }
      }
      String[] toret = new String[badTags.size()];
      for (int i = 0; i < toret.length; i++) {
         toret[i] = badTags.pop();
      }
      return toret;
   }

   /**
    * Return the partner Project, or null if the record couldnt be located
    */
   public PartnerProject getAssociatedPartnerProject() throws SQLException {
      String query = "select partner from project where id=?";
      Connection j = null;
      PreparedStatement ps = null;
      try {
         j = DatabaseWrapper.getConnection();
         ps = j.prepareStatement(query);
         ps.setInt(1, projectID);
         ResultSet rs = ps.executeQuery();
         if (rs.next() && rs.getInt(1) > 0) {
            return new PartnerProject(rs.getInt(1));
         }
         return null;
      } finally {
         DatabaseWrapper.closeDBConnection(j);
         DatabaseWrapper.closePreparedStatement(ps);
      }
   }

   /**
    * Associate this project with a project pipeline. This changes the character hotkeys, buttons, and
    * schema and adds a user to this project.
    */
   public void setAssociatedPartnerProject(int id) throws SQLException {
      String query = "update project set partner=? where id=?";
      Connection j = null;
      PreparedStatement ps = null;
      try {
         j = DatabaseWrapper.getConnection();
         ps = j.prepareStatement(query);
         ps.setInt(1, id);
         ps.setInt(2, projectID);
         ps.execute();
      } finally {
         DatabaseWrapper.closeDBConnection(j);
         DatabaseWrapper.closePreparedStatement(ps);
      }
   }

   /**
    * Returns all projects connected to a partner Project, or null if none
    */
   public static Project[] getAllAssociatedProjects(int id) throws SQLException {
      Project[] all;
      String query = "select id from project where partner=? ";
      Connection j = null;
      PreparedStatement ps = null;
      Stack<Project> tmp = new Stack();
      try {
         j = DatabaseWrapper.getConnection();
         ps = j.prepareStatement(query);
         ps.setInt(1, id);
         ResultSet rs = ps.executeQuery();
         while (rs.next()) {
            tmp.push(new Project(rs.getInt(1)));
         }
      } finally {
         DatabaseWrapper.closeDBConnection(j);
         DatabaseWrapper.closePreparedStatement(ps);
      }
      all = new Project[tmp.size()];
      //odd looking way of doing this copy, I know, but it was convenient
      while (!tmp.empty()) {
         all[tmp.size() - 1] = tmp.pop();
      }
      return all;
   }

   /**
    * Returns all projects updated in the last 2 months, or null if none
    */
   public static Project[] getAllActiveProjects() throws SQLException {
      Project[] active;
      String query = "SELECT DISTINCT projectid FROM transcription WHERE DATE > ( NOW( ) + INTERVAL -2 MONTH )";
      Connection j = null;
      PreparedStatement ps = null;
      Stack<Project> tmp = new Stack();
      try {
         j = DatabaseWrapper.getConnection();
         ps = j.prepareStatement(query);
         ResultSet rs = ps.executeQuery();
         while (rs.next()) {
            tmp.push(new Project(rs.getInt(1)));
         }
      } finally {
         DatabaseWrapper.closeDBConnection(j);
         DatabaseWrapper.closePreparedStatement(ps);
      }
      active = new Project[tmp.size()];
      //odd looking way of doing this copy, I know, but it was convenient
      while (!tmp.empty()) {
         active[tmp.size() - 1] = tmp.pop();
      }
      return active;
   }

   /**
    * Build a new Project with the values stored herein, useful when using this class as a bean
    */
   public int build() {
      try {
         int toret = new Project(projectName, groupID).projectID;
         return toret;
      } catch (SQLException ex) {
         return 0;
      }
   }

   /**
    * Delete the Project information for this Project. Does not delete the underlying data, as Im sure the
    * next request Ill get is to undelete something...returns false if it failed to delete for some reason.
    */
   public Boolean delete() throws SQLException {
      if (this.projectID > 0) {
         String query = "delete from project where id=?";
         Connection j = null;
         PreparedStatement qry = null;
         try {
            j = DatabaseWrapper.getConnection();
            qry = j.prepareStatement(query);
            qry.setInt(1, projectID);

            qry.execute();
            return true;
         } finally {
            if (j != null) {
               DatabaseWrapper.closeDBConnection(j);
            }
            DatabaseWrapper.closePreparedStatement(qry);
         }
      }
      return false;
   }

   /**
    * @Depricated use the constructor instead
    */
   public void fetch() throws SQLException {
      if (this.projectID != 0) {
         Connection j = null;
         PreparedStatement qry = null;
         try {
            j = DatabaseWrapper.getConnection();
            qry = j.prepareStatement("select * from project where id=?");
            qry.setInt(1, projectID);
            ResultSet rs = qry.executeQuery();
            if (rs.next()) {
               projectName = rs.getString("name");
               groupID = rs.getInt("grp");
            }
         } finally {
            if (j != null) {
               DatabaseWrapper.closeDBConnection(j);
               DatabaseWrapper.closePreparedStatement(qry);
            } else {
               System.err.print("Attempt to close DB connection failed, connection was null" + this.getClass().getName() + "\n");
            }
         }
      }
   }

   /**
    * Add a single page to this Project
    */
   private void addPage(int folio) throws SQLException {
      Folio[] f = this.getFolios();
      Folio[] newFolios = new Folio[f.length + 1];
      for (int i = 0; i < f.length; i++) {
         newFolios[i] = f[i];

      }
      newFolios[newFolios.length - 1] = new Folio(folio);
      this.setFolios(newFolios, projectID);

   }

   /**
    * Remove a single page from this Project
    */
   private void removePage(int folio) throws SQLException {
      Folio[] f = this.getFolios();
      for (int i = 0; i < f.length; i++) {
         if (f[i].getFolioNumber() == folio) {
            Folio[] newFolios = new Folio[f.length - 1];
            for (int j = 0; j < f.length; j++) {
               if (j > i) {
                  newFolios[j] = f[j + 1];
               } else {
                  if (j == i) {
                     //do nothing
                  } else {
                     newFolios[j] = f[j];
                  }
               }
               this.setFolios(newFolios, projectID);
            }
         }
      }

   }

   /**
    * returns -1 if an error occurs, probably due to deletion or the lack of folios.
    */
   public int firstPage() throws SQLException {
      // @TODO:  If there're no folios, this throws an ArrayIndexOutOfBoundsException.
      try {
         return getFolios()[0].getFolioNumber();
      } catch (NullPointerException | ArrayIndexOutOfBoundsException ex) {
         LOG.log(Level.SEVERE, "Error loading first page for project " + projectID, ex);
         return -1;
      }
   }

   /**
    * Get the unique identifier of the next page image to be displayed according to the sequence being used
    * by this Project
    */
   public int getFollowingPage(int current) throws SQLException {
      Folio[] fols = this.getFolios();
      for (int i = 0; i < fols.length; i++) {
         if (fols[i].getFolioNumber() == current && i + 1 < fols.length) {
            return (fols[i + 1].folioNumber);
         }
      }
      return 0;
   }

   /**
    * Get the unique identifier of the previous page image according to the sequence being used by this Project
    */
   public int getPreceedingPage(int current) throws SQLException {
      Folio[] fols = this.getFolios();
      for (int i = 0; i < fols.length; i++) {
         if (fols[i].getFolioNumber() == current && i != 0) {
            return (fols[i - 1].folioNumber);
         }
      }
      return 0;
   }

   /**
    * Build OAC annotations our of the lines transcription
    */
   public String getOAC(int folioNumber) throws SQLException, IOException {
      Model model = ModelFactory.createDefaultModel();
      Transcription[] transcriptions = Transcription.getProjectTranscriptions(projectID, folioNumber);
      for (int i = 0; i < transcriptions.length; i++) {
         StringReader r = new StringReader(transcriptions[i].getAsOAC());
         model.read(r, "");

      }
      StringWriter w = new StringWriter();
      model.write(w, "N3");

      return w.toString();
   }

   /**
    * @Depricated this code needs to be updated to build a sequence after the changes to OAC
    */
   public String getOACSequence() throws SQLException {
      String toret = "";
      Model model = ModelFactory.createDefaultModel();
      model.setNsPrefix("dms", "http://dms.stanford.edu/ns/");
      model.setNsPrefix("oac", "http://www.openannotation.org/ns/");
      model.setNsPrefix("rdf", "http://www.w3.org/1999/02/22-rdf-syntax-ns#");
      model.setNsPrefix("ore", "http://www.openarchives.org/ore/terms/");
      model.setNsPrefix("cnt", "http://www.w3.org/2008/content#");
      model.setNsPrefix("dc", "http://purl.org/dc/elements/1.1/");
      model.setNsPrefix("dcterms", "http://purl.org/dc/terms/");

      Folio[] fols = this.getFolios();
      Resource sequence = model.createResource("http://t-pen.org/sequences/");
      Resource DMSview = model.createResource("http://dms.stanford.edu/ns/View");
      Property rdfType = model.createProperty("http://www.w3.org/1999/02/22-rdf-syntax-ns#", "type");
      RDFList l = model.createList(new RDFNode[]{sequence});
      Property aggregates = model.createProperty("http://www.openarchives.org/ore/terms/", "aggregates");
      for (int i = 0; i < fols.length; i++) {
         Resource view = model.createResource("http://t-pen.org/views/" + fols[i].folioNumber);
         view.addProperty(rdfType, view);
         sequence.addProperty(aggregates, view);
         l.add(view);
      }
      StringWriter tmp = new StringWriter();
      model.write(tmp);
      return tmp.getBuffer().toString();
   }

   /**
    * Add a new Project log comment
    */
   public void addLogEntry(String text, int uid) throws SQLException {
      String query = "insert into projectLog(projectID, uid, content) values(?,?,?)";
      Connection j = null;
      PreparedStatement qry = null;
      String deleteQuery = "delete from projectLog where content=? and uid=? and projectID=?";
      try {
         j = DatabaseWrapper.getConnection();
         qry = j.prepareStatement(deleteQuery);
         qry.setInt(3, projectID);
         qry.setInt(2, uid);
         qry.setString(1, text);
         qry.execute();
      } finally {
         DatabaseWrapper.closeDBConnection(j);
         DatabaseWrapper.closePreparedStatement(qry);
      }
      try {
         j = DatabaseWrapper.getConnection();
         qry = j.prepareStatement(query);
         qry.setInt(1, projectID);
         qry.setInt(2, uid);
         qry.setString(3, text);
         qry.execute();
      } finally {
         DatabaseWrapper.closeDBConnection(j);
         DatabaseWrapper.closePreparedStatement(qry);
      }
   }

   /**
    * Retrieve the log of Project comments in order by date.
    */
   public String getProjectLog() throws SQLException {
      String query = "select content,creationDate,uid from projectLog where projectID=? order by creationDate desc limit 1000";
      StringBuffer toret = new StringBuffer("");
      Connection j = null;
      PreparedStatement qry = null;
      try {
         j = DatabaseWrapper.getConnection();
         qry = j.prepareStatement(query);
         qry.setInt(1, projectID);
         ResultSet rs = qry.executeQuery();
         while (rs.next()) {
            User u = new User(rs.getInt("uid"));
            SimpleDateFormat d = new SimpleDateFormat();
            toret.append("<div class=\"logEntry\"><div class=\"logDate\">" + d.format(rs.getTimestamp("creationDate")) + "</div>");
            toret.append("<div class=\"logAuthor\">" + ESAPI.encoder().encodeForHTML(u.getFname() + " " + u.getLname()) + "</div>");
            //removed esapi encoding 
            toret.append("<div class=\"logContent\">" + (rs.getString("content")) + "</div></div>");
         }
      } finally {
         DatabaseWrapper.closeDBConnection(j);
         DatabaseWrapper.closePreparedStatement(qry);
      }
      return toret.toString();
   }

   /**
    * Retrieve the requested number of Project comments in order by date, from date.
    */
   public String getProjectLog(int recordCount, int firstRecord) throws SQLException {
      String query = "select content,creationDate,uid from projectLog where projectID=? order by creationDate desc limit ?,?";
      StringBuilder toret = new StringBuilder("");
      Connection j = null;
      PreparedStatement qry = null;
      try {
         j = DatabaseWrapper.getConnection();
         qry = j.prepareStatement(query);
         qry.setInt(1, projectID);
         qry.setInt(2, firstRecord);
         qry.setInt(3, recordCount);
         ResultSet rs = qry.executeQuery();
         while (rs.next()) {
            User u = new User(rs.getInt("uid"));
            SimpleDateFormat d = new SimpleDateFormat();
            toret.append("<div class=\"logEntry\"><div class=\"logDate\">" + d.format(rs.getTimestamp("creationDate")) + "</div>");
            toret.append("<div class=\"logAuthor\">" + ESAPI.encoder().encodeForHTML(u.getFname() + " " + u.getLname()) + "</div>");
            //removed esapi encoding to allow links in entries
            toret.append("<div class=\"logContent\">" + (rs.getString("content")) + "</div></div>");
         }
      } finally {
         if (j != null) {
            DatabaseWrapper.closeDBConnection(j);
         }
         DatabaseWrapper.closePreparedStatement(qry);
      }
      return toret.toString();
   }

   /**
    * Retrieve the requested number of Project comments in order by date.
    */
   public String getProjectLog(int recordCount) throws SQLException {
      return getProjectLog(recordCount, 0);
   }

   public String getSchemaURL() throws SQLException {
      String toret = "";
      Connection j = null;
      PreparedStatement ps = null;
      try {
         j = DatabaseWrapper.getConnection();
         ps = j.prepareStatement("select schemaURL from project where id=?");
         ps.setInt(1, this.projectID);
         ResultSet rs = ps.executeQuery();
         if (rs.next()) {
            return rs.getString("schemaURL");
         }
      } finally {
         DatabaseWrapper.closeDBConnection(j);
         DatabaseWrapper.closePreparedStatement(ps);

      }
      return toret;
   }

   /**
    * Returns true if the url was successfully updated
    */
   public Boolean setSchemaURL(String newURL) throws SQLException {
      try {
         //if it isnt a valid url, an exception gets thrown by the following
         URL schemaURL = new URL(newURL);
         Connection j = null;
         PreparedStatement ps = null;
         try {
            j = DatabaseWrapper.getConnection();
            ps = j.prepareStatement("update project set schemaURL=? where id=?");
            ps.setInt(2, this.projectID);
            ps.setString(1, newURL);
            ps.execute();

         } finally {
            DatabaseWrapper.closeDBConnection(j);
            DatabaseWrapper.closePreparedStatement(ps);
         }
         return true;
      } catch (MalformedURLException ex) {
         return false;
      }
   }
   
   private static final Logger LOG = Logger.getLogger(Project.class.getName());
}
