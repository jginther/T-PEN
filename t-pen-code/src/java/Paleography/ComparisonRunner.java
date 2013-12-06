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
package Paleography;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Hashtable;
import java.util.Vector;
import detectimages.blob;
import detectimages.blobManager;
import detectimages.overloadLoader;
import detectimages.pageComparer;
import textdisplay.DatabaseWrapper;
import textdisplay.Folio;
import textdisplay.Manuscript;

/**
 * This class runs paleographic feature comparisons
  */
public class ComparisonRunner {

    /**
     * This version runs comparisons of every available image in the manuscript against every other available image in the manuscript.
     * @param m the manuscript object to run the comparisons on
     * @throws SQLException
     * @throws FileNotFoundException
     * @throws IOException
     */
    public ComparisonRunner(Manuscript m) throws SQLException, FileNotFoundException, IOException {
        //get the folios from this Manuscript that have existing character data suitable for comparison
        String query = "select * from folios where msID=? and paleography!='0000-00-00 00:00:00' order by sequence,pageName";
        Connection j = null;
        PreparedStatement ps = null;
        String fileLocation = Folio.getRbTok("PALEODATADIR") + "/" + m.getID() + "/";
        blobManager bm = new blobManager();
        try {
            j = DatabaseWrapper.getConnection();
            ps = j.prepareStatement(query);
            ps.setInt(1, m.getID());
            ResultSet rs = ps.executeQuery();
            Vector<Integer> folioIDs = new Vector();
            File outputLoc = new File(Folio.getRbTok("PALEOTEMPDIR") + fileLocation);
            if (!outputLoc.exists()) {
                outputLoc.mkdirs();
            }
            File[] existingData = outputLoc.listFiles();
            for (int ctr = 0; ctr < existingData.length; ctr++) {
                existingData[ctr].delete();
            }
            while (rs.next()) {
                folioIDs.add(rs.getInt("pageNumber"));
            }
            //Compare every image to every other image
            for (int i = 0; i < folioIDs.size(); i++) {
                for (int k = i + 1; k < folioIDs.size(); k++) {

                    String[] assignment = new String[2];
                    assignment[0] = fileLocation + folioIDs.get(i) + ".txt";
                    assignment[1] = fileLocation + folioIDs.get(k) + ".txt";
                    System.out.print(assignment[0] + " : " + assignment[1] + "\n");
                    Vector<blob> blobs = bm.get(fileLocation + folioIDs.get(i) + ".txt");
                    if (blobs == null) {
                        bm.add(blob.getBlobs(fileLocation + folioIDs.get(i) + ".txt"), fileLocation + folioIDs.get(i) + ".txt");
                        blobs = blobs = bm.get(fileLocation + folioIDs.get(i) + ".txt");
                    }
                    pageComparer comparer = new pageComparer(blobs, fileLocation + folioIDs.get(k) + ".txt", assignment, bm);
                    comparer.setOutputLocation(Folio.getRbTok("PALEOTEMPDIR"));
                    comparer.call();
                }
            }
            overloadLoader n = new overloadLoader(new File(Folio.getRbTok("PALEOTEMPDIR") + fileLocation), new Hashtable(), new Hashtable(), true);
            //overloadLoader.setCharCounts();
        } finally {
            DatabaseWrapper.closeDBConnection(j);
            DatabaseWrapper.closePreparedStatement(ps);
        }
    }

    /**
     * Runs comparisons of the specified folio against all other available images in the manuscript. Much quicker than doing a full manuscript run if you dont need that.
     * @param f the folio to run
     * @throws SQLException
     * @throws FileNotFoundException
     * @throws IOException
     */
    public ComparisonRunner(Folio f) throws SQLException, FileNotFoundException, IOException {
        //get the folios from this Manuscript that have existing character data suitable for comparison
        String query = "select * from folios where msID=? and paleography!='0000-00-00 00:00:00' order by sequence,pageName";
        Connection j = null;
        PreparedStatement ps = null;
        Manuscript m = new Manuscript(f.getFolioNumber());
        String fileLocation = Folio.getRbTok("PALEODATADIR") + "/" + m.getID() + "/";
        blobManager bm = new blobManager();
        try {
            j = DatabaseWrapper.getConnection();
            ps = j.prepareStatement(query);
            ps.setInt(1, m.getID());
            ResultSet rs = ps.executeQuery();
            Vector<Integer> folioIDs = new Vector();
            File outputLoc = new File(Folio.getRbTok("PALEOTEMPDIR") + fileLocation);
            if (!outputLoc.exists()) {
                outputLoc.mkdirs();
            }
            File[] existingData = outputLoc.listFiles();
            for (int ctr = 0; ctr < existingData.length; ctr++) {
              //  existingData[ctr].delete();
            //}
            if(existingData[ctr].getName().contains(""+f.getFolioNumber()))
            {
                return;
            }
            }
            
            while (rs.next()) {
                folioIDs.add(rs.getInt("pageNumber"));
            }
            //Compare every image to every other image
            for (int i = 0; i < folioIDs.size(); i++) {
                if (folioIDs.get(i) == f.getFolioNumber()) {
                    for (int k = i + 1; k < folioIDs.size(); k++) {
                        String[] assignment = new String[2];
                        assignment[0] = fileLocation + folioIDs.get(i) + ".txt";
                        assignment[1] = fileLocation + folioIDs.get(k) + ".txt";
                        System.out.print(assignment[0] + " : " + assignment[1] + "\n");
                        Vector<blob> blobs = bm.get(fileLocation + folioIDs.get(i) + ".txt");
                        if (blobs == null) {
                            bm.add(blob.getBlobs(fileLocation + folioIDs.get(i) + ".txt"), fileLocation + folioIDs.get(i) + ".txt");
                            blobs = blobs = bm.get(fileLocation + folioIDs.get(i) + ".txt");
                        }
                        pageComparer comparer = new pageComparer(blobs, fileLocation + folioIDs.get(k) + ".txt", assignment, bm);
                        comparer.setOutputLocation(Folio.getRbTok("PALEOTEMPDIR"));
                        comparer.call();
                    }
                }
            }
            //overloadLoader n = new overloadLoader(new File(Folio.getRbTok("PALEOTEMPDIR") + fileLocation), new Hashtable(), new Hashtable(), true);
            //overloadLoader.setCharCounts();
        } finally {
            DatabaseWrapper.closeDBConnection(j);
            DatabaseWrapper.closePreparedStatement(ps);
        }
    }
}
