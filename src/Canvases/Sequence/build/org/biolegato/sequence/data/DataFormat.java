/*
 * DataFormat.java
 *
 * Created on January 30, 2008, 11:54 AM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */
package org.biolegato.sequence.data;

import java.io.IOException;
import java.util.Scanner;
import javax.swing.filechooser.FileFilter;

/**
 * <p>Class to represent reading of data formats.</p>
 *
 * <p>To add new formats, you must not only extend this class, but also add your
 * your format to the variable 'FORMAT_LIST', so it can be displayed to the user
 * in all open/save dialogue boxes.  Also, the 'autodetect' and 'getFormat'
 * methods must be modified to include your new format.</p>
 * 
 * <p>Please note that no further file formats are expected for BioLegato, at
 * this time, because it is assumed that readseq will handle translating all
 * formats not internally supported by BioLegato into FastA or GenBANK, which is
 * supported internally by BioLegato.</p>
 **
 * @author Graham Alvare
 * @author Brian Fristensky
 */
public abstract class DataFormat extends FileFilter {

    /**
     * A static final object used for handling GenBank files.
     **
     * This object is used for JOptionPane file chooser drop downs,
     * and for general file translation
     */
    public static final DataFormat GENBANK = new GenBankFile2008();
    /**
     * A static final object used for handling normal GDE files.
     **
     * This object is used for JOptionPane file chooser drop downs,
     * and for general file translation
     */
    public static final DataFormat GDEFILE = new GDEFile();
    /**
     * A static final object used for handling GDE flat-files.
     **
     * This object is used for JOptionPane file chooser drop downs,
     * and for general file translation
     */
    public static final DataFormat GDEFLAT = new GDEFlatfile();
    /**
     * A static final object used for handling FastA files.
     **
     * This object is used for JOptionPane file chooser drop downs,
     * and for general file translation
     */
    public static final DataFormat FASTA   = new FastAFile();
    /**
     * The list of available file formats
     */
    public static final DataFormat[] FORMAT_LIST = new DataFormat[]{
        GENBANK, GDEFILE, GDEFLAT, FASTA
    };

    /**
     * Creates a new instance of DataFormat
     */
    public DataFormat() {
    }

    /**
     * <p>Converts data from the BioLegato's sequence canvas internal format
     * to the given file format specified by the DataFormat object.</p>
     *
     * <p>This version is a stub method to call the more elaborate version of
     * the method which handles the offset and length parameters (passing an
     * offset value of zero, and a length of -1, representing the entire length
     * of the line in the data model).</p>
     **
     * @param  destination  the destination Appendable to write the data to.
     * @param  datamodel    the data model object to read from.
     * @param  y            the line number to read from the data model.
     * @throws IOException  if an error occurs while writing to the destination.
     */
    public final void convertTo(Appendable destination,
                                Dataset datamodel, int y) throws IOException {
        convertTo(destination, datamodel, y, 0, -1);
    }

    /**
     * <p>Converts data from the BioLegato's sequence canvas internal format
     * to the given file format.</p>
     *
     * <p>This version is a stub method to call the more elaborate version of
     * the method which handles a sequence from the data model directly.</p>
     **
     * @param  destination  the destination Appendable to write the data to.
     * @param  datamodel    the data model object to read from.
     * @param  y            the line number to read from the data model.
     * @param  offset       the X-co-ordinate to start the conversion at.
     * @param  length       the number of characters to read from the 'offset'.
     *                      NOTE: a value less than zero is considered to
     *                            mean the length of the entire sequence on
     *                            line 'y'.  Additionally, values greater than
     *                            the length are also treated to be exactly the
     *                            length of the entire sequence on line 'y'.
     * @throws IOException  if an error occurs while writing to the destination.
     */
    public final void convertTo(Appendable destination, Dataset datamodel,
                            int y, int offset, int length) throws IOException {
        // Extract the line from the data model to convert.
        Seq line = datamodel.getLine(y);

        // ensure that the offset is not less than 0
        offset = Math.max(0, offset);

        // Check the length bounds (if 'length' is less than zero, then set the
        // variable 'length' to the length of the sequence)
        if (length < 0) {
            length = line.getSequence().length();
        } else {
            length = Math.min(length, line.getSequence().length());
        }

        // Convert the sequence.
        DataFormat.this.convertTo(destination, line, offset, length);
    }

    /**
     * <p>Converts data from the BioLegato's sequence canvas internal format
     * to the given file format.</p>
     *
     * <p>This method does the actual conversion (thus, you should override
     * this method for any sub-classes).</p>
     **
     * @param  destination  the destination Appendable to write the data to.
     * @param  data         the sequence object to convert.
     * @param  offset       the offset in the sequence to start the conversion.
     * @param  length       the number of characters to convert.
     * @throws IOException  if an error occurs while writing to the destination.
     */
    public abstract void convertTo(Appendable destination, Seq data,
                                    int offset, int length) throws IOException;

