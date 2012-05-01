
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

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Vector;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.logging.Level;
import java.util.logging.Logger;


public class Main {

    /**
     * @param args the command line arguments
     */


    
    public static void main(String[] args) throws IOException {
        int taskCount=4;
        class prioritythread implements ThreadFactory{

            public Thread newThread(Runnable r)
            {
                Thread t=new Thread(r);
                t.setPriority(Thread.MIN_PRIORITY);
                return t ;

            }

    }
        ExecutorService executor = null;
        if(args.length>=2)
        {
            taskCount=Integer.parseInt(args[1]);
        }
        executor=Executors.newFixedThreadPool(taskCount,new prioritythread());

        /*if(args.length!=1)
        {
            System.out.print("usage: detectImages imagefolder");
            return;
        }*/
       // File outterDirectory=new File(args[0]);
        //File [] dirs=outterDirectory.listFiles();
        //for(int j=0;j<dirs.length;j++)
        //{
            try{
       // File msDir=dirs[j];
        String imageDir="/usr/profile";
        //String imageDir=msDir.getAbsolutePath();
        //File directory=msDir.listFiles()[0];
        //File [] images=directory.listFiles();
        File [] images=new File(imageDir).listFiles();

try{
   new File(imageDir+"/scaled/").mkdir();
   new File(imageDir+"/processed/").mkdir();
   new File(imageDir+"/queries/").mkdir();
}catch (Exception e){}


        for(int i=0;i<images.length;i++)
        {
            try{
            if(images[i].getName().contains(".jpg"))
            {
                runner r=new runner();
                r.i=i;
                r.imageDir=imageDir;
                r.images=images;
                executor.submit(r);
            }
            //System.out.print("found "+j.lines.size()+"\n");
            }
            catch(Exception e)
            {
                
            }
        }

    }
            catch(Exception e)
        {
                e.printStackTrace();
         //   System.out.print(e.toString()+"\n");
        }
               
        }

    private static class runner implements Runnable
    {
    protected File [] images;
    protected String imageDir;
    protected int i;
        public void run() {
            FileWriter writer = null;
            try {
                System.out.print("Starting " + images[i].getName() + "\n");
                BufferedImage img = imageHelpers.readAsBufferedImage(images[i].getPath());
                if (img.getHeight() != 1000) {
                    int width = (int) (1000 / (double) img.getHeight() * img.getWidth());
                    img = imageHelpers.scale(img, 1000, width);
                }
                imageHelpers.writeImage(img, imageDir + "/scaled/" + images[i].getName());
                BufferedImage flippedImage = imageHelpers.cloneBufferedImage(img);
                flippedImage = imageHelpers.flipHorizontal(flippedImage);
                BufferedImage bin = img;//imageHelpers.binaryThreshold(img, 4);
                Detector myDetector = new Detector(img, bin);
                myDetector.debugLabel=images[i].getName();
                myDetector.smeared = bin;
                myDetector.graphical = true;
                myDetector.forceSingle = false;
                myDetector.vsmearDist = 15;
                myDetector.hsmearDist = 15;
                /*detectimages.line[] flipped = null;
                try {
                    myDetector.detect();
                } catch (ArithmeticException e) {
                }
                if (flipped == null || flipped.length < myDetector.lines.size() || (flipped.length == myDetector.lines.size() && flipped[0].getWidth() < myDetector.lines.elementAt(0).getWidth())) {
                    flipped = myDetector.lines.toArray(new detectimages.line[myDetector.lines.size()]);
                }
                //bin = imageHelpers.binaryThreshold(flippedImage, 4);

                imageHelpers.writeImage(bin, imageDir + "/scaled/bin" + images[i].getName());
                myDetector.bin = bin;
                myDetector.img = flippedImage;
                Detector flipDetector = new Detector(flippedImage, bin);
                flipDetector.smeared = bin;
                flipDetector.detect();
                System.out.print("flip line ctr=" + flipDetector.lines.size() + "\n");
                if (flipDetector.lines.size() > flipped.length || (flipped.length == flipDetector.lines.size() && flipped[0].getWidth() < flipDetector.lines.elementAt(0).getWidth())) {
                    flipped = flipDetector.lines.toArray(new detectimages.line[flipDetector.lines.size()]);
                    //the coordinates in these lines are based on a horizontally flipped image (reflected over vertical axis), so they need to be corrected.
                    for(int i=0;i<flipped.length;i++)
                    {
                        //System.out.print(""+ flipped[i].getStartHorizontal()+" becomes "+(bin.getWidth()-flipped[i].getStartHorizontal()-flipped[i].getWidth())+"\n");
                        flipped[i].setStartHorizontal(bin.getWidth()-flipped[i].getStartHorizontal()-flipped[i].getWidth());
                    }
                }*/
                //myDetector.vsmear(10,bin);
                for(int horCtr=0;horCtr<bin.getWidth();horCtr++)
                    for(int j=0;j<bin.getHeight();j++)
                        if(bin.getRGB(horCtr, j)==0xffffffff)
                            bin.setRGB(horCtr, j, 0x000000);
                        else
                            bin.setRGB(horCtr, j, 0xffffff);
                Vector<xycut.rectangle> v=xycut.segment(bin);
                for(int j=0;j<v.size();j++)
                {
                    xycut.rectangle r=v.get(j);

                    int color = 0x0000ff;
                    if(j%2==0)
                        color=0xff0000;
                    bin = imageHelpers.highlightBlock(img, r.x, r.y,r.y1-r.y,  r.x1-r.x, color);
                    System.out.print(r.x+","+r.y+","+r.x1+","+r.y1+"\n");
                    /*for(int xctr=r.x;xctr<r.x1;xctr++)
                    {       int yctr=r.y;
                            bin.setRGB(xctr, yctr, 0xaaaaaa);
                            yctr=r.y1;
                            bin.setRGB(xctr, yctr, 0xaaaaaa);
                    }
                    for(int yctr=r.y;yctr<r.y1;yctr++)
                    {       int xctr=r.x;
                            bin.setRGB(xctr, yctr, 0xaaaaaa);
                            xctr=r.x1;
                            bin.setRGB(xctr, yctr, 0xaaaaaa);
                    }*/
                }
                imageHelpers.writeImage(bin, imageDir +"/scaled/cols"+images[i].getName());
                writer = new FileWriter(new File("" + imageDir + "/queries/" + images[i].getName() + ""));
                /*System.out.print(imageDir + ":" + images[i].getName() + ":" + flipped.length + "\n");
                line[] linePositions = flipped;
                for (int k = 0; k < linePositions.length; k++) {
                    linePositions[k].commitQuery(writer, images[i].getName());
                    if (k % 2 == 1) {
                        int color = 0x0000ff;
                        img = imageHelpers.highlightBlock(img, linePositions[k].getStartHorizontal(), linePositions[k].getStartVertical() - linePositions[k].getDistance(), linePositions[k].getDistance(), linePositions[k].getWidth(), color);
                    } else {
                        int color = 0xff0000;
                        img = imageHelpers.highlightBlock(img, linePositions[k].getStartHorizontal(), linePositions[k].getStartVertical() - linePositions[k].getDistance(), linePositions[k].getDistance(), linePositions[k].getWidth(), color);
                    }
                }
                imageHelpers.writeImage(img, "" + imageDir + "/processed/" + images[i].getName());*/
                throw new UnsupportedOperationException("Not supported yet.");
            } catch (IOException ex) {
                //Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
                ex.printStackTrace();
            } finally {
                try {
                    writer.close();
                } catch (IOException ex) {
                    Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }

    }

}
