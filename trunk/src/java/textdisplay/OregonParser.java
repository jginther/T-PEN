/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package textdisplay;

import java.io.IOException;
import java.net.MalformedURLException;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author obi1one
 */
public class OregonParser {
public static void main(String[] args) throws SQLException
    {
    Manuscript ms=new Manuscript("", "University of Oregon",args[0],"Eugene");
    int cid=Integer.parseInt(args[1]);
    Archive a=new Archive("University of Oregon");
        try {

            a.getAvailableCollectionsFromSite("University of Oregon", ms.getID(), cid);
        } catch (MalformedURLException ex) {
            Logger.getLogger(OregonParser.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(OregonParser.class.getName()).log(Level.SEVERE, null, ex);
        } catch (SQLException ex) {
            Logger.getLogger(OregonParser.class.getName()).log(Level.SEVERE, null, ex);
        }

}
}
