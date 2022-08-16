package org.biopcd.widgets;

import java.awt.Container;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.sql.ResultSet;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Pattern;
import javax.swing.AbstractAction;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import org.biopcd.sql.PCDSQL;

/**
 * A wrapper class used to abstract combo boxes and lists
 * within command windows.
 **
 * NOTE: By default, choicesql will be used to obtain the
 *       names and values for the list.  Choicesql stores
 *       an SQL command to execute.  The list widget object
 *       expects two columns to be returned from the SQL
 *       command; however, if only one is returned, the
 *       value of that single column will be used as both
 *       the name and value for each entry.  If choicesql
 *       is null, then names and values will be used to
 *       fill the list widget.
 **
 * @author Graham Alvare
 * @author Brian Fristensky
 */
public abstract class ListWidget extends SimpleWidget {
    /**
     * Stores the current value selected by the chooser.
     */
    protected int index = 0;
    /**
     * The label to associate with the list widget
     */
    protected String label = null;
    /**
     * The list of choice names
     */
    protected String[] cnames = null;
    /**
     * The list of choice values
     */
    protected String[] values = null;
    /**
     * This array is used for the template toArray method for lists
     * i.e. toArray(BLANK_STRING_ARRAY)
     */
    public static final String[] BLANK_STRING_ARRAY = new String[0];
    /**
     * used for serialization
     */
    private static final long serialVersionUID = 7526472295622776158L;
    /**
     * The database connection to use when the list widget's choices are
     * populated via a database query.  The SQL to use for populating the
     * list widget's choice section are encapsulated in this object.
     */
    private PCDSQL choicesql = null;


    /**
     * Creates a new instance of the abstract class list widget.
     * This abstract class relates to all list-like parameter
     * widgets, providing shared code to ease implementation
     * and reduce redundant coding. (this specific constructor
     * is used by indirectly by the PCD editor ONLY!).
     **
     * @param name  the PCD variable name (this name can be referenced
     *              in the command using the % symbol; for example,
     *              if the name value was set to "A", the value of this
     *              widget could be accessed by using %A% (lower or
     *              upper-case) within the PCD menu command string.
     */
    public ListWidget(String name) {
        this(name, "New list widget", new String[0], new String[0], 0);
    }

    /**
     * Creates a new instance of the abstract class list widget.
     * This abstract class relates to all list-like parameter widgets,
     * providing shared code to ease implementation and reduce redundant.
     * coding (this specific constructor is used to simplify code sharing
     * for the other list widget constructors).
     **
     * @param name   the PCD variable name (this name can be referenced
     *               in the command using the % symbol; for example,
     *               if the name value was set to "A", the value of this
     *               widget could be accessed by using %A% (lower or
     *               upper-case) within the PCD menu command string.
     * @param label  the label to display representing the parameter to be
     *               manipulated by the number widget.  This is the text the
     *               user will see to the left of the widget in any
     *               BioLegato menu windows.
     * @param index  the initial value for the list widget to display
     *               (represents an index within both the 'cnames'
     *               and 'values' arrays)
     */
    private ListWidget(String name, String label, int index) {
        super(name);

        // Set the label and index for the list widget.
        this.label = label;
        this.index = index;
    }

