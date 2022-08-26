package org.biolegato.tables;

import java.awt.Component;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Map;
import javax.swing.AbstractAction;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenuItem;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JTextField;
import javax.swing.JPanel;
import java.awt.GridLayout;
import javax.swing.JCheckBox;
import java.awt.FlowLayout;
import javax.swing.filechooser.FileFilter;
import javax.swing.table.DefaultTableModel;
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
 * BioLegato's table canvas.
 **
 * @author Graham Alvare
 * @author Brian Fristensky
 */
public class TableCanvas extends DataCanvas {
    /**
     * The selection modes available for the table canvas.  There are currently
     * three modes available to the user.  The purpose of selection modes is to
     * allow the user to click on individual rows or columns, easily, in the
     * table canvas.  To see this in action, please run the table canvas.
     * (The selection modes are easier to explain visually than through text.)
     */
    public static enum SelectionMode {
        /**
         * In this mode, the user selects individual cells in the canvas.
         */
        CELL,
        /**
         * In this mode, the user selects individual columns in the canvas.
         */
        COLUMN,
        /**
         * In this mode, the user selects individual rows in the canvas.
         */
        ROW;
    };
    /**
     * Stores whether or not the data within the canvas is editable
     * (this is read from BioLegato's properties).
     */
    final boolean editable = !"true".equalsIgnoreCase(getProperty("readonly"));
    /**
     * Stores whether or not the data within the canvas has generic column
     * headers (i.e. A, B, C, ...) or named headers (the first row of any data
     * imported into the table canvas).  True means named headers are used
     * instead of generic column headers.
     */
    final boolean namedHeaders
            = !"true".equalsIgnoreCase(getProperty("table.generic_headers"));
    /**
     * The table model to store the canvas content.
     */
    DefaultTableModel tableModel = new DefaultTableModel() {
        /**
         * Tests whether a cell within the table model is editable.  This method
         * is customized to first test whether BioLegato's readonly parameter is
         * enabled.  If the readonly parameter is set in BioLegato's properties,
         * then none of the cells in the table are editable.
         **
         * @param row the row index of the cell to test.
         * @param col the column index of the cell to test.
         * @return whether the cell is editable (true = yes, false = no).
         */
        @Override
        public boolean isCellEditable(int row, int col) {
            return editable && super.isCellEditable(row, col);
        }
    };
    /**
     * The actual JTable object for displaying the table canvas data.
     */
    final JTable tablePane = new JTable(tableModel);
    /**
     * Stores the current row header model for the table.  This is used to
     * display row numbers (and row headers in the future).
     */
    private TableRowModel rowHeaderModel = new TableRowModel(tableModel);
    /**
     * The file filter object for reading in CSV files.
     */
    private static final FileFilter CSV_FILTER = new CSVFile();
    /**
     * The file filter object for reading in TSV files.
     */
    private static final FileFilter TSV_FILTER = TSVFile.TSV_FILTER;
    /**
     * An empty two-dimensional string array.  This is used as the default
     * return value for the function getData.
     */
    private static final String[][] EMPTY_SET = new String[0][0];
    /**
     * The menu item "Select All"
     */
    public final AbstractAction selectAllA = new AbstractAction("Select All") {
        /**
         * Serialization number - required for no warnings
         */
        private static final long serialVersionUID = 7526472295622777033L;


        /**
         * Sets the mnemonic for the event.
         */
        {
            putValue(MNEMONIC_KEY, new Integer(java.awt.event.KeyEvent.VK_A));
        }

        /**
         * Event handler - select all of the cells in the canvas.
         **
         * @param evt ignored by this method.
         */
        public void actionPerformed(java.awt.event.ActionEvent evt) {
            tablePane.selectAll();
        }
    };
    SelectionMode mode = SelectionMode.CELL;

    
    
    /**
     * Creates a new instance of a TableCanvas.  This is the most basic
     * constructor available.
     */
    public TableCanvas() {
        super();
        
        // Add the "Select All..." menu item.
        addMenuHeading("Edit").add(new JMenuItem(selectAllA));
    }

    /**
     * Creates a new instance of TableCanvas.  This constructor will accept
     * a Map of values for BioLegato properties.
     **
     * @param props the BioLegato properties to initialize the canvas with.
     *              This parameter is ignored if null.
     */
    public TableCanvas(Map<? extends Object, ? extends Object> props) {
        super(props);

        // Add the "Select All..." menu item.
        addMenuHeading("Edit").add(new JMenuItem(selectAllA));
    }

