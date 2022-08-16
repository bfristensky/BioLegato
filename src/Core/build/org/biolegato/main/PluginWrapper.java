/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.biolegato.main;

import java.lang.reflect.Constructor;

/**
 * This class is used to wrap plugins.  This makes accessing the class
 * represented by the plugin easier.
 **
 * @author Graham Alvare
 * @author Brian Fristensky
 */
public class PluginWrapper {
    /**
     * The name of the class wrapped by the plugin wrapper class.
     */
    private String pluginName;
    /**
     * The class instance of the plugin.
     * This is used for all of the funcionality of the plugin
     */
    private Class<?> pluginClass;

    /**
     * Creates a new instance of the plugin wrapper class.
     **
     * @param pluginName the name of the plugin
     * @param pluginClass the instance of the plugin
     */
    public PluginWrapper (String pluginName, Class<?> pluginClass) {
        this.pluginName = pluginName;
        this.pluginClass = pluginClass;
    }

    /**
     * Creates a new object the plugin class.
     * Alias for create(new Class[]{}, new Object[]{});
     **
     * @return  the instance of the object of the type of plugin
     */
    public Object create () {
        return this.create(new Class[0], new Object[0]);
    }

    /**
     * Creates a new object the wrapped plugin class
     * (returns null if unsuccessful).
     **
     * @param classes the class types to use when searching for the constructor
     * @param data the data to pass to the plugin's class constructor
     * @return the instance of the object of the type of plugin
     */
    public Object create (Class[] classes, Object[] data) {
        Object result = null;	// the object to return.
	
        try {
	    // create the new instance

            result = pluginClass.getConstructor(classes).newInstance(data);
        } catch (Throwable cex) {
	    // print error messages if an error occurs
            try {
                System.err.println("Plugin Wrapper (plugin '" + pluginName
                        + "') - Constructor error: "
                        + pluginClass.getConstructor(classes));
            } catch (Throwable pex) {
                System.err.println("Plugin Wrapper (plugin '" + pluginName
                        + "') - Invalid plugin");
            }
            cex.printStackTrace(System.err);
        }
        return result;
    }

    /**
     * Invokes a static method from the plugin.
     * Alias for: smethod(name, new Class[]{}, new Object[]{});
     **
     * @param name the name of the method
     * @return the result of the method
     */
    public Object smethod (String name) {
        return smethod(name, new Class[]{}, new Object[]{});
    }

    /**
     * Invokes a static method from the plugin (returns null if not successful).
     **
     * @param name the name of the method
     * @param classes the classes for the parameters
     * @param parameters the parameters to use
     * @return the result of the method
     */
    public Object smethod (String name, Class[] classes, Object[] parameters) {
        Object result = null;	// the resulting object
	
        try {
	    // calls the static method of the object.
            result = pluginClass.getMethod(name, classes).invoke(null,
                    parameters);
        } catch (Throwable th) {
	    // print error messages if the method fails.
            System.err.println("Plugin Wrapper (plugin '" + pluginName
                    + "') - Invalid static method: " + name);
            th.printStackTrace(System.err);
        }
        return result;
    }

    /**
     * Used to test inheritance for the plugin class.
     **
     * @param test the class to test inheritance of
     * @return the result of the test
     */
    public boolean isA (Class<?> test) {
        return test.isAssignableFrom(pluginClass);
    }

    /**
     * Returns the class's name.
     **
     * @return the name of the plugin
     */
    public String getName () {
        return pluginName;
    }

    /**
     * Wrapper method for getDeclaredClasses -
     * returns every class declared by the wrapped class
     **
     * @return an array of all of the declared classes
     */
    public Class<?>[] getDeclaredClasses() {
        return pluginClass.getDeclaredClasses();
    }

    /**
     * Confirms whether the class represented by this
     * plugin wrapper object contains a specific constructor
     **
     * @param  testClasses  The classes of the objects which can be passed
     *                      to the constructor.
     * @return whether      The class wrapped by this plugin wrapper contains
     *                      the specific constructor.
     */
    public boolean containsConstructor(Class[] testClasses) {
        boolean result = false;
        Constructor<?>[] constructors = pluginClass.getConstructors();

        // iterate through every constuctor, or until we find a match
        // (whichever comes first)
        for (int countConstruct = 0; countConstruct < constructors.length
                && ! result; countConstruct++) {
            Class<?>[] cclass
                    = constructors[countConstruct].getParameterTypes();

            // see if the current constructor has the same number of arguments
            if (cclass.length == testClasses.length) {
                boolean failure = false;
                
                // check each argument, and see if it is the same
                for (int countclass = 0; countclass < cclass.length
                        && ! failure; countclass++) {
                    failure = (!cclass[countclass].isAssignableFrom(
                            testClasses[countclass]));
                }
                result = !failure;
            }
        }
        return result;
    }
}
