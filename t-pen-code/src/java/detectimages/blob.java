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

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadFactory;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import javax.imageio.ImageIO;
import javax.swing.JComponent;
import javax.swing.JFrame;

/**
 * A blob is a representation of the boolean matrix that describes a single glyph from a binarized image
 */
public class blob implements Serializable {
   //all images are resized to have this height.

   static final int maxHeight = 2000;
   static final int minBlobSize = 15;

   /**
    * Draw a blob on an image starting from the given coordinates.
    */
   public static void drawBlob(BufferedImage img, int x, int y, blob b) {
      int color = 0xcc0000;
      if (b.size % 3 == 0) {
         color = 0xcc0000;
      }
      if (b.size % 3 == 1) {
         color = 0x000099;
      }
      if (b.size % 3 == 2) {
         color = 0x006600;
      }

      for (int i = 0; i < b.pixels.size(); i++) {
         pixel p = b.pixels.get(i);
         try {
            img.setRGB(x + p.x, y + p.y, color);
         } catch (ArrayIndexOutOfBoundsException e) {
            return;
         }
      }
   }

   /**
    * Draw the blob onto a buffere
    */
   public static void drawBlob(BufferedImage img, int x, int y, blob b, int color) {
      for (int i = 0; i < b.pixels.size(); i++) {
         pixel p = b.pixels.get(i);
         try {
            img.setRGB(x + p.x, y + p.y, color);
         } catch (ArrayIndexOutOfBoundsException e) {
         }
      }
   }
//coordinates of a pixel in the blob. Any pixel will do.
   static Pattern p = Pattern.compile("\\n");
   static Pattern comma = Pattern.compile(",");
   protected int x;
   protected int y;
   protected int width;
   protected int height;
   public matrixBlob matrixVersion;
   public BlockBlob blockVersion;
   public altBlob altVersion;
   protected int size;
   public blob blob2;
   protected Vector<pixel> pixels = new Vector(25, 25);
   public int color;
   public int id;
   public BufferedImage copy;
   public pixel[] arrayVersion;

   public blob() {
      size = 0;
      x = 0;
      y = 0;
      id = -1;
   }

   public blob(int x, int y) {
      this.x = x;
      this.y = y;
      //pixels.push(new pixel(x,y));
      id = -1;
      size = 0;
   }

   /**
    * Service a request for a single blob from a page, intended for drawing the blob. Currently loads the
    * entire page worth of data, so quite inefficient. Returns null if it couldnt service the request.
    */
   public static blob getBlob(String imageName, int blobIdentifier) {
      try {
         Vector<blob> b = blob.getBlobs("data/" + imageName);

         return b.get(blobIdentifier);
      } catch (FileNotFoundException ex) {
         Logger.getLogger(blob.class.getName()).log(Level.SEVERE, null, ex);
         return null;
      } catch (IOException ex) {
         Logger.getLogger(blob.class.getName()).log(Level.SEVERE, null, ex);
         return null;
      }
   }

   /**
    * Service a request for a single blob from a page, intended for drawing the blob. Currently loads the
    * entire page worth of data, so quite inefficient. Returns null if it couldnt service the request.
    */
   public static blob getBlob(String path, String imageName, int blobIdentifier) {
      try {
         Vector<blob> b = blob.getBlobs(path + imageName);

         return b.get(blobIdentifier);
      } catch (FileNotFoundException ex) {
         Logger.getLogger(blob.class.getName()).log(Level.SEVERE, null, ex);
         return null;
      } catch (IOException ex) {
         Logger.getLogger(blob.class.getName()).log(Level.SEVERE, null, ex);
         return null;
      }
   }

   /**
    * Draw this blob on the image in the requested color. Color is RGB.
    */
   protected void drawBlob(BufferedImage img, int color) {
      Stack<pixel> pixels2 = new Stack();
      for (int i = 0; i < pixels.size(); i++) {
         pixel thisOne = pixels.get(i);
         try {
            img.setRGB(thisOne.x, thisOne.y, color);
         } catch (ArrayIndexOutOfBoundsException ex) {
         }
         pixels2.push(thisOne);
      }
      //displayImage(img);
      pixels = pixels2;
   }

