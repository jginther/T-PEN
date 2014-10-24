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
import java.util.Stack;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.owasp.esapi.ESAPI;
/**Handles archiving and retrieving old versions of an annotation*/
public class ArchivedAnnotation {
    private String text;
   private int x;
   private int y;
   private int h;
   private int w;
   private int id;
   private int folio;
   private int projectID;
   private int archivedID;

   /**
    * This gets the ID for this particular version of the annotation. Dont confuse with annotationID. Several ArchivedAnnotations can share the same annotationID but not archivedID
    * @return the archidedID
    */
   public int getArchivedID() {
        return archivedID;
    }

    public int getFolio() {
        return folio;
    }

    public int getH() {
        return h;
    }

    public int getId() {
        return id;
    }

    public int getProjectID() {
        return projectID;
    }

    public String getText() {
        return ESAPI.encoder().encodeForHTML(text);
    }

    public int getW() {
        return w;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }
    /**
     *
     * @param archivedID
     * @throws SQLException
     */
    public ArchivedAnnotation(int archivedID) throws SQLException
    {
        String query="select * from archivedAnnotation where achrivedID=?";
        Connection j=null;
PreparedStatement ps=null;
        try{
            j=DatabaseWrapper.getConnection();
            ps=j.prepareStatement(query);
            ps.setInt(1, archivedID);
            ResultSet rs=ps.executeQuery();
            if(rs.next())
            {
              x=rs.getInt("x");
              y=rs.getInt("y");
              h=rs.getInt("h");
              w=rs.getInt("w");
              folio=rs.getInt("folio");
              projectID=rs.getInt("projectID");
              id=rs.getInt("id");
              text=rs.getString("text");
              this.archivedID=archivedID;
            }
        }
        finally{
DatabaseWrapper.closeDBConnection(j);
DatabaseWrapper.closePreparedStatement(ps);
        }
    }
    /**
     * Get ArchivedAnnotation objects for every archived version of the annotation
     * @param annotationID annotation id for the annotation, not to be confused with archivedid
     * @return array of ArchivedAnnotations in the order they were archived
     * @throws SQLException
     */
    public static ArchivedAnnotation[] getAllArchivedVersions(int annotationID) throws SQLException
    {
        ArchivedAnnotation [] toret=null;
        String query="select archivedID from archivedAnnotation where id=? order by archivedID";
        Connection j=null;
PreparedStatement ps=null;
        Stack<ArchivedAnnotation> tmp=new Stack();
        try{
            j=DatabaseWrapper.getConnection();
            ps=j.prepareStatement(query);
            ps.setInt(1, annotationID);
            ResultSet rs=ps.executeQuery();
            while(rs.next())
            {
                ArchivedAnnotation a=new ArchivedAnnotation(rs.getInt("archivedID"));
                tmp.push(a);
            }
            toret=new ArchivedAnnotation[tmp.size()];
            for(int i=0;i<toret.length;i++)
            {
                toret[i]=tmp.pop();
            }
        }
        finally
        {
DatabaseWrapper.closeDBConnection(j);
DatabaseWrapper.closePreparedStatement(ps);
        }
        return toret;
        
    }
    
}
