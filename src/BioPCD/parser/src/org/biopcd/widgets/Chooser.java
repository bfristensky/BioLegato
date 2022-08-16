package org.biopcd.widgets;

import java.awt.Container;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import org.biopcd.sql.PCDSQL;

/**
 * Wrapper class for a group of radiobuttons.
 **
 * NOTE: This can be added to any JContainer since it extends JPanel.
 **
 * @author Graham Alvare
 * @author Brian Fristensky
 */
public class Chooser extends ListWidget implements ActionListener {
    /**
     * Required for serialization purposes.
     */
    private static final long serialVersionUID = 7526472295622776152L;
    /**
     * Stores the radio buttons displayed by the widget object.
     */
    private JRadioButton[] buttons;
    /**
     * The icon for the widget
     */
    public static final Icon WIDGET_ICON = new ImageIcon(
            Chooser.class.getClassLoader().getResource(
                    "org/biopcd/icons/chooser.png"));
    
    /**
     * Creates a new instance of a chooser widget (used by PCD edit ONLY!).
     **
     * @param name  the PCD variable name (this name can be referenced
     *              in the command using the % symbol; for example,
     *              if the name value was set to "A", the value of this
     *              widget could be accessed by using %A% (lower or
     *              upper-case) within the PCD menu command string.
     */
    public Chooser(String name) {
        super(name);
    }

    /**
     * Creates a new instance of chooser widget.
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
    public Chooser(String name, String label, PCDSQL choicesql, int index) {
        super(name, label, choicesql, index);
    }

    /**
     * Creates a new instance of a chooser widget.
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
    public Chooser(String name, String label, String[] cnames,
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
    public void display(Container dest, CloseableWindow window) {
        // a box to store the label beside the combobox (for alignment purposes)
        Box result = new Box(BoxLayout.LINE_AXIS);
        // the radio button group
        // (used to ensure that only one radio button is selected at a time)
        ButtonGroup group = new ButtonGroup();
        // a panel to store all of the radio buttons created
        JPanel panel = new JPanel();
        // the current radio button being processed/created
        JRadioButton radio;

        buttons = new JRadioButton[cnames.length];

        // add the label to the box "result", if the value of the
        // string "label" is not null
        if (label != null) {
            result.add(new JLabel(label));
        }

        // iterate through the array of choices for the radio buttons to create
        // for each value in the array, create a radio button
        // for each radio button, add it to a radiobutton group
        // (this is important to ensure that only one
        // radiobuton can be selected at a time)
        for (int count = 0; count < cnames.length; count++) {
            radio = new JRadioButton(cnames[count]);
            panel.add(radio);
            radio.setActionCommand(String.valueOf(count));
            radio.addActionListener(this);
            if (index == count) {
                radio.setSelected(true);
            }
            group.add(radio);
            buttons[count] = radio;
        }
        // add the radio button panel to the "result" box
        result.add(panel);
        
        // adds the combobox to the destination
        dest.add(result);
    }
   
    /**
     * Handles selection changes of the radio buttons.
     **
     * @param e used to determine which button is selected
     *          (by use of getActionCommand()).
     */
    public void actionPerformed(ActionEvent e) {
        setValue(e.getActionCommand());
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
        buttons = null;
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
        super.pcdOut(scope, out, "chooser");
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
        
        // Update the selection of any currently displayed choice radio buttons.
        // This is done by iterating through all of the buttons to ensure that
        // only one choice option is selected in the button array.
        if (buttons != null) {
            for (int count = 0; count < buttons.length; count++) {
                if (index == count) {
                    buttons[count].setSelected(true);
                } else {
                    buttons[count].setSelected(false);
                }
            }
        }
    }
}
