/*
 * Copyright 2011-2013 Saint Louis University. Licensed under the
 * Educational Community License, Version 2.0 (the "License"); you may
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

import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.transform.stream.StreamSource;
import net.sf.saxon.s9api.Processor;
import net.sf.saxon.s9api.SaxonApiException;
import net.sf.saxon.s9api.Serializer;
import net.sf.saxon.s9api.XdmNode;
import net.sf.saxon.s9api.XsltCompiler;
import net.sf.saxon.s9api.XsltExecutable;
import net.sf.saxon.s9api.XsltTransformer;
import org.apache.commons.lang.StringEscapeUtils;
import org.owasp.esapi.ESAPI;


/**
 * A button for inserting an XML tag into a transcription
 */
public class TagButton {

   int projectID = -1;
   int uid = -1;
   int position;
   String tag;
   String[] parameters;

   /**
    * Get the description for the tag, used to label the tag button
    *
    * @return the tag description, or the tag text if the description hasn't been set
    */
   public String getDescription() {
      return StringEscapeUtils.escapeHtml(description.length() > 0 ? description : tag);
   }
   String description;

   public String getXMLColor() {
      if (xmlColor.length() > 0) {
         return xmlColor;
      } else {
         return "";
      }
   }

   public void updateXMLColor(String color) throws SQLException {
      String query = "update buttons set color=? where project=? and position=?";
      Connection j = null;
      PreparedStatement ps = null;
      try {
         j = DatabaseWrapper.getConnection();
         ps = j.prepareStatement(query);
         ps.setString(1, color);
         ps.setInt(1, projectID);
         ps.setInt(2, position);
         ps.execute();
         this.xmlColor = color;
      } finally {
         DatabaseWrapper.closeDBConnection(j);
         DatabaseWrapper.closePreparedStatement(ps);
      }

   }
   String xmlColor;

   /**
    * Add a new button, tag is the tag name only, no brackets
    */
   public TagButton(int uid, int position, String tag, String description) throws SQLException {
      Connection j = null;
      PreparedStatement stmt = null;
      try {
         String query = "insert into buttons(uid,position,text,description) values (?,?,?,?)";
         j = DatabaseWrapper.getConnection();
         stmt = j.prepareStatement(query);
         stmt.setString(3, tag);
         stmt.setInt(1, uid);
         stmt.setInt(2, position);
         stmt.setString(3, tag);
         stmt.setString(4, description);
         stmt.execute();

         this.tag = tag;
         this.position = position;
         this.uid = uid;
         this.xmlColor = "";
      } finally {
         DatabaseWrapper.closeDBConnection(j);
         DatabaseWrapper.closePreparedStatement(stmt);
      }
   }

   /**
    * Add a new button, tag is the tag name only, no brackets
    */
   public TagButton(int projectID, int position, String tag, Boolean project, String description) throws SQLException {
      Connection j = null;
      PreparedStatement stmt = null;
      try {
         TagButton t = new TagButton(projectID, position, true);
         t.deleteTag();
      } catch (Exception e) {
         LOG.log(Level.SEVERE, null, e);
      }
      try {
         String query = "insert into projectButtons(project,position,text,description) values (?,?,?,?)";
         j = DatabaseWrapper.getConnection();
         stmt = j.prepareStatement(query);
         stmt.setString(3, tag);
         stmt.setInt(1, projectID);
         stmt.setInt(2, position);
         stmt.setString(3, tag);
         stmt.setString(4, description);
         stmt.execute();

         this.tag = tag;
         this.position = position;
         this.uid = -1;
         this.projectID = projectID;
         this.xmlColor = "";
      } catch (Exception e) {
         LOG.log(Level.SEVERE, null, e);
      } finally {
         DatabaseWrapper.closeDBConnection(j);
         DatabaseWrapper.closePreparedStatement(stmt);
      }
   }

