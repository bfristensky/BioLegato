package org.biopcd.widgets;

import java.awt.Component;
import java.awt.Container;
import java.io.IOException;
import java.io.Serializable;
import javax.swing.JFrame;


/**
 * <p>An interface to relate widget variables and invisible variables (such as
 * temporary files).  Relating both invisible and visible PCD variables provides
 * a common interface for BioLegato to interact with various PCD widgets, such
 * as NumberWidgets and ListWidgets.  Thus, code which executes commands (for
 * example), such as CommandButton, can be agnostic as to whether the widget is
 * visible or invisible, a numerical widget or list widget, etc.</p>
 *
 * <p>In the case of invisible widgets, the display function should be left
 * empty.</p>
 *
 * <p>In the case that a variable does not have any values (i.e. display only),
 * such as TabbedWidget, if the widget does house other widgets (like
 * TabbedWidget), then it should create a map object (by recursively calling
 * each of its child widgets' getInstance functions) containing variable names
 * as keys, and variable values as entries, where each entry corresponds to a
 * child widget.  This map object should then be passed to the WidgetInstance
 * constructor, and then returned.</p>
 *
 * <p>In the case that a variable does not have any values, and has no
 * sub-widgets, then it should just pass an empty string ("") to its
 * WidgetInstance constructor for the getInstance method.  Please note that at
 * the time of this writing, getInstance is not expected to return null (this
 * could be a future improvement, depending on whether a need arises).</p>
 **
 * @author Graham Alvare
 * @author Brian Fristensky
 */
public interface Widget extends Serializable {

    /**
     * Creates a new widget instance of the widget
     **
     * <p>A widget instance is an object that stores the value of a widget past
     * after the widget has been closed.  This is useful for concurrency.
     * Because more than one BioLegato PCD command can be run simultaneously,
     * BioLegato needs to store the values used to run each command separately.
     * For instance, if the user runs command A, then changing the value of a
     * widget in A's parameter window should not affect the currently running
     * job (i.e. command A).  This is achieved through WidgetInstance objects.
     * </p>
     *
     * <p>In the case that a variable does not have any values (i.e. display
     * only), such as TabbedWidget, if the widget does house other widgets (like
     * TabbedWidget), then it should create a map object (by recursively calling
     * each of its child widgets' getInstance functions) containing variable
     * names as keys, and variable values as entries, where each entry
     * corresponds to a child widget.  This map object should then be passed to
     * the WidgetInstance constructor, and then returned.</p>
     *
     * <p>In the case that a variable does not have any values, and has no
     * sub-widgets, then it should just return null (see CommandButton for an
     * example of this.)</p>
     **
     * @return a widget instance for usage in the current menu.
     */
    public abstract WidgetInstance getInstance();

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
    public abstract void display(Container dest, CloseableWindow window);

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
    public abstract void pcdOut (int scope, Appendable out) throws IOException;

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
    public abstract Component displayEdit (final JFrame mainFrame);

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
    public abstract void setValue(String newValue);
}
