/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.turtleshell.cmd;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import org.turtleshell.TEnv;

/**
 * A simplified implementation of the UNIX grep command.
 **
 * This simplified version of the UNIX grep only accepts input from standard in.
 * If you wish to grep files, please use the simplified fgrep command.
 * 
 * COMMAND LINE ARGUMENTS
 * ----------------------
 *
 * grep [flags] pattern(s)
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
 *  -n              # When outputting the results of the matcher, this flag
 *                  # indicates that each line of output should be preceded by
 *                  # the line's relative line number within the file.  In fgrep,
 *                  # each file starts at line one (1), and the line counter is
 *                  # reset for each file processed.
 *
 *  -c              # When outputting the results of the matcher, this flag
 *                  # indicates that only the number of matching lines should be
 *                  # printed for each file.  In fgrep, each file starts at line
 *                  # one (1), and the line counter is reset for each file
 *                  # processed.
 *                  
 *                  # The -c flag overrides the -n flag; therefore, if both were
 *                  # specified, only the -c flag would be used!
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
public class GREP {

    /**
     * An enumeration of the output types supported by this GREP
     */
    public static enum OutputType {
        /**
         * Outputs all of the lines matched, as they appear within the source
         */
        REGULAR,
        /**
         * Outputs all of the lines matched preceded by line numbers
         */
        LINE_NUMBERS,
        /**
         * Outputs only a count of the number of lines matched within the file
         */
        COUNTS_ONLY,
        /**
         * Outputs only the file names where matches were found -- ONLY used in FGREP
         */
        FILENAMES,
        /**
         * Does not output anything
         */
        NO_OUTPUT;
    }

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
    public static int exec (TEnv env, final String[] args, Appendable output, Reader input) {
        int count  = 0;
        int result = -1;

        // pattern flags
        boolean negativeMatch = false;
        boolean caseInsensitive = false;
        boolean regex = true;

        // program flags
        OutputType outputFormat = OutputType.REGULAR;

        // the pattern lists
        List<Pattern> positivePatterns = new ArrayList<Pattern>();
        List<Pattern> negativePatterns = new ArrayList<Pattern>();

        while (count < args.length) {
            if (args[count].startsWith("-")) {
                char[] flags = args[count].substring(1).toCharArray();
                for (int flag_count = 0; flag_count < flags.length; flag_count++) {
                    switch (flags[flag_count]) {
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
                            if (outputFormat != OutputType.NO_OUTPUT) {
                                outputFormat = OutputType.COUNTS_ONLY;
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
            } else {
                if (negativeMatch) {
                    addPattern(negativePatterns, args[count], caseInsensitive, regex);
                } else {
                    addPattern(positivePatterns, args[count], caseInsensitive, regex);
                }
                // reset all of the pattern flags for the next pattern
                negativeMatch = false;
                caseInsensitive = false;
                regex = true;
            }
            count++;
        }

        // perform the matching
        result = matchInput(outputFormat, input, output, positivePatterns, negativePatterns, "");

        if (outputFormat == OutputType.COUNTS_ONLY) {
            try {
                output.append(String.valueOf(Math.max(0, result))).append("\n");
            } catch (Exception ex) {
            }
        }

        // return value the number of matches found
        // >0 	A match was found (NOTE: negative matching counts as matches).
        //      the number indicates the number of matches total.
        // 0 	No match was found.
        // <0 	A syntax error was found or a file was inaccessible (even if matches were found).

        return result;
    }

    /**
     * Adds patterns to GREP, from a file.
     **
     * Reads in a file, containing patterns, and adds the patterns to the list of
     * search patterns.  GREP will then take these patterns and attempt to match
     * them within its input.
     **
     * @param env the Turtle shell environment to read any necessary variable values from.
     * @param patterns the list of patterns that this instance of grep will use to match input.  This is where the patterns from the file are added.
     * @param filename the filename of the file to read patterns from.
     * @param caseInsensitive whether the pattern matching should be case insensitive
     * @param regex whether the pattern is a regular expression (true) or a string (false) to match
     */
    protected static void addFilePatterns(TEnv env, List<Pattern> patterns, String filename, boolean caseInsensitive, boolean regex) {
        try {
            String line;
            BufferedReader breader = new BufferedReader(new FileReader(env.fixpath(filename)));

            while ((line = breader.readLine()) != null) {
                addPattern(patterns, line, caseInsensitive, regex);
            }

            breader.close();
        } catch (IOException ioe) {
            ioe.printStackTrace(System.err);
        }
    }

    /**
     * Adds a single pattern to GREP.
     **
     * @param patterns the list of patterns that this instance of grep will use to match input.  This is where the pattern is added.
     * @param line the pattern to parse into GREP
     * @param caseInsensitive whether the pattern matching should be case insensitive
     * @param regex whether the pattern is a regular expression (true) or a string (false) to match
     */
    protected static void addPattern(List<Pattern> patterns, String line, boolean caseInsensitive, boolean regex) {
        // handle non-regular expression patterns
        if (!regex) {
            line = "\\Q" + line + "\\E";
        }

        // branch based on whether the pattern is case sensitive
        if (caseInsensitive) {
            patterns.add(Pattern.compile(line, Pattern.CASE_INSENSITIVE));
        } else {
            patterns.add(Pattern.compile(line));
        }
    }

    /**
     * Performs the matching algorithm for any given input reader
     **
     * @param outputFormat how the output should be printed to the appendable output object
     * @param input the source of the input to perform the match algorithm on
     * @param output the destination for output
     * @param positivePatterns the list of positive patterns loaded into grep (these will match true if the pattern is found in the input)
     * @param negativePatterns the list of negative patterns loaded into grep (these will match true if the pattern is NOT found in the input)
     * @param prefix a prefix to head any output (used by FGREP to add the filename to the output)
     * @return the number of matches found within the input reader (<0 indicates an error occurred)
     */
    protected static int matchInput(OutputType outputFormat, Reader input, Appendable output, List<Pattern> positivePatterns, List<Pattern> negativePatterns, String prefix) {
        int result = 0;

        try {
            int match_count = 0;
            String line;
            LineNumberReader breader = new LineNumberReader(input);
            breader.setLineNumber(1);
            while ((line = breader.readLine()) != null) {
                if (match(positivePatterns, negativePatterns, line)) {
                    match_count++;
                    switch(outputFormat) {
                        case LINE_NUMBERS:
                            output.append(prefix).append(String.valueOf(breader.getLineNumber())).append(":").append(line).append("\n");
                            break;
                        case REGULAR:
                            output.append(prefix).append(line).append("\n");
                            break;
                        default:
                            break;
                    }
                    match_count++;
                }
            }
            result = match_count;
        } catch (IOException ioe) {
            ioe.printStackTrace(System.err);
        }
        return result;
    }

    /**
     * Matches any positive or negative pattern loaded into GREP, within a given line
     **
     * @param positivePatterns the list of positive patterns loaded into grep (these will match true if the pattern is found in the input)
     * @param negativePatterns the list of negative patterns loaded into grep (these will match true if the pattern is NOT found in the input)
     * @param line the line to search for the pattern(s)
     * @return whether the pattern was found
     */
    protected static boolean match(List<Pattern> positivePatterns, List<Pattern> negativePatterns, String line) {
        boolean matched = false;

        // test all of the negative patterns
        // a match indicates that the pattern IS present within the line
        for (Pattern p : positivePatterns) {
            if (p.matcher(line).find()) {
                matched = true;
                break;
            }
        }

        // test all of the negative patterns
        // a match indicates that the pattern is NOT present within the line
        for (Pattern p : negativePatterns) {
            if (!p.matcher(line).find()) {
                matched = true;
                break;
            }
        }

        return matched;
    }
}