    /**
     * Creates a new instance of TableCanvas, reading results from an SQL query
     * into the table, by default.
     **
     * @param rs the results of an SQL query; these are to be used as the
     *           initial contents of the table canvas.
     */
    protected TableCanvas(ResultSet rs) {
        this(null, rs);
    }

    /**
     * Creates a new instance of TableCanvas, reading results from an SQL query
     * into the table, by default.  This constructor will also accept a Map of
     * values for BioLegato properties.
     **
     * @param props the BioLegato properties to initialize the canvas with.
     *              This parameter is ignored if null.
     * @param rs    the results of an SQL query; these are to be used as the
     *              initial contents of the table canvas.
     */
    public TableCanvas(Map<? extends Object, ? extends Object> props,
            ResultSet rs) {
        this(props);

        // Parse the SQL query.
        try {
            // Obtain the metadata from the SQL query result set.
            ResultSetMetaData meta = rs.getMetaData();
            // Determine the number of columns in the result set.
            int colmax = meta.getColumnCount();
            // Create a new array to cache each row while extracting each row
            // from the result set.
            String[] record = new String[colmax];

            // Add the column headers from the result set to the JTable.
            for (int count = 1; count <= colmax; count++) {
                tableModel.addColumn(meta.getColumnName(count));
            }

            // Add each row from the result set to the table.
            while (rs.next()) {
                for (int count = 0; count < colmax; count++) {
                  record[count] = rs.getString(count + 1);
                }
                tableModel.addRow(record);
            }
        } catch (SQLException sqle) {
            sqle.printStackTrace(System.err);
        }
    }

    /**
     * Creates a new instance of TableCanvas.  This constructor will accept
     * a Map of values for BioLegato properties, and a 2D string array
     * containing the initial data for the TableCanvas.  The first index
     * corresponds to the row number, and the second index corresponds to the
     * column number.  For example, the index dataImport[1][4] corresponds to
     * the 2nd row, 5th column.  The first row contains the column names/headers
     * for the table.
     **
     * @param props      the BioLegato properties to initialize the canvas with.
     *                   This parameter is ignored if null.
     * @param dataImport a 2D string array to initialize the canvas with.
     */
    public TableCanvas(Map<? extends Object, ? extends Object> propertiesadd,
            String[][] dataImport) {
        this(propertiesadd);

        // Ensure that the 2D array is not null and not empty.
        if (dataImport != null && dataImport.length > 0) {
            // Add the columns and headers to the table.
            for (int col = 0; col < dataImport[0].length; col++) {
                tableModel.addColumn(dataImport[0][col]);
            }

            // Add the data for each row in the 2D string array to the table.
            for (int row = 0; row < dataImport.length; row++) {
                tableModel.addRow(dataImport[row]);
            }
        }
    }

    /**
     * Starts a new instance of BioLegato, using the table canvas.
     **
     * @param args the command line arguments for BioLegato.
     */
    public static void main (String[] args) {
        BLMain.main(TableCanvas.class, args);
    }

    /**
     * Returns the current/selected data in the canvas as a 2D string array.
     * The first index corresponds to the row number, and the second index
     * corresponds to the column number.  For example, the index
     * dataImport[1][4] corresponds to the 2nd row, 5th column.  The first row
     * contains the column names/headers for the table.  This method is an alias
     * for: getData(false).
     **
     * @return the current data stored in the table canvas
     *         (as a 2D string array).
     */
    public String[][] getData() {
        return getData(false);
    }

