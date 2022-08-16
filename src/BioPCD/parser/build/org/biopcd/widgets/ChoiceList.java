
package org.biopcd.widgets;

import java.awt.Container;
import java.io.IOException;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import org.biopcd.sql.PCDSQL;

/**
 * A wrapper class used to abstract choice lists within command windows.
 **
 * @author Graham Alvare
 * @author Brian Fristensky
 */
public class ChoiceList extends ListWidget {
    /**
     * The list containing the data.
     */
    protected transient JList choice_list = null;
    /**
     * Used for serialization purposes.
     */
    private static final long serialVersionUID = 7526472295622776158L;
    /**
     * The icon for the widget
     */
    public static final Icon WIDGET_ICON = new ImageIcon(
            ChoiceList.class.getClassLoader().getResource(
                    "org/biopcd/icons/choicelist.png"));

    /**
     * Creates a new instance of a choice list widget
     * (this specific constructor is used by the PCD editor ONLY!).
     **
     * @param name  the PCD variable name (this name can be referenced
     *              in the command using the % symbol; for example,
     *              if the name value was set to "A", the value of this
     *              widget could be accessed by using %A% (lower or
     *              upper-case) within the PCD menu command string.
     */
    public ChoiceList(String name) {
        super(name);
    }
    
    /**
     * Creates a new instance of choice list widget.
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
    public ChoiceList(String name, String label, PCDSQL choicesql, int index) {
        super(name, label, choicesql, index);
    }

    /**
     * Creates a new instance of ChoiceList
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
    public ChoiceList(String name, String label, String[] cnames,
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
        // a box to store the label beside the combobox (for alignment purposes)
        Box result = new Box(BoxLayout.LINE_AXIS);
        
        // add the label to the box "result",
        // if the value of the string "label" is not null
        if (label != null) {
            result.add(new JLabel(label));
        }

        // a combo box which returns its value when getValue is called
        choice_list = new JList(cnames);
        
        // handle the default value for the list
        choice_list.setSelectedIndex(index);

        // make the choice_list only support single selection
        choice_list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        
        // add the choice list (and make it scrollable)
        result.add(new JScrollPane(choice_list,
                JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED));
        
        // adds the combobox to the destination
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
        if (choice_list != null) {
            index = choice_list.getSelectedIndex();
            choice_list = null;
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
        super.pcdOut(scope, out, "list");
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

        // Update the selection of any currently displayed choice lists.
        if (choice_list != null) {
            choice_list.setSelectedIndex(index);
        }
    }
}
