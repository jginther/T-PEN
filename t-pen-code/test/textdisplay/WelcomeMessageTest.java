/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package textdisplay;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.BeforeClass;

/**
 *
 * @author jdeerin1
 */
public class WelcomeMessageTest {
    
    public WelcomeMessageTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }
    
    @Before
    public void setUp() {
    }

    @Test
    public void testSetMessage() throws Exception {
    }

    @Test
    public void testGetMessagePlain() throws Exception {
    }

    @Test
    public void testGetMessage() throws Exception {
            WelcomeMessage w=new WelcomeMessage();
        String msg=w.getMessage("jdeerin1@slu.edu", "newpass");
        System.out.println(msg);
        assertEquals(msg.contains("newpass"),true);
    }
}
