/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.biolegato.gde2pcd;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.File;
import java.io.IOException;
import java.io.FileWriter;
import java.net.URL;
import java.util.Set;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.regex.Pattern;

/**
 * The main class of the GDE to PCD menu file converter
 **
 * @author Graham Alvare
 * @author Brian Fristensky
 */
public class GDE2PCD {

    /**
     * Array list of the parameters of any object in the PCD menu file format.
     * This is ordered because PCD files are order-specific, whereas GDE menu
     * files are not order-specific (i.e. each tag in GDE can go anywhere).
     */
    private static String[] ordered_keys = new String[] {
        "type",
        "name",
        "label",
        "icon",
        "tooltip",
        "system",
        "exec",
        "direction",
        "format",
        "min",
        "max",
        "default",
        "save",
        "overwrite",
        "shell",
        "close",
        "check",
        "choices"
    };

    /**
     * A list of the various types of BioLegato instances in BIRCH
     */
    private static String[] biolegatos = new String[] {
        "bldna",
        "blprotein",
        "blmarker",
        "bltable",
        "bltree"
    };
    /**
     * The ProtectionDomain CodeSource URL for the GDE2PCD class (used for
     * getting the path of the program directory -- PROGRAM_DIR).
     */
    public static final URL purl
            = GDE2PCD.class.getProtectionDomain().getCodeSource().getLocation();
    /**
     * This constant is set to the path of BioLegato.jar
     * The value of this constant determined at runtime.
     */
    public static final String PROGRAM_DIR =
            new File(purl.getPath()).isDirectory()
                ? purl.getPath()
                : new File(new File(purl.getPath()).getParent()).getPath();
    /**
     * The output directory for the generated PCD menus.
     */
    public static final File WRITE_DIR = new File(PROGRAM_DIR, "output");
    /**
     * This constant is set to the path of where biolegato was run
     * The value of this constant determined at runtime.
     */
    public static final String CURRENT_DIR = System.getProperty("user.dir");
    /**
     * Stores the user's home directory
     */
    public static final String HOME_DIR = System.getProperty("user.home");
    /**
     * The extension to use for the menu files
     */
    public static final String PCD_EXTENSION = ".blmenu";

