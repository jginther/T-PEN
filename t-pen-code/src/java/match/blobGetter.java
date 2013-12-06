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

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import detectimages.blob;
import textdisplay.Manuscript;

/**
 *
 * @author jdeerin1
 */
public class blobGetter {

   int h, y, x, w;

   public static blob getRawBlob(int folio, int blob) throws SQLException {

      Manuscript ms = new Manuscript(folio);
      blob b;
      String path = textdisplay.Folio.getRbTok("PALEODATADIR") + "/" + ms.getID() + "/";
      b = detectimages.blob.getBlob(path, folio + ".txt", blob);
      return b;

   }

   public blobGetter(String img, int blob) throws SQLException {
      Connection j = null;
      int folioNum = Integer.parseInt(img.split("\\.")[0]);
      Manuscript ms = new Manuscript(folioNum);
      String path = textdisplay.Folio.getRbTok("PALEODATADIR") + "/" + ms.getID() + "/";
      try {
         j = textdisplay.DatabaseWrapper.getConnection();

         String selectQuery = "select * from `blobs` where `img`=? and `blob`=?";
         PreparedStatement select = j.prepareStatement(selectQuery);
         if (img.contains(".")) {
            select.setString(1, img);
         } else {
            select.setString(1, img + ".jpg");
         }
         select.setInt(2, blob);
         ResultSet rs = select.executeQuery();
         if (rs.next()) {
            y = rs.getInt("y");
            x = rs.getInt("x");
            h = rs.getInt("h");
            w = rs.getInt("w");
         } else {
            blob b = null;
            if (img.contains(".")) {
               b = detectimages.blob.getBlob(path, img, blob);
            } else {
               b = detectimages.blob.getBlob(path, img + ".txt", blob);
            }

            if (b == null) {
               System.out.print("missing blob " + img + ":" + blob + "\n");

            }
            y = b.getY();
            x = b.getX();
            h = b.getHeight();
            w = b.getWidth();
         }
      } finally {
         j.close();
      }

   }

   public int getX() {
      return x;
   }

   public int getY() {
      return y;
   }

   public int getHeight() {
      return h;
   }

   public int getWidth() {
      return w;
   }
}
