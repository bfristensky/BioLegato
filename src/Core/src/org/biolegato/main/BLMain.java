/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.biolegato.main;

import java.awt.Desktop;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.regex.Pattern;
import javax.imageio.ImageIO;
import javax.swing.Action;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import org.biopcd.parser.CommandThread;
import org.biopcd.parser.PCD;
import org.biopcd.parser.PCDObject;
import org.biopcd.parser.RunWindow;

/**
 * The main BioLegato class.
 * This class contains all of the basic code to launch BioLegato.
 **
 * @author Graham Alvare
 * @author Brian Fristensky
 */
public class BLMain {
    /**
     * The width to display the icons in BioLegato menus
     */
    private static final int ICONW = 16;
    /**
     * The height to display the icons in BioLegato menus
     */
    private static final int ICONH = 16;
    /**
     * This constant is set to the path of where biolegato was run
     * The value of this constant determined at runtime.
     */
    public static final String CURRENT_DIR = (String)
            AccessController.doPrivileged(new PrivilegedAction() {
            public Object run() {
                try {
                    return System.getProperty("user.dir");
                } catch (Exception ex) {
                    ex.printStackTrace(System.err);
                    return "";
                }
            }
        }
    );
    /**
     * Stores all of the plugins loaded into BioLegato
     */
    private static Map<String, PluginWrapper> plugins
            = new HashMap<String, PluginWrapper>();
    /**
     * Stores information regarding the usage of the debug command
     * NOTE: This should always be accessed using BLMain.debug
     */
    public static boolean debug = false;

//---------------------------- BioLegato METHODS ----------------------------//

