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
import java.util.Vector;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;
import imageLines.ImageHelpers;


public class matrixBlob {

   int xSize, ySize;
   int pixelCount;
   public int[][] matrix;

   public matrixBlob(blob b) {
      pixelCount = b.size;
      matrix = new int[b.getWidth() + 1][b.getHeight() + 1];
      xSize = b.getHeight();
      ySize = b.getWidth();
      // System.out.print("x size is "+xSize+" Y size is "+ySize+"\n");
      for (int i = 0; i < b.arrayVersion.length; i++) {

         matrix[b.arrayVersion[i].x][b.arrayVersion[i].y] = 1;
      }
   }

   public matrixBlob scaleMatrixBlob(blob b, int height) {
      try {

         int[][] orig = b.matrixVersion.matrix;
         double mult = (double) height / orig[0].length;
         int[][] newOne = new int[(int) (mult * orig.length)][height];
         int newPixelCount = 0;
         for (int i = 0; i < newOne.length; i++) {
            for (int j = 0; j < newOne[0].length; j++) {
               try {
                  int f = (int) (i / (float) mult);
                  int p = (int) (j / (float) mult);
                  // System.out.print("mult"+mult+" f:"+f+" p:"+p+" newOne:"+newOne.length+" "+newOne[0].length+"b: "+orig.length+" "+orig[0].length+"\n");
                  if (f > orig.length - 1) {
                     f = orig.length - 1;
                  }
                  if (p > orig[0].length - 1) {
                     p = orig[0].length - 1;
                  }
                  newOne[i][j] = orig[f][p];
                  if (newOne[i][j] == 1) {
                     newPixelCount++;
                  }

               } catch (ArrayIndexOutOfBoundsException e) {
                  System.out.print("error at i=" + i + " j=" + j + "\n");
                  e.printStackTrace();
                  System.out.flush();
               }
            }
         }
         return new matrixBlob(newOne, newPixelCount);
      } catch (Exception e) {
         e.printStackTrace();
      }
      return null;
   }

   public matrixBlob(int[][] mat, int pixelCount) {
      this.matrix = mat;
      this.pixelCount = pixelCount;
   }

   public double compareWithImplicitScaling(matrixBlob b) {
      try {
         double toret = 0.0;
         int lcm = matrixBlob.determineLCM(b.matrix[0].length, this.matrix[0].length);

         int aMultiplier = lcm / this.matrix[0].length;
         int bMultiplier = lcm / b.matrix[0].length;

         int matches = 0;
         int total = 0;
         //matrixA[i][j]=a.matrix[i/aMultiplier][j/aMultiplier];
         for (int i = 0; i < lcm; i++) {
            for (int j = 0; j < matrix.length * aMultiplier && j < b.matrix.length * bMultiplier; j++) {
               matches += (matrix[j / aMultiplier][i / aMultiplier] & b.matrix[j / bMultiplier][i / bMultiplier]);
               total++;
            }
         }
         //System.out.print("matches:"+ matches+" lcm:"+lcm+" b:"+b.matrix.length+"x"+b.matrix[0].length+":"+bMultiplier+ "  a:"+this.matrix.length+"x"+matrix[0].length+":"+aMultiplier+"\n");
         return (double) matches / total;
         /*if(pixelCount>b.pixelCount)
          return (double)matches/(lcm*matrix.length*aMultiplier);
          else
          return (double)matches/(lcm*b.matrix.length*bMultiplier);*/
      } catch (Exception e) {
         e.printStackTrace();
         return 0.0;
      }




   }

   public double compareWithScaling(matrixBlob b) {
      if (Math.abs(this.matrix.length / this.matrix[0].length - b.matrix.length / b.matrix[0].length) > .3) {
         return 0.0;
      }

      try {
         double toret = 0.0;
         matrixBlob[] res = matrixBlob.scaleUsingCommonFactor(this, b);
         if (res.length != 2) {
            return 0.0;
         }
         matrixBlob A = res[0];
         matrixBlob B = res[1];
         //A.writeGlyph("b"+System.currentTimeMillis(), B);
         toret = A.compareTo(B);
         /*if(toret>.6){
          A.writeGlyph("b"+System.currentTimeMillis(), B);
          B.writeGlyph("b"+System.currentTimeMillis(), A);
          }*/
         //this.writeGlyph("bl"+this.pixelCount+"_"+toret+"_", b);
         return toret;
      } catch (Exception e) {
         e.printStackTrace();
      }
      return 0.0;

   }

   public double compareTo(matrixBlob b) {
      try {
         int matches = 0;
         for (int i = 0; i < matrix.length && i < b.matrix.length; i++) {
            for (int j = 0; j < matrix[0].length && j < b.matrix[0].length; j++) {
               matches += (matrix[i][j] & b.matrix[i][j]);
            }
         }


         if (pixelCount > b.pixelCount) {
            return (double) matches / pixelCount;
         } else {
            return (double) matches / b.pixelCount;
         }
      } catch (Exception e) {
         e.printStackTrace();
      }
      return 0.0;
   }

