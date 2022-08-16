package org.biolegato.sequence.data;
/*
 * FastAFile.java
 *
 * Created on January 30, 2008, 11:58 AM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

import java.io.File;
import java.io.IOException;
import java.util.Scanner;


/**
 * <p>FastA file format parser.</p>
 *
 * <p><i>NOTE: this parser will automatically detect whether the sequence
 *       buffer/stream object contains protein, RNA or DNA FastA data.</i></p>
 **
 * @author Graham Alvare
 * @author Brian Fristensky
 */
public class FastAFile extends DataFormat {
    /**
     * The delimiter for sequence entries in FastA files
     * (i.e. the > symbol at the beginning of a line).
     */
    private static final String DELIMITER = "(?m:^)>";

    /**
     * Creates a new instance of FastAFile
     */
    public FastAFile() {
    }

    /**
     * Translates data from the BioLegato's sequence canvas internal format to
     * the given file format.
     **
     * @param  result       the destination Appendable to write the data to.
     * @param  seq          the sequence object to convert.
     * @param  offset       the offset in the sequence to start the conversion.
     * @param  length       the number of characters to convert.
     * @throws IOException  if an error occurs while writing to the destination.
     */
    public void convertTo(Appendable result, Seq seq, int offset, int length)
                                                            throws IOException {
        // Translate the sequence to FastA.
        if (seq != null) {
            result.append(">").append(seq.getName());
            /*
            if (seq.get("description") != null
                    && !"".equals(seq.get("description"))) {
                result.append(" ").append(seq.get("description"));
            }*/
            result.append("\n");
            result.append(seq.getSequence(), offset, offset + length);
            result.append("\n");
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
        // The index (within the variable 'curr') of the first new-line ('\n')
        // character in the sequence (the '\n' character denotes the ending of
        // the name/'>' character line).
        int nlidx;
        // The current string/entry to parse.
        String curr = null;

        // Ensure usage of the proper delimiter (the > character at the
        // beginning of a line).
        source.useDelimiter("(?m:^)>");

        // If there is more FastA data in the stream/scanner, parse it.
        if (source.hasNext()) {
            // Get the next sequence to parse.
            curr = source.next();

            // Remove all comments from the sequence.
            curr.replaceAll("[#;][^\n]*\n", "\n");

            // Determine the index of the first new-line character in the
            // text to be parsed into a sequence.
            nlidx = curr.indexOf('\n');

            // Ensure that there is at least one new-line character in the
            // sequence data to parse.
            if (nlidx >= 0) {
                String prefix = "";
                String description = "";
                String name = curr.substring(0, nlidx).trim();
                StringBuffer sbuff = new StringBuffer(curr.substring(nlidx + 1
                        ).replaceAll("[^A-Za-z\\*\\-]", "").trim());

                // Locate description data, if applicable, with the name field.
                if (name.indexOf(' ') > 0) {
                    description = name.substring(name.indexOf(' ') + 1);
                    name = name.substring(0, name.indexOf(' '));
                }

                // Remove all GenBank '|' fields from the name field,
                // (except the GI number, if applicable).
                if (name.indexOf('|') >= 0) {
                    prefix = name.substring(0, name.indexOf('|'));
                    name = name.substring(name.indexOf('|') + 1);
                    if (name.indexOf('|') >= 0) {
                        name = name.substring(0, name.indexOf('|'));
                    }
                    name = prefix + "|" + name;
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
                    char [] text = new char[sbuff.length()];
                    sbuff.getChars(0, text.length, text, 0);
                    datamodel.insert(x, y, text, 0, text.length, true);
                } else {
                    datamodel.add(y, new Seq(Seq.detectType(sbuff), name,
                                                        sbuff, description));
                }
                // Increment the line counter to the next line.
                y++;
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
        return test.findInLine("(?=" + DELIMITER + ")") != null;
    }
    
    /**
     * Determines whether a specified file is of type FASTA file
     * (based on extension).  This method is part of the FileFilter interface.
     * Currently the only extensions supported are ".wrp", ".fasta", and ".fsa".
     **
     * @param  file the file to test.
     * @return true if the file is of type FastA file (otherwise false).
     * @see    javax.swing.filechooser.FileFilter#accept
     */
    public boolean accept(File file) {
        // There are four types of files that will be displayed
        // from a file chooser filtered to only show FastA files.
        // These four types of files are:
        //
        // 1. directories
        // 2. files ending with the extension .wrp
        // 2. files ending with the extension .fasta
        // 2. files ending with the extension .fsa
        return (file.isDirectory()
                || file.getAbsolutePath().toLowerCase().endsWith(".wrp")
                || file.getAbsolutePath().toLowerCase().endsWith(".fasta")
                || file.getAbsolutePath().toLowerCase().endsWith(".fsa"));
    }

    /**
     * Returns a description of the file format that can be displayed to the
     * user.  This method is part of the FileFilter interface.
     **
     * @return the string description of the file format.
     * @see    javax.swing.filechooser.FileFilter#getDescription
     */
    public String getDescription() {
        return "FastA file (*.wrp,*.fasta,*.fsa)";
    }
}
