/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.turtleshell.cmd;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Collections;
import org.turtleshell.TEnv;

/**
 * Sorts lines of input from standard input in descending order.
 **
 * This command accepts input via standard input, sorts each line of input based
 * on its content, and prints the resulting output to standard output.  The sort
 * method is the same behaviour as the default sort in Java - i.e. descending order.
 *
 * let's say you have a file 'shopping.list' containing the following text:
 *
 *      6. milk
 *      3. orange
 *      1. apple
 *      5. bread
 *      2. banana
 *      4. rice
 *
 * if you only wanted to sort the file, you could run the following command:
 *
 * cat shopping.list | sort
 *
 * This would print the following:
 *
 *      1. apple
 *      2. banana
 *      3. orange
 *      4. rice
 *      5. bread
 *      6. milk
 *
 * NOTE: the lines are sorted in descending order.
 * 
 * COMMAND LINE ARGUMENTS
 * ----------------------
 * This command does not accept any arguments.
 *
 * EXECUTION STATUS
 * ----------------
 * This command returns one (1) unless an exception is encountered.  In the case
 * of an exception, this command will return zero (0)
 **
 * @author Graham Alvare
 * @author Brian Fristensky
 */
public class RSORT {
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
        ArrayList<String> lines = new ArrayList<String>();
        BufferedReader bread;

        try {
            bread = new BufferedReader(input);
            while ((linein = bread.readLine()) != null) {
                lines.add(linein + "\n");
            }
            Collections.sort(lines);
            for (String line : lines) {
                output.append(line);
            }
            result = 1;
        } catch (IOException ex) {
            ex.printStackTrace(System.err);
        }

        return result;
    }
}
