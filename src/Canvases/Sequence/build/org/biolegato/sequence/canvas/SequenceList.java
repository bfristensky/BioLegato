/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.biolegato.sequence.canvas;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.util.Arrays;
import java.util.Scanner;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JTextField;
import org.biolegato.sequence.data.DataFormat;
import org.biolegato.sequence.data.Dataset;
import org.biolegato.sequence.data.SequenceWindow;

/**
 * The JList of sequences in the Sequence canvas (to the left of the text-area).
 **
 * @author Graham Alvare
 * @author Brian Fristensky
 */
public final class SequenceList extends JList implements SequenceCanvasObject {

    /**
     * The last time the mouse was clicked (used for double click detection).
     */
    private long lastClickTime = -1000;
    /**
     * The entry (i.e. sequence) selected on the last click.
     */
    private int lastClickEntry = -1;
    /**
     * Self reference (for inner classes).
     */
    private final SequenceList list = this;
    /**
     * Used for the relationship between the data model and the list.
     */
    protected Dataset datamodel;
    /**
     * Canvas object which owns this SequenceList instance.
     */
    protected SequenceCanvas canvas;
    /**
     * The amount of time (ms) between clicks to be considered a double click.
     */
    public static final int DBL_CLICK_TIME = 300;
//////////////////
//**************//
//* MENU ITEMS *//
//**************//
//////////////////
    /**
     * The menu item "Select group"
     */
    public final AbstractAction selectGroupAction
            = new AbstractAction("Select group") {
        /**
         * Serialization number - required for no warnings.
         */
        private static final long serialVersionUID = 7526472295622776157L;

        /**
         * Sets the mnemonic for the event.
         */
        {
            putValue(MNEMONIC_KEY, new Integer(java.awt.event.KeyEvent.VK_S));
        }

        /**
         * Event handler - selects all items within the same group as the
         * sequence selected by the user (if more than one group was selected,
         * then use the first sequence in the sequence list's group.
         **
         * @param evt ignored by this method.
         */
        public void actionPerformed(java.awt.event.ActionEvent evt) {
            int[] group = null;

            for (int index : getSelectedIndices()) {
                // getLine the currently selected sequence
                group = datamodel.getgroup(index);
                
                if (group != null) {
                    // set the selection indicies
                    setSelectedIndices(group);
                }
            }
        }
    };
    /**
     * The menu item "Get info..."
     */
    public final AbstractAction getInfoAction
            = new AbstractAction("Get info...") {
        /**
         * Serialization number - required for no warnings.
         */
        private static final long serialVersionUID = 7526472295622776157L;

        /**
         * Sets the mnemonic for the event.
         */
        {
            putValue(MNEMONIC_KEY, new Integer(java.awt.event.KeyEvent.VK_G));
        }

        /**
         * Event handler - display a sequence information window for the
         * sequences selected.
         **
         * @param evt ignored by this method.
         */
        public void actionPerformed(java.awt.event.ActionEvent evt) {
            if (!isSelectionEmpty()) {
                new SequenceWindow(canvas, datamodel, canvas.getJFrame(),
                        getSelectedIndices()).setVisible(true);
                                // see Bugzilla #1201 - GetInfo - Colourmask: new colours don't display
                // Only part of the SequenceTextArea is repainted. You have to
                // perform an action like resizing or scrolling to get the entire
                // SequenceTextArea to repaint.
                // I'd think repainting at this point would fix, it but apparently
                // not. Could this be a bug in Java?
                //canvas.repaint();
            }
        }
    };
    /**
     * The menu item "Delete"
     */
    private final AbstractAction deleteAction = new AbstractAction("Delete") {
        /**
         * Serialization number - required for no warnings.
         */
        private static final long serialVersionUID = 7526472295622776157L;

        /**
         * Sets the mnemonic for the event.
         */
        {
            putValue(MNEMONIC_KEY, new Integer(java.awt.event.KeyEvent.VK_D));
        }

        /**
         * Event handler - deletes the sequences selected.
         **
         * @param evt ignored by this method.
         */
        public void actionPerformed(java.awt.event.ActionEvent evt) {
            deleteSelection();
        }
    };
    /**
     * The menu item "Select All"
     */
    public final AbstractAction selectAllAction
            = new AbstractAction("Select All") {
        /**
         * Serialization number - required for no warnings.
         */
        private static final long serialVersionUID = 7526472295622777033L;

        /**
         * Sets the mnemonic for the event.
         */
        {
            putValue(MNEMONIC_KEY, new Integer(java.awt.event.KeyEvent.VK_A));
        }

        /**
         * Event handler - select all of the sequences in the canvas.
         **
         * @param evt ignored by this method.
         */
        public void actionPerformed(java.awt.event.ActionEvent evt) {
            list.setSelectionInterval(0, datamodel.getSize() - 1);
            list.requestFocus();
        }
    };
    /**
     * The abstract action "Select sequence by name"
     */
    public final AbstractAction selectByNameAction
            = new AbstractAction("Select sequence by name") {
        /**
         * Serialization number - required for no warnings.
         */
        private static final long serialVersionUID = 7526472295622777033L;

        /**
         * Sets the mnemonic for the event.
         */
        {
            putValue(MNEMONIC_KEY, new Integer(java.awt.event.KeyEvent.VK_S));
        }

        /**
         * Event handler - open a window to select, or deselect sequences in the
         *                 canvas by sequence-name.
         **
         * @param evt ignored by this method.
         */
        public void actionPerformed(java.awt.event.ActionEvent evt) {
            // Request focus for the canvas window.
            list.requestFocus();
            list.requestFocusInWindow();

            // The actual select by name window.
            final JDialog window
                    = new JDialog(canvas.getJFrame(), "Select by name");
            // Used for obtaining the name to search for.
            final JTextField nameSelector = new JTextField();
            // The button used to select the sequences based on their name.
            final Action selectAction = new AbstractAction("Select") {
                /**
                 * Event handler - deselect sequences in the canvas by name,
                 *                 when clicked.
                 **
                 * @param evt ignored by this method.
                 */
                public void actionPerformed(ActionEvent e) {
                    // The text to search for when selecting or deselecting.
                    String test = nameSelector.getText().toLowerCase();

                    // Select the sequences based on name.
                    for (int count = 0; count < datamodel.getSize(); count ++) {
                        if (datamodel.getElementAt(count).toString(
                                ).toLowerCase().contains(test)) {
                            list.addSelectionInterval(count, count);
                        }
                    }
                }
            };
            // The button used to deselect the sequences based on their name.
            final Action deselectAction = new AbstractAction("Deselect") {
                /**
                 * Event handler - select sequences in the canvas by name,
                 *                 when clicked.
                 **
                 * @param evt ignored by this method.
                 */
                public void actionPerformed(ActionEvent e) {
                    // The text to search for when selecting or deselecting.
                    String test = nameSelector.getText().toLowerCase();

                    // Deselect the sequences based on name.
                    for (int count = 0; count < datamodel.getSize(); count ++) {
                        if (datamodel.getElementAt(count).toString(
                                ).toLowerCase().contains(test)) {
                            list.removeSelectionInterval(count, count);
                        }
                    }
                }
            };
            // The button used to close the select or deselect by name window.
            final Action closeAction = new AbstractAction("Close") {
                /**
                 * Event handler - close the select/deselect by name window.
                 **
                 * @param evt ignored by this method.
                 */
                public void actionPerformed(ActionEvent e) {
                    window.dispose();
                }
            };

            // create the main panel
            Box mainPanel = new Box(BoxLayout.PAGE_AXIS);

            // create a panel for entering the name to search for
            Box entryPanel = new Box(BoxLayout.LINE_AXIS);
            entryPanel.add(new JLabel("Name:"));
            entryPanel.add(nameSelector);
            mainPanel.add(entryPanel);

            // create a panel to contain all of the buttons
            Box buttonPanel = new Box(BoxLayout.LINE_AXIS);
            buttonPanel.add(new JButton(selectAction));
            buttonPanel.add(new JButton(deselectAction));
            buttonPanel.add(new JButton(closeAction));
            mainPanel.add(buttonPanel);

            // configure the name textbox button
            nameSelector.addActionListener(selectAction);

            // display the window
            window.add(mainPanel);
            window.pack();
            window.setLocationRelativeTo(canvas.getJFrame());
            window.setVisible(true);
            window.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        }
    };
    /**
     * Action for grouping sequences in the canvas
     */
    public final AbstractAction groupAction = new AbstractAction("Group") {
        /**
         * Serialization number - required for no warnings.
         */
        private static final long serialVersionUID = 7526472295622777032L;

        /**
         * Event handler - group the sequences.
         **
         * @param evt ignored by this method.
         */
        public void actionPerformed(ActionEvent e) {
            datamodel.group(list.getSelectedIndices());
        }
    };
    /**
     * Action for ungrouping/degrouping sequences in the canvas
     */
    public final AbstractAction ungroupAction = new AbstractAction("Ungroup") {
        /**
         * Serialization number - required for no warnings.
         */
        private static final long serialVersionUID = 7526472295622777032L;

        /**
         * Event handler - ungroup the sequences.
         **
         * @param evt ignored by this method.
         */
        public void actionPerformed(ActionEvent e) {
            datamodel.ungroup(list.getSelectedIndices());
        }
    };
    /**
     * The right click menu for the Sequence sequence list.
     */
    protected final JPopupMenu popup = new JPopupMenu();
    /**
     * The mouse adapter to handle mouse clicks for the list's popup menu
     */
    private final MouseAdapter popupMouseAdapter = new MouseAdapter() {
            /**
             * Checks for double clicks.  On a double click, this method opens
             * up a sequence properties window.
             **
             * @param event the mouse event to check for the double click.
             */
            @Override
            public void mouseClicked(MouseEvent event) {
                // If double click select the entire group.
                if (event.getClickCount() > 2
                        || (getSelectedIndex() == lastClickEntry
                        && event.getWhen() - lastClickTime < DBL_CLICK_TIME)) {
                    selectGroupAction.actionPerformed(null);
                }

                // Update the last selected sequence and last time clicked
                // counter/trracker variables.
                lastClickTime = event.getWhen();
                lastClickEntry = getSelectedIndex();
            }

            /**
             * Checks for right clicks.  On a right click, this method opens
             * up a popup menu.  This is the same method as: mousePressed.
             **
             * @param event the mouse event to check for the right click.
             */
            @Override
            public void mousePressed(MouseEvent event) {
                if (event.isPopupTrigger()) {
                    popup.show(event.getComponent(), event.getX() - getX(),
                            event.getY() - getY());
                }
            }

            /**
             * Checks for right clicks.  On a right click, this method opens
             * up a popup menu.  This is the same method as: mouseReleased.
             **
             * @param event the mouse event to check for the right click.
             */
            @Override
            public void mouseReleased(MouseEvent event) {
                if (event.isPopupTrigger()) {
                    popup.show(event.getComponent(), event.getX() - getX(),
                            event.getY() - getY());
                }
            }
        };
    /**
     * Used in serialization.
     */
    private static final long serialVersionUID = 7526472295622777009L;



///////////////////////
//*******************//
//** CLASS METHODS **//
//*******************//
///////////////////////
    /**
     * Constructs a new SequenceList
     **
     * @param canvas    the sequence canvas parent to the sequence list object.
     * @param datamodel the data model object storing the sequence data.
     */
    public SequenceList(SequenceCanvas canvas, Dataset datamodel) {
        super(datamodel);

        // initialize variables
        this.datamodel = datamodel;
        this.canvas = canvas;

        // Add the menu items to the pop-up items.
        // First, handle the read only property (i.e. prevent any
        // possible data manipulation if editable is set to false!)
        if (canvas.editable) {
            // add all of the canvas actions to the popup menu
            popup.add(new JMenuItem(canvas.cutAct));
            popup.add(new JMenuItem(canvas.copyAct));
            popup.add(new JMenuItem(canvas.pasteAct));
            popup.add(new JMenuItem(deleteAction));

        }
        popup.add(new JMenuItem(groupAction));
        popup.add(new JMenuItem(ungroupAction));
        popup.add(new JMenuItem(selectAllAction));
        popup.add(new JMenuItem(selectGroupAction));
        popup.add(new JMenuItem(selectByNameAction));
        popup.add(new JMenuItem(getInfoAction));

        // Add the menu item listeners.
        addMouseListener(popupMouseAdapter);

        // Set the display properties.
        setForeground(Color.BLACK);
        setBackground(new Color(255, 255, 240));
        setSize(getPreferredSize());
        setFont(canvas.currentFont);
        setFixedCellHeight(getFontMetrics(canvas.currentFont).getHeight());
        repaint();
    }

