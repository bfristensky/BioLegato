package org.biopcd.widgets;

import java.awt.Component;
import java.awt.Container;
import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import org.biopcd.parser.PCD;

/**
 * A wrapper class used to abstract file selection within command windows.
 **
 * @author Graham Alvare
 * @author Brian Fristensky
 */
public abstract class AbstractFileChooser extends TextWidget {
    /**
     * The FileSelectionMode for the JFileChooser;
     */
    private int mode;
    /**
     * The text to display for the "choose file"/Browse button
     */
    private String chooserButtonText;

    /**
     * Creates a new instance of the abstract class file/directory
     * chooser widget. This abstract class relates to all file/directory
     * chooser-like parameter widgets, providing shared code to ease
     * implementation and reduce redundant coding.
     * (this specific constructor is used by the PCD editor ONLY!).
     **
     * @param name  the PCD variable name (this name can be referenced
     *              in the command using the % symbol; for example,
     *              if the name value was set to "A", the value of this
     *              widget could be accessed by using %A% (lower or
     *              upper-case) within the PCD menu command string.
     * @param mode  the file chooser mode (directories, files or both).
     * @param chooserButtonText  the text to display in the "Browse" button.
     */
    public AbstractFileChooser(String name, int mode,
            String chooserButtonText) {
        super(name);

        this.mode   = mode;
        this.chooserButtonText = chooserButtonText;
    }

    /**
     * Creates a new instance of the abstract class file/directory
     * chooser widget. This abstract class relates to all file/directory
     * chooser-like parameter widgets, providing shared code to ease
     * implementation and reduce redundant coding.
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
     * @param value  the default value for the file chooser.
     * @param mode   the file chooser mode (directories, files or both).
     * @param chooserButtonText  the text to display in the "Browse" button.
     */
    public AbstractFileChooser(String name, String label, String value,
                               int mode, String chooserButtonText) {
        super(name, label, value);
        
        this.mode   = mode;
        this.chooserButtonText = chooserButtonText;
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
    @Override
    public void display(Container dest, final CloseableWindow window) {
        Box result = new Box(BoxLayout.LINE_AXIS);
        JButton pickFile = new JButton(new AbstractAction(chooserButtonText) {
            /**
             * Handle the choose file button
             **
             * @param e ignored because there is only one widget
             *          calling this method.
             */
            public void actionPerformed(ActionEvent e) {
                JFileChooser openDialog = new JFileChooser();
                Component parent = (window != null ? window.getJFrame() : null);

                openDialog.setCurrentDirectory(PCD.getCurrentPWD());
                openDialog.setAcceptAllFileFilterUsed(true);

                openDialog.setFileSelectionMode(mode);

                // if a file is selected, display it
                if (openDialog.showOpenDialog(parent)
                            == JFileChooser.APPROVE_OPTION) {
                    textfield.setText("\""
                            + openDialog.getSelectedFile().getPath() + "\"");
                    if (openDialog.getCurrentDirectory() != null) {
                        PCD.setCurrentPWD(openDialog.getCurrentDirectory());
                    }
                }
            }
        });

        super.display(result, window);
        result.add(pickFile);

        dest.add(result);
    }
}
