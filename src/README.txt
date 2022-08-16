================================================================================
BioLegato (version 1.0.0) README DOCUMENTATION FOR ADVANCED USERS
================================================================================

TABLE OF CONTENTS
-----------------
0. IMPORTANT FOREWORD
1. General Documentation
    1.1. OVERVIEW AND ORGANIZATION
    1.2. CREATING PROGRAM MENUS (BIOPCD EDITOR)
    1.3. COMMAND LINE PARAMETERS
    1.4. PROPERTIES
    1.5. ENVIRONMENT VARIABLES
2. Developer Documentation
    2.1. BIOLEGATO OVERVIEW
    2.2. CANVAS EXTENSIONS
    2.3. CREATING BIOPCD MENUS (ADVANCED)
    2.4. CUSTOM NON-BIOPCD MENUS
    2.5. CURRENT BIOLEGATO COMPONENT LIST:
    2.6. TURTLE SHELL
    2.7. JAVACC (.JJ) FILES
    2.8. ADDITIONAL CONSIDERATIONS


================================================================================
0. IMPORTANT FOREWORD
================================================================================

BEFORE YOU BEGIN ANY PROJECT WHICH INVOLVES INTERFACING WITH OR MODIFICATION OF
BIOLEGATO'S SOURCE CODE, PLEASE READ THE FOLLOWING DOCUMENTS:

    1.a) ALL RELEVANT JAVADOC CODE - FOR ANY CLASSES AND METHODS INVOLVED.
      b) ANY CROSS-REFERENCED MATERIAL IN THE RELEVANT JAVADOC CODE.
    2.   SECTIONS 2.1 AND 2.8 OF THIS DOCUMENT.
    3.   THE DEFAULT PROPERTIES FILE (Core/src/default.properties).
    4.   BIOLEGATO'S MANPAGE (Core/src/manpage.txt).


================================================================================
1. General Documentation
================================================================================

1.1. OVERVIEW AND ORGANIZATION
------------------------------
BioLeagto is a completely customizable graphical user interface.  BioLegato is
most notable as the main front-end for BIRCH (a collection of Bioinformatics
tools created and maintained primarily by Dr. Fristensky at the University of
Manitoba).

PLEASE READY SECTION ZERO (0) -- "IMPORTANT FOREWORD" -- BEFORE BEGINNING TO
READ THIS DOCUMENT ANY FURTHER!


1.2. CREATING PROGRAM MENUS (BIOPCD EDITOR)
-------------------------------------------
BioPCD menus (the regular menus used in BioLegato) may be created by using the
graphical "BioPCD editor" included with BioLegato.  The BioPCD editor is a
BioLegato canvas, which lets you drag and drop menu items to create BioLegato
menus.  To use the BioPCD editor, run: "java -jar biopcdedit.jar".  Note, you
may optionally add menu items to, and change the properties for this canvas.


1.3. COMMAND LINE PARAMETERS
----------------------------
Please see manpage.txt (located in the directory: Core/src) for a complete list
of all BioLegato commands.

You may also run: "java -jar image.jar --man" (or replace image.jar with any
other canvas JAR file) to generate the manpage document on-the-fly.

BioLegato command line parameters are handled by the class method:
    Core.src.org.biolegato.main.BLMain.main(Class<? extends DataCanvas> canvas,
                                            String[] args)


1.4. PROPERTIES
---------------------
Please see manpage.txt (located in the directory: Core/src) for a list of basic
BioLegato properties.

You may also run: "java -jar image.jar --man" (or replace image.jar with any
other canvas JAR file) to generate the manpage document on-the-fly.

Please see the file: Core/src/default.properties for a list of all BioLegato
properties (with descriptions) -- including experimental, deprecated, and
advanced properties.


1.5. ENVIRONMENT VARIABLES
--------------------------
Please see manpage.txt (located in the directory: Core/src) for a complete list
of all environment variables used by BioLegato.

You may also run: "java -jar image.jar --man" (or replace image.jar with any
other canvas JAR file) to generate the manpage document on-the-fly.

