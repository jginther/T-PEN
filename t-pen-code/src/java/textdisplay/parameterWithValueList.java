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

import java.util.Vector;

 /**Inner class for storing default parameter values gleened from a schema. These are only used in the automatic population
     of project buttons from a schema, hence there isnt a mechanism built into the tagbutton class to account for them.*/
    public class parameterWithValueList
    {
        public String parameter;
        public Vector <String> values;
        public parameterWithValueList(String parameterVal)
        {
            parameter=parameterVal;
            values=new Vector();
        }


    }
