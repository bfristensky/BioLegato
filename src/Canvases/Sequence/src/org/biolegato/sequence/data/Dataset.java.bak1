/*
 * Dataset.java
 *
 * Created on September 30, 2008, 11:08 AM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */
package org.biolegato.sequence.data;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.swing.AbstractListModel;
import org.biolegato.sequence.canvas.ColourMask;
import org.biolegato.sequence.canvas.SequenceCanvas;

/**
 * <p>The internal document format for BioLegato.</p>
 *
 * <p>This document is structured as a linked list of sequences.  Each character
 * has an offset based on its position within the list and it's position within
 * its containing sequence.  Sequences start at 0 (first character in the first
 * sequence in the list, and ens with the last character in the last sequence
 * within the list.</p>
 **
 * @author Graham Alvare
 * @author Brian Fristensky
 */
public class Dataset extends AbstractListModel {

    /**
     * The parent canvas for the Dataset sequence object container.
     */
    private SequenceCanvas canvas;
    /**
     * This linked list used to store all of the height in the document.
     * <p>
     *  Each y is stored as a linked list of sequence wrappers.
     *  Each sequence wrapper is characterized by a sequence and an index
     *  offset.  The index offset is used to determine the offset of the
     *  sequence within the document.
     * </p>
     */
    private final List<Seq> lines = new ArrayList<Seq>();
    /**
     * Keeps track of the maximum groupID number in usage.  This is used to
     * ensure that there are no groupID number collisions.
     */
    private static int maxGroup = 0;
    /**
     * The maximum number of groups to support.  The value of this variable sets
     * the group array size.
     */
    private final static int DEFAULT_MAX_GROUPS = 30;
    /**
     * A pseudo-hash table (array) used to map all groups (by number) to the
     * sequence objects which are in the group.  Each index in the array
     * corresponds to a groupID number.
     */
    private static Set<Seq>[] group2seq = new Set[DEFAULT_MAX_GROUPS];

    /**
     * Creates a new instance of the Dataset object.
     **
     * @param  canvas  the parent sequence canvas to associate with the Dataset
     *                 object with.
     */
    public Dataset(SequenceCanvas canvas) {
        this.canvas = canvas;
    }

////////////////////////////////
//****************************//
//* DIRECT DATA MODIFICATION *//
//****************************//
////////////////////////////////
    /**
     * Adds a sequence to the Dataset sequence object container.  This function
     * also calls calls all of the listeners which are listening to the Dataset
     * object.  Mostly (ane even 'completely', at the time of this writing),
     * listeners are used for displaying the data in the sequence list and the
     * sequence text area.  Note that the add method inserts a sequence object,
     * as a whole, into the Dataset, while the insert method inserts the text
     * of a sequence object into the middle of the already present sequence
     * object in the Dataset.
     **
     * @param  y    the line index number to insert the sequence object at.
     * @param  seq  the sequence object to insert.
     * @return true if the insertion was successful, otherwise false.
     */
    public boolean add(int y, Seq seq) {
        boolean result = false;

        // Ensure that the parameters passed to this function are appropriate.
        if (seq != null && y >= 0 && y <= getSize()) {
            // Insert the sequence and notify all listener methods.
            lines.add(y, seq);
            fireIntervalAdded(this, y, y);
            result = true;
        }
        return result;
    }

