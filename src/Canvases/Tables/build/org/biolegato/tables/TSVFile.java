/* Generated By:JavaCC: Do not edit this line. TSVFile.java */
package org.biolegato.tables;
/*
 * TSVFile.java
 *
 * Created on January 30, 2008, 11:58 AM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

import java.io.BufferedReader;
import javax.swing.filechooser.FileFilter;
import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.util.List;
import java.util.ArrayList;
import org.biolegato.main.DataCanvas;

/**
 * <p>TSV (Tab Separated Values) file format parser.  A TSV file is a
 * spreadsheet, where the rows are separated by new line characters (i.e. each
 * line of the file is a row), and the columns are separated by tab characters.
 * Thus an example of a TSV file would be:</p>
 * <pre>
 *       Name	Phone Num	Birthday
 *       John	555-5123	Apr. 30, 1937
 *       Jane	555-4352	Feb. 13, 1976
 *       Sam	555-6333	Jan. 7, 1961
 *       Tom	555-7777	Aug. 4, 1943</pre>
 * <p>Where name, phone number, and birthday are all separated by tab characters
 * and are each columns of a spreadsheet.</p>
 *
 * <p><i>NOTE: fields may be double or single quoted.  In such cases, the single
 *       or double quotes character may be escaped by doubling it (e.g. "AB""C"
 *       will be read as AB"C).</i></p>
 **
 * @author Graham Alvare
 * @author Brian Fristensky
 */
public class TSVFile implements TSVFileConstants {
    /**
     * An empty 1D string array for calling the List.toArray method.
     */
    public static final String[] EST = new String[0];
    /**
     * An empty 2D string array for calling the List.toArray method.
     */
    public static final String[][] DBL_EST = new String[0][];
    /**
     * TSV file format filter (for file chooser objects).  This file filter will
     * determine which files in a file chooser are TSV (tab separated values)
     * files, based on their file extension.
     */
    public static final FileFilter TSV_FILTER = new FileFilter () {
        /**
         * Determines whether a file is a TSV file (based on extension).
         * Currently the only extensions supported are ".csv", ".tab" and ".tsv"
         **
         * @param  file  the file to test for TSV format compatibility.
         * @return true if the file is a TSV file (otherwise false).
         * @see javax.swing.filechooser.FileFilter#accept
         */
        public boolean accept (File file) {
            return (file.isDirectory()
                    || file.getAbsolutePath().toLowerCase().endsWith(".csv")
                    || file.getAbsolutePath().toLowerCase().endsWith(".tab")
                    || file.getAbsolutePath().toLowerCase().endsWith(".tsv"));
        }

        /**
         * Returns a description of the file format that can be displayed to the
         * user.  This allows the user to easily find and identify a file format
         * in the file type combo box (within file chooser objects).
         **
         * @return the string description of the file format.
         * @see javax.swing.filechooser.FileFilter#getDescription
         */
        public String getDescription () {
            return "TSV - Tab Separated Values file (*.csv,*.tab,*.tsv)";
        }
    };

    /**
     * Used to auto-detect BioLegato table canvas formats.  This function will
     * return true if the format is suspected to be a TSV file.  Otherwise, the
     * function will return false.
     **
     * @param test the reader to attempt to detect the format of.
     * @return whether the format is a TSV file.
     */
    public boolean isFormat (Reader test) {
        // The current character being parsed (for auto-detection).
        // If the check variable is a tab character '\t' at the end of parsing,
        // then the file is suspected to be a TSV file.  Note that we only
        // parse the file until the end of line or tab character is encountered.
        // Thus, our purpose is to see if there is at least one column (tab) on
        // the first line (indicating the format could likely be a TSV file).
        int check = ' ';

        try {
            // Iterate through the stream until either a tab or end-line
            // character is encountered.  Store each character in the 'check'
            // variable.  This will later be tested.  If the character is tab,
            // then there is at least one column on the first line of the file.
            while (check != '\u005cn' && check != '\u005cr' && check != '\u005ct') {
                // Mark the stream, so we can later reset it to the beginning.
                test.mark(2);

                // Get the next character in the stream (to test).
                check = test.read();
            }

            // Reset the stream to the beginning for parsing.
            test.reset();
        } catch (IOException e) {
            e.printStackTrace(System.err);
        }

        // Return true if there is at least one column on the first line of the
        // suspected TSV file.
        return (check == '\u005ct');
    }

