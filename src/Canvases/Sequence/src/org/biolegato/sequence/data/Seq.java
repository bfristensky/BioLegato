/*
 * Seq.java
 *
 * Created on August 28, 2008, 2:44 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */
package org.biolegato.sequence.data;

import org.biolegato.sequence.canvas.ColourMask;
import java.io.Serializable;

/**
 * <p>This class is used to contain all sequence related functions.</p>
 *
 * <p><dl><dt>The main functionality of this class is to wrap the following
 *  parameters:</dt>
 *      <dd>name, type, topology, direction, strandedness, description,
 *      groupID, sequence, original, mask, protect_align, protect_ambig,
 *      and protect_unambig</dd></dl>
 * </p>
 *
 * <p>Please see a description of each of these fields before making any major
 * edits to this class.  Additionally, please note that the toString method
 * should not be edited lightly (because it is relied upon by other classes).
 * </p>
 *
 * <p><u>WHY PACKAGE ACCESS FOR MANY VARIABLES?</u></p>
 * <!--  -------------------------------------- -->
 * <p>Because the access is restricted to only the package level, there should
 * not be any major issues with regard to code bugs.</p>
 * <ul>
 *      <li>http://java.dzone.com/articles/getter-setter-use-or-not-use-0</li>
 *      <li>http://www.artima.com/intv/sway2.html</li>
 * </ul>
 *
 * <p>In addition, because these variables do not have to be validated
 * (they can be any value, including null), and the variables are only
 * changed by classes and methods within the package there is no reason for
 * accessor methods because it would only cause a slow-down
 * (i.e. additional overhead).  In addition, the variables are only given
 * package access to minimize the amount of code that can change them.</p>
 *
 * <p>Please note that any variable defined in the scope of a class without the
 * word 'public' or 'private' preceding it, is defined as package scope.
 * Package scope means that only classes and methods in the same package can
 * alter or read the value directly.</p>
 *
 * <p><u>WHY TRANSIENT?</u></p>
 * <!--  -------------- -->
 * Certain parts of the object, such as the protection status and groupID,
 * are not necessary to store when serializing this object.  To indicate
 * this to Java, we add the word 'transient' before each variable
 * declaration.
 **
 * @author Graham Alvare
 * @author Brian Fristensky
 */
public final class Seq implements Cloneable, Serializable {

    /**
     * This enum is used for typing/storing sequence types.
     * This method of storage ensures proper type casting.
     * Feel free to add more types as necessary.
     */
    public static enum Type {

        /**
         * Sequence type DNA
         */
        DNA {

            /**
             * Prints a nicely formatted string representation for "DNA type"
             * enum object (Type.DNA).
             **
             * @return "DNA"
             */
            @Override
            public String toString() {
                return "DNA";
            }
        },
        /**
         * Sequence type RNA
         */
        RNA {

            /**
             * Prints a nicely formatted string representation for "RNA type"
             * enum object (Type.RNA).
             **
             * @return "RNA"
             */
            @Override
            public String toString() {
                return "RNA";
            }
        },
        /**
         * Sequence type protein
         */
        PROTEIN {

            /**
             * Prints a nicely formatted string representation for the
             * "protein type" enum object (Type.PROTEIN).
             **
             * @return "Protein"
             */
            @Override
            public String toString() {
                return "Protein";
            }
        },
        /**
         * Sequence type colour mask
         */
        MASK {

            /**
             * Prints a nicely formatted string representation for the
             * "colour mask type" enum object (Type.MASK).
             **
             * @return "Colour mask"
             */
            @Override
            public String toString() {
                return "Colour mask";
            }
        },
        /**
         * Sequence type is text
         * (represented as '"')
         */
        TEXT {

            /**
             * Prints a nicely formatted string representation for the
             * "text type" enum object (Type.TEXT).
             **
             * @return "Text"
             */
            @Override
            public String toString() {
                return "Text";
            }
        };
    }

    /**
     * Used for typing/storing sequence direction.
     * This is used for all sequences which may have direction.
     * This enum may be ignored if you are dealing with non-sequence
     * test types (such as text), or any type of test that either
     * doesn't have or doesn't need to distinguish direction.
     */
    public static enum Direction {

