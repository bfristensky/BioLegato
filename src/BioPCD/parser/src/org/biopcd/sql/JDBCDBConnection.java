/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.biopcd.sql;

import java.awt.event.ActionEvent;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import javax.swing.AbstractAction;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;

/**
 * (EXPERIMENTAL!)
 * Stores PCD connection data.
 **
 * @author Graham Alvare
 * @author Brian Fristensky
 */
public class JDBCDBConnection {
    /**
     * The driver to use for accessing the database.
     */
    private String     driver   = null;
    /**
     * The URL for the database.
     */
    private String     url      = null;
    /**
     * The username to access the database with.
     */
    private String     user     = null;
    /**
     * The password to access the database with.
     */
    private String     password = null;
    /**
     * Whether or not to prompt the user for a username and password for
     * accessing the database.
     */
    private boolean    prompt   = false;
    /**
     * The connection object to use to communicate with the database.
     */
    private Connection conn     = null;

    /**
     * Creates a new PCD SQL connection object.
     **
     * @param driver the class driver for the database
     * @param url the database URL to connect to
     * @param prompt whether to prompt for the username and password, if null
     */
    public JDBCDBConnection (String driver, String url, boolean prompt) {
        this(driver, url, null, null);

        this.prompt = prompt;
    }
    /**
     * Creates a new PCD SQL connection object.
     **
     * @param driver the class driver for the database
     * @param url the database URL to connect to
     * @param user the user to log into the database server with
     * @param password the password to log into the database server with
     */
    public JDBCDBConnection (String driver, String url,
                             String user, String password) {
        this.driver   = driver;
        this.url      = url;
        this.user     = user;
        this.password = password;
    }

    /**
     * Connect to the database. Creates a new JDBC Connection object based on
     * the driver, URL, username, and password specified by this object.
     **
     * @return the JDBC Connection object.
     */
    public Connection connect() {
        // Ensure that the database is not connected, otherwise this method
        // should reuse the existing database.
        if (conn == null) {
            try {
                Class.forName(driver);
                if (user != null || prompt) {
                    // Prompt the user for a username and password to access
                    // the database.
                    if (user == null) {
                        final JDialog promptDlg = new JDialog();
                        JTextField promptUser   = new JTextField();
                        JPasswordField promptPW = new JPasswordField();
                        JPanel promptPanel      = new JPanel();
                        JPanel promptUPanel     = new JPanel();
                        JPanel promptPWPanel    = new JPanel();

                        promptPanel.setLayout(new BoxLayout(promptPanel,
                                BoxLayout.PAGE_AXIS));
                        promptUPanel.setLayout(new BoxLayout(promptUPanel,
                                BoxLayout.LINE_AXIS));
                        promptPWPanel.setLayout(new BoxLayout(promptPWPanel,
                                BoxLayout.LINE_AXIS));

                        promptUPanel.add(new JLabel("Username: "));
                        promptUPanel.add(promptUser);
                        promptPWPanel.add(new JLabel("Password: "));
                        promptPWPanel.add(promptPW);

                        promptPanel.add(new JLabel("Connect to: " + url));
                        promptPanel.add(promptUPanel);
                        promptPanel.add(promptPWPanel);
                        promptPanel.add(new JButton(
                                new AbstractAction("Connect") {
                            public void actionPerformed(ActionEvent e) {
                                promptDlg.setVisible(false);
                                promptDlg.dispose();
                            }
                        }));
                        promptDlg.add(promptPanel);
                        promptDlg.setDefaultCloseOperation(
                                JDialog.DO_NOTHING_ON_CLOSE);
                        promptDlg.pack();
                        promptDlg.setVisible(true);

                        user = promptUser.getText();
                        password = String.valueOf(promptPW.getPassword());
                    }
                    conn = DriverManager.getConnection(url, user, password);
                } else {
                    conn = DriverManager.getConnection(url);
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

        return conn;
    }

    /**
     * Close the database connection represented by this object.
     */
    public void close() {
        // Ensure that there is a Connection object associated with this object.
        if (conn != null) {
            try {
                conn.close();
            } catch (SQLException ex1) {
                ex1.printStackTrace(System.err);
            }
            conn = null;
        }
    }

    /**
     * Writes the BioPCD representation of the PCD SQL command
     **
     * @param scope the level of scope to write the menu widget.
     * @param out the writer object to output the BioPCD code.
     */
    public void pcdOut(int scope, Appendable out) throws IOException {
        // Generate the proper scope indentation.
        for (int count = 0; count < scope + 1; count++) {
            out.append("    ");
        }
        // FIX - MAKE THIS THE PROPER FORMAT!
        out.append("database \"");
        out.append(driver.replaceAll("\"", "\"\""));
        out.append("\" \"");
        out.append(url.replaceAll("\"", "\"\""));
        out.append("\" \"");
        out.append(user.replaceAll("\"", "\"\""));
        out.append("\" \"");
        out.append(password.replaceAll("\"", "\"\""));
        out.append("\"");
    }
}
