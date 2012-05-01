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

/**
 *
 * @author obi1one
 */
public class Annotation
{
   private String text;
   private int x;
   private int y;
   private int h;
   private int w;
   private int id;
   private int folio;
   private int projectID;


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
     * Create a new annotation
     * @param folio the folio the annotation is on
     * @param projectID the projectID for the project the annotation is part of
     * @param text text of the annotation
     * @param x
     * @param y
     * @param h
     * @param w
     * @throws SQLException
     */
    public Annotation(int folio, int projectID,String text, int x, int y, int h, int w) throws SQLException
   {
       String query="insert into annotation(folio,projectID,text,x,y,h,w) values(?,?,?,?,?,?,?)";
       Connection j=null;
PreparedStatement ps=null;
       try{
           j=DatabaseWrapper.getConnection();
           ps=j.prepareStatement(query, PreparedStatement.RETURN_GENERATED_KEYS);
           ps.setInt(1, folio);
           ps.setInt(2, projectID);
           ps.setString(3, text);
           ps.setInt(4, x);
           ps.setInt(5, y);
           ps.setInt(6, h);
           ps.setInt(7, w);
           ps.execute();
           ResultSet keys=ps.getGeneratedKeys();
           keys.next();
           this.id=keys.getInt(1);
           this.x=x;
           this.y=y;
           this.h=h;
           this.w=w;
           this.text=text;
           this.projectID=projectID;
           this.folio=folio;
       }
       finally{
DatabaseWrapper.closeDBConnection(j);
DatabaseWrapper.closePreparedStatement(ps);
       }
   }
   /**
    * Retrieve an existing annotation by unique id.
    * @param id unique id for the annotation
    * @throws SQLException
    */
   public Annotation(int id) throws SQLException
   {
       String query="select * from annotation where id=?";
       Connection j=null;
PreparedStatement ps=null;
       try{
           j=DatabaseWrapper.getConnection();
           ps=j.prepareStatement(query);
           ps.setInt(1, id);
           ResultSet rs=ps.executeQuery();
           if(rs.next())
           {
               this.x=rs.getInt("x");
               this.y=rs.getInt("y");
               this.h=rs.getInt("h");
               this.w=rs.getInt("w");
               this.folio=rs.getInt("folio");
               this.projectID=rs.getInt("projectID");
               this.id=id;
               this.text=rs.getString("text");
           }
       }
       finally
       {
DatabaseWrapper.closeDBConnection(j);
DatabaseWrapper.closePreparedStatement(ps);
       }
   }
   /**
    * Update the text of an annotation
    * @param text new text
    * @throws SQLException
    */
   public void updateAnnotationContent(String text) throws SQLException
   {
       createArchiveCopy();
          String query="update annotation set text=? where id=?";
       Connection j=null;
PreparedStatement ps=null;
       try{
           j=DatabaseWrapper.getConnection();
           ps=j.prepareStatement(query);
           ps.setString(1, text);
           ps.setInt(2, this.id);
           ps.execute();
           this.text=text;
       }
       finally{
DatabaseWrapper.closeDBConnection(j);
DatabaseWrapper.closePreparedStatement(ps);
       }
   }
   /**
    * Update the coordinates and dimensions of the annotation
    * @param x
    * @param y
    * @param h
    * @param w
    * @throws SQLException
    */
   public void updateAnnoationPosition(int x, int y, int h, int w) throws SQLException
   {
       createArchiveCopy();
              String query="update annotation set x=?,y=?,h=?,w=? where id=?";
       Connection j=null;
PreparedStatement ps=null;
       try{
           j=DatabaseWrapper.getConnection();
           ps=j.prepareStatement(query);
           ps.setInt(1, x);
           ps.setInt(2, y);
           ps.setInt(3, h);
           ps.setInt(4, w);
           ps.setInt(5, this.id);
           ps.execute();
           this.x=x;
           this.y=y;
           this.h=h;
           this.w=w;
       }
       finally{
DatabaseWrapper.closeDBConnection(j);
DatabaseWrapper.closePreparedStatement(ps);
       }
   }
   /**
    * Update the folio the annotation is on
    * @param folio
    * @throws SQLException
    */
   public void updateAnnotationFolio(int folio) throws SQLException
   {
       createArchiveCopy();
       String query="update annotation set folio=? where id=?";
       Connection j=null; 
PreparedStatement ps=null;
       
       try{ 
           j=DatabaseWrapper.getConnection();
           ps=j.prepareStatement(query);
           ps.setInt(1, folio);
           ps.setInt(2, this.id);
           ps.execute();
           this.folio=folio;
       }
       finally{
DatabaseWrapper.closeDBConnection(j);
DatabaseWrapper.closePreparedStatement(ps);
       }
   }
   /**
    * Delete the annotation entirely
    * @throws SQLException
    */
   public void delete() throws SQLException
   {
       String query="delete from annotation where id=?";
       PreparedStatement ps=null;
       Connection j=null;
       try{
           j=DatabaseWrapper.getConnection();
           ps=j.prepareStatement(query);
           ps.setInt(1, this.id);
           ps.execute();
       }
       finally
       {
           DatabaseWrapper.closeDBConnection(j);
           DatabaseWrapper.closePreparedStatement(ps);
       }
   }
   /**Duplicate this annotation, creating something with the same x,y,h,w, folio, projectID and text, but a new id.
    * @return
    * @throws SQLException
    */
   public Annotation duplicate() throws SQLException
   {
       return new Annotation(this.folio,this.projectID,this.text,this.x,this.y,this.h,this.w);
   }
   private void createArchiveCopy() throws SQLException
   {
       String query="insert into archivedAnnotation(folio,projectID,text,x,y,h,w,id) (select folio,projectID,text,x,y,h,w,id from annotation where id=?)";
       Connection j=null;
PreparedStatement ps=null;
       try{
           j=DatabaseWrapper.getConnection();
           ps=j.prepareStatement(query);
           ps.setInt(1,this.id);
           ps.execute();
       }
       finally
       {
DatabaseWrapper.closeDBConnection(j);
DatabaseWrapper.closePreparedStatement(ps);
       }
       
   }
   /**
    * Get all annotations on a particular folio that are part of a particular project
    * @param projectID
    * @param folio
    * @return
    * @throws SQLException
    */
   public static Annotation[] getAnnotationSet(int projectID,int folio) throws SQLException
{
    Annotation [] toret=null;
    Stack <Annotation> tmp =new Stack();
    String query="select id from annotation where projectID=? and folio=?";
    Connection j=null;
PreparedStatement ps=null;
    try{
        j=DatabaseWrapper.getConnection();
        ps=j.prepareStatement(query);
        ps.setInt(1, projectID);
        ps.setInt(2, folio);
        
        ResultSet rs=ps.executeQuery();
        while(rs.next())
        {
            Annotation a=new Annotation(rs.getInt("id"));
            tmp.push(a);
        }
        toret=new Annotation[tmp.size()];
        for(int i=0;i<toret.length;i++)
            toret[i]=tmp.pop();
    }
    finally{
DatabaseWrapper.closeDBConnection(j);
DatabaseWrapper.closePreparedStatement(ps);
    }
    return toret;
}
}
