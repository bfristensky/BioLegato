/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.turtleshell.cmd;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import org.turtleshell.TEnv;

/**
 * A simplified implementation of the UNIX grep command.
 **
 * This simplified version of the UNIX grep only greps files.  If you wish to grep
 * input from standard input, please use the simplified fgrep command.
 *
 * COMMAND LINE ARGUMENTS
 * ----------------------
 *
 * grep [flags] [pattern*] search_path(s)
 *
 * A pattern will only be accepted without a preceding flag, if the -f or -e flags
 * have not been used.
 *
 * flags can be any one of the following:
 *
 * # Pattern matching flags
 *  -i              # case insensitive matching
 *
 *  -v              # negative matching -- the next pattern found will not be
 *                  # matched instead grep will test the pattern as follows: if
 *                  # the pattern is not found in the input, then grep will treat
 *                  # the pattern as matching.  If the pattern IS found within
 *                  # the input, then grep will treat the pattern as NOT matching.
 *
 *                  # NOTE: this is the reverse of grep's usual behaviour
 *
 *  -F              # treat the next pattern as a literal string,
 *                  # rather than a regular expression
 *
 *  -f file         # reads a list of patterns from a file using the current flags
 *
 *  -e pattern      # adds another pattern to the list of patterns (this is
 *                  # primarily used in fgrep, which otherwise would only accept
 *                  # one pattern
 *
 * # Program and output flags
 *
 *  -R              # recurse through any directories within the search path
 *
 *  -r              # same as -R
 *
 *  -n              # When outputting the results of the matcher, this flag
 *                  # indicates that each line of output should be preceded by
 *                  # the line's relative line number within the file.  In fgrep,
 *                  # each file starts at line one (1), and the line counter
 *                  # is reset for each file processed.
 *
 *  -c              # When outputting the results of the matcher, this flag
 *                  # indicates that only the number of matching lines should
 *                  # be printed for each file.  In fgrep, each file starts at
 *                  # line one (1), and the line counter is reset for each file
 *                  # processed.
 *
 *                  # The -c flag overrides the -n flag; therefore, if both were
 *                  # specified, only the -c flag would be used!
 *
 *  -l              # When outputting the results of the matcher, this flag
 *                  # indicates that only file names containing matches should
 *                  # be printed.
 *
 *  -q              # Suppress all output from GREP!
 *                  
 *                  # This flag overrides ALL other output flags!
 *
 * EXECUTION STATUS
 * ----------------
 * This command returns the following values based on whether the program matched its input:
 *
 * >0 	A match was found (NOTE: negative matching counts as matches).
 *      the number indicates the number of matches total.
 * 0 	No match was found.
 * <0 	A syntax error was found or a file was inaccessible (even if matches were found).
 **
 * @author Graham Alvare
 * @author Brian Fristensky
 */
public class FGREP extends GREP {

    /**
     * A general empty File object array, for using a list's toArray() method
     */
    public final static File[] FILE_NULL_ARRAY = new File[0];

