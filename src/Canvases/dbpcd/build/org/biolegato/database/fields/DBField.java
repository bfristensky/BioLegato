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

/**
 *
 * @author alvare
 */
public interface DBField {
    /**
     * Displays the content of a field within a dataset entry.  The dataset
     * entry is represented by the object 'results'.  The display mode is for
     * viewing purposes, and does not let the user edit the entry content.
     **
     * @param conn the database connection to use for any further queries
     * @param parent the parent container to add components to for displaying the field
     * @param results the object containing all of the fields and values for a given database entry
     */
    public void view(Connection conn, Container parent, Map<String,String> results);

    /**
     * Displays the content of a field within a dataset entry.  The dataset
     * entry is represented by the object 'results'.  The display mode is for
     * editing purposes, and allows the user to edit the content of the field.
     **
     * @param conn the database connection to use for any further queries
     * @param parent the parent container to add components to for displaying the field
     * @param results the object containing all of the fields and values for a given database entry
     */
    public void edit(Connection conn, Container parent, Map<String,String> results);
    
    /**
     * Displays the current field in an editor panel.
     * This method should be similar to displayEdit
     * from BioPCD's Widget class (BioLegato ver. 0.7.9)
     **
     * @param mainFrame a JFrame object for any editor popup windows to be modal to
     * @param conn a database connection object - for reference fields
     * @return a component object representative of the current database field.
     */
    public Component schemaEditor(final Connection conn, final JFrame mainFrame);

    /**
     * Writes the DBPCD representation of the database field to a writer object
     **
     * @param scope the level of scope to write the database field.
     * @param out the writer object to output the PCD code.
     * @param isKey whether the field is the primary key for the current schema
     */
    public void dbpcdout (int scope, Appendable out, boolean isKey) throws IOException;

    /**
     * Writes the CREATE TABLE representation of the current database field to a writer object
     **
     * @param out the writer object to output the CREATE TABLE SQL-statement code.
     */
    public void createTable (Appendable out) throws IOException;

    /**
     * Returns the database SQL name of the current field
     **
     * @return the name of the current database field object
     */
    public String getName();

    /**
     * Returns the SQL command(s) or fragment(s) necessary to save the current
     * database field.  This function is exclusively called by DBSchema.
     **
     * @param keyvalue the value for the database key (null if it is a new entry)
     * @return the SQL command(s) or fragment(s) - for DBSchema
     */
    public String save(String keyvalue);
}
