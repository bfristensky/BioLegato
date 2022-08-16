/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.biopcd.widgets;

import java.awt.Component;

/**
 * <p>This class provides a common interface for all RunnableWindows (and other
 * windows) which may be closed.  The purpose of this interface is to allow
 * plugin menus to integrate themselves with PCD's widget code.  Thus, a
 * customized window (made using the plugin menus) can work with CommandThread,
 * CommandButton, etc.</p>
 *
 * <p>An additional feature of this class is that it allows access to the JFrame
 * object corresponding to the parent window.  The purpose of this is to provide
 * modality to any child dialog windows created by the child widgets; however,
 * this JFrame access could possibly be used for other purposes in the future.
 * A good example of the 'getJFrame' method's utility is the AbstractFileChoser
 * class.  The AbstractFileChooser class creates a child file chooser dialog
 * window to allow the user to select a file(s); however, this window should be
 * modal (otherwise the window can get out of sync with the window enclosing
 * the menu parameters).  Thus, access to the JFrame object is essential for the
 * AbstractFileChooser class.</p>
 **
 * @author Graham Alvare
 * @author Brian Fristensky
 */
public interface CloseableWindow {
    /**
     * Action for command buttons to close the window.
     * This is used to close parameter windows when a
     * command is being executed.
     */
    public void close();

    /**
     * <p>This method returns a Component object, such as
     * a JFrame or JDialog object, which can be used as
     * a parent window object for modal child windows.</p>
     *
     * <p>See AbstractFileChooser for an example.</p>
     **
     * @return the Component object to be used as a parent for child windows.
     */
    public Component getJFrame();
}
