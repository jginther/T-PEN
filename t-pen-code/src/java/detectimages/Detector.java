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

import java.awt.image.*;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import static java.lang.Math.abs;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import static edu.slu.util.ImageUtils.cloneImage;


/**
 * This is the column and line detector. It is old, so it has a fair amount of legacy code and some really
 * bad implementation decisions, like using fixed size arrays in some places to store unknown amounts of
 * data.
 */
public class Detector {

   public Boolean threeLines;
   public BufferedImage img;
   public BufferedImage smeared;
   public BufferedImage bin;//binary thresholded version of the image
   public BufferedImage binstor;
   public int[] goodLines; //Array of detected lines of text
   public int[] goodCols;
   public int found;		//number of lines of text
   public int mean_dist;
   public BufferedWriter records;
   public int[] linesTop;
   public int[] colsStart;
   public int vsmearDist;
   public int hsmearDist;
   public int vsmearColDist;
   public int hsmearColDist;
   public Boolean chopped;
   public int[] colLinesWithWidth;
   public int[] colLinesStartPos;
   public int[] colHeight;
   public int startPos;
   public int width;
   public int linectr;
   public int columnExclusionDist;
   public int minLinesPerCol;
   public int subdivisons;
   public int imageFractionForColumn = 40;
   public Vector<line> lines;
   public Vector<line> columns;
   public Hashtable<Integer, Integer> startPositions;
   public Boolean findingCols;
   public Boolean graphical = true;
   public Boolean forceSingle = false;
   public Boolean changed;//This tracks whether the lines vector has changed since last draw, useful for preventing unneeded redraws of a gui
   public String debugLabel = "";
   public int white = 0xffffff;

   public Detector(BufferedImage img, BufferedImage bin) {
      columnExclusionDist = 0;
      lines = new Vector<line>();
      columns = new Vector<line>();
      linectr = 0;
      hsmearDist = 5;
      vsmearDist = 5;
      hsmearColDist = 15;
      vsmearColDist = 15;
      minLinesPerCol = 0;
      threeLines = false;
      changed = true;
      chopped = false;
      subdivisons = 1;
      this.img = img;
      this.bin = bin;
      binstor = new BufferedImage(bin.getWidth(), bin.getHeight(), bin.getType());
      bin.copyData(binstor.getRaster());
      startPositions = new Hashtable<Integer, Integer>();
      smeared = new BufferedImage(img.getWidth(), img.getHeight(), img.getType());
      goodLines = new int[img.getHeight()];
      findingCols = false;
      colLinesWithWidth = new int[2000];
      colLinesStartPos = new int[2000];
      colHeight = new int[2000];
   }
   
   /**
    * Add an additional line to the lines in goodLines
    */
   public void addline(int newline) {
      int j, i;
      System.out.println(found);
      for (i = 0; i < found; i++) {
         if (goodLines[i] == 0) {
            System.out.println("error:line " + i + " is zero");
         }
         if (goodLines[i] >= newline) {
            for (j = found; j >= i; j--) {
               goodLines[j + 1] = goodLines[j];
            }
            goodLines[i] = newline;
            found++;
            return;
         }
      }
      goodLines[i] = newline;
      found++;
   }

   /**
    * Reset the detector state.
    */
   public void zeroLines() {
      try {
         if (goodCols == null) {
            goodCols = new int[2000];
         }
         if (colsStart == null) {
            colsStart = new int[2000];
         }
         if (colLinesWithWidth == null) {
            colLinesWithWidth = new int[2000];
         }
         if (colLinesStartPos == null) {
            colLinesStartPos = new int[2000];
         }
         if (colHeight == null) {
            colHeight = new int[2000];
         }

         lines = new Vector<line>();
         changed = true;
         for (int i = 0; i < goodLines.length && i < linesTop.length; i++) {
            goodLines[i] = 0;
            linesTop[i] = 0;
            goodCols[i] = 0;
            colsStart[i] = 0;
            colLinesWithWidth[i] = 0;
            colLinesStartPos[i] = 0;
            colHeight[i] = 0;
         }
      } catch (Exception e) {
      }
   }

