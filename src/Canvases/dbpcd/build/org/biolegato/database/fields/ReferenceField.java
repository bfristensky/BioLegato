/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.biolegato.database.fields;

import java.awt.Component;
import java.awt.Container;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javax.swing.AbstractAction;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import org.biolegato.database.DBSchema;
import org.biopcd.widgets.SimpleWidget;
import org.biopcd.widgets.WidgetInstance;
import org.biolegato.database.EntriesListModel;

/**
 *
 * @author alvare
 */
public class ReferenceField implements DBField {

    /**
     * The name of the table for which the references are stored.
     */
    protected String name;
    /**
     * The root schema object - the schema which is being referenced from
     *
     * This is really an arbitrary designation since both schemas 'root'
     * and 'reference' can be read by the same table (i.e. the table is
     * bidirectional/cross-referencing).  The 'root' schema is just the
     * schema which we are examining with emphasis.
     *
     * For example (to clarify the above):
     * if you load a schema 'abc' into your current BLDB window, and schema
     * 'abc' has a  reference field for another schema, 'def', then 'abc'
     * would be the root schema, and 'def' would be the reference shema.
     */
    protected DBSchema root;
    /**
     * The reference schema object - the schema which is being referenced
     *
     * This is really an arbitrary designation since both schemas 'root'
     * and 'reference' can be read by the same table (i.e. the table is
     * bidirectional/cross-referencing).  The 'root' schema is just the
     * schema which we are examining with emphasis.
     *
     * For example (to clarify the above):
     * if you load a schema 'abc' into your current BLDB window, and schema
     * 'abc' has a  reference field for another schema, 'def', then 'abc'
     * would be the root schema, and 'def' would be the reference shema.
     */
    protected DBSchema reference;
    /**
     * the label for the text field
     */
    protected String label = null;
    /**
     * The list containing the data.
     */
    protected transient JList choice_list = null;
    /**
     * The list of indices selected
     */
    protected int[] indices = new int[0];

    /**
     * Creates a new instance of a reference field.
     * This version of the constructor is used by the FieldGenerator class
     **
     * @param name the database key of the current field
     * @param root the root schema to reference from
     * @param reference the schema to reference
     */
    public ReferenceField (String name, DBSchema root, DBSchema reference) {
        this(name, "New field", root, reference);
    }

    /**
     * Creates a new instance of a reference field.
     * This version of the constructor is used by the FieldGenerator class
     **
     * @param name the database key of the current field
     * @param label the label to display representing the parameter to be manipulated by the text field.
     * @param root the root schema to reference from
     * @param reference the schema to reference
     */
    public ReferenceField (String name, String label, DBSchema root, DBSchema reference) {
        this.name  = name;
        this.label = label;
        this.root  = root;
        this.reference = reference;
    }

    /**
     * Creates a new instance of a reference field.
     **
     * @param name the database key of the current field
     * @param label the label to display representing the parameter to be manipulated by the text field.
     * @param rootTable the table from which the reference comes
     * @param rootKey the primary key of the table from which the reference comes
     * @param tableRef the table to reference
     * @param keyRef the primary key of the table to reference
     * @param refName the column in the reference table to display
     */
    public ReferenceField (String name, String label, String rootTable, KeyableField rootKey, String tableRef, KeyableField keyRef, DBField refName) {
        this(name, label, new DBSchema(rootTable, rootKey), new DBSchema(tableRef, keyRef, refName));
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
        Statement retrieve   = null;
        ResultSet refresults = null;
        JPanel panel = new JPanel();
        String refKey   = reference.getKey().getName();
        String rootKey  = root.getKey().getName();
        String keyValue = results.get(root.getKey().getName().toUpperCase());
        StringBuilder resultString = new StringBuilder();

        panel.setLayout(new BoxLayout(panel, BoxLayout.LINE_AXIS));
        panel.add(new JLabel(label + ": "));
        
        try {
            retrieve = conn.createStatement();
            System.out.println("Ref Query:  SELECT " + refKey + " FROM " + name + " WHERE " + rootKey + " = '" + keyValue.replaceAll("'", "\\\'") + "'");
            refresults = retrieve.executeQuery("SELECT " + refKey + " FROM " + name + " WHERE " + rootKey + " = '" + keyValue.replaceAll("'", "\\\'") + "'");

            while (refresults.next()) {
                resultString.append(refresults.getString(refKey));
                resultString.append("\n");
            }
            panel.add(new JLabel(resultString.toString()));
        } catch (SQLException ex) {
            panel.add(new JLabel(" --- DB REF ERROR! --- "));
            ex.printStackTrace(System.err);
        } finally {
            if (refresults != null) {
                try {
                    refresults.close();
                } catch (SQLException ex) {
                    ex.printStackTrace(System.err);
                }
            }
            if (retrieve != null) {
                try {
                    retrieve.close();
                } catch (SQLException ex) {
                    ex.printStackTrace(System.err);
                }
            }
        }
        parent.add(panel);
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
        int[] resultarray;
        String refKey   = reference.getKey().getName();
        String rootKey  = root.getKey().getName();
        Statement retrieve   = null;
        ResultSet refresults = null;
        // a box to store the label beside the combobox (for alignment purposes)
        Box result = new Box(BoxLayout.LINE_AXIS);
        // the list model for selecting database entries from
        EntriesListModel model = new EntriesListModel(conn, reference);
        // the temporary array list which stores the indices to select within
        // the choice_list object.  The selection is based on what values
        // are stored in the reference table.  This step needs to be done
        // becuase Java's JList object only supports setSelectIndices for
        // programmatically selecting mulitple items in a list
        List<Integer> resultindices = new ArrayList<Integer>();

        // add the label to the box "result", if the value of the string "label" is not null
        if (label != null) {
            result.add(new JLabel(label));
        }

        // a combo box which returns its value when getValue is called
        choice_list = new JList(model);
        model.checkupdate();

        // handle the default value for the list
        choice_list.setSelectedIndices(indices);

        // make the choice_list only support single selection
        choice_list.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);