        /**
         * Direction of the sequence goes from 3' to 5'
         */
        FROM3TO5 {

            /**
             * Prints a nicely formatted string representation for the
             * "From 3' to 5'" enum object (Direction.FROM3TO5).
             **
             * @return "From 3' to 5'"
             */
            @Override
            public String toString() {
                return "From 3' to 5'";
            }
        },
        /**
         * Direction of the sequence goes from 5' to 3'
         */
        FROM5TO3 {

            /**
             * Prints a nicely formatted string representation for the
             * "From 5' to 3'" enum object (Direction.FROM5TO3).
             **
             * @return "From 5' to 3'"
             */
            @Override
            public String toString() {
                return "From 5' to 3'";
            }
        };
    }

    /**
     * Used for typing/storing sequence topology.
     * This is used for all sequences which may have topology.
     * This enum may be ignored if you are dealing with non-sequence
     * test types (such as text), or any type of test that either
     * doesn't have or doesn't need to distinguish topology.
     */
    public static enum Topology {

        /**
         * Linear topology
         */
        LINEAR {

            /**
             * Prints a nicely formatted string representation for the
             * "linear topology" enum object (Topology.LINEAR).
             **
             * @return "Linear"
             */
            @Override
            public String toString() {
                return "Linear";
            }
        },
        /**
         * Circular topology
         */
        CIRCULAR {

            /**
             * Prints a nicely formatted string representation for the
             * "circular topology" enum object (Topology.CIRCULAR).
             **
             * @return "Circular"
             */
            @Override
            public String toString() {
                return "Circular";
            }
        };
    }

    /**
     * Used for typing/storing sequence strandedness.
     * This is used for all sequences which may have strandedness.
     * This enum may be ignored if you are dealing with non-sequence
     * test types (such as text), or any type of test that either
     * doesn't have or doesn't need to distinguish strandedness.
     */
    public static enum Strandedness {

        /**
         * Single stranded sequence
         */
        SINGLE {

            /**
             * Prints a nicely formatted string representation for the
             * "single stranded" enum object (Strandedness.SINGLE).
             **
             * @return "Single stranded"
             */
            @Override
            public String toString() {
                return "Single stranded";
            }
        },
        /**
         * Double stranded sequence
         * (represented as 'D')
         */
        DOUBLE {

            /**
             * Prints a nicely formatted string representation for the
             * "double stranded" enum object (Strandedness.DOUBLE).
             **
             * @return "Double stranded"
             */
            @Override
            public String toString() {
                return "Double stranded";
            }
        },
        /**
         * Mixed stranded sequence
         * (represented as 'M')
         */
        MIXED {

            /**
             * Prints a nicely formatted string representation for the
             * "mixed stranded" enum object (Strandedness.MIXED).
             **
             * @return "Mixed strandedness"
             */
            @Override
            public String toString() {
                return "Mixed strandedness";
            }
        };
    }

    //////////////////////////////////
    // NOTES ON THE VARIABLES BELOW //
    //////////////////////////////////
    // WHY PACKAGE ACCESS?
    // -------------------
    // because the access is restricted to only the package level, there should
    // not be any major issues with regard to code bugs
    //
    // http://java.dzone.com/articles/getter-setter-use-or-not-use-0
    // http://www.artima.com/intv/sway2.html
    //
    // In addition, because these variables do not have to be validated
    // (they can be any value, including null), and the variables are only
    // changed by classes and methods within the package there is no reason for
    // accessor methods because it would only cause a slow-down
    // (i.e. additional overhead).  In addition, the variables are only given
    // package access to minimize the amount of code that can change them.
    //
    // Please note that any variable defined in the scope of a class without the
    // word 'public' or 'private' preceding it, is defined as package scope.
    // Package scope means that only classes and methods in the same package can
    // alter or read the value directly.
    //
    // WHY TRANSIENT?
    // --------------
    // Certain parts of the object, such as the protection status and groupID,
    // are not necessary to store when serializing this object.  To indicate
    // this to Java, we add the word 'transient' before each variable
    // declaration.
    //////////////////////////////////

