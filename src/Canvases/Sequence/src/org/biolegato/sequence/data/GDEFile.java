package org.biolegato.sequence.data;
/*
 * GDEFile.java
 *
 * Created on January 30, 2008, 11:58 AM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */
import org.biolegato.sequence.data.Seq.Direction;
import org.biolegato.sequence.data.Seq.Strandedness;
import org.biolegato.sequence.data.Seq.Topology;
import org.biolegato.sequence.data.Seq.Type;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Scanner;
import java.util.regex.*;
import org.biolegato.main.BLMain;
import org.biolegato.main.DataCanvas;

/**
 * This class acts as a parser/translator for standard GDE files.
 **
 * @author Graham Alvare
 * @author Brian Fristensky
 */
public class GDEFile extends DataFormat {
    /**
     * The delimiter for entries in GDE files.
     * (i.e. the { symbol).
     */
    private static final String DELIMITER = "\\{";
    /**
     * The pattern to denote fields within an entry.
     */
    private static final Pattern FIELD_PATTERN
            = Pattern.compile("^(\\w+)\\s+(\".*?\"|[^\"].*?$)",
                Pattern.MULTILINE | Pattern.DOTALL);

    /**
     * Creates a new instance of GDEFile
     */
    public GDEFile () {
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
        // Translate the data
        // NOTE: append is faster than + or concat operators
        if (seq != null) {
            // Print all of the Fields for the GDE file (as one big append-chain
            // statement, with field translation/formatting where necessary).
            //
            // NOTE: this code is messy because writing big GDE files used to be
            //       considerably slow.  Therefore, this code was optimized to
            //       run much faster by using the append command.
            //
            // NOTE: The whitespace after each GDE field is only for decorative
            //       purposes (to make the files neat.
            result.append("{\nname            \"").append(
                    quote(seq.getName())).append(
// TODO: reimplement longname
//		"\"\nlongname        \"").append(
//			quote(seq.getName())).append(
// TODO: reimplement accession numbers
//		"\"\nsequence-ID     \"").append(
//			quote(seq.getAccession())).append(
                "\"\ncreation-date   \""
                    ).append((new SimpleDateFormat("MM/dd/yy kk:mm:ss")
                        ).format(new Date())).append(
                "\"\ndirection       ").append(
                    (Direction.FROM5TO3.equals(seq.getDirection())
                        ? "1" : "-1")).append(
                "\nstrandedness    ").append(
                    (Strandedness.SINGLE.equals(seq.getStrandedness())
                        ? "1" : "2")).append(
                "\ntype            ").append(
                    blTypetoGDE(seq.getType())).append(
                "\n").append((Topology.CIRCULAR.equals(
                    seq.getTopology())
                        ? "circular        1\n" : "")).append(
                "offset          0\ngroupID         ").append(
                    Integer.toString(
                        Math.min(0, seq.getGroupID() - 1))).append(
                "\ncreator         \"").append(quote("")).append(
                "\"\ndescrip         \"").append(
                        quote(seq.getDescription())).append(
// TODO: reimplement comments section?
/*		"\"\ncomments        \"").append(
                    quote(seq.get("comments"))).append(*/
                "\"\nsequence        \"").append(
                        quote(seq.getSequence().toString()),
                            offset, offset + length).append("\"\n}\n");
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
        Matcher matches = null;

        // Ensure usage of the proper delimiter.
        source.useDelimiter(DELIMITER);

        // Translate the data.
        if (source.hasNext()) {
            // The group ID for the GDE entry stored in the file.
            int groupID = 0;
            // The type of data stored in the GDE file.
            Type type = Type.DNA;
            // The name of the current entry in the GDE file.
            String name = "";
            // The data sequence stored in the current GDE entry.
            String sequence = "";
            // The direction of the sequence stored in the current GDE entry.
            Direction direction = Direction.FROM5TO3;
            // The strandedness of the sequence stored in the current GDE entry.
            Strandedness strands = Strandedness.SINGLE;
            // The topology of the sequence stored in the current GDE entry.
            Topology topology = Topology.LINEAR;
            // The description of the sequence stored in the current GDE entry.
            String description = null;
            // Stores the name of the current field being parsed.
            String fname = "";
            // Stores the value of the current field being parsed.
            String fvalue = "";

            // Make sure that there are fields present in the GDE entry.
            if (matches == null) {
                matches = FIELD_PATTERN.matcher(source.next().trim());
            } else {
                matches.reset(source.next().trim());
            }
            
            // Process the names and values for the fields in each GDE entry.
            while (matches.find()) {
                // Get the name and value for the current field to process.
                fname = matches.group(1);
                fvalue = matches.group(2);

                // If the value is quoted, remove the quotation marks from it.
                if (fvalue.startsWith("\"")) {
                    fvalue = fvalue.substring(1, fvalue.length() - 1);
                }

                if ("".equals(fname)) {
                    // SKIP empty fields/lines.
                } else if ("type".equals(fname)) {
                    // Determine the type of data stored in the GDE file.
                    if ("RNA".equalsIgnoreCase(fvalue)) {
                        type = Type.RNA;
                    } else if ("PROTEIN".equalsIgnoreCase(fvalue)) {
                        type = Type.PROTEIN;
                    } else if ("TEXT".equalsIgnoreCase(fvalue)) {
                        type = Type.TEXT;
                    } else if ("MASK".equalsIgnoreCase(fvalue)) {
                        type = Type.MASK;
                    } else {
                        type = Type.DNA;
                    }

                } else if ("groupID".equals(fname)) {
                    // Parse the groupID from a GDE file (if it is numerical).
		    if (DataCanvas.testNumber(fvalue.trim())) {
			try {
			    groupID = Integer.parseInt(fvalue);
			} catch (Exception ex) {
			}
		    }

                } else if ("circular".equals(fname)) {
                    // Parse the topology field (i.e. is the sequence circular).
                    if ("0".equals(fvalue)) {
                        topology = Topology.LINEAR;
                    } else {
                        topology = Topology.CIRCULAR;
                    }

                } else if ("direction".equals(fname)) {
                    // Parse the entry's direction field.
                    if ("-1".equals(fvalue)) {
                        direction = Direction.FROM3TO5;
                    } else {
                        direction = Direction.FROM5TO3;
                    }

                } else if ("strandedness".equals(fname)) {
                    // Parse the entry's strandedness field.
                    if ("1".equals(fvalue)) {
                        strands = Strandedness.SINGLE;
                    } else {
                        strands = Strandedness.DOUBLE;
                    }

                } else if ("name".equals(fname)) {
                    // Parse the entry's name field.
                    name = fvalue;
                    
                } else if ("descrip".equals(fname)) {
                    // Parse the entry's description field.
                    description = fvalue;

                } else if ("sequence".equals(fname)) {
                    // Parse the data sequence represented by the entry.
                    sequence = fvalue.replaceAll("[\\r\\n]", "");
                    
// TODO: reimplement cration date for GDE files
/*              } else if ("creation-date".equals(fname)) {
                    creationDate = new Date(fvalue);*/
// TODO: reimplement creator section for GDE files
/*                } else if ("creator".equals(fname)) {
                    sequence.put("creator", fvalue);*/
// TODO: reimplement accession numbers
/*                } else if ("sequence-ID".equals(fname)) {
                    sequence.setAccession(value);*/
// TODO: reimplement comments section for GDE files
/*                } else if ("comments".equals(fname)) {
                    sequence.put("comments", fvalue);*/
// TODO: reimplement longname section for GDE files
/*              } else if ("longname".equals(fname)) {
                    sequence.put("longname", fvalue);*/

                } else if (BLMain.debug) {
                    // Print a warning if any other fields are present,
                    // notifying the user that the field is unsupported.
                    System.err.println("GDE file parser - Unsupported attribute"
                            + " (" + fname /*+ " = " + fvalue */ + " )");
                }
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
                char[] seqarray = sequence.toCharArray();
                datamodel.insert(x, y, seqarray, 0, seqarray.length, true);
            } else {
                datamodel.add(y, new Seq(type, name, new StringBuffer(sequence),
                        direction, topology, strands, groupID, description));
            }

            // Skip the ending of the entry.
            source.skip("\\}?");
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
     * Determines whether a specified file is of type GDE file
     * (based on extension).  This method is part of the FileFilter interface.
     * Currently the only extension supported is ".gde".
     **
     * @param file the file to test
     * @return true if the file is of type GDE file (otherwise false)
     * @see javax.swing.filechooser.FileFilter#accept
     */
    public boolean accept (File file) {
        // There are two types of file that will be displayed
        // from a file chooser filtered to only show GDE flat-files.
        // These two types of files are:
        //
        // 1. directories
        // 2. files ending with the extension .gde
        return (file.isDirectory() || file.getAbsolutePath().toLowerCase().
                endsWith(".gde"));
    }

    /**
     * Returns a description of the file format that can be displayed to the
     * user.  This method is part of the FileFilter interface.
     **
     * @return the string description of the file format
     * @see javax.swing.filechooser.FileFilter#getDescription
     */
    public String getDescription () {
        return "GDE file (*.gde)";
    }

    /**
     * Quotes a GDE string (replaces " with ' and {} with [])
     **
     * @param input	the string to quote
     * @return the quoted string
     */
    public static String quote (String input) {
        return (input != null ? input.replaceAll("\"", "\'"
                ).replaceAll("\\{", "\\[").replaceAll("\\}", "\\]") : "");
    }

    /**
     * Converts a sequence's type enum to the GDE file format
     **
     * @param type the type enum to convert
     * @return the GDE equivilent
     */
    private static String blTypetoGDE (Type type) {
        String result = "DNA";

        switch (type) {
            case RNA:
                result = "RNA";
                break;
            case PROTEIN:
                result = "PROTEIN";
                break;
            case MASK:
                result = "MASK";
                break;
            case TEXT:
                result = "TEXT";
                break;
        }
        return result;
    }
}