    /**
     * <p>Inserts text into the Dataset sequence object container.  This
     * function also calls calls all of the listeners which are listening to the
     * Dataset object.  Mostly (ane even 'completely', at the time of this
     * writing), listeners are used for displaying the data in the sequence list
     * and the sequence text area.  Note that the add method inserts a sequence
     * object, as a whole, into the Dataset, while the insert method inserts the
     * text of a sequence object into the middle of the already present sequence
     * object in the Dataset.</p>
     *
     * <p>This function also obeys all sequence permissions/protections (as long
     * as the protect boolean is set to true).  In the case of text insertion,
     * text may NOT be inserted if it contains a protected character.  For
     * example, if the sequence is a protein sequence and the unambiguous
     * character protections are set to true, then the text "BZAZ" may not be
     * inserted because it contains one unambiguous character (A, for alanine);
     * however, the text "BZZZ" may be inserted, as long as the ambiguous
     * character protections are set to false.  See Dataset.isProtectionsOn for
     * more information about character protections.</p>
     **
     * @param x       the X-coordinate to insert the sequence text.
     * @param y       the Y-coordinate to insert the sequence text.
     * @param text    the sequence text to insert.
     * @param offset  the offset within the array to insert.
     * @param length  the length in characters of data from the array to insert.
     * @param protect whether to test the protections of the sequence, already
     *                in the Dataset, before inserting the text.
     * @return true if the insertion was successful, otherwise false.
     */
    public boolean insert(int x, int y, char[] text, int offset,
            int length, boolean protect) {
        Seq current;
        boolean result = false;

        // Ensure that the parameters passed to this function are appropriate.
        // (Particularly the Y-coordinate/line offset, and the sequence text.)
        if (text != null && y >= 0 && y <= getSize()) {
            current = getLine(y);

            // Check the sequence's protection settings if
            if (!protect || !Dataset.isProtectionsOn(current.type,
                    current.protect_align, current.protect_ambig,
                    current.protect_unambig, text, 0, text.length)) {
                // BRANCH
                // if we are at the end of the sequence, we are appending
                // characters to the sequence; otherwise, we are inserting
                // characters.  This branch mainly has to do with performance
                // and avoiding possible exceptions.
                if (current.sequence.length() > x) {
                    current.sequence.insert(x, text, offset, length);
                } else {
                    current.sequence.append(text, offset, length);
                }

                // Deletes GenBank original copies of the sequence
                // this is because we are modifying the sequence, so we do not
                // want the original sequence to be exported
                current.original = null;

                // Call the canvas and notify it that the sequence length has
                // changed.  This is essential for repainting the text area.
                SequenceCanvas.textLengthChanged(x, y, text.length);
            }
        }
        return result;
    }

    /**
     * <p>Deletes a portion of a sequence from the Dataset sequence object
     * container.  This function also calls calls all of the listeners which are
     * listening to the Dataset object.  Mostly (ane even 'completely', at the
     * time of this writing), listeners are used for displaying the data in the
     * sequence list and the sequence text area.  Note that the add method
     * deletes a sequence object, as a whole, into the Dataset, while the delete
     * method deletes the text of a sequence object into the middle of the
     * already present sequence object in the Dataset.</p>
     *
     * <p>This function also obeys all sequence permissions/protections (as long
     * as the protect boolean is set to true).  In the case of text deletion,
     * text may NOT be deleted if it contains a protected character.  For
     * example, if the sequence is a protein sequence and the unambiguous
     * character protections are set to true, then the text "BZAZ" may not be
     * deleted because it contains one unambiguous character (A, for alanine);
     * however, the text "BZZZ" may be deleted, as long as the ambiguous
     * character protections are set to false.</p>
     **
     * @param x       the X-coordinate to begin the the sequence text deletion.
     * @param y       the Y-coordinate to begin the the sequence text deletion.
     * @param length  is the length (number of characters) of text to delete.
     * @param protect whether to test the protections of the sequence, already
     *                in the Dataset, before deleting the text.
     * @return true if the deletion was successful, otherwise false.
     */
    public boolean delete(int x, int y, int length, boolean protect) {
        Seq curr;
        char[] text = null;
        final int xend = x + length;
        boolean pseq   = false;
        boolean result = false;

        // ensure that the deletion co-ordinates are appropriate.
        // this is to prevent exceptions such as retrieving sequences
        // which are out of bounds, and performing deletions on negative
        // co-ordinates.
        //
        // Please note that this if-clause tests for length > 0.  There is an
        // else if clause below that handles length == 0, which counts as a
        // deletion (i.e. abstaining from action means deleting zero characters)
        if (y >= 0 && y <= getSize() && x >= 0 && length > 0) {
            curr = getLine(y);

            // ensure that the deletion does not exceed the sequence length
            if (xend <= curr.sequence.length()) {
                // do any character protection tests (if necessary)
                // to ensure that we are not deleting any protected characters
                if (protect) {
                    // Create an array of the characters to be deleted.  This
                    // array will be used to test characters for protection
                    // status.
                    text = new char[length];
                    curr.sequence.getChars(x, xend, text, 0);

                    // test protection status
                    pseq = isProtectionsOn(curr.type, curr.protect_align,
                        curr.protect_ambig,
                        curr.protect_unambig, text, 0, xend + 1);
                }

                // if the characters deleted from the sequence are
                // not protected, then delete them.
                if (!pseq) {
                    curr.sequence.delete(x, x + length);

                    // Deletes GenBank original copies of the sequence
                    // this is because we are modifying the sequence, so we do
                    // not want the original sequence to be exported.
                    curr.original = null;

                    // Call the canvas and notify it that the sequence length
                    // has changed (+ a positive length indicates an insertion;
                    // - a negative length indicates a deletion) hence we negate
                    // the length.
                    SequenceCanvas.textLengthChanged(x, y, 0 - length);
                    result = true;
                }
            }
        } else if (length == 0) {
            // if the length is zero, then technically we have deleted
            // zero characters by not performing any deletion.  Therefore,
            // the function will return true if length == 0.
            result = true;
        }
        return result;
    }