   /**
    * Add a new button, tag is the tag name only, no brackets. params needs to have a length of 5
    */
   public TagButton(int uid, int position, String tag, String[] params) throws SQLException {
      Connection j = null;
      PreparedStatement stmt = null;
      try {
         String query = "insert into buttons(uid,position,text,param1, param2, param3, param4, param5) values (?,?,?,?,?,?,?,?)";
         j = DatabaseWrapper.getConnection();
         stmt = j.prepareStatement(query);
         stmt.setString(3, tag);
         stmt.setInt(1, uid);
         stmt.setInt(2, position);
         stmt.setString(3, params[0]);
         stmt.setString(4, params[1]);
         stmt.setString(5, params[2]);
         stmt.setString(6, params[3]);
         stmt.setString(7, params[4]);
         stmt.execute();
         for (int i = 0; i < params.length; i++) {
            if (params[i] == null || params[i].contains("null")) {
               params[i] = "";
            }
         }
         this.tag = tag;
         this.position = position;
         this.uid = uid;
         this.xmlColor = "";
      } finally {
         DatabaseWrapper.closeDBConnection(j);
         DatabaseWrapper.closePreparedStatement(stmt);
      }
   }

   /**
    * Add a new button, tag is the tag name only, no brackets. params needs to have a length of 5
    */
   public TagButton(int projectID, int position, String tag, String[] params, Boolean isProject, String description) throws SQLException {
      Connection j = null;
      PreparedStatement stmt = null;
      try {
         //delete any existing tag in that position
         try {
            TagButton t = new TagButton(projectID, position, true);
            t.deleteTag();
         } catch (Exception e) {
            LOG.log(Level.SEVERE, null, e);
         }
         for (int i = 0; i < params.length; i++) {
            if (params[i] == null || params[i].contains("null")) {
               params[i] = "";
            }
         }
         String query = "insert into projectButtons(project,position,text,param1, param2, param3, param4, param5, description) values (?,?,?,?,?,?,?,?,?)";
         j = DatabaseWrapper.getConnection();
         stmt = j.prepareStatement(query);
         stmt.setString(3, tag);
         stmt.setInt(1, projectID);
         stmt.setInt(2, position);
         stmt.setString(3, tag);
         stmt.setString(4, params[0]);
         stmt.setString(5, params[1]);
         stmt.setString(6, params[2]);
         stmt.setString(7, params[3]);
         stmt.setString(8, params[4]);
         stmt.setString(9, description);
         stmt.execute();

         this.tag = tag;
         this.position = position;
         this.uid = -1;
         this.projectID = projectID;
         this.xmlColor = "";
      } finally {
         DatabaseWrapper.closeDBConnection(j);
         DatabaseWrapper.closePreparedStatement(stmt);
      }
   }

   /**
    * Retrieve and existing button
    */
   public TagButton(int uid, int position) throws SQLException {
      Connection j = null;
      PreparedStatement stmt = null;
      try {
         String query = "select * from buttons where uid=? and position=?";
         j = DatabaseWrapper.getConnection();
         stmt = j.prepareStatement(query);
         stmt.setInt(1, uid);
         stmt.setInt(2, position);
         ResultSet rs = stmt.executeQuery();
         if (rs.next()) {
            this.uid = uid;
            this.position = position;
            this.tag = rs.getString("text");
            this.description = rs.getString("description");
            this.xmlColor = rs.getString("color");
            if (rs.getString("param1").length() > 0) {
               parameters = new String[5];

               parameters[0] = ESAPI.encoder().encodeForHTML(rs.getString("param1"));
               if (!parameters[0].contains("&quot;")) {
                  parameters[0] += "=&quot;&quot;";
               }
               if (rs.getString("param2").length() > 0) {
                  parameters[1] = ESAPI.encoder().encodeForHTML(rs.getString("param2"));
                  if (!parameters[1].contains("&quot;")) {
                     parameters[1] += "=&quot;&quot;";
                  }
               }
               if (rs.getString("param3").length() > 0) {
                  parameters[2] = ESAPI.encoder().encodeForHTML(rs.getString("param3"));
                  if (!parameters[2].contains("&quot;")) {
                     parameters[2] += "=&quot;&quot;";
                  }
               }
               if (rs.getString("param4").length() > 0) {
                  parameters[3] = ESAPI.encoder().encodeForHTML(rs.getString("param4"));
                  if (!parameters[3].contains("&quot;")) {
                     parameters[3] += "=&quot;&quot;";
                  }
               }
               if (rs.getString("param5").length() > 0) {
                  parameters[4] = ESAPI.encoder().encodeForHTML(rs.getString("param5"));
                  if (!parameters[4].contains("&quot;")) {
                     parameters[4] += "=&quot;&quot;";
                  }
               }
            }
         } else {
            this.uid = 0;
            this.position = 0;
            this.tag = "";

         }
      } finally {
         DatabaseWrapper.closeDBConnection(j);
         DatabaseWrapper.closePreparedStatement(stmt);
      }
   }

