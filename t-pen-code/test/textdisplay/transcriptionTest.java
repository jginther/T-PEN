/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package textdisplay;

import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author ijrikgnmd
 */
public class transcriptionTest {
    
    public transcriptionTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }
    
    @Before
    public void setUp() {
        try {
            //create a transcription for user 1, folio 1, with coordinates 0,1,200,400
            
            transcription t=new transcription(1, 1,  0, 1, 200, 400, false);
            t=new transcription(1, 1,  0, 1, 200, 400, true);
            
            
        } catch (SQLException ex) {
            Logger.getLogger(transcriptionTest.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    @After
    public void tearDown() {
    }

    /**
     * Test of setComment method, of class transcription.
     */
    @Test
    public void testSetComment() throws Exception {
    }

    /**
     * Test of setText method, of class transcription.
     */
    @Test
    public void testSetText() throws Exception {
    }

    /**
     * Test of setHeight method, of class transcription.
     */
    @Test
    public void testSetHeight() throws Exception {
    }

    /**
     * Test of setWidth method, of class transcription.
     */
    @Test
    public void testSetWidth() throws Exception {
    }

    /**
     * Test of setX method, of class transcription.
     */
    @Test
    public void testSetX() throws Exception {
    }

    /**
     * Test of setY method, of class transcription.
     */
    @Test
    public void testSetY() throws Exception {
    }

    /**
     * Test of getHeight method, of class transcription.
     */
    @Test
    public void testGetHeight() {
    }

    /**
     * Test of getLineID method, of class transcription.
     */
    @Test
    public void testGetLineID() {
    }

    /**
     * Test of getTimestamp method, of class transcription.
     */
    @Test
    public void testGetTimestamp() {
    }

    /**
     * Test of getWidth method, of class transcription.
     */
    @Test
    public void testGetWidth() {
    }

    /**
     * Test of getX method, of class transcription.
     */
    @Test
    public void testGetX() {
    }

    /**
     * Test of getY method, of class transcription.
     */
    @Test
    public void testGetY() {
    }

    /**
     * Test of getComment method, of class transcription.
     */
    @Test
    public void testGetComment() {
    }

    /**
     * Test of getFolio method, of class transcription.
     */
    @Test
    public void testGetFolio() {
    }

    /**
     * Test of getLine method, of class transcription.
     */
    @Test
    public void testGetLine() {
    }

    /**
     * Test of remove method, of class transcription.
     */
    @Test
    public void testRemove() throws Exception {
    }

    /**
     * Test of getText method, of class transcription.
     */
    @Test
    public void testGetText() {
    }

    /**
     * Test of getProjectTranscriptions method, of class transcription.
     */
    @Test
    public void testGetProjectTranscriptions() throws Exception {
    }

    /**
     * Test of main method, of class transcription.
     */
    @Test
    public void testMain() throws Exception {
    }

    /**
     * Test of getPersonalTranscriptions method, of class transcription.
     */
    @Test
    public void testGetPersonalTranscriptions() throws Exception {
    }

    /**
     * Test of getOAC method, of class transcription.
     */
    @Test
    public void testGetOAC() throws Exception {
    }

    /**
     * Test of commit method, of class transcription.
     */
    @Test
    public void testCommit() throws Exception {
    }

    /**
     * Test of makeCopy method, of class transcription.
     */
    @Test
    public void testMakeCopy() throws Exception {
    }

    /**
     * Test of updateColumnWidth method, of class transcription.
     */
    @Test
    public void testUpdateColumnWidth() throws Exception {
    }

    /**
     * Test of updateColumnLeft method, of class transcription.
     */
    @Test
    public void testUpdateColumnLeft() throws Exception {
    }
}
