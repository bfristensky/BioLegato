/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.biolegato.sequence.canvas;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

/**
 * A class for setting properties for the Sequence canvas.
 **
 * @author Graham Alvare
 * @author Brian Fristensky
 */
public class SequenceCanvasProperties extends JDialog
                                    implements ActionListener, ChangeListener {
    /**
     * The parent sequence canvas to set the properties for.
     */
    SequenceCanvas canvas;
    /**
     * A spinner model for setting the font size of the text in the sequence
     * canvas.
     */
    final SpinnerNumberModel fSizeM = new SpinnerNumberModel(50, 6, 94, 1);
    /**
     * A checkbox for making the fonts in the sequence canvas bold.
     */
    final JCheckBox boldCB = new JCheckBox("Bold");
    /**
     * A button for closing the properties window.
     */
    final JButton closeB = new JButton("Close");
    /**
     * Used for serialization purposes
     */
    private static final long serialVersionUID = 7526472295622777011L;

    /**
     * Creates a new SequenceCanvasProperties object.
     **
     * @param window the parent window
     * @param canvas the canvas associated with its properties
     */
    public SequenceCanvasProperties (final JFrame window,
                                     final SequenceCanvas canvas) {
        super(window);                       // Call the superclass constructor.

        // Link this class to the parent class.
        this.canvas = canvas;

        // Create new box objects to display the data.
        final Box mainPane  = new Box(BoxLayout.PAGE_AXIS);
        final Box outerPane = new Box(BoxLayout.PAGE_AXIS);

        // Add the main panel and close button to the outer panel.
        outerPane.add(mainPane);
        outerPane.add(closeB);

        // Add an action listener to the close button, to perform the close
        // window action when the close button is clicked.
        closeB.addActionListener(this);

        // Add the font size spinner.
        add(new JLabel("Font size"));
        JSpinner sizeSP = new JSpinner(fSizeM);
        sizeSP.setValue(canvas.currentFont.getSize());
        sizeSP.addChangeListener(this);
        mainPane.add(sizeSP);

        // Add the bold button.
        boldCB.setSelected(canvas.currentFont.isBold());
        boldCB.addActionListener(this);
        mainPane.add(boldCB);

        // Display the properties panel and window.
        add(outerPane);
        setLocationRelativeTo(window);
        pack();
        setVisible(true);
        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
    }

    /**
     * Handles changes to the font size spinner
     * (updates the font on spinner value change).
     **
     * @param evt the event object created by the state change
     * @see org.biolegato.sequence.canvas.SequenceCanvas#updateFont(int, boolean)
     */
    public void stateChanged(ChangeEvent evt) {
        if (evt.getSource() == fSizeM) {
            canvas.updateFont(fSizeM.getNumber().intValue(),
                    boldCB.isSelected());
        }
    }

    /**
     * Handles close-button and bold check box clicks
     * (updates the font and closes the window).
     **
     * @param evt the event object to handle
     * @see org.biolegato.sequence.canvas.SequenceCanvas#updateFont(int, boolean)
     */
    public void actionPerformed(ActionEvent evt) {
        canvas.updateFont(fSizeM.getNumber().intValue(), boldCB.isSelected());
        if (evt.getSource() == closeB) {
            dispose();
        }
    }
}
