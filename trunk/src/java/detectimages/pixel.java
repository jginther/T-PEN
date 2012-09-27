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

import java.io.Serializable;

public class pixel implements Comparable, Serializable {

    protected short x;
    protected short y;

    public pixel(int a, int b) {
        x = (short) a;
        y = (short) b;
    }

    public int compare(Object o1, Object o2) {

        pixel a = (pixel) o1;
        pixel b = (pixel) o2;
        if (a.x < b.x) {
            return 1;
        }
        if (a.x > b.x) {
            return -1;
        }
        return 0;




    }

    public int compareTo(Object o) {
        pixel b = (pixel) o;
        if (this.x < b.x) {
            return 1;
        }
        if (this.x > b.x) {
            return -1;
        }
        return 0;

    }
}