   /**
    * Retrieve and existing button
    */
   public TagButton(int projectID, int position, Boolean isProject) throws SQLException {
      Connection j = null;
      PreparedStatement stmt = null;
      try {
         String query = "select * from projectButtons where project=? and position=?";
         j = DatabaseWrapper.getConnection();
         stmt = j.prepareStatement(query);
         stmt.setInt(1, projectID);
         stmt.setInt(2, position);
         ResultSet rs = stmt.executeQuery();
         if (rs.next()) {
            this.uid = -1;
            this.projectID = projectID;
            this.position = position;
            this.tag = rs.getString("text");
            this.xmlColor = rs.getString("color");
            this.description = rs.getString("description");
            if (rs.getString("param1").length() > 0) {
               parameters = new String[5];

               parameters[0] = ESAPI.encoder().encodeForHTML(rs.getString("param1"));
               if (!parameters[0].contains("&quot;")) {
                  parameters[0] += "=&quot;&quot;";
               }
               if (rs.getString("param2").length() > 0) {
                  parameters[1] = ESAPI.encoder().encodeForHTML(rs.getString("param2"));
                  if (!parameters[1].contains("&quot;")) {
                     parameters[1] += "=&quot;&quot;";
                  }
               }
               if (rs.getString("param3").length() > 0) {
                  parameters[2] = ESAPI.encoder().encodeForHTML(rs.getString("param3"));
                  if (!parameters[2].contains("&quot;")) {
                     parameters[2] += "=&quot;&quot;";
                  }
               }
               if (rs.getString("param4").length() > 0) {
                  parameters[3] = ESAPI.encoder().encodeForHTML(rs.getString("param4"));
                  if (!parameters[3].contains("&quot;")) {
                     parameters[3] += "=&quot;&quot;";
                  }
               }
               if (rs.getString("param5").length() > 0) {
                  parameters[4] = ESAPI.encoder().encodeForHTML(rs.getString("param5"));
                  if (!parameters[4].contains("&quot;")) {
                     parameters[4] += "=&quot;&quot;";
                  }
               }
            }
         } else {
            this.uid = 0;
            this.position = 0;
            this.tag = "";
         }
      } finally {
         DatabaseWrapper.closeDBConnection(j);
         DatabaseWrapper.closePreparedStatement(stmt);
      }
   }

   /**
    * Set the parameters for this button.
    */
   public void updateParameters(String[] parameters) throws SQLException {
      Connection j = null;
      PreparedStatement stmt = null;
      try {
         this.parameters = parameters;
         String query;
         if (this.projectID > 0) {
            query = "update projectButtons set param1=?, param2=?, param3=?, param4=?, param5=? where project=? and position=?";
         } else {
            query = "update buttons set param1=?, param2=?, param3=?, param4=?, param5=? where uid=? and position=?";
         }
         for (int i = 0; i < parameters.length; i++) {
            if (parameters[i] == null || parameters[i].contains("null")) {
               parameters[i] = "";
            }
         }
         j = DatabaseWrapper.getConnection();
         stmt = j.prepareStatement(query);
         stmt.setString(1, parameters[0]);
         stmt.setString(2, parameters[1]);
         stmt.setString(3, parameters[2]);
         stmt.setString(4, parameters[3]);
         stmt.setString(5, parameters[4]);
         if (projectID > 0) {
            stmt.setInt(6, projectID);
         } else {
            stmt.setInt(6, uid);
         }
         stmt.setInt(7, position);
         stmt.execute();
         //if none of the parameters had content, make sure hasParameters will return false...
         Boolean noParams = true;
         for (int i = 0; i < parameters.length; i++) {
            if (parameters[i].length() > 0) {
               parameters[i] = parameters[i] + "=&quot;&quot;";
               noParams = false;
            }
         }
         if (noParams) {
            this.parameters = null;
         }
      } finally {
         DatabaseWrapper.closeDBConnection(j);
         DatabaseWrapper.closePreparedStatement(stmt);
      }

   }

   /**
    * Return the parameters as strings. They will contain quotes and an equal sign
    *
    * @return array of 5 parameters
    */
   public String[] getparameters() {
      return parameters;
   }

   /**
    * Are there stored parameters for this button? Useful weh deciding the editing layout
    */
   public Boolean hasParameters() {
      if (parameters != null) {
         return true;
      }
      return false;

   }