    /**
     * Creates a new instance of the abstract class list widget.
     * This abstract class relates to all list-like parameter widgets,
     * providing shared code to ease implementation and reduce redundant coding.
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
    public ListWidget(String name, String label, PCDSQL choicesql, int index) {
        this(name, label, index);

        // Set the SQL block for the list widget.
        this.choicesql = choicesql;

        try {
            // Print a debug message (this is still an undocumented experimental
            // feature of BioPCD!)
            System.out.println("sql listwidget!");

            // Create variables for extracting the choices from the SQL command.
            String nameCol;
            String valueCol;
            List<String> nameList  = new LinkedList<String>();
            List<String> valueList = new LinkedList<String>();

            // Run the SQL command.
            ResultSet choices = choicesql.query();

            if (choices != null) {
                // Iterate through the choices, and add them to the list widget.
                // Note that the first column of the SQL result is the choice
                // name and the second column is the choice value.
                while (choices.next()) {
                    nameCol  = choices.getString(1);
                    valueCol = choices.getString(2);
                    nameList.add(nameCol);
                    valueList.add(valueCol);
                    System.out.println("Adding: " + nameCol
                            + "    " + valueCol);
                }
            }

            // Convert the Java List objects into String arrays.
            cnames = nameList.toArray(BLANK_STRING_ARRAY);
            values = valueList.toArray(BLANK_STRING_ARRAY);
        } catch (Exception ex) {
            // Print any error information, and set the choice list to be empty.
            ex.printStackTrace(System.err);
            cnames  = BLANK_STRING_ARRAY;
            values = BLANK_STRING_ARRAY;
        }
    }

    /**
     * Creates a new instance of the abstract class list widget.
     * This abstract class relates to all list-like parameter widgets,
     * providing shared code to ease implementation and reduce redundant coding.
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
    public ListWidget(String name, String label, String[] cnames,
                      String[] values, int index) {
        this(name, label, index);

        // Set the choices for the list.
        this.cnames = cnames;
        this.values = values;

        // ensure that the default value makes sense
        // (i.e. within the bounds of cnames and values)
        if (index > cnames.length || index < 0) {
            index = 0;
        }
    }

    /**
     * <p>Populates a container with the component objects
     * necessary for editing the current widget's properties.</p>
     *
     * <p>This method takes the Container 'dest' and populates it with
     * components which can change the properties of the current widget.  In
     * other words, this is the window which pops up when you double click on a
     * widget in the PCD editor.  For instance, if you place a text box in a PCD
     * menu, then double click on it, you can set its internal PCD 'name' and
     * default value, among other things.</p>
     *
     * <p>This method returns an action listener, which is called when the
     * widget should update.  The reason for this is class extension.  To allow
     * sub-classes to use the same method ('editWindow') with only one button,
     * and without re-writing code, an ActionListener object can be passed
     * downwards to the child class.  The child class may add code to call its
     * parent class's ActionListener.</p>
     *
     * <p>Please note that the ActionListener will likely be used by a calling
     * method to create an "Update" button.</p>
     **
     * @param  dest  the destination Container object; this is where the
     *               'editWindow' function will add add all of the Components
     *               necessary for editing the Widget parameters (NOTE: this
     *               class implements the Widget interface).
     * @return the action listener associated with updating the current widget.
     *         When this method is called, the Widget should be updated to use
     *         the parameters specified in the Components displayed on 'dest'.
     */
    public ActionListener editWindow(Container dest) {
        // Create the labels for each of the components for
        // editing the widget parameters.
        final JLabel lblLbl = new JLabel("Label/name: ");
        final JLabel valLbl = new JLabel("Default value: ");
        final JLabel addNameLbl = new JLabel("Name: ");
        final JLabel addValueLbl = new JLabel("Value: ");

        // Create a text box for editing the widget's label.  This is the label
        // to display representing the parameter to be manipulated by the
        // widget.  This is the text the user will see to the left of the
        // widget in any BioLegato menu windows.
        final JTextField lblTxt = new JTextField(label, 20);

        // Create a combobox for setting the default option for the list widget.
        final JComboBox valChoice = new JComboBox(cnames);

        // Create two textboxes for adding choices to the list widget.
        // The first, 'addNameTxt' is the name for the new choice (what the user
        // will see).  The second, 'addValueTxt' is the command line
        // representation of the choice (what the %NAME% will be replaced with).
        final JTextField addNameTxt = new JTextField(20);
        final JTextField addValueTxt = new JTextField(20);

        // Create boxes and panels for formatting the edit parameters window.
        final JPanel addPnl   = new JPanel();
        final Box lblPnl      = new Box(BoxLayout.LINE_AXIS);
        final Box valPnl      = new Box(BoxLayout.LINE_AXIS);
        final Box addNamePnl  = new Box(BoxLayout.LINE_AXIS);
        final Box addValuePnl = new Box(BoxLayout.LINE_AXIS);

        // Create a new button for adding choices to the list.
        JButton addBtn = new JButton(new AbstractAction("Add choice") {
            /**
             * Add the new choice to the list.  The choice name (what the user
             * will see) will be obtained from 'addNameTxt', and the choice
             * value (the substitution value at the command line) will be
             * obtained from 'addValueTxt'.
             **
             * @param e  the ActionEvent for the add choice button.  This is
             *           ignored because it is not needed by this function.
             */
            public void actionPerformed(ActionEvent e) {
                // A temporary array for modifying the choice arrays.
                String[] temp;

                // The name and value for the new choice.
                String name = addNameTxt.getText();
                String value = addValueTxt.getText();

                // Add the new choice to the combo box of choices
                valChoice.addItem(name);

                // Branch based on whether the current choice array is empty.
                if (cnames.length > 0) {
                    // If the choice array is not empty, we must presever all
                    // of the choices already in the list widget.

                    // Transfer the new choice name into the array of choice
                    // names.  This is done by creating a temporary array that
                    // is one index larger than the current choice name array,
                    // copying all of the current choices into the new array,
                    // then adding the new choice name as the last index for
                    // the new choice name array and overwriting the old array.
                    temp = new String[cnames.length + 1];
                    System.arraycopy(cnames, 0, temp, 0, cnames.length);
                    temp[cnames.length] = name;
                    cnames = temp;

                    // Transfer the new choice value into the array of choice
                    // values.  This is done by creating a temporary array that
                    // is one index larger than the current choice value array,
                    // copying all of the current choices into the new array,
                    // then adding the new choice value as the last index for
                    // the new choice value array and overwriting the old array.
                    temp = new String[values.length + 1];
                    System.arraycopy(values, 0, temp, 0, values.length);
                    temp[values.length] = value;
                    values = temp;
                } else {
                    // If the choice array is empty, create a new array for the
                    // new choice value and name.
                    cnames = new String[] {name};
                    values = new String[] {value};
                }
            }
        });
        
        // Create a new button for removing choices from the list.
        JButton deleteBtn = new JButton(new AbstractAction("Delete choice") {
            /**
             * Remove a choice from the list widget.  The choice index will be
             * obtained from the 'valChoice' combo box.
             **
             * @param e  the ActionEvent for the delete choice button.  This is
             *           ignored because it is not needed by this function.
             */
            public void actionPerformed(ActionEvent e) {
                // A temporary array for modifying the choice arrays.
                String[] temp;

                // Obtain the index of the choice to delete.
                int idx = valChoice.getSelectedIndex();

                // Ensure that the index of the choice to delete is valid.
                if (idx >= 0 && cnames.length > 0) {
                    // Remove the choice from the combo box of choices
                    valChoice.removeItemAt(idx);

                    ////////////////////////////////////
                    // DELETE FROM CHOICE NAMES ARRAY //
                    ////////////////////////////////////

                    // Create a new choice names array
                    temp = new String[cnames.length - 1];

                    // If the index to remove is greater than zero, add all of
                    // the choice names from zero upto the index into the new
                    // choice names array.
                    if (idx > 0) {
                        System.arraycopy(cnames, 0, temp, 0,
                                Math.min(idx, cnames.length));
                    }

                    // If the index to remove is less than the length of the
                    // current choice names array, add all of the choice names
                    // from the deletion index, upto the length of the current
                    // choice names array, into the new choice names array.
                    if (idx < cnames.length) {
                        System.arraycopy(cnames, idx + 1, temp, idx,
                                cnames.length - idx - 1);
                    }

                    // Overwrite the old choice names array with the new one.
                    cnames = temp;


                    /////////////////////////////////////
                    // DELETE FROM CHOICE VALUES ARRAY //
                    /////////////////////////////////////

                    // Create a new choice values array
                    temp = new String[values.length - 1];

                    // If the index to remove is greater than zero, add all of
                    // the choice values from zero upto the index into the new
                    // choice values array.
                    if (idx > 0) {
                        System.arraycopy(values, 0, temp, 0,
                                Math.min(idx, values.length));
                    }

                    // If the index to remove is less than the length of the
                    // current choice values array, add all of the choice values
                    // from the deletion index, upto the length of the current
                    // choice values array, into the new choice values array.
                    if (idx < values.length) {
                        System.arraycopy(values, idx + 1, temp, idx,
                                values.length - idx - 1);
                    }

                    // Overwrite the old choice values array with the new one.
                    values = temp;
                }
            }
        });

        // If 'index' is valid, set the combo box to select the choice
        // located at 'index'.
        if (index < cnames.length) {
            valChoice.setSelectedIndex(index);
        }

        // Add the components for editing the widgets to their own panels
        // (so human-readable labels will be displayed beside the components).
        lblPnl.add(lblLbl);
        lblPnl.add(lblTxt);
        valPnl.add(valLbl);
        valPnl.add(valChoice);
        valPnl.add(deleteBtn);
        addNamePnl.add(addNameLbl);
        addNamePnl.add(addNameTxt);
        addValuePnl.add(addValueLbl);
        addValuePnl.add(addValueTxt);

        // Configure the add new choice panel.
        addPnl.setLayout(new BoxLayout(addPnl, BoxLayout.PAGE_AXIS));
        addPnl.add(addNamePnl);
        addPnl.add(addValuePnl);
        addPnl.add(addBtn);

        // Add all of the panels to the edit parameters window main body panel.
        dest.add(lblPnl);
        dest.add(valPnl);
        dest.add(addPnl);

        // Return a new action listener for updating the text and default index
        // for the list widget.
        return new ActionListener() {
            /**
             * Update the widget based on the editor window.
             **
             * @param e  This event will be ignored, because the function
             *           unconditionally updates the widget and does not need
             *           any specific auxiliary information from the ActionEvent
             *           to do so.  (All of the information needed is available
             *           directly from the Components used in the code above).
             *           Note that 'final' objects may be accessed from within
             *           classes extended in-body such as this class.
             */
            public void actionPerformed(ActionEvent e) {
                label = lblTxt.getText();
                index = valChoice.getSelectedIndex();
            }
        };
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
    public void pcdOut (int scope, Appendable out, String type)
                                                        throws IOException {
        // Call the parent class's pcdOut method.
        super.pcdOut(scope, out);
        
        /////////////////
        // WIDGET TYPE //
        /////////////////
        // Generate the scope indentation for the next PCD line.
        for (int count = 0; count < scope + 1; count++) {
            out.append("    ");
        }
        // Print the widget type PCD code.
        out.append("type ");
        out.append(type.replaceAll("\"", "\"\""));
        out.append("\n");


        //////////////////
        // WIDGET LABEL //
        //////////////////
        // Generate the scope indentation for the next PCD line.
        for (int count = 0; count < scope + 1; count++) {
            out.append("    ");
        }
        // Print the PCD code for label (for the widget) to display to the user.
        out.append("label \"");
        out.append(label.replaceAll("\"", "\"\""));
        out.append("\"\n");


        ///////////////////
        // DEFAULT VALUE //
        ///////////////////
        // Generate the scope indentation for the next PCD line.
        for (int count = 0; count < scope + 1; count++) {
            out.append("    ");
        }
        // Print the PCD code for the default index for the list widget.
        out.append("default ");
        out.append(String.valueOf(index));
        out.append("\n");


        /////////////
        // CHOICES //
        /////////////
        // Generate the scope indentation for the next PCD line.
        for (int count = 0; count < scope + 1; count++) {
            out.append("    ");
        }
        // Print the choices heading PCD code.
        out.append("choices");

        if (choicesql == null) {
            // If the list widget is not an SQL list, print each choice for
            // the list widget (on a separate line -- choice names and values
            // are separated by at least one space, and are surrounded by
            // double-quotation marks.
            out.append("\n");

            // Iterate through each choice in the list (using the names array).
            for (int choice = 0; choice < cnames.length; choice++) {
                // Generate the scope indentation for the next PCD line.
                for (int count = 0; count < scope + 1; count++) {
                    out.append("    ");
                }
                // Print the name of the list choice.
                out.append("\"");
                out.append(cnames[choice].replaceAll("\"", "\"\""));
                // Print the value of the list choice.
                out.append("\" \"");
                out.append(values[choice].replaceAll("\"", "\"\""));
                out.append("\"\n");
            }
        } else {
            // If the list widget is an SQL list, print the SQL PCD code.
            out.append(" ");
            choicesql.pcdOut(0, out);
        }
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
    public void setValue(String newValue) {
        // Check whether the value is a numeric index or value.
        if (newValue != null && Pattern.matches("^-?\\d+$", newValue)) {
            // If the value is an index, then set the index directly.
            try {
                index = Integer.parseInt(newValue);
            } catch (Throwable th) {
            }
        } else {
            // If the value is not a numerical index, then search the array of
            // choice names for the name that maches 'newValue'.
            for (int count = 0; count < cnames.length; count++) {
                if (cnames[count].equals(newValue)) {
                    index = count;
                    break;
                }
            }
        }
    }

    /**
     * <p>Creates a new widget instance of the widget</p>
     *
     * <p>A widget instance is an object that stores the value of a widget past
     * after the widget has been closed.  This is useful for concurrency.
     * Because more than one BioLegato PCD command can be run simultaneously,
     * BioLegato needs to store the values used to run each command separately.
     * For instance, if the user runs command A, then changing the value of a
     * widget in A's parameter window should not affect the currently running
     * job (i.e. command A).  This is achieved through WidgetInstance objects.
     * </p>
     *
     * <p>In this case the WidgetInstance contains the value selected in the
     * list box widget.</p>
     *
     * <p><i>NOTE: ALL subclasses should call this function AFTER they modify
     *       the
     *       index and clean up their displayed java components.  See
     *       ComboBoxWidget, Chooser, or ChoiceList for usage examples.</i></p>
     *
     * <p><i>WHY?  because the list widget object will handle SQL based lists
     *       intrinsically.  This will save you the time and effort writing
     *       code to handle BOTH SQL based lists and array based lists.</i></p>
     **
     * @return a widget instance (which contains the value of the list box)
     *         for usage in the current menu.
     */
    public WidgetInstance getInstance() {
        return new WidgetInstance(values[index]);
    }
}
