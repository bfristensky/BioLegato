/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.turtleshell.tasks;

import org.turtleshell.TEnv;
import java.io.Closeable;
import java.io.IOException;
import java.util.regex.Pattern;

/**
 * A general wrapper for tasks that can be performed in turtle shell
 **
 * @author Graham Alvare
 * @author Brian Fristensky
 */
public abstract class TTask implements Closeable, Appendable {

    /**
     * Whether or not the class must wait to finish execution before returning
     * from exec()
     */
    protected boolean wait = true;
    /**
     * Where any output from the task should be sent
     */
    protected Appendable output = System.out;

    /**
     * Creates a new task object that pipes its output to System.out
     */
    public TTask () {
        this(System.out);
    }

    /**
     * Creates a new task object
     **
     * @param output where to send the output of the task
     */
    private TTask (Appendable output) {
        this.output = output;
    }

    /**
     * Changes where the output from the task should be sent
     **
     * @param out the new appendable object to send the output of the task to.
     */
    public void setoutput(Appendable out) {
        output = out;
    }

    /**
     * Indicated that the task should not be performed sequentially (i.e. that it should
     * be executed as a thread.
     */
    public void bkg() {
        wait = false;
    }

    /**
     * Indicated that the task should be performed sequentially (i.e. that it should NOT
     * be executed as a thread.
     */
    public void forcefg() {
        wait = true;
    }

    /**
     * The execution code for the task
     **
     * @param env the Turtle shell environment to read any necessary variable values from.
     * @return the execution status of the task
     */
    public abstract int exec(TEnv env);

///////////////////////////////////
//*******************************//
//* STREAM MANIPULATION METHODS *//
//*******************************//
///////////////////////////////////

    /**
     * Appends data to the standard input of the task
     **
     * @param csq the character sequence to append
     * @return the appendable object to append to.
     * @throws IOException any IOExceptions that occur while appending the data
     */
    public abstract Appendable append(CharSequence csq) throws IOException;

    /**
     * Appends data to the standard input of the task
     **
     * @param csq the character sequence to append
     * @param start the start location within the stream to append
     * @param end the end location within the stream to append
     * @return the appendable object to append to.
     * @throws IOException any IOExceptions that occur while appending the data
     */
    public abstract Appendable append(CharSequence csq, int start, int end) throws IOException;

    /**
     * Appends data to the standard input of the task
     **
     * @param c the character to append
     * @return the appendable object to append to.
     * @throws IOException any IOExceptions that occur while appending the data
     */
    public abstract Appendable append(char c) throws IOException;

    /**
     * Closes the output stream of the task
     */
    public void close() {
    }

    /**
     * Checks if a string is all digits (i.e. an integer).
     **
     * @param test the string to test.
     * @return true if the string only contains digits.
     */
    public static boolean testNumber(String test) {
        return Pattern.matches("^-?\\d+$", test);
    }

    /**
     * Used to determine whether the current command is running and able to accept
     * input from another command, or from a file
     **
     * @return whether the command is able to accept input
     */
    public abstract boolean isReady();
}