    /**
     * This variable stores the numerical groupID for the sequence.
     */
    transient int groupID = 0;
    /**
     * This variable stores the sequence type for the data.
     */
    Type type = Type.DNA;
    /**
     * The short name of the sequence.
     */
    String name;
    /**
     * A longer description of the sequence.
     */
    String description = null;
    /**
     * The topology of the sequence data (linear or circular), where applicable.
     */
    Topology topology = Topology.LINEAR;
    /**
     * The direction of the sequence data, where applicable.  This applies only
     * to the direction of nucleotide sequences (e.g. 5' to 3' or 3' to 5').
     */
    Direction direction = Direction.FROM5TO3;
    /**
     * The strandedness of the sequence data (single, double, or mixed),
     * where applicable.
     */
    Strandedness strandedness = Strandedness.SINGLE;
    /**
     * The actual sequence data for the current sequence.
     */
    StringBuffer sequence;
    /**
     * The colour mask to display the sequence with.
     */
    transient ColourMask mask = null;
    /**
     * <p>The protection status for alignment characters in the sequence.</p>
     *
     * <p>According to GDE (version 2.2), for nucleotide sequences, any character,
     * which is not an ambiguous or unambiguous character, is an alignment
     * character.</p>
     *
     * <p>Put simply, any character in a nucleotide sequence, other than:
     * <code>B, D, H, I, K, M, N, R, S, V, W, Y, A, C, G, T, or U </code>, is an
     * alignment character.</p>
     *
     * <p>For protein sequences (ref. GDE 2.2) only the following characters are
     * considered alignment characters:
     * <pre>' ', '\n', '\t', '\r' '-'</pre> (i.e. whitespace or dash characters)
     * </p>
     *
     * <p>Text sequences do not handle protection characters.</p>
     *
     * <p>All protections character processing is handled by the method:
     *          Dataset.isProtectionsOn</p>
     *
     * <p>By default, this protection setting is disabled (false).</p>
     **
     * @see org.biolegato.sequence.data.Dataset#isProtectionsOn(org.biolegato.sequence.data.Seq.Type, boolean, boolean, boolean, char[], int, int)
     */
    transient boolean protect_align = false;
    /**
     * <p>The protection status for ambiguous characters in the sequence.</p>
     *
     * <p>The ambiguous characters for nucleotide sequences (RNA or DNA),
     * according to GDE (version 2.2) are as follows:
     *          <pre>B, D, H, I, K, M, N, R, S, V, W, Y</pre></p>
     *
     * <p>The ambiguous characters for protein sequences (ref. GDE 2.2) are:
     *          <pre>B, J, X, Z, *</pre></p>
     *
     * <p>Text sequences do not handle protection characters.</p>
     *
     * <p>All protections character processing is handled by the method:
     *          Dataset.isProtectionsOn</p>
     *
     * <p>By default, this protection setting is enabled (true), unless the
     * sequence provided to the constructor is empty (in which case the
     * protection setting will default to false, so the user can create/type a
     * new sequence into BioLegato).</p>
     **
     * @see org.biolegato.sequence.data.Dataset#isProtectionsOn(org.biolegato.sequence.data.Seq.Type, boolean, boolean, boolean, char[], int, int)
     */
    transient boolean protect_ambig = true;
    /**
     * <p>The protection status for unambiguous characters in the sequence.</p>
     *
     * <p>The unambiguous characters in nucleotide sequences (RNA or DNA),
     * according to GDE (version 2.2) are as follows:
     *          <pre>A, C, G, T, U</pre></p>
     *
     * <p>For protein sequences (ref. GDE 2.2), any character, which is not an
     * ambiguous or alignment character, is considered an unambiguous character.
     * </p>
     *
     * <p>Put simply, any character in a protein sequence, other than: <code>*,
     * B, X, J, Z,</code> whitespace or dash characters (<code>' ', '\n', '\t',
     * '\r' '-'</code>), is considered to be an unambiguous character
     * (regardless of whether it is actually a valid protein character -- e.g.
     * <code>'+'</code> is considered an unambiguous amino acid).  Why?  To
     * accommodate future and modified amino acids.  For example, Selenocysteine
     * (AA code U) is not a standard amino acid, but is appropriately handled by
     * this algorithm.
     * </p>
     *
     * <p>Technically, one could use just every letter other than B, X, Y and Z
     * to handle the unambiguous characters (thereby skipping any possible non-
     * alphabetical expansions).  This differs from the current approach, which
     * includes non-alphabet characters. There is some merit to this, and so if
     * one decides this approach is more valuable in the future, one could
     * simply change the code in Dataset.  I have left the case statements for
     * each alphabetical character (above the "default" case) in the method
     * 'isProtectionsOn', in case such an algorithm is desired in the future.
     * </p>
     *
     * <p>Text sequences do not handle protection characters.</p>
     *
     * <p>All protections character processing is handled by the method:
     *          Dataset.isProtectionsOn</p>
     *
     * <p>By default, this protection setting is enabled (true), unless the
     * sequence provided to the constructor is empty (in which case the
     * protection setting will default to false, so the user can create/type a
     * new sequence into BioLegato).</p>
     **
     * @see org.biolegato.sequence.data.Dataset#isProtectionsOn(org.biolegato.sequence.data.Seq.Type, boolean, boolean, boolean, char[], int, int)
     */
    transient boolean protect_unambig = true;
    /**
     * <p>This variable stores the original header of the sequence if the
     * sequence was loaded from a GenBANK file.  This is useful for preserving
     * header metadata, such as annotation features, CDS, etc.  This variable is
     * left null for sequences which are not read from GenBANK files.</p>
     *
     * <p>In addition, if a sequence is modified, its GenBANK original header
     * (stored in this variable) is lost (set this variable to null) because the
     * header will no longer be applicable.  For example, if part of a sequence
     * is deleted or changed, then the features in the sequence will no longer
     * be valid, and so we delete the original header from memory.</p>
     *
     * <p><u>In summary:</u><br />
     * GenBANK original headers are stored from GenBANK files read into
     * BioLegato.  These headers are used for ensuring that CDS, annotation, and
     * other header data (when present) is not lost from unaltered GenBANK files
     * read into BioLegato.</p>
     */
    CharSequence original = null;
    /**
     * Used for serialization purposes.
     */
    private static final long serialVersionUID = 7526472295622777024L;

///////////////
//***********//
//* METHODS *//
//***********//
///////////////
    /**
     * <p>Constructs new instances of a sequence object.  The names of the
     * constructor parameters passed correspond directly with the class fields
     * of the same names (i.e. the parameter 'name' corresponds directly to the
     * sequence object field 'name').</p>
     *
     * <p>In this case, the sequence object will be a nameless empty DNA
     * sequence.</p>
     */
    public Seq() {
        this(Type.DNA, "", new StringBuffer());
    }

