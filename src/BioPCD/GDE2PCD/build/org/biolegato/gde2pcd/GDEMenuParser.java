/*
 * GDEMenuParser.java
 *
 * Created on June 9, 2010, 1:28 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */
package org.biolegato.gde2pcd;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.Map;
import java.util.LinkedHashMap;
import java.util.List;

/**
 * This class parses GDE's menu file format into a hashed string representation,
 * which can be interpreted or written to output as PCD or any other menu file
 * format.
 **
 * @author Graham Alvare
 * @author Brian Fristensky
 */
public class GDEMenuParser {

    /**
     * Creates a new instance of GDEMenuParser.
     */
    public GDEMenuParser() {
    }

    /**
     * This function loads a GDE formatted menu file into the program.
     **
     * @param menu hash map to read the gde menu file contents into
     * @param file the file to read.
     */
    public static void readGDEMenuFile(Map<String, Map<String,
            Map<String, Object>>> menu, File file) {
        System.out.println("*********** READING FILE: "
                + file.getAbsolutePath() + " ***********");
        readGDEMenuFile(menu, "File", file, null, Collections.EMPTY_LIST);
    }

    /**
     * <p>This function loads a GDE formatted menu file, and converts it into a
     * PCD code hashmap.  The PCD hashmap will be at least 3 levels deep.  The
     * first hashmap level will contain the menu names as keys, and an ordered
     * hashmap of menu items for that menu as the value.  The second level
     * (the hashmap of menu items) has the menu item names as its keys, and an
     * ordered hashmap of the menu item's widgets as its values.  The third
     * level has the widget variable names (prefaced by 'var "' and ended by
     * '"') as its keys, and an ordered hashmap of thevariable/widget's fields
     * as its values.  There is a fourth level in the case of list widgets.
     * List widgets have a 'choices' field which contains a hashmap of choices
     * as its value.  The choices hashmap uses the keys as the name to display
     * to the user (for selection), and the values to use for command line
     * substitution as its values.  (NOTE: this was chosen to ensure that there
     * are no duplicates presented to the user, while allowing multiple options
     * to yield the same command line substitution -- which is not preferred,
     * but still quite acceptable.)</p>
     *
     * <p><i>NOTE: the first level refers to the key/value pair of the returned
     *       object, while the second level refers to the key/value pair of the
     *       hashmap stored as a value in the first level.</i></p>
     **
     * @param menuName the name of the current heading.
     * @param file the file to read.
     */
    public static Map<String, Object> readGDEMenuFile(Map<String,
            Map<String, Map<String, Object>>> menu, String menuName,
            File file, String filename, List<String> systems) {
        // Whether the widget we are creating is a list/choice widget.
        // This boolean is important for managing the PCD choice list for
        // the widget (to avoid improperly formatted PCD code - e.g. choice
        // widgets without choices, or non-choice widgets with choices).
        boolean choice_widget = false;
        // The current line in the file
        String line = "";
        // The current field name being parsed
        String fname = "";
        // The current field value
        String fvalue = "";
        // The current menu item's name
        String inName = null;
        // The current menu item's name
        String outName = null;
        // The current menu item's name
        String argName = null;
        // The current menu item's name
        String itemName = "";
        // The data for the current run button (used only on button creation)
        String runCommand = null;
        // The data for the current help button (used only on button creation)
        String helpCommand = null;
        // the BufferedReader for reading the GDE menu
        BufferedReader reader = null;
        // used for replacing GDE regular variable names with
        // BioLegato variable names
        Set<String> argList = new HashSet<String>();
        // used for replacing GDE file variable names with
        // BioLegato variable names
        Set<String> fileVars = new HashSet<String>();
        // the current menu item being processed
        Map<String, Object> currentItem = null;
        // Used for storing variable choices
        Map<String, String> choices = new LinkedHashMap<String, String>();
        // The data for the current input file
        Map<String, Object> infile  = new LinkedHashMap<String, Object>();
        // The data for the current output file
        Map<String, Object> outfile = new LinkedHashMap<String, Object>();
        // The data for the current argument
        Map<String, Object> arg = new LinkedHashMap<String, Object>();

        // Ensure the GDE menu file exists before trying to read it.
        if (file.exists() && file.isFile() && file.canRead()) {
            try {
                // Open the GDE menus file.
                reader = new BufferedReader(new FileReader(file));

                do {
                    // Read the next line in the file.
                    fname  = "";
                    fvalue = "";
                    line = reader.readLine();

                    // Skip comments and blank lines and ensure
                    // that there is a : on the current line.
                    if (line != null && !"".equals(line)
                            && line.indexOf(':') >= 0 &&
                            (line.startsWith("#@") || !line.startsWith("#"))) {
                        // remove biolegato specific parsing header
                        // (since we wish to parse these commands)
                        if (line.startsWith("#@")) {
                            line = line.substring(2);
                        }

                        // the first parameter is the field name
                        // the second parameter is the field data
                        fname  = line.substring(0,
                                    line.indexOf(':')).trim().toLowerCase();
                        fvalue = line.substring(line.indexOf(':') + 1).trim();
                    }

//////////////////////////////////////////////////
//**********************************************//
//* add any pending arguments to the menu item *//
//**********************************************//
//////////////////////////////////////////////////
                    if (currentItem != null) {
                        // Add the new infile if the 'item', 'arg', 'in',
                        // or 'menu' fields are read in, or we reach the end of
                        // the menu item file.
                        if ((line == null || "item".equals(fname)
                                || "arg".equals(fname) || "in".equals(fname)
                                || "menu".equals(fname)) && inName != null) {
                            // Ensure the infile has a proper variable type
                            // field, and direction field.
                            infile.put("type", "tempfile");
                            infile.put("direction", "in");

                            // Add the infile to the menu item.
                            currentItem.put("var \"" + inName + "\"", infile);
                            fileVars.add(inName);

                            // Set up all variables for the next infile.
                            infile = new LinkedHashMap<String, Object>();
                            inName = null;
                        }

                        // Add the new outfile if the 'item', 'arg', 'out',
                        // or 'menu' fields are read in, or we reach the end of
                        // the menu item file.
                        if ((line == null || "item".equals(fname)
                                || "arg".equals(fname) || "out".equals(fname)
                                || "menu".equals(fname)) && outName != null) {
                            // Ensure the outfile has a proper variable type
                            // field, and direction field.
                            outfile.put("type", "tempfile");
                            outfile.put("direction", "out");

                            // Add the outfile to the menu item.
                            currentItem.put("var \"" + outName + "\"", outfile);
                            fileVars.add(outName);

                            // Set up all variables for the next outfile.
                            outfile = new LinkedHashMap<String, Object>();
                            outName = null;
                        }

                        // Add the new menu item if the 'item', 'arg', or 'menu'
                        // fields are read in, or we reach the end of file.
                        if ((line == null || "item".equals(fname)
                                || "arg".equals(fname) || "menu".equals(fname))
                                && argName != null) {
                            // Add the choices list object to the widget, only
                            // if the widget is a list-type widget.
                            if (choice_widget) {
                                arg.put("choices", choices);
                            }

                            // This is necessary before doing any further
                            // processing.
                            if (argName != null && arg.containsKey("type")) {
                                currentItem.put("var \"" + argName + "\"",
                                        new LinkedHashMap<String, Object>(arg));
                                argList.add(argName.toLowerCase());
                            }

                            // Set up all variables for the next menu item.
                            choices = new LinkedHashMap<String, String>();
                            arg = new LinkedHashMap<String, Object>();
                            choice_widget = false;
                        }
                    }
//////////////////////////////
//**************************//
//* create a new menu item *//
//**************************//
//////////////////////////////
                    if (line == null || "item".equals(fname)) {
                        // If the current item is not null, then add the current
                        // menu item to the list of all menu items (i.e.
                        // finalize the PCD code).
                        if (currentItem != null) {
			    if (argList.isEmpty() && helpCommand == null
                                    && runCommand != null) {
                                // If the agrument list is empty and there is no
                                // help command, make the PCD menu item an exec-
                                // only command (i.e. a menu item which executes
                                // a command when clicked without prompting the
                                // user for any parameters).  This is done by
                                // setting the PCD exec parameter to the command
                                // the menu item will run when clicked.
				currentItem.put("exec", GDE2BLArgs(argList,
                                        fileVars, runCommand));

			    } else if (argList.isEmpty() && helpCommand != null
                                    && runCommand == null) {
                                // If the agrument list is empty and there is no
                                // run command, but there IS a help command,
                                // make the PCD menu item an exec-only command
                                // (i.e. a menu item which executes a command
                                // when clicked without prompting the user for
                                // any parameters).  This is done by setting
                                // the PCD exec parameter to the command the
                                // menu item will run when clicked.
                                //
                                // In this case, the exec command will be the
                                // help command instead of the run command.
				currentItem.put("exec", GDE2BLArgs(argList,
                                        fileVars, helpCommand));
			    } else {
                                // Otherwise (else), this will be a standard
                                // widget command, which presents a list of
                                // options to the user before running a command.

                                // Convert any instance of a GDE variable name,
                                // in the 'choices' fields of any list widget,
                                // to a valid PCD variable name.
				GDE2BLArgs(argList, fileVars, currentItem);

                                // Create a new panel to store the Run and Help
                                // buttons.  This step is required, so the Run
                                // and Help buttons appear side-by-side.
                                currentItem.put("panel",
                                        new LinkedHashMap<String,Object>());

                                // Create the run button for the menu item.
                                //
                                // TODO: make this run the command if there are
                                //       no other variables or buttons
				if (runCommand != null) {
				    ((Map)currentItem.get("panel")).put(
                                            "var \"Run\"",
                                            createGDEButton("Run",
                                                GDE2BLArgs(argList, fileVars,
                                                    runCommand), true));
				}

                                // Create the help button for the menu item.
				if (helpCommand != null) {
				    ((Map)currentItem.get("panel")).put(
                                            "var \"Help\"",
                                            createGDEButton("Help",
                                                GDE2BLArgs(argList, fileVars,
                                                    helpCommand), false));
				}
			    }
                        }

                        // Create the menu for adding the menu item to (if it
                        // does not already exist!)
                        if (!menu.containsKey(menuName)) {
                            menu.put(menuName, new LinkedHashMap<String,
                                    Map<String, Object>>());
                        }

                        // Set the name for the new menu item.
                        itemName = fvalue;

                        // If the line is not null (i.e. if we are not at the
                        // end of the GDE file), then create a new menu item.
                        if (line != null) {
                            // Create a new hashmap for the new menu item.
                            currentItem = new LinkedHashMap<String, Object>();

                            // Set the name and system fields of
                            // the new menu item.
                            currentItem.put("name", itemName);
                            if (systems != null && !systems.isEmpty()) {
                                currentItem.put("system", systems);
                            }

                            // Add the menu item to its parent menu.
                            if (filename == null) {
                                menu.get(menuName).put(itemName, currentItem);
                            } else {
                                menu.get(menuName).put(filename, currentItem);
                            }
                        }
                        
                        // Clear everything for the new menu item.
                        runCommand = null;
                        helpCommand = null;
                        fileVars.clear();
                        argList.clear();
                    }
                    // Only continue if the line is not blank or null.
                    else if (!"".equals(fname)) {
/////////////////////////////////
//*****************************//
//* create a new menu heading *//
//*****************************//
/////////////////////////////////
                        if ("menu".equals(fname)) {
                            // Set the name of the menu to generate menu items.
                            menuName = fvalue;
                        } else if (currentItem != null) {

////////////////////////////////////
//********************************//
//* do menu item data processing *//
//********************************//
////////////////////////////////////
// this is optional for improving the usability of the menu item
                            if ("itemmeta".equals(fname)
                                    && fvalue.length() >= 1) {
                                // Generate the mnemonic for the menu item
                                // this method allows only mnemonics of length 1
                                // (menu mnemonics in java are all of length 1,
                                // except complex ones involving
                                // shift/ctrl/etc.)
                                if ((fvalue.charAt(0) >= 'a'
                                        && fvalue.charAt(0) <= 'z')
                                        || (fvalue.charAt(0) >= 'A'
                                        && fvalue.charAt(0) <= 'Z')) {
                                    // CURRENTLY NOT ENABLED IN PCD!
                                    //currentItem.putValue(
                                    //  javax.swing.Action.MNEMONIC_KEY,
                                    //  new Integer(
                                    //  javax.swing.KeyStroke.getKeyStroke(
                                    //     fieldValue.charAt(0)).getKeyCode()));
                                }
                            } else if ("itemlabel".equals(fname)) {
                            } else if ("itemopen".equals(fname)) {
                            } else if ("itemhelp".equals(fname)) {
                                // Store the command to run when the help button
                                // is clicked for the program (also ensures that
                                // a help button is generated for the menu item.
                                if (!fvalue.trim().equals("")) {
                                    if (!fvalue.startsWith("$")
                                            && !fvalue.startsWith("/")) {
                                        fvalue = "$BIRCH/" + fvalue;
                                    }
                                    // Get the help command for the menu item.
                                    helpCommand = "$BIRCH/script"
                                            + "/gde_help_viewer.csh " + fvalue;
                                }

                            } else if ("itemmethod".equals(fname)) {
                                // Get the command corresponding to
                                // the menu item.
                                runCommand = fvalue;

                            }

////////////////////////////////////////
//************************************//
//* do argument parameter processing *//
//************************************//
////////////////////////////////////////
// this is used to allow for parameter passing
// for running commands
                            else if ("arg".equals(fname)) {
                                // Creates a new argument parameter.
                                argName = fvalue.toLowerCase();

                            } else if ("argtype".equals(fname)) {
                                // Sets the argument type of the widget.
				if ("choice_list".equalsIgnoreCase(fvalue)) {
				    fvalue = "list";
                                    choice_widget = true;
				} else if ("choice_menu".equalsIgnoreCase(fvalue)) {
				    fvalue = "combobox";
                                    choice_widget = true;
				} else if ("chooser".equalsIgnoreCase(fvalue)) {
				    fvalue = "chooser";
                                    choice_widget = true;
				} else if ("file_chooser".equalsIgnoreCase(fvalue)) {
				    fvalue = "file";
				} else if ("slider".equalsIgnoreCase(fvalue)) {
				    fvalue = "number";
				}
                                arg.put("type", fvalue);

                            } else if ("arglabel".equals(fname)) {
                                // Changes the label of the command.
                                // The label is the name of the
                                // argument presented to the user.
                                arg.put("label", fvalue);

                            } else if ("argmin".equals(fname)) {
                                // Changes the minimum value for the argument.
                                // (This is applicable only to number choosers.)
                                try {
                                    arg.put("min", Integer.parseInt(fvalue));
                                } catch (NumberFormatException nfe) {
                                    nfe.printStackTrace(System.err);
                                }

                            } else if ("argmax".equals(fname)) {
                                // Changes the maximum value for the argument.
                                // (This is applicable only to number choosers.)
                                try {
                                    arg.put("max", Integer.parseInt(fvalue));
                                } catch (NumberFormatException nfe) {
                                    nfe.printStackTrace(System.err);
                                }

                            } else if ("argvalue".equals(fname)
                                    || "argtext".equals(fname)) {
                                // Changes the default value for the argument.
                                arg.put("default", fvalue);

                            } else if ("argchoice".equals(fname)) {
                                // Ensure that there is at least one more ':'
                                // character in the line.  In GDE, argument
                                // choices are specified by three fields,
                                // separated by colons.  The first field is
                                // 'argchoice' to indicate that what comes next
                                // is a choice.  The second field is the name
                                // of the choice (what to present to the user.
                                // The third field is the value of the choice
                                // to use for command line substitution (if
                                // the choice is selected by the user).
                                if (fvalue.indexOf(':') >= 0) {
                                    // Adds a new choice for the argument this
                                    // is applicable only to selection widgets.
                                    choices.put(fvalue.substring(0,
                                            fvalue.indexOf(':')),
                                            fvalue.substring(fvalue.indexOf(':')
                                                + 1));
                                } else {
                                    // Add a blank choice to the list, and tell
                                    // the user that the GDE file contains an
                                    // improperly formed argchoice entry.
                                    choices.put(fvalue, "");
                                    System.err.println("Badly formed "
                                            + "argchoice field: " + fvalue);
                                }
                            }
///////////////////////////////////
//*******************************//
//* do I/O parameter processing *//
//*******************************//
///////////////////////////////////
                            //***********************
                            //* Handle Input fields *
                            //***********************
                            else if ("in".equals(fname)) {
                                // Get the name of the new input file variable.
                                inName = fvalue.toLowerCase();
                            } else if ("informat".equals(fname)) {
                                // Set the format of the input file variable.
                                //
                                // The valid GDE formats are:
                                //
                                //          flat, GDE, and genbank.
                                //
                                // Note that GDE flatfiles are very similar to
                                // FastA, the main exception being that the
                                // first character of a sequence name ('>' in
                                // FastA) is either the character: #, %, @, "
                                //
                                //  # inidicates the file contains DNA/RNA.
                                //  % inidicates the file contains Protein.
                                //  @ inidicates the file contains colour masks.
                                //  " inidicates the file contains Text.
                                //
                                infile.put("format", fvalue);

                            } else if ("inmask".equals(fname)) {
                                // Warn the user that this feature is
                                // unsupported (I could not find proper
                                // documentation of what this does, and even
                                // after examining the source code, it seems as
                                // though this feature does not work properly
                                // in GDE anyways).
                                System.err.println("Warning unsupported GDE "
                                        + "menu field \"inmask\" -- ignoring");

                            } else if ("insave".equals(fname)) {
                                // Set the save of the output file variable.
                                // If this flag is set, the file will not be
                                // deleted after the GDE command is executed.
                                infile.put("save", "true");
                            }

                            //************************
                            //* Handle Output fields *
                            //************************
                            else if ("out".equals(fname)) {
                                // Get the name of the new output file variable.
                                outName = fvalue.toLowerCase();

                            } else if ("outformat".equals(fname)) {
                                // Set the format of the output file variable.
                                //
                                // The valid GDE formats are:
                                //
                                //          flat, GDE, and genbank.
                                //
                                // Note that GDE flatfiles are very similar to
                                // FastA, the main exception being that the
                                // first character of a sequence name ('>' in
                                // FastA) is either the character: #, %, @, "
                                //
                                //  # inidicates the file contains DNA/RNA.
                                //  % inidicates the file contains Protein.
                                //  @ inidicates the file contains colour masks.
                                //  " inidicates the file contains Text.
                                //
                                outfile.put("format", fvalue);

                            } else if ("outsave".equals(fname)) {
                                // Set the save of the output file variable.
                                // If this flag is set, the file will not be
                                // deleted after the GDE command is executed.
                                outfile.put("save", "true");

                            } else if ("outoverwrite".equals(fname)) {
                                // Set the overwrite flag in the file.  If this
                                // flag is set, the file will overwrite whatever
                                // is selected in the canvas with the output
                                // from the program (when the program is done
                                // executing).
                                outfile.put("overwrite", "true");

                            } else {
                                // If the GDE field is not recognized, print an
                                // error message to stderr.
                                System.err.println("Invalid GDE field (item: "
                                        + itemName + "): "
                                        + fname + " = " + fvalue);
                            }
                        } else {
                            // If a GDE field cannot be parsed, print an error
                            // message to stderr alerting the user to the
                            // possibility that the file might NOT be a valid
                            // GDE file (or that the file just contains errors,
                            // or that there is a bug or unsupported GDE field
                            // in this program).
                            System.err.println("Invalid GDE location or field"
                                    + " value (item: " + itemName + "): "
                                    + fname + " = " + fvalue);
                        }
                    }
                } while (line != null);

                // Close the file.
                reader.close();
            } catch (Throwable e) {
                e.printStackTrace(System.err);
            }
        }
	return currentItem;
    }