    /**
     * <dl><dt>Changes the case of the currently selected sequence.  If the
     * sequence is of inconsistent case, the case of the first character as the
     * case of the sequence, for case conversion.  For example:</dt>
     *
     *      <dd><p>xYzJWwa23  --- will become ---> WYZJWWA23</p></dd>
     * </dl>
     */
    public void changeCase() {
        // The length of the sequence to process.
        int length = 0;
        // The sequence data to process.
        char[] data = null;

        // Iterate through every sequence selected.
        for (int sequenceNumber : getSelectedIndices()) {
            // Obtain the length of the current sequence to alter.
            length = datamodel.getSequenceLength(sequenceNumber);
            
            // Only update the size of the data array (the array to contain the
            // sequence characters, which will have their case altered), if the
            // length of the data array is less than the length of the current
            // sequence (i.e., the sequence we wish to extract into 'data').
            if (data == null || data.length < length) {
                // Create a new array to contain the sequence data.
                data   = new char[length];
            }
            
            // Extract the characters for the current sequence from the Dataset
            // sequence object container.
            datamodel.getSequence(data, sequenceNumber, 0, length);

            // invert the case of the sequence
            for (int count = 0; count < length; count++) {
                data[count] = (Character.isUpperCase(data[count])
                    ? Character.toLowerCase(data[count])
                    : Character.toUpperCase(data[count]));
            }

            // Delete the old version of the sequence, then insert the modified
            // (case changed) version -- i.e. alter by replacement.
            datamodel.delete(0, sequenceNumber, length, false);
            datamodel.insert(0, sequenceNumber, data, 0, length, false);
        }
    }