    /**
     * Adds sequences to the Dataset sequence object container.  This function
     * also calls calls all of the listeners which are listening to the Dataset
     * object.  Mostly (ane even 'completely', at the time of this writing),
     * listeners are used for displaying the data in the sequence list and the
     * sequence text area.  Note that the add method inserts a sequence object,
     * as a whole, into the Dataset, while the insert method inserts the text
     * of a sequence object into the middle of the already present sequence
     * object in the Dataset.
     **
     * @param  y    the line index number to insert the sequences object at.
     * @param  seqs the collection of sequence objects to insert.
     * @return true if the insertion was successful, otherwise false.
     */
    public boolean addSequences(int y, Collection<Seq> seqs) {
        boolean result = false;

        // ensure that the parameters are appropriate
        if (seqs != null && seqs.size() > 0 && y >= 0 && y <= getSize()) {
            // insert the sequences and notify all listener methods.
            result = lines.addAll(y, seqs);
            if (result) {
                fireIntervalAdded(this, y, y + seqs.size() - 1);
            }
        }
        return result;
    }

    /**
     * Removes multiple sequences from the Dataset sequence object container.
     **
     * @param indices an array of line numbers to remove.
     */
    public void removeSequences(final int[] indices) {
        // 'high_idx' corresponds to the largest number index for the interval
        // we are currently exploring within the line numbers array.
        int high_idx = indices.length - 1;

        // 'low_idx' corresponds to the smallest number index for the interval
        // we are currently exploring within the line numbers array.
        int low_idx = high_idx;

        // Sort the Y-coordinate values/line numbers.  This is done so we can
        // iterate backwards through a sorted list.  This will be explained
        // below.
        Arrays.sort(indices);

        // Ensure that there are height to delete.
        if (!lines.isEmpty() && indices != null) {
            // Iterate backwards through each sequence y number and delete it
            // this ensures that when deleting a line number, other line number
            // indices will not change when deleting.
            //
            // If another order was used, we could accidentally delete the wrong
            // sequences
            //
            // For example:
            //      suppose we have the following data
            //
            //          [0] = 'abcdef'
            //          [1] = 'ghij'
            //          [2] = 'klmn'
            //          [3] = 'opqr'
            //
            //      suppose we call removeSequences (new int[] {1,2,0});
            //
            //      the result we would expect would be
            //
            //          [0] = 'abcdef'
            //
            //      if we were not to change sort the line numbers and iterate
            //      backwards we would first delete sequence 1:
            //
            //          [0] = 'abcdef'
            //          [1] = 'klmn'
            //          [2] = 'opqr'
            //
            //      as you can see the indices changed, therefore when we delete
            //      sequence 2, we would get
            //
            //          [0] = 'abcdef'
            //          [1] = 'klmn'
            //
            //      and finally
            //
            //          [0] = 'klmn'
            //
            //      NOTE: this is not our expected result!
            //
            //      The problem of order can be solved by sorting the sequence
            //      line numbers, and removing them from highest to lowest
            //      (i.e. sorted array parsed in reversed order).
            //
            // We do not reverse the array, but instead parse it backwards,
            // because it is more efficient, and avoids "reinventing the wheel"
            // (Arrays.sort is a built in Java function, and reversal functions
            // would take more CPU time and computer memory to implement).
            while (low_idx >= 0) {
                // Make high_idx and low_idx the same value.
                high_idx = low_idx;

                // Try to form an interval.
                //
                // We try to form intervals because we want to minimize the
                // number of calls to the listener methods (i.e. increase the
                // speed of the program).
                //
                // This optimization is necessary, is because  creating objects
                // is very time-expensive, and each time we call
                // ListDataListener.intervalRemoved, we must create a new
                // ListDataEvent object.
                while (low_idx > 0 && indices[low_idx - 1]
                        == indices[low_idx] - 1) {
                    ungroup(lines.remove(indices[low_idx]));
                    low_idx--;
                }

                // remove the final line (NOTE: the above method will remove
                // every sequence in the interval, EXCEPT the last one (i.e. the
                // lowest index).
                ungroup(lines.remove(indices[low_idx]));

                // Send an interval removed event to all of this dataset's
                // ListDataListeners.
                fireIntervalRemoved(this, indices[low_idx], indices[high_idx]);
                low_idx--;
            }
        }
    }

//////////////////////
//******************//
//* DATA RETRIEVAL *//
//******************//
//////////////////////

