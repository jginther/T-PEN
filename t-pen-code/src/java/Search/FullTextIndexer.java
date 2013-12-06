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
import java.util.Hashtable;
import java.util.Stack;
import org.apache.lucene.index.CorruptIndexException;


/**
 *
 * Build a database table for quotation searching on frequently quoted source documents like the bible.
 */
public class FullTextIndexer {

   /**
    * Uses a database table with 10 word segments to allow for quick quotation searching when we want to
    * try to find quotes from a textual source being used in transcriptions.
    *
    * @throws SQLException
    * @throws CorruptIndexException
    * @throws IOException
    */
   public FullTextIndexer() throws SQLException, CorruptIndexException, IOException {
/*      //this static location needs to be parameterized
      String src = "/usr/txt2/";
      File dir = new File(src);
      File[] files = dir.listFiles();
      String corpus = "";
      String deleteQuery = "truncate table quotationSources";
      String insertQuery = "insert into quotationSources (workTitle, headword, word1, word2, word3, word4,word5,word6,word7,word8,word9) values(?,?,?,?,?,?,?,?,?,?,?)";

      Connection j = null;
      PreparedStatement del = null;
      PreparedStatement insert = null;
      try {
         j = DatabaseWrapper.getConnection();

         del = j.prepareStatement(deleteQuery);
         del.execute();
         j.setAutoCommit(false);
         insert = j.prepareStatement(insertQuery);

         for (int i = 0; i < files.length; i++) {
            if (files[i].getAbsolutePath().contains("txt")) {
               System.out.print(files[i].getName() + "\n");
               File srcFile = new File(files[i].getAbsolutePath());
               String possibleSource = readFileAsString(files[i].getAbsolutePath());
               possibleSource = possibleSource.replace("\n", " ");
               possibleSource = possibleSource.toLowerCase().replaceAll("[ ]{2,}", " ");
               possibleSource = possibleSource.replaceAll("<.+?>", "");
               possibleSource = possibleSource.replace(")", " ");
               possibleSource = possibleSource.replace("(", " ");
               possibleSource = possibleSource.replace("[", " ");
               possibleSource = possibleSource.replace("]", " ");
               possibleSource = possibleSource.replace(".", " ");
               possibleSource = possibleSource.replace("?", " ");
               possibleSource = possibleSource.replace("!", " ");
               possibleSource = possibleSource.replace(";", " ");
               possibleSource = possibleSource.replace(":", " ");
               possibleSource = possibleSource.replace(",", " ");
               possibleSource = possibleSource.replace("-", "");
               //replace multiple spaces

               String oldsource = possibleSource;
               possibleSource = possibleSource.toLowerCase().replaceAll("  ", " ");
               while (oldsource.length() != possibleSource.length()) {
                  oldsource = possibleSource;
                  possibleSource = possibleSource.toLowerCase().replaceAll("  ", " ");
               }

               String[] sourceWords = possibleSource.split(" ");
               Hashtable<String, Stack<Integer>> ht = getMashup(sourceWords);
               Enumeration e = ht.keys();
               while (e.hasMoreElements()) {
                  String word = (String) e.nextElement();
                  if (word.length() < 127) {
                     Stack<Integer> instances = ht.get(word);
                     while (!instances.empty()) {
                        int pos = instances.pop();
                        insert.setString(1, files[i].getName());
                        insert.setString(2, sourceWords[pos]);
                        int start = 2;
                        for (int ctr = 1; ctr < 10; ctr++) {
                           if (pos + ctr < sourceWords.length && sourceWords[pos + ctr].length() < 127) {
                              insert.setString(ctr + start, sourceWords[pos + ctr]);
                           } else {
                              insert.setString(ctr + start, "");
                           }
                        }
                        insert.execute();


                     }
                  }
               }
               j.commit();
            }
         }
      } finally {
         j.setAutoCommit(true);
         DatabaseWrapper.closeDBConnection(j);
         DatabaseWrapper.closePreparedStatement(del);
         DatabaseWrapper.closePreparedStatement(insert);
      }*/
   }

   /**
    * Creates a hashtable of strings(words) and the number of times they occur
    *
    * @param text array of words
    */
   public static Hashtable<String, Stack<Integer>> getMashup(String[] text) {
      Hashtable<String, Stack<Integer>> toret = new Hashtable();
/*      for (int i = 0; i < text.length; i++) {
         if (toret.containsKey(text[i])) {
            Stack cur = toret.get(text[i]);
            cur.push(i);
         } else {
            Stack s = new Stack<Integer>();
            s.push(i);
            toret.put(text[i], s);
         }
      }*/
      return toret;
   }
}
