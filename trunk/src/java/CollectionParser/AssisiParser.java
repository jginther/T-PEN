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

public class AssisiParser {
    /**
     * This parser begins by fetching a page that lists all
     * of the manuscripts available from Assisi. Using
     * the identifying information listed on that page, it checks to see
     * if a manuscript with that shelfmark already exists.  If it does exist,
     * nothing is done.  If it does not exist, a page containing
     * the image listing is loaded and parsed.  The image identifiers
     * found on the page are combined with the known IP and
     * path of the server that provides the images to create a folio record.
     * 
     * @param archive
     * @throws MalformedURLException
     * @throws IOException
     * @throws SQLException 
     */
    public static void parse(String archive) throws MalformedURLException, IOException, SQLException
    {
        if (archive.compareTo("Assisi") == 0)
        {
            String leftovers="";
            Vector<String> nums = new Vector();
            Vector<String> labels = new Vector();
            String base="http://88.48.84.154/bbw/jsp/volumes/?fl_cerca=S&text=&ds_shelfmark=&id_library=0&id_creator=&order=2&limit=9998";
            URL mslisting = new URL(base);
            BufferedReader wholeReader = new BufferedReader(new InputStreamReader(mslisting.openConnection().getInputStream()));
            
            String buf = "";
            Boolean in = false;
            int ctr = 0;
            
            if (!wholeReader.ready()) {
                System.out.print("reader not ready, stalling .5 seconds!\n");
                try {
                    Thread.sleep(500);//sleep for 1000 ms
                } catch (InterruptedException ie) {
                    //If this thread was intrrupted by nother thread
                }
            }
            
            StringBuilder total=new StringBuilder("");
            while (wholeReader.ready()) {
                total.append(wholeReader.readLine()).append("\n");
                if (!wholeReader.ready()) {
                    System.out.print("reader not ready, stalling .5 seconds!\n");
                    try {
                        Thread.sleep(500);//sleep for 1000 ms
                    } catch (InterruptedException ie) {
                        //If this thread was intrrupted by nother thread
                    }
                }
            }
            
            BufferedReader b=new BufferedReader(new StringReader(total.toString()));
            int linecount=0;
            while(b.ready())
            {
                buf = b.readLine();
                linecount++;
                if (buf==null)
                {
                    System.out.print("linecount:"+linecount+"\n");
                    return;
                }
                
                //System.out.print(buf+"\n");
                if (buf.contains("javascript:seleziona") && !buf.contains("</a>")) {
                    //System.out.print("1\n");
                    String num = buf.split("javascript:seleziona")[1].split("'")[1];
                    //num=num.substring(2);
                    System.out.print(buf+":"+num+"\n");
                    
                    nums.add(num);
                    //b.readLine();
                    String labelbuff=b.readLine();
                    if (labelbuff.contains("wordwrap"))
                    {
                        labelbuff=b.readLine();
                    }
                    labelbuff=labelbuff.trim();
                    if(labelbuff.length()==2)
                        labelbuff="0"+labelbuff;
                    if(labelbuff.length()==1)
                        labelbuff="00"+labelbuff;
                    
                    labels.add(labelbuff);
                    String existsQuery="select * from manuscript where msIdentifier=? and archive=?";
                    Boolean exists=false;
                    Connection j=null;
                    PreparedStatement ps=null;
                    try {
                        j=DatabaseWrapper.getConnection();
                        ps=j.prepareStatement(existsQuery);
                        ps.setString(1,labelbuff);
                        ps.setString(2, "Assisi");
                        ResultSet rs=ps.executeQuery();
                        if(rs.next())
                            exists=true;
                        
                    }
                    finally {
                        DatabaseWrapper.closeDBConnection(j);
                        DatabaseWrapper.closePreparedStatement(ps);
                    }
                    
                    if (!exists)
                    {
                        String xmlUrl="http://88.48.84.154//bbw/DataShowImages.xml?p="+num+"BBW1BBW2";
                        URL imgListing = new URL(xmlUrl);
                        BufferedReader xmlReader = new BufferedReader(new InputStreamReader(imgListing.openConnection().getInputStream()));
                        while( xmlReader.ready())
                        {
                            String line=xmlReader.readLine();
                            //System.out.print(line+"\n");
                            String[] imageNames=line.split("<NAME>");
                            String[] imageLabels=line.split("Sheet&nbsp;");
                            
                            Manuscript m=new Manuscript("Assisi, Fondo Antico", "Assisi", labelbuff, "Assisi");
                            for(int i=1;i<imageNames.length;i++)
                            {
                                String imageIdentifier=imageNames[i].split("</NAME>")[0];
                                String imageLabel=imageLabels[i].split("</b>")[0];
                                System.out.print("Adding "+labelbuff+" "+imageIdentifier+" "+imageLabel+"\n");
                                Folio.createFolioRecord(m.getCollection(), imageLabel, "http://64.19.142.12/88.48.84.154/bbw/FastViewImage?t=1&i="+imageIdentifier, "Assisi", m.getID());
                            }
                        }
                        
                        System.out.print("label:"+labels.get(labels.size()-1)+" num:"+nums.get(nums.size()-1)+"\n");
                    }
                }
                else {
                    if (false&&buf.contains("<td align=\"center\" title=\""))
                    {
                        //System.out.print("2\n");
                        String label = buf.split("<td align=\"center\" title=\"")[1].split("\"")[0];
                        System.out.print(buf+":"+label+"\n");
                        labels.add(label);
                    }
                    else
                    {
                        //System.out.print("3\n");
                       // leftovers+=buf;
                    }
                }
            }
            System.out.print(leftovers);
            for(int i=0;i<labels.size();i++)
            {
                System.out.print("label: "+labels.get(i) +"\n");
            }
            for(int i=0;i<nums.size();i++)
            {
                System.out.print("id: "+nums.get(i)+"\n");
            }
            String imageURI="http://64.19.142.12/88.48.84.154/bbw/FastViewImage?t=1&i=";
        }
    }
}
