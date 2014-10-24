/*
 * @author Jon Deering
Copyright 2011 Saint Louis University. Licensed under the Educational Community License, Version 2.0 (the "License"); you may not use
this file except in compliance with the License.

You may obtain a copy of the License at http://www.osedu.org/licenses/ECL-2.0

Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions
and limitations under the License.
 */
package user;

import java.math.BigInteger;
import java.security.NoSuchAlgorithmException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Stack;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.mail.MessagingException;
import textdisplay.*;
import utils.RandomString;

/**
 *
 * Represents a user of the application. This handles creating new accounts, authenticating users, and easily getting data attached to the account.
 */
public class User
    {

    private int UID;
    private String Uname;
    private String fname;
    private String lname;
    private String openID;

    private String getOpenID()
        {
        return openID;
        }

    /**Get the unique user identifier for the owner of this comment
     * @return 
     */
    public int getUID()
        {
        return UID;
        }

    /**Returns the last Folio this person transcribed which was NOT in a Project, -1 indicates nothing was found*/
    public int getLastModifiedFolio() throws SQLException
        {

        String query = "select folio from transcription where creator=? and projectID=0 order by date desc limit 1";
        Connection j = null;
PreparedStatement ps=null;
        try
            {
            j = DatabaseWrapper.getConnection();
            ps = j.prepareStatement(query);
            ps.setInt(1, UID);
            ResultSet rs = ps.executeQuery();
            if (rs.next())
                {
                return rs.getInt(1);
                } else
                {
                return -1;
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
    /**Returns a date object representing the last time this user was active, or the epoc (time 0) if they have never been active*/
    public Date getLastActiveDate() throws SQLException
        {

        String query = "select lastActive from users where UID=?";
        Connection j = null;
PreparedStatement ps=null;
        try
            {
            j = DatabaseWrapper.getConnection();
            ps = j.prepareStatement(query);
            ps.setInt(1, UID);
            ResultSet rs = ps.executeQuery();
            if (rs.next())
                {
                    String toret="";
                    try{
                        return new Date(rs.getTimestamp(1).getTime());
                        
                    }
                    catch(SQLException e)
                    {
                        return new Date(0);
                    }
                        
                
                } else
                {
                return new Date(0);
                }
            } finally
            {
DatabaseWrapper.closeDBConnection(j);
DatabaseWrapper.closePreparedStatement(ps);
            }
        }
    /**return the number of minutes since last activity, or -1 if they have never been active*/
    public int getMinutesSinceLastActive() throws SQLException
    {
        Date d=this.getLastActiveDate();
        if(d.getTime()==0)
            return -1;
        long msdiff=System.currentTimeMillis()-d.getTime();
                        int minutesDiff=(int) (msdiff/60000);
                        
                        return minutesDiff;
    }
/**Returns the last Folio this person transcribed, -1 indicates nothing was found*/
    public String getAnyLastModifiedFolio() throws SQLException
        {
        String query = "select folio,projectID from transcription where creator=? order by date desc limit 1";
        String toret = "";
        Connection j = null;
PreparedStatement ps=null;
        try
            {
            j = DatabaseWrapper.getConnection();
            ps = j.prepareStatement(query);
            ps.setInt(1, UID);
            ResultSet rs = ps.executeQuery();
            if (rs.next())
                {
                toret += "" + rs.getInt(1);
                toret += ",";
                toret += "" + rs.getInt(2);
                return toret;
                } else
                {
                return "-1";
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

    /**Returns the last Project the user modified a page in, -1 indicates nothing was found*/
    public int getLastModifiedProject() throws SQLException
        {
        String query = "select projectID from transcription where projectID>0 and creator=? order by date desc limit 1";
        Connection j = null;
PreparedStatement ps=null;
        try
            {
            j = DatabaseWrapper.getConnection();
            ps = j.prepareStatement(query);
            ps.setInt(1, UID);
            ResultSet rs = ps.executeQuery();
            if (rs.next())
                {
                return rs.getInt(1);
                } else
                {
                return -1;
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

    /**Get the username (part of email address before @) of the owner of this comment
     * @return
     */
    public String getUname()
        {
        try
            {

            return openID.substring(0, openID.indexOf('@'));
            } catch (Exception e)
            {
            return Uname;
            }
        }

    /**Get the first name of the owner of this comment
     * @return
     */
    public String getFname()
        {
        return fname;
        }

    /**Get the first name of the owner of this comment
     * @return
     */
    public String getLname()
        {
        return lname;
        }

    /** Populate the user's info from the database
     * @param id user's unique id
     */
    public User(int id) throws SQLException
        {
        Connection j = null;
PreparedStatement qry=null;
        qry=null;
        try
            {
            j = DatabaseWrapper.getConnection();
            
            qry = j.prepareStatement("select * from users where UID=?");
            qry.setInt(1, id);
            ResultSet rs = qry.executeQuery();
            if (rs.next())
                {
                UID = id;
                Uname = rs.getString("Uname");
                lname = rs.getString("lname");
                fname = rs.getString("fname");
                this.openID = rs.getString("openID");
                }
            } finally
            {
            DatabaseWrapper.closeDBConnection(j);
            DatabaseWrapper.closePreparedStatement(qry);
            }
        }

    /**
     * Populate the object based on the openid response
     * @param openID The unique URL returned by the user's openid provider
     * @param smt A dummy param added because a single String constructor already exists.
     * @throws SQLException
     */
    public User(String openID, Boolean smt) throws SQLException
        {
        Connection j = null;
PreparedStatement qry=null;
        qry=null;
        try
            {
            j = DatabaseWrapper.getConnection();
            
            qry = j.prepareStatement("select * from users where openID=?");
            qry.setString(1, openID);
            ResultSet rs = qry.executeQuery();
            if (rs.next())
                {
                UID = rs.getInt("UID");
                Uname = rs.getString("Uname");
                lname = rs.getString("lname");
                fname = rs.getString("fname");
                this.openID = rs.getString("openID");
                }
            } finally
            {
            DatabaseWrapper.closeDBConnection(j);
            DatabaseWrapper.closePreparedStatement(qry);
            }
        }

    /**
     * Used to facilitate logins via username(email) and password
     * @param uname username, typically email address
     * @param pass unhashed password
     * @throws NoSuchAlgorithmException
     */
    public User(String uname, String pass) throws NoSuchAlgorithmException
        {
        Connection j = null;
PreparedStatement qry=null;
        qry=null;
        try
            {
            j = DatabaseWrapper.getConnection();
            
            
            qry = j.prepareStatement("Select UID,pass from users where Uname=? and pass=SHA1(?)");
            qry.setString(1, uname);
            qry.setString(2, pass);
            ResultSet rs;
            rs = qry.executeQuery();
            if (rs.next())
                {
                this.UID = Integer.parseInt(rs.getString("UID"));
                this.Uname = uname;
                User tmp = new User(UID);
                this.fname = tmp.fname;
                this.lname = tmp.lname;
                
                } else
                {
                qry = j.prepareStatement("Select UID,pass from users where Uname=? and pass=(?)");
                qry.setString(1, uname);
                 java.security.MessageDigest d =null;
            d = java.security.MessageDigest.getInstance("SHA-1");
            d.reset();
            d.update(pass.getBytes());
             pass=new String(d.digest());

            qry.setString(2, pass);
            rs=qry.executeQuery();
            
            
            rs = qry.executeQuery();
            if (rs.next())
                {
                this.UID = Integer.parseInt(rs.getString("UID"));
                this.Uname = uname;
                User tmp = new User(UID);
                this.fname = tmp.fname;
                this.lname = tmp.lname;

                }
                return;
                }
            } catch (Exception e)
            {
            return;
            } finally
            {
            DatabaseWrapper.closeDBConnection(j);
            DatabaseWrapper.closePreparedStatement(qry);
            }
        }

    /**
     * Populate the user's info from the database if and only if the password provided is correct.
     * @param id user's unique id
     * @param pass md5 hashed password
     * @throws SQLException
     */
    public User(int id, String pass) throws SQLException
        {
        Connection j = null;
PreparedStatement qry=null;
        qry=null;
        try
            {
            j = DatabaseWrapper.getConnection();
            
            //the openID='' means prevents someone trying to login to an openid account using a password, they can only use openid
            qry = j.prepareStatement("select * from users where UID=? and password=? and openID=''");
            qry.setInt(1, id);
            qry.setString(2, pass);
            ResultSet rs = qry.executeQuery();
            if (rs.next())
                {
                UID = id;
                Uname = rs.getString("Uname");
                lname = rs.getString("lname");
                fname = rs.getString("fname");
                this.openID = rs.getString("openID");
               
                } else
                {
               
                UID = 0;
                }
            } finally
            {
            DatabaseWrapper.closeDBConnection(j);
            DatabaseWrapper.closePreparedStatement(qry);
            }
        }
/**Record that this user accepts the TPEN terms of use*/
    public void acceptUserAgreement() throws SQLException
    {
        String query="update users set accepted=?, hasAccepted=1 where uid=?";
        Connection j=null;
PreparedStatement ps=null;
        try{
            j=DatabaseWrapper.getConnection();
            ps=j.prepareStatement(query);
            ps.setTimestamp(1, new java.sql.Timestamp(System.currentTimeMillis()));
            ps.setInt(2, UID);
            ps.execute();
        }
        finally{
DatabaseWrapper.closeDBConnection(j);
DatabaseWrapper.closePreparedStatement(ps);
        }
    }
    /**Check whether this user has accepted the TPEN terms of use*/
    public Boolean hasAcceptedUserAgreement() throws SQLException, ParseException
    {
        String query="select hasAccepted from users where uid=?";
        Connection j=null;
PreparedStatement ps=null;
        try{
            j=DatabaseWrapper.getConnection();
            ps=j.prepareStatement(query);
            ps.setInt(1, UID);
            ResultSet rs=ps.executeQuery();
            //DateFormat dfm=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                                
            if(rs.next())
            {
                if(rs.getInt("hasAccepted")==1)
                {
                    return true;
                }
            }
        }
        finally{
DatabaseWrapper.closeDBConnection(j);
DatabaseWrapper.closePreparedStatement(ps);
        }
        return false;
    }
    //Returns 0 on success, 1 on email error, or 2 if the user object is skrewy.
    public int contactTeam(String body)
    {
        
        
        if (this.getUID() > 0)
            {
            textdisplay.mailer m = new textdisplay.mailer();
            body = this.getFname() + " " + this.getLname() + " (" + this.getUname() + ") says:\n"+body;
            
            try
                {
                m.sendMail(Folio.getRbTok("EMAILSERVER"), "TPEN@t-pen.org", Folio.getRbTok("NOTIFICATIONEMAIL"), "TPEN contact", body);
                } catch (Exception e)
                {
                return 2; //created user, but email issue occured
                }
            return 0; //total success
            } else
            {
            return 1; //improper user
            }
        }
    
    /**
     * Sign up a new user. Returns 0 for success, 1 for failure to create user account, and 2 for success creating user but failure to send notification email, likely due to being
     * run on a test system with no email server
     * @param uname email address
     * @param lname last name
     * @param fname first name
     * @return
     * @throws SQLException
     */
    public static int signup(String uname, String lname, String fname) throws SQLException
        {
        User newUser = new User(uname, lname, fname, "");
        if (newUser.getUID() > 0)
            {
            textdisplay.mailer m = new textdisplay.mailer();
            String body = newUser.getFname() + " " + newUser.getLname() + " (" + newUser.getUname() + ") has created a new account, which needs your approval.\n";
            body += "Proceed to http://t-pen.org/TPEN/admin.jsp to approve their account";
            try
                {
                m.sendMail(Folio.getRbTok("EMAILSERVER"), "TPEN@t-pen.org", Folio.getRbTok("NOTIFICATIONEMAIL"), "new user request", body);
                } catch (Exception e)
                {
                return 2; //created user, but email issue occured
                }
            return 0; //total success
            } else
            {
            return 1; //failed to create
            }
        }

    /**
     * Populate a user object based on their username(email). The idea is that if a user needs to be invited to a group it may be done via their email
     * @param Uname username (email address)
     * @throws SQLException
     */
    public User(String Uname) throws SQLException
        {
        Connection j = null;
PreparedStatement qry=null;
        qry=null;
        try
            {
            j = DatabaseWrapper.getConnection();
            
            qry = j.prepareStatement("select * from users where Uname=?");
            qry.setString(1, Uname);
            ResultSet rs = qry.executeQuery();
            if (rs.next())
                {
                UID = rs.getInt("UID");
                this.Uname = Uname;
                lname = rs.getString("lname");
                fname = rs.getString("fname");
                this.openID = rs.getString("openID");

                }
            } finally
            {
            DatabaseWrapper.closeDBConnection(j);
            DatabaseWrapper.closePreparedStatement(qry);
            }
        }

    /**
     * Create a new user account that authenticates via uname and password. Password is expected to be hashed
     * @param Uname username(email)
     * @param lname last name
     * @param fname first name
     * @param password hashed password
     * @throws SQLException
     */
    public User(String Uname, String lname, String fname, String password) throws SQLException
        {

        this.Uname = Uname;
        this.lname = lname;
        this.fname = fname;

        if (!this.exists())
            {
            this.commit(password);
            this.UID = new User(Uname).UID;
            } else
            {
            this.UID = -1;
            }

        }

    /**Create a new user account that authenticates via openid

     * @param Uname email address
     * @param openID the unique url assigned to the user by their openid provider
     * @param fname first name
     * @param lname last name
     * @param password md5 hashed password
     * @throws SQLException
     *
     */
    public User(String Uname, String lname, String fname, String password, String openID) throws SQLException
        {

        this.Uname = Uname;
        this.lname = lname;
        this.fname = fname;
        this.openID = openID;
        if (!this.exists())
            {
            this.commit(password);
            this.UID = new User(Uname).UID;
            } else
            {
            this.UID = -1;
            }

        }

    /**
     * Deactivate the user by setting this user's password to empty, which prevents them logging in until they are approved by an administrator
     * @throws SQLException
     */
    public void deactivate() throws SQLException
        {
        String query = "update users set pass='inactive' where UID=?";
        Connection j = null;
PreparedStatement ps=null;
        try
            {
            j = DatabaseWrapper.getConnection();
            ps = j.prepareStatement(query);
            ps.setInt(1, UID);
            ps.execute();
            } finally
            {
DatabaseWrapper.closeDBConnection(j);
DatabaseWrapper.closePreparedStatement(ps);
            }

        }

    /**
     * List all users active users (primarily used to allow them to be deactivated)
     * @return Array of users currently permitted to access TPEN
     * @throws SQLException
     */
    public static User[] getAllActiveUsers() throws SQLException
        {
        User[] toret = new User[0];
        String query = "select UID from users where pass>'' and pass!='inactive' order by lname, fname";
        Connection j = null;
        PreparedStatement ps=null;
        try
            {
            j = DatabaseWrapper.getConnection();
            ps = j.prepareStatement(query);
            ResultSet rs = ps.executeQuery();
            while (rs.next())
                {
                User[] tmp = new User[toret.length + 1];
                for (int i = 0; i < toret.length; i++)
                    {
                    tmp[i] = toret[i];
                    }
                toret = tmp;
                toret[toret.length - 1] = new User(rs.getInt(1));
                }


            } finally
            {
                DatabaseWrapper.closeDBConnection(j);
                DatabaseWrapper.closePreparedStatement(ps);
            }
        return toret;
        }

        /**Returns all users active in the last 2 months, or null if none*/
    public static User[] getRecentUsers() throws SQLException {
        User[] active;
        String query = "SELECT DISTINCT creator FROM transcription WHERE DATE > ( NOW( ) + INTERVAL -2 MONTH )";
        Connection j = null;
        PreparedStatement ps = null;
        Stack<User> tmp = new Stack();
        try {
            j = DatabaseWrapper.getConnection();
            ps = j.prepareStatement(query);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                tmp.push(new User(rs.getInt(1)));
            }
        } finally {
            DatabaseWrapper.closeDBConnection(j);
            DatabaseWrapper.closePreparedStatement(ps);
        }
        active = new User[tmp.size()];
        //odd looking way of doing this copy, I know, but it was convenient
        while (!tmp.empty()) {
            active[tmp.size() - 1] = tmp.pop();
        }
        return active;
    }

    /**
     * Return an array of users who have blank passwords and openids, which indicates they are awaiting approval by an admin
     * @return array of users who are not currently approved to access TPEN
     * @throws SQLException
     */

    public static User[] getUnapprovedUsers() throws SQLException
        {
        User[] toret = new User[0];
        String query = "select UID from users where pass='' and openID=''";
        Connection j = null;
PreparedStatement ps=null;
        try
            {
            j = DatabaseWrapper.getConnection();
            ps = j.prepareStatement(query);
            ResultSet rs = ps.executeQuery();
            while (rs.next())
                {
                User[] tmp = new User[toret.length + 1];
                for (int i = 0; i < toret.length; i++)
                    {
                    tmp[i] = toret[i];
                    }
                toret = tmp;
                toret[toret.length - 1] = new User(rs.getInt(1));
                }


            } finally
            {
DatabaseWrapper.closeDBConnection(j);
DatabaseWrapper.closePreparedStatement(ps);
            }
        return toret;
        }

    /**
     * Save this user record only if it is a new user. Does nothing if the user already exists. 
     * @param pass should be the hash of the SHA-1 password they will be using
     * @throws SQLException
     */
    public void commit(String pass) throws SQLException
        {
        Connection j = null; 
PreparedStatement qry=null;
        qry=null;
        try
            {
            j = DatabaseWrapper.getConnection();
           
            if (this.exists())
                {
                /*qry=j.prepareStatement("update users set Uname=?, lname=?, fname=? where UID=?");
                qry.setInt(4, UID);
                qry.setString(1, Uname);
                qry.setString(2, lname);
                qry.setString(3, fname);
                qry.execute();*/
                } else
                {
                qry = j.prepareStatement("insert into users (Uname, lname, fname, pass,openID) values(?,?,?,?,?)");
                if(pass.compareTo("")==0)
                    qry = j.prepareStatement("insert into users (Uname, lname, fname, pass,openID) values(?,?,?,?,?)");
                qry.setString(1, Uname);
                qry.setString(2, lname);
                qry.setString(3, fname);
                qry.setString(4, pass);
                if (this.openID == null)
                    {
                    openID = "";
                    }
                qry.setString(5, openID);
                qry.execute();


                }
            } finally
            {
            DatabaseWrapper.closeDBConnection(j);
            DatabaseWrapper.closePreparedStatement(qry);
            }
        }

    /**
     * Create a new sudo random password for this user, set their password to match it, and return the text of the new password
     * @return the new password
     * @throws SQLException
     * @throws NoSuchAlgorithmException
     * @throws MessagingException
     */
    public String resetPassword() throws SQLException, NoSuchAlgorithmException, MessagingException
        {
        String newPass = "";
        RandomString r = new RandomString(10);
        newPass = r.nextString();
        
        //store the hashed version in pass, that goes to the DB and the unhased version is returned
        String pass = newPass;
        String query = "update users set pass=SHA1(?) where UID=?";
        Connection j = null;
PreparedStatement ps=null;
        try
            {
            j = DatabaseWrapper.getConnection();
            ps = j.prepareStatement(query);
            ps.setString(1, pass);
            ps.setInt(2, UID);
            ps.execute();
            } finally
            {
DatabaseWrapper.closeDBConnection(j);
DatabaseWrapper.closePreparedStatement(ps);
            }
        textdisplay.mailer m = new textdisplay.mailer();
        String body = "Your TPEN password has been set to " + newPass + "\n" + "You should head to http://t-pen.org and change it.";
//        System.out.print("new pass is "+pass+"\n");
        m.sendMail(Folio.getRbTok("EMAILSERVER"), "TPEN@t-pen.org", this.Uname, "TPEN Password Reset", body);
        return newPass;
        }
    public String resetPassword(Boolean email) throws SQLException, NoSuchAlgorithmException, MessagingException
        {
            if(email)return(resetPassword());
            
        String newPass = "";
        RandomString r = new RandomString(10);
        newPass = r.nextString();
        
        //store the hashed version in pass, that goes to the DB and the unhased version is returned
        String pass = newPass;
        String query = "update users set pass=SHA1(?) where UID=?";
        Connection j = null;
PreparedStatement ps=null;
        try
            {
            j = DatabaseWrapper.getConnection();
            ps = j.prepareStatement(query);
            ps.setString(1, pass);
            ps.setInt(2, UID);
            ps.execute();
            } finally
            {
DatabaseWrapper.closeDBConnection(j);
DatabaseWrapper.closePreparedStatement(ps);
            }
        return newPass;
        }
    /**Send the welcome message and set the user's password.*/
    public String activateUser() throws SQLException, NoSuchAlgorithmException, MessagingException, Exception
        {
        textdisplay.mailer m = new textdisplay.mailer();
//        System.out.print("new pass is "+pass+"\n");
        String pass=resetPassword(false);
        m.sendMail(Folio.getRbTok("EMAILSERVER"), Folio.getRbTok("NOTIFICATIONEMAIL"), this.Uname, "Welcome to TPEN", new WelcomeMessage().getMessage(this.fname+" "+this.lname,pass) );
        return this.resetPassword();
        }
    /**This sets the last time the user was active to the current time. Used for determining who is online, and keeping track of active vs inactive users*/
    public void updateLastActive() throws SQLException
    {
        String query="update users set lastActive=now() where UID=?";
        Connection j=null;
PreparedStatement ps=null;
        try
        {
            j=DatabaseWrapper.getConnection();
            ps=j.prepareStatement(query);
            ps.setInt(1, UID);
            ps.execute();
        }
        finally
        {
DatabaseWrapper.closeDBConnection(j);
DatabaseWrapper.closePreparedStatement(ps);
        }
    }
    /**Returns all users who have been active in the last 60 seconds*/
public static User[] getLoggedInUsers() throws SQLException
    {
        String query="select UID from users where UNIX_TIMESTAMP(lastActive)>(UNIX_TIMESTAMP(now())-60)";
        Connection j=null;
PreparedStatement ps=null;
        Stack<User> tmp=new Stack();
        try
        {
            j=DatabaseWrapper.getConnection();
            ps=j.prepareStatement(query);
            
            ResultSet rs=ps.executeQuery();
            while(rs.next())
                    {
                        tmp.push(new User(rs.getInt(1)));
                    }
        }
        finally
        {
DatabaseWrapper.closeDBConnection(j);
DatabaseWrapper.closePreparedStatement(ps);
        }
        User [] toret=new User[tmp.size()];
        for(int i=0;i<toret.length;i++)
        {
            toret[i]=tmp.pop();
        }
        return toret;
    }
    /**
     * Is there already a record for this Uname or ID?
     * @return
     * @throws SQLException
     */
    public Boolean exists() throws SQLException
        {
        Connection j = null;
PreparedStatement qry=null;
        qry=null;
        try
            {
            if (openID != null)
                {
                return false;
                }
            j = DatabaseWrapper.getConnection();
            
            qry = j.prepareStatement("select * from users where Uname=?");
            qry.setString(1, Uname);
            ResultSet rs = qry.executeQuery();
            if (rs.next())
                {
                return true;
                }
            qry = j.prepareStatement("select * from users where UID=?");
            qry.setInt(1, UID);
            rs = qry.executeQuery();
            if (rs.next())
                {
                return true;
                }

            return false;
            } finally
            {
            DatabaseWrapper.closeDBConnection(j);
            DatabaseWrapper.closePreparedStatement(qry);
            }
        }

    /**
     * Get the groups this user belongs to
     * @return
     */
    public Group[] getUserGroups()
        {
        Connection j = null;
PreparedStatement qry=null;
        qry=null;
        try
            {
            j = DatabaseWrapper.getConnection();

            
            qry = j.prepareStatement("select * from groupMembers where UID=?");
            qry.setInt(1, UID);
            ResultSet rs = qry.executeQuery();
            int recordCount = 0;
            while (rs.next())
                {
                recordCount++;
                }
            rs.beforeFirst();
            Group[] groups = new Group[recordCount];
            recordCount = 0;
            while (rs.next())
                {
                groups[recordCount] = new Group(rs.getInt("GID"));
                recordCount++;
                }
DatabaseWrapper.closeDBConnection(j);
DatabaseWrapper.closePreparedStatement(qry);
            return groups;
            } catch (SQLException e)
            {
            return null;
            } finally
            {
            DatabaseWrapper.closeDBConnection(j);
            DatabaseWrapper.closePreparedStatement(qry);
            }
        }
/**
 * Return an int of lines of transcription for which this creator
 * @param uid int User ID
 * @return int count of lines
 * @throws SQLException 
 */
    public int getUserTranscriptionCount() throws SQLException
{
    String query="select * from transcription where creator=?";
    Connection j=null;
PreparedStatement ps=null;
    Stack<Transcription> orderedTranscriptions=new Stack();
    try{
        j=DatabaseWrapper.getConnection();
        ps=j.prepareStatement(query);
        ps.setInt(1, UID);
        ResultSet rs=ps.executeQuery();
        int recordCount = 0;
            while (rs.next())
                {
                recordCount++;
                }
        return recordCount;
    }
    finally
    {
DatabaseWrapper.closeDBConnection(j);
DatabaseWrapper.closePreparedStatement(ps);
    }
    
}
        /**
         * Return an array of all projects this user is a member of
         * @return array of projects
         * @throws SQLException
         */

    public Project[] getUserProjects() throws SQLException
        {
        new ProjectPriority(UID).verifyPriorityContents();
        String query = "select distinct(project.id) from project join groupMembers on grp=GID join projectPriorities on id=projectID where groupMembers.UID=? and projectPriorities.uid=?  order by projectPriorities.priority desc, project.name desc";
        Connection j = null;
PreparedStatement ps=null;
        try
            {
            j = DatabaseWrapper.getConnection();
            ps = j.prepareStatement(query);
            ps.setInt(1, UID);
            ps.setInt(2, UID);
            ResultSet rs = ps.executeQuery();
            Stack<Integer> projectIDs = new Stack();
            while (rs.next())
                {
                projectIDs.push(rs.getInt("project.id"));
                }
            Project[] toret = new Project[projectIDs.size()];
            int ctr = 0;
            while (!projectIDs.empty())
                {
                toret[ctr] = new Project(projectIDs.pop());
                ctr++;
                }
            return toret;
            } finally
            {
DatabaseWrapper.closeDBConnection(j);
DatabaseWrapper.closePreparedStatement(ps);
            }
        }

    /**
     * Returns a dropdown of all the users who have work that can be reviewed
     * @return
     * @throws SQLException
     */
    public String getAllUsersDropdown() throws SQLException
        {
        Connection j = null;
PreparedStatement qry=null;
        qry=null;
        try
            {
            String toret = "";
            j = DatabaseWrapper.getConnection();
            String qryText = "select distinct creator from transcription";
            
            qry = j.prepareStatement(qryText);
            ResultSet rs = qry.executeQuery();
            while (rs.next())
                {
                User tmp = new User(rs.getInt("creator"));
                toret += "<option value=\"" + tmp.UID + "\">" + tmp.fname + " " + tmp.lname + "</option>";
                }
DatabaseWrapper.closeDBConnection(j);
DatabaseWrapper.closePreparedStatement(qry);
            return toret;
            } finally
            {
            DatabaseWrapper.closeDBConnection(j);
            DatabaseWrapper.closePreparedStatement(qry);
            }
        }

    /**Inivte a new user to join TPEN. Returns 0 on successful user creation, 1 on user creation failure, and 2 if user creation succeeded
    but email failed, which is common on test systems and should produce a warning*/
    public int invite(String uname, String fname, String lname) throws SQLException
        {
        Boolean emailFailure = false;
        User newUser = new User(uname, lname, fname, "");
        if (newUser.getUID() > 0)
            {
            textdisplay.mailer m = new textdisplay.mailer();
            String body = this.getFname() + " " + this.getLname() + " (" + this.getUname() + ") has invited  " + newUser.getFname() + " " + newUser.getLname() + " (" + newUser.getUname() + ") to join TPEN, which needs your approval.\n";
            body += "Proceed to http://t-pen.org/TPEN/admin.jsp to approve their account";
            try
                {
                m.sendMail(Folio.getRbTok("EMAILSERVER"), "TPEN@t-pen.org", Folio.getRbTok("NOTIFICATIONEMAIL"), "new user request", body);
                } catch (Exception e)
                {
                emailFailure = true;
                }
            //send a notification email to the invitee
            body = this.getFname() + " " + this.getLname() + " (" + this.getUname() + ") has invited you to join their transcription project on TPEN. At this early stage in testing, an administrator must activate your account before you will recieve an email with your password.\n Thank you for your patience.\nThe TPEN team";
            try
                {
                m.sendMail(Folio.getRbTok("EMAILSERVER"), "TPEN@t-pen.org", newUser.getUname(), "An invitation to transcribe on TPEN", body);
                } catch (Exception e)
                {
                emailFailure = true;
                }
            if (!emailFailure)
                {
                return 0;
                } else
                {
                return 2;
                }
            }

        return 1;
        }

    /**
     * Is this user an application administrator
     * @return
     */
    public Boolean isAdmin() throws SQLException
        {
        String query="select * from admins where uid=?";
        Connection j = null;
PreparedStatement qry=null;
        qry=null;
        try
            {
            j = DatabaseWrapper.getConnection();
            qry=j.prepareStatement(query);
            qry.setInt(1, UID);
            ResultSet rs=qry.executeQuery();
            if(rs.next())
                return true;
            return false;
        }
        finally
        {
            DatabaseWrapper.closeDBConnection(j);
            DatabaseWrapper.closePreparedStatement(qry);
        }
        
        }
    /**
     * Is this user account awaiting admin approval?
     * @return true if they havent been approved yet. This sets their password to "" which is an impossible sha-1 hash
     * @throws SQLException 
     */

    public Boolean requiresApproval() throws SQLException
        {
        String query = "select UID from users where UID=? and pass=''";
        Connection j = null;
PreparedStatement ps=null;
        try
            {
            j = DatabaseWrapper.getConnection();
            ps = j.prepareStatement(query);
            ps.setInt(1, UID);
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

    /**Update this user's password.
    @param password The plain text of the new password*/
    public void updatePassword(String password) throws NoSuchAlgorithmException, SQLException
        {
        
        String query = "update users set pass=SHA1(?) where UID=?";
        Connection j = null;
PreparedStatement ps=null;
        try
            {
            j = DatabaseWrapper.getConnection();
            ps = j.prepareStatement(query);
            ps.setString(1, password);
            ps.setInt(2, UID);
            ps.execute();
            } finally
            {
DatabaseWrapper.closeDBConnection(j);
DatabaseWrapper.closePreparedStatement(ps);
            }
        }
    /**
     * Record the user's acceptance of the ipr agreement
     * @param folioNum Folio they were viewing when they accepted it
     * @throws SQLException 
     */
    public void acceptIPR(int folioNum) throws SQLException
    {
         Connection j = null;
PreparedStatement ps=null;
        try
            {
        j=DatabaseWrapper.getConnection();
        String query="insert into ipr (uid,archive) values(?,?)";
        ps=j.prepareStatement(query);
        ps.setString(2, new Folio(folioNum).getArchive());
        ps.setInt(1, UID);
        ps.execute();
        }
        finally
        {
DatabaseWrapper.closeDBConnection(j);
DatabaseWrapper.closePreparedStatement(ps);
        }
    }

    /**Has this user accepted the IPR inde\mnity message for this image collection*/
    public Boolean hasAcceptedIPR(int folioNum) throws SQLException
        {
        Connection j = null;
PreparedStatement qry=null;
        qry=null;
        try
            {
      
            Folio fol = new Folio(folioNum);
            fol.getArchive();
            String query = "select * from ipr where archive=? and uid=?";
            j = DatabaseWrapper.getConnection();
            
            qry = j.prepareStatement(query);
            qry.setString(1, fol.getArchive());
            qry.setInt(2, UID);
            ResultSet rs = qry.executeQuery();
            if (rs.next())
                {
                //we have already recoreded their acceptance
                
                return true;
                }
           
            return false;
            } finally
            {
            DatabaseWrapper.closeDBConnection(j);
            DatabaseWrapper.closePreparedStatement(qry);
            }
        }
    /**Return an array of all manuscripts this person controls. If they are an admin, that is every restricted ms*/
    public Manuscript [] getUserControlledManuscripts() throws SQLException
    {
        Manuscript [] toret=new Manuscript [0];
        //all restricted manuscripts have the user id of the controller in the restricted field

        String query="select * from manuscript where restricted=? order by city, repository,msIdentifier";
        //if they are an admin, get all restricted MSS
        if(this.isAdmin())
            query="select * from manuscript where restricted>0 order by city, repository,msIdentifier";
        Connection j=null;
PreparedStatement ps=null;
        try{
            j=DatabaseWrapper.getConnection();
            ps=j.prepareStatement(query);
            if(!this.isAdmin())
            ps.setInt(1, UID);
            ResultSet rs=ps.executeQuery();
            while(rs.next())
            {
                //elongate that array, add the new member
                Manuscript ms=new Manuscript(rs.getInt("id"),true);
                Manuscript [] tmp=new Manuscript[toret.length+1];
                for(int i=0;i<toret.length;i++)
                    tmp[i]=toret[i];
                tmp[tmp.length-1]=ms;
                toret=tmp;
            }
        }
        finally
        {
DatabaseWrapper.closeDBConnection(j);
DatabaseWrapper.closePreparedStatement(ps);
        }

        return toret;
    }
    public static User[] getAdmins() throws SQLException
    {
        String query="select * from admins";
        Connection j=null;
        PreparedStatement ps=null;
        ArrayList toret=new ArrayList();
        try{
            j=DatabaseWrapper.getConnection();
            ps=j.prepareStatement(query);
            ResultSet rs=ps.executeQuery();
            while(rs.next())
            {
                toret.add(new User(rs.getInt(1)));
            }
        }
        finally
        {
            DatabaseWrapper.closeDBConnection(j);
            DatabaseWrapper.closePreparedStatement(ps);
        }
        User [] tr=new User[toret.size()];
        for (int i=0;i<tr.length;i++){
        tr[i]=(User) toret.get(i);
        }
        return tr;
    }
    
    }