    /**
     * This method fills in more information about the buttons in GDE.
     * GDE stores only two buttons - run and help - which are stored only as
     * commands.  To convert these buttons into PCD, we need to fill in some
     * blanks, such as whether or not to close the dialog box for program
     * parameters.
     **
     * @param label        the text to display for the button - Run or Help
     * @param command      the command to execute by clicking on the button
     * @param closeWindow  whether or not the program parameters dialog box
     *                     should be closed by clicking on the button (true for
     *                     run, false for help)
     * @return the text hash-table representation of the button (i.e. an
     *         internal format to the menu file converter)
     */
    public static Map<String, Object> createGDEButton(String label,
            String command, boolean closeWindow) {
        // Create a new ordered hash map to store all of the
        // PCD code for the new command button.
        Map<String, Object> button = new LinkedHashMap<String, Object>();

        // Add the PCD fields to the new button.
        button.put("type", "button");
        button.put("label", label);
        button.put("shell", command);

        // Add the appropriate PCD code, which determines whether the
        // command button will close the run window when clicked, or not.
        if (closeWindow) {
            button.put("close", "true");
        } else {
            button.put("close", "false");
        }

        // Return the generated run button's PCD code hashmap.
        return button;
    }

    /**
     * Converts GDE-style variables in a command string to BioLegato/PCD-style
     * variables.
     **
     * GDE stores variables in one of two methods: $VARIABLE for arguments, and
     * VARIABLE for files (where VARIABLE represents the name of the variable).
     * In contrast, PCD stores variables as %VARIABLE%, regardless of whether
     * the variables are files or arguments.  This method converts all of the
     * GDE-style variables within a command string into PCD-style
     **
     * @param argList     the list of arguments (args) used within the current
     *                    GDE menu item.
     * @param fileVars    the list of file variables used within the current
     *                    GDE menu item.
     * @param runCommand  the current command string to parse and convert
     *                    the variable styles (GDE-style).
     * @return the converted command string (PCD-style)
     */
    private static String GDE2BLArgs(Set<String> argList, Set<String> fileVars,
                                     String runCommand) {
        // The start position of the argument/variable name to process.
        // (This is the position of the $ character of the variable name.)
        int start = -1;
        // The end position of the argument/variable name to process.  (This
        // is one index position past the last character of the variable name.)
        int end = -1;
        // The array of valid GDE argument/variable names.  This is used because
        // both UNIX and GDE uses $ to precede their variable names; however,
        // PCD uses % characters (which differs from UNIX).  To avoid losing
        // UNIX variables in the conversion (due to mistaking them for GDE
        // variables), we shall check this list to ensure that we only convert
        // GDE variables to PCD, and leave the UNIX variables alone.
        String[] argArray = argList.toArray(new String[0]);
        // The current variable name matched by the parser.  This will be tested
        // to determine whether the name is a GDE variable or a UNIX variable.
        String test = null;

        // Sort the array of GDE arguments (this is to allow binary searches).
        // While binary search is O(log n), we could have used a hashset
        // directly, which is O(1).  While there must be a reason for this
        // decision, this code is quite old, and so changing it is not
        // recommended unless there is further need to use the GDE to PCD
        // converter on any large scale (i.e. a lot of files to test to make
        // sure that any changes do not introduce major bugs).
        Arrays.sort(argArray);

        // Create the command string.
        // NOTE: variable.toString().replaceAll(Pattern.quote("$"), "\\\\\\$")
        //          is used to prevent regex grouping (e.g. $0, etc.)).
        while ((start = runCommand.indexOf('$', start)) >= 0) {
            // Ensure that the end character position is at least one character
            // past the start position.
            end = start + 1;

            // Keep growing the variable name (by incrementing end character
            // position) UNTIL we either reach the end of the string, OR a
            // non-letter, non-digit character other than '_'.
            while (end < runCommand.length()
                    && (Character.isLetterOrDigit(runCommand.charAt(end))
                        || runCommand.charAt(end) == '_')) {
                end++;
            }

            // Extract the variable name to test (if it is a GDE variable name).
            test = runCommand.substring(start + 1, end).toLowerCase();

            // Search the GDE varaible name list for the variable name.  If the
            // variable name is found in the GDE variable name list (argArray),
            // then replace the variable name with the PCD code equivalent.
            if (Arrays.binarySearch(argArray, test) >= 0) {
                runCommand = (start == 0 ? "" : runCommand.substring(0, start))
                        + '%' + runCommand.substring(start + 1, end) + '%'
                        + (end > runCommand.length()
                            ? "" : runCommand.substring(end));
            }
            
            // Ensure that the start character position is incemented to avoid
            // an infinite loop (i.e. repetitively matching the '$' character at
            // the same position -- this would happen if we do not increment the
            // start position and the variable is a non-GDE variable.)
            start ++;
        }

        // DEBUG PRINT MESSAGE (MAY BE REMOVED IN THE FUTURE!)
        System.out.println("STARTING FILE VARIABLES");

        // Iterate through and replace all of the GDE file variables (these are
        // replaced by exact matches, the same way GDE replaces file variable
        // names).  NOTE: these variable names are NOT preceded by '$'!
        for (String file : fileVars) {
            runCommand = runCommand.replaceAll("((?i:\\Q" + file + "\\E))",
                    "%" + file + "%");
            // DEBUG PRINT MESSAGE (MAY BE REMOVED IN THE FUTURE!)
            System.out.println("MATCHING - " + file + "-" + runCommand);
        }

        // DEBUG PRINT MESSAGE (MAY BE REMOVED IN THE FUTURE!)
        System.out.println("DONE FILE VARIABLES");

        // Return the converted GDE command.
        return runCommand;
    }

