/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.turtleshell.tasks;

import java.io.IOException;

/**
 * An abstract wrapper class for tasks which contain more than one sub-tasks.
 **
 * The original purpose of this wrapper class lies within the append capabilities
 * of each command.  Based on Turtle shell's parser, whenever a task is followed by an
 * &&, ||, ;, or any similar symbol, the parser creates a new object to encapsulate
 * both the first command, and the command after the symbol.  This has a huge caveat
 * for commands piping into the first command (the one preceding the symbol).
 * Therefore, this class was created to uniformly pass along all append calls to
 * the first command.
 **
 * @author Graham Alvare
 * @author Brian Fristensky
 */
public abstract class DualTask extends TTask {
    /**
     * The first task.
     */
    protected TTask current;
    /**
     * The second task.
     */
    protected TTask next;

    public DualTask(TTask current, TTask next) {
        this.current = current;
        this.next    = next;
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
        current.append(csq);
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
        current.append(csq, start, end);
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
        current.append(c);
        return this;
    }

    /**
     * Closes the output stream of the first task ('current')
     */
    @Override
    public void close() {
        current.close();
    }

    /**
     * Used to determine whether the current command is running and able to accept
     * input from another command, or from a file
     **
     * @return whether the command is able to accept input
     */
    public boolean isReady() {
        return current.isReady();
    }
}
