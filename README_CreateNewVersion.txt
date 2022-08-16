NetBeans doesn't seem to have a mechanism for creating a 
new version of a project. In particular, the bioLegato
project has custom ant scripts for doing the builds, 
so you really have to copy the directory tree intact.

 The best way to do this by hand:

1. Make a copy of the existing directory tree under a new name.

cp -RPp bioLegato.1.0.5.1 bioLegato.1.0.6

This command preserves symbolic links. The main project directory
has links to the jarfiles that are created each time the project
is built.

2. Edit src/ants.properties to change the version number

Tools --> Templates --> Makefiles --> ants.properties

app.version=1.0.6

During a build, the ant script tells ant to replace variables in the source
code such as @VERSION@ with the values from this file. New .java files are
created in a number of build directories, and these are the java files
that actually get compiled.

3. Edit src/nbproject/project.xml to change version number

There are two lines where this needs to be changed. Both look like

            <name>BioLegato-1.0.6/name>

4. In Netbeans:

    File --> Open Project
    choose the src directory 
    Click on Open Project

    If you wish, you may also select Run --> Set Main Project

5. Build the project

    #The first time you build, choose Run --> Clean and Build

    If we do this  in going from 1.0.5.1 1.0.6 we get lots of  symbol
    not found errors referring to Scanner. For now, let's avoid Clean
    and just run 
    Run --> Build

    Ok, got Run --> Clean and Build to work after an iitial Run. Don't now why that happened.

6. In the java directory, change the symbolic link bioLegato to point to the
   new bioLegato directory.

7. To test, launch any bioLegato eg. birch. In the Help --> About menu, 
   you should see the new version number.


