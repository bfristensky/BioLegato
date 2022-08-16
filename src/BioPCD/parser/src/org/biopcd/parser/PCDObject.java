/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.biopcd.parser;

import java.io.File;
import java.util.Map;
import java.util.Set;
import org.biopcd.widgets.Widget;
import org.biopcd.parser.PCD;

/**
 * Objects representing an entire PCD menu.
 * These objects are used to pass PCD parsed data to BLMain,
 * so the JMenuItem objects can be created for the data read in.
 **
 * @author Graham Alvare
 * @author Brian Fristensky
 */
public class PCDObject {
    /**
     * The parent directory that the menu item was read from (optional)
     */
    public File  dir = null;
    /**
     * The menu item name for the current PCD file program
     */
    public String  name;
    /**
     * If the exec parameter in a PCD file is set, this variable will
     * also be set.  This variable is used to run commands which do not
     * have any associated display widgets.  If this is variable is not
     * null, the command will run once the menu button is pressed.  The
     * command used for running will be stored in this variable.
     */
    public String  exec;
    /**
     * The menu item icon for the current PCD file program
     */
    public String  icon;
    /**
     * The menu item tooltip text for the current PCD file program
     */
    public String  tooltip;
    /**
     * Stores which systems the current PCD file is supported on
     */
    public Set<SystemToken> systems;
    /**
     * The widget list to use for running the current PCD command.
     */
    public Map<String, Widget> widgetList;

    /**
     * Creates a new PCD object.
     **
     * @param dir        the directory where the PCD menu was loaded.  This is
     *                   important for relative icon paths.
     * @param name       the name of the PCD menu
     * @param exec       the command for the PCD menu to execute when clicked
     *                   (without presenting widgets to the user), set to null
     *                   (default) if you want to use widgets and command
     *                   button instead
     * @param icon       the icon for the PCD menu command
     * @param tooltip    the tooltip text to present to the user for this
     *                   PCD command
     * @param systems    a set object containing all of the
     *                   system-architectures the PCD menu will work with
     * @param widgetList a map object containing all of the PCD widgets
     *                   associated with this menu item
     */
    public PCDObject (File dir, String name, String exec, String icon,
                      String tooltip, Set<SystemToken> systems,
                      Map<String, Widget> widgetList) {
        this.dir  = dir;
        this.name = name;
        this.exec = exec;
        this.icon = icon;
        this.tooltip = tooltip;
        this.systems = systems;
        this.widgetList = widgetList;
    }

    /**
     * Calculates whether this PCD menu is supported
     * on the current system-architecture configuration.
     **
     * @return true if the PCD menu is supported on the current system.
     */
    public boolean isSystemSupported() {
        /* Stores whether the current PCD file is supported on the
         * current operating system. */
        boolean systemSupported = (systems.size() <= 0);

        // Iterate through each operating system supported by the PCD menu.
        for (SystemToken sys : systems) {
            /* Stores the status of whether the current operating system is
             * supported by the software represented in the PCD file */
            boolean osSupported = false;
            /* Stores the status of whether the current machine architecture is
             * supported by the software represented in the PCD file */
            boolean archSupported = sys.archs.size() <= 0;

            /* match each operating system token and determine whether or not
             * the operating system matches the current OS */
            switch (sys.os) {
                case ALL:
                    osSupported = true;
                    break;
                case LINUX:
                    osSupported = PCD.CURRENT_OS == PCD.OS.LINUX;
                    break;
                case OSX:
                    osSupported = PCD.CURRENT_OS == PCD.OS.OSX;
                    break;
                case SOLARIS:
                    osSupported = PCD.CURRENT_OS == PCD.OS.SOLARIS;
                    break;
                case UNIX:
                    osSupported = PCD.CURRENT_OS.isUNIX();
                    break;
                case WINDOWS:
                    osSupported = PCD.CURRENT_OS.isWindows();
                    break;
                default:
                    osSupported = false;
                    break;
            }

            // if the current operating system is supported, check whether the
            // architecture is also supported (this is especially important for
            // operating systems like Linux and Solaris, which can run on
            // multiple incompatible architectures -- e.g. Solaris on SPARC vs.
            // Solaris on AMD64, or Linux PowerPC vs. Linux ARM vs. Linux x86).
            if (osSupported) {
                for (SystemToken.ARCH arch : sys.archs) {
                    /* match each architecture token and determine
                     * whether or not the architecture token matches
                     * the current system architecture */
                    switch (arch) {
                        case ALL:
                            archSupported = true;
                            break;
                        case X86:
                            archSupported = archSupported
                                || PCD.CURRENT_ARCH == PCD.ARCH.X86
                                || PCD.CURRENT_ARCH == PCD.ARCH.AMD64;
                            break;
                        case AMD64:
                            archSupported = archSupported
                                || PCD.CURRENT_ARCH == PCD.ARCH.AMD64;
                            break;
                        case SPARC:
                            archSupported = archSupported
                                || PCD.CURRENT_ARCH == PCD.ARCH.SPARC;
                            break;
                    }
                }
            }
            /* add the results of the current operating system support
             * test to the final result of whether the current machine
             * can run the PCD file */
            systemSupported = systemSupported
                    || ( archSupported && osSupported );
        }
        return systemSupported;
    }
}
