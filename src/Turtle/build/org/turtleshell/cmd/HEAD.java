/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.turtleshell.cmd;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import org.turtleshell.TEnv;
import org.turtleshell.tasks.TTask;

/**
 * Deletes the trailing text of a stream (aka. removes the 'tail' from a stream)
 **
 * This command accepts input via standard input, removes the 'tail' from the input, and
 * prints the resulting output to standard output.  The 'tail' is defined as any text
 * beyond a certain portion of the stream.  While a vague definition, an example will
 * help illustrate this point.
 *
 * let's say you have a file 'shopping.list' containing the following text:
 *
 *      Shopping list
 *      1. apple
 *      2. banana
 *      3. orange
 *      4. rice
 *      5. bread
 *      6. milk
 *      .....
 *
 * if you only wanted the first 3 entries of the file, you could run the following command
 *
 * cat shopping.list | head 4
 *
 * This would print the following:
 *
 *      Shopping list
 *      1. apple
 *      2. banana
 *      3. orange
 *
 * the command line argument 4 means to print the first 4 lines (regardless of their content).
 *
 * COMMAND LINE ARGUMENTS
 * ----------------------
 * This command accepts only one parameter: the number of lines to print.
 *
 * EXECUTION STATUS
 * ----------------
 * This command returns one (1) unless an exception is encountered.  In the case
 * of an exception, this command will return zero (0)
 **
 * @author Graham Alvare
 * @author Brian Fristensky
 */
public class HEAD {
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
        int length = 10;
        int result = 0;
        int count = 0;
        String linein;
        BufferedReader bread;
        boolean characters = false;

        String lengthArg;

        // try to parse a command line parameter length, if available
        try {
            if (args.length >= 1) {
                lengthArg = args[0].trim();
                if (args.length >= 2 && !TTask.testNumber(lengthArg)) {
                    if (lengthArg.equals("-c")) {
                        characters = true;
                    }
                    lengthArg = args[1];
                }
                if (lengthArg.charAt(0) == '-') {
                    lengthArg = lengthArg.substring(1);
                }
                if (TTask.testNumber(lengthArg)) {
                    length = Integer.parseInt(lengthArg);
                }
            }
        } catch (Exception e) {
            length = 10;
        }

        try {
            bread = new BufferedReader(input);
            if (characters) {
                while (count < length && (linein = bread.readLine()) != null) {
                    if (linein.length() + count > length) {
                        output.append(linein, 0, length - count).append("\n");
                    } else {
                        output.append(linein).append("\n");
                    }
                    count += linein.length();
                }
            } else {
                while (count < length && (linein = bread.readLine()) != null) {
                    output.append(linein).append("\n");
                    count++;
                }
            }
            result = 1;
        } catch (IOException ex) {
            ex.printStackTrace(System.err);
        }

        return result;
    }
}
