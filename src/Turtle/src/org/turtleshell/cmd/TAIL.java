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
 * Deletes the trailing text of a stream (aka. removes the 'head' from a stream)
 **
 * This command accepts input via standard input, removes the 'head' from the input, and
 * prints the resulting output to standard output.  The 'head' is defined as leading portion
 * of the stream.  While a vague definition, an example will help illustrate this point:
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
 *
 * if you only wanted the last 3 entries of the file, you could run the following command
 *
 * cat shopping.list | tail 3
 *
 * This would print the following:
 *
 *      4. rice
 *      5. bread
 *      6. milk
 *
 * the command line argument 3 means to print the last 3 lines (regardless of their content).
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
public class TAIL {
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
        String lengthArg;
        int result = 0;
        int index= 0;
        String linein;
        BufferedReader bread;
        boolean characters = false;
        boolean reverseindex = true;

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
                if (lengthArg.charAt(0) == '+') {
                    lengthArg = lengthArg.substring(1);
                    reverseindex = false;
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
                if (reverseindex) {
                    int charsread;
                    int totalread = 0;
                    char[] tail   = new char[length];
                    char[] cache  = new char[length];

                    // TODO: printing in reverse order
                    while ((charsread = bread.read(cache)) >= 0) {
                        if (charsread + index > tail.length) {
                            int remaining = tail.length - index;
                            System.arraycopy(cache, 0, tail, index, tail.length - index);
                            System.arraycopy(cache, remaining, tail, 0, charsread - remaining);
                        } else {
                            System.arraycopy(cache, 0, tail, index, charsread);
                        }
                        index += charsread;
                        index %= tail.length;
                        totalread += charsread;
                    }
                    String outtail = new String(tail);

                    output.append(outtail, index, Math.min(totalread, tail.length));
                    if (index > 0) {
                        output.append(outtail, 0, index);
                    }
                } else {
                    // TODO: printing in reverse order
                    bread.skip(length);
                    while ((linein = bread.readLine()) != null) {
                        output.append(linein).append("\n");
                    }
                }
            } else {
                if (reverseindex) {
                    String[] tail = new String[length];

                    // TODO: printing in reverse order
                    while ((linein = bread.readLine()) != null) {
                        index--;
                        if (index < 0) {
                            index = tail.length - 1;
                        }
                        tail[index] = linein;
                    }
                    if (tail[index] != null) {
                        output.append(tail[index]).append("\n");
                        for (int count = index + 1 % tail.length; count != index && tail[index] != null; count = (count + 1)  % tail.length) {
                            output.append(tail[index]).append("\n");
                        }
                    }
                } else {
                    // TODO: printing in reverse order
                    index = 1;
                    while ((linein = bread.readLine()) != null) {
                        if (index >= length) {
                            output.append(linein).append("\n");
                        }
                        index++;
                    }
                }
            }
            result = 1;
        } catch (IOException ex) {
            ex.printStackTrace(System.err);
        }

        return result;
    }
}
