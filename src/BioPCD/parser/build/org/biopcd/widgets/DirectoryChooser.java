package org.biopcd.widgets;

import java.io.IOException;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JFileChooser;

/**
 * A wrapper class used for directory selection within command windows.
 **
 * @author Graham Alvare
 * @author Brian Fristensky
 */
public class DirectoryChooser extends AbstractFileChooser {
    /**
     * The icon for the widget
     */
    public static final Icon WIDGET_ICON = new ImageIcon(
            DirectoryChooser.class.getClassLoader().getResource(
                    "org/biopcd/icons/dirchooser.png"));

    /**
     * Creates a new instance of a directory chooser widget
     * (this specific constructor is used by the PCD editor ONLY!).
     **
     * @param name  the PCD variable name (this name can be referenced
     *              in the command using the % symbol; for example,
     *              if the name value was set to "A", the value of this
     *              widget could be accessed by using %A% (lower or
     *              upper-case) within the PCD menu command string.
     */
    public DirectoryChooser(String name) {
        super(name, JFileChooser.DIRECTORIES_ONLY, "Choose directory...");
    }

    /**
     * Creates a new instance of a file chooser widget.
     **
     * @param name    the PCD variable name (this name can be referenced
     *                in the command using the % symbol; for example,
     *                if the name value was set to "A", the value of this
     *                widget could be accessed by using %A% (lower or
     *                upper-case) within the PCD menu command string.
     * @param label   the label to display representing the parameter to be
     *                manipulated by the number widget.  This is the text the
     *                user will see to the left of the widget in any
     *                BioLegato menu windows.
     * @param value   the default value for the file chooser
     */
    public DirectoryChooser(String name, String label, String value) {
        super(name, label, value, JFileChooser.DIRECTORIES_ONLY,
                "Choose directory...");
    }

    /**
     * Writes the BioPCD representation of the menu widget to a writer object
     * (see BioLegato's BioPCD editor for more details)
     **
     * @param scope  the level of scope to write the menu widget.  In the case
     *               of PCD, the scope of each line is indicated by the number
     *               of spaced preceding the line.  Every 4 spaces count as
     *               one level of scope (any number not divisible by 4 is
     *               considered an error), thus if a line is preceded by 4
     *               spaces, its scope level is considered to be 1
     * @param out    the Appendable object to output the BioPCD code.
     */
    @Override
    public void pcdOut (int scope, Appendable out) throws IOException {
        super.pcdOut(scope, out, "dir");
    }
}
