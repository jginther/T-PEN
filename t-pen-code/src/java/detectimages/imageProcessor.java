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

import static edu.slu.util.ImageUtils.cloneImage;
import java.awt.image.BufferedImage;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Vector;
import javax.media.jai.JAI;
import javax.media.jai.PlanarImage;
import imageLines.ImageHelpers;


public class imageProcessor implements Runnable {

   private BufferedWriter records;
   private File imageFile;
   private int maxHeight;
   private String dataPath;
   private BufferedImage orig;
   private BufferedImage untouchedImage;

   public String getDataPath() {
      return dataPath;
   }

   public void setDataPath(String dataPath) {
      this.dataPath = dataPath;
   }

   /**
    * Constructor for use in batch processing of images, line detection and character extraction
    *
    * @param w writer where processing record can go
    * @param img image that hasnt undergone preprocessing
    * @param h maximum height for images, standard is 1000
    * @param data location to store results
    */
   public imageProcessor(BufferedWriter w, File img, int h, String data) {
      records = w;
      imageFile = img;
      maxHeight = h;
      dataPath = data;

   }

   /**
    * Constructor for use in realtime line detection.
    *
    * @param img
    * @param h
    */
   public imageProcessor(BufferedImage img, int h) {
      records = null;

      this.untouchedImage = img;
      this.orig = img;
      maxHeight = h;
      dataPath = "";

   }
   static int thresholdMethod = 0;

   public static void setThesholdMethod(int meth) {
      thresholdMethod = meth;
   }

   public void doLines(File image, String ms) throws IOException {
      if (image.getName().contains(".jpg")) {
         System.out.print("Starting " + image.getName() + "\n");
         BufferedImage img = ImageHelpers.readAsBufferedImage(image.getPath());
         if (img.getHeight() != 1000) {
            int width = (int) (1000 / (double) img.getHeight() * img.getWidth());
            img = ImageHelpers.scale(img, 1000, width);
         }
         BufferedImage bin = ImageHelpers.binaryThreshold(img, 0);
         Detector j = new Detector(img, bin);
         j.graphical = true;
         j.smeared = bin;
         j.detect();
         FileWriter writer = new FileWriter(new File("queries/" + ms + "/" + image.getName()));
         System.out.print(":" + image.getName() + ":" + ":" + j.lines.size() + "\n");
         for (int k = 0; k < j.lines.size(); k++) {
            j.lines.get(k).commitQuery(writer, image.getName());
            int height = j.lines.get(k).getStartVertical();
            int w = j.lines.get(k).getWidth();
            int start = j.lines.get(k).getStartHorizontal();
            for (int l = start; l < start + w; l++) {
               img.setRGB(l, height, 0xFF0000);
            }
         }
         writer.flush();
         writer.close();
      }
   }

