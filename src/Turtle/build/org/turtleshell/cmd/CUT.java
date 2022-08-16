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
 * This command extracts data from standard input based on character position and
 * field delimited matching.  Please see this command's argument documentation for
 * more information about how this command works.
 *
 * COMMAND LINE ARGUMENTS
 * ----------------------
 *
 * This command only accepts the following command line argument formats:
 *
 * cut -d delimiter -f number_range
 * cut -f number_range -d delimiter
 * cut -c number_range
 *
 *  -d delimiter    # specifies a delimiter for the field parameter
 *                  # delimiter is a regular expression!
 *                  # see Java's guide on regular expressions (Pattern in the API)
 *                  # for more details.
 *
 *  -f              # specifies which fields to extract from input based on
 *                  # the delimiter set by the -d parameter
 *
 *  -c              # specifies which characters from each line of input should
 *                  # be extracted.  This parameter may be used instead of the
 *                  # field-delimiter parameters
 *
 * number_range     # this is any numerical set of numbers - e.g., 1-4,7-9,11-
 *                  # if the last number in a set is followed by a only a dash
 *                  # (i.e., 4-), this means the set of numbers from the number
 *                  # indicated until the last number possible (e.g., -c 3- means
 *                  # every character after the second character, exclusive)
 *
 *                  # a number range must be sorted in ascending order without any
 *                  # overlap; otherwise, expect unexpected results.
 *
 * EXECUTION STATUS
 * ----------------
 * This command returns one (1) unless an exception is encountered.  In the case
 * of an exception, this command will return zero (0)
 **
 * @author Graham Alvare
 * @author Brian Fristensky
 */
public class CUT {
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
        String line;
        BufferedReader bread;

        try {
            bread = new BufferedReader(input);

            // try to parse a command line parameters
            if (args.length >= 2 && "-c".equalsIgnoreCase(args[0])) {
                int[][] rangelist = getRangeArray(args[1]);

                while ((line = bread.readLine()) != null) {
                    // process data based on the command line arguments
                    for (int[] range : rangelist) {
                        if (range[1] >= 0) {
                            output.append(line, range[0], range[1]);
                        } else {
                            output.append(line, range[0], line.length());
                        }
                    }
                    output.append("\n");
                }
                result = 1;
            } else if (args.length >= 4 && (("-d".equalsIgnoreCase(args[0]) &&
                    "-f".equalsIgnoreCase(args[2]))
                    || ("-f".equalsIgnoreCase(args[0]) && "-d".equalsIgnoreCase(args[2])))) {
                String delimiter;
                int[][] fields;
                String[] cutline;

                if ("-d".equalsIgnoreCase(args[0])) {
                    delimiter = args[1];
                    fields = getRangeArray(args[3]);
                } else {
                    delimiter = args[3];
                    fields = getRangeArray(args[1]);
                }

                while ((line = bread.readLine()) != null) {
                    cutline = line.split(delimiter);
                    
                    // process data based on the command line arguments
                    for (int[] range : fields) {
                        if (range[1] >= 0) {
                            for (int count = range[0]; count <= Math.min(range[1], cutline.length - 1); count++) {
                                output.append(cutline[count]);
                            }
                        } else {
                            for (int count = range[0]; count < cutline.length; count++) {
                                output.append(cutline[count]);
                            }
                        }
                    }
                    output.append("\n");
                }
                result = 1;
            }
        } catch (Exception e) {
            e.printStackTrace(System.err);
        }
        return result;
    }

    private static int[][] getRangeArray(String rangestring) throws Exception {
        // process the ranges into a simpler format, for performance
        String[] temp2;
        String[] tempranges = rangestring.split(",");
        int[][] ranges = new int[tempranges.length][];

        for (int count = 0; count < tempranges.length; count++) {
            if (tempranges[count].contains("-")) {
                temp2 = tempranges[count].split("-");

                if (temp2.length > 0) {
                    ranges[count] = new int[2];
                    ranges[count][0] = Integer.parseInt(temp2[0]);
                    ranges[count][1] = -1;
                    if (temp2.length > 1 && temp2[1].length() > 0) {
                        ranges[count][1] = Integer.parseInt(temp2[1]);
                    }

                    if (ranges[count][1] >= 0 && ranges[count][0] > ranges[count][1]) {
                        throw new NumberFormatException();
                    }
                }
            } else {
                ranges[count] = new int[2];
                ranges[count][0] = Integer.parseInt(tempranges[count]);
                ranges[count][1] = ranges[count][0];
            }
        }

        return ranges;
    }
}
