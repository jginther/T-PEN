/*
 * @author Jon Deering
Copyright 2011 Saint Louis University. Licensed under the Educational Community License, Version 2.0 (the "License"); you may not use
this file except in compliance with the License.

You may obtain a copy of the License at http://www.osedu.org/licenses/ECL-2.0

Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions
and limitations under the License.
 */
package utils;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Stack;
import textdisplay.DatabaseWrapper;

/**
 *
 * Checks for duplicate records that can be problematic. Not currently used.
 */
public class duplicateMerger {

    public static String checkDups() throws SQLException {
        String toret = "";
        String query = "select distinct pageName, collection, archive from folios";
        String folioNumberQuery = "select pageNumber from folios where pageName=? and collection=? and archive=?";
        String transcriptionCountQuery = "select count(line) from transcription where folio=?";

        Connection j = null;
        PreparedStatement ps = null;
        try {
            j = DatabaseWrapper.getConnection();
            ps = j.prepareStatement(folioNumberQuery);

            ResultSet pages = ps.executeQuery(query);
            while (pages.next()) {
                ps.setString(1, pages.getString("pageName"));
                ps.setString(2, pages.getString("collection"));
                ps.setString(3, pages.getString("archive"));
                ResultSet folioCount = ps.executeQuery();
                Stack<Integer> folios = new Stack();
                System.out.print("testing " + pages.getString("pageName"));
                toret += "testing " + pages.getString("pageName");
                while (folioCount.next()) {
                    folios.push(folioCount.getInt("pageNumber"));
                }
                if (folios.size() > 1) {
                    toret += ("found duplicate group ");
                    System.out.print("found duplicate group ");
                    while (!folios.isEmpty()) {
                        int thisOne = folios.pop();
                        System.out.print(" " + thisOne);
                        toret += (" " + thisOne);
                    }
                    toret += "\n<br>";
                    System.out.print("\n");
                }
            }

            return toret;
        } finally {
            if (j != null) {
                DatabaseWrapper.closeDBConnection(j);
            }
            DatabaseWrapper.closePreparedStatement(ps);
        }
    }
}
