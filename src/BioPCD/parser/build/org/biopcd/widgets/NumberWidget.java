package org.biopcd.widgets;

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.border.Border;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

/**
 * Wraps component(s) for selecting numeric values.
 **
 * @author Graham Alvare
 * @author Brian Fristensky
 */
public class NumberWidget extends SimpleWidget implements ChangeListener {
    /**
     * The minimum number allowed
     */
    private int min;
    /**
     * The maximum number allowed
     */
    private int max;
    /**
     * The default for the number field
     */
    private int value;
    /**
     * The label for the widget
     */
    protected String label = null;
    /**
     * The slider portion of the number selector widget.
     */
    private JSlider slider = null;
    /**
     * The slider portion of the number selector widget.
     */
    private JSpinner spinner = null;
    /**
     * Used for serialization purposes.
     */
    private static final long serialVersionUID = 7526472295622776161L;
    /**
     * The icon for the widget
     */
    public static final Icon WIDGET_ICON = new ImageIcon(
            NumberWidget.class.getClassLoader().getResource(
                    "org/biopcd/icons/numberwidget.png"));

    /**
     * Creates a new instance of a number chooser widget
     * (this specific constructor is used by the PCD editor ONLY!).
     **
     * @param name  the PCD variable name (this name can be referenced
     *              in the command using the % symbol; for example,
     *              if the name value was set to "A", the value of this
     *              widget could be accessed by using %A% (lower or
     *              upper-case) within the PCD menu command string.
     */
    public NumberWidget(String name) {
        this(name, "New number chooser", 0, 500000, 0);
    }

    /**
     * Creates a new instance of a number chooser widget
     * (or simply, number widget)
     **
     * @param name   the PCD variable name (this name can be referenced
     *               in the command using the % symbol; for example,
     *               if the name value was set to "A", the value of this
     *               widget could be accessed by using %A% (lower or
     *               upper-case) within the PCD menu command string.
     * @param label  the label to display representing the parameter to be
     *               manipulated by the number widget.  This is the text the
     *               user will see to the left of the widget in any BioLegato
     *               menu windows.
     * @param min    the minimum numerical value selectable by the number widget
     * @param max    the maximum numerical value selectable by the number widget
     * @param value  the default number to be selected by the number widget
     *               (this is the value that the widget is initialized with).
     *               Please note that this widget remembers whatever value the
     *               user last selected; therefore, the default value only
     *               applies when the window is first opened, afterwards, the
     *               widget will default to whatever value the user previously
     *               selected.
     */
    public NumberWidget (String name, String label,
                         int min, int max, int value) {
        super(name);
        
        // Copy all of the constructor variables to the method variables.
        this.label = label;
        this.min = min;
        this.max = max;
        this.value = value;
    }

