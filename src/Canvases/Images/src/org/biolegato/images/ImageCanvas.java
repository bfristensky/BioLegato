package org.biolegato.images;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.io.BufferedReader;
import java.util.Map;
import javax.imageio.ImageIO;
import javax.swing.AbstractAction;
import javax.swing.ImageIcon;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.filechooser.FileFilter;
import org.biolegato.main.BLMain;
import org.biolegato.main.DataCanvas;
import org.biopcd.parser.PCD;
/*
 * TableCanvas.java
 *
 * Created on November 15, 2010, 4:08 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

/**
 * BioLegato's image canvas
 **
 * @author Graham Alvare
 * @author Brian Fristensky
 */
public class ImageCanvas extends DataCanvas {
    /**
     * The display pane to show the image on
     */
    JLabel display = new JLabel();
    /**
     * Stores the current image
     */
    String imageFile = null;
    /**
     * Stores the current dataset represented by the canvas
     */
    String data = null;
    /**
     * Stores the image data to display in the image canvas.
     */
    BufferedImage img = null;
    /**
     * The zoom level of the canvas (as a percent of normal zoom).
     * By default, this value is 100 -- i.e. neither zoomed in or out.
     */
    private float zoom = 100.f;
    /**
     * Stores the minimum zoom level allowed within the image canvas (based on
     * BioLegato's properties.
     */
    final float MIN_ZOOM = 10.f;
    /**
     * Stores the maximum zoom level allowed within the image canvas (based on
     * BioLegato's properties.
     */
    final float MAX_ZOOM = 1000.f;
    /**
     * Stores the minimum zoom level allowed within the image canvas (based on
     * BioLegato's properties.
     */
    final float ZOOM_STEP = 10.f;
    /**
     * The file filter object for reading in CSV files
     */
    private static final FileFilter IMAGE_FILTER = new FileFilter() {
        /**
         * Detect whether a file is in BioLegato image format (or a directory).
         * The file format is tested based on the file's extension.
         **
         * @param  f the file object to test the format of.
         * @return true, if the file is a BioLegato image file, otherwise false.
         */
        @Override
        public boolean accept(File f) {
            return f.isDirectory() || (f.exists() && f.canRead()
                    && f.getName().toLowerCase().endsWith(".blimg"));
        }

        /**
         * Provide a description for BioLegato image files.
         **
         * @return a string description for BioLegato image files.
         */
        @Override
        public String getDescription() {
            return "BioLegato Image Files (*.blimg)";
        }
    };

    /**
     * Creates a new instance of ImageCanvas
     */
    public ImageCanvas() {
        super();
    }

    /**
     * Creates a new instance of ImageCanvas
     **
     * @param importProps  the properties object to import to the new canvas.
     */
    public ImageCanvas(Map<? extends Object, ? extends Object> importProps) {
        super(importProps);
    }

    /**
     * Starts BioLegato image canvas
     **
     * @param args the command line arguments for BioLegato
     */
    public static void main (String[] args) {
        BLMain.main(ImageCanvas.class, args);
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
        return "Image";
    }