   /**
    * Find the topmost and leftmost points, and subtract their positions from all pixels, the result being a
    * coordinate set that describes this blob from a base of 0,0
    */
   protected void calculateRelativeCoordinates() {

      int top_lowest = 9999999;
      int left_lowest = 9999999;
      for (int i = 0; i < pixels.size(); i++) {
         pixel thisOne = pixels.get(i);
         if (thisOne.y < left_lowest) {
            left_lowest = thisOne.y;
         }
         if (thisOne.x < top_lowest) {
            top_lowest = thisOne.x;
         }


      }

      //This is the point that the relative coordinates are based on, store its real location within the image so the blob can be
      //redrawn later using the blob data.
      this.x = top_lowest;
      this.y = left_lowest;
      for (int i = 0; i < pixels.size(); i++) {
         pixel tmp = pixels.get(i);
         tmp.x = (short) (tmp.x - this.x);
         tmp.y = (short) (tmp.y - this.y);
      }
      sort();
   }

   /**
    * Sort blob pixels by x, with y as the secondary sort criteria
    */
   private void sort() {
      pixel[] pixelArray = pixels.toArray(new pixel[pixels.size()]);
      Arrays.sort(pixelArray);
      pixels = new Vector();
      for (int i = 0; i < pixelArray.length; i++) {
         pixels.add(pixelArray[i]);
      }
   }

   /**
    * count the number of black pixels adjacent to x,y using recursion. this is the character/blob
    * segmentation mechanism. It is primative.
    */
   public void count(BufferedImage img, int currentx, int currenty) {
      //increase size and set this pixel to white so it doesnt get counted again
      //System.out.println(currentx+" "+currenty);

      //if(size>50000)
      //	return;

      img.setRGB(currentx, currenty, -1);
      if (size > 1 && size < 4000) {
         copy.setRGB(currentx, currenty, color);
         pixels.add(new pixel(currentx, currenty));

         //imageHelpers.writeImage(copy, "/usr/web/bin"+size+".jpg");
      }
      //prevent stack overflow if the blob is too large, if it is this large it is uninteresting
      if (size > 4000) {
         return;
      }
      size++;
      //check the 8 surrounding pixels, if any of them is black, call this function again.
      try {
         /*if(img.getRGB(currentx-1, currenty)!=-1)

          {
          count(img,currentx-1, currenty);
          }
          if(img.getRGB(currentx-1, currenty+1)!=-1)
          {
          count(img,currentx-1, currenty+1);
          }*/
         if (img.getRGB(currentx - 1, currenty) != -1) {
            count(img, currentx - 1, currenty);
         }
         if (img.getRGB(currentx, currenty + 1) != -1) {
            count(img, currentx, currenty + 1);
         }
         if (img.getRGB(currentx, currenty - 1) != -1) {
            count(img, currentx, currenty - 1);
         }
         if (img.getRGB(currentx + 1, currenty) != -1) {
            count(img, currentx + 1, currenty);
         }
         /*if(img.getRGB(currentx+1, currenty+1)!=-1)
          {
          count(img,currentx+1, currenty+1);
          }
          if(img.getRGB(currentx+1, currenty-1)!=-1)
          {
          count(img,currentx+1, currenty-1);
          }*/
      } catch (ArrayIndexOutOfBoundsException e) {
      }
   }

   /**
    * Given mean height and width for this page, this blob will attpemt to break itself into a reasonable
    * number of smaller pieces.
    */
   /*blob [] breakBlob(int width, int height)
    {
    
    blob [] toret=new blob[1];
    matrixBlob thisOne = new matrixBlob(this);
    int [] [] matrixVersion=thisOne.matrix
    return toret;
    }*/
   /**
    * count the number of black pixels adjacent to x,y No longer used, blob drawing happens after this so
    * overly large and small blobs are excluded from the drawing process, and so the same methodology can be
    * used for the graphical demonstration where blobs are drawn as they are matched.
    */
   public void count(BufferedImage img, BufferedImage bin, int currentx, int currenty) {
      //increase size and set this pixel to white so it doesnt get counted again
      //System.out.println(currentx+" "+currenty);

      if (size > 5000) {
         return;
      }
      img.setRGB(currentx, currenty, -1);
      if (size > 1) {
         copy.setRGB(currentx, currenty, color);

         //imageHelpers.writeImage(copy, "/usr/web/bin"+size+".jpg");
      }
      size++;
      //check the 8 surrounding pixels, if any of them is black, call this function again.
      try {
         if (img.getRGB(currentx - 1, currenty) != -1) {
            count(img, bin, currentx - 1, currenty);
         }
         /*if(img.getRGB(currentx-1, currenty+1)!=-1)
          {
          count(img,bin,currentx-1, currenty+1);
          }
          if(img.getRGB(currentx-1, currenty-1)!=-1)
          {
          count(img,bin,currentx-1, currenty-1);
          }*/
         if (img.getRGB(currentx, currenty + 1) != -1) {
            count(img, bin, currentx, currenty + 1);
         }
         if (img.getRGB(currentx, currenty - 1) != -1) {
            count(img, bin, currentx, currenty - 1);
         }
         if (img.getRGB(currentx + 1, currenty) != -1) {
            count(img, bin, currentx + 1, currenty);
         }
         /*if(img.getRGB(currentx+1, currenty+1)!=-1)
          {
          count(img,bin,currentx+1, currenty+1);
          }
          if(img.getRGB(currentx+1, currenty-1)!=-1)
          {
          count(img,bin,currentx+1, currenty-1);
          }*/
      } catch (ArrayIndexOutOfBoundsException e) {
      }
   }

