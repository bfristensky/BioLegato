/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.biolegato.database.fields;

import java.awt.Component;
import java.awt.Container;
import java.io.IOException;
import java.sql.Connection;
import java.util.Map;
import javax.swing.JButton;
import javax.swing.JFrame;
import org.biopcd.widgets.CommandButton;

/**
 *
 * @author alvare
 */
public class DBCommand extends CommandButton implements DBField {

    public final static String[] booleanValues = new String[]{"true", "false"};

    /**
     * Creates a new instance of a boolean field.
     * This version of the constructor is used by the FieldGenerator class
     **
     * @param label the label to display on the button.
     */
    public DBCommand (String label) {
        this(label, "");
    }

    /**
     * Creates a new instance of a boolean field.
     **
     * @param label the label to display on the button.
     * @param command the command to execute when the button is clicked
     */
    public DBCommand (String label, String command) {
        super("");
    }

    public void view(Connection conn, Container parent, Map<String,String> results) {
        view(conn, parent, results, true);
    }
    public void view(Connection conn, Container parent, Map<String,String> results, boolean enabled) {
        JButton button = new JButton(label);

        button.addActionListener(new DBCommandThread(command, results, turtle));
        button.setEnabled(enabled);
        parent.add(button);
    }

    public void edit(Connection conn, Container parent, Map<String,String> results) {
        view(conn, parent, results);
    }

    /**
     * Displays the current field in an editor panel.
     * This method should be similar to displayEdit
     * from BioPCD's Widget class (BioLegato ver. 0.7.9)
     **
     * @param mainFrame a JFrame object for any editor popup windows to be modal to
     * @param conn a database connection object - for reference fields
     * @return a component object representative of the current database field.
     */
    public Component schemaEditor(final Connection conn, final JFrame mainFrame) {
        return this.displayEdit(mainFrame);
    }

    /**
     * Writes the DBPCD representation of the database field to a writer object
     **
     * @param scope the level of scope to write the database field.
     * @param out the writer object to output the PCD code.
     * @param isKey whether the field is the primary key for the current schema
     */
    @Override
    public void dbpcdout (int scope, Appendable out, boolean isKey) throws IOException {
        for (int count = 0; count < scope; count++) {
            out.append("    ");
        }
        if (isKey) {
            out.append("key ");
        }
        out.append("button \"");
        out.append(label.replaceAll("\"", "\"\""));
        out.append("\"\n");
        for (int count = 0; count < scope + 1; count++) {
            out.append("    ");
        }
        out.append("exec \"");
        out.append(command.replaceAll("\"", "\"\""));
        out.append("\"\n");
    }

    /**
     * Writes the CREATE TABLE representation of the current database field to a writer object
     **
     * @param out the writer object to output the CREATE TABLE SQL-statement code.
     */
    public void createTable(Appendable out) throws IOException {
    }

    /**
     * Returns the database SQL name of the current field
     **
     * @return the name of the current database field object
     */
    public String getName() {
        return null;
    }

    /**
     * Returns the SQL command(s) or fragment(s) necessary to save the current
     * database field.  This function is exclusively called by DBSchema.
     **
     * @param keyvalue the value for the database key (null if it is a new entry)
     * @return the SQL command(s) or fragment(s) - for DBSchema
     */
    public String save(String keyvalue) {
        return null;
    }
}
