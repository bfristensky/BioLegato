package org.biopcd.widgets;

import java.awt.Container;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.Map;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import org.biopcd.parser.CommandThread;

/**
 * Creates a JButton, which when clicked runs internal API
 * functions or external commands
 **
 * @author Graham Alvare
 * @author Brian Fristensky
 */
public class CommandButton extends SimpleWidget {
    /**
     * the text to display for the button.
     */
    protected String label = null;
    /**
     * Whether the button closes the main window when clicked.
     */
    protected boolean close = false;
    /**
     * The command to run when the button is clicked.
     */
    protected String command = null;
    /**
     * The hash map containing all of the widgets to be used
     * as parameters for the button.
     */
    protected Map<String, Widget> widgets = null;
    /**
     * The icon for the widget
     */
    public static final Icon WIDGET_ICON = new ImageIcon(
            CommandButton.class.getClassLoader().getResource(
                    "org/biopcd/icons/commandbutton.png"));
    
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
    public CommandButton(String name) {
        this(name, null, "New button", "echo hello", true);
    }

    /**
     * Creates a new instance of CommandButton
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
     * @param widgets the widgets that are used by the current button
     *                when executing commands
     * @param command the command to run when the button is clicked
     * @param close   whether to close the parameter window after
     *                the command is done
     */
    public CommandButton(String name, Map<String, Widget> widgets, String label,
                         String command, boolean close) {
        super(name);

        // Copy all of the constructor variables to the method variables.
        this.label   = label;
        this.widgets = widgets;
        this.command = command;
        this.close   = close;
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
        // Create a new button to display to the user.
        JButton button = new JButton(label);

        // If the command button close flag is set, add an action listener to
        // the command button to close the window when the button is clicked.
        if (close && window != null) {
            button.addActionListener(new ActionListener() {
                /**
                 * Close the window then the CommandButton is clicked.
                 **
                 * @param e  the ActionEvent is ignored by this method.
                 */
                public void actionPerformed(ActionEvent e) {
                    window.close();
                }
            });
        }
        // Add the CommandThread ActionListener to the button.  This will run
        // the command represented by the button.
        button.addActionListener(new CommandThread(command, widgets));

        // Add the CommandButton widget to the destination Container.
        dest.add(button);
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
     * <p>In this case CommandButtons do not have any values, and do not have any
     * sub-widgets.  Thus, this method just returns null.</p>
     **
     * @return null.
     */
    public WidgetInstance getInstance() {
        return null;
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
        final JLabel cmdLbl = new JLabel("Command: ");

        // Create a text box for editing the widget's label.  This is the label
        // to display representing the parameter to be manipulated by the
        // widget.  This is the text the user will see to the left of the
        // widget in any BioLegato menu windows.
        final JTextField lblTxt = new JTextField(label, 20);
        // Create a text box for editing the command for the button to run when
        // it is clicked by the user.
        final JTextArea  cmdTxt = new JTextArea(command);
        // Create a checkbox to control the status of the close window flag.
        final JCheckBox closeCB = new JCheckBox(
                "close the BioLegato menu window after clicking this button " +
                "(e.g. Run buttons in BIRCH)");

        // Create boxes and panels for formatting the edit parameters window.
        final Box lblPnl = new Box(BoxLayout.LINE_AXIS);
        final Box cmdPnl = new Box(BoxLayout.LINE_AXIS);

        // Add the components for editing the widgets to their own panels
        // (so human-readable labels will be displayed beside the components).
        lblPnl.add(lblLbl);
        lblPnl.add(lblTxt);
        cmdPnl.add(cmdLbl);
        cmdPnl.add(cmdTxt);

        // Add all of the panels to the edit parameters window main body panel.
        dest.add(lblPnl);
        dest.add(cmdPnl);
        dest.add(closeCB);

        // Return a new action listener for updating the command button.
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
                command = cmdTxt.getText();
                close = closeCB.isSelected();
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
    @Override
    public void pcdOut(int scope, Appendable out) throws IOException {
        ////////////////////////////
        // BUTTON HEADER AND NAME //
        ////////////////////////////
        // Generate the scope indentation for the next PCD line.
        for (int count = 0; count < scope; count++) {
            out.append("    ");
        }
        // Print the button's header and name in the PCD code.
        out.append("act \"");
        out.append(label.replaceAll("\"", "\"\""));
        out.append("\"\n");


        ////////////////////
        // BUTTON COMMAND //
        ////////////////////
        // Generate the scope indentation for the next PCD line.
        for (int count = 0; count < scope + 1; count++) {
            out.append("    ");
        }
        // Print the command for the button into the PCD code.
        out.append("command \"");
        out.append(command.replaceAll("\"", "\"\""));
        out.append("\"\n");


        ///////////////////////
        // WINDOW CLOSE FLAG //
        ///////////////////////
        // Generate the scope indentation for the next PCD line.
        for (int count = 0; count < scope + 1; count++) {
            out.append("    ");
        }
        // Print the close flag PCD code.
        out.append("close ");
        out.append(close ? "true" : "false");
        out.append("\n");
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
     *
     * <p><i>NOTE: because this widget does not use values (it is a
     *       CommandButton), this method is left empty.</i></p>
     **
     * @param newValue  the new value for the widget.
     */
    public void setValue(String newValue) {
    }
}
