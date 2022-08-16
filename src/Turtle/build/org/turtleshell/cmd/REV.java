/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.turtleshell.cmd;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.Reader;
import org.turtleshell.TEnv;

/**
 * Reverses the text on each line in the standard pipeline
 **
 * This command reads in data from standard in, and reverses the text on each line.
 *
 * COMMAND LINE ARGUMENTS
 * ----------------------
 * This command does not accept any command line arguments.
 *
 * EXECUTION STATUS
 * ----------------
 * This command returns one (1) unless an exception is encountered.  In the case
 * of an exception, this command will return zero (0)
 **
 * @author Graham Alvare
 * @author Brian Fristensky
 */
public class REV {
    /**
     * The main calling method for this command
     **
     * @param args see class description for a list of available command line arguments
     * @return the execution status of this command (see class description)
     */
    public static int main(String[] args) {
        return exec(null, args, System.out, new InputStreamReader(System.in));
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
        int result = 1;
        String line;
        BufferedReader fread;

        try {
            fread = new BufferedReader(input);
            while ((line = fread.readLine()) != null) {
                output.append(new StringBuffer(line).reverse()).append("\n");
            }
            result = 1;
        } catch (Exception ex) {
            ex.printStackTrace(System.err);
            result = 0;
        }

        return result;
    }
}
