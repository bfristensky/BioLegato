/*
 * RunWindow.java
 *
 * Created on January 5, 2010, 2:06 PM
 *
 * To change this template, choose Tools | Template Manager
 * and display the template in the editor.
 */
package org.biolegato.database;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.io.IOException;
import java.io.Reader;
import java.awt.Component;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collections;
import java.util.Map;
import javax.swing.AbstractAction;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTextField;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import org.biolegato.database.schemaeditor.SchemaFieldsPanel;
import org.biopcd.widgets.Widget;
import org.biopcd.widgets.CloseableWindow;
import org.biolegato.main.BLMain;
import org.biolegato.main.DataCanvas;
import org.biolegato.database.fields.DBTextField;

/**
 * The run window is used within the menu system to display options for running programs
 **
 * @author Graham Alvare
 * @author Brian Fristensky
 */
public class DBCanvas extends DataCanvas implements CloseableWindow {

    /**
     * The database connection for the DB canvas
     */
    private Connection conn;
    /**
     * The current database schema selected
     */
    private DBSchema schema;
    /**
     * The name of the window
     */
    private String name = null;
    /**
     * List of widgets for the window.
     */
    protected Map<String, Widget> widgets = Collections.EMPTY_MAP;
    /**
     * The main panel object for the canvas (this is used for refreshing
     * while in applet mode, so everything can be displayed within one window.
     */
    protected JPanel mainPanel = new JPanel();
    /**
     * A self reference to the current object (this is used for customized
     * subclass code defined within the class's methods).
     */
    final public DBCanvas dbcanvasSelf = this;
    /**
     * A button object used to return to the database home page/screen
     */
    final JButton dbBack = new JButton(new AbstractAction("Go Back") {
        public void actionPerformed(ActionEvent e) {
            dbBack();
        }
    });

    /**
     * Creates a new instance of BioLegato's database canvas
     */
    public DBCanvas() {
        this(null);
    }

    /**
     * Creates a new instance of BioLegato's database canvas
     **
     * @param importProperties a list of properties attributes to import into the canvas
     */
    public DBCanvas(Map<? extends Object, ? extends Object> importProperties) {
        super(importProperties);

        String driver  = getProperty("db.driver");
        String url     = "jdbc:" + getProperty("db.url");
        String user    = getProperty("db.user");
        String passwd  = getProperty("db.password");
        boolean prompt = "true".equalsIgnoreCase(getProperty("db.prompt"));

        if ("".equals(getProperty("db.driver"))) {
            if (url.toLowerCase().startsWith("jdbc:hsqldb")) {
                driver = "org.hsqldb.jdbcDriver";
            } else if (url.toLowerCase().startsWith("jdbc:mysql")) {
                driver = "com.mysql.jdbc.Driver";
            } else {
                System.err.println("INVALID DRIVER!");
            }
        } else if ("hsqldb".equalsIgnoreCase(getProperty("db.driver"))
                || "hsql".equalsIgnoreCase(getProperty("db.driver"))) {
            driver = "org.hsqldb.jdbcDriver";
            url += ";hsqldb.write_delay=false";
        } else if ("mysql".equalsIgnoreCase(getProperty("db.driver"))) {
            driver = "com.mysql.jdbc.Driver";
        }

        try {
            Class.forName(driver);
            if (!"user".equals("") || prompt) {
                if ("user".equals("")) {
                    final JDialog promptDlg = new JDialog(getJFrame());
                    JTextField promptUser   = new JTextField();
                    JPasswordField promptPW = new JPasswordField();
                    JPanel promptPanel      = new JPanel();
                    JPanel promptUPanel     = new JPanel();
                    JPanel promptPWPanel    = new JPanel();

                    promptPanel.setLayout(new BoxLayout(promptPanel, BoxLayout.PAGE_AXIS));
                    promptUPanel.setLayout(new BoxLayout(promptUPanel, BoxLayout.LINE_AXIS));
                    promptPWPanel.setLayout(new BoxLayout(promptPWPanel, BoxLayout.LINE_AXIS));

                    promptUPanel.add(new JLabel("Username: "));
                    promptUPanel.add(promptUser);
                    promptPWPanel.add(new JLabel("Password: "));
                    promptPWPanel.add(promptPW);

                    promptPanel.add(new JLabel("Connect to: " + url));
                    promptPanel.add(promptUPanel);
                    promptPanel.add(promptPWPanel);
                    promptPanel.add(new JButton(new AbstractAction("Connect") {
                        public void actionPerformed(ActionEvent e) {
                            promptDlg.setVisible(false);
                            promptDlg.dispose();
                        }
                    }));
                    promptDlg.add(promptPanel);
                    promptDlg.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
                    promptDlg.pack();
                    promptDlg.setModal(true);
                    promptDlg.setVisible(true);

                    user = promptUser.getText();
                    passwd = String.valueOf(promptPW.getPassword());
                }
                conn = DriverManager.getConnection(url, user, passwd);
            } else {
                conn = DriverManager.getConnection(url);
            }
            if (driver.equalsIgnoreCase("org.hsqldb.jdbcDriver")) {
                Statement stmnt = null;

                try {
                    stmnt = conn.createStatement();
                    stmnt.executeUpdate("SET WRITE_DELAY FALSE;");
                } catch (SQLException ex) {
                    ex.printStackTrace(System.err);
                } finally {
                    if (stmnt != null) {
                        try {
                            stmnt.close();
                        } catch (SQLException ex) {
                            ex.printStackTrace(System.err);
                        }
                    }
                }
            }
        } catch (ClassNotFoundException ex) {
            ex.printStackTrace(System.err);
        } catch (SQLException ex) {
            if (conn != null) {
                close();
            }
            ex.printStackTrace(System.err);
        }
    }