    /**
     * <p>Converts a string from the given file format into the BioLegato
     * internal format.</p>
     *
     * <p>This implementation of the method just calls the more elaborate
 convertSequence (which returns a boolean) until all of the sequences
 are loaded from the stream/reader object (wrapped in a scanner,
 incrementing the value of y every time the convertSequence method is
 called, until convertSequence returns false).</p>
     *
     * <p>Please be sure to set the delimiter for the scanner object before
     *    using it.</p>
     **
     * @param  datamodel    the destination data model to store the converted
     *                      sequence data.
     * @param  data         the Scanner object to parse data from.
     * @param  x            the X-coordinate (character offset) in the data
     *                      model to insert the converted sequence data.
     * @param  y            the Y-coordinate (line) in the data model to insert
     *                      the converted sequence data.
     * @throws IOException  any exceptions that occur while reading the stream.
     */
    public void convertFrom(Dataset datamodel, Scanner data, int x, int y)
                                                            throws IOException {
        // Continually extract sequences from the scanner until there are none
        // left to extract simultaneously increment the y co-ordinate, so the
        // newly extracted sequences will be in sequential order (i.e. in the
        // sequence from which they were extracted from the scanner).
        while (convertSequence(datamodel, data, x, y, false)) {
            y++;
        }
    }

    /**
     * <p>Converts a single sequence from a scanner into BioLegato.</p>
     * <p>Please be sure to set the delimiter for the scanner object before
     *    using it.</p>
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
     * 
     * @return whether there is any remaining data in the source Scanner that
     *         can be parsed by the file format object (invalid data does not
     *         count -- this is why we use a Scanner object, we can look ahead
     *         and search the underlying stream using regular expressions).
     */
    public abstract boolean convertSequence(Dataset datamodel, Scanner source,
                            int x, int y, boolean addonly) throws IOException;

    /**
     * <p>Used as part of the DataFormat auto-detection algorithm.  This method
     *    tests if the data represented in a scanner can be parsed by the
     *    current DataFormat object.</p>
     * <p>Please be sure to set the delimiter for the scanner object before
     *    using it.</p>
     **
     * @param  test the Scanner object to parse and determine if it contains
     *              data which can be parsed by this DataFormat object.
     * @throws IOException  returns any possible I/O exceptions
     *                      (such as mark not supported)
     * @return whether the format can be parsed by this DataFormat object
     */
    protected abstract boolean isFormat(Scanner test) throws IOException;

    /**
     * <p>Used to auto-detect file formats.</p>
     * <p>Please be sure to set the delimiter for the scanner object before
     *    using it.</p>
     **
     * @param  data the string to detect the file format of.
     * @return The resulting sequences.
     */
    public static DataFormat autodetect(Scanner data) throws IOException {
        DataFormat result = DataFormat.GDEFLAT;

        // skip any leading whitespace
        data.skip("[\\s\n\r]*");

        // ensure the data we are using is not null
        if (data != null) {
            // Iterate through all file formats.
            // (I just implemented this as a large if-statement because there
            // are only four (4) file formats, and it is not expected that any
            // further formats will be added to BioLegato).
            if (DataFormat.GENBANK.isFormat(data)) {
                result = DataFormat.GENBANK;
            } else if (DataFormat.GDEFILE.isFormat(data)) {
                result = DataFormat.GDEFILE;
            } else if (DataFormat.FASTA.isFormat(data)) {
                result = DataFormat.FASTA;
            }
        }
        return result;
    }

////////////////////////
//********************//
//* STATIC FUNCTIONS *//
//********************//
////////////////////////
    /**
     * Finds the file type that corresponds to a given GDE/PCD file format
     * hashname (returns the GDE flat file, by default, if not successful).
     **
     * @param  hashname the PCD/GDE hash name to search for.
     * @return The result of the search (returns GDEFLAT on failure).
     */
    public static DataFormat getFormat(String hashname) {
        // The data format corresponding to the string specified
        // (default to GDE flat file if no format matches).
        DataFormat found = DataFormat.GDEFLAT;

        // Find the DataFormat object which corresponds to the hash name.
        // (I just implemented this as a large if-statement because there
        // are only four (4) file formats, and it is not expected that any
        // further formats will be added to BioLegato).
        if ("genbank".equals(hashname)) {
            found = DataFormat.GENBANK;
        } else if ("gde".equals(hashname)) {
            found = DataFormat.GDEFILE;
        } else if ("fasta".equals(hashname)) {
            found = DataFormat.FASTA;
        }
        return found;
    }
}
