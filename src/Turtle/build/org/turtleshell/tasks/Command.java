/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.turtleshell.tasks;

import java.io.BufferedReader;
import java.io.Closeable;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.IOException;
import java.io.PipedReader;
import java.io.PipedWriter;
import java.io.Reader;
import java.io.Writer;
import java.lang.reflect.Method;
import java.util.HashMap;
import org.turtleshell.strings.TString;
import org.turtleshell.cmd.*;
import org.turtleshell.*;

/**
 * A command object for turtle shell.
 * This object executes commands, whether external or internal, which perform basic
 * operations (such as moving files, echoing output, etc.).
 *
 * For a list of internal commands, please see the variable 'internalCommands',
 * and for the parameters of each command, please see each command's class.
 **
 * @author Graham Alvare
 * @author Brian Fristensky
 */
public class Command extends TTask implements Runnable {

    /**
     * The command string for this object to run
     **
     * NOTE: TString objects are used in favour of string objects because of the
     * necessary ability to execute the output of other commands.
     *
     * For example: echo `expr 1 + 1`
     *      This would execute expr 1 + 1, which would return 2
     *      The value 2 would substitute `expr 1 + 1` within the command, and
     *      the final command would be -- echo "2"
     */
    private TString[] cmd;
    /**
     * The standard input stream of the current running instance
     * (of the command represented by this object).
     **
     * This stream is used by this object internally (via threading - see run() method)
     * to pipe input and output to other command objects and standard in.
     */
    private Writer    stdinWriter  = null;
    /**
     * The standard output stream of the current running instance
     * (of the command represented by this object).
     **
     * This stream is used by this object internally (via threading - see run() method)
     * to pipe input and output to other command objects and standard out.
     */
    private Reader    stdoutReader = null;
    /**
     * HashMap containing all of the internal commands recognized by turtle shell,
     * and their corresponding class objects.
     **
     * This is used to make maintenance of internal commands in Turtle easier.
     */
    private static HashMap<String,Class> internalCommands = new HashMap<String,Class>() {
        {
/*            put("cat",    CAT.class);
            put("cd",     CD.class);
            put("cp",     CP.class);
            put("copy",   CP.class);
            put("cut",    CUT.class);
            put("date",   DATE.class);
            put("echo",   ECHO.class);
            put("expr",   EXPR.class);
            put("fgrep",  FGREP.class);
            put("grep",   GREP.class);
            put("head",   HEAD.class);
            put("man",    MAN.class);
            put("mkdirs", MKDIRS.class);
            put("mkdir",  MKDIRS.class);
            put("mv",     MV.class);
            put("paste",  PASTE.class);
            put("pwd",    PWD.class);
            put("rm",     RM.class);
            put("rev",    REV.class);
            put("rsort",  RSORT.class);
            //put("sed",    SED.class);
            put("sort",   SORT.class);
            //put("tac",    TAC.class);
            put("tail",   TAIL.class);
            put("test",   TEST.class);
            put("[",      TEST.class);
            put("tr",     TR.class);
            put("uniq",   UNIQ.class);
            put("wc",     WC.class);*/
        }
    };
    /**
     * A "blank reader" dummy class.  This reader is used for situations in which
     * providing a valid reader object is necessary or useful; however, the reader
     * to be provided should NOT provide any data.  This reader always returns -1
     * and its close method does not do anything.
     */
    private Reader BLANK_READER = new Reader() {
        /**
         * Does nothing except return -1 (i.e., a failed read by the Reader
         * interface definition of the read method.
         */
        @Override
        public int read(char[] cbuf, int off, int len) throws IOException {
            return -1;
        }

        /**
         * Does nothing
         */
        @Override
        public void close() throws IOException {}

    };

    /**
     * Creates a general (blank) command object -- a NOP!
     */
    public Command () {
        this(TString.NULL_TSTRING);
    }

    /**
     * Creates a command object defined by a command with parameters
     **
     * @param cmd the command and its parameters
     */
    public Command (TString [] cmd) {
        super();
        this.cmd = cmd;
    }

    /**
     * Executes the command.
     **
     * This branches off either directly to the method 'run_command()' if the command
     * is supposed to wait until finished execution; or it will run 'run_command()'
     * in a thread, if not.
     **
     * Synonym for exec_pipe(false);
     **
     * @param env the Turtle shell environment to read any necessary variable values from.
     * @return the execution status of the command, or zero if the command is running in a separate thread.
     */
    public int exec(TEnv env) {
        return exec_pipe(env, false);
    }