    /**
     * <p>Constructs new instances of a sequence object.  The names of the
     * constructor parameters passed correspond directly with the class fields
     * of the same names (i.e. the parameter 'name' corresponds directly to the
     * sequence object field 'name').</p>
     *
     * <p>The only thing really noteworthy is that if the sequence provided is
     * empty, the character protection settings will all be set to false.  This
     * functionality is handled by this specific constructor method (hence why
     * all other constructor methods, except the clone/copy Seq constructor
     * method, interface with this constructor).</p>
     *
     * <p>This constructor is called directly by BioLegato's GDE flat-file
     * parser, and also unifies all of the other Seq object constructors (except
     * the copy/clone Seq constructor).</p>
     **
     * @param type the type of data to store in the sequence object.
     * @param name the name of the sequence object.
     * @param sequence the text sequence to store in the sequence object.
     * @see org.biolegato.sequence.data.Seq#type
     * @see org.biolegato.sequence.data.Seq#name
     * @see org.biolegato.sequence.data.Seq#sequence
     * @see org.biolegato.sequence.data.Seq#Seq()
     * @see org.biolegato.sequence.data.Seq#Seq(org.biolegato.sequence.data.Seq.Type, java.lang.String, java.lang.StringBuffer, java.lang.String)
     * @see org.biolegato.sequence.data.Seq#Seq(org.biolegato.sequence.data.Seq.Type, java.lang.String, java.lang.StringBuffer, org.biolegato.sequence.data.Seq.Direction, org.biolegato.sequence.data.Seq.Topology, org.biolegato.sequence.data.Seq.Strandedness)
     * @see org.biolegato.sequence.data.Seq#Seq(org.biolegato.sequence.data.Seq.Type, java.lang.String, java.lang.StringBuffer, org.biolegato.sequence.data.Seq.Direction, org.biolegato.sequence.data.Seq.Topology, org.biolegato.sequence.data.Seq.Strandedness, java.lang.CharSequence)
     * @see org.biolegato.sequence.data.Seq#Seq(org.biolegato.sequence.data.Seq.Type, java.lang.String, java.lang.StringBuffer, org.biolegato.sequence.data.Seq.Direction, org.biolegato.sequence.data.Seq.Topology, org.biolegato.sequence.data.Seq.Strandedness, int, java.lang.String)
     */
    public Seq(Type type, String name, StringBuffer sequence) {
        // Copy the parameters specified in the constructor to the new object.
        this.name = name;
        this.type = type;
        this.sequence = sequence;

        // Set the default protections to false if the sequence is empty.
        if (this.sequence != null && sequence.length() == 0) {
            protect_align = false;
            protect_ambig = false;
            protect_unambig = false;
        }
    }

