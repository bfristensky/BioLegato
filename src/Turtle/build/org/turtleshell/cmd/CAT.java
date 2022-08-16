/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.turtleshell.cmd;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import org.turtleshell.TEnv;

/**
 * Reads a file to standard output
 **
 * This command reads in any number of files specified by its command line arguments
 * and outputs the contents of these files to standard output.
 *
 * COMMAND LINE ARGUMENTS
 * ----------------------
 * This command accepts any number of command line arguments.  Each command line
 * arguments should be a file name.
 *
 * EXECUTION STATUS
 * ----------------
 * This command returns one (1) unless an exception is encountered.  In the case
 * of an exception, this command will return zero (0)
 **
 * @author Graham Alvare
 * @author Brian Fristensky
 */
public class CAT {
    /**
     * The main calling method for this command
     **
     * @param args see class description for a list of available command line arguments
     * @return the execution status of this command (see class description)
     */
    public static int main(String[] args) {
        return exec(new TEnv(), args, System.out, new InputStreamReader(System.in));
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
            for (String file : args) {
                try {
                    fread = new BufferedReader(new FileReader(env.fixpath(file)));
                    while ((line = fread.readLine()) != null) {
                        output.append(line).append("\n");
                    }
                } catch (IOException ioe) {
                    ioe.printStackTrace(System.err);
                    result = 0;
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace(System.err);
            result = 0;
        }

        return result;
    }
}
