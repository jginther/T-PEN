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
import com.hp.hpl.jena.rdf.model.RDFList;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.ResIterator;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Selector;
import com.hp.hpl.jena.rdf.model.SimpleSelector;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.vocabulary.RDF;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.sql.SQLException;
import java.util.Stack;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * A sequence of canvases which represent a manuscript.
 */
public class sequence {
    
    private canvas[] sequenceItems;
    
    public String getCity() {
        return city;
    }
    
    public String getCollection() {
        return collection;
    }
    
    public String getRepository() {
        return repository;
    }
    
    private String city;
    private String collection;
    private String repository;
    
    
    public canvas[] getSequenceItems() {
        return sequenceItems;
    }
    
    
    public canvas getSequenceElement(int position) {
        if (sequenceItems.length > position) {
            return sequenceItems[position - 1];
        }
        return null;
    }
    
    
    /**
     * build a sequence object given the url of the graph serialization and its format
     * 
     * @param sequenceUrl
     * @param format
     * @throws IOException
     * @throws SQLException 
     */
    public sequence(URL[] sequenceUrl, String format) throws IOException, SQLException {
        city = "";
        collection = "";
        repository = "";
        
        // variable to hold values temporarily
        String tmpVal = "";
        
        Model sequenceModel = ModelFactory.createDefaultModel();
        Stack<canvas> accumulator = new Stack();
        int positionCounter = 1;
        //Read all of the urls that were given into a single graph. Usually we just get the manifest,
        //but the imageannotations can also be included
        for (int i = 0; i < sequenceUrl.length; i++) {
            HttpURLConnection connection = null;
            connection = (HttpURLConnection) sequenceUrl[i].openConnection();
            connection.setRequestMethod("GET");
            //this header is for Hopkins, they server based on requested type
            //connection.setRequestProperty("accept", "application/xml");
            connection.setDoOutput(true);
            connection.setReadTimeout(10000);
            connection.connect();
            BufferedReader rd = null;
            rd = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            sequenceModel.read(rd, null, format);
        }
        //sequenceModel.write(System.out); 
        //this query finds sequence
        String queryString = "prefix dms:<http://dms.stanford.edu/ns/> prefix sc:<http://www.shared-canvas.org/ns/> select ?subject ?predicate WHERE{{?subject ?predicate dms:Sequence} union {?subject ?predicate sc:Sequence} }";
        
        //Find the image annotation aggregation uri
        String queryString2 = "prefix dms:<http://dms.stanford.edu/ns/> prefix sc:<http://www.shared-canvas.org/ns/> select ?subject ?predicate WHERE{{?subject ?predicate dms:ImageAnnotationList}union{?subject ?predicate sc:ImageAnnotationList}}";
        //Find the tei metadata for the manuscript. Items are settlement, repository, and collection+idno (condensed into collection for our purposes)
        String queryString3 = "prefix tei:<http://www.tei-c.org/ns/1.0/> select ?sub ?object WHERE{ ?sub tei:settlement ?object }";
        String queryString4 = "prefix tei:<http://www.tei-c.org/ns/1.0/> select ?sub ?object WHERE{ ?sub tei:collection ?object }";
        String queryString5 = "prefix tei:<http://www.tei-c.org/ns/1.0/> select ?sub ?object WHERE{ ?sub tei:idno ?object }";
        String queryString6 = "prefix tei:<http://www.tei-c.org/ns/1.0/> select ?sub ?object WHERE{ ?sub tei:repository ?object }";
        String queryString7 = "prefix tei:<http://www.tei-c.org/ns/1.0/> select ?sub ?object WHERE{ ?sub tei:institution ?object }";
        
        Query query = QueryFactory.create(queryString);
        QueryExecution qe = QueryExecutionFactory.create(query, sequenceModel);
        ResultSet results = qe.execSelect();
        String m3UrlString = "";
        
        //find the uri for the normal sequence
        while (results.hasNext()) {
            QuerySolution qs = results.next();
            m3UrlString = qs.get("subject").toString();
            String resourceQueryString = "prefix ore:<http://www.openarchives.org/ore/terms/> select ?object  WHERE{ <" + m3UrlString + "> ore:isDescribedBy ?object}";
            query = QueryFactory.create(resourceQueryString);
            qe = QueryExecutionFactory.create(query, sequenceModel);
            results = qe.execSelect();
            //now find where the normal sequence actually resides
            if (results.hasNext()) {
                qs = results.next();
                m3UrlString = qs.get("object").toString();
//                System.out.print("\t\tNormal sequence:  found at URL="+qs.get("object").toString()+"\n");
            }
            else {
                System.out.print("\t\tNormal sequence:  not found!");
                return;
            }
        }
        
        query = QueryFactory.create(queryString2);
        qe = QueryExecutionFactory.create(query, sequenceModel);
        results = qe.execSelect();
        String ImgAnnoUrlString = "";
        if (results.hasNext()) {
            QuerySolution qs = results.next();
            //this is the uri of the image annotation list
            ImgAnnoUrlString = qs.get("subject").toString();
            //now find the associated resource
            String resourceQueryString = "prefix ore:<http://www.openarchives.org/ore/terms/> select ?object  WHERE{ <" + ImgAnnoUrlString + "> ore:isDescribedBy ?object}";
            query = QueryFactory.create(resourceQueryString);
            qe = QueryExecutionFactory.create(query, sequenceModel);
            results = qe.execSelect();
            //now find where the image-annotation list actually resides
            if (results.hasNext()) {
                qs = results.next();
                ImgAnnoUrlString = qs.get("object").toString();
                System.out.print("\t\tImage-annotation list:  found at URL="+ImgAnnoUrlString+"\n");
            }
            else {
                System.out.print("\t\tImage-annotation list:  not found!");
            }
            
        }
        
        // get city (from <tei:settlement>)
        query = QueryFactory.create(queryString3);
        qe = QueryExecutionFactory.create(query, sequenceModel);
        results = qe.execSelect();
        if (results.hasNext()) {
            QuerySolution qs = results.next();
            city = qs.get("object").toString();
        }
        
        // get collection (from <tei:collection>)
        query = QueryFactory.create(queryString4);
        qe = QueryExecutionFactory.create(query, sequenceModel);
        results = qe.execSelect();
//        tmpVal = "";
        if (results.hasNext()) {
            QuerySolution qs = results.next();
            collection += qs.get("object").toString();
        }
        
        // get collection, part B (from <tei:idno>)
        query = QueryFactory.create(queryString5);
        qe = QueryExecutionFactory.create(query, sequenceModel);
        results = qe.execSelect();
        tmpVal = "";
        if (results.hasNext()) {
            QuerySolution qs = results.next();
            tmpVal = qs.get("object").toString();
            if (0 < tmpVal.length()) {
                collection += ((0<collection.length())?" ":"") + tmpVal;
            }
        }
        
//        System.out.println("FOUND COLLECTION_TOTAL<String*"+collection.length()+">=\"" + collection + "\"!");
        
        // get repository (from <tei:repository>)
        query = QueryFactory.create(queryString6);
        qe = QueryExecutionFactory.create(query, sequenceModel);
        results = qe.execSelect();
        if (results.hasNext()) {
            QuerySolution qs = results.next();
            repository = qs.get("object").toString();
        }else{
            query = QueryFactory.create(queryString7);
            qe = QueryExecutionFactory.create(query, sequenceModel);
            results = qe.execSelect();
            if(results.hasNext()){
                QuerySolution qs = results.next();
                repository = qs.get("object").toString();
            }
        }
        
        //
        //if a location for the image annotations and the sequence was found, load them into a separate graph
        //
        if (m3UrlString.compareTo("") != 0 && ImgAnnoUrlString.compareTo("") != 0) {
            URL m3Url = new URL(m3UrlString);
            URL imgAnnoURL = new URL(ImgAnnoUrlString);
            HttpURLConnection m3connection = null;
            m3connection = (HttpURLConnection) m3Url.openConnection();
            m3connection.setRequestMethod("GET");
           // m3connection.setRequestProperty("accept", "application/xml");
            m3connection.setDoOutput(true);
            m3connection.setReadTimeout(10000);
            m3connection.connect();
            if (m3UrlString.toLowerCase().endsWith("n3")) {
                format = "N3";
            } else {
                format = "";
            }
            
            Model m3Model = ModelFactory.createDefaultModel();
            BufferedReader m3Reader = null;
            m3Reader = new BufferedReader(new InputStreamReader(m3connection.getInputStream()));
            m3Model.read(m3Reader, null, format);
            m3connection = (HttpURLConnection) imgAnnoURL.openConnection();
            m3connection.setRequestMethod("GET");
          //  m3connection.setRequestProperty("accept", "application/xml");
            m3connection.setDoOutput(true);
            m3connection.setReadTimeout(10000);
            m3connection.connect();
            m3Reader = new BufferedReader(new InputStreamReader(m3connection.getInputStream()));
            
            sequenceModel = ModelFactory.createDefaultModel();
            if (ImgAnnoUrlString.toLowerCase().endsWith("n3")) {
                format = "N3";
            } else {
                format = "";
            }
            //while(m3Reader.ready())
            //    System.out.print(m3Reader.readLine());
            //m3Reader.reset();
            sequenceModel.read(m3Reader, null, format);
            
            //find the sequence list (which in jena is a jena list) to get the canvases in order. 
            queryString = "prefix rdf:<http://www.w3.org/1999/02/22-rdf-syntax-ns#> PREFIX list: <http://jena.hpl.hp.com/ARQ/list#> select * where{   ?subject list:member ?obj}"; //<http://www.w3.org/1999/02/22-rdf-syntax-ns#first>  ?predicate}";
            query = QueryFactory.create(queryString);
            query = QueryFactory.create(queryString);
            qe = QueryExecutionFactory.create(query, m3Model);
            results = qe.execSelect();
            while (results.hasNext()) {
                QuerySolution qs = results.next();
                //canvas uri
                String canvas = qs.get("obj").toString();
                Resource r = qs.get("obj").asResource();
                
//                System.out.print("\t\t\tCanvas:  found at URI="+canvas+"\n");
//                System.out.print("\t\t\t\tCanvas:  resource URI="+r.getURI()+"\n");
                
                //fetch the canvas title
                Query innerQuery = QueryFactory.create("select  ?pred where { <" + r.getURI() + "> " + "<http://purl.org/dc/elements/1.1/title> ?pred}");
                qe = QueryExecutionFactory.create(innerQuery, m3Model);
                ResultSet innerResults = qe.execSelect();
                String title="";
                while (innerResults.hasNext()) {
                    QuerySolution qs2 = innerResults.next();
                    //canvas title
                    title = qs2.get("pred").toString();
                }
                
                //
                // fetch canvas dimensions
                //
                String canvasWidth="", canvasHeight="";
                int cwidth=0, cheight=0;
                innerQuery = QueryFactory.create("prefix exif:<http://www.w3.org/2003/12/exif/ns#> select  ?pred  where {  <" + r.getURI() + "> " + "exif:width ?pred}");
                qe = QueryExecutionFactory.create(innerQuery, m3Model);
                innerResults = qe.execSelect();
                while (innerResults.hasNext()) {
                    QuerySolution qs2 = innerResults.next();
                    canvasWidth = qs2.get("pred").toString().split("\\^")[0];
                }
                innerQuery = QueryFactory.create("prefix exif:<http://www.w3.org/2003/12/exif/ns#> select  ?pred where { <" + r.getURI() + "> " + "exif:height ?pred}");
                qe = QueryExecutionFactory.create(innerQuery, m3Model);
                innerResults = qe.execSelect();
                while (innerResults.hasNext()) {
                    QuerySolution qs2 = innerResults.next();
                    canvasHeight = qs2.get("pred").toString().split("\\^")[0];
                }
                // now, parse the strings into actual int's
                if(canvasWidth.compareTo("")!=0) {
                    try {
                        cwidth=Integer.parseInt(canvasWidth);
                    }
                    catch(NumberFormatException e) {
                        //just leave it as 0
                    }
                }
                if(canvasHeight.compareTo("")!=0) {
                    try {
                        cheight=Integer.parseInt(canvasHeight);
                    }
                    catch(NumberFormatException e) {
                        //just leave it as 0
                    }
                }
                
//                System.out.print("\t\t\t\tCanvas:  title="+title+"\n");
//                System.out.print("\t\t\t\tCanvas:  width="+canvasWidth+" (as int: ="+cwidth+")\n");
//                System.out.print("\t\t\t\tCanvas:  height="+canvasHeight+" (as int: ="+cheight+")\n");
                
                //find anything that has the canvas as a target. Should find an image annotation.
                Query innerQuery3 = QueryFactory.create("select ?sub  where { ?sub <http://www.openannotation.org/ns/hasTarget> <" + r.getURI() + "> }");
                qe = QueryExecutionFactory.create(innerQuery3, sequenceModel);
                ResultSet innerResults2 = qe.execSelect();
                while (innerResults2.hasNext()) {
                    QuerySolution innerqs2 = innerResults2.next();
                    innerqs2.getResource("sub");
                    
//                    System.out.print("\t\t\t\tFound sub that targets canvas at URI="+innerqs2.getResource("sub").getURI()+"\n");
                    
                    Query innerQuery4 = QueryFactory.create("select ?pred  where { <" + innerqs2.getResource("sub").getURI() + "> <http://www.openannotation.org/ns/hasBody> ?pred }");
                    qe = QueryExecutionFactory.create(innerQuery4, sequenceModel);
                    ResultSet innerResults4 = qe.execSelect();
                    //the body of the image annotation can be an image or an imagechoice
                    if (innerResults4.hasNext()) {
                        QuerySolution innerqs4 = innerResults4.next();
                        String img = innerqs4.getResource("pred").getURI();
                        
//                        System.out.print("\t\t\t\t\tFound ?image annotation?:  at URL="+img+"\n");
                        
                        //check to see if this is an imagechoice or an imagebody
                        innerQuery4 = QueryFactory.create(" prefix dms:<http://dms.stanford.edu/ns/> prefix sc:<http://www.shared-canvas.org/ns/>  select ?pred   where {{ <" + img + "> ?pred dms:ImageBody } union{<" + img + "> ?pred sc:ImageBody }}");
                        qe = QueryExecutionFactory.create(innerQuery4, sequenceModel);
                        innerResults4 = qe.execSelect();
                        
                        // image body
                        if (innerResults4.hasNext()) {
//                            System.out.print("\t\t\t\t\t\tfound imagebody\n");
                            
                            innerqs4 = innerResults4.next();
                            
                            //
                            // get image dimensions
                            //
                            innerQuery = QueryFactory.create("prefix exif:<http://www.w3.org/2003/12/exif/ns#> select  ?obj where { <" + img + "> " + " exif:height ?obj  }");
                            qe = QueryExecutionFactory.create(innerQuery, sequenceModel);
                            innerResults = qe.execSelect();
                            String imageHeight="";
                            while (innerResults.hasNext()) {
                                QuerySolution qs2 = innerResults.next();
                                imageHeight = qs2.get("obj").toString().split("\\^")[0];
                            }
                            int imgHeight=0;
                            try{
                                imgHeight=Integer.parseInt(imageHeight);
                            }
                            catch(NumberFormatException e)
                            {
                            }
                            
                            innerQuery = QueryFactory.create("prefix exif:<http://www.w3.org/2003/12/exif/ns#> select  ?obj where { <" + img + "> " + " exif:width ?obj  }");
                            qe = QueryExecutionFactory.create(innerQuery, sequenceModel);
                            innerResults = qe.execSelect();
                            String imageWidth="";
                            while (innerResults.hasNext()) {
                                QuerySolution qs2 = innerResults.next();
                                imageWidth = qs2.get("obj").toString().split("\\^")[0];
                            }
                            int imgWidth=0;
                            try{
                                imgWidth=Integer.parseInt(imageWidth);
                            }
                            catch(NumberFormatException e)
                            {
                            }
                            
//                            System.out.print("\t\t\t\t\t\tdimensions: [W="+imgWidth+" x H="+imgHeight+"]\n");
                            
                            //
                            // add our new image
                            //
                            canvas tmp = new canvas(canvas, title, new ImageChoice[]{new ImageChoice(img, imgWidth,imgHeight )}, positionCounter,cwidth,cheight);
                            accumulator.push(tmp);
                            positionCounter++;
                        }
                        
                        //
                        // ImageChoice
                        //
                        else {
//                            System.out.print("\t\t\t\t\t\tfound imagechoice\n");
                            
                            //this was an imagechoice, query deeper to find all possible images
                            innerQuery4 = QueryFactory.create("prefix dms:<http://dms.stanford.edu/ns/> prefix sc:<http://www.shared-canvas.org/ns/>  select ?obj  where {{{ <" + img + "> dms:option ?obj }UNION{ <" + img + "> dms:default ?obj }} union { <" + img + "> sc:option ?obj }UNION{ <" + img + "> sc:default ?obj }}");
                            qe = QueryExecutionFactory.create(innerQuery4, sequenceModel);
                            innerResults4 = qe.execSelect();
                            //Build an image choice for each image in the options list.
                            Stack<ImageChoice> imgs = new Stack();
                            while (innerResults4.hasNext()) {
                                innerqs4 = innerResults4.next();
                                String img2 = innerqs4.getResource("obj").getURI();
                                
//                                System.out.print("\t\t\t\t\t\t\tfound option at URI="+img2+"\n");
                                
                                imgs.push(new ImageChoice(img2, 0, 0));
                            }
                            
                            ImageChoice[] imgAnnos = new ImageChoice[imgs.size()];
                            
                            // if we did NOT get any Choices...
                            if(imgs.size()==0)
                            {
                                String imageurl="";
                                imgAnnos=new ImageChoice[1];
                                
                                // if the first call to .split() returns nothing,
                                // the second call throws an exception (ArrayIndexOutOfBoundsException)
                                try {
                                    imageurl=canvas.split("#")[0];
                                    imageurl=imageurl.split("data/")[1];
                                    imageurl="http://fsiserver.library.jhu.edu/server?type=image&source=rose/"+imageurl+"&height=2000";
                                }
                                // @TODO:  Catch a more-specific exception...  NullPointerException?
                                catch (Exception e)
                                {
                                    System.out.println("Error while populating ImageChoice!");
                                    System.out.println("\t--at Canvas URI="+canvas);
                                    System.out.println("\t--at target-er URI="+innerqs2.getResource("sub").getURI());
                                    System.out.println("\t--at ?image annotation? URI="+img);
                                    System.out.println("\t--msg = "+e.toString());
                                }
                                
                                imgAnnos[0]=new ImageChoice(imageurl,0,0);
                            }
                            
                            // if we DID get Choices, add 'em to our stack
                            int ctr = 0;
                            while (!imgs.empty()) {
                                imgAnnos[ctr] = imgs.pop();
                                ctr++;
                            }
                            
                            if(title==null || title.compareTo("")==0)
                            {
                                title=canvas.split("#")[0];
                                title=title.split("/")[title.split("/").length-1];
                                title=title.split("\\.")[1];
                            }
                            //now build a canvas with all of those images associated
                            canvas tmp = new canvas(canvas, title, imgAnnos, positionCounter,cwidth,cheight);
                            accumulator.push(tmp);
                            positionCounter++;
                        }
                    } else {
//                        System.out.println("Found no body for image annotation!");
                    }
                }
            }
        }
        
        sequenceItems = new canvas[accumulator.size()];
        int ctr = 0;
        while (!accumulator.empty()) {
            sequenceItems[ctr] = accumulator.pop();
            ctr++;
        }
        canvas [] rev=new canvas[sequenceItems.length];
        for(int i=0;i<rev.length;i++)
        {
            rev[rev.length-i-1]=sequenceItems[i];
        }
        sequenceItems=rev;
    }
    
    
    /**
     * TODO:  Complete.
     * 
     * @param args
     * @throws SQLException 
     */
    public static void main(String[] args) throws SQLException {
        try {
            URL[] urls = new URL[2];
            //urls[0] = new URL("http://www.shared-canvas.org/impl/demo4/res/W165/Manifest.xml");
            urls[0] = new URL("http://rosetest.library.jhu.edu/m3//Arras897");
            urls[1] = new URL("http://rosetest.library.jhu.edu/m3//Arras897/images");
            
            sequence s = new sequence(urls, "");
            //System.out.print("mine:"+s.createRDF(""));
            //if(true)return;
            //if city is populated, print shelfmark
            if (s.getCity().compareTo("") != 0) {
                System.out.print("Shelfmark:" + s.city + ", " + s.repository + ", " + s.collection + "\n");
            }
            canvas[] canvases = s.getSequenceItems();
            for (int i = 0; i < canvases.length; i++) {
                System.out.print("Position:" + canvases[i].getPosition() + "\n");
                System.out.print("Title:" + canvases[i].getTitle() + "\n");
                System.out.print("Canvas:" + canvases[i].getCanvas() +" w:"+canvases[i].getWidth()+" h:"+canvases[i].getHeight()+ "\n");
                
                ImageChoice[] images = canvases[i].getImageURL();
                if(false && images.length==0)
                {
                    String imageurl=canvases[i].getCanvas().split("#")[0];
                    imageurl=imageurl.split("data/")[1];
                    imageurl="http://fsiserver.library.jhu.edu/server?type=image&source=rose/"+imageurl+"&height=2000";
                    ImageChoice [] im=new ImageChoice[1];
                    im[0]=new ImageChoice(imageurl,0,0);
                    canvases[i].setImageURL(im);
                    images=im;
                }
                for (int c = 0; c < images.length; c++) {
                    System.out.print("Image:" + images[c].getImageURL() +" w:"+images[c].getWidth()+" h:"+images[c].getHeight()+ "\n");
                }
                
            }
        } catch (IOException ex) {
            Logger.getLogger(sequence.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    
    /**
     * TODO:  Complete.
     * 
     * @param format
     * @return String
     */
    public String createRDF(String format)
    {
        String toret="";
        Model model = ModelFactory.createDefaultModel();
        model.setNsPrefix("dms", "http://dms.stanford.edu/ns/");
        model.setNsPrefix("oac", "http://www.openannotation.org/ns/");
        model.setNsPrefix("rdf", "http://www.w3.org/1999/02/22-rdf-syntax-ns#");
        model.setNsPrefix("ore", "http://www.openarchives.org/ore/terms/");
        model.setNsPrefix("cnt", "http://www.w3.org/2008/content#");
        model.setNsPrefix("dc", "http://purl.org/dc/elements/1.1/");
        model.setNsPrefix("dcterms", "http://purl.org/dc/terms/");
        
        Resource seq=model.createResource("http://t-pen.org/sequences/");
        Resource DMScanvas=model.createResource("http://dms.stanford.edu/ns/Canvas");
        Resource imageBody=model.createResource("http://dms.stanford.edu/ns/ImageBody");
        Property rdfType=model.createProperty("http://www.w3.org/1999/02/22-rdf-syntax-ns#", "type");
        Property OAChasTarget=model.createProperty("http://www.openannotation.org/ns/", "hastarget");
        Property OAChasBody=model.createProperty("http://www.openannotation.org/ns/", "hasBody");
        RDFList l=model.createList(new RDFNode[]{seq});
        Property aggregates=model.createProperty("http://www.openarchives.org/ore/terms/","aggregates");
        for(int i=0;i<this.sequenceItems.length;i++)
        {
            Resource view=model.createResource(sequenceItems[i].getCanvas());
            //view.addProperty(rdfType,view );
            view.addProperty(rdfType, DMScanvas);
            seq.addProperty(aggregates,view);
            Resource image=model.createResource(sequenceItems[i].getImageURL()[0].getImageURL());
            image.addProperty(rdfType, imageBody);
            Resource imageAnnotation=model.createResource(sequenceItems[i].getCanvas()+"/ImageAnnotation");
            imageAnnotation.addProperty(OAChasTarget, view);
            imageAnnotation.addProperty(OAChasBody, image);
            
            l.add(view);
        }
        StringWriter tmp=new StringWriter();
        model.write(tmp);
        return tmp.getBuffer().toString();
    }
    
    
}
