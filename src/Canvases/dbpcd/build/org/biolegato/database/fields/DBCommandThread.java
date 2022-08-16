/*
 * CommandThread.java
 *
 * Created on January 6, 2010, 4:12 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.biolegato.database.fields;

import org.biopcd.parser.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.OutputStreamWriter;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.turtleshell.Turtle;

/**
 * Class used for making abstract actions which launch threads.
 **
 * @author Graham Alvare
 * @author Brian Fristensky
 */
public class DBCommandThread implements ActionListener, Runnable {

    /**
     * A result set for the current dataset entry.
     * The mapings within the result set are as follows:
     *
     * The key is what we will search for when we do a string replace of the command string
     * This is what we perform when we modify the command string, replacing the values of
     * keys with the values of their associated fields.  For example, in the case of:
     *
     * numseq $START $END in1 ......
     *
     * 'in1', '$START' and '$END' would be valid keys within the result set.  These keys will
     * then be mapped to the fields from which values would be obtained.  For instance,
     * if the field for '$START' is set to 10, and the field for '$END' is set to 20,
     * then the command would be as follows:
     *
     * numseq 10 20 bio1431242.tmp .....
     *
     * Note that 'in1' also gets replaced; however, it is not visible.  This is because,
     * in this example, 'in1' is a "file field".  These are objects which create files based
     * on the data within the canvas, and the data selected by the user.  This data gets passed
     * to the program through a file (in this case 'bio1431242.tmp').
     *
     * It is important to note that some file fields can also capture data obtained from
     * running a program, and pass that program into BioLegato.  To find out more about
     * how such file fields work, please look at "TempFile.java" and any additional menu
     * documentation.
     */
    private Map<String,String> fields = null;
    /**
     * The action to perform.
     *
     * Note that this command may contain variables which will be handled by the
     * 'fields' result set.  Please see the commenting for the variable 'fields'
     * for more information.
     */
    private String command = null;
    /**
     * Whether to use the Turtle Shell or the system's native shell for running the command.
     */
    private boolean turtle = true;
    /**
     * This constant is used for serialization
     */
    private static final long serialVersionUID = 7526472295622777001L;
    /**
     * Determines the shell to execute, if we are not using Turtleshell
     */
    private static final String SHELL = (System.getenv("BL_SHELL") != null
            ? System.getenv("BL_SHELL")
            : (PCD.CURRENT_OS == PCD.OS.WINDOWS_9X ? "command.com"
            : (PCD.CURRENT_OS == PCD.OS.WINDOWS_NT ? "cmd.exe" : "/bin/sh")));
    /**
     * Determines the parameter for directing the shell to execute a command,
     * if we are not using Turtleshell
     */
    private static final String SHELL_EXEC_PARAM = (System.getenv("BL_SHELL_PARAM") != null
            ? System.getenv("BL_SHELL_PARAM")
            : (PCD.CURRENT_OS.isWindows() ? "/C" : "-c"));
    /**
     * Whether to operate in debug mode
     */
    private boolean debug;
    
    /**
     * Creates a new instance of CommandThread.
     *
     * This version of the constructor should be used if there are no command
     * variables that need to be handled within the command string.
     **
     * See 'fields' variable for more information about command line variables.
     **
     * @param command the command for the thread to run
     * @param turtle whether to use the Turtle Shell or the system's native shell for running the command.
     */
    public DBCommandThread(String command, boolean turtle) {
        this(command, null, turtle);
    }

    /**
     * Creates a new instance of CommandThread.
     **
     * Please note that this version of the command thread constructor takes
     * a result set called 'fields'.
     *
     * Here is a brief overview of how 'fields' works:
     * A result set containing mapings for all of the fields.
     * The mapings within the result set are as follows:
     *
     * The key is what we will search for when we do a string replace of the command string
     * This is what we perform when we modify the command string, replacing the values of
     * keys with the values of their associated fields.  For example, in the case of:
     *
     * numseq $START $END in1 ......
     *
     * 'in1', '$START' and '$END' would be valid keys within the result set.  These keys will
     * then be mapped to the fields from which values would be obtained.  For instance,
     * if the field for '$START' is set to 10, and the field for '$END' is set to 20,
     * then the command would be as follows:
     *
     * numseq 10 20 bio1431242.tmp .....
     *
     * Note that 'in1' also gets replaced; however, it is not visible.  This is because,
     * in this example, 'in1' is a "file field".  These are objects which create files based
     * on the data within the canvas, and the data selected by the user.  This data gets passed
     * to the program through a file (in this case 'bio1431242.tmp').
     *
     * It is important to note that some file fields can also capture data obtained from
     * running a program, and pass that program into BioLegato.  To find out more about
     * how such file fields work, please look at "TempFile.java" and any additional menu
     * documentation.
     **
     * @param command the command for the thread to run
     * @param fields a result set used for mapping command line variables (see above).
     * @param turtle whether to use the Turtle Shell or the system's native shell for running the command.
     */
    public DBCommandThread(String command, Map<String,String> fields, boolean turtle) {
        this.command = command;
        this.fields = fields;
        this.turtle = turtle;
    }
    
