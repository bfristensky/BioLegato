/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.turtleshell;

import org.turtleshell.strings.TString;
import java.io.Closeable;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

/**
 *
 * @author alvare
 */
public class TurtleFileWriter implements Appendable, Closeable {
    boolean append;
    TString filename;
    File    fileobj = null;
    private FileWriter writer = null;

    public TurtleFileWriter (TString filename, boolean append) {
        this.filename = filename;
        this.append   = append;
    }

    public void open(TEnv env) {
        try {
            System.out.println("Opening file: " + filename.getValue(env));
            if (fileobj == null) {
                fileobj = env.fixpath(filename.getValue(env));
                if (!append && fileobj.exists()) {
                    fileobj.delete();
                }
            }
            writer = new FileWriter(fileobj);
        } catch (IOException ioe) {
            ioe.printStackTrace(System.err);
        }
    }

    public Appendable append(CharSequence csq) throws IOException {
        if (writer != null) {
            writer.append(csq);
            writer.flush();
        }
        return this;
    }

    public Appendable append(CharSequence csq, int start, int end) throws IOException {
        if (writer != null) {
            writer.append(csq, start, end);
            writer.flush();
        }
        return this;
    }

    public Appendable append(char c) throws IOException {
        if (writer != null) {
            writer.append(c);
            writer.flush();
        }
        return this;
    }

    public void close() throws IOException {
        if (fileobj == null) {
            System.err.println("ERROR!  File never opened!!!!");
        }
        if (writer != null) {
            writer.flush();
            writer.close();
            writer = null;
        }
    }

    /**
     * Prints the Turtle shell syntax of the current Turtle shell object
     **
     * @return the appropriate Turtle shell representation of the current Turtle shell object
     */
    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();

        builder.append(" > ").append(filename);
        return builder.toString();
    }

}