    /**
     * Executes the command.
     **
     * This branches off either directly to the method 'run_command()' if the command
     * is supposed to wait until finished execution; or it will run 'run_command()'
     * in a thread, if not.
     **
     * @param env the Turtle shell environment to read any necessary variable values from.
     * @param pipeInput whether to pipe input from standard in
     * @return the execution status of the command, or zero if the command is running in a separate thread.
     */
    private int exec_pipe(final TEnv env, final boolean pipeInput) {
        int result = 1;

        if (output instanceof TurtleFileWriter) {
            ((TurtleFileWriter)output).open(env);
        }
        if (wait) {
            if (output instanceof TTask) {
                exec_bg(env, pipeInput);
                result = ((Command)output).exec_pipe(env, true);
            } else {
                result = run_command(env, pipeInput);
            }
        } else {
            if (output instanceof TTask) {
                new Thread(new Runnable() {
                    public void run() {
                        exec_bg(env, pipeInput);
                        ((Command)output).exec_pipe(env, true);
                    }
                }).start();
            } else {
                exec_bg(env, pipeInput);
            }
        }

        return result;
    }

    /**
     * Executes the command in the background.
     * This branches off either directly to the method 'run_command()' if the command
     * is supposed to wait until finished execution; or it will run 'run_command()'
     * in a thread, if not.
     **
     * @param env the Turtle shell environment to read any necessary variable values from.
     * @param pipeInput whether to pipe input from standard in
     * @return the execution status of the command, or zero if the command is running in a separate thread.
     */
    private void exec_bg(final TEnv env, final boolean pipeInput) {
        // handle background processes via multithreading
        new Thread(new Runnable() {
            public void run() {
                run_command(env, pipeInput);
            }
        }).start();
    }


    /**
     * This method runs actual commands.  It has two branch options;
     *  1. run internal commands
     *  2. run external commands
     * In either case, the output is piped within the turtle shell system.
     **
     * @param env the Turtle shell environment to read any necessary variable values from.
     * @param pipeInput whether to pipe input from standard in
     * @return the execution status of the command
     */
    private int run_command(TEnv env, boolean pipeInput) {
        int result = 0;
        String exec_cmd  = cmd[0].getValue(env);
        Thread transferStreams = new Thread(this);

        try {
            if (exec_cmd.contains("=") || (cmd.length > 1 && cmd[1].getValue(env).startsWith("="))) {
                int index;
                String name;
                String value;
                String cmd_full = "";

                for (String str : shellStringToRealString(env, cmd)) {
                    cmd_full += str + " ";
                }
                index = cmd_full.indexOf('=');
                name = cmd_full.substring(0, index);
                value = cmd_full.substring(index);
                env.put(name, value);
                result = 1;
            } else if(internalCommands.containsKey(exec_cmd)) {
                //////////////////////////////
                // handle internal commands //
                //////////////////////////////
                // set up the streams for I/O piping
                stdoutReader  = new PipedReader();
                stdinWriter   = new PipedWriter();

                // create the linked piped streams for I/O piping
                Writer stdoutp = new PipedWriter((PipedReader)stdoutReader);
                Reader stdinp  = new PipedReader((PipedWriter)stdinWriter);

                // obtain the command line arguments
                String[] args = shellStringGetArgsString(env, cmd);

                if (!pipeInput) {
                    stdinWriter.close();
                    stdinp.close();
                    stdinp = BLANK_READER;
                }

                // start the stream I/O transfer thread
                transferStreams.start();

                // execute the command
                Class c = internalCommands.get(exec_cmd);
                Method m = c.getMethod("exec", new Class[] { TEnv.class, String[].class, Appendable.class, Reader.class});
                result = (Integer) m.invoke(null, env, args, stdoutp, stdinp);

                stdoutp.flush();

                // close the piped output stream
                stdoutp.close();
            } else {
                String[] commandArray = shellStringToRealString(env, cmd);
                // debug
                for (String s : commandArray) {
                    commandLine += "\"" + s + "\" ";
                }
                System.out.println("EXECUTING: " + commandLine + " (" + pipeInput + ")"
                        + "\n    APPENDING TO: " + (output != System.out ? output : "System.out"));
                System.out.flush();
                System.out.flush();

                System.out.println("    PATH: " + env.fixpath("."));
                /*System.out.println("    ENV:");
                for (String s : env.getenvp()) {
                    System.out.println("        " + s);
                }*/
                // end debug

                //////////////////////////////
                // handle external commands //
                //////////////////////////////
                // execute the program
                Process p = Runtime.getRuntime().exec(commandArray, env.getenvp(), env.fixpath("."));

                // get the streams for input and output
                stdinWriter  = new OutputStreamWriter(p.getOutputStream());
                stdoutReader = new InputStreamReader(p.getInputStream());

                new Thread(new StreamCopier(p.getErrorStream(), System.err)).start();

                if (!pipeInput) {
                    p.getOutputStream().close();
                }

                // start the stream I/O transfer thread
                transferStreams.start();

                // wait for the external process to exit
                result = p.waitFor();
            }
            // join the stream I/O transfer thread with the current thread
            transferStreams.join();
        } catch (Exception ex) {
            ex.printStackTrace(System.err);
        }

        return result;
    }

