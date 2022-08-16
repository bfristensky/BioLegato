/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.biolegato.sequence.canvas;

import java.awt.Color;
import java.awt.Graphics;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.LineNumberReader;
import javax.swing.filechooser.FileFilter;
import org.biolegato.main.DataCanvas;

/**
 * <p><u>Colour mask support file</u></p>
 *
 * <p>The file contains many methods and constants for colour mask support in
 * SequenceTextArea objects.  The methods and constants are outlined below:</p>
 *
 * <p>To read in colour masks, use EITHER of the below class methods:
 * <ol>
 *      <li>public static ColourMask readCharMaskFile (File file)</li>
 *                           <!--  ---- OR ----  -->
 *      <li>public static ColourMask readPosMaskFile  (File file)</li>
 * </ol></p>
 *
 * <p><b><i>DON'T USE THE CONSTRUCTOR DIRECTLY FROM OUTSIDE THIS CLASS!!!</i>
 * </b></p>
 *
 * <p><u>The two main types of colour masks:</u></p>
 * <!--  ----------------------------------- -->
 * <ol>
 *   <li><dl><dt>Character-based colour masks</dt>
 *      <dd>These colour masks determine the colour of each character based on
 *      the letter code of the character.  For example if a character is A, the
 *      colour could be Green, etc.</dd></dl></li>
 *   <li><dl><dt>Position-based colour masks</dt>
 *      <dd>Unlike character-based colour masks, position-based colour masks
 *      determine the colour of the character based on the position of the
 *      character within a string.  For example, if a character is in the 3rd
 *      position of a string, it could be green regardless of whether the
 *      character is A or B.</dd></dl></li>
 * </ol>
 *
 * <p><u>Character based colour format specification</u></p>
 * <!--  ------------------------------------------- -->
 * <p>The character based file format parsed by this class is the BioLegato
 * character based colour mask format.  It is defined as follows:</p>
 * <ol>
 *     <li>The colour mask name is determined from the file name.</li>
 *     <li>All filenames should have the extension.csv; however, any file may be
 *     read in, regardless of extension.</li>
 *     <li>All files are tab delimited (all whitespace represented within this
 *     specification are tabs)</li>
 *     <li>Each file begins with the line "aa    colour"</li>
 *     <li>Each line after the first contains in the left column a list of
 *     characters (non-delimited), and an HTML-style colour (6 digit hexadecimal
 *     number preceded by '#') in the right hand column.  The semantics of the
 *     file indicate that every line using this format will be interpreted as
 *     telling BioLegato to use the colour in the right column for all of the
 *     characters in the left column.</li>
 * </ol>
 *
 * <p>An example of the above format:</p>
 * <code>
 * aa   colour
 * abc  #FFFFFF
 * k    #ACEED0
 * yz   #040404
 * </code>
 **
 * @author Graham Alvare
 * @author Brian Fristensky
 */
public final class ColourMask {
    /**
     * The type of mask represented by this ColourMask object.
     */
    private MaskType type;
    /**
     * The list of colours for the mask.
     */
    private Color[] colours;
    /**
     * The name of the colour mask (used for displaying to the user).
     */
    private String name;