    /**
     * Deletes the currently selected sequences.
     */
    public void deleteSelection() {
        datamodel.removeSequences(getSelectedIndices());
    }

    /**
     * Reads data into the sequence list.
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
        // The current index to process within the selected_rows array.
        int index = 0;
        // Stores the list of selected indices within the SequenceList.
        int[] selected_rows = getSelectedIndices();
        // Used to determine when to stop translating (i.e. either the end of
        // the stream, or the end of translatable data).
        boolean result = true;

        // Insert the sequences in the middle of the list (based on selection),
        // if and only if sequences are already selected in the SequenceList.
        if (selected_rows != null && selected_rows.length > 0) {
            // Overwrite the sequences if the overwrite flag is set.
            if (overwrite) {
                datamodel.removeSequences(selected_rows);
            }

            // Sort the selected_rows array.
            Arrays.sort(selected_rows);

            // Iterate through the indexes in selected_rows and perform the
            // insertion where possible.
            while (result && index < selected_rows.length) {
                result = format.convertSequence(datamodel, source, 0,
                                                  selected_rows[index], true);
                index++;
            }
        }

        // Translate any data in the buffer/stream beyond the scope of any
        // insertion points (denoted by selected sequences).
        while (result) {
            result = format.convertSequence(datamodel, source, 0,
                                              datamodel.getSize(), true);
        }
    }
    /**
     * Writes the selected sequences out to an appendable object
     **
     * @param  fmt          the format to write the data in.
     * @param  dest         the destination Appendable object to write the data.
     * @throws IOException  throws an IOException if there is any error
     *                      appending the data to the Appendable object.
     */
    public void writeOut(DataFormat fmt, Appendable dest) throws IOException {
        // Translate and write the file.
        for (int lineNumber : getSelectedIndices()) {
            fmt.convertTo(dest, datamodel, lineNumber);
        }
    }
    
}
