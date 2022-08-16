/*
 * PreProcess.java
 *
 * Created on June 23, 2022
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */
package org.biopcd.parser;

import java.io.File;
import java.io.FileReader           ;
import java.io.IOException          ;
import java.io.BufferedReader       ;
import java.io.FileWriter           ;
import java.io.BufferedWriter       ;
import java.util.regex.Pattern      ;
import java.util.Map                ;
import java.util.regex.Matcher      ;

/**
 * This package is for code to pre-process a blmenu file, prior to parsing PCD.
 * 
 * @author Brian Fristensky
 * 
 */
public class PreProcess {
    
    /** This method indents a string with blanks. It does the same thing as
    String.indent(). However, that method was only introduced in Java 12, 
    and we want to be backward compatable to Java 8. In future versions of
    bioLegato, use of strindent should be replaced by String.indent.
    @original string to be indented
    @n number of leading blanks to be added
    @return string left-indented with blanks */
    public static String strindent (String original, int n){
        
        /* blanks is 80 blank characters. We can make a substring of up to 80 blanks to
        use for indentation. */
        final String blanks = "                                                                                ";
        String intstring = original;
        if (n > 0) {
            intstring = blanks.substring(0,n) + intstring; 
        }
        return intstring;
    }

private final static Pattern ENV_VAR_PATTERN =
    //Pattern.compile("\\$\\{([A-Za-z0-9_.-]+)(?::([^\\}]*))?\\}");
    Pattern.compile("\\$([A-Za-z0-9_.-]+)");
/**
 * Substitute environment variables in file paths.
 * 
 * based on https://stackoverflow.com/questions/4752817/expand-environment-variables-in-text
 * <strike>Handle "hello ${var}", "${var:default}" formats for environ variable expansion.</strike>
 */
public static String substituteEnvVars(String text) {
    return substituteEnvVars(text, System.getenv());
}
/**
 * <strike>Handle "hello ${var}", "${var:default}", find var in replaceMap replace value.</strike>
 */
public static String substituteEnvVars(String text, Map<String, ?> replaceMap) {
    StringBuilder sb = new StringBuilder();
    Matcher matcher = ENV_VAR_PATTERN.matcher(text);
    int index = 0;
    while (matcher.find()) {
        sb.append(text, index, matcher.start());
        String var = matcher.group(1);
        Object obj = replaceMap.get(var);
        String value;
        if (obj != null)
            value = String.valueOf(obj);
        else {
            // if no env variable, see if a default value was specified
            value = matcher.group(2);
            if (value == null)
                value = "";
        }
        sb.append(value);
        index = matcher.end();
    }
    sb.append(text, index, text.length());
    return sb.toString();
}    
    
    
    /**
     * Parses out the name of an include file from an @include directive, and
     * writes the contents of that file to the output blmenu file.
     * @param incline line containing an include directive
     * @param sourcedir directory containing the blmenu file
     * @param BW writes lines from the include file to the output blmenu file
     */
    public static void copyInclude(String incline, String sourcedir, BufferedWriter BW) {
      
        final String fullpattern = "^\\s*\\@include\\s+.+";
        final String sep = System.getProperty("file.separator");
        
        if (incline.matches(fullpattern)) {       
            // Parse out the include filename from incline.
            int n = incline.indexOf("@");
            //String indent = strindent(incline,n);
            //System.out.println("indenting: ");
            String [] tokens = incline.trim().split("\\s+");
            String rawfn = tokens[1].replaceAll("\"", "");
            String incfn = substituteEnvVars(rawfn);
            File infile = null;
            if (incfn.indexOf(sep) == -1) { // file in same directory as .blmenu file
                infile = new File(sourcedir + sep + incfn);                    
            }
            else { // fully qualified file path
                infile = new File(incfn);
            }           
            // Write the contents of the include file to the BufferedWriter.
            //System.out.println("copyInclude: Include filename: "+ incfn); 
            if (infile.isFile()) {
                try {
                    FileReader FR= new FileReader(infile);
                    BufferedReader BR = new BufferedReader(FR);
                    String testline = new String();
                    while ((testline = BR.readLine()) != null) {
                        if (testline.matches(fullpattern)) {
                            System.out.println("BioLegato.copyinclude: Recursion detected in " + incfn);
                            System.out.println(testline);
                            System.out.println("Include files may not contain @include directives.");
                        }
                        else {
                            //System.out.println(strindent(testline,n));
                            BW.write(strindent(testline,n));
                            BW.newLine();
                        }
                    }
                    BR.close();  
                }
                catch (IOException e) {
                    e.printStackTrace();   
                }                
            }
            else {
                System.out.println("copyInclude: " + incfn + " not found.");
            }
        }    
        else {
            System.out.println("BioLegato.copyInclude: Poorly structured include line: "+ incline);
        }
    try {    
        BW.flush();  
    }
    catch (IOException bwf) {
        bwf.printStackTrace();  
    }
    }

    /**
     * <p>Inserts PCD code from a file into the blmenu. The motivation for this
     *  is to allow lists for choice_menu or chooser widgets to be
     * maintained in a file. Any time the include file changes, the blmenu 
     * will be updated at run time. This function is similar to the C include directive.</p>
     * 
     * <p>The include file may either be in the same directory as the blmenu file, 
     * or may be a fully qualified file path. The path may include environment variables.
     * For example, the following two lines are equivalent:</p>
     * <pre>
     *          &#64;include "blastdb_n_leftjust.blinclude"
     *          &#64;include "$BIRCH/local/dat/bldna/PCD/Database/blastdb_n_leftjust.blinclude"
     *</pre>
     * 
     *<p>By convention, an include file would have the file extension ".blinclude". However, 
     * this is not enforced, so any legal filename  would be allowed. Include files
     * may NOT have the following filenames: .blmenu, .pcd, .blitem, .biopcd.</p>
     *
     * <p>An include line in the blmenu file must match</p>
     * <pre>
     * {@code
     * [<whitespace>]@include<blank>[<blank>][double quote]path[double quote]
     * }
     * </pre>
     * <p>Recursion in include files is not supported. If an include file
     * contains an &#64;include reference, that line is ignored and an error message
     * is printed to the console.
     * </p>
     **
     * @param rawfile the original blmenu file with include lines.
     * @return completefile the modified blmenu file with includes added.
     */

    public static File includePCD(final File rawfile) {
        File completefile = null;
        final String inclpattern = "^\\s*\\@include.*";
        try {
            completefile = File.createTempFile("bio", ".blmenu");
            FileReader fr = new FileReader(rawfile);
            BufferedReader BR = new BufferedReader(fr);
            FileWriter fw = new FileWriter(completefile);
            BufferedWriter BW = new BufferedWriter(fw);
            
            // If the include file is in the
            // same directory as the .blmenu file, We can find out that directory
            // from rawfile.
            String testline = null;
            String sourcedir = rawfile.getParent();
            //System.out.println("sourcedir: " + sourcedir);
            //System.out.println("===== Reading " + rawfile.toString() + " ========");
            while ((testline = BR.readLine()) != null) {
                if (testline.matches(inclpattern)) {
                    copyInclude(testline, sourcedir, BW);
                }
                else {
                    BW.write(testline);
                    BW.newLine();
                }
            }
            BR.close();
            BW.close();
            completefile.deleteOnExit();

        }
        catch (IOException e) {
            e.printStackTrace();
        }

        return completefile;
    }

//------------------------------ END OF METHOD ------------------------------//

    
}