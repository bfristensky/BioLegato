/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.turtleshell.tasks;

import org.turtleshell.TEnv;

/**
 * A turtle shell task wrapper for two commands.
 * The two commands are performed as such:
 *
 * The first command is performed, and if the execution value returned by the
 * command is equal to zero, the second command is also performed.  If, however,
 * the first command returned a non-zero value, the second command is skipped.
 **
 * @author Graham Alvare
 * @author Brian Fristensky
 */
public class OrTask extends DualTask {
    /**
     * Constructs a new task wrapper representing an exclusive disjunction of tasks
     **
     * @param current the first task (to test the execution status of)
     * @param next the second task (to execute based on the execution status of the first task)
     */
    public OrTask(TTask current, TTask next) {
        super(current, next);
    }

    /**
     * Performs both the test of the first task, and the appropriate skipping or execution
     * of the second task
     **
     * The execution status of this command is based on whether both tasks are executed.
     * If only one task is executed, then the status of that task will be returned;
     * otherwise, the status of the second task executed will be returned.
     **
     * @param env the Turtle shell environment to read any necessary variable values from.
     * @return the status of the task(s) executed (based on whether one or both tasks were executed) -- see above!
     */
    @Override
    public int exec(TEnv env) {
        int result = current.exec(env);
        if (result == 0) {
            result = next.exec(env);
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

        builder.append(current.toString()).append(" || ").append(next.toString());
        return builder.toString();
    }
}
