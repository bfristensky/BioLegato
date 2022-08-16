/*
 * RunWindow.java
 *
 * Created on January 5, 2010, 2:06 PM
 *
 * To change this template, choose Tools | Template Manager
 * and display the template in the editor.
 */
package org.biopcd.parser;

import org.biopcd.widgets.Widget;
import org.biopcd.widgets.CloseableWindow;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Map;
import javax.swing.AbstractAction;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JScrollPane;

/**
 * The run window is used within the menu system to display
 * options for running programs
 **
 * @author Graham Alvare
 * @author Brian Fristensky
 */
public class RunWindow implements ActionListener, CloseableWindow {

    /**
     * The name of the window
     */
    private String name = null;
    /**
     * The current window
     */
    protected JDialog runWindow = null;
    /**
     * List of widgets for the window.
     */
    protected Map<String, Widget> widgets;
    /**
     * Used in serialization.
     */
    private static final long serialVersionUID = 7526472295622777007L;
    /**
     * The initial size for the run window
     */
    private static final Dimension INITIAL_SIZE = new Dimension(100, 100);
    /**
     * The minimum size for the run window
     */
    private static final Dimension MINIMUM_SIZE = new Dimension(50, 50);
    /**
     * The parent window for the run window object
     */
    private JFrame parent;
    /**
     * The icon for the current menu item
     */
    private final Image icon;

    /**
     * Creates a new instance of RunWindow
     **
     * @param name the name to display in the window's title bar
     * @param widgets the list of variable widgets to associate with the window
     */
    public RunWindow(String name, Map<String, Widget> widgets,
                     JFrame mainWindow, Image icon) {
        this.name = name;
        this.widgets = widgets;
        this.parent = mainWindow;
        this.icon = icon;
    }

    /**
     * Displays the command parameter window.
     **
     * @param e is currently ignored by this function.
     */
    public void actionPerformed(ActionEvent e) {
        // The portion of the window containing the visible variable widgets.
        Container variablePane = new Box(BoxLayout.PAGE_AXIS);

        // Print when this function is called (if DEBUG MODE is enabled).
        if (PCD.debug) {
            System.out.println("action performed!");
        }

        // Initialize and display all of the variable widgets.
        for (Widget w : widgets.values()) {
            if (PCD.debug) {
                System.out.println("widget " + w);
            }
            w.display(variablePane, this);
        }

        // Create the new dialog window object (to display widgets for user
        // input), and create a new Box for layout purposes.
        runWindow = new JDialog(parent, name, true);
        Container windowPane = new Box(BoxLayout.PAGE_AXIS);

        // Create the pane and make it scrollable.
        runWindow.setContentPane(new JScrollPane(windowPane));
        windowPane.add(variablePane);

        // Finish configuring the window.
        runWindow.setMinimumSize(MINIMUM_SIZE);
        runWindow.setSize(INITIAL_SIZE);
        runWindow.setLocationRelativeTo(parent);
        runWindow.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        runWindow.setVisible(false);

        // Set the icon (if not null).
        if (icon != null) {
            runWindow.setIconImage(icon);
        }

        // Display the window.
        runWindow.pack();
        runWindow.setVisible(true);
    }

    /**
     * Action for command buttons to close the window.
     * This is used to close parameter windows when a command is being executed.
     */
    public void close() {
        if (runWindow != null) {
            runWindow.setVisible(false);
            runWindow.dispose();
            runWindow = null;
        }
    }

    /**
     * This method returns a Component object, such as
     * a JFrame or JDialog object, which can be used as
     * a parent window object for modal child windows.
     **
     * @return the Component object to be used as a parent for child windows.
     */
    public Component getJFrame() {
        return runWindow;
    }
}
