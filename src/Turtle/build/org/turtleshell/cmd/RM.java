/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.turtleshell.cmd;

import java.io.File;
import java.io.Reader;
import org.turtleshell.TEnv;

/**
 * This command deletes files
 *
 * COMMAND LINE ARGUMENTS
 * ----------------------
 * This command accepts any number of command line arguments.  The command line
 * arguments should be formatted in the following manner:
 *
 * rm [flags] file(s)
 *
 * Flags:
 * 
 *  -r/-R   # delete recursively
 *
 * EXECUTION STATUS
 * ----------------
 * This command returns one (1) unless an exception is encountered.  In the case
 * of an exception, this command will return zero (0)
 **
 * @author Graham Alvare
 * @author Brian Fristensky
 */
public class RM {
    /**
     * The main calling method for this command
     **
     * @param args see class description for a list of available command line arguments
     * @return the execution status of this command (see class description)
     */
    public static int main(String[] args) {
        return exec(new TEnv(), args, null, null);
    }
    
    /**
     * The functional body of this command
     **
     * @param env the Turtle shell environment to read any necessary variable values from.
     * @param args see class description for a list of available command line arguments
     * @param output the standard output for this command
     * @param input the standard input for this command
     * @return the execution status of this command (see class description)
     */
    public static int exec (TEnv env, String[] args, Appendable output, Reader input) {
        boolean result = true;

        // branch based on whether the deletion is recursive
        if (args.length > 0 && "-r".equalsIgnoreCase(args[0])) {
            // proceed past the first argument (-R) and delete all paths recursively
            for (int count = 1; count < args.length; count++) {
                result &= deletePath(env.fixpath(args[count]));
            }
        } else {
            // delete all paths without recursion
            for (String path : args) {
                result &= env.fixpath(path).delete();
            }
        }
        return (result ? 1 : 0);
    }

    /**
     * Delete a path recursively
     **
     * @param remove the path to begin the deletion at
     * @return whether the deletion was successful
     */
    private static boolean deletePath(File remove) {
        if (remove.isDirectory()) {
            for (File subfile : remove.listFiles()) {
                deletePath(subfile);
            }
        }
        return remove.delete();
    }

}
