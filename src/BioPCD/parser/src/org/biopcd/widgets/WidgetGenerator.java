/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.biopcd.widgets;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.lang.reflect.InvocationTargetException;
import javax.swing.Icon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.TransferHandler;

/**
 * Abstract interface to relate widget variables and invisible variables
 * (such as temporary files)
 **
 * Acknowledgement - Bryan E. Smith - bryanesmith@gmail.com
 * Thanks for providing sample Java drag&drop code that helped
 * me with writing the underlying framework
 **
 * @author Graham Alvare
 * @author Brian Fristensky
 */
public class WidgetGenerator extends JLabel
                             implements Transferable {
    /**
     * The class the widget generator will generate new instances
     * of when dropped.
     */
    protected Class wclass;
    /**
     * The parent window containing the widget generator.
     */
    protected JFrame mainFrame;
    /**
     * A name counter used to help ensure that each dropped widget has a unique
     * default name.
     */
    protected static int nameCount = 1;
    /**
     * The data flavour (of the widget generator) used for drag-and-drop.
     */
    public static final DataFlavor FLAVOUR
            = new DataFlavor(DataFlavor.javaJVMLocalObjectMimeType + ";class="
                    + WidgetGenerator.class.getName(), "Widget Generator");

    /**
     * Creates a new WidgetGenerator instance.
     **
     * @param mainFrame The parent frame for the WidgetGenerator.
     * @param display   The icon to display for the WidgetGenerator.
     * @param tooltip   The tooltip to display for the WidgetGenerator
     * @param wclass    The Widget class to generate.
     */
    public WidgetGenerator(JFrame mainFrame, Icon display,
                           String tooltip, Class wclass) {
        super(display);

        this.wclass = wclass;
        this.mainFrame = mainFrame;

        // Used to pass the WidgetGenerator reference to the MouseAdapter.
        final WidgetGenerator generator = this;

        // Add the listener which will export this panel for dragging
        // NOTE: we use a mouse adapter to make the code smaller (and avoid
        //       adding unused methods for handling mouse button release, etc.)
        this.addMouseListener(new MouseAdapter() {
            /**
             * Begin the drag-and-drop, when the mouse button is pressed.
             **
             * @param event  this is used to export the widget generator to the
             *               drag-and-drop sub-system.
             */
            @Override
            public void mousePressed(MouseEvent event) {
                TransferHandler handler = getTransferHandler();
                handler.exportAsDrag(generator, event, TransferHandler.COPY);
            }
        });

        // Add the handler, which negotiates between drop target and this
        // draggable panel
        this.setTransferHandler(new WidgetTransferHandler());

        this.setToolTipText(tooltip);
    }

    /**
     * <p>One of three methods defined by the Transferable interface.</p>
     * <p>If multiple DataFlavor's are supported, can choose what
     *    Object to return.</p>
     * <p>In this case, we only support one: the actual JPanel.</p>
     * <p>Note we could easily support more than one. For example,
     *    if supports text and drops to a JTextField, could return
     *    the label's text or any arbitrary text.</p>
     **
     * @param  flavor The flavour to request the data to be transfered in.
     * @return The object to transfer.
     */
    public Object getTransferData(DataFlavor flavor) {
        // Return a self-reference if the data flavour requested is the widget
        // generator flavour.  (If not, return null.)
        return (FLAVOUR.equals(flavor) ? this : null);
    }

    /**
     * <p>One of three methods defined by the Transferable interface.</p>
     * <p>Returns supported DataFlavor. Again, we're only supporting this
     *    actual Object within the JVM.</p>
     * <p>For more information, see the JavaDoc for DataFlavor.</p>
     **
     * @return All of the data flavours supported by the object
     */
    public DataFlavor[] getTransferDataFlavors() {
        return new DataFlavor[] {FLAVOUR};
    }

    /**
     * <p>One of three methods defined by the Transferable interface.</p>
     * <p>Determines whether this object supports the DataFlavor. In this case,
     *    only one is supported: for this object itself.</p>
     **
     * @param  flavor The flavour to check and see if it is supported.
     * @return True if DataFlavor is supported, otherwise false.
     */
    public boolean isDataFlavorSupported(DataFlavor flavor) {
        return FLAVOUR.equals(flavor);
    }

    /**
     * Generate a new widget to drop in the destination window.
     **
     * @return the new widget object.
     */
    public Widget drop() throws NoSuchMethodException, InstantiationException,
            IllegalAccessException, IllegalArgumentException,
            InvocationTargetException {
        return ((Widget) wclass.getConstructor(new Class[]{
            String.class }).newInstance(new Object[] {"var" + nameCount++}));
    }
} // RandomDragAndDropPanelsDemo