   /**
    * Check whether the object populated properly
    *
    * @return false if there was a problem in populating the object previously
    */
   public boolean exists() {
      if (uid == 0 && projectID <= 0) {
         return false;
      }
      return true;
   }

   /**
    * Alter the position of an existing tag.
    */
   public void updatePosition(int newPos) throws SQLException {
      Connection j = null;
      PreparedStatement ps = null;
      try {
         String query = "UPDATE buttons set position=? where uid=? and position=?";
         if (projectID > 0) {
            query = "UPDATE projectButtons set position=? where project=? and position=?";
         }

         j = DatabaseWrapper.getConnection();
         ps = j.prepareStatement(query);
         ps.setInt(1, newPos);
         ps.setInt(3, position);
         if (projectID > 0) {
            ps.setInt(2, projectID);
         } else {
            ps.setInt(2, uid);
         }
      } finally {
         DatabaseWrapper.closeDBConnection(j);
         DatabaseWrapper.closePreparedStatement(ps);
      }
   }

   /**
    * Update the tag label of an existing button.
    */
   public void updateTag(String newTag) throws SQLException {
      Connection j = null;
      PreparedStatement stmt = null;
      try {
         String query;
         if (projectID > 0) {
            query = "update projectButtons set text=? where project=? and position=?";
         } else {
            query = "update buttons set text=? where uid=? and position=?";
         }

         j = DatabaseWrapper.getConnection();
         stmt = j.prepareStatement(query);
         stmt.setString(1, newTag);
         if (projectID > 0) {
            stmt.setInt(2, projectID);
         } else {
            stmt.setInt(2, uid);
         }
         stmt.setInt(3, position);
         stmt.execute();
         tag = newTag;
      } finally {
         DatabaseWrapper.closeDBConnection(j);
         DatabaseWrapper.closePreparedStatement(stmt);
      }
   }

   /**
    * The actual button markup
    */
   public String getButton() {
      Date date = new Date(System.currentTimeMillis());
      StackTraceElement[] t = Thread.currentThread().getStackTrace();
      DateFormat formatter = new SimpleDateFormat("yyyy-mm-dd H:M:S");
      formatter.format(date);
      String stackTrace = "";
      Boolean caller = false;
      for (int i = 0; i < t.length; i++) {
         stackTrace += t[i].toString() + "\n";
         if (t[i].toString().contains("getAllProjectButtons")) {
            caller = true;
         }
      }
      if (!caller) {
         LOG.log(Level.SEVERE, "{0} Running tagButton.getAllProjectButtons\n{1}", new Object[]{formatter.format(date), stackTrace});
      }
      return "<span class=\"lookLikeButtons\" title=\"" + getFullTag() + "\" onclick=\"Interaction.insertTag('" + tag + "', '" + getFullTag() + "');\">" + getDescription() + "</span>";
   }

   /**
    * return oepning and closing tag including brackets
    */
   public String getFullTag() {

      if (parameters != null && parameters.length == 5) {
         String toret = "<" + tag;
         if (parameters[0] != null) {
            toret += " " + parameters[0];
         }
         if (parameters[1] != null) {
            toret += " " + parameters[1];
         }
         if (parameters[2] != null) {
            toret += " " + parameters[2];
         }
         if (parameters[3] != null) {
            toret += " " + parameters[3];
         }
         if (parameters[4] != null) {
            toret += " " + parameters[4];
         }


         toret += ">";//+ "</" + tag + ">";
         return toret;
      } else {
         return "<" + tag + ">";//+ "</" + tag + ">";
      }
   }

   /**
    * get the tag text without brackets or parameters
    *
    * @return tag text
    */
   public String getTag() {
      if (tag != null) {
         return tag;
      } else {
         return "";
      }
   }

   /**
    * get all of the buttons the user has created, create a few dummy ones if they dont have any
    */
   public static String getAllButtons(int uid) throws SQLException {
      Connection j = null;
      PreparedStatement stmt = null;
      try {
         String toret = "";
         String query = "select * from buttons where uid=?";
         j = DatabaseWrapper.getConnection();
         stmt = j.prepareStatement(query);
         stmt.setInt(1, uid);
         ResultSet rs = stmt.executeQuery();
         int ctr = 0;
         while (rs.next()) {
            int position = rs.getInt("position");
            TagButton b = new TagButton(uid, position);
            toret += b.getButton();
            ctr++;
         }
         if (ctr == 0) {
            TagButton b = new TagButton(uid, 1, "temp", "button description");
            b = new TagButton(uid, 2, "temp", "button description");
            b = new TagButton(uid, 3, "temp", "button description");
            b = new TagButton(uid, 4, "temp", "button description");
         }
         return toret;
      } finally {
         DatabaseWrapper.closeDBConnection(j);
         DatabaseWrapper.closePreparedStatement(stmt);
      }
   }

