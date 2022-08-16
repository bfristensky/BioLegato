/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.turtleshell.cmd;

import java.io.File;
import java.io.Reader;
import org.turtleshell.TEnv;

/**
 * This command moves a file to a new location
 *
 * COMMAND LINE ARGUMENTS
 * ----------------------
 * This command accepts two command line parameters:
 *
 *  First, a the file to move
 *  Second, the destination to move the file to
 *
 * EXECUTION STATUS
 * ----------------
 * This command returns one (1) unless an exception is encountered.  In the case
 * of an exception, this command will return zero (0)
 **
 * @author Graham Alvare
 * @author Brian Fristensky
 */
public class MV {
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
        int result = 0;
        File source;
        File dest;

        if (args.length > 1) {
            source = env.fixpath(args[0]);
            dest   = env.fixpath(args[1]);
            result = (source.renameTo(dest) ? 1 : 0);
        }
        return result;
    }
}