    /**
     * <p>Constructs new instances of a sequence object.  The names of the
     * constructor parameters passed correspond directly with the class fields
     * of the same names (i.e. the parameter 'name' corresponds directly to the
     * sequence object field 'name').</p>
     *
     * <p>The only thing really noteworthy is that if the sequence provided is
     * empty, the character protection settings will all be set to false.</p>
     *
     * <p>This constructor is called by BioLegato's FastA file parser.</p>
     **
     * @param type the type of data to store in the sequence object.
     * @param name the name of the sequence object.
     * @param sequence the text sequence to store in the sequence object.
     * @param description the description of the sequence object.
     * @see org.biolegato.sequence.data.Seq#type
     * @see org.biolegato.sequence.data.Seq#name
     * @see org.biolegato.sequence.data.Seq#sequence
     * @see org.biolegato.sequence.data.Seq#description
     * @see org.biolegato.sequence.data.FastAFile
     */
    public Seq(Type type, String name,
            StringBuffer sequence, String description) {
        this(type, name, sequence);
        
        this.description = description;
    }

    /**
     * <p>Constructs new instances of a sequence object.  The names of the
     * constructor parameters passed correspond directly with the class fields
     * of the same names (i.e. the parameter 'name' corresponds directly to the
     * sequence object field 'name').</p>
     *
     * <p>The only thing really noteworthy is that if the sequence provided is
     * empty, the character protection settings will all be set to false.</p>
     *
     * <p>This method unifies the Seq constructors called by the GenBANK and
     * GDE parsers.</p>
     **
     * @param type the type of data to store in the sequence object.
     * @param name the name of the sequence object.
     * @param sequence the text sequence to store in the sequence object.
     * @param direction the direction of the sequence (if nucleotide).
     * @param topology the topology of the sequence (if nucleotide).
     * @param strandedness the strandedness of the sequence (if nucleotide).
     * @see org.biolegato.sequence.data.Seq#type
     * @see org.biolegato.sequence.data.Seq#name
     * @see org.biolegato.sequence.data.Seq#sequence
     * @see org.biolegato.sequence.data.Seq#direction
     * @see org.biolegato.sequence.data.Seq#topology
     * @see org.biolegato.sequence.data.Seq#strandedness
     * @see org.biolegato.sequence.data.Seq#Seq(org.biolegato.sequence.data.Seq.Type, java.lang.String, java.lang.StringBuffer, org.biolegato.sequence.data.Seq.Direction, org.biolegato.sequence.data.Seq.Topology, org.biolegato.sequence.data.Seq.Strandedness, java.lang.CharSequence)
     * @see org.biolegato.sequence.data.Seq#Seq(org.biolegato.sequence.data.Seq.Type, java.lang.String, java.lang.StringBuffer, org.biolegato.sequence.data.Seq.Direction, org.biolegato.sequence.data.Seq.Topology, org.biolegato.sequence.data.Seq.Strandedness, int, java.lang.String)
     */
    public Seq(Type type, String name, StringBuffer sequence,
            Direction direction, Topology topology, Strandedness strandedness) {
        this(type, name, sequence);

        // Copy parameters specified in the constructor to the new object.
        this.topology = topology;
        this.direction = direction;
        this.strandedness = strandedness;
    }

