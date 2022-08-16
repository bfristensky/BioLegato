/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.turtleshell.cmd;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.Reader;
import java.nio.channels.FileChannel;
import org.turtleshell.TEnv;

/**
 * A class to copy paths (always recursively).
 *
 * COMMAND LINE ARGUMENTS
 * ----------------------
 * The parameters of this command are as follows:
 *      cp [source path] [destination path]
 *
 * EXECUTION STATUS
 * ----------------
 * This command returns one (1) unless an exception is encountered.  In the case
 * of an exception, this command will return zero (0)
 **
 * @author Graham Alvare
 * @author Brian Fristensky
 */
public class CP {
    /**
     * The main calling method for this command
     **
     * @param args see class description for a list of available command line arguments
     * @return the execution status of this command (see class description)
     */
    public static int main(String[] args) {
        return exec(new TEnv(), args, null, null);
    }

    /**
     * The functional body of this command
     **
     * @param env the Turtle shell environment to read any necessary variable values from.
     * @param args see class description for a list of available command line arguments
     * @param output the standard output for this command
     * @param input the standard input for this command
     * @return the execution status of this command (see class description)
     */
    public static int exec (TEnv env, String[] args, Appendable output, Reader input) {
        int result = 0;

        if (args.length >= 2) {
            copyPath(env, env.fixpath(args[0]), args[1]);
        }

        return result; // general_exec(args);
    }

    /**
     * Copies a path recursively
     **
     * @param env the Turtle shell environment to read any necessary variable values from.
     * @param sourcepath the source path to copy
     * @param destpath the destination path
     */
    public static void copyPath (TEnv env, File sourcepath, String destpath) {
        String subdest = destpath + File.separator + sourcepath.getName();
        File destFile;

        if (sourcepath.isDirectory()) {
            for (File sub : sourcepath.listFiles()) {
                copyPath(env, sub, subdest);
            }
        } else {
            try {
                destFile = env.fixpath(destpath);
                destFile.mkdirs();
                copyFile(sourcepath, destFile);
            } catch (IOException ioe) {
                ioe.printStackTrace(System.err);
            }
        }
    }

    /**
     * File copy method
     * modified from: http://stackoverflow.com/questions/106770/standard-concise-way-to-copy-a-file-in-java
     * Copies a file from sourceFile to destFile
     **
     * @param sourceFile the source file
     * @param destFile the copy destination file
     * @throws IOException any exceptions that occur while copying
     */
    public static void copyFile(File sourceFile, File destFile) throws IOException {
        if(!destFile.exists()) {
            destFile.createNewFile();
        }

        FileChannel source = null;
        FileChannel destination = null;

        try {
            // open the source and destination file channels
            source = new FileInputStream(sourceFile).getChannel();
            destination = new FileOutputStream(destFile).getChannel();

            // transfter data from the source file channel to the destination file channel
            destination.transferFrom(source, 0, source.size());

            // keep executable permissions (if possible)
            destFile.setExecutable(sourceFile.canExecute());
        }
        finally {
            if(source != null) {
                source.close();
            }
            if(destination != null) {
                destination.close();
            }
        }
    }
}