    /**
     * <p>These are the default colours specified in GDE colour mask files.</p>
     * 
     * <p>The format of a GDE colour mask file is indicated by numbers, which in
     * this implementation of the format correspond to indices in the hash.</p>
     */
    public static final Color[] STDCOLOURS = new Color[]{
        /*
         * DEFAULT GDE COLOURS
         */
        // Red
        new Color(255, 0, 0),
        // Orange
        new Color(240, 180, 20),
        // Blue
        new Color(0, 0, 255),
        // Black
        new Color(0, 0, 0),
        // Green
        new Color(0, 200, 0),
        // Pink
        new Color(255, 0, 160),
        // Tirquoise
        new Color(0, 160, 200),
        // Cyan
        new Color(0, 127, 255),
        // Olive 2
        new Color(105, 139, 30),
        // Purple
        new Color(128, 0, 255),
        // Blue-grey
        new Color(125, 158, 192),
        // Dark yellow
        new Color(205, 205, 0),
        // Deep pink
        new Color(139, 10, 80),
        // Burnt sienna
        new Color(233, 116, 81),
        // Medium tirquoise
        new Color(0, 180, 127),
        // Charteuse (med)
        new Color(127, 230, 0)};
    /**
     * The colour mask file reader for GDE's position-based colour mask files.
     */
    public static final FileFilter CHAR_MASK_FILTER = new FileFilter() {

        /**
         * Determines whether a file is of type "character colour mask"
         **
         * @param pathname the file to test
         * @return true if the file is of type "character colour mask"
         */
        public boolean accept(File pathname) {
            return true;
        }
        /**
         * Returns the description of "character colour mask"
         **
         * @return the string "Character-based colour mask file"
         */
        public String getDescription() {
            return "Character-based colour mask file";
        }
    };
    /**
     * The colour mask file reader for BioLegato's
     * character-based colour mask files.
     */
    public static final FileFilter GDE_MASK_FILTER = new FileFilter() {
        /**
         * Determines whether a file is of type "position colour mask"
         **
         * @param pathname the file to test
         * @return true if the file is of type "position colour mask"
         */
        public boolean accept(File pathname) {
            return true;
        }
        /**
         * Returns the description of "position colour mask"
         **
         * @return the string "GDE colour mask file"
         */
        public String getDescription() {
            return "GDE colour mask file";
        }
    };

    /**
     * The type of mask defined by the colour mask class
     * (i.e. is it a character or position based mask)
     */
    public static enum MaskType {
        POSITION, CHARACTER;
    }
    
    /**
     * The current default FOREG colour of normal unselected text
     */
    public static final Color FOREG = Color.BLACK;
    /**
     * The functional maximum size of the hashtable.
     * NOTE: 36 hash size = 26 letters + 10 numerical digits (0 to 9).
     */
    public static final int HASH_SIZE = 36;

    /**
     * Creates a new instance of a colour mask
     **
     * @param type     the type of the colour mask
     *                      (Positional or Character based)
     * @param name     the name of the colour mask
     *                      (used in comboboxes for human identification)
     * @param colours  the array of colours to use in the mask
     */
    protected ColourMask (MaskType type, String name, Color[] colours) {
        this.type = type;
        this.colours = colours;
        this.name = name;
    }

    /**
     * Draws a character string using the character colour coding specified by
     * the ColourMask object.
     **
     * @param gfx    the Graphics object to draw the text string to.
     * @param array  the array of characters to draw in colour.
     * @param offset the offset, within the array, to begin drawing characters.
     * @param length the number of characters, within the array, to draw.
     * @param xstart the X-coordinate to begin drawing characters at.
     * @param ystart the Y-coordinate to begin drawing characters at.
     */
    public void drawString (Graphics gfx, char[] array, int offset,
                            int length, int xstart, int ystart) {
        // The hash key to find the colour of the current character.
        int hash_key = 0;
        // Obtain the width of the character (assuming a fixed width font,
        // all characters should have equal width; however, for non-fixed width
        // fonts, the G character is generally one of the widest characters,
        // and is, thus, used as the standard width).
        final int width = gfx.getFontMetrics().charWidth('G');
        // the maximum position to draw within the character array.
        final int end = Math.min(array.length - 1, offset + length);

        // Loop through all of the characters in the array.  Also, track
        // the current x position of the character to draw.
        for (int index = offset, x = xstart; index < end; index++, x+= width) {
            // Handle position based colour masks.
            hash_key = index;

            // Handle character based colour masks.
            if (type == MaskType.CHARACTER) {
                hash_key = Character.digit(array[index], HASH_SIZE);
            }

            // Obtain the colour from the mask based on the hash_key.
            // If the hash_key is out of bounds (e.g. the string is longer than
            // the maximum position in a position-based colour mask), then just
            // use the standard BioLegato foreground colour.
            if (hash_key >= 0 && hash_key < colours.length) {
                gfx.setColor(colours[hash_key]);
            } else {
                gfx.setColor(FOREG);
            }

            // Draw the character to the screen.
            gfx.drawChars(array, index, 1, x, ystart);

        }
    }

