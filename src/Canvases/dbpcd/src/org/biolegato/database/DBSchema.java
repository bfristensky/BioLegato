package org.biolegato.database;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.TextField;
import java.awt.datatransfer.Transferable;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.dnd.DropTargetEvent;
import java.awt.dnd.DropTargetListener;
import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import javax.swing.BoxLayout;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import org.biolegato.database.fields.DBCommand;
import org.biolegato.database.fields.DBField;
import org.biolegato.database.fields.DBTextField;
import org.biolegato.database.fields.KeyableField;
import org.biolegato.database.fields.ReferenceField;
import org.biolegato.database.schemaeditor.FieldGenerator;
import org.biolegato.database.schemaeditor.FieldTransferHandler;

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author alvare
 */
public class DBSchema {
    private String table;
    private DBField namecol;
    //private Map<String, DBField> fields = new LinkedHashMap<String,DBField>();
    private KeyableField key;
    private Collection<DBField> fields;

    public DBSchema (String table) {
        this(table, new DBTextField(table + "_key"));
    }

    public DBSchema (String table, KeyableField key) {
        this(table, key, key);
    }

    public DBSchema (String table, KeyableField key, DBField namecol) {
        this(table, key, namecol, new ArrayList<DBField>());
    }

    public DBSchema (String table, KeyableField key, DBField namecol, Collection<DBField> fields) {
        this.table = table;
        this.key = key;
        this.namecol = namecol;
        this.fields = fields;

        if (namecol == null) {
            this.namecol = this.key;
        }
    }

    public String getTable() {
        return table;
    }

    public KeyableField getKey() {
        return key;
    }

    public DBField getNameCol() {
        return namecol;
    }

    public void view (Connection conn, Container mainPanel, String value) {
        JPanel    panel   = new JPanel();
        Statement stmnt   = null;
        ResultSet results = null;
        ResultSetMetaData  metadata  = null;
        Map<String,String> resultMap = new LinkedHashMap<String,String>();

        try {
            panel.setLayout(new BoxLayout(panel, BoxLayout.PAGE_AXIS));
            stmnt    = conn.createStatement();
            results  = stmnt.executeQuery("SELECT * FROM " + table + " WHERE " + key.getName() + " = '" + value.replaceAll("'", "\\\'") + "\'");
            metadata = results.getMetaData();

            if (results.next()) {
                final int cols = metadata.getColumnCount();
                System.out.println("cols: " + cols);
                for (int count = 1; count <= cols; count++) {
                    System.out.println("   adding: " + metadata.getColumnName(count) + " -- " + results.getString(count));
                    resultMap.put(metadata.getColumnName(count).toUpperCase(), results.getString(count));
                }
            }

            key.view(conn, panel, resultMap);

            if (!fields.contains(namecol) && namecol != key) {
                namecol.view(conn, panel, resultMap);
            }
            
            for (DBField f : fields) {
                f.view(conn, panel, resultMap);
            }

            mainPanel.add(panel);
        } catch (SQLException ex) {
            ex.printStackTrace(System.err);
            mainPanel.add(new JLabel(" --- DB ERROR! Could not read: " + table + " --- "));
        } finally {
            if (results != null) {
                try {
                    results.close();
                } catch (SQLException ex) {
                    ex.printStackTrace(System.err);
                }
            }
            if (stmnt != null) {
                try {
                    stmnt.close();
                } catch (SQLException ex) {
                    ex.printStackTrace(System.err);
                }
            }
        }
    }

    public void edit(Connection conn, Container parent, String value) {
        JPanel    panel   = new JPanel();
        Statement stmnt   = null;
        ResultSet results = null;
        ResultSetMetaData  metadata  = null;
        Map<String,String> resultMap = new LinkedHashMap<String,String>();

        try {
            if (value != null) {
                panel.setLayout(new BoxLayout(panel, BoxLayout.PAGE_AXIS));
                stmnt    = conn.createStatement();
                results  = stmnt.executeQuery("SELECT * FROM " + table + " WHERE " + key.getName() + " = '" + value.replaceAll("\"", "\\\"") + "'");
                metadata = results.getMetaData();

                if (results.next()) {
                    final int cols = metadata.getColumnCount();
                    for (int count = 1; count <= cols; count++) {
                        resultMap.put(metadata.getColumnName(count), results.getString(count));
                    }
                }
            }

            key.edit(conn, panel, resultMap);

            if (!fields.contains(namecol) && namecol != key) {
                System.out.println("key:     " + key);
                System.out.println("namecol: " + namecol);
                namecol.edit(conn, panel, resultMap);
            }
            
            for (DBField f : fields) {
                if (!(f instanceof DBCommand)) {
                    f.edit(conn, panel, resultMap);
                }
            }

            parent.add(panel);
        } catch (SQLException ex) {
            ex.printStackTrace(System.err);
            parent.add(new JLabel(" --- DB ERROR! Could not read: " + table + " --- "));
        } finally {
            if (results != null) {
                try {
                    results.close();
                } catch (SQLException ex) {
                    ex.printStackTrace(System.err);
                }
            }
            if (stmnt != null) {
                try {
                    stmnt.close();
                } catch (SQLException ex) {
                    ex.printStackTrace(System.err);
                }
            }
        }
    }