   /**
    * Main method to run image processing and blob comparisons
    */
   public static void main(String[] args) throws IOException, SQLException, InterruptedException {
      int taskCount = 4;
      int thresholdMethod = 4;
      ExecutorService executor = null;
      try {
         class prioritythread implements ThreadFactory {

            public Thread newThread(Runnable r) {
               Thread t = new Thread(r);
               t.setPriority(Thread.MIN_PRIORITY);
               return t;

            }
         }
         //Console con=new Console(); //was used when this was a distributed client

         String infile = "assignments.dat";//contains assignments to be completed
         String outfile = "savedBlobs.txt";
         String logfile = "out.log";
         String statefile = "state.dat";
         String imgPath = "imgs/";
         boolean noDB = false; //if true, use data files rather than the database
         boolean ProcessImages = false;

         //is this a request to create data files from images?
         if ((args.length == 4 || args.length == 3) && args[0].compareTo("processImages") == 0) {
            ProcessImages = true;
            imgPath = args[1];
            taskCount = Integer.parseInt(args[2]);
            if (args.length == 4) {
               thresholdMethod = Integer.parseInt(args[3]);
            }
            imageProcessor.setThesholdMethod(thresholdMethod);
         }


         //is this a request to carry out a set of comparisons?
         if (args.length >= 4) {
            infile = args[0];
            outfile = args[1];
            statefile = args[2];
            logfile = args[3];
            if (args.length >= 5) {
               try {
                  taskCount = Integer.parseInt(args[4]);
               } catch (NumberFormatException e) {
               }
            }
            noDB = true;
         }
         OutputStream outf = new FileOutputStream(new File(logfile));
         //System.setOut(new PrintStream(outf));
         //System.out.print("Close this window to end processing.\n"); //this line was printed to the console
         String dataPath = "data/";
         File dataPathFile = new File(dataPath);

         if (!dataPathFile.exists()) {
            dataPathFile.mkdir();
         }


         File imagePathFile = new File(imgPath);
         if (!imagePathFile.exists()) {
            imagePathFile.mkdir();
         }

         executor = Executors.newFixedThreadPool(taskCount, new prioritythread());

         boolean cropImage = true;
         boolean graphics = false;
         /**
          * Should graphics be activated during this run
          */
         if (args.length == 5) {
            if (args[4].compareTo("graphics") == 0) {
               graphics = true;
            }

         }

         File dir = new File(imgPath);
         //these two images are used for the graphical display
         BufferedImage orig = null;
         BufferedImage comparedTo = null;


         //File[] subdirs=dir.listFiles();

         BufferedWriter records = new BufferedWriter(new FileWriter("savedBlobs.txt"));

//for(int subDirCtr=0;subDirCtr<subdirs.length;subDirCtr++)
         {

            File[] images = dir.listFiles();
            for (int c = 0; c < images.length; c++) {
               try {
                  if (images[c].listFiles().length > 0) {
                     images = images[c].listFiles();
                  }
                  break;
               } catch (Exception e) {
               }
            }
            //System.out.print(subdirs[subDirCtr].getAbsoluteFile()+"\n");
            //System.out.flush();
            Vector[] blobarrayall = new Vector[images.length];
            //if ProcessImages is set, create data files based on a set of images located in imgPath
            for (int fileIndex = images.length - 1; fileIndex >= 0 && ProcessImages; fileIndex--) {
               if (images[fileIndex].getName().contains(".jpg")) {
                  executor.submit(new imageProcessor(records, images[fileIndex], maxHeight, dataPath));
               } else {
                  System.out.print("Skipping " + images[fileIndex].getName() + "\n");
               }
            }
         }
         //if we were processing images, this is the end of the line
         if (ProcessImages) {
            executor.shutdown();
            return;
         }

         //if(true)return;
         int matches;
         ImagePanel2 origpan = null;
         ImagePanel2 comparedTopan = null;
         String a1 = "";
         String a2 = "";
         Vector<blob> blobs = null;
         Vector<blob> blobs2 = null;
         blobManager manager = new blobManager();
         //Use assignment files rather than getting assignments from a database
         if (noDB || true) {
            FileWriter w = new FileWriter(new File(outfile), true);
            File stateFile = new File(statefile);
            String[] assignment;
            String[] nextAssignment = null; //used to prefetch the next assignment
            BufferedReader assignmentReader = new BufferedReader(new FileReader(infile));
            File stateFileTmp = new File(statefile + "tmp");
            //This recovers if the program ended before finishing its workload, starting by completeing the last comparison it was working on
            if (stateFile.exists()) {
               try {
                  assignment = blob.getAssignment(new BufferedReader(new FileReader(statefile)), assignmentReader);
               } catch (Exception e) {
                  assignment = blob.getAssignment(assignmentReader, stateFile);
               }
            } else {
               assignment = blob.getAssignment(assignmentReader, stateFile);
            }
            LinkedList<Future<String>> set = new LinkedList<Future<String>>();
            while (true) {

               for (int pageCtr = 0; pageCtr < taskCount * 8; pageCtr++) {
                  while (assignment == null) {

                     if (!assignmentReader.ready()) {
                        while (set.size() > 0) {
                           String res = set.remove().get();
                           w.append(res);
                           w.flush();

                        }
                        executor.shutdown();
                        return;
                     }
                     assignment = blob.getAssignment(assignmentReader, stateFile);
                     if (assignment != null) {
                        break;
                     }
                     System.out.print("No assignment recieved, quiting...\n");
                     executor.shutdown();
                     return;

                  }//no more work to do all assigmen


                  //Vector <blob>blobs=blobarrayall[k];
                  //Vector <blob>blobs2=blobarrayall[c];
                  try {
                     String tmpStr = assignment[0].replace(".jpg", "");

                     blobs = manager.get(dataPath + assignment[0].replace(".jpg", "") + ".txt");
                     if (blobs == null) {
                        manager.add(getBlobs(dataPath + assignment[0].replace(".jpg", "") + ".txt"), dataPath + assignment[0].replace(".jpg", "") + ".txt");
                        blobs = manager.get(dataPath + assignment[0].replace(".jpg", "") + ".txt");
                     }
                     //getBlobs(dataPath+assignment[0].replace(".jpg", "")+".txt");
                     a1 = tmpStr;

                     //tmpStr=assignment[1].replace(".jpg", "");
                     //if(tmpStr.compareTo(a2)!=0)
                     //{
                     //blobs2=getBlobs(dataPath+assignment[1].replace(".jpg", "")+".txt");
                     System.out.print("Starting\n");
                     System.out.flush();
                     //runOpenCl(blobs,blobs2);
                     //}
                     // System.out.print(assignment[0]+" "+ assignment[1]+"\n");
                     // System.out.flush();
                     if (assignment[0].compareTo(assignment[1]) != 0) {
                        set.add(executor.submit(new pageComparer(blobs, dataPath + assignment[1].replace(".jpg", "") + ".txt", assignment, manager)));
                     }
                     assignment = null;
                  } catch (Exception e) {
                     assignment = null;
                  }
               }
               String res;
               while (set.size() > taskCount * 2) {

                  res = set.remove().get();
                  w.append(res);
                  w.flush();

               }





            }

         } else {
            //old code to use a mysql database for assignment management has been removed
            //writeImage(orig,"/usr/web/frank"+images[k].getName().replace(".jpg", "")+images[c].getName());
         }
      } catch (Exception e) {
         System.out.print("Quitting due to the following error:\n");
         e.printStackTrace();
         if (executor != null) {
            executor.shutdown();
         }
      }
   }