    /**
     * Returns a count of the total number of sequences (lines) in the Dataset
     * sequence object container.
     **
     * @return the total number of sequences in the Dataset object.
     */
    public int getSize() {
        return lines.size();
    }
    
    /**
     * Retrieves a sequence object from the Dataset specified by its
     * Y-coordinate/line number.  If the Y-coordinate is invalid, a value of
     * null is returned instead.
     **
     * @param number the Y-coordinate of the sequence to retrieve.
     * @return the sequence retrieved (null if the Y-coordinate is invalid).
     * @see org.biolegato.sequence.data.Seq
     */
    Seq getLine(final int number) {
        // Ensures the Y-coordinate (number) is valid -- i.e. greater than zero,
        // and less than the maximum sequence number in the Dataset object.
        return (number >= 0 && number < getSize() ? lines.get(number) : null);
    }

    /**
     * Retrieves the length (in characters) of a given sequence 'line' in the
     * Dataset sequence object container.  The sequence is specified by its
     * Y-coordinate.  If the Y-coordinate value is invalid, a length of -1 is
     * returned.  Invalid Y-coordinates are values which exceed the total number
     * of sequences in the Dataset, or values less than zero.
     **
     * @param number the y coordinate to retrieve the sequence.
     * @return the length of sequence (-1 if 'number' is invalid).
     */
    public int getSequenceLength(final int number) {
        Seq current = getLine(number);

        return (current != null ? current.sequence.length() : -1);
    }

    /**
     * Retrieves the colour mask object for a given sequence 'line' in the
     * Dataset sequence object container.  The sequence is specified by its
     * Y-coordinate.  If the Y-coordinate value is invalid, the DEFAULT_MASK
     * object is returned instead.  Invalid Y-coordinates are values which
     * exceed the total number of sequences in the Dataset, or negative values.
     **
     * @param  number the line number to retrieve the colour mask for.
     * @return the colour mask for the given sequence.
     * @see org.biolegato.sequence.canvas.SequenceCanvas#DEFAULT_MASK
     */
    public ColourMask getMask(final int number) {
        Seq current = getLine(number);

        return (current != null ? current.mask : canvas.DEFAULT_MASK);
    }

    /**
     * <p>Retrieves the text contained within a sequence object in the Dataset.
     * The sequence object to extract text from is specified by its
     * Y-coordinate.  The offset and number of characters to extract from the
     * sequence are specified by 'offset' and 'length'.  The destination for the
     * text extracted is the character array 'array'.  If the Y-coordinate or
     * offset values are invalid, no data is extracted.  Invalid Y-coordinates
     * are values which exceed the total number of sequences in the Dataset, or
     * values less than zero.  Invalid offsets are those which exceed the number
     * of characters on the sequence "line", or offset values less than zero.
     * </p>
     *
     * <p>In contrast, if the length value exceeds the number of characters in
     * the sequence, the sequence's length is used instead.  Thus, the length
     * parameter may be slightly invalid (it may exceed the sequence length, but
     * NOT be less than zero).</p>
     **
     * @param  array  the destination for the characters to be copied to.
     * @param  number the sequence "line number"/Y-coordinate to extract data.
     * @param  offset the offset within the sequence to begin copying data from.
     * @param  length the number of characters to attempt to copy.
     * @return the number of characters actually copied.
     */
    public int getSequence(char[] array, int number, int offset, int length) {
        // The position within the sequence to end the sequence character
        // extraction.  On failure, or an extraction length of zero, setting the
        // 'endpos' to the value of 'offset' will ensure that the calculation
        // for the number of characters copied will work out to zero.
        int endpos  = offset;
        // Obtain the sequence object, within the Dataset, to extract sequence
        // characters from.  If the index ('number') is invalid, this parameter
        // will be null; hence, we will test for null later in the code.
        Seq current = getLine(number);

        // Ensure that the sequence was successfully extracted from the Dataset
        // object.  Also ensure that the array is not null, the offset is not
        // negative and the number of characters to extract (length) is greater
        // than zero (if zero, we don't extract any characters anyways).
        if (array != null && current != null && offset >= 0 && length > 0) {
            // Calculate the position within the sequence to end the extraction.
            // This will be limited to the maximum possible position in the
            // sequence to prevent overflow.
            endpos = Math.min(current.sequence.length(), offset + length);

            // Extract the characters from the sequence.
            if (endpos - offset > 0) {
                current.sequence.getChars(offset, endpos, array, 0);
            }
        }

        // Calculate the number of characters
        return endpos - offset;
    }

