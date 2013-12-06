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
package imageLines;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.mail.MessagingException;
import textdisplay.DatabaseWrapper;
import textdisplay.Folio;
import textdisplay.Manuscript;
import textdisplay.mailer;
import user.User;

/**
 * Handles logging of image requests, tracking of success and failure as well as load times, so we can set
 * user expectations.
 */
public class ImageRequest {

   private long startTime;
   private Boolean cacheHit;
   private int id;

   /**
    * A new image request.
    *
    * @param folioNumber requested folio number
    * @param UID user who requested it. 0 means it was an internal TPEN request to service parsing and the
    * like.
    * @throws SQLException
    */
   public ImageRequest(int folioNumber, int UID) throws SQLException {
      System.out.print("saving record for " + folioNumber + " " + UID + "\n");
      startTime = System.currentTimeMillis();
      Folio f = new Folio(folioNumber);
      cacheHit = f.isCached();
      String query = "insert into imageRequest(UID,folio,cacheHit,elapsedTime,date,succeeded,msg) values (?,?,?,?,NOW(),?,?)";
      Connection j = null;
      PreparedStatement ps = null;
      try {
         j = DatabaseWrapper.getConnection();
         ps = j.prepareStatement(query, PreparedStatement.RETURN_GENERATED_KEYS);
         ps.setInt(1, UID);
         ps.setInt(2, folioNumber);
         ps.setBoolean(3, cacheHit);
         ps.setInt(4, 0);
         ps.setBoolean(5, false);
         ps.setString(6, "started");
         ps.execute();
         ResultSet rs = ps.getGeneratedKeys();
         if (rs.next()) {
            this.id = rs.getInt(1);
         } else {
            this.id = -1;
         }
      } finally {
         DatabaseWrapper.closeDBConnection(j);
         DatabaseWrapper.closePreparedStatement(ps);
      }
   }

   /**
    * If the image request was successfully serviced, mark it as such.
    *
    * @throws Exception
    */
   public void completeSuccess() throws SQLException {
      if (id > 0) {
         long timeElapsed = System.currentTimeMillis() - startTime;
         try (Connection j = DatabaseWrapper.getConnection()) {
            try (PreparedStatement ps = j.prepareStatement("update imageRequest set elapsedTime=?, succeeded=true, msg='' where id=?")) {
               ps.setInt(1, (int)timeElapsed);
               ps.setInt(2, id);
               ps.executeUpdate();
            }
         }
      } else {
         throw new IllegalArgumentException("Missing request ID.");
      }
   }

   /**
    * The image couldn't be delivered. Indicate that in our records along with any reason we can provide
    *
    * @param msg Anything about why the image delivery failed.
    * @throws Exception
    */
   public void completeFail(String msg) throws SQLException {
      if (msg == null) {
         msg = "null msg";
      }
      if (id > 0) {
         long timeElapsed = System.currentTimeMillis() - startTime;
         String query = "update imageRequest set elapsedTime=?, succeeded=false, msg=? where id=?";
         try (Connection j = DatabaseWrapper.getConnection()) {
            try (PreparedStatement ps = j.prepareStatement(query)) {
               ps.setInt(1, (int) timeElapsed);
               ps.setString(2, msg);
               ps.setInt(3, id);
               ps.executeUpdate();
            }
         }
      } else {
         throw new IllegalArgumentException("Missing request ID.");
      }
   }

   /**
    * Look at the last 10 successful non cache hit image requests for an archive and compute the mean
    * turnaround time.
    *
    * @param archive Name of the archive you want to check on.
    * @return
    * @throws SQLException
    */
   public static int getAverageElapsedTime(String archive) throws SQLException {
      String query = "select elapsedTime from imageRequest join folios on imageRequest.folio=folios.pageNumber where archive=? and cacheHit=false order by date limit 10";
      Connection j = null;
      PreparedStatement ps = null;
      try {
         j = DatabaseWrapper.getConnection();
         ps = j.prepareStatement(query);
         ps.setString(1, archive);
         ResultSet rs = ps.executeQuery();
         int sum = 0;
         int count = 0;
         while (rs.next()) {
            sum += rs.getInt(1);
            count++;
         }
         if (count == 0) {
            return 0;
         }
         return sum / count;
      } finally {
         DatabaseWrapper.closeDBConnection(j);
         DatabaseWrapper.closePreparedStatement(ps);
      }
   }

   public static void EmailReport(int minutes) throws SQLException {
      minutes++;
      try (Connection j = DatabaseWrapper.getConnection()) {
         try (PreparedStatement ps = j.prepareStatement("select distinct(folio) from imageRequest where succeeded=false and date>DATE_SUB(now(), INTERVAL ? MINUTE) and date<DATE_SUB(now(), INTERVAL 1 MINUTE )")) {
            ps.setInt(1, minutes);
            ResultSet rs = ps.executeQuery();
            String body = "";
            while (rs.next()) {
               Folio f = new Folio(rs.getInt(1));
               Manuscript ms = new Manuscript(rs.getInt(1));
               body += "\nFolio " + rs.getInt(1) + " " + ms.getShelfMark() + " " + f.getPageName() + " " + f.getImageURL() + "\n";
            }
            if (body.compareTo("") == 0) {
               //dont send an empty email
               return;
            }
            body = "The following images failed to load in the last " + minutes + " minutes.\n" + body;
            User[] admins = user.User.getAdmins();
            mailer m = new mailer();
            for (User i : admins) {
               try {
                  m.sendMail(Folio.getRbTok("EMAILSERVER"), "TPEN@t-pen.org", i.getUname(), "TPEN image issue", body);
               } catch (MessagingException ex) {
                  Logger.getLogger(ImageRequest.class.getName()).log(Level.SEVERE, null, ex);
               }
            }
         }
      }
   }
}
