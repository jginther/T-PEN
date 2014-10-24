/*
 * @author Jon Deering
Copyright 2011 Saint Louis University. Licensed under the Educational Community License, Version 2.0 (the "License"); you may not use
this file except in compliance with the License.

You may obtain a copy of the License at http://www.osedu.org/licenses/ECL-2.0

Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions
and limitations under the License.
 */


package imageLines;
import java.awt.image.BufferedImage;
import java.awt.image.RasterFormatException;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

/**This class stores the information needed to draw a Line. It also holds a method to draw this Line on a buffered image*/
public class Line {

	private int width;
	private int startHorizontal;
	private int startVertical;
	private int distance;
        /**
         * noarg constuctor. Use setters to set the values when using this.
         */
	public Line ()
	{

	}
        /**
         * Build a Line without specifying the distance between it and the previous Line
         * @param w
         * @param hor
         * @param ver
         */
	public Line (int w, int hor,int ver)
	{
		width=w;
		startHorizontal=hor;
		startVertical=ver;
		distance=0;
	}
        /**
         * typical constructor
         * @param w
         * @param hor
         * @param ver
         * @param dist
         */
	public Line (int w, int hor,int ver, int dist)
	{
		width=w;
		startHorizontal=hor;
		startVertical=ver;
		distance=dist;

	}
        /**Draw a Line showing where this Line of text occurs on the bufferedimage that this Line occurs in*/
	public BufferedImage drawLine(BufferedImage img, int color)
	{
		if(width>0)
		{
			for(int i=0;i<width;i++)
				img.setRGB(i+startHorizontal, startVertical, color);
		}
		return img;
	}
	/**write this Line as a jpg with the name file*/
	public Boolean commit(String file, BufferedImage img )
	{
		if(distance==0)
			return false;
		try{
		writeImage(img.getSubimage(startHorizontal-15, startVertical-distance*2-6, width+30,distance*4 ),file);
		}
		catch (RasterFormatException e)
		{

		}
		return true;
	}
	public void setWidth(int w)
	{
		width=w;
	}
	public void setStartHorizontal(int hor)
	{
		startHorizontal=hor;
	}
	public void setStartVertical(int ver)
	{
		startVertical=ver;
	}
	public int getStartHorizontal()
	{
		return startHorizontal;
	}
	public int getStartVertical()
	{
		return startVertical;
	}
	public int getWidth()
	{
		return width;
	}
        
	public int getDistance()
	{
		return distance;
	}
	/**write an image as a jpg with the name file*/
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
}
