/*
 * Copyright 2013 Saint Louis University. Licensed under the
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
 */
package edu.slu.tpen.transfer;

import java.awt.Rectangle;
import java.io.IOException;
import java.io.InputStream;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import static edu.slu.util.JsonUtils.*;
import textdisplay.Folio;
import textdisplay.Project;
import textdisplay.Transcription;
import user.User;


/**
 * Import data using plain JSON (not JSON-LD).
 *
 * @author tarkvara
 */
public class JsonImporter {
   private Project project;
   private Folio[] folios;
   private User user;

   public JsonImporter(Project proj, User u) throws SQLException {
      project = proj;
      folios = proj.getFolios();
      user = u;
   }
   
   public void update(InputStream input) throws IOException, SQLException {

      Map<String, Object> payload = new ObjectMapper().readValue(input, new TypeReference<Map<String, Object>>() {});
      Map<String, Object> manifest = getObject(payload, "manifest", true);
      List<Object> canvasses = getArray(manifest, "canvasses", true);

      if (canvasses.size() != folios.length) {
         throw new IOException(String.format("Malformed JSON input:  %d folios in T-PEN project, but %d canvasses in input.", folios.length, canvasses.size()));
      }
      int folioI = 0;
      for (Object c: canvasses) {
         if (!(c instanceof Map)) {
            throw new IOException("Malformed JSON input: canvasses entry is not an object.");
         }
         Folio f = folios[folioI++];
         Map<String, Object> canvas = (Map<String, Object>)c;
         String folioURI = getFolioURI(canvas, f);
         if (folioURI == null) {
            throw new IOException(String.format("Malformed JSON input: no image match for folio %s.", f.getImageName()));
         }

         Transcription[] transcrs = Transcription.getProjectTranscriptions(project.getProjectID(), f.getFolioNumber());

         List<Object> rows = getArray(canvas, "rows", true);
         int transcrI = 0;
         for (Object r: rows) {
            if (!(r instanceof Map)) {
               throw new IOException("Malformed JSON input: rows entry is not an object.");
            }
            if (transcrI < transcrs.length) {
               updateTranscription((Map<String, Object>)r, transcrs[transcrI++]);
            } else {
               // Newly inserted lines.
               insertTranscription((Map<String, Object>)r, f);
            }
         }
         // Delete any trailing transcription lines which no longer corresponde to rows.
         while (transcrI < transcrs.length) {
            removeTranscription(transcrs[transcrI]);
            transcrI++;
         }
         folioI++;
      }
   }

   /**
    * Given a Tradamus canvas and a T-PEN folio, get the corresponding image URI which both are using.
    *
    * @param canvData JSON object containing Tradamus canvas data
    * @param f T-PEN folio
    * @return true if they represent the same page
    */
   private static String getFolioURI(Map<String, Object> canvData, Folio f) throws IOException, SQLException {
      List<Object> images = getArray(canvData, "images", true);
      if (images == null || images.isEmpty()) {
         // This is a text-only (imageless) transcription.  T-PEN has no way of handling this.
         throw new IOException("Malformed JSON input: canvas with no images.");
      }
      
      String folioURI = Folio.getRbTok("SERVERURL") + f.getImageURLResize();

      for (Object o: images) {
         if (!(o instanceof Map)) {
            throw new IOException("Malformed JSON input: images entry is not an object.");
         }
         String imageURI = (String)((Map<String, Object>)o).get("uri");
         if (imageURI == null) {
            throw new IOException("Malformed JSON input: missing URI for image.");
         }
         if (imageURI.equals(folioURI)) {
            return folioURI;
         }
      }
      return null;
   }
   
   /**
    * Update a transcription based on a map of JSON row data.
    * @param rowData row data from JSON
    * @param transcr transcription to be updated
    */
   private void updateTranscription(Map<String, Object> rowData, Transcription transcr) throws IOException, SQLException {
      Map<String, Object> b = getObject(rowData, "bounds", true);
      String cont = (String)rowData.get("content");
      transcr.update(cont, getRectangle(b));
   }

   /**
    * Insert a new transcription based on newly-added row data.
    *
    * @param rowData row data from JSON
    * @param f folio to which the new row is being added
    */
   private void insertTranscription(Map<String, Object> rowData, Folio f) throws IOException, SQLException {
      Map<String, Object> b = getObject(rowData, "bounds", true);
      Rectangle r = getRectangle(b);
      
      // This Transcription constructor creates a record in the database.
      String cont = (String)rowData.get("content");
      String note = "";
      Transcription newTranscr = new Transcription(user, project.getProjectID(), f.getFolioNumber(), cont, note, r);
   }

   /**
    * Remove a transcription line which no longer has a corresponding row.
    *
    * @param transcr transcription to be removed
    */
   private void removeTranscription(Transcription transcr) throws SQLException {
      transcr.remove();
   }

   private static Logger LOG = Logger.getLogger(JsonImporter.class.getName());
}
