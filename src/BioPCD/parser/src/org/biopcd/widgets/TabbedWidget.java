package org.biopcd.widgets;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.datatransfer.Transferable;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.dnd.DropTargetEvent;
import java.awt.dnd.DropTargetListener;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;

/**
 * A tabbed widget enables a PCD menu to have tabs with sub-widgets.  This
 * functionality enables PCD menu creators to organize the options for a program
 * into groups, making it easier for the user to run complicated or multiple
 * step programs.
 **
 * @author Graham Alvare
 * @author Brian Fristensky
 */
public class TabbedWidget implements Widget {
    /**
     * Temporarily stores parameters before they are added to the TabbedWidget.
     */
    Map<String, Map<String, Widget>> tabs
            = new LinkedHashMap<String, Map<String, Widget>>();
    /**
     * Used for serialization purposes.
     */
    private static final long serialVersionUID = 7526472295622776161L;
    /**
     * The icon for the widget.
     */
    public static final Icon WIDGET_ICON = new ImageIcon(
            NumberWidget.class.getClassLoader().getResource(
                    "org/biopcd/icons/tabbedwidget.png"));


    /**
     * Creates a new instance of a tabbed widget
     */
    public TabbedWidget () {
    }

    /**
     * Adds a new tab to the tabbed widget
     **
     * @param name        the name of the new tab to create.  This name will
     *                    be displayed to the user.
     * @param tabWidgets  the collection of widgets to display whenever the
     *                    tab is selected (i.e., the widgets which are
     *                    associated with this tab.
     */
    public void addTab(String name, Map<String, Widget> tabWidgets) {
        tabs.put(name, tabWidgets);
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
        // Create a new JTabbedPane to display all of the tabs in the widget.
        JTabbedPane container = new JTabbedPane();

        // Iterate through each tab in the widget and add it to "container".
        for (Map.Entry<String, Map<String, Widget>> tb : tabs.entrySet()) {
            // Create a box align every component within the tab along the
            // PAGE_AXIS (i.e. Y-axis from top to bottom).
            Box tabdata = new Box(BoxLayout.PAGE_AXIS);

            // Iterate through every widget in the tab and add it to the Box
            // (which adds it, in turn, to the JTabbedPane).
            for (Widget widget : tb.getValue().values()) {
                widget.display(tabdata, window);
            }

            // Add the Box to the JTabbedPane.
            container.addTab(tb.getKey(), tabdata);
        }

        // Add the JTabbedPane to the destination Container object.
        dest.add(container);
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
     * <p>In this case TabbedWidgets do not have any values (i.e. display only);
     * however, TabbedWidgets CAN house house other widgets (such as
     * NumberWidgets), which CAN have values.  Therefore, the PanelWidget class
     * returns a map object (generated by recursively calling each of its child
     * widgets' getInstance functions) contained inside a WidgetInstace object.
     * The map is comprised of WidgetInstances for each child widget to the
     * panel.  The keys of the map are the variable names for the child widgets,
     * and the entries are the actual WidgetInstance objects.</p>
     **
     * @return a widget instance for usage in the current menu.
     */
    public WidgetInstance getInstance () {
        // Create a new map to store all of the WidgetInstance objects for
        // the widgets contained within all of the tabs.
        Map<String, WidgetInstance> valuemap
                = new LinkedHashMap<String, WidgetInstance>();

        // Iteate through each tab.
        for (Map<String, Widget> tabvariables : tabs.values()) {
            // Iterate through each widget in each tab
            for (Map.Entry<String, Widget> widget : tabvariables.entrySet()) {
                // Add the widget to the map "valuemap".
                // The 'name' of the widget is the map key, and the
                // WidgetInstance is the value of the map entry.
                valuemap.put(widget.getKey(), widget.getValue().getInstance());
            }
        }

        // Create and return a new WidgetInstance, which will house 'valuemap'
        return new WidgetInstance(valuemap);
    }

    /**
     * Populates a container with the component objects
     * necessary for editing the current widget
     **
     * @param  parent the JDialog/JFrame/etc. which contains the edit
     *                information (this parameter should ONLY be used
     *                for creating modal child windows for displaying
     *                error messages and the like!)
     * @param  dest   the destination to display the widget-editing components
     * @return the action listener associated with updating the current widget
     */
    public ActionListener edit(Component parent, Container dest) {
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
    public void pcdOut(int scope, Appendable out) throws IOException {
        // Adjust the scope level of the current line by adding
        // groups of four (4) spaces for each scope level.
        for (int count = 0; count < scope; count++) {
            out.append("    ");
        }
        // Print 'tabset' (this precedes any tab widget specification)
        out.append("tabset\n");

        // Print out each of the tabs for the tabbed widget.
        // NOTE: the scope of each tab is one level higher than "tabset"
        for (Map.Entry<String, Map<String, Widget>> tentry : tabs.entrySet()) {
            // Adjust the scope level of the current line by adding
            // groups of four (4) spaces for each scope level.
            for (int count = 0; count < scope + 1; count++) {
                out.append("    ");
            }

            // Print out the tab name.
            out.append("tab \"");
            out.append(tentry.getKey().replaceAll("\"", "\"\""));
            out.append("\"\n");

            // Print out every widget contained within the tab.
            // NOTE: the scope of each widget will be 2 levels higher than
            //       "tabset" (or, in other words, 1 level higher than the
            //       scope level for the tab declaration).
            for (Map.Entry<String,Widget> w : tentry.getValue().entrySet()) {
                w.getValue().pcdOut(scope + 2, out);
            }
        }
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
    public Component displayEdit(final JFrame mainFrame) {
        // The panel to display everything.  This is where all of the Components
        // associated with editing the TabbedWidget will be displayed.
        final JPanel openPane = new JPanel();

        // A JTabbedPane object to display the current status of the
        // TabbedWidget being edited.
        final JTabbedPane tabpane = new JTabbedPane();

        // A panel to organize all of the buttons and components associated with
        // adding new tabs to the TabbedWidget.
        final JPanel addPnl = new JPanel();

        // Create a text field for adding tabs to the TabbedWidget.  This text
        // field will contain the name for the new tab.
        final JTextField newTabName = new JTextField("new tab", 20);

        // Create a new button for adding tabs to the TabbedWidget.
        final JButton addTabButton = new JButton("Add");

        // Create a new DropTarget to accept widgets which are dropped onto
        // the TabbedWidget.  This is important because it allows the user to
        // add widgets to a tab within the TabbedWidget.
        final DropTarget dropTarget = new DropTarget(tabpane,
            new DropTargetListener() {
                /**
                 * A running count of the number of widgets added to the
                 * TabbedWidget.  This will be used for ensuring each variable
                 * has a unique name within the 'tabs' hashtable.
                 */
                private int added = 0;

                /**
                 * Handles when the drop action has changed
                 * (DOES NOTHING AT THIS MOMENT!)
                 **
                 * @param dte  Ignored, because this method currently does
                 *             nothing.
                 */
                public void dragEnter(DropTargetDragEvent dtde) {}

                /**
                 * Handles when a drag cursor enters the drop target area.
                 **
                 * @param dte  Ignored, because this method changes the cursor
                 *             regardless of whatever extraneous information
                 *             about the event is available.
                 */
                public void dragOver(DropTargetDragEvent dtde) {
                    openPane.setCursor(Cursor.getPredefinedCursor(
                            Cursor.HAND_CURSOR));
                }

                /**
                 * Handles when the drop action has changed
                 * (DOES NOTHING AT THIS MOMENT!)
                 **
                 * @param dte  Ignored, because this method currently does
                 *             nothing.
                 */
                public void dropActionChanged(DropTargetDragEvent dtde) {}

                /**
                 * Handles when a drag cursor leaves the drop target area.
                 **
                 * @param dte  Ignored, because this method changes the cursor
                 *             regardless of whatever extraneous information
                 *             about the event is available.
                 */
                public void dragExit(DropTargetEvent dte) {
                    openPane.setCursor(Cursor.getPredefinedCursor(
                            Cursor.CROSSHAIR_CURSOR));
                }

                /**
                 * <p>The user drops the item. Performs the drag
                 *    and drop calculations and layout.</p>
                 * @param dtde  The event object containing all of the
                 *              information necessary and available for the
                 *              drag-and-drop procedure.
                 */
                public void drop(DropTargetDropEvent dtde) {
                    try {
                        // Declare the variables for performing the drop.
                        Widget widget = null;
                        Object transferableObj = null;
                        Transferable trans = null;

                        // Get the index of the currently displayed tab.
                        // Make sure this is not negative (negative means that
                        // no tab is selected, in which case we shall default
                        // to the first tab being selected.)
                        int stab = Math.max(0, tabpane.getSelectedIndex());

                        // Get the name of the tab which is currently selected.
                        String tabName = tabpane.getTitleAt(stab);

                        // Get the panel for the currently selected tab.
                        JPanel tabComponent
                                = (JPanel)tabpane.getComponentAt(stab);

                        // Reset the cursor to normal.
                        openPane.setCursor(Cursor.getDefaultCursor());

                        System.out.println("dropped in tab: " + tabName);

                        // Get the transferable object for the event.
                        trans = dtde.getTransferable();

                        // What does the Transferable support?  If it supports
                        // WidgetGenerators, then we can perform a drop action.
                        if (trans.isDataFlavorSupported(
                                WidgetGenerator.FLAVOUR)) {
                            // Get the WidgetGenerator object being dropped.
                            transferableObj
                                    = dtde.getTransferable().getTransferData(
                                            WidgetGenerator.FLAVOUR);
                            
                            /////////////////////////////
                            // Perform the drop action //
                            /////////////////////////////
                            // 1. Create a widget from the dropped
                            //    WidgetGenerator object.
                            // 2. Add the widget to the tab (so that it is
                            //    displayed within the editor panel.)
                            // 3. Add the widget to the current tab's hashtable.
                            // 4. Increment the number of variables added
                            //    counter, such that each variable has a unique
                            //    identifier within the TabbedWidget.  This is
                            //    important because the new variable
                            //    should have a unique name,
                            /////////////////////////////
                            widget = ((WidgetGenerator)transferableObj).drop();
                            tabComponent.add(widget.displayEdit(mainFrame));
                            tabs.get(tabName).put("var" + added, widget);
                            added++;
                        }

                        // Update the layout of the editor display panel.
                        openPane.doLayout();
                        openPane.setSize(openPane.getPreferredSize());
                        openPane.validate();
                        openPane.repaint(50L);
                    } catch (Exception ex) {
                        // Print any error messages produced by the drop event.
                        ex.printStackTrace(System.err);
                    }
                }
            });

        // Create a KeyListener object to listen for the key presses and provide
        // feedback to the user as to whether the name for the new tab, which
        // the user is entering, is valid.  A tab name is valid if it is not
        // blank AND it is unique (i.e. there cannot be two tabs with the same
        // name).  If the name is invalid, a red border will appear around the
        // add tab panel, and the add tab button will become disabled.
        final KeyListener checkTabAdd = new KeyListener() {
            /**
             * Handle keys typed by the user.  This method will either make the
             * invalid (red) border appear if the new tab name is invalid, or
             * make it disappear if the new tab name valid.  Additionally, if
             * the name is invalid, the "Add Tab" button will be disabled, and
             * if re-enabled once the name is valid again.  Validity of new tab
             * names is as follows: if the name is blank or not unique (i.e.
             * already exists), the name is invalid; all other names are valid.
             **
             * @param e  the KeyEvent to obtain information about which key was
             *           pressed by the user. Note, THIS IS IGNORED, because
             *           we are not interested in which key was typed; but
             *           rather, what the current name for the new tab is.
             */
            public void keyTyped(KeyEvent e) {
                // Obtain the current text in the new tab text field.
                String tabName = newTabName.getText();

                // Test the new tab name for validity
                if ("".equals(tabName) || tabs.containsKey(tabName)) {
                    // Invalid tab name.  Make a red border around the add tab
                    // panel, and disable the "Add Tab" button.
                    addPnl.setBorder(BorderFactory.createLineBorder(Color.RED));
                    addTabButton.setEnabled(false);
                } else {
                    // Valid tab name.  Remove any red border around the add tab
                    // panel, and enable the "Add Tab" button.
                    addPnl.setBorder(BorderFactory.createEmptyBorder());
                    addTabButton.setEnabled(true);
                }
            }

            /**
             * <p>Handle keys typed by the user.  This method will either make
             * the invalid (red) border appear if the new tab name is invalid,
             * or make it disappear if the new tab name valid.  Additionally, if
             * the name is invalid, the "Add Tab" button will be disabled, and
             * if re-enabled once the name is valid again.  Validity of new tab
             * names is as follows: if the name is blank or not unique (i.e.
             * already exists), the name is invalid; all other names are valid.
             * </p>
             *
             * <p><i>NOTE: This function just calls 'keyTyped', thus it provides
             *       no additional functionality.</i></p>
             **
             * @param e  the KeyEvent to obtain information about which key was
             *           pressed by the user.
             */
            public void keyPressed(KeyEvent e) {
                keyTyped(e);
            }

            /**
             * <p>Handle keys typed by the user.  This method will either make
             * the invalid (red) border appear if the new tab name is invalid,
             * or make it disappear if the new tab name valid.  Additionally, if
             * the name is invalid, the "Add Tab" button will be disabled, and
             * if re-enabled once the name is valid again.  Validity of new tab
             * names is as follows: if the name is blank or not unique (i.e.
             * already exists), the name is invalid; all other names are valid.
             * </p>
             *
             * <p><i>NOTE: This function just calls 'keyTyped', thus it provides
             *       no additional functionality.</i></p>
             **
             * @param e  the KeyEvent to obtain information about which key was
             *           pressed by the user.
             */
            public void keyReleased(KeyEvent e) {
                keyTyped(e);
            }

        };

        // Create an action listener for the "Add Tab" button.  This function
        // will test for tab name validity and add valid new tabs to the
        // TabbedWidget.
        final ActionListener addTab = new ActionListener() {
            /**
             * <p>Count the number of tabs added to the TabbedWidget.  The
             * purpose of this counter is to create a new default name for the
             * new tab text field.  Thus, the user can click new tab continually
             * (without manually changing the text in the new tab text field),
             * and new tabs will be generated.</p>
             *
             * <p>After a new tab is created, this value is incremented, and the
             * value of the new tab text field will become "new tab #" + tcount
             * </p>
             */
            private int tcount = 1;

            /**
             * Create the new tab.
             **
             * @param e  this is ignored by the method (only one object -- the
             *           add tab button) should be calling this ActionListener.
             */
            public void actionPerformed(ActionEvent e) {
                // The name for the new tab to create.
                final String tabName = newTabName.getText();
                // The panel to contain widgets within the new tab.
                final JPanel newTb = new JPanel();

                // Test for tab name validity, and print relevant error messages
                if ("".equals(tabName)) {
                    // Print an error message indicating that the tab name is
                    // BLANK.
                    JOptionPane.showMessageDialog(mainFrame,
                            "ERROR!  You cannot create tabs with blank names");
                } else if (tabs.containsKey(tabName)) {
                    // Print an error message indicating that the tab name is
                    // NOT unique.
                    JOptionPane.showMessageDialog(mainFrame,
                            "ERROR!  A tab with that name already exists\n" +
                            "All tabs in a tab pane must be named uniquely");
                } else {
                    // Add the new tab to the TabbedWidget.
                    tabpane.addTab(tabName, newTb);

                    // Create a new entry in the tabs hashtable, so that
                    // sub-widgets for the tab can be easily accessed.
                    tabs.put(tabName, new LinkedHashMap<String, Widget>());

                    // Set the layout manager for the new tab.
                    newTb.setLayout(new BoxLayout(newTb, BoxLayout.PAGE_AXIS));

                    // Configure the new tab to be able to accept widgets which
                    // are drag-and-dropped ontop of it.
                    newTb.setTransferHandler(new WidgetTransferHandler());
                    newTb.setDropTarget(dropTarget);

                    // Set the new tab to be the currently opened tab within
                    // the TabbedWidget.
                    tabpane.setSelectedComponent(newTb);

                    // Increment the number of tabs counter.
                    tcount++;

                    // Change the default name for the next tab within the
                    // tabbed widget.
                    newTabName.setText("new tab #" + tcount);

                    // Call the KeyListner object for the new tab text field,
                    // to check for name validity and display a red border
                    // if the new tab name is invalid.
                    checkTabAdd.keyTyped(null);
                }
            }
        };

        // Add the action listener for adding tabs to the "Add Tab" button.
        addTabButton.addActionListener(addTab);

        // Format the add tab/new tab panel.
        addPnl.setLayout(new BoxLayout(addPnl, BoxLayout.LINE_AXIS));
        addPnl.add(new JLabel("New tab:"));
        addPnl.add(newTabName);
        addPnl.add(addTabButton);

        // Add a key listener to the tab name text field.  This key listener
        // will listen for key presses and provide feedback to the user as to
        // whether the name for the tab is valid or not.
        newTabName.addKeyListener(checkTabAdd);

        // Code for handling drag and drop events from widget objects.
        tabpane.setTransferHandler(new WidgetTransferHandler());

        // Create the listener to do the work when dropping on this object!
        tabpane.setDropTarget(dropTarget);

        // Add all of the tabs and subwidgets to the JTabbedPane.
        for (Map.Entry<String, Map<String, Widget>> tb : tabs.entrySet()) {
            JPanel newTb = new JPanel();

            // Add the tab to the JTabbedPane.
            tabpane.addTab(tb.getKey(), newTb);
            newTb.setLayout(new BoxLayout(newTb, BoxLayout.PAGE_AXIS));
            newTb.setTransferHandler(new WidgetTransferHandler());
            newTb.setDropTarget(dropTarget);
            tabpane.setSelectedComponent(newTb);
            
            // Add all of the sub-widgets to the tab in the display editor.
            for (Widget widget : tb.getValue().values()) {
                newTb.add(widget.displayEdit(mainFrame));
            }
        }

        // Format the final display panel, and add all of the components,
        // associated with editing, to it.  In other words, we will be adding
        // the tabpane to display the current status of the TabbedWidget, and
        // the add tab panel to allow the user to create new tabs within the
        // widget.  openPane is the panel which will be displayed to the user.
        openPane.setLayout(new BorderLayout());
        openPane.add(tabpane, BorderLayout.CENTER);
        openPane.add(addPnl, BorderLayout.SOUTH);

        // Return the openPane object to the calling method.
        return openPane;
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
     *       TabbedWidget), this method is left empty.</i></p>
     **
     * @param newValue  the new value for the widget.
     */
    public void setValue(String newValue) {
    }
}

