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
package ImageUpload;

import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Enumeration;
import java.util.Stack;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;
import imageLines.ImageHelpers;
import javax.imageio.ImageIO;
import textdisplay.Folio;
import textdisplay.Manuscript;
import user.User;

/**
 * This class handles the process of creating a manuscript and project from a user uploaded zip file full of
 * jpg images.
 */
public class UserImageCollection {

   /**
    * Delete a manuscript created on a set of private images
    *
    * @param m the manuscript to delete. Manuscript.getArchive must be "private"
    * @throws SQLException
    */
   public static void delete(Manuscript m) throws SQLException {
      if (m.getArchive().compareTo("private") != 0) {
         return;
      }

      Folio[] fols = m.getFolios();
      for (int i = 0; i < fols.length; i++) {

         //delete the image
         File tmp = new File(fols[i].getImageName());
         if (tmp.exists()) {
            tmp.delete();
         }
         /*@TODO consider implementing deeper deletion of the other data*/
         //remove the Folio record

         //remove any project references to this

         //remove any transcriptions that reference this


      }
      //now delete the Manuscript record
   }

   private static File[] removeItem(File[] old, int item) {
      File[] res = new File[old.length - 1];
      int pos = 0;
      for (int i = 0; i < old.length; i++) {
         if (i != item) {
            res[pos] = old[i];
            pos++;
         }
      }
      return res;
   }

   /**
    * Unzip the images in the zip file, put them in the proper location, and create folios records for them
    *
    * @param zippedFile name of the zip file without path
    * @param uploader uploading user
    * @param ms manuscript created to contain these images
    * @throws Exception
    */
   public UserImageCollection(File zippedFile, User uploader, Manuscript ms) throws Exception {
      String directory = Folio.getRbTok("uploadLocation");
      File dir = new File(directory + "/" + uploader.getLname() + "/" + ms.getID());
      if (!dir.exists()) {
         dir.mkdirs();

      }

      File newZippedFile = new File(dir.getAbsoluteFile() + "/" + zippedFile.getName());
      zippedFile.renameTo(newZippedFile);
      zippedFile = newZippedFile;
      extractFolder(zippedFile.getAbsolutePath());
      File[] images = getAllJPGsRecursive(dir);
      for (int i = 0; i < images.length; i++) {
         if (!validateImage(images[i])) {
            System.out.print("bad image, would do something\n");
            images = removeItem(images, i);
            i--;
         }

      }
      for (int i = 0; i < images.length; i++) {
         Folio.createFolioRecord(ms.getCollection(), images[i].getName(), images[i].getAbsolutePath(), "private", ms.getID(), i, "");
      }


   }

   /**
    * Recurse through this directory finding all images in whatever folders are in it
    *
    * @param dir the directory to look in
    * @return
    */
   public static File[] getAllJPGsRecursive(File dir) {
      Stack<File> ret = getJPGSInFolder(dir);
      File[] toret = new File[ret.size()];
      for (int i = 0; i < toret.length; i++) {
         toret[i] = ret.pop();
      }
      return toret;
   }

   /**
    * Find all jpgs in this folder and all subfolders
    *
    * @param dir dir the directory to look in
    * @return
    */
   public static Stack<File> getJPGSInFolder(File dir) {
      Stack<File> res = new Stack();
      File[] allFiles = dir.listFiles();
      for (int i = 0; i < allFiles.length; i++) {
         if (allFiles[i].getName().toLowerCase().contains(".jpg")) {
            res.add(allFiles[i]);
         } else {
            if (allFiles[i].isDirectory()) {
               res = merge(res, getJPGSInFolder(allFiles[i]));
            }
         }

      }
      return res;
   }

   /**
    * Helper method to merge 2 stacks
    */
   private static Stack merge(Stack a, Stack b) {
      Stack c = new Stack();
      while (!a.empty()) {
         c.push(a.pop());
      }
      while (!b.empty()) {
         c.push(b.pop());
      }
      return c;

   }

   /**
    * This function was created as an answer to the stack overflow question
    * http://stackoverflow.com/questions/981578/how-to-unzip-files-recursively-in-java I decided not to
    * reinvent the wheel
    *
    * @param zipFile The location of the zip file
    * @throws ZipException on incomplete uploads etc
    * @throws IOException if the file doesnt exist..
    */
   static public void extractFolder(String zipFile) throws ZipException, IOException {
      System.out.println(zipFile);
      int BUFFER = 2048;
      File file = new File(zipFile);

      ZipFile zip = new ZipFile(file);
      String newPath = zipFile.substring(0, zipFile.length() - 4);

      new File(newPath).mkdir();
      Enumeration zipFileEntries = zip.entries();

      // Process each entry
      while (zipFileEntries.hasMoreElements()) {
         // grab a zip file entry
         ZipEntry entry = (ZipEntry) zipFileEntries.nextElement();
         String currentEntry = entry.getName();
         File destFile = new File(newPath, currentEntry);
         //destFile = new File(newPath, destFile.getName());
         File destinationParent = destFile.getParentFile();

         // create the parent directory structure if needed
         destinationParent.mkdirs();

         if (!entry.isDirectory()) {
            try (BufferedInputStream is = new BufferedInputStream(zip.getInputStream(entry))) {
               int currentByte;
               // establish buffer for writing file
               byte data[] = new byte[BUFFER];

               // write the current file to disk
               FileOutputStream fos = new FileOutputStream(destFile);
               try (BufferedOutputStream dest = new BufferedOutputStream(fos, BUFFER)) {
                  while ((currentByte = is.read(data, 0, BUFFER)) != -1) {
                     dest.write(data, 0, currentByte);
                  }
                  dest.flush();
               }
            }
         }

         if (currentEntry.endsWith(".zip")) {
            // found a zip file, try to open
            extractFolder(destFile.getAbsolutePath());
         }
      }
   }

   /**
    * Check that the image is actually a valid jpg image by loading it as a BufferedImage
    */
   private static Boolean validateImage(File f) {
      try {
         BufferedImage img = ImageHelpers.readAsBufferedImage(f.getAbsolutePath());
         img = ImageHelpers.scale(img, 2000);
         ImageIO.write(img, "jpg", f);
      } catch (Exception e) {
         LOG.log(Level.SEVERE, e.getMessage());
         return false;
      }
      return true;
   }
   
   private static final Logger LOG = Logger.getLogger(UserImageCollection.class.getName());
}
