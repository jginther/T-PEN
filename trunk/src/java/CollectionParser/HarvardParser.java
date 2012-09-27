/*
 * @author Jon Deering
Copyright 2011 Saint Louis University. Licensed under the Educational Community License, Version 2.0 (the "License"); you may not use
this file except in compliance with the License.

You may obtain a copy of the License at http://www.osedu.org/licenses/ECL-2.0

Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions
and limitations under the License.
 */

package CollectionParser;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.net.ConnectException;
import java.net.MalformedURLException;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;
import textdisplay.DatabaseWrapper;
import textdisplay.Manuscript;

public class HarvardParser {
    /**
     * (@TODO:  Complete.)
     * 
     * This is currently the most-complicated parser.  First, it loads
     * a listing of the manuscript collections (English, Latin, etc)
     * they (@BOZO:  Who's "they"?--Harvard?) host.  Second,
     * for each manuscript collection, it loads a manuscript listing
     * (ex. Lat 140).  Third, for each collection listing, it loads
     * a contents list.  Fourth, it follows the link provided
     * for each image to actually find the image url.
     * 
     * @param args
     * @throws MalformedURLException
     * @throws IOException 
     */
    public static void main(String[] args) throws MalformedURLException, IOException
    {
        HarvardParser h=new HarvardParser();
        Boolean added=false;
        String toret="";
        
        System.out.print("starting");
        String url = "http://hcl.harvard.edu/libraries/houghton/collections/early_manuscripts/about.cfm";
        URL mslisting = new URL(url);StringBuilder bBuilder=new StringBuilder("");
        BufferedReader breader = null;
        try
        {
            breader=new BufferedReader(new InputStreamReader(mslisting.openConnection().getInputStream()));
        }
        catch(ConnectException e)
        {
            breader=new BufferedReader(new InputStreamReader(mslisting.openConnection().getInputStream()));
        }
        
        if (!breader.ready()) {
            System.out.print("reader not ready, stalling .5 seconds!\n");
            try {
                Thread.sleep(500);//sleep for 1000 ms
            }
            catch (InterruptedException ie) {
                //If this thread was intrrupted by another thread
            }
        }
        while(breader.ready())
        {
            bBuilder.append(breader.readLine()+"\n");
            if (!breader.ready()) {
                System.out.print("reader not ready, stalling .5 seconds!\n");
                try {
                    Thread.sleep(500);//sleep for 1000 ms
                }
                catch (InterruptedException ie) {
                    //If this thread was intrrupted by another thread
                }
            }
        }
        
        BufferedReader b = new BufferedReader(new StringReader(bBuilder.toString()));
        String buf = "";
        Boolean in = false;
        int ctr = 0;
        
        while (b.ready()) {
            // if(ctr>3)
            //   return urls;
            
            buf = b.readLine();
            System.out.print("buf:"+buf+"\n");
            //System.out.flush();
            // urls+=buf;
            
            if (buf.contains("<a href=\"/libraries/houghton/collections/early_manuscripts/bibliographies/")) {
                
                try {
                    //if(!buf.contains("#"))
                    {
                        ctr++;
                        System.out.print("added\n");
                        added = true;
                        String s = buf.split("href=\"")[1].split("\"")[0];
                        toret = "junk," + "http://hcl.harvard.edu" + s;
                        //urls+=s+"/";
                    }
                } catch (Exception e) {
                }
            }
            
            if (added) {
                added = false;
                URL documentListing = new URL(toret.split(",")[1]);
                BufferedReader dLR=new BufferedReader(new InputStreamReader(documentListing.openConnection().getInputStream()));
                
                if (!dLR.ready()) {
                    System.out.print("Dcoument reader not ready, sleeping .5 seconds!\n");
                    try {
                        Thread.sleep(500);//sleep for 1000 ms
                    } catch (InterruptedException ie) {
                        //If this thread was intrrupted by nother thread
                    }
                }
                StringBuilder dLRBuilder=new StringBuilder("");
                while(dLR.ready())
                {
                    dLRBuilder.append(dLR.readLine()+"\n");
                    int retryCtr=0;
                    while (!dLR.ready()) {
                        System.out.print("Dcoument reader not ready, sleeping .5 seconds!\n");
                        try {
                            Thread.sleep(500);//sleep for 1000 ms
                        } catch (InterruptedException ie) {
                            //If this thread was intrrupted by nother thread
                        }
                        retryCtr++;
                        if(retryCtr>4)
                            break;
                    }
                }
                
                BufferedReader documentListingReader = new BufferedReader(new StringReader(dLRBuilder.toString()));
                if(documentListingReader.ready()){
                    String msName = "";
                    while (documentListingReader.ready()) {
                        String buffer = documentListingReader.readLine();
                        if(buffer!=null)
                        {
                            if (buffer.contains("target=\"_blank\">MS")) {
                                msName = buffer.split("target=\"_blank\">")[1].split("</a>")[0];
                            }
                            if (buffer.contains("Digital    Images</a>") || buffer.contains("Digital    Facsimile</a>")) {
                                String msURL = buffer.split("href=\"")[1].split("\"")[0];
                                System.out.print("Would be adding " + msName + " with url " + msURL + "\n");
                                h.findImages(msURL, null);
                            }
                        }
                        else
                        {
                            break;
                        }
                    }
                    //find the facsimile and digital image links on this MS listing
                }
                
            }
        }
    }
    
    
    private int findImages(String msURL, Manuscript m) throws MalformedURLException, IOException
    {
        int addedCount=0;
        String marker = "navigation";
        URL msPageURL = new URL(msURL+"");
        BufferedReader msPage = this.fetchAndPrepare(msURL);
        int indentationCount=0;
        while (msPage.ready()) {
            String msBuffer = msPage.readLine();
            if (msBuffer==null)
                break;
            
            if (msBuffer.contains(marker) && msBuffer.contains("<frame")) {
                String navUrl = msBuffer.split("src=\"")[1].split("\"")[0];
                navUrl = "http://pds.lib.harvard.edu" + navUrl+"&treeaction=expand";
                URL navURL = new URL(navUrl);
                BufferedReader navPage = this.fetchAndPrepare(navUrl);
                Boolean nextLineIsCitation = false;
                String imageurl = "";
                Boolean titleSoon = false;
                while (navPage.ready()) {
                    String navBuffer = navPage.readLine();
                    if (navBuffer==null)
                        break;
                    
                    //if m isnt null, it was passed in, so leave it alone
                    if (nextLineIsCitation && m==null) {
                        try {
                            String citation = navBuffer;
                            nextLineIsCitation = false;
                            System.out.print("Citation is " + navBuffer.trim() + "\n");
                            navBuffer = "MS " + navBuffer.split("MS ")[1].split("\\.")[0];
                            Manuscript test = new Manuscript(navBuffer.trim(), "Harvard");
                            if (test.getID() == 0) {
                                m = new Manuscript("Houghton Library", "Harvard", navBuffer.trim(),"Cambridge, MA" );
                            } else {
                                m = test;
                                return 0;
                            }
                        } catch (Exception e) {
                            System.out.print("Citation didnt parse right\n!!!");
                        }
                    }
                    
                    if (navBuffer.contains("citationDiv")) {
                        nextLineIsCitation = true;
                    }
                    
                    if (navBuffer.contains("<a href=") ) {
                        imageurl = navBuffer.split("<a href=\"")[1].split("\"")[0];
                        imageurl = "http://pds.lib.harvard.edu" + imageurl + "&op=t";
                        // System.out.print(imageurl+"\n");
                        //imageurl=imageurl.replace("http://pds.lib.harvard.edu/pds", "http://ids.lib.harvard.edu/ids");
                        indentationCount=navBuffer.split("blank").length;
                        titleSoon = true;
                    }
                    if (!titleSoon)
                    {
                        if (navBuffer.contains("title") && navBuffer.contains("seq") && m != null)
                        {
                            System.out.print("Found content but not titlesoon!\n");
                        }
                        else
                        {
                            if (navBuffer.contains("title") && navBuffer.contains("seq"))
                            {
                                System.out.print("Found content but not titlesoon and m is null!\n");
                            }
                        }
                    }
                    if (titleSoon) {
                        if (navBuffer.contains("title") && navBuffer.contains("seq") && m != null) {
                            addedCount+=this.doInner(navBuffer, imageurl, m);
                        }
                        else {
                            if (navBuffer.contains("title") && navBuffer.contains("seq") ) {
                                System.out.print("m is null!\n");
                            }
                        }
                    }
                }
            }
        }
        return addedCount;
    }
    
    
    /**
     * 
     * @param navBuffer
     * @param imageurl
     * @param m
     * @return
     * @throws MalformedURLException
     * @throws IOException 
     */
    private int doInner(String navBuffer, String imageurl, Manuscript m) throws MalformedURLException, IOException
    {
        int addedCount=0;
        String title = navBuffer.split("title=\"")[1].split("\"")[0];
        if(navBuffer.split("seq")[1].contains("-"))
        {
            System.out.print("Suspect deeper indirection from title "+navBuffer+". Skipping.\n");
            //int cnt=this.findImages(imageurl.split("op=t")[0], m);
            //if(cnt>0)
                return 0;
        }
        
        try {
            Thread.sleep(5000);//sleep for 1000 ms
        } catch (InterruptedException ie) {
            //If this thread was intrrupted by nother thread
        }
        
        BufferedReader imagePage = new BufferedReader(new InputStreamReader(new URL(imageurl).openConnection().getInputStream()));
        while (imagePage.ready()) {
            //System.out.print("reading\n");
            String imagePageBuffer = imagePage.readLine();
            if (imagePageBuffer.contains("<img") && imagePageBuffer.contains("ids.lib")) {
                try {
                    String finalImageUrl = imagePageBuffer.split("src=\"")[1].split("\"")[0].split("xcap=")[0];
                    finalImageUrl = finalImageUrl.replace("s=.25", "s=.5");
                    finalImageUrl = finalImageUrl.replace("width=1200", "width=2400");
                    finalImageUrl = finalImageUrl.replace("height=1200", "height=2400");
                    finalImageUrl = finalImageUrl.replace("&amp;", "&");
                    
                    System.out.print(m.getCollection()+" "+title +"\n");
                    Connection j = null;
                    PreparedStatement checkPS=null;
                    PreparedStatement ps=null;
                    try {
                        try {
                            Thread.sleep(50);//sleep for 1000 ms
                        } catch (InterruptedException ie) {
                            //If this thread was intrrupted by nother thread
                        }
                        
                        j = DatabaseWrapper.getConnection();
                        String checkQuery="select * from folios where uri=?";
                        checkPS = j.prepareStatement(checkQuery);
                        checkPS.setString(1,finalImageUrl );
                        ResultSet rs=checkPS.executeQuery();
                        if(!rs.next())
                        {
                            String query = "insert into folios (msID, uri,pageName,imageName,collection,archive) values(?,?,?,?,?,?)";
                            ps = j.prepareStatement(query);
                            ps.setInt(1, m.getID());
                            ps.setString(2, finalImageUrl);
                            ps.setString(3, title);
                            ps.setString(4, finalImageUrl);
                            ps.setString(5, m.getCollection());
                            ps.setString(6, "Harvard");
                            ps.execute();
                            addedCount++;
                        }
                    } catch (Exception e) {
                       StackTraceElement[] el=e.getStackTrace();
                        for(int i=0;i<el.length;i++)
                        {
                            System.out.print(el[i].toString()+"\n" );
                        }
                    } finally {
                        DatabaseWrapper.closeDBConnection(j);
                        DatabaseWrapper.closePreparedStatement(ps);
                        DatabaseWrapper.closePreparedStatement(checkPS);
                    }
                } catch (Exception e) {
                    StackTraceElement[] el=e.getStackTrace();
                    for(int i=0;i<el.length;i++)
                    {
                        System.out.print(el[i].toString()+"\n" );
                    }
                }
            }
        }
        return addedCount;
    }
    
    
    /**
     * (@TODO:  Complete.)
     * 
     * @param url
     * @return
     * @throws MalformedURLException
     * @throws IOException 
     */
    public static BufferedReader fetchAndPrepare(String url) throws MalformedURLException, IOException
    {
        try {
            Thread.sleep(5000);//sleep for 1000 ms
        } catch (InterruptedException ie) {
            //If this thread was intrrupted by nother thread
        }
        
        try {
            BufferedReader dLR=new BufferedReader(new InputStreamReader(new URL(url).openConnection().getInputStream()));
            
            // if the reader's not ready, stall for a bit
            if (!dLR.ready()) {
                System.out.print("Dcoument reader not ready, sleeping .5 seconds!\n");
                try {
                    Thread.sleep(500);//sleep for 1000 ms
                } catch (InterruptedException ie) {
                    //If this thread was intrrupted by nother thread
                }
            }
            
            StringBuilder dLRBuilder=new StringBuilder("");
            while(dLR.ready())
            {
                dLRBuilder.append(dLR.readLine()+"\n");
                int retryCtr=0;
                while (!dLR.ready()) {
                    System.out.print("Dcoument reader not ready, sleeping .5 seconds!\n");
                    try {
                        Thread.sleep(500);//sleep for 1000 ms
                    } catch (InterruptedException ie) {
                        //If this thread was intrrupted by nother thread
                    }
                    retryCtr++;
                    if(retryCtr>14)
                        break;
                }
            }
            
            BufferedReader toret = new BufferedReader(new StringReader(dLRBuilder.toString()));
            return toret;
        }
        catch(Exception e)
        {}
        
        // BOZO:  Should this be in the catch{}? or a finally{}?
        BufferedReader toret = new BufferedReader(new StringReader(""));
        return toret;
    }
}
