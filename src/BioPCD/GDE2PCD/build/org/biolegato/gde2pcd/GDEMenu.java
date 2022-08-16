package org.biolegato.gde2pcd;

import java.io.File;
import java.util.Map;

/**
 * GDEMenu is used to read and parse GDE menu files into into BioLegato.
 **
 * @author Graham Alvare
 * @author Brian Fristensky
 */
public class GDEMenu {

    /**
     * Reads in the default .GDEMenus files.
     **
     * @param menu the menu hash to read the GDE menus into.
     */
    public static void loadMenu(Map<String,
            Map<String, Map<String, Object>>> menu) {
        try {
            // Read the menus from the GDE directory.
            if (System.getenv("GDE_HELP_DIR") != null) {
                GDEMenuParser.readGDEMenuFile(menu,
                        new File(System.getenv("GDE_HELP_DIR")
                            + File.separator + ".GDEmenus"));
            } else {
                System.err.println("GDE_HELP_DIR not set!");
            }

            // Read any menus from the current working directory.
            GDEMenuParser.readGDEMenuFile(menu, new File(GDE2PCD.PROGRAM_DIR
                    + File.separator + ".GDEmenus"));

            // Read any menus from the home directory (if they exist).
            if (GDE2PCD.HOME_DIR != null) {
                GDEMenuParser.readGDEMenuFile(menu, new File(GDE2PCD.HOME_DIR
                        + File.separator + ".GDEmenus"));
            } else {
                System.err.println("HOME not set!");
            }
        } catch (Throwable e) {
            e.printStackTrace(System.err);
        }
    }

}
