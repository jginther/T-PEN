/*
 * Copyright 2013 Saint Louis University. Licensed under the
 *	Educational Community License, Version 2.0 (the "License"); you may
 * not use this file except in compliance with the License. You may
 * obtain a copy of the License at
 *
 * http://www.osedu.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an "AS IS"
 * BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing
 * permissions and limitations under the License.
 */
package edu.slu.util;

import java.io.FileNotFoundException;
import java.util.LinkedHashMap;
import java.util.Map;


/**
 * Various utility methods to fill in for limitations in the Java language.
 *
 * @author tarkvara
 */
public class LangUtils {
   /**
    * Sometimes Throwable.getMessage() returns a useless string (e.g. "null" for a NullPointerException),
    * and we want to return a string which is more meaningful to the end-user.
    */
   public static String getMessage(Throwable t) {
      if (t instanceof NullPointerException) {
         return "Null pointer exception";
      } else if (t instanceof FileNotFoundException) {
         return String.format("File %s not found", t.getMessage());
      } else if (t instanceof ArrayIndexOutOfBoundsException) {
         return "Array index out of bounds: " + t.getMessage();
      } else if (t instanceof OutOfMemoryError) {
         return "Out of memory: " + t.getMessage();
      } else if (t instanceof NumberFormatException) {
         String msg = t.getMessage();
         int quotePos = msg.indexOf('\"');
         if (quotePos > 0) {
            // Exception message is of form "For input string: \"foo\"".
            return String.format("Unable to interpret %s as a number", msg.substring(quotePos));
         }
         return msg;
      } else {
         if (t.getMessage() != null) {
            return t.getMessage();
         } else {
            return t.toString();
         }
      }
   }

   /**
    * Utility function for creating and populating a map using var-args.
    * @param args list of key/value pairs
    * @return a map suitable for serialising to JSON
    */
   public static Map<String, Object> buildQuickMap(String... args) {
      Map<String, Object> result = new LinkedHashMap<>();
      for (int i = 0; i < args.length; i += 2) {
         result.put(args[i], args[i + 1]);
      }
      return result;
   }   
}