   public static BufferedImage drawBlob(blob b, BufferedImage bin) {

      return bin;
   }

   /**
    * Display the image...like on the screen. Useful for debugging, not so great on a server.
    */
   public static ImagePanel2 displayImage(BufferedImage img) {
      JFrame fr = new JFrame();
      fr.setDefaultCloseOperation(fr.EXIT_ON_CLOSE);
      //fr.setTitle(title)
      ImagePanel2 pan = new ImagePanel2(img);
      Toolkit toolkit = java.awt.Toolkit.getDefaultToolkit();
      Dimension screensize = toolkit.getScreenSize();

      pan.setSize(screensize.width, screensize.height);
      fr.getContentPane().add(pan);
      fr.pack();
      fr.setSize(img.getWidth(), img.getHeight());
      fr.show();
      return pan;
   }

   public static class ImagePanel2 extends JComponent {

      protected BufferedImage image;

      public ImagePanel2() {
      }

      public ImagePanel2(BufferedImage img) {
         image = img;
      }

      public void setImage(BufferedImage img) {
         image = img;
      }

      public void paintComponent(Graphics g) {
         Rectangle rect = this.getBounds();
         if (image != null) {
            int newWidth, newHeight;
            double scale = image.getWidth() / 800;
            newHeight = 800;
            newWidth = (int) (image.getWidth() / scale);
            g.drawImage(image, 0, 0, image.getWidth(), image.getHeight(), this);
         }
      }
   }

