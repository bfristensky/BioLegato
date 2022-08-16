package org.biolegato.gde2pcd;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.File;
import java.util.Collections;
import java.util.LinkedList;
import java.util.Map;
import java.util.LinkedHashMap;
import java.util.List;

/**
 * GDEMenu is used to read and parse GDE menuFile files into into BioLegato
 **
 * @author Graham Alvare
 * @author Brian Fristensky
 */
public class GDEMakeMenus {

    /**
     * Reads the makemenus.py BIRCH GDE menus.
     **
     * @param basedir    the directory to read the GDE menus from.
     * @param menu       the hashtable to store the menus in.
     * @param readLocal  whether to read ldir.param and local GDE menus.
     */
    public static void loadMenu(String basedir, Map<String, Map<String,
                            Map<String, Object>>> menu, boolean readLocal) {
        // The current line of the file being read.
        String line;
        // The file object of the makemenus file to read.
        File mfile     = null;
        // The current directory being processed.
        String dirname = "";
        // The menulist file for a specific GDE menu directory.
        File menulist  = null;
        LinkedList<String> menulistArray = new LinkedList<String>();
        LinkedHashMap<String, LinkedHashMap<String, SystemLimitedMenu>> menuhash
                = new LinkedHashMap<String, LinkedHashMap<String,
                                                          SystemLimitedMenu>>();

        // The BufferedReader for reading the GDE menu files.
        BufferedReader reader = null;

        // Add the menus directory to the list of directories to read GDE menus
        // from.
        menulistArray.add(basedir + File.separator + "menus");

        // Read the local GDE menus.
        if (readLocal) {
            // A file pointer to ldir.param.  ldir.param is a pointer to the
            // local menus for the BIRCH installation.  Local menus are supposed
            // to be system specific, and hence they will not change with BIRCH
            // upgrades.  Thus, content from local menus will always overwrite
            // the content from non-local menus (without overwriting the files,
            // of course).
            File ldirparam = new File(basedir + File.separator + "ldir.param");

            if (ldirparam.exists() && ldirparam.canRead()
                    && ldirparam.isFile()) {
                try {
                    BufferedReader localreader = new BufferedReader(
                            new FileReader(ldirparam));
                    // Add all of the paths specified in ldir.param to the
                    // menulist search path list
                    while ((line = localreader.readLine()) != null) {
                        menulistArray.add(line);
                    }
                    localreader.close();
                    localreader = null;
                } catch (Throwable e) {
                    e.printStackTrace(System.err);
                }
            }
        }

        // Read all of the menulist files in the menulist search path list.
        for (String currentFile : menulistArray) {
            menulist = new File(currentFile + File.separator + "menulist");
            if (menulist.exists() && menulist.canRead() && menulist.isFile()) {
                try {
                    reader = new BufferedReader(new FileReader(menulist));

                    while ((line = reader.readLine()) != null) {
                        if (line.length() > 0 && !line.trim().equals("")) {
                            if (line.startsWith("#")) {
                                // Skip any comment lines (i.e. lines beginning
                                // with the '#' character).
                            } else if (Character.isWhitespace(line.charAt(0))) {
                                // If the line begins with a space, interpret
                                // the line as being a menu item (or .item) file
                                // within a menu directory.  (Note, the menu
                                // directory will be speicified on a line
                                // without any preceding whitespace.)

                                // The index of the tab character (for string
                                // replacements.
                                final int tidx = line.indexOf('\t');

                                // The system flags for the current menu item.
                                char[] sys = null;

                                // Trim the whitespace off of the menu item
                                // line (within the menulist file).
                                line = line.trim();

                                // Read the menu item data (i.e. system flags
                                // and menu item filename).
                                if (line.indexOf('\t') >= 0) {
                                    sys  = line.substring(tidx + 1
                                            ).trim().toCharArray();
                                    line = line.substring(0, tidx);
                                }

                                // Create a File object for the menu item.
                                mfile = new File(currentFile
                                        + File.separatorChar + dirname
                                        + File.separatorChar + line + ".item");

                                // If the menu item file exists, is a file, and
                                // can be read, then add it's File object
                                // (menufile) to the menu hashtable.  This hash-
                                // table will be used later to read in the menu
                                // item files.  The hashtable design allows
                                // regular menu items (within the hashtable) to
                                // be overwritten with local menu items.
                                if (mfile.exists() && mfile.isFile()
                                        && mfile.canRead()) {
                                    menuhash.get(dirname).put(line,
                                            new SystemLimitedMenu(sys, mfile));
                                } else {
                                    System.err.println("Cannot read menu item: "
                                            + mfile.getAbsolutePath() + "   "
                                            + mfile.exists() + "   "
                                            + mfile.isFile() + "   "
                                            + mfile.canRead());
                                }
                            } else {
                                // Set the current directory/menu name to the
                                // value of the current line in the 'menulist'
                                // file.  (This is done for all lines without
                                // preceding whitespace).
                                dirname = line;

                                // Add the directory name to the menu hashtable.
                                // This directory name will correspond to a menu
                                // within the PCD menu structure.
                                if (!menuhash.containsKey(dirname)) {
                                    menuhash.put(dirname, new LinkedHashMap
                                            <String, SystemLimitedMenu>());
                                }
                            }
                        }
                    }
                    // Close the reader object (for the menulist file).
                    reader.close();
                } catch (Throwable e) {
                    e.printStackTrace(System.err);
                }
            }
        }

        // Read and parse all of the GDE menu item files in the menu hashtable.
        for (Map.Entry<String, LinkedHashMap<String, SystemLimitedMenu>> mdir
                : menuhash.entrySet()) {
            for (Map.Entry<String, SystemLimitedMenu> file
                    : mdir.getValue().entrySet()) {
                GDEMenuParser.readGDEMenuFile(menu, mdir.getKey(),
                        file.getValue().getFile(), file.getKey(),
                        file.getValue().getSystems());
            }
        }
    }

