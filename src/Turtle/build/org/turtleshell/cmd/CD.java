/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.turtleshell.cmd;

import java.io.File;
import java.io.IOException;
import java.io.Reader;
import org.turtleshell.TEnv;

/**
 * This command changes the current working directory
 *
 * COMMAND LINE ARGUMENTS
 * ----------------------
 * This command accepts one command line argument and changes the current working
 * directory to the directory represented by this one command line parameter
 * (this is the same as UNIX, DOS, and Windows' CD commands w/o any frills)
 *
 * EXECUTION STATUS
 * ----------------
 * This command returns one (1) unless an exception is encountered.  In the case
 * of an exception, this command will return zero (0)
 **
 * @author Graham Alvare
 * @author Brian Fristensky
 */
public class CD {
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
        File newpwd;
        boolean result = false;

        try {
            // branch based on whether the deletion is recursive
            if (args.length > 0) {
                newpwd = env.fixpath(args[0]);
                if (newpwd.isDirectory() && newpwd.canRead()) {
                    env.put("PWD", newpwd.getCanonicalPath());
                    result = true;
                }
            }
        } catch (IOException ioe) {
            ioe.printStackTrace(System.err);
        }
        return (result ? 1 : 0);
    }
}
