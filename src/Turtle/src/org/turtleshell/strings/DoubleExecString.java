/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.turtleshell.strings;

import java.io.StringReader;
import org.turtleshell.*;
import org.turtleshell.tasks.TTask;

/**
 * This object wraps executable string
 **
 * An object to wrap commands as strings.  The command wrapped by this object
 * will be executed whenever the value of the string is needed.  The value of the
 * string will be the standard output of the command.  For example:
 *
 *      echo `expr 1 + 1
 *
 * This command would first execute the subcommand `expr 1 + 1`.  This would yield
 * the output "2".  Then, we would have the final command: echo "2".  Executing this
 * command would print 2 to the standard output stream (System.out).  Without wrapping
 * strings in turtle shell, we would not be able to obtain the "2", because it is
 * generated dynamically by running the command expr 1 + 1.
 **
 * @author Graham Alvare
 * @author Brian Fristensky
 */
public class DoubleExecString implements TString {
    /**
     * The command which we will be using its standard output as a shell string
     */
    protected TTask exec = null;

    /**
     * Creates a new executable turtle shell string object represented by the turtle
     * shell command 'exec'
     **
     * @param exec the turtle shell command which we will be using its standard output as a shell string
     */
    public DoubleExecString(String exec) throws org.turtleshell.ParseException {
        this.exec    = new Turtle(new StringReader(exec)).CommandList();
    }

    /**
     * Creates a new executable turtle shell string object represented by the turtle
     * shell command 'exec'
     **
     * @param exec the turtle shell command which we will be using its standard output as a shell string
     */
    public DoubleExecString(TTask exec) {
        this.exec    = exec;
    }

    /**
     * Returns the standard output of the command represented by this object in the
     * form of a Java string object.  Please note that all variable names contained within
     * the standard output of this object (i.e., $ANYTHING) will NOT be replaced by their
     * local turtle shell stored variable values!
     **
     * @param env the Turtle shell environment to read any necessary variable values from.
     * @return the standard output of the command represented by this object in the form of a Java string object
     */
    public String getValue(TEnv env) {
        StringBuffer resultrun = new StringBuffer();
        exec.setoutput(resultrun);
        exec.forcefg();
        exec.exec(env);
        return resultrun.toString();
    }

    /**
     * Prints the Turtle shell syntax of the current Turtle shell object
     **
     * @return the appropriate Turtle shell representation of the current Turtle shell object
     */
    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();

        builder.append("$(").append(exec.toString()).append(")");
        return builder.toString();
    }
}
