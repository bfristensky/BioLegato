/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.biopcd.editor;

import java.awt.BorderLayout;
import org.biopcd.widgets.Widget;
import org.biopcd.widgets.WidgetGenerator;
import org.biopcd.widgets.WidgetTransferHandler;
import java.awt.Cursor;
import java.awt.datatransfer.Transferable;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.dnd.DropTargetEvent;
import java.awt.dnd.DropTargetListener;
import java.io.Flushable;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import javax.swing.BoxLayout;
import javax.swing.JFrame;
import javax.swing.JPanel;
import org.biopcd.parser.SystemToken;

/**
 * The PCD menu canvas allows the user to edit a PCD menu.  The canvas allows
 * dragging-and-dropping WidgetGenerators, from which widgets will be created
 * for the menu.  The canvas additionally stores information about the PCD menu
 * itself, such as the name, icon, tooltip, and exec parameter (if the menu just
 * executes without any user parameter input).  Additionally, the canvas will
 * store which systems are supported by the command run by the menu, and which
 * widgets to display (if any) when the menu is clicked.
 **
 * @author Graham Alvare
 * @author Brian Fristensky
 */
public class MenuCanvas extends JPanel implements DropTargetListener {
    /**
     * The parent window of the menu canvas.
     */
    protected JFrame parent;
    /**
     * The name of the PCD menu.
     */
    protected String name = "New program";
    /**
     * The icon for the PCD menu.
     */
    protected String icon = "";
    /**
     * The tool-tip for the PCD menu.
     */
    protected String tip  = "";
    /**
     * <p>The exec string for the PCD menu.  If this string is not blank and not
     * null, when the PCD menu is clicked on by the user (in BioLegato, NOT the
     * editor), the command in the exec string will be run without prompting
     * the user to enter values for running the command.</p>
     *
     * <p><i>NOTE: The usual behaviour (when 'exec' is left blank) is for a
     *       "Run" command button the be added to the menu.  The "Run" command
     *       button, when clicked, will run the command.  This allows the user
     *       to set the parameters for the command via the menu's widgets.</i>
     * </p>
     */
    protected String exec = "";
    /**
     * The systems and architectures supported by the command.
     */
    protected Set<SystemToken> systems = new HashSet<SystemToken>();
    /**
     * The list of widgets associated with the command.
     */
    protected ArrayList<Widget> widgetList = new ArrayList<Widget>();
    /**
     * The main JPanel for the canvas.  This is where all the widgets (in their
     * editor form) will be displayed for the user to interact with them.
     */
    protected JPanel mainPanel = new JPanel();

    /**
     * Create a new menu canvas.
     **
     * @param parent  the parent window to house the canvas.  This is important
     *                for creating modal dialog boxes.
     */
    public MenuCanvas(final JFrame parent) {
        this.parent = parent;

        // Again, needs to negotiate with the draggable object
        setTransferHandler(new WidgetTransferHandler());

        // Create the listener to do the work when dropping on this object!
        setDropTarget(new DropTarget(this, this));

        setLayout(new BorderLayout());
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.PAGE_AXIS));

        add(mainPanel, BorderLayout.CENTER);
        add(new JPanel(), BorderLayout.SOUTH);
    }

    /**
     * Handles when the drop action has changed (DOES NOTHING AT THIS MOMENT!)
     **
     * @param dtde  Ignored, because this method currently does nothing.
     */
    public void dragEnter(DropTargetDragEvent dtde) {}

    /**
     * Handles when a drag cursor enters the drop target area.
     **
     * @param dtde  Ignored, because this method changes the cursor regardless
     *              of whatever extraneous information about the event is
     *              available.
     */
    public void dragOver(DropTargetDragEvent dtde) {
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    }

    /**
     * Handles when the drop action has changed (DOES NOTHING AT THIS MOMENT!)
     **
     * @param dtde  Ignored, because this method currently does nothing.
     */
    public void dropActionChanged(DropTargetDragEvent dtde) {}

    /**
     * Handles when a drag cursor leaves the drop target area.
     **
     * @param dte  Ignored, because this method changes the cursor regardless
     *             of whatever extraneous information about the event is
     *             available.
     */
    public void dragExit(DropTargetEvent dte) {
        setCursor(Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));
    }

    /**
     * <p>The user drops the item. Performs the drag and drop calculations
     *      and layout.</p>
     **
     * @param dtde  The event object containing all of the information necessary
     *              and available for the drag-and-drop procedure.
     */
    public void drop(DropTargetDropEvent dtde) {
        // Declare the variables for performing the drop.
        Widget widget = null;
        Object transferableObj = null;
        Transferable transferable = null;

        // Reset the cursor to normal.
        setCursor(Cursor.getDefaultCursor());

        try {
            // Get the transferable object for the event.
            transferable = dtde.getTransferable();

            // What does the Transferable support?  If it supports
            // WidgetGenerators, then we can perform a drop action.
            if (transferable.isDataFlavorSupported(WidgetGenerator.FLAVOUR)) {
                // Get the WidgetGenerator object being dropped.
                transferableObj = dtde.getTransferable().getTransferData(
                        WidgetGenerator.FLAVOUR);

                /////////////////////////////
                // Perform the drop action //
                /////////////////////////////
                // 1. Create a widget from the dropped WidgetGenerator object.
                // 2. Add the widget to the canvas.
                /////////////////////////////
                widget = ((WidgetGenerator)transferableObj).drop();
                addWidget(widget);
            }
        } catch (Exception ex) { /* nope, not the place */
            ex.printStackTrace(System.err);
        }
    }

    /**
     * Writes the BioPCD representation of the run window to a writer object
     * (see BioLegato's BioPCD editor for more details)
     **
     * @param scope the level of scope to write the run window.
     * @param out the writer object to output the BioPCD code.
     */
    public void pcdOut(int scope, Appendable out) throws IOException {
        out.append("name \"");
        out.append(name.replaceAll("\"", "\"\""));
        out.append("\"\n");
        if (! "".equals(icon)) {
            out.append("icon \"");
            out.append(icon.replaceAll("\"", "\"\""));
            out.append("\"\n");
        }
        out.append("tip \"");
        out.append(tip.replaceAll("\"", "\"\""));
        out.append("\"\n");
        if (systems.size() > 0) {
            out.append("system\n");
            for (SystemToken sys : systems) {
                out.append("    ");
                out.append(sys.toString());
                out.append("\n");
            }
        }
        if (! "".equals(exec)) {
            out.append("exec \"");
            out.append(exec.replaceAll("\"", "\"\""));
            out.append("\"\n");
        }

        for (Widget widget : widgetList) {
            widget.pcdOut(0, out);
            if (out instanceof Flushable) {
                ((Flushable)out).flush();
            }
        }
    }

    /**
     * Adds a widget to the PCD menu canvas.
     **
     * @param widget  the widget to add to the canvas.
     */
    public void addWidget(Widget widget) {
        mainPanel.add(widget.displayEdit(parent));
        widgetList.add(widget);
        mainPanel.doLayout();
        mainPanel.setSize(getPreferredSize());
        mainPanel.validate();
        mainPanel.repaint(50L);
    }

    /**
     * Clears the canvas of all widgets (thereby making it blank).
     */
    public void clear() {
        widgetList.clear();
        mainPanel.removeAll();
    }
}
