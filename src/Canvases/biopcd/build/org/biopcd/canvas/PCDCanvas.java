/*
 * RunWindow.java
 *
 * Created on January 5, 2010, 2:06 PM
 *
 * To change this template, choose Tools | Template Manager
 * and display the template in the editor.
 */
package org.biopcd.canvas;

import java.io.IOException;
import java.io.Reader;
import java.awt.Component;
import java.awt.Container;
import java.io.BufferedReader;
import java.io.File;
import java.io.Flushable;
import java.util.Collections;
import java.util.Map;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import org.biopcd.parser.ParseException;
import org.biopcd.widgets.Widget;
import org.biopcd.widgets.CloseableWindow;
import org.biolegato.main.BLMain;
import org.biolegato.main.DataCanvas;
import org.biopcd.parser.PCD;
import org.biopcd.parser.PCDObject;
import org.biopcd.widgets.WidgetInstance;

/**
 * The run window is used within the menu system to display options for running
 * programs.
 **
 * @author Graham Alvare
 * @author Brian Fristensky
 */
public class PCDCanvas extends DataCanvas implements CloseableWindow {
    /**
     * The name of the window
     */
    private String name = null;
    /**
     * List of widgets for the window.
     */
    protected Map<String, Widget> widgets = Collections.EMPTY_MAP;

    /**
     * Creates a new instance of BioLegato's BioPCD canvas
     */
    public PCDCanvas() {
        this(null);
    }

    /**
     * Creates a new instance of BioLegato's BioPCD canvas
     **
     * @param importProperties  a list of properties attributes to import into
     *                          the canvas
     */
    public PCDCanvas(Map<? extends Object, ? extends Object> importProperties) {
        super(importProperties);
    }

    /**
     * Creates a new instance of BioLegato's BioPCD canvas
     **
     * @param name     the name to display in the window's title bar
     * @param widgets  the list of variable widgets to associate with the window
     */
    public PCDCanvas(String name, Map<String, Widget> widgets) {
        this.name = name;
        this.widgets = widgets;
    }

    /**
     * Displays the command parameter window.
     **
     * @return the swing or awt component to display as the canvas.
     */
    @Override
    public Component display() {
        // the portion of the window containing the visible variable widgets.
        Container variablePane = new Box(BoxLayout.PAGE_AXIS);

        // initialize and display all of the variable widgets
        for (Widget w : widgets.values()) {
            System.out.println("widget " + w);
            w.display(variablePane, this);
        }

        // create the pane and make it scrollable
        return new JScrollPane(variablePane);
    }

    /**
     * Action for command buttons to close the window.
     * This is used to close parameter windows when a command is being executed.
     */
    public void close() {
        JFrame runWindow = this.getJFrame();

        // If there is a RunWindow available, close it.
        if (runWindow != null) {
            runWindow.setVisible(false);
            runWindow.dispose();
        }
    }

    /**
     * Possible vestigial code!
     * This code is used to provide a plugin name for the canvas.  The purpose
     * of the plugin name system was to allow multiple canvases to be loaded
     * (via. BioLegato's plugin system), and then have the properties select
     * which canvas to use based on the plugin names.  This architecture has
     * since been replaced, and so this code may no longer be used.
     **
     * @return     the name to display for the canvas in
     *             all program text referring to it.
     * @deprecated vestigial code from a previous BioLegato framework structure.
     */
    @Override
    public String getPluginName() {
        return "PCD canvas";
    }

