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
package match;

import detectimages.blob;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Vector;
import textdisplay.Manuscript;

/**
 *
 * @author jdeerin1
 */
public class allBlobDivs {

   private String divs;

   /**
    * @depricated
    */
   public allBlobDivs(String pageIdentifier) throws FileNotFoundException, IOException, SQLException {
      String onMouseOver = "";
      divs = "";
      Vector<blob> allblobs = blob.getBlobs("/usr/data/" + pageIdentifier + ".txt");
      matchLocater m = new matchLocater(Integer.parseInt(pageIdentifier), 0, true);
      //Connection j=dbWrapper.getConnection();
      for (int i = 0; i < allblobs.size(); i++) {
         //if(allblobs.get(i).getSize()>=50)
         int count = m.getBlobMatchCount(i);
         if (count > 0) {
            divs += "<a title=\"" + count + "\" href=\"?b=" + i + "&p=" + pageIdentifier + "\"><div id=\"" + i + "\" style=\"z-index:1;background-color:blue;filter:alpha(opacity=50);opacity: 0.5;-moz-opacity:0.5;position:absolute;left:" + allblobs.get(i).getX() + "px;top:" + allblobs.get(i).getY() + "px;width:" + allblobs.get(i).getWidth() + "px;height:" + allblobs.get(i).getHeight() + "px; \" > " + "</div>" + "</a>";
         }
      }
      // j.close();

   }

   public allBlobDivs(int folioNum) throws FileNotFoundException, IOException, SQLException {
      StringBuilder newBlobs = new StringBuilder("");
      Manuscript ms = new Manuscript(folioNum);
      Vector<blob> allblobs = blob.getBlobs(textdisplay.Folio.getRbTok("PALEODATADIR") + "/" + ms.getID() + "/" + folioNum + ".txt");
      matchLocater m = new matchLocater(folioNum, 0, "sup");
      //Connection j=dbWrapper.getConnection();
      for (int i = 0; i < allblobs.size(); i++) {
         //if(allblobs.get(i).getSize()>=50)
         int count = m.getBlobMatchCount(i);
         if (count > 0) {
            newBlobs.append("<a blobid='").append(i)
                    .append("' class='blob' title='").append(count)
                    .append("' href='?b=").append(i)
                    .append("&p=").append(folioNum)
                    .append("' blobx='").append(allblobs.get(i).getX())
                    .append("' bloby='").append(allblobs.get(i).getY())
                    .append("' blobwidth='").append(allblobs.get(i).getHeight())
                    .append("' blobheight='").append(allblobs.get(i).getWidth())
                    .append("' ></a>");
         }
      }
      // j.close();

   }

   public allBlobDivs(int folioNum, int matchLevel) throws FileNotFoundException, IOException, SQLException {
      StringBuilder newBlobs = new StringBuilder("");
      Manuscript ms = new Manuscript(folioNum);
      Vector<blob> allblobs = blob.getBlobs(textdisplay.Folio.getRbTok("PALEODATADIR") + "/" + ms.getID() + "/" + folioNum + ".txt");
      matchLocater m = new matchLocater(folioNum, 0, "sup", matchLevel);
      //Connection j=dbWrapper.getConnection();
      for (int i = 0; i < allblobs.size(); i++) {
         //if(allblobs.get(i).getSize()>=50)
         int count = m.getBlobMatchCount(i);
         int maxLevel = m.getBlobMaxMatchLevel(i);
         if (count > 0) {
            newBlobs.append("<a blobid='").append(i)
                    .append("' matchLevel='").append(maxLevel)
                    .append("' class='blob' title='").append(count)
                    .append("' href='?b=").append(i)
                    .append("&p=").append(folioNum)
                    .append("&matchLevel=").append(matchLevel)
                    .append("' blobx='").append(allblobs.get(i).getX())
                    .append("' bloby='").append(allblobs.get(i).getY())
                    .append("' blobwidth='").append(allblobs.get(i).getHeight())
                    .append("' blobheight='").append(allblobs.get(i).getWidth())
                    .append("' ></a>");
         }
         divs = newBlobs.toString();
      }
      // j.close();

   }

   public String getDivs() {
      if ("null".equals(divs)) {
         divs = "<div id='urgentError'><p id='errorMessage'>No matches have been discovered; this page might be pending further analysis.</p></div>";
      }
      return divs;
   }
}
