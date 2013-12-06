
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
/**This is a representation of the boolean matrix that describes a single glyph from a binarized image uses a single bit per pixel and bitwise and
 to run comparisons more quickly*/
public class altBlob {

    int [][] data;
    int [][][]dataOffset;
    //lookupTable is a table of hammering values (the number of set bits). This is cool because it allows the comparer to use an integer in which the set bits represent a black pixel
    //resulting in running 16 pixel overlay checks via a single bitwise and
    static int [] lookupTable;
    static{
        lookupTable=new int [65536];
        for(int i=0;i<lookupTable.length;i++)
        {
            lookupTable[i]=NumberOfSetBits(i);
        }
    }
    private static int NumberOfSetBits(int i)
{
    i = i - ((i >> 1) & 0x55555555);
    i = (i & 0x33333333) + ((i >> 2) & 0x33333333);
    return (((i + (i >> 4)) & 0x0F0F0F0F) * 0x01010101) >> 24;
}
    /**Create this type of blob, which is more efficient for comparison, from a regular blob*/
    public altBlob (blob b)
    {
        try{
        data=new int[b.height%16+1][b.width];
        int [][] thearr=b.matrixVersion.matrix;
        for(int i=0;i<data.length;i++)
            for(int k=0;k<data[0].length;k++)
            {
                int val=0;
                
                for(int ctr=0;ctr<16;ctr++)
                {

                    if(i*16+ctr<thearr.length&&(thearr[i*16+ctr][k])==1)
                    val+=Math.pow(2,ctr );
                }
                data[i][k]=val;

            }
        }
        catch(Exception e)
        {

            StackTraceElement [] er=e.getStackTrace();
            int l=0;
        }
    }
    public altBlob (blob b, int offset)
    {
        try{
        dataOffset=new int[offset][b.height%16+1][b.width];
        int [][] thearr=b.matrixVersion.matrix;
        
        
        for(int i=0;i<data.length;i++)
            for(int k=0;k<data[0].length;k++)
            {
                int val=0;
                
                for(int ctr=0;ctr<16;ctr++)
                {

                    if(i*16+ctr<thearr.length&&(thearr[i*16+ctr][k])==1)
                    val+=Math.pow(2,ctr );
                }
                dataOffset[0][i][k]=val;

            }
        
        for(int i=0;i<data.length;i++)
            for(int k=0;k<data[0].length;k++)
            {
                int val=0;
                
                for(int ctr=0;ctr<16;ctr++)
                {

                    if(i*16+ctr-1<thearr.length&&(thearr[i*16-1+ctr][k])==1)
                    val+=Math.pow(2,ctr );
                }
                dataOffset[1][i][k]=val;

            }
        for(int i=0;i<data.length;i++)
            for(int k=0;k<data[0].length;k++)
            {
                int val=0;
                
                for(int ctr=0;ctr<16;ctr++)
                {

                    if(i*16+ctr+1<thearr.length&&(thearr[i*16+1+ctr][k])==1)
                    val+=Math.pow(2,ctr );
                }
                dataOffset[2][i][k]=val;

            }
        
    }
        catch(Exception e)
        {

            StackTraceElement [] er=e.getStackTrace();
            int l=0;
        }
    }
    /**Compare 2 altblobs and return the count of overlapping pixels*/
    public int run(altBlob b)
    {
        int max0=data.length;
        if(b.data.length<max0)
            max0=b.data.length;
        int max1=data[0].length;
        if(b.data[0].length<max1)
            max1=b.data[0].length;

        int total=0;
        for(int i=0;i<max0;i++)
            for(int k=0;k<max1;k++)
            {
                
                total+=lookupTable[data[i][k]&b.data[i][k]];
               
            }
        return total;
    }
    public int run_offset(altBlob b)
    {
        int bigTotal=0;
        int max0=data.length;
        if(b.data.length<max0)
            max0=b.data.length;
        int max1=data[0].length;
        if(b.data[0].length<max1)
            max1=b.data[0].length;

        int total=0;
        for(int i=0;i<max0;i++)
            for(int k=0;k<max1;k++)
            {
                total+=lookupTable[data[i][k]&b.data[i][k]];
            }
        if(total>bigTotal)
            bigTotal=total;
        return bigTotal;
    }
    
    

}