   public int getX() {
      return x;
   }

   public int getY() {
      return y;
   }

   public int getSize() {
      return size;
   }

   /**
    * Returns the percentage of pixels in the relative positioned blobs that are in common
    */
   public static double compare(blob a, blob b) {
      if (a.size - b.size > 500 || a.size - b.size < -500) {
         return 0.01;
      }
      int good = 0;
      //first, find the center of mass of each blob, this will be used to best align them. well, maybe not best.
      int axSum = 0;
      int aySum = 0;
      int bxSum = 0;
      int bySum = 0;
      pixel[] aArray = a.pixels.toArray(new pixel[a.pixels.size()]);
      pixel[] bArray = b.pixels.toArray(new pixel[b.pixels.size()]);

      int innercount = 0;
      /* for(int i=0;i<aArray.length;i++)
       {
       pixel aCurrent=aArray[i];
       axSum+=aCurrent.x;
       aySum+=aCurrent.y;
       }

       for(int i=0;i<bArray.length;i++)
       {
       pixel bCurrent=bArray[i];
       Arrays.sort(bArray);
       bxSum+=bCurrent.x;
       bySum+=bCurrent.y;
       }

       bxSum=bxSum/b.size;
       bySum=bySum/b.size;

       aySum=aySum/a.size;
       axSum=axSum/a.size;
       int yDiff=aySum-bySum;
       int xDiff=axSum-bxSum;
       if(xDiff<0)
       xDiff=xDiff*-1;
       if(yDiff<0)
       yDiff=xDiff*-1;
       System.out.print("xDiff:"+xDiff+" yDiff:"+yDiff+"\n");
       int maxgood=0;*/



      Boolean wouldabroke = false;
      for (int i = 0; i < aArray.length; i++) {


         innercount = 0;
         pixel aCurrent = aArray[i];

         for (int j = 0; j < bArray.length; j++) {
            pixel bCurrent = bArray[j];
            if ((aCurrent.x == (bCurrent.x) && aCurrent.y == (bCurrent.y))) {
               //System.out.print("offset "+ offset+"\n");

               good++;


            }

            innercount++;
         }
      }


      if (a.size > b.size) {
         return (double) good / a.size;
      } else {
         return (double) good / b.size;
      }

   }

   /**
    * @Depricated we no longer write blob data to a database
    */
   public static void writeBlob(Connection j, blob b, String image) throws SQLException {
      String query = "Insert into blobs(image, size) values(?,?)";
      String pixelQuery = "Insert into pixels(x,y,BlobId) values(?,?,?)";

      PreparedStatement stmt = j.prepareStatement(query, PreparedStatement.RETURN_GENERATED_KEYS);
      stmt.setInt(2, b.size);
      stmt.setString(1, image);
      ResultSet rs;
      stmt.execute();
      rs = stmt.getGeneratedKeys();
      rs.next();
      int blobID = rs.getInt(1);

      /* stmt=j.prepareStatement("insert into bin_blobs (image, blob, blobID) values(?,?,?)");
       stmt.setString(1, image);
       stmt.setObject(2, b);
       stmt.setInt(3, blobID);
       stmt.execute();*/


      stmt = j.prepareStatement(pixelQuery);
      stmt.setInt(3, blobID);
      Enumeration e = b.pixels.elements();
      String InsertQuery = "insert into pixels(x,y,BlobId) values(";
      int ctr = 0;
      while (e.hasMoreElements()) {

         pixel thisOne = (pixel) e.nextElement();

         stmt.setInt(1, thisOne.x);
         stmt.setInt(2, thisOne.y);
         stmt.execute();
      }





   }

   /**
    * Write the coordinate pairs for all black pixels in this blob in an xml format that can be read later
    */
   public static void writeBlob(BufferedWriter w, blob b) throws IOException {
      if (b.pixels.size() < 25) {
         return;
      }
      if (b.pixels.size() > 2000) {
         return;
      }
      //save the original location of the blob within its image
      w.append("b<sx>" + b.x + "</sx><sy>" + b.y + "</sy>\n");
      Enumeration e = b.pixels.elements();
      while (e.hasMoreElements()) {
         pixel p = (pixel) e.nextElement();
         w.append(+p.x + "," + p.y + "\n");
      }

      w.flush();
   }

