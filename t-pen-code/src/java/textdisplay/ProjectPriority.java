/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package textdisplay;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Stack;
import java.util.logging.Level;
import java.util.logging.Logger;
/**
 *
 * @author obi1one
 */
public class ProjectPriority {
    private int uid;
public ProjectPriority(int uid)
    {
this.uid=uid;

    }
    public int [][] getOrderedProjects() throws SQLException
    {int [][] toret=new int [0][0];
     String query="select * from projectPriorities where uid=? order by priority desc";
     Stack<Integer> projectIDs=new Stack();
     Stack<Integer> priorities=new Stack();
     Connection j = null;
PreparedStatement ps=null;
        try
            {
            j = DatabaseWrapper.getConnection();
            ps = j.prepareStatement(query);
            ps.setInt(1, uid);
            ResultSet rs=ps.executeQuery();
            while(rs.next())
            {
            projectIDs.push(rs.getInt("projectID"));
            priorities.push(rs.getInt("priority"));
            }

            }
        finally{
            DatabaseWrapper.closeDBConnection(j);
            DatabaseWrapper.closePreparedStatement(ps);
        }

        //because the results were stored in a stack, they will come out backward, so reverse them using another stack
        Stack<Integer> reverse=new Stack();
        Stack<Integer> reversePriorities=new Stack();
        while(!projectIDs.empty())
        {
            reverse.push(projectIDs.pop());
            reversePriorities.push((priorities.pop()));
        }
        toret=new int[2][reverse.size()];
        int i=0;
        while(!reverse.empty())
        {
            toret[0][i]=reverse.pop();
            toret[1][i]=reversePriorities.pop();
            i++;
        }
        return toret;
    }
    public Boolean setPriority(int projectID,int priority) throws SQLException
    {
        String checkQuery="select * from projectPriorities where projectID=? and uid=?";
        Connection j = null;
PreparedStatement ps=null;
        try
            {
            j = DatabaseWrapper.getConnection();
            ps = j.prepareStatement(checkQuery);
            ps.setInt(1, projectID);
            ps.setInt(2, uid);
            ResultSet rs=ps.executeQuery();
            if(rs.next())
            {

                String query="update projectPriorities set priority=? where projectID=? and uid=?";
                ps=j.prepareStatement(query);
                ps.setInt(1, priority);
                ps.setInt(2, projectID);
                ps.setInt(3, uid);
                ps.execute();
                return true;
            }
 else
            {
                return false;
 }
            }
        finally
        {
            DatabaseWrapper.closeDBConnection(j);
            DatabaseWrapper.closePreparedStatement(ps);
        }
    }
    public void verifyPriorityContents() throws SQLException
    {
        String projectSelector="select * from project join groupMembers on project.grp=groupMembers.GID where groupMembers.UID=?";
        String insertQuery="insert into projectPriorities(uid,projectID, priority) values(?,?,?)";
        String checkQuery="select * from projectPriorities where uid=? and projectID=?";

        Connection j = null;
PreparedStatement ps=null;
PreparedStatement inserter=null;
PreparedStatement checker=null;
        try
            {
            j = DatabaseWrapper.getConnection();
            inserter=j.prepareStatement(insertQuery);
                ps = j.prepareStatement(projectSelector);
                checker=j.prepareStatement(checkQuery);
                ps.setInt(1, uid);
            ResultSet rs=ps.executeQuery();
            while(rs.next())
            {
                checker.setInt(1, uid);
                checker.setInt(2, rs.getInt("id"));
                ResultSet checkSet=checker.executeQuery();
                if(!checkSet.next())
                {
                    inserter.setInt(1, uid);
                    inserter.setInt(2, rs.getInt("id"));
                    inserter.setInt(3, 0);
                    inserter.execute();

                }

                }
                
            

            }
            finally
        {
                DatabaseWrapper.closePreparedStatement(checker);
                DatabaseWrapper.closePreparedStatement(inserter);
                DatabaseWrapper.closeDBConnection(j);
            DatabaseWrapper.closePreparedStatement(ps);
            }


    }
    public static void main(String [] args)
    {
        ProjectPriority p=new ProjectPriority(1);
        try {
            //p.verifyPriorityContents();
            //p.setPriority(24, 1);
            int [][] ret=p.getOrderedProjects();
            for(int i=0;i<ret[0].length;i++)
            {
                System.out.print(""+ret[0][i]+":"+ret[1][i]+"\n");
            }
        } catch (SQLException ex) {
            Logger.getLogger(ProjectPriority.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
