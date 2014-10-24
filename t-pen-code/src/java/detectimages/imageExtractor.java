/*
 * Copyright 2011-2013 Saint Louis University. Licensed under the
 *	Educational Community License, Version 2.0 (the "License"); you may
 * not use this file except in compliance with the License. You may
 * obtain a copy of the License at
 *
 * http://www.osedu.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an "AS IS"
 * BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing
 * permissions and limitations under the License.
 * 
 * @author Jon Deering
 */
package detectimages;

import java.awt.image.BufferedImage;
import java.io.File;
import java.util.concurrent.Callable;
import imageLines.ImageHelpers;

public class imageExtractor implements Callable {

   private String destinationDir;
   private String destinationFileName;
   File imageFile;

   public imageExtractor(String destinationDir, String destinationFileName, File imageFile) {
      this.destinationDir = destinationDir;
      this.destinationFileName = destinationFileName;
      this.imageFile = imageFile;
   }

   @Override
   public Object call() throws Exception {
      BufferedImage img = ImageHelpers.readAsBufferedImage(imageFile.getAbsolutePath());
      detectimages.imageProcessor proc = new imageProcessor(img, 2000);
      proc.setDataPath(destinationDir);//this has to be the dir the file will go in without the filename
      proc.run2(destinationFileName);
      return null;
   }
}
