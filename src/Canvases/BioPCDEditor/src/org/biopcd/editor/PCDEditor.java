/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.biopcd.editor;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.Flushable;
import java.io.IOException;
import java.io.Reader;
import java.io.Serializable;
import java.util.Map;
import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTextField;
import javax.swing.filechooser.FileFilter;
import org.biolegato.main.BLMain;
import org.biolegato.main.DataCanvas;
import org.biopcd.parser.PCD;
import org.biopcd.parser.PCDObject;
import org.biopcd.parser.ParseException;
import org.biopcd.widgets.Widget;

/**
 * The BioPCDMenu editor
 **
 * Acknowledgement - Bryan E. Smith - bryanesmith@gmail.com
 * Thanks for providing sample Java drag&drop code that helped
 * guide me with writing the underlying framework
 **
 * @author Graham Alvare
 * @author Brian Fristensky
 */
public class PCDEditor extends DataCanvas implements Serializable {
    /**
     * A file pointer to the current working directory (this is used for picking
     * the default folder to open or save files).
     */
    protected File pwd = new File(System.getProperty("user.dir"));;
    /**
     * A self-reference to this PCD editor object (used by overloaded classes).
     */
    protected final PCDEditor self = this;
    /**
     * A file filter object designed to filter PCD files in an open or save
     * dialogue box.
     */
    public final FileFilter PCD_FILE_FILTER = new FileFilter() {
            /**
             * Determines whether a file is a PCD file or not.
             **
             * @param pathname  the path to determine whether it points to
             *                  a PCD file.
             * @return true if the path points to a PCD file or a directory.
             *        (false, if not.)
             */
            public boolean accept(File pathname) {
                String lowerpath = pathname.getAbsolutePath().toLowerCase();
                return (pathname.isDirectory() || lowerpath.endsWith(".pcd")
                        || lowerpath.endsWith(".blmenu")
                        || lowerpath.endsWith(".blitem")
                        || lowerpath.endsWith(".biopcd"));
            }

            /**
             * The human readable description for the file filter.
             **
             * @return a text description of the PCD file.
             */
            @Override
            public String getDescription() {
                return "BioPCD file (.blmenu,.pcd,.blitem,.biopcd)";
            }
        };

    /**
     * An empty string array to use as the command line parameters for creating
     * new windows/instances of the PCD editor.
     */
    public static final String[] NULL_ARGS = new String[0];
    /**
     * The menu canvas for viewing 
     */
    private MenuCanvas menuCanvas;
    /**
     * The menu widget panel containing the PCD editor's widget generators for
     * adding new widget to the menu canvas
     */
    private MenuWidgetsPanel widgetPanel;

    /**
     * Creates a new instance of the BioPCD editor canvas
     */
    public PCDEditor() {
        super();
    }

