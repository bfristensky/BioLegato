/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.turtleshell.cmd;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.InputStreamReader;
import java.io.Reader;
import org.turtleshell.TEnv;

/**
 * Concatenates a file to standard output with a delimiter
 **
 * This command reads in any number of files specified by its command line arguments
 * and concatenates the contents of these files to standard output separated by
 * a delimiter line for line.  If no delimiter is specified, the default is TAB!
 *
 * The to the left of the delimiter will be the content content from standard in,
 * and to the right of the delimiter will be the content from the file.  This
 * relationship to the delimiter will be maintained even if one of the two inputs
 * exceeds the other in length.
 *
 * Example #1:
 *
 *      Delimiter:  "," (comma)
 *
 *      Standard in contains:
 *
 *          ABC
 *          DEF
 *          GHI
 *          JKL
 *
 *      The file contains:
 * 
 *          123
 *          456
 *          789
 *
 *      The output would be:
 *
 *          ABC,123
 *          DEF,456
 *          GHI,789
 *          JKL,
 *
 * Example #2:
 *
 *      Delimiter:  "," (comma)
 *
 *      Standard in contains:
 *
 *          ABC
 *          GHI
 *
 *      The file contains:
 *
 *          123
 *          456
 *          789
 *
 *      The output would be:
 *
 *          ABC,123
 *          GHI,456
 *          ,789
 *
 *
 * COMMAND LINE ARGUMENTS
 * ----------------------
 * This command accepts one file and an optional delimiter as such:
 *
 *  paste [-d delimiter] file
 *
 * EXECUTION STATUS
 * ----------------
 * This command returns one (1) unless an exception is encountered.  In the case
 * of an exception, this command will return zero (0)
 **
 * @author Graham Alvare
 * @author Brian Fristensky
 */
public class PASTE {
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
        String linefile;
        String lineinput;
        String delimiter = "\t";
        BufferedReader iread;
        BufferedReader fread;

        try {
            if (args.length >= 3 && "-d".equalsIgnoreCase(args[0])) {
                delimiter = args[1];
                fread = new BufferedReader(new FileReader(env.fixpath(args[2])));
            } else {
                fread = new BufferedReader(new FileReader(env.fixpath(args[0])));
            }

            // create a new buffered reader to buffer the standard input stream
            iread = new BufferedReader(input);

            while ((linefile = fread.readLine()) != null &&(lineinput = iread.readLine()) != null) {
                output.append(lineinput).append(delimiter).append(linefile).append("\n");
            }

            // CLEAN-UP (if either standard in, or the file contains more lines, append them
            // to the end of input, with the delimiter (as if this were a TSV file, and
            // we wanted to maintain column semantics - i.e., the file column always contains
            // content from the file, and the input stream column always contains content
            // from the input stream).
            while ((linefile = fread.readLine()) != null) {
                output.append(delimiter).append(linefile).append("\n");
            }
            while ((lineinput = iread.readLine()) != null) {
                output.append(lineinput).append(delimiter).append("\n");
            }
        } catch (Exception ex) {
            ex.printStackTrace(System.err);
            result = 0;
        }

        return result;
    }
}
