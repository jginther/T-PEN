
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

import java.util.Hashtable;
import java.util.Vector;

public class blobManager {
private Hashtable<String,Vector<blob>> blobs;
final int size=50;
public blobManager()
{
    blobs=new Hashtable();
}
public Vector<blob> get(String filename)
{
    if(blobs.containsKey(filename))
    return blobs.get(filename);
    else
        return null;

}
public synchronized void add(Vector<blob> b, String filename)
{
    if(Runtime.getRuntime().freeMemory()<50000000)
    {
        System.out.print("No space for new cache entry"+filename+"\n");
        blobs.remove(blobs.keys().nextElement());
        


    }

    blobs.put(filename, b);

}
public void destroy()
{
    this.blobs=null;
    Runtime.getRuntime().gc();
}
}