   public void drawBlob(BufferedImage img, int x, int y, int color) {
      //img=imageHelpers.scale(img, matrix[0].length*2);
      for (int i = 0; i < matrix.length; i++) {
         for (int j = 0; j < matrix[0].length; j++) {
            if (matrix[i][j] == 1) {
               img.setRGB(x + j, y + i, color);
            }
         }
      }
   }

   public double compareToWithAdjust(matrixBlob b) {
      if (b.matrix.length == 0) {
         return 0.0;
      }
      if (matrix.length == 0) {
         return 0.0;
      }
      try {
         int matches = 0;
         int matches2 = 0;

         int bestMatch = 0;
         int len = b.matrix[0].length - matrix[0].length;
         if (len < 1) {
            len = 1;
         }

         try {
            for (int offsetx = 0; offsetx < 1; offsetx++) {
               for (int offsety = 0; offsety < len; offsety++) {

                  for (int i = 0; i + offsety < matrix.length && i + offsety < b.matrix.length; i++) {
                     for (int j = 0; j + offsetx < matrix[0].length && j + offsetx < b.matrix[0].length; j++) {

                        //matches+=(matrix[i+offsety][j+offsetx] & b.matrix[i][j]);
                        matches2 += (matrix[i][j] & b.matrix[i + offsety][j + offsetx]);
                     }
                  }

                  if (matches > bestMatch) {

                     bestMatch = matches;
                  }
                  if (matches2 > bestMatch) {

                     bestMatch = matches2;
                  }

                  matches = 0;
                  matches2 = 0;
               }
            }
         } catch (Exception e) {
            e.printStackTrace();
         }


         if (pixelCount > b.pixelCount) {
            return (double) bestMatch / (double) pixelCount;
         } else {
            return (double) bestMatch / (double) b.pixelCount;
         }

      } catch (Exception e) {
         e.printStackTrace();
         return 0.0;
      }
   }

   private blob[] findblobs() {
      blob[] toret = new blob[0];
      Vector<pixel> pixels = new Vector();
      for (int i = 0; i < ySize; i++) {
         for (int j = 0; j < xSize; j++) {
            if (matrix[i][j] == 1) {
               count(i, j, pixels);
               if (pixels.size() > 25) {
                  blob tmp = new blob();
                  blob[] newOne = new blob[toret.length + 1];
                  for (int ctr = 0; ctr < toret.length; ctr++) {
                     newOne[ctr] = toret[ctr];
                  }
                  newOne[newOne.length - 1] = tmp;
                  toret = newOne;
                  tmp.pixels = pixels;
               }
               pixels = new Vector();
            }
         }
      }
      return toret;
   }

   public blob[] breakBlob(int width, int height) {
      int bestBet = 0;
      int worstThickness = 9999;
      int thickness = 99999;

      for (int i = 0; i < this.xSize; i++) {
         for (int j = 0; j < this.ySize; j++) {

            if (matrix[j][i] == 1) {

               thickness++;
            }

         }
         if (thickness > 0 && thickness < worstThickness) {
            bestBet = i;
            worstThickness = thickness;
         }
         thickness = 0;
      }

      //if the thinnes point was 1 pixel thick, white that pixel out and rerun the blob pixel accumulator, should get 2 blobs.
      if (bestBet > 0) {
         for (int j = 0; j < this.ySize; j++) {
            matrix[j][bestBet] = 0;
         }
         blob[] toadd = findblobs();
         return toadd;



      }
      return null;
   }

   public void count(int currentx, int currenty, Vector<pixel> pixels) {
      //increase size and set this pixel to white so it doesnt get counted again
      //System.out.println(currentx+" "+currenty);

      //if(size>50000)
      //	return;

      matrix[currentx][currenty] = 0;
      pixels.add(new pixel(currenty, currentx));

      //imageHelpers.writeImage(copy, "/usr/web/bin"+size+".jpg");

      //prevent stack overflow if the blob is too large, if it is this large it is uninteresting


      //check the 8 surrounding pixels, if any of them is black, call this function again.

      /*if(img.getRGB(currentx-1, currenty)!=-1)

       {
       count(img,currentx-1, currenty);
       }
       if(img.getRGB(currentx-1, currenty+1)!=-1)
       {
       count(img,currentx-1, currenty+1);
       }*/
      try {
//1
         if (matrix[currentx - 1][currenty - 1] == 1) {
            count(currentx - 1, currenty - 1, pixels);
         }
//2
         if (matrix[currentx][currenty + 1] == 1) {
            count(currentx, currenty + 1, pixels);
         }
         //3
         if (matrix[currentx][currenty - 1] == 1) {
            count(currentx, currenty - 1, pixels);
         }
         //4
         if (matrix[currentx + 1][currenty] == 1) {
            count(currentx + 1, currenty, pixels);
         }
      } catch (ArrayIndexOutOfBoundsException e) {
      }



   }

