/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package textdisplay;

import java.util.Vector;

/**
 *
 * @author jim
 */
 /**Inner calss for storing default parameter values gleened from a schema. These are only used in the automatic population
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
