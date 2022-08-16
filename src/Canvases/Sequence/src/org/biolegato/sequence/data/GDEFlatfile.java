package org.biolegato.sequence.data;
/*
 * GDEFlatfile.java
 *
 * Created on January 30, 2008, 11:58 AM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

import java.io.File;
import java.io.IOException;
import java.util.Scanner;
import java.util.StringTokenizer;
import org.biolegato.sequence.data.Seq.Type;

/**
 * This class acts as a parser/translator for GDE flat files.
 **
 * @author Graham Alvare
 * @author Brian Fristensky
 */
public class GDEFlatfile extends DataFormat {
    /**
     * The delimiter for sequence entries in GDE flat-files.
     * (i.e. the #,%,", or @ symbol at the beginning of a line.
     */
    private static final String DELIMITER = "(?m:^)(?=[#%@\"])";

    /**
     * Creates a new instance of GDEFlatfile
     */
    public GDEFlatfile() {
    }

    /**
     * Translates data from the BioLegato's sequence canvas internal format to
     * the given file format.
     **
     * @param  out          the destination Appendable to write the data to.
     * @param  seq          the sequence object to convert.
     * @param  offset       the offset in the sequence to start the conversion.
     * @param  length       the number of characters to convert.
     * @throws IOException  if an error occurs while writing to the destination.
     */
    public void convertTo(Appendable out, Seq seq, int offset, int length)
                                                            throws IOException {
        // The type character read from the file/stream.
        char type = '#';

        // Translate the data.
        if (seq != null) {
            // Convert the type enumeration to a GDE flat-file character code.
            type = typeToFlatFile((Type) seq.getType());

            // NOTE: append is faster than + or concat operators
            out.append(type).append(seq.getName()).append("\n"
                    ).append(seq.getSequence(), offset, offset + length
                    ).append("\n");
        }
    }

    /**
     * Translates a single sequence from a scanner into BioLegato
     **
     * @param  datamodel    the destination data model to store the translated
     *                      sequence data.
     * @param  source       the data source to parse data from.
     * @param  x            the X-coordinate (character offset) in the data
     *                      model to insert the converted sequence data.
     * @param  y            the Y-coordinate (line) in the data model to insert
     *                      the converted sequence data.
     * @param  addonly      whether to not overwrite any data in the data model,
     *                      by only adding sequence data after the end of the
     *                      data model object.
     * @throws IOException  any exceptions that occur while reading the stream.
     * @return whether there is any remaining data in the source Scanner that
     *         can be parsed by the file format object (invalid data does not
     *         count -- this is why we use a Scanner object, we can look ahead
     *         and search the underlying stream using regular expressions).
     */
    public boolean convertSequence(Dataset datamodel, Scanner source,
                            int x, int y, boolean addonly) throws IOException {
        // Stores the number of sequences in the data model at the time the
        // function is called.
        final int modellength = datamodel.getSize();
        // The current string/entry to parse.
        String parsable = null;

        // Ensure usage of the proper delimiter by the Scanner object.
        source.useDelimiter(DELIMITER);

        // If there is more GDE flat-file data in the stream/scanner, parse it.
        if (source.hasNext()) {
            // Get the next set of sequence data to parse.
            parsable = source.next().trim();

            // Ensure the data stars with a proper GDE character code.
            if (parsable.startsWith("#")
                    || parsable.startsWith("%") || parsable.startsWith("@")
                    || parsable.startsWith("\"")) {
                // Convert the GDE character code to a sequence type.
                Seq.Type type = flatFileToType(parsable.charAt(0));
                // Stores the name of the GDE sequence parsed.
                String name = null;
                // Stores the sequence parsed.
                StringBuffer sequence = new StringBuffer();

                // Ensure that there is a new-line character in the parsable
                // text.  The new-line character denotes the end of the name
                // line in a GDE flat-file.  If the new-line character exists
                // in the parsable text, then load the name and sequence.  If
                // there is no new-line character in the parsable text, load
                // only the sequence name, leaving the sequence, itself, empty.
                if (parsable.indexOf('\n') >= 0) {
                    name = new StringTokenizer(parsable.substring(1,
                            parsable.indexOf('\n'))).nextToken();
                    sequence.append(parsable.substring(parsable.indexOf('\n')
                            + 1).replaceAll("\\s", ""));
                } else {
                    name = parsable.substring(1);
                }

                // If the line number index is past the number of lines in the
                // data model, or the 'addonly' flag are set, then add the new
                // sequence data to the end of the data model; otherwise, insert
                // the sequence into the middle of an existing sequence in the
                // data model.
                //
                // (NOTE: this if-statement is written in reverse of what was
                //        said above, because the case of what was said above
                //        is simpler than its reverse case).
                if (y < modellength && !addonly) {
                    char [] text = new char[sequence.length()];
                    sequence.getChars(0, text.length, text, 0);
                    datamodel.insert(x, y, text, 0, text.length, true);
                } else {
                    datamodel.add(y, new Seq(type, name, sequence));
                }
                // Increment the line counter to the next line.
                y++;
            } else {
                System.err.println("GDE flat file parser - Invalid GDE flat file");
            }
        }
        return source.hasNext();
    }

