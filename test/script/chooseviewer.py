#!/usr/bin/env python3

#optparse is deprecated in favor of argparse as of Python 2.7. However,
# since 2.7 is not always present on many systems, at this writing,
# it is safer to stick with optparse for now. It should be easy
# to change later, since the syntax is very similar between argparse and optparse.
from optparse import OptionParser

import browser
import re
import os
import os.path
import shutil
import sys
import subprocess
import time


'''
chooseviewer.py - choose a program for viewing a file, based either on the filename.extension, or
by an extension supplied on the command line. If --delete is set, delete the file after viewing.

The first arugment is a filename or URL. Usually, we can figure out
the type of file based on the file extension. However, if a second
argument is given, it is assumed to be a typical file extension
such as pdf, html, jpg etc. This is mainly for the case in which
a file doesn't have an extension, and we need to be able to tell
chooseviewer the type of file.

Synopsis: chooseviewer.py <filename> [--ext extension][--delete] [--wait] 

@modified: July  1, 2022
@author: Brian Fristensky
@contact: frist@cc.umanitoba.ca  
'''


PROGRAM = "chooseviewer.py : "
USAGE = "\n\tUSAGE: chooseviewer.py <filename> [--ext extension][--delete]"

DEBUG = True
if DEBUG :
    print('Debugging mode on')


# - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
class Parameters:
    """
      	Wrapper class for command line parameters
      	"""
    def __init__(self):
        """
     	  Initializes arguments:
		Target = ""
                Ext = ""
                Delete = False
                Wait = False

     	  Then calls read_args() to fill in their values from command line
          """
        self.Target = sys.argv[1]
        self.Ext  = ""
        self.Delete  = False
        self.Wait = False 
        self.read_args()
        

        if DEBUG :
            print('------------ Parameters from command line ------')
            print('    Target: ' + self.Target)
            print('    Ext: ' + self.Ext)
            print('    Delete: ' + str(self.Delete))
            print('    Wait: ' + str(self.Wait))
            print()  

    def read_args(self):
        """
        	Read command line arguments into a Parameter object
    	"""          
        parser = OptionParser()
        parser.add_option("--ext", dest="ext", action="store", default="", help="file type")
        parser.add_option("--delete", dest="delete", action="store_true", default=False, help="delete file after viewing")
        parser.add_option("--wait", dest="wait", action="store_true", default=False, help="wait for viewer to terminate before terminating chooseviewer.py")
        (options, args) = parser.parse_args() 
        self.Ext = options.ext
        self.Delete = options.delete
        self.Wait = options.wait

#---------------------------------------------------------------
def LaunchViewer(FileName,Ext,Delete,Wait) :

    # Get rid of a leading period from a file extension. This means it doesn't
    # matter whether or not sys.argv[2]  begins with a dot.
    if Ext[0] == '.' :
        Ext = Ext[1:]

    # Some programs can't figure out how to open a file if it doesn't have the right file 
    # extension, so we make a duplicate file with a recognizable extension, and work with
    # that.
    if Ext != "Web" :
        Target = 'chooseviewer' + str(os.getpid()) + "." + Ext
        shutil.copy(FileName,Target)
    else:
        Target = FileName

    if DEBUG :
        print ("chooseviewer.py - Opening file: " + Target)

    #Most of these external calls have been switched over to subprocess, from birchscript.forkrun.
    # What seems to happen is that birchscript.forkrun runs in the background, allowing chooseviewer.py to 
    # terminate. This means that if the script that called chooseviewer deletes the input file as its
    # next step, that file will be deleted before the ace* scripts have time to make a copy. 
    # We probably also need a similar fix for web pages, but that would require a separate script
    # that opened a web browser.

    if Ext in ("Web", "html", "shtml", "htm"):
        # -------- Web pages
        #browser.forkbrowser(Target)
        Program = os.environ.get("BL_Browser")
            
    elif Ext == "pdf":
        # -------- PDF, PostScript
        Program = os.environ.get("BL_PDFViewer")

    elif Ext in ("ps", "eps"):
        Program = os.environ.get("BL_PSViewer")

    elif Ext in ("xls", "xlsx", "ods", "sxc", "csv", "tsv", "dif", "dbf","ctab"):
        # -------- Spreadsheet
        Program = os.environ.get("BL_Spreadsheet")

    elif Ext in ("odt", "sxw", "doc", "docx", "rtf"):
        # -------- Document
        Program = os.environ.get("BL_Document")

    elif Ext in ("gif", "jpg", "jpeg", "png", "bmp", "tif", "tiff"):
        # -------- Bitmap graphics
        Program = os.environ.get("BL_ImageViewer")

    else:
        # -------- Default is text editor
        Program = os.environ.get("BL_TextEditor")

    """    
    Treat the Program variable as a list of tokens 
    We do it this way to accommodate MacOSX, which launches 
    viewers using the open command.
    For example, to open a file using firefox

    open -a firefox filename

    """
    comstr = Program.split() #Break command into a list of tokens
    comstr.append(Target)

    if Wait :
        p = subprocess.Popen(comstr)
        p.wait()
        os.remove(Target)
    else:
        # I tried lots of permutations of subprocess, but was never able to get the
        # browser to open, and wait, and then delete the file, except by using os.system.
        #p = subprocess.Popen(comstr)
        #time.sleep(20)
        #os.remove(Target)
        COMMAND = '(' + Program + ' ' + Target + '; sleep 120; rm ' + Target + ')&'
        #p = subprocess.run(COMMAND, shell=True)
        os.system(COMMAND)
        

    # Remove the original, except if it is an external URL
    if (Delete and (not Ext == "Web")) :
        if DEBUG :
            print ('Deleting ' + FileName)
        time.sleep(5)
        os.remove(FileName)    

#======================== MAIN PROCEDURE ==========================

def chooseviewer():
    """
    	Called when not in documentation mode.
    	"""
    print ('Running chooseviewer.py')
    # Read parameters from command line
    P = Parameters()

    OKAY = True
    # If the Target is an external web page, we set Ext to Web. 
    # Launchviewer will not try to delete an external web page.
    if (re.search('^http:|^https:|^ftp:|^ftps:', P.Target)):
        P.Ext = "Web"

    # This is for files on the local filesystem.
    else :    
        if (os.path.isdir(P.Target)):
            OKAY = False
            print( "chooseviewer.py: cannot open directories!")
        elif (not os.path.exists(P.Target)):
            OKAY = False
            print ('chooseviewer.py: file not found')    

        if OKAY :
            if P.Ext == "" :
                P.Ext=os.path.splitext(P.Target)[1]
            P.Ext = P.Ext.lower()   
    if OKAY :
        if DEBUG :
            print('------------ Final Parameter Values ------')
            print('    Target: ' + P.Target)
            print('    Ext: ' + P.Ext)
            print('    Delete: ' + str(P.Delete)) 
            print('    Wait: ' + str(P.Wait)) 
        LaunchViewer(P.Target,P.Ext,P.Delete,P.Wait)
	
if __name__=="__main__":    
        chooseviewer()