    /**
     * Creates a new instance of the BioPCD editor canvas
     **
     * @param importProperties  the properties to use for the PCD editor canvas.
     */
    public PCDEditor(Map<? extends Object, ? extends Object> importProperties) {
        super(importProperties);

        // The parent window of the PCD editor canvas.
        final JFrame parent = getJFrame();

        // Get the File and Edit menus (to add PCD editor canvas specific
        // menu items -- such as New, Open, and Save).
        JMenu filemenu = addMenuHeading("File");
        JMenu editmenu = addMenuHeading("Edit");

        // Create a new menu canvas and menu widgets panel.
        menuCanvas = new MenuCanvas(parent);
        widgetPanel = new MenuWidgetsPanel(parent);

        // Add a "New" button to the File menu.
        filemenu.add(new JMenuItem(new AbstractAction("New") {
            /**
             * Create a new PCD menu editor window.
             **
             * @param event  not used by this function (can even be null).
             */
            public void actionPerformed(ActionEvent event) {
                try {
                    main(NULL_ARGS);
                } catch (Exception ex) {
                    ex.printStackTrace(System.err);
                }
            }
        }));

        // Add an "Open..." button to the File menu.
        filemenu.add(new JMenuItem(new AbstractAction("Open...") {
            /**
             * Open a PCD menu into the menu canvas.
             **
             * @param event  not used by this function (can even be null).
             */
            public void actionPerformed(ActionEvent event) {
                File[] openFiles;
                JFileChooser openDialog = new JFileChooser();

                // Configure the open file dialog box.
                openDialog.setCurrentDirectory(pwd);
                openDialog.setAcceptAllFileFilterUsed(false);
                openDialog.setMultiSelectionEnabled(true);
                openDialog.addChoosableFileFilter(PCD_FILE_FILTER);

                // If a file is selected, open it
                if (openDialog.showOpenDialog(self)
                        == JFileChooser.APPROVE_OPTION) {
                    if (openDialog.getSelectedFiles() != null) {
                        openFiles = openDialog.getSelectedFiles();
                        for (File ofile : openFiles) {
                            // Make sure that the current file object in
                            // the selected files array exists and is a file.
                            if (ofile.exists() && ofile.isFile()) {
                                try {
                                    // Make sure that the file is not empty.
                                    if (ofile.length() > 0) {
                                        // Read the file into the canvas.
                                        FileReader rdr = new FileReader(ofile);
                                        readFile("pcd", rdr, true,false);
                                        rdr.close();
                                    }
                                } catch (Throwable e) {
                                    e.printStackTrace(System.err);
                                }
                            }
                        }
                    }
                    // Update the current directory to default save and open
                    // dialog boxes to.
                    if (openDialog.getCurrentDirectory() != null) {
                        pwd = openDialog.getCurrentDirectory();
                    }
                }
            }
        }));

        // Add an "Save..." button to the File menu.
        filemenu.add(new JMenuItem(new AbstractAction("Save...") {
            /**
             * Save the menu canvas as a PCD menu file.
             **
             * @param event  not used by this function (can even be null).
             */
            public void actionPerformed(ActionEvent event) {
                JFileChooser saveDialog = new JFileChooser();

                // Configure the save file dialog box.
                saveDialog.setCurrentDirectory(pwd);
                saveDialog.setAcceptAllFileFilterUsed(false);
                saveDialog.addChoosableFileFilter(PCD_FILE_FILTER);

                // if a file is selected, save to it
                if (saveDialog.showSaveDialog(self)
                        == JFileChooser.APPROVE_OPTION
                        && saveDialog.getSelectedFile() != null
                        && (!saveDialog.getSelectedFile().exists()
                        || javax.swing.JOptionPane.showConfirmDialog(null,
                            "Overwrite existing file?", "Overwrite",
                            javax.swing.JOptionPane.OK_CANCEL_OPTION,
                        javax.swing.JOptionPane.QUESTION_MESSAGE)
                            != javax.swing.JOptionPane.CANCEL_OPTION)) {
                    // write the file
                    try {
                        // the file writer object to write the file with.
                        FileWriter writer
                                = new FileWriter(saveDialog.getSelectedFile());

                        // write the PCD code to the file
                        writeFile("pcd", writer, true);

                        // flush and close the file writer buffer.
                        writer.flush();
                        writer.close();
                    } catch (Throwable exception) {
                        exception.printStackTrace(System.err);
                    }
                    
                    // Update the current directory to default save and open
                    // dialog boxes to.
                    if (saveDialog.getCurrentDirectory() != null) {
                        pwd = saveDialog.getCurrentDirectory();
                    }
                }
            }
        }));

        // Add an "Edit menu details..." button to the Edit menu.
        editmenu.add(new JMenuItem(new AbstractAction("Edit menu details...") {
            /**
             * Alter the menu file details, such as the menu name, and what
             * systems/architectures the menu command supports.
             **
             * @param event  not used by this function (can even be null).
             */
            public void actionPerformed(ActionEvent e) {
                // Create a new window to edit the details of the menu in the
                // canvas.
                final JDialog detailsW = new JDialog(parent, "Menu details");

                // Configure the panels for the menu details dialog box.
                JPanel hPanel  = new JPanel();
                JPanel namePnl = new JPanel();
                JPanel iconPnl = new JPanel();
                JPanel tipPnl  = new JPanel();
                JPanel execPnl = new JPanel();
                JPanel endPnl  = new JPanel();

                // Create text boxes for the user to edit the menu.
                final JTextField nameTB = new JTextField(menuCanvas.name, 20);
                final JTextField iconTB = new JTextField(menuCanvas.icon, 20);
                final JTextField tipTB  = new JTextField(menuCanvas.tip,  20);
                final JTextField execTB = new JTextField(menuCanvas.exec, 20);

                // Create a button to browse for a file to use for the
                // menu's icon.
                JButton iconBrowse = new JButton(new AbstractAction("Browse") {
                    /**
                     * Browse for an icon for the menu item.
                     **
                     * @param event  not used by this function
                     *               (can even be null).
                     */
                    public void actionPerformed(ActionEvent e) {
                        JFileChooser oDlg = new JFileChooser();
                        oDlg.setCurrentDirectory(PCD.getCurrentPWD());
                        oDlg.setAcceptAllFileFilterUsed(true);

                        oDlg.setFileSelectionMode(JFileChooser.FILES_ONLY);

                        // If a file is selected, display it.
                        if (oDlg.showOpenDialog(detailsW)
                                == JFileChooser.APPROVE_OPTION) {
                            // Update the icon for the menu.
                            iconTB.setText(oDlg.getSelectedFile().getPath());

                            // Update the current directory to default save
                            // and open dialog boxes to.
                            if (oDlg.getCurrentDirectory() != null) {
                                PCD.setCurrentPWD(oDlg.getCurrentDirectory());
                            }
                        }
                    }
                });

                // Create a button for saving the menu details.
                JButton saveButton = new JButton(new AbstractAction("OK") {
                    /**
                     * Update the details of the menu.
                     **
                     * @param event  not used by this function
                     *               (can even be null).
                     */
                    public void actionPerformed(ActionEvent e) {
                        // Update the details of the menu.
                        menuCanvas.name = nameTB.getText();
                        menuCanvas.icon = iconTB.getText();
                        menuCanvas.tip  = tipTB.getText();
                        menuCanvas.exec = execTB.getText();

                        // Close the menu details window.
                        detailsW.setVisible(false);
                        detailsW.dispose();
                    }
                });

                // Create a button for cancelling changes to the menu details.
                JButton closeButton = new JButton(new AbstractAction("Cancel") {
                    /**
                     * Do not save or update the details of the current menu.
                     **
                     * @param event  not used by this function
                     *               (can even be null).
                     */
                    public void actionPerformed(ActionEvent e) {
                        // Close the menu details window.
                        detailsW.setVisible(false);
                        detailsW.dispose();
                    }
                });

                // Configure the layout of the panels in the menu details
                // window.
                hPanel.setLayout(new BoxLayout(hPanel, BoxLayout.PAGE_AXIS));
                namePnl.setLayout(new BoxLayout(namePnl, BoxLayout.LINE_AXIS));
                iconPnl.setLayout(new BoxLayout(iconPnl, BoxLayout.LINE_AXIS));
                tipPnl.setLayout(new BoxLayout(tipPnl, BoxLayout.LINE_AXIS));
                execPnl.setLayout(new BoxLayout(execPnl, BoxLayout.LINE_AXIS));
                endPnl.setLayout(new BoxLayout(endPnl, BoxLayout.LINE_AXIS));

                // Add the Components to the menu details sub-panels:
                namePnl.add(new JLabel("Menu name:"));
                namePnl.add(nameTB);

                iconPnl.add(new JLabel("Icon path:"));
                iconPnl.add(iconTB);
                iconPnl.add(iconBrowse);

                tipPnl.add(new JLabel("Tooltip:"));
                tipPnl.add(tipTB);

                // Configure the 'exec' parameter panel.
                execPnl.setBorder(BorderFactory.createTitledBorder(
                        BorderFactory.createLineBorder(Color.black),
                        "Advanced"));
                execPnl.add(new JLabel("Exec field (left blank by default):"));
                execPnl.add(execTB);

                // Configure the buttons panel (OK/Cancel).
                endPnl.add(saveButton);
                endPnl.add(closeButton);

                // Configure the main panel of the menu details window.
                hPanel.add(namePnl);
                hPanel.add(iconPnl);
                hPanel.add(tipPnl);
                hPanel.add(execPnl);
                hPanel.add(endPnl);

                // Make the menu details window visible.
                detailsW.add(hPanel);
                detailsW.pack();
                detailsW.setMinimumSize(getSize());
                detailsW.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
                detailsW.setVisible(true);
            }
        }));
    }