    /**
     * The main calling method for this command
     **
     * @param args see class description for a list of available command line arguments
     * @return the execution status of this command (see class description)
     */
    public static int main(String[] args) {
        return FGREP.exec(new TEnv(), args, System.out, new InputStreamReader(System.in));
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
    public static int exec (TEnv env, final String[] args, Appendable output, Reader input) {
        int count  = 0;
        int result = -1;

        // pattern flags
        boolean negativeMatch = false;
        boolean caseInsensitive = false;
        boolean regex = true;

        // program flags
        boolean recursion = false;
        OutputType outputFormat = OutputType.REGULAR;

        // the pattern lists
        List<Pattern> positivePatterns = new ArrayList<Pattern>();
        List<Pattern> negativePatterns = new ArrayList<Pattern>();

        // the search paths
        List<File> searchPaths = new ArrayList<File>();

        while (count < args.length) {
            if (args[count].startsWith("-")) {
                char[] flags = args[count].substring(1).toCharArray();
                for (int flag_count = 0; flag_count < flags.length; flag_count++) {
                    switch (flags[flag_count]) {
                        case 'r': case 'R':
                            // Searches directories recursively. By default, links to directories are followed.
                            recursion = true;
                            break;
                        case 'i':
                            // case insensitive
                            caseInsensitive = true;
                            break;
                        case 'v':
                            // Displays all lines not matching the specified pattern.
                            negativeMatch = true;
                            break;
                        case 'F':
                            // Treats each specified pattern as a string instead of a regular expression.
                            // A NULL string matches every line.
                            regex = false;
                            break;
                        case 'n':
                            // Precedes each line with the relative line number in the file. Each file starts at line 1, and the line counter is reset for each file processed.
                            if (outputFormat == OutputType.REGULAR) {
                                outputFormat = OutputType.LINE_NUMBERS;
                            }
                            break;
                        case 'c':
                            // Displays only a count of matching lines.
                            // The -c flag with the -n flags behaves like the -c flag only.
                            if (outputFormat != OutputType.FILENAMES && outputFormat != OutputType.NO_OUTPUT) {
                                outputFormat = OutputType.COUNTS_ONLY;
                            }
                            break;
                        case 'l':
                            // file names
                            if (outputFormat != OutputType.NO_OUTPUT) {
                                outputFormat = OutputType.FILENAMES;
                            }
                            break;
                        case 'q':
                            // Suppresses all writing to standard output, regardless of matching lines.
                            // Exits with a zero status if an input line is selected.  The -q flag with
                            // any combination of the -c, -l and -n flags behaves like the -q flag only.
                            outputFormat = OutputType.NO_OUTPUT;
                            break;
                        case 'e':
                            // handles an extra command line pattern for parsing
                            // add an extra increment because the next parameter is an expression,
                            // which we have just parsed
                            count++;
                            if (count < args.length) {
                                if (negativeMatch) {
                                    addPattern(negativePatterns, args[count], caseInsensitive, regex);
                                } else {
                                    addPattern(positivePatterns, args[count], caseInsensitive, regex);
                                }
                            }

                            // reset all of the pattern flags for the next pattern
                            negativeMatch = false;
                            caseInsensitive = false;
                            regex = true;

                            // BREAK OUT OF THE FOR LOOP!
                            flag_count = flags.length + 1;
                            break;
                        case 'f':
                            // handles a file with patterns for parsing
                            // add an extra increment because the next parameter is a file,
                            // which we have just parsed
                            count++;
                            if (count < args.length) {
                                if (negativeMatch) {
                                    addFilePatterns(env, negativePatterns, args[count], caseInsensitive, regex);
                                } else {
                                    addFilePatterns(env, positivePatterns, args[count], caseInsensitive, regex);
                                }
                            }

                            // reset all of the pattern flags for the next pattern
                            negativeMatch = false;
                            caseInsensitive = false;
                            regex = true;

                            // BREAK OUT OF THE FOR LOOP!
                            flag_count = flags.length + 1;
                            break;
                        default:
                            break;
                    }
                }
            } else if (positivePatterns.isEmpty() && negativePatterns.isEmpty()) {
                if (negativeMatch) {
                    addPattern(negativePatterns, args[count], caseInsensitive, regex);
                } else {
                    addPattern(positivePatterns, args[count], caseInsensitive, regex);
                }

                // reset all of the pattern flags for the next pattern
                negativeMatch = false;
                caseInsensitive = false;
                regex = true;
            } else {
                // add to search path
                searchPaths.add(env.fixpath(args[count]));
            }
            count++;
        }

        // perform the matching
        result = matchFiles(outputFormat, searchPaths.toArray(FILE_NULL_ARRAY), output, positivePatterns, negativePatterns, recursion);

        // return value the number of matches found
        // >0 	A match was found.
        // 0 	No match was found.
        // <0 	A syntax error was found or a file was inaccessible (even if matches were found).

        return result;
    }

    private static int matchFiles(OutputType outputFormat, File[] searchPaths, Appendable output, List<Pattern> positivePatterns, List<Pattern> negativePatterns, boolean recursion) {
        int matches = 0;
        int result  = 0;

        for (File path : searchPaths) {
            // test files individually, or if the current path is a directory:
            // 1. branch recursively, if recursion is set to true; otherwise,
            // 2. skip the directory
            if (path.isFile() && path.canRead()) {
                try {
                    matches = matchInput(outputFormat, new FileReader(path), output, positivePatterns, negativePatterns, path.getCanonicalPath() + ":");
                    if (outputFormat == OutputType.COUNTS_ONLY) {
                        try {
                            output.append(path.getCanonicalPath()).append(":").append(String.valueOf(Math.max(0, matches))).append("\n");
                        } catch (Exception ex) {
                        }
                    }
                    if (outputFormat == OutputType.FILENAMES && matches > 0) {
                        output.append(path.getCanonicalPath()).append("\n");
                    }
                } catch (IOException ioe) {
                    ioe.printStackTrace(System.err);
                    matches = -1;
                }
            } else if (path.isDirectory() && recursion) {
                matches = matchFiles(outputFormat, path.listFiles(), output, positivePatterns, negativePatterns, recursion);
            }

            // if the execution status is less than one, or there was a recent error, then keep the
            // result less than one; otherwise, add all of the matches to the result exectuion status.
            if (matches >= 0 && result >= 0) {
                result += matches;
            } else {
                result = -1;
            }
        }
        return result;
    }
}
