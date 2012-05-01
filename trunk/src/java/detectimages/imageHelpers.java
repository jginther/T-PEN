
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

import com.sun.image.codec.jpeg.JPEGCodec;
import com.sun.image.codec.jpeg.JPEGImageDecoder;
import com.sun.media.jai.codecimpl.TIFFImageDecoder;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.awt.image.renderable.ParameterBlock;
import java.io.File;

import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;
import java.util.Arrays;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import javax.media.jai.Histogram;
import javax.media.jai.JAI;
import javax.media.jai.PlanarImage;

/**Provides a bunch of helper functions for dealing with bufferedimages*/
public class imageHelpers
{
    /**
     * Scale the image to have the specified height in pixels, maintaining aspect ratio
     * @param img
     * @param height
     * @return
     */
    public static BufferedImage scale(BufferedImage img, int height)
    {
        double scale=(double)height/img.getHeight();
        int width=(int) (img.getWidth() * scale);
        return scale(img,height,width);
    }
/**
 * Detect the columns in a binarized image
 * @param bin Binarized version of the iamge
 * @param fileName Filename to be used for saving debug information
 * @param v Vector to store detected columns
 * @return
 */
    public static BufferedImage createViewableVerticalProfile(BufferedImage bin, String fileName, Vector<line> v)
    {
        int white=0xff000000;
        int black=0xffffffff;
        BufferedImage toret=cloneBufferedImage(bin);
        int [] vals=new int[toret.getWidth()];
        for(int i=0;i<vals.length;i++)
            vals[i]=0;
        for(int i=0;i<toret.getWidth();i++)
            for(int j=0;j<toret.getHeight();j++)
            {
                if(toret.getRGB(i, j)==white)
                    vals[i]++;
            }
        int [] ordered=Arrays.copyOf(vals, vals.length);
            Arrays.sort(ordered);int x=0;
            int x1=0;
            Vector<line> lines=new Vector();
            int median=ordered[ordered.length/6];
            //find the number of values in the ordered array between median and 2*median. If the distance is large, do nothing. If the distance is small, increase the value of median by 50%
            int start=0;

            
                /*
                if(ordered[i]>=(median+median/2))
                {
                    int dist=i-ordered.length/6;
                    if(dist>ordered.length/20)
                    {
                        median=median+median/2;
                        System.out.print("increasing median by 50%\n");
                    }
                    else
                    {
                        System.out.print("Not changing median\n");
                    }

                    break;
                }
            }*/
            median=median+median/5;
        for(int i=0;i<vals.length;i++)
        {
            if(vals[i]>median)
            {
            for(int j=0;j<vals[i];j++)
            {
                toret.setRGB(i, j, black);
            }
            for(int j=vals[i];j<toret.getHeight();j++)
            {
                toret.setRGB(i, j, white);
            }
            }
            else
            {
               for(int j=0;j<toret.getHeight();j++)
            {
                toret.setRGB(i, j, white);
            }
            }
        }
        try {
           // FileWriter w = new FileWriter(new File("/usr/profilelogs/"+fileName.replace(".jpg",".txt")));
           // imageHelpers.writeImage(toret, "/usr/profilelogs/"+fileName);
            
            int sum=0;
            
            //w.append("median is "+ordered[ordered.length/50]+"\n");
            for(int i=0;i<vals.length;i++)
            {
             if(vals[i]<=median)
             {
                // w.append("0\n");
                 if(x>0)
                 {
                     x1=i;
                     line l=new line();
                     l.setStartHorizontal(x);
                     l.setWidth(x1-x);
                     //System.out.print("adding col "+x+" "+x1+"\n");
                     lines.add(l);
                     x=0;
                     x1=0;
                 }
             }
             else
             {
                // w.append(""+vals[i]+"\n");
                 if(x==0)
                     x=i;
                 if(i==vals.length-1)
                 {
                     x1=i;
                     line l=new line();
                     l.setStartHorizontal(x);
                     l.setWidth(x1-x);
                     //System.out.print("adding col "+x+" "+x1+"\n");
                     lines.add(l);
                     x=0;
                     x1=0;
                 }
             }
            


            
             
            } 
            
            Vector<line> finalizedLines=new Vector();
            int minimalColumnFraction=8;
            for(int j=0;j<lines.size();j++)
            {
                line l=lines.get(j);
                //merging needs to consider the magnitude of the difference from median
                    if(j>0)
                    {
                        line prev=lines.get(j-1);
                        if(l.getStartHorizontal()-   prev.getStartHorizontal()-prev.getWidth()<25 && (l.getWidth()<bin.getWidth()/minimalColumnFraction || prev.getWidth()<bin.getWidth()/minimalColumnFraction || l.getStartHorizontal()-   prev.getStartHorizontal()-prev.getWidth()<3))
                        {
                            prev.setWidth(prev.getWidth()+l.getWidth());
                            lines.remove(l);
                            j--;
                        }
                        else
                        {
                           // System.out.print(" gap is "+l.getStartHorizontal()+" "+ prev.getStartHorizontal()+" "+prev.getWidth()+"\n");
                        }
                    }
                
            }
            for(int j=0;j<lines.size();j++)
            {
                line l=lines.get(j);
                //if this is a small column try to merge it
                if(l.getWidth()<bin.getWidth()/minimalColumnFraction)
                {
                    /*if(l.getWidth()!=0)
                    {
                    finalizedLines.add(l);
                    l.setStartVertical(0);
                    l.setDistance(bin.getHeight()/2);
                    imageHelpers.highlightBlock(bin,l.getStartHorizontal(), l.getStartVertical(), l.getDistance(), l.getWidth(), 0x0000ff);
                    }*/
                }
                else
                {
                    v.add(l);
                    l.setStartVertical(0);
                    l.setDistance(bin.getHeight()-1);

                    imageHelpers.highlightBlock(bin,l.getStartHorizontal(), l.getStartVertical(), l.getDistance(), l.getWidth(), 0xff0000);

                }
                
            }//imageHelpers.writeImage(imageHelpers.scale(bin, 1000),"/usr/cols/"+fileName);
           // w.close();
        } catch (Exception ex) {
            Logger.getLogger(imageHelpers.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        return toret;
    }
    /**
 * Detect the columns in a binarized image
 * @param bin Binarized version of the iamge
 * @param fileName Filename to be used for saving debug information
 * @param v Vector to store detected columns
 * @return
 */
    public static BufferedImage createViewableVerticalProfile(BufferedImage bin, String fileName, Vector<line> v, double skew)
    {
        int white=0xff000000;
        int black=0xffffffff;
        BufferedImage toret=cloneBufferedImage(bin);
        int [] vals=new int[toret.getWidth()];
        for(int i=0;i<vals.length;i++)
            vals[i]=0;
        for(int i=0;i<toret.getWidth();i++)
            for(int j=0;j<toret.getHeight();j++)
            {
                if((int)(i+j*skew)<toret.getWidth())
                if(toret.getRGB((int) (i + j * skew), j)==white)
                    vals[i]++;
            }
        int [] ordered=Arrays.copyOf(vals, vals.length);
            Arrays.sort(ordered);int x=0;
            int x1=0;
            Vector<line> lines=new Vector();
            int median=ordered[ordered.length/6];
        for(int i=0;i<vals.length;i++)
        {
            if(vals[i]>median)
            {
            for(int j=0;j<vals[i];j++)
            {
                toret.setRGB(i, j, black);
            }
            for(int j=vals[i];j<toret.getHeight();j++)
            {
                toret.setRGB(i, j, white);
            }
            }
        }
        try {
            //FileWriter w = new FileWriter(new File("/usr/profilelogs/"+fileName.replace(".jpg",".txt")));
            int binsize=50;
            int sum=0;
            imageHelpers.writeImage(toret, "/usr/profilelogs/"+fileName);
            //w.append("median is "+ordered[ordered.length/50]+"\n");
            for(int i=0;i<vals.length;i++)
            {
             if(vals[i]<=median)
             {
                 //w.append("0\n");
                 if(x>0)
                 {
                     x1=i;
                     line l=new line();
                     l.setStartHorizontal(x);
                     l.setWidth(x1-x);
                     //System.out.print("adding col "+x+" "+x1+"\n");
                     lines.add(l);
                     x=0;
                     x1=0;
                 }
             }
             else
             {
                // w.append(""+vals[i]+"\n");
                 if(x==0)
                     x=i;
                 if(i==vals.length-1)
                 {
                     x1=i;
                     line l=new line();
                     l.setStartHorizontal(x);
                     l.setWidth(x1-x);
                     //System.out.print("adding col "+x+" "+x1+"\n");
                     lines.add(l);
                     x=0;
                     x1=0;
                 }
             }





            }

            Vector<line> finalizedLines=new Vector();
            int minimalColumnFraction=8;
            for(int j=0;j<lines.size();j++)
            {
                line l=lines.get(j);
                //merging needs to consider the magnitude of the difference from median
                    if(j>0)
                    {
                        line prev=lines.get(j-1);
                        if(l.getStartHorizontal()-   prev.getStartHorizontal()-prev.getWidth()<25 && (l.getWidth()<bin.getWidth()/minimalColumnFraction || prev.getWidth()<bin.getWidth()/minimalColumnFraction || l.getStartHorizontal()-   prev.getStartHorizontal()-prev.getWidth()<5))
                        {
                            prev.setWidth(prev.getWidth()+l.getWidth());
                            lines.remove(l);
                            j--;
                        }
                        else
                        {
                           // System.out.print(" gap is "+l.getStartHorizontal()+" "+ prev.getStartHorizontal()+" "+prev.getWidth()+"\n");
                        }
                    }

            }
            for(int j=0;j<lines.size();j++)
            {
                line l=lines.get(j);
                //if this is a small column try to merge it
                if(l.getWidth()<bin.getWidth()/minimalColumnFraction)
                {
                    /*if(l.getWidth()!=0)
                    {
                    finalizedLines.add(l);
                    l.setStartVertical(0);
                    l.setDistance(bin.getHeight()/2);
                    imageHelpers.highlightBlock(bin,l.getStartHorizontal(), l.getStartVertical(), l.getDistance(), l.getWidth(), 0x0000ff);
                    }*/
                }
                else
                {
                    v.add(l);
                    l.setStartVertical(0);
                    l.setDistance(bin.getHeight()-1);
                    imageHelpers.highlightBlock(bin,l.getStartHorizontal(), l.getStartVertical(), l.getDistance(), l.getWidth(), 0xff0000);

                }

            }imageHelpers.writeImage(imageHelpers.scale(bin, 1000),"/usr/cols/"+fileName);
            //w.close();
        } catch (Exception ex) {
            Logger.getLogger(imageHelpers.class.getName()).log(Level.SEVERE, null, ex);
        }

        return toret;
    }

    /**
     * Scale the image to have the specified height and width in pixels
     * @param img
     * @param height
     * @param width
     * @return
     */
    public static BufferedImage scale(BufferedImage img, int height, int width)
	{


	   BufferedImage bdest =
	      new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
	   Graphics2D g = bdest.createGraphics();
	   AffineTransform at =
	      AffineTransform.getScaleInstance((double)width/img.getWidth(),
	          (double)height/img.getHeight());
	   g.drawRenderedImage(img,at);
	   return bdest;


	}
        /**
         * Perform binary thresholding with 1 of the 5 available methods
         * @param img
         * @param method 0:Iterative Bisection
         *               1:Maximum Entropy
         *               2:Maximum Variance
         *               3:Minimum Error
         *               4:Minimum Fuzziness
         * @return
         */
        public static BufferedImage binaryThreshold(BufferedImage img, int method)
	{
		PlanarImage j = PlanarImage.wrapRenderedImage(img);  //JAI.create("fileload", imageFile.getPath());
	      //double[][] matrix = {{ 0.114, 0.587, 0.299, 0 }};
        double[][] matrix = {{ 0.114, 0.587, 0.299, 0 }};
	      ParameterBlock pb = new ParameterBlock();
	      pb.addSource(j);
	      pb.add(matrix);
	      try
	      {
	    	  j = JAI.create("bandcombine", pb);
	      }
	      catch (IllegalArgumentException e)
	      {

	      }


		//displayImage(j.getAsBufferedImage());
		pb = new ParameterBlock();
		   pb.addSource(j);
		   pb.add(null); // The ROI
		   pb.add(1);
		  pb.add(1);
		   pb.add(new int[]{256});
		   pb.add(new double[]{0});
		   pb.add(new double[]{256});
		   // Calculate the histogram of the image.
		   PlanarImage dummyImage = JAI.create("histogram", pb);
		   Histogram h = (Histogram)dummyImage.getProperty("histogram");
		   // Calculate the thresholds based on the selected method.

                   //for(int i=0;i<5;i++)
		double[] thresholds = null;

		   switch(method)
		     {
		     case  0: // Iterative Bisection
		       thresholds = h.getIterativeThreshold(); break;
		     case  1: // Maximum Entropy
		       thresholds = h.getMaxEntropyThreshold(); break;
		     case  2: // Maximum Variance
		      thresholds = h.getMaxVarianceThreshold(); break;
		    case  3: // Minimum Error
		       thresholds = h.getMinErrorThreshold(); break;
		    case  4: // Minimum Fuzziness
		       thresholds = h.getMinFuzzinessThreshold(); break;

		    }
		  int threshold = (int)thresholds[0];
		   // Change the UI to use the new threshold.

		BufferedImage bin = (binarize(threshold,j));

		return bin;
	}
        /**
         * Threshold the image after removing what appears to be a background color on the border
         * @param img the color image
         * @param method the method to use, 0-4
         * @param removeBackground 
         * @return
         */
        public static BufferedImage binaryThreshold(BufferedImage img, int method, Boolean removeBackground)
	{
            if(removeBackground)
            {
                BufferedImage scratch=cloneBufferedImage(img);
                scratch=grayscale(scratch);
                scratch=removeBackground(scratch,0xffcccccc);
                 for(int i=0;i<img.getWidth();i++)
                for(int j=0;j<img.getHeight();j++)
                {
                    if(scratch.getRGB(i, j)==0xffffffff)
                    {
                        img.setRGB(i, j,0xffffff);

                    //System.out.print("setting\n");

                    }
                }
		PlanarImage j = PlanarImage.wrapRenderedImage(img);  //JAI.create("fileload", imageFile.getPath());
	      //double[][] matrix = {{ 0.114, 0.587, 0.299, 0 }};
        double[][] matrix = {{ 0.114, 0.587, 0.299, 0 }};
	      ParameterBlock pb = new ParameterBlock();
	      pb.addSource(j);
	      pb.add(matrix);
	      try
	      {
	    	  j = JAI.create("bandcombine", pb);
	      }
	      catch (IllegalArgumentException e)
	      {

	      }


		//displayImage(j.getAsBufferedImage());
		pb = new ParameterBlock();
		   pb.addSource(j);
		   pb.add(null); // The ROI
		   pb.add(1);
		  pb.add(1);
		   pb.add(new int[]{256});
		   pb.add(new double[]{0});
		   pb.add(new double[]{256});
		   // Calculate the histogram of the image.
		   PlanarImage dummyImage = JAI.create("histogram", pb);
		   Histogram h = (Histogram)dummyImage.getProperty("histogram");
		   // Calculate the thresholds based on the selected method.

                   //for(int i=0;i<5;i++)
		double[] thresholds = null;

		   switch(method)
		     {
		     case  0: // Iterative Bisection
		       thresholds = h.getIterativeThreshold(); break;
		     case  1: // Maximum Entropy
		       thresholds = h.getMaxEntropyThreshold(); break;
		     case  2: // Maximum Variance
		      thresholds = h.getMaxVarianceThreshold(); break;
		    case  3: // Minimum Error
		       thresholds = h.getMinErrorThreshold(); break;
		    case  4: // Minimum Fuzziness
		       thresholds = h.getMinFuzzinessThreshold(); break;

		    }
		  int threshold = (int)thresholds[0];
		   // Change the UI to use the new threshold.

		BufferedImage bin = (binarize(threshold,j));

		return bin;
            }
            return binaryThreshold(img,method);
	}
	private static BufferedImage binarize(int threshold, PlanarImage image)
    {
	    // Binarizes the original image.
		//if(threshold>5)
		//threshold-=3;
	    ParameterBlock pb = new ParameterBlock();
	    pb.addSource(image);
	    pb.add(1.0*threshold);
	    // Creates a new, thresholded image and uses it on the DisplayJAI component
	    PlanarImage thresholdedImage = JAI.create("binarize", pb);
	    return thresholdedImage.getAsBufferedImage();
	    }
        /**Read an image from a file into a BufferedImage
         * @param filename
         * @return
         */
public static BufferedImage readAsBufferedImage(String filename)
    {
	      try {
	         FileInputStream fis = new FileInputStream(filename);
                 if(filename.endsWith("tif"))
                 {
                 PlanarImage planar = JAI.create("FileLoad", filename);
                 return planar.getAsBufferedImage();
                 }

	         JPEGImageDecoder decoder = JPEGCodec.createJPEGDecoder(fis);
	         BufferedImage bi = decoder.decodeAsBufferedImage();
	         return bi;
	      } catch(Exception e) {
	         System.out.println(e);
	         return null;
	      }
	 }
/**Read a jpeg image from a url into a BufferedImage
 * @param imageURL
 * @return
 */
public static BufferedImage readAsBufferedImage(URL imageURL) {
	      try {
                 if(imageURL.getFile().endsWith("tif"))
                 {
                 PlanarImage planar = JAI.create("url", imageURL);
                 return planar.getAsBufferedImage();
                 }
	         JPEGImageDecoder decoder = JPEGCodec.createJPEGDecoder(imageURL.openStream());
	         BufferedImage bi = decoder.decodeAsBufferedImage();
	         return bi;
	      } catch(Exception e) {
	         System.out.println(e);
	         return null;
	      }
	   }
	
/**
 * Save a bufferedimage as a jpg
 * @param img
 * @param filename
 */
public static void writeImage(BufferedImage img, String filename)
	   {
		   try
		   {
		   ImageIO.write(img, "jpg", new File(filename) );
		   }
		   catch (IOException e)
		   {
			   System.out.println(e);
		   }

	   }
/**
 * Transpose the image horizontally
 * @param image
 * @return
 */
public static BufferedImage flipHorizontal(BufferedImage image)
    {
        PlanarImage j = PlanarImage.wrapRenderedImage(image);
        j=(PlanarImage)JAI.create("transpose", j,javax.media.jai.operator.TransposeDescriptor.FLIP_HORIZONTAL);
        return j.getAsBufferedImage();
    }
    /**
     * Highlight a portion of the image im yellow
     * @param img
     * @param x
     * @param y
     * @param height
     * @param width
     * @return
     */
    public static BufferedImage highlightPortion(BufferedImage img, int x, int y, int height, int width)
    {

        for(int i=(int) (x - .5 * width);i<x+width*2;i++)
            try{
                img.setRGB(i, y-width,  0xff0000);
                img.setRGB(i, y+2*width,  0xff0000);
                }
                //if there was an out of bounds its because of the size doubling, ignore it
                catch (ArrayIndexOutOfBoundsException e)
                {}
            for(int j=(int) (y - .5 * height);j<y+height*2;j++)
               try{
                img.setRGB(x-width, j,  0xff0000);
                img.setRGB(x+2*width, j,  0xff0000);
                }
                //if there was an out of bounds its because of the size doubling, ignore it
                catch (ArrayIndexOutOfBoundsException e)
                {}
            
        return img;
    }
    /**
     * Highlight a portion of the image im the requested color
     * @param img
     * @param x
     * @param y
     * @param height
     * @param width
     * @param color
     * @return
     */
    public static BufferedImage highlightBlock(BufferedImage img, int x, int y, int height, int width,  int color)
    {

        for(int i=(int) (x);i<=x+width;i++)
            for(int j=(int) (y);j<=y+height;j++)
               try{
                   if(i==x+width || j==y+height || j==y || i==x)
                       img.setRGB(i, j,  0x000000);
                   else
                img.setRGB(i, j, (int) (color ^ img.getRGB(i, j) ));

                }
                //if there was an out of bounds its because of the size doubling, ignore it
                catch (ArrayIndexOutOfBoundsException e)
                {}

        return img;
    }
     public static BufferedImage grayscale(BufferedImage img)
    {
        BufferedImage b=new BufferedImage(img.getWidth(), img.getHeight(),BufferedImage.TYPE_BYTE_GRAY);
            for(int i=0;i<img.getWidth();i++)
                for(int j=0;j<img.getHeight();j++)
                {
                    b.setRGB(i, j, img.getRGB(i, j));
                }
                img=b;
		return(img);
    }
public static void main(String [] args)
{
    BufferedImage img=readAsBufferedImage("/usr/testimage.jpg");
    BufferedImage bin=imageHelpers.binaryThreshold(img, 0);
    Detector d=new Detector(img, bin);
    d.detect();
    System.out.print(d.lines.size()+"\n");
    img=imageHelpers.grayscale(img);
    imageHelpers.writeImage(img, "/usr/tested0.jpg");
    img=imageHelpers.removeBackground(img, 0xffcccccc);
    imageHelpers.writeImage(img, "/usr/tested1.jpg");
}
    /**Attempts to isolate the forground pixels, and mark all background pixels with a neutral color based on the palette in the image*/
    public static BufferedImage removeBackground(BufferedImage bin, int thresh)
    {
        int vsmearColDist=3;
        int imageFractionForColumn=40;// 1/this number is the way this gets used
        long sum=0;
         for(int i=0;i<bin.getWidth();i++)
			   for(int j=0;j<bin.getHeight()-vsmearColDist*2;j++)
                               sum+=bin.getRGB(i, j);
                         
        int mean=(int) (sum / (bin.getHeight() * bin.getWidth()));
        thresh=mean;
        //start at the center and spiral outwards, as the middle is expected to have the content of interest, which ends somewhere outside
        
                    for(int j=0;j<bin.getHeight();j++)
			  for(int i=0;i<bin.getWidth();i++)
				   {
                              //if(Integer.toHexString(bin.getRGB(i,j)).compareTo("ffffffff")!=0 )
                              //System.out.print(Integer.toHexString(bin.getRGB(i,j))+"\n");
					   if(bin.getRGB(i,j)<thresh)
					   {
						   int farAway=(i+(bin.getWidth()/imageFractionForColumn)); //needs to be a parm
						   if(farAway>bin.getWidth())
							   farAway=bin.getWidth();
						   Boolean whiteout=true;
						   for(int backwardsCtr=i;backwardsCtr<farAway;backwardsCtr++)
						   {
							   if((bin.getRGB(backwardsCtr,j)>thresh))
							   {
								   whiteout=false;
								   i=farAway+1;
								   break;
								   //This isnt a big horizontal area that should be blanked out. Neither is anything between
								   //j and farAway obviously.

							   }
						   }
						   if(whiteout)
						   {
							   for(int backwardsCtr=i;backwardsCtr<farAway;backwardsCtr++)
								   bin.setRGB(backwardsCtr,j,0xffffff);
						   }

					   }
				   }

		  for(int i=0;i<bin.getWidth();i++)
			   for(int j=0;j<bin.getHeight()-vsmearColDist*2;j++)
			   {
                               int dist=bin.getHeight()/imageFractionForColumn;
                               //System.out.print(dist+" ");
				   if(bin.getRGB(i,j)<thresh)
				   {
					   int farAway=(j+(dist)); //needs to be a parm
					   if(farAway>bin.getHeight())
						   farAway=bin.getHeight();
					   Boolean whiteout=true;
					   for(int backwardsCtr=j;backwardsCtr<farAway;backwardsCtr++)
					   {
						   if((bin.getRGB(i,backwardsCtr)>thresh))
						   {
							   whiteout=false;
							   j=farAway+1;
							   break;
							   //This isnt a big vertical area that should be blanked out. Neither is anything between
							   //j and farAway obviously.
						   }
					   }
					   if(whiteout)
					   {
						   for(int backwardsCtr=j;backwardsCtr<farAway;backwardsCtr++)
							   bin.setRGB(i,backwardsCtr,0xffffff);
					   }
				   }
			   }
        int count=1;
        sum=0;
        for(int i=0;i<bin.getWidth();i++)
			   for(int j=0;j<bin.getHeight()-vsmearColDist*2;j++)
                           {
                               if(bin.getRGB(i,j)!=0xffffffff)
                               {
                                 sum+=bin.getRGB(i,j);
                                 count++;
                               }
                           }
        mean=   (int) (sum / count);
        /*for(int i=0;i<bin.getWidth();i++)
			   for(int j=0;j<bin.getHeight()-vsmearColDist*2;j++)
                           {
                               if(bin.getRGB(i,j)==0xffffffff)
                                   bin.setRGB(i,j,mean);
                           }*/
		  writeImage(bin,"/usr/web/altBGtmp1.jpg");

        return bin;
    }
    /** */
    public BufferedImage smear(int max_dist, BufferedImage bin)
	   {
		  int thresh=-1700000;
		  Boolean found_partner=false;
			   for(int j=0;j<bin.getHeight();j++)
			   {
				   for(int i=0;i<bin.getWidth()-max_dist;i++)
				   if(bin.getRGB(i,j)<thresh)
				   {
					   found_partner=false;
					   for(int k=max_dist-1;k>0;k--)
					   { if((i+k)>(bin.getWidth()-max_dist))
						   {
							   k=bin.getWidth()-i-max_dist;
						   }
						  if (bin.getRGB(i+k,j)<thresh)
						  {
							  found_partner=true;
							  for(int l=k;l>0;l--)
							  {
								  bin.setRGB(i+l, j, 0x000000);
							  }
                              i+=k;
							  k=0;
						  }
					   //If this pixel didnt get smeared, white it out.
					   if(!found_partner)
					   {
						   bin.setRGB(i,j,0xffffff);
						   i=i+max_dist-1;
					   }
				   }
				   }
			   }
		   return bin;
	   }
    public BufferedImage vsmear(int max_dist, BufferedImage bin)
	   {
		  int thresh=-1700000;


		  Boolean found_partner=false;
				   for(int i=0;i<bin.getWidth();i++)
					   for(int j=0;j<bin.getHeight()-max_dist;j++)
					   {
				   if(bin.getRGB(i,j)<thresh)
				   {
					   found_partner=false;
					   for(int k=max_dist-1;k>0;k--)
                       {
                           if((j+k)>bin.getHeight()-max_dist)
                           {
                               break;
                           }
						  if (bin.getRGB(i,j+k)<thresh)
						  {
							  found_partner=true;
							  for(int l=k;l>0;l--)
							  {
								  bin.setRGB(i, j+l, 0x000000);
							  }
                              j+=k;
						  }
                       }
					   //If this pixel didnt get smeared, white it out.
					   if(!found_partner)
					   {
						   bin.setRGB(i,j,0xffffff);
						   j=j+max_dist-1;
					   }
				   }
			   }
		   //displayImage(this.bin);
		   return bin;
	   }
    /**Clone a buffered image, returning an image witht he same content that you can modify without impacting the
     original*/
    public static BufferedImage cloneBufferedImage(BufferedImage img)
    {
        BufferedImage copy=new BufferedImage(img.getWidth(),img.getHeight(),img.getType());
		for(int i=0;i<img.getHeight()-1;i++)
			for(int j=0;j<img.getWidth()-1;j++)
			{
				copy.setRGB( j,i, img.getRGB(j, i));
			}
		return copy;
    }
}
