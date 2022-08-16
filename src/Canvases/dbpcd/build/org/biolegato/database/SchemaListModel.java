/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.biolegato.database;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.swing.ListModel;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;

/**
 *
 * @author alvare
 */
public class SchemaListModel implements ListModel {

    String path;
    Set<String> schemas;
    List<String> quickSchemas;
    List<ListDataListener> listeners = new ArrayList<ListDataListener>();
    ListModel listSelf = this;
    Thread updateThread;
    
    public SchemaListModel(final String path) {
        this.path = path;
        this.quickSchemas = getSchemas(path);
        this.schemas = new HashSet<String>(quickSchemas);

        schemas.addAll(quickSchemas);

        /*this.updateThread = new Thread(new Runnable() {
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

    public void checkupdate() {
        List<String> newSchemasList = getSchemas(path);
        Set<String> newSchemas = new HashSet<String>(newSchemasList);

        if (!newSchemas.containsAll(schemas) || !schemas.containsAll(newSchemas)) {
            for (ListDataListener l : listeners) {
                l.contentsChanged(new ListDataEvent(listSelf, ListDataEvent.INTERVAL_REMOVED, 0, quickSchemas.size()));
            }
            schemas = newSchemas;
            quickSchemas = newSchemasList;
            for (ListDataListener l : listeners) {
                l.contentsChanged(new ListDataEvent(listSelf, ListDataEvent.INTERVAL_ADDED, 0, quickSchemas.size()));
            }
        }
    }

    public int getSize() {
        return quickSchemas.size();
    }

    public Object getElementAt(int index) {
        return quickSchemas.get(index);
    }

    public void addListDataListener(ListDataListener l) {
        listeners.add(l);
    }

    public void removeListDataListener(ListDataListener l) {
        listeners.remove(l);
    }


    /**
     * Get all of the schemas available for the database canvas
     **
     * @return a string array containing all of the schemas available
     */
    public static List<String> getSchemas(String path) {
        String filename;
        File searchPath = new File(path);
        List<String> schemas = new ArrayList<String>();

        if (searchPath.exists() && searchPath.isDirectory() && searchPath.canRead()) {
            for (File file : searchPath.listFiles()) {
                filename = file.getName();
                if (filename.toLowerCase().endsWith(".schema")) {
                    schemas.add(filename.substring(0, filename.length() - 7));
                }
            }
        }

        return schemas;
    }

    @Override
    protected void finalize() {
        if (updateThread != null) {
            updateThread.stop();
        }
    }
}