BioLegato environment variables are handled by the class method:
    Core.src.org.biolegato.main.BLMain.envreplace(String original)


================================================================================
2. Developers Documentation
================================================================================

2.1. BIOLEGATO OVERVIEW
-----------------------
BioLegato was written as a set of modules which work together to produce a GUI.
While this may sound very complicated, the code is actually fairly simple and
straightforward.

When you launch BioLegato, you are actually running a canvas extension JAR file.
These JAR files interface with BioLegato's core, which provides menus to the
canvas, and a common interface for running files.  These menus are provided in
one of two ways: (1) through the BioPCD parser module (easy to use); (2) through
executable menu plugins (rarely used, but provide the greatest flexibility).

There are only types of extensions currently supported by BioLegato:
    1. Canvases
    2. Custom menus (non-BioPCD)

Both of these extension types will be discussed with thorough detail in other
sections of this README document.

Please note that this project also contains JavaCC files, which compile into
Java code.  JavaCC files are denoted by the extension .jj, and require special
consideration (provided in their own section)


2.2. CANVAS EXTENSIONS
----------------------
All of the interfacing with the user is done through the Datacanvas interface.
This interface is located in:  org.biolegato.main

Please be sure to also examine the "default.properties" file before writing any
extension in BioLegato, as certain properties should be supported by most, if
not all canvases in BioLegato.

All DataCanvas extensions should contain a "main" method of the following
specification:  public static void main (String[] args); this method should load
BLMain.main(class, args), where class is the identity of the class to load.  For
example, if you wrote an extension called ImageCanvas, you would write the
following main method:

    public static void main (String[] args) {
        BLMain.main(ImageCanvas.class, args);
    }

This is required to load the canvas from the JAR file.

In addition all DataCanvas extension MUST contain the following constructor
methods:
    1. An empty constructor which requires no arguments/parameters:
            e.g. ImageCanvas()

    2. A constructor which accepts a map of properties (the properties should be
       passed up to the super class):
            e.g. ImageCanvas(Map<? extends Object, ? extends Object> properties)

These constructor methods will be called as necessary by BLMain to generate and
display the new BioLegato canvas.

Please note that every DataCanvas object MUST implement the PCDIO interface.
The PCDIO interface is used for providing a streamlined method for the canvas to
interact with menus.


2.3. CREATING BIOPCD MENUS (ADVANCED)
-------------------------------------
Please see the format definition of the BioPCD language in the Javadocs for a
complete grammatical description of BioPCD:  org.biopcd.parser.PCD

There are currently two ways for loading menu items, through BioPCD, into
BioLegato:  In the first way, BioPCD files may be loaded directly into
BioLegato.  This first way is the standard, and most common method for loading
menus in BioLegato.  Menu files may be loaded into BioLegato using the
"pcd.menus.path" parameter.

The second method for loading menus, through BioPCD into BioLegato, is by
executing a shell command, then piping the input into the parser.  This may be
done by setting the "pcd.exec" parameter to the command to read the BioPCD menus
from.  Only the input stream (stdin) will be piped into the BioPCD parser (the
stderr stream will be ignored).  This code is considered to be in the beta stage
of development, thus it has been tested but may still contain some bugs.

Please also be sure to consider reading the JavaCC section, as BioPCD utilizes
JavaCC for much of its parsing.


2.4. CUSTOM NON-BIOPCD MENUS
----------------------------
Custom non-BioPCD menus may be created using the JMenu and JMenuItem interfaces.
To be loaded by BioLegato, all custom non-BioPCD menus MUST comply with one of
the below options, or be loaded through the code within one of the two options
(i.e. a JMenuItem loaded through the JMenu in option 2):

    Option 1:
        1. Be in a .class file.
        2. Extend the JMenuItem class.
        3. Contain a constructor which accepts a DataCanvas object as its only
           parameter (this is the constructor called when loading the class).

    Option 2:
        1. Be in a .jar file.
        2. Extend the JMenu class.
        3. Contain a constructor which accepts no parameters (this is the
           constructor called when loading the class).

