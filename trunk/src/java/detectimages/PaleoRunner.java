
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

import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;


public class PaleoRunner
{
    public static void main(String [] args)
    {
      if(args[0].compareTo("processImages")==0){
            //args=new String[]{"/usr/parker"};
            //first process the images
          String threads="4";
          if(args.length==3)
              threads=args[2];
            String[] p = new String[]{"processImages",args[1],threads};
        try {
            
            blob.main(p);
        } catch (IOException ex) {
            Logger.getLogger(PaleoRunner.class.getName()).log(Level.SEVERE, null, ex);
        } catch (SQLException ex) {
            Logger.getLogger(PaleoRunner.class.getName()).log(Level.SEVERE, null, ex);
        } catch (InterruptedException ex) {
            Logger.getLogger(PaleoRunner.class.getName()).log(Level.SEVERE, null, ex);
        }
        }
       
           if(args[0].compareTo("assignments")==0){
            //build the assignment files

            String[] p = new String[]{"assignments",args[1],"10000000"};
        try {
            
            blob.main(p);
        } catch (IOException ex) {
            Logger.getLogger(PaleoRunner.class.getName()).log(Level.SEVERE, null, ex);
        } catch (SQLException ex) {
            Logger.getLogger(PaleoRunner.class.getName()).log(Level.SEVERE, null, ex);
        } catch (InterruptedException ex) {
            Logger.getLogger(PaleoRunner.class.getName()).log(Level.SEVERE, null, ex);
        }
        }
            //if(true)return;
            //run the comparisons
      if(args[0].compareTo("run")==0){
          String threads="4";
          if(args.length==6)
              threads=args[5];
            String []p = new String[]{args[1],args[2],args[3],args[4],threads};
        
            try {
                try {
                    blob.main(p);
                } catch (IOException ex) {
                    Logger.getLogger(PaleoRunner.class.getName()).log(Level.SEVERE, null, ex);
                }
            } catch (SQLException ex) {
                Logger.getLogger(PaleoRunner.class.getName()).log(Level.SEVERE, null, ex);
            } catch (InterruptedException ex) {
                Logger.getLogger(PaleoRunner.class.getName()).log(Level.SEVERE, null, ex);
            }
          }
            //now load the results
      try{
            if(false)
            overloadLoader.main(args);
        } catch (FileNotFoundException ex) {
            Logger.getLogger(PaleoRunner.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(PaleoRunner.class.getName()).log(Level.SEVERE, null, ex);
        }



    }
}
