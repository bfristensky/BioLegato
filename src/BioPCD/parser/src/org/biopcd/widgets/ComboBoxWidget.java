/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.biopcd.widgets;

import java.awt.Container;
import java.io.IOException;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;
import javax.swing.plaf.basic.BasicComboBoxUI;
import javax.swing.plaf.basic.BasicComboPopup;
import javax.swing.plaf.basic.ComboPopup;
import org.biopcd.sql.PCDSQL;

/**
 * A wrapper class used to abstract combo boxes within command windows.
 **
 * @author Graham Alvare
 * @author Brian Fristensky
 */
public class ComboBoxWidget extends ListWidget {
    /**
     * The combo box object to display
     */
    protected transient JComboBox combobox = null;
    /**
     * The UI to enable scrolling of the JComboBox.
     */
    public final BasicComboBoxUI COMBOBOX_UI = new BasicComboBoxUI() {
        /**
         * Override the combo box UI's createPopup method.  This method is
         * called when the user clicks on the combo box to change its current
         * selection.  When this overridden method is called, the combo box
         * will pop-up with a horizontally and vertically scrollable pop-up
         * list of choices.
         **
         * @return ComboPopup  the combobox pop-up object to display when the
         *                     combo box is clicked on.
         */
        @Override
        protected ComboPopup createPopup() {
            // Create an overridden BasicComboPopup object.
            BasicComboPopup uipopup = new BasicComboPopup(comboBox){
                /**
                 * Override the createScroller method of the BasicComboPopup
                 * object.  This method takes the list of the BasicComboPopup,
                 * and encloses it in a JScrollPane which can display both
                 * horizontal and vertical scroll bars.
                 **
                 * @return
                 */
                @Override
                protected JScrollPane createScroller() {
                    // Return the JScrollPane object.
                    return new JScrollPane(list,
                            ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
                            ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
                }
            };
            // Return the modified BasicComboPopup object.
            return uipopup;
        }
    };
    /**
     * used for serialization
     */
    private static final long serialVersionUID = 7526472295622776158L;
    /**
     * The icon for the widget
     */
    public static final Icon WIDGET_ICON = new ImageIcon(
            ComboBoxWidget.class.getClassLoader().getResource(
                    "org/biopcd/icons/combobox.png"));
    
    /**
     * Creates a new instance of a combo box widget
     * (this specific constructor is used by the PCD editor ONLY!).
     **
     * @param name  the PCD variable name (this name can be referenced
     *              in the command using the % symbol; for example,
     *              if the name value was set to "A", the value of this
     *              widget could be accessed by using %A% (lower or
     *              upper-case) within the PCD menu command string.
     */
    public ComboBoxWidget(String name) {
        super(name);
    }

    /**
     * Creates a new instance of combo box widget.
     **
     * @param name       the PCD variable name (this name can be referenced
     *                   in the command using the % symbol; for example,
     *                   if the name value was set to "A", the value of this
     *                   widget could be accessed by using %A% (lower or
     *                   upper-case) within the PCD menu command string.
     * @param label      the label to display representing the parameter to be
     *                   manipulated by the number widget.  This is the text the
     *                   user will see to the left of the widget in any
     *                   BioLegato menu windows.
     * @param choicesql  the SQL query to use for obtaining the list widget's
     *                   choice values and names.
     * @param index      the initial value for the list widget to display
     *                   (represents an index within both the 'cnames'
     *                   and 'values' arrays)
     */
    public ComboBoxWidget(String name, String label,
                          PCDSQL choicesql, int index) {
        super(name, label, choicesql, index);
    }

    /**
     * Creates a new instance of a combo box parameter widget
     * (represents "choice_list" in GDE menu files).
     **
     * @param name    the PCD variable name (this name can be referenced
     *                in the command using the % symbol; for example,
     *                if the name value was set to "A", the value of this
     *                widget could be accessed by using %A% (lower or
     *                upper-case) within the PCD menu command string.
     * @param label   the label to display representing the parameter to be
     *                manipulated by the number widget.  This is the text the
     *                user will see to the left of the widget in any
     *                BioLegato menu windows.
     * @param cnames  the list of names for each option within the list widget
     *                (related to the 'values' method-parameter)
     * @param values  the list of values for each option within the list widget
     *                (related to the 'cnames' method-parameter)
     * @param index   the initial value for the list widget to display
     *                (represents an index within both the 'cnames'
     *                and 'values' arrays)
     */
    public ComboBoxWidget(String name, String label, String[] cnames,
                          String[] values, int index) {
        super(name, label, cnames, values, index);
    }
    
    /**
     * Displays the current widget within the container 'dest'.
     **
     * @param dest   the destination Container to display the widget.  Note that
     *               this will almost definitely be different from the window
     *               parameter, and in most cases, should be a JPanel object.
     * @param window the parent window to communicate with.  The communication
     *               involved is supposed to be limited to just using 'window'
     *               to create modal dialog boxes when necessary (for example,
     *               the AbstractFileChooser's "Browse" file choice dialog box).
     *               Please note that this field may be null!! (e.g. displaying
     *               the current state of the widget in the editor canvas)
     */
    public void display(Container dest, final CloseableWindow window) {
        // A Box container to store the label beside the combobox
        // (for visual display purposes).
        Box result = new Box(BoxLayout.LINE_AXIS);
        
        // Add the label to the box "result",
        // if the value of the string "label" is not null.
        if (label != null) {
            result.add(new JLabel(label));
        }

        // Create the actual combobox object.
        combobox = new JComboBox(cnames);
        
        // Handle the default value and UI scheme for the list,
        // then add it to the box "default".
        if (index < cnames.length) {
            combobox.setSelectedIndex(index);
        }

        // Update the combo box UI
        combobox.setUI(COMBOBOX_UI);
        combobox.updateUI();

        // Add the combo box to the destination Box container.
        result.add(combobox);
        
        // Adds the combobox widget to the destination Container.
        dest.add(result);
    }
    
    /**
     * Creates a new widget instance of the widget
     **
     * A widget instance is an object that stores the value of a widget past
     * after the widget has been closed.  This is useful for concurrency.
     * Because more than one BioLegato PCD command can be run simultaneously,
     * BioLegato needs to store the values used to run each command separately.
     * For instance, if the user runs command A, then changing the value of a
     * widget in A's parameter window should not affect the currently running
     * job (i.e. command A).  This is achieved through WidgetInstance objects.
     **
     * @return a widget instance for usage in the current menu.
     */
    @Override
    public WidgetInstance getInstance() {
        // failsafe: if the combobox is null, then use the value of "index";
        //           otherwise, read the current value for the widget using
        //           the index selected in the combobox
        if (combobox != null) {
            index = combobox.getSelectedIndex();
            combobox = null;
        }
        return super.getInstance();
    }

    /**
     * Writes the BioPCD representation of the menu widget to a writer object
     * (see BioLegato's BioPCD editor for more details)
     **
     * @param scope  the level of scope to write the menu widget.  In the case
     *               of PCD, the scope of each line is indicated by the number
     *               of spaced preceding the line.  Every 4 spaces count as
     *               one level of scope (any number not divisible by 4 is
     *               considered an error), thus if a line is preceded by 4
     *               spaces, its scope level is considered to be 1
     * @param out    the Appendable object to output the BioPCD code.
     */
    @Override
    public void pcdOut (int scope, Appendable out) throws IOException {
        super.pcdOut(scope, out, "combobox");
    }

    /**
     * <p>Changes the current value for the widget.  This is used to ensure that
     * any Components that the widget creates for a PCD menu will update the
     * widget object itself.  This is important because the widget is expected
     * to store the last value it was set to after a window was closed.</p>
     *
     * <p>For example, if you opened a PCD menu and set a NumberWidget to 10,
     * and then closed the window, if you reopen the window the NumberWidget
     * should still be 10 (regardless of any default values).</p>
     **
     * @param newValue  the new value for the widget.
     */
    @Override
    public void setValue(String newValue) {
        super.setValue(newValue);
        // Update the selection of any currently displayed combo boxes.
        if (combobox != null) {
            combobox.setSelectedIndex(index);
        }
    }
}
