/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package OAC;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.ResIterator;
import com.hp.hpl.jena.rdf.model.Resource;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;

/**
 *
 * A canvas from the staford opencanvas data model. Represents a single displayable canvas where a page or a bunch of page fragments might reside.
 */
public class view
{
    private String uri;
    private int width;
    private int height;
    String [] images;
    Model theGraph;

    public view (Model m, String uri) throws MalformedURLException, IOException
        {
        this.uri = uri;
        theGraph = m;
        theGraph.setNsPrefix("dms", "http://dms.stanford.edu/ns/");
        theGraph.setNsPrefix("oac", "http://www.openannotation.org/ns/");
        theGraph.setNsPrefix("rdf", "http://www.w3.org/1999/02/22-rdf-syntax-ns#");
        theGraph.setNsPrefix("ore", "http://www.openarchives.org/ore/terms/");
        theGraph.setNsPrefix("cnt", "http://www.w3.org/2008/content#");
        theGraph.setNsPrefix("dc", "http://purl.org/dc/elements/1.1/");
        theGraph.setNsPrefix("dcterms", "http://purl.org/dc/terms/");
        URL viewUrl = new URL(uri);
        // use the FileManager to find the input file
       


        // read the RDF/XML file
        
        // if it were turtle model.read(in,null, "N3");
        Property transcriptionProperty=theGraph.createProperty("http://www.openannotation.org/ns/", "imageAnnotation");
        ResIterator r=theGraph.listResourcesWithProperty(transcriptionProperty);
        while(r.hasNext())
        {
            Resource theImage=r.next();
            String imageurl=theImage.getURI();
            String[] tmp=new String[images.length+1];
            for(int i=0;i<images.length;i++)
            {
                tmp[i]=images[i];
            }
            tmp[tmp.length-1]=imageurl;
            images=tmp;
        }

        }
    /**Return associated image uris*/
    public String[] getImages()
    {
        return images;
    }


}
