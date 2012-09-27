/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package CollectionParser;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;
import textdisplay.Archive;
import textdisplay.DatabaseWrapper;
import textdisplay.Folio;
import textdisplay.Manuscript;

public class UniversityOfOregonParser {
    
    /**
     * (@TODO:  Complete.)
     * 
     * @param archive
     * @param cid
     * @param id
     * @throws MalformedURLException
     * @throws IOException
     * @throws SQLException 
     */
    public static void parse(String archive, int cid, int id) throws MalformedURLException, IOException, SQLException {
        if (archive.compareTo("University of Oregon") == 0) {
            Vector<Integer> nums = new Vector();
            Vector<String> labels = new Vector();
            String base = "https://oregondigital.org/cdm4/document.php?CISOROOT=/petrarch&CISOPTR=" + cid + "&REC=3";
            URL mslisting = new URL(base);
            BufferedReader b = new BufferedReader(new InputStreamReader(mslisting.openConnection().getInputStream()));
            
            String buf = "";
            Boolean in = false;
            int ctr = 0;
            
            // if our reader's not ready, stall for a quick bit
            // BOZO:  Should this be a loop?
            if (!b.ready()) {
                System.out.print("reader not ready, stalling .5 seconds!\n");
                try {
                    Thread.sleep(500);//sleep for 1000 ms
                } catch (InterruptedException ie) {
                    //If this thread was intrrupted by nother thread
                }
            }
            
            // actually read in the buffer contents
            while (b.ready()) {
                buf = b.readLine();
                if (buf.contains("CISOPTR=")) {
                    try {
                        String num = buf.split("CISOPTR=")[1].split("&")[0];
                        String label = buf.split("\"")[3];
                        labels.add(label);
                        int cisoptr = Integer.parseInt(num);
                        nums.add(cisoptr);
                    } catch (Exception e) {
                        //dont care
                    }
                }
            }
            
            Manuscript ms = new Manuscript(id, true);
            for (int i = 0; i < nums.size(); i++) {
                String imgstr = "https://oregondigital.org/cgi-bin/getimage.exe?CISOROOT=/petrarch&CISOPTR=" + nums.get(i) + "&DMSCALE=100&DMWIDTH=5000&DMHEIGHT=5000&DMX=0&DMY=0&DMTEXT=&REC=1&DMTHUMB=0&DMROTATE=0";
                Connection j = null;
                PreparedStatement ps = null;
                try {
                    j = DatabaseWrapper.getConnection();
                    String query = "insert into folios (msID, uri,pageName,imageName,collection,archive) values(?,?,?,?,?,?)";
                    ps = j.prepareStatement(query);
                    ps.setInt(1, id);
                    ps.setString(2, imgstr);
                    ps.setString(3, labels.get(i));
                    ps.setString(4, imgstr);
                    ps.setString(5, ms.getCollection());
                    ps.setString(6, "University of Oregon");
                    ps.execute();
                    
                } finally {
                    DatabaseWrapper.closeDBConnection(j);
                    DatabaseWrapper.closePreparedStatement(ps);
                }
            }
        }
    }
}
