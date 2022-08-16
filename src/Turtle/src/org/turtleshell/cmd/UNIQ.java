/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.turtleshell.cmd;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.TreeSet;
import org.turtleshell.TEnv;

/**
 * Removes duplicate entries from a stream.
 **
 * This command accepts input via standard input, and removes all duplicates, and
 * prints the resulting output to standard output.
 *
 * COMMAND LINE ARGUMENTS
 * ----------------------
 * This command does NOT accept any command line arguments as parameters
 *
 * EXECUTION STATUS
 * ----------------
 * This command returns one (1) unless an exception is encountered.  In the case
 * of an exception, this command will return zero (0)
 **
 * @author Graham Alvare
 * @author Brian Fristensky
 */
public class UNIQ {
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
        int result = 0;
        String linein;
        TreeSet<String> lines = new TreeSet<String>();
        BufferedReader bread;

        try {
            bread = new BufferedReader(input);

            // read input from standard in
            while ((linein = bread.readLine()) != null) {
                // if the line is a duplicate, skip it
                if (!lines.contains(linein)) {
                    output.append(linein + "\n");
                    lines.add(linein);
                }
            }
            
            // if the command reaches this far, it has succeeded, the execution
            // status should now be set to one (1)
            result = 1;
        } catch (IOException ex) {
            ex.printStackTrace(System.err);
        }
        return result;
    }

}
