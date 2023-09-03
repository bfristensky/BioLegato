/*
 * Main.java
 *
 * Created on November 7, 2007, 1:04 PM
 *
 * This is the file which contains all of the main classes, and functions for
 * running BioLegato.
 *
 * Current serializable number:
 * private static final long serialVersionUID = 7526472295622777040L;
 *
 * Released numbers:
 */
package org.biolegato.main;

import java.awt.BorderLayout;
import java.awt.Component;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.regex.Pattern;
import javax.swing.AbstractAction;
import javax.swing.JApplet;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import org.biopcd.parser.PCDIO;

/**
 * The main program and generic function class.
 * <p>
 *  This class is used to generate the main window, do all startup
 *  processing, and run the program.  This class also contains most
 *  of the utility functions.
 * </p>
 **
 * @author Graham Alvare
 * @author Brian Fristensky
 * @version 1.1.1 25-Mar-2011
 */
public abstract class DataCanvas extends JApplet implements PCDIO {


/////////////////////////
//*********************//
//* PROGRAM CONSTANTS *//
//*********************//
/////////////////////////
    /**
     * This constant stores an empty string (to avoid recreation)
     */
    public static final String EMPTY_STRING = "";
    /**
     * This constant is used for Serialization
     */
    public static final long serialVersionUID = 7526472295622776147L;
    /**
     * The menu item for BioLegato's "About..."
     */
    public final JMenuItem ABOUT_MENUITEM = new JMenuItem(
            new AbstractAction("About...") {

        /* Serialization number - required for no warnings*/
        private static final long serialVersionUID = 7526472295622776157L;

        {
            putValue(MNEMONIC_KEY, new Integer(java.awt.event.KeyEvent.VK_A));
        }  /* Sets the mnemonic for the event */


        public void actionPerformed(java.awt.event.ActionEvent evt) {
            // NOTE: 1.1.1 is replaced with the actual version number
            // by BioLegato's Apache Ant build script.
            JOptionPane.showMessageDialog(window,
                    "BioLegato version 1.1.1\n"
                        + "\n"
                        + "Please cite:\n"
                        + "Alvare G, Roche-Lima A, Fristensky B\n"
                        + "BioLegato: a programmable, object-oriented graphic user interface.\n"
                        + "BMC Bioinformatics 24, 316 (2023)\n"
                        + "https://doi.org/10.1186/s12859-023-05436-4\n",
                    "About BioLegato",
                    JOptionPane.QUESTION_MESSAGE);
        }
    });
    /**
     * The menu item for BioLegato's "Exit..."
     */
    public final JMenuItem EXIT_MENUITEM = new JMenuItem(
            new AbstractAction("Exit") {

        /* Serialization number - required for no warnings*/
        private static final long serialVersionUID = 7526472295622776157L;


        {
            putValue(MNEMONIC_KEY, new Integer(java.awt.event.KeyEvent.VK_X));
        }  /* Sets the mnemonic for the event */


        public void actionPerformed(java.awt.event.ActionEvent evt) {
            window.dispose();
        }
    });

/////////////////
//*************//
//* VARIABLES *//
//*************//
/////////////////
    /**
     * Reference to the main canvas's window
     * (intended to be used for creating modal child dialogue boxes)
     */
    private JFrame window = null;
    /**
     * Reference to the main canvas's menu
     */
    private JMenuBar menu;
    /**
     * <p>Stores the properties for BioLegato.</p>
     *
     * <p>Please see the manpage (within the jar, manpage.txt) for a list of
     * available properties.</p>
     *
     * <p>This properties class will read properties files from the directory
     * containing BioLegato, the user directory and finally the directory
     * BioLegato was launched from.  Please note that this behaviour can
     * be changed using the BL_PROPERTIES environment variable.</p>
     *
     * <p><i>NOTE: for path properties BioLegato will replace all $'s with the
     *       appropriate environment variables if set.</i></p>
     */
    protected Properties properties = new Properties() {
        {
            // load the default properties JAR file.
            // ensure this is done by the catch statement, otherwise exit.
            try {
                load(DataCanvas.class.getResourceAsStream(
                        "/default.properties"));
            } catch (IOException ioe) {
                System.err.println("FATAL ERROR - CORRUPT JAR FILE");
                ioe.printStackTrace(System.err);
                System.exit(1);
            }
        }

        /**
         * Changes a property within the BLProperties object
         **
         * @param   key the key of the property to alter.
         * @param   value the new value of the property.
         * @return  the old value of the property changed
         *          (null if no previous value).
         */
        @Override
        public Object setProperty(String key, String value) {
            Object result = null;

            // ensure that the key and value parameters are not null
            if (key != null && value != null) {
                // make sure the key is always lower case
                key = key.toLowerCase();

                // the old value of the property changed
                result = super.setProperty(key, value);
            }

            // return the function
            return result;
        }

        /**
         * Obtains a property from the BLProperties object.
         **
         * @param key the key of the property.
         * @return the current value of the property (returns "" if not set).
         */
        @Override
        public String getProperty(String key) {
            // make sure the key is always lower case
            key = key.toLowerCase();

            String result = super.getProperty(key);
            if (result == null) {
                result = EMPTY_STRING;
            } else {
                result = BLMain.envreplace(result);
            }
            return result;
        }
    };
    /**
     * Stores all of the menu headings in BioLegato's main window.
     * This hashtable is used to add menu items to BioLegato's menu headings.
     * The key of the hashtable corresponds to the label of the menu heading.
     * The value of the hashtable is the menu heading's object.
     */
    private Map<String, JMenu> menuHeadings =
            new HashMap<String, JMenu>();
    /**
     * This constant stores the program's name
     */
    public String NAME = getProperty("title");

/////////////////////////
//*********************//
//* STARTUP FUNCTIONS *//
//*********************//
/////////////////////////