    /**
     * <p>Returns the name of the colour mask.</p>
     *
     * <p>This is useful for any comboboxes or other widgets which wish to
     * elicit the name of the colour mask.</p>
     **
     * @return the name of the colour mask.
     */
    @Override
    public String toString() {
        return name;
    }

    /**
     * Reads and parses a character colour mask file.
     **
     * @param file the file to read.
     * @return an array of colour masks defined by the file.
     */
    public static ColourMask readCharMaskFile(File file) throws IOException{
        // The index of the tab chracter within a given line.  This is used
        // for reading BioLegato's character colour mask files.
        int tidx     = 0;
        // The index within the colour hash to add the current colour.
        int hashpos  = 0;
        // A character array representation of the current line.
        char[] carr = null;
        // The current line read in by the reader object.
        String line = "";
        // The name of the current colour mask.
        String maskname = null;
        // The current colour parsed from the file.
        Color colour = Color.BLACK;
        // The reader object for reading the colour mask.
        LineNumberReader reader = null;
        // The colour mask hash for storing the colours.
        Color[] hash = new Color[ColourMask.HASH_SIZE];

        // create a new line reader object
        reader = new LineNumberReader(new FileReader(file.getPath()));

        // loop through the file, line by line
        do {
            line = reader.readLine();
            if (line != null) {
                line = line.trim().toLowerCase();
                // skip the first line
                // NOTE: we support both spellings of colour in the character
                //       colour mask file.  Colour is the official spelling of
                //       most of the English speaking world (including Canada
                //       and the U.K.), color is the official spelling of the
                //       United States.
                if (!"aa\tcolour".equals(line) && !"aa\tcolor".equals(line)
                        && !"".equals(line)) {
                    // Ensure that there is a tab (and # symbol) on the current
                    // line.  All character colour mask files are TSV's (without
                    // quotation marks), which use the # symbol to specify RGB
                    // colour codes in hexadecimal (similar to HTML).
                    if ((tidx = line.indexOf("\t#")) >= 0) {
                        try {
                            // Parse the two columns of the colour mask file,
                            // (the characters to colour, and the colour to use
                            // for displaying them).
                            carr = line.substring(0, tidx).trim().toCharArray();

                            // Read the colour code on the current line.
                            colour = new Color(Integer.parseInt(
                                    line.substring(tidx + 2).trim(), 16));

                            // Read each character on the current line, and
                            // set it's character colour to the colour specified
                            // on the current line (NOTE: this is done by
                            // altering the character colour mask hashtable).
                            for (char chr : carr) {
                                hashpos = Character.digit(chr, HASH_SIZE);
                                if (hashpos >= 0) {
                                    hash[hashpos] = colour;
                                }
                            }
                        } catch (NumberFormatException nfe) {
                            // Print an error message if the colour code is NOT
                            // a valid hexadecimal number.
                            System.err.println("Sequence Colour Mask Reader -"
                                    + " Error in file \"" + file.getPath()
                                    + "\" on line " + reader.getLineNumber()
                                    + " -- invalid colour: "
                                    + line.substring(tidx + 2).trim());
                        }
                    } else {
                        // Print an error message if the line in the character
                        // colour mask file is not formatted properly.
                        System.err.println("Sequence Colour Mask Reader -"
                                + " Error in file \"" + file.getPath()
                                + "\" on line " + reader.getLineNumber()
                                + " -- missing tabulation: " + line);
                    }
                }
            }
        } while (line != null);

        // Set the name of the colour mask to the filename it was read from.
        maskname = file.getName().toLowerCase();

        // If the colur mask ends with the extension CSV, remove this from the
        // colour mask's name.
        if (maskname.endsWith(".csv")) {
            maskname = maskname.substring(0, maskname.length() - 4);
        }

        // Return the character colour mask object parsed from the file.
        return new ColourMask(ColourMask.MaskType.CHARACTER, maskname, hash);
    }

