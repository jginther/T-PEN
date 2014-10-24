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

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.bind.DatatypeConverter;


/**
 * Various network-related utility functions.
 *
 * @author tarkvara
 */
public class NetUtils {
   /**
    * Connect to the given URL using basic authentication.
    *
    * @param url URL to connect to
    * @param user user-name to use
    * @param pass password to use
    * @return <code>InputStream</code> to be used (e.g. for loading images)
    */
   public static InputStream openBasicAuthStream(URL url, String user, String pass) throws IOException {
      HttpURLConnection conn = (HttpURLConnection)url.openConnection();
      String userPassword = user + ":" + pass;
      String base64 = DatatypeConverter.printBase64Binary(userPassword.getBytes("ISO-8859-1"));
      conn.setRequestProperty("Authorization", "Basic " + base64);
      
      InputStream result = conn.getInputStream();
		int sc = conn.getResponseCode();
      LOG.log(Level.INFO, "Basic auth {0} returned {1} as stream, {2} as response", new Object[] { base64, result, sc });
      if (sc == 302) {
         url = new URL(conn.getHeaderField("Location"));
         LOG.log(Level.INFO, "Moved.  Retrying basic auth at {0}", url);
         return openBasicAuthStream(url, user, pass);
      }

      return result;
   }
   
   public static InputStream openCookiedStream(URL url, String cookieVal) throws IOException {
      HttpURLConnection conn = (HttpURLConnection)url.openConnection();
      conn.setRequestProperty("Cookie", cookieVal);
      return conn.getInputStream();
   }

   public static String loadCookie(URL url) throws IOException {
      URLConnection conn = url.openConnection();
      conn.connect();
      Map<String, List<String>> headers = conn.getHeaderFields();
      List<String> values = headers.get("Set-Cookie");
      if (values.size() > 0) {
         return values.get(0);
      }
      return "";
   }
   
   private static final Logger LOG = Logger.getLogger(NetUtils.class.getName());
}