    /**
     * <p>Starts BioLegato from the command line.</p>
     *
     * <p>This method is split into the following sections:</p>
     * <ol>
     *   <li>Declare function variables.</li>
     *   <li>Load BioLegato's properties.</li>
     *   <li>Load plug-ins.</li>
     *   <li>Process command-line arguments.</li>
     *   <li>Correct DEBUG MODE status</li>
     *   <li>Start the BioLegato interface
     *       (calls 'main' with the canvas to load)</li>
     * </ol>
     * <p><i>Note, the above list is <u>NOT</u> the same as the list for
     *       <code>main (String[] args)</code>.</i></p>
     * <p>This method contains code necessary for enabling Turtle SHELL.</p>
     **
     * @param canvas the canvas class to load BioLegato with.
     * @param args the command line arguments for BioLegato.
     */
    public static void main(Class<? extends DataCanvas> canvas, String[] args) {
/////////////////////////////
//*************************//
//* 1. FUNCTION VARIABLES *//
//*************************//
/////////////////////////////
        // RESERVED (general purpose - inner scoped)
        // fileIn
        // currentCanvas
        Properties   propImport      = new Properties();
        List<File>   dataAdd         = new LinkedList<File>();
        String[]     propertiesFiles = null;
        boolean      serverMode      = false;
        boolean      pipeInput       = false;

////////////////////////////////////
//********************************//
//* 2. PROPERTIES INITIALIZATION *//
//********************************//
////////////////////////////////////

        // ------------------ UNCOMMENT BELOW FOR TURTLESHELL ------------------
        //Turtle.localenv.put("PWD",     CURRENT_DIR);
        //Turtle.localenv.put("BL_DIR",  envreplace("$BL_DIR"));
        //Turtle.localenv.put("BL_HOME", CURRENT_DIR);
        // ------------------ UNCOMMENT ABOVE FOR TURTLESHELL ------------------

        ////////////////////////////////////////////////////////////////////////
        // Generate a list of properties files to read                        //
        // This list is obtained from the $BL_PROPERTIES environment variable //
        ////////////////////////////////////////////////////////////////////////
        // The $BL_PROPERTIES environment variable is treated as a list of
        // properties files for BioLegato.  Each list entry is seperated by
        // File.separator (':' in UNIX, ';' in Windows).  The files are read
        // in string order from left-to-right.
        if (System.getenv("BL_PROPERTIES") != null) {
            propertiesFiles = toPathList(System.getenv("BL_PROPERTIES"));
        } else {
            // If $BL_PROPERTIES is not set, then the default properties
            // file location is: "$BL_DIR/.blproperties".
            propertiesFiles = new String[] { "$BL_DIR" + File.separator
                    + ".blproperties" };
        }

        ////////////////////////////////////////////////////////////////////////
        // Load the default properties JAR file.                              //
        ////////////////////////////////////////////////////////////////////////
        // This coad reads in the default properties from the JAR file.  This
        // ensures that all properties have default values.
        // Reading is done with a try-catch block to ensure that BioLegato
        // will NOT start if there are any errors reading the default
        // properties file.  The default properties file is contained within
        // the root directory of BioLegato's JAR file.
        try {
            // Read the JAR's default properties located
            // at /default.properties, within the JAR file.
            propImport.load(DataCanvas.class.getResourceAsStream(
                    "/default.properties"));
        } catch (IOException ioe) {
            System.err.println("FATAL ERROR - CORRUPT JAR FILE");
            ioe.printStackTrace(System.err);
            System.exit(1);
        }

        ////////////////////////////////////////////////////////////////////////
        // Read all of the properties
        ////////////////////////////////////////////////////////////////////////
        // Read all of the additional properties files
        // (specified by the $BL_PROPERTIES environment variable).
        for (String file : propertiesFiles) {
            File propertiesFileTemp = new File(file);

            // If the properties path specified is a directory, then load the
            // .blproperties file contained within that directory.
            if (propertiesFileTemp.isDirectory()) {
                propertiesFileTemp = new File(file + File.separator
                        + ".blproperties");
            }

            // If the path specified is a file, then load the properties from
            // that file.
            if (propertiesFileTemp.exists() && propertiesFileTemp.canRead()
                    && propertiesFileTemp.isFile()) {
                try {
                    propImport.load(new FileInputStream(propertiesFileTemp));
                } catch (Throwable e) {
                    e.printStackTrace(System.err);
                }
            }
        }
        propertiesFiles = null;

////////////////////////
//********************//
//* 3. LOAD PLUG-INS *//
//********************//
////////////////////////

        for (String pluginDir : toPathList(propImport.getProperty("plugins"))) {
            // load the plugins and file formats
            PluginLoader.loadPlugins(plugins, pluginDir);
        }

/////////////////////////////////////////
//*************************************//
//* 4. PROCESS COMMAND-LINE ARGUMENTS *//
//*************************************//
/////////////////////////////////////////

        // ensure that args is not null
        if (args != null) {
            // itterate through the command arguments
            for (String rawarg : args) {
                try {
                    // discard null arguments
                    if (rawarg != null) {
                        File fileIn = null;

                        // Create a new file object.  This file object is used
                        // to test if the argument specified (rawarg) is a valid
                        // system file.  If not, then we must do parameter
                        // parsing.
                        fileIn = new File(rawarg);

                        // if the argument is a file name then read the file;
                        // otherwise, parse the argument
                        if (fileIn != null && fileIn.exists() && fileIn.isFile()
                                && fileIn.canRead()) {
                            dataAdd.add(fileIn);
                        } else if (rawarg.startsWith("/") || rawarg.startsWith(
                                "-")) {
                            // copy the command line argument
                            String value = null;
                            String argument = rawarg;

                            // trim the argument
                            if (argument.startsWith("--")
                                    && argument.length() > 2) {
                                // trim -- from the argument name
                                argument = argument.substring(2);
                            } else if ((argument.startsWith("/")
                                        || argument.startsWith("-"))
                                    && argument.length() > 1) {
                                // trim - or / from the argument name
                                argument = argument.substring(1);
                            }

                            // If there is an equals '=' sign then the argument
                            // has a value.  Set the value variable to the
                            // arguments' value for further parsing.
                            if (argument.indexOf('=') > 0) {
                                value = argument.substring(
                                        argument.indexOf('=') + 1);
                                argument = argument.substring(0,
                                        argument.indexOf('='));
                            }

                            // make the argument lower case for better parsing
                            argument = argument.toLowerCase().trim();

                            //////////////////////////////////////
                            //**********************************//
                            //* PROCESS COMMAND LINE ARGUMENTS *//
                            //**********************************//
                            //////////////////////////////////////
                            if ("help".equals(argument) || "h".equals(argument)
                                    || "?".equals(argument)) {
                                ////////////
                                //********//
                                //* HELP *//
                                //********//
                                ////////////
                                // show BioLegato's usage
                                System.out.println(
                                        "Usage: biolegato [options] [files]\n"
                                        + "Use --optionlist  to see a detailed"
                                            + " list of options\n"
                                        + "Use --manpage     to see BioLegato's"
                                            + " manpage");
                                System.exit(0);
                                
                            } else if ("optionlist".equals(argument)) {
                                ///////////////////
                                //***************//
                                //* OPTION LIST *//
                                //***************//
                                ///////////////////
                                // Show BioLegato's list of options:
                                // i.e., write the file "optionlist.txt" to
                                // System.in -- "optionlist.txt" is located
                                // within the root (/) of the jar file
                                new StreamCopier(StreamCopier.DEFAULT_BUFF_SIZE,
                                        DataCanvas.class.getResourceAsStream(
                                            "/optionlist.txt"),
                                        System.out).run();
                                System.exit(0);

                            } else if ("manpage".equals(argument)
                                    || "man".equals(argument)) {
                                ///////////////
                                //***********//
                                //* MANPAGE *//
                                //***********//
                                ///////////////
                                // Show BioLegato's manpage:
                                // i.e., write the file "manpage.txt" to
                                // System.in -- "manpage.txt" is located
                                // within the root (/) of the jar file
                                new StreamCopier(StreamCopier.DEFAULT_BUFF_SIZE,
                                        DataCanvas.class.getResourceAsStream(
                                            "/manpage.txt"), System.out).run();
                                System.exit(0);

                            } else if (("exec-properties".equals(argument)
                                    || "ep".equals(argument))
                                    && value != null) {
                                ///////////////////////
                                //*******************//
                                //* EXEC PROPERTIES *//
                                //*******************//
                                ///////////////////////
                                // Executes a command specified (after an equals
                                // sign), and read the standard output from this
                                // command as if it were a properties file.
                                Process p = Runtime.getRuntime().exec(
                                        BLMain.envreplace(value).split("\\s"));
                                p.getOutputStream().close();
                                propImport.load(p.getInputStream());
                                
                            } else if ("version".equals(argument)
                                    || "v".equals(argument)) {
                                ///////////////
                                //***********//
                                //* VERSION *//
                                //***********//
                                ///////////////
                                // Show BioLegato's version number.
                                // NOTE: @VERSION@ is replaced with the
                                // version number of BioLegato by the ant
                                // build script.
                                System.out.println("BioLegato v@VERSION@");
                                System.exit(0);
                                
                            } else if ("debug".equals(argument)) {
                                /////////////
                                //*********//
                                //* DEBUG *//
                                //*********//
                                /////////////
                                // Force/enable DEBUG MODE.
                                debug = true;
                                
                            } else if ("plugins".equals(argument)) {
                                ///////////////
                                //***********//
                                //* PLUGINS *//
                                //***********//
                                ///////////////
                                // Show all of the plugins loaded successfully
                                // into BioLegato.
                                System.out.println(
                                          "***********\n"
                                        + "* PLUGINS *\n"
                                        + "***********");
                                System.out.println("(Current plugins path: "
                                        + propImport.getProperty("plugins")
                                        + ")\n"
                                        + "-- listing plugins loaded --");
                                String[] pluginList =
                                        plugins.keySet().toArray(
                                        new String[0]);
                                for (String pluginName : pluginList) {
                                    System.out.append("Plugin: "
                                            ).append(pluginName).append("\n");
                                }
                                System.out.flush();
                                System.out.println("-- end of plugin list --");
                                
                            } else if ("pipe".equals(argument)) {
                                ////////////
                                //********//
                                //* PIPE *//
                                //********//
                                ////////////
                                // Pipe input from System.in into the canvas
                                pipeInput = true;
                            } else if ("server".equals(argument)) {
                                //////////////
                                //**********//
                                //* SERVER *//
                                //**********//
                                //////////////
                                // Starts BioLegato as a PCD menus server for
                                // other BioLegato instances.
                                // (NOT YET IMPLEMENTED)
                                serverMode = true;
                            } else {
                                System.err.println("Unknown argument: "
                                        + rawarg);
                            }
                        }
                    }
                } catch (Throwable th) {
                    System.err.println("Error processing argument: " + rawarg);
                    th.printStackTrace(System.err);
                }
            }
        }

////////////////////////////////////
//********************************//
//* 5. CORRECT DEBUG MODE STATUS *//
//********************************//
////////////////////////////////////
        // Correctly set the debug parameter after all command-line arguments
        // are specified.  The debug variable is set after processing the
        // enture command-line, because additional properties files and a
        // command-line specific debug-switch can both alter whether the debug
        // parameter should be turned on.  Please note that the command-line
        // "debug" switch takes precedence over any properties file value for
        // debug mode.
        if ("true".equalsIgnoreCase(propImport.getProperty("debug"))
                || (System.getenv("BL_DEBUG") != null
                    && !"".equals(System.getenv("BL_DEBUG").trim())
                    && !"false".equalsIgnoreCase(System.getenv("debug")))) {
            debug = true;
        }

        // If BioLegato is in DEBUG MODE, then display that the
        // command-line arguments were read successfully.
        if (debug) {
            System.err.println("Command line arguments read successfully");
            
            // Also, display all of the properties values loaded into BioLegato.
            System.err.println(
                      "**************\n"
                    + "* PROPERTIES *\n"
                    + "**************");
            propImport.list(System.err);
        }

////////////////////////////////////
//********************************//
//* 6. START BIOLEGATO INTERFACE *//
//********************************//
////////////////////////////////////
        if (!serverMode && canvas != null) {
            main(canvas, propImport, dataAdd, pipeInput);
        } else {
            // TODO: implement server mode for BioLegato
            try {
                //Naming.rebind ("BioLegato", new BLServer ());
                System.out.println ("BioLegato Server is ready.");
            } catch (Exception e) {
                System.out.println ("BioLegato Server failed: " + e);
            }
        }
    }

//------------------------------ END OF METHOD ------------------------------//
    