    /**
     * Handles synchronization between the slider and the spinner.
     **
     * @param e used to determine which widget was changed.
     */
    public void stateChanged (ChangeEvent e) {
        // Obtain the new value from either the spinner or the slider.
        if (e.getSource() == spinner) {
            value = ((Number) spinner.getValue()).intValue();
        } else {
            value = slider.getValue();
        }

        // Ensure that the slider and spinner remain in-sync.
        slider.setValue(value);
        spinner.setValue(value);
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
        // A box object to ensure that any label associated with the number
        // widget is displayed to the left of the slider and spinner Components.
        Box container = new Box(BoxLayout.LINE_AXIS);

        // Creates the label for the text field widget.
        if (label != null) {
            container.add(new JLabel(label));
        }

        // If the slider and spinner don't exist, create them.
        if (slider == null && spinner == null) {
            // create the spinner
            spinner = new JSpinner(new SpinnerNumberModel(value, min, max, 1));
            spinner.addChangeListener(this);

            // create the slider
            slider = new JSlider(JSlider.HORIZONTAL, min, max, value);
            slider.addChangeListener(this);
        }

        // Add the spinner container
        container.add(spinner);
        container.add(new JLabel("" + min));

        // Add the slider to the container
        container.add(slider);
        container.add(new JLabel("" + max));

        // Add the 'container' Box to the destination Container.
        dest.add(container);
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
    public WidgetInstance getInstance () {
        // Clear the slider and spinner objects.
        slider  = null;
        spinner = null;

        // Create the new widget instance for the number widget.
        return new WidgetInstance(value);
    }

    /**
     * Displays the current widget in an editor panel.  This is completely
     * customizable; for example, TabbedWidgets display an JTabbedPane with
     * buttons at the bottom for adding tabs.  To see this function in action,
     * using the PCD editor, drag and drop a widget into a new menu.  What you
     * see in the new menu is EXACTLY the Component object returned by this
     * function.
     **
     * @param  mainFrame  a JFrame object for adding modality to any dialog
     *                    boxes, which are created by this function.
     * @return a component object to display in the editor.
     */
    @Override
    public Component displayEdit (final JFrame mainFrame) {
        // The error message to display if the minimum value is greater than the
        // maximum value of the number widget.
        final String RNG_ERR
                = "ERROR - minimum value greater than maximum value";
        // The error message to display if the default value is not between the
        // minimum and maximum values of the number widget.
        final String VAL_ERR
                = "ERROR - new default value not between the maximum";

        // Create the label object representing this widget.
        final JLabel lbl = new JLabel();

        // Create a panel for drawing the widget in its current state.
        JPanel panel = new JPanel();

        // Draw the widget in its current state to 'panel'
        display(panel, null);

        // Create an icon for the JLabel 'lbl'.  This icon should display the
        // widget (which was written tot the JPanel 'panel').
        SimpleWidget.editImage(lbl, panel);
        
        // Add a mouse listener to read double-clicks on the Component generated
        // by this function.  A double-click signals to the editor that the user
        // wishes to modify the parameters of this widget.
        lbl.addMouseListener(new MouseAdapter() {
            /**
             * Listen for double-clicks.  If the user double-clicks on the
             * Component (i.e. the JLabel 'lbl'), then create a new window to
             * edit the parameters of this widget object.  The Components for
             * editing the widget parameters are generated by the 'editWindow'
             * function.  This method/function below additionally adds a text
             * field for editing the variable's PCD name, and an update button
             * (which updates the widget object's parameters).
             **
             * @param e  the MouseEvent containing information about the click
             *           event.  In this case, this function only examines the
             *           event object to see if the mouse click was a double or
             *           single click.
             */
            @Override
            public void mouseClicked(MouseEvent event) {
                if (event.isPopupTrigger() || event.getClickCount() == 2) {
                    // Create the dialog window for editing the widget's
                    // properties and parameters.
                    final JDialog pWindow = new JDialog(mainFrame, true);
                    
                    // Create the labels for each of the components for
                    // editing the widget parameters.
                    final JLabel lblLbl = new JLabel("Label/name: ");
                    final JLabel valLbl = new JLabel("Default value: ");
                    final JLabel minLbl = new JLabel("Minimum value: ");
                    final JLabel maxLbl = new JLabel("Maximum value: ");

                    // Create the edit [BioPCD] variable name text box and
                    // label, and add them to the edit widget parameters window.
                    final JTextField nameField = new JTextField(name, 20);
                    // Create a text box for editing the widget's label.  This
                    // is the label to display representing the parameter to be
                    // manipulated by the widget.  This is the text the user
                    // will see to the left of the widget in any BioLegato menu
                    // windows.
                    final JTextField lblTxt = new JTextField(label, 20);
                    // Create a spinner object for setting the default value for
                    // the number widget.
                    final JSpinner valNum = new JSpinner();
                    // Create a spinner object for setting the minimum value for
                    // the number widget.
                    final JSpinner minNum = new JSpinner();
                    // Create a spinner object for setting the maximum value for
                    // the number widget.
                    final JSpinner maxNum = new JSpinner();

                    // Create boxes and panels for formatting the edit
                    // parameters window.
                    JPanel panel = new JPanel();
                    JPanel namePanel = new JPanel();
                    final Box lblPnl = new Box(BoxLayout.LINE_AXIS);
                    final Box valPnl = new Box(BoxLayout.LINE_AXIS);
                    final Box minPnl = new Box(BoxLayout.LINE_AXIS);
                    final Box maxPnl = new Box(BoxLayout.LINE_AXIS);

                    // Create an update button for updating the number widget.
                    final JButton updateButton = new JButton(
                            new AbstractAction("Update") {
                        /**
                         * Update the widget based on the editor window.
                         **
                         * @param e  This event will be ignored, because the
                         *           function unconditionally updates the widget
                         *           and does not need any specific auxiliary
                         *           information from the ActionEvent to do so.
                         *           (All of the information needed is available
                         *           directly from the Components used in the
                         *           code above). Note that 'final' objects may
                         *           be accessed from within classes extended
                         *           in-body such as this class.
                         */
                        public void actionPerformed(ActionEvent e) {
                            JPanel panel = new JPanel();

                            // Obtain the new default, minimum, and maximum
                            // values for the number widget.
                            int nVal = ((Number) valNum.getValue()).intValue();
                            int nMin = ((Number) minNum.getValue()).intValue();
                            int nMax = ((Number) maxNum.getValue()).intValue();

                            // Check if the new default, minimum, and maximum
                            // values for the number widget are valid.
                            if (nMin > nMax) {
                                JOptionPane.showMessageDialog(pWindow, RNG_ERR);
                            } else if (nVal > nMax || nVal < nMin) {
                                JOptionPane.showMessageDialog(pWindow, VAL_ERR);
                            } else {
                                // Update the widget.
                                name  = nameField.getText();
                                label = lblTxt.getText();
                                value = nVal;
                                min   = nMin;
                                max   = nMax;

                                // Update the slider and spinner if they exist.
                                if (slider != null && spinner != null) {
                                    slider.setValue(value);
                                    spinner.setValue(value);
                                }

                                // Dispose of the properties window.
                                pWindow.dispose();

                                // Update the display image for the number
                                // widget (this is displayed in the PCD editor.
                                display(panel, null);
                                SimpleWidget.editImage(lbl, panel);
                            }
                        }
                    });

                    // Create a change listener object to handle whether the
                    // minimum, maximum and default values for the number widget
                    // are valid.  If any value is invalid, it will have a red
                    // border around its spinner in the editor window.
                    final ChangeListener updateChangeListener
                            = new ChangeListener() {
                        /**
                         * The empty border to display around Components when
                         * the value used by the component is valid.
                         */
                        final Border okBorder
                                = BorderFactory.createEmptyBorder();
                        /**
                         * The red border to display around Components when the
                         * value used by the component is invalid.
                         */
                        final Border errBorder
                                = BorderFactory.createLineBorder(Color.RED);
                        public void stateChanged(ChangeEvent e) {
                            int nVal = ((Number) valNum.getValue()).intValue();
                            int nMin = ((Number) minNum.getValue()).intValue();
                            int nMax = ((Number) maxNum.getValue()).intValue();

                            // Check if the new values are valid.
                            if (nMin > nMax) {
                                // If the minimum value is greater than the
                                // maximum value, all values are considered
                                // invalid.
                                minPnl.setBorder(errBorder);
                                maxPnl.setBorder(errBorder);
                                valPnl.setBorder(errBorder);
                                updateButton.setEnabled(false);
                            } else if (nVal > nMax || nVal < nMin) {
                                // If the default value is not between the
                                // minimum and maximum value, it is considered
                                // invalid.
                                minPnl.setBorder(okBorder);
                                maxPnl.setBorder(okBorder);
                                valPnl.setBorder(errBorder);
                                updateButton.setEnabled(false);
                            } else {
                                // BASE CASE: everything is valid.
                                minPnl.setBorder(okBorder);
                                maxPnl.setBorder(okBorder);
                                valPnl.setBorder(okBorder);
                                updateButton.setEnabled(true);
                            }
                        }
                    };

                    // Set the layout for all of the panels in the window.
                    panel.setLayout(new BoxLayout(panel, BoxLayout.PAGE_AXIS));
                    namePanel.setLayout(new BoxLayout(namePanel,
                            BoxLayout.LINE_AXIS));

                    // Add the components for editing the widgets to their own
                    // panels (so human-readable labels will be displayed beside
                    // the components).
                    namePanel.add(new JLabel("Variable name"));
                    namePanel.add(nameField);
                    lblPnl.add(lblLbl);
                    lblPnl.add(lblTxt);
                    valPnl.add(valLbl);
                    valPnl.add(valNum);
                    minPnl.add(minLbl);
                    minPnl.add(minNum);
                    maxPnl.add(maxLbl);
                    maxPnl.add(maxNum);

                    // Set the default, minimum, and maximum spinners' values.
                    valNum.setValue(value);
                    minNum.setValue(min);
                    maxNum.setValue(max);

                    // Add all of the panels to the edit parameters
                    // window main body panel.
                    panel.add(namePanel);
                    panel.add(lblPnl);
                    panel.add(valPnl);
                    panel.add(minPnl);
                    panel.add(maxPnl);
                    panel.add(updateButton);

                    // Add the change listener to the default, minimum, and
                    // maximum value spinners.  The change listener will detect
                    // invalid selections for the default, minimum, and maximum
                    // values, and provide feedback to the user (via red borders
                    // surrounding the spinners).
                    valNum.addChangeListener(updateChangeListener);
                    minNum.addChangeListener(updateChangeListener);
                    maxNum.addChangeListener(updateChangeListener);

                    // Display the edit parameters window.
                    pWindow.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
                    pWindow.add(panel);
                    pWindow.pack();
                    pWindow.setVisible(true);
                }
                // Consume the event object (so it does not call other methods).
                event.consume();
            }
        });
        return lbl;
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
        return null;
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
        super.pcdOut(scope, out);
        
        ///////////////////
        // VARIABLE TYPE //
        ///////////////////
        // Generate the scope indentation for the next PCD line.
        for (int count = 0; count < scope + 1; count++) {
            out.append("    ");
        }
        // Print the variable type PCD code.
        out.append("type number\n");


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
        // MINIMUM VALUE //
        ///////////////////
        // Generate the scope indentation for the next PCD line.
        for (int count = 0; count < scope + 1; count++) {
            out.append("    ");
        }
        // Print the minimum value PCD code.
        out.append("min ");
        out.append(String.valueOf(min));
        out.append("\n");


        ///////////////////
        // MAXIMUM VALUE //
        ///////////////////
        // Generate the scope indentation for the next PCD line.
        for (int count = 0; count < scope + 1; count++) {
            out.append("    ");
        }
        // Print the maximum value PCD code.
        out.append("max ");
        out.append(String.valueOf(max));
        out.append("\n");


        ///////////////////
        // DEFAULT VALUE //
        ///////////////////
        // Generate the scope indentation for the next PCD line.
        for (int count = 0; count < scope + 1; count++) {
            out.append("    ");
        }
        // Print the default value PCD code.
        out.append("default ");
        out.append(String.valueOf(value));
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
     **
     * @param newValue  the new value for the widget.
     */
    public void setValue(String newValue) {
        try {
            // Try to obtain a numerical value from the value passed to the
            // method.
            value = Integer.parseInt(newValue);

            // If any slider or spinner object exist, update their values.
            if (slider != null && spinner != null) {
                slider.setValue(value);
                spinner.setValue(value);
            }
        } catch (Throwable th) {
        }
    }
}