/*
 * @author Jon Deering
Copyright 2011 Saint Louis University. Licensed under the Educational Community License, Version 2.0 (the "License"); you may not use
this file except in compliance with the License.

You may obtain a copy of the License at http://www.osedu.org/licenses/ECL-2.0

Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions
and limitations under the License.
 */
package Search;


import textdisplay.DatabaseWrapper;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Stack;
import java.util.logging.Level;
import java.util.logging.Logger;
import textdisplay.Manuscript;
import textdisplay.Project;

/**
 *
 * Carries out searches for quotations by transcriptions from some source texts we indexed earlier
 */
public class TextSearcher {

    /**
     *
     * @param query the transcription that may quote some known sources
     * @param wordCount the min word length result you will accept
     * @param fuzzyCount the number of non matching words you want to permit in the wordCount long result strings
     * @return
     * @throws SQLException
     */
    public static String search(String query, int wordCount, int fuzzyCount) throws SQLException {
        String toret = "";
        Connection j = null;
        PreparedStatement ps = null;
        try {
            j = DatabaseWrapper.getConnection();
            String selectQuery = "select * from quotationSources where headWord=? and word1=?";
            String[] queryParts = query.split(" ");
            if (queryParts.length < 2) {
                return "";
            }
            ps = j.prepareStatement(selectQuery);
            ps.setString(1, queryParts[0]);
            ps.setString(2, queryParts[1]);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                String tmp = rs.getString("workTitle") + ": " + rs.getString("headWord") + " " + rs.getString("word1");
                int count = 2;
                while (count < 10) {
                    String nextWord = rs.getString("word" + count);
                    if (count < queryParts.length && (matchStrings(nextWord, queryParts[count]) || fuzzyCount > 0)) {
                        if (matchStrings(nextWord, queryParts[count])) {
                            tmp += " " + nextWord;
                            count++;
                        } else {
                            tmp += " " + nextWord;
                            fuzzyCount--;
                            count++;
                        }
                    } else {
                        break;
                    }
                }
                tmp += "\n";
                if (count >= wordCount) {
                    for (int i = 0; i < 11; i++) {
                        toret += rs.getString(i + 1) + " ";
                    }
                    toret += "\n";
                }
            }
        } finally {
            DatabaseWrapper.closeDBConnection(j);
            DatabaseWrapper.closePreparedStatement(ps);
        }
        return toret;
    }

    /**
     * Runs a quotation search on a string and a text file in the filesystem. Not currently used, but great for testing the quality of DB based quotation searches.
     * @param corpus text
     * @param sourceFile location of the quotation source
     * @return a text description of found results
     * @throws SQLException
     * @throws FileNotFoundException
     * @throws IOException
     */
    public static String compare(String corpus, String sourceFile) throws SQLException, FileNotFoundException, IOException {
        String possibleSource = readFileAsString(sourceFile);//.replaceAll("\n", " ").replaceAll("\\s+", " ");
        final int minlength = 3;
        final int minwords = 3;
        String[] corpusWords = corpus.split(" ");
        String[] sourceWords = possibleSource.split(" ");
        Hashtable<String, Stack<Integer>> ht = getMashup(sourceWords);
        int corpusstart = 0;
        int thisPhraseWords = minwords;
        Boolean good = false;//Is the phrase as held found?
        Boolean hasBeenGood = false;//Has this phrase been found at a previous length?
        int foundpos = 0;
        String toret = "";
        if (true) {
            return TextSearcher.compare(corpusWords, ht, sourceWords);
        }
        long start = System.currentTimeMillis();
        while (corpusstart + thisPhraseWords < corpusWords.length) {
            if (hasBeenGood == true && (good == false || !(corpusstart + thisPhraseWords < corpusWords.length))) {
                toret += ("Found a match " + corpusstart + " letters into text1 (which is " + corpusWords.length + " letters long) with text 2 word  " + foundpos + " of " + sourceWords.length + ":\n");
                for (int i = 0; i < thisPhraseWords - 1; i++) {
                    toret += corpusWords[i + corpusstart] + " ";

                }
                toret += "<br/>\n";
                hasBeenGood = false;
                corpusstart = corpusstart + thisPhraseWords - 1;
            } else {
                if (good == false) {
                    //The minword phrase wasnt found, move along 1 word and try again
                    thisPhraseWords = minwords;//The number of words in the current phrase starts as 3.
                    corpusstart++;
                }
            }
            good = false;
            for (int i = 0; i < sourceWords.length; i++) {
                if (sourceWords[i].compareTo(corpusWords[corpusstart]) == 0) {
                    int j;
                    foundpos = i;
                    //If this look breaks (menaing one of the words in the corpus phrase doesnt match the one it should in the source phrase
                    //good is not set to true.
                    for (j = 1; j < thisPhraseWords; j++) {
                        try {
                            if (sourceWords[i + j].compareTo(corpusWords[corpusstart + j]) != 0) {
                                break;
                            }
                        } catch (ArrayIndexOutOfBoundsException er) {
                            j = 0;
                            break;
                        }
                    }
                    if (j == thisPhraseWords) {
                        hasBeenGood = true;
                        good = true;
                    }
                }
            }


            if (good) {
                thisPhraseWords++;
            }



        }
        long elapsedTimeMillis2 = System.currentTimeMillis() - start;
        System.out.print("Completed mem " + elapsedTimeMillis2 + "\n");
        return toret;
    }

    /**
     * Split the text into individual words
     * @param corpus text
     * @return array of individual words
     * @throws SQLException
     */
    public static String[] getWords(String corpus) throws SQLException {
        corpus = corpus.replace("\\.", "");
        corpus = corpus.replace("\\?", "");
        corpus = corpus.replace(";", "");
        corpus = corpus.replace(",", "");
        corpus = corpus.replace("\\!", "");
        corpus = corpus.replace("\\[*.\\]", "");
        String[] plainString = corpus.split(" ");
        return plainString;
    }

    /**
     * Carry out a search for matches given 2 chunks of text that have been split into individual words
     * @param corpus
     * @param ht 
     * @param source
     * @return
     */
    public static String compare(String[] corpus, Hashtable<String, Stack<Integer>> ht, String[] source) {
        String toret = "";
        for (int i = 0; i < corpus.length; i++) {
            Stack<Integer> s = ht.get(corpus[i]);
            if (s != null) {
                for (int j = 0; j < s.size(); j++) {
                    int pos = s.elementAt(j);
                    //corpus[i] is the same as source[j], check the following words
                    int count = 1;
                    while (true && pos + count < source.length && i + count < corpus.length) {
                        if (source[pos + count].compareTo(corpus[i + count]) == 0) {
                            count++;
                        } else {
                            break;
                        }
                    }
                    if (count > 2) {
                        toret += ("Found a match " + i + " letters into text1 (which is " + corpus.length + " letters long) with text 2 word  " + j + " of " + source.length + ":\n");
                        for (int ctr = 0; ctr < count; ctr++) {
                            toret += (source[pos + ctr] + " ");
                        }
                        toret += ("\n");
                    }

                }
            }
        }
        return toret;
    }

    /**Read the text from a file in a way that preserves special characters*/
    private static String readFileAsString(String filePath)
            throws java.io.IOException {
        StringBuffer fileData = new StringBuffer(1000);
        char[] buf = new char[1024];
        int numRead = 0;
        Reader in = new InputStreamReader(new FileInputStream(filePath), "UTF-8");
        while ((numRead = in.read(buf)) != -1) {
            String readData = String.valueOf(buf, 0, numRead);
            fileData.append(readData);
            buf = new char[1024];
        }
        in.close();
        return fileData.toString();
    }
    /**Demonstrates the usage.*/
    public static void main(String[] args) throws SQLException, FileNotFoundException, IOException {
        String src = "/usr/textold/";
        File dir = new File(src);
        File[] files = dir.listFiles();
        String corpus = Manuscript.getFullDocument(new Project(31), false);
        corpus = corpus.replace(".", " ");
        corpus = corpus.replace("(", " ");
        corpus = corpus.replace(")", " ");
        corpus = corpus.replace("?", " ");
        corpus = corpus.replace(";", " ");
        corpus = corpus.replace(":", " ");
        corpus = corpus.replace("!", " ");


        corpus = corpus.replace(",", " ");

        corpus = corpus.replace("\\[*.\\]", "").replaceAll("\n", " ").replaceAll("\\s+", " ");
        corpus = corpus.replaceAll("<.+?>", " ");
        corpus = corpus.toLowerCase();
        /*for(int i=0;i<files.length;i++)
        
        {
        if(files[i].getAbsolutePath().contains("txt"))
        {
        System.out.print("starting "+files[i].getAbsolutePath()+"\n");
        //String query=Manuscript.getFullDocument(new Project(17), false);
        String query="mittet v dare exhibet te duce graecia mittetet sidon nilusque rates nunc nostra serenusorsa iuves haec ut latias vox impleat urbes haemoniam";
        System.out.print(TextSearcher.compare(query, files[i].getAbsolutePath()));
        }
        }*/
        String selectQuery = "select * from transcription where text!=''";
        Connection j = null;
        PreparedStatement ps = null;
        try {
            j = DatabaseWrapper.getConnection();
            ps = j.prepareStatement(selectQuery);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                corpus = rs.getString("text");
                corpus = corpus.replace(".", " ");
                corpus = corpus.replace("(", " ");
                corpus = corpus.replace(")", " ");
                corpus = corpus.replace("?", " ");
                corpus = corpus.replace(";", " ");
                corpus = corpus.replace(":", " ");
                corpus = corpus.replace("!", " ");


                corpus = corpus.replace(",", " ");

                corpus = corpus.replace("\\[*.\\]", "").replaceAll("\n", " ").replaceAll("\\s+", " ");
                corpus = corpus.replaceAll("<.+?>", " ");
                corpus = corpus.toLowerCase();
                String oldsource = corpus;
                corpus = corpus.toLowerCase().replaceAll("  ", " ");
                while (oldsource.length() != corpus.length()) {
                    oldsource = corpus;
                    corpus = corpus.toLowerCase().replaceAll("  ", " ");
                }
                //System.out.print(corpus+"\n");
                String res = TextSearcher.search(corpus, 5, 0);
                if (res != null && res.length() > 1) {
                    System.out.print(rs.getInt("id") + "\n" + res + " " + corpus + "\n");
                }
            }
        } finally {
            DatabaseWrapper.closeDBConnection(j);
            DatabaseWrapper.closePreparedStatement(ps);
        }
    }

    /**
     * Build counts of word occurrance into a hashtable
     * @param text
     * @return
     */
    public static Hashtable<String, Stack<Integer>> getMashup(String[] text) {
        Hashtable<String, Stack<Integer>> toret = new Hashtable();
        for (int i = 0; i < text.length; i++) {
            if (toret.containsKey(text[i])) {

                Stack cur = toret.get(text[i]);

                cur.push(i);
                //System.out.print("pushing a val \n");

            } else {
                Stack s = new Stack<Integer>();
                s.push(i);
                toret.put(text[i], s);
            }
        }
        return toret;
    }

    /**
     * Custom string comparison intended to match words with slightly different endings in a very crude fashion. ie mud and muddle would match.
     * @param s1
     * @param s2
     * @return
     */
    public static Boolean matchStrings(String s1, String s2) {
        if (s1.length() <= s2.length()) {
            int matchCount = 0;
            for (int i = 0; i < s1.length(); i++) {
                if (s1.charAt(i) == s2.charAt(i)) {
                    matchCount++;
                }
            }
            if (s1.length() == matchCount) {
                return true;
            }
        } else {

            int matchCount = 0;
            for (int i = 0; i < s2.length(); i++) {
                if (s1.charAt(i) == s2.charAt(i)) {
                    matchCount++;
                }
            }
            if (s1.length() == matchCount) {
                return true;
            }
        }
        return false;
    }
}
