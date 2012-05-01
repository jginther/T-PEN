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

import java.io.BufferedReader;
import java.io.IOException;
import java.util.Stack;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.lucene.index.*;
import org.apache.lucene.document.*;
import org.apache.lucene.store.*;
import org.apache.lucene.analysis.*;
import org.apache.lucene.analysis.standard.*;
import org.apache.lucene.search.*;
import org.apache.lucene.queryParser.*;
import org.apache.lucene.search.highlight.Formatter;
import org.apache.lucene.search.highlight.Highlighter;
import org.apache.lucene.search.highlight.QueryScorer;
import org.apache.lucene.search.highlight.SimpleHTMLFormatter;
import org.apache.lucene.search.highlight.TokenGroup;
import textdisplay.Archive;
import textdisplay.Transcription;
import textdisplay.Folio;
import user.User;

/**Carries out searches on the transcriptions in TPEN, respecting access controls along the way*/
public class SearchExecutor {

    BufferedReader reader;
    Field field;
    Document doc;
    Analyzer analyser;
    Directory directory;
    IndexWriter writer;
    String fileData;
    String[] stubs;
    String line;
    String def;
    private Boolean error = false;
    private int totalHits;
    private int totalPages;

    /**
     *
     * @throws IOException
     */
    public SearchExecutor() throws IOException {
        fileData = "";
        directory = FSDirectory.getDirectory("/usr/web/tosend/indexENAP", false);
        //Read this location from an XML config file
        analyser = new StandardAnalyzer();


    }

    /**
     * get the number of search hits
     * @return the number of search hits
     */
    public int getTotalHits() {
        return totalHits;
    }

    /**
     * get the number of pages of hits given the speciified paging scheme
     * @return number of pages of hits given the speciified paging scheme
     */
    public int getTotalPages() {
        return totalPages;
    }

    /**
     * Search user generated transcriptions. Results contain embedded images. Results are restricted to those the user usrID has
     * permissing to view the transcription
     * @param searchWord
     * @param language
     * @param order
     * @param paged
     * @param pageNumber
     * @param usrID
     * @return
     * @throws Exception
     */
    public Stack<Transcription> transcriptionSearch(String searchWord, String language, int order, Boolean paged, int pageNumber, String usrID) throws Exception {
        Boolean wildcard = true;
        final int pageSize = 20; //Number of results per page, could be made a parm one day
        final int maxResults = 1000; //No matter what dont return more than this many results from Lucene. This is ok because result filtering occurs before this limitation is applied
        String returnStringArray = "";
        //we dont currently worry about language filtering, but ENAP did, so we could do it if we wanted to
        if (language != null && language.length() > 1) {
            //searchWord=searchWord+" AND lang:"+language;
        }
        /**@TODO the location should be a param*/
        IndexSearcher is = new IndexSearcher("/usr/indexTranscriptions");
        QueryParser parser = new QueryParser("text", analyser);
        Sort newsort;
        Query query = parser.parse(searchWord);
        is.rewrite(parser.parse(searchWord));
        QueryScorer queryScorer = new QueryScorer(query);
        ScoreDoc[] hits;
        //If the person wasnt logged in, give them only public comments. comment owner
        if (usrID.compareTo("") == 0) {
            usrID = "0";
        }
        Query secQuery = parser.parse("security:private OR creator:" + usrID);
        //This will filter search results so only comments owned by the user and public comments will be returned
        QueryFilter secFilter = new QueryFilter(query);
        //If a sort was specified, use it, otherwise use the default sorting which is by hit quality
        if (order > 0) {
            //order=1 means sort by line number, first line of the text is first.
            //order=2 means inverse sort by line number, last line of the text is first.
            //Java, the Nanny language, doesn't want to let me use newsort even if I ensure its not a null pointer
            //So if were going to use a filter, set the filter to type 1, then check to see if it should be something else.

            try {
                hits = is.search(query, secFilter, maxResults).scoreDocs;
            } catch (org.apache.lucene.search.BooleanQuery.TooManyClauses e) {
                return null;
            }
        } else {
            try {
                newsort = new Sort("creator", false);
                hits = is.search(query, secFilter, maxResults, newsort).scoreDocs;

            } catch (org.apache.lucene.search.BooleanQuery.TooManyClauses e) {
                return null;
            }
        }
        //Start at the hit that belongs at the top of the page they requested. For page 2, that is 19
        //Ensure we do not print more than pageNumber hits, or go beyond the end of the hit list
        String link = "";
        int ctr = 1;
        Stack<Transcription> results = new Stack();
        SimpleHTMLFormatter formatter = new SimpleHTMLFormatter("<span class=\"highlight\">", "</span>");
        Highlighter highlighter = new Highlighter(formatter, queryScorer);

        if (pageSize * (pageNumber - 1) < hits.length) {
            returnStringArray += "Your search for \"<b>" + searchWord + "</b>\" returned " + hits.length + " results.<br/>";
            for (int i = pageSize * (pageNumber - 1); i < hits.length && i - (pageSize * (pageNumber - 1)) < pageSize; i++) {

                Document hitDoc = is.doc(hits[i].doc);

                field = hitDoc.getField("line");
                Transcription t = new Transcription(Integer.parseInt(hitDoc.getField("id").stringValue()));
                results.add(t);
                String paragraph = field.stringValue();
                String pageno = "";
                String creator = hitDoc.getField("creator").stringValue();
                user.User u = new User(Integer.parseInt(creator));
                creator = "" + u.getLname() + " " + u.getFname();
                if (isInteger(paragraph)) {

                    field = hitDoc.getField("page");
                    pageno = field.stringValue();
                    if (pageno == null) {
                        pageno = "hi null";
                    }
                    if (paragraph == null) {
                        paragraph = "hola null";
                    }
                } else {
                    String folio = "";
                    Folio f = new Folio(Integer.parseInt(folio));
                    link = "&nbsp;&nbsp;&nbsp;<a href=transcriptionImageTest.jsp?p=" + folio + ">" + field.stringValue() + "(Archive:" + f.getArchive() + " Shelfmark:" + f.getCollectionName() + " page:" + folio + ")</a>";
                }
                returnStringArray = returnStringArray + (ctr + ". " + link + "<br/>");
                ctr++;
            }
        } else /*we dont have any results for the page/search they gave us*/ {
            returnStringArray = "No results to display.";
        }
        totalHits = hits.length;
        totalPages = hits.length / pageSize;
        if (hits.length % pageSize > 0) {
            totalPages++;
        }
        if (!wildcard) {
            return results;
        } else {
            return results;
        }
    }

    /**
     * Does the input String parse to an integer without error?
     * @param input A string you think is a number
     * @return
     */
    private boolean isInteger(String input) {
        try {
            Integer.parseInt(input);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
