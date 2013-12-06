/*
 * @author Jon Deering
Copyright 2011 Saint Louis University. Licensed under the Educational Community License, Version 2.0 (the "License"); you may not use
this file except in compliance with the License.

You may obtain a copy of the License at http://www.osedu.org/licenses/ECL-2.0

Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions
and limitations under the License.
 */

package dmstech;

import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Stack;

/**
 * TODO:  Complete.
 */
public class manifestAggregation {
    
    /**
     * TODO:  Complete.
     * 
     * @param url
     * @return String[]
     * @throws MalformedURLException
     * @throws IOException 
     */
    public static String [] getManifestUrls(String url) throws MalformedURLException, IOException
    {
        String[] toret=null;
        Stack<String>urls=new Stack();
        String format="";
        URL m3Url=new URL(url);
        HttpURLConnection m3connection = null;
        
        // set up our connection for downloading
        m3connection = (HttpURLConnection) m3Url.openConnection();
        m3connection.setRequestMethod("GET");
        m3connection.setRequestProperty("accept", "application/xml");
        m3connection.setDoOutput(true);
        m3connection.setReadTimeout(10000);
        m3connection.connect();
        
        // determine our format
        if (url.toLowerCase().endsWith("n3")) {
            format = "N3";
        } else {
            format = "";
        }
        
        // open our input stream
        BufferedReader m3Reader = null;
        m3Reader = new BufferedReader(new InputStreamReader(m3connection.getInputStream()));
        
        // create our model (BOZO:  What does this actually do?)
        Model m3Model = ModelFactory.createDefaultModel();
        m3Model.read(m3Reader, null, format);
        
        // get all aggregations as a {subject}-{obj} pair
        String queryString="prefix ore:<http://www.openarchives.org/ore/terms/> select * where{   ?subject ore:describes ?obj  }";
        Query query = QueryFactory.create(queryString);
        QueryExecution qe = QueryExecutionFactory.create(query, m3Model);
        ResultSet results = qe.execSelect();
        while (results.hasNext()) {
            QuerySolution qs = results.next();
            queryString="prefix ore:<http://www.openarchives.org/ore/terms/> select * where{ ?sub ore:describes <"+qs.get("obj")+"> }";
            query = QueryFactory.create(queryString);
            qe = QueryExecutionFactory.create(query, m3Model);
            ResultSet results2 = qe.execSelect();
            while(results2.hasNext())
            {
                qs = results2.next();
                urls.push(qs.get("sub").toString());
            }
        }
        
        // convert our Stack<> to String[] and return our array of manifest URLs
        toret=new String [urls.size()];
        for(int i=0;i<toret.length;i++)
        {
            toret[i]=urls.pop();
        }
        return toret;
    }
}
