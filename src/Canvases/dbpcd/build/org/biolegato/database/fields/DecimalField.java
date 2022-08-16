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
import javax.swing.JFrame;
import javax.swing.JLabel;
import org.biopcd.widgets.TextWidget;

/**
 *
 * @author alvare
 */
public class DecimalField extends TextWidget implements DBField {

    /**
     * Creates a new instance of a decimal field.
     * This version of the constructor is used by the FieldGenerator class
     **
     * @param name the database key of the current field
     */
    public DecimalField (String name) {
        this(name, "New field");
    }

    /**
     * Creates a new instance of a decimal field.
     **
     * @param name the database key of the current field
     * @param label the label to display representing the parameter to be manipulated by the text field.
     */
    public DecimalField (String name, String label) {
        super(name, label, "");
    }

    /**
     * Displays the content of a field within a dataset entry.  The dataset
     * entry is represented by the object 'results'.  The display mode is for
     * viewing purposes, and does not let the user edit the entry content.
     **
     * @param conn the database connection to use for any further queries
     * @param parent the parent container to add components to for displaying the field
     * @param results the object containing all of the fields and values for a given database entry
     */
    public void view(Connection conn, Container parent, Map<String,String> results) {
        // the name MUST be upper case!
        parent.add(new JLabel(label + ": " + results.get(name.toUpperCase())));
    }

    /**
     * Displays the content of a field within a dataset entry.  The dataset
     * entry is represented by the object 'results'.  The display mode is for
     * editing purposes, and allows the user to edit the content of the field.
     **
     * @param conn the database connection to use for any further queries
     * @param parent the parent container to add components to for displaying the field
     * @param results the object containing all of the fields and values for a given database entry
     */
    public void edit(Connection conn, Container parent, Map<String,String> results) {
        // the name MUST be upper case!
        if (results != null) {
            this.setValue(results.get(name.toUpperCase()));
        }
        this.display(parent, null);
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
        out.append("float \"");
        out.append(name.replaceAll("\"", "\"\""));
        out.append("\"\n");
        for (int count = 0; count < scope + 1; count++) {
            out.append("    ");
        }
        out.append("label \"");
        out.append(label.replaceAll("\"", "\"\""));
        out.append("\"\n");
    }

    /**
     * Writes the CREATE TABLE representation of the current database field to a writer object
     **
     * @param out the writer object to output the CREATE TABLE SQL-statement code.
     */
    public void createTable(Appendable out) throws IOException {
        out.append(name);
        out.append(" float not null");
    }

    /**
     * Returns the database SQL name of the current field
     **
     * @return the name of the current database field object
     */
    public String getName() {
        return super.name;
    }

    /**
     * Returns the database SQL value of the current field
     **
     * @return the value of the current database field object
     */
    public String getValue() {
        return super.getInstance().getValue().toString();
    }

    /**
     * Returns the database SQL type of the current field
     **
     * @return the type of the current database field object
     */
    public String getType() {
        return "float";
    }

    /**
     * Returns the SQL command(s) or fragment(s) necessary to save the current
     * database field.  This function is exclusively called by DBSchema.
     **
     * @param keyvalue the value for the database key (null if it is a new entry)
     * @return the SQL command(s) or fragment(s) - for DBSchema
     */
    public String save(String keyvalue) {
        String result = "'" + getInstance().getValue().toString().replaceAll("'", "\\\'") + "'";

        if (keyvalue != null) {
            result = name + " = " + result;
        }
        return result;
    }
}
