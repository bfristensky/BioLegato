/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.biolegato.tables;

import java.awt.Component;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JTable;
import javax.swing.ListCellRenderer;
import javax.swing.UIManager;
import javax.swing.table.JTableHeader;

/**
 * A class for rendering the row headers (line numbers) for a table.  This class
 * is necessary because there is no intrinsic way to display the row numbers
 * properly.  This class formats the output of the row numbers (visually -- e.g.
 * colour, etc.), so that they can be displayed properly within the table
 * canvas.
 **
 * @author Graham Alvare
 * @author Brian Fristensky
 */
public class RowHeaderRenderer extends JLabel implements ListCellRenderer {
    /**
     * Creates a new instance of the row header renderer object.
     **
     * @param table the JTable object to build the row header renderer for.
     */
    public RowHeaderRenderer(JTable table) {
        JTableHeader header = table.getTableHeader();
        setOpaque(true);
        setBorder(UIManager.getBorder("TableHeader.cellBorder"));
        setHorizontalAlignment(CENTER);
        setForeground(header.getForeground());
        setBackground(header.getBackground());
        setFont(header.getFont());
    }

    /**
     * <p>Handles rendering the row numbers for the table object.</p>
     *
     * <p>This method ignores all of the parameters, except 'value'.  This
     * method calls its own <code>setText</code> method based on the value of
     * the method parameter 'value'.</p>
     **
     * @param  list         the list to display the row numbers.
     * @param  value        the value to render for the row number.
     * @param  index        the index to render.
     * @param  isSelected   whether to render the cell as selected.
     * @param  cellHasFocus whether the cell to render is focused.
     * @return the rendered component (i.e. this object).
     */
    public Component getListCellRendererComponent(JList list, Object value,
        int index, boolean isSelected, boolean cellHasFocus) {
        // Set the row number to render.
        setText((value == null) ? "" : value.toString());

        // Return a self reference.
        return this;
    }
}
