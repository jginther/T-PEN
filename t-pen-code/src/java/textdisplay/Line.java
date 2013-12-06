/*
 * @author Jon Deering
Copyright 2011 Saint Louis University. Licensed under the Educational Community License, Version 2.0 (the "License"); you may not use
this file except in compliance with the License.

You may obtain a copy of the License at http://www.osedu.org/licenses/ECL-2.0

Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions
and limitations under the License.
 */
package textdisplay;

/**
 The Line object is used by folio and Detector to store the information about a parsed Line in an image.
 */

public class Line
    {
        int left;
        int right;
        int top;
        int bottom;
        public Boolean zoom=false;
        /**Unused at this time, allows you to specify that you want information for a copy of the image of double the nromal size*/
        public void setZoom()
        {
            zoom=true;
        }
        public Line(int left, int right, int top, int bottom)
        {
            this.left=left;
            this.right=right;
            this.top=top;
            this.bottom=bottom;
        }
        public Line(detectimages.line l)
        {
          //  this.left=l.getStartHorizontal();
        //    this.right=l.getWidth()+left;
         //   this.top=l.getStartVertical();
        //    this.bottom=l.getDistance();


            this.left=        l.getStartHorizontal();
             this.right=       l.getWidth();
             this.top=l.getStartVertical();
             this.bottom=l.getDistance()+l.getStartVertical();
        }
        /**
         * calculate the height of this Line as bottom-top, returns 25 if the height is negative
         * @return
         */
        public int getHeight()
        {
            int defaultHeight=25;
            int tmp=bottom-top;
            if(zoom)
            {
                tmp=tmp*2;
                defaultHeight=defaultHeight*2;
            }
            if(tmp<0)
            return (defaultHeight);
            return tmp;
        }
        /**
         * Calculate the width
         * @return
         */
        public int getWidth()
        {


          
                return right-left;
        
        }
        /**
         * retrieve the topmost point, aka vertical start
         * @return
         */
        public int getTop()
        {
           
                return top;
            
        }
        /**
         * retrieve the leftmost point, aka horizontal start
         * @return
         */
        public int getLeft()
        {
            return left;
        }
        /**
         * retrieve the bottom of this Line, vertical end
         * @return
         */
        public int getBottom()
        {
            if(!zoom)
            return bottom;
            else
                return bottom*2;
        }
}