    /**
     * Returns the current/selected data in the canvas as a 2D string array.
     * The first index corresponds to the row number, and the second index
     * corresponds to the column number.  For example, the index
     * dataImport[1][4] corresponds to the 2nd row, 5th column.  The first row
     * contains the column names/headers for the table.
     **
     * @param forceall use the entire canvas instead of just only exporting
     *                 whichever sequences are selected.
     * @return the current data stored in the table canvas
     *         (as a 2D string array).
     */
    public String[][] getData(boolean forceall) {
        // Determine the first row to start writing the data out.  If named
        // column headers are enabled, then the absolute first row of the output
        // 2D string array (row zero) is written from the column headers, and
        // the data begins at row one.  If, however, named column headers are
        // disabled, then the data is written starting at the first row of the
        // output 2D string array (row zero).
        int firstrow = (namedHeaders ? 1 : 0);
        // A list of rows selected (used to limit the output).  This will only
        // have values set, if the selection mode is either row or cell based.
        int[] rows = null;
        // A list of columns selected (used to limit the output).  This will
        // only have values set, if the selection mode is either column or cell
        // based.
        int[] columns = null;
        // The output 2D string array.
        String[][] result = EMPTY_SET;

        // Correct the value of the forceall variable based on whether anything
        // is actually selected.  If nothing is selected, then forceall will be
        // set to true, regardless of its actual value.
        forceall = forceall || (tablePane.getSelectedColumnCount() == 0
                && tablePane.getSelectedRowCount() == 0);

        // If forceall is not set, then determine the filtration needed for
        // outputting the selected text in the canvas only.
        if (!forceall) {
            // Handle row or cell based selections, by extracting the rows in
            // the table canvas which are selected.
            if (mode == SelectionMode.ROW || mode == SelectionMode.CELL) {
                rows = tablePane.getSelectedRows();
            }
            
            // Handle column or cell based selections, by extracting the columns
            // in the table canvas which are selected.
            if (mode == SelectionMode.COLUMN || mode == SelectionMode.CELL) {
                columns = tablePane.getSelectedColumns();
            }
        }

        // Branch based on whether rows are filtered.
        if (forceall || rows == null) {
            // If rows are NOT filtered, then we will write all of the rows,
            // when we get to the row extraction part of getData(boolean).
            // Start by initializing the output 2D string array.
            result = new String[firstrow + tablePane.getRowCount()][];

            // If we are using named column headers, then start by extracting
            // the column headers and writing it as the first row in the 2D
            // string array (the first row is row number zero).
            if (namedHeaders) {
                result[0] = getColumnHeaders(columns);
            }

            // Write each of the rows in the table canvas to the output 2D
            // string array.
            for (int y = 0; y < tablePane.getRowCount(); y++) {
                result[firstrow + y] = getRowData(columns, y);
            }
        } else {
            // If rows ARE filtered, then we will write only the rows that are
            // selected by the user, when we get to the row extraction part of
            // the getData(boolean) method.  Start by initializing the output
            // 2D string array.
            result = new String[firstrow + rows.length][];

            // If we are using named column headers, then start by extracting
            // the column headers and writing it as the first row in the 2D
            // string array (the first row is row number zero).
            if (namedHeaders) {
                result[0] = getColumnHeaders(columns);
            }

            // Write only the rows, which are currently selected in the table
            // canvas, to the output 2D string array.
            for (int y = 0; y < rows.length; y++) {
                result[firstrow + y] = getRowData(columns, rows[y]);
            }
        }
        // Return the output 2D string array object.
        return result;
    }

    /**
     * Retrieves the column headers (names) for a given set of columns.
     **
     * @param columns an array of column indices (export all columns if null).
     * @return a string array of column header (names).
     */
    String[] getColumnHeaders(int[] columns) {
        // The string array that will store the column headers.
        String[] result = null;

        // If the columns variable is null, export all columns, otherwise only
        // export the columns selected by the array 'columns'.
        if (columns == null) {
            // Ensure that all column names are exported.  Start by creating a
            // string array large enough to hold all of the column headers.
            result = new String[tableModel.getColumnCount()];

            // Iterate through each column and export it to the array.
            for (int x = 0; x < tableModel.getColumnCount(); x++) {
                result[x] = tableModel.getColumnName(x);
            }
        } else {
            // Ensure that all of the selected column names are exported.  Start
            // by creating a string array large enough to hold all of the column
            // headers that will be exported.
            result = new String[columns.length];

            // Iterate through each column selected and export it to the array.
            for (int x = 0; x < columns.length; x++) {
                result[x] = tableModel.getColumnName(columns[x]);
            }
        }
        // Return the column header array.
        return result;
    }

    /**
     * Retrieves all of the data for a given set of columns in a given row.
     **
     * @param columns the list of column indices to limit the output by (if this
     *                is set to null, then all columns will be used).
     * @param row     the row indices to export data for.
     * @return the array containing all of the data for the given column and row
     *         indices only.
     */
    String[] getRowData (int[] columns, int row) {
        // The string array that will store the row data.
        String[] result = null;

        // If the columns variable is null, export data for all of the columns,
        // otherwise only export data for the selected by the array 'columns'.
        if (columns == null) {
            // Ensure that data for each column is exported.  Start by creating
            // a string array large enough to hold all of the column data for
            // the current row.
            result = new String[tableModel.getColumnCount()];

            // Iterate through each column in the current row, and export the
            // cell data to the output array.
            for (int x = 0; x < tableModel.getColumnCount(); x++) {
                result[x] = (String) tableModel.getValueAt(row, x);
            }
            System.out.println();
            
        } else {         
            // Ensure that all of the data for the selected columns is exported.
            // Start by creating a string array large enough to hold all of the
            // columns data selected for export from the current row.
            result = new String[columns.length];

            // Iterate through each selected column in the current row, and
            // export the cell data to the output array.
            for (int x = 0; x < columns.length; x++) {
                result[x] = (String) tableModel.getValueAt(row, columns[x]);
            }
        }
        // Return the column header array.
        return result;
    }

