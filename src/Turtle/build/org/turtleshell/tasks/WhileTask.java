/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.turtleshell.tasks;

import org.turtleshell.TEnv;

/**
 * A task to perform while loops
 **
 * This task performs a task (or a list of tasks via sequential tasks and other
 * task containers) defined by the class variable 'exec', repeatedly based on
 * the status returned by the task 'test'.  The status of 'test' is always tested
 * before each iteration of the while-loop (which is the standard behaviour of most,
 * if not all programming languages which implement while loops), and if the status
 * of test is not zero, then exec will be executed until the status returned by 'test'
 * is equal to zero.
 **
 * @author Graham Alvare
 * @author Brian Fristensky
 */
public class WhileTask extends LogicTask {

    /**
     * Creates a while loop task
     **
     * @param test the test condition command for the loop
     * @param exec the loop command (to be executed on each iteration)
     */
    public WhileTask(TTask test, TTask exec) {
        super(test, exec);
    }

    /**
     * Executes the while loop.
     **
     * @param env the Turtle shell environment to read any necessary variable values from.
     * @return always returns 1
     */
    @Override
    public int exec(TEnv env) {
        while(test.exec(env) != 0) {
            exec.exec(env);
        }
        return 1;
    }

    /**
     * Prints the Turtle shell syntax of the current Turtle shell object
     **
     * @return the appropriate Turtle shell representation of the current Turtle shell object
     */
    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();

        builder.append("while ").append(test.toString()).append("; do\n").append(exec.toString());
        builder.append("\ndone");
        return builder.toString();
    }
}