    /**
     * Converts GDE-style variables in lists to BioLegato/PCD-style variables.
     **
     * GDE stores variables in one of two methods: $VARIABLE for arguments, and
     * VARIABLE for files (where VARIABLE represents the name of the variable).
     * In contrast, PCD stores variables as %VARIABLE%, regardless of whether
     * the variables are files or arguments.  This method converts all of the
     * GDE-style variables within lists into PCD-style.
     **
     * This specific version of the method replaces all of the GDE variable
     * names in a list widget's choices with PCD-style variable names.
     **
     * @param argList   the list of arguments (args) used within the current
     *                  GDE menu item
     * @param fileVars  the list of file variables used within the current
     *                  GDE menu item
     * @param map       the hash map to convert all of the list values within to
     *                  PCD-style
     */
    private static void GDE2BLArgs(Set<String> argList, Set<String> fileVars,
            Map<String, Object> map) {
        // A map object containing the arguments to replace.
        Map<String, Object> arg;

        // Iterate through every entry in the GDE argument variable name list.
        for (String akey : argList) {
            // Extract the variable's data from the complete map of all
            // variables for the GDE/PCD menu item.
            arg = (Map<String, Object>) map.get("var \"" + akey + "\"");

            // If the variable is a list widget (i.e. contains a "choices"
            // parameter), then replace all of the GDE variable names in
            // each choice with PCD variable names.
            if (arg.containsKey("choices")) {
                // Iterate through choice option in the list widget variable's
                // choices hashmap.
                for (Map.Entry<String, String> e :
                        ((Map<String, String>) arg.get("choices")).entrySet()) {
                    // Replace every instance of a GDE variable name in the
                    // choices hashmap with its equivalent PCD variable name.
                    ((Map<String, String>) arg.get("choices")).put(e.getKey(),
                            GDE2BLArgs(argList, fileVars, e.getValue()));
                }
            }
        }
    }
}
