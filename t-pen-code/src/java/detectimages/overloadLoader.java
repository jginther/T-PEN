
/*
 * @author Jon Deering
 Copyright 2011 Saint Louis University. Licensed under the Educational Community License, Version 2.0 (the "License"); you may not use
 this file except in compliance with the License.

 You may obtain a copy of the License at http://www.osedu.org/licenses/ECL-2.0

 Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS,
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions
 and limitations under the License.
 */

package detectimages;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.Vector;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadFactory;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import textdisplay.DatabaseWrapper;

/**This class handles the manipulation of the massive result sets created by the paleographic comparison process. It has legacy code for using a database
 as well as the current, flat file based code.*/
public class overloadLoader
{
    public static void setBlob(blob b)
    {
        
    }
    public static void setBlobs() throws SQLException
    {
        Connection j=DatabaseWrapper.getConnection();
         String selectQuery="select distinct(`img2`) from blobmatches";
         PreparedStatement select=j.prepareStatement(selectQuery);
         ResultSet imgs=select.executeQuery();
        while (imgs.next())
        {
            try {
                Vector <blob> b=blob.getBlobs("/usr/data/"+imgs.getString(1).replace(".jpg", "")+".txt");
                String insertQuery="insert into blobs (`blob`, `img`,x,y,h,w) values(?,?,?,?,?,?)";
                PreparedStatement ps=j.prepareStatement(insertQuery);
                for(int i=0;i<b.size();i++)
                {
                    blob current=b.elementAt(i);
                    ps.setInt(1, current.id);
                    ps.setString(2, imgs.getString(1));
                    ps.setInt(3, current.x);
                    ps.setInt(4, current.y);
                    ps.setInt(5, current.height);
                    ps.setInt(6, current.width);
                    ps.execute();

                }

            } catch (FileNotFoundException ex) {
                Logger.getLogger(overloadLoader.class.getName()).log(Level.SEVERE, null, ex);
            } catch (IOException ex) {
                Logger.getLogger(overloadLoader.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
    public static void setCharCounts() throws SQLException
    {
        Connection j=DatabaseWrapper.getConnection();
        String selectQuery="select distinct(`img1`) from blobmatches";
        String selectQuery2="select distinct(`blob1`) from blobmatches where `img1`=?";
        String selectQuery3="select count(img1) from blobmatches where `img1`=? and `blob1`=?";
         PreparedStatement select=j.prepareStatement(selectQuery);
         PreparedStatement select2=j.prepareStatement(selectQuery2);
         PreparedStatement select3=j.prepareStatement(selectQuery3);
        String insertQuery="insert into charactercount (img, `blob`, count, MS) values(?,?,?,415)";
        PreparedStatement ins=j.prepareStatement(insertQuery);
        System.out.print("getting distinct images\n");
        System.out.flush();
        ResultSet imgs=select.executeQuery();
        while (imgs.next())
        {
            System.out.print("getting blobs for image "+imgs.getString(1)+"\n");
            System.out.flush();
            select2.setString(1, imgs.getString(1));
            ResultSet blobs=select2.executeQuery();
            while(blobs.next())
            {
                //System.out.print("getting count for blob "+blobs.getString(1)+"\n");
            //System.out.flush();
                select3.setString(1, imgs.getString(1));
                select3.setString(2, blobs.getString(1));
                ResultSet rs=select3.executeQuery();
                rs.next();
                ins.setString(1,imgs.getString(1));
                ins.setString(2,blobs.getString(1));
                ins.setInt(3, rs.getInt(1));
                ins.execute();

            }
        }
    }
    public overloadLoader(File dir) throws FileNotFoundException, IOException, SQLException
    {
        Connection j=DatabaseWrapper.getConnection();
        String selectQuery="select count from charactercount where img=? and `blob`=?";
        PreparedStatement select=j.prepareStatement(selectQuery);
        String insertQuery="insert into charactercount (img, `blob`, count, MS) values(?,?,?,415)";
        PreparedStatement insert=j.prepareStatement(insertQuery);
        String updateQuery="update charactercount set count=? where img=? and `blob`=?";
        PreparedStatement update=j.prepareStatement(updateQuery);
        BufferedReader r;
        File [] files=dir.listFiles();
        for(int i=0;i<files.length;i++)
        {
        System.out.print(files[i].getName()+"\n");
        r= new BufferedReader(new FileReader(files[i]));
        String buff;
        while(r.ready())
        {
            buff=r.readLine();
            String [] imageNames=buff.split("    ");
            String img1=imageNames[0].split(":")[0];
            //blob id number, counts from 1-the number of blobs on the page
            int count1=Integer.parseInt(imageNames[0].split(":")[1]);
            String img2=imageNames[1].split(":")[0];
            int count2=Integer.parseInt(imageNames[1].split(":")[1]);
            select.setString(1, img1);
            select.setInt(2, count1);
            ResultSet rs=select.executeQuery();
            //if there was a result, we already have a count running for this blob, just increment it
            if(rs.next())
            {
                int count=rs.getInt(1);
                count++;
                update.setInt(1,count);
                update.setString(2, img1);
                update.setInt(3, count1);
                update.execute();
            }
            else
            {
                insert.setString(1, img1);
                insert.setInt(2, count1);
                insert.setInt(3, 1);
                insert.execute();
            }
            //now do the same for the 2nd blob in the matching pair
            select.setString(1, img2);
            select.setInt(2, count2);
            rs=select.executeQuery();
            //if there was a result, we already have a count running for this blob, just increment it
            if(rs.next())
            {
                int count=rs.getInt(1);
                count++;
                update.setInt(1,count);
                update.setString(2, img2);
                update.setInt(3, count2);
                update.execute();
            }
            else
            {
                insert.setString(1, img2);
                insert.setInt(2, count2);
                insert.setInt(3, 1);
                insert.execute();
            }
        }
        }

    }
    public overloadLoader(File dir, Hashtable <Integer,Integer> h,Hashtable <Integer,Integer> h2) throws FileNotFoundException, IOException, SQLException
    {

      BufferedReader r;
        File [] files=dir.listFiles();
        Connection j=DatabaseWrapper.getConnection();
        String insertQuery="insert into charactercount (img, `blob`, count, MS) values(?,?,?,415)";
        PreparedStatement insert=j.prepareStatement(insertQuery);
        String updateQuery="update charactercount set count=? where img=? and `blob`=?";
        PreparedStatement update=j.prepareStatement(updateQuery);
        for(int i=0;i<files.length;i++)
        {
        h=null;
        r= new BufferedReader(new FileReader(files[i]));
        System.out.print(files[i].getName()+"\n");
        String buff;
        String query="select * from charactercount where img=?";
        PreparedStatement select=j.prepareStatement(query);
        while(r.ready())
        {
            buff=r.readLine();
            String [] imageNames=buff.split(";");
            String img1=imageNames[0].split(":")[0];
            //blob id number, counts from 1-the number of blobs on the page
            int count1=Integer.parseInt(imageNames[0].split(":")[1]);
            String img2=imageNames[1].split(":")[0];
            int count2=Integer.parseInt(imageNames[1].split(":")[1]);
            if(img1.compareTo(img2)!=0)
            {
            if(h==null)
            {
                h=new Hashtable();
                select.setString(1, img1);
                ResultSet rs=select.executeQuery();
                while(rs.next())
                {
                    h.put(rs.getInt("blob"), rs.getInt("count"));

                }
                 h2=new Hashtable();
                select.setString(1, img2);
                rs=select.executeQuery();
                while(rs.next())
                {
                h2.put(rs.getInt("blob"), rs.getInt("count"));
                }
            }
            if(!h.containsKey(count1))
            {
            insert.setString(1, img1);
                insert.setInt(2, count1);
                insert.setInt(3, 0);
                insert.execute();
                h.put(count1, 0);
            }
            if(!h2.containsKey(count2))
            {
                insert.setString(1, img2);
                insert.setInt(2, count2);
                insert.setInt(3, 0);
                insert.execute();
                h2.put(count2, 0);
            }
            int current=h.get(count1);
            h.remove(count1);
            h.put(count1, current+1);
    
            current=h2.get(count2);
           
            h2.remove(count2);
            h2.put(count2, current+1);

            if(!r.ready())
            {
                 j.setAutoCommit(false);
                Enumeration ht=h.keys();
                while(ht.hasMoreElements())
                {
                    int blob=(Integer)ht.nextElement();
                    int count=h.get(blob);
                    update.setInt(1,count);
                    update.setString(2, img1);
                    update.setInt(3, blob);
                    update.addBatch();
                }
                Enumeration ht2=h2.keys();
                while(ht.hasMoreElements())
                {
                    int blob=(Integer)ht2.nextElement();
                    int count=h2.get(blob);
                    update.setInt(1,count);
                    update.setString(2, img2);
                    update.setInt(3, blob);
                    update.addBatch();
                }
                update.executeBatch();
                j.commit();
            j.setAutoCommit(true);
            }
        }
        }
        }
    }

    public overloadLoader(File dir, Hashtable <Integer,Integer> h,Hashtable <Integer,Integer> h2, Boolean drilldown) throws FileNotFoundException, IOException, SQLException
    {

      BufferedReader r;
        File [] files=dir.listFiles();
        Pattern colon=Pattern.compile(":");
        Pattern tab=Pattern.compile(";");
        Connection j=null;
        try{
            j=DatabaseWrapper.getConnection();
        String insertQuery="insert into charactercount (img, `blob`, count, MS) values(?,?,?,415)";
        PreparedStatement insert=j.prepareStatement(insertQuery);
        insert.execute("delete  from blobmatches");
        insert.execute("delete  from charactercount");
        String updateQuery="update charactercount set count=? where img=? and `blob`=?";
        PreparedStatement update=j.prepareStatement(updateQuery);
        String blobsInsert="insert into blobmatches (img1, blob1, img2, blob2) values(?,?,?,?)";
        PreparedStatement blobInsertStatement=j.prepareStatement(blobsInsert);
        FileWriter dataFile=new FileWriter(new File("/usr/ddata.dat"));
        for(int i=0;i<files.length;i++)
        {
        h=null;
        r= new BufferedReader(new FileReader(files[i]));
        System.out.print(files[i].getName()+"\n");
        String buff;
        String query="select * from charactercount where img=?";
        PreparedStatement select=j.prepareStatement(query);
        j.setAutoCommit(false);
        StringBuilder insertQ=new StringBuilder("insert into blobmatches (img1, blob1, img2, blob2) values ");
        int insertCtr=0;
        int ctr=0;
        int taskCount=3;
        ExecutorService executor = null;
        LinkedList<Future> set = new LinkedList<Future>();

    class prioritythread implements ThreadFactory{

            public Thread newThread(Runnable r)
            {
                Thread t=new Thread(r);
                t.setPriority(Thread.MIN_PRIORITY);
                return t ;

            }

    }
    executor = Executors.newFixedThreadPool(taskCount,new prioritythread());
        while(r.ready())
        {
            buff=r.readLine();
            String [] imageNames=tab.split(buff);
            String img1=colon.split(imageNames[0])[0];
            //blob id number, counts from 1-the number of blobs on the page
            try{
            int count1=Integer.parseInt(colon.split(imageNames[0])[1].split(";")[0]);
            String img2=colon.split(imageNames[1])[0];
            int count2=Integer.parseInt(colon.split(imageNames[1])[1].split(";")[0]);
            if(img1.compareTo(img2)!=0)
            {
            /*if(h==null)
            {
                h=new Hashtable();
                select.setString(1, img1);
                ResultSet rs=select.executeQuery();
                while(rs.next())
                {
                    h.put(rs.getInt("blob"), rs.getInt("count"));

                }
                 h2=new Hashtable();
                select.setString(1, img2);
                rs=select.executeQuery();
                while(rs.next())
                {
                h2.put(rs.getInt("blob"), rs.getInt("count"));
                }
            }
            if(!h.containsKey(count1))
            {
            insert.setString(1, img1);
                insert.setInt(2, count1);
                insert.setInt(3, 0);
                insert.execute();
                 j.commit();
                h.put(count1, 0);
            }
            if(!h2.containsKey(count2))
            {
                insert.setString(1, img2);
                insert.setInt(2, count2);
                insert.setInt(3, 0);
                insert.execute();
                 j.commit();
                h2.put(count2, 0);
            }
            int current=h.get(count1);
            h.remove(count1);
            h.put(count1, current+1);

            current=h2.get(count2);

            h2.remove(count2);
            h2.put(count2, current+1);*/

                    dataFile.append(img1+","+count1+","+img2+","+count2+"\n");

            /*blobInsertStatement.setString(1, img1.replace(".txt", ""));
            blobInsertStatement.setInt(2,count1);
            blobInsertStatement.setString(3, img2.replace(".txt", ""));
            blobInsertStatement.setInt(4,count2);
            blobInsertStatement.addBatch();
             * */
                //insertQ.append("('"+img1.replace(".txt", "")+"',"+count1+",'"+img2.replace(".txt", "")+"',"+count2+")");
            //ctr++;

            /*if(!r.ready() || ctr%10000==0)
            {
                insertQ.append(";");
                if(!r.ready())
                while(set.size()>0)
                    set.remove().get();
                while(set.size()>taskCount*2)
                    set.remove().get();
                set.add(executor.submit(new inserter(insertQ.toString())));
               // blobInsertStatement.execute(insertQ.toString());
                insertQ=new StringBuilder("insert into blobmatches (img1, blob1, img2, blob2) values  ");

            }
            else
            {
                insertQ.append(",");
            }*
                 /*
                Enumeration ht=h.keys();
                while(ht.hasMoreElements())
                {
                    int blob=(Integer)ht.nextElement();
                    int count=h.get(blob);
                    update.setInt(1,count);
                    update.setString(2, img1);
                    update.setInt(3, blob);
                    update.addBatch();
                }
                Enumeration ht2=h2.keys();
                while(ht.hasMoreElements())
                {
                    int blob=(Integer)ht2.nextElement();
                    int count=h2.get(blob);
                    update.setInt(1,count);
                    update.setString(2, img2);
                    update.setInt(3, blob);
                    update.addBatch();

                }
                update.executeBatch();
                  * */

/*                blobInsertStatement.executeBatch();
                j.commit();
          
            }*/
        }
            }
            catch(Exception e)
            {
                e.printStackTrace();
            }
        }
        }
                dataFile.close();
                //now run that


                try{
                
                j.createStatement().execute("LOAD DATA LOCAL INFILE '/usr/ddata.dat' into table blobmatches FIELDS TERMINATED BY ',';");
                }

                finally{

                    DatabaseWrapper.closeDBConnection(j);
                }
        }finally

    {
                    DatabaseWrapper.closeDBConnection(j);
                }

    }
    public static void main(String [] args) throws FileNotFoundException, IOException
    {
        Hashtable <Integer,Integer> h = null;
        Hashtable <Integer,Integer> h2 = null;
        try {
           overloadLoader n = new overloadLoader(new File("/usr/results/overload"), h, h2,true);
           //overloadLoader n = new overloadLoader(new File("E:\\project173"), h, h2,true);
            //
            setCharCounts();
           // setBlobs();
            //try {
            //
            /*try {
            overloadLoader n = new overloadLoader(new File("C:\\Documents and Settings\\jdeerin1\\My Documents\\overload"));
            } catch (FileNotFoundException ex) {
            Logger.getLogger(overloadLoader.class.getName()).log(Level.SEVERE, null, ex);
            } catch (IOException ex) {
            Logger.getLogger(overloadLoader.class.getName()).log(Level.SEVERE, null, ex);
            } catch (SQLException ex) {
            Logger.getLogger(overloadLoader.class.getName()).log(Level.SEVERE, null, ex);
            }
            } catch (FileNotFoundException ex) {
            Logger.getLogger(overloadLoader.class.getName()).log(Level.SEVERE, null, ex);
            } catch (IOException ex) {
            Logger.getLogger(overloadLoader.class.getName()).log(Level.SEVERE, null, ex);
            } catch (SQLException ex) {
            Logger.getLogger(overloadLoader.class.getName()).log(Level.SEVERE, null, ex);
            }*/
        } catch (SQLException ex) {
            Logger.getLogger(overloadLoader.class.getName()).log(Level.SEVERE, null, ex);
        }


    }
    


       class inserter implements Runnable{
           private String toRun;
    public inserter(String s)
    {
        toRun=s;
    }
        public void run() {
            Connection j=null;
            try
            {
                j=DatabaseWrapper.getConnection();
                try {
                    j.createStatement().execute(toRun);
                } catch (SQLException ex) {
                    Logger.getLogger(overloadLoader.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
            finally{
                DatabaseWrapper.closeDBConnection(j);

            }
            
        }

        }
    
}