    /**
     * Wrapper class to store system restrictions on GDE menu files.
     **
     * @author Graham Alvare
     * @author Brian Fristensky
     */
    private static class SystemLimitedMenu {
        /**
         * A list of the systems supported by the menu item.
         * This is a list of PCD system lines to add to the PCD output file.
         */
        private List<String> systems = Collections.emptyList();
        /**
         * The File object containing the menu item's GDE code.
         */
        private File menufile;

        /**
         * Creates a new menu item wrapper object.
         **
         * @param system    the makemenus system flags for the menu item.
         * @param menufile  the menu item's File pointer object.
         */
        public SystemLimitedMenu(char[] system, File menufile) {
            // Copy the menu item's file object to the class.
            this.menufile = menufile;

            // Convert the GDE makemenus system flags (if present) to PCD code.
            if (system != null && system.length > 0) {
                // Boolean for determining whether or not the menu item is
                // supported on Linux (either 32-bit or 64-bit)
                boolean linux         = false;
                // Boolean for determining whether or not the menu item is
                // supported on 32-bit Linux
                boolean linux_32      = false;
                // Boolean for determining whether or not the menu item is
                // supported on 64-bit Linux
                boolean linux_64      = false;
                // Boolean for determining whether or not the menu item is
                // supported on Solaris (either x86 or SPARC based machines)
                boolean solaris       = false;
                // Boolean for determining whether or not the menu item is
                // supported on SPARC machines running Solaris
                boolean solaris_sparc = false;
                // Boolean for determining whether or not the menu item is
                // supported on Intel x86-based  machines Solaris
                boolean solaris_intel = false;

                // Set-up the linked list to store the PCD lines for each system
                // supported by the GDE menu.
                systems = new LinkedList<String>();

                // Iterate through, and process, every system flag for
                // the GDE menu item.
                for (char av : system) {
                    switch (av) {
                        case 'S':   // solaris-sparc
                            solaris_sparc = true;
                            solaris = true;
                            break;
                        case 's':   // solaris-amd
                            solaris_intel = true;
                            solaris = true;
                            break;
                        case 'L':   // linux-intel
                            linux_32 = true;
                            linux = true;
                            break;
                        case 'l':   // linux-x86_64
                            linux_64 = true;
                            linux = true;
                            break;
                    }
                }

                // If the GDE menu is supported on Linux, generate the PCD code
                // for the menu to support Linux.
                if (linux) {
                    String linuxStr = "linux    ";
                    if (linux_32) {
                        linuxStr += "x86";
                        if (linux_64) {
                            linuxStr += ",amd64";
                        }
                    } else {
                        linuxStr += "amd64";
                    }
                    systems.add(linuxStr);
                }

                // If the GDE menu is supported on Solaris, generate the PCD
                // code for the menu to support Solaris.
                if (solaris) {
                    String solarisStr = "solaris    ";
                    if (solaris_sparc) {
                        solarisStr += "sparc";
                        if (solaris_intel) {
                            solarisStr += ",x86";
                        }
                    } else {
                        solarisStr += "x86";
                    }
                    systems.add(solarisStr);
                }
            }
        }

        /**
         * Return the File object for the menu item.
         **
         * @return the File object for the menu item.
         */
        public File getFile() {
            return menufile;
        }

        /**
         * Return the list of supported systems (as PCD code) for the menu item.
         **
         * @return the list of supported systems for the menu item.
         */
        public List<String> getSystems() {
            return systems;
        }
    }
}
