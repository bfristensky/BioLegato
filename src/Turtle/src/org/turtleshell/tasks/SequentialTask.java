/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.turtleshell.tasks;

import org.turtleshell.TEnv;

/**
 * A turtle shell task wrapper for two commands that are executed in sequence.
 * The first command must wait for the second command to finish before it can start.
 **
 * @author Graham Alvare
 * @author Brian Fristensky
 */
public class SequentialTask extends DualTask {
    /**
     * Creates a new sequential task object with two tasks
     **
     * @param current the command to execute first
     * @param next the command to execute second
     */
    public SequentialTask(TTask current, TTask next) {
        super(current, next);
    }

    /**
     * Executes each command, one after the other.
     **
     * @param env the Turtle shell environment to read any necessary variable values from.
     * @return the execution status of the second command.
     */
    @Override
    public int exec(TEnv env) {
        current.exec(env );
        return next.exec(env);
    }

    /**
     * Prints the Turtle shell syntax of the current Turtle shell object
     **
     * @return the appropriate Turtle shell representation of the current Turtle shell object
     */
    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();

        builder.append(current.toString()).append("; ").append(next.toString());
        return builder.toString();
    }
}
