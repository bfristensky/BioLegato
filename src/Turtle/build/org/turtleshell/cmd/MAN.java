/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.turtleshell.cmd;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import org.turtleshell.TEnv;

/**
 * MAN - Turtle shell's manual page command
 **
 * The parameters of this command are as follows:
 *
 *      man "command"
 *          where "command" is any valid turtle shell command
 *          This prints the manual page for that commands
 *
 *      man commands
 *          This will print a list of valid turtle shell commands
 *
 *      man syntax
 *          This will print a manual page for turtle shell's syntax
 **
 * @author Graham Alvare
 * @author Brian Fristensky
 */
public class MAN {
    /**
     * The main calling method for this command
     **
     * @param args see class description for a list of available command line arguments
     * @return the execution status of this command (see class description)
     */
    public static int main(String[] args) {
        return exec(null, args, System.out, null);
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
        String line;
        String manpage = "MAN";
        InputStream stream;
        BufferedReader sread;

        if (args.length > 0) {
            manpage = args[0];
        }
        try {
            stream = MAN.class.getResourceAsStream(File.separator + "manpages" + File.separator + manpage.toUpperCase() + ".man");
            if (stream != null) {
                sread = new BufferedReader(new InputStreamReader(stream));
                while ((line = sread.readLine()) != null) {
                    output.append(line).append("\n");
                }
                result = 1;
            } else {
                output.append("COMMAND '").append(args[0]).append("' DOES NOT HAVE A MANPAGE!\n");
            }
        } catch (Exception ex) {
            ex.printStackTrace(System.err);
        }
        return result; // general_exec(exec_cmd);
    }
}