    /**
     * Reads TSV data from a Reader object into a table canvas.
     **
     * @param table        the table canvas to read the TSV file into.
     * @param currentFile  the Reader object to parsed the TSV data from.
     * @param overwrite    whether to overwrite the currently selected content
     *                     in the table canvas with the data parsed by this
     *                     function.
     * @throws java.io.IOException thrown if there is any exception while
     *                             reading from the Reader object.
     */
    public static void readFile(TableCanvas table, Reader currentFile,
                                boolean overwrite, boolean forceall) throws IOException {
        try {
            if (overwrite) {

                // We need to jot down the number of columns before we delete rows
                int numCols = table.tableModel.getColumnCount();

                // If no data are selected, delete all data in the canvas
                boolean NothingSelected = (table.tablePane.getSelectedColumnCount() == 0
                    && table.tablePane.getSelectedRowCount() == 0);
                if (forceall || NothingSelected) {
                    table.setSelectionMode(TableCanvas.SelectionMode.ROW);
                    table.tablePane.selectAll();
                    table.deleteSelectedRows();
                    table.tablePane.clearSelection();
                }

                // DOESN'T ELIMINATE THE ERROR, EVEN IF WE RUN addRow(dummy) TWICE
                // An error would be generated if we had an empty canvas, so add a dummy
                // first row and clear the selection.  
                /**if (forceall || NothingSelected) {
                      String[] dummy = new String[numCols];
                      for (int i = 0; i < numCols; i++ ) {
                          dummy[i] = "";
                      };
                      table.tableModel.addRow(dummy);
                } 
             */
            }
            // Create a new parser object to read the TSV file.
            TSVFile parser = new TSVFile(currentFile);
            //System.out.println("Checkpoint 1");
            //System.out.flush();
            // Parse the TSV data from the Reader object.
            parser.parseTSV(table);
            //System.out.println("Checkpoint 3");
        } catch (ParseException pe) {
            pe.printStackTrace(System.err);
        }
    }

    /**
     * Reads TSV data from a Reader object into a 2D string array.  Please note
     * that the 2D string array is specified as follows: the rows are the first
     * index of the array (e.g. data[0] is the first row of the array), and the
     * columns are the second index of the array (e.g. data[0][1] is the second
     * column of the first row of the array.
     **
     * @param currentFile the Reader object to parsed the TSV data from.
     * @throws java.io.IOException thrown if there is any exception while
     *                             reading from the Reader object.
     */
    public static String[][] readTSV(Reader currentFile) throws IOException {
        // Stores the 2D string array parsed from the file.
        // By default, this variable contains an empty 2D string array.
        String[][] result = DBL_EST;

        try {
            // Create a new parser object to read the TSV file.
            TSVFile parser = new TSVFile(currentFile);

            // Parse the TSV data from the Reader object.
            result = parser.parseDoubleArray();
        } catch (ParseException pe) {
            pe.printStackTrace(System.err);
        }
        return result;
    }

