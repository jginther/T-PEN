/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package OAC;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.ResIterator;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Selector;
import com.hp.hpl.jena.rdf.model.SimpleSelector;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.vocabulary.RDF;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author jdeerin1
 */
public class sequence
{
    private String [] sequenceItems;
    public String getSequenceElement(int position)
    {
        if(sequenceItems.length>position)
            return sequenceItems[position-1];
        return "";
    }
    /**build a sequence object given the url of the graph serialization and its format*/
    public sequence(URL []sequenceUrl, String format) throws IOException
    {
        Model sequenceModel = ModelFactory.createDefaultModel();


 // use the FileManager to find the input file
    InputStreamReader in=null;
    int positionCounter=1;
    for(int i=0;i<sequenceUrl.length;i++)
    {
        HttpURLConnection connection = null;
        connection = (HttpURLConnection)sequenceUrl[i].openConnection();
          connection.setRequestMethod("GET");
          connection.setRequestProperty("accept", "application/n3");
          connection.setDoOutput(true);
          connection.setReadTimeout(10000);

          connection.connect();

          //get the output stream writer and write the output to the server
          //not needed in this example
          //wr = new OutputStreamWriter(connection.getOutputStream());
          //wr.write("");
          //wr.flush();

          //read the result from the server
           BufferedReader rd  = null;
          rd  = new BufferedReader(new InputStreamReader(connection.getInputStream()));
          
//    in= new InputStreamReader(sequenceUrl[i].openStream());
          //while(rd.ready())
          //    System.out.print(rd.readLine()+"\n");
         // if(true)return;
          sequenceModel.read(rd,null, format);
          //sequenceModel.write(System.out, "N3");
    }
    Property seqProperty=sequenceModel.getProperty("http://www.openarchives.org/ore/terms/aggregates");
        ResIterator sequences=sequenceModel.listResourcesWithProperty(seqProperty);//.listSubjects();
        Resource listItem =null;
        String queryString="prefix dms:<http://dms.stanford.edu/ns/> select ?subject ?predicate WHERE{?subject ?predicate dms:Sequence}";

        Query query = QueryFactory.create(queryString);
        QueryExecution qe = QueryExecutionFactory.create(query, sequenceModel);
        ResultSet results = qe.execSelect();
        String m3UrlString="";
        while(results.hasNext())
        {
            System.out.print("found 1 \n");
            QuerySolution qs=results.next();
            m3UrlString=qs.get("subject").toString();
            //just for testing the query
            //System.out.print(qs.get("subject")+"\n");
            //System.out.print(qs.get("predicate")+"\n");
        }
        if(m3UrlString.compareTo("")!=0)
        {
            URL m3Url=new URL(m3UrlString+".xml");
            HttpURLConnection m3connection = null;
        m3connection = (HttpURLConnection)m3Url.openConnection();
          m3connection.setRequestMethod("GET");
          m3connection.setRequestProperty("accept", "application/n3");
          m3connection.setDoOutput(true);
          m3connection.setReadTimeout(10000);
          m3connection.connect();
          Model m3Model = ModelFactory.createDefaultModel();
          BufferedReader m3Reader  = null;
          m3Reader  = new BufferedReader(new InputStreamReader(m3connection.getInputStream()));
          m3Model.read(m3Reader, null, format);
          //m3Model.write(System.out, "N3");
           
          queryString="prefix rdf:<http://www.w3.org/1999/02/22-rdf-syntax-ns#> PREFIX list: <http://jena.hpl.hp.com/ARQ/list#> select * where{   ?subject list:member ?predicate}"; //<http://www.w3.org/1999/02/22-rdf-syntax-ns#first>  ?predicate}";
          query = QueryFactory.create(queryString);
          
        query = QueryFactory.create(queryString);
          qe = QueryExecutionFactory.create(query, m3Model);
         results = qe.execSelect();
        System.out.print("Query results:\n");
        while(results.hasNext())
        {
            QuerySolution qs=results.next();
            
            //just for testing the query
            //System.out.print("sub:"+qs.get("subject").toString()+"\n");
            System.out.print("Position:"+positionCounter+"\n"); 
            System.out.print("Canvas:"+qs.get("predicate").toString()+"\n");
            Resource r=qs.get("predicate").asResource();
            //System.out.print(" stats:"+r.getId()+" :"+r.isAnon()+", "+r.isLiteral()+", "+r.isResource()+", "+r.isURIResource()+"\n");

            //System.out.print(qs.get("predicate")+"\n");
            
            Query innerQuery=QueryFactory.create("select  ?pred where { <"+r.getURI()+"> "+"<http://purl.org/dc/elements/1.1/title> ?pred}");
            qe = QueryExecutionFactory.create(innerQuery, m3Model);
            ResultSet innerResults=qe.execSelect();
            while(innerResults.hasNext())

            {
                QuerySolution qs2=innerResults.next();
                System.out.print("Title:"+qs2.get("pred")+"\n");
                
                    Query innerQuery3=QueryFactory.create("select ?pred  where { ?pred <http://www.openannotation.org/ns/hasTarget> <"+r.getURI()+"> }");
                  qe = QueryExecutionFactory.create(innerQuery3, sequenceModel);
                ResultSet innerResults2=qe.execSelect();
                while(innerResults2.hasNext())
                {
                    QuerySolution innerqs2=innerResults2.next();
                    //System.out.print("1:"+innerqs2.get("pred")+" "+"\n");
                    innerqs2.getResource("pred");
                    
                    //now get the image
                    Query innerQuery4=QueryFactory.create("select ?pred  where { <"+innerqs2.getResource("pred").getURI()+"> <http://www.openannotation.org/ns/hasBody> ?pred }");
                  qe = QueryExecutionFactory.create(innerQuery4, sequenceModel);
                ResultSet innerResults4=qe.execSelect();
                if(innerResults4.hasNext())
                {
                    QuerySolution innerqs4=innerResults4.next();
                    System.out.print("Image:"+innerqs4.get("pred")+" "+"\n\n");
                    positionCounter++;
                }
                }   
                
                
                
                
                
             
            }
            
        }


        }
        if(true)return;

        //listItem =sequenceModel.getResource("http://dmstech.groups.stanford.edu/Machaut/ffr22546/manifest/NormalSequence");

        while(sequences.hasNext())
        {
            listItem=sequences.next();
            if(listItem.getNameSpace()!=null)
            {
            //System.out.print(listItem.getNameSpace()+"\n");

            if(listItem.getNameSpace().compareTo("dms")==0)
            {
                System.out.print("listitem:"+listItem.getURI()+"\n");
                break;
            }
            }
        }
 for (; !listItem.equals(RDF.nil);)
 {
 // If the list is of resources ...
 Resource entry = listItem.getProperty(RDF.first).getResource();
System.out.print(entry.getURI()+"\n");

view v=new view(sequenceModel,entry.getURI());
//System.out.print(v.getImages().length+"\n");



 // Move on to next list item
 listItem = listItem.getProperty(RDF.rest).getResource();
 }
    }
    /**Retrieve all views as an in order array*/
    public view [] getViews()
    {
        return null;
    }
    public static void main(String [] args)
    {
        try
            {
             String w = "http://67.23.4.192/NormalSequence.xml";
             URL [] urls=new URL[2];
             urls[0]=new URL("http://dms-data.stanford.edu/Oxford/Bodley342/Manifest.xml");//"http://67.23.4.192/NormalSequence.xml");//"http://rosetest.library.jhu.edu/m3/Francais12594");
             urls[1]=new URL("http://dms-data.stanford.edu/Oxford/Bodley342/ImageAnnotations.xml");
             //URL [] urls=new URL[3];
            //urls[0]=new URL("http://67.23.4.192/IA.n3");
             //urls[1]=new URL("http://67.23.4.192/normalSequence.n3");
             
             //urls[2]=new URL("http://67.23.4.192/ImageCollection.n3");

            new sequence(urls, "");

            } catch (IOException ex)
            {
            Logger.getLogger(sequence.class.getName()).log(Level.SEVERE, null, ex);
            }
    }
}
