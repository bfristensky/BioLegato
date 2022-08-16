/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.biolegato.database;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import javax.swing.ListModel;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;

/**
 * A list model object for displaying the entries of a given
 * table/schema within a database.  This list model is designed
 * to display all of the entries within a schema by the value
 * of each entry's name-column.  The list model also retains
 * functions for more in-depth querying, such as searching for
 * a given entry based on its key.
 **
 * @author Graham Alvare
 * @author Brian Fristensky
 */
public class EntriesListModel implements ListModel {

    /**
     * The database table/schema to display the entries for
     */
    protected DBSchema schema;
    /**
     * The SQL connection object to use for retrieving the entries
     */
    protected Connection conn;
    /**
     * The current 'cached' list of database entries for the given schema
     * This is used as a reference and modified by the function checkupdate.
     */
    protected List<Entry> entries;
    /**
     * The list of ListDataListener objects associated with the list model
     */
    protected List<ListDataListener> listeners = new ArrayList<ListDataListener>();
    /**
     * A self-reference for any specialized subclass objects defined within
     * the scope of a method within this class.
     *
     * For example:
     * this.updateThread = new Thread(new Runnable() {
     *      public void run() {
     *          while (true) {
     *              try {
     *                  listSelf.checkupdate();
     *                  Thread.sleep(5 * 1000);
     *              } catch (InterruptedException ex) {
     *                  ex.printStackTrace(System.err);
     *              }
     *          }
     *      }
     *
     *  });
     */
    public ListModel listSelf = this;
    /**
     * A pointer to the current update thread for the list model.
     * This may be optionally used to ensure that the contents of the list
     * are kept up-to-date; however, this could affect the findIndex function
     * if this thread is used.
     */
    private Thread updateThread = null;

    /**
     * Creates a new instance of the entries list model object - a list model
     * for displaying all of the entries of a schema.  See class description
     * for more details.
     **
     * @param conn the database connection to perform SQL queries on
     * @param schema the database schema to display entries for
     */
    public EntriesListModel(final Connection conn, final DBSchema schema) {
        // copy the following constructor parameters into object variables
        this.conn    = conn;
        this.schema  = schema;
        this.entries = getEntries(conn, schema);

        // The code below was taken out of service because it would negatively
        // affect the functionality of the method 'findIndex'
        /*
         this.updateThread = new Thread(new Runnable() {
            public void run() {
                while (true) {
                    try {
                        checkupdate();
                        Thread.sleep(5 * 1000);
                    } catch (InterruptedException ex) {
                        ex.printStackTrace(System.err);
                    }
                }
            }

        });
        updateThread.start();*/
    }

    /**
     * Checks for updates to the entries within a database schema and updates
     * the list model when changes are detected.
     */
    public void checkupdate() {
        // the newly fetched schema entry cache to test for changes
        List<Entry> newEntriesList = getEntries(conn, schema);

        // if a change is detected, then the if statement will evaluate to true
        if (!newEntriesList.containsAll(entries) || !entries.containsAll(newEntriesList)) {
            // tell all of the listeners that the list has lost
            // all of ist old entries (we are communicating a
            // 'delete all and replace' to all listener objects.
            for (ListDataListener l : listeners) {
                l.contentsChanged(new ListDataEvent(listSelf, ListDataEvent.INTERVAL_REMOVED, 0, entries.size()));
            }

            // update the entries cache to the newest version
            entries = newEntriesList;

            // tell all of the listeners that the list has
            // gained the new entries (in newEntriesList)
            for (ListDataListener l : listeners) {
                l.contentsChanged(new ListDataEvent(listSelf, ListDataEvent.INTERVAL_ADDED, 0, entries.size()));
            }
        }
    }

    /**
     * Returns the current size of the list model
     * (the # of database entries for the current schema)
     **
     * @return the current size of the list model
     */
    public int getSize() {
        return entries.size();
    }

    /**
     * Returns a specific Entry object for the given index
     **
     * @param index the list index to return the object for
     * @return the corresponding Entry object
     */
    public Object getElementAt(int index) {
        return entries.get(index);
    }

    /**
     * Adds a listener to the list model's collection of listener objects
     **
     * @param l the listener to add
     */
    public void addListDataListener(ListDataListener l) {
        listeners.add(l);
    }

    /**
     * Removes a listener from the list model's collection of listener objects
     **
     * @param l the listener to add
     */
    public void removeListDataListener(ListDataListener l) {
        listeners.remove(l);
    }