    /**
     * <p>Constructs new instances of a sequence object.  The names of the
     * constructor parameters passed correspond directly with the class fields
     * of the same names (i.e. the parameter 'name' corresponds directly to the
     * sequence object field 'name').</p>
     *
     * <p>The only thing really noteworthy is that if the sequence provided is
     * empty, the character protection settings will all be set to false.</p>
     *
     * <p>This constructor is called by BioLegato's GenBANK parser.</p>
     **
     * @param type the type of data to store in the sequence object.
     * @param name the name of the sequence object.
     * @param sequence the text sequence to store in the sequence object.
     * @param direction the direction of the sequence (if nucleotide).
     * @param topology the topology of the sequence (if nucleotide).
     * @param strandedness the strandedness of the sequence (if nucleotide).
     * @param original the original GenBANK header for the sequence data.
     * @see org.biolegato.sequence.data.Seq#type
     * @see org.biolegato.sequence.data.Seq#name
     * @see org.biolegato.sequence.data.Seq#sequence
     * @see org.biolegato.sequence.data.Seq#direction
     * @see org.biolegato.sequence.data.Seq#topology
     * @see org.biolegato.sequence.data.Seq#strandedness
     * @see org.biolegato.sequence.data.Seq#original
     * @see org.biolegato.sequence.data.GenBankFile2008
     */
    public Seq(Type type, String name, StringBuffer sequence,
            Direction direction, Topology topology, Strandedness strandedness,
            CharSequence original) {
        this(type, name, sequence, direction, topology, strandedness);

        // Copy the GenBANK original header to the new object.
        this.original = original;
    }

    /**
     * <p>Constructs new instances of a sequence object.  The names of the
     * constructor parameters passed correspond directly with the class fields
     * of the same names (i.e. the parameter 'name' corresponds directly to the
     * sequence object field 'name').</p>
     *
     * <p>The only thing really noteworthy is that if the sequence provided is
     * empty, the character protection settings will all be set to false.</p>
     *
     * <p>This constructor is called by BioLegato's GDE parser.</p>
     **
     * @param type the type of data to store in the sequence object.
     * @param name the name of the sequence object.
     * @param sequence the text sequence to store in the sequence object.
     * @param direction the direction of the sequence (if nucleotide).
     * @param topology the topology of the sequence (if nucleotide).
     * @param strandedness the strandedness of the sequence (if nucleotide).
     * @param groupID the group ID for the sequence object.
     * @param description the description of the sequence object.
     * @see org.biolegato.sequence.data.Seq#type
     * @see org.biolegato.sequence.data.Seq#name
     * @see org.biolegato.sequence.data.Seq#sequence
     * @see org.biolegato.sequence.data.Seq#direction
     * @see org.biolegato.sequence.data.Seq#topology
     * @see org.biolegato.sequence.data.Seq#strandedness
     * @see org.biolegato.sequence.data.Seq#groupID
     * @see org.biolegato.sequence.data.Seq#description
     * @see org.biolegato.sequence.data.GDEFile
     */
    public Seq(Type type, String name, StringBuffer sequence,
            Direction direction, Topology topology, Strandedness strandedness,
            int groupID, String description) {
        this(type, name, sequence, direction, topology, strandedness);

        // Copy parameters specified in the constructor to the new object.
        this.groupID = groupID;
        this.description = description;
    }

    /**
     * Constructs new instance of a sequence object by copying data from
     * another, pre-existing sequence object.
     **
     * @param data the sequence to copy data from.
     * @see org.biolegato.sequence.data.Seq#clone() 
     */
    public Seq(Seq data) {
        // Copy data from the old object to the new object.
        this.name = data.name;
        this.type = data.type;
        this.topology = data.topology;
        this.direction = data.direction;
        this.strandedness = data.strandedness;
        this.protect_align = data.protect_align;
        this.protect_ambig = data.protect_ambig;
        this.protect_unambig = data.protect_unambig;
        this.sequence = data.sequence;
        this.original = data.original;
    }

//////////////////////////
//**********************//
//* SEQUENCE FUNCTIONS *//
//**********************//
//////////////////////////

