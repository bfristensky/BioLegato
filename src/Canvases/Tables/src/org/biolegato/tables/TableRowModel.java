/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.biolegato.tables;

import java.util.ArrayList;
import javax.swing.ListModel;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.TableModel;

/**
 * A class used for displaying row numbers beside the table.  This class keeps
 * track of the row numbers, which are then displayed in a JList object beside
 * the JTable object.
 **
 * @author Graham Alvare
 * @author Brian Fristensky
 */
class TableRowModel implements ListModel, TableModelListener {
    /**
     * The number of rows within the table model.
     */
    int rowcount = 0;
    /**
     * The parent table model to count row numbers for.
     */
    private TableModel model;
    /**
     * A list of row headers to display (in case there are any).  These row
     * headers will override the row numbers, if they are present, until there
     * are more rows than headers.
     */
    ArrayList<String> values;
    /**
     * Tracks listeners to this object.  The listeners will be updated whenever
     * the row number model has any changes (such as new rows added).
     */
    ArrayList<ListDataListener> listeners = new ArrayList<ListDataListener>();

    /**
     * Constructs a new table row number model object.  This object will
     * manage the header for each row (whether string header or row number).
     **
     * @param model  the table model to link to this object, and elicit row
     *               counts from.
     */
    public TableRowModel(TableModel model) {
        this(model, null);
    }

    /**
     * Constructs a new table row number model object.  This object will
     * manage the header for each row (whether string header or row number).
     **
     * @param model   the table model to link to this object.
     * @param values  the list names to display as the header for each row.
     *                These string numbers will be used instead of row numbers,
     *                until there are more rows than there are entries in the
     *                'values' list object.
     */
    public TableRowModel(TableModel model, ArrayList<String> values) {
        // Copy the constructor parameters to class variables.
        this.model  = model;
        this.values = values;

        // Add this object as a table model listener.  This way, changes to the
        // number of rows in the table model will synchronize with changes to
        // the number of rows stored in/displayed by this object.
        model.addTableModelListener(this);

        // Update the count of the number of rows in the table.
        rowcount = model.getRowCount();
    }

    /**
     * Returns a count of the number of rows within the table model.  This
     * method also, forcefully, synchronizes its row counter with the table
     * model's row counter.
     **
     * @return a count of the number of rows within the table model.
     */
    public int getSize() {
        rowcount = model.getRowCount();

        return rowcount;
    }

    /**
     * Retrieves the header for given row number.  This will either be a string
     * header (if one exists for the given row), or the row number itself.
     **
     * @param index the index/number of the row to obtain the header for.
     * @return the header for the given row.
     */
    public Object getElementAt(int index) {
        // Stores the row header value.
        String value = null;

        // If there is a string header available for this row, then use it;
        // otherwise convert the row number (plus one) to a string, and return
        // it as the row header.
        if (values != null && index < values.size()) {
            value = values.get(index);
        } else {
            value = String.valueOf(index + 1);
        }

        // Return the row header value.
        return value;
    }

    /**
     * Links a listener to the current object.  The listener will be updated
     * whenever the number of rows in the table model changes.
     **
     * @param l the listener.
     */
    public void addListDataListener(ListDataListener l) {
        listeners.add(l);
    }

    /**
     * Unlinks a listener to the current object.  The listener will no longer be
     * updated (by this class) whenever the number of rows in the table model
     * changes.
     **
     * @param l the listener.
     */
    public void removeListDataListener(ListDataListener l) {
        listeners.remove(l);
    }

    /**
     * <p>Keeps the class variable 'rowcount' synchronized with the parent table
     * model's row counter.  This is done by listening to the table model and
     * re-counting the number of rows whenever the 'tableChanged' function is
     * called.</p>
     *
     * <p>In addition, this function calls all listeners (for this class).</p>
     **
     * @param evt  tracks the table change events of the parent table.  This is
     *             ignored (but may be used in the future), because this
     *             function simply re-counts the number of rows regardless of
     *             what details are contained in the TableModelEvent object.
     *             Thus, this can be null.
     */
    public void tableChanged(TableModelEvent evt) {
        // The ListDataEvent object to pass to all of this class's listeners.
        ListDataEvent result = null;

        // Ensure that the number of rows in the table has actually changed.
        if (rowcount != model.getRowCount()) {
            // Branch based on whether the number of rows has increased or
            // decreased.
            if (rowcount > model.getRowCount()) {
                // Send a new ListDataEvent event to all listeners broadcasting
                // that the number of rows in the table has increased.
                result = new ListDataEvent(this, ListDataEvent.INTERVAL_ADDED,
                        rowcount, model.getRowCount());
            } else if (rowcount < model.getRowCount()) {
                // Send a new ListDataEvent event to all listeners broadcasting
                // that the number of rows in the table has decreased.
                result = new ListDataEvent(this, ListDataEvent.INTERVAL_REMOVED,
                        rowcount, model.getRowCount());
            } else {
                // Send a new ListDataEvent event to all listeners broadcasting
                // that the number of rows in the table has not changed, but
                // somehow the contents of the rows has changed.  THIS CODE
                // SHOULD NEVER BE CALLED, BUT IS AVAILABLE FOR EXPANDING THE
                // FUNCTIONALITY OF THE LISTENERS IN THE FUTURE.
                result = new ListDataEvent(this, ListDataEvent.CONTENTS_CHANGED,
                        0, model.getRowCount());
            }

            // Communicate with the listeners and send the ListDataEvent object.
            for (ListDataListener l : listeners) {
                l.intervalAdded(result);
            }

            // Update the list model's row counter.
            rowcount = model.getRowCount();
        }
    }

    /**
     * Adds a string row header to the list of row headers.
     **
     * @param string the name of the string row header to add.
     */
    void add(String string) {
        // Ensure that the new string row header is not null.
        if (values != null) {
            // Add the row header.
            values.add(string);
        }
    }
    
}