   /**
    * Processes images performing line segmentation and optional character blob extratction. Intended for
    * multithreaded runs.
    */
   public void run() {
      System.out.print(Thread.currentThread().getId() + "\n");
      try {
         try {
            File outfile = new File(dataPath + imageFile.getName().replace("jpg", "txt"));
            if (outfile.exists()) {
               return;
            }
            String ms = "nada";
            records.write("<");
            records.write(imageFile.getName() + ">\n");
            BufferedImage bin = ImageHelpers.readAsBufferedImage(imageFile.getAbsolutePath());
            bin = ImageHelpers.scale(bin, maxHeight);
            orig = ImageHelpers.readAsBufferedImage(imageFile.getAbsolutePath());
            orig = ImageHelpers.scale(orig, maxHeight);
            BufferedImage stored = cloneImage(ImageHelpers.scale(orig, maxHeight));
            BufferedImage copy = ImageHelpers.readAsBufferedImage(imageFile.getAbsolutePath());
            copy = ImageHelpers.scale(copy, maxHeight);
            records.flush();
            System.out.println(imageFile.getName());
            PlanarImage threshedImage = JAI.create("fileload", imageFile.getAbsolutePath());
            BufferedImage ok = threshedImage.getAsBufferedImage();
            bin = ImageHelpers.scale(ImageHelpers.binaryThreshold(ok, 4), maxHeight);
            for (int i = 0; i < bin.getWidth(); i++) {
               for (int j = 0; j < bin.getHeight(); j++) {
                  orig.setRGB(i, j, -1);
               }
            }
            Boolean doBlobExtract = true;
            if (doBlobExtract) {
               for (int i = 0; i < bin.getWidth(); i++) {
                  for (int j = 0; j < bin.getHeight(); j++) {
                     orig.setRGB(i, j, -1);
                  }
               }
               //ImageHelpers.writeImage(bin, "/usr/web/broken/" + imageFile.getName() + "bin" + ".jpg");
               Vector<blob> blobs = new Vector<blob>();
               for (int i = 0; i < bin.getWidth(); i++) {
                  for (int j = 0; j < bin.getHeight(); j++) {
                     if (bin.getRGB(i, j) != -1) {
                        blob thisOne = new blob(i, j);
                        thisOne.copy = orig;
                        if (blobs.size() % 3 == 0) {
                           thisOne.color = 0xcc0000;
                        }
                        if (blobs.size() % 3 == 1) {
                           thisOne.color = 0x000099;
                        }
                        if (blobs.size() % 3 == 2) {
                           thisOne.color = 0x006600;
                        }
                        thisOne.count(bin, thisOne.getX(), thisOne.getY());
                        if (thisOne.size > 5) {
                           blobs.add(thisOne);
                           if ((thisOne.getSize() - (thisOne.getSize() % 10)) != 5000 && (thisOne.getSize() - (thisOne.getSize() % 10)) != 0) {

                              thisOne.calculateRelativeCoordinates();
                              thisOne.drawBlob(orig, thisOne.color);

                           }
                        }
                     }
                  }
               }
               BufferedWriter blobWriter = new BufferedWriter(new FileWriter(outfile));

               int ctr = 1;
               for (int i = 0; i < blobs.size(); i++) {
                  if ((blobs.get(i).size < 4000)) {
                     try {
                        blobs.get(i).id = ctr;
                        ctr++;
                        //blobs.get(i).color=0x000000;
                        blobs.get(i).arrayVersion = blobs.get(i).pixels.toArray(new pixel[blobs.get(i).pixels.size()]);
                        int maxX = 0;
                        for (int k = 0; k < blobs.get(i).arrayVersion.length; k++) {
                           if (blobs.get(i).arrayVersion[k].x > maxX) {
                              maxX = blobs.get(i).arrayVersion[k].x;
                           }
                        }
                        blobs.get(i).width = maxX;
                        int maxY = 0;
                        for (int k = 0; k < blobs.get(i).arrayVersion.length; k++) {
                           if (blobs.get(i).arrayVersion[k].y > maxY) {
                              maxY = blobs.get(i).arrayVersion[k].y;
                           }
                        }
                        blobs.get(i).height = maxY;


                        blobs.get(i).matrixVersion = new matrixBlob(blobs.get(i));
                     } catch (Exception e) {
                        e.printStackTrace();
                     }
                     // blob.writeBlob(blobWriter, blobs.get(i));
                     blob.writeMatrixBlob(blobWriter, blobs.get(i));
                     blobs.set(i, null);//.matrixVersion=null;
                     //blobs.get(i).arrayVersion=null;
                     // blob.writeBlob(blobWriter, blobs.get(i));
                     //blob.drawBlob(orig, blobs.get(i).x, blobs.get(i).y, blobs.get(i), 0x000000);
                  }
               }
               System.out.print("found " + ctr + " blobs\n");

            }

            try {


               if (!doBlobExtract) {
                  orig = bin;
               }
               //ImageHelpers.writeImage(orig, "/usr/web/broken/" + imageFile.getName() + "broken" + ".jpg");

               Vector<line> lines = new Vector();
               createViewableVerticalProfile(orig, imageFile.getName(), lines);
               //ImageHelpers.writeImage(ImageHelpers.createViewableVerticalProfile(orig, imageFile.getName(), lines), "/usr/web/broken/profile" + imageFile.getName() + "broken" + ".jpg");
               FileWriter writer = null;
               //writer = new FileWriter(new File("/usr/web/queries/" + imageFile.getName() + ""));
               orig = ImageHelpers.scale(orig, 1000);
               Detector d = new Detector(orig, orig);
               if (lines.size() > 3) {
                  lines = new Vector();
                  lines.add(new line());
                  lines.get(0).setStartHorizontal(0);
                  lines.get(0).setStartVertical(0);
                  lines.get(0).setWidth(orig.getWidth());
                  lines.get(0).setDistance(maxHeight);
               }
               //d.detect();
               for (int i = 0; i < lines.size(); i++) {
                  line col = lines.get(i);
                  BufferedImage storedBin = ImageHelpers.binaryThreshold(stored, 4);
                  BufferedImage colOnly = storedBin.getSubimage(col.getStartHorizontal(), col.getStartVertical(), col.getWidth(), col.getDistance());
                  d = new Detector(colOnly, colOnly);
                  d.debugLabel = imageFile.getName();
                  d.forceSingle = true;
                  d.detect();
                  System.out.print("total lines in col is " + d.lines.size() + "\n");
                  for (int j = 0; j < d.lines.size(); j++) {
                     line r = d.lines.get(j);
                     r.setStartHorizontal(r.getStartHorizontal() + col.getStartHorizontal());
                     r.setStartVertical(r.getStartVertical() + col.getStartVertical());
                     //r.commitQuery(writer, imageFile.getName());
                     if (j % 2 == 1) {
                        int color = 0x0000ff;
                        stored = highlightBlock(stored, r.getStartHorizontal(), r.getStartVertical() - r.getDistance(), r.getDistance(), r.getWidth(), color);
                     } else {
                        int color = 0xff0000;
                        stored = highlightBlock(stored, r.getStartHorizontal(), r.getStartVertical() - r.getDistance(), r.getDistance(), r.getWidth(), color);
                     }
                  }
               }
               //ImageHelpers.writeImage(stored, "" + "/usr/web/processed/" + imageFile.getName());



               //writer.close();
            } catch (Exception e) {
               e.printStackTrace();
            }
         } catch (IOException ex) {
            System.out.print(ex.getMessage() + "\n");
         }
      } catch (Exception e) {
         e.printStackTrace();
      }
      return;
   }