    public Component schemaEditor(final JFrame window, final Connection conn, final String value) {
        final JPanel mainPanel   = new JPanel();
        final JPanel submainPane = new JPanel();
        final JScrollPane scrollPane = new JScrollPane(submainPane, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);

        // Again, needs to negotiate with the draggable object
        mainPanel.setTransferHandler(new FieldTransferHandler());

        // Create the listener to do the work when dropping on this object!
        mainPanel.setDropTarget(new DropTarget(mainPanel, new DropTargetListener() {
            // Could easily find uses for these, like cursor changes, etc.
            public void dragEnter(DropTargetDragEvent dtde) {}
            public void dragOver(DropTargetDragEvent dtde) {
                scrollPane.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            }
            public void dropActionChanged(DropTargetDragEvent dtde) {}
            public void dragExit(DropTargetEvent dte) {
                scrollPane.setCursor(Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));
            }

            /**
             * <p>The user drops the item. Performs the drag and drop calculations and layout.</p>
             * @param dtde
             */
            public void drop(DropTargetDropEvent dtde) {

                // THIS IS WHERE THE ACTION HAPPENS - WHERE THE OBJECT DROP OCCURS
                System.out.println("Drop");

                // Done with cursors, dropping
                scrollPane.setCursor(Cursor.getDefaultCursor());

                Object transferableObj = null;
                Transferable transferable = null;

                try {
                    // Get the y offset from the top of the WorkFlowSheetPanel
                    // for the drop option (the cursor on the drop)
                    final int dropXLoc = dtde.getLocation().y;
                    final int dropYLoc = dtde.getLocation().x;

                    transferable = dtde.getTransferable();
                    //DropTargetContext context = dtde.getDropTargetContext();
                    DBField field = null;

                    // What does the Transferable support
                    if (transferable.isDataFlavorSupported(FieldGenerator.FLAVOUR)) {
                        transferableObj = dtde.getTransferable().getTransferData(FieldGenerator.FLAVOUR);

                        // Perform the drop action
                        field = ((FieldGenerator)transferableObj).drop();
                        System.out.println("FieldGenerator");
                        if (field != null) {
                            mainPanel.add(field.schemaEditor(conn, window));
                            fields.add(field);
                            mainPanel.doLayout();
                            mainPanel.setSize(mainPanel.getPreferredSize());
                            submainPane.setSize(mainPanel.getPreferredSize());
                            scrollPane.doLayout();
                            scrollPane.validate();
                            scrollPane.repaint(50L);
                        } else {
                            System.out.println("    Skipping null field!");
                        }
                    }

                    System.out.println("fieldListSize: " + fields.size());
                } catch (Exception ex) { /* nope, not the place */
                    ex.printStackTrace(System.err);
                }

                // If didn't find an item, bail
                if (transferableObj == null) {
                    return;
                }
            }
        }));