   /**
    * Smear the image horizontally, any areas with pixels close together horizontally (within max_dist
    * pixels of each other) will have the space between them filled in with black.
    */
   public int smear(int max_dist) {
      //if(true)return 1;
      smeared = this.bin.getSubimage(0, 0, this.bin.getWidth(), this.bin.getHeight());
      int thresh = -1700000;
      Boolean found_partner;
      for (int j = 0; j < bin.getHeight(); j++) {
         for (int i = 0; i < img.getWidth() - max_dist; i++) {
            if (this.bin.getRGB(i, j) < thresh) {
               found_partner = false;
               for (int k = max_dist - 1; k > 0; k--) {
                  if ((i + k) > (img.getWidth() - max_dist)) {
                     k = img.getWidth() - i - max_dist;
                  }
                  if (this.bin.getRGB(i + k, j) < thresh) {
                     found_partner = true;
                     for (int l = k; l > 0; l--) {
                        smeared.setRGB(i + l, j, 0x000000);
                     }
                     i += k;
                     k = 0;
                  }
                  //If this pixel didnt get smeared, white it out.
                  if (!found_partner) {
                     smeared.setRGB(i, j, white);
                     i = i + max_dist - 1;
                  }
               }
            }
         }
      }
      return 0;
   }

   /**
    * Smear the image vertically, any areas with pixels close together horizontally (within max_dist
    * pixels of each other) will have the space between them filled in with black.
    */
   public int vsmear(int max_dist, BufferedImage onlySmearedPortions) throws IOException {
      smeared = bin.getSubimage(0, 0, bin.getWidth(), bin.getHeight());
      int thresh = -1700000;


      Boolean found_partner;
      for (int i = 0; i < img.getWidth(); i++) {
         for (int j = 0; j < img.getHeight() - max_dist; j++) {
            if (bin.getRGB(i, j) < thresh) {
               found_partner = false;
               for (int k = max_dist - 1; k > 0; k--) {
                  if ((j + k) > bin.getHeight() - max_dist) {
                     break;
                  }
                  if (bin.getRGB(i, j + k) < thresh) {
                     found_partner = true;
                     for (int l = k; l > 0; l--) {
                        smeared.setRGB(i, j + l, 0x000000);
                        onlySmearedPortions.setRGB(i, j + l, 0x000000);
                     }
                     j += k;
                  }
               }
               //If this pixel didnt get smeared, white it out.
               if (!found_partner) {
                  smeared.setRGB(i, j, white);
                  onlySmearedPortions.setRGB(i, j, white);
                  j = j + max_dist - 1;
               }
            }
         }
      }
      if (debugLabel.length() > 1) {
         ImageIO.write(bin, "jpg", new File("/usr/debugImages/smeared" + debugLabel));
      }
      return 0;
   }

   /**
    * Use this findLines method to subdivide the image that is passed in into subdivisions pieces, find
    * lines in each of them, then join the findings into one set of lines that don't duplicate any lines.
    * thisImage must be binarized but not smeared
    */
   public BufferedImage findLines(int subdivisions, BufferedImage thisImage) {
      //displayImage (thisImage);
      BufferedImage toreturn = cloneImage(thisImage);
      try {
         int[][] results = new int[subdivisions][100];
         int dist = 0;

         for (int i = 0; i < subdivisons; i++) {
            Detector j = new Detector(toreturn.getSubimage(toreturn.getWidth() / subdivisons * i, 0, toreturn.getWidth() / subdivisions, toreturn.getHeight()), toreturn.getSubimage(toreturn.getWidth() / subdivisions * i, 0, toreturn.getWidth() / subdivisions, toreturn.getHeight()));
            j.hsmearDist = hsmearDist;
            j.vsmearDist = vsmearDist;
            j.findLines();
            for (int k = 0; k < j.found; k++) {
               results[i][k] = j.goodLines[k];
               dist = j.mean_dist;
            }
         }
         int[] tmp;
         if (subdivisons == 1) {
            tmp = results[0];
         } else {
            tmp = this.mergeColumnPortions(results[0], results[1], results[2], toreturn.getWidth() / subdivisions);
         }
         for (int i = 0; tmp[i] > 0; i++) {
            lines.add(new line(toreturn.getWidth(), 0, tmp[i], dist));
         }
      } catch (Exception e) {
      }
      //System.out.println(subdivisons+"in");
      return toreturn;
   }

