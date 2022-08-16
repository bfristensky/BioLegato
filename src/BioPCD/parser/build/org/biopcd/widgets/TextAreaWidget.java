package org.biopcd.widgets;

import java.awt.Container;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.JTextArea;
import javax.swing.JScrollPane;

/**
 * A wrapper class used to abstract textareas within command windows.
 **
 * @author Graham Alvare
 * @author Brian Fristensky
 */
public class TextAreaWidget extends SimpleWidget {
    /**
     * the label for the text field
     */
    protected String label = "New text widget";
    /**
     * the default value for the text field
     */
    protected String value = "";
    /**
     * Determines whether to delete the file after execution.
     */
    private boolean save = false;
    /**
     * The text field for obtaining the data
     */
    protected transient JTextArea textarea = null;
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
    public TextAreaWidget(String name) {
        super(name);
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
    public TextAreaWidget (String name, String label,
                           String value, boolean save) {
        super(name);

        this.label = label;
        this.value = value;
        this.save  = save;
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
        Box result = new Box(BoxLayout.LINE_AXIS);

        /* creates the label for the text field widget */
        if (label != null) {
            result.add(new JLabel(label));
        }

        /* creates the visible text field widget */
        textarea = new JTextArea(5, 20);
        if (value != null) {
            textarea.setText(value);
        }
        result.add(new JScrollPane(textarea,
                JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
                JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS));

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
    public WidgetInstance getInstance () {
        // The filename to use for the textarea output.
        String result = "";
        // The path to generate the temporary file.
        File location = new File(System.getProperty("user.dir"));
        // The file object to write the textarea to.
        File currentFile = null;

        // Clean up -- get the text from the textarea object, and
        //             then set the textarea object to null.
        if (textarea != null) {
            value = textarea.getText();
            textarea = null;
        }

        try {
            // Create the file.
            currentFile = File.createTempFile("bio", null, location);

            // If the file is not supposed to be saved after execution,
            // set the File's deleteOnExit flag to true.  Ensures that
            // the file is deleted even if BioLegato exits prematurely
            if (!save) {
                currentFile.deleteOnExit();
            }

            // The file writer object to write the file with.
            FileWriter writer = new FileWriter(currentFile);

            // Write the file.
            writer.write(value);

            // flush and close the file writer buffer.
            writer.flush();
            writer.close();
        } catch (IOException ex) {
            ex.printStackTrace(System.err);
        }
        if (currentFile != null) {
            result = currentFile.getName();
            // TODO: replace with getPath in the new file format
        }

        final File closeFile = currentFile;
        return new WidgetInstance((result != null && !"".equals(result.trim())
                ? result : "nullfile")) {
            /**
             * <p>Notifies the variable that the program has now completed
             * successfully.  This allowed the widget to perform operations
             * based on no longer being visible.</p>
             *
             * <p>In the case of the temporary file widget, this method causes
             * the widget to release any files currently in use and import
             * any content that is designated to be imported into the canvas.
             * This method, furthermore, deletes any temporary files which
             * are not designated to be saved for post-program-termination use.
             * </p>
             */
            @Override
            public void close() {
                // Releases all of the files
                if (closeFile != null && closeFile.exists()) {
                    // Unless the save parameter is set,
                    // delete the file after the program is done.
                    if (!save) {
                        closeFile.delete();
                    }
                }
            }
        };
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
        final JLabel lblLbl = new JLabel("Label/name: ");
        final JLabel valLbl = new JLabel("Default value: ");
        final JTextField lblTxt = new JTextField(label, 20);
        final JTextArea  valTxt = new JTextArea(value, 5, 20);
        final Box lblPnl = new Box(BoxLayout.LINE_AXIS);
        final Box valPnl = new Box(BoxLayout.LINE_AXIS);

        lblPnl.add(lblLbl);
        lblPnl.add(lblTxt);
        valPnl.add(valLbl);
        valPnl.add(new JScrollPane(valTxt,
                JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
                JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS));

        dest.add(lblPnl);
        dest.add(valPnl);

        return new ActionListener() {
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
        for (int count = 0; count < scope + 1; count++) {
            out.append("    ");
        }
        out.append("type ");
        out.append(type);
        out.append("\n");
        for (int count = 0; count < scope + 1; count++) {
            out.append("    ");
        }
        out.append("label \"");
        out.append(label.replaceAll("\"", "\"\""));
        out.append("\"\n");
        for (int count = 0; count < scope + 1; count++) {
            out.append("    ");
        }
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
        value = newValue;
        if (textarea != null) {
            textarea.setText(value);
        }
    }
}
