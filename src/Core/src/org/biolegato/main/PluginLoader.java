/*
 * Plugin.java
 *
 * Created on October 22, 2008, 1:56 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */
package org.biolegato.main;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Enumeration;
import java.util.Map;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * This class is used to handle plugins.
 **
 * @author Graham Alvare
 * @author Brian Fristensky
 */
public class PluginLoader {

    /**
     * Loads an the plugins into BioLegato.
     **
     * @param pluginHash The hash map to store the loaded plugins
     * @param directory  The directory to load the plugins from
     */
    public static void loadPlugins(Map<String, PluginWrapper> pluginHash,
            String directory) {
        // The name of the current class being loaded.
        String className = null;
        // The current directory to load from.
        File pluginDirectory = new File(directory);

        // Check that the directory exists, and iterate through its files.
        if (pluginDirectory.exists() && pluginDirectory.isDirectory()) {
            for (File file : pluginDirectory.listFiles()) {
                try {
                    // Branch based on whether the current entry is a class
                    // file, a jar file or a directory.
                    if (file.exists() && file.canRead() && file.isFile()
                            && file.getName().toLowerCase().endsWith(
                                ".class")) {
                        // If the entry is a class file, read the file's
                        // comprising classes.  Obtain the class name by
                        // removing the extension from the filename
                        className = file.getName().substring(0,
                                file.getName().length() - 6);
                        loadClasses(pluginHash,
                                pluginDirectory.toURI().toURL(), className);
                    } else if (file.exists() && file.canRead() && file.isFile()
                            && file.getName().endsWith(".jar")) {
                        // if the entry is a jar file, read the file's
                        // contained classes.
                        loadJar(pluginHash, file);
                    } else if (file.exists() && file.canRead()
                            && file.isDirectory()) {
                        // if the entry is a directory, traverse the
                        // directory for files.
                        loadPlugins(pluginHash, file.getAbsolutePath());
                    }
                } catch (Throwable th) {
                    System.err.println(
                            "Plugin Loader - Error loading the file: "
                            + file.getAbsolutePath());
                    th.printStackTrace(System.err);
                }
            }
        }
    }

    /**
     * Loads all plugin classes within a jar file.
     **
     * @param pluginHash  The hash map to store the loaded plugins.
     * @param file        The jar file to read classes from.
     */
    public static void loadJar(Map<String, PluginWrapper> pluginHash,
            File file) throws IOException {
        // The current list of entries in the jar file.
        Enumeration<JarEntry> entries = null;
        // The jar file object to read.
        JarFile jarfile = new JarFile(file);
        // The current entry being parsed.
        JarEntry entry = null;
        // The name of the current class.
        String className = "";

        entries = jarfile.entries();
        while (entries.hasMoreElements()) {
            entry = entries.nextElement();
            if (!entry.isDirectory() && entry.getName().toLowerCase().endsWith(
                    ".class")) {
                // get the class name by removing the extension from
                // the filename
                className = entry.getName().substring(0,
                        entry.getName().length() - 6);
                className = className.replace("/", ".");
                loadClasses(pluginHash, file.toURI().toURL(), className);
            }
        }
    }

    /**
     * Loads a class and its subclasses for a given file.
     **
     * @param pluginHash The destined hashtable containing all of the
     *                   plugins loaded in BioLegato.
     * @param url        The url of the parent directory of the file.
     * @param className  The name of the class to load.
     */
    public static void loadClasses(Map<String, PluginWrapper> pluginHash,
            URL url, String className) {
        PluginWrapper currentClass = loadClass(url, className);

        // load the class
        if (currentClass != null) {
            // insert the class into the hashtable
            pluginHash.put(className, currentClass);

            // load any subclasses
            for (Class c : currentClass.getDeclaredClasses()) {
                currentClass = loadClass(url, c.getName());
                if (currentClass != null) {
                    pluginHash.put(c.getName(), currentClass);
                }
            }
        }
    }

    /**
     * Loads a class and its subclasses for a given url.
     **
     * @param url the url to load the classes in
     * @param name the name of the class to load
     * @return the wrapped loaded plugin object
     */
    public static PluginWrapper loadClass(URL url, String name) {
        PluginWrapper result = null;
        try {

            // load the class
            Class currentClass
                    = new URLClassLoader(new URL[] {url}).loadClass(name);

            // insert the class into the hashtable
            result = new PluginWrapper(name, currentClass);
        } catch (Throwable th) {
            // on failure print an error message
            System.err.println("Plugin Loader - Error loading the class: "
                    + url.getPath() + " (" + name + ")");
            th.printStackTrace(System.err);
        }
        return result;
    }
}