   /**
    * Rather than writing coordinate pairs, write the dimensions of the blob canvas and 1 for black 0 for
    * white, this reads in much faster than coordinate pairs
    */
   public static void writeMatrixBlob(BufferedWriter w, blob b) throws IOException {
      /**
       * if(b.pixels.size()<25) return; if(b.pixels.size()>4000)
        return;*
       */
      //save the original location of the blob within its image
      w.append("b<sx>" + b.x + "</sx><sy>" + b.y + "</sy>\n");

      int[][] matrix = b.matrixVersion.matrix;
      if (matrix.length <= 0) {
         return;
      }
      w.append("<szx>" + matrix.length + "</szx><szy>" + matrix[0].length + "</szy><id>" + b.id + "</id>\n");
      for (int i = 0; i < matrix.length; i++) {
         for (int j = 0; j < matrix[0].length; j++) {
            w.append(matrix[i][j] + ",");
         }
      }


      w.flush();
   }

   /**
    * Read a comparison assignment from an assignment file
    */
   public static String[] getAssignment(BufferedReader assignmentReader, File statefile) {
      try {
         String buff = assignmentReader.readLine();
         String[] toret = buff.split(" ");
         FileWriter w = new FileWriter(statefile);
         w.write(buff);
         w.flush();
         w.close();
         return toret;

      } catch (IOException ex) {

         return null;
      }

   }

   /**
    * Read a comparison assignment from an assignment file
    */
   public static String[] getAssignment(BufferedReader stateReader, BufferedReader assignmentReader) {
      try {
         String buff = stateReader.readLine();
         String assignment = "";
         while (assignment.compareTo(buff) != 0) {
            assignment = assignmentReader.readLine();
         }
         String[] toret = buff.split(" ");
         return toret;

      } catch (IOException ex) {

         return null;
      }

   }

   /**
    * @Depricated we no longer save these values to a DB
    */
   public static void writeResults(Connection j, int count, String[] imageNames, int ms) throws SQLException {

      try {
         //Connection j=dbWrapperOld.getConnection();
         //PreparedStatement stmt=j.prepareStatement("insert into comparisons (count,image1,image2,ms) values(? ,? ,?,? )");
         PreparedStatement stmt = j.prepareStatement("update comparisons set count=? where image1=? and image2=?");
         //    PreparedStatement stmt=j.prepareStatement("insert into comparisons(count,image1,image2) values(?,?,?)");
         stmt.setInt(1, count);
         stmt.setString(2, imageNames[0]);
         stmt.setString(3, imageNames[1]);
         stmt.setInt(4, ms);
         stmt.execute();
         //j.close();
         return;
      } catch (Exception e) {
         System.out.print("Encountered error sending results. " + e.toString() + "\n. Retrying in 5 seconds...");

      }
      //failed last time, try again.
      try {
         Thread.sleep(4000);
      } catch (InterruptedException ex) {
         Logger.getLogger(blob.class.getName()).log(Level.SEVERE, null, ex);
      }
   }

   public static synchronized void writeMatchResults(String toWrite, FileWriter w) {

      try {

         w.append(toWrite);
         w.flush();
      } catch (IOException ex) {
         ex.printStackTrace();
      }
   }

   public static synchronized void writeMatchResults(int count1, int count2, String[] imageNames, FileWriter w) {

      try {

         w.append(imageNames[0] + ":" + count1 + ";" + imageNames[1] + ":" + count2 + "\n");
         w.flush();
      } catch (IOException ex) {
         ex.printStackTrace();
      }
   }

   public static void writeResults(int count, String[] imageNames, FileWriter w) throws SQLException {
      try {
         w.append(imageNames[0] + ":" + imageNames[1] + ":" + count + "\n");
         System.out.print(imageNames[0] + ":" + imageNames[1] + ":" + count + "\n");
         System.out.flush();
         w.flush();
      } catch (IOException ex) {
         Logger.getLogger(blob.class.getName()).log(Level.SEVERE, null, ex);
      }
   }