    /**
     * <p>Reads a file into the canvas.
     * Currently, the ImageCanvas only support the BioLegato image format.  This
     * format is specified as follows.  BioLegato image files contain only two
     * lines:</p>
     *
     * <ol>
     *  <li><p>The image line.  This line specifies the image filename to
     *      display in the canvas.  The line is comprised of the word image,
     *      followed by some whitespace, and the image file's filename in
     *      double-quotation marks (all double-quotation marks in the filename
     *      are doubled).</p>
     *      <dl>
     *          <dt>Example 1:</dt>
     *          <dd>
     *              for the file:             <code>abcdefg.jpg</code><br />
     *              the image line would be:  <code>image   "abcdefg.jpg"</code>
     *          </dd>
     *          <dt>Example 2:</dt>
     *          <dd>
     *              for the file:             <code>q"abc.png</code><br />
     *              the image line would be:  <code>image   "q""abc.png"</code>
     *          </dd>
     *      </dl>
     *  </li>
     *
     *  <li><p>The data line.  This line specifies the data for the canvas to
     *      pass to any programs it runs.  This line is formatted similar to the
     *      image line except it is prefaced by 'data' instead of 'image', and
     *      the data (in the double-quotes) can be multi-lined.</p>
     *      <dl>
     *          <dt>Example 1:</dt>
     *          <dd>
     *              <p>The file:</p>
     *              <pre>
     *     image   "abcdefg.jpg"
     *     data    "xyz
     *     123""
     *     456"</pre>
     *              <p>Would use the image file: abcdefg.jpg
     *              And pass the following output to any programs run by
     *              the BioLegato canvas:</p>
     *     <pre>
     *     xyz
     *     123"
     *     456</pre>
     *          </dd>
     *      </dl>
     *  </li>
     * </ol>
     **
     * @param format the   file format to use for parsing the file.
     * @param currentFile  the file to read in.
     * @param overwrite    whether to overwrite the currently selected sequences
     *                     with the data imported.
     */
    public void readFile(String format, Reader currentFile, boolean overwrite, boolean forceall)
                                                            throws IOException {
        // The current line to parse in the file.
        String line = "";
        // The BufferedReader object used to read the file line-by-line.
        BufferedReader bread = new BufferedReader(currentFile);

        // Iterate through every line in the file.
        while ((line = bread.readLine()) != null) {
            // Read the image line.
            if (line.toLowerCase().startsWith("image")) {
                // Get the filename from the image line (i.e. remove the word
                // 'image' from the line, and process).
                imageFile = line.substring(5).trim();

                // Remove the double-quotation marks on either end of the
                // filename.  Note, although the specification demands double-
                // quotation marks, this program will handle errors gracefully,
                // and parse filenames which are not surrounded by the proper
                // double-quotation marsk.
                if (imageFile.startsWith("\"") && imageFile.endsWith("\"")
                        && imageFile.length() > 1) {
                    imageFile = imageFile.substring(1,
                            imageFile.length() - 1).replace("\"\"", "\"");
                }

                // Read the image file.
                img = ImageIO.read(new File(BLMain.envreplace(imageFile)));

                // Make the image canvas display the new image (at normal zoom).
                setZoom(zoom);
            }

            // Read the data line.
            if (line.toLowerCase().startsWith("data")) {
                // Get the data from the data line (i.e. remove the word
                // 'data' from the line, and process).
                data = line.substring(4).trim();
                
                // Remove the double-quotation marks on either end of the
                // filename.  Note, if double-quotation marks are not detected,
                // the program will handle this error by skipping the data line.
                if (data.startsWith("\"") && data.endsWith("\"")
                        && data.length() > 1) {
                    data = data.substring(1,
                            data.length() - 1).replace("\"\"", "\"");
                }
            }
        }
    }

    /**
     * Writes a file out from the canvas
     **
     * @param format   the file format to use for writing the file.
     * @param outfile  the file to write out.
     * @param forceall use the entire canvas instead of just its selected
     *                 sequences.
     */
    public void writeFile(String format, Appendable outfile, boolean forceall)
                                                            throws IOException {
        if (format.equals("blimage")) {
            outfile.append("image   ").append(imageFile)
                    .append("\n").append("data\n    \"").append(
                    data.replaceAll("\"", "\"\"")).append("\"").append("\n");
        } else {
            outfile.append(data);
        }
    }

    /**
     * Displays the main pane of the image canvas
     **
     * @return the scroll pane containing the image canvas
     */
    public Component display() {
        JPanel mainImagePanel = new JPanel();

        //////////////////////////////////////
        //**********************************//
        //* ADD THE DEFAULT TOP MENU ITEMS *//
        //**********************************//
        //////////////////////////////////////

        // Add the "Open" button
        addMenuHeading("File").insert(new JMenuItem(
                new AbstractAction("Open...") {

            /**
             * Serialization number - required to compile without warnings.
             */
            private static final long serialVersionUID = 7526472295622776157L;
            /**
             * Initialize the mnemonic key for the menu item.
             */
            {
                putValue(MNEMONIC_KEY,
                        new Integer(java.awt.event.KeyEvent.VK_O));
            }
            /**
             * Event handler - open a file when called.
             **
             * @param evt ignored by this method.
             */
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                // A variable to store the files to open.
                File[] openFiles;
                // Create the file chooser to select the file to open.
                JFileChooser openDialog = new JFileChooser();

                // Configure the Open file dialog box.
                openDialog.setCurrentDirectory(PCD.getCurrentPWD());
                openDialog.setAcceptAllFileFilterUsed(false);
                openDialog.setMultiSelectionEnabled(true);
                openDialog.addChoosableFileFilter(IMAGE_FILTER);

                // if a file is selected, open it
                if (openDialog.showOpenDialog(getJFrame())
                        == JFileChooser.APPROVE_OPTION) {
                    if (openDialog.getSelectedFiles() != null) {
                        openFiles = openDialog.getSelectedFiles();
                        
                        // Handle multiple files being selected
                        for (File ofile : openFiles) {
                            if (ofile.exists() && ofile.isFile()) {
                                try {
                                    readFile("image",
                                            new FileReader(ofile), false,false);
                                } catch (IOException ioe) {
                                    ioe.printStackTrace(System.err);
                                }
                            }
                        }
                    }

                    // Update the PWD for BioLegato (i.e. so all other file
                    // open/save dialog boxes will start in the directory the
                    // user last saved or opened a file).
                    if (openDialog.getCurrentDirectory() != null) {
                        PCD.setCurrentPWD(openDialog.getCurrentDirectory());
                    }
                }
            }
        }), 0);