    /**
     * Reads and parses a position colour mask file.
     **
     * @param file the file to read.
     * @return an array of colour masks defined by the file.
     */
    public static ColourMask readPosMaskFile (File file) throws IOException {
        // The current position number within the mask.
        int number = 1;
        // The length of the colour mask.
        int length = 0;
        // The current line to parse in the file.
        String line = "";
        // The name of the colour mask.
        String name = file.getName();
        // The GDE number of the current colour parsed (corresponds to GDE
        // standard colours in the array STDCOLOURS).
        int cnum = 0;
        // The colours in the colour mask
        Color[] colours = null;
        // The reader object for reading the colour mask.
        BufferedReader reader = null;
//        boolean nodash = false; // Not currently supported

        // Open a buffered reader for reading the colour mask file.
        reader = new BufferedReader(new FileReader(file.getPath()));

        // Read the first line of the file.
        line = reader.readLine();

        // If the current line of the file contains the name field, parse it.
        if (line != null && line.toLowerCase().startsWith("name:")) {
            name = line.substring(5);
            line = reader.readLine();
        }
        
        // If the current line of the file contains the length field, parse it.
        if (line != null && line.toLowerCase().startsWith("length:")) {
            try {
                length = Integer.parseInt(line.substring(7));
                colours = new Color[length];
            } catch (Throwable nfe) {
                nfe.printStackTrace(System.err);
            }
            line = reader.readLine();
        } else {
            // If there is no length field, print an error message and continue.
            System.err.println("Sequence Colour Mask Reader -"
                    + " Invalid colour GDE mask file -"
                    + " missing \"length:\" field!");
        }

        // Print an error message if the nodash field is used in the file.
        if (line != null && line.toLowerCase().startsWith("nodash:")) {
            System.err.println("Sequence Colour Mask Reader -"
                    + " Biolegato does not support \"nodash:\""
                    + " in GDE colour mask files");
//                    nodash = true;
            line = reader.readLine();
        }

        // Make sure the file contains a start field.
        if (line != null && line.toLowerCase().startsWith("start:")) {
            // Read the next line.
            line = reader.readLine();

            // Keep reading the file until we have reached the maximum length
            // of the position colour mask.
            for (number = 0; line != null && number < length; number++) {
                // Trim the whitespace from the current line.
                line = line.trim();

                // Make sure that the line contains only numbers on it.
                //
                // NOTE: each line in a position colour mask file, past the
                //       start field, contains exactly one number.  This number
                //       corresponds to a GDE colour code.  The GDE colour codes
                //       used by BioLegato are listed in STDOLOURS.
                if (DataCanvas.testNumber(line)) {
                    try {
                        // Parse the GDE colour code on the line.
                        cnum = Integer.parseInt(line);
                        if (cnum > 0 && cnum <= STDCOLOURS.length) {
                            colours[number] = STDCOLOURS[cnum - 1];
                        } else {
                            // Print an error message if the GDE colour code
                            // is invalid (less than zero or greater than 16).
                            System.err.println("Sequence Colour Mask Reader -"
                                    + " Invalid colour #" + cnum);
                        }
                    } catch (NumberFormatException nfe) {
                        // Print an error if there was any trouble parsing the
                        // numerical value of the GDE colour code.
                        nfe.printStackTrace(System.err);
                    }
                } else {
                    // Print an error message if the line in the file did not
                    // contain a numerical value (i.e. only number digits).
                    System.err.println("Sequence Colour Mask Reader - "
                            + "\"" + line + "\" is not a colour");
                }
                // Read the next line in the file.
                line = reader.readLine();
            }

            // Almost never executed.
            // Code to handle an array length that exceeds the number of colours
            // indicated by the colour length property of the file.
            if (number < length) {
                Color[] temp = colours;
                colours = new Color[number];
                System.arraycopy(temp, 0, colours, 0, colours.length);
                System.err.println("Sequence Colour Mask Reader -"
                        + " Reached end of file before end of colour");
            }
        } else {
            // Print an error message if the file did not start with the "start"
            // field (i.e. an invalid GDE "position-based" colour mask file).
            System.err.println("Sequence Colour Mask Reader -"
                    + " Missing GDE colour mask data!");
        }

        // Return the colour mask corresponding to the data stored in the file.
        return new ColourMask(ColourMask.MaskType.POSITION, name, colours);
    }
}