    /**
     * <p>Converts a number into a column letter(s).  This is used if the first
     *    line of a file being imported does not contain column headers -- i.e.
     *    namedHeaders is set to false.  The column letter(s) are a base-26
     *    representation of the number 'columnCount' using alphabet letters.</p>
     *
     * <p>The only exception is that the value zero cannot be represented, thus
     *    the letter "A" represents the value zero in the right-most digit, but
     *    the letter "A" represents the value one for every other digit.  This
     *    is okay, because we are only displaying column letters, and not
     *    creating a true base-10 to base-26 converter.</p>
     *
     * <p>For example, the value 27 is equal to "AA" (26 + 1)<br />
     *    Likewise, the value 3 is equal to "C", and the value 56 is "BC"</p>
     **
     * @param columnCount the number of the column to convert into a letter
     * @return the letter that corresponds to column #columnCount
     */
    public static String columnLetter(int columnCount) {
        // A string array to hold the resulting alphabetical column name.
        String result = "";

        // Loop through each base-26 digit (A-Z) within the number.  This is
        // done in case the number provided for columnCount is greater than 26
        // (the number of letters in the English alphabet).  In the case where
        // the number is greater than 26, more digits/letters are added to the
        // left, to ensure the number is accurately and uniquely represented.
        //
        // The algorithm is as follows:
        do {
            // 1. Determine the current character to write to the output string.
            //    This is done by taking columnCount, and applying the modulus
            //    operator with a value of 26.  Then, this value is added to
            //    'A', which will convert it to the appropriate letter.
            result = ((char)(((int)'A') + ((columnCount - 1) %26))) + result;

            // 2. Divide the columnCount value by 26.  This moves to the next
            //    digit.  Thus, in the next loop, we will examine the next
            //    base-26 digit to output.
            columnCount = (int) ((columnCount - 1) / 26);

            // 3. Loop until there are no more digits to add (columnCount == 0)
        } while (columnCount > 0);

        // Return the base-26 (alphabet) representation of the number in
        // the base-10 integer parameter columnCount.
        return result;
    }

    /**
     * Adds a list of column headers to the current table model.  These column
     * headers are added as new columns.
     **
     * @param columns the array of column names to use for adding columns.
     */
    public void addColumns(String[] columns) {
        while (tableModel.getColumnCount() < columns.length) {
            tableModel.addColumn(columns[tableModel.getColumnCount()]);
        }
    }

    /**
     * Adds a row to the table canvas.  If the row contains more columns than
     * the table canvas, more (generic) columns are added to allow the row to
     * be added in full. if the row is shorter than the column width,  
     * replace row with a row that is the same size as the table, padding with blank fields.
     **
     * @param row the array of values to use for the new row.
     */
    void addRow(String[] row) {
        // If there are less columns in the table canvas than in the row array
        // we wish to add, then add new columns to the table until there is
        // enough room to add all of the row data.  The new columns added will
        // use generic headers (because there are no column header names
        // available for them to use).
        
        while (tableModel.getColumnCount() < row.length) {
            tableModel.addColumn(
                    TableCanvas.columnLetter(tableModel.getColumnCount() + 1));
        };
        
        // Conversely, if the row is shorter than the column width, 
        // replace row with a row that is the same size as the table,
        // padding with blank fields.
        
        int TableWidth = tableModel.getColumnCount();
        if (TableWidth > row.length) {
            //System.out.println("addRow: padding row. row.length= " + row.length);
            String[] newrow = new String[TableWidth];
            int i=0;
            while (i < TableWidth) {
                if (i < row.length) {
                    newrow[i] = row[i];
                }
                else {
                    newrow[i] = " NA";
                }
                i++;
            row = newrow;
            }
        }
        
        //String message = "";
        // make sure cells aren't null or empty
        for (int i=0; i < row.length; i++) {
            if (row[i] == null || row[i] == "") {
                row[i] = " ";
            };
            //message = message + " [" + row[i] + "]";
        }
        // Add the row data to the table canvas.
        //System.out.println("addRow: checkpoint 1 " +  message);
        //System.out.flush();
        tableModel.addRow(row);

    }
    