    /**
     * <p>Starts a new BioLegato instance (i.e. loads a canvas).</p>
     *
     * <p>This method is split into the following sections:</p>
     * <ol>
     *   <li>Initialize the canvas.</li>
     *   <li>Read initial canvas data -- read any files (such as GenBANK or
     *      FASTA DNA/Protein sequences) to initialize the canvas with).</li>
     *   <li>Add the initial BioLegato menu headings (e.g. File, Edit).</li>
     *   <li>Read in the custom PCD menus.</li>
     *   <li>Add the default trailing menu items.</li>
     *   <li>DONE: Display the new BioLegato instance.</li>
     * </ol>
     **
     * @param canvasClass the DataCanvas to load BioLegato with.
     */
    public static void main(Class<? extends DataCanvas> canvasClass,
            Properties propImport, List<File> dataAdd, boolean pipeInput) {

////////////////////////////////
//****************************//
//* 1. INITIALIZE THE CANVAS *//
//****************************//
////////////////////////////////
        
        // Declare canvas variable
        DataCanvas    canvas          = null;

        try {
            canvas = (DataCanvas) canvasClass.getConstructor(new Class[]{
                Map.class}).newInstance(new Object[]{propImport});

///////////////////////////////////
//*******************************//
//* 2. READ INITIAL CANVAS DATA *//
//*******************************//
///////////////////////////////////
            // get the input for the canvas from system.in
            if (pipeInput) {
                try {
                    canvas.readFile("", new InputStreamReader(System.in),
                            false,false);
                } catch (IOException ioe) {
                    System.out.println("Failed to read System.in");
                    ioe.printStackTrace(System.err);
                }
            }

            for (File file : dataAdd) {
                try {
                    if (file != null && file.exists() && file.isFile()
                            && file.length() > 0) {
                        canvas.readFile("", new FileReader(file), false,false);
                    }
                } catch (FileNotFoundException ex) {
                    ex.printStackTrace(System.err);
                } catch (IOException ex) {
                    ex.printStackTrace(System.err);
                }
            }

///////////////////////////////////////
//***********************************//
//* 3. READ IN THE CUSTOM PCD MENUS *//
//***********************************//
///////////////////////////////////////

            // load all PCD menu files
            loadPCD(canvas);

            // load any Java class "Plugin menus"
            for (String filename
                    : toPathList(canvas.getProperty("pcd.menus.path"))) {
                loadPluginMenus(canvas, new File(filename));
            }

//////////////////////////////////////////////
//******************************************//
//* 4. ADD THE DEFAULT TRAILING MENU ITEMS *//
//******************************************//
//////////////////////////////////////////////

            // Add the "Exit" button
            canvas.addMenuHeading("File").add(canvas.EXIT_MENUITEM);

            // Add the "About" button
            canvas.addMenuHeading("Help").add(canvas.ABOUT_MENUITEM);

///////////////////////////////////////////////////
//***********************************************//
//* 5. DONE: DISPLAY THE NEW BIOLEGATO INSTANCE *//
//***********************************************//
///////////////////////////////////////////////////

            canvas.createJFrame();
        } catch (Exception ex) {
            ex.printStackTrace(System.err);
        }
    }

//------------------------------ END OF METHOD ------------------------------//

