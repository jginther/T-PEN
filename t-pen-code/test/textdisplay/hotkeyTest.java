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
public class hotkeyTest {
    
    public hotkeyTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }
    
    @Before
    public void setUp() {
        hotkey instance;
        try {
            instance = new hotkey(1, 1);
            if (!instance.exists()) {
                instance = new hotkey(8, 1, 1);
            }

            instance = new hotkey(1, 1,true);
            if (!instance.exists()) {
                instance = new hotkey(8, 1, 1,true);
            }
            
            
        } catch (SQLException ex) {
            Logger.getLogger(hotkeyTest.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        
    }
    
    @After
    public void tearDown() {
    }

    /**
     * Test of exists method, of class hotkey.
     */
    @Test
    public void testExists() {
        System.out.println("exists");
        hotkey instance;
        try {
            instance = new hotkey(1);
            assertEquals(instance.exists(), false);
            instance.key=5;
            assertEquals(instance.exists(), true);        
        } catch (SQLException ex) {
            fail("sql exception");
        }
    }

    /**
     * Test of setKey method, of class hotkey.
     */
    @Test
    public void testSetKey() throws Exception {
        System.out.println("setKey");
        int newKey = 0;
        
        hotkey instance = new hotkey(1,1);
        if(!instance.exists())
        {
            fail("trying to test with non existant key!");
        }
        int oldKey=instance.key;
        
        if(oldKey>5)
            newKey=4;
        else
            newKey=6;
        instance.setKey(newKey);
        //after running setkey, the instace's key value should be updated
        assertEquals(instance.key,newKey);
        //so should the database key
        hotkey updatedKey=new hotkey(1,1);
        assertEquals(updatedKey.key,newKey);
        
        //now test the same thing but with project specific keys
        instance = new hotkey(1,1,true);
        if(!instance.exists())
        {
            fail("trying to test with non existant key!");
        }
        
        
        if(oldKey>5)
            newKey=4;
        else
            newKey=6;
        instance.setKey(newKey);
        //after running setkey, the instace's key value should be updated
        assertEquals(instance.key,newKey);
        //so should the database key
        updatedKey=new hotkey(1,1,true);
        assertEquals(updatedKey.key,newKey);
        
        
    }

    /**
     * Test of getButton method, of class hotkey.
     */
    @Test
    public void testGetButton() {
        try {
            int newKey = 0;
            
            hotkey instance = new hotkey(1,1);
            if(!instance.exists())
            {
                fail("trying to test with non existant key!");
            }
            int oldKey=instance.key;
            
            if(oldKey>5)
                newKey=4;
            else
                newKey=6;
            instance.setKey(newKey);
            //after running setkey, the instace's key value should be updated
            assertEquals(instance.key,newKey);
            //so should the database key
            hotkey updatedKey=new hotkey(1,1);
            assertEquals(updatedKey.key,newKey);
            
            //now test the same thing but with project specific keys
            instance = new hotkey(1,1,true);
            if(!instance.exists())
            {
                fail("trying to test with non existant key!");
            }
            if(oldKey>5)
                newKey=4;
            else
                newKey=6;
            instance.setKey(newKey);
            //the button value is just a string version of the key. test that the object and database both give the correct answer
            assertEquals(instance.getButton(),""+newKey);
            updatedKey=new hotkey(1,1,true);
            assertEquals(updatedKey.getButton(),""+newKey);
        } catch (SQLException ex) {
            fail("sql exception"+ex.getLocalizedMessage());
        }
        
    }

    /**
     * Test of getButtonInteger method, of class hotkey.
     */
    @Test
    public void testGetButtonInteger() {
        System.out.println("getButtonInteger is not tested, same as getButton just returned as an int");
    }

    /**
     * Test of changePosition method, of class hotkey.
     */
    @Test
    public void testChangePosition() throws Exception {
       int newPosition = 0;
            try{
            hotkey instance = new hotkey(1,1);
            if(!instance.exists())
            {
                fail("trying to test with non existant key!");
            }
            int oldPosition=instance.position;
   
            if(oldPosition>1)
                newPosition=1;
            else
                newPosition=2;
             hotkey oldKey=new hotkey(1,newPosition);
            if(oldKey.exists())
                oldKey.delete();
            instance.changePosition(newPosition);
            
            int key=instance.key;
            //after running setkey, the instace's key value should be updated
            assertEquals(instance.position,newPosition);
            //so should the database key
            hotkey updatedKey=new hotkey(1,newPosition);
            assertEquals(updatedKey.key,key);
            updatedKey.changePosition(oldPosition);
            instance = new hotkey(1,1,true);
            if(!instance.exists())
            {
                fail("trying to test with non existant key!");
            }
             oldPosition=instance.position;
            
            if(oldPosition>1)
                newPosition=1;
            else
                newPosition=2;
            
            oldKey=new hotkey(1,newPosition,true);
            if(oldKey.exists())
                oldKey.delete();instance.changePosition(newPosition);
             key=instance.key;
            //after running setkey, the instace's key value should be updated
            assertEquals(instance.position,newPosition);
            //so should the database key
             updatedKey=new hotkey(1,newPosition,true);
            assertEquals(updatedKey.key,key);
            updatedKey.changePosition(oldPosition);
        } catch (SQLException ex) {
            fail("sql exception"+ex.getLocalizedMessage());
        }
        
    }

    /**
     * Test of javascriptToAddButtons method, of class hotkey.
     */
    @Test
    public void testJavascriptToAddButtons() throws Exception {
        //test that the javacscript to add a button on the page matches the expected output
        hotkey instance = new hotkey(1,1);
        
            String keyScript=instance.javascriptToAddButtons(1);
            if(!keyScript.startsWith("<script"))
            {
                fail("missing script tag");
            }
            if(!keyScript.contains("</script>"))
            {
                fail("missing script end tag");
            }
            
    }

    /**
     * Test of javascriptToAddProjectButtons method, of class hotkey.
     */
    @Test
    public void testJavascriptToAddProjectButtons() throws Exception {
        hotkey instance = new hotkey(1,1,true);
        String keyScript=instance.javascriptToAddButtons(1);
            if(!keyScript.startsWith("<script"))
            {
                fail("missing script tag");
            }
            if(!keyScript.contains("</script>"))
            {
                fail("missing script end tag");
            }
    }

    /**
     * Test of keyhandler method, of class hotkey.
     */
    @Test
    public void testKeyhandler() {
        System.out.println("keyhandler is not tested, is depricated");
        
       
    }

    /**
     * Test of delete method, of class hotkey.
     */
    @Test
    public void testDelete() throws Exception {
        hotkey instance = new hotkey(1,1);
        instance.setKey(6);
        int key=instance.key;
        instance.delete();
        instance = new hotkey(1,1);
        
        if(instance.key==key)
            fail("key values match, deletion didnt occur!");
        instance.setKey(key);
        instance = new hotkey(1,1,true);
        key=instance.key;
        instance.delete();
        instance = new hotkey(1,1,true);
        if(instance.key==key)
            fail("key values match, deletion didnt occur!");
        instance.setKey(key);
    }
}
