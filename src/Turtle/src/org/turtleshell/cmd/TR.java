/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.turtleshell.cmd;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.HashMap;
import java.util.HashSet;
import java.util.TreeSet;
import org.turtleshell.TEnv;

/**
 * Replaces characters within text from standard in, and pipes the results to stdout.
 **
 * This command accepts input via standard input, replaces individual characters in
 * the text (all occurrences), and prints the resulting output to standard output.
 *
 * For example:
 *
 * echo "apples" | tr 'pl' 'li'
 *
 * will produce the output 'allies'
 *
 * COMMAND LINE ARGUMENTS
 * ----------------------
 * This command accepts two arguments:
 *
 * (1) A list of characters to replace within the text from standard in
 * (2) The list of replacements for each of the characters in the first argument
 *
 * Each character in argument (2) will have a one-to-one correspondence with a
 * character from argument (1).  For example:
 *
 * tr 'abc' 'def'
 *
 * will replace all occurrences of the letter 'a' with the letter 'd'
 * will replace all occurrences of the letter 'b' with the letter 'e'
 * will replace all occurrences of the letter 'c' with the letter 'f'
 *
 * EXECUTION STATUS
 * ----------------
 * This command returns one (1) unless an exception is encountered.  In the case
 * of an exception, this command will return zero (0)
 **
 * @author Graham Alvare
 * @author Brian Fristensky
 */
public class TR {
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
        int result = 0;

        if (args.length > 1) {
            if ("-d".equals(args[0])) {
                HashSet<Character> delete = new HashSet<Character>();
                for (char delchar : args[1].toCharArray()) {
                    delete.add(delchar);
                }
                result = tr_d(delete, output, input);
            } else {
                HashMap<Character, Character> replace = new HashMap<Character, Character>();
                char[] inchars  = args[0].toCharArray();
                char[] outchars = args[1].toCharArray();
                int    finalidx = Math.min(inchars.length, outchars.length);

                for (int count = 0; count < finalidx; count++) {
                    replace.put(inchars[count], outchars[count]);
                }
                result = tr_r(replace, output, input);
            }
        } else {
            System.err.println("Usage (1) tr -d <character set>\n"
                    + "Usage (2) tr <character set> <replacement set>>");
        }
        return result;
    }

    private static int tr_d(final HashSet<Character> delete, Appendable output, Reader input) {
        int result = 0;
        String linein;
        int index = 0;
        char[] linearray;
        char[] processarray;
        char[] outarray;
        TreeSet<String> lines = new TreeSet<String>();
        BufferedReader  bread;

        try {
            bread = new BufferedReader(input);
            while ((linein = bread.readLine()) != null) {
                linearray = (linein + "\n").toCharArray();
                processarray  = new char[linearray.length];
                index = 0;
                for (int count = 0; count < linearray.length; count++) {
                    // TODO: handle null! (deletions!)
                    if (!delete.contains(linearray[count])) {
                        processarray[index] = linearray[count];
                        index++;
                    }
                }
                if (index > 0) {
                    outarray = new char[index];
                    System.arraycopy(processarray, 0, outarray, 0, Math.max(index - 1, 0));
                    String outarraystr = new String(outarray);
                    output.append(outarraystr);
                }
            }
            result = 1;
        } catch (IOException ex) {
            ex.printStackTrace(System.err);
        }

        return result;
    }

    private static int tr_r(final HashMap<Character, Character> replace, Appendable output, Reader input) {
        int result = 0;

        String linein;
        char[] linearray;
        TreeSet<String> lines = new TreeSet<String>();
        BufferedReader bread;
        try {
            bread = new BufferedReader(input);
            while ((linein = bread.readLine()) != null) {
                linearray = (linein + "\n").toCharArray();
                for (int count = 0; count < linearray.length; count++) {
                    // TODO: handle null! (deletions!)
                    if (replace.containsKey(linearray[count])) {
                        linearray[count] = replace.get(linearray[count]);
                    }
                }
                String linarraystr = new String(linearray);
                output.append(linarraystr);
            }
            result = 1;
        } catch (IOException ex) {
            ex.printStackTrace(System.err);
        }

        return result;
    }
}