   /**
    * This handles searching for lines within the image, and removes lines that are too close together or
    * too far away to be reasonable. Calculates the mean distance between elements as well.
    */
   public void findLines() {
      try {
         BufferedImage onlySmearedPortions = cloneImage(bin);
         if (true) {
            for (int i = 1; i < smeared.getWidth() - 1; i++) {

               for (int j = 0; j < smeared.getHeight() - 1; j++) {
                  {
                     onlySmearedPortions.setRGB(i, j, white);
                  }
               }
            }
         }
         smear(hsmearDist);
         vsmear(vsmearDist, onlySmearedPortions);
         smear(hsmearDist);
         int i, j;
         long[] means = new long[smeared.getHeight()];
         long meanTabulator;
         found = 0;
         int[] lines = new int[smeared.getHeight()];
         linesTop = new int[smeared.getHeight()];
         long meanOfMeans = 0;
         //Findnegative indicates whether we are looking for a dark area right now or a light area. true=light. Once we find one, start looking for the other
         Boolean findingNegative = true;
         for (i = 1; i < smeared.getHeight() - 1; i++) {
            meanTabulator = 0;
            for (j = 0; j < smeared.getWidth(); j++) {
               meanTabulator += smeared.getRGB(j, i);
            }
            means[i] = meanTabulator / smeared.getWidth();
            meanOfMeans += means[i];
         }
         meanOfMeans = meanOfMeans / i; //mean line height
         for (i = 1; i < smeared.getHeight() - 3; i++) {
            if (findingNegative && means[i] < meanOfMeans && means[i + 1] < meanOfMeans && means[i + 2] < meanOfMeans) {
               findingNegative = !findingNegative;
               linesTop[found] = i;
            } else if (!findingNegative && means[i] > meanOfMeans && means[i + 1] > meanOfMeans && means[i + 2] > meanOfMeans) //If we are looking for a dark area, and this is one, this is a line of text.
            {
               lines[found] = i;
               found++;
               //Just inverts findingNegative
               findingNegative = !findingNegative;
            }
         }
         mean_dist = 0;
         for (i = 1; i < smeared.getWidth() && lines[i] != 0; i++) {
            mean_dist += lines[i] - lines[i - 1];
            found++;
         }
         mean_dist = mean_dist / found;
         found = 0;
         for (i = 0; i < smeared.getWidth() && lines[i] != 0; i++) {
            //use this if to attempt to exclude lines that are very different in height from the norm.
            //if (lines[i + 1] - lines[i] < 4 * mean_dist && lines[i + 1] - lines[i] > mean_dist * .5) {
            goodLines[found] = lines[i];
            linesTop[found] = linesTop[i];
            found++;
            //} else {
            //Can be used to monitor the lines that were excluded by the above criterea
            //System.out.print("skipping "+i+"\n");
            //}
         }
//        goodLines[found] = (int) (lines[i]);
         //       linesTop[found] = linesTop[i];
         //found++;
      } catch (Exception e) {
         System.out.print(e.toString());
      }
   }

