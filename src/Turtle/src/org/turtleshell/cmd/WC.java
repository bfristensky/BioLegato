/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.turtleshell.cmd;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import org.turtleshell.TEnv;

/**
 * <p><b><i>
 * Prints either a character, word or line count of the input stream.
 * </i></b></p>
 **
 * <p><i>NOTE: the output of this command differs from its UNIX counterpart.</i>
 * </p>
 *
 * <p>Unlike UNIX, this will print only one count, and will print the count with
 * a new line.  There will be no spacing before or after the count.</p>
 *
 * <p><u>COMMAND LINE ARGUMENTS</u></p>
 * <!--  ---------------------- -->
 * <p>This command accepts either ZERO OR ONE of the following arguments:
 *
 * <table>
 *  <tr><td>-c</td><td>Perform a character count</td></tr>
 *  <tr><td>-w</td><td>word line count (using whitespace
 *                     as a delimiter)</td></tr>
 *  <tr><td>-l</td><td>Perform a line count</td></tr>
 * </table>
 *
 * <p>The default behaviour is to print a word count (i.e., if no parameters are
 * specified, wc will act as if the -w parameter was specified).</p>
 *
 * <p><u>EXECUTION STATUS</u></p>
 * <!--  ---------------- -->
 * <p>This command returns one (1) unless an exception is encountered.  In the case
 * of an exception, this command will return zero (0)</p>
 **
 * @author Graham Alvare
 * @author Brian Fristensky
 */
public class WC {
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
        int count = 0;
        String line;
        boolean emptybuffer = true;

        if (args.length >= 1) {
            try {
                BufferedReader iread = new BufferedReader(input);

                if ("-l".equalsIgnoreCase(args[0])) {
                    while ((line = iread.readLine()) != null) {
                        count++;
                    }
                } else if ("-c".equalsIgnoreCase(args[0])) {
                    while ((line = iread.readLine()) != null) {
                        count+= line.length() + 1;
                    }
                } else {
                    while ((line = iread.readLine()) != null) {
                        count+= line.split("[ \t\n\r]*").length;
                    }
                }

                // append a new line character to the end of output
                output.append(String.valueOf(count)).append("\n");

                // the program has succeeded; therefore, the execution status should be one (1)
                result = 1;
            } catch (IOException e) {
                e.printStackTrace(System.err);
            }
        }

        return result;
    }
}