    /**
     * The main method for this object (does the GDE to PCD conversion)
     **
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        // TODO code application logic here
        Map<String, Map<String, Map<String, Object>>> menu
                = new LinkedHashMap<String, Map<String, Map<String, Object>>>();

        // Process any command line arguments (if applicable).
        if (args != null && args.length >= 1) {
            // Whether or not to process any makemenus paths specified by
            // $GDE_MAKEMENUS_DIR
            boolean makemenus = false;
            // Whether or not to process any .GDEmenus files specified in the
            // standard GDE paths (including relevant environment variables).
            boolean standard  = false;
            // Whether or not to process any BIRCH installations (specified by
            // the $BIRCH variable).
            boolean birch     = false;

            // prevent repeat reading of command line variables
            // i.e. we only want to translate each version of
            //      the menus once since additional translation
            //      only overwrites the older menu copy thereby
            //      wasting the program and the user's time
            for (String arg : args) {
                if ("--makemenus".equals(arg)) {
                    makemenus = true;
                }
                if ("--birch".equals(arg)) {
                    birch = true;
                }
                if ("--standard".equals(arg)) {
                    standard = true;
                }
            }

            // Read a makemenus directory specified by $GDE_MAKEMENUS_DIR
            if (makemenus) {
                if (System.getenv("GDE_MAKEMENUS_DIR") != null) {
                    menu.clear();
                    GDEMakeMenus.loadMenu(System.getenv("GDE_MAKEMENUS_DIR"),
                            menu, true);
                    writePCD(new File(WRITE_DIR, "makemenus"), menu);
                } else {
                    System.err.println("ERROR: GDE_MAKEMENUS_DIR not set!");
                }
            }

            // Read in .GDEmenus files following the search paths used by GDE.
            if (standard) {
                menu.clear();
                GDEMenu.loadMenu(menu);
                writePCD(new File(WRITE_DIR, "standard"), menu);
            }

            // Read in GDE menus from a BIRCH installation.
            // (The $BIRCH environment variable MUST be specified!)
            if (birch) {
                if (System.getenv("BIRCH") != null) {
                    // Read all of the makemenus from the different BioLegato
                    // types that comprise a traditional BIRCH installation.
                    // (Note, this may change in the future, as GDE makemenus
                    // are being abandonned in favour of PCD menus.
                    for (String subdir : biolegatos) {
                        menu.clear();
                        GDEMakeMenus.loadMenu(System.getenv("BIRCH")
                                + File.separator + "dat"
                                + File.separator + subdir, menu, false);
                        writePCD(new File(WRITE_DIR, subdir), menu);

                        menu.clear();
                        GDEMakeMenus.loadMenu(System.getenv("BIRCH")
                                + File.separator + "local"
                                + File.separator + "dat"
                                + File.separator + subdir, menu, false);
                        writePCD(new File(WRITE_DIR, "local"
                                + File.separator + subdir), menu);
                    }
                } else {
                    System.err.println("ERROR: BIRCH not set!");
                }
            }
        } else {
            // If no command line arguments are specified, print usage.txt
            // (located in this JAR file's root folder) to the screen to tell
            // the user how to run the program (this program MUST have command
            // line arguments specified, in order to work properly).
            BufferedReader bufferedReader
                    = new BufferedReader(new InputStreamReader(
                        GDE2PCD.class.getResourceAsStream(File.separator
                            + "usage.txt")));
            try {
                String line = "";

                while ((line = bufferedReader.readLine()) != null) {
                    System.out.println(line);
                }
            } catch (IOException ioe) {
                ioe.printStackTrace();
            }
        }
    }

    /**
     * Writes a parsed intermediate menu map object to a PCD menu directory and
     * file structure.
     **
     * @param basedir the base directory to write the PCD to
     * @param menu the intermediate menu map object
     */
    private static void writePCD(File basedir,
            Map<String, Map<String, Map<String, Object>>> menu) {
        // The directory path for the PCD file to write (i.e. the menu name).
        File dirpath;
        // The PCD menu file to write.
        File menufile;
        // The file writer object for writing the pcd_order file for the output
        // directory (this is because both .GDEmenus and GDE makemenus have an
        // order, which this converter will preseve by using a pcd_order file).
        // This pcd_order file is specific for the MENUS (i.e. it is in the
        // parent directory of all of the PCD menu DIRECTORIES).
        FileWriter dirpcdorder;
        // The file writer object for writing the pcd_order file for the output
        // directory (this is because both .GDEmenus and GDE makemenus have an
        // order, which this converter will preseve by using a pcd_order file).
        // This pcd_order file is specific for the MENU ITEMS (i.e. it is in the
        // parent directory of all of the PCD menu item FILES).
        FileWriter filepcdorder;

        try {
            // Ensure that all of the necessary parent directories, for the base
            // directory, exist (by creating them if necessary).
            if (!basedir.exists()) {
                basedir.mkdirs();
            }

            // Create a new pcd_order FileWriter object to write the menu
            // directories pcd_order file.
            dirpcdorder = new FileWriter(new File(basedir, "pcd_order"));

            // iterate through the menus and create their respective
            // output directories
            for (Map.Entry<String, Map<String, Map<String, Object>>> dirEntry
                    : menu.entrySet()) {
                // ensure that the order of menu items is preserved by writing
                // a pcd_order file to the output directory
                dirpcdorder.append(dirEntry.getKey()).append("\n").flush();

                // ensure that the output directory exists
                dirpath = new File(basedir, dirEntry.getKey());
                if (!dirpath.exists()) {
                    dirpath.mkdirs();
                }

                System.out.println("MENU: " + dirpath + "\n"
                    + "-----------------");

                // open the pcdorder file for the current menu's output
                // directory
                filepcdorder = new FileWriter(new File(dirpath, "pcd_order"));

                // iterate and write the menu items to the directory
                for (Map.Entry<String, Map<String, Object>> menufileEntry
                        : dirEntry.getValue().entrySet()) {
                    // ensure that the order of menu items is preserved by
                    // writing a pcd_order file to the output directory
                    filepcdorder.append(menufileEntry.getKey()).append(
                            PCD_EXTENSION).append("\n").flush();

                    // write the menu item file
                    menufile = new File(dirpath, menufileEntry.getKey()
                            + PCD_EXTENSION);
                    System.out.println("ITEM: " + menufile);

                    try {
                        FileWriter out = new FileWriter(menufile);

                        // write the hashmap to the menu item output file
                        printRecursive(out, 0, menufileEntry.getValue(), false);
                        printRecursive(System.out, 0,
                                menufileEntry.getValue(), false);

                        // ensure proper flushing of the reader objects
                        out.flush();
                        out.close();
                    } catch (IOException ioe) {
                        ioe.printStackTrace(System.err);
                    }
                }
                // Flush and close the PCD menu item files' pcd_order file.
                filepcdorder.flush();
                filepcdorder.close();
            }
            // Flush and close the PCD menu directories' pcd_order file.
            dirpcdorder.flush();
            dirpcdorder.close();
        } catch (IOException ioe) {
            ioe.printStackTrace(System.err);
        }
    }

    /**
     * Recursively prints a Map to an Appendable object
     **
     * @param out the appendable object
     * @param level the level of indentation to print the entries at
     * @param menu the map object to print
     * @param quote whether or not to quote the entries
     * @throws IOException any IOExceptions thrown by writing to out
     */
    public static void printRecursive(Appendable out, int level, Map
            <? extends String,? extends Object> menu, boolean quote)
                                                            throws IOException {
        // Stores the keys for all of the entries in the menu item hashtable.
        // This is done (in conjunction with ordered_keys) to ensure that all of
        // the menu item's PCD entries are printed in the correct order.
        Set<String> keyset = new LinkedHashSet<String>(menu.keySet());

        // Print all of the known keys in the order required by the PCD parser.
        for (String key : ordered_keys) {
            if (keyset.contains(key)) {
                printKey(out, level, key, menu, quote);
                keyset.remove(key);
            }
        }

        // In case there are keys that this menu converter is unaware of, print
        // them, too; however, this loop is likely never executed.
        for (String key : keyset) {
            printKey(out, level, key, menu, quote);
	}
    }