    /**
     * Constructs a default instance of a DataCanvas
     */
    public DataCanvas() {
        this((Map) null);
    }

    /**
     * Constructs a new instance of a DataCanvas, importing a properties
     * map to overwrite default program properties.
     **
     * @param importProperties a map containing the properties to overwrite
     */
    public DataCanvas(Map<? extends Object, ? extends Object>
            importProperties) {
        // Import the properties specified by the importProperties map.
        if (importProperties != null) {
            this.properties.putAll(importProperties);
        }

        // Create the menu bar.
        menu = new JMenuBar();
        setJMenuBar(menu);

        // Add File and Edit to the menu headings -- to ensure that they
        // are the 1st and 2nd menu headings (from the left) on the
        // program's menu bar.
        addMenuHeading("File");
        addMenuHeading("Edit");
    }
    
    /**
     * Starts BioLegato Applet version.
     */
    @Override
    public void init() {
        javax.swing.JRootPane grootPane = this.getRootPane();
        grootPane.putClientProperty("defeatSystemEventQueueCheck",
                Boolean.TRUE);
        //Execute a job on the event-dispatching thread;
        // creating this applet's GUI.
        try {
////////////
//********//
//* MENU *//
//********//
////////////

            javax.swing.SwingUtilities.invokeAndWait(new Runnable() {
                public void run() {
                    // Ensure that the File and Edit headings come first.
                    addMenuHeading("File");
                    addMenuHeading("Edit");

                    // Event handlers - add the Exit and About menu items.
                    addMenuHeading("File").add(EXIT_MENUITEM);
                    addMenuHeading("Help").add(ABOUT_MENUITEM);

                    JPanel panel = new JPanel();
                    panel.setLayout(new BorderLayout());
                    panel.add(BorderLayout.SOUTH, new JLabel("Count: "
                            + menu.getMenuCount()));
                    panel.add(BorderLayout.NORTH, menu);
                    panel.add(BorderLayout.CENTER, display());
                    add(panel);
                }
            });
        } catch (Exception e) {
            System.err.println("createGUI didn't complete successfully");
        }
        //main(new String[0]);
    }


////////////////////////////
//************************//
//* PROPERTIES FUNCTIONS *//
//************************//
////////////////////////////
    /**
     * Retrieves individual settings for BioLegato.
     * This is used to obtain values of properties in BioLeagato
     **
     * @param property the property key to retrieve the value for
     * @return the property value corresponding to the key parameter
     */
    public String getProperty(String property) {
        return properties.getProperty(property);
    }

////////////////////////
//********************//
//* WINDOW FUNCTIONS *//
//********************//
////////////////////////
    /**
     * Provides access for other classes to the main program window JFrame
     * object.
     **
     * @return the JFrame object corresponding to the main program window.
     */
    public JFrame getJFrame() {
        return window;
    }

