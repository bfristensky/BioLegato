/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.biopcd.widgets;

/**
 * <p>The corresponding widget instance, to store variable information
 * until the current command is finished execution.</p>
 * 
 * <p>A widget instance is an object that stores the value of a widget past
 * after the widget has been closed.  This is useful for concurrency.
 * Because more than one BioLegato PCD command can be run simultaneously,
 * BioLegato needs to store the values used to run each command separately.
 * For instance, if the user runs command A, then changing the value of a
 * widget in A's parameter window should not affect the currently running
 * job (i.e. command A).  This is achieved through WidgetInstance objects.</p>
 *
 * <p>In the case that a variable does not have any values (i.e. display only),
 * such as TabbedWidget, if the widget does house other widgets (like
 * TabbedWidget), then it should create a map object (by recursively calling
 * each of its child widgets' getInstance functions) containing variable
 * names as keys, and variable values as entries, where each entry
 * corresponds to a child widget.  This map object should then be passed to
 * the WidgetInstance constructor, and then returned.</p>
 *
 * <p>In the case that a variable does not have any values, and has no
 * sub-widgets, then it should just return null (see CommandButton for
 * an example of this.)</p>
 **
 * @author Graham Alvare
 * @author Brian Fristensky
 */
public class WidgetInstance {
    /**
     * The value for the widget instance to store.
     */
    private Object value;

    /**
     * Creates a new widget instance for storing variable information
     * for the command (until command completion)
     **
     * @param value the value for the new widget instance to store.
     */
    public WidgetInstance(Object value) {
        this.value = value;
    }

    /**
     * Returns the value stored by the WidgetInstance object.
     **
     * @return the value stored by the WidgetInstance object.
     */
    public Object getValue() {
        return value;
    }

    /**
     * <p>Notifies the variable that the program has now completed successfully.
     * This allowed the widget to perform operations based on no longer being
     * visible.</p>
     *
     * <p>A good example of such usage could be to release files held reference
     * by the object.  Another example could be to remove any references to
     * no longer needed objects such as those representing its swing display
     * (i.e. JComponents which are no longer visible and will likely not be
     * reused). The latter example is recommended when possible, so as to
     * conserve memory.</p>
     */
    public void close() {}
}
