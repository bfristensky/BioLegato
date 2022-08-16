package org.biopcd.widgets;

import java.awt.Component;
import java.awt.Container;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.IOException;
import javax.swing.AbstractAction;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;


/**
 * <p>An abstract class to relate widget variables and
 * invisible variables (such as temporary files).</p>
 *
 * <p>This class extends the widget class by adding support for the 'name'
 * parameter of variables.  Each variable should have an internal name,
 * which may be used for command line substitution.</p>
 **
 * @author Graham Alvare
 * @author Brian Fristensky
 */
public abstract class SimpleWidget implements Widget {

    /**
     * The PCD variable name (this name can be referenced in the command
     * using the % symbol; for example, if the name value was set to "A",
     * the value of this widget could be accessed by using %A% (lower or
     * upper-case) within the PCD menu command string.
     */
    protected String name;

    /**
     * Creates a new instance of the abstract class SimpleWidget
     **
     * @param name  the PCD variable name (this name can be referenced
     *              in the command using the % symbol; for example,
     *              if the name value was set to "A", the value of this
     *              widget could be accessed by using %A% (lower or
     *              upper-case) within the PCD menu command string.
     */
    public SimpleWidget(String name) {
        this.name = name;
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
    public void pcdOut (int scope, Appendable out) throws IOException {
        // Generate the scope indentation for the next PCD line.
        for (int count = 0; count < scope; count++) {
            out.append("    ");
        }
        // Print the variable declaration BioPCD code.
        out.append("var \"");
        out.append(name.replaceAll("\"", "\"\""));
        out.append("\"\n");
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
    public abstract ActionListener editWindow(Container dest);

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
    public Component displayEdit (final JFrame mainFrame) {
        // Create the label object representing this widget.
        final JLabel lbl = new JLabel();
        // Create a panel for drawing the widget in its current state.
        JPanel panel = new JPanel();

        // Draw the widget in its current state to 'panel'
        display(panel, null);

        // Create an icon for the JLabel 'lbl'.  This icon should display the
        // widget (which was written tot the JPanel 'panel').
        editImage(lbl, panel);

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
                    final JDialog propertiesWindow
                            = new JDialog(mainFrame, true);

                    // Create panels for formatting the window's Components.
                    JPanel panel = new JPanel();
                    JPanel namePanel = new JPanel();

                    // Create a text field to contain the BioPCD variable name.
                    final JTextField nameField = new JTextField(name, 20);

                    // Set the layout for all of the panels in the window.
                    panel.setLayout(new BoxLayout(panel, BoxLayout.PAGE_AXIS));
                    namePanel.setLayout(new BoxLayout(namePanel,
                            BoxLayout.LINE_AXIS));

                    // Create the edit [BioPCD] variable name text box and
                    // label, and add them to the edit widget parameters window.
                    namePanel.add(new JLabel("Variable name"));
                    namePanel.add(nameField);
                    panel.add(namePanel);

                    // Get all of the widgets, and the update ActionListener for
                    // the edit parameters window.
                    final ActionListener listener = editWindow(panel);

                    // Add the update button to the edit widget parameters
                    // window.
                    panel.add(new JButton(new AbstractAction("Update") {
                        /**
                         * Update the widget's properties.
                         **
                         * @param e  the ActionEvent object representing the
                         *           action triggering the widget to update.
                         */
                        public void actionPerformed(ActionEvent e) {
                            // A panel used for updating the display of the
                            // widget's current state in the PCD editor.
                            JPanel panel2 = new JPanel();

                            // Call the editWindow's ActionListener object.
                            listener.actionPerformed(e);

                            // Get the new name from the 'nameField' text field.
                            name = nameField.getText();

                            // Dispose of the edit widget parameters window.
                            propertiesWindow.dispose();

                            // Update the display of the widget's current state
                            // in the PCD editor.
                            display(panel2, null);
                            editImage(lbl, panel2);
                        }
                    }));
                    // Display the edit parameters window.
                    propertiesWindow.setDefaultCloseOperation(
                            JDialog.DISPOSE_ON_CLOSE);
                    propertiesWindow.add(panel);
                    propertiesWindow.pack();
                    propertiesWindow.setVisible(true);
                }
                event.consume();
            }
        });

        // Return the JLabel representing the widget in its current state.
        return lbl;
    }

    /**
     * Utility method to generate the image object representing
     * the current content of the widget.  This is used by the
     * PCD editor to display widgets in the menu canvas.  The
     * reason for separating this method is that TempFile does
     * not implement a body display method; therefore, we must
     * use an alternate method to display TempFiles to the user
     **
     * @param lbl the destination label object to generate the image on
     * @param panel the panel containing the widget to draw the editImage for
     */
    public static void editImage(JLabel lbl, JPanel panel) {
        // Get the height and width for the panel object passed.
        int width = panel.getPreferredSize().width;
        int height = panel.getPreferredSize().height;

        // Displaying the widget (in its current state), by drawing it to an
        // image object, then using the image as the icon for the JLabel 'lbl'.
        BufferedImage image = new BufferedImage(width, height,
                BufferedImage.TYPE_INT_ARGB);
        Graphics2D gfx = image.createGraphics();
        recursiveDraw(panel, gfx);

        // If the width and height are not zero, set the icon for 'lbl'.
        if (width == 0 || height == 0) {
            lbl.setText("INVALID IMAGE");
        } else {
            lbl.setIcon(new ImageIcon(image));
        }
    }

    /**
     * Utility method to recursively draw every subcomponent in a Component
     * object onto a graphics object.  This method is employed by the
     * displayEdit method above to generate an image representation of
     * the current widget object.
     **
     * @param comp the Component object to recursively draw
     * @param gfx  the Graphics object to output the image to
     */
    public static void recursiveDraw(Component comp, Graphics gfx) {
        // Recursively draw all of the components in the widget.
        if (comp instanceof Container) {
            for (Component c : ((Container)comp).getComponents()) {
                if (c instanceof Container) {
                    recursiveDraw(c, gfx);
                }
            }
        }
        
        // Draw the Component 'comp' to the 'gfx' Graphics object.
        comp.setSize(comp.getPreferredSize());
        comp.setVisible(true);
        comp.doLayout();
        comp.validate();
        comp.paint(gfx);
    }
}
