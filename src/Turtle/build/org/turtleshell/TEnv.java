/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.turtleshell;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * The local environment hash map.
 * This is used for storing variables used locally in turtle shell.
 * Whenever you perform a set or retrieval of variables, this hash is
 * used as opposed to the underlying system's environment hash; however,
 * if a variable is referenced that is not in the local environment hash,
 * Turtle shell WILL use the value in the underlying operating system's
 * environment, if available.
 **
 * @author Graham Alvare
 * @author Brian Fristensky
 */
public class TEnv extends HashMap<String, String> {
    /**
     * The escape character for Turtle shell
     */
    public static final char BACKSLASH = '\\';
    /**
     * This constant is used to improve the legibility of the code
     * which figures out the PROGRAM_DIR properties.  DO NOT OTHERWISE
     * USE THIS CONSTANT, AS IT MAY DISAPPEAR IN FUTURE VERSIONS!
     */
    private static final File EXE_DIR_DONT_USE = new File(
        TEnv.class.getProtectionDomain().getCodeSource()
            .getLocation().getPath());
    /**
     * This constant is set to the path of turtle.jar
     * The value of this constant determined at runtime.
     */
    public static final String PROGRAM_DIR =
            ( EXE_DIR_DONT_USE.isDirectory()
            ? EXE_DIR_DONT_USE.getPath()
            : EXE_DIR_DONT_USE.getParentFile().getPath() );

    /**
     * Creates a new "empty" local turtle shell environment
     */
    public TEnv () {
        super(System.getenv());

        put("PWD",  PROGRAM_DIR);
        put("HOME", System.getProperty("user.home"));
    }

    /**
     * Creates a new local turtle shell environment
     **
     * @param importMap the map of values to import into the current environment
     */
    public TEnv (Map<String,String> importMap) {
        this();
        putAll(importMap);
    }

    /**
     * Fixes paths, by making them relative to the current directory,
     * when necessary.  The necessity of this comes from situations where
     * ~ and . are used, or a file name is specified without a directory.
     * This method fixed all of these paths to the current working
     * directory which is manipulated within turtleshell via $PWD, cd,
     * and pwd.
     **
     * IF YOU WANT TO CREATE OR UPDATE *ANY* COMMANDS,
     * USE THIS WHENEVER YOU ARE ACCESSING PATHS IN THE
     * FILESYSEM (THIS WILL CREATE FILE OBJECTS FOR YOU!)
     **
     * @param change the string to modify.
     * @return the modified string.
     */
    public File fixpath(String change) {
        if (change.contains("~/")) {
            change.replaceAll("~/", System.getProperty("user.home") + File.separator);
        } else if (change.trim().equals("~")) {
            change = System.getProperty("user.home");
        }
        change.replaceAll("/", File.separator);
        return new File(get("PWD"), change);
    }

    /**
     * Returns the local turtle shell environment as an envp string for Java's
     * Runtime.exec() method.
     **
     * @return the envp string array for Runtime.exec()
     */
    public String[] getenvp () {
	Set<Map.Entry<String, String>> entries = entrySet();
        String[] envp = new String[entries.size()];
        int count = 0;

        for (Map.Entry<String, String> entry : entries) {
            // EMERGENCY BREAK! (should NEVER be executed)
            if (count >= envp.length) {
                break;
            }
            envp[count] = entry.getKey() + "=" + entry.getValue();
            count++;
        }

        return envp;
    }

    /**
     * Replaces $VARIABLE name with environment variables.
     * This function provides functionality similar to BASH.
     *
     * NOTE: if the variable is $BL_HOME, and this is not set,
     *	the variable will default to the current working directory.
     **
     * @param change the string to modify.
     * @return the modified string.
     */
    public String envreplace(String change) {
        int start = 0;
        int end = -1;
        String replace = null;
        String variable = "";

        if (change != null) {
            // replace all of the $ variables with their corresponding values
            // the while loop matches each $ character start site in the string
            while ((start = change.indexOf('$', start)) >= start
                    && start > -1) {
                // ensure that the start position is not preceded by
                // a '\' (escape) character
                if (start == 0 || change.charAt(start - 1) != BACKSLASH) {
                    // the for loop looks for the end of the variable name
                    for (end = start + 1; end < change.length()
                            && (change.charAt(end) == '_'
                            || Character.isLetterOrDigit(change.charAt(end)));
                        end++) {
                        /* ITTERATE - skip characters until we find the
                         *            end of the variable name */
                    }

                    // skip empty $'s
                    if (start + 1 < end) {
                        // get the information to perform the string replacement.
                        variable = change.substring(start + 1, end);
                        replace = "";

                        // if the local variable is set, retrieve its contents
                        if (containsKey(variable)) {
                            replace = get(variable);
                        }

                        // perform the string replacement.
                        if (replace != null) {
                            change = change.substring(0, start) + replace
                                        + change.substring(end);
                        } else {
                            start++;
                        }
                    } else {
                        start++;
                    }
                }
            }
        }
        return change;
    }
}