   /**
    * get all of the buttons for this Project
    */
   public static String getAllProjectButtons(int projectID) throws SQLException {
      Date date = new Date(System.currentTimeMillis());
      StackTraceElement[] t = Thread.currentThread().getStackTrace();
      DateFormat formatter = new SimpleDateFormat("yyyy-mm-dd H:M:S");
      formatter.format(date);
      String stackTrace = "";
      for (int i = 0; i < t.length; i++) {
         stackTrace += t[i].toString() + "\n";
      }

      LOG.log(Level.SEVERE, "{0} Running tagButton.getAllProjectButtons\n{1}", new Object[]{formatter.format(date), stackTrace});
      Connection j = null;
      PreparedStatement stmt = null;
      try {
         String toret = "";
         String query = "select distinct(position) from projectButtons where project=? order by position";
         j = DatabaseWrapper.getConnection();
         stmt = j.prepareStatement(query);
         stmt.setInt(1, projectID);
         ResultSet rs = stmt.executeQuery();
         int ctr = 0;
         while (rs.next()) {
            int position = rs.getInt("position");
            try {
               TagButton b = new TagButton(projectID, position, true);
               toret += b.getButton();
            } catch (NullPointerException e) {
            }
            ctr++;
         }
         if (ctr == 0) {
            TagButton b = new TagButton(projectID, 1, "temp", true, "button description");
            b = new TagButton(projectID, 2, "temp", true, "button description");
            b = new TagButton(projectID, 3, "temp", true, "button description");
            b = new TagButton(projectID, 4, "temp", true, "button description");
         }
         return toret;
      } finally {
         DatabaseWrapper.closeDBConnection(j);
         DatabaseWrapper.closePreparedStatement(stmt);
      }
   }

