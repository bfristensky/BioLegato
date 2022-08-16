/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.biolegato.database.fields;

/**
 * A subclass of DBField
 * This subclass is used to provide easy access to methods which are only
 * applicable to fields which can be used as the primary key for a BLDB database
 **
 * @author Graham Alvare
 * @author Brian Fristensky
 */
public interface KeyableField extends DBField {
    /**
     * Returns the value of the current field
     **
     * @return the value of the current database field object
     */
    public String getValue();
}