    /**
     * Return the group ID number of the sequence object.
     **
     * @return the group ID number of the sequence object.
     * @see org.biolegato.sequence.data.Seq#groupID
     */
    final int getGroupID() {
        return groupID;
    }

    /**
     * Return the name of the sequence object.
     **
     * @return the name of the sequence object.
     * @see org.biolegato.sequence.data.Seq#name
     */
    final String getName() {
        return name;
    }

    /**
     * Return the long description of the sequence object.
     **
     * @return the long description of the sequence object.
     * @see org.biolegato.sequence.data.Seq#description
     */
    final String getDescription() {
        return description;
    }

    /**
     * Return the type of data stored in the sequence object (DNA, RNA, etc.)
     **
     * @return the type of data stored in the sequence object.
     * @see org.biolegato.sequence.data.Seq.Type
     * @see org.biolegato.sequence.data.Seq#type
     */
    final Type getType() {
        return type;
    }

    /**
     * Return the direction of nucleotide data stored in the sequence object.
     * This is only applicable if the data stored in the sequence object is
     * nucleotide data (DNA or RNA).
     **
     * @return the direction of nucleotide data stored in the sequence object.
     * @see org.biolegato.sequence.data.Seq.Direction
     * @see org.biolegato.sequence.data.Seq#direction
     */
    final Direction getDirection() {
        return direction;
    }

    /**
     * Return the topology of nucleotide data stored in the sequence object.
     * This is only applicable if the data stored in the sequence object is
     * nucleotide data (DNA or RNA).
     **
     * @return the topology of nucleotide data stored in the sequence object.
     * @see org.biolegato.sequence.data.Seq.Topology
     * @see org.biolegato.sequence.data.Seq#topology
     */
    final Topology getTopology() {
        return topology;
    }

    /**
     * Return the strandedness of nucleotide data stored in the sequence object.
     * This is only applicable if the data stored in the sequence object is
     * nucleotide data (DNA or RNA).
     **
     * @return the strandedness of nucleotide data stored in the object.
     * @see org.biolegato.sequence.data.Seq.Strandedness
     * @see org.biolegato.sequence.data.Seq#strandedness
     */
    final Strandedness getStrandedness() {
        return strandedness;
    }

    /**
     * Return the original GenBANK header of the data stored in the sequence
     * object.  This is only applicable if the data stored in the sequence
     * object was read from a GenBANK file, and has not been altered by the
     * user.  The reasons for storing and using this information are outlined
     * in the field 'strandedness' in this same class.  If no information is
     * present, a value of null is returned.
     **
     * @return the original GenBANK header of the data stored in the sequence
     *         object (null if either not present or applicable).
     * @see org.biolegato.sequence.data.Seq#original
     */
    final CharSequence getOriginal() {
        return original;
    }

    /**
     * Return the raw nucleotide, amino acid, or text sequence data stored by
     * this sequence object.
     **
     * @return the raw nucleotide, amino acid, or text sequence data
     *         stored by this object.
     * @see org.biolegato.sequence.data.Seq#sequence
     */
    final StringBuffer getSequence() {
        return sequence;
    }
    
    /**
     * Return the colour mask object to use for colouring the display of the
     * sequence data (see getSequence) stored in this sequence object.
     **
     * @return the strandedness of nucleotide data stored in the object.
     * @see org.biolegato.sequence.data.Seq#mask
     * @see org.biolegato.sequence.data.Seq#sequence
     * @see org.biolegato.sequence.data.Seq#getSequence() 
     */
    final ColourMask getMask() {
        return mask;
    }