    /**
     * Reads data into the canvas
     **
     * @param format     the file format to use for parsing the file.
     * @param in         the "stream" to read in from.
     * @param overwrite  whether to overwrite the currently selected sequences
     *                   with the data imported.
     * @throws IOException if an error occurs while reading
     */
    public void readFile(String format, Reader in, boolean overwrite, boolean forceall)
                                                            throws IOException {
        if ("pcd".equalsIgnoreCase(format)
                || "biopcd".equalsIgnoreCase(format)) {
            // Read in a BioPCD file.
            try {
                // Parse the stream into a PCD object.
                PCDObject obj = PCD.loadPCDStream (in, new File(BLMain.CURRENT_DIR), this);

                // Set the name of the canvas to the name of the PCD object.
                setName(obj.name);

                // Copy the widget list for the PCD object into the canvas.
                this.widgets = obj.widgetList;

                // If the exec parameter is set, print a warning.
                if (obj.exec != null && !"".equals(obj.exec)) {
                    System.err.println(
                        "Found unsupported BioPCD attribute 'exec'!\n"
                        + "    While the attribute 'exec' is supported in"
                        + "BioLegato's\n   PCD menus, it is not supported "
                        + "by BioLegato's PCD canvas");
                }

                // If the icon parameter is set, print a warning.
                if (obj.icon != null && !"".equals(obj.icon)) {
                    System.err.println(
                        "Found unsupported BioPCD attribute 'icon'!\n"
                        + "    While the attribute 'icon' is supported in"
                        + "BioLegato's\n   PCD menus, it is not supported "
                        + "by BioLegato's PCD canvas");
                }

                // If the tooltip ('tip') parameter is set, print a warning.
                if (obj.tooltip != null && !"".equals(obj.tooltip)) {
                    System.err.println(
                        "Found unsupported BioPCD attribute 'tip'!\n"
                        + "    While the attribute 'tip' is supported in"
                        + "BioLegato's\n   PCD menus, it is not supported "
                        + "by BioLegato's PCD canvas");
                }

                // If the systems parameter is set, print a warning.
                if (obj.systems != null && !obj.systems.isEmpty()) {
                    System.err.println(
                        "Found unsupported BioPCD attribute 'system'!\n"
                        + "    While the attribute 'system' is supported in"
                        + "BioLegato's\n   PCD menus, it is not supported "
                        + "by BioLegato's PCD canvas");
                }
            } catch (ParseException ex) {
                ex.printStackTrace(System.err);
            }
        } else if ("values".equalsIgnoreCase(format)) {
            // Parse a PCD values file.
            ValuesFile.readValues(in, widgets);
        } else {
            // Ensure that the stream supports marking (if not, create a
            // BufferedReader object, which does support marking, to wrap
            // the 'in' stream.
            if (!in.markSupported()) {
                in = new BufferedReader(in);
            }

            // Print an error message (if the format is blank),
            // since the format is not recognized.
            if (!"".equals(format)) {
                System.out.println("Unknown format! - " + format);
            }

            // Double-check that the buffered reader object on the current OS
            // supports marking should always be true; however, if it is not,
            // we let the program fail gracefully.
            if (in.markSupported()) {
                char[] test = new char[1000];

                // Read in 1000 characters for autodetection.  Also, mark the
                // stream before and reset the stream, so subsequent read
                // operations will read from the beginning of the stream
                // (thus, making the autodetection process invisible/seamless
                // to other methods and parts of the code).
                System.out.println("Attempting to autodetect file format");
                in.mark(1000);
                in.read(test);
                in.reset();

                // TODO: improve autodetection algorithm!
                if (new String(test).contains("    ")) {
                    System.out.println("Reading BioPCD file");
                    readFile("biopcd", in, overwrite,forceall);
                } else {
                    System.out.println("Reading values file");
                    readFile("values", in, overwrite,forceall);
                }
            } else {
                // Inform the user if autodetection is not possible because
                // of problems using the strem mark feature.
                System.err.println("Autodetection not supported by Java!");
            }
        }
    }

    /**
     * Writes data out from the canvas
     **
     * @param format    the file format to use for writing the file.
     * @param out       the "stream" to write out to.
     * @param forceall  use the entire canvas instead of just its selected
     *                  sequences.
     * @throws IOException if an error occurs while writing
     */
    public void writeFile(String format, Appendable out, boolean forceall)
                                                            throws IOException {
        WidgetInstance instance = null;

        if ("pcd".equalsIgnoreCase(format)
                || "biopcd".equalsIgnoreCase(format)) {
            // Write the PCD code for the canvas contents.
            // Write the name of the canvas, first.
            out.append("name \"");
            out.append(name.replaceAll("\"", "\"\""));
            out.append("\"\n");

            // Write the PCD code for each widget in the canvas.
            for (Widget widget : widgets.values()) {
                widget.pcdOut(0, out);
                if (out instanceof Flushable) {
                    ((Flushable)out).flush();
                }
            }
        } else if ("values".equalsIgnoreCase(format) 
                || "tsv".equalsIgnoreCase(format)) {
            // Handles which delimiter is used to separate keys from values.
            // In this case, the variable includes the quotation marks after
            // the key and before the value.
            String delimiter = "\"=\"";
            
            // Change the delimiter equals sign to the tab character (for TSV).
            if ("tsv".equalsIgnoreCase(format)) {
                delimiter = "\"\t\"";
            }

            // Iterate through every widget and write the values.
            for (Map.Entry<String,Widget> entry : widgets.entrySet()) {
                // Get a widget instance for the widget object.
                instance = entry.getValue().getInstance();

                // Write the key (enclosed in double-quotation marks), and
                // write the equals sign (before the value).
                out.append("\"");
                out.append(entry.getKey().replaceAll("\"", "\"\""));
                out.append(delimiter);

                // If there a widget instance was successfully generated, then
                // append the value to the PCD values file.
                if (instance != null) {
                    // Replace every instance of the character " with "".
                    out.append(instance.getValue().toString(
                            ).replaceAll("\"", "\"\""));
                }

                // Terminate the line of the PCD values file.
                out.append("\"\n");

                // Flush the output stream, if applicable.
                // This forces that the stream cache (if the stream represents
                // a file) to be written to disk.
                if (out instanceof Flushable) {
                    ((Flushable)out).flush();
                }
            }
        } else {
            System.out.println("Unknown format: " + format);
        }
    }

    /**
     * Starts BioLegato PCD canvas
     **
     * @param args the command line arguments for BioLegato
     */
    public static void main (String[] args) {
        BLMain.main(PCDCanvas.class, args);
    }

}