   public void run2(String outfilename) {
      try {
         try {
            File outfile = new File(dataPath + outfilename);
            if (outfile.exists()) {
               return;
            }

            BufferedImage bin = cloneImage(orig);
            bin = ImageHelpers.scale(bin, maxHeight);
            orig = ImageHelpers.scale(orig, maxHeight);
            BufferedImage stored = cloneImage(ImageHelpers.scale(orig, maxHeight));

            BufferedImage copy = cloneImage(orig);


            PlanarImage threshedImage = PlanarImage.wrapRenderedImage(orig);
            BufferedImage ok = threshedImage.getAsBufferedImage();
            bin = ImageHelpers.scale(ImageHelpers.binaryThreshold(ok, 4), maxHeight);
            for (int i = 0; i < bin.getWidth(); i++) {
               for (int j = 0; j < bin.getHeight(); j++) {
                  orig.setRGB(i, j, -1);
               }
            }
            Boolean doBlobExtract = true;
            if (doBlobExtract) {
               for (int i = 0; i < bin.getWidth(); i++) {
                  for (int j = 0; j < bin.getHeight(); j++) {
                     orig.setRGB(i, j, -1);
                  }
               }
               //ImageHelpers.writeImage(bin, "/usr/web/broken/" + imageFile.getName() + "bin" + ".jpg");
               Vector<blob> blobs = new Vector<blob>();
               for (int i = 0; i < bin.getWidth(); i++) {
                  for (int j = 0; j < bin.getHeight(); j++) {
                     if (bin.getRGB(i, j) != -1) {
                        blob thisOne = new blob(i, j);
                        thisOne.copy = orig;
                        if (blobs.size() % 3 == 0) {
                           thisOne.color = 0xcc0000;
                        }
                        if (blobs.size() % 3 == 1) {
                           thisOne.color = 0x000099;
                        }
                        if (blobs.size() % 3 == 2) {
                           thisOne.color = 0x006600;
                        }
                        thisOne.count(bin, thisOne.getX(), thisOne.getY());
                        if (thisOne.size > 25) {
                           blobs.add(thisOne);
                           if ((thisOne.getSize() - (thisOne.getSize() % 10)) != 5000 && (thisOne.getSize() - (thisOne.getSize() % 10)) != 0) {

                              thisOne.calculateRelativeCoordinates();
                              thisOne.drawBlob(orig, thisOne.color);

                           }
                        }
                     }
                  }
               }
               BufferedWriter blobWriter = new BufferedWriter(new FileWriter(outfile));
               int ctr = 1;

               for (int i = 0; i < blobs.size(); i++) {
                  if ((blobs.get(i).size < 4000)) {
                     try {
                        blobs.get(i).id = ctr;
                        ctr++;
                        //blobs.get(i).color=0x000000;
                        blobs.get(i).arrayVersion = blobs.get(i).pixels.toArray(new pixel[blobs.get(i).pixels.size()]);
                        int maxX = 0;
                        for (int k = 0; k < blobs.get(i).arrayVersion.length; k++) {
                           if (blobs.get(i).arrayVersion[k].x > maxX) {
                              maxX = blobs.get(i).arrayVersion[k].x;
                           }
                        }
                        blobs.get(i).width = maxX;
                        int maxY = 0;
                        for (int k = 0; k < blobs.get(i).arrayVersion.length; k++) {
                           if (blobs.get(i).arrayVersion[k].y > maxY) {
                              maxY = blobs.get(i).arrayVersion[k].y;
                           }
                        }
                        blobs.get(i).height = maxY;


                        blobs.get(i).matrixVersion = new matrixBlob(blobs.get(i));
                     } catch (Exception e) {
                        e.printStackTrace();
                     }
                     // blob.writeBlob(blobWriter, blobs.get(i));
                     blobs.get(i).id = ctr;


                     blob.writeMatrixBlob(blobWriter, blobs.get(i));
                     blobs.set(i, null);//.matrixVersion=null;
                     //blobs.get(i).arrayVersion=null;
                     // blob.writeBlob(blobWriter, blobs.get(i));
                     //blob.drawBlob(orig, blobs.get(i).x, blobs.get(i).y, blobs.get(i), 0x000000);
                  }
               }
               System.out.print("found " + ctr + " blobs\n");
            }

            try {


               if (!doBlobExtract) {
                  orig = bin;
               }
               //ImageHelpers.writeImage(orig, "/usr/web/broken/" + imageFile.getName() + "broken" + ".jpg");

               Vector<line> lines = new Vector();
               createViewableVerticalProfile(orig, outfilename, lines);
               //ImageHelpers.writeImage(ImageHelpers.createViewableVerticalProfile(orig, imageFile.getName(), lines), "/usr/web/broken/profile" + imageFile.getName() + "broken" + ".jpg");
               FileWriter writer = null;
               //writer = new FileWriter(new File("/usr/web/queries/" + imageFile.getName() + ""));
               orig = ImageHelpers.scale(orig, 1000);
               Detector d = new Detector(orig, orig);
               if (lines.size() > 3) {
                  lines = new Vector();
                  lines.add(new line());
                  lines.get(0).setStartHorizontal(0);
                  lines.get(0).setStartVertical(0);
                  lines.get(0).setWidth(orig.getWidth());
                  lines.get(0).setDistance(maxHeight);
               }
               //d.detect();
               for (int i = 0; i < lines.size(); i++) {
                  line col = lines.get(i);
                  BufferedImage storedBin = ImageHelpers.binaryThreshold(stored, 4);
                  BufferedImage colOnly = storedBin.getSubimage(col.getStartHorizontal(), col.getStartVertical(), col.getWidth(), col.getDistance());
                  d = new Detector(colOnly, colOnly);
                  d.debugLabel = outfilename;
                  d.forceSingle = true;
                  d.detect();
                  System.out.print("total lines in col is " + d.lines.size() + "\n");
                  for (int j = 0; j < d.lines.size(); j++) {
                     line r = d.lines.get(j);
                     r.setStartHorizontal(r.getStartHorizontal() + col.getStartHorizontal());
                     r.setStartVertical(r.getStartVertical() + col.getStartVertical());
                     //r.commitQuery(writer, imageFile.getName());
                     if (j % 2 == 1) {
                        int color = 0x0000ff;
                        stored = highlightBlock(stored, r.getStartHorizontal(), r.getStartVertical() - r.getDistance(), r.getDistance(), r.getWidth(), color);
                     } else {
                        int color = 0xff0000;
                        stored = highlightBlock(stored, r.getStartHorizontal(), r.getStartVertical() - r.getDistance(), r.getDistance(), r.getWidth(), color);
                     }
                  }
               }
               //ImageHelpers.writeImage(stored, "" + "/usr/web/processed/" + imageFile.getName());



               //writer.close();
            } catch (Exception e) {
               e.printStackTrace();
            }
         } catch (IOException ex) {
            System.out.print(ex.getMessage() + "\n");
         }
      } catch (Exception e) {
         e.printStackTrace();
      }
      return;
   }