   /**
    * empty a Project of tags to load new tags
    */
   public void removeButtonsByProject(int projectID) throws SQLException {
      String query = "DELETE FROM projectbuttons WHERE project=?";
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

   /**
    * Change the tag description
    *
    * @param desc new description. Should be short because this is the button label.
    * @throws SQLException
    */
   public void updateDescription(String desc) throws SQLException {
      String query = "update buttons set description=? where uid=? and position=?";
      if (this.projectID > 0) {
         query = "update projectButtons set description=? where project=? and position=?";
      }
      Connection j = null;
      PreparedStatement ps = null;
      try {
         j = DatabaseWrapper.getConnection();
         ps = j.prepareStatement(query);
         if (this.projectID > 0) {
            ps.setInt(2, projectID);
         } else {
            ps.setInt(2, uid);
         }
         ps.setInt(3, position);
         ps.setString(1, desc);
         ps.execute();
      } finally {
         DatabaseWrapper.closeDBConnection(j);
         DatabaseWrapper.closePreparedStatement(ps);
      }
   }

   /**
    * Deletes the tag button
    *
    * @throws SQLException
    */
   public void deleteTag() throws SQLException {
      String query = "delete from buttons where uid=? and position=?";
      if (this.projectID > 0) {
         query = "delete from projectButtons where project=? and position=?";
      }
      Connection j = null;
      PreparedStatement ps = null;
      PreparedStatement update = null;
      String updateQuery = "update buttons set position=? where uid=? and position=?";
      if (this.projectID > 0) {
         updateQuery = "update projectButtons set position=? where project=? and position=?";
      }
      try {
         j = DatabaseWrapper.getConnection();
         ps = j.prepareStatement(query);
         if (this.projectID > 0) {
            ps.setInt(1, projectID);
         } else {
            ps.setInt(1, uid);
         }
         ps.setInt(2, position);
         ps.execute();
         //now reorder them
         update = j.prepareStatement(updateQuery);

         TagButton t;
         while (true) {
            if (this.projectID > 0) {
               t = new TagButton(projectID, position + 1, true);
            } else {
               t = new TagButton(uid, position + 1);
            }
            if (t.uid == 0 && t.projectID <= 0) {
               break;
            }
            update.setInt(1, position);
            if (projectID > 0) {
               update.setInt(2, projectID);
            } else {
               update.setInt(2, uid);
            }
            update.setInt(3, position + 1);
            update.execute();
            position++;
         }

      } finally {
         DatabaseWrapper.closeDBConnection(j);
         DatabaseWrapper.closePreparedStatement(ps);
         DatabaseWrapper.closePreparedStatement(update);
      }


   }

   /**
    * Update the text of the xml tag
    *
    * @param newTag new tag text without brackets
    * @throws SQLException
    */
   public void setTag(String newTag) throws SQLException {
      Connection j = null;
      PreparedStatement stmt = null;
      try {
         String query = "update buttons set text=? where uid=? and position=?";
         if (projectID > 0) {
            query = "update projectButtons set text=? where project=? and position=?";
         }
         j = DatabaseWrapper.getConnection();
         stmt = j.prepareStatement(query);
         stmt.setString(1, newTag);
         if (projectID > 0) {
            stmt.setInt(2, projectID);
         } else {
            stmt.setInt(2, uid);
         }
         stmt.setInt(3, position);
         stmt.execute();
         tag = newTag;
      } finally {
         DatabaseWrapper.closeDBConnection(j);
         DatabaseWrapper.closePreparedStatement(stmt);
      }
   }

   /**
    * run the schema tag extraction xsl on the schema the stream points to
    */
   public static String xslRunner(StreamSource xml) throws SaxonApiException {
      Processor proc = new Processor(false);
      XsltCompiler comp = proc.newXsltCompiler();
      XsltExecutable exp = comp.compile(new StreamSource(new File(Folio.getRbTok("XSLTLOCATION") + "schema2.xsl")));
      XdmNode source = proc.newDocumentBuilder().build(xml);
      Serializer out = new Serializer();
      StringWriter w = new StringWriter();
      out.setOutputWriter(w);
      XsltTransformer trans = exp.load();
      trans.setInitialContextNode(source);
      trans.setDestination(out);
      trans.transform();
      return w.toString();
   }

   /**
    * Returns a list of tags defined in the Project schema
    */
   public static String[][] getTagsFromSchema(Project p, List<parameterWithValueList> v) throws SQLException, MalformedURLException, IOException {
      try {

         URL schemaurl = new URL(p.getSchemaURL());
         StreamSource schemaStream = new StreamSource(schemaurl.openStream());
         String tagList = xslRunner(schemaStream);
         tagList = tagList.replaceAll("<.*?>", "").replaceAll(" ", "");
         String[] tagsAndParamData = tagList.split("\n");
         String[][] toret = new String[tagsAndParamData.length][];
         for (int i = 0; i < tagsAndParamData.length; i++) {

            String[] tmp = tagsAndParamData[i].split("/");
            toret[i] = tmp;

            for (int j = 0; j < tmp.length; j++) {
               //if there are any default parameter lists, remove that list and build the needed parameterWithValueList objects and put them into v
               if (tmp[j].contains("{")) {
                  String[] tmp2 = tmp[j].split("\\{");
                  tmp[j] = tmp2[0];
                  //tmp2[1] contaions the value list and a terminating }
                  String valueList = tmp2[1].replace("\\}", "");
                  //now split the value list, they are delimited by a single space
                  String[] values = valueList.split(",");
                  parameterWithValueList param = new parameterWithValueList(tmp[j]);
                  param.values.addAll(Arrays.asList(values));
                  v.add(param);
               }
            }
         }
         return toret;
      } catch (SaxonApiException ex) {
         Logger.getLogger(TagButton.class.getName()).log(Level.SEVERE, null, ex);
      }
      return null;
   }

   /**
    * Returns user tags
    */
   public static String[] getTagsFromUser(int uid) throws SQLException, IOException {
      Connection j = null;
      PreparedStatement stmt = null;
      try {
         String toret = "";
         String query = "select * from buttons where uid=?";
         j = DatabaseWrapper.getConnection();
         stmt = j.prepareStatement(query);
         stmt.setInt(1, uid);
         ResultSet rs = stmt.executeQuery();
         int ctr = 0;
         while (rs.next()) {
            int position = rs.getInt("position");
            TagButton b = new TagButton(uid, position);
            toret += b.getTag() + "\n";
            ctr++;
         }
         return toret.split("\n");
      } finally {
         DatabaseWrapper.closeDBConnection(j);
         DatabaseWrapper.closePreparedStatement(stmt);
      }
   }
   
   private static final Logger LOG = Logger.getLogger(TagButton.class.getName());
}