    /**
     * <p>For each instance of $XXX (where XXX is the name of an environment
     * variable), this function replaces $XXX with its current value (which
     * is obtained from the environment via. System.getenv).</p>
     *
     * <p>This function provides functionality similar to BASH (i.e. replaces
     * $XXX with the value of XXX in the environment.)</p>
     *
     * <p><i>NOTE: there are some environment variables intrinsic to BioLegato.
     * <br />These variables are:
     * <dl><dt>$BL_DIR</dt><dd>the directory which contains BioLegato</dd>
     *     <dt>$BL_HOME</dt><dd>the directory where the user launched BioLegato
     *                     (this is the only intrinsic variable, which the
     *                      environment can override -- i.e. if $BL_HOME is
     *                      set in the System's environment, the System
     *                      environment value will take precedence).</dd>
     *     <dt>$HOME</dt><dd>the user's home directory</dd></dl>
     * </i></p>
     **
     * @param original the string to modify.
     * @return the modified string.
     */
    public static String envreplace(final String original) {
        // TRANSITION WRAPPING (for future JApplet support!)
        return (String) AccessController.doPrivileged(
            new PrivilegedAction() {
                public Object run() {
                    try {
                        int start = 0;
                        int end = -1;
                        String replace = null;
                        String variable = "";
                        String change = original;

                        /**
                         * This constant is used to improve the legibility of
                         * the code which figures out the PROGRAM_DIR
                         * properties.  DO NOT OTHERWISE USE THIS CONSTANT,
                         * AS IT MAY DISAPPEAR IN FUTURE VERSIONS!
                         */
                        final File EXE_DIR_DONT_USE = new File(
                                DataCanvas.class.getProtectionDomain(
                                ).getCodeSource().getLocation().getPath());
                        /**
                         * This constant is set to the path of BioLegato.jar
                         * The value of this constant determined at runtime.
                         */
                        final String PROGRAM_DIR
                                = ( EXE_DIR_DONT_USE.isDirectory()
                                  ? EXE_DIR_DONT_USE.getPath()
                                  : EXE_DIR_DONT_USE.getParentFile().getPath()
                                  );
                        /**
                         * Stores the user's home directory
                         * TRANSITION WRAPPING (for future JApplet support!)
                         */
                        final String HOME_DIR = (String)
                            AccessController.doPrivileged(
                            new PrivilegedAction() {
                                public Object run() {
                                    try {
                                        return System.getProperty("user.home");
                                    } catch (Throwable th) {
                                        th.printStackTrace(System.err);
                                        return CURRENT_DIR;
                                    }
                                }
                            }
                        );

                        // Protect against a null input string
                        if (change != null) {
                            /////////////////////////////////////////
                            // Replace each variable in the string //
                            /////////////////////////////////////////
                            // This is done with the following algorithm:
                            //      INITIALIZE:
                            //      - start the algorithm with 'start' = 0
                            //
                            //      BEGIN LOOP:
                            //      - find the next $ symbol from 'start'
                            //      - use a for loop to find how long the
                            //          environment variable name is, set
                            //          this position to 'end'
                            //      - search the environment for the variable:
                            //          - if the variable is BL_DIR or HOME
                            //            then use the intrinsic values
                            //          - if the variable is BL_HOME, then only
                            //            use the intrinsic value, if it has no
                            //            set value in the system environment
                            //      - replace the $ variable instacnce
                            //          - if the variable does not exist in
                            //            the environment, or instrinsically,
                            //            then skip it and just increment
                            //            'start' (i.e. do not replace);
                            //            however, DO NOT RELY ON THIS, because
                            //            if the value is set to "", then the
                            //            variable WILL be replaced with ""!
                            //      END LOOP
                            /////////////////////////////////////////
                            while ((start = change.indexOf('$', start)) >= start
                                    && start > -1) {
                                for (end = start + 1; end < change.length() &&
                                        (change.charAt(end) == '_'
                                            || Character.isLetterOrDigit(
                                            change.charAt(end))); end++) {
                                    /* ITTERATE */
                                }

                                // get the information to perform the string
                                // replacement.
                                variable = change.substring(start + 1, end);
                                replace = System.getenv(variable);

                                // ensure BL_DIR is set properly.
                                if (variable.equalsIgnoreCase("BL_DIR")) {
                                    replace = PROGRAM_DIR;
                                }

                                // ensure HOME is set properly.
                                if (variable.equalsIgnoreCase("HOME")) {
                                    replace = HOME_DIR;
                                }

                                // ensure BL_HOME is set properly.
                                if (variable.equalsIgnoreCase("BL_HOME")) {
                                    if (replace == null || "".equals(
                                            replace.trim())
                                            || !new File(replace).exists()) {
                                        replace = CURRENT_DIR;
                                    } else {
                                        replace = new File(
                                                replace).getAbsolutePath();
                                    }
                                }

                                // perform the string replacement.
                                if (replace != null) {
                                    change = change.substring(0, start)
                                            + replace + change.substring(end);
                                } else {
                                    // if there is no replacement, just skip
                                    // this instance of $VARIABLE
                                    start++;
                                }
                            }
                        }
                        // return the replaced string
                        return change;
                    } catch (Throwable th) {
                        th.printStackTrace(System.err);
                        return original;
                    }
                }
            });
    }

//------------------------------ END OF METHOD ------------------------------//

    
    /**
     * <p>Separates a string into a list of substrings, using File.pathSeparator
     * as the delimiter.  This allows the user to specify multiple paths in a
     * string.</p>
     * 
     * <p>This function does NOT evaluates any environment variables within
     * the original or split strings.</p>
     **
     * @param searchstring the string to split into multiple file paths.
     * @return a list of file paths contained within the input string.
     */
    public static String[] toPathList (String searchstring) {
        // Allow for escaping the File.pathSeparator character in each path
        String[] pathlist = searchstring.split("(?<!\\\\)"
                + Pattern.quote(File.pathSeparator));

        // Fix all escaped the File.pathSeparator characters
        for (int idx = 0; idx < pathlist.length; idx++) {
            pathlist[idx] = pathlist[idx].replace("\\" + File.pathSeparator,
                    File.pathSeparator);
        }
        return pathlist;
    }

//------------------------------ END OF METHOD ------------------------------//