        // if the map is not empty or null, set the value
        // of the current widget to whatever the results are
        if (results != null && results.containsKey(rootKey.toUpperCase())) {
            System.out.println("rootKey found!");
            try {
                String keyValue = results.get(rootKey.toUpperCase());

                System.out.println("    value: " + keyValue);
                retrieve = conn.createStatement();
                System.out.println("    query: SELECT " + refKey + " FROM " + name + " WHERE " + rootKey + " = '" + keyValue.replaceAll("'", "\\\'") + "'");
                refresults = retrieve.executeQuery("SELECT " + refKey + " FROM " + name + " WHERE " + rootKey + " = '" + keyValue.replaceAll("'", "\\\'") + "'");

                while (refresults.next()) {
                    Integer index = model.findIndex(refresults.getString(refKey));
                    System.out.println("        index: " + index + " --- " + refresults.getString(refKey));

                    if (index != null) {
                        resultindices.add(index);
                    }
                }

                resultarray = new int[resultindices.size()];

                for (int count = 0; count < resultarray.length; count++) {
                    resultarray[count] = resultindices.get(count);
                }
                choice_list.setSelectedIndices(resultarray);
            } catch (SQLException ex) {
                ex.printStackTrace(System.err);
            } finally {
                if (refresults != null) {
                    try {
                        refresults.close();
                    } catch (SQLException ex) {
                        ex.printStackTrace(System.err);
                    }
                }
                if (retrieve != null) {
                    try {
                        retrieve.close();
                    } catch (SQLException ex) {
                        ex.printStackTrace(System.err);
                    }
                }
            }
        }
        
        // add the choice list (and make it scrollable)
        result.add(new JScrollPane(choice_list, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED));

