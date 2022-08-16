package org.biopcd.widgets;

import java.awt.Container;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JTextField;
import org.biopcd.parser.CommandThread;

/**
 * A wrapper class used to abstract text fields within command windows.
 **
 * @author Graham Alvare
 * @author Brian Fristensky
 */
public class TextWidget extends SimpleWidget {
    /**
     * the label for the text field
     */
    protected String label = null;
    /**
     * the default value for the text field
     */
    protected String value = null;
    /**
     * The text field for obtaining the data
     */
    protected transient JTextField textfield = null;
    /**
     * Used for serialization purposes
     */
    private static final long serialVersionUID = 7526472295622776159L;
    /**
     * The icon for the widget
     */
    public static final Icon WIDGET_ICON = new ImageIcon(
            TextWidget.class.getClassLoader().getResource(
                    "org/biopcd/icons/textwidget.png"));


    /**
     * Creates a new instance of a text widget
     * (this specific constructor is used by the PCD editor ONLY!).
     **
     * @param name  the PCD variable name (this name can be referenced
     *              in the command using the % symbol; for example,
     *              if the name value was set to "A", the value of this
     *              widget could be accessed by using %A% (lower or
     *              upper-case) within the PCD menu command string.
     */
    public TextWidget(String name) {
        this(name, "New text widget", "");
    }

    /**
     * Creates a new instance of a text widget.
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
     * @param value  the default value for the text widget
     */
    public TextWidget (String name, String label, String value) {
        super(name);

        // Copy all of the constructor variables to the method variables.
        this.label = label;
        this.value = value;
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
        // A box object to ensure that any label associated with the text widget
        // is displayed to the left of the text widget's text field.
        Box result = new Box(BoxLayout.LINE_AXIS);

        // Creates the label for the text field widget.
        if (label != null) {
            result.add(new JLabel(label));
        }

        // Creates the visible text field widget.
        textfield = new JTextField(20);
        if (value != null) {
            textfield.setText(value);
        }
        // Add the textfield to the box object (so that it is beside any
        // associated JLabel).
        result.add(textfield);

        // Add the text widget to the destination container.
        dest.add(result);
    }

    /**
     * <p>Creates a new widget instance of the widget.</p>
     *
     * <p>A widget instance is an object that stores the value of a widget past
     * after the widget has been closed.  This is useful for concurrency.
     * Because more than one BioLegato PCD command can be run simultaneously,
     * BioLegato needs to store the values used to run each command separately.
     * For instance, if the user runs command A, then changing the value of a
     * widget in A's parameter window should not affect the currently running
     * job (i.e. command A).  This is achieved through WidgetInstance objects.
     * </p>
     **
     * @return a widget instance for usage in the current menu.
     */
    public WidgetInstance getInstance () {
        // If the text field is not null, obtain the value for the
        // WidgetInstance from the text field, and update the widget's value.
        if (textfield != null) {
            value = CommandThread.quote(textfield.getText());
            textfield = null;
        }

        // Create the new widget instance for the text widget.
        return new WidgetInstance(value);
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

        // Create a text box for editing the widget's label.  This is the label
        // to display representing the parameter to be manipulated by the
        // widget.  This is the text the user will see to the left of the
        // widget in any BioLegato menu windows.
        final JTextField lblTxt = new JTextField(label, 20);
        // Create a combobox for setting the default value for the text widget.
        final JTextField valTxt = new JTextField(value, 20);

        // Create boxes and panels for formatting the edit parameters window.
        final Box lblPnl = new Box(BoxLayout.LINE_AXIS);
        final Box valPnl = new Box(BoxLayout.LINE_AXIS);

        // Add the components for editing the widgets to their own panels
        // (so human-readable labels will be displayed beside the components).
        lblPnl.add(lblLbl);
        lblPnl.add(lblTxt);
        valPnl.add(valLbl);
        valPnl.add(valTxt);

        // Add all of the panels to the edit parameters window main body panel.
        dest.add(lblPnl);
        dest.add(valPnl);

        // Return a new action listener for updating the text widget.
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
                value = valTxt.getText();
            }
        };
    }

    /**
     * Writes the BioPCD representation of the menu widget to a writer object
     * (see BioLegato's BioPCD editor for more details)
     **
     * @param scope the level of scope to write the menu widget.
     * @param out the writer object to output the BioPCD code.
     */
    @Override
    public void pcdOut (int scope, Appendable out) throws IOException {
        pcdOut(scope, out, "text");
    }

    /**
     * Writes the BioPCD representation of the menu widget to a writer object
     * (see BioLegato's BioPCD editor for more details)
     **
     * @param scope the level of scope to write the menu widget.
     * @param out the writer object to output the BioPCD code.
     */
    protected void pcdOut (int scope, Appendable out, String type)
            throws IOException {
        super.pcdOut(scope, out);

        ///////////////////
        // VARIABLE TYPE //
        ///////////////////
        // Generate the scope indentation for the next PCD line.
        for (int count = 0; count < scope + 1; count++) {
            out.append("    ");
        }
        // Print the variable type PCD code.
        out.append("type ");
        out.append(type);
        out.append("\n");


        ////////////////////
        // VARIABLE LABEL //
        ////////////////////
        // Generate the scope indentation for the next PCD line.
        for (int count = 0; count < scope + 1; count++) {
            out.append("    ");
        }
        // Print the variable's label PCD code.
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
        // Print the default value PCD code.
        out.append("default \"");
        out.append(value.replaceAll("\"", "\"\""));
        out.append("\"\n");
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
        // Set the new value.
        value = newValue;

        // If the textfield is not null, set the value of the textfield to the
        // new value.
        if (textfield != null) {
            textfield.setText(value);
        }
    }
}
