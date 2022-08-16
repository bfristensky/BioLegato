/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.turtleshell.tasks;

import java.io.IOException;
import org.turtleshell.TEnv;

/**
 * A task to perform if statement functionality
 **
 * This task performs a task (or a list of tasks via sequential tasks and other
 * task containers) defined by the class variable 'exec' or another task (or a list
 * of tasks via sequential tasks and other task containers) defined by the variable
 * 'else', based on the status returned by the task 'test'.  If the status of 'test'
 * is not zero, then 'exec' will be executed; otherwise, 'else' will be executed.
 * 'else' can be null, and in that case no command is executed, and exec() will return zero
 **
 * @author Graham Alvare
 * @author Brian Fristensky
 */
public class IfTask extends LogicTask {
    /**
     * The else command
     * ----------------
     * This command will be executed if the if-condition yields false (can be null - a null
     * indicates that no command will be executed if the if-condition returns false.
     *
     * NOTE: false in this instance is zero
     */
    TTask elsecmd = null;

    /**
     * Creates an if condition task
     **
     * @param test the test condition command for the if-block
     * @param exec the command to execute if the loop condition is true (returns zero)
     * @param elsecmd the command to execute if the loop condition is false (returns non-zero)
     */
    public IfTask(TTask test, TTask exec, TTask elsecmd) {
        super(test, exec);
        this.elsecmd = elsecmd;
    }

    /**
     * Executes the if condition.
     **
     * @param env the Turtle shell environment to read any necessary variable values from.
     * @return returns the value of the last command executed -- the true command if the loop yielded true, the false command if the loop yielded true (or zero if no false command and the loop yielded false)
     */
    @Override
    public int exec(TEnv env) {
        int result = test.exec(env);

        if(result != 0) {
            exec.exec(env);
        } else if (elsecmd != null) {
            elsecmd.exec(env);
        }
        return result;
    }

    /**
     * Prints the Turtle shell syntax of the current Turtle shell object
     **
     * @return the appropriate Turtle shell representation of the current Turtle shell object
     */
    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();

        builder.append("if ").append(test.toString()).append("; then\n").append(exec.toString());
        if (elsecmd != null) {
            builder.append("\nelse\n").append(elsecmd.toString());
        }
        builder.append("\nfi");
        return builder.toString();
    }
}