   /**
    * Load the blobs associated with a particular image from the database
    */
   /*public static Vector<blob> getBlobs(String image, Connection j) throws SQLException
    {
    Vector <blob> blobs=new Vector();
    PreparedStatement stmt=j.prepareStatement("select id from blobs where image=?");
    stmt.setString(1, image);
    ResultSet rs=stmt.executeQuery();
    while(rs.next())
    {
    PreparedStatement stmt2=j.prepareStatement("select x,y from pixels where blobID=?");
    ResultSet pixelSet=stmt2.executeQuery();
    blob thisOne=new blob();
    while(rs.next())
    {
    pixel thisPixel=new pixel(pixelSet.getInt("x"),pixelSet.getInt("y"));
    thisOne.pixels.push(thisPixel);
    }

    }
    return blobs;
    }*/
   /**
    * Read the blobs stored in a file
    */
   public static Vector<blob> getBlobs(String filename) throws FileNotFoundException, IOException {
      //do this is the blob file is in matrix format rather than coordinate pair format
      if (true) {
         return (getMatrixBlobs(filename));
      }

      Vector<blob> blobs = new Vector();
//if(lastPage!=null)
      //   return lastPage;
      blob thisOne = new blob();
      int maxX = 0;
      int maxY = 0;
      String buff;

      int idCounter = 0;

      String[] stuff = blob.readFileIntoArray(filename); //p.split(readFile(filename.replace(".txt.txt", ".txt")));

      for (int i = 0; i < stuff.length; i++) {
         buff = stuff[i];

         if (buff.contains("b")) {
            if (thisOne != null) {
               thisOne.arrayVersion = thisOne.pixels.toArray(new pixel[thisOne.pixels.size()]);
               thisOne.width = maxX;
               thisOne.height = maxY;
               thisOne.matrixVersion = new matrixBlob(thisOne);
               //thisOne.blockVersion=new BlockBlob(thisOne.matrixVersion);
               thisOne.altVersion = new altBlob(thisOne);
               blobs.add(thisOne);
               thisOne.id = idCounter;
               idCounter++;
               maxX = 0;
               maxY = 0;
            }
            thisOne = new blob();
            try {
               int startx = buff.indexOf("<sx>") + 4;
               int endx = buff.indexOf("</sx>");
               int starty = buff.indexOf("<sy>") + 4;
               int endy = buff.indexOf("</sy>");
               int x = Integer.parseInt(buff.substring(startx, endx));

               int y = Integer.parseInt(buff.substring(starty, endy));
               thisOne.x = x;
               thisOne.y = y;

            } catch (StringIndexOutOfBoundsException e) {
               int k = 0;
            }
         } else {
            String[] parts = comma.split(buff);
            int x = Integer.parseInt(parts[0]);

            int y = Integer.parseInt(parts[1]);
            if (x > maxX) {
               maxX = x;
            }
            if (y > maxY) {
               maxY = y;
            }
            thisOne.pixels.add(new pixel(x, y));
            thisOne.size++;
         }

      }
      //System.out.print(blobs.size() + " " + idCounter);
      lastPage = blobs;
      return blobs;
   }

   /**
    * Read all blobs from the file. The file is in matrix format rather than coordinate pair format
    */
   public static Vector<blob> getMatrixBlobs(String filename) throws FileNotFoundException, IOException {
      try {

         Vector<blob> blobs = new Vector();
//if(lastPage!=null)
         //   return lastPage;
         blob thisOne = null;
         int maxX = 0;
         int maxY = 0;
         String buff;
//BufferedImage img=imageHelpers.readAsBufferedImage("/usr/blankimg.jpg");
         String fileName = filename.split("/")[filename.split("/").length - 1];
//img=imageHelpers.scale(img,2500);
         int idCounter = 0;

         String[] stuff = blob.readFileIntoArray(filename); //p.split(readFile(filename.replace(".txt.txt", ".txt")));
         int[][] matrix = null;

         for (int i = 0; i < stuff.length; i++) {
            buff = stuff[i];

            if (buff.contains("b")) {
               if (thisOne != null) {

                  thisOne.arrayVersion = thisOne.pixels.toArray(new pixel[thisOne.pixels.size()]);
                  thisOne.width = matrix.length;
                  thisOne.height = matrix[0].length;
                  thisOne.matrixVersion = new matrixBlob(matrix, thisOne.size);
                  //thisOne.blockVersion=new BlockBlob(thisOne.matrixVersion);
                  //thisOne.matrixVersion.drawBlob(img, thisOne.x, thisOne.y, 0xff0000);
                  thisOne.altVersion = new altBlob(thisOne);
                  blobs.add(thisOne);
                  thisOne.id = blobs.size() - 1;

                  idCounter++;
                  maxX = 0;
                  maxY = 0;
               }
               thisOne = new blob();
               try {
                  int startx = buff.indexOf("<sx>") + 4;
                  int endx = buff.indexOf("</sx>");
                  int starty = buff.indexOf("<sy>") + 4;
                  int endy = buff.indexOf("</sy>");
                  int x = Integer.parseInt(buff.substring(startx, endx));

                  int y = Integer.parseInt(buff.substring(starty, endy));
                  thisOne.x = x;
                  thisOne.y = y;

               } catch (StringIndexOutOfBoundsException e) {
                  int k = 0;
               }
               //now read the next line, which contains the matrix dimensions
               i++;
               buff = stuff[i];
               int startx = buff.indexOf("<szx>") + 5;
               int endx = buff.indexOf("</szx>");
               int starty = buff.indexOf("<szy>") + 5;
               int endy = buff.indexOf("</szy>");
               int startid = buff.indexOf("<id>") + 4;
               int endid = buff.indexOf("</id>");
               int id = Integer.parseInt(buff.substring(startid, endid));
               int sizeX = Integer.parseInt(buff.substring(startx, endx));
               thisOne.id = id;
               int sizeY = Integer.parseInt(buff.substring(starty, endy));
               matrix = new int[sizeY][sizeX];
               i++;
               buff = stuff[i];
               String[] parts = comma.split(buff);
               for (int ctr = 0; ctr < parts.length; ctr++) {
                  if (parts[ctr].compareTo("1") == 0) {
                     //thisOne.pixels.add(new pixel(ctr/sizeY,ctr-ctr/sizeY));
                     thisOne.size++;
                     matrix[(ctr % sizeY)][ctr / sizeY] = 1;
                  }
               }

            }




         }
         //System.out.print(blobs.size() + " " + idCounter);
         //imageHelpers.writeImage(img, "/usr/blobimgs/"+ fileName +".jpg");
         lastPage = blobs;
         return blobs;
      } catch (Exception e) {
         e.printStackTrace();
      }
      return null;

   }