 /**
     * Deletes the current row(s) selected.  This
     * function is only applicable if rows are selected (either using
     * the row or cell selection mode).
     **
     */    
    void deleteSelectedRows() {
           
        int[] rowSelection = tablePane.getSelectedRows();

        // Sort the list of rows selected (this is important so we can
        // iterate backwards through it).
        Arrays.sort(rowSelection);
        
        //tablePane.clearSelection();
        
// Iterate backwards through the list of rows and delete each
        // row in the list.  We iterate backwards, so we start with
        // the highest row number, and work our way to the lowest row
        // number.  The reason for working from highest to lowest is
        // that deleting low row numbers can change the indices of
        // higher row numbers.  For example:
        //
        //     If we have the data
        //           1 sssss
        //           2 ddddd
        //           3 ccccc
        //           4 eeeee
        //     And we delete the indices 2 and 3; if we delete 2 before
        //     3, we will get the wrong result:
        //           1 sssss
        //           3 ccccc
        //     However, if we delete the larger row first, we will get
        //     the correct result:
        //           1 sssss
        //           4 eeeee
        for (int count = rowSelection.length - 1; count >= 0; count--) {
            tableModel.removeRow(rowSelection[count]);
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
        return "Table";
    }

    /**
     * Reads a data into the canvas.  This method only handles CSV (comma-
     * separated-values) and TSV (tab-separated-values) spreadsheet table
     * formats.  If any other formats are passed to this method, this method
     * will default to the TSV format.  For more information about the exact
     * parsing specification of BioLegato's TSV and CSV formats, please see
     * the CSVFile and TSVFile JavaCC code and classes.
     **
     * @param format       the file format to use for parsing the file.  If the
     *                     string "" is passed, the PCDIO object should auto-
     *                     detect the format of the data.
     * @param in           the "file" (or stream) to read in from.
     * @param overwrite    whether to overwrite the currently selected
     *                     data in the current canvas with the data
     *                     being imported by this function/method.
     * @param forceall     force selection of the entire canvas.
     * 
     * @throws IOException if an error occurs while reading
     * @see org.biolegato.tables.CSVFile
     * @see org.biolegato.tables.TSVFile
     */
    public void readFile(String format, Reader in, boolean overwrite, boolean forceall)
                                                            throws IOException {
        // TODO: add more formats
        if (format.equalsIgnoreCase("csv")) {
            CSVFile.readFile(this, in, overwrite, forceall);
        } else if (format.equalsIgnoreCase("tsv")) {
            TSVFile.readFile(this, in, overwrite, forceall);
        } else {
            // TODO fix autodetect!!!
            TSVFile.readFile(this, in, overwrite, forceall);
        }
    }

    /**
     * Writes data out from the canvas to an Appendable object.  This method
     * only handles CSV (comma-separated-values) and TSV (tab-separated-values)
     * spreadsheet table formats.  If any other formats are passed to this
     * method, this method won't write any data to the Appendable object.  For
     * more information about the exact parsing specification of BioLegato's TSV
     * and CSV formats, please see the CSVFile and TSVFile JavaCC code and
     * classes.
     **
     * @param  format      the file format to use for writing the file.
     * @param  out         the "file" (or stream) to write out to.
     * @param  forceall    write the entire contents of the canvas
     *                     instead of just the currently selected
     *                     sequences in the canvas.
     * @throws IOException if an error occurs while writing
     * @see org.biolegato.tables.CSVFile
     * @see org.biolegato.tables.TSVFile
     */
    public void writeFile(String format, Appendable out, boolean forceall)
                                                            throws IOException {
        if (format.equalsIgnoreCase("csv")) {
            CSVFile.writeFile(out, getData(forceall));
        } else if (format.equalsIgnoreCase("tsv")) {
            TSVFile.writeFile(out, getData(forceall));
        }
    }

        /**
     * Changes the SelectioMode of the TableCanvas
     **
     * @param  m      Selection mode from TableCanvas.SelectionMode
     *
     * 
     * @see org.biolegato.tables.CSVFile
     * @see org.biolegato.tables.TSVFile
     */
    public void setSelectionMode(SelectionMode m) {
        mode = m;
}
    
    /**
     * Displays the table canvas within a BioLegato window.
     **
     * @return the Component object containing the canvas display data.
     */
    public Component display() {
        // The main box panel to display the table canvas in.
        Box mainPane = new Box(BoxLayout.PAGE_AXIS);
        // A scroll pane to display the table canvas in.  (This will allow the
        // user to scroll the table horizontally and vertically.)
        JScrollPane scrollPane = new JScrollPane(
                JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
                JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);

        // Configure the scroll pane to view the table within the canvas.
        scrollPane.setViewportView(tablePane);

        // Configure the table.
        tablePane.setAutoCreateColumnsFromModel(true);
        tablePane.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        tablePane.setFont(new Font("Monospaced", Font.PLAIN, 12));
        tablePane.doLayout();

        // Add the table's scroll pane to the main panel.
        mainPane.add(scrollPane);
        JRadioButton radioCell;
        radioCell = new JRadioButton(new AbstractAction("by cell") {
                /**
                 * When the user clicks on this radio button, change the table
                 * canvas's selection mode to "by cell".
                 **
                 * @param evt this parameter is not used by this method.
                 */
                public void actionPerformed(ActionEvent evt) {
                    tablePane.setCellSelectionEnabled(true);
                    tablePane.setColumnSelectionAllowed(true);
                    tablePane.setRowSelectionAllowed(true);
                    mode = SelectionMode.CELL;
                }
            });
        JRadioButton radioCol;
        radioCol = new JRadioButton(new AbstractAction("by column") {
                /**
                 * When the user clicks on this radio button, change the table
                 * canvas's selection mode to "by column".
                 **
                 * @param evt this parameter is not used by this method.
                 */
                public void actionPerformed(ActionEvent evt) {
                    tablePane.setCellSelectionEnabled(false);
                    tablePane.setColumnSelectionAllowed(true);
                    tablePane.setRowSelectionAllowed(false);
                    mode = SelectionMode.COLUMN;
                }
            });
        JRadioButton radioRow;
        radioRow = new JRadioButton(new AbstractAction("by row") {
                /**
                 * When the user clicks on this radio button, change the table
                 * canvas's selection mode to "by row".
                 **
                 * @param evt this parameter is not used by this method.
                 */
                public void actionPerformed(ActionEvent e) {
                    tablePane.setCellSelectionEnabled(false);
                    tablePane.setColumnSelectionAllowed(false);
                    tablePane.setRowSelectionAllowed(true);
                    mode = SelectionMode.ROW;
                }
            });

        // Set the table selection model.  If the BioLegato property
        // "table.selection" is set to "row" or or "column", then limit the user
        // to only select by either rows or columns in the table canvas.  If the
        // property "table.selection" is set to any other value, then let the
        // user choose how they want to select data within the table canvas.
        // For more information on this property, please see default.properties,
        // in BioLegato's Core/src directory.
        if ("row".equals(getProperty("table.selection"))) {
            // Force the user to only be able to select data in the table canvas
            // by row.
            mode = SelectionMode.ROW;
            tablePane.setCellSelectionEnabled(false);
            tablePane.setColumnSelectionAllowed(false);
            tablePane.setRowSelectionAllowed(true);
        } else if ("column".equals(getProperty("table.selection"))) {
            // Force the user to only be able to select data in the table canvas
            // by column.
            mode = SelectionMode.COLUMN;
            tablePane.setCellSelectionEnabled(false);
            tablePane.setColumnSelectionAllowed(true);
            tablePane.setRowSelectionAllowed(false);
        } else {
            // Allow the user to choose how they wish to select data within the
            // table canvas.  The current options for the user are: by row, by
            // column, or by cell.

            // Create a new box panel to contain the selection modes, as radio
            // buttons, for the user to control.
            Box selectionPane = new Box(BoxLayout.LINE_AXIS);
            // A variable to cache data for the new radio buttons to add.
            
            // Create a button group to ensure that each selection mode is
            // mutually exclusive (i.e. the user can only pick one at a time).
            ButtonGroup group = new ButtonGroup();

            // Configure the table canvas to use the "by cell" selection mode,
            // by default.
            tablePane.setCellSelectionEnabled(true);
            tablePane.setColumnSelectionAllowed(true);
            tablePane.setRowSelectionAllowed(true);

            // Add a label header for the selection mode box panel.
            selectionPane.add(new JLabel("Selection mode"));

            // Create and add the "by cell" selection mode radio button.  (This
            // mode will be selected by default.)
            
            radioCell.setSelected(true);
            group.add(radioCell);
            selectionPane.add(radioCell);

            // Create and add the "by column" selection mode radio button.
            
            group.add(radioCol);
            selectionPane.add(radioCol);

            // Create and add the "by row" selection mode radio button.
            
            group.add(radioRow);
            selectionPane.add(radioRow);

            // Add the selection mode panel to the main panel.
            mainPane.add(selectionPane);
        }

        // Create a new list for storing the row headers (i.e. row numbers to
        // the right of the JTable object).
        final JList rowHeader = new JList(rowHeaderModel);
        rowHeader.setFixedCellWidth(50);
        rowHeader.setFixedCellHeight(tablePane.getRowHeight());
                                    // + tablePane.getRowMargin());
        rowHeader.setCellRenderer(new RowHeaderRenderer(tablePane));
        scrollPane.setRowHeaderView(rowHeader);
        
        AbstractAction find = new AbstractAction("Find") {
            @Override
            public void actionPerformed(ActionEvent ae) {

                JTextField primaryStr = new JTextField(10);
                JTextField secondaryStr = new JTextField(10);
                ButtonGroup group = new ButtonGroup();
                JRadioButton radio1 = new JRadioButton("Selected Only");
                JRadioButton radio2 = new JRadioButton("Regular Expression");
                JRadioButton radio3 = new JRadioButton("Row Selection");
                radio3.setSelected(true);
                group.add(radio3);
                JRadioButton radio4 = new JRadioButton("Column Selection");
                group.add(radio4);

                JPanel myPanel = new JPanel(new GridLayout(4, 2));
                myPanel.add(new JLabel("Primary String:"));
                myPanel.add(primaryStr);
                myPanel.add(new JLabel("Secondary String:"));
                myPanel.add(secondaryStr);
                myPanel.add(radio1);
                myPanel.add(radio2);
                myPanel.add(radio3);
                myPanel.add(radio4);

                int result = JOptionPane.showConfirmDialog(null, myPanel, "Find", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
                if (result == JOptionPane.OK_OPTION) {
                    String priStr = primaryStr.getText();
                    String secStr = secondaryStr.getText();

                    if (!radio1.isSelected()) {
                        tablePane.clearSelection();
                    }

                    String[][] data = getData();

                    if (radio3.isSelected()) {
                        tablePane.setRowSelectionAllowed(true);
                        tablePane.setColumnSelectionAllowed(false);
                        mode = SelectionMode.ROW;
                        radioRow.setSelected(true);
                    } else {
                        tablePane.setRowSelectionAllowed(false);
                        tablePane.setColumnSelectionAllowed(true);
                        mode = SelectionMode.COLUMN;
                        radioCol.setSelected(true);
                    }

                    for (int row = 0; row < data.length; row++) {
                        for (int col = 0; col < data[row].length; col++) {
                            if (data[row][col].contains(priStr)) {
                                System.out.println("Row: " + row + ", Col: " + col + "\nData: " + data[row][col]);
                                if (radio3.isSelected()) {
                                    tablePane.addRowSelectionInterval(row - 1, row - 1);
                                } else {
                                    tablePane.addColumnSelectionInterval(col, col);
                                }
                            }
                        }
                    }
                }
            }
        };
        
        addMenuHeading("Edit").add(new JMenuItem(find));
 
        //////////////////////////////////
        //******************************//
        //* ADD THE DEFAULT MENU ITEMS *//
        //******************************//
        //////////////////////////////////

        ///////////////////////////
        // Add the "Open" button //
        ///////////////////////////
        addMenuHeading("File").insert(new JMenuItem(
                new AbstractAction("Open...") {
            /**
             * Serialization number - required for no warnings.
             */
            private static final long serialVersionUID = 7526472295622776157L;
            
            /**
             * Sets the mnemonic for the event.
             */
            {
                putValue(MNEMONIC_KEY, new Integer(KeyEvent.VK_O));
            }

            /**
             * Event handler - displays a JFileChooser so that the user can
             * select and open a file.  Once the file is selected, the file will
             * be read into the current canvas, using whichever FileFilter
             * choice the user selected.  The two FileFilters in the
             * JFileChooser are CSV_FILTER and TSV_FILTER.  If CSV_FILTER is
             * selected, then the file will be parsed as a CSV file.  If
             * TSV_FILTER is selected, then the file will be parsed as a TSV
             * file.
             **
             * @param evt ignored by this method.
             * @see org.biolegato.tables.CSVFile
             * @see org.biolegato.tables.TSVFile
             */
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                // A variable for storing all of the files the user selects
                // inside the JFileChooser window.
                File[] openFiles;
                // The JFileChooser windwow, from which the user selects the
                // file to open.
                JFileChooser openDlg = new JFileChooser();

                // Configure the JFileChooser object to use PCD's current
                // directory as its default directory; enable multiple file
                // selection; and to disable the "accept all" file filter
                // (which will correspond to file type auto-detection).
                openDlg.setCurrentDirectory(PCD.getCurrentPWD());
                openDlg.setAcceptAllFileFilterUsed(false);
                openDlg.setMultiSelectionEnabled(true);

                // Add the CSV and TSV file filters.
                openDlg.addChoosableFileFilter(CSV_FILTER);
                openDlg.addChoosableFileFilter(TSV_FILTER);

                // If the user clicks the OK button, then check to see if at
                // least one file is selected, and also update the current PCD
                // path (such that any subsequent new file choosers will use the
                // directory last browsed as their default directory.  If files
                // are selected, then read them into the canvas.
                if (openDlg.showOpenDialog(getJFrame())
                        == JFileChooser.APPROVE_OPTION) {
                    // Extract the files (if any) selected in the JFileChooser.
                    openFiles = openDlg.getSelectedFiles();
                    
                    // Prevent parsing a null value for openFiles.
                    if (openFiles != null) {
                        // Iterate through all of the files selected in the
                        // JFileChooser object.
                        for (File opf : openFiles) {
                            // Ensure that the file exists and is a file (i.e. skip
                            // directories and non-existent files).
                            if (opf.exists() && opf.isFile()) {
                                try {
                                    // Read the file into the table canvas,
                                    // using the appropriate file filter (csv,
                                    // or tsv).
                                    readFile((openDlg.getFileFilter()
                                            == CSV_FILTER ? "csv" : "tsv"),
                                        new FileReader(opf), false,false);
                                } catch (IOException ioe) {
                                    // Print a stack trace if any error occurs.
                                    ioe.printStackTrace(System.err);
                                }
                            }
                        }
                    }
                    
                    // Update the PCD working directory.
                    if (openDlg.getCurrentDirectory() != null) {
                        PCD.setCurrentPWD(openDlg.getCurrentDirectory());
                    }
                }
            }
        }), 0);

        /////////////////////////////////
        // Add the "Properties" button //
        /////////////////////////////////
        addMenuHeading("File").insert(new JMenuItem(new AbstractAction("Properties...") {
            /**
             * Serialization number - required for no warnings.
             */
            private static final long serialVersionUID = 7526472295622776157L;
            /**
             * Sets the mnemonic for the event.
             */
            {
                putValue(MNEMONIC_KEY, new Integer(KeyEvent.VK_P));
            }
            /**
             * The "Properties..." menu item action.  This action will open a
             * window, so the user can change the properties of the table
             * canvas.  The properties window is controlled (and displayed,
             * etc.) by the TableCanvasProperties class.
             **
             * @param evt ignored by this method.
             * @see org.biolegato.sequence.canvas.TableCanvasProperties
             */
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                new TableCanvasProperties(getJFrame(), tablePane, rowHeader);
            }
        }), 2);

        /////////////////////////////////
        // Add the "Delete Row" button //
        /////////////////////////////////
        if (!"true".equalsIgnoreCase(getProperty("readonly"))) {
            addMenuHeading("Edit").insert(new JMenuItem(new AbstractAction("Delete Row(s)") {
            /**
             * Serialization number - required for no warnings.
             */
            private static final long serialVersionUID = 7526472295622776157L;
            /**
             * Sets the mnemonic for the event.
             */
            {
                putValue(MNEMONIC_KEY, new Integer(KeyEvent.VK_O));
            }
            /**
             * Event handler - deletes the current row(s) selected.  This
             * function is only applicable if rows are selected (either using
             * the row or cell selection mode).
             **
             * @param evt ignored by this method.
             */
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                // Extract a list of the rows selected.
                int[] rowSelection = tablePane.getSelectedRows();

                // Sort the list of rows selected (this is important so we can
                // iterate backwards through it).
                Arrays.sort(rowSelection);

                // Iterate backwards through the list of rows and delete each
                // row in the list.  We iterate backwards, so we start with
                // the highest row number, and work our way to the lowest row
                // number.  The reason for working from highest to lowest is
                // that deleting low row numbers can change the indices of
                // higher row numbers.  For example:
                //
                //     If we have the data
                //           1 sssss
                //           2 ddddd
                //           3 ccccc
                //           4 eeeee
                //     And we delete the indices 2 and 3; if we delete 2 before
                //     3, we will get the wrong result:
                //           1 sssss
                //           3 ccccc
                //     However, if we delete the larger row first, we will get
                //     the correct result:
                //           1 sssss
                //           4 eeeee
                for (int count = rowSelection.length - 1; count >= 0; count--) {
                    tableModel.removeRow(rowSelection[count]);
                }
            }
        }), 0);
        }

        return mainPane;
    }
}
