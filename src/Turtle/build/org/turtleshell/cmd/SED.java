/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.turtleshell.cmd;

import java.io.InputStreamReader;
import java.io.Reader;
import org.turtleshell.TEnv;

/**
 * A simplified implementation of the UNIX sed command.
 **
 * NOT FINISHED!!!
 *
 * THE FOLLOWING COMMAND IS UNDER CONSTRUCTION, EXECUTING THIS COMMAND IN TURTLE
 * SHELL WILL ONLY EXECUTE THE LOCAL SYSTEM COPY, NOT THIS CLASS COPY!
 **
 * This simplified version of the UNIX sed only accepts input from standard in.
 * 
 * COMMAND LINE ARGUMENTS
 * ----------------------
 *
 * sed [flags] command_phrase
 * sed [flags including -e command_phrase]
 *
 * # flags:
 *  -e command_phrase   # used for specifying multiple commands on the same line
 *
 *  -f file             # reads a list of commands from a file
 *
 * # command phrase structures
 *
 *      [scope] command_list
 *
 * # command phrase structure elements in detail:
 *
 *  scope:
 *      the scope/range of the command being processed.
 *      this accepts the following format:
 *
 *          list_of_ranges
 *
 *          list_of_ranges  =>  range   "," list_of_ranges
 *          range           =>  line
 *                          |   line    "-"
 *                          |   line    "-" line
 *          line            =>  line_number | regular_expression
 *
 *      examples:
 *          300           # the 300th line ONLY!
 *          100-1000      # lines 100 to 1000
 *          100,200       # lines 100 and 2000
 *          /start/       # all lines matching the regular expression /start/
 *          /start/-/end/ # all lines inclusively between the lines matching
 *                        # the /start/ and /end/ regular expressions
 *
 *      NOTE: THIS SYNTAX DIFFERS HEAVILY FROM UNIX SED!  This syntax instead
 *            resembles the character/column specification for the cut command.
 *            The choice of this difference is due to the added versatility of
 *            the cut command.
 * 
 *  command_list:
 *
 *      A command list in sed is a new-line separated list of commands and their
 *      parameters.  Each command in the command list can be preceded by a number,
 *      to indicate how many times the command is to be repeated (e.g., " 3p".
 *
 *  command & parameters:
 *
 *      All commands will be performed ONLY within the scope specified above!
 *
 *      ex  execute:
 *          --------
 *          MY OWN CREATION!
 *
 *          Executes a command in turtle shell, using the output from the line as
 *          The command's standard input!
 *
 *          This is a really cool way to perform data manipulation on a set of lines.
 *          $LINE within the command will be replaced with the current line number;
 *          HOWEVER, the environment variable $LINE will NOT BE SET!  Therefore,
 *          if you wish to manipulate the line number, you should first save it to
 *          a local variable of some sort.
 *
 *          The output from this command will replace the current line.
 *
 *      s   substitution:
 *          -------------
 *          The command 's' accepts two
 *
 *      d   delete
 *          ------
 *          Does not print the current line (can only be performed once per line).
 *
 *      p   print
 *          -----
 *          Duplicates the current line.  This forces sed to print the current line
 *          again (the default behaviour is to print the current line once).
 *
 *      !   NEGATIVE match for command
 *          --------------------------
 *          The '!' command is always followed immediately by another command.  The
 *          command that follows the '!' will only be executed on lines outside of
 *          the range specified in the 'scope' parameter.
 *
 *      q   stop
 *          ----
 *          Stops parsing
 *
 *      {}  subcommands
 *          -----------
 *          Specifies a command phrase within the given level of scope.  The format
 *          of what goes between the braces '{}', is the same as the command phrase
 *          structure specified above.
 *
 *      a   append
 *          ------
 *          appends a line after the current line
 *
 *      i   insert
 *          ------
 *          inserts a line preceding the current line
 *
 *      c   change
 *          ------
 *          replaces the current line with the parameter given after change
 *
 *      =   print line number
 *          -----------------
 *          prints the current line number
 *
 *
 * THERE ARE MORE COMMANDS TO COME FROM: http://www.grymoire.com/Unix/Sed.html
 * 
 * regular expression types
 *
 *
 * EXECUTION STATUS
 * ----------------
 * This command returns one (1) unless an exception is encountered.  In the case
 * of an exception, this command will return zero (0)
 **
 * @author Graham Alvare
 * @author Brian Fristensky
 */
public class SED {
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
    public static int exec (TEnv env, final String[] args, Appendable output, Reader input) {
        int result = -1;

        // return value the number of matches found
        // >0 	A match was found (NOTE: negative matching counts as matches).
        //      the number indicates the number of matches total.
        // 0 	No match was found.
        // <0 	A syntax error was found or a file was inaccessible (even if matches were found).

        return result;
    }
}
