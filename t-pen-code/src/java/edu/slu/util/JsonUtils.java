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

import java.awt.Rectangle;
import java.io.IOException;
import java.util.List;
import java.util.Map;


/**
 * A collection of utility methods for dealing with JSON and JSON-LD data.
 *
 * @author tarkvara
 */
public class JsonUtils {
   /**
    * Get an object from a JSON object.
    * @param map map in which the object is found
    * @param key key to look for
    * @param nonNull if true, throw an exception for null values
    * @return the object at the given key
    * @throws IOException if the JSON input is malformed
    */
   public static Map<String, Object> getObject(Map<String, Object> map, String key, boolean nonNull) throws IOException {
      Object obj = map.get(key);
      if (obj == null && nonNull) {
         throw new IOException(String.format("Malformed JSON input: missing \"%s\" entry.", key));
      }
      if (obj != null && !(obj instanceof Map)) {
         throw new IOException(String.format("Malformed JSON input: \"%s\" entry is not an object.", key));
      }
      return (Map<String, Object>)obj;
   }
              
   /**
    * Get an array from a JSON map.
    * @param map map in which the object is found
    * @param key key to look for
    * @param nonNull of true, throw an exception for null values
    * @return the object at the given key
    * @throws IOException if the JSON input is malformed
    */
   public static List<Object> getArray(Map<String, Object> map, String key, boolean nonNull) throws IOException {
      Object obj = map.get(key);
      if (obj == null && nonNull) {
         throw new IOException(String.format("Malformed JSON input: missing \"%s\" entry.", key));
      }
      if (obj != null && !(obj instanceof List)) {
         throw new IOException(String.format("Malformed JSON input: \"%s\" entry is not an array.", key));
      }
      return (List<Object>)obj;
   }

   /**
    * Extract a rectangle which has been serialised to JSON as x,y,w,h.
    * @param bounds map containing bounds values
    * @return the rectangle which was serialised
    * @throws IOException if any of the coordinates can't be interpreted
    */
   public static Rectangle getRectangle(Map<String, Object> bounds) throws IOException {
      return new Rectangle(getInt(bounds, "x"), getInt(bounds, "y"), getInt(bounds, "width"), getInt(bounds, "height"));
   }

   /**
    * Extract an integer value from a JSON map.  Used for extracting coordinates from rectangles.
    * @param map map containing integers of interest
    * @param key field which contains the value
    * @return the field's value as an int.
    */
   public static int getInt(Map<String, Object> map, String key) throws IOException {
      Object o = map.get(key);
      if (o == null || !(o instanceof Number)) {
         throw new IOException(String.format("Malformed JSON input: unable to extract numeric value for \"%s\"", key));
      }
      return ((Number)o).intValue();
   }

   /**
    * Extract the bounding rectangle from the target portion of a URI.
    * @param str URI to be parsed
    * @return rectangle stored as #xywh=1,2,3,4, or null
    */
   public static Rectangle getXYWH(String str) {
      int hashPos = str.lastIndexOf("#xywh=");
      if (hashPos >= 0) {
         String[] coordStrs = str.substring(hashPos + 6).split(",");
         if (coordStrs.length == 4) {
            return new Rectangle(Integer.parseInt(coordStrs[0]), Integer.parseInt(coordStrs[1]), Integer.parseInt(coordStrs[2]), Integer.parseInt(coordStrs[3]));
         }
      }
      return null;
   }
}
