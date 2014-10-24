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
public class AnnotationTest {
    
    public AnnotationTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
        Annotation a=new Annotation(1,2,"hello",3,4,5,6);
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }
    
    @Before
    public void setUp() {
    }
    
    @After
    public void tearDown() {
    }

    /**
     * Test of getFolio method, of class Annotation.
     */
    @Test
    public void testGetFolio() {
        try {
            Annotation a=new Annotation(1,2,"hello",3,4,5,6);
            assertEquals(a.getFolio(),1);
                
        } catch (SQLException ex) {
            Logger.getLogger(AnnotationTest.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Test of getH method, of class Annotation.
     */
    @Test
    public void testGetH() {
        try {
            Annotation a=new Annotation(1,2,"hello",3,4,5,6);
            assertEquals(a.getH(),5);
                
        } catch (SQLException ex) {
            Logger.getLogger(AnnotationTest.class.getName()).log(Level.SEVERE, null, ex);
        }
        
    }

    /**
     * Test of getId method, of class Annotation.
     */
    @Test
    public void testGetId() {
        try {
            Annotation a=new Annotation(1);
            assertEquals(a.getId(),1);
                
        } catch (SQLException ex) {
            Logger.getLogger(AnnotationTest.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Test of getProjectID method, of class Annotation.
     */
    @Test
    public void testGetProjectID() {
        try {
            Annotation a=new Annotation(1,2,"hello",3,4,5,6);
            assertEquals(a.getProjectID(),2);
                
        } catch (SQLException ex) {
            Logger.getLogger(AnnotationTest.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Test of getText method, of class Annotation.
     */
    @Test
    public void testGetText() {
        try {
            Annotation a=new Annotation(1,2,"hello",3,4,5,6);
            assertEquals(a.getText(),"hello");
                
        } catch (SQLException ex) {
            Logger.getLogger(AnnotationTest.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Test of getW method, of class Annotation.
     */
    @Test
    public void testGetW() {
        try {
            Annotation a=new Annotation(1,2,"hello",3,4,5,6);
            assertEquals(a.getW(),6);
                
        } catch (SQLException ex) {
            Logger.getLogger(AnnotationTest.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Test of getX method, of class Annotation.
     */
    @Test
    public void testGetX() {
        try {
            Annotation a=new Annotation(1,2,"hello",3,4,5,6);
            assertEquals(a.getX(),3);
                
        } catch (SQLException ex) {
            Logger.getLogger(AnnotationTest.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Test of getY method, of class Annotation.
     */
    @Test
    public void testGetY() {
        try {
            Annotation a=new Annotation(1,2,"hello",3,4,5,6);
            assertEquals(a.getY(),4);
                
        } catch (SQLException ex) {
            Logger.getLogger(AnnotationTest.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Test of updateAnnotationContent method, of class Annotation.
     */
    @Test
    public void testUpdateAnnotationContent() throws Exception {
        try {
            Annotation a=new Annotation(1,2,"hello",3,4,5,6);
            String newText="welcome";
            a.updateAnnotationContent(newText);
            assertEquals(newText,a.getText());
            a=new Annotation(a.getId());
            assertEquals(newText,a.getText());
        } catch (SQLException ex) {
            Logger.getLogger(AnnotationTest.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Test of updateAnnoationPosition method, of class Annotation.
     */
    @Test
    public void testUpdateAnnoationPosition() throws Exception {
        try {
            Annotation a=new Annotation(1,2,"hello",3,4,5,6);
            a.updateAnnoationPosition(7, 8, 9, 10);
            assertEquals(7,a.getX());
            assertEquals(8,a.getY());
            assertEquals(9,a.getH());
            assertEquals(10,a.getW());
            a=new Annotation(a.getId());
            assertEquals(7,a.getX());
            assertEquals(8,a.getY());
            assertEquals(9,a.getH());
            assertEquals(10,a.getW());
  
        } catch (SQLException ex) {
            Logger.getLogger(AnnotationTest.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Test of updateAnnotationFolio method, of class Annotation.
     */
    @Test
    public void testUpdateAnnotationFolio() throws Exception {
                try {
            Annotation a=new Annotation(1,2,"hello",3,4,5,6);
            a.updateAnnotationFolio(4);
            assertEquals(4,a.getFolio());
            a=new Annotation(a.getId());
            assertEquals(4,a.getFolio());
            
                }
                catch(SQLException ex)
                {
                    Logger.getLogger(AnnotationTest.class.getName()).log(Level.SEVERE, null, ex);
                }
    }

    /**
     * Test of duplicate method, of class Annotation.
     */
    @Test
    public void testDuplicate() throws Exception {
        try {
            Annotation a=new Annotation(1,2,"hello",3,4,5,6);
            Annotation b=a.duplicate();
            assertEquals(b.getFolio(),a.getFolio());
            assertEquals(b.getX(),a.getX());
            assertEquals(b.getY(),a.getY());
            assertEquals(b.getH(),a.getH());
            assertEquals(b.getW(),a.getW());
            assertEquals(b.getProjectID(),a.getProjectID());
            //this is a way of checking that the ids arent the same
            assertEquals(b.getId()==a.getId(),false);
                
            
                }
                catch(SQLException ex)
                {
                    Logger.getLogger(AnnotationTest.class.getName()).log(Level.SEVERE, null, ex);
                }
    }
}
