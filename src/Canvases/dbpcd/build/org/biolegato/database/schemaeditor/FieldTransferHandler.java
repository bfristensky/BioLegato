/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.biolegato.database.schemaeditor;

import java.awt.datatransfer.Transferable;
import java.awt.dnd.DragSourceDragEvent;
import java.awt.dnd.DragSourceMotionListener;
import javax.swing.JComponent;
import javax.swing.TransferHandler;
import org.biolegato.database.fields.DBField;

/**
 * Database field transfer handler - for dragging and dropping database fields
 **
 * Acknowledgement - Bryan E. Smith - bryanesmith@gmail.com
 * Thanks for providing sample Java drag&drop code that helped
 * me with writing the underlying framework
 **
 * @author Graham Alvare
 * @author Brian Fristensky
 */
public class FieldTransferHandler extends TransferHandler implements DragSourceMotionListener {

    public FieldTransferHandler() {
        super();
    }

    /**
     * <p>This creates the Transferable object. In our case,
     *    RandomDragAndDropPanel implements Transferable,
     *    so this requires only a type cast.</p>
     * @param  cmp The component to make transferable.
     * @return     The transferable object coresponding to 'cmp'.
     */
    @Override()
    public Transferable createTransferable(JComponent cmp) {

        //System.out.println("Step 3 of 7: Casting the RandomDragAndDropPanel as Transferable. The Transferable RandomDragAndDropPanel will be queried for acceptable DataFlavors as it enters drop targets, as well as eventually present the target with the Object it transfers.");

        // TaskInstancePanel implements Transferable
        if (cmp instanceof Transferable) {
            return (Transferable) cmp;
        }

        // Not found
        return null;
    }

    public void dragMouseMoved(DragSourceDragEvent dsde) {}

    /**
     * <p>This is queried to see whether the component can be copied, moved, both or neither. We are only concerned with copying.</p>
     * @param  cmp The component to get source actions for.
     * @return     The source action integer for the component.
     */
    @Override()
    public int getSourceActions(JComponent cmp) {

        //System.out.println("Step 2 of 7: Returning the acceptable TransferHandler action. Our RandomDragAndDropPanel accepts Copy only.");

        if (cmp instanceof DBField) {
            System.out.println("move");
            return TransferHandler.MOVE;
        } else if (cmp instanceof Transferable) {
            System.out.println("copy");
            return TransferHandler.COPY;
        }

        return TransferHandler.NONE;
    }
} // DragAndDropTransferHandler