    /**
     * Adds a menu heading (JMenu) to our menu (BLMenu).
     **
     * @param  name  the name of the menu heading
     * @return       either the JMenu which was added or the JMenu that
     *               corresponds to the existing menu heading.
     */
    public final JMenu addMenuHeading(String name) {
        JMenu heading = menuHeadings.get(name);	// the JMenu object to add

        // check if the heading already exists in the menu.
        // (null indicates that the menu heading is new)
        if (heading == null) {
            // create a new menu heading object
            heading = new JMenu(name);

            // set the mnemonic
            if (name != null && name.length() >= 1
                    && ((name.charAt(0) >= 'a' && name.charAt(0) <= 'z')
                    || (name.charAt(0) >= 'A' && name.charAt(0) <= 'Z'))) {
                heading.setMnemonic(name.charAt(0));
            }

            // add the heading
            menuHeadings.put(name, heading);
            menu.add(heading);
        }

        // return the menu heading object
        return heading;
    }

    /**
     * Adds a menu heading (JMenu) to our menu (BLMenu).
     **
     * @param   order  the position to place the menu tag
     * @param   name   the name of the menu heading
     * @return         either the JMenu which was added or the JMenu
     *                 that corresponds to the existing menu heading.
     */
    public JMenu addMenuHeading(int order, String name) {
        // the hading to add the item to.
        JMenu heading = addMenuHeading(name);
        
        // ensure that the menu heading is in the correct order
        if (menu.getComponentIndex(heading) != order) {
            menu.remove(heading);
            menu.add(heading, order);
        }
        return heading;
    }

/////////////////////////
//*********************//
//* GENERAL FUNCTIONS *//
//*********************//
/////////////////////////
    /**
     * Sends an error message to BioLegato's standard err.
     **
     * @param message the error message to send.
     * @param location the location the error occurred.
     */
    public void error(String message, String location) {
        // print the error to the error stream
        System.err.println(NAME + ((location != null
                && !"".equals(location.trim()))
                ? " (" + location + ")" : "") + ": ERROR --- " + message);
    }

    /**
     * Sends an warning message to BioLegato's standard err.
     **
     * @param message the warning message to send.
     * @param location the location the error occurred.
     */
    public void warning(String message, String location) {
        // prints the warning to BioLegato's error stream
        System.err.println(NAME + ((location != null
                && !"".equals(location.trim()))
                ? " (" + location + ")" : "") + ": WARNING --- " + message);
    }

    /**
     * Sends a message to BioLegato's standard out.
     **
     * @param message the message to send.
     * @param location the location the message was sent from.
     */
    public void message(String message, String location) {
        // prints the warning to BioLegato's error stream
        System.out.println(NAME + ((location != null
                && !"".equals(location.trim()))
                ? " (" + location + ")" : "") + ": " + message);
    }

    /**
     * Checks if a character array is all digits.
     **
     * @param test the character array to test.
     * @return true if the array only contains digits.
     */
    public static boolean testNumber(String test) {
        return Pattern.matches("^-?\\d+$", test);
    }

    /**
     * Used to create a JFrame containing the current canvas
     */
    public final void createJFrame() {
        window = new JFrame(getProperty("title"));
        window.setJMenuBar(menu);
        window.add(display());

        // center and draw the frame on the screen
        window.pack();
        window.setVisible(true);
        window.setLocationRelativeTo(null);
        window.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
    }

    /**
     * Displays the main pane of the data canvas
     **
     * @return the swing or awt component to display as the canvas.
     */
    public abstract Component display();

    /**
     * Possible vestigial code!
     * This code is used to provide a plugin name for the canvas.  The purpose
     * of the plugin name system was to allow multiple canvases to be loaded
     * (via. BioLegato's plugin system), and then have the properties select
     * which canvas to use based on the plugin names.  This architecture has
     * since been replaced, and so this code may no longer be used.
     **
     * @return     the name to display for the canvas in
     *             all program text referring to it.
     * @deprecated vestigial code from a previous BioLegato framework structure.
     */
    public abstract String getPluginName ();
}
