package org.biolegato.sequence.data;
/*
 * GenBankFile.java
 *
 * Created on January 30, 2008, 11:58 AM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

import org.biolegato.sequence.data.Seq.Type;
import org.biolegato.sequence.data.Seq.Topology;
import org.biolegato.sequence.data.Seq.Strandedness;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Scanner;
import java.util.regex.*;
import org.biolegato.sequence.data.Seq.Direction;

/**
 * This class acts as a parser/translator for GenBank files (using the file
 * specification current to 2008).
 * 
 * The SEQUENCE field may contain any alphabetical character or the gap character
 * ie. [a-zA-Z-]
 **
 * @author Graham Alvare
 * @author Brian Fristensky
 */
public class GenBankFile2008 extends DataFormat {
    /**
     * The delimiter for data entries in GenBANK files
     * (i.e. the word "LOCUS" at the beginning of a line).
     */
    private static final String DELIMITER      = "(?im:(?=^LOCUS))";
    /**
     * A string of whitespace, used for padding sequence names
     * (because GenBANK files are character position-sensitive).
     */
    private final static String SPACEPAD
            = "                                 ";
    /**
     * A regular expression pattern used to help read the locus tag line.
     * (the locus tag line is the first line in a GenBANK file).
     */
    private static final Pattern LOCUS_PATTERN
            = Pattern.compile("^LOCUS\\s+([^\\s]+)\\s+(?:\\d+\\s+)?"
            + "(aa|bp\\s+[\\w-]+)\\s+(circular\\s+|linear\\s+)?",
            Pattern.CASE_INSENSITIVE);

    /**
     * Creates a new instance of GenBankFile2008
     */
    public GenBankFile2008() {
    }

    /**
     * Converts data from the BioLegato's sequence canvas internal format to
     * the given file format. If the sequence was read from a GenBank file, 
     * the original annotation has been saved unmodified, so we write that. 
     * If a sequence was read from a file in some format other than GenBank (eg.
     * fasta), we write a pseudo-GenBank file containing the data available.
     **
     * @param  result       the destination Appendable to write the data to.
     * @param  seq          the sequence object to convert.
     * @param  offset       the offset in the sequence to start the conversion.
     * @param  length       the number of characters to convert.
     * @throws IOException  if an error occurs while writing to the destination.
     */
    public void convertTo(Appendable result, Seq seq, int offset, int length)
                                                            throws IOException {
        // The length of the name of the sequence (used for space padding).
        int namelength;
        // The total length of the original sequence.
        int sequenceLength;
        // A StringBuffer to store the sequence data to print.
        StringBuffer sequence;
        // Stores the maximum position within the sequence to print.
        final int sequencemax = offset + length;

        // Convert the data
        // NOTE: append is faster than + or concat operators
        if (seq != null && offset >= 0 && length >= 0) {
            // Obtain the sequence characters to print.
            sequence = seq.getSequence();
            sequenceLength = Math.min(sequencemax, sequence.length());

            // If there is an original field in the sequence object, use that
            // instead of a fake generated BioLegato GenBANK header.
            if (seq.getOriginal() == null || offset != 0
                    || length != sequence.length()) {
                // Begin the locus line.
                result.append("LOCUS       ");

                // Append the name to the stream.  Note that we ensure this
                // field is 16 characters long with right-side padding of
                // spaces if necessary.
                namelength = seq.getName().length();
                if (namelength < 16) {
                    result.append(seq.getName()).append(SPACEPAD,
                            0, 16 - namelength);
                } else {
                    result.append(seq.getName().substring(0, 16));
                }

                // Print the length of the sequence stored in the file.
                result.append(" ").append(String.format("%11d", length));

                // Print the strandedness of the nucleotide data in the GenBANK
                // file (if it is not a protein GenBANK file).
                if (!Type.PROTEIN.equals(seq.getType())) {
                    result.append(" bp ");
                    /// Print the strandedness if it is set for the sequence.
                    if (seq.getStrandedness() != null) {
                        // Converts a sequence's strandedness enumeration to
                        // GenBANK's string representation format.
                        switch (seq.getStrandedness()) {
                            case DOUBLE:
                                result.append("ds");
                                break;
                            case MIXED:
                                result.append("ms");
                                break;
                            default:
                                result.append("ss");
                                break;
                        }
                        
                        // Append a dash and the type of sequence
                        // (i.e. DNA or RNA)
                        result.append("-");
                        result.append((!Type.RNA.equals(seq.getType())
                                ? "DNA     " : "RNA     "));
                    } else {
                        result.append((!Type.RNA.equals(seq.getType())
                                ? "DNA        " : "RNA        "));
                    }
                } else {
                    result.append(" aa            ");
                }

                // Print the topology of the sequence.
                result.append((!Topology.CIRCULAR.equals(seq.getTopology())
                        ? "linear   " : "circular "));

// TODO: reimplement classification reading abilities for BioLegato
/*                if (seq.containsKey("classification")) {
                result.append(seq.get("classification")).append(" ");
                } else {*/

                // Division code + space.
                result.append("CON ");
//                }
                
                // Print the time the GenBANK file was generated.
                result.append((new SimpleDateFormat("dd-MMM-yyyy")).format(
                        new Date()).toUpperCase());
                result.append("\n");

                // Print the description field for the sequence.
                if (!"".equals(seq.getDescription())) {
                    result.append("DESCRIPTION ").append(
                            seq.getDescription()).append("\n");
                }
            } else {
                // We have to remove the last newline from the end of the annotation.
                // Apparently, the append function adds a newline, so if we don't do
                // this, an extra blank line appears between the end of the annotation
                // and the ORIGIN line.
                result.append(seq.getOriginal().subSequence(0, seq.getOriginal().length()-1));
            }
            // Print the ORIGIN line and begin writing the sequence.
            // NOTE:  GenBANK sequences have a special format involving spacing,
            //        numbering, and a limited number of characters per row.
            result.append("ORIGIN");

            // Print the sequence (60 characters per row).
            for (int count = offset; count < sequenceLength; count += 60) {
                // Print the sequence position counter.
                result.append("\n").append(String.format("%9d", (count + 1)));

                // Print the spacing and sequence characters.
                for (int spaceCount = count, next = count + 10;
                        spaceCount < count + 60 && spaceCount < sequenceLength;
                        spaceCount = next, next += 10) {
                    result.append(" ").append(sequence, spaceCount,
                            // math.min
                            (sequenceLength < next ? sequenceLength : next));
                }
            }
            result.append("\n//\n"); // Two right slashes denote end of a GenBank entry
        }
    }