    /**
     * Displays the command parameter window.
     */
    @Override
    public Component display() {
        JFrame frame = getJFrame();

        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.PAGE_AXIS));

        dbBack();

        if (frame != null) {
            frame.setSize(400, 400);
            frame.setMinimumSize(mainPanel.getSize());
        }

        return mainPanel;
    }

    /**
     * The code for displaying the database home screen/page
     */
    public void dbBack() {
        final JPanel dbButtonPanel = new JPanel();
        final SchemaListModel listModel = new SchemaListModel(getProperty("db.schemas"));
        final JList  dbList = new JList(listModel);
        final JScrollPane dbListPane = new JScrollPane(dbList);
        final JPanel dbMain = new JPanel();
        
        /////////
        // CREATE TABLE
        /////////
        // used to create and modify schemas
        final JButton dbCreate = new JButton(new AbstractAction("Create table") {
            public void actionPerformed(ActionEvent e) {
                tableEditor();
            }
        });

        /////////
        // BROWSE TABLE
        /////////
        // all of the database's entry functionality
        final JButton dbBrowse = new JButton(new AbstractAction("Browse table") {
            public void actionPerformed(ActionEvent e) {
                browseTable(dbList.getSelectedValue().toString());
            }
        });

        dbButtonPanel.setLayout(new BoxLayout(dbButtonPanel, BoxLayout.LINE_AXIS));
        dbButtonPanel.add(dbCreate);
        dbButtonPanel.add(dbBrowse);
        
        dbMain.setLayout(new BoxLayout(dbMain, BoxLayout.PAGE_AXIS));
        dbMain.add(dbListPane);
        dbMain.add(dbButtonPanel);

        listModel.checkupdate();
        
        refreshMainPanel(dbMain);
    }

    public void refreshMainPanel(Component add) {
        // TODO: add this functionality to all of the Database Canvas buttons!
        JFrame frame = getJFrame();
        mainPanel.removeAll();
        mainPanel.add(add);

        if (frame != null) {
            frame.doLayout();
            frame.validate();
            frame.repaint(50L);
        } else {
            mainPanel.doLayout();
            mainPanel.validate();
            mainPanel.repaint(50L);
        }
    }

    public void loadSchema(String schemaname) {
        schema = readSchema(schemaname);
        System.out.println("loaded schema: " + schema);
    }

    public DBSchema readSchema(String schemaname) {
        DBSchema result = null;
        try {
            DBPCD parser = new DBPCD(new FileReader(new File(getProperty("db.schemas") + File.separator + schemaname + ".schema")));
            result = parser.parseSchema(new File(getProperty("db.schemas")), false);
        } catch (Exception ex) {
            ex.printStackTrace(System.err);
        }
        return result;
    }

    public String[] getSchemas() {
        return SchemaListModel.getSchemas(getProperty("db.schemas")).toArray(new String[0]);
    }

    /**
     * Get all of the entries for the current database schema
     **
     * @return a string array containing all of the entries for the schema
     */
    public String[] getEntries() {
        return EntriesListModel.getEntries(conn, schema).toArray(new String[0]);
    }

    /**
     * Action for command buttons to close the window.
     * This is used to close parameter windows when a command is being executed.
     */
    public void close() {
        JFrame runWindow = this.getJFrame();
        
        if (runWindow != null) {
            runWindow.setVisible(false);
            runWindow.dispose();
        }
    }

    /**
     * Returns the name of this canvas (i.e. database canvas)
     **
     * @return the string "BLDB canvas"
     */
    @Override
    public String getPluginName() {
        return "BLDB canvas";
    }

    /**
     * Reads data into the canvas
     **
     * @param format the file format to use for parsing the file.
     * @param in the "stream" to read in from.
     * @param overwrite whether to overwrite the currently selected sequences with the data imported.
     * @throws IOException if an error occurs while reading
     */
    public void readFile(String format, Reader in, boolean overwrite) throws IOException {
    }

    /**
     * Writes data out from the canvas
     **
     * @param format the file format to use for writing the file.
     * @param out the "stream" to write out to.
     * @param forceall use the entire canvas instead of just its selected sequences.
     * @throws IOException if an error occurs while writing
     */
    public void writeFile(String format, Appendable out, boolean forceall) throws IOException {
    }

    /**
     * Starts BioLegato PCD canvas
     **
     * @param args the command line arguments for BioLegato
     */
    public static void main (String[] args) {
        BLMain.main(DBCanvas.class, args);
    }

    /**
     * Returns the current schema
     **
     * @return the schema currently selected in the canvas
     */
    public DBSchema getCurrentSchema() {
        return schema;
    }

    /**
     * Displays the brows table canvas for the BLDB
     **
     * @param value - the schema to view/browse
     */
    public void browseTable(String value) {
        loadSchema(value);

        final EntriesListModel entriesModel = new EntriesListModel(conn, schema);
        final JList entryList = new JList(entriesModel);
        final JPanel entryButtonPanel = new JPanel();
        final JPanel entriesPanel  = new JPanel();

        entriesPanel.setLayout(new BoxLayout(entriesPanel, BoxLayout.PAGE_AXIS));
        entriesPanel.add(new JScrollPane(entryList));
        entriesPanel.add(entryButtonPanel);
        refreshMainPanel(entriesPanel);
    }

    /**
     * Displays the schema/table editor for the BLDB
     */
    private void tableEditor() {
        JPanel topPanel = new JPanel();
        JPanel dbCreateMainPanel = new JPanel();
        JPanel dbSchemaButtons   = new JPanel();
        JPanel dbSchemaButtonGrp = new JPanel();
        SchemaFieldsPanel fieldsPanel  = new SchemaFieldsPanel(dbcanvasSelf);

        final JButton schemaSave = new JButton(new AbstractAction("Save") {
            public void actionPerformed(ActionEvent e) {
                Statement stmnt = null;

                // TODO: save the DBPCD schema file, and create the actual table
                try {
                    stmnt = conn.createStatement();
                    System.out.println("CREATE TABLE PCD");
                    System.out.println("----------------");
                    schema.pcdOut(0, System.out);
                    System.out.println();
                    System.out.println("writing PCD to: " + getProperty("db.schemas"));
                    FileWriter writer = new FileWriter(new File(getProperty("db.schemas"), schema.getTable() + ".schema"));
                    schema.pcdOut(0, writer);
                    writer.close();
                    System.out.println("writing DB to: " + getProperty("db.url"));
                    System.out.println();
                    schema.createTable(stmnt);
                } catch (IOException ioe) {
                    ioe.printStackTrace(System.err);
                } catch (SQLException sqle) {
                    sqle.printStackTrace(System.err);
                } finally {
                    if (stmnt != null) {
                        try {
                            stmnt.close();
                        } catch (SQLException sqle) {
                            sqle.printStackTrace(System.err);
                        }
                    }
                }
                //throw new UnsupportedOperationException("Not supported yet.");
                dbBack();
            }
        });

        final JPanel tableProperties = new JPanel();
        final JPanel tableNamePanel  = new JPanel();
        final JPanel tableKeyPanel   = new JPanel();
        final JTextField tableNameTB = new JTextField();
        final JTextField keyNameTB   = new JTextField();
        final JButton dbBack = new JButton(new AbstractAction("Go Back") {
            public void actionPerformed(ActionEvent e) {
                dbBack();
            }
        });
        
        tableNamePanel.setLayout(new BoxLayout(tableNamePanel, BoxLayout.LINE_AXIS));
        tableNamePanel.add(new JLabel("Table Name:"));
        tableNamePanel.add(tableNameTB);

        tableKeyPanel.setLayout(new BoxLayout(tableKeyPanel, BoxLayout.LINE_AXIS));
        tableKeyPanel.add(new JLabel("Table Key:"));
        tableKeyPanel.add(keyNameTB);

        tableProperties.setLayout(new BoxLayout(tableProperties, BoxLayout.PAGE_AXIS));
        tableProperties.add(tableNamePanel);
        tableProperties.add(tableKeyPanel);

        int create_ok = 0;

        do {
            create_ok = JOptionPane.showConfirmDialog(getJFrame(), tableProperties, "Create New Table", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        } while (create_ok == 0 && (!tableNameTB.getText().matches("^[A-Za-z][A-Za-z0-9_]*$") || !keyNameTB.getText().matches("^[A-Za-z][A-Za-z0-9_]*$")));

        if (create_ok == 0) {
            schema = new DBSchema(tableNameTB.getText(), new DBTextField(keyNameTB.getText()));

            topPanel.setLayout(new BoxLayout(topPanel, BoxLayout.PAGE_AXIS));
            topPanel.add(fieldsPanel);
            topPanel.add(new JSeparator());

            dbSchemaButtonGrp.setLayout(new BoxLayout(dbSchemaButtonGrp, BoxLayout.LINE_AXIS));
            dbSchemaButtonGrp.add(schemaSave, BorderLayout.EAST);
            dbSchemaButtonGrp.add(dbBack,     BorderLayout.EAST);

            dbSchemaButtons.setLayout(new BorderLayout());
            dbSchemaButtons.add(dbSchemaButtonGrp, BorderLayout.EAST);

            dbCreateMainPanel.setLayout(new BorderLayout());
            dbCreateMainPanel.add(topPanel, BorderLayout.PAGE_START);
            dbCreateMainPanel.add(schema.schemaEditor(getJFrame(), conn, name));
            dbCreateMainPanel.add(dbSchemaButtons, BorderLayout.PAGE_END);

            // fix the main panel
            refreshMainPanel(dbCreateMainPanel);
        }
    }
}