        // adds the combobox to the destination
        parent.add(result);
    }

    /**
     * Displays the current field in an editor panel.
     * This method was copied and adapted from displayEdit
     * in SimpleWidget (BioLegato ver. 0.7.9)
     **
     * @param mainFrame a JFrame object for any editor popup windows to be modal to
     * @param conn a database connection object - for reference fields
     * @return a component object representative of the current widget.
     */
    public Component schemaEditor(final Connection conn, final JFrame mainFrame) {
        final JLabel lbl = new JLabel();
        JPanel panel = new JPanel();

        edit(conn, panel, null);
        
        SimpleWidget.editImage(lbl, panel);
        lbl.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent event) {
                if (event.isPopupTrigger() || event.getClickCount() == 2) {
                    final JDialog propertiesWindow = new JDialog(mainFrame, true);
                    JPanel panel = new JPanel();
                    JPanel namePanel = new JPanel();
                    final JTextField nameField = new JTextField(name, 20);
                    panel.setLayout(new BoxLayout(panel, BoxLayout.PAGE_AXIS));
                    namePanel.setLayout(new BoxLayout(namePanel, BoxLayout.LINE_AXIS));

                    namePanel.add(new JLabel("Variable name"));
                    namePanel.add(nameField);

                    panel.add(namePanel);

                    final ActionListener listener = editWindow(panel);

                    panel.add(new JButton(new AbstractAction("Update") {

                        public void actionPerformed(ActionEvent e) {
                            JPanel panel = new JPanel();

                            listener.actionPerformed(e);
                            name = nameField.getText();
                            propertiesWindow.dispose();

                            edit(conn, panel, null);

                            SimpleWidget.editImage(lbl, panel);
                        }
                    }));
                    propertiesWindow.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
                    propertiesWindow.add(panel);
                    propertiesWindow.pack();
                    propertiesWindow.setVisible(true);
                }
                event.consume();
            }
        });
        return lbl;
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
        if (isKey) {
            throw new UnsupportedOperationException("CANNOT USE A REFERENCE KEY AS A PRIMARY KEY!");
        }

        /////////
        // print the 'ref' "parent" field
        /////////
        // Synopsis: The 'ref' tag specifies that the object within the next
        //           scope level specifies a reference field.  The name of the
        //           reference field is the table/schema name within the
        //           database back end that stores the reference between the
        //           two tables cross-referenced by the field.
        /////////
        // print the scope spacing before each field
        for (int count = 0; count < scope; count++) {
            out.append("    ");
        }
        out.append("ref \"");
        out.append(name.replaceAll("\"", "\"\""));
        out.append("\"\n");

        /////////
        // print the 'label' field
        /////////
        // Synopsis: The 'label' tag is a general property of all BLDB fields.
        //           The 'label' tag denotes the human readable name for the
        //           BLDB field.  This label is displayed in a JLabel object
        //           to the left of the field, when the field is displayed.
        /////////
        // print the scope spacing before each field
        for (int count = 0; count < scope + 1; count++) {
            out.append("    ");
        }
        out.append("label \"");
        out.append(label.replaceAll("\"", "\"\""));
        out.append("\"\n");
        
        /////////
        // print the 'table' field
        /////////
        // Synopsis: The 'table' field denotes which table to cross-reference.
        //           The table's DBPCD file will be read for further schema
        //           information, such as the primary key and name column, thus
        //           the table must agree with the .schema file rather than the
        //           actual back-end table name (however, both names - the
        //           .schema file and the back-end SQL table name should
        //           be the same!)
        /////////
        // print the scope spacing before each field
        for (int count = 0; count < scope + 1; count++) {
            out.append("    ");
        }
        out.append("table \"");
        out.append(reference.getTable().replaceAll("\"", "\"\""));
        out.append("\"\n");
    }


    /**
     * Writes the CREATE TABLE representation of the current database field to a writer object
     **
     * @param out the writer object to output the CREATE TABLE SQL-statement code.
     */
    public void createTable(Appendable out) throws IOException {
        out.append("CREATE TABLE ");
        out.append(name);
        out.append(" (");
        root.getKey().createTable(out);
        out.append(",");
        reference.getKey().createTable(out);
        out.append(");");
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
        boolean first_entry = true;
        StringBuilder sql = new StringBuilder();

        // delete any existing references from the reference table
        // (we will be replacing them all with new reference entries)
        if (keyvalue != null) {
            sql.append("DELETE FROM ");
            sql.append(name);
            sql.append(" WHERE ");
            sql.append(root.getKey().getName());
            sql.append(" = '");
            sql.append(keyvalue);
            sql.append("'; ");
        }

        // insert the new reference entries into the cross-reference table
        sql.append("INSERT INTO ");
        sql.append(name);
        sql.append(" (");
        sql.append(root.getKey().getName());
        sql.append(", ");
        sql.append(reference.getKey().getName());
        sql.append(") VALUES ");
        for (Object value : choice_list.getSelectedValues()) {
            if (!first_entry) {
                sql.append(",");
            }
            sql.append("('");
            sql.append(keyvalue);
            sql.append("','");
            sql.append(((EntriesListModel.Entry) value).getKey());
            sql.append("')");
            first_entry = false;
        }
        
        return sql.toString();
    }

    /**
     * Returns the value of the variable.
     **
     * @return the current value of the variable class.
     */
    public WidgetInstance getInstance () {
        /*if (textfield != null) {
            value = CommandThread.quote(textfield.getText());
            textfield = null;
        }
        return new WidgetInstance(value);*/
        throw new UnsupportedOperationException("Not yet implemented");
    }

    /**
     * Populates a container with the component objects
     * necessary for editing the current widget
     **
     * @param dest the destination to display the widget-editing components
     * @return the action listener associated with updating the current widget
     */
    public ActionListener editWindow(Container dest) {
        final JLabel lblLbl = new JLabel("Label/name: ");
        final JTextField lblTxt = new JTextField(label, 20);
        final Box lblPnl = new Box(BoxLayout.LINE_AXIS);

        lblPnl.add(lblLbl);
        lblPnl.add(lblTxt);

        dest.add(lblPnl);

        return new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                label = lblTxt.getText();
            }
        };
    }

    /**
     * Changes the current default value for the widget.
     * Reference fields cannot be currently set using 'setValue' - therefore
     * calling this method will produce an UnsupportedOperationException.
     **
     * @param newValue the new default value for the widget.
     */
    public void setValue(String newValue) {
        /*if (newValue != null && Pattern.matches("^-?\\d+$", newValue)) {
            try {
                index = Integer.parseInt(newValue);
            } catch (Throwable th) {
            }
        } else {
            for (int count = 0; count < names.length; count++) {
                if (names[count].equals(newValue)) {
                    index = count;
                    break;
                }
            }
        }*/
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
