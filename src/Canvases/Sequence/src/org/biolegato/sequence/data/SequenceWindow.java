/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.biolegato.sequence.data;

import org.biolegato.sequence.canvas.ColourMask;
import java.awt.event.ItemEvent;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemListener;
import java.io.File;
import java.io.IOException;
import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JTextField;
import org.biolegato.sequence.canvas.SequenceCanvas;
import org.biopcd.parser.PCD;

/**
 * A window for editing/viewing the properties of sequences in the canvas.
 **
 * @author Graham Alvare
 * @author Brian Fristensky
 */
public class SequenceWindow extends JDialog implements ActionListener,
                                                                ItemListener {
    /**
     * The sequence(s) associated with the window.
     */
    private int[] sequences;
    /**
     * The combobox containing all colour masks available for the sequences.
     */
    private JComboBox maskbox;
    /**
     * The text area used to obtain the sequence name from.
     */
    private JTextField namebox = new JTextField();
    /**
     * The checkbox used to modify the protection status of
     * sequence alignment gaps.
     */
    private JCheckBox pAlignCB = new JCheckBox("Alignment gaps");
    /**
     * The checkbox used to modify the protection status of
     * sequence ambiguous characters.
     */
    private JCheckBox pAmbigCB = new JCheckBox("Ambiguous characters");
    /**
     * The checkbox used to modify the protection status of
     * sequence unambiguous characters.
     */
    private JCheckBox pUnambCB = new JCheckBox("Unambiguous characters");
    /**
     * The combobox used for modifying the sequence type (.
     */
    private JComboBox typebox = new JComboBox(new Object[]{Seq.Type.DNA,
                Seq.Type.RNA,
                Seq.Type.PROTEIN,
                Seq.Type.TEXT});
    /**
     * The combobox used for modifying the sequence's direction.
     */
    private JComboBox directionbox = new JComboBox(new Object[]{
                Seq.Direction.FROM5TO3, Seq.Direction.FROM3TO5});
    /**
     * The combobox used for modifying the sequence's topology.
     */
    private JComboBox topologybox = new JComboBox(new Object[]{
                Seq.Topology.LINEAR, Seq.Topology.CIRCULAR});
    /**
     * The combobox used for modifying the sequence's strandedness.
     */
    private JComboBox strandbox = new JComboBox(
            new Object[]{Seq.Strandedness.SINGLE,
                Seq.Strandedness.MIXED,
                Seq.Strandedness.DOUBLE});
    /**
     * The data model to associate with the sequences.
     */
    private Dataset datamodel;
    /**
     * The text to display in comboboxes when more than one option is selected.
     */
    public static final String MULTIPLE_SELECTED = "<multiple selected...>";
    /**
     * Used for serialization purposes.
     */
    public static final long serialVersionUID = 7526472295622777013L;

    /**
     * Creates a window for editing the properties of selected sequences.
     **
     * @param datamodel  the data model where the sequences are stored
     * @param window     the parent window of the sequences.
     * @param sequences  the sequences to edit properties for.
     */
    public SequenceWindow(final SequenceCanvas canvas,
            final Dataset datamodel,
            final JFrame window,
            final int[] sequences) {
        super(window, "Sequence properties");

        Seq curr = null;

        // Create tbe colour mask combo box.
        maskbox = new JComboBox(canvas.colourMasks.toArray());

        // transfer parameters to local class variables
        this.datamodel = datamodel;
        this.sequences = sequences;

        // Ensure that at least one sequence is selected.
        if (sequences.length >= 1) {
            // Obtain the data for the first sequence in the list.
            curr = datamodel.getLine(sequences[0]);

            // If there is only one sequence selected, set the text stored in
            // the name textbox.
            if (sequences.length == 1) {
                namebox.setText(curr.name);
            }

            // Set the selected items in the type, direction, topology and
            // strandedness checkboxes.
            typebox.setSelectedItem(curr.type);
            directionbox.setSelectedItem(curr.direction);
            topologybox.setSelectedItem(curr.topology);
            strandbox.setSelectedItem(curr.strandedness);

            // Set-up the character protection checkboxes.
            pAlignCB.setSelected(curr.protect_align);
            pAmbigCB.setSelected(curr.protect_ambig);
            pUnambCB.setSelected(curr.protect_unambig);

            // Select the colour mask for the data.
            if (curr.mask != null) {
                maskbox.setSelectedItem(canvas.DEFAULT_MASK);
            } else {
                maskbox.setSelectedItem(curr.mask);
            }
        }

        // Iterate through all of the sequence selected (if more than one), and
        // check if each sequence has the same settings (except name).  If the
        // other sequences selected do not have the same settings/parameters,
        // then change the text of those settings to "Multiple Selected...".
        // This will alert the user that more than one option is selected, and
        // changing the value of the combo box will change the value for all of
        // the selected sequences.  In the case of checkboxes, the displayed
        // value will be true, if the value for one of the sequences is true.
        for (int count = 1; (!pAlignCB.isSelected()
                || !pAmbigCB.isSelected()
                || !pUnambCB.isSelected()
                || typebox.getSelectedItem() != MULTIPLE_SELECTED
                || directionbox.getSelectedItem() != MULTIPLE_SELECTED
                || topologybox.getSelectedItem() != MULTIPLE_SELECTED
                || strandbox.getSelectedItem() != MULTIPLE_SELECTED
                ) && count < sequences.length; count++) {
            // Cache the current sequence to exmine (from the list of sequences
            // which are selected in the canvas).
            curr = datamodel.getLine(sequences[count]);

            // If the current sequence entry is not null, then process it.
            if (curr != null) {
                // Analyze the type of the current sequence to determine whether
                // the sequence type comobo box should display the option
                // "Multiple selected", or not.
                if (typebox.getSelectedItem() != MULTIPLE_SELECTED
                        && typebox.getSelectedItem() != curr.type) {
                    typebox.insertItemAt(MULTIPLE_SELECTED, 0);
                    typebox.setSelectedItem(MULTIPLE_SELECTED);
                }

                // Analyze the direction of the current sequence to determine
                // whether the sequence direction comobo box should display the
                // option "Multiple selected", or not.
                if (directionbox.getSelectedItem() != MULTIPLE_SELECTED
                        && directionbox.getSelectedItem() != curr.direction) {
                    directionbox.insertItemAt(MULTIPLE_SELECTED, 0);
                    directionbox.setSelectedItem(MULTIPLE_SELECTED);
                }

                // Analyze the topology of the current sequence to determine
                // whether the sequence topology comobo box should display the
                // option "Multiple selected", or not.
                if (topologybox.getSelectedItem() != MULTIPLE_SELECTED
                        && topologybox.getSelectedItem() != curr.topology) {
                    topologybox.insertItemAt(MULTIPLE_SELECTED, 0);
                    topologybox.setSelectedItem(MULTIPLE_SELECTED);
                }

                // Analyze the strandedness of the current sequence to determine
                // whether the sequence strandedness comobo box should display
                // the option "Multiple selected", or not.
                if (strandbox.getSelectedItem() != MULTIPLE_SELECTED
                        && strandbox.getSelectedItem() != curr.strandedness) {
                    strandbox.insertItemAt(MULTIPLE_SELECTED, 0);
                    strandbox.setSelectedItem(MULTIPLE_SELECTED);
                }

                // Analyze the colour mask of the current sequence to determine
                // whether the sequence colour mask comobo box should display
                // the option "Multiple selected", or not.
                if (!MULTIPLE_SELECTED.equals(maskbox.getSelectedItem())
                        && maskbox.getSelectedItem() != curr.mask
                        || (curr.mask == null && maskbox.getSelectedItem()
                                                    == canvas.DEFAULT_MASK)) {
                    maskbox.insertItemAt(SequenceWindow.MULTIPLE_SELECTED, 0);
                    maskbox.setSelectedItem(SequenceWindow.MULTIPLE_SELECTED);
                }

                // Analyze the character protection status of sequence alignment
                // characters.  If the protection status is set for at least one
                // sequence selected, the protection checkbox should be checked.
                if (!pAlignCB.isSelected() && curr.protect_align) {
                    pAlignCB.setSelected(true);
                }

                // Analyze the character protection status of ambiguous sequence
                // characters.  If the protection status is set for at least one
                // sequence selected, the protection checkbox should be checked.
                if (!pAmbigCB.isSelected() && curr.protect_ambig) {
                    pAmbigCB.setSelected(true);
                }

                // Analyze the character protection status of unambiguous
                // sequence characters.  If the protection status is set for at
                // least one sequence selected, the protection checkbox should
                // be checked.
                if (!pUnambCB.isSelected() && curr.protect_unambig) {
                    pUnambCB.setSelected(true);
                }
            }
        }

        // Configure the windows's main display panel.  This panel will contain
        // all of the other display panels for changing the settings of the
        // sequences selected.  The purpose of enclosing other panels in this
        // main panel is as follows: the mainPanel will align all of the other
        // panels vertically along the Y-axis; in contrast, each individual
        // setting panel (such as nameBox) will align a descriptive label and
        // associated setting-component along the horizontal X-axis.  This
        // method allows for two axes to be used with simplicity.
        Box mainBox = new Box(BoxLayout.PAGE_AXIS);

        // Configure the panel to align the "Name" label and the textbox for
        // changing the selected sequence(s) name(s) along the horizontal axis.
        Box nameBox = new Box(BoxLayout.LINE_AXIS);
        nameBox.add(new JLabel("Name:"));
        nameBox.add(namebox);
        mainBox.add(nameBox);

        // Configure the panel to align the "Type" label and the combo box for
        // changing the selected sequence(s) data type(s) along the X-axis.
        Box typeBox = new Box(BoxLayout.LINE_AXIS);
        typeBox.add(new JLabel("Type:"));
        typeBox.add(typebox);
        typebox.addItemListener(this);
        mainBox.add(typeBox);

        // Configure the panel to align the "Direction" label and the textbox
        // for changing the selected sequence(s) direction(s) along the X-axis.
        Box directionBox = new Box(BoxLayout.LINE_AXIS);
        directionBox.add(new JLabel("Direction:"));
        directionBox.add(directionbox);
        mainBox.add(directionBox);

        // Configure the panel to align the "Topology" label and the textbox for
        // changing the selected sequence(s) topology(s) along the X-axis.
        Box topologyBox = new Box(BoxLayout.LINE_AXIS);
        topologyBox.add(new JLabel("Topology:"));
        topologyBox.add(topologybox);
        mainBox.add(topologyBox);

        // Configure the panel to align the "Strandedness" label and the textbox
        // for changing the selected sequence(s) Strandedness(es) along the
        // horizontal X-axis.
        Box strandednessBox = new Box(BoxLayout.LINE_AXIS);
        strandednessBox.add(new JLabel("Strandedness:"));
        strandednessBox.add(strandbox);
        mainBox.add(strandednessBox);

        // Configure the panel to align all of the sequence character protection
        // checkboxes along the horizontal X-axis.  In addition, the Box object
        // will have a border and title to group the buttons and indicate that
        // they will be used for sequence character protection.
        Box protectionBox = new Box(BoxLayout.PAGE_AXIS);
        protectionBox.add(pAlignCB);
        protectionBox.add(pAmbigCB);
        protectionBox.add(pUnambCB);
        protectionBox.setBorder(BorderFactory.createTitledBorder(
                "Set character protections"));
        mainBox.add(protectionBox);

        
        // Configure the panel to align the "Colour mask" label, the combo box
        // for selecting colour masks already loaded into BioLegato, and the
        // "Import file..." button (for importing colour masks into BioLegato)
        // along the horizontal X-axis.
        Box maskPane = new Box(BoxLayout.LINE_AXIS);
        maskPane.add(new JLabel("Colour mask:"));
        maskPane.add(maskbox);
        
        // Create the "Import file..." button.
        maskPane.add(new JButton(new AbstractAction("Import file...") {
            /**
             * Imports colour masks into BioLegato when the "Import file..."
             * button is clicked by the user.
             **
             * @param evt is ignored by this function.
             */
            public void actionPerformed(ActionEvent evt) {
                // The file object to read the colour mask from.
                File file = null;
                // The colour mask object parsed from the file.
                ColourMask maskadd = null;
                // The JFileChooser for selecting the file to import.
                JFileChooser openDlg = new JFileChooser();

                // Configure the directory of the JFileChooser, and disable the
                // accept-all (inspecific) file filter in the JFileChooser.
                openDlg.setCurrentDirectory(PCD.getCurrentPWD());
                openDlg.setAcceptAllFileFilterUsed(false);

                // Add the default file filters to the JFileChooser.
                openDlg.addChoosableFileFilter(ColourMask.CHAR_MASK_FILTER);
                openDlg.addChoosableFileFilter(ColourMask.GDE_MASK_FILTER);

                // Set the default file filter for the JFileChooser.
                openDlg.setFileFilter(ColourMask.CHAR_MASK_FILTER);

                // If a file is selected in the JFileChooser, open it.
                if (openDlg.showOpenDialog(window) == openDlg.APPROVE_OPTION) {
                    // Get the file selected in the JFileChooser.
                    file = openDlg.getSelectedFile();

                    // If the selected file is readable, parse the file.
                    if (file != null && file.exists()
                            && file.isFile() && file.canRead()) {
                        try {
                            // Handle the parsing of the file based on the file
                            // filter selected by the user.
                            if (openDlg.getFileFilter()
                                    == ColourMask.CHAR_MASK_FILTER) {
                                maskadd = ColourMask.readCharMaskFile(file);
                            } else if (openDlg.getFileFilter()
                                    == ColourMask.GDE_MASK_FILTER) {
                                maskadd = ColourMask.readPosMaskFile(file);
                            }
                        } catch (IOException ioe) {
                            ioe.printStackTrace(System.err);
                        }

                        // If a colour mask was successfully parsed, add it to
                        // BioLegato's list of colour masks.
                        if (maskadd != null) {
                            maskbox.addItem(maskadd);
                            canvas.colourMasks.add(maskadd);
                        }
                    }

                    // Update the current working directory of BioLegato.
                    if (openDlg.getCurrentDirectory() != null) {
                        PCD.setCurrentPWD(openDlg.getCurrentDirectory());
                    }
                }
            }
        }));
        mainBox.add(maskPane);

        // Add a button, for updating the sequences, to the main panel.
        JButton update = new JButton("Update");
        update.setActionCommand("update");
        update.addActionListener(this);
        mainBox.add(update);

        // Do an initial update as to which parameters should be enabled based
        // on the current sequence type (for example, proteins do not have a
        // strandedness setting available in BioLegato, as strandedness does
        // not make sense for proteins at the time of this writing).
        itemStateChanged(new ItemEvent(typebox, typebox.getSelectedIndex(),
                typebox.getSelectedItem(),
                ItemEvent.SELECTED));

        // Display the window to the user for updating the selected sequences.
        add(mainBox);
        pack();
        setLocationRelativeTo(window);
        setVisible(true);
        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
    }

    /**
     * Updates sequence data based on the values selected in the window.
     **
     * @param e this is used to confirm that the update button was pressed.
     */
    public void actionPerformed(ActionEvent e) {
        // Ensure that the proper action command is supplied before the selected
        // sequences are altered.
        if ("update".equals(e.getActionCommand())) {
            // Obtain the new chracter protection statuses.
            boolean protect_align = pAlignCB.isSelected();
            boolean protect_ambig = pAmbigCB.isSelected();
            boolean protect_uambig = pUnambCB.isSelected();

            // Declare variables to store the new properties for the selected
            // sequences.  Note that a value of null for a given property
            // indicates that the selected sequences will not have that property
            // altered when the update button is clicked.  (This is useful for
            // the MULTIPLE_SELECTED combo box item.
            Seq.Type type = null;
            Seq.Direction direction = null;
            Seq.Topology topology = null;
            Seq.Strandedness strandedness = null;
            ColourMask mask = null;

            // Fill in the variables as necessary (skipping MULTIPLE_SELECTED).
            if (!MULTIPLE_SELECTED.equals(typebox.getSelectedItem())) {
                type = (Seq.Type) typebox.getSelectedItem();
            }
            if (!MULTIPLE_SELECTED.equals(directionbox.getSelectedItem())) {
                direction = (Seq.Direction) directionbox.getSelectedItem();
            }
            if (!MULTIPLE_SELECTED.equals(topologybox.getSelectedItem())) {
                topology = (Seq.Topology) topologybox.getSelectedItem();
            }
            if (!MULTIPLE_SELECTED.equals(strandbox.getSelectedItem())) {
                strandedness = (Seq.Strandedness) strandbox.getSelectedItem();
            }
            if (!MULTIPLE_SELECTED.equals(maskbox.getSelectedItem())) {
                mask = (ColourMask) maskbox.getSelectedItem();
            }

            // Itereate through all of the selected sequences int the canvas and
            // alter their properties based on the values selected in the update
            // sequences window.
            for (int y : sequences) {
                // A boolean for determining if the sequence was modified.
                // If a sequence is modified, its GenBANK original header is
                // lost because it will no longer be applicable.  GenBANK
                // original headers are stored from GenBANK files read into
                // BioLegato.  These headers are used for ensuring that CDS,
                // annotation, and other header data (when present) is not lost
                // from unaltered GenBANK files read into BioLegato.
                boolean modified = false;

                // Get the sequence to alter.
                Seq seq = datamodel.getLine(y);

                // Modify the parameters/field of the sequences selected (when
                // the new value is NOT null, and not the same as what is
                // already selected in the sequence).
                if (!"".equals(namebox.getText())
                        && !seq.name.equals(namebox.getText())) {
                    seq.name = namebox.getText();
                    modified = true;
                }
                if (type != null && type != seq.type) {
                    seq.type = type;
                    modified = true;
                }
                if (direction != null && direction != seq.direction) {
                    seq.direction = direction;
                    modified = true;
                }
                if (topology != null && topology != seq.topology) {
                    seq.topology = topology;
                    modified = true;
                }
                if (strandedness != null && strandedness != seq.strandedness) {
                    seq.strandedness = strandedness;
                    modified = true;
                }

                // Modify the colour mask of the sequences selected (when the
                // new colour mask value is NOT null).  This will NOT count as a
                // modification because the sequence data is unaltered.
                if (mask != null) {
                    seq.mask = mask;
                }

                // Alter the protection status for the sequence selected (this
                // will not count as a modification because the sequence data
                // will not be alder).
                seq.protect_align   = protect_align;
                seq.protect_ambig   = protect_ambig;
                seq.protect_unambig = protect_uambig;

                // If the sequence is part of a group alter the protection
                // status of all other sequences in the group to match the
                // sequence just altered.
                if (seq.groupID > 0) {
                    int[] group = datamodel.getgroup(y);

                    if (group != null) {
                        for (int gln : group) {
                            Seq groupseq = datamodel.getLine(gln);
                            groupseq.protect_align = protect_align;
                            groupseq.protect_ambig = protect_ambig;
                            groupseq.protect_unambig = protect_uambig;
                        }
                    }
                }

                // If the properties of the current sequence were altered, then
                // delete the original GenBANK header.
                if (modified) {
                    seq.original = null;
                    datamodel.sequenceChanged(y);
                }
            }
        }
        // Dispose the sequence properties window.
        dispose();
        
    }

    /**
     * Handles sequence-type changes made (this includes greying out parameters
     * which are not applicable to the current sequence type).
     **
     * @param evt is currently ignored by the function.
     */
    public void itemStateChanged(ItemEvent evt) {
        if (Seq.Type.DNA.equals(typebox.getSelectedItem())
                || Seq.Type.RNA.equals(typebox.getSelectedItem())) {
            // Enable the direction, topology and strandedness fields for the
            // user, when the sequence type is DNA or RNA.
            directionbox.setEnabled(true);
            topologybox.setEnabled(true);
            strandbox.setEnabled(true);
        } else if (Seq.Type.PROTEIN.equals(typebox.getSelectedItem())) {
            // Disable the direction, topology and strandedness fields for the
            // user, when the sequence type is protein.
            directionbox.setEnabled(false);
            topologybox.setEnabled(false);
            strandbox.setEnabled(false);
        } else if (Seq.Type.TEXT.equals(typebox.getSelectedItem())) {
            // Disable the direction, topology and strandedness fields for the
            // user, when the sequence type is text.
            directionbox.setEnabled(false);
            topologybox.setEnabled(false);
            strandbox.setEnabled(false);
        }
    }
}