    /**
     * Formats and prints an individual key within a map
     **
     * @param out    the Appendable object to print to.
     * @param level  the level of indentation to print the entry at.
     * @param key    the key within the map object to print.
     * @param menu   the map object to print from.
     * @param quote  whether or not to quote the entries.
     * @throws IOException any IOExceptions thrown by writing to out
     */
    public static void printKey(Appendable out, int level, String key, Map
            <? extends String,? extends Object> menu, boolean quote)
                                                            throws IOException {
        // Get the value in the hash object to print.
	Object value = menu.get(key);

        // If the value is not null, then print it.
        if (value != null) {
            if (value instanceof Map) {
                // If the value is a map object, then recursively print it.
                Map submap = (Map) value;

                if (!submap.isEmpty()) {
                    tabbedPrint(out, level, key);
                    System.out.println("**** recursing: " + key);
                    printRecursive(out, level + 1, submap,
                            "choices".equalsIgnoreCase(key));
                }
            } else if(value instanceof List) {
                // If the value is a list onbject, then recursively print it.
                List sublist = (List) value;

                if (!sublist.isEmpty()) {
                    tabbedPrint(out, level, key);
                    System.out.println("**** recursing: " + key);
                    for (Object listitem : sublist) {
                        tabbedPrint(out, level + 1, listitem);
                    }
                }
            } else {
                // If the value is NOT a map or list, then print it
                // (non-recursively) using quotations as necessary.
                if (quote || (!key.equals("type") && !"true".equals(value)
                            && !"false".equals(value) && !key.equals("min")
                            && !key.equals("max") && !key.equals("direction")
                            && (!key.equals("default")
                        || (!menu.containsKey("min")
                            && !menu.containsKey("max")
                            && !menu.containsKey("choices"))))) {
                    // PRINT DEBUG INFORMATION FOR "DEFAULT" CHOICE
                    // (THIS IS LIKELY DATED CODE; HOWEVER, BECAUSE THE GDE TO
                    // PCD CONVERTER IS NO LONGER BEING USED, THIS CODE IS KEPT
                    // AS-IS TO AVOID THE ADDITION OF BUGS (SO THAT ANYONE, WHO
                    // WANTS TO, CAN STILL USE THE GDE TO PCD CONVERTER).
                    if (key.equals("default")) {
                        System.out.println(!menu.containsKey("min") + "   "
                                + !menu.containsKey("max") + "   "
                                + !menu.containsKey("choices") + " --- "
                                + (!key.equals("default")
                                || (!menu.containsKey("min")
                                    && !menu.containsKey("max")
                                    && !menu.containsKey("choices"))));
                        for (String k : menu.keySet()) {
                            System.out.println("    " + k);
                        }
                    }
                    // Quote the value string.
                    value = quote(value.toString());
                }

                // If the quote flag is set, quote the key for the hash.  (This
                // is mainly useful for the 'choices' of list widget types)
                if (quote) {
                    key = quote(key.toString());
                }

                // Print the current key-value pair.
                tabbedPrint(out, level, key
                        + "             ".substring(0,
                            Math.max(1, 12 - key.length())) + value);
            }
        }
    }

    /**
     * Prints a string at a given indentation level.
     **
     * @param out    the appendable object to print to.
     * @param level  the level of indentation to print the string.
     * @param value  the string to print.
     * @throws IOException
     */
    public static void tabbedPrint(Appendable out, int level, Object value)
                                                            throws IOException {
        // Print the indentation to the Appendable object.
        for (int count = 0; count < level; count++) {
            out.append("    ");
        }

        // Print the string and the new-line character to the Appendable object.
	out.append(String.valueOf(value)).append("\n");
    }

    /**
     * Quotes a given string
     **
     * @param string the string to quote
     * @return the quoted string
     */
    private static String quote(String string) {
        // Quote a string by doubling all double-quotes characters in the string
        // and adding a double-quotes character to either end of the string.
        return "\"" + string.replace("\"", "\"\"") + "\"";
    }

    /**
     * Checks if a character array is all digits.
     **
     * @param test the character array to test.
     * @return true if the array only contains digits.
     */
    public static boolean testNumber(char[] test) {
        // Determine if the character array is a number.  This will only handle
        // integer numbers (either positive or negative) comprised of an
        // optional negative sign (at the start) and only digits (for the rest
        // of the string.  Blank strings will not match.
        return Pattern.matches("^-?\\d+$", new String(test));
    }
}