    /**
     * <p>Recursively load all of the Plugin menus (within a given path)
     * into a BioLegato canvas.  Note that Plugin menus are Java classes which
     * conform to the following specification:</p>
     *
     * <ol>
     *  <li><dl><dt>extend the JMenuItem, JMenu, or Action class</dt>
     *    <dd><i>NOTE: JMenu objects can only be read from .jar files, while
     *           JMenuItem and Action can only be read from .class files!</i>
     *    </dd></dl></li>
     *  <li>has one constructor which accepts a Datacanvas as its parameter</li>
     *  <li>the class is contained within a .class or .jar file</li>
     * </ol>
     */
    public static void loadPluginMenus(DataCanvas canvas, File path) {
        String tpath = path.getAbsolutePath().toLowerCase();

        // Ensure that the path is valid.
        if (path.exists() && path.canRead()) {
            // RECURSION if-statement:  1 recursive case and 2 base cases
            if (path.isDirectory()) {
                // RECURSIVE CASE: if the parameter 'path' is a directory, call
                //      loadPluginMenus on each subdirectory/file within 'path'
                for (File subdir : path.listFiles()) {
                    loadPluginMenus(canvas, subdir);
                }
            } else if (tpath.endsWith(".class")) {
                // BASE CASE #1/2: if the file is a class file, try loading it.
                try {
                    /*
                     * Handles reading in class files instead of PCD
                     * (this feature can be enabled from the properties file
                     * for BioLegato).
                     */
                    
                    // The DataCanvas class object.  Used to find the proper
                    // constructor method for the class represented by
                    // the plugin.
                    final Class [] canvasClass
                            = new Class [] { DataCanvas.class };

                    // The current canvas.  Used for calling the plugin class's
                    // constructor method (after found using canvasClass).
                    final Object[] canvasObject
                            = new Object[] { canvas };

                    // The plugin hash object.  Used for indexing plugins.
                    Map<String, PluginWrapper> pluginHash
                            = new HashMap<String, PluginWrapper>();

                    // Load the classes from the .class file into 'pluginHash'
                    PluginLoader.loadClasses(pluginHash, path.getParentFile(
                            ).toURI().toURL(), path.getName().substring(0,
                            path.getName().length() - 6));

                    // Parse each class in 'pluginHash'; if it is a JMenuItem,
                    // or an Action, then try loading it as a BioLegato menu.
                    for (PluginWrapper plugin : pluginHash.values()) {
                        // SKIP all classes which contain a $ in their names
                        if (!plugin.getName().contains("$")) {
                            if (plugin.isA(JMenuItem.class)) {
                                // handle JMenuItem objects
                                try {
                                    // Create a new JMenuBar from the plugin
                                    // class.
                                    canvas.getJMenuBar().add((JMenuItem)
                                            plugin.create(canvasClass,
                                                canvasObject));
                                } catch (Throwable th) {
                                    // Handle failure creating the JMenuBar.
                                    System.err.println("BioPCD: error loading"
                                            + " the plugin menu: "
                                            + plugin.getName());
                                    th.printStackTrace(System.err);
                                }
                            } else if (plugin.isA(Action.class)) {
                                // handle Action objects
                                try {
                                    // Create a new JMenuItem from the plugin
                                    // class.
                                    canvas.getJMenuBar().add(new JMenuItem(
                                            (Action) plugin.create(canvasClass,
                                                canvasObject)));
                                } catch (Throwable th) {
                                    // Handle failure creating the JMenuItem.
                                    System.err.println("BioPCD: error loading"
                                            + " the plugin menu: "
                                            + plugin.getName());
                                    th.printStackTrace(System.err);
                                }
                            }
                        }
                    }
                } catch (Throwable th) {
                    // Handle failure reading the plugin.
                    System.err.println("BioPCD: error loading the class: "
                            + path);
                    th.printStackTrace(System.err);
                }
            } else if (tpath.endsWith(".jar")) {
                // BASE CASE #2/2: if the file is a JAR file, load each class
                //      within the JAR file.
                try {
                    /*
                     * Handles reading in class files instead of PCD
                     * (this feature can be enabled from the properties
                     * file for BioLegato).
                     */
                    Map<String, PluginWrapper> pluginHash
                            = new HashMap<String, PluginWrapper>();

                    // Load the classes from the .jar file into 'pluginHash'
                    PluginLoader.loadJar(pluginHash, path);

                    // Parse each class in 'pluginHash'; if it is a JMenu
                    // class, then try loading it as a BioLegato menu.
                    for (PluginWrapper plugin : pluginHash.values()) {
                        if (plugin.isA(JMenu.class)) {
                            try {
                                canvas.getJMenuBar().add(
                                        (JMenu) plugin.create());
                            } catch (Throwable th) {
                                System.err.println("BioPCD: error loading the"
                                        + " plugin: " + plugin.getName());
                                th.printStackTrace(System.err);
                            }
                        }
                    }
                } catch (Throwable th) {
                    System.err.println("BioPCD: error loading the jar file: "
                            + path);
                    th.printStackTrace(System.err);
                }
            }
        }
    }

//------------------------------ END OF METHOD ------------------------------//

