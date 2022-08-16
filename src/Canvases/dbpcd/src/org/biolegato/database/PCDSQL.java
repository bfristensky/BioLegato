/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.biolegato.database;

import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 *
 * @author alvare
 */
public class PCDSQL {
    private String sql = null;
    private JDBCDBConnection connection = null;
    private boolean fullquery = true;
    
    /**
     * Creates a new PCD SQL query object.
     **
     * @param connection the database connection to perform the SQL on
     * @param sql the SQL command to query
     * @param fullquery whether to print the full database connection details when outputting the query via pcdOut
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
