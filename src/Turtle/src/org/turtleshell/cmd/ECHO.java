/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.turtleshell.cmd;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import org.turtleshell.TEnv;

/**
 * Echoes command line arguments to standard out
 **
 * This command accepts command line arguments and prints them to standard out.
 *
 * COMMAND LINE ARGUMENTS
 * ----------------------
 * Any number of command line arguments will be accepted
 *
 * EXECUTION STATUS
 * ----------------
 * This command returns one (1) unless an exception is encountered.  In the case
 * of an exception, this command will return zero (0)
 **
 * @author Graham Alvare
 * @author Brian Fristensky
 */
public class ECHO {
    /**
     * The main calling method for this command
     **
     * @param args see class description for a list of available command line arguments
     * @return the execution status of this command (see class description)
     */
    public static int main(String[] args) {
        return exec(null, args, System.out, null);
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
        boolean emptybuffer = true;

        try {
            // iterate through the command line arguments
            for (String argument : args) {
                // add a space between command line arguments (but only between)
                if (!emptybuffer) {
                    output.append(" ");
                }
                output.append(argument);
                emptybuffer = false;
            }

            // append a new line character to the end of output
            output.append("\n");

            // flush the written data
            if (output instanceof Writer) {
                ((Writer)output).flush();
            }

            // the program has succeeded; therefore, the execution status should be one (1)
            result = 1;
        } catch (IOException e) {
            e.printStackTrace(System.err);
        }

        return result;
    }
}
