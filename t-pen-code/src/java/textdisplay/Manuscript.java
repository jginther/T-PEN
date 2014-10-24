/*
 * @author Jon Deering
Copyright 2011 Saint Louis University. Licensed under the Educational Community License, Version 2.0 (the "License"); you may not use
this file except in compliance with the License.

You may obtain a copy of the License at http://www.osedu.org/licenses/ECL-2.0

Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions
and limitations under the License.
 */
package textdisplay;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import textdisplay.DatabaseWrapper;
import textdisplay.Folio;
import java.util.Stack;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.mail.MessagingException;
import org.owasp.esapi.ESAPI;
import user.User;
/**
 * Represents a single Manuscript, which is a collection of folios (images). The Manuscript is hosted by an archive, and has a shelfmark composed of the
 * Manuscript identifier(collection) and the repository.
 * @author jdeering
 */
public class Manuscript
{
    
    /**Alpha numeric ms identifier*/
    private String collection;
    private String archive;
    private String repository;
    private String city;
    
    /**unique id for this ms, not to be confused with the alphanumeric ms identifier */
    private int id=0;
    
    /**Get the Manuscript from the hosting archive and collection*/
    public Manuscript(String collection, String archive) throws SQLException
    {
        this.archive = archive;
        this.collection = collection;
        String query = "select * from manuscript where archive=? and msIdentifier=?";
        Connection j = null;
        PreparedStatement ps=null;
        try
        {
            j = DatabaseWrapper.getConnection();
            ps = j.prepareStatement(query);
            ps.setString(1, archive);
            ps.setString(2, collection);
            ResultSet rs = ps.executeQuery();
            if (rs.next())
            {
                this.id = rs.getInt("id");
                this.repository = rs.getString("repository");
                this.city = rs.getString("city");
            }
            else
            {
                this.id=0;
            }
        } finally
        {
            if (j != null)
            {
                DatabaseWrapper.closeDBConnection(j);
                DatabaseWrapper.closePreparedStatement(ps);
            }
        }
    }
    
    
   /**Used internally when large numbers of manuscripts are being instantiated via a single query*/
    public Manuscript(int id, String city, String repository, String collection, String archive)
    {
        this.id = id;
        this.city = city;
        this.repository = repository;
        this.collection = collection;
        this.archive = archive;
    }
    
    
    /**Return the total number of manuscripts in the data store*/
    public static int getTotalManuscriptCount() throws SQLException
    {
        Connection j = null;
        PreparedStatement ps=null;
        String query = "select count(id) from manuscript where restricted!=-999";
        try
        {
            j = DatabaseWrapper.getConnection();
            ps = j.prepareStatement(query);
            ResultSet rs = ps.executeQuery();
            if (rs.next())
            {
                return rs.getInt(1);
            }
            return 0;
        } finally
        {
            DatabaseWrapper.closeDBConnection(j);
            DatabaseWrapper.closePreparedStatement(ps);
        }
    }
    
    
    /**Get the Manuscript based on Manuscript ID*/
    public Manuscript(int id, Boolean tr) throws SQLException
    {
        String query = "select city,repository, msIdentifier,archive from manuscript where id=?";
        Connection j = null;
        PreparedStatement ps=null;
        try
        {
            j = DatabaseWrapper.getConnection();
            ps = j.prepareStatement(query);
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next())
            {
                this.id = id;
                this.city = rs.getString(1);
                this.repository = rs.getString(2);
                this.collection = rs.getString(3);
                this.archive = rs.getString(4);
            }
        } finally
        {
            if (j != null)
            {
                DatabaseWrapper.closeDBConnection(j);
                DatabaseWrapper.closePreparedStatement(ps);
            }
        }
    }
    
    
    /**Get the Manuscript that contains this particular Folio*/
    public Manuscript(int folio) throws SQLException
    {
        String query = "select msID from folios where pageNumber=?";
        Connection j = null;
        PreparedStatement ps=null;
        try
        {
            j = DatabaseWrapper.getConnection();
            ps = j.prepareStatement(query);
            ps.setInt(1, folio);
            ResultSet rs = ps.executeQuery();
            if (rs.next())
            {
                this.id = rs.getInt(1);
                query = "select city,repository, msIdentifier,archive from manuscript where id=?";
                ps = j.prepareStatement(query);
                ps.setInt(1, id);
                rs = ps.executeQuery();
                if (rs.next())
                {
                    this.city = rs.getString(1);
                    this.repository = rs.getString(2);
                    this.collection = rs.getString(3);
                    this.archive = rs.getString(4);
                }
            }
        } finally
        {
            if (j != null)
            {
                DatabaseWrapper.closeDBConnection(j);
                DatabaseWrapper.closePreparedStatement(ps);
            }
        }
    }
    
    
    public String getArchive()
    {
        return archive;
    }
    
    
    public String getCity()
    {
        return city;
    }
    
    
    public String getCollection()
    {
        return collection;
    }
    
    
    public String getRepository()
    {
        return repository;
    }
    
    
    public int getID()
    {
        return id;
    }
    
    
    /**Create and store a new manuscript. Generates a new manuscript id.*/
    public Manuscript(String repository, String archive, String collection, String city) throws SQLException
    {
        String query="INSERT INTO manuscript (repository, archive, msIdentifier,city) "
                + "SELECT ?,?,?,? "
                + "FROM dual "
                + "WHERE NOT EXISTS ("
                + "SELECT * FROM manuscript "
                + "WHERE repository=? "
                + "AND msIdentifier=?"
                + ")";
        System.out.println("************************************first query = "+query);
        Connection j=null;
        PreparedStatement ps=null;
        try {
            j=DatabaseWrapper.getConnection();
            ps=j.prepareStatement(query,PreparedStatement.RETURN_GENERATED_KEYS);
            ps.setString(1, repository);
            ps.setString(2, archive);
            ps.setString(3, collection);
            ps.setString(4, city);
            ps.setString(5, repository);
            ps.setString(6, collection);
            ps.execute();
            ResultSet rs=ps.getGeneratedKeys();
            if (rs.next())
            {
                this.id=rs.getInt(1);
                this.collection=collection;
                this.city=city;
                this.repository=repository;
                this.archive=archive;
            }else{
                DatabaseWrapper.closeDBConnection(j);
                DatabaseWrapper.closePreparedStatement(ps);
                query = "SELECT * FROM manuscript WHERE repository=? AND msIdentifier=?";
                System.out.println("+++++++++++++++++++++++++++++++++++++++++++second query = "+query);
                j = DatabaseWrapper.getConnection();
                System.out.println("%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%% connection = " + j);
                ps = j.prepareStatement(query);
                ps.setString(1, repository);
                ps.setString(2, collection);
                rs = ps.executeQuery();
                if(rs.next()){
                    this.id=rs.getInt(1);
                    System.out.println("&&&&&&&&&&&&&&&&&&&&&&&&&&&&&& id = " + this.id);
                    this.collection=collection;
                    this.city=city;
                    this.repository=repository;
                    this.archive=archive;
                }
            }
        }
        finally {
            DatabaseWrapper.closeDBConnection(j);
            DatabaseWrapper.closePreparedStatement(ps);
        }
    }
    
    
    /**Update the metadata values for this Manuscript*/
    public void update(String city, String repository, String collection) throws SQLException
    {
        String updateQuery = "update manuscript set city=?, repository=?, msIdentifier=? where id=?";
        Connection j = null;
        PreparedStatement ps=null;
        try
            {
                j = DatabaseWrapper.getConnection();
                ps = j.prepareStatement(updateQuery);
                ps.setString(1, city);
                ps.setString(2, repository);
                ps.setString(3, collection);
                ps.setInt(4, id);
                ps.execute();
            } finally
            {
            if (j != null)
            {
                DatabaseWrapper.closeDBConnection(j);
                DatabaseWrapper.closePreparedStatement(ps);
            }
        }
    }
    
    
    /**Get a list of manuscripts housed in a particular city and repository. Best to use getAllCities() and getAllRepositorites to find out which ones are available to avoid
    spelling issues.*/
    public static Manuscript[] getManuscriptsByCityAndRepository(String city, String repo) throws SQLException
    {
        Manuscript[] toret = new Manuscript[0];
        String query = "select id from manuscript where repository=? and city=? and restricted!=-999 order by msIdentifier" ;
        Connection j = null;
        PreparedStatement ps=null;
        try
        {
            j = DatabaseWrapper.getConnection();
            ps = j.prepareStatement(query);
            ps.setString(1, repo);
            ps.setString(2, city);
            
            ResultSet rs = ps.executeQuery();
            while (rs.next())
            {
                Manuscript[] tmp = new Manuscript[toret.length + 1];
                for (int i = 0; i < toret.length; i++)
                {
                    tmp[i] = toret[i];
                }
                Manuscript nextOne = new Manuscript(rs.getInt("id"), true);
                tmp[tmp.length - 1] = nextOne;
                toret = tmp;
            }
        } finally
        {
            if (j != null)
            {
                DatabaseWrapper.closeDBConnection(j);
                DatabaseWrapper.closePreparedStatement(ps);
            }
        }
        return toret;
    }
    
    
    /**Get a list of manuscripts housed in a particular repository. Best to use getAllRepositories() to find out which ones are available to avoid
    spelling issues.*/
    public static Manuscript[] getManuscriptsByRepository(String repo) throws SQLException
    {
        Manuscript[] toret = new Manuscript[0];
        String query = "select id from manuscript where repository=? and restricted!=-999 order by msIdentifier";
        Connection j = null;
        PreparedStatement ps=null;
        try
        {
            j = DatabaseWrapper.getConnection();
            ps = j.prepareStatement(query);
            ps.setString(1, repo);
            ResultSet rs = ps.executeQuery();
            while (rs.next())
            {
                Manuscript[] tmp = new Manuscript[toret.length + 1];
                for (int i = 0; i < toret.length; i++)
                {
                    tmp[i] = toret[i];
                }
                Manuscript nextOne = new Manuscript(rs.getInt("id"), true);
                tmp[tmp.length - 1] = nextOne;
                toret = tmp;
            }
        } finally
        {
            if (j != null)
            {
                DatabaseWrapper.closeDBConnection(j);
                DatabaseWrapper.closePreparedStatement(ps);
            }
        }
    return toret;
    }
    
    
    /**Create a string array with each string containing the name of a city, to allow for filtering of
    Manuscript requests*/
    public static String[] getAllRepositories() throws SQLException
    {
        String[] toret = new String[0];
        String query = "select distinct(repository) from manuscript where restricted!=-999 order by repository";
        Connection j = null;
        PreparedStatement ps=null;
        try
        {
            j = DatabaseWrapper.getConnection();
            ps = j.prepareStatement(query);
            ResultSet rs = ps.executeQuery();
            while (rs.next())
            {
                String[] tmp = new String[toret.length + 1];
                for (int i = 0; i < toret.length; i++)
                {
                    tmp[i] = toret[i];
                }
                toret = tmp;
                toret[toret.length - 1] = rs.getString(1);
            }
        } finally
        {
            if (j != null)
            {
                DatabaseWrapper.closeDBConnection(j);
                DatabaseWrapper.closePreparedStatement(ps);
            }
        }
        return toret;
    }
    
    
    /**Create a string array with each string containing the name of a repository, to allow for filtering of
    Manuscript requests. name may contain spaces, so be aware of the need to url encode them in requests*/
    public static String[] getAllCities() throws SQLException
    {
        String[] toret = new String[0];
        String query = "select distinct(city) from manuscript  where restricted!=-999 order by city";
        Connection j = null;
        PreparedStatement ps=null;
        try
        {
            j = DatabaseWrapper.getConnection();
            ps = j.prepareStatement(query);
            ResultSet rs = ps.executeQuery();
            while (rs.next())
            {
                String[] tmp = new String[toret.length + 1];
                for (int i = 0; i < toret.length; i++)
                {
                    tmp[i] = toret[i];
                }
                toret = tmp;
                toret[toret.length - 1] = rs.getString(1);
            }
        } finally
        {
            if (j != null)
            {
                DatabaseWrapper.closeDBConnection(j);
                DatabaseWrapper.closePreparedStatement(ps);
            }
        }
        return toret;
    }
    
    
    /**Look up the repositories in a given city*/
    public static String[] getRepositoriesByCity(String city) throws SQLException
    {
        String query = "select distinct(repository) from manuscript where city=? and restricted!=-999";
        Connection j = null;
        PreparedStatement ps=null;
        String[] toret = new String[0];
        try
        {
            j = DatabaseWrapper.getConnection();
            ps = j.prepareStatement(query);
            ps.setString(1, city);
            ResultSet rs = ps.executeQuery();
            while (rs.next())
            {
                String[] tmp = new String[toret.length + 1];
                for (int i = 0; i < toret.length; i++)
                {
                    tmp[i] = toret[i];
                }
                toret = tmp;
                toret[toret.length - 1] = rs.getString(1);
            }
        } finally
        {
            DatabaseWrapper.closeDBConnection(j);
            DatabaseWrapper.closePreparedStatement(ps);
        }
        return toret;
    }
    
    
    /**Get a list of manuscripts housed in a particular city. Best to use getAllCities() to find out which ones are available to avoid
    spelling issues.*/
    public static Manuscript[] getManuscriptsByCity(String city) throws SQLException
    {
        Manuscript[] toret = new Manuscript[0];
        String query = "select * from manuscript where city=? and restricted!=-999 order by msIdentifier";
        Connection j = null;
        PreparedStatement ps=null;
        try
        {
            j = DatabaseWrapper.getConnection();
            ps = j.prepareStatement(query);
            ps.setString(1, city);
            ResultSet rs = ps.executeQuery();
            while (rs.next())
            {
                Manuscript[] tmp = new Manuscript[toret.length + 1];
                for (int i = 0; i < toret.length; i++)
                {
                    tmp[i] = toret[i];
                }
                Manuscript nextOne = new Manuscript(rs.getInt("id"), rs.getString("city"), rs.getString("repository"), rs.getString("msIdentifier"), rs.getString("archive"));
                tmp[tmp.length - 1] = nextOne;
                toret = tmp;
            }
        } finally
        {
            if (j != null)
            {
                DatabaseWrapper.closeDBConnection(j);
                DatabaseWrapper.closePreparedStatement(ps);
            }
        }
        return toret;
    }
    
    
    /**Get the full transcription as a single string*/
    public String getFullDocument(User u) throws SQLException
    {
        String toret = "";
        String query = "select transcription.text, transcription.line from transcription join folios on folios.pageNumber=transcription.folio where collection=? and archive=? and creator=? and projectID=0 order by folio,line";
        Connection j = null;
        PreparedStatement ps=null;
        long startTime = System.currentTimeMillis();
        try
        {
            j = DatabaseWrapper.getConnection();
            ps = j.prepareStatement(query);
            ps.setString(1, collection);
            ps.setString(2, archive);
            ps.setInt(3, u.getUID());
            ResultSet rs = ps.executeQuery();
            while (rs.next())
            {
                toret += "" + rs.getInt("transcription.line") + " " + rs.getString(1) + "<br>";
            }
            return toret;
        } finally
        {
            long totalTime = System.currentTimeMillis() - startTime;
            System.out.print("Built a ms object in " + totalTime + "ms\n");
            if (j != null)
            {
                DatabaseWrapper.closeDBConnection(j);
                DatabaseWrapper.closePreparedStatement(ps);
            }
        }
    }
    
    
    /**Get the full transcription as a single string*/
    public static String getFullDocument(Project p, Boolean includeNotes) throws SQLException
    {
        Boolean newLine=false;
        String toret = "";
        String query = "select transcription.text, transcription.comment from transcription join folios on folios.pageNumber=transcription.folio where projectID=? order by folio,line";
        Connection j = null;
        PreparedStatement ps=null;
        
        try
        {
            j = DatabaseWrapper.getConnection();
            ps = j.prepareStatement(query);
            
            ps.setInt(1, p.projectID);
            ResultSet rs = ps.executeQuery();
            while (rs.next())
            {
                String tmp=rs.getString(1);
                
                if(tmp.endsWith(p.linebreakSymbol))
                {
                    tmp=tmp.substring(0,tmp.length()-p.linebreakSymbol.length());
                }
                else
                {
                    if(!tmp.endsWith(" "))
                    {
                        tmp=tmp+" ";
                    }
                }
                if(newLine)
                    toret += tmp+"\n";
                else
                    toret += tmp;
                if(includeNotes && rs.getString(2).trim().length()>0 && rs.getString(2).compareTo(" ")!=0&& rs.getString(2).compareTo("null")!=0)
                {
                    if(toret.endsWith("\n"))
                    {
                        toret += "<tpen_note>"+rs.getString(2)+"</tpen_note>\n";
                    }
                    else
                    {
                        toret += "\n<tpen_note>"+rs.getString(2)+"</tpen_note>\n";
                    }
                }
            }
            return toret;
        } finally
        {
            if (j != null)
            {
                DatabaseWrapper.closeDBConnection(j);
                DatabaseWrapper.closePreparedStatement(ps);
            }
        }
    }
    
    
    /**Get the full transcription as a single string*/
    public static String getFullDocument(Project p, TagFilter.noteStyles includeNotes, Boolean newLine, Boolean pageLabels, Boolean imageWrap) throws SQLException
    {
        
        String toret = "";
        String query = "select transcription.text, transcription.comment, transcription.folio from transcription join projectFolios on projectFolios.folio=transcription.folio where projectID=? order by position,x,y";
                        
        Connection j = null;
        PreparedStatement ps=null;
        
        try
        {
            j = DatabaseWrapper.getConnection();
            ps = j.prepareStatement(query);
            
            ps.setInt(1, p.projectID);
            ResultSet rs = ps.executeQuery();
            int oldfolio=-1;
            int note_ctr=1;
            String endNotes="\n";
            while (rs.next())
            {
                if(pageLabels &&rs.getInt("transcription.folio")!=oldfolio)
                {
                    
                    oldfolio=rs.getInt("transcription.folio");
                    toret+="["+new Folio(oldfolio).getPageName()+"]\n";
                   
                }
                if(imageWrap &&rs.getInt("transcription.folio")!=oldfolio )
                {
                    oldfolio=rs.getInt("transcription.folio");
                    toret+="<TPENimage name=\""+ESAPI.encoder().encodeForXML(ESAPI.encoder().decodeForHTML(new Folio(oldfolio).getPageName()))+"\" ";
                    if(new Folio(oldfolio).getImageURL().contains("t-pen"))
                        toret+=" url=\"\"/>";
                    else
                        toret+=" url=\""+ESAPI.encoder().encodeForXML(new Folio(oldfolio).getImageURL())+"\"/>";
                }
                String tmp=rs.getString(1);
                if(tmp.length()!=0)
                {
                    if(tmp.endsWith(p.linebreakSymbol))
                    {
                        tmp=tmp.substring(0,tmp.length()-p.linebreakSymbol.length());
                    }
                    else
                    {
                        if(!tmp.endsWith(" "))
                        {
                            tmp=tmp+" ";
                        }
                    }
                    if(newLine)
                        toret += tmp+"\n";
                    else
                        toret += tmp;
                    if(!(includeNotes==TagFilter.noteStyles.remove) && !(includeNotes==TagFilter.noteStyles.endnote ) && rs.getString(2).trim().length()>0 && rs.getString(2).compareTo(" ")!=0 && rs.getString(2).compareTo("null")!=0)
                    {
                        if(toret.endsWith("\n"))
                        {
                            toret += "<tpen_note>"+rs.getString(2)+"</tpen_note>\n";
                        }
                        else
                        {
                            toret += "\n<tpen_note>"+rs.getString(2)+"</tpen_note>\n";
                        }
                    }
                    if((includeNotes==TagFilter.noteStyles.endnote) && rs.getString(2).trim().length()>0 && rs.getString(2).compareTo(" ")!=0 && rs.getString(2).compareTo("null")!=0)
                    {
                        if(toret.charAt(toret.length()-1)==' ')
                        {
                            toret=toret.substring(0,toret.length()-1);
                            toret += "<tpen_note>"+note_ctr+"</tpen_note> ";
                        }
                        if(toret.charAt(toret.length()-1)=='\n')
                        {
                            toret=toret.substring(0,toret.length()-1);
                            toret += "<tpen_note>"+note_ctr+"</tpen_note>\n";
                        }
                        
                        endNotes+=note_ctr+" "+rs.getString(2)+"\n";
                        note_ctr++;
                        
                    }
                    
                }
            }
            return toret+endNotes;
        } finally
        {
            DatabaseWrapper.closeDBConnection(j);
            DatabaseWrapper.closePreparedStatement(ps);
        }
    }
    
    
    /**Get the full transcription as a single string*/
    public static String getPartialDocument(Project p, TagFilter.noteStyles includeNotes, Boolean newLine, Boolean pageLabels, int beginFolio, int endFolio) throws SQLException
    {
        Boolean inRange=false;
        Boolean goingOutOfRange=false;
        String toret = "";
        String query = "select transcription.text, transcription.comment, transcription.folio from transcription join projectFolios on projectFolios.folio=transcription.folio and projectFolios.project=? where projectID=? order by projectFolios.position,x,y";
        Connection j = null;
        PreparedStatement ps=null;
        int note_ctr=1;
        String endNotes="\n";
        
        try
        {
            j = DatabaseWrapper.getConnection();
            ps = j.prepareStatement(query);
            
            ps.setInt(1, p.projectID);
            ps.setInt(2, p.projectID);
            ResultSet rs = ps.executeQuery();
            int oldfolio=-1;
            while (rs.next())
            {
                int folionum=rs.getInt("transcription.folio");
                if(!inRange)
                {
                    if(rs.getInt("transcription.folio")==beginFolio)
                    {
                        inRange=true;
                    }
                }
                if(rs.getInt("transcription.folio")!=oldfolio)
                {
                    oldfolio=rs.getInt("transcription.folio");
                    if(pageLabels && inRange) toret+="["+new Folio(oldfolio).getPageName()+"]\n";
                    if(goingOutOfRange)
                        inRange=false;
                }
                if(inRange)
                {
                    if(rs.getInt("transcription.folio")==endFolio)
                    {
                        goingOutOfRange=true;
                    }
                }
                if(inRange) {
                    String tmp=rs.getString(1);
                    if(tmp.length()!=0)
                    {
                        if(tmp.endsWith(p.linebreakSymbol))
                        {
                            tmp=tmp.substring(0,tmp.length()-p.linebreakSymbol.length());
                        }
                        else
                        {
                            if(!tmp.endsWith(" "))
                            {
                                tmp=tmp+" ";
                            }
                        }
                        if(newLine)
                            toret += tmp+"\n";
                        else
                            toret += tmp;
                        if(!(includeNotes==TagFilter.noteStyles.remove) && !(includeNotes==TagFilter.noteStyles.endnote ) && rs.getString(2).trim().length()>0 && rs.getString(2).compareTo(" ")!=0 && rs.getString(2).compareTo("null")!=0)
                        {
                            if(toret.endsWith("\n"))
                            {
                                toret += "<tpen_note>"+rs.getString(2)+"</tpen_note>\n";
                            }
                            else
                            {
                                toret += "\n<tpen_note>"+rs.getString(2)+"</tpen_note>\n";
                            }
                        }
                        if((includeNotes==TagFilter.noteStyles.endnote) && rs.getString(2).trim().length()>0 && rs.getString(2).compareTo(" ")!=0 && rs.getString(2).compareTo("null")!=0)
                        {
                            if(toret.charAt(toret.length()-1)==' ')
                            {
                                toret=toret.substring(0,toret.length()-1);
                                toret += "<tpen_note>"+note_ctr+"</tpen_note> ";
                            }
                            if(toret.charAt(toret.length()-1)=='\n')
                            {
                                toret=toret.substring(0,toret.length()-1);
                                toret += "<tpen_note>"+note_ctr+"</tpen_note>\n";
                            }
                            endNotes+=note_ctr+" "+rs.getString(2)+"\n";
                            note_ctr++;
                            
                        }
                    }
                }
            }
            return toret+endNotes;
        } finally
        {
            if (j != null)
            {
                DatabaseWrapper.closeDBConnection(j);
                DatabaseWrapper.closePreparedStatement(ps);
            }
        }
    }
    
    
    /**Get the full transcription as a single string*/
    public static String getPartialDocument(Project p, TagFilter.noteStyles includeNotes, Boolean newLine, Boolean pageLabels, int beginFolio, int endFolio, Boolean imageWrap) throws SQLException
    {
        Boolean inRange=false;
        Boolean goingOutOfRange=false;
        String toret = "";
        String query = "select transcription.text, transcription.comment, transcription.folio from transcription join projectFolios on projectFolios.folio=transcription.folio and projectFolios.project=? where projectID=? order by projectFolios.position,x,y";
        Connection j = null;
        PreparedStatement ps=null;
        int note_ctr=1;
        String endNotes="\n";
        
        try
        {
            j = DatabaseWrapper.getConnection();
            ps = j.prepareStatement(query);
            
            ps.setInt(1, p.projectID);
            ps.setInt(2, p.projectID);
            ResultSet rs = ps.executeQuery();
            int oldfolio=-1;
            while (rs.next())
            {
                int folionum=rs.getInt("transcription.folio");
                if(!inRange)
                {
                    if(rs.getInt("transcription.folio")==beginFolio)
                    {
                        inRange=true;
                    }
                }
                
                if(rs.getInt("transcription.folio")!=oldfolio)
                {
                    oldfolio=rs.getInt("transcription.folio");
                    if(pageLabels && inRange) toret+="["+new Folio(oldfolio).getPageName()+"]\n";
                    if(imageWrap)
                    {
                        toret+="<TPENimage name=\""+ESAPI.encoder().encodeForXML(ESAPI.encoder().decodeForHTML(new Folio(oldfolio).getPageName()))+"\" ";
                        if(new Folio(oldfolio).getImageURL().contains("t-pen"))
                             toret+=" url=\"\"/>";
                        else
                             toret+=" url=\""+ESAPI.encoder().encodeForXML(new Folio(oldfolio).getImageURL())+"\"/>";
                    }
                    if(goingOutOfRange)
                        inRange=false;
                }
                
                if(inRange)
                {
                    if(rs.getInt("transcription.folio")==endFolio)
                    {
                        goingOutOfRange=true;
                    }
                }
                
                if(inRange) {
                    String tmp=rs.getString(1);
                    if(tmp.length()!=0)
                    {
                        if(tmp.endsWith(p.linebreakSymbol))
                        {
                            tmp=tmp.substring(0,tmp.length()-p.linebreakSymbol.length());
                        }
                        else
                        {
                            if(!tmp.endsWith(" "))
                            {
                                tmp=tmp+" ";
                            }
                        }
                        
                        if(newLine)
                            toret += tmp+"\n";
                        else
                            toret += tmp;
                        
                        if(!(includeNotes==TagFilter.noteStyles.remove) && !(includeNotes==TagFilter.noteStyles.endnote ) && rs.getString(2).trim().length()>0 && rs.getString(2).compareTo(" ")!=0 && rs.getString(2).compareTo("null")!=0)
                        {
                            if(toret.endsWith("\n"))
                            {
                                toret += "<tpen_note>"+rs.getString(2)+"</tpen_note>\n";
                            }
                            else
                            {
                                toret += "\n<tpen_note>"+rs.getString(2)+"</tpen_note>\n";
                            }
                        }
                        
                        if((includeNotes==TagFilter.noteStyles.endnote) && rs.getString(2).trim().length()>0 && rs.getString(2).compareTo(" ")!=0 && rs.getString(2).compareTo("null")!=0)
                        {
                            if(toret.charAt(toret.length()-1)==' ')
                            {
                                toret=toret.substring(0,toret.length()-1);
                                toret += "<tpen_note>"+note_ctr+"</tpen_note> ";
                            }
                            if(toret.charAt(toret.length()-1)=='\n')
                            {
                                toret=toret.substring(0,toret.length()-1);
                                toret += "<tpen_note>"+note_ctr+"</tpen_note>\n";
                            }
                            endNotes+=note_ctr+" "+rs.getString(2)+"\n";
                            note_ctr++;
                            
                        }
                    }
                }
            }
            return toret+endNotes;
        } finally
        {
            DatabaseWrapper.closeDBConnection(j);
            DatabaseWrapper.closePreparedStatement(ps);
        }
    }
    
    
    /**Build a shelfmark from city, repository and collection*/
    public String getShelfMark()
    {
        return city + ", " + this.repository + " " + this.collection;
    }
    
    
    /**
     * Get the page number of the first page of this ms
     * @return the Folio number of the first page of this MS, or -1 if there are no folios
     * @throws SQLException
     */
    public int getFirstPage() throws SQLException
    {
       Folio[] folios= this.getFolios();
       if(folios.length>0)
           return folios[0].folioNumber;
       return -1;
    }
    
    
    /**
     * Remove authorization for the specified user to access images from this Manuscript
     * @param uid unique id of the user who is losing their access
     * @throws SQLException
     */
    public void deauthorizeUser(int uid) throws SQLException
    {
        String query = "delete from manuscriptPermissions where uid=? and msID=?";
        Connection j = null;
        PreparedStatement ps=null;
        try
        {
            j = DatabaseWrapper.getConnection();
            ps = j.prepareStatement(query);
            ps.setInt(1, uid);
            ps.setInt(2, this.id);
            ps.execute();
        } finally
        {
            DatabaseWrapper.closeDBConnection(j);
            DatabaseWrapper.closePreparedStatement(ps);
        }
    }
    
    
    /**
     * Authorize a user to access a restricted MSS
     * @param uid unique id of the user gaining access
     * @throws SQLException
     */
    public void authorizeUser(int uid) throws SQLException
    {
        String query = "insert into manuscriptPermissions (uid,msId) values(?,?)";
        Connection j = null;
        PreparedStatement ps=null;
        try
        {
            j = DatabaseWrapper.getConnection();
            ps = j.prepareStatement(query);
            ps.setInt(1, uid);
            ps.setInt(2, this.id);
            ps.execute();
        } finally
        {
            DatabaseWrapper.closeDBConnection(j);
            DatabaseWrapper.closePreparedStatement(ps);
        }
    }
    
    
    /**
     * Authorize a user to access a restricted MSS
     * @param uid unique id of the user gaining access
     * @throws SQLException
     */
    public void authorizeUser(int uid, Boolean notify) throws SQLException
    {
        String query = "insert into manuscriptPermissions (uid,msId) values(?,?)";
        Connection j = null;
        PreparedStatement ps=null;
        try
        {
            j = DatabaseWrapper.getConnection();
            ps = j.prepareStatement(query);
            ps.setInt(1, uid);
            ps.setInt(2, this.id);
            ps.execute();
            mailer m=new mailer();
            
            User requestor=new User(uid);
            String message="Your access to  "+this.getShelfMark()+", has been granted.\n";
            
            try {
                /**@TODO make mail server a config parameter*/
                m.sendMail(Folio.getRbTok("EMAILSERVER"), "TPEN@t-pen.org", requestor.getUname(), "TPEN manuscript access request", message);
            } catch (MessagingException ex) {
                Logger.getLogger(Manuscript.class.getName()).log(Level.SEVERE, null, ex);
            }
        } finally
        {
            DatabaseWrapper.closeDBConnection(j);
            DatabaseWrapper.closePreparedStatement(ps);
        }
    }
    
    
    /**
     * get an array of users who are allowed to access this Manuscript
     * @return array of all users who are permitted to access this Manuscript
     * @throws SQLException
     */
    public User[] getAuthorizedUsers() throws SQLException
    {
        String query = "select * from manuscriptPermissions where msID=?";
        Connection j = null;
        PreparedStatement ps=null;
        User[] s = new User[0];
        try
        {
            j = DatabaseWrapper.getConnection();
            ps = j.prepareStatement(query);
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            while (rs.next())
            {
                int uid = rs.getInt("uid");
                User u = new User(uid);
                User[] tmp = new User[s.length + 1];
                for (int i = 0; i < s.length; i++)
                {
                    tmp[i] = s[i];
                }
                tmp[tmp.length - 1] = u;
                s = tmp;
            }
        } finally
        {
            DatabaseWrapper.closeDBConnection(j);
            DatabaseWrapper.closePreparedStatement(ps);
        }
        return s;
    }
    
    
    /**
     * determine whether a user is permitted to view images from a Manuscript
     * @param u user object for the user who is having their access questioned
     * @return true if they are permitted false if not
     * @throws SQLException
     */
    public Boolean isAuthorized(User u) throws SQLException
    {
        String query = "select * from manuscriptPermissions where msId=? and uid=?";
        Connection j = null;
        PreparedStatement ps=null;
        try
        {
            j = DatabaseWrapper.getConnection();
            ps = j.prepareStatement(query);
            ps.setInt(1, id);
            ps.setInt(2, u.getUID());
            ResultSet rs = ps.executeQuery();
            if (rs.next())
            {
                return true;
            } else
            {
                return false;
            }
        } finally
        {
            DatabaseWrapper.closeDBConnection(j);
            DatabaseWrapper.closePreparedStatement(ps);
        }
    }
    
    
    /**
     * Return the user.User in charge of approving access to this particular MSS. Returns null if this isnt an access restrincted document.
     * @return null if no user exists, user object otherwise
     * @throws SQLException
     */
    public User getControllingUser() throws SQLException
    {
        if (!this.isRestricted())
        {
            return null;
        }
        String query = "select restricted from manuscript where id=?";
        Connection j = null;
        PreparedStatement ps=null;
        try
        {
            j = DatabaseWrapper.getConnection();
            ps = j.prepareStatement(query);
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next())
            {
                return new User(rs.getInt(1));
            }
        } finally
        {
            DatabaseWrapper.closeDBConnection(j);
            DatabaseWrapper.closePreparedStatement(ps);
        }
        
        return null;
    }
    
    
    /**Returns true if this Manuscript is restricted. If that is the case, getControllingUser will get the user who controls this Manuscript, getAuthorizedUsers will
    get all people allowed to view it, adn isAuthorized will check whether a specific user is permitted to use it*/
    public Boolean isRestricted() throws SQLException
    {
        String query = "select restricted from manuscript where id=?";
        Connection j = null;
        PreparedStatement ps=null;
        try
        {
            j = DatabaseWrapper.getConnection();
            ps = j.prepareStatement(query);
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next())
            {
                if (rs.getInt(1) != 0)
                    {
                    return true;
                    }
            }
        } finally
        {
            DatabaseWrapper.closeDBConnection(j);
            DatabaseWrapper.closePreparedStatement(ps);
        }
        return false;
    }
    
    
    /**Remove restricted access for this Manuscript*/
    public void makeUnresricted()
    {
        try
        {
            //just set the restriction ser id to 0
            this.makeRestricted(0);
        } catch (SQLException ex)
        {
            Logger.getLogger(Manuscript.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    
    /**make this a restricted access Manuscript*/
    public void makeRestricted(int controllingUID) throws SQLException
    {
        String query = "update manuscript set restricted=? where id=?";
        Connection j = null;
        PreparedStatement ps=null;
        try
        {
            j = DatabaseWrapper.getConnection();
            ps = j.prepareStatement(query);
            ps.setInt(1, controllingUID);
            ps.setInt(2, id);
            ps.execute();
        } finally
        {
            DatabaseWrapper.closeDBConnection(j);
            DatabaseWrapper.closePreparedStatement(ps);
        }
    }
    
    
    /**Sends an email to the user with control of the manuscript to indicate the requestor would like access to work with the manuscript.*/
    public Boolean contactControllingUser(String userMessage, User requestor)
    {
        mailer m=new mailer();
        try {
            User controller=this.getControllingUser();
            String message=requestor.getFname()+" "+requestor.getLname()+" ("+requestor.getUname()+") has requested access to "+this.getShelfMark()+", which you control. Below is the text of their request.\n"+userMessage;
            message+="\n"+"To approve thisr request visit http://www.t-pen.org/TPEN/manuscriptAdmin.jsp?ms="+this.getID()+"&email="+requestor.getUname()+"&submitted=true";
            /**@TODO make mail server a config parameter*/
            m.sendMail(Folio.getRbTok("EMAILSERVER"), "TPEN@t-pen.org", controller.getUname(), "TPEN manuscript access request", message);
            return true;
        }
        catch (Exception e)
        {
            return false;
        }
    }
    
    
    /**
     * Build xml for all public Line positions in a Manuscript. Useful for exporting known Line positions for use in another tool.
     * @return valid xml
     * @throws SQLException 
     */
    public String getLinePositions() throws SQLException
    {
        String toret="";
        String query="select pageNumber, imageName from folios where msID=?";
        Connection j=null;
        PreparedStatement ps=null;
        try {
            j=DatabaseWrapper.getConnection();
            ps=j.prepareStatement(query);
            ps.setInt(1, this.id);
            ResultSet rs=ps.executeQuery();
            while(rs.next())
            {
                Folio f=new Folio(rs.getInt(1));
                toret+="<image name=\""+ESAPI.encoder().encodeForXML(rs.getString(2))+"\">";
                Line [] allLines=f.getlines();
                for(int i=0;i<allLines.length;i++)
                {
                    toret+="<line><x>"+allLines[i].getLeft()+"</x><y>"+allLines[i].getTop()+"</y><w>"+allLines[i].getWidth()+"</w><h>"+allLines[i].getHeight()+"</h></line>\n";
                }
                toret+="</image>";
            }
        }
        finally {
            DatabaseWrapper.closeDBConnection(j);
            DatabaseWrapper.closePreparedStatement(ps);
        }
        return toret;
    }
    
    
    /**
     * Build a list of all Folio numbers in this Manuscript. Useful for checking whether something is part of this set, or adding this set to a virtual Manuscript (Project)
     * @return ordered array sorted using the same method as the dropdown generation for manuscripts
     * @throws SQLException 
     */
    public int [] getFolioNumbers() throws SQLException
    {
        Connection j = null;
        PreparedStatement stmt=null;
        try
        {
            j = DatabaseWrapper.getConnection();
            String qry = "select * from folios where msID=? ";
            stmt = j.prepareStatement(qry);
            stmt.setInt(1, id);
            
            ResultSet rs = stmt.executeQuery();
            Stack<String> pageNames = new Stack();
            Stack<Integer> pageNumbers = new Stack();
            while (rs.next())
            {
                //toret+="<option value=\""+rs.getInt("pageNumber")+"\">"+textdisplay.archive.getShelfMark(rs.getString("archive"))+" "+rs.getString("collection")+" "+rs.getString("pageName")+"</option>";
                //pageNames.add(rs.getString("pageName"));
                pageNames.add(Folio.zeroPadLastNumberFourPlaces(rs.getString("pageName").replace("-000", "")));
                pageNumbers.add(rs.getInt("pageNumber"));
            }
            int[] pageNumbersArray = new int[pageNumbers.size()];
            String[] paddedPageNameArray = new String[pageNames.size()];
            
            for (int i = 0; i < paddedPageNameArray.length; i++)
            {
                paddedPageNameArray[i] = pageNames.elementAt(i);
                pageNumbersArray[i] = pageNumbers.get(i);
            }
            
            //sort the pages according to the zero padded names of the pages
            for (int i = 0; i < paddedPageNameArray.length; i++)
            {
                for (int k = 0; k < paddedPageNameArray.length - 1; k++)
                {
                    if (paddedPageNameArray[k].compareTo(paddedPageNameArray[k + 1]) > 0)
                    {
                        String tmpStr = paddedPageNameArray[k];
                        paddedPageNameArray[k] = paddedPageNameArray[k + 1];
                        paddedPageNameArray[k + 1] = tmpStr;
                        int tmpInt = pageNumbersArray[k];
                        pageNumbersArray[k] = pageNumbersArray[k + 1];
                        pageNumbersArray[k + 1] = tmpInt;
                    }
                }
            }
            return pageNumbersArray;
        }
        finally
        {
            DatabaseWrapper.closeDBConnection(j);
            DatabaseWrapper.closePreparedStatement(stmt);
        }
    }
    
    
    /**
     * Build an array of the folios that constitute this Manuscript
     * @return array of folios in correct order
     * @throws SQLException
     */
    public Folio [] getFolios() throws SQLException
    {
        Connection j = null;
        PreparedStatement stmt=null;
        try
        {
            
            j = DatabaseWrapper.getConnection();
            
            String qry = "select * from folios where msID=? order by sequence,pageNumber ";
            if(this.getArchive().compareTo("Stanford")==0)
                qry = "select * from folios where msID=? order by sequence";
            stmt = j.prepareStatement(qry);
            
            stmt.setInt(1, id);
            
            ResultSet rs = stmt.executeQuery();
            Stack<String> pageNames = new Stack();
            Stack<Integer> pageNumbers = new Stack();
            
            while (rs.next())
            {
                //toret+="<option value=\""+rs.getInt("pageNumber")+"\">"+textdisplay.archive.getShelfMark(rs.getString("archive"))+" "+rs.getString("collection")+" "+rs.getString("pageName")+"</option>";
                //pageNames.add(rs.getString("pageName"));
                pageNames.add(Folio.zeroPadLastNumberFourPlaces(rs.getString("pageName").replace("-000", "")));
                pageNumbers.add(rs.getInt("pageNumber"));
            }
            int[] pageNumbersArray = new int[pageNumbers.size()];
            String[] paddedPageNameArray = new String[pageNames.size()];
            
            for (int i = 0; i < paddedPageNameArray.length; i++)
            {
                paddedPageNameArray[i] = pageNames.elementAt(i);
                pageNumbersArray[i] = pageNumbers.get(i);
            }
            if(this.getArchive().compareTo("Stanford")!=0)
            {
            //sort the pages according to the zero padded names of the pages
                for (int i = 0; i < paddedPageNameArray.length; i++)
                {
                    for (int k = 0; k < paddedPageNameArray.length - 1; k++)
                    {
                        if (paddedPageNameArray[k].compareTo(paddedPageNameArray[k + 1]) > 0)
                        {
                            String tmpStr = paddedPageNameArray[k];
                            paddedPageNameArray[k] = paddedPageNameArray[k + 1];
                            paddedPageNameArray[k + 1] = tmpStr;
                            int tmpInt = pageNumbersArray[k];
                            pageNumbersArray[k] = pageNumbersArray[k + 1];
                            pageNumbersArray[k + 1] = tmpInt;
                        }
                    }
                }
            }
            Folio [] toret=new Folio[pageNumbersArray.length];
            for(int i=0;i<toret.length;i++)
            {
                toret[i]=new Folio(pageNumbersArray[i]);
            }
            
            return toret;
        }
        finally
        {
            DatabaseWrapper.closeDBConnection(j);
            DatabaseWrapper.closePreparedStatement(stmt);
        }
    }
    
    /**
     * This method cleans up the manuscript in which msIdentifier is empty. 
     * @return void
     */
    public void cleanEmptyMSIDManuscript()
    {
        String query = "DELETE FROM manuscript WHERE NOT EXISTS (SELECT * FROM folios WHERE msID = id) AND msIdentifier = ''";
        Connection conn = DatabaseWrapper.getConnection();
        PreparedStatement ps = null;
        try {
            ps = conn.prepareStatement(query);
            ps.executeUpdate();
        } catch (SQLException ex) {
            Logger.getLogger(Manuscript.class.getName()).log(Level.SEVERE, null, ex);
        }finally{
            DatabaseWrapper.closeDBConnection(conn);
            DatabaseWrapper.closePreparedStatement(ps);
        }
    }
    
    
    
}
