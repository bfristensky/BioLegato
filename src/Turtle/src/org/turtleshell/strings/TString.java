/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.turtleshell.strings;

import org.turtleshell.TEnv;

/**
 * An object to wrap strings (and anything that produces strings) that are used
 * within turtle shell (as strings).  An example of something used in turtle shell
 * as a string is a command.
 *
 * The reason for creating a class to wrap strings as opposed to using the native
 * Java strings class is that strings can also be generated from the output of
 * commands executed in turtle shell.  A classic example would be the command:
 *
 *      echo `expr 1 + 1
 *
 * This command would first execute the subcommand `expr 1 + 1`.  This would yield
 * the output "2".  Then, we would have the final command: echo "2".  Executing this
 * command would print 2 to the standard output stream (System.out).  Without wrapping
 * strings in turtle shell, we would not be able to obtain the "2", because it is
 * generated dynamically by running the command expr 1 + 1.  Therefore, we need
 * to wrap all strings in turtle shell until command execution.
 **
 * @author Graham Alvare
 * @author Brian Fristensky
 */
public interface TString {

    /**
     * An empty array of TString objects, for general use in other classes
     */
    public final static TString[] NULL_TSTRING = new TString[0];

    /**
     * Obtains the string representation of the TString object
     **
     * Because all TString objects must eventually be converted into strings or
     * discarded, we need a method to obtain the string value of the TString object.
     **
     * @param env the Turtle shell environment to read any necessary variable values from.
     * @return The string value of the current TString
     */
    public abstract String getValue(TEnv env);
}
