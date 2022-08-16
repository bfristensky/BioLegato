/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.turtleshell.cmd;

import java.io.IOException;
import java.io.Reader;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import org.turtleshell.TEnv;

/**
 * Prints the current date and time to the command line.
 **
 * COMMAND LINE ARGUMENTS
 * ----------------------
 * This command accepts either no command line arguments, or a date format string
 * as defined by Java's SimpleDateFormat class.  Please note that the default is
 * not necessarily the same as the general UNIX 'date' command's default!
 *
 * EXECUTION STATUS
 * ----------------
 * This command returns one (1) unless an exception is encountered.  In the case
 * of an exception, this command will return zero (0)
 **
 * @author Graham Alvare
 * @author Brian Fristensky
 */
public class DATE {
    /**
     * The main calling method for this command
     **
     * @param args see class description for a list of available command line arguments
     * @return the execution status of this command (see class description)
     */
    public static int main(String[] args) {
        return exec(null, args, System.out, null);
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
        String dateFormat = "EEE  d MMM yyyy HH:mm:ss zz";

        try {
            if (args.length > 1) {
                dateFormat = args[0];
            }
            output.append(new SimpleDateFormat(dateFormat).format(Calendar.getInstance().getTime())).append("\n");

            // the program has succeeded; therefore, the execution status should be one (1)
            result = 1;
        } catch (IOException e) {
            e.printStackTrace(System.err);
        }

        return result;
    }
}
