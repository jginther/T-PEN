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

import com.nativelibs4java.opencl.*;
import com.nativelibs4java.opencl.CLMem.*;
import com.nativelibs4java.opencl.util.*;
import com.nativelibs4java.util.*;
import java.io.File;
import java.io.IOException;
import org.bridj.Pointer;
import static org.bridj.Pointer.*;
import static java.lang.Math.*;
import java.nio.ByteOrder;


public class BlockBlob {

    static final int size = 8;
    PixelBlock[][] blocks;

    public BlockBlob(matrixBlob b) {
        blocks = new PixelBlock[b.matrix.length / size + 1][b.matrix[0].length / size + 1];
        for (int i = 0; i < blocks.length; i++) {
            for (int j = 0; j < blocks[0].length; j++) {
                blocks[i][j] = new PixelBlock(b.matrix, i * size, j * size, size);
            }
        }
    }

    public int compare(BlockBlob b) {
        int result = 0;
        if (true) {
            return blocks[0][0].compare(b.blocks[0][0]);
        }
        for (int i = 0; i < blocks.length; i++) {
            for (int j = 0; j < blocks[0].length; j++) {
                if (i < b.blocks.length && j < b.blocks[0].length) {
                    result += blocks[i][j].compare(b.blocks[i][j]);
                }
            }
        }

        return result;
    }
    static CLKernel compareKernel = null;

    public int[] openCLcompare(BlockBlob[] blockBlobs, int blockDim1, int blockDim2) throws IOException {
        blockDim1 = 0;
        blockDim2 = 0;

        int[] results = new int[blockBlobs.length];
        CLContext context = JavaCL.createBestContext();
        CLQueue queue = context.createDefaultQueue();
        ByteOrder byteOrder = context.getByteOrder();


        // .order(byteOrder),



        int totalCompareBlocks = blockBlobs.length;
        Pointer<Integer> aPtr = allocateInts(totalCompareBlocks * size * size).order(byteOrder);
        int ctr = 0;
        for (int i = 0; i < totalCompareBlocks; i++) {
            if (blocks.length >= blockDim1 && blocks[0].length >= blockDim2) {
                for (int j = 0; j < size * size; j++) {
                    aPtr.set(ctr, blockBlobs[i].blocks[0][0].block[j]);
                    ctr++;
                }
            }
        }
        Pointer<Integer> bPtr = allocateInts(size * size).order(byteOrder);
        for (int i = 0; i < size * size; i++) {
            bPtr.set(i, this.blocks[0][0].block[i]);

        }

        // Create OpenCL input buffers (using the native memory pointers aPtr and bPtr) :
        CLBuffer<Integer> baseBlock = context.createBuffer(Usage.Input, bPtr);
        CLBuffer<Integer> compareBlocks = context.createBuffer(Usage.Input, aPtr);

        // Create an OpenCL output buffer :
        CLBuffer<Integer> result = context.createIntBuffer(Usage.Input, blockBlobs.length);

        // Read the program sources and compile them :
        String src = IOUtils.readText(new File("/usr/blockCompare.cl"));

        CLProgram program = context.createProgram(src);

        // Get and call the kernel :
        //   System.out.print("creating kernel\n");
        //if(compareKernel==null)
        compareKernel = program.createKernel("blockCompare");
        compareKernel.setArgs(baseBlock, compareBlocks, result, size * size);
        CLEvent addEvt = compareKernel.enqueueNDRange(queue, new int[]{totalCompareBlocks});
        // System.out.print("ran kernel\n");
        int[] cpuresults = new int[totalCompareBlocks];
        for (int j = 0; j < totalCompareBlocks; j++) {

            int ctr2 = 0;
            for (int i = 0; i < size * size; i++) {
                if (bPtr.get(i) == 1 && aPtr.get(j * size * size + i) == bPtr.get(i)) {
                    ctr2++;
                }
            }
            cpuresults[j] = ctr2;
        }
        Pointer<Integer> outPtr = result.read(queue, addEvt); // blocks until add_floats finished
        // Print the first 10 output values :
        for (int i = 0; i < totalCompareBlocks; i++) {
            results[i] = outPtr.get(i);
        }
        outPtr = null;
        aPtr = null;
        bPtr = null;

        compareBlocks.release();
        result.release();
        baseBlock.release();
        compareBlocks = null;
        result = null;
        baseBlock = null;
        return results;
    }
}