NOTE: If you use option 2, you may load JMenuItems in any way desired through
      your JMenu class.

If a menu follows all of the above rules, it should be compatible with
BioLegato; however, it is always best to confirm this by testing the menu
thoroughly with the canvas it is intended for.  To do this, just put the class
or JAR files in the PCD menus directory.  This directory is set by the BioLegato
parameter: "pcd.menus.path".


2.5. CURRENT BIOLEGATO COMPONENT LIST
-------------------------------------
All components are listed by their relative path from the base BioLegato
directory.  (NOTE: folder and directory are used interchangeably throughout all
documentation, as are the word pairs: function/method, and boolean/flag).

The current version of BioLegato contains the following standard modules:

    1. Core     -- The main BioLegato core.  This body of code integrates all of
                   the appropriate modules together to display a canvas.  This
                   is possibly the last place a developer will want to make
                   major changes to; however, this is the first place to look,
                   for documentation and information, when building an extension
                   for BioLegato.  This is where all extensions interface with.

    2. BioPCD   -- This is a separate module, which parses BioLegato's
                   BioPCD menu format.  The BioPCD parser was separated
                   from the Core to allow anyone interested to use BioPCD
                   in applications other than BioLegato.  Such use is
                   currently unknown.

                   To separate out the BioPCD parser, just copy it to any
                   directory, and change the following line in it's build.xml
                   file:

                        <pathelement location="../lib/ant-contrib.jar" />

                   to point to a valid ant-contrib.jar file (NOTE: you can find
                   a valid ant-contrib.jar file in BioLegato's lib directory!)

    3. Canvases -- The standard extensions/canvases included with every
                   BioLegato installation; these are:

                       a) BioPCDEditor
                       b) Sequence
                       c) Images
                       d) Tables
                       e) biopcd

In addition to modules, a basic installation of BioLegato contains the following
tool:

    1. BioPCD/GDE2PCD  -- A tool to convert GDE menus to BioPCD menus compatible
                          with BioLegato versions after 0.7.7 (the last version
                          to formally support GDE menus).  This code was used
                          to convert the entire BIRCH GDE framework into BioPCD
                          menus.  Hence, this tool is considered thoroughly
                          tested, and completely stable.  Thus, modifications to
                          the code (except bug fixes) should be avoided.

Also, the current version of BioLegato contains two experimental bodies of code.
These will be described below:

    1. Turtle         -- This directory contains a beta-version of Turtle SHELL.
                         Turtle SHELL is a UNIX command shell written by Graham
                         Alvare under the supervision of Dr. Brian Fristensky.

                         This has its own section below because it is fairly
                         close to completion and should not be discarded.

    2. Canvases/dbpcd -- This directory contains a pre-alpha prototype of a
                         BioLegato database canvas; as such, this code is not
                         well supported or documented, and may either be
                         scavenged, salvaged, or scrapped in future versions,
                         without notice.


2.6. TURTLE SHELL
-----------------
The Turtle SHELL command shell interface was designed to help ensure a
consistent and universal language for specifying basic commands across multiple
platforms.

Turtle SHELL supports some commands (such as CAT, TR, etc.) intrinsically
(sometimes with a little parameter modification -- see GREP), while maintaining
the ability to run commands externally.

Turtle SHELL was developed with Windows in mind.  Currently, BIRCH (which
BioLegato is the main GUI for) works on many UNIX based systems (Mac OS X,
Linux, Solaris); however, because all of the framework and BioPCD menus are
written to run shell commands, Windows support is not yet possible, without
major revisions.

There are two parts to making BIRCH run on Windows ("WinBIRCH"):
    1. Make Python scripts for all of the BIRCH
       framework shell scripts (mostly done).
    2. Make a universal language for handling complex
       BioPCD commands (run by Command thread).

NOTE: Currently, BioPCD commands are run through the UNIX shell /bin/sh.

Please note that none of the Javadoc documentation in Turtle SHELL is formatted
for HTML.  The documentation is intended to be read in Netbeans, or any
non-wrapping text editor.