   /**
    * Perform column and line detection on the image
    *
    * @param doBlobExtract Use an additional preprocessing method that looks for likely characters, and only
    * uses them int he column/line detection to the exclusion of all other content in the image. Best for
    * microfilm images.
    * @return Vector containing all lines, null in case of unrecoverable error
    */
   public Vector<line> detectLines(Boolean doBlobExtract) {

      BufferedImage bin = cloneImage(untouchedImage);
      bin = ImageHelpers.scale(bin, maxHeight);
      orig = cloneImage(untouchedImage);
      orig = ImageHelpers.scale(orig, maxHeight);
      BufferedImage stored = cloneImage(ImageHelpers.scale(orig, 1000));
      BufferedImage copy = cloneImage(untouchedImage);
      copy = ImageHelpers.scale(copy, maxHeight);
      // PlanarImage threshedImage = JAI.create("fileload", imageFile.getAbsolutePath());
      BufferedImage ok = cloneImage(untouchedImage);
      bin = ImageHelpers.scale(ImageHelpers.binaryThreshold(ok, 4), maxHeight);
      for (int i = 0; i < bin.getWidth(); i++) {
         for (int j = 0; j < bin.getHeight(); j++) {
            orig.setRGB(i, j, -1);
         }
      }

      if (doBlobExtract) {
         for (int i = 0; i < bin.getWidth(); i++) {
            for (int j = 0; j < bin.getHeight(); j++) {
               orig.setRGB(i, j, -1);
            }
         }
         Vector<blob> blobs = new Vector<blob>();
         for (int i = 0; i < bin.getWidth(); i++) {
            for (int j = 0; j < bin.getHeight(); j++) {
               if (bin.getRGB(i, j) != -1) {
                  blob thisOne = new blob(i, j);
                  thisOne.copy = orig;
                  if (blobs.size() % 3 == 0) {
                     thisOne.color = 0xcc0000;
                  }
                  if (blobs.size() % 3 == 1) {
                     thisOne.color = 0x000099;
                  }
                  if (blobs.size() % 3 == 2) {
                     thisOne.color = 0x006600;
                  }
                  thisOne.count(bin, thisOne.getX(), thisOne.getY());
                  if (thisOne.size > 5) {
                     blobs.add(thisOne);
                     if ((thisOne.getSize() - (thisOne.getSize() % 10)) != 5000 && (thisOne.getSize() - (thisOne.getSize() % 10)) != 0) {

                        thisOne.calculateRelativeCoordinates();
                        thisOne.drawBlob(orig, thisOne.color);

                     }
                  }
               }
            }
         }

         for (int i = 0; i < blobs.size(); i++) {
            if ((blobs.get(i).size < 4000)) {
               blob.drawBlob(orig, blobs.get(i).x, blobs.get(i).y, blobs.get(i), 0x000000);
            }
         }

      }

      try {


         if (!doBlobExtract) {
            orig = bin;
         }


         Vector<line> lines = new Vector();
         createViewableVerticalProfile(orig, "", lines);
         FileWriter writer = null;

         orig = ImageHelpers.scale(orig, 1000);
         Detector d = new Detector(orig, orig);
         if (lines.size() > 3) {
            lines = new Vector();
            lines.add(new line());
            lines.get(0).setStartHorizontal(0);
            lines.get(0).setStartVertical(0);
            lines.get(0).setWidth(orig.getWidth());
            lines.get(0).setDistance(maxHeight);
         }
         //d.detect();
         Vector<line> allLines = new Vector();
         for (int i = 0; i < lines.size(); i++) {
            line col = lines.get(i);
            BufferedImage storedBin = ImageHelpers.binaryThreshold(stored, 4);
            BufferedImage colOnly = storedBin.getSubimage(col.getStartHorizontal(), col.getStartVertical(), col.getWidth(), col.getDistance());
            d = new Detector(colOnly, colOnly);
            //d.debugLabel = imageFile.getName();
            d.forceSingle = true;
            d.detect();
            System.out.print("total lines in col is " + d.lines.size() + "\n");
            for (int j = 0; j < d.lines.size(); j++) {
               line r = d.lines.get(j);
               allLines.add(r);
               r.setStartHorizontal(r.getStartHorizontal() + col.getStartHorizontal());
               r.setStartVertical(r.getStartVertical() + col.getStartVertical());
//                        r.commitQuery(writer, imageFile.getName());
               if (j % 2 == 1) {
                  int color = 0x0000ff;
                  stored = highlightBlock(stored, r.getStartHorizontal(), r.getStartVertical() - r.getDistance(), r.getDistance(), r.getWidth(), color);
               } else {
                  int color = 0xff0000;
                  stored = highlightBlock(stored, r.getStartHorizontal(), r.getStartVertical() - r.getDistance(), r.getDistance(), r.getWidth(), color);
               }
            }
         }
         //ImageHelpers.writeImage(stored, "" + "/usr/web/processed/" + imageFile.getName());


         //ImageHelpers.writeImage(orig, "/usr/web/broken/" + imageFile.getName() + "broken" + ".jpg");
         return allLines;
      } catch (Exception e) {
         e.printStackTrace();
      }
      return null;
   }