   /**
    * Run line and column detection
    */
   public void detect() throws IOException {
      // Create a copy of bin that wont be modified during the column search,
      // so the line search has a clean copy to work on
      BufferedImage onlySmearedPortions = cloneImage(bin);
      for (int i = 1; i < smeared.getWidth() - 1; i++) {
         for (int j = 0; j < smeared.getHeight() - 1; j++) {
            onlySmearedPortions.setRGB(i, j, white);
         }
      }

      if (!debugLabel.isEmpty()) {
         ImageIO.write(bin, "jpg", new File("/usr/debugImages/" + debugLabel + "_binarized.jpg"));
      }
      binstor = cloneImage(bin);//imageHelpers.binaryThreshold(img, 0);

      findingCols = true;
      vsmear(vsmearColDist * 2, bin);
      smear(hsmearColDist);
      vsmear(vsmearColDist * 2, onlySmearedPortions);
      bin = onlySmearedPortions;
      goodCols = new int[2000];
      colsStart = new int[2000];
      int i, j;
      j = 0;
      long[] means = new long[smeared.getWidth()];
      long meanTabulator = 0;
      found = 0;
      int[] cols = new int[smeared.getHeight()];
      colsStart = new int[smeared.getHeight()];
      long meanOfMeans = 0;

      int[] finalizedLines = new int[2000];
      //Findnegative indicates whether we are looking for a dark area right now or a light area. true=light. Once we find one, start looking for the other
      Boolean findingNegative = true;
      //this all calculates the mean pixel color for each hortizonal position

      for (i = 1; i < smeared.getWidth() - 1; i++) {

         meanTabulator = 0;
         for (j = 0; j < smeared.getHeight() - 1; j++) {
            meanTabulator += smeared.getRGB(i, j);
         }
         means[i] = meanTabulator / smeared.getHeight();
         meanOfMeans += means[i];
      }
      meanOfMeans = meanOfMeans / i;
      line l = new line();
      for (i = 1; i < smeared.getWidth() - 3; i++) {
         if (findingNegative && means[i] < meanOfMeans) { //&& means[i + 1] < meanOfMeans
            findingNegative = !findingNegative;
            colsStart[found] = i;
            l = new line();
            l.setStartHorizontal(i);
         } else if (!findingNegative && means[i] > meanOfMeans)//&& means[i + 1] > meanOfMeans) //If we are looking for a dark area, and this is one, this is a line of text.
         {
            cols[found] = i;
            found++;
            l.setWidth(i - l.getStartHorizontal());
            l.setStartVertical(0);
            l.setDistance(bin.getHeight());
            if (l.getWidth() * 5 > bin.getWidth()) //if the col is more than 20% of the image width
            {
               columns.add(l);
               System.out.print("line is:" + l.getStartHorizontal() + "," + l.getWidth() + "," + l.getStartVertical() + "," + l.getDistance() + "\n");
            }
            //Just inverts findingNegative
            findingNegative = !findingNegative;
         }
      }
      /*
       * Is there a column on the left or on the right that is consistent with
       * a portion of the previous or next page being included in the image?
       * If so, crop out that column in both img and bin, and rerun this
       * process.
       */
      //TODO add border column removal

      linectr = 0;

      //Find lines in the left, center, and right of the column.
      int[][] tmp = new int[3][2000];
      int savcolwidth = 0;
      int saveStartPos = 0;
      int fullcolwidth = 0;


      //if no columns were found, treat the image as 1 big column
      Boolean makeSingleCol = false;
      if (columns.size() == 0) {
         makeSingleCol = true;
         System.out.print("Column detection doesnt pass sanity check, forcing single col\n");
      }
      if (forceSingle) {
         makeSingleCol = true;
         System.out.print("Forcing single column by user request\n");
      }
      if (makeSingleCol) {

         columns = new Vector();
         line tmpLine = new line();
         tmpLine.setStartVertical(0);
         tmpLine.setStartHorizontal(0);
         tmpLine.setWidth(bin.getWidth());
         tmpLine.setDistance(bin.getHeight());
         columns.add(tmpLine);
      }

      bin = cloneImage(binstor);
      /*
       * columns = new Vector();
       *
       * for(int i=0;i<potentialColumns.size();i++) { line tmpLine = new
       * line(); rectangle r=potentialColumns.elementAt(i);
       * tmpLine.setStartHorizontal(r.x); tmpLine.setStartVertical(r.y);
       * tmpLine.setWidth(r.x1-r.x); tmpLine.setDistance(r.y1-r.y);
       * columns.add(tmpLine);
       }
       */
      //line l=new line();
      for (int ctr = 0; ctr < columns.size(); ctr++) {
         l = columns.get(ctr);
         //is this a good column or a page margin?
         //if width is > height or the column width is less than 1/8 of the page width exclude it
         if (l.getWidth() < l.getDistance() && l.getWidth() > bin.getWidth() / 8) {
            //System.out.print("column is:" + l.getStartHorizontal() + "," + l.getWidth() + "," + l.getStartVertical() + "," + l.getDistance() + "\n");
            BufferedImage thisColumnOnly = img.getSubimage(l.getStartHorizontal(), l.getStartVertical(), l.getWidth(), l.getDistance());
            BufferedImage thisColumnOnlyBin = binstor.getSubimage(l.getStartHorizontal(), l.getStartVertical(), l.getWidth(), l.getDistance());
            if (debugLabel.compareTo("") != 0) {
               ImageIO.write(thisColumnOnlyBin, "jpg", new File("/usr/debugImages/" + debugLabel + "_col" + ctr + ".jpg"));
            }
            Detector colLines = new Detector(thisColumnOnly, thisColumnOnlyBin);
            colLines.hsmearDist = this.hsmearDist;
            colLines.subdivisons = subdivisons;
            colLines.vsmearDist = this.vsmearDist;
            colLines.findLines(3, thisColumnOnlyBin);
            //System.out.print("found "+ colLines.lines.size()+" lines\n" );

            Iterator<line> e = colLines.lines.iterator();
            if (colLines.lines.size() >= minLinesPerCol) {
               for (int k = 0; k < colLines.lines.size(); k++) {
                  line thisLine = colLines.lines.elementAt(k);
                  thisLine.setStartHorizontal(thisLine.getStartHorizontal() + l.getStartHorizontal());
                  thisLine.setStartVertical(thisLine.getStartVertical() + l.getStartVertical());
                  //System.out.print(thisLine.getWidth()+"\n");
                  lines.add(thisLine);
               }
            }
         }
      }
      if (debugLabel.compareTo("") != 0) {
         ImageIO.write(bin, "jpg", new File("/usr/debugImages/" + debugLabel + "_last_step.jpg"));
      }

      colLinesWithWidth = new int[2000];
      findingCols = false;
      changed = true;
   }