        submainPane.setLayout(new BorderLayout());
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.PAGE_AXIS));

        submainPane.add(mainPanel, BorderLayout.CENTER);
        submainPane.add(new JPanel(), BorderLayout.SOUTH);

        mainPanel.add(key.schemaEditor(conn, window));
        if (!fields.contains(namecol) && namecol != key) {
            mainPanel.add(namecol.schemaEditor(conn, window));
        }
        for (DBField f : fields) {
            mainPanel.add(f.schemaEditor(conn, window));
        }
        mainPanel.doLayout();
        mainPanel.setSize(mainPanel.getPreferredSize());
        submainPane.setSize(mainPanel.getPreferredSize());
        scrollPane.doLayout();
        scrollPane.validate();
        scrollPane.repaint(50L);

        return scrollPane;
    }


    /**
     * Writes the DBPCD representation of the database schema to a writer object
     **
     * @param scope the level of scope to write the database schema.
     * @param out the writer object to output the PCD code.
     */
    public void pcdOut (int scope, Appendable out) throws IOException {
        for (int count = 0; count < scope; count++) {
            out.append("    ");
        }
        out.append("schema \"");
        out.append(table.replaceAll("\"", "\"\""));
        out.append("\"\n");
        for (int count = 0; count < scope + 1; count++) {
            out.append("    ");
        }
        out.append("namecol \"");
        out.append(namecol.getName().replaceAll("\"", "\"\""));
        out.append("\"\n");
        
        key.dbpcdout(scope + 1, out, true);
        
        if (!fields.contains(namecol) && namecol != key) {
            namecol.dbpcdout(scope + 1, out, false);
        }
        for (DBField f : fields) {
            if (f != key) {
                f.dbpcdout(scope + 1, out, false);
            }
        }
    }

    /**
     * Writes the CREATE TABLE representation of the database schema to a writer object
     **
     * @param stmnt the Statement object to output the CREATE TABLE SQL-statement code.
     */
    public void createTable (Statement stmnt) throws SQLException, IOException {
        boolean first_field = true;
        StringBuilder referenceTables = null;
        StringBuilder createTableString = new StringBuilder();

        createTableString.append("CREATE TABLE ");
        createTableString.append(table.replaceAll("\"", "\"\""));
        createTableString.append(" (");

        key.createTable(createTableString);

        if (!fields.contains(namecol) && namecol != key) {
            namecol.createTable(createTableString);
        }
        
        for (DBField f : fields) {
            if (!(f instanceof DBCommand || f instanceof ReferenceField)) {
                createTableString.append(",");
                f.createTable(createTableString);
            } else if (f instanceof ReferenceField) {
                // save on memory and speed, since most tables will not use references
                if (referenceTables == null) {
                    referenceTables = new StringBuilder();
                }
                f.createTable(referenceTables);
            }
        }
        createTableString.append(");\n");

        // save on memory and speed, since most tables will not use references
        if (referenceTables != null) {
            createTableString.append(referenceTables);
        }

        stmnt.executeUpdate(createTableString.toString());
    }

    public void save(Connection conn, String keyvalue) {
        Statement stmnt = null;
        StringBuilder referenceSQL = null;
        StringBuilder sql = new StringBuilder();
        boolean first_field = true;

        if (keyvalue == null) {
            sql.append("INSERT INTO ");
            sql.append(table);
            sql.append(" (");
            sql.append(key.getName());
            if (!fields.contains(namecol) && namecol != key) {
                sql.append(",");
                sql.append(namecol.getName());
            }
            for (DBField f : fields) {
                if (!(f instanceof DBCommand || f instanceof ReferenceField)) {
                    sql.append(",");
                    sql.append(f.getName());
                }
            }
            sql.append(") VALUES (");
        } else {
            sql.append("UPDATE ");
            sql.append(table);
            sql.append(" SET ");
        }

        sql.append(key.save(keyvalue));
        if (!fields.contains(namecol) && namecol != key) {
            sql.append(",");
            sql.append(namecol.save(keyvalue));
        }
        for (DBField f : fields) {
            if (!(f instanceof DBCommand || f instanceof ReferenceField)) {
                sql.append(",");
                sql.append(f.save(keyvalue));
            } else if (f instanceof ReferenceField) {
                // save on memory and speed, since most tables will not use references
                if (referenceSQL == null) {
                    referenceSQL = new StringBuilder();
                }
                referenceSQL.append(f.save(key.getValue()));
            }
        }
        if (keyvalue == null) {
            sql.append(")");
        }
        sql.append(";");

        try {
            stmnt = conn.createStatement();
            System.out.println("SQL: " + sql.toString());
            stmnt.executeUpdate(sql.toString());
            if (referenceSQL != null) {
                System.out.println("Ref SQL: " + referenceSQL.toString());
                stmnt.executeUpdate(referenceSQL.toString());
            }
        } catch (SQLException sqlex) {
            sqlex.printStackTrace(System.err);
        } finally {
            if (stmnt != null) {
                try {
                    stmnt.close();
                } catch (SQLException sqlex) {
                    sqlex.printStackTrace(System.err);
                }
            }
        }

        //throw new UnsupportedOperationException("Not yet implemented");
    }
}
