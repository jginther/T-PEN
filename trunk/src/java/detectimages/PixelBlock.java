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

public class PixelBlock {
 private int size;
 public int [] block;
 public PixelBlock(int [][] matrix, int dim1, int dim2, int size)
 {
     this.size=size;
     block=new int[size*size];
     for (int i=0;i<size;i++)
         for(int j=0;j<size;j++)
         {
             if(dim1+i<matrix.length && dim2+j<matrix[0].length)
             {
                 block[i*size+j]=matrix[dim1+i][dim2+j];
             }
             else
             {
                 block[i*size+j]=0;
             }
         }
 }
 public int compare(PixelBlock p)
 {
     int result=0;
     for(int i=0;i<block.length;i++)
     {
         if(block[i]==1 && p.block[i]==1)
             result++;
     }
     return result;
 
 }         

}
