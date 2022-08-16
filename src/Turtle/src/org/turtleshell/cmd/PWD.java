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
 * Prints the current working directory to the command line.
 **
 * COMMAND LINE ARGUMENTS
 * ----------------------
 * This command does not parse any command line arguments!
 *
 * EXECUTION STATUS
 * ----------------
 * This command returns one (1) unless an exception is encountered.  In the case
 * of an exception, this command will return zero (0)
 **
 * @author Graham Alvare
 * @author Brian Fristensky
 */
public class PWD {
    /**
     * The main calling method for this command
     **
     * @param args see class description for a list of available command line arguments
     * @return the execution status of this command (see class description)
     */
    public static int main(String[] args) {
        return exec(new TEnv(), args, System.out, null);
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
            // append a new line character to the end of output
            output.append(env.envreplace("$PWD")).append("\n");

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
