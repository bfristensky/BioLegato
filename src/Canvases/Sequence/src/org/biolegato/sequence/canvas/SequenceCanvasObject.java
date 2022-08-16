/*
 * SequenceCanvasObject.java
 *
 * Created on August 26, 2010, 11:30 AM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.biolegato.sequence.canvas;

import java.util.Scanner;
import java.io.IOException;
import org.biolegato.sequence.data.DataFormat;

/**
 * An interface for abstracting the two canvas objects (SequenceTextArea and
 * SequenceList) within BioLegato's Sequence canvas.  This is useful for
 * streamlining access to both, especially when using Sequence canvas specific
 * menu commands, which alter the currently selected sequence.
 **
 * @author Graham Alvare
 * @author Brian Fristensky
 */
public interface SequenceCanvasObject {

    /**
     * Cuts/deletes the content selected in the sequence canvas object.
     */
    public void deleteSelection();

    /**
     * Writes the current contents of the canvas object to an Appendable object.
     **
     * @param  format       the format to write the data in.
     * @param  dest         the destination Appendable object to write the data.
     * @throws IOException  throws an IOException if there is any error
     *                      appending the data to the Appendable object.
     */
    public void writeOut(DataFormat format, Appendable dest) throws IOException;

    /**
     * Reads contents into the current sequence canvas object.
     **
     * @param  format       the file format to write the data in.
     * @param  source       the data source to read data from.
     * @param  overwrite    whether to overwrite the data currently selected
     *                      in the canvas object.
     * @throws IOException  throws an IOException if there is any error
     *                      reading data from the Scanner object.
     */
    public void readIn(DataFormat format, Scanner source, boolean overwrite)
                                                            throws IOException;

    /**
     * <dl><dt>Changes the case of the currently selected sequence.  If the
     * sequence is of inconsistent case, the case of the first character as the
     * case of the sequence, for case conversion.  For example:</dt>
     *
     *      <dd><p>xYzJWwa23  --- will become ---> WYZJWWA23</p></dd>
     * </dl>
     */
    public void changeCase ();
}