   public BufferedImage writeGlyph(String prefix) throws IOException {
      BufferedImage toret = ImageHelpers.readAsBufferedImage("/usr/blankimg.jpg");
      if (toret.getWidth() > matrix.length && toret.getHeight() > matrix[0].length) {
         toret = toret.getSubimage(0, 0, matrix.length, matrix[0].length);
      }

      for (int i = 0; i < matrix.length; i++) {
         for (int j = 0; j < matrix[0].length; j++) {
            if (matrix[i][j] == 1 && toret.getWidth() > i && toret.getHeight() > j) {

               toret.setRGB(i, j, 0xff0000);
            }
         }
      }
      ImageIO.write(toret, "jpg", new File("/usr/glyphs/" + prefix + Thread.currentThread().getId() + ".jpg"));
      return toret;
   }

   public BufferedImage writeGlyph(String prefix, matrixBlob mb2) throws IOException {
      BufferedImage toret = ImageHelpers.readAsBufferedImage("/usr/blankimg.jpg");
      toret = ImageHelpers.scale(toret, this.matrix.length + mb2.matrix.length, this.matrix[0].length + mb2.matrix[0].length);

      //if( toret.getWidth()>matrix.length && toret.getHeight()>matrix[0].length)
      // toret=toret.getSubimage(0, 0, matrix.length, matrix[0].length);

      for (int i = 0; i < matrix.length; i++) {
         for (int j = 0; j < matrix[0].length; j++) {
            if (matrix[i][j] == 1 && toret.getWidth() > i && toret.getHeight() > j) {

               toret.setRGB(j, i, 0xff0000);
            }
         }
      }
      for (int i = 0; i < mb2.matrix.length; i++) {
         for (int j = 0; j < mb2.matrix[0].length; j++) {
            if (mb2.matrix[i][j] == 1 && toret.getWidth() > i + matrix.length && toret.getHeight() > j + matrix[0].length) {

               toret.setRGB(j + matrix[0].length, i + matrix.length, 0x0000ff);
            }
         }
      }
//    toret=toret.getSubimage(0, 0, matrix.length+mb2.matrix.length, matrix[0].length+mb2.matrix[0].length);
      ImageIO.write(toret, "jpg", new File("/usr/glyphs/" + prefix + System.currentTimeMillis() + Thread.currentThread().getId() + ".jpg"));
      return toret;
   }

   /**
    * Returns an array containing 2 elements, matrix a at index 0 and matrix b at index 1
    */
   public static matrixBlob[] scaleUsingCommonFactor(matrixBlob a, matrixBlob b) {

      matrixBlob[] toret = new matrixBlob[2];

      int commonFactor = determineLCM(a.matrix.length, b.matrix.length); //lazy wasteful way to calculate this, but fine for testing purposes.
      //System.out.print(a.matrix.length+":"+b.matrix.length+":"+commonFactor+"\n");
      int aMultiplier = commonFactor / a.matrix.length;
      int bMultiplier = commonFactor / b.matrix.length;
      int[][] matrixA = new int[commonFactor][aMultiplier * a.matrix[0].length];

      int[][] matrixB = new int[commonFactor][bMultiplier * b.matrix[0].length];
      int pixelsA = 0;
      int pixelsB = 0;
      for (int i = 0; i < matrixA.length; i++) {
         for (int j = 0; j < matrixA[0].length; j++) {
            matrixA[i][j] = a.matrix[i / aMultiplier][j / aMultiplier];
            if (matrixA[i][j] == 1) {
               pixelsA++;
            }
         }
      }
      for (int i = 0; i < matrixB.length; i++) {
         for (int j = 0; j < matrixB[0].length; j++) {
            matrixB[i][j] = b.matrix[i / bMultiplier][j / bMultiplier];
            if (matrixB[i][j] == 1) {
               pixelsB++;
            }
         }
      }
      toret[0] = new matrixBlob(matrixA, pixelsA);
      toret[1] = new matrixBlob(matrixB, pixelsB);


      return toret;
   }

   public static int determineLCM(int a, int b) {
      int num1, num2;
      if (a > b) {
         num1 = a;
         num2 = b;
      } else {
         num1 = b;
         num2 = a;
      }
      for (int i = 1; i <= num2; i++) {
         if ((num1 * i) % num2 == 0) {
            return i * num1;
         }
      }
      throw new Error("Error");
   }
}