    /**
     * Loads the entire PCD menu structure into BioLegato
     */
    public static void loadPCD (DataCanvas canvas) {
        // Stores the PCD menu items parsed
        Map<String,Map<String,PCDObject>> result
                = new LinkedHashMap<String,Map<String,PCDObject>>();

        
        // Set PCD to only be in DEBUG MODE if BioLegato is also in DEBUG MODE
        PCD.debug = debug;

        // Load every PCD file, recursively, from BioLegato's "pcd.menus.path"
        // configuration parameter.  This parameter supports multiple paths,
        // by using BLMain.toPathList. With each iteration, path is set to
        // the one of the directories in pcd.menus.path.
        for (String path
                : BLMain.toPathList(canvas.getProperty("pcd.menus.path"))) {
            PCD.loadPCDPath(new File(path), result, canvas, canvas.getJFrame());
        }


        
        // If the "pcd.exec" configuration parameter is set, then run the
        // command specified by "pcd.exec" and pipe its output to the PCD parser
        if (!"".equals(canvas.getProperty("pcd.exec"))) {
            try {
                // Execute the program and extract its standard ouptut
                Process p = Runtime.getRuntime().exec(BLMain.envreplace(
                        canvas.getProperty("pcd.exec")).split("\\s"));
                p.getOutputStream().close();

                // Pipe the output into the PCD parser.
                File home = new File(BLMain.envreplace("$BL_HOME"));
                PCD parser = new PCD(new InputStreamReader(p.getInputStream()));
                new Thread(new StreamCopier(StreamCopier.DEFAULT_BUFF_SIZE,
                        p.getErrorStream(), System.err)).start();

                // Parse the menu items
                parser.parseFullMenu(result, 0, home, canvas,
                        canvas.getJFrame());
            } catch (Throwable th) {
                th.printStackTrace(System.err);
            }
        }

        // Add every menu item parsed by the PCD parser to BioLegato's menu bar

        for (Map.Entry<String, Map<String, PCDObject>> entry
                    : result.entrySet()) {
            for (PCDObject pcdo
                        : entry.getValue().values()) {
                                
                // Generate the JMenuItem object for the menu
                JMenuItem jmiresult = generateJMenuItem(pcdo,
                        canvas.getJFrame());

                // Add the menu to the menu bar
                canvas.addMenuHeading(entry.getKey()).add(jmiresult);
            }
        }
    }

//------------------------------ END OF METHOD ------------------------------//

