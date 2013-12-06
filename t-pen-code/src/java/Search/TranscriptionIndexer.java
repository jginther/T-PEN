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
 *
 * @author Jon Deering
 */
package Search;

import java.io.IOException;
import java.sql.SQLException;
import org.apache.lucene.index.CorruptIndexException;
import textdisplay.Transcription;

/**
 * Disabled for now; used to build an index of transcriptions, public and private
 */
public class TranscriptionIndexer {

   /**
    * Build/rebuild the transcription search index from scratch.
    *
    * @throws SQLException
    * @throws CorruptIndexException
    * @throws IOException
    */
   public TranscriptionIndexer() throws SQLException, CorruptIndexException, IOException {
/*      IndexWriter writer = null;
      Analyzer analyser;
      Directory directory;

      // @TODO parameterized location
     String dest = "/usr/indexTranscriptions";

      directory = FSDirectory.getDirectory(dest, true);
      analyser = new StandardAnalyzer();
      writer = new IndexWriter(directory, analyser, true);
      PreparedStatement stmt = null;
      Connection j = null;
      try {
         j = DatabaseWrapper.getConnection();
         String query = "select * from transcription where text!='' and creator>0 order by folio, line";
         stmt = j.prepareStatement(query);
         ResultSet rs = stmt.executeQuery();
         while (rs.next()) {
            int folio = rs.getInt("folio");
            int line = rs.getInt("line");
            int UID = rs.getInt("creator");
            int id = rs.getInt("id");
            Transcription t = new Transcription(id);
            Document doc = new Document();
            Field field;
            field = new Field("text", t.getText(), Field.Store.YES, Field.Index.ANALYZED);
            doc.add(field);
            field = new Field("comment", t.getComment(), Field.Store.YES, Field.Index.ANALYZED);
            doc.add(field);
            field = new Field("creator", "" + t.UID, Field.Store.YES, Field.Index.ANALYZED);
            doc.add(field);
            field = new Field("security", "" + "private", Field.Store.YES, Field.Index.ANALYZED);
            doc.add(field);
            field = new Field("line", "" + t.getLine(), Field.Store.YES, Field.Index.ANALYZED);
            doc.add(field);
            field = new Field("page", "" + t.getFolio(), Field.Store.YES, Field.Index.ANALYZED);
            doc.add(field);
            field = new Field("id", "" + t.getLineID(), Field.Store.YES, Field.Index.ANALYZED);
            doc.add(field);
            field = new Field("manuscript", "" + new Manuscript(t.getFolio()).getID(), Field.Store.YES, Field.Index.ANALYZED);
            doc.add(field);
            field = new Field("projectID", "" + t.getProjectID(), Field.Store.YES, Field.Index.ANALYZED);
            doc.add(field);
            writer.addDocument(doc);
         }
      } catch (Exception ex) {
         ex.printStackTrace();
         if (writer != null) {
            writer.commit();
            writer.close();
            return;
         }
         ex.printStackTrace();
      } finally {
         DatabaseWrapper.closeDBConnection(j);
         DatabaseWrapper.closePreparedStatement(stmt);
      }

      writer.commit();
      writer.close();*/
   }

   /**
    * Update the text/comment/image positioning of a transcription that has previusly been indexed
    *
    * @param t the transcription that was updated
    * @throws IOException
    * @throws SQLException
    */
   public static void update(Transcription t) throws IOException, SQLException {
/*
      IndexWriter writer = null;
      Analyzer analyser;
      Directory directory;

      // @TODO parameterize this location
      String dest = "/usr/indexTranscriptions";

      directory = FSDirectory.getDirectory(dest, true);
      analyser = new StandardAnalyzer();
      writer = new IndexWriter(directory, analyser, true);
      Term j = new Term("id", "" + t.getLineID());
      writer.deleteDocuments(j);
      Document doc = new Document();
      Field field;
      field = new Field("text", t.getText(), Field.Store.YES, Field.Index.ANALYZED);
      doc.add(field);
      field = new Field("comment", t.getComment(), Field.Store.YES, Field.Index.ANALYZED);
      doc.add(field);
      field = new Field("creator", "" + t.UID, Field.Store.YES, Field.Index.ANALYZED);
      doc.add(field);
      field = new Field("security", "" + "private", Field.Store.YES, Field.Index.ANALYZED);
      doc.add(field);
      field = new Field("line", "" + t.getLine(), Field.Store.YES, Field.Index.ANALYZED);
      doc.add(field);
      field = new Field("page", "" + t.getFolio(), Field.Store.YES, Field.Index.ANALYZED);
      doc.add(field);
      field = new Field("id", "" + t.getLineID(), Field.Store.YES, Field.Index.ANALYZED);
      doc.add(field);
      field = new Field("manuscript", "" + new Manuscript(t.getFolio()).getID(), Field.Store.YES, Field.Index.ANALYZED);
      doc.add(field);
      field = new Field("projectID", "" + t.getProjectID(), Field.Store.YES, Field.Index.ANALYZED);
      doc.add(field);
      writer.addDocument(doc);
      writer.commit();
      writer.close();*/
   }

   /**
    * Add a transcription to the index
    *
    * @param t Transcription that isn't currently indexed
    * @throws IOException
    * @throws SQLException
    */
   public static void add(Transcription t) throws IOException, SQLException {
/*      IndexWriter writer = null;
      Analyzer analyser;
      Directory directory;
      
      // @TODO parameterize this location
      String dest = "/usr/indexTranscriptions";

      directory = FSDirectory.getDirectory(dest, true);
      analyser = new StandardAnalyzer();
      writer = new IndexWriter(directory, analyser, true);
      Document doc = new Document();
      Field field;
      field = new Field("text", t.getText(), Field.Store.YES, Field.Index.ANALYZED);
      doc.add(field);
      field = new Field("comment", t.getComment(), Field.Store.YES, Field.Index.ANALYZED);
      doc.add(field);
      field = new Field("creator", "" + t.UID, Field.Store.YES, Field.Index.ANALYZED);
      doc.add(field);
      field = new Field("security", "" + "private", Field.Store.YES, Field.Index.ANALYZED);
      doc.add(field);
      field = new Field("line", "" + t.getLine(), Field.Store.YES, Field.Index.ANALYZED);
      doc.add(field);
      field = new Field("page", "" + t.getFolio(), Field.Store.YES, Field.Index.ANALYZED);
      doc.add(field);
      field = new Field("id", "" + t.getLineID(), Field.Store.YES, Field.Index.ANALYZED);
      doc.add(field);
      field = new Field("manuscript", "" + new Manuscript(t.getFolio()).getID(), Field.Store.YES, Field.Index.ANALYZED);
      doc.add(field);
      field = new Field("projectID", "" + t.getProjectID(), Field.Store.YES, Field.Index.ANALYZED);
      doc.add(field);
      writer.addDocument(doc);
      writer.commit();
      writer.close();
*/
   }
}
