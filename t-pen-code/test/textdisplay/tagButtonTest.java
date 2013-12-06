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
public class tagButtonTest {
    
    public tagButtonTest() {
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
            
            //basic testing example
            tagButton instance=new tagButton(1,1);
            if(!instance.exists())
            {
                instance=new tagButton(1,1,"testtag","");
            }
            instance.updateTag("testtag");
            instance.updateDescription("testingdescription");
            //this is for testing getDescription to be sure it will give the tag name if no description was given
            instance=new tagButton(1,2);
            if(!instance.exists())
            {
                instance=new tagButton(1,2,"testtag","");
            }
            instance.updateTag("testtag");
            instance.updateDescription("");
            
            //same 2 samples for projects
            instance=new tagButton(1,1,true);
            if(!instance.exists())
            {
                instance=new tagButton(1,1,"testtag",true,"");
            }
            instance.updateTag("testtag");
            instance.updateDescription("");
            
            instance=new tagButton(1,2,true);
            if(!instance.exists())
            {
                instance=new tagButton(1,2,"testtag",true,"");
            }
            instance.updateTag("testtag");
            instance.updateDescription("");
            
        } catch (SQLException ex) {
            Logger.getLogger(tagButtonTest.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    @After
    public void tearDown() {
    }

    /**
     * Test of getDescription method, of class tagButton.
     */
    @Test
    public void testGetDescription() {
        try {
            tagButton instance=new tagButton(1,1);
            if(instance.exists())
            {
                //this one should have a description different fromt he tag
                assertEquals(instance.getDescription(),"testingdescription");
            }
            else
            {
                fail("attempting to test with non existant tag.");
            }
            instance=new tagButton(1,2);
            if(instance.exists())
            {
                //this one should give the tag name as the description
                assertEquals(instance.getDescription(),instance.getTag());
            }
            else
            {
                fail("attempting to test with non existant tag.");
            }
            instance=new tagButton(1,1,true);
            if(instance.exists())
            {
                
            }
            else
            {
                fail("attempting to test with non existant tag.");
            }
        } catch (SQLException ex) {
            fail("sql exception "+ex.getLocalizedMessage());
        }
    }


    /**
     * Test of getXMLColor method, of class tagButton.
     */
    @Test
    public void testGetXMLColor() {
    System.out.print("Skipping test of getXMLColor");
    }

    /**
     * Test of updateParameters method, of class tagButton.
     */
    @Test
    public void testUpdateParameters() throws Exception {
        tagButton instance=new tagButton(1,1);
        String [] params=new String [5];
        params[0]="p1";
        params[1]="p2";
        params[2]="p3";
        params[3]="p4";
        params[4]="p5";
        String [] emptyParams=new String[5];
        emptyParams[0]="";
        emptyParams[1]="";
        emptyParams[2]="";
        emptyParams[3]="";
        emptyParams[4]="";
        instance.updateParameters(params);
        assertEquals(params[0], instance.parameters[0]);
        assertEquals(params[1], instance.parameters[1]);
        assertEquals(params[2], instance.parameters[2]);
        assertEquals(params[3], instance.parameters[3]);
        assertEquals(params[4], instance.parameters[4]);
        //now make sure the db updated
        instance = new tagButton(1, 1);
        assertEquals(params[0], instance.parameters[0]);
        assertEquals(params[1], instance.parameters[1]);
        assertEquals(params[2], instance.parameters[2]);
        assertEquals(params[3], instance.parameters[3]);
        assertEquals(params[4], instance.parameters[4]);
        //now check with a set of empty parameters
        instance.updateParameters(emptyParams);
        assertEquals(instance.hasParameters(),false);
        instance = new tagButton(1, 1);
        assertEquals(instance.hasParameters(),false);
    }

    /**
     * Test of getparameters method, of class tagButton.
     */
    @Test
    public void testGetparameters() {
    }

    /**
     * Test of hasParameters method, of class tagButton.
     */
    @Test
    public void testHasParameters() {
    }

    /**
     * Test of exists method, of class tagButton.
     */
    @Test
    public void testExists() {
    }

    /**
     * Test of updatePosition method, of class tagButton.
     */
    @Test
    public void testUpdatePosition() throws Exception {
    }

    /**
     * Test of updateTag method, of class tagButton.
     */
    @Test
    public void testUpdateTag() throws Exception {
    }

    /**
     * Test of getButton method, of class tagButton.
     */
    @Test
    public void testGetButton() {
    }

    /**
     * Test of getButtonScript method, of class tagButton.
     */
    @Test
    public void testGetButtonScript() {
    }

    /**
     * Test of getFullTag method, of class tagButton.
     */
    @Test
    public void testGetFullTag() {
    }

    /**
     * Test of getTag method, of class tagButton.
     */
    @Test
    public void testGetTag() {
    }

    /**
     * Test of getAllButtons method, of class tagButton.
     */
    @Test
    public void testGetAllButtons() throws Exception {
    }

    /**
     * Test of getAllProjectButtons method, of class tagButton.
     */
    @Test
    public void testGetAllProjectButtons() throws Exception {
    }

    /**
     * Test of removeButtonsByProject method, of class tagButton.
     */
    @Test
    public void testRemoveButtonsByProject() throws Exception {
    }

    /**
     * Test of updateDescription method, of class tagButton.
     */
    @Test
    public void testUpdateDescription() throws Exception {
    }

    /**
     * Test of deleteTag method, of class tagButton.
     */
    @Test
    public void testDeleteTag() throws Exception {
    }

    /**
     * Test of setTag method, of class tagButton.
     */
    @Test
    public void testSetTag() throws Exception {
    }

    /**
     * Test of main method, of class tagButton.
     */
    @Test
    public void testMain() throws Exception {
    }

    /**
     * Test of xslRunner method, of class tagButton.
     */
    @Test
    public void testXslRunner() throws Exception {
    }

    /**
     * Test of getTagsFromSchema method, of class tagButton.
     */
    @Test
    public void testGetTagsFromSchema() throws Exception {
    }

    /**
     * Test of getTagsFromUser method, of class tagButton.
     */
    @Test
    public void testGetTagsFromUser() throws Exception {
    }
}
