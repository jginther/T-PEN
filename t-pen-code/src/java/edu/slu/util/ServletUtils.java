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
import java.lang.reflect.InvocationTargetException;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import com.mysql.jdbc.exceptions.jdbc4.MySQLIntegrityConstraintViolationException;
import textdisplay.Folio;


/**
 * Various utility methods for use by servlets.
 *
 * @author tarkvara
 */
public class ServletUtils {
   /**
    * Get the base content type without any trailing optional elements like charset.
    */
   public static String getBaseContentType(String contentType) {
      int semiPos = contentType.indexOf(';');
      if (semiPos > 0) {
         contentType = contentType.substring(0, semiPos);
      }
      return contentType;
   }

   /**
    * Get the base content type without any trailing optional elements like charset.
    */
   public static String getBaseContentType(HttpServletRequest req) {
      return getBaseContentType(req.getContentType());
   }

   /**
    * Get the currently logged in UID.
    * @param req request from client
    * @param resp response back to client
    * @return the UID from the current session, or -1 if none
    */
   public static int getUID(HttpServletRequest req, HttpServletResponse resp) throws IOException {
      HttpSession sess = req.getSession();
      if (sess != null) {
         Object uidStr = sess.getAttribute("UID");
         if (uidStr != null) {
            return Integer.parseInt(uidStr.toString());
         }
      }
      resp.sendError(HttpServletResponse.SC_UNAUTHORIZED);
      return -1;
   }
   
   /**
    * Check for the given host in a version.properties setting.
    */
   public static boolean verifyHostInList(String host, String propName) {
      String propVal = Folio.getRbTok(propName);
      if (propVal != null) {
         String[] propHosts = propVal.split(",");
         for (String h: propHosts) {
            if (host.equals(h)) {
               return true;
            }
         }
      }
      return false;
   }
   /**
    * Report a servlet-related error to the client.
    * @param resp response for passing stuff back to the client
    * @param code HTTP error code
    * @param ex exception which was caught
    * @param msg human-friendly error message
    * @throws IOException 
    */
   public static void reportError(HttpServletResponse resp, int code, Throwable ex, String msg) throws IOException {
      LOG.log(Level.SEVERE, msg, ex);
      resp.sendError(code, String.format("%s: %s", msg, LangUtils.getMessage(ex)));
   }

   /**
    * Handle some commonly thrown internal exceptions.
    * @param resp
    * @param ex
    * @throws IOException 
    */
   public static void reportInternalError(HttpServletResponse resp, Throwable ex) throws IOException {
      if (ex instanceof InvocationTargetException) {
         reportInternalError(resp, ex.getCause());
      } else if (ex instanceof MySQLIntegrityConstraintViolationException) {
         reportError(resp, HttpServletResponse.SC_CONFLICT, ex, "Database integrity violation");
      } else if (ex instanceof SQLException) {
         reportError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, ex, "Database error");
      } else if (ex instanceof NumberFormatException) {
         reportError(resp, HttpServletResponse.SC_BAD_REQUEST, ex, "Unable to parse number");
      } else if (ex instanceof NoSuchMethodException) {
         resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "No such method: " + ex.getMessage());
      } else {
         reportError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, ex, "Internal server error");
      }
   }

   private static final Logger LOG = Logger.getLogger(ServletUtils.class.getName());
}