    /**
     * Called when a field in a sequence is modified.  This method is currently
     * called from within the Dataset class, and by the SequenceWindow class.
     * This method sends events to all of the listener objects listening to this
     * Dataset object.
     **
     * @param index the sequence "line number"/Y-coordinate modified.
     * @see org.biolegato.sequence.data.SequenceWindow
     */
    void sequenceChanged(final int index) {
        fireContentsChanged(this, index, index);
    }

///////////////
//***********//
//* GENERAL *//
//***********//
///////////////

    /**
     * Creates a new sequence-group comprised of the sequences specified by
     * Y-coordinate/"line numbers" in the array parameter 'sequences'.  Please
     * note that this method first "ungroups" all of sequences passed to it
     * before creating a new group.  This ensures that no sequence belongs to
     * more than one group at a time.
     **
     * @param sequences  the array of Y-coordinates corresponding to sequences
     *                   to include in the new group.
     */
    public void group(int[] sequences) {
        Seq seq;
        int maxGroupStart = maxGroup;

        // Ensure that there is an entry in group2seq to add the sequence to.
        while (group2seq[maxGroup] != null) {
            // Increase the new group number pointer.
            maxGroup++;
            maxGroup %= (group2seq.length - 1); // ensures wraparound
            
            // Should virtually never be run (except if someone is using a lot
            // of groups).  This code increases the maximum number of groups.
            if (maxGroup == maxGroupStart) {
                Set<Seq>[] tmp = new Set[group2seq.length + DEFAULT_MAX_GROUPS];
                System.arraycopy(group2seq, 0, tmp, 0, group2seq.length);
                group2seq = tmp;
                break;
            }
        }

        // Create a new hash set to house the groups.
        group2seq[maxGroup] = new HashSet<Seq>();

        // TODO: Collections.sort();

        // Add all of the new sequences to the group.
        for (int y : sequences) {
            // Get the sequence to add to the group.
            seq = lines.get(y);

            // Remove the sequence from any other group(s) it may belong to.
            ungroup(seq);

            // Set the groupID for the sequence.
            seq.groupID = maxGroup + 1;

            // Fire a sequence change event to all of the listeners.
            sequenceChanged(lines.indexOf(seq));

            // Add the sequence to the group2sequence translation hash table.
            group2seq[maxGroup].add(seq);
        }
    }

    /**
     * Ungroups an array of sequences by iteratively calling the single-sequence
     * ungroup function for each sequence number in the array.
     **
     * @param sequences the array of Y-coordinates/"line numbers", which
     *                  correspond to the sequences to ungroup.
     */
    public void ungroup(int[] sequences) {
        // The current sequence to ungroup.
        Seq seq;

        // Iterate through each of the Y-coordinates in the array.
        for (int y : sequences) {
            // Get the sequence to ungroup.
            seq = lines.get(y);

            // Remove the sequence from any group(s) it may belong to.
            ungroup(seq);

            // Set the group ID for the sequence to zero.
            seq.groupID = 0;

            // Fire a sequence change event to all of the listeners.
            sequenceChanged(lines.indexOf(seq));
        }
    }

