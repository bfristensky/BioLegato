/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.turtleshell.tasks;

import java.io.IOException;

/**
 * An abstract task to handle and store necessary information for program logic blocks
 **
 * This task object handles generic processes and information required for
 * performing program logic code block operations, such as if-blocks and loops.
 **
 * @author Graham Alvare
 * @author Brian Fristensky
 */
public abstract class LogicTask extends TTask {
    /**
     * The test command
     * ----------------
     * This command will be tested for execution status.  If the execution status is
     * zero, the exec command will be executed.  If this is a loop, the test command
     * will be executed until it returns zero, otherwise, it will only be tested once.
     */
    protected TTask test    = null;
    /**
     * The exec command
     * ----------------
     * This command will be executed when the test case is true (either once,
     * in the case of an if-block, or multiple times in the case of a loop).
     * This command may be a sequence of commands, or just a single command.
     */
    protected TTask exec    = null;

    /**
     * Creates a new program logic abstract task
     **
     * @param test the test condition command for the loop
     * @param exec the loop command (to be executed on each iteration)
     */
    public LogicTask(TTask test, TTask exec) {
        this.test = test;
        this.exec = exec;
    }

    /**
     * Appends data to the standard input of the first task ('current')
     **
     * @param csq the character sequence to append
     * @return the appendable object to append to.
     * @throws IOException any IOExceptions that occur while appending the data
     */
    @Override
    public Appendable append(CharSequence csq) throws IOException {
        exec.append(csq);
        return this;
    }

    /**
     * Appends data to the standard input of the first task ('current')
     **
     * @param csq the character sequence to append
     * @param start the start location within the stream to append
     * @param end the end location within the stream to append
     * @return the appendable object to append to.
     * @throws IOException any IOExceptions that occur while appending the data
     */
    @Override
    public Appendable append(CharSequence csq, int start, int end) throws IOException {
        exec.append(csq, start, end);
        return this;
    }

    /**
     * Appends data to the standard input of the first task ('current')
     **
     * @param c the character to append
     * @return the appendable object to append to.
     * @throws IOException any IOExceptions that occur while appending the data
     */
    @Override
    public Appendable append(char c) throws IOException {
        exec.append(c);
        return this;
    }

    /**
     * Used to determine whether the current command is running and able to accept
     * input from another command, or from a file
     **
     * @return whether the command is able to accept input
     */
    public boolean isReady() {
        return exec.isReady();
    }
}
