/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.biopcd.editor;

import org.biopcd.widgets.*;
import javax.swing.JFrame;
import javax.swing.JToolBar;

/**
 * Widget panel (the tool-bar for adding widgets to an existing BioPCD file)
 **
 * @author Graham Alvare
 * @author Brian Fristensky
 */
public class MenuWidgetsPanel extends JToolBar {

    /**
     * Create a new menu widgets panel.
     **
     * @param parent  the parent window for the panel.  This is used by the
     *                WidgetGenerator class.
     */
    public MenuWidgetsPanel (JFrame parent) {
        super("Menu widgets", HORIZONTAL);

        // Make the tool-bar floatable.
        this.setFloatable(false);

        // Add drag-and-drop support to the tool-bar.
        this.setTransferHandler(new WidgetTransferHandler());

        // Add all of the WidgetGenerators to the tool bar.
        this.add(new WidgetGenerator(parent, TextWidget.WIDGET_ICON,
                "Text field", TextWidget.class));
        this.addSeparator();
        this.add(new WidgetGenerator(parent, Chooser.WIDGET_ICON,
                "Radio buttons", Chooser.class));
        this.addSeparator();
        this.add(new WidgetGenerator(parent, NumberWidget.WIDGET_ICON,
                "Number chooser", NumberWidget.class));
        this.addSeparator();
        this.add(new WidgetGenerator(parent, ComboBoxWidget.WIDGET_ICON,
                "Combobox", ComboBoxWidget.class));
        this.addSeparator();
        this.add(new WidgetGenerator(parent, ChoiceList.WIDGET_ICON,
                "Choice list", ChoiceList.class));
        this.addSeparator();
        this.add(new WidgetGenerator(parent, FileChooser.WIDGET_ICON,
                "File chooser", FileChooser.class));
        this.addSeparator();
        this.add(new WidgetGenerator(parent, DirectoryChooser.WIDGET_ICON,
                "Directory chooser", DirectoryChooser.class));
        this.addSeparator();
        this.add(new WidgetGenerator(parent, TempFile.WIDGET_ICON,
                "Temporary file (to communicate canvas content)",
                TempFile.class));
        this.addSeparator();
        this.add(new WidgetGenerator(parent, CommandButton.WIDGET_ICON,
                "Command button", CommandButton.class));
        this.addSeparator();
        this.add(new PanelWidgetGenerator(parent, TabbedWidget.WIDGET_ICON,
                "Tabbed pane", TabbedWidget.class));
        this.addSeparator();
        this.add(new PanelWidgetGenerator(parent, PanelWidget.WIDGET_ICON,
                "Horizontal panel", PanelWidget.class));

        // Display the tool-bar.
        setVisible(true);
    }
}