    /**
     * Writes a 2D string array as TSV data into an Appendable object.  Please
     * note that the rows are the first index of the array (e.g. data[0] is the
     * first row of the array), and the columns are the second index of the
     * array (e.g. data[0][1] is the second column of the first row of the
     * array.
     **
     * @param writer the Appendable object to write the TSV data to.
     * @param data   the 2D string array to parse into TSV data.
     * @throws java.io.IOException thrown if there is any exception while
     *                             writing to the Appendable object.
     */
    public static void writeFile(Appendable writer, String[][] data)
                                                            throws IOException {
        // The current table cell being written.
        String cell;

        // Translate each row in the 2D array and write it to the file.
        // The original version enclosed each cell in double quotes (")
        // The motivation was to accommodate MS-Excell, but Excell no longer
        // requires quotes. Lines to begin and end a cell have been commented out.
        for (int y = 0; y < data.length; y++) {
            // Translate each column in the row array and write it to the file.
            // Start by writing the first column.
            if (data[y].length > 0) {
                //writer.append('\"');
                // Double all of the double-quotation marks.  This is to
                // distinguish double-quotation marks in each field from
                // double-quotation marks surrounding the fields.
                writer.append(data[y][0].replaceAll("\u005c"", "\u005c"\u005c""));
                //writer.append('\"');
            }
            // Write each additional column, appending the delimiter character
            // before it (i.e. the tab character) -- e.g. "\tABC"
            for (int x = 1; x < data[y].length; x++) {
                cell = data[y][x];
                writer.append('\u005ct');
                //writer.append('\"');
                // If the cell is not null, double all of the double-quotation
                // marks.  This is to distinguish double-quotation marks in each
                // field from double-quotation marks surrounding the fields.
                if (cell != null) {
                    writer.append(data[y][x].replaceAll("\u005c"", "\u005c"\u005c""));
                }
                //writer.append('\"');
            }
            writer.append('\u005cn');
        }
    }

/**
 * Parse TSV data into a table canvas.
 **
 * @param table the table canvas to parse the data into.
 * @see org.biolegato.tables.TableCanvas#namedHeaders
 */
  final public void parseTSV(TableCanvas table) throws ParseException {
    /* Stores the spreadsheet row most recently parsed from the data. */
    String[] row;
    /* Read the first row of the TSV data. */
    
        row = parseRow();
        //System.out.println("parseTSV:row length: " + row.length );
        /* If named column headers is enabled, then read the first row as column
         * headers for the table. */
        if (table.namedHeaders) {
            //System.out.println("Checkpoint 2.1a");
            table.addColumns(row);
        } else {
            //System.out.println("Checkpoint 2.1b");
            //System.out.flush();
            table.addRow(row);
        }
    //System.out.println("Checkpoint 2.2");

    label_1:
    while (true) {
      switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
      case RDELIM:
        ;
        break;
      default:
        jj_la1[0] = jj_gen;
        break label_1;
      }
      jj_consume_token(RDELIM);
      row = parseRow();
                                  table.addRow(row);
    }
    jj_consume_token(0);
  }

/**
 * Parse TSV data into a table canvas.  Please note that the 2D string array is
 * specified as follows: the rows are the first index of the array (e.g. data[0]
 * is the first row of the array), and the columns are the second index of the
 * array (e.g. data[0][1] is the second column of the first row of the array.
 **
 * @param table the table canvas to parse the data into.
 */
  final public String[][] parseDoubleArray() throws ParseException {
    /* Stores the spreadsheet row most recently parsed from the data. */
    String[] row;
    /* An array list of all of the rows parsed in the file.  This will later
     * be converted into a 2D string array via. the List.toString method */
    List<String[]> rows = new ArrayList<String[]>();
    /* Read the first row of the TSV data. */
        row = parseRow();
                       rows.add(row);
    label_2:
    while (true) {
      switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
      case RDELIM:
        ;
        break;
      default:
        jj_la1[1] = jj_gen;
        break label_2;
      }
      jj_consume_token(RDELIM);
      row = parseRow();
                                  rows.add(row);
    }
    jj_consume_token(0);
      {if (true) return rows.toArray(DBL_EST);}
    throw new Error("Missing return statement in function");
  }

/**
 * Parses a row of the TSV data.
 **
 * @return the array of columns contained in the row.
 */
  final public String[] parseRow() throws ParseException {
    /* Stores the spreadsheet column most recently parsed from the data. */
    String col;
    /* Stores all of the columns parsed from the current row.  This will later
     * be converted into a string array via. the List.toString method */
    List<String> columns = new ArrayList<String>();
    /* Read the first column in the row. */
        col = parseColumn();
                          columns.add(col);
    label_3:
    while (true) {
      switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
      case CDELIM:
        ;
        break;
      default:
        jj_la1[2] = jj_gen;
        break label_3;
      }
      jj_consume_token(CDELIM);
      col = parseColumn();
                                     columns.add(col);
    }
      {if (true) return columns.toArray(EST);}
    throw new Error("Missing return statement in function");
  }

/**
 * Parses a column of the TSV data.
 **
 * @return a single column, represented as a string object.
 */
  final public String parseColumn() throws ParseException {
    /* The most recently read in column token.  The string image of this token
     * (which is the actual literal string read from the tokenizer) will be
     * parsed into the String object returned by this function/method. */
    Token t;
    /* The string representaton of the column read by the tokenizer/parser. */
    String result = "";
    switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
    case SQSTART:
    case DQSTART:
    case CHAR:
      switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
      case CHAR:
        /* Handles non-quoted columns/cells. */
                t = jj_consume_token(CHAR);
                                   result =  t.image;
        switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
        case CHARS:
          t = jj_consume_token(CHARS);
                                   result += t.image;
          break;
        default:
          jj_la1[3] = jj_gen;
          ;
        }
        break;
      case SQSTART:
        jj_consume_token(SQSTART);
        t = jj_consume_token(SCHAR);
                                   result =  t.image;
        jj_consume_token(SQEND);
        break;
      case DQSTART:
        jj_consume_token(DQSTART);
        t = jj_consume_token(DCHAR);
                                   result =  t.image;
        jj_consume_token(DQEND);
        break;
      default:
        jj_la1[4] = jj_gen;
        jj_consume_token(-1);
        throw new ParseException();
      }
      break;
    default:
      jj_la1[5] = jj_gen;
      ;
    }
      {if (true) return result;}
    throw new Error("Missing return statement in function");
  }

  /** Generated Token Manager. */
  public TSVFileTokenManager token_source;
  SimpleCharStream jj_input_stream;
  /** Current token. */
  public Token token;
  /** Next token. */
  public Token jj_nt;
  private int jj_ntk;
  private int jj_gen;
  final private int[] jj_la1 = new int[6];
  static private int[] jj_la1_0;
  static {
      jj_la1_init_0();
   }
   private static void jj_la1_init_0() {
      jj_la1_0 = new int[] {0x4,0x4,0x2,0x40,0x38,0x38,};
   }

  /** Constructor with InputStream. */
  public TSVFile(java.io.InputStream stream) {
     this(stream, null);
  }
  /** Constructor with InputStream and supplied encoding */
  public TSVFile(java.io.InputStream stream, String encoding) {
    try { jj_input_stream = new SimpleCharStream(stream, encoding, 1, 1); } catch(java.io.UnsupportedEncodingException e) { throw new RuntimeException(e); }
    token_source = new TSVFileTokenManager(jj_input_stream);
    token = new Token();
    jj_ntk = -1;
    jj_gen = 0;
    for (int i = 0; i < 6; i++) jj_la1[i] = -1;
  }

  /** Reinitialise. */
  public void ReInit(java.io.InputStream stream) {
     ReInit(stream, null);
  }
  /** Reinitialise. */
  public void ReInit(java.io.InputStream stream, String encoding) {
    try { jj_input_stream.ReInit(stream, encoding, 1, 1); } catch(java.io.UnsupportedEncodingException e) { throw new RuntimeException(e); }
    token_source.ReInit(jj_input_stream);
    token = new Token();
    jj_ntk = -1;
    jj_gen = 0;
    for (int i = 0; i < 6; i++) jj_la1[i] = -1;
  }

  /** Constructor. */
  public TSVFile(java.io.Reader stream) {
    jj_input_stream = new SimpleCharStream(stream, 1, 1);
    token_source = new TSVFileTokenManager(jj_input_stream);
    token = new Token();
    jj_ntk = -1;
    jj_gen = 0;
    for (int i = 0; i < 6; i++) jj_la1[i] = -1;
  }

  /** Reinitialise. */
  public void ReInit(java.io.Reader stream) {
    jj_input_stream.ReInit(stream, 1, 1);
    token_source.ReInit(jj_input_stream);
    token = new Token();
    jj_ntk = -1;
    jj_gen = 0;
    for (int i = 0; i < 6; i++) jj_la1[i] = -1;
  }

  /** Constructor with generated Token Manager. */
  public TSVFile(TSVFileTokenManager tm) {
    token_source = tm;
    token = new Token();
    jj_ntk = -1;
    jj_gen = 0;
    for (int i = 0; i < 6; i++) jj_la1[i] = -1;
  }

  /** Reinitialise. */
  public void ReInit(TSVFileTokenManager tm) {
    token_source = tm;
    token = new Token();
    jj_ntk = -1;
    jj_gen = 0;
    for (int i = 0; i < 6; i++) jj_la1[i] = -1;
  }

  private Token jj_consume_token(int kind) throws ParseException {
    Token oldToken;
    if ((oldToken = token).next != null) token = token.next;
    else token = token.next = token_source.getNextToken();
    jj_ntk = -1;
    if (token.kind == kind) {
      jj_gen++;
      return token;
    }
    token = oldToken;
    jj_kind = kind;
    throw generateParseException();
  }


