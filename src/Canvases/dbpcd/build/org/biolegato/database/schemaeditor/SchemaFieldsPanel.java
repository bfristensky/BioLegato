/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.biolegato.database.schemaeditor;

import org.biopcd.widgets.*;
import org.biolegato.database.fields.*;
import javax.swing.JFrame;
import javax.swing.JToolBar;
import org.biolegato.database.DBCanvas;

/**
 * Field panel (for adding database fields to a BioLegato DB)
 **
 * @author Graham Alvare
 * @author Brian Fristensky
 */
public class SchemaFieldsPanel extends JToolBar {

    public SchemaFieldsPanel (DBCanvas parent) {
        super("Database fields", HORIZONTAL);

        this.setFloatable(false);

        this.setTransferHandler(new WidgetTransferHandler());

        this.add(new FieldGenerator(parent, TextWidget.WIDGET_ICON, "Text field", DBTextField.class));
        this.addSeparator();
        this.add(new FieldGenerator(parent, Chooser.WIDGET_ICON, "Boolean field", BooleanField.class));
        this.addSeparator();
        this.add(new FieldGenerator(parent, NumberWidget.WIDGET_ICON, "Number field", NumberField.class));
        this.addSeparator();
        this.add(new FieldGenerator(parent, ChoiceList.WIDGET_ICON, "Reference field", ReferenceField.class));
        this.addSeparator();
        this.add(new FieldGenerator(parent, CommandButton.WIDGET_ICON, "Command button", DBCommand.class));

        setVisible(true);
    }
}