    /**
     * Used as part of the DataFormat auto-detection algorithm.  This method
     * tests if the data represented in a scanner can be parsed by the current
     * DataFormat object.
     **
     * @param  test the Scanner object to parse and determine if it contains
     *              data which can be parsed by this DataFormat object.
     * @throws IOException  returns any possible I/O exceptions
     *                      (such as mark not supported)
     * @return whether the format can be parsed by this DataFormat object
     */
    @Override
    public boolean isFormat(Scanner test) throws IOException {
        return test.findInLine(DELIMITER) != null;
    }
    
    /**
     * Determines whether a specified file is of type GDE flat file
     * (based on extension).  This method is part of the FileFilter interface.
     * Currently the only extension supported is ".flat".
     **
     * @param file the file to test
     * @return true if the file is of type GDE flat file (otherwise false)
     * @see javax.swing.filechooser.FileFilter#accept
     */
    public boolean accept(File file) {
        // There are two types of file that will be displayed
        // from a file chooser filtered to only show GDE flat-files.
        // These two types of files are:
        //
        // 1. directories
        // 2. files ending with the extension .flat
        return (file.isDirectory() || file.getAbsolutePath().toLowerCase().
                endsWith(".flat"));
    }

    /**
     * Returns a description of the file format that can be displayed to the
     * user.  This method is part of the FileFilter interface.
     **
     * @return the string description of the file format
     * @see javax.swing.filechooser.FileFilter#getDescription
     */
    public String getDescription() {
        return "GDE Flatfile (*.flat)";
    }

    /**
     * <p>Converts a GDE flat file type character into a Type enumeration.</p>
     *
     * <p>The mapping is as follows:</p>
     * <table>
     *      <tr><th>character</th>           <th>sequence type</th></tr>
     *          <tr><td>#</td>               <td>DNA/RNA</td></tr>
     *          <tr><td>%</td>               <td>Protein</td></tr>
     *          <tr><td>@</td>               <td>Colour mask</td></tr>
     *          <tr><td>"</td>               <td>Text</td></tr>
     * </table>
     **
     * @param  test the character to obtain the Type enumeration object for.
     * @return the resulting type enumeration.
     */
    private static Type flatFileToType(char test) {
        Type result = Type.DNA;
        switch (test) {
            case '%':
                result = Type.PROTEIN;
                break;
            case '@':
                result = Type.MASK;
                break;
            case '\"':
                result = Type.TEXT;
                break;
        }
        return result;
    }

    /**
     * <p>Converts a sequence's type enumeration to the GDE flat file format.
     * </p>
     *
     * <p>The mapping is as follows:</p>
     * <table>
     *      <tr><th>character</th>           <th>sequence type</th></tr>
     *          <tr><td>#</td>               <td>DNA/RNA</td></tr>
     *          <tr><td>%</td>               <td>Protein</td></tr>
     *          <tr><td>@</td>               <td>Colour mask</td></tr>
     *          <tr><td>"</td>               <td>Text</td></tr>
     * </table>
     **
     * @param  type the type enumeration to convert to text.
     * @return the GDE flat file text-equivalent.
     */
    private static char typeToFlatFile(Type t) {
        char result = '#';
        switch (t) {
            case PROTEIN:
                result = '%';
                break;
            case MASK:
                result = '@';
                break;
            case TEXT:
                result = '\"';
                break;
        }
        return result;
    }
}
