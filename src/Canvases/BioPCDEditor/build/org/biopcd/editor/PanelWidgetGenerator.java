/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.biopcd.editor;

import org.biopcd.widgets.WidgetGenerator;
import org.biopcd.widgets.Widget;
import java.lang.reflect.InvocationTargetException;
import javax.swing.Icon;
import javax.swing.JFrame;

/**
 * A modified version of the widget generator, designed to handle
 * panel and tabbed widgets.  The modification is necessary to handle
 * the difference between panel (and tabbed widget) constructors, and
 * other widget type constructors.
 **
 * @author Graham Alvare
 * @author Brian Fristensky
 */
public class PanelWidgetGenerator extends WidgetGenerator {

    /**
     * Creates a new PanelWidgetGenerator object.
     **
     * @param mainFrame  the parent JFrame object to associate the widget
     *                   generator with.
     * @param display    the icon to display for the widget generator.
     * @param tooltip    the tooltip to display for the widget generator.
     * @param wclass     the widget class to generate new instances of.
     */
    public PanelWidgetGenerator(JFrame mainFrame, Icon display, String tooltip, Class wclass) {
        super(mainFrame, display, tooltip, wclass);
    }

    /**
     * Generate a new widget to drop in the destination window.
     **
     * @return the new widget object.
     */
    public Widget drop() throws NoSuchMethodException, InstantiationException,
            IllegalAccessException, IllegalArgumentException,
            InvocationTargetException {
        return ((Widget) wclass.getConstructor().newInstance());
    }
} // RandomDragAndDropPanelsDemo