Turtle SHELL mostly works (with intrinsic commands disabled); however,
there are some bugs which need to be fixed before it is completely ready to be
included as a documented feature.

Although the intrinsic commands are disabled (to make debugging simpler), all
of the currently implemented intrinsic commands should work with only some minor
modifications and debugging.  In addition, many of the common UNIX shell
commands have already been ported over to Turtle SHELL
(see the Turtle/src/org/turtleshell/cmd directory for a full list of commands
currently supported by Turtle SHELL).

    TO ENABLE TURTLESHELL:
    ----------------------
    1. Uncomment the relevant lines in:
         BioPCD/parser/src/org/biopcd/parser/CommandThread.java
         Core/src/org/biolegato/main/BLMain.java
    2. Add the Turtle SHELL to the "build.list" parameter in in ./build.xml
       (BioLegato's main Apache Ant build script -- in the root BioLegato source
       directory).  This may be done as follows:
            - Suppose there is the following on the "build.list" line:
                    <property name="build.list"
                              value="BioPCD,Core,Canvases" />
            - To add Turtle SHELL, add the text "Turtle," before all other
              entries in the value:
                    <property name="build.list"
                              value="Turtle,BioPCD,Core,Canvases" />
    3. DONE!

When Turtle SHELL works properly, without bugs, when running external commands,
you can enable internal/intrinsic command support by uncommenting all of the
"put" lines in the 'internalCommands' hashtable.  (These lines are commented
using the /* */ Java comment tags.)

The basic procedure for adding intrinsic commands to Turtle SHELL is as follows:

    1. Create a Java file for the new command (see
       Turtle/src/org/turtleshell/cmd for a list of examples).  It MUST contain
       the method: public static int main(String[] args);

    2. Put the Java file in Turtle/src/org/turtleshell/cmd

    3. Create a manpage file similar to any file in Turtle/src/manpages

    4. Put the manpage file in Turtle/src/manpages

    5. Add your command to the hashtable 'internalCommands' in the file:
       Turtle/src/org/turtleshell/tasks

    6. DONE!

Please also pay attention to the JavaCC part of this document, as Turtle SHELL
does utilize JavaCC for much of its parsing.


2.7. JJ FILES
-------------
All of the .jj files are JavaCC grammar files.  These files are parsed by the
JavaCC compiler-compiler.  Documentation for JavaCC is available at:
            http://www.engr.mun.ca/~theo/JavaCC-FAQ/

In addition, I recommend the following book on JavaCC:
     "Generating Parsers with JavaCC" by Thomas Copeland

JavaCC allows specification of a grammar parser with embedded semantic Java
code.  A JavaCC's .jj file contains both the parser grammar and and semantic
Java code together.  When a JavaCC file is compiled, it will generate some Java
classes, which will handle the grammar.


2.8. ADDITIONAL CONSIDERATIONS
------------------------------
Before beginning any development project, always be sure to check the properties
files and see the examples.  If your code or idea deviates significantly, it
will be very beneficial to do some minor work (either adding features or doing
some minor debugging) with BioLegato first!  Also, always be sure to read the
Javadoc.  With the exception of Turtle SHELL, all of the Javadoc is written to
be viewed in an HTML browser.

To view the Javadoc, run the command "ant docs" from the terminal - OR - click
"Run > Generate Javadoc" in the menu bar of Netbeans.  Then, open the Javadoc
documentation in the biolegato/docs directory.

Most of the text files have been formatted to 80 characters/columns wide:

12345678901234567890123456789012345678901234567890123456789012345678901234567890
         1         2         3         4         5         6         7         8

The purpose of this formatting is to facilitate printing any of the files in
BioLegato to a terminal or to a printer (i.e. wrapping Java code can look very
strange and make reading more difficult).

There has been some experimental work making BioLegato compatible with Java's
Applet framework.  Test HTML files, for use with appletviewer, are available in
the "applet_work" directory.  Currently, BioLegato does NOT run as an Applet.


============================== END OF README FILE ==============================
