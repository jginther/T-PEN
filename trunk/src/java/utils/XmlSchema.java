/*
 * @author Jon Deering
Copyright 2011 Saint Louis University. Licensed under the Educational Community License, Version 2.0 (the "License"); you may not use
this file except in compliance with the License.

You may obtain a copy of the License at http://www.osedu.org/licenses/ECL-2.0

Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions
and limitations under the License.
 */

package utils;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.net.URL;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.*;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;
import org.xml.sax.SAXException;
import textdisplay.Manuscript;
import textdisplay.Project;
import textdisplay.TagFilter;

/**Handles validating the contents of a project against the associated xml schema*/
public class XmlSchema
{
    private String schemaURL;

    /**
     * 
     */
    public enum types {RELAXNG,RELAXNG_COMPACT,DTD,SCHEMATRON};
    private types type;
    private String messages="";

    /**
     * This is the only constructor
     * @param schemaURL The url for the schema
     * @param type the XmlSchema.types that describes the format of the schema. Not all are supported.
     */
    public XmlSchema(String schemaURL, types type)
    {
        this.type=type;
        this.schemaURL=schemaURL;
    }
    /**
     * Run validation on the pass in xml using this schema
     * @param xml String with a (hopefully) well formed and schema compliant xml document
     * @return
     * @throws SAXException
     * @throws IOException
     */
    public Boolean validate(String xml) throws SAXException, IOException
    {
        //messages will contain this message if the schema is in a not yet supported format
        messages="Not yet implemented";
        if(this.type==types.RELAXNG_COMPACT)
         System.setProperty("javax.xml.validation.SchemaFactory:"+XMLConstants.RELAXNG_NS_URI,"com.thaiopensource.relaxng.jaxp.CompactSyntaxSchemaFactory");
        else
            System.setProperty("javax.xml.validation.SchemaFactory:"+XMLConstants.RELAXNG_NS_URI,"com.thaiopensource.relaxng.jaxp.XMLSyntaxSchemaFactory");
        SchemaFactory factory = SchemaFactory.newInstance(XMLConstants.RELAXNG_NS_URI);
        //change to use the schema stored here
        Schema schemaObj = factory.newSchema(new URL(schemaURL));
        Validator validator = schemaObj.newValidator();
        Source source = new StreamSource(new StringReader(xml));
        try {
            validator.validate(source);
            return(true);
        }
        catch (SAXException ex) {
            
            messages=ex.getMessage();
            return false;
        }
    }
    /**
     * Fetch the messages after the attempted validation
     * @return
     */
    public String getMessages()
    {
        return messages;
    }


    /**quick way to validate against TEIALL
     * @param text Full text of the document
     * @return text description of any errors
     * @throws SAXException
     * @throws IOException
     */
public static String validateTEIAll(String text) throws SAXException, IOException {

        String fileName="The document";

        // 1. Lookup a factory for the RNG schema
        System.setProperty("javax.xml.validation.SchemaFactory:"+XMLConstants.RELAXNG_NS_URI,"com.thaiopensource.relaxng.jaxp.CompactSyntaxSchemaFactory");
        SchemaFactory factory = SchemaFactory.newInstance(XMLConstants.RELAXNG_NS_URI);
        File schemaLocation = new File("/usr/web/tei_all.rnc"); //use a schema stored on the server, could easily use a remote one but tei-all is ~275k
        Schema schema = factory.newSchema(schemaLocation);
        Validator validator = schema.newValidator();
        Source source = new StreamSource(new StringReader(text));
        try {
            validator.validate(source);
            return(fileName + " is valid.");
        }
        catch (SAXException ex) {
            return(fileName + " is not valid because "+ex.getMessage());
        }

    }
}