/** Get the next Token. */
  final public Token getNextToken() {
    if (token.next != null) token = token.next;
    else token = token.next = token_source.getNextToken();
    jj_ntk = -1;
    jj_gen++;
    return token;
  }

/** Get the specific Token. */
  final public Token getToken(int index) {
    Token t = token;
    for (int i = 0; i < index; i++) {
      if (t.next != null) t = t.next;
      else t = t.next = token_source.getNextToken();
    }
    return t;
  }

  private int jj_ntk() {
    if ((jj_nt=token.next) == null)
      return (jj_ntk = (token.next=token_source.getNextToken()).kind);
    else
      return (jj_ntk = jj_nt.kind);
  }

  private java.util.List<int[]> jj_expentries = new java.util.ArrayList<int[]>();
  private int[] jj_expentry;
  private int jj_kind = -1;

  /** Generate ParseException. */
  public ParseException generateParseException() {
    jj_expentries.clear();
    boolean[] la1tokens = new boolean[11];
    if (jj_kind >= 0) {
      la1tokens[jj_kind] = true;
      jj_kind = -1;
    }
    for (int i = 0; i < 6; i++) {
      if (jj_la1[i] == jj_gen) {
        for (int j = 0; j < 32; j++) {
          if ((jj_la1_0[i] & (1<<j)) != 0) {
            la1tokens[j] = true;
          }
        }
      }
    }
    for (int i = 0; i < 11; i++) {
      if (la1tokens[i]) {
        jj_expentry = new int[1];
        jj_expentry[0] = i;
        jj_expentries.add(jj_expentry);
      }
    }
    int[][] exptokseq = new int[jj_expentries.size()][];
    for (int i = 0; i < jj_expentries.size(); i++) {
      exptokseq[i] = jj_expentries.get(i);
    }
    return new ParseException(token, exptokseq, tokenImage);
  }

  /** Enable tracing. */
  final public void enable_tracing() {
  }

  /** Disable tracing. */
  final public void disable_tracing() {
  }

}
