/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.biolegato.database.schemaeditor;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.lang.reflect.InvocationTargetException;
import javax.swing.BoxLayout;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.TransferHandler;
import org.biolegato.database.DBCanvas;
import org.biolegato.database.DBSchema;
import org.biolegato.database.fields.DBField;
import org.biolegato.database.fields.ReferenceField;

/**
 * Abstract interface to generate database fields
 **
 * Acknowledgement - Bryan E. Smith - bryanesmith@gmail.com
 * Thanks for providing sample Java drag&drop code that helped
 * me with writing the underlying framework
 **
 * @author Graham Alvare
 * @author Brian Fristensky
 */
public class FieldGenerator extends JLabel implements Transferable, MouseListener {

    protected Class wclass;
    protected DBCanvas dbcanvas;
    protected static int nameCount = 1;
    
    public static final DataFlavor FLAVOUR = new DataFlavor(DataFlavor.javaJVMLocalObjectMimeType +
";class=" + FieldGenerator.class.getName(), "Database Field Generator");

    public FieldGenerator(DBCanvas dbcanvas, Icon display, String tooltip, Class wclass) {
        super(display);

        this.wclass = wclass;
        this.dbcanvas = dbcanvas;

        // Add the listener which will export this panel for dragging
        this.addMouseListener(this);

        // Add the handler, which negotiates between drop target and this
        // draggable panel
        this.setTransferHandler(new FieldTransferHandler());

        this.setToolTipText(tooltip);
    }

    /**
     * <p>One of three methods defined by the Transferable interface.</p>
     * <p>If multiple DataFlavor's are supported, can choose what Object to return.</p>
     * <p>In this case, we only support one: the actual JPanel.</p>
     * <p>Note we could easily support more than one. For example, if supports text and drops to a JTextField, could return the label's text or any arbitrary text.</p>
     * @param  flavor The flavour to request the data to be transfered in.
     * @return The object to transfer.
     */
    public Object getTransferData(DataFlavor flavor) {
        Object transferData = null;

        // For now, assume wants this class... see loadDnD
        if (FLAVOUR.equals(flavor)) {
            transferData = this;
        }

        return transferData;
    }

    /**
     * <p>One of three methods defined by the Transferable interface.</p>
     * <p>Returns supported DataFlavor. Again, we're only supporting
     *    this actual Object within the JVM.</p>
     * <p>For more information, see the JavaDoc for DataFlavor.</p>
     * @return All of the data flavours supported by the object
     */
    public DataFlavor[] getTransferDataFlavors() {

        DataFlavor[] flavors = {FLAVOUR};

        return flavors;
    }

    /**
     * <p>One of three methods defined by the Transferable interface.</p>
     * <p>Determines whether this object supports the DataFlavor.
     *    In this case, only one is supported: for this object itself.</p>
     * @param  flavor The flavour to check and see if it is supported.
     * @return True if DataFlavor is supported, otherwise false.
     */
    public boolean isDataFlavorSupported(DataFlavor flavor) {
        return FLAVOUR.equals(flavor);
    }

    public void mousePressed(MouseEvent event) {
        System.out.println("Drag");

        TransferHandler handler = this.getTransferHandler();
        handler.exportAsDrag(this, event, TransferHandler.COPY);
    }

    public void mouseClicked(MouseEvent e) {}
    public void mouseReleased(MouseEvent e) {}
    public void mouseEntered(MouseEvent e) {}
    public void mouseExited(MouseEvent e) {}

    public DBField drop() throws NoSuchMethodException, InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
        DBField result = null;
        
        if (!ReferenceField.class.equals(wclass)) {
            result = ((DBField) wclass.getConstructor(new Class[]{String.class}).newInstance(new Object[] {"field" + nameCount++}));
        } else {
            JPanel   display = new JPanel();
            JList    dbList  = new JList(dbcanvas.getSchemas());
            DBSchema schema  = null;
            display.setLayout(new BoxLayout(display, BoxLayout.LINE_AXIS));
            display.add(new JScrollPane(dbList));

            int create = JOptionPane.showConfirmDialog(dbcanvas.getJFrame(), display, "Create Reference Field", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
            System.out.println("Selected: " + create);

            if (create == 0 && !dbList.isSelectionEmpty()) {
                System.out.println("reference field");
                schema = dbcanvas.readSchema(dbList.getSelectedValue().toString());
                if (schema != null) {
                    result = ((DBField) wclass.getConstructor(new Class[]{String.class, DBSchema.class, DBSchema.class}).newInstance(new Object[] {"field" + nameCount++, dbcanvas.getCurrentSchema(), schema}));
                }
            }
        }
        return result;
    }
} // RandomDragAndDropPanelsDemo
