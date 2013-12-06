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

import java.awt.Dimension;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.LinkedHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import static edu.slu.util.LangUtils.buildQuickMap;
import imageLines.ImageCache;
import org.apache.commons.lang.StringUtils;
import textdisplay.Folio;
import textdisplay.Project;
import textdisplay.Transcription;

/**
 * Class which manages serialisation to JSON-LD. Builds a Map containing the
 * Project's data, and then uses Jackson to serialise it as JSON.
 *
 * @author tarkvara
 */
public class JsonLDExporter {

   /**
    * Holds data which will be serialised to JSON.
    */
   Map<String, Object> manifestData;

   /**
    * Populate a map which will contain all the relevant project information.
    *
    * @param proj the project to be exported.
    * @throws SQLException
    */
   public JsonLDExporter(Project proj) throws SQLException, IOException {
      Folio[] folios = proj.getFolios();

      try {
         String projName = "http://t-pen.org/" + URLEncoder.encode(proj.getProjectName(), "UTF-8");
         manifestData = new LinkedHashMap<>();
         manifestData.put("@context", "http://www.shared-canvas.org/ns/context.json");
         manifestData.put("@id", projName + "/manifest.json");
         manifestData.put("@type", "sc:Manifest");
         manifestData.put("label", proj.getProjectName());

         Map<String, Object> pages = new LinkedHashMap<>();
         pages.put("@id", projName + "/sequence/normal");
         pages.put("@type", "sc:Sequence");
         pages.put("label", "Current Page Order");

         List<Map<String, Object>> pageList = new ArrayList<>();
         for (Folio f : folios) {
            pageList.add(buildPage(proj.getProjectID(), projName, f));
         }
         pages.put("canvases", pageList);
         manifestData.put("sequences", new Object[] { pages });
      } catch (UnsupportedEncodingException ignored) {
      }
   }

   public String export() throws JsonProcessingException {
      ObjectMapper mapper = new ObjectMapper();
      return mapper.writer().withDefaultPrettyPrinter().writeValueAsString(manifestData);
   }

   /**
    * Get the map which contains the serialisable information for the given
    * page.
    *
    * @param f the folio to be exported
    * @return a map containing the relevant info, suitable for Jackson
    * serialisation
    */
   private Map<String, Object> buildPage(int projID, String projName, Folio f) throws SQLException, IOException {

      String canvasID = projName + "/canvas/" + URLEncoder.encode(f.getPageName(), "UTF-8");

      Dimension pageDim = ImageCache.getImageDimension(f.getFolioNumber());
      if (pageDim == null) {
         LOG.log(Level.INFO, "Image for {0} not found in cache, loading image...", f.getFolioNumber());
         pageDim = f.getImageDimension();
      }
      LOG.log(Level.INFO, "pageDim={0}", pageDim);

      Map<String, Object> result = new LinkedHashMap<>();
      result.put("@id", canvasID);
      result.put("@type", "sc:Canvas");
      result.put("label", f.getPageName());

      int canvasHeight = 1000;
      result.put("height", canvasHeight);
      
      if (pageDim != null) {
         int canvasWidth = pageDim.width * canvasHeight / pageDim.height;  // Convert to canvas coordinates.
         result.put("width", canvasWidth);
      }
      List<Object> resources = new ArrayList<>();
      Map<String, Object> imageAnnot = new LinkedHashMap<>();
      imageAnnot.put("@type", "oa:Annotation");
      imageAnnot.put("motivation", "sc:painting");
      
      Map<String, Object> imageResource = buildQuickMap("@id", Folio.getRbTok("SERVERURL") + f.getImageURLResize(), "@type", "dctypes:Image", "format", "image/jpeg");
//      imageResource.put("iiif", ?);
      if (pageDim != null) {
         imageResource.put("height", pageDim.height);
         imageResource.put("width", pageDim.width);
      }
      imageAnnot.put("resource", imageResource);

      imageAnnot.put("on", canvasID);
      resources.add(imageAnnot);

      Transcription[] transcriptions = Transcription.getProjectTranscriptions(projID, f.getFolioNumber());
      for (Transcription t : transcriptions) {
         // Body of the annotation.  Contains the actual text.
         Map<String, Object> lineAnnot = new LinkedHashMap<>();
         String lineURI = projName + "/line/" + t.getLineID();
         lineAnnot.put("@id", lineURI);
         lineAnnot.put("@type", "oa:Annotation");
         lineAnnot.put("motivation", "sc:painting");
         lineAnnot.put("resource", buildQuickMap("@type", "cnt:ContentAsText", "cnt:chars", t.getTextUnencoded()));
         lineAnnot.put("on", String.format("%s#xywh=%d,%d,%d,%d", canvasID, t.getX(), t.getY(), t.getWidth(), t.getHeight()));
         resources.add(lineAnnot);
         
         String note = t.getComment();
         if (!StringUtils.isEmpty(note)) {
            Map<String, Object> noteAnnot = new LinkedHashMap<>();
            noteAnnot.put("@id", projName + "/note/" + t.getLineID());
            noteAnnot.put("@type", "oa:Annotation");
            noteAnnot.put("motivation", "oa:commenting");
            noteAnnot.put("resource", buildQuickMap("@type", "cnt:ContentAsText", "cnt:chars", note));
            noteAnnot.put("on", lineURI);
            resources.add(noteAnnot);
         }
      }
      result.put("resources", resources);
      return result;
   }

   private static final Logger LOG = Logger.getLogger(JsonLDExporter.class.getName());
}