   /**
    * Quick method for reading a file in and spliting by line
    */
   public static String[] readFileIntoArray(String file) {
      String[] toret = null;

      Vector<String> v = new Vector();
      try {
         BufferedReader b = new BufferedReader(new FileReader(new File(file)));
         while (b.ready()) {
            v.add(b.readLine());
         }
      } catch (IOException ex) {
         Logger.getLogger(blob.class.getName()).log(Level.SEVERE, null, ex);
      }
      toret = new String[v.size()];
      for (int i = 0; i < toret.length; i++) {
         toret[i] = v.get(i);
      }
      return toret;
   }

   /**
    * Faster way to read the entire data file in 1 go, courtesy of stackoverflow
    */
   private static String readFile(String path) throws IOException {
      FileInputStream stream = new FileInputStream(new File(path));
      try {
         FileChannel fc = stream.getChannel();
         MappedByteBuffer bb = fc.map(FileChannel.MapMode.READ_ONLY, 0, fc.size());
         /* Instead of using default, pass in a decoder. */
         return Charset.defaultCharset().decode(bb).toString();
      } finally {
         stream.close();
      }
   }

   /**
    * @Depricated This is left over fromt he time when we ran this as a distributed computing project
    */
   public static void downloadFile(String urlString, String destination) throws MalformedURLException, IOException {
      URL url = new URL(urlString);
      url.openConnection();
      URLConnection conn = url.openConnection();


      // Read all the text returned by the server
      InputStream in = conn.getInputStream();
      File dest = new File(destination);
      if (dest.exists()) {
         dest.delete();
      }
      BufferedOutputStream tmpOut = new BufferedOutputStream(new FileOutputStream(destination));
      int total = conn.getContentLength() / 1000000;
      int done = 0;
      int doneMB = -1;
      byte[] bytes = new byte[4096];

      String str = "";
      while (true) {
         int len = in.read(bytes);
         if (len == -1) {
            break;
         }
         tmpOut.write(bytes, 0, len);
         done += len;

         if (doneMB < done / 1000000) {
            doneMB = done / 1000000;

            System.out.print("" + doneMB + "mb / " + total + "\n");
         }

         tmpOut.flush();
      }
      in.close();
   }

   /**
    * @Depricated This is left over fromt he time when we ran this as a distributed computing project
    */
   public static void unzipFile(String fileLoc, String dataPath) throws FileNotFoundException, IOException {
      int BUFFER = 4096;
      BufferedOutputStream dest = null;
      FileInputStream fis = new FileInputStream(fileLoc);
      ZipInputStream zis = new ZipInputStream(new BufferedInputStream(fis));
      ZipEntry entry;
      while ((entry = zis.getNextEntry()) != null) {
         System.out.println("Extracting: " + entry);
         int count;
         byte data[] = new byte[BUFFER];
         // write the files to the disk
         FileOutputStream fos = new FileOutputStream(dataPath + entry.getName());
         dest = new BufferedOutputStream(fos, BUFFER);
         while ((count = zis.read(data, 0, BUFFER))
                 != -1) {
            dest.write(data, 0, count);
         }
         dest.flush();
         dest.close();
      }
      zis.close();
   }

   public int getWidth() {

      return width;
   }

   public int getHeight() {

      return height;
   }
   static Vector<blob> lastPage;
}
