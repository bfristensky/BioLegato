package org.biolegato.sequence.canvas;

/*
 * SequenceTextArea.java
 *
 * Created on August 18, 2008, 11:10 AM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */
import javax.swing.JComponent;
import org.biolegato.sequence.data.Dataset;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.io.IOException;
import java.util.HashSet;
import java.util.Scanner;
import java.util.Set;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;
import org.biolegato.main.BLMain;
import org.biolegato.sequence.data.DataFormat;

/**
 * <p>A general canvas with more functionality support than JTextArea.</p>
 *
 * <p>This canvas was originally created to support the rectangular selection
 * model, and has since been upgraded to also support different colour schemes,
 * sequence documents, and many other features.</p>
 *
 * <p>The rectangular selection model is based on GDE.  In the rectangular
 * selection model (also called the "box" or "column" selection model), a user
 * selects text by "drawing" a box around it.  This method is described on the
 * following page as the "box" selection model:
 * http://www.physics.nyu.edu/grierlab/idl_html_help/Editor6.html</p>
 *
 * <p>Colour schemes are provided by the ColourMask class.</p>
 **
 * @author Graham Alvare
 * @author Brian Fristensky
 * @see org.biolegato.sequence.canvas.ColourMask
 */
public class SequenceTextArea extends JComponent implements ListDataListener,
        SequenceCanvasObject, KeyListener, MouseMotionListener, MouseListener {

    /**
     * The height of each row (in pixels) in the text area.  This is calculated
     * based on the current font and any padding.  Storing the height is
     * required not only to draw text accurately, but also to ensure that the
     * SequenceList and SequenceTextArea scroll synchronously.
     */
    protected int rowHeight = 1;
    /**
     * The width of each column in the text area.  This is made uniform to make
     * calculations easier.  To ensure uniformity, even in variable width fonts,
     * we use the widest character in most fonts ('G') to determine the column
     * width.  Still, it is strongly recommended to only allow and use
     * fixed-width fonts with the SequenceTextArea just-in-case.  Storing the
     * column is required to draw text accurately and ensure proper scrolling
     * is available (to set the scroll bar length by adjusting the window's
     * size and preferred size).
     */
    protected int colWidth = 1;
    /**
     * The canvas associated with the text area.  This is used for communication
     * directly with the SequenceCanvas, and indirectly with the SequenceList.
     * Also, some default parameters and menu items (for the right-click menu)
     * are obtained from the SequenceCanvas.
     */
    private SequenceCanvas canvas = null;
    /**
     * If set to true, when the user types text, it will overwrite whatever
     * existing text is already under the cursor.  This is the same behaviour of
     * "INSERT MODE" (controlled by pressing the INSERT key) in most word
     * processors, such as Word and OpenOffice.  Therefore, this mode is
     * controlled, within the SequenceTextArea, by pressing the INSERT key.
     * (Pressing the INSERT key turns this mode ON/OFF.)
     */
    protected boolean insertMode = false;
    /**
     * <p>Used to track whether the shift key is pressed.  This will determine
     * whether the selection is resized or cancelled when the user moved the
     * cursor using the keyboard (up/down/left/right), or when the user clicks
     * somewhere within the text area.</p>
     * 
     * <p><b>To sum up: (1)</b> if this variable is set to <b>false</b>, then
     * the SHIFT key is NOT currently pressed, therefore moving the cursor in
     * the text area should CANCEL the current selection (if any text is
     * selected); <b>(2)</b> if this variable is set to <b>true</b> then the
     * SHIFT key IS currently pressed, therefore moving the cursor in the text
     * area should SHRINK OR STRETCH the current selection (or create a new
     * selection if no text is selected).</p>
     **
     * @see org.biolegato.sequence.canvas.SequenceTextArea#selectionMouse
     */
    protected boolean selectionMove = false;
    /**
     * Used to track mouse holds and releases.  Like selectionMove, this
     * variable is also used for determining whether to stretch/shrink, or
     * cancel selections on cursor movement; however, this variable is ONLY
     * applicable to mouse cursor movements.  This variable is set to true when
     * the mouse is drag-clicked, and set to false when the mouse button is
     * released.  If this variable is false, moving the mouse will cancel the
     * current selection (if any text is selected).  If this variable is true,
     * moving the mouse cursor will stretch or shrink the current selection
     * (or create a new selection if nothing is currently selected).
     **
     * @see org.biolegato.sequence.canvas.SequenceTextArea#selectionMove
     */
    protected boolean selectionMouse = false;
    /**
     * The current column number/X-coordinate of the text caret cursor.
     * This value is used for many calculations, including text insertions and
     * deletions, text selection, and drawing.  For speed and to make code
     * maintenance easier, the text caret position is only modified by the
     * changePosition function.
     **
     * @see org.biolegato.sequence.canvas.SequenceTextArea#row
     * @see org.biolegato.sequence.canvas.SequenceTextArea#sx
     * @see org.biolegato.sequence.canvas.SequenceTextArea#sy
     * @see org.biolegato.sequence.canvas.SequenceTextArea#changePosition(boolean, int, int)
     */
    protected int col = 0;
    /**
     * The current row number/Y-coordinate of the text caret cursor.
     * This value is used for many calculations, including text insertions and
     * deletions, text selection, and drawing.  For speed and to make code
     * maintenance easier, the text caret position is <u>ONLY</u> modified by
     * the changePosition function.
     **
     * @see org.biolegato.sequence.canvas.SequenceTextArea#col
     * @see org.biolegato.sequence.canvas.SequenceTextArea#sx
     * @see org.biolegato.sequence.canvas.SequenceTextArea#sy
     * @see org.biolegato.sequence.canvas.SequenceTextArea#changePosition(boolean, int, int)
     */
    protected int row = 0;
    /**
     * <p>The column number/X-coordinate of the selection start position.</p>
     * <p>When moving the cursor without selecting text, the sx and sy variables
     *    follow (contain the same values as) the current text caret ('row' and
     *    'col' variables).  However, when a text selection is begun (by holding
     *    on the shift key or dragging the mouse), the sx and sy variables are
     *    fixed the position where the text selection began (they no longer
     *    follow the text caret).  Therefore, the selection area can be
     *    calculated as the difference between the text caret position ('row'
     *    and 'col') and the selection position ('sx' and 'sy').</p>
     * <p>For speed and to make code maintenance easier, the selection start
     *    position is <u>ONLY</u> modified by the changePosition function.
     *    Also for speed, with the exception of isSelectionEmpty(), every other
     *    function, which handles selections, uses minsx and minsy instead of
     *    sx directly.  This is because virtually every selection algorithm
     *    needs to perform calculations based on the difference between the two
     *    points in a selection (the start and end points); using minsx and
     *    maxsx simplifies these calculations, and avoids negative numbers.</p>
     * <p><i>NOTE: if 'sx' is is the same as the variable 'col', then there
     *             is currently no text selected.</i></p>
     **
     * @see org.biolegato.sequence.canvas.SequenceTextArea#row
     * @see org.biolegato.sequence.canvas.SequenceTextArea#col
     * @see org.biolegato.sequence.canvas.SequenceTextArea#sy
     * @see org.biolegato.sequence.canvas.SequenceTextArea#changePosition(boolean, int, int)
     * @see org.biolegato.sequence.canvas.SequenceTextArea#isSelectionEmpty()
     */
    private int sx = 0;
    /**
     * <p>The row number/Y-coordinate of the selection start position.</p>
     * <p>When moving the cursor without selecting text, the sx and sy variables
     *    follow (contain the same values as) the current text caret ('row' and
     *    'col' variables).  However, when a text selection is begun (by holding
     *    on the shift key or dragging the mouse), the sx and sy variables are
     *    fixed the position where the text selection began (they no longer
     *    follow the text caret).  Therefore, the selection area can be
     *    calculated as the difference between the text caret position ('row'
     *    and 'col') and the selection position ('sx' and 'sy').</p>
     * <p>For speed and to make code maintenance easier, the selection start
     *    position is <u>ONLY</u> modified by the changePosition function.
     *    Also for speed, every function, which handles selections, uses minsy
     *    and minsy instead of sy directly.  This is because virtually every
     *    selection algorithm needs to perform calculations based on the
     *    difference between the two points in a selection (the start and end
     *    points); using minsy and maxsy simplifies these calculations, and
     *    avoids negative numbers.</p>
     * <p><i>NOTE: if 'sy' is is the same as the variable 'col', then there
     *             is MAY STILL be text selected.  However, the text selected
     *             would be limited within ONE row.</i></p>
     **
     * @see org.biolegato.sequence.canvas.SequenceTextArea#row
     * @see org.biolegato.sequence.canvas.SequenceTextArea#col
     * @see org.biolegato.sequence.canvas.SequenceTextArea#sx
     * @see org.biolegato.sequence.canvas.SequenceTextArea#changePosition(boolean, int, int)
     */
    private int sy = 0;
    /**
     * <p>The mathematical minimum of the X-coordinates for the current text
     * position (caret) and the selection start position.  Although this value
     * could be calculated on the fly by using the Math.min function, this value
     * is instead cached to enable faster drawing.</p>
     * <p>This variable is also used in other bodies of code (such as text
     *    deletion and insertion code) because using this variable, instead
     *    using of sx/sy directly, can also speed up these other functions.</p>
     *
     * <p><i>NOTE: this value should only be modified by the
     *             <u>changePosition</u> function!</i></p>
     **
     * @see org.biolegato.sequence.canvas.SequenceTextArea#sx
     * @see org.biolegato.sequence.canvas.SequenceTextArea#minsy
     * @see org.biolegato.sequence.canvas.SequenceTextArea#maxsx
     * @see org.biolegato.sequence.canvas.SequenceTextArea#changePosition(boolean, int, int)
     */
    private int minsx = 0;
    /**
     * <p>The mathematical minimum of the Y-coordinates for the current text
     * position (caret) and the selection start position.  Although this value
     * could be calculated on the fly by using the Math.min function, this value
     * is instead cached to enable faster drawing.</p>
     * <p>This variable is also used in other bodies of code (such as text
     *    deletion and insertion code) because using this variable, instead
     *    using of sx/sy directly, can also speed up these other functions.</p>
     *
     * <p><i>NOTE: this value should only be modified by the
     *             <u>changePosition</u> function!</i></p>
     **
     * @see org.biolegato.sequence.canvas.SequenceTextArea#sy
     * @see org.biolegato.sequence.canvas.SequenceTextArea#minsx
     * @see org.biolegato.sequence.canvas.SequenceTextArea#maxsy
     * @see org.biolegato.sequence.canvas.SequenceTextArea#changePosition(boolean, int, int)
     */
    private int minsy = 0;
    /**
     * <p>The mathematical maximum of the X-coordinates for the current text
     * position (caret) and the selection start position.  Although this value
     * could be calculated on the fly by using the Math.min function, this value
     * is instead cached to enable faster drawing.</p>
     * <p>This variable is also used in other bodies of code (such as text
     *    deletion and insertion code) because using this variable, instead
     *    using of sx/sy directly, can also speed up these other functions.</p>
     *
     * <p><i>NOTE: this value should only be modified by the
     *             <u>changePosition</u> function!</i></p>
     **
     * @see org.biolegato.sequence.canvas.SequenceTextArea#sx
     * @see org.biolegato.sequence.canvas.SequenceTextArea#minsx
     * @see org.biolegato.sequence.canvas.SequenceTextArea#maxsy
     * @see org.biolegato.sequence.canvas.SequenceTextArea#changePosition(boolean, int, int)
     */
    private int maxsx = 0;
    /**
     * <p>The mathematical maximum of the X-coordinates for the current text
     * position (caret) and the selection start position.  Although this value
     * could be calculated on the fly by using the Math.min function, this value
     * is instead cached to enable faster drawing.</p>
     * <p>This variable is also used in other bodies of code (such as text
     *    deletion and insertion code) because using this variable, instead
     *    using of sx/sy directly, can also speed up these other functions.</p>
     *
     * <p><i>NOTE: this value should only be modified by the
     *             <u>changePosition</u> function!</i></p>
     **
     * @see org.biolegato.sequence.canvas.SequenceTextArea#sy
     * @see org.biolegato.sequence.canvas.SequenceTextArea#minsy
     * @see org.biolegato.sequence.canvas.SequenceTextArea#maxsx
     * @see org.biolegato.sequence.canvas.SequenceTextArea#changePosition(boolean, int, int)
     */
    private int maxsy = 0;
    /**
     * The right click menu for the text area.  This menu is displayed whenever
     * the user right-clicks the mouse anywhere in the text area.  This value
     * is modified by both the SequenceTextArea class, and by the SequenceCanvas
     * class.
     **
     * @see org.biolegato.sequence.canvas.SequenceCanvas
     */
    protected JPopupMenu popup = new JPopupMenu();
    /**
     * This reference is used to connect the data model/Dataset sequence object
     * container to the text area.  This object is used for painting and editing
     * sequence objects.  Also, this datamodel ensures proper synchronicity
     * between the SequenceList, SequenceCanvas and SequenceTextArea objects.
     */
    protected Dataset datamodel;
    /**
     * Stores the length of the longest line in the text area.  This value is
     * <u>ONLY</u> updated by the function: updateLength.  This value is stored
     * to ensure that the vertical scroll bar has an appropriate length.  The
     * vertical scroll bar size is controlled by the text area's size (getSize,
     * setSize) and preferred size (getPreferredSize, setPreferredSize).  The
     * actual functionality of changing the text area size (and preferred size)
     * is done by the function refreshSize.
     **
     * @see org.biolegato.sequence.canvas.SequenceTextArea#refreshSize()
     * @see org.biolegato.sequence.canvas.SequenceTextArea#updateLength(int, int)
     * @see javax.swing.JComponent#getSize()
     * @see javax.swing.JComponent#setSize(java.awt.Dimension) 
     * @see javax.swing.JComponent#getPreferredSize()
     * @see javax.swing.JComponent#setPreferredSize(java.awt.Dimension)
     */
    private int longestline = 0;
    /**
     * Self-reference (used for inner-classes).
     */
    public final SequenceTextArea blTextAreaSelf = this;
    /**
     * The foreground colour of selected text.
     */
    public static final Color SELECTFG = Color.WHITE;
    /**
     * The background colour of selected text.
     */
    public static final Color SELECTBG = Color.BLUE;
    /**
     * This constant is used for serialization purposes.  Also, this value is
     * required to compile this class without warnings.
     */
    public static final long serialVersionUID = 7526472295622777004L;

    /**
     * Creates a new instance of a SequenceTextArea.
     **
     * @param canvas    the parent canvas of the text area.
     * @param datamodel the data model to view the text from.
     */
    public SequenceTextArea(SequenceCanvas canvas, Dataset datamodel) {
        // Import all applicable constructor parameters into the object.
        this.datamodel = datamodel;
        this.canvas = canvas;

        // Configure the text area to listen for events (such as key presses).
        datamodel.addListDataListener(blTextAreaSelf);
        addKeyListener(blTextAreaSelf);
        addMouseListener(blTextAreaSelf);
        addMouseMotionListener(blTextAreaSelf);

        // Create the popup menu; however, first check whether the "readonly"
        // property is set to "true".  If the "readonly" property is set to
        // "true", then skip adding any edit commands to the popup menu.
        //
        // (In the current implementation, all commands are skipped, because
        // all of the commands in the popup menu are currently edit commands.)
        if (canvas.editable) {
            popup.add(new JMenuItem(canvas.cutAct));
            popup.add(new JMenuItem(canvas.copyAct));
            popup.add(new JMenuItem(canvas.pasteAct));
        }

        // Configure the display parameters for the text area.
        setFont(canvas.currentFont);
        setForeground(Color.BLACK);
        setBackground(Color.WHITE);
        setFocusTraversalKeysEnabled(false);

        // Adjust the canvas size.
        updateLength(0, datamodel.getSize());
        refreshSize();
        
        // Paint the text area.
        setDoubleBuffered(true);
        repaint();
    }

///////////////////////
//*******************//
//* DISPLAY METHODS *//
//*******************//
///////////////////////
    /**
     * Paints the current sequence text area component to a graphics object.
     * This function uses the current clip bounds to determine the area which
     * needs to be redrawn.  By using the current clip bounds, this method can
     * work faster by only redrawing the areas of the component which need
     * redrawing (i.e. areas which have been updated).
     **
     * @param gfx the graphics instance to paint the window to.
     */
    @Override
    public void paintComponent(Graphics gfx) {
        try {
            ////////////////////////////////
            // Print adjustment constants //
            ////////////////////////////////
            // These vareiables are used to offset X and Y coordinate
            // calculations for printing.  The fontcenterXmod and fontcenterYmod
            // refer to empirically determined values which must be added to X
            // and Y co-ordinates, such that the font is centred in the text
            // area, and synchronized with the sequence list (so that they
            // scroll together).
            final int fontcenterXmod = colWidth / 2;
            final int fontcenterYmod = -5;

            /////////////////////////////////////
            // Drawing area boundary constants //
            /////////////////////////////////////
            // The rectangular drawing bounds for the paintComponent call.
            // This is used to determine the area which needs to be painted,
            // thereby avoiding the need to print the entire canvas every time
            // this function is called.
            final Rectangle area = gfx.getClipBounds();

            // The variables below store the drawing area coordinates as row,
            // column (sequence) coordinates instead of X/Y screen coordinates.
            // This conversion is necessary for determining what characters and
            // sequences need to be printed.
            final int startcol  = area.x / colWidth;
            final int startrow  = area.y / rowHeight;
            final int collength = (area.width / colWidth) + 2;
            final int stoprow   = Math.max(0, Math.min(startrow +
                    (area.height / rowHeight) + 2, datamodel.getSize() - 1));

            ///////////////////////////////////////
            // Selection area boundary constants //
            ///////////////////////////////////////
            // The column coordinates of the selection zone, revised to be
            // relative to the beginning of the portion of text visible.
            // For example, if we were to copy an array containing all of the
            // sequence from row Y, starting from column X (the first column we
            // are drawing), minsxcol and maxsxcol would be relative to X.
            // These revised coordinates are calculated, because we are
            // extracting only a subset of the sequence for printing (based on
            // the drawing area boundary constants).  Therefore, we need to
            // calculate how to print the text based on the subset extracted.
            final int minsxcol = Math.max(0, minsx - startcol);
            final int maxsxcol = Math.max(0, maxsx - startcol);

            //////////////////////////////////////////
            // Graphics screen X and Y co-ordinates //
            //////////////////////////////////////////
            // The position to begin drawing characters at.  These coordinates
            // are based on the clip area, but rounded to the next larger size.
            // For instance, if the clip area is from colum 0.9 to 3.2, instead
            // of drawing columns from 1 to 3, we will draw from 0 to 4 (the
            // next largest size).  Therefore, we recalculate all of the screen
            // coordinates to fit this rounding model.
            final int xstart      = (startcol * colWidth)    + fontcenterXmod;
            final int ystart      = (startrow * rowHeight)   + fontcenterYmod
                                                             + rowHeight;

            // Calculate where the selection area should be on the screen
            // (within each row printed).
            final int startSelectX = xstart + (minsxcol * colWidth);
            final int endSelectX   = xstart + (maxsxcol * colWidth);

            // Calculate where to place the current text caret/cursor
            // (in X, Y screen co-ordinates).
            final int cursorX      = (col * colWidth) + fontcenterXmod;
            final int cursorY      = (row * rowHeight) + fontcenterYmod;

            /////////////////////////////////////////////////////
            // Define temporary storage structures for looping //
            /////////////////////////////////////////////////////
            // These variables are declared outside the loop, so they can be
            // printed in debug messages (if the printing fails).
            int curr_row = startrow;
            int curr_y = ystart;
            int datalength = -1;
            char[] print = new char[collength];
            ColourMask mask = null;

            try {
                // Print the normal background first.  This must be done because
                // selected text has a background different from the normal
                // text background; therefore, we cannot ensure that the text
                // we are drawing was not previously selected, and thus a
                // different background colour.  Also, redrawing the background
                // clears any problems created by redrawing different text.
                gfx.setFont(getFont());
                gfx.setColor(getBackground());
                gfx.fillRect(startcol * colWidth, startrow * rowHeight,
                        collength * colWidth,
                        (stoprow - startrow + 1) * rowHeight);

                // Paint the background for any selected text (the blue part).
                if (startSelectX >= 0 && startSelectX < endSelectX
                        && maxsy >= startrow && minsy <= stoprow) {
                    // Determine the box area to print the text (Y-coordinates).
                    final int startbox = Math.max(startrow, minsy);
                    final int endbox = Math.min(stoprow, maxsy) - startbox + 1;

                    // Print the blue box (background).
                    gfx.setColor(SELECTBG);
                    gfx.fillRect(startSelectX, startbox * rowHeight
                            + (fontcenterYmod / 2),
                            (endSelectX - startSelectX),
                            endbox * rowHeight);
                }

                // Print each sequence row within the print area.
                for (curr_row = startrow, curr_y = ystart;
                        curr_row <= stoprow; curr_row++, curr_y += rowHeight) {

                    // Extract a character array of sequence characters to print
                    // within the current row.
                    datalength = datamodel.getSequence(print, curr_row,
                                                       startcol, collength);

                    // If the array length is greater than zero, then there are
                    // characters to print.  If not, then the row can be skipped
                    // because the row's sequence does not contain any
                    // characters within the printing/clip range given to this
                    // method/function.
                    if (datalength > 0) {

                        // Use the colour masks specified for the current
                        // sequence object, if available.  If no colour mask is
                        // available for the current sequence, then use the
                        // default sequence colour mask.
                        mask = datamodel.getMask(curr_row);

                        // Check whether the segment of the current row
                        // to be drawn contains a portion of selected text.
                        // If so, we must draw the selected text differently
                        // (using the selected text foreground and background).
                        if (curr_row >= minsy && curr_row <= maxsy
                                              && maxsx >= startcol) {
                            /////////////////////////////////
                            // UNSELECTED TEXT TO THE LEFT //
                            /////////////////////////////////
                            // Since the line contains selected text, check
                            // whether the selection starts before or after the
                            // starting position of the area to draw.  If it is
                            // after, then we must begin drawing the unselected
                            // before (to the left of) the selected text.
                            if (minsx > startcol) {
                                // Branch based on whether a colour mask is
                                // available to use for drawing.
                                if (mask != null) {
                                    // If a colour mask for the sequence is
                                    // available, use it.
                                    mask.drawString(gfx, print, 0,
                                            Math.min(datalength, minsxcol),
                                            xstart, curr_y);
	                        } else if (canvas.DEFAULT_MASK != null) {
                                    // If no colour mask is available for the
                                    // sequence, BUT a default mask is available
                                    // use the default mask.
	                            canvas.DEFAULT_MASK.drawString(gfx, print,
                                            0, datalength, xstart, curr_y);
                                } else {
                                    // If no colour mask is available for the
                                    // sequence, and a default colour mask is
                                    // also unavailable, use the default
                                    // foreground colour (Colourmask.FOREG).
                                    gfx.setColor(ColourMask.FOREG);
                                    gfx.drawChars(print, 0, Math.min(datalength,
                                            minsxcol), xstart, curr_y);
                                }
                            }

                            ///////////////////
                            // SELECTED TEXT //
                            ///////////////////
                            // Since the line contains selected text, first
                            // check whether the line is long enough to have
                            // any selected text to print, within the drawing
                            // region defined by the clip area.  If so, then
                            // set the foreground colour and paint the selected
                            // text regions.  (Do not use any colour masking.)
                            if (minsxcol <= datalength) {
                                gfx.setColor(SELECTFG);
                                gfx.drawChars(print, minsxcol,
                                        (Math.min(maxsxcol, datalength)
                                            - minsxcol), startSelectX, curr_y);
                            }

                            //////////////////////////////////
                            // UNSELECTED TEXT TO THE RIGHT //
                            //////////////////////////////////
                            // Since the line contains selected text, check
                            // whether there is any text, within the clip area
                            // drawing region, to print after the selected text.
                            // If so, print any text (to the right) of the
                            // selected text region.
                            if (maxsxcol <= datalength) {
                                // Branch based on whether a colour mask is
                                // available to use for drawing.
                                if (mask != null) {
                                    // If a colour mask for the sequence is
                                    // available, use it.
                                    mask.drawString(gfx, print, maxsxcol,
                                            datalength - maxsxcol,
                                            endSelectX, curr_y);
	                        } else if (canvas.DEFAULT_MASK != null) {
                                    // If no colour mask is available for the
                                    // sequence, BUT a default mask is available
                                    // use the default mask.
	                            canvas.DEFAULT_MASK.drawString(gfx, print,
                                            0, datalength, xstart, curr_y);
                                } else {
                                    // If no colour mask is available for the
                                    // sequence, and a default colour mask is
                                    // also unavailable, use the default
                                    // foreground colour (Colourmask.FOREG).
                                    gfx.setColor(ColourMask.FOREG);
                                    gfx.drawChars(print, maxsxcol,
                                            datalength - maxsxcol,
                                            endSelectX, curr_y);
                                }
                            }
                        } else if (mask != null) {
                            // Since no text is selected, just print the text.
                            // Because a colour mask is available, use it when
                            // printing the text.
                            mask.drawString(gfx, print, 0,
                                    datalength, xstart, curr_y);
                        } else if (canvas.DEFAULT_MASK != null) {
                            // Since no text is selected, just print the text.
                            // Because no colour mask is available for the
                            // sequence, BUT a default mask is available, use
                            // the default colour mask.
                            canvas.DEFAULT_MASK.drawString(gfx, print, 0,
                                    datalength, xstart, curr_y);
                        } else {
                            // Since no text is selected, just print the text.
                            // Because no colour mask is available for the
                            // sequence, and a default colour mask is also
                            // unavailable, use the default foreground colour
                            // for printing the sequence (Colourmask.FOREG).
                            gfx.setColor(ColourMask.FOREG);
                            gfx.drawChars(print, 0, datalength, xstart, curr_y);
                        }
                    }
                }

                // Draw the text cursor/caret.
                if (hasFocus() && gfx.hitClip(cursorX, cursorY, 1, rowHeight)) {
                    gfx.setColor(getForeground());
                    gfx.drawLine(cursorX, cursorY,
                                 cursorX, cursorY + rowHeight);
                }
            } catch (Throwable th) {
                // Print all available debug information (and a stack trace),
                // if any error occurs while painting the canvas.
                System.err.println("---- Printing failed! ----");
                System.err.println("    row:          " + curr_row);
                System.err.println("    startcol:     " + startcol);
                System.err.println("    startrow:     " + startrow);
                System.err.println("    collength:    " + collength);
                System.err.println("    stoprow:      " + stoprow);
                System.err.println("    xstart:       " + xstart);
                System.err.println("    ystart:       " + ystart);
                System.err.println("    startSelectX: " + startSelectX);
                System.err.println("    endSelectX:   " + endSelectX);
                System.err.println("    minsxcol:     " + minsxcol);
                System.err.println("    maxsxcol:     " + maxsxcol);
                System.err.println("    datalength:   " + datalength);
                System.err.println("    print:        " + print);
                System.err.println("    print.length: " + print.length);
                System.err.println("    mask:         " + mask);
                th.printStackTrace(System.err);
            }
        } catch (Throwable th) {
            // Print a stack grace if any errors occur while initializing the
            // paint variables (i.e. calculation errors).
            System.err.println("---- Variable initialization failed! ----");
            th.printStackTrace(System.err);
        }
    }

    /**
     * Updates the font for the text area.  This method also updates the row
     * height and column width, updates the size of the canvas (for scrolling)
     * and reprints the canvas, using the new font, automatically.
     **
     * @param font the new font for the text area.
     */
    @Override
    public final void setFont(Font font) {
        // Begin by setting the font for the parent class.
        super.setFont(font);

        // Update the column height and row width (for accurate text printing).
        colWidth = getFontMetrics(font).charWidth('G');
        rowHeight = getFontMetrics(font).getHeight();

        // Refresh the canvas size (so the scrollbars will be accurate), and
        // repaint the text area.
        refreshSize();
        repaint();
    }

////////////////////////
//********************//
//* KEYBOARD METHODS *//
//********************//
////////////////////////
    /**
     * Processes keys typed within the text area.  This specific method handles
     * all alphanumeric keys, and the ENTER (or RETURN) key.
     **
     * @param event the KeyEvent for the key typed.
     * @see org.biolegato.sequence.canvas.SequenceCanvas#editable
     * @see org.biolegato.sequence.canvas.SequenceTextArea#insertMode
     * @see org.biolegato.sequence.canvas.SequenceTextArea#changePosition(boolean, int, int)
     * @see org.biolegato.sequence.canvas.SequenceTextArea#deleteSelection(boolean)
     * @see org.biolegato.sequence.data.Dataset#delete(int, int, int, boolean)
     * @see org.biolegato.sequence.data.Dataset#insert(int, int, char[], int, int, boolean)
     * @see org.biolegato.sequence.data.Dataset#getSequenceLength(int) 
     */
    public void keyTyped(KeyEvent event) {
        boolean canInsert = true;

        try {
            switch (event.getKeyChar()) {
                ////////////////////////////////////////////
                // SKIP THE BACKSPACE AND DELETE KEYS, AND EXTENDED KEYS BECAUSE
                // THEY ARE HANDLED IN THE OTHER KEYEVENT HANDLER METHODS.
                case KeyEvent.VK_BACK_SPACE:
                case KeyEvent.VK_DELETE:
                case KeyEvent.CHAR_UNDEFINED:
                    break;

                ////////////////////////////////////////////
                // This body of code handles the ENTER/RETURN key.
                case KeyEvent.VK_ENTER:
                    // We only change the row (same effect as downkey, except
                    // clears selection -- "deselects" the text), because
                    // sequences should NOT contain any newline characters
                    // within them.
                    if (row + 1 < datamodel.getSize()) {
                        changePosition(false, col, row + 1);
                    }
                    break;

                ////////////////////////////////////////////
                // This body of code handles all alphanumeric keys.
                default:
                    // First, handle the readonly property (i.e. prevent any
                    // possible data manipulation if the editable status is on!)
                    if (canvas.editable) {
                        // If any text is selected, delete that text.  Or, if no
                        // text is selected, but the insertion status is on,
                        // (insertion status is controlled by the INSERT key)
                        // then delete the next character in the sequence,
                        // before inserting the new character (this will make
                        // the insertion look like an overwrite to the user,
                        // assuming the permissions are correct).
                        if (!isSelectionEmpty()) {
                            deleteSelection(true);
                        } else if (insertMode && datamodel.getSize() > 0
                                && datamodel.getSequenceLength(row) > 0) {
                            canInsert = delete(col, row, 1, 0, true);
                        }

                        // Insert the new character, unless there was a problem
                        // deleting characters in the selection (if text was
                        // selected prior to calling this function), or unless
                        // there was a problem deleting the next character, if
                        // the insertion status was turned on prior to calling
                        // this function (keyTyped).
                        if (isSelectionEmpty() || !insertMode || canInsert) {
                            // Insert the new character.
                            insert(col, row,
                                    new char[]{ event.getKeyChar() }, true);
                            // Move the text caret forward one character.
                            changePosition(false, col + 1, row);
                        }
                    }
                    break;
            }
        } catch (Throwable e) {
            // Print a stack trace if there were any errors.
            e.printStackTrace(System.err);
        }
        // Consume the key event object, such that it is not handled twice.
        event.consume();
    }

    /**
     * Processes key presses within the text area.  The keys handled by this
     * method are: BACKSPACE, DELETE, LEFT, RIGHT, UP, DOWN, SHIFT, CTRL, CUT,
     * INSERT, PASTE, COPY.
     **
     * @param event the KeyEvent for the key pressed.
     * @see org.biolegato.sequence.canvas.SequenceCanvas#cutAct
     * @see org.biolegato.sequence.canvas.SequenceCanvas#copyAct
     * @see org.biolegato.sequence.canvas.SequenceCanvas#pasteAct
     * @see org.biolegato.sequence.canvas.SequenceTextArea#insertMode
     * @see org.biolegato.sequence.canvas.SequenceTextArea#selectionMove
     * @see org.biolegato.sequence.canvas.SequenceTextArea#changePosition(boolean, int, int)
     * @see org.biolegato.sequence.canvas.SequenceTextArea#deleteSelection(boolean)
     */
    public void keyPressed(KeyEvent event) {
        // The column to move the cursor to if the user backspaces from the
        // beginning of the current sequence line/row.  This will be the last
        // character of the sequence above the current.  By default, this value
        // will be zero, so backspacing from the first row will not move the
        // cursor.
        int bspcol = 0;

        try {
            switch (event.getKeyChar()) {
                ////////////////////////////////////////////
                // This body of code handles the backspace key.
                case KeyEvent.VK_BACK_SPACE:
                    // First, handle the readonly property (i.e. prevent any
                    // possible data manipulation if the editable status is on!)
                    if (canvas.editable) {
                        if (!isSelectionEmpty()) {
                            // If any text is selected, just delete the text.
                            deleteSelection(true);
                        } else if (row > 0 || col > 0) {
                            // If no text is selected, move the cursor back one
                            // space, and delete the character in-between the
                            // new and old text-caret positons.  One exception,
                            // if we reach the beginning of any sequence; in
                            // this case, we move the cursor to the line above
                            // (not deleting any characters).
                            //
                            // First, we determine whether we will be moving to
                            // the line above.  If so, we will get the position
                            // of the last character in that row/line, so we can
                            // properly move the cursor to the end of that row.
                            if (col <= 0 && row > 0) {
                                bspcol = datamodel.getSequenceLength(row - 1);
                            }

                            // If we are at the beginning of a sequence row,
                            // just move the cursor to the row above (without
                            // deleting any characters); otherwise, delete the
                            // character before the text caret and move the text
                            // caret back one space (to compensate).
                            if (col <= 0) {
                                changePosition(false, bspcol, row - 1);
                            } else if (delete(col - 1, row, 1, 0, true)) {
                                changePosition(false, col - 1, row);
                            }
                        }
                    }
                    break;

                ////////////////////////////////////////////
                // This body of code handles the delete key.
                case KeyEvent.VK_DELETE:
                    // First, handle the readonly property (i.e. prevent any
                    // possible data manipulation if the editable status is on!)
                    if (canvas.editable) {
                        if (!isSelectionEmpty()) {
                            // If any text is selected, just delete the text.
                            deleteSelection(true);
                        } else if (datamodel.getSize() > 0
                                && datamodel.getSequenceLength(0) > 0) {
                            // If no text is selected, delete the next character
                            // to the right of the text cursor caret.
                            delete(col, row, 1, 0, true);
                        }
                    }
                    break;

                ////////////////////////////////////////////
                // This body of code handles extended keys.
                // The extended keys handled are: LEFT, RIGHT, UP, DOWN, PASTE
                //                                SHIFT, CTRL, CUT, COPY, INSERT
                case KeyEvent.CHAR_UNDEFINED:
                    switch (event.getKeyCode()) {

                        ////////////////////////////////////////////
                        // Handle the shift key by starting text selection.
                        case KeyEvent.VK_SHIFT:
                            selectionMove = true;
                            break;

                        ////////////////////////////////////////////
                        // Handle the left key by moving the cursor left; or,
                        // when the caret cursor reaches the beginning of the
                        // line/row, move the cursor to the last position on the
                        // line/row above the current.
                        case KeyEvent.VK_LEFT:
                            if (col > 0) {
                                changePosition(selectionMove, col - 1, row);
                            } else if (row > 0) {
                                changePosition(selectionMove,
                                    datamodel.getSequenceLength(row), row - 1);
                            }
                            break;

                        ////////////////////////////////////////////
                        // Handle the left key by moving the cursor right; or,
                        // when the caret cursor reaches the end of the line
                        // move the cursor to the first position on the line
                        // above the current.
                        case KeyEvent.VK_RIGHT:
                            if (col + 1 <= datamodel.getSequenceLength(row)) {
                                changePosition(selectionMove, col + 1, row);
                            } else if (row + 1 < datamodel.getSize()) {
                                changePosition(selectionMove, 0, row + 1);
                            }
                            break;

                        ////////////////////////////////////////////
                        // Handle the up key by moving the cursor up a line.
                        case KeyEvent.VK_UP:
                            if (row > 0) {
                                changePosition(selectionMove, col, row - 1);
                            }
                            break;

                        ////////////////////////////////////////////
                        // Handle the down key by moving the cursor down a line.
                        case KeyEvent.VK_DOWN:
                            if (row + 1 < datamodel.getSize()) {
                                changePosition(selectionMove, col, row + 1);
                            }
                            break;

                        ////////////////////////////////////////////
                        // Handle the home key by moving the cursor to the
                        // beginning of the current line.
                        case KeyEvent.VK_HOME:
                            changePosition(selectionMove, 0, row);
                            break;

                        ////////////////////////////////////////////
                        // Handle the end key by moving the cursor to the end of
                        // the current line.
                        case KeyEvent.VK_END:
                            changePosition(selectionMove,
                                    datamodel.getSequenceLength(row), row);
                            break;

                        ////////////////////////////////////////////
                        // Handle the INSERT key, and all key commands specific
                        // to the Solaris keyboard.  (i.e. all key which are
                        // identified by key codes instead of key characters).
                        default:
                            // Here, we separate out the key commands that might
                            // interfere with readonly functionality.  We can
                            // handle the readonly function by either enabling
                            // or disabling the code for these commands, as
                            // necessary, based on the  status of the readonly
                            // property.  In this case, the readonly property is
                            // read indirectly through the sequence canvas's
                            // editable field (which is the opposite value of
                            // the readonly property).
                            //
                            // NOTE: the backspace and delete keys are specified
                            //       as key characters, so they are handled
                            //       separately.
                            if (canvas.editable) {
                                switch (event.getKeyCode()) {
                                    ////////////////////////////////////////////
                                    // Handle the copy key by copying the text.
                                    case KeyEvent.VK_COPY:
                                        canvas.copyAct.actionPerformed(
                                            new ActionEvent(this,
                                                ActionEvent.ACTION_FIRST,
                                                "copy"));
                                        break;

                                    ////////////////////////////////////////////
                                    // Handle the cut key by cutting the text.
                                    case KeyEvent.VK_CUT:
                                        canvas.cutAct.actionPerformed(
                                            new ActionEvent(this,
                                                ActionEvent.ACTION_FIRST,
                                                "cut"));
                                        break;

                                    ////////////////////////////////////////////
                                    // Handle the paste key by pasting the text.
                                    case KeyEvent.VK_PASTE:
                                        canvas.pasteAct.actionPerformed(
                                            new ActionEvent(this,
                                                ActionEvent.ACTION_FIRST,
                                                "paste"));
                                        break;

                                    ////////////////////////////////////////////
                                    // Handle the insert key by changing the
                                    // insertion status flag.
                                    case KeyEvent.VK_INSERT:
                                        insertMode = !insertMode;
                                        canvas.insertionMode(insertMode);
                                        break;

                                    ////////////////////////////////////////////
                                    // Print a warning message because the
                                    // character is not recognized by BioLegato,
                                    // if this default case is reached.
                                    default:
                                        if (BLMain.debug) {
                                            System.err.println(
                                                    "Sequence text area - "
                                                    + "Unhandled key pressed "
                                                    + "--- getKeyChar() = "
                                                    + ((int) event.getKeyChar())
                                                    + "\tgetKeyCode() = "
                                                    + event.getKeyCode());
                                        }
                                        break;
                                }
                            }
                            break;
                    }
                    ////////////////////////////////////////////
                    // If the current column position is past the end of the
                    // current line, then move the cursor the end of the current
                    // line.
                    if (col >= datamodel.getSequenceLength(row)) {
                        changePosition(selectionMove,
                                datamodel.getSequenceLength(row), row);
                    }
                    break;

                ////////////////////////////////////////////
                // If any other key character is read, just delete any
                // currently selected text.
                default:
                    deleteSelection(true);
                    break;
            }
        } catch (Throwable e) {
            // Print a stack trace if there were any errors.
            e.printStackTrace(System.err);
        }
        // Consume the key event object, such that it is not handled twice.
        event.consume();
    }

    /**
     * Processes key releases within the text area.  This method mainly handles
     * turning off the text selection flag if the SHIFT key is released.  The
     * text selection status is handled and read by the changePosition method.
     **
     * @param event the KeyEvent for the key released.
     * @see org.biolegato.sequence.canvas.SequenceTextArea#selectionMove
     * @see org.biolegato.sequence.canvas.SequenceTextArea#changePosition(boolean, int, int)
     */
    public void keyReleased(KeyEvent event) {
        try {
            // Get to the SHIFT key.  A switch statement was only used to make
            // the code look similar to the other key event methods.
            switch (event.getKeyChar()) {
                case KeyEvent.CHAR_UNDEFINED:
                    switch (event.getKeyCode()) {
                        case KeyEvent.VK_SHIFT:
                            selectionMove = false;
                            break;

                        default:
                            break;
                    }
                    break;
                    
                default:
                    break;
            }
        } catch (Throwable e) {
            // Print a stack trace if there were any errors.
            e.printStackTrace(System.err);
        }
        // Consume the key event object, such that it is not handled twice.
        event.consume();
    }

/////////////////////
//*****************//
//* MOUSE METHODS *//
//*****************//
/////////////////////
    /**
     * Handles mouse clicks, by moving the text caret cursor.  If selectMove is
     * set to true, then the current selection will be stretched or shrunk
     * according to the mouse movement.  In contrast, if selectMove is set to
     * false, the current text selection (if any) will be cleared (deselected).
     **
     * @param event the MouseEvent object corresponding to the mouse click.
     */
    public void mouseClicked(MouseEvent event) {
        if (!event.isPopupTrigger()) {
            // calculate the text area row and column numbers from  the X and Y
            // screen co-ordinates, using the row height and column width.
            changePosition(selectionMove, Math.round(event.getX() / colWidth),
                    Math.round(event.getY() / rowHeight));

            // Request the focus from within the current window.
            requestFocus();
            requestFocusInWindow();
        }
    }

    /**
     * Handles mouse button presses.  This method examines the mouse press event
     * for right-mouse button clicks.  If the right-hand mouse button is
     * clicked, then this method will display the text area's pop-up menu.
     **
     * @param event the MouseEvent corresponding to the mouse button press.
     */
    public void mousePressed(MouseEvent event) {
        if (event.isPopupTrigger()) {
            popup.show(this, event.getX(), event.getY());
        }
    }

    /**
     * Handles mouse button releases.  This method will set 'selectionMouse' to
     * false, and examine the button released.  If the button released is the
     * right-hand mouse button was released, then the pop-up menu will be shown.
     **
     * @param event the MouseEvent corresponding to the mouse button release.
     */
    public void mouseReleased(MouseEvent event) {
        if (event.isPopupTrigger()) {
            popup.show(event.getComponent(), event.getX(), event.getY());
        } else {
            selectionMouse = false;
        }
    }

    /**
     * Handles the mouse entering the text area. Currently this function does
     * absolutely nothing; it is only here because it is required by to
     * implement the MouseMotionListener interface.
     **
     * @param event the MouseEvent object corresponding to the mouse entering
     *              the text area.
     */
    public void mouseEntered(MouseEvent event) {
    }

    /**
     * Handles the mouse exiting the text area. Currently this function does
     * absolutely nothing; it is only here because it is required by to
     * implement the MouseMotionListener interface.
     **
     * @param event the MouseEvent object corresponding to the mouse leaving
     *              the text area.
     */
    public void mouseExited(MouseEvent event) {
    }

    /**
     * Handles mouse drags.  Updates the selection boundaries if the user drags
     * the mouse, within the text area.
     **
     * @param event the MouseEvent object corresponding to the mouse drag.
     */
    public void mouseDragged(MouseEvent event) {
        // Skip any right click events.
        if (!event.isPopupTrigger()) {
            // Calculate the row and column numbers from the X and Y coordinates
            // and update the current position.
            changePosition(selectionMouse || selectionMove,
                    Math.round(event.getX() / colWidth),
                    Math.round(event.getY() / rowHeight));

            // Set the mouse selection property to true.
            selectionMouse = true;

            // Request focus for the text area.
            requestFocus();
            requestFocusInWindow();
        }
    }

    /**
     * Handles general mouse movements. Currently this function does absolutely
     * nothing; it is only here because it is required by to implement the
     * MouseMotionListener interface.
     **
     * @param event the MouseEvent corresponding to text area mouse movement.
     */
    public void mouseMoved(MouseEvent event) {
    }

/////////////////////////////////////
//*********************************//
//* EXTERNAL MANIPULATION METHODS *//
//*********************************//
/////////////////////////////////////
    /**
     * Changes the case of the currently selected sequence.  For example, the
     * string: "aaaaaa" would become "AAAAAA", and likewise the string "BBBB"
     * would become "bbbb".  (If the sequence is of inconsistent case, such as
     * "cDcDcD", then the case of the entire sequence is changed to the opposite
     * case of the first character in the sequence (i.e. "CDCDCD").
     */
    public void changeCase() {
        // The number of characters, on each line, to change the case of.
        int    length;
        // An array used to extract and modify the sequence data's case.
        char[] data;

        // Ensure that some text is selected, before beginning.
        if (!isSelectionEmpty()) {
            // Create a new array to use for extracting the sequence data.
            data = new char[maxsx - minsx];

            // Iterate through each line in the selected text.
            for (int y = minsy; y <= maxsy; y++) {
                // Determine the length of text selected on the current line.
                length = datamodel.getSequence(data, y, minsx, maxsx - minsx);

                // If there is text selected on the current line, then alter its
                // case (from upper to lower, or lower to upper).
                if (length > 0) {

                    // Invert the case of the sequence data selected.
                    for (int count = 0; count < length; count++) {
                        data[count] = (Character.isUpperCase(data[count])
                            ? Character.toLowerCase(data[count])
                            : Character.toUpperCase(data[count]));
                    }

                    // Replace the original text with the altered-case version
                    // of the text selected.
                    datamodel.delete(minsx, y, length, false);
                    datamodel.insert(minsx, y, data, 0, length, false);
                }
            }
        }
    }

/////////////////////////////////////
//*********************************//
//* INTERNAL MANIPULATION METHODS *//
//*********************************//
/////////////////////////////////////
    /**
     * <p>Inserts a string into the textarea's underlying Dataset object.</p>
     * <p>NOTE: this is a wrapper method for <b>insert</b>(<i>x</i>, <i>y</i>,
     *       <i>data</i>).</p>
     ** 
     * @param x    the X co-ordinate (column number) to insert the string at.
     * @param y    the Y co-ordinate (row number) to insert the string at.
     * @param text the string to insert.
     * @param groupinsert whether to insert the text in all sequences that are
     *                    part of the same group as 'y'.
     */
    public void insert(final int x, final int y, final char[] text,
                       final boolean groupinsert) {
        // The list of sequences within the same group as the sequence at the
        // line (row) number specified by the variable 'y'.  This list will be
        // null if the sequence at 'y' is not part of any sequence group.
        int[] group = datamodel.getgroup(y);

        // Branch!  If the text is to be inserted into all sequences in the same
        // group as 'y', and 'y' is actually in a group, then we insert the text
        // into all sequences in the group.  Otherwise, we just insert the text
        // into sequence 'y' only.
        if (groupinsert && group != null) {
            // Iterate through the group and perform the mass insertion.
            for (int number : group) {
                insert(x, number, text, false);
            }
        } else {
            datamodel.insert(x, y, text, 0, text.length, true);
        }
    }

    /**
     * Deletes characters from the textarea's underlying Dataset
     * NOTE: deletions are performed in a sequential manner
     **
     * @param x the X-offset/column number to start the deletion from.
     * @param y the Y-offset/line number to start deleting characters from.
     * @param w the width of the deletion; the number of characters along the
     *          X-axis to delete, for each sequence.  If a sequence affected by
     *          the deletion is shorter than the deletion zone (x to x+w), then
     *          that sequence will have its tail deleted instead (from x to the
     *          end of the sequence).  This avoids any errors.
     * @param h the height of the deletion; the number of sequences along the
     *          Y-axis to be affected by the deletion.
     * @param groupdel whether to delete from sequences outside x,y,w,h that are
     *                 grouped to at least one sequence within x,y,w,h.
     * @return true if the deletion was successful for at least one line within
     *         the deletion range (regardless of whether x+w > sequence length);
     *         false, if no sequences were deleted.
     */
    public boolean delete(final int x, final int y, final int w,
                          final int h, final boolean groupdel) {
        // The maximum sequence/line number to iterate to.
        int max = y + h;
        // An array of sequences in the same group as the sequence currently
        // being deleted from.
        Set<Integer> group = new HashSet<Integer>();
        // The result boolean to return.  This is true if at least one sequence
        // character deletion was successful (regardless of whether x+w exceeded
        // the sequence length of the successful deletion).
        boolean result = false;

        // Iterate through every sequence in the deletion zone (x, y, w, h)
        for (int count = y; count <= max; count++) {
            // If group deletions are turned on, add all of the sequences in the
            // same group to the set of grouped sequences to delete.  We do this
            // in case any of the sequences in the zone are part of the same
            // sequence (which would cause the sequence to be deleted from
            // twice!)
            if (groupdel) {
                // Obtain a list of sequences which are in the same group as
                // sequence number 'count'.
                int[] groupedseq = datamodel.getgroup(count);
                
                // If no sequences are in the same group as 'count', then we
                // skip the code below.  This is to avoid null pointer
                // exceptions -- i.e. when no sequences are in the same group
                // as 'count', the function 'getgroup' will return null.
                //
                // In contrast, if sequences are in the same group as 'count',
                // we add them to our 'group' set.  We use a set in this case
                // to avoid duplication -- i.e. if two sequences selected for
                // modification are in the same group.
                if (groupedseq != null) {
                    for (int gln : groupedseq) {
                        group.add(gln);
                    }
                }
            }
            result |= datamodel.delete(x, count, w, true);
        }

        // If we are performing a group deletion, and there are sequences to
        // delete (i.e. at least one grouped sequence), then we need to execute
        // more code (to delete from the sequences in the same group).
        if (groupdel && !group.isEmpty()) {
            // itterate through the group and perform the mass insertion
            for (int gln : group) {
                // Ensure that the sequence number is valid.  Note that the
                // number of sequences should NOT change, because we are only
                // deleting characters from the sequences (and not removing any
                // actual full sequences from the Dataset object!)
                if ((gln < y || gln > max)
                        && gln < datamodel.getSize()) {
                    result |= datamodel.delete(x, gln, w, true);
                } else if (gln >= datamodel.getSize()) {
                    System.err.println("Sequence text area -"
                            + " Invalid row number: " + gln);
                }
            }
        }

        // Return the deletion status boolean.
        return result;
    }

    /**
     * <p>Deletes any text currently selected.  This function is often used
     * before an insertion or as part of a deletion.</p>
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
     * @param protect whether to protect the sequences via testing of sequenc
     *                protections for each character deleted.
     * @return whether the deletion was successful.
     * @see org.biolegato.sequence.canvas.SequenceTextArea#keyTyped(java.awt.event.KeyEvent)
     * @see org.biolegato.sequence.canvas.SequenceTextArea#keyPressed(java.awt.event.KeyEvent)
     * @see org.biolegato.sequence.canvas.SequenceTextArea#readIn(org.biolegato.sequence.data.DataFormat, java.util.Scanner, boolean)
     * @see org.biolegato.sequence.data.Dataset#isProtectionsOn(org.biolegato.sequence.data.Seq.Type, boolean, boolean, boolean, char[], int, int)
     */
    public boolean deleteSelection(boolean protect) {
        // Obtain the deletion zone coordinates from the selection zone (minsx,
        // minsy, maxsx, maxsy).
        final int x = minsx;
        final int y = minsy;
        final int w = maxsx - x;
        final int h = maxsy - y;

        // Initialize the deletion result boolean to false.
        boolean result = false;

        // Ensure that the selection is NOT empty, and the delete can be
        // performed (using the protect flag passed to the function).
        if (!isSelectionEmpty() && delete(x, y, w, h, protect)) {
            // Move the cursor to the beginning (lowest point) in the selection,
            // and update the deletion result boolean to true.
            changePosition(false, x, y);
            result = true;
        }
        return result;
    }

    /**
     * Deletes any text currently selected.  This function is often used before
     * an insertion or as part of a deletion.  This method is called by the
     * SequenceCanvas class, and is required as part of implementing the
     * SequenceCanvasObject interface.
     **
     * @see org.biolegato.sequence.canvas.SequenceCanvasObject#deleteSelection() 
     */
    public void deleteSelection() {
        deleteSelection(false);
    }
    
////////////////////////
//********************//
//* LISTENER METHODS *//
//********************//
////////////////////////
    /**
     * This method is called when a field in a sequence is modified.  This
     * method calculates the area of the sequence text area that needs to be
     * repainted.  This method is called, primarily, by the Dataset sequence
     * object container.
     **
     * @param e the list data event corresponding to the modification.  This
     *          object contains information about the coordinates affected, and
     *          is used to avoid repainting the canvas.  Although, this method
     *          does accept null as a parameter, passing null should be avoided,
     *          because every time the null value is passed, the ENTIRE text
     *          area is repainted (thereby slowing BioLegato).
     */
    public void contentsChanged(ListDataEvent e) {
        // Ensure that the event is not null.  If the event is null, then the
        // entire text area must be repainted as a precaution.
        if (e != null) {
            // Update the size of the textarea from the event object.
            updateLength(e.getIndex0(), e.getIndex1());

            // Set the X co-ordinate to start repainting at.
            final int x = 0;
            // Obtain the width of the area to repaint.  This is the maximum
            // possible X co-ordinate on the screen.
            final int w = (int) getSize().getWidth();
            // Obtain the Y co-ordinate to start repainting at.
            final int y = row2Y(e.getIndex0());
            // Calculate the height of the area to repaint.
            final int h = row2Y(e.getIndex1() - e.getIndex0() + 1);

            // Repaint the modified area of the text area.
            repaint(x, y, w, h);
        } else {
            // In case the event object is null, this else-clause will provide a
            // backup method for repainting the canvas appropriately (albeit
            // less efficiently).  In this case, the entire canvas will be
            // repainted.

            // Update the size of the text area and repaint the canvas.
            updateLength(0, datamodel.getSize());
            repaint();
        }
    }
    
    /**
     * Called when a sequence is added to a Dataset.  This method calculates the
     * area of the sequence text area that needs to be repainted.  This method
     * is called, primarily, by the Dataset sequence object container.  This
     * method also attempts to move parts of the canvas, so as to optimize the
     * repainting speed.
     **
     * @param e the list data event corresponding to the addition.  This object
     *          contains information about the coordinates affected, and is used
     *          to avoid repainting the canvas.  Although, this method does
     *          accept null as a parameter, passing null should be avoided,
     *          because every time the null value is passed, the ENTIRE text
     *          area is repainted (thereby slowing BioLegato).
     */
    public void intervalAdded(ListDataEvent e) {
        // Ensure that the event is not null.  If the event is null, then the
        // entire text area must be repainted as a precaution.
        if (e != null) {
            // Update the size of the text area.
            updateLength(e.getIndex0(), e.getIndex1());

            // Set the X co-ordinate to start repainting at.
            final int x = 0;
            // Obtain the width of the area to repaint.  This is the maximum
            // possible X co-ordinate on the screen.
            final int w = (int) getSize().getWidth();
            // Obtain the Y co-ordinate for the area of the screen to move down.
            final int y = row2Y(e.getIndex0());
            // Obtain the Y co-ordinate to start repainting at.
            final int dy = row2Y(e.getIndex1() + 1);
            // Calculate the height of the area to move down.
            final int h = (int) getSize().getHeight() - y;
            // Cache the graphics object to use for moving parts of the screen.
            final Graphics gfx = getGraphics();

            // Ensure that the graphics object is not null, so we can move parts
            // of the screen (thereby speeding up the repainting algorithm).
            if (gfx != null) {
                // Move the unchanged part of the canvas down one notch, to make
                // room for the new sequence(s) added.
                gfx.copyArea(x, y, w, h, x, dy);

                // Repaint the modified area of the text area.  This is the area
                // on the screen where the added sequences will be displayed.
                repaint(x, y, w, dy - y);
            } else {
                // If no graphics object is available, ensure that the method
                // still functions properly by repainting the entire modified
                // area of the canvas (including the areas of the canvas which
                // just move downwards by some offset).
                repaint(x, y, w, h);
            }
        } else {
            // In case the event object is null, this else-clause will provide a
            // backup method for repainting the canvas appropriately (albeit
            // less efficiently).  In this case, the entire canvas will be
            // repainted.

            // Update the size of the text area and repaint the canvas.
            updateLength(0, datamodel.getSize());
            repaint();
        }
    }

    /**
     * Called when a sequence is removed from a Dataset.  This method calculates
     * the area of the sequence text area that needs to be repainted.  This
     * method is called, primarily, by the Dataset sequence object container.
     * This method also attempts to move parts of the canvas, so as to optimize
     * the repainting speed.
     **
     * @param e the list data event corresponding to the deletion.  This object
     *          contains information about the coordinates affected, and is used
     *          to avoid repainting the canvas.  Although, this method does
     *          accept null as a parameter, passing null should be avoided,
     *          because every time the null value is passed, the ENTIRE text
     *          area is repainted (thereby slowing BioLegato).
     */
    public void intervalRemoved(ListDataEvent e) {
        // Ensure that the event is not null.  If the event is null, then the
        // entire text area must be repainted as a precaution.
        if (e != null) {
            // Update the size of the text area.
            updateLength(e.getIndex0(), e.getIndex1());

            // Set the X co-ordinate to start repainting at.
            final int x = 0;
            // Obtain the width of the area to repaint.  This is the maximum
            // possible X co-ordinate on the screen.
            final int w = (int) getSize().getWidth();
            // The original Y co-ordinate of the area to move.
            final int y = row2Y(e.getIndex1() + 1);
            // The Y new co-ordinate for the area (which will be moved up).
            final int dy = row2Y(e.getIndex0());
            // Calculate the height of the area to move up.
            final int h = (int) getSize().getHeight() - y;
            // Cache the graphics object to use for moving parts of the screen.
            final Graphics gfx = getGraphics();

            // Ensure that the graphics object is not null, so we can move parts
            // of the screen (thereby speeding up the repainting algorithm).
            if (gfx != null) {
                // Move the unchanged part of the canvas up one notch, thereby
                // deleting the sequences from the screen.
                gfx.copyArea(x, y, w, h, x, dy);
            } else {
                // If no graphics object is available, ensure that the method
                // still functions properly by repainting the entire modified
                // area of the canvas (including the areas of the canvas which
                // just move downwards by some offset).
                repaint(x, y, w, h);
            }
        } else {
            // In case the event object is null, this else-clause will provide a
            // backup method for repainting the canvas appropriately (albeit
            // less efficiently).  In this case, the entire canvas will be
            // repainted.

            // Update the size of the text area and repaint the canvas.
            updateLength(0, datamodel.getSize());
            repaint();
        }
    }

    /**
     * Manages text length changes.  This method calculates the area of the
     * sequence text area that needs to be repainted.  This method is called,
     * primarily, by the SequenceCanvas class.  This method also attempts to
     * move parts of the canvas, so as to optimize the repainting speed.
     **
     * @param x the X-coordinate/column where the text length change began.
     * @param y the Y-coordinate/line number where the text length was changed.
     * @param length the number of characters, by which the line was extended.
     *               If length is less than zero, the line will be shrunk
     *               instead of extended.
     */
    public void textLengthChanged(int x, int y, int length) {
        // Translate the column X-coordinate to a screen X-coordinate.
        final int xstart = x * this.colWidth;
        // Determine the screen width of the text length change.
        final int xwidth = Math.abs(length) * this.colWidth;
        // Translate the row Y-coordinate to a screen Y-coordinate.
        final int ystart = row2Y(row);
        // Cache the graphics object to use for moving parts of the screen.
        final Graphics gfx = getGraphics();

        // Update the size of the textarea.
        updateLength(y, y);
        
        // Ensure that the graphics object is not null, so we can move parts of
        // the screen (thereby speeding up the repainting algorithm).
        if (gfx != null) {
            // Determine if the line grew (length > 0) or shrunk (length < 0).
            // If length is equal to zero, we will use the shrunk code, only
            // because it is simpler, and avoids a call to the repaint function.
            if (length > 0) {
                // Move the unchanged part of the canvas to the left, thereby
                // making space for the new characters added.
                gfx.copyArea(xstart, ystart, getWidth(), rowHeight, xwidth, 0);

                // Repaint the area where the new characters are located.
                repaint(xstart, ystart, xwidth, rowHeight);
            } else {
                // Move the unchanged part of the canvas to the right, thereby
                // deleting characters from the screen.
                gfx.copyArea(xstart + xwidth, ystart, getWidth(), rowHeight,
                        0 - xwidth, 0);
            }
        } else {
            // In case the event object is null, this else-clause will provide a
            // backup method for repainting the canvas appropriately (albeit
            // less efficiently).  In this case, the entire row, which was
            // altered, will be repainted.

            // Update the size of the text area and repaint the canvas.
            repaint(xstart, ystart, getWidth(), rowHeight);
        }
    }

    /**
     * This method receives insertion mode change updates.  This ensures that,
     * if the text area is "split" (implemented by two text areas which are
     * synchronized by the SequenceCanvas class), then the insertion mode of
     * both text areas will be synchronized.  This method is called by the
     * SequenceCanvas class's insertionMode(boolean) method, which in-turn is
     * called by SequenceTextArea.keyPressed(KeyEvent).
     **
     * @param mode the new insertion mode status for the text area.
     * @see org.biolegato.sequence.canvas.SequenceCanvas#insertionMode(boolean)
     * @see org.biolegato.sequence.canvas.SequenceTextArea#keyPressed(java.awt.event.KeyEvent) 
     */
    public void insertionMode(boolean mode) {
        this.insertMode = mode;
    }

/////////////////////////
//*********************//
//* SELECTION METHODS *//
//*********************//
/////////////////////////
    /**
     * Updates/moves the cursor to a new position.  This method will update the
     * text selection, if the select boolean is set to true.  This will ensure
     * that the minsx, minsy, maxsx and maxsy variables are all set properly.
     **
     * @param select whether the position should maintain selection status (i.e. true for SHIFT key).
     * @param newx the column co-ordinate of the new position.
     * @param newy the row co-ordinate of the new position.
     */
    protected void changePosition(boolean select, int newx, int newy) {
        final int maxlines = datamodel.getSize() - 1;
        final int oldx = this.col;
        final int oldy = this.row;

        // ensure that the new row and column are valid and
        // update the row and column
        this.row = Math.max(0, Math.min(newy, maxlines));
        this.col = Math.max(0, Math.min(newx,
                                       (datamodel.getSequenceLength(newy))));

        // Determine whether we are updating the text selection (1st branch).
        // If we are updating the text selection, then we do not move sx or sy
        // at all.  The first branch code will also be executed if ths sx and
        // sy values are equal to the old X and Y coordinates; this indicates
        // that a new text selection is initiated.  The second branch (else
        // condition) will be executed if there is no selection whatsoever.
        // In addition to moving the X and Y cooridnates separately from sx and
        // sy, the first branch calcualtes repainting differently from the
        // second branch.  The first branch repaints a selection area, while the
        // second branch only repaints the text caret cursor.
        //
        // Currently, the difference in repaint structure is negligable, because
        // we call the repaint (entire) canvas function at the end of this
        // method.  This was done because the branch method of repainting needs
        // some debugging.  Please feel free to debug the repainting done by the
        // branch method, and disable painting the entire canvas.
        if (select || oldx != sx || oldy != sy) {
            // Calculate the X co-ordinate to start repainting.
            final int x = Math.min(oldx, minsx) * colWidth;
            // Calculate the Y co-ordinate to start repainting.
            final int y = Math.min(oldy, minsy) * rowHeight;
            // Calculate the width of the area to repaint.
            final int w = (Math.max(oldx, maxsx) + 1) * colWidth - x;
            // Calculate the height of the area to repaint.
            final int h = (Math.max(oldy, maxsy) + 1) * rowHeight - y;

            // If the selection is disabled, then ensure that sx and xy track
            // the X and Y coordinates of the text caret cursor.
            if (!select) {
                sx = col;
                sy = row;
            }
            
            // repaint the modified area of the textarea
            repaint(x, y, w, h);
        } else {
            // Ensure that sx and xy track the X and Y coordinates caret cursor.
            sx = col;
            sy = row;

            // Repaint the area of the old caret cursor, and repaint the new
            // caret cursor.
            repaint(oldx * colWidth, oldy * rowHeight, colWidth, rowHeight);
        }

        // Update any cursor listeners to the change in coordinates.
        canvas.cursorChange(col, row);

        // Ensure any parent scroll bars track the caret cursor.
        scrollRectToVisible(new Rectangle(col * colWidth,
                                          row * rowHeight, 1, 1));

        // Notify the parent SequenceCanvas if a new text selection was made, or
        // if an existing text selection was updated.
        if (select && canvas != null) {
            canvas.selectionMade(this);
        }

        // Properly set the minsx and maxsx values.
        // NOTE: this is faster than individual calls to Math.min and Math.max.
        if (sx > col) {
            minsx = col;
            maxsx = sx;
        } else {
            minsx = sx;
            maxsx = col;
        }

        // Properly set the minsy and maxsy values.
        // NOTE: this is faster than individual calls to Math.min and Math.max.
        if (sy > row) {
            minsy = row;
            maxsy = sy;
        } else {
            minsy = sy;
            maxsy = row;
        }

        // TODO: remove, and replace with zoned repainting.
        repaint();
    }

    /**
     * Clears the current text selection (does NOT delete any text).  This is
     * done by calling changePosition with the current column and row values,
     * and the select parameter set to false.
     **
     * @see org.biolegato.sequence.canvas.SequenceTextArea#changePosition(boolean, int, int) 
     */
    public final void clearSelection() {
        changePosition(false, col, row);
    }

    /**
     * Refreshes the size of the text area (for scroll size purposes).  This
     * method will handle any changes to the size of the text area, and will
     * call all appropriate methods (including parent layout manager methods).
     **
     * @see javax.swing.JComponent#setSize(java.awt.Dimension)
     * @see javax.swing.JComponent#setPreferredSize(java.awt.Dimension)
     * @see javax.swing.JComponent#revalidate() 
     */
    protected final void refreshSize() {
        // Calculate the new width for the text area (based on the longest line)
        final int width = (longestline + 1) * colWidth;
        // Calculate the new height for the text area.
        final int height = rowHeight * datamodel.getSize();

        // Set the size and preferred size of the text area.
        setSize(width, height);
        setPreferredSize(getSize());

        // Revalidate the text area component (calls parent layout managers).
        revalidate();
    }

    /**
     * Tests if the text selection shape is empty (i.e. no text is selected).
     * This is determined by comparing the variables 'sx' and 'col'; if they are
     * both the same value, then the selection is considered empty.  Note that
     * sy is not considered, because if sy is the same value as row, all that is
     * indicated is that any text selected is limited to the line specified by
     * either the sy or col variable (since they are both the same).
     **
     * @return true if no text is selected.
     * @see org.biolegato.sequence.canvas.SequenceTextArea#row
     * @see org.biolegato.sequence.canvas.SequenceTextArea#col
     * @see org.biolegato.sequence.canvas.SequenceTextArea#sx
     * @see org.biolegato.sequence.canvas.SequenceTextArea#sy
     */
    public final boolean isSelectionEmpty() {
        return (sx == col);
    }

////////////////////
//****************//
//* MATH METHODS *//
//****************//
////////////////////
    /**
     * Converts a Dataset sequence container row number into a
     * Y coordinate "on the screen" (in the Graphics object).
     **
     * @param r the row number to convert.
     * @return the corresponding "screen" Y value.
     * @see org.biolegato.sequence.canvas.SequenceTextArea#rowHeight
     */
    protected final int row2Y(int r) {
        return (r * rowHeight);
    }

    /**
     * Updates the internal counter which stores the length of the longest line
     * in the text area.  The longest line is used for determining the size and
     * preferred size of the text area.
     **
     * @param start the first offset to examine for the update
     * @param end the last offset to examine for the update
     * @see org.biolegato.sequence.canvas.SequenceTextArea#longestline
     * @see org.biolegato.sequence.canvas.SequenceTextArea#refreshSize()
     */
    public void updateLength(int start, int end) {
        // Stores the new length of the current line being examined.
        int newlength;
        // Ensure that the end line does not exceed the Dataset sequence object
        // container's length.
        end = Math.min(datamodel.getSize() - 1, end);
        // Ensure that the start position is not greater than the end position.
        start = Math.min(start, end);
        // Ensure that the start value is not negative.
        Math.max(0, start);

        // Iterate through each sequence, for which the length has been altered.
        for (int count = start; count <= end; count++) {
            // Extract the new length for the sequence.
            newlength = datamodel.getSequenceLength(count) + 1;

            // If the new length of the sequence is greater than the length of
            // the longest line, update the length of the longest line.
            if (newlength > longestline) {
                longestline = newlength;
            }
        }
        // Refresh the size of the canvas.
        refreshSize();
    }

    /**
     * Reads contents into the text area.
     **
     * @param  format       the file format to write the data in.
     * @param  source       the data source to read data from.
     * @param  overwrite    whether to overwrite the data currently selected
     *                      in the canvas object.
     * @throws IOException  throws an IOException if there is any error
     *                      reading data from the Scanner object.
     */
    public void readIn(DataFormat format, Scanner source, boolean overwrite)
                                                            throws IOException {
        // If the overwrite parameter is set to true, delete any text currently
        // selected.
        if (overwrite) {
            deleteSelection(false);
        }

        // Read in the new data.
        format.convertFrom(datamodel, source, col, row);
    }
    /**
     * Writes the current contents of the canvas object to an Appendable object.
     **
     * @param  fmt          the format to write the data in.
     * @param  dest         the destination Appendable object to write the data.
     * @throws IOException  throws an IOException if there is any error
     *                      appending the data to the Appendable object.
     */
    public void writeOut(DataFormat fmt, Appendable dest) throws IOException {
        // The length of text currently selected (on each line).
        final int length = maxsx - minsx;

        // Iterate and write the selected text from each line in the selection.
        for (int lineNumber = minsy; lineNumber <= maxsy; lineNumber++) {
            fmt.convertTo(dest, datamodel, lineNumber, minsx, length);
        }
    }
}
