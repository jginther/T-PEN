/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package textdisplay;

import dmstech.canvas;
import dmstech.sequence;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author ijrikgnmd
 */
public class somemain {
    public static void main(String [] args) throws MalformedURLException, IOException, SQLException
    {
        URL [] urls=new URL [2];
        urls[0] = new URL("http://rosetest.library.jhu.edu/m3//Arras897");
        urls[1] = new URL("http://rosetest.library.jhu.edu/m3//Arras897/images");
            
            sequence s = new sequence(urls, "");
        Connection dbConn=null;
                try{
                    dbConn=DatabaseWrapper.getConnection();
                                
                                canvas [] canvases=s.getSequenceItems();
                                    Manuscript m=new Manuscript("Bibliothèque municipale d'Arras","Rose","Arras 897","Paris");
                                    String msg="Added manuscript"+  "Manuscript "+ m.getID()+" is now "+m.getShelfMark()+". It has "+canvases.length+" canvases.";
                                    Logger.getAnonymousLogger().log(Level.SEVERE, msg);
                                    
                                
                                //System.out.print(s.getRepository()+"Stanford"+s.getCollection()+s.getCity()+"\n");
                                for(int l=0;l<canvases.length;l++)
                                {
                                Folio.createFolioRecord("Arras 897", canvases[l].getTitle(), canvases[l].getImageURL()[0].getImageURL(), "Rose", m.getID(), canvases[l].getPosition(), canvases[l].getCanvas());
                                
                                }
                           
                            
                            
                            
                }
                        finally{dbConn.close();}
    }
}