   /**
    * Take the lines from the 3 subcolumns and combines them to create a single array of lines, without duplicates
    */
   private int[] mergeColumnPortions(int[] a, int[] b, int[] c, int colwidth) {
      try {
         FileWriter f = new FileWriter(new File("/usr/log.txt"));
         f.append("Merging with lengths " + a.length + " " + b.length + " and " + c.length + "\n");
      } catch (IOException ex) {
         Logger.getLogger(Detector.class.getName()).log(Level.SEVERE, null, ex);
      }


      int[] newListing = new int[2000];
      int finalLineCount = 0;
      int nexta = 0;
      int nextb = 0;
      int nextc = 0;
      float slope_insanity_num = (float) 75.0;
      /*
       * The lowest number for vertical position is definitely the next line.
       * The trick after finding it, is to determine whether the other 2
       * current line positions are part of the same line, part of a different
       * line, or the entirety of a different line.
       *
       */
      try {
         while (a[nexta] > 0 || b[nextb] > 0 || c[nextc] > 0) {
            int lowest = 99999;
            if (lowest > a[nexta] && a[nexta] > 0) {
               lowest = a[nexta];
            }
            if (lowest > b[nextb] && b[nextb] > 0) {
               lowest = b[nextb];
            }
            if (lowest > c[nextc] && c[nextc] > 0) {
               lowest = c[nextc];
            }
            //calculate the slope the line would have to have for lowest and each of the other 2 points to be on the same line
            //If the slope is silly, dont drop that point when writing this line to the final listing, because it is part of anther line.

            if (lowest == a[nexta]) {
               //slope from a to b is the height difference between a and b divided by the length of a, which is colwidth
               float slopeb = (a[nexta] - b[nextb]) / (float) colwidth;

               float slopec = (a[nexta] - c[nextc]) / ((float) colwidth * 2);

               if (abs(slopeb) < slope_insanity_num) {
                  System.out.println("skipping a b");
                  nextb++;
               } else {
                  System.out.print(abs(slopeb) + "pres\n");
               }
               if (abs(slopec) < slope_insanity_num) {
                  nextc++;
                  System.out.println("skipping a c");
               } else {
                  System.out.print(abs(slopec) + "pres\n");
               }
               newListing[finalLineCount] = a[nexta];
               finalLineCount++;
               nexta++;
            } else if (lowest == b[nextb]) {
               float slopea = (b[nextb] - a[nexta]) / (float) colwidth;

               float slopec = (b[nextb] - c[nextc]) / ((float) colwidth);

               if (abs(slopea) < slope_insanity_num) {
                  nexta++;
               } else {
                  System.out.print(abs(slopea) + "inb\n");
               }

               if (abs(slopec) < slope_insanity_num) {
                  nextc++;
               } else {
                  System.out.print(abs(slopec) + "inb\n");
               }
               newListing[finalLineCount] = b[nextb];
               finalLineCount++;
               nextb++;
            } else {
               float slopea = (c[nextc] - a[nexta]) / ((float) colwidth * 2);

               float slopeb = (c[nextc] - b[nextb]) / ((float) colwidth);


               if (abs(slopea) < slope_insanity_num) {
                  nexta++;
               } else {
                  System.out.print(abs(slopea) + "inc\n");
               }
               if (abs(slopeb) < slope_insanity_num) {
                  nextb++;
               } else {
                  System.out.print(abs(slopeb) + "inc\n");
               }
               newListing[finalLineCount] = c[nextc];
               finalLineCount++;
               nextc++;
            }
         }
         return newListing;
      } catch (Exception e) {
         return newListing;
      }
   }
   
   private static final Logger LOG = Logger.getLogger(Detector.class.getName());
}
