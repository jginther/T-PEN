/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package CollectionParser;

import dmstech.canvas;
import dmstech.manifestAggregation;
import dmstech.sequence;
import dmstech.thing;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.mail.*;
import javax.mail.internet.*;
import textdisplay.Archive;
import textdisplay.DatabaseWrapper;
import textdisplay.Folio;
import textdisplay.Manuscript;
import textdisplay.mailer;

/**
 * A parser for Shared Canvas repositories.  Generalizable, but currently set up
 * for pulling data from Stanford via <http://dms-data.stanford.edu/>.
 * 
 * @see <http://www.shared-canvas.org/datamodel/spec/>
 * @author jdeering
 */
public class StanfordParser {
    
    private static int NUM_COLLECTIONS = 5;
    
    private static String REPO_XML_URI = "http://dms-data.stanford.edu/Repository.xml";
    
    /**
     * (@TODO:  Complete.)
     * Adds into T-PEN's database any manuscript data
     * that has not yet been recorded.
     * 
     * @param archive
     * @throws IOException 
     */
    public static void parse(String archive) throws IOException {
        String mailServer = "slumailrelay.slu.edu";
        String mailFrom = "TPEN@t-pen.org";
        String mailTo = "teague.costas@uky.edu";
        
        // create a HashMap for our collection data
        //      [0] => (String) name of the collection
        //      [1] => (boolean) whether to parse to collection
        HashMap collParams = new HashMap(NUM_COLLECTIONS);
        collParams.put("Walters", new Object[] {"Walters", true});
        collParams.put("Stanford", new Object[] {"Stanford", false});
        collParams.put("Parker", new Object[] {"Parker", false});
        collParams.put("BnF", new Object[] {"BnF", false});
        collParams.put("Oxford", new Object[] {"Oxford", false});
        
        Object[] currCollParams = null;
        String currCollName = "(null)";
        boolean currCollParse = false;
        
        // get a listing of collections (each collection is a listing of manifests;
        // each manifest is an aggregation of... data points)
        String[] res = thing.getManifestAggregations(REPO_XML_URI);
        for (int i=0; i<res.length; i++) {
            System.out.println("\nCOLLECTION[i="+i+"]");
            System.out.println("--URI="+res[i]);
            
            // parse out the collection name, assuming the format is:
            // "(.*)/COLL_NAME/([^/]*)"
            currCollName = res[i].substring(0, res[i].lastIndexOf("/"));
            currCollName = currCollName.substring(currCollName.lastIndexOf("/")+1);
            System.out.println("--name="+currCollName);
            
            try {
                currCollParams = (Object[])collParams.get(currCollName);
            }
            catch(NullPointerException e) {
                // we don't have an entry for this collection
                currCollParams = null;
            }
            
            // if we don't have an entry for this collection, skip ahead
            if (currCollParams == null) {
                System.out.println("--name not recognized; skipping");
                continue;
            }
            
            System.out.println("--name recognized; found parameters at name '"+currCollParams[0].toString()+"'");
            System.out.println("--parsing: "+(((Boolean)currCollParams[1])?"yes":"no"));
            System.out.println();
            
            //
            // Collection loop
            //
            if ((Boolean)currCollParams[1] == false) {
                // skip this collection
//                System.out.println("--skipping");
                continue;
            }
            else {
                
                // get URLs for all manuscript manifests in this collection
                String[] p = manifestAggregation.getManifestUrls(res[i]);
                
                // loop over each manuscript-manifest URL (in this collection)
                for (int j=0; j<p.length; j++) {
                    System.out.print("\t--at [j="+j+"]=");
                    System.out.print(p[j] + "\n");
                    
                    try {
                        // get the Normal sequence for the manuscript manifest
                        sequence s = new sequence(new URL[]{new URL(p[j])}, "");
                        
//                        System.out.println("\t\tfinished instantiating sequence; N="+s.getSequenceItems().length);
                        System.out.println("\t\t\tFiling Meta: "+
                                           "["+
                                                "REPO="+s.getRepository()+
                                                "; ARCH="+"Stanford"+
                                                "; COLL="+s.getCollection()+
                                                "; CITY="+s.getCity()+
                                           "]");
                        
                        System.out.println("\t\tloading image data...");
                        
                        // load all image data
                        try {
                            // the sequence might be empty--meaning the manuscript
                            // might not have any canvases that actually have images
                            mailer mail = new mailer();
                            if (s==null || s.getSequenceItems()==null || !(s.getSequenceItems().length>0)) {
                                String errorMsg = "Found no canvases with images for URL[j="+j+"]" + p[j] + "\n" +
                                                  "\n" +
                                                  "Collection[i="+i+"] = \""+res[i]+"\"\n" +
                                                  "\n";
                                try {
                                    System.out.println("\t\tERROR:  Found no canvases with images!");
                                    // public void sendMail(to, subject, messageBody) throws MessagingException, AddressException
                                    mail.sendMail(mailTo, "T-PEN: Error loading Stanford MSS", errorMsg);
                                }
                                catch (Exception e) {
                                    // BOZO:  Mailing failed.  Print message?  Log error?
                                    System.out.print("ERROR:  Failed to send e-mail; msg="+errorMsg);
                                }
                            }
                            
                            canvas[] canvases = s.getSequenceItems();
                            try {
                                //public Manuscript(String repository, String Archive, String collection, String city) throws SQLException
                                //
                                Manuscript m = new Manuscript(s.getRepository(), "Stanford", s.getCollection(), s.getCity());
                                String msg = "Added manuscript; "+"Manuscript ID="+m.getID()+" is now "+m.getShelfMark()+". It has N="+canvases.length+" canvases.";
                                Logger.getAnonymousLogger().log(Level.SEVERE, msg);
                                
                                mail.sendMail(mailTo, "T-PEN: Added manuscript", msg);
                                
                                // for each canvas, get and store the image URLs
                                String imageURL;
                                for (int l=0; l<canvases.length; l++) {
                                    imageURL = canvases[l].getImageURL()[0].getImageURL();
                                    
                                    System.out.println("\t\t\tAt canvas[l="+l+"]");
                                    System.out.println("\t\t\t\tURI="+imageURL);
                                    System.out.println("\t\t\t\tRecording images...");
                                    
                                    Folio.createFolioRecord(s.getCollection(), canvases[l].getTitle(), imageURL, "Stanford", m.getID(), canvases[l].getPosition(), canvases[l].getCanvas());
                                    
                                }
                                
                            }
                            catch (Exception e) {
                            }
                            
                        }                    catch (Exception e) {
// CUBAP inserted to compile
                        }

                        
                    }
                    catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                
            }
        }
        
    }
    
    
    /**
     * (@TODO: Complete.)
     * 
     * @param manifestURL
     * @throws MalformedURLException
     * @throws IOException
     * @throws SQLException 
     */
    public static void parseSingleMS(String manifestURL) throws MalformedURLException, IOException, SQLException {
        sequence s = new sequence(new URL[]{new URL(manifestURL)}, "");
        canvas[] canvases = s.getSequenceItems();
        Manuscript m = new Manuscript(s.getRepository(), "Stanford", s.getCollection(), s.getCity());
        for (int l=0; l<canvases.length; l++) {
            Folio.createFolioRecord(s.getCollection(), canvases[l].getTitle(), canvases[l].getImageURL()[0].getImageURL(), "Stanford", m.getID(), canvases[l].getPosition(), canvases[l].getCanvas());
        }
    }
    
    
    /**
     * Runs this (Stanford) parser, using the manifest XML file found
     * via the hardcoded URI.
     * 
     * @param args 
     */
    public static void main(String[] args) {
        try {
            StanfordParser.parseSingleMS("http://dms-data.stanford.edu/Walters/nh004tn3893/Manifest.xml");
        } catch (MalformedURLException ex) {
            Logger.getLogger(StanfordParser.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(StanfordParser.class.getName()).log(Level.SEVERE, null, ex);
        } catch (SQLException ex) {
            Logger.getLogger(StanfordParser.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
}
