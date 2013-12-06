
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

import java.util.concurrent.Callable;


public class blobComparer implements Callable
{
    public blob a;
    public blob b;
    public blob[]blobArray;
    public blobComparer(blob one, blob two)
    {
        a=one;
        b=two;
    }
    public blobComparer( blob [] blobArray)
    {
       this.blobArray=blobArray;
    }
    public Object call()
    {
        Double [] results=new Double[blobArray.length*blobArray.length/2];
        int ctr=0;
        System.out.print("starting \n");
        for(int i=0;i<blobArray.length;i+=2)
            {
                //if this is the last work set it may not contain a full compliment of work to be done
                if(blobArray[i]==null || blobArray[i+1]== null)
                    return results;
                a=blobArray[i];
                b=blobArray[i+1];
                results[ctr]=run();
                ctr++;
            }
        return results;
    }
    public double run()
    {
        if(true)
            return a.matrixVersion.compareTo(b.matrixVersion);
        //return a.matrixVersion.compareToWithAdjust(b.matrixVersion);
        int good = 0;
        
        int maxFailures = (int) (a.arrayVersion.length * .3);
        for (int i = a.arrayVersion.length - 1; i >= 0; i--)
        {
            if(hasPixel(a.arrayVersion[i],b.arrayVersion))
            {
                good++;
            } else
            {
                maxFailures--;
            }
            if (maxFailures <= 0)
            {
                return .1;
            }
        }
        if (a.size > b.size)
        {
            return (double) good / a.size;
        } else
        {
            return (double) good / b.size;
        }
    }
private boolean hasPixel(pixel p, pixel [] blob)
{
    for (int i=blob.length-1;i>=0;i--)
    {
        if(p.x==blob[i].x && p.y==blob[i].y)
            return true;
    }
    return false;
}
       

    }