    /**
     * Display the PCD editor canvas.
     **
     * @return the Component object used to display the PCD editor canvas.
     */
    public Component display() {
        JPanel topPanel = new JPanel();
        JPanel mainPanel = new JPanel();
        topPanel.setLayout(new BoxLayout(topPanel, BoxLayout.PAGE_AXIS));
        topPanel.add(widgetPanel);
        topPanel.add(new JSeparator());
        mainPanel.setLayout(new BorderLayout());
        mainPanel.add(topPanel, BorderLayout.PAGE_START);
        mainPanel.add(new JScrollPane(menuCanvas,
                JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
                JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS), BorderLayout.CENTER);

        setSize(Math.max(widgetPanel.getPreferredSize().width, 400), 400);
        setMinimumSize(getSize());
        return mainPanel;
    }

    /**
     * Starts BioLegato PCD editor canvas
     **
     * @param args the command line arguments for BioLegato
     */
    public static void main (String[] args) {
        BLMain.main(PCDEditor.class, args);
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
        return "pcdedit";
    }

    /**
     * Reads PCD data into the canvas.
     **
     * @param format     the file format to use for parsing the file.
     * @param in         the "stream" to read in from.
     * @param overwrite  whether to overwrite the currently selected sequences
     *                   with the data imported.
     * @throws IOException if an error occurs while reading
     */
    public void readFile(String format, Reader in, boolean overwrite, boolean forceall)
                                                            throws IOException {
        try {
            // If the overwrite parameter is set to true, clear the menu canvas
            // before reading in the PCD file.
            if (overwrite) {
                menuCanvas.clear();
            }
            
            PCDObject o = PCD.loadPCDStream(in, null, this);

            menuCanvas.name = o.name;
            menuCanvas.icon = o.icon;
            menuCanvas.tip = o.tooltip;
            menuCanvas.exec = o.exec;
            menuCanvas.systems = o.systems;

            for (Widget w : o.widgetList.values()) {
                menuCanvas.addWidget(w);
            }
        } catch(ParseException pe) {
            pe.printStackTrace(System.err);
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
        // Write the PCD code for the menu canvas.
        menuCanvas.pcdOut(0, out);

        // flush and close the file writer buffer.
        if (out instanceof Flushable) {
            ((Flushable)out).flush();
        }
    }
}