    /**
     * Removes a single sequence from a group.
     **
     * @param seq the sequence to ungroup.
     */
    private void ungroup(Seq seq) {
        // The list of all sequences in the group which the sequence belongs to.
        Set<Seq> group = null;

        // Ensure that the sequence is not null.
        if (seq != null) {
            // Ensure that the groupID for the sequence is valid.
            if (seq.groupID > 0 && seq.groupID <= group2seq.length) {
                // Obtain all of the sequences in the same group as the sequence
                // which we will remove from the group.
                group = group2seq[seq.groupID - 1];

                // If the group is not null (i.e. a valid group), the proceed to
                // remove the sequence from the group.
                if (group != null) {
                    // Remove the sequence from the group.
                    group.remove(seq);

                    // If the group no longer has any other sequence in it, then
                    // remove the group from the sequence groups hash table.
                    if (group.size() <= 0) {
                        group2seq[seq.groupID - 1] = null;
                    }
                }
            }
        }
    }

    /**
     * Obtains all of the line numbers of the sequences in a particular group.
     * The group is specified to be the same group as the sequence on the line
     * number ('lineNumber').  Thus, another way to word this is "this function
     * obtains all of the sequences which belong to the same group as the
     * sequence contained at the Y-coordinate value 'lineNumber'".
     **
     * @param lineNumber  the Y-coordinate/"line number" of the sequence to
     *                    use as a reference for determining the group number.
     * @return an array containing all of the line numbers in the group.
     */
    public int[] getgroup(int lineNumber) {
        // A counter used for determining the position in the result array to
        // add the next sequence line number.
        int count = 0;
        // The result array to store all of the line numbers of the sequences
        // in the group.
        int[] result = null;
        // Stores the group number of the reference sequence contained on the
        // line 'lineNumber'.
        int groupNumber = getLine(lineNumber).groupID;
        // Stores all of the sequences in the current group.
        Set<Seq> group;

        // Ensure that the group number of the sequence is valid.
        if (groupNumber > 0 && groupNumber <= group2seq.length
                && group2seq[groupNumber - 1] != null) {
            // Obtain the set object containing all of the sequences in the
            // same group as the sequence specified by 'lineNumber'.
            group = group2seq[groupNumber - 1];

            // Create a new array object for storing the line numbers of the
            // sequences in the group.
            result = new int[group.size()];

            // Determine the line number of each sequence in the group and add
            // the line number to the result array.  This must be done because
            // group2seq houses direct sequence objects, and NOT line numbers.
            for (Seq c : group) {
                result[count] = lines.indexOf(c);
                count++;
            }
        }

        // Return the array of line numbers of sequences in the same group as
        // the sequence contained on the line 'lineNumber'.
        return result;
    }

////////////////////////////
//************************//
//* LIST MODEL FUNCTIONS *//
//************************//
////////////////////////////
    /**
     * Returns the names of sequences based on their Y-coordinate/"line number".
     * This function is used to interface with the JList object.  Please note
     * that the Sequence objects returned will print their sequence name and
     * group number (in a formatted string), when their toString methods are
     * called.
     **
     * @param  index the Y-coordinate of the sequence to obtain.
     * @return returns the sequence object at the specified Y-coordinate.
     *         In turn, this sequence object will return its sequence name when
     *         its toString method is called.
     * @see org.biolegato.sequence.data.Seq#toString() 
     */
    public Object getElementAt(int index) {
        return getLine(index);
    }

/////////////////////////////////
//*****************************//
//* PROTECTION STATUS METHODS *//
//*****************************//
/////////////////////////////////
    /**
     * <p>Checks a string against all of a sequence's protection settings.  A
     * string of text is said to violate the protection settings (for deletion
     * or insertion purposes) if the text contains any characters which are
     * protected.  Whether a character is protected is determined by the Seq
     * object's protection status settings.  These protection status settings
     * are split into three variables (for 3 classes of characters):</p>
     * <ol>
     *      <li><b>protect_align</b> -- for alingment characters.  These
     *              are the characters which represent alignment gaps, such as
     *              dashes and whitespace.  For a complete list of alignment
     *              characters, please see the Seq object's protect_align
     *              field specification.</li>
     *
     *      <li><b>protect_ambig</b> -- for ambiguous sequence characters.
     *              Ambiguous sequence characters are characters which can
     *              represent more than one item (such as nucleotide or amino
     *              acid) in a sequence.  For example, 'N' is an ambiguous DNA
     *              character, because it can represent any DNA nucleotide.
     *              For a complete list of ambiguous sequence characters, please
     *              see the Seq object's protect_ambig field specification.</li>
     *
     *      <li><b>protect_uambig</b> -- for unambiguous sequence characters.
     *              Unambiguous sequence characters are characters which can
     *              represent only one item (e.g. nucleotide or amino acid) in a
     *              sequence.  For example, 'A' is an unambiguous DNA character,
     *              because it only can represent one DNA nucleotide (Adenine).
     *              For a complete list of unambiguous sequence characters,
     *              please see the Seq object's protect_unambig field
     *              specification.</li>
     * </ol>
     * <p>If a character from any of the three classes is matched in the text
     * passed to this function, and the character matched has its protection
     * status is true, the text is barred from insertion or deletion.  In
     * contrast, any character matched in the text with its character protection
     * status set to false is ignored.  Thus text containing all characters
     * which have false protection statuses may be inserted or deleted.  A
     * couple of examples will help demonstrate this:</p>
     * <ol>
     *      <li>Consider adding the text 'BZZ' to a protein sequence.  If the
     *          ambiguous character protection is off (false), then the text
     *          should be added without problem, regardless of the protection
     *          status for alignment or unambiguous characters.  Thus, this
     *          function will return the value of false.</li>
     *      <li>Consider adding the text 'BZAZ' to a protein sequence.  If the
     *          ambiguous character protection is off (false), but the
     *          unambiguous character protection status is on (true) then the
     *          text cannot be added because it contains the amino acid Alanine,
     *          which is a protected unambiguous amino acid.  Thus the function
     *          will return the value of true.</li>
     *      <li>Now, consider adding the same text as above ('BZAZ') to the same
     *          protein sequence, except now both the ambiguous character
     *          protection is off (false) and the unambiguous character
     *          protection status is also off.  Now, the text may be added to
     *          the protein sequence without problems.</li>
     * </ol>
     * <p><b><i>Please note that ON/OFF are interchangeable in this document
     * with the terms TRUE/FALSE, where ON is TRUE, and OFF is FALSE.</i></b>
     * </p>
     *
     * <p><b><i>Please also note that only DNA, RNA and PROTEIN sequences have
     *  protection settings, thus any non DNA, RNA, or PROTEIN sequence type
     *  tested will always return false, regardless of the text.  An example
     *  of a non-RNA/DNA/PROTEIN sequence is Seq.Type.TEXT.</i></b></p>
     **
     * @param type           the type of the sequence to test.
     * @param protect_align  the protection status of alignment characters.
     * @param protect_ambig  the protection status of ambiguous characters.
     * @param protect_uambig the protection status of unambiguous characters.
     * @param test           the sequence text to test.
     * @param start          the index within 'text' to start the test.
     * @param end            the index within 'text' to end the test.
     * @return true if the text violates the protection settings of the
     *         sequence, and hence the text should not be inserted or deleted.
     * @see org.biolegato.sequence.data.Seq#protect_align
     * @see org.biolegato.sequence.data.Seq#protect_ambig
     * @see org.biolegato.sequence.data.Seq#protect_unambig
     * @see org.biolegato.sequence.data.Seq.Type#DNA
     * @see org.biolegato.sequence.data.Seq.Type#RNA
     * @see org.biolegato.sequence.data.Seq.Type#PROTEIN
     * @see org.biolegato.sequence.data.Seq.Type#MASK
     * @see org.biolegato.sequence.data.Seq.Type#TEXT
     * @see org.biolegato.sequence.data.Dataset#insert(int, int, char[], int, int, boolean)
     * @see org.biolegato.sequence.data.Dataset#delete(int, int, int, boolean)
     */
    public static boolean isProtectionsOn(Seq.Type type,
            boolean protect_align, boolean protect_ambig,
            boolean protect_uambig, char[] test, int start, int end) {
        // The result of this function (whether the text tested can be inserted
        // or deleted into a given sequence, based on sequence protections).
        // By default this value is false (because types not recognized by this
        // function, such as TEXT will count as having no protection status by
        // this function).
        boolean protect = false;

        // Ensure that the end point does not exceed the test array's length.
        end = Math.min(end, test.length - 1);

        // Ensure that at least one proection setting is on, before testing.
        // Otherwise there is no reason to test (because if all protection
        // settings are off, then the text is unprotected and any character may
        // be inserted or deleted).
        if (protect_ambig || protect_uambig || protect_align) {
            // Branch based on whether the sequence is a nucleotide sequence, or
            // an amino acid/protein sequence.  If the sequence is neither an
            // amino acid or nucleotide sequence, then this method will return
            // false (i.e. the text to be inserted or deleted is unprotected).
            if (type == Seq.Type.DNA || type == Seq.Type.RNA) {
                for (int count = start; !protect && count <= end; count++) {
                    // Iterate through each character in the text string until
                    // we either reach the end of the string, or a protected
                    // character is found.  Character types are based on a
                    // combination of the sources listed below and a thorough
                    // testing of character protections in GDE.
                    //
                    // SOURCES:
                    //  http://home.cc.umanitoba.ca/~psgendb/formats.html
                    switch (Character.toLowerCase(test[count])) {
                        case 'b':   // G or T or C
                        case 'd':   // G or T or A
                        case 'h':   // A or C or T
                        case 'i':   // RESERVED(?) -- Copied behaviour from GDE.
                        case 'k':   // G or T
                        case 'm':   // A or C
                        case 'n':   // Any
                        case 'r':   // Purine (A or G)
                        case 's':   // G or C
                        case 'v':   // G or C or A
                        case 'w':   // A or T
                        case 'y':   // Pyrimidine (C or T)
                            protect = protect_ambig;
                            break;

                        case 'a':   // Adenosine (A)
                        case 'c':   // Cytosine  (C)
                        case 'g':   // Guanine   (G)
                        case 't':   // Thymine   (T)
                        case 'u':   // Uracil    (U)
                            protect = protect_uambig;
                            break;
                            
                        default:
                            protect = protect_align;
                            break;
                    }
                }
            } else if (type == Seq.Type.PROTEIN) {
                for (int count = start; !protect && count < end; count++) {
                    // Iterate through each character in the text string until
                    // we either reach the end of the string, or a protected
                    // character is found.  Character types are based on a
                    // combination of the sources listed below and a thorough
                    // testing of character protections in GDE.
                    //
                    // SOURCES:
                    //  http://home.cc.umanitoba.ca/~psgendb/formats.html
                    //  http://www.ddbj.nig.ac.jp/sub/ref2-e.html
                    //  http://www.bioinformatics.org/sms/iupac.html
                    switch (Character.toLowerCase(test[count])) {
                        // Various standard alignment and whitespace characters.
                        case ' ': case '\n': case '\t': case '\r': case '-':
                            protect = protect_align;
                            break;

                        case 'b':   // Aspartic acid or Asparagine (Asx)
                        case 'j':   // Leucine or isoleucine       (Leu or Ile)
                        case 'x':   // UNKNOWN                     (ANY)
                        case 'z':   // Glutamic acid or Glutamine  (Glx)
                        case '*':   // STOP
                            protect = protect_ambig;
                            break;

                        // The current implementation treats every character
                        // (not just letter) -- excluding NOT B, J, X, Z, *, a
                        // whitespace character (space, tab, new-line, carriage
                        // return), or a dash -- as an unambiguous sequence
                        // character.  To change this implementation to the
                        // such that only letters are considered (and everything
                        // which is not a letter is considered an alignment
                        // gap), move the 'default:' case to the end of the
                        // protect_align multiple-case statement.
                        case 'a':   // Alanine (Ala)
                        case 'c':   // Cysteine (Cys)
                        case 'd':   // Aspartic Acid (Asp)
                        case 'e':   // Gluamic Acid (Glu)
                        case 'f':   // Phenylalanine (Phe)
                        case 'g':   // Glycine (Gly)
                        case 'h':   // Histidine (His)
                        case 'i':   // Isoleucine (Ile)
                        case 'k':   // Lysine (Lys)
                        case 'l':   // Leucine (Leu)
                        case 'm':   // Methionine (Met)
                        case 'n':   // Asparagine (Asn)
                        case 'o':   // **Pyrrolysine (Pyl) -- NON-STANDARD AA!
                        case 'q':   // Glutamine (Gln)
                        case 'p':   // Proline (Pro)
                        case 'r':   // Arginine (Arg)
                        case 's':   // Serine (Ser)
                        case 't':   // Threonine (Thr)
                        case 'u':   // Selenocysteine (Sec)
                        case 'v':   // Valine (Val)
                        case 'w':   // Tryptophan (Trp)
                        case 'y':   // Tyrosine (Tyr)
                        default:
                            protect = protect_uambig;
                            break;
                    }
                }
            }
        }

        // Return the status of the protections test.
        return protect;
    }
}