    /**
     * <p>Creates a string representation of the Seq and its fields
     * This representation is limited to '[GROUP#|_] NAME' because the toString
     * method will be called when the sequence is displayed in any JList.</p>
     *
     * <p>A couple of examples are as follows:</p>
     * <pre>
     *      1 HEXOKINASE
     *      1 PYRUVATE KINASE
     *      1 ENOLASE
     *      2 ACONITASE
     *      _ OTC</pre>
     *
     * <p>
     * Where the glycolysis enzymes (HEXOKINASE, PYRUVATE KINASE, and ENOLASE)
     * are in group 1, the citric acid enzyme (ACONITASE) is in group 2, and
     * the urea cycle enzyme (OTC -- ornithine transcarbamylase) is not grouped.
     * </p>
     *
     * <p>Reliability and performance are the reasons why the string format
     * limitation of '[GROUP#|_] NAME' was imposed:</p>
     * <ol>
     *  <li>Reliability: the code will provide a simpler implementation for
     *      displaying sequences in a list.  Instead of writing a custom class
     *      to display a list of sequences or using other possible complicated
     *      implementations (e.g. writing multiple external methods to "wrap"
     *      the sequence), the toString method and Java's intrinsic JList
     *      provide a simple streamlined way to display a list of sequences.
     *      Because the code is simpler, it should be more reliable.</li>
     *  <li>Performance: because the code is simpler and can be accessed
     *      directly by the Java API (no "wrapping" necessary), the amount of
     *      Java code required to display the sequence list is minimal.
     *      Additionally, JList's implementation is likely (but not necessarily)
     *      optimized C/C++ code, which should (theoretically) be much faster
     *      than any practical custom JComponent display list code.</li>
     *  <li>Readability: smaller code is almost always more readable (as long as
     *      it is documented properly).</li>
     * </ol>
     **
     * @return a string containing the name and group number of the sequence.
     *         The format is as follows: '[GROUP#|_] NAME'.
     * @see org.biolegato.sequence.data.Seq#groupID
     * @see org.biolegato.sequence.data.Seq#name
     * @see org.biolegato.sequence.data.Dataset#getElementAt(int) 
     */
    @Override
    public String toString() {
        StringBuilder string = new StringBuilder();

        // Append the group ID if not 0 (otherwise append '_ ') to the start of
        // the output string builder (beginning the output string).
        if (groupID > 0) {
            string.append(groupID).append(" ");
        } else {
            string.append("_ ");
        }

        // Append the name to the output string after the group ID (or '_ ').
        string.append(name);
        
        return string.toString();
    }

    /**
     * Clones the current sequence object.
     **
     * @return a cloned copy of this sequence object.
     */
    @Override
    public Object clone() {
        return new Seq(this);
    }

    /**
     * <p>This function detects the type of a sequence (RNA, DNA, or protein).
     * </p>
     *
     * <p>Please note that this function's implementation is very simple and may
     * not be accurate in all cases.  This function reads sequence data and
     * looks for the characters F, E, J, L, O, Q, X, Z, or U.  If the characters
     * F, E, J, L, O, Q, X, or Z are encountered, the sequence is determined to
     * be an amino acid/protein sequence; however, if the character U is found,
     * the sequence is determined to be RNA.  If both the character U and any
     * character from the protein set (F, E, J, L, O, Q, X, or Z) are found,
     * then the first of those characters found will be used to determine the
     * sequence type.  Note that if none of those characters (F, E, J, L, O, Q,
     * X, Z, or U) are detected, then the sequence type will default to DNA.</p>
     **
     * @param data the sequence object to detect the type for.
     * @return the sequence type detected.
     */
    public static Type detectType(StringBuffer data) {
        Seq.Type result = Seq.Type.DNA;

        // The current character in the sequence to examine.
        char test;
        // Create a new array to contain the sequence data to examine.
        char[] array = new char[data.length()];

        // Extract the the sequence data into an array.
        data.getChars(0, data.length(), array, 0);

        // Iterate through the sequence array until we either reach the end of
        // the array, or until the sequence type is determined not to be DNA.
        for (int count = 0; count < array.length
                && result == Seq.Type.DNA; count++) {
            // Convert the test character to upper-case (this way we avoid
            // testing both upper and lower case letters for matches).
            test = Character.toUpperCase(array[count]);
            
            if (test == 'U') {
                // If the sequence contains the character U, it is likely
                // (but not necessarily) an RNA nucleotide sequence.
                result = Seq.Type.RNA;
            } else if (test == 'F' || test == 'E' || test == 'J'
                    || test == 'L' || test == 'O' || test == 'Q'
                    || test == 'X' || test == 'Z') {
                // If the sequence contains F, E, J, L, O, Q, X or Z, it is
                // likely (but not necessarily) an amino acid sequence.
                result = Seq.Type.PROTEIN;
            }
        }

        // Return the results of the type detection.
        return result;
    }
}