    String commandLine = "";

    /**
     * The stream I/O transfer thread body
     */
    public void run () {
        String line;

        try {
            if (stdoutReader != null) {
                BufferedReader stdoutBuffer = new BufferedReader(stdoutReader);
                if (output instanceof TTask) {
                    while (!((TTask)output).isReady()) {
                        // wait!
                    }
                }
                while ((line = stdoutBuffer.readLine()) != null) {
                    output.append(line).append("\n");
                    // debug
                    System.out.println("   appending");
                    System.out.println("       line: " + line);
                    System.out.println("       from: " + commandLine);
                    System.out.println("       to:   " + output);
                    // end debug
                }
                // debug
                System.out.println("closing: " + commandLine);
                // end debug
                if (output instanceof Closeable && output != System.out) {
                    ((Closeable)output).close();
                }
                stdoutReader.close();
                stdoutReader = null;
            }
        } catch (Exception ex) {
            ex.printStackTrace(System.err);
        }
    }

    /**
     * Appends data to the standard input of the task
     **
     * @param csq the character sequence to append
     * @return the appendable object to append to.
     * @throws IOException any IOExceptions that occur while appending the data
     */
    @Override
    public Appendable append(CharSequence csq) throws IOException {
        return stdinWriter.append(csq);
    }

    /**
     * Appends data to the standard input of the task
     **
     * @param csq the character sequence to append
     * @param start the start location within the stream to append
     * @param end the end location within the stream to append
     * @return the appendable object to append to.
     * @throws IOException any IOExceptions that occur while appending the data
     */
    @Override
    public Appendable append(CharSequence csq, int start, int end) throws IOException {
        return stdinWriter.append(csq, start, end);
    }

    /**
     * Appends data to the standard input of the task
     **
     * @param c the character to append
     * @return the appendable object to append to.
     * @throws IOException any IOExceptions that occur while appending the data
     */
    @Override
    public Appendable append(char c) throws IOException {
        return stdinWriter.append(c);
    }

    /**
     * Closes the output stream of the task
     */
    @Override
    public void close() {
        try {
            ((Closeable)stdinWriter).close();
            stdinWriter = null;
        } catch (Exception e) {
            e.printStackTrace(System.err);
        }
    }

    /**
     * Convert a turtle shell string array into an array of java strings
     **
     * @param env the Turtle shell environment to read any necessary variable values from.
     * @param convert the turtle shell string array to convert
     * @return the resulting java string array
     */
    private static String[] shellStringToRealString (TEnv env, TString[] convert) {
        String[] result = new String[convert.length];

        for (int count = 0; count < convert.length; count++) {
            if (convert[count] != null) {
                result[count] = convert[count].getValue(env);
            } else {
                result[count] = "";
            }
        }
        return result;
    }

    /**
     * Convert a turtle shell string array into an array of java strings; HOWEVER,
     * unlike "shellStringToRealString()", we skip the first index of the array,
     * because we are ONLY interested in the argument portion of the command!
     **
     * @param env the Turtle shell environment to read any necessary variable values from.
     * @param convert the turtle shell string array to convert
     * @return the resulting java string array
     */
    private static String[] shellStringGetArgsString (TEnv env, TString[] convert) {
        String[] result = new String[convert.length - 1];

        for (int count = 1; count < convert.length; count++) {
            if (convert[count] != null) {
                result[count - 1] = convert[count].getValue(env);
            } else {
                result[count - 1] = "";
            }
        }
        return result;
    }

    /**
     * Used to determine whether the current command is running and able to accept
     * input from another command, or from a file
     **
     * @return whether the command is able to accept input
     */
    public boolean isReady() {
        return stdinWriter != null;
    }

    /**
     * Prints the Turtle shell syntax of the current Turtle shell object
     **
     * @return the appropriate Turtle shell representation of the current Turtle shell object
     */
    @Override
    public String toString() {
        StringBuilder result = new StringBuilder();

        for (TString c : cmd) {
            result.append(c.toString()).append(" ");
        }

        if (!wait) {
            result.append("&");
        }

        if (output != null && output != System.out) {
            if (output instanceof Command) {
                result.append("|").append(output);
            } else {
                result.append(">").append(output);
            }
        }
        return result.toString();
    }
}