    /**
     * <p>Generates JMenuItems for PCD objects read in by the parser.</p>
     *
     * <p>This method ensures the following:</p>
     * <ol>
     *   <li>only PCD menus supported on the current system will be loaded
     *       (e.g. if you run BioLegato, as part of BIRCH, on a Mac OS X
     *             machine, the BIRCH PCD menu directory will contain BOTH
     *             Linux and OS X menus, but BioLegato will only load those
     *             menus compatible with OS X -- ignoring those which only
     *             work on Linux, or other operating systems).</li>
     *
     *   <li>PCD menus can have program icons<br />
     *          <i>NOTE: program icons can use relative or absolute paths,
     *                with OR without environment variables.  The environment
     *                variables are handled by 'envreplace', thus all BioLegato
     *                intrinsic variables (e.g. $BL_DIR) are available for use!
     *          </i>
     *   </li>
     *
     *   <li>PCD menus can have tooltips</li>
     *
     *   <li>PCD menus are imported as either:
     *     <ol type="a">
     *       <li><dl><dt>CommandThread objects</dt><dd>do not require the user
     *                  to set parameters to run the program</dd></dl></li>
     *       <li><dl><dt>RunWindow objects</dt><dd>require the user to set
     *                  parameters</dd></dl></li>
     *     </ol>
     *   </li>
     * </ol>
     **
     * @param pcdo the PCD object to generate the JMenuItem for
     * @param parent the parent JFrame (for modality purposes)
     * @return returns the corresponding JMenuItem object for the PCD code.
     */
    private static JMenuItem generateJMenuItem(PCDObject pcdo, JFrame parent) {
        /* A file object to ensure a proper icon file path exists */
        File imageFile = null;
        /* The icon file name for displaying on the menu */
        ImageIcon imageIcon = null;
        /* The icon data for displaying in the parameters window
         * for the menu item*/
        BufferedImage image = null;
        /* The resulting menu item that was parsed from the file */
        JMenuItem jmiresult = null;

        //////////////////////////////////////////////////////////////////////
        // 1. Ensure that only PCD menus supported by the current system    //
        //    (and computer architechture) are loaded into BioLegato.       //
        //////////////////////////////////////////////////////////////////////
        if (pcdo.isSystemSupported()) {
            ///////////////////////////////////////////
            // 2. Load any applicable program icons. //
            ///////////////////////////////////////////
            try {
                // check if we are loading a program icon
                if (pcdo.icon != null && !"".equals(pcdo.icon)) {
                    // Handle environment variables in the path
                    String imagePath = BLMain.envreplace(pcdo.icon);

                    /////////////////////////////
                    // Handle image file paths //
                    /////////////////////////////
                    // This code block will branch based on whether the path
                    // specified for the program icon's image file is relative
                    // or absolute.  This is because Java's File class provides
                    // at least two constructors -- one for relative paths and
                    // one for absolute paths.  To avoid any possible
                    // under-the-hood API problems, BioLegato detects which
                    // constructor to use, and used the appropriate constructor.
                    /////////////////////////////
                    if (imagePath.startsWith("/")) {
                        imageFile = new File(imagePath);
                    } else {
                        imageFile = new File(imagePath, imagePath);
                    }

                    // Load the program icon's image file into a Java ImageIcon
                    // object, for use with the program's final JMenuItem
                    if (imageFile != null && imageFile.exists()
                            && imageFile.canRead() && imageFile.isFile()) {
                        BufferedImage fileImage = ImageIO.read(imageFile);

                        // image load code adapted from:
                        // http://stackoverflow.com/questions/6916693/jmenuitem-imageicon-too-big
                        if (fileImage != null) {
                            image = new BufferedImage(ICONW, ICONH,
                                    BufferedImage.TYPE_INT_RGB);
                            image.getGraphics().drawImage(fileImage, 0, 0,
                                    ICONW, ICONH, null);
                            imageIcon = new ImageIcon(image);
                        }
                    }
                }
            } catch (Exception e) {
                System.err.println("BioPCD: Invalid image format: "
                        + pcdo.icon);
                imageIcon = null;
            }

            // If an image icon was loaded successfully, then we add it to the
            // program's JMenuItem.  Otherwise, we do not add any image to the
            // program's JMenuItem object.
            if (imageIcon != null) {
                jmiresult = new JMenuItem(pcdo.name, imageIcon);
            } else {
                jmiresult = new JMenuItem(pcdo.name);
            }

            // If a tooltip was specified in the PCD code,
            // add it to the program's JMenuItem object!
            if (pcdo.tooltip != null && !"".equals(pcdo.tooltip)) {
                jmiresult.setToolTipText(pcdo.tooltip);
            }

            // Determine whether to create a new window on clicking the menu
            // item or directly run the command specified in the PCD file's
            // exec parameter.
            //
            // This is determined by wheter the exec parameter is specified
            // in the PCD file!
            if (pcdo.exec != null) {
                jmiresult.addActionListener(new CommandThread(pcdo.exec,
                        pcdo.widgetList));
            } else {
                jmiresult.addActionListener(new RunWindow(pcdo.name,
                        pcdo.widgetList, parent, image));
            }
        } else {
            System.out.println("System not supported");
        }
        return jmiresult;
    }


//------------------------------ END OF METHOD ------------------------------//

    /**
     * Opens an HTML web browser in BioLegato
     **
     * WARNING: if the function fails, null will be returned instead
     *          of an object.
     **
     * @param dest the destination URL to display in the web browser.
     */
    public void browser (URL dest) {
        Desktop desktop = Desktop.getDesktop();

        // if Java 1.7 is installed, and desktop.browse is supported
        if(desktop.isSupported( java.awt.Desktop.Action.BROWSE ) ) {
            try {
                desktop.browse( dest.toURI() );
            } catch ( Exception e ) {
                System.err.println( e.getMessage() );
            }
        } else {
            // default to BioLegato's VERY simple HTML browser
            JFrame dialog = new JFrame("URL: " + dest);
            JTextPane webpane;
            JScrollPane browser = null;
            if (dest != null) {
                try {
                    webpane = new JTextPane();
                    webpane.setContentType("text/html");
                    webpane.setPage(dest);
                    webpane.setEditable(false);
                    browser = new JScrollPane(webpane,
                            JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                            JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
                } catch (IOException ioe) {
                    ioe.printStackTrace(System.err);
                }
            }
            dialog.add(browser);
            dialog.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            dialog.setVisible(true);
            dialog.pack();
        }
    }
}
