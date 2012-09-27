/*
 * @author Jon Deering
Copyright 2011 Saint Louis University. Licensed under the Educational Community License, Version 2.0 (the "License"); you may not use
this file except in compliance with the License.

You may obtain a copy of the License at http://www.osedu.org/licenses/ECL-2.0

Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions
and limitations under the License.
 */
package CollectionParser;

import detectimages.blob;
import java.sql.SQLException;
import textdisplay.Folio;
import textdisplay.Manuscript;


public class NorthwesternParser {
    /**
     * (@TODO:  Complete.)
     * 
     * This parser takes an array of rows of data from a csv file, 
     * along with the city and repo, and creates a manuscript 
     * and the folio records from the provided information. 
     * The expected csv row contains ms identifier,
     * something I dont use, image url, image name 
     * 
     * @param csvData
     * @param city
     * @param repo
     * @throws SQLException 
     */
    public static void parse(String[] csvData, String city, String repo) throws SQLException
    {
        //split by \n to get individual rows
        
        String currentMS="";
        Manuscript ms=null;
        int sequenceCounter=1;
        for(String row:csvData)
        {
            //split by commas to get mss, name, and url
            String[] elements= row.split(",");
            if(currentMS.compareTo(elements[0])!=0)
            {
                //create new Manuscript
                ms=new Manuscript(repo,"Northwestern",elements[0],city);
                currentMS=elements[0];
            }
            Folio.createFolioRecord(currentMS,elements[2] ,elements[3] , "Northwestern", ms.getID(), sequenceCounter, "");
            sequenceCounter++;
        }
    }
    
    
    /**
     * Passing the location of a CSV file
     * 
     * @param args
     * @throws SQLException 
     */
    public static void main(String[] args) throws SQLException
    {
        String[] a=blob.readFileIntoArray(args[0]);
        NorthwesternParser.parse(a, "Evanston", "Northwestern University Library");
    }
}
