/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.biopcd.sql;

import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * (EXPERIMENTAL!)
 * A class to store SQL queries read in by PCD.  The main purpose of this class
 * (as opposed to just a simple SQL string) is to link JDBCDB connection objects
 * to SQL queries.  Additionally, this class allows for flags to be associated
 * with the SQL queriers.
 **
 * @author Graham Alvare
 * @author Brian Fristensky
 */
public class PCDSQL {
    /**
     * The SQL statement represented by the PCDSQL object
     */
    private String sql = null;
    /**
     * The connection object to execute the SQL command with.
     */
    private JDBCDBConnection connection = null;
    /**
     * Whether or not the query and connection were specified as a "fullquery"
     * in the PCD code.  A full query means that the connection and SQL query
     * were specified together and are inseparable.
     */
    private boolean fullquery = true;
    
    /**
     * Creates a new PCD SQL query object.
     **
     * @param connection the database connection to perform the SQL on
     * @param sql        the SQL command to query
     * @param fullquery  whether to print the full database connection
     *                   details when outputting the query via pcdOut
     */
    public PCDSQL (JDBCDBConnection connection, String sql, boolean fullquery) {
        System.out.println("new PCDSQL!");

        this.sql = sql;
        this.connection = connection;
        this.fullquery = fullquery;
    }

    public ResultSet query() {
        // The corresponding Java database connection object
        Statement stmnt   = null;
        ResultSet results = null;
        Connection connect = connection.connect();

        if (connect != null) {
            try {
                stmnt = connect.createStatement();
                if (stmnt.execute(sql)) {
                    results = stmnt.getResultSet();
                }
                connect.close();
            } catch (SQLException ex) {
                ex.printStackTrace(System.err);
            }
        }

        return results;
    }

    /**
     * Writes the BioPCD representation of the PCD SQL command
     **
     * @param scope the level of scope to write the menu widget.
     * @param out the writer object to output the BioPCD code.
     */
    public void pcdOut(int scope, Appendable out) throws IOException {
        // If it is a full query, print the connection's PCD code first,
        // then print the PCD code for the query.  Please note that a
        // connection's PCD code will not terminate the line, so the query
        // tag may be appended directly preceding the connetion PCD code.
        if (fullquery) {
            connection.pcdOut(scope, out);
            out.append(" ");
        } else {
            for (int count = 0; count < scope; count++) {
                out.append("    ");
            }
        }
        out.append("query \"");
        out.append(sql.replaceAll("\"", "\"\""));
    }
}