    /**
     * Get all of the entries for the current database schema
     **
     * @param schema the schema to retrieve entries for
     * @return a string array containing all of the entries for the schema
     */
    public static List<Entry> getEntries(Connection conn, DBSchema schema) {
        // the SQL name of the schema's key column
        String keycol = schema.getKey().getName();
        // the SQL name of the schema's name-column
        String namecol = schema.getNameCol().getName();
        // the SQL statement object o perform database queries with
        Statement stmnt = null;
        // the result set produced by querying the database for entries
        ResultSet results = null;
        // the list of entries retrieved from the database
        List<Entry> entries = new ArrayList<Entry>();

        // ensure that the database connection is NOT null before proceeding
        if (conn != null) {
            try {
                // execute
                stmnt = conn.createStatement();

                // blanked out - for debug purposes only
                //System.out.println("SQL: SELECT " + namecol + "," + keycol + " FROM " + schema.getTable());

                // execute the select query and get the results
                // the query below selects the name-column and key
                // column for every row for the given schema in the database
                results = stmnt.executeQuery("SELECT " + namecol + "," + keycol + " FROM " + schema.getTable());

                // loop through the result set and migrate
                // the entries into a list of entry objects
                while (results.next()) {
                    entries.add(new Entry(results.getString(keycol), results.getString(namecol)));
                }
            } catch (Exception ex) {
                ex.printStackTrace(System.err);
            } finally {
                // ensure that the result set is closed after the query
                if (results != null) {
                    try {
                        results.close();
                    } catch (SQLException ex) {
                        ex.printStackTrace(System.err);
                    }
                }

                // ensure that the statement object is closed after the query
                if (stmnt != null) {
                    try {
                        stmnt.close();
                    } catch (SQLException ex) {
                        ex.printStackTrace(System.err);
                    }
                }
            }
        }

        return entries;
    }

    /**
     * An overridden finalize method - if the updateThread variable is used
     * to ensure continuous dynamic updating, then we must stop the thread
     * when the object is deleted to avoid memory leak and Exception throwing.
     */
    @Override
    protected void finalize() {
        if (updateThread != null) {
            updateThread.stop();
        }
    }

    /**
     * Determine the index value for a given database key within the list model
     **
     * @param key the key value to search with
     * @return the index value for the database key within the drop-down list
     */
    public int findIndex(String key) {
        int index = 0;

        // loop through the indices of the array to find the index number for the given key.
        for (index = 0; index < entries.size() && !entries.get(index).equals(key); index++) {
            // empty loop to find the correct index
        }
        return index;
    }

    /**
     * A class used to store the relationship between a database key and the
     * database name-column value for a given database entry.
     * The general purpose of this class is to allow the list model to search
     * by database key (using the findIndex method), while displaying only the
     * name-column value of each entry to the user
     **
     * @author Graham Alvare
     * @author Brian Fristensky
     */
    public static class Entry {
        /**
         * The schema key value for the current database entry
         */
        private String key;
        /**
         * The name-column value for the current database entry
         */
        private String value;

        /**
         * Constructs a new schema entry object for use within the list model
         **
         * @param key the schema key value for the current database entry
         * @param value the name-column value for the current database entry
         */
        public Entry (String key, String value) {
            this.key = key;
            this.value = value;
        }

        /**
         * Returns a string representation of the database entry object.
         * This string representation is just the value of the name-column
         * (variable name 'value').  This method is called toString because
         * we wish to make any JList (or equivalent) display objects display
         * the entry's name-column value (JList calls the toString method when
         * determining what to display), while retaining all of the additional
         * functionality when the JList (or equivalent)'s getSelectedValues
         * method is called (which returns an array of the raw objects).
         **
         * @return the name-column value for the current database entry
         */
        @Override
        public String toString() {
            return value;
        }

        /**
         * Returns the schema key value for the database entry
         **
         * @return the schema key value for the current database entry
         */
        public String getKey() {
            return key;
        }

        /**
         * Tests an arbitrary object 'o' for equality with the current
         * entry object.  If the object tested is not an Entry object,
         * then the object will be tested for equality based on whether
         * it is equal to the entry's schema key value.  If the object
         * is an entry object, then the schema keys of both entries
         * will be tested for equality.
         **
         * @param o the object to test for equality
         * @return the result of the test
         */
        @Override
        public boolean equals(Object o) {
            // store the result of the equality test in a boolean object
            // this is to streamline the different tests we can perform
            // to determine equality based on whether the object 'o' is
            // an Entry object, or another type of other object
            boolean result = false;

            // branched based on what type of object is 'o'.
            if (o instanceof Entry) {
                result = ((Entry)o).key.equals(key);
            } else {
                result = o.equals(key);
            }
            return result;
        }
    }
}
