/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.biolegato.main;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * <p>Stream copier copies the contents of one stream to another.
 * The copying can be multi or single threaded.</p>
 *
 * <p>
 *  An example of how to use the object would be:
 * </p>
 *
 * <code>
 * new Thread(new StreamCopier(StreamCopier.DEFAULT_BUFF_SIZE,
 *                              System.in, System.out)).start();
 * </code>
 * <p>
 *  The above example would copy whatever the contents of System.in
 *  are to System.out.
 * </p>
 **
 * @author Graham Alvare
 * @author Brian Fristensky
 */
public class StreamCopier implements Runnable {
    /**
     * The input stream source for the copy process.
     */
    private InputStream in;
    /**
     * The output stream destination for the copy process.
     */
    private OutputStream out;
    /**
     * The buffer to store data from the input stream before
     * it can be written to the output stream.
     */
    private byte[] buffer;
    /**
     * The default buffer size for copying streams
     */
    public static final int DEFAULT_BUFF_SIZE = 1000;

    /**
     * Creates a new instance of the stream copier object
     **
     * @param buffsize the size to use for the buffer.
     * @param in the input stream source to copy from.
     * @param out the output stream destination to copy to.
     */
    public StreamCopier (int buffsize, InputStream in, OutputStream out) {
        this.in = in;
        this.out = out;
        this.buffer = new byte[buffsize];
    }

    /**
     * <p>This run method does the copying of the source stream to
     * the destination.</p>
     *
     * <p>The reason for the method's name and for using an object
     * as opposed to a static method is because the stream copier
     * can work as a separate thread.  Perhaps the best example of
     * where this is useful would be running a command.  When a
     * command is being run, its output is not printed directly to
     * the console (at least not any way that I am aware of at the
     * time of this writing).  Because the output is instead saved
     * in a stream within a Process object, we must manually copy
     * the data to System.out, etc.  Since there are two important
     * streams we must consider when doing this (stdout and stderr),
     * by multithreading the copy process we can handle both streams
     * simultaneously, and hence the script should appear to run
     * similar to how it would if we were to run it directly at
     * the command promps.</p>
     */
    public void run() {
        // this thread object prints the output from the program
        // algorithm adapted from:
        // http://java.sun.com/docs/books/performance/1st_edition/html/JPIOPerformance.fm.html
        int len = 0;
        
        try {
            // loop until the stream is closed.
            while ((len = in.read(buffer)) != -1) {
                out.write(buffer, 0, len);
            }
        } catch (IOException ioe) {
            ioe.printStackTrace(System.err);
        }
    }
}