    /**
     * Converts a single sequence from a scanner into BioLegato
     **
     * @param  datamodel    the destination data model to store the converted
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
        // Separate the header from the sequence.
        String[] parray = null;
        // A string matcher to match the locus line fields.
        Matcher locusMatcher = null;

        // Ensure usage of the proper delimiter.
        source.useDelimiter(DELIMITER);

        // Make sure that there are GenBank records to parse.
        if (source.hasNext()) {
            parray = source.next().split("ORIGIN[^\n\r]*");

            // Ensure that there is a header and sequence for the GenBANK file.
            if (parray.length >= 1) {
                // Configure the parser to parse the locus line.
                if (locusMatcher == null) {
                    locusMatcher = LOCUS_PATTERN.matcher(parray[0]);
                } else {
                    locusMatcher.reset(parray[0]);
                }

                // Read the data on the locus line.
                if (locusMatcher.find()) {
                    // Begin a StringBuffer to start reading the sequence.
                    StringBuffer sequencebuffer = new StringBuffer("");

                    // If the ORIGIN tag is present in the GenBANK file, then
                    // parse the ORIGIN sequence data into a StringBuffer.
                    if (parray.length >= 2) {
                        sequencebuffer = new StringBuffer(
                                // The NCBI GenBank Release Notes do not specify which
                                // characters are legal in a SEQUENCE. Apparently, a gap
                                // character is okay. So we add gap as a legal character.
                                //parray[1].replaceAll("[^a-zA-Z]", ""));
                                parray[1].replaceAll("[^a-zA-Z-]", ""));
                    }

                    // If the line number index is past the number of lines in
                    // the data model, or the 'addonly' flag are set, then add
                    // the new sequence data to the end of the data model;
                    // otherwise, insert the sequence into the middle of an
                    // existing sequence in the data model.
                    //
                    // (NOTE: this if-statement is written in reverse order
                    //        of what is stated above).
                    if (y < modellength && !addonly) {
                        // INSERT the sequence data into the canvas.
                        // NOTE: no further locus tag parsing needs to take
                        // place if we are inserting the sequence into the
                        // canvas, since insertions only involve the sequence
                        // itself, and no metadata (such as type, etc.)
                        char [] text = new char[sequencebuffer.length()];
                        sequencebuffer.getChars(0, text.length, text, 0);
                        datamodel.insert(x, y, text, 0, text.length, true);
                    } else {
                        // ADD a new sequence to the end of the data model.
                        // begin by reading further information from the locus
                        // line.
                        Type type = Type.DNA;
                        Direction direction = Direction.FROM5TO3;
                        Strandedness strandedness = Strandedness.SINGLE;
                        Topology topology = (locusMatcher.group(3) != null
                                && "circular".equals(locusMatcher.group(3
                                    ).toLowerCase().trim())
                                ? Seq.Topology.CIRCULAR : Seq.Topology.LINEAR);
                        String name = locusMatcher.group(1);
                        String type_text = locusMatcher.group(2).toLowerCase();
                        StringBuilder original
                                = new StringBuilder(parray[0]).append("\n");

                        // Determine if the sequence is protein or nucleic acid.
                        // If the sequence is nucleic acid, determine the
                        // strandedness, and use toType to determine if the
                        // sequence is RNA or DNA.
                        if ("aa".equals(type_text)) {
                            type = Type.PROTEIN;
                        } else if (type_text.startsWith("bp")) {
                            int dashidx = type_text.indexOf('-');
                            if (dashidx >= 0) {
                                strandedness = toStrandedness(
                                        type_text.substring(3, dashidx).trim());
                                type = toType(type_text.substring(dashidx + 1));
                            } else {
                                type = toType(type_text.substring(3));
                            }
                        }

                        // Add the sequence to the canvas.
                        datamodel.add(y, new Seq(type, name, sequencebuffer,
                                direction, topology, strandedness, original));
                    }
                    // Increment the line counter to the next line.
                    y++;
                }
            } else {
                System.out.println("ERROR READING GENBANK SEQUENCE: " + parray);
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
     * Determines whether a specified file is of type GenBank file
     * (based on extension).  This method is part of the FileFilter interface.
     * Currently the only extensions supported are ".gen", ".gp", and ".gb".
     **
     * @param file the file to test
     * @return true if the file is of type GenBank file (otherwise false)
     * @see javax.swing.filechooser.FileFilter#accept
     */
    public boolean accept(File file) {
        // There are four types of files that will be displayed
        // from a file chooser filtered to only show GenBANK files.
        // These four types of files are:
        //
        // 1. directories
        // 2. files ending with the extension .gen
        // 2. files ending with the extension .gp
        // 2. files ending with the extension .gb
        return (file.isDirectory()
                || file.getAbsolutePath().toLowerCase().endsWith(".gen")
                || file.getAbsolutePath().toLowerCase().endsWith(".gp")
                || file.getAbsolutePath().toLowerCase().endsWith(".gb"));
    }

    /**
     * Returns a description of the file format that can be displayed to the
     * user.  This method is part of the FileFilter interface.
     **
     * @return the string description of the file format
     * @see javax.swing.filechooser.FileFilter#getDescription
     */
    public String getDescription() {
        return "GenBank file (*.gb,*.gp,*.gen)";
    }

    /**
     * Used to convert GB's strandedness to BioLegato's strandedness structure
     **
     * @param string the string to convert.
     * @return the strandedness corresponding to the string parameter
     */
    private static Strandedness toStrandedness(String test) {
        Strandedness result = Strandedness.MIXED;
        if ("ss".equalsIgnoreCase(test)) {
            result = Strandedness.SINGLE;
        } else if ("ds".equalsIgnoreCase(test)) {
            result = Strandedness.DOUBLE;
        }
        return result;
    }

    /**
     * Used to convert GB's sequence type to BioLegato's type structure
     **
     * @param string the string to convert.
     * @return the type corresponding to the string parameter
     */
    private static Type toType(String string) {
        Type result = Type.DNA;
        if (string.toLowerCase().contains("rna")) {
            result = Type.RNA;
        }
        return result;
    }
}