        // Set the layout manager for the image canvas.
        mainImagePanel.setLayout(new BorderLayout());

        // Add the image (enclosed in a scroll area) to the image canvas.
        mainImagePanel.add(new JScrollPane(display), BorderLayout.CENTER);

        // If zoom is enabled, add the zoom controls to the canvas.
        if ("true".equalsIgnoreCase(getProperty("zoom"))) {
            // Create a new spinner to set the zoom level with.
            final JSpinner zoomSpinner
                    = new JSpinner(new SpinnerNumberModel(100,
                            MIN_ZOOM, MAX_ZOOM, ZOOM_STEP));
            // Create a new panel to add the zoom controls to the image canvas.
            JPanel zoomPanel = new JPanel();

            // Add the zoom controls to the zoom panel.
            zoomPanel.add(new JLabel("ZOOM: "));
            zoomPanel.add(zoomSpinner);
            zoomPanel.add(new JLabel("%"));

            // Add a change listener, which will update the canvas zoom when
            // the zoom property is changed.
            zoomSpinner.addChangeListener(new ChangeListener() {
                public void stateChanged(ChangeEvent e) {
                    setZoom(((Number) zoomSpinner.getValue()).floatValue());
                }
            });

            // Add the zoom control panel to the image canvas.
            mainImagePanel.add(zoomPanel, BorderLayout.SOUTH);
        }

        // Return the image canvas's display Component/Container object.
        return mainImagePanel;
    }

    /**
     * Change the zoom level of the current image displayed in the image canvas.
     * This method also updates the current image displayed in the image canvas,
     * if the object variable 'img' has changed (including if img becomes null).
     **
     * @param zoom the level to zoom the current image (as a percentage).
     *             (For example, the value 100 corresponds to 100%, NOT 100x.)
     */
    public void setZoom(float newzoom) {
        try {
            // Make sure the image object is not null before trying to zoom it.
            if (img != null) {
                // Ensure that the zoom does not exceed the maximum or minimum
                // zoom levels allowed within the image canvas.
                if (newzoom > MIN_ZOOM && newzoom < MAX_ZOOM) {
                    // Obtain the original width and height values.
                    final float ow = img.getWidth();
                    final float oh = img.getHeight();

                    // Ensure that the zoomed image will be a greater dimension
                    // than one pixel wide, or one pixel high.
                    if (ow > 1 && oh > 1) {
                        // Calculate the new size of the zoomed image.
                        final int zw = Math.round((ow * newzoom) / 100.f);
                        final int zh = Math.round((oh * newzoom) / 100.f);

                        // Resize the image based on the zoom level.
                        BufferedImage zimg = new BufferedImage(zw, zh,
                                BufferedImage.TYPE_3BYTE_BGR);
                        Graphics2D gfx = zimg.createGraphics();
                        gfx.drawImage(img, 0, 0, zw, zh, null);
                        gfx.dispose();

                        // Make the image canvas display the zoomed image.
                        display.setIcon(new ImageIcon(zimg));
                        
                        // Copy the zoom parameter to the object, only if the
                        // zoom was successful.
                        this.zoom = newzoom;
                    }
                }
            } else {
                // Copy the zoom parameter to the object, only if the zoom was
                // successful.  However, zooming a null image is ALWAYS
                // successful.
                display.setIcon(null);
            }

        } catch (Throwable th) {
            // Print any errors which occur while zooming (but only if debug
            // is enabled).
            th.printStackTrace(System.err);
        }
    }
}
