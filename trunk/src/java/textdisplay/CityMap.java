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

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**Store additional information that needs to be sent with the city name for google maps to correctly plot the location.*/
public class CityMap {
    private String city;
    private String value;
    /**
     * Fetch information for a particular city. There is nothing to prevent the iclusion of country information, but we try to stick to just city name.
     * @param city the city name as stored in TPEN
     * @throws SQLException
     */
    public CityMap(String city) throws SQLException
    {
    String query="select * from citymap where city=?";
    Connection j=null;
    PreparedStatement ps=null;
    try{
        j=DatabaseWrapper.getConnection();
        ps=j.prepareStatement(query);
        ps.setString(1, city);
        ResultSet rs=ps.executeQuery();
        if(rs.next())
        {
            this.city=city;
            this.value=rs.getString("value");
        }
 else
        {
            ps=j.prepareStatement("insert into citymap(city,value) values(?,'')");
            ps.setString(1, city);
            ps.execute();
            this.city=city;
            this.value="";
 }
    }
    finally{
        DatabaseWrapper.closeDBConnection(j);
        DatabaseWrapper.closePreparedStatement(ps);

    }
}
    /**
     * Get the additional information stored for this city. Usually the country region or state
     */
    public String getValue()
    {
    return value;
}
    /**
     * Set the additional information stored for this city. Usually the country region or state
     */
    public void setValue(String value) throws SQLException
    {
    this.value=value;
    String query="update citymap set value=? where city=?";
    Connection j=null;
    PreparedStatement ps=null;
    try{
        j=DatabaseWrapper.getConnection();
        ps=j.prepareStatement(query);
        ps.setString(1, value);
        ps.setString(2, city);
        ps.execute();

    }
    finally{
        DatabaseWrapper.closeDBConnection(j);
        DatabaseWrapper.closePreparedStatement(ps);

    }
}

}
