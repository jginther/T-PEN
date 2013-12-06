/*
 * @author Jon Deering
Copyright 2011 Saint Louis University. Licensed under the Educational Community License, Version 2.0 (the "License"); you may not use
this file except in compliance with the License.

You may obtain a copy of the License at http://www.osedu.org/licenses/ECL-2.0

Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions
and limitations under the License.
 */

package match;

/**
 The line object is used by folio and Detector to store the information about a parsed line in an image.
 */

public class line
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
        public line(int left, int right, int top, int bottom)
        {
            this.left=left;
            this.right=right;
            this.top=top;
            this.bottom=bottom;
        }
        /**
         * calculate the height of this line as bottom-top, returns 25 if the height is negative
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


            if(!zoom)
                return right-left;
            else
                return (right-left)*2;
        }
        /**
         * retrieve the topmost point, aka vertical start
         * @return
         */
        public int getTop()
        {
            if(!zoom)
                return top;
            else
                return top*2;
        }
        /**
         * retrieve the leftmost point, aka horizontal start
         * @return
         */
        public int getLeft()
        {
            if(!zoom)
            return left-((right-left)/4);
            else
                return (left-((right-left)/4))*2;
        }
        /**
         * retrieve the bottom of this line, vertical end
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