    /**
     * Runs the command.
     **
     * A fairly straightforward method; this method runs the 'run' method
     * as a separate thread.  Please see the 'run' method for more information.
     **
     * @param e ignored by this function
     */
    public void actionPerformed(ActionEvent e) {
        new Thread(this).start();
    }
    
    /**
     * Used for running the command.
     **
     *	This function gathers all of the parameter settings from the fields,
     *	then generates and executes the corresponding command string.
     */
    public void run() {
        String run = command;

        if (fields != null && !fields.isEmpty()) {
            // handle some degree of recursion (i.e. a variable as a value of a variable)
            for (int count = 0; count < 3; count++) {
                run = traverseTree(run, fields);
            }
            run = DBCommandThread.unquote(run);
        }
        if (turtle) {
            shellCommand(run, "");
        } else {
            // execute the program and collect program output
            Turtle.shellCommand(run);
        }
    }
    
    /**
     * Parses the command string, find all of the variable markings (%VAR_NAME%),
     * and replace them with their corresponding variable values.
     *
     * This method is called just before executing a command (via. a run button)
     **
     * @param fields the result set (as a map) containing all of the fields to parse
     * @return the processed command string
     */
    private String traverseTree(String run, Map<String,String> map) {
        if (PCD.debug) {
            System.out.println("Replacing command line arguments");
        }
        for (Map.Entry<String,String> entry : map.entrySet()) {
            if (entry != null && entry.getValue() != null) {
                if (entry.getValue() != null) {
                    run = run.replaceAll("(?i:" + Pattern.quote("%" + entry.getKey() + "%") + ")", Matcher.quoteReplacement(entry.getValue().toString()));
                } else {
                    run = run.replaceAll("(?i:" + Pattern.quote("%" + entry.getKey() + "%") + ")", Matcher.quoteReplacement(""));
                }
            }
        }
        return run;
    }

    /**
     * Quotes a string, so it will not be modified by replaceArguments
     **
     * @param str the string to add quotation to
     * @return the quoted string
     */
    public static String quote(String str) {
        return str.replaceAll(Pattern.quote("%"), Matcher.quoteReplacement("%%"));
    }

    /**
     * Removes quotations added to a string by the 'quote' method
     **
     * @param str the string to remove quotation from
     * @return the "unquoted" string
     */
    public static String unquote(String str) {
        return str.replaceAll(Pattern.quote("%%"), Matcher.quoteReplacement("%"));
    }

    /**
     * Runs simple shell commands.
     * Reroutes all output to the console.
     **
     * @param cmd the command string to run
     * @param data the data to use as standard input (System.in)
     */
    public static void shellCommand(String cmd, String data) {
        Process process = null;                     // the process object obtained on execution
        StringBuilder message = new StringBuilder();	// used for printing debug information
        String[] execute = new String[]{cmd};           // default case - run the command by itself

        /* Ensures that the command will be executed properly as a shell command
         * <p>This function generates a command list for execution.  The command list will contain
         *	the appropriate shell for the current operating system, followed by the "execution-argument",
         *	(whatever flag is required to tell the shell that the rest of the commandline should be executed
         *	by the shell), followed by the command to execute (the variable cmd)</p>
         * <p>Operating system values obtained from http://lopica.sourceforge.net/os.html</p>
         */
        // builds the execution array
        if (SHELL != null) {
            // if there is a shell for the current operating system, execute the command through the shell
            if (SHELL_EXEC_PARAM != null) {
                execute =
                        new String[]{SHELL,
                            SHELL_EXEC_PARAM, cmd};
            } else {
                execute = new String[]{SHELL,
                            cmd};
            }
        }

        // relay the command string the message system
        System.err.println("BioPCD: executing - " + cmd);

        // obtain the process object for the command
        try {
            process = Runtime.getRuntime().exec(execute);

            // ensure the process object is not null
            if (process != null) {
                // passes any data to the program via standard in
                if (data != null) {
                    (new OutputStreamWriter(process.getOutputStream())).write(
                            data);
                }

                // output the program's stdin and stderr to the command prompt
                new Thread(new StreamCopier(StreamCopier.DEFAULT_BUFF_SIZE, process.getInputStream(), System.out)).start();
                new Thread(new StreamCopier(StreamCopier.DEFAULT_BUFF_SIZE, process.getErrorStream(), System.out)).start();

                // display the command's result if debug is enabled
                System.err.println("BioPCD: Command executed successfully, return status: "
                        + process.waitFor());
            }
        } catch (Throwable e) {
            // if there are any errors, print them to the error prompt
            System.err.println("BioLegato: error executing command!");
            e.printStackTrace(System.err);
        }
    }
}