   /**
    * Detect the columns in a binarized image
    *
    * @param bin Binarized version of the image
    * @param fileName Filename to be used for saving debug information
    * @param v Vector to store detected columns
    * @return
    */
   private static BufferedImage createViewableVerticalProfile(BufferedImage bin, String fileName, List<line> v) {
      int white = 0xff000000;
      int black = 0xffffffff;
      BufferedImage toret = cloneImage(bin);
      int[] vals = new int[toret.getWidth()];
      for (int i = 0; i < vals.length; i++) {
         vals[i] = 0;
      }
      for (int i = 0; i < toret.getWidth(); i++) {
         for (int j = 0; j < toret.getHeight(); j++) {
            if (toret.getRGB(i, j) == white) {
               vals[i]++;
            }
         }
      }
      int[] ordered = Arrays.copyOf(vals, vals.length);
      Arrays.sort(ordered);
      int x = 0;
      List<line> lines = new ArrayList<>();
      int median = ordered[ordered.length / 6];
      median = median + median / 5;
      for (int i = 0; i < vals.length; i++) {
         if (vals[i] > median) {
            for (int j = 0; j < vals[i]; j++) {
               toret.setRGB(i, j, black);
            }
            for (int j = vals[i]; j < toret.getHeight(); j++) {
               toret.setRGB(i, j, white);
            }
         } else {
            for (int j = 0; j < toret.getHeight(); j++) {
               toret.setRGB(i, j, white);
            }
         }
      }
      for (int i = 0; i < vals.length; i++) {
         if (vals[i] <= median) {
            // w.append("0\n");
            if (x > 0) {
               line l = new line();
               l.setStartHorizontal(x);
               l.setWidth(i - x);
               lines.add(l);
               x = 0;
            }
         } else {
            // w.append(""+vals[i]+"\n");
            if (x == 0) {
               x = i;
            }
            if (i == vals.length - 1) {
               line l = new line();
               l.setStartHorizontal(x);
               l.setWidth(i - x);
               lines.add(l);
               x = 0;
            }
         }
      }

      int minimalColumnFraction = 8;
      for (int j = 0; j < lines.size(); j++) {
         line l = lines.get(j);
         //merging needs to consider the magnitude of the difference from median
         if (j > 0) {
            line prev = lines.get(j - 1);
            if (l.getStartHorizontal() - prev.getStartHorizontal() - prev.getWidth() < 25 && (l.getWidth() < bin.getWidth() / minimalColumnFraction || prev.getWidth() < bin.getWidth() / minimalColumnFraction || l.getStartHorizontal() - prev.getStartHorizontal() - prev.getWidth() < 3)) {
               prev.setWidth(prev.getWidth() + l.getWidth());
               lines.remove(l);
               j--;
            } else {
               // System.out.print(" gap is "+l.getStartHorizontal()+" "+ prev.getStartHorizontal()+" "+prev.getWidth()+"\n");
            }
         }

      }
      for (int j = 0; j < lines.size(); j++) {
         line l = lines.get(j);
         //if this is a small column try to merge it
         if (l.getWidth() < bin.getWidth() / minimalColumnFraction) {
            /*if(l.getWidth()!=0)
             {
             finalizedLines.add(l);
             l.setStartVertical(0);
             l.setDistance(bin.getHeight()/2);
             imageHelpers.highlightBlock(bin,l.getStartHorizontal(), l.getStartVertical(), l.getDistance(), l.getWidth(), 0x0000ff);
             }*/
         } else {
            v.add(l);
            l.setStartVertical(0);
            l.setDistance(bin.getHeight() - 1);

            highlightBlock(bin, l.getStartHorizontal(), l.getStartVertical(), l.getDistance(), l.getWidth(), 0xff0000);
         }
      }

      return toret;
   }

   /**
    * Highlight a portion of the image im the requested color
    *
    * @param img
    * @param x
    * @param y
    * @param height
    * @param width
    * @param color
    * @return
    */
   private static BufferedImage highlightBlock(BufferedImage img, int x, int y, int height, int width, int color) {

      for (int i = (int) (x); i <= x + width; i++) {
         for (int j = (int) (y); j <= y + height; j++) {
            try {
               if (i == x + width || j == y + height || j == y || i == x) {
                  img.setRGB(i, j, 0x000000);
               } else {
                  img.setRGB(i, j, (int) (color ^ img.getRGB(i, j)));
               }

            } //if there was an out of bounds its because of the size doubling, ignore it
            catch (ArrayIndexOutOfBoundsException e) {
            }
         }
      }

      return img;
   }
}
