/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.biolegato.tables;

import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.AbstractAction;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JSpinner;
import javax.swing.JTable;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.table.TableColumn;

/**
 * A class for setting properties specific to the table canvas.
 **
 * @author Graham Alvare
 * @author Brian Fristensky
 */
public class TableCanvasProperties extends JDialog implements ChangeListener {
    /**
     * The parent table canvas to adjust properties for.
     */
    JTable canvas;
    /**
     * The row header list for the table canvas.  This is used for adjusting the
     * font properties of the row headers.  (The row headers are the boxes that
     * are used to display the row numbers to the left of the table.)
     */
    JList header;
    /**
     * A spinner model for adjusting the table canvas font size.
     */
    final SpinnerNumberModel fontSizeSP = new SpinnerNumberModel(50, 6, 94, 1);
    /**
     * A check-box for making the fonts in the table canvas bold.
     */
    final JCheckBox boldCB = new JCheckBox("Bold");
    /**
     * A JButton for closing the properties window.
     */
    final JButton closeB = new JButton("Close");
    /**
     * Used for serialization purposes
     */
    private static final long serialVersionUID = 7526472295622777011L;

    /**
     * Creates a new TableCanvasProperties object to change the properties of
     * the table canvas.  Note that this also opens the properties window.
     **
     * @param window    the parent window (used for modality).
     * @param canvas    the table canvas to adjust the properties of.
     * @param rowHeader the row header list for table canvas; this is used for
     *                  adjusting the font properties of the row headers.
     */
    public TableCanvasProperties(final JFrame window, final JTable canvas,
            final JList rowHeader) {
        super(window);

        // Copy the constructor parameters
        this.canvas = canvas;
        this.header = rowHeader;

        // Create an action listener for handling the close button being
        // clicked.  This action listener will close the window and update the
        // table canvas with any selected properties.
        ActionListener actListener = new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                stateChanged(null);
                if (e.getSource() == closeB) {
                    dispose();
                }
            }
        };

        // Create panel boxes for aligning the properties to set in this table
        // canvas properties window.
        final Box outerPane = new Box(BoxLayout.PAGE_AXIS);
        final Box mainPane = new Box(BoxLayout.PAGE_AXIS);

        // Configure the outer canvas properties window display panel.
        outerPane.add(mainPane);
        outerPane.add(closeB);

        // Add the close window action listener to the close button.
        closeB.addActionListener(actListener);

        // Add the components for adjusting the font size to the table canvas
        // properties window.
        add(new JLabel("Font size"));
        JSpinner sizeSP = new JSpinner(fontSizeSP);
        sizeSP.setValue(canvas.getFont().getSize());
        sizeSP.addChangeListener(this);
        mainPane.add(sizeSP);

        // Add the bold font checkbox to the table canvas properties window.
        boldCB.setSelected(canvas.getFont().isBold());
        boldCB.addActionListener(actListener);
        mainPane.add(boldCB);

        // Display the properties window to the user.
        add(outerPane);
        setLocationRelativeTo(window);
        pack();
        setVisible(true);
        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
    }

    /**
     * Update the font size when the font spinner's numerical value changes.
     **
     * @param evt this parameter is ignored by this method.
     */
    public void stateChanged(ChangeEvent evt) {
        // The new font object to set the canvas font with.
        final Font new_font = new Font("Monospaced", (boldCB.isSelected()
                ? Font.BOLD : Font.PLAIN), fontSizeSP.getNumber().intValue());
        // The font width before changing the font size.
        int old_w = canvas.getFontMetrics(canvas.getFont()).getWidths()['a'];
        // The new font width, after changing the font size.
        int new_w = canvas.getFontMetrics(new_font).getWidths()['a'];
        // The height of the new font.
        int new_h = canvas.getFontMetrics(new_font).getHeight();
        // The prefered width of the current colum being processed.
        int col_w;
        // Caches the current column object when adjusting the column widths.
        TableColumn column;

        // Change the font and row height of the canvas.
        canvas.setFont(new_font);
        canvas.setRowHeight(new_h);

        // Adjust the column width for all of the columns in the table.
        for (int index = 0; index < canvas.getColumnCount(); index++) {
            // Cache the current column to process.
            column = canvas.getColumnModel().getColumn(index);

            // Determine the preferred width of the current column
            // using the old font.
            col_w  = column.getPreferredWidth();

            // Adjust the preferred width of the current column, using the new
            // and old column widths as a ratio.
            column.setPreferredWidth(Math.round((col_w * new_w) / old_w));
        }
        // Adjust the font size of the row header object.
        header.setFont(new_font);
        header.setFixedCellHeight(canvas.getRowHeight());
    }
}
