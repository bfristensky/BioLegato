#!/usr/bin/env python3

'''
bl_rename.py - Given a series of filenames, replace the target string with a new string. 

Synopsis: bl_rename.py --replace oldpattern  newpattern file [file]...
          bl_rename.py --noblanks file [file]...
          bl_rename.py --chblanks newpattern file [file]...

    Rename files by substituting a new string in place of a target string.

EXAMPLE

    Given the following files in the current working directory:

    testfile1.text
    testfile2.text
    testfile3.text
    testfile4.text

    bl_rename.py text txt *

    changes the names to

    testfile1.txt
    testfile2.txt
    testfile3.txt
    testfile4.txt

    bl_rename.py ile '' *

    changes the names to

    testf1.txt
    testf2.txt
    testf3.txt
    testf4.txt    
   

@modified: March 8, 2021
@author: Brian Fristensky
@contact: Brian.Fristensky@umanitoba.ca  
'''

import argparse
import os
import re
import sys

PROGRAM = "bl_rename.py : "
USAGE = "\n\tUSAGE: bl_rename.py target [newname]"

DEBUG = True
if DEBUG :
    print('bl_rename.py: Debugging mode on')

# - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
class Parameters:
    """
      	Wrapper class for command line parameters
      	"""
    def __init__(self):
        """
     	  Initializes arguments:
                OLDPAT = ""
                NEWPAT = ""
                FILES = []

     	  Then calls read_args() to fill in their values from command line
          """
        self.TASK = "replace"
        self.OLDPAT = "" 
        self.NEWPAT = "" 
        self.FILES = []          
        self.read_args()

        if DEBUG :
            print('------------ Parameters from command line ------') 
            print('    TASK: ' + self.TASK)
            print('    OLDPAT: ' + self.OLDPAT)
            print('    NEWPAT: ' + self.NEWPAT)
            print('    FILES: ' + str(self.FILES))
            print()  

    def unquote(self, S):
        """
            Remove leading and trailing quotes from a string
            @param STR: The string to clean up
            @type STR: str
            """

        if not S == "" : 
            if S.startswith('"') :
                S = S.replace('"', '')
            else:
                S = S.replace("'", "")
        return S

    def read_args(self):
        """
        	Read command line arguments into a Parameter object
    	"""                  

        parser = argparse.ArgumentParser()
        parser.add_argument("--replace", nargs=2)
        parser.add_argument("--chblanks", nargs=1)
        parser.add_argument("--noblanks", action="store_true")
        parser.add_argument("--camel", action="store_true")
        parser.add_argument("files")

        args = parser.parse_args()

        self.FILES = []
        infile = open(args.files,"r")
        names = infile.readlines()
        infile.close()
        for f in names :
            print(f)
            self.FILES.append(self.unquote(f.strip()))           

        try :
            if args.replace :
                self.TASK = "replace"
                self.OLDPAT = args.replace[0]
                self.NEWPAT = args.replace[1]
            elif args.chblanks :
                self.TASK = "chblanks"
                self.OLDPAT = " "
                self.NEWPAT = args.chblanks[0]
            elif args.noblanks :
                self.TASK = "noblanks"
                self.OLDPAT = " "
                self.NEWPAT = ""
            else :
                self.TASK = "camel"
        except ValueError:
            print(USAGE)

# - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
def ToCamel(S) :
    """
    Given a string containing blanks, break into tokens, convert individual tokens to
    camel case, and return a camelcase string without blanks.
    Camel case represents several words in a single string by capitalizing the
    first letter of each wors eg. ThisIsAnExampleOfCamelCase
    """
    Words = S.split(" ")
    cs = ""
    for w in Words :
        cs = cs + w[0].upper() + w[1:].lower() 
    return cs

# - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
def ReName(TASK,OLDPAT,NEWPAT,FILES) :

    for FN in FILES :
        if TASK == "camel" :
            NewFN = ToCamel(FN)
        else :
            NewFN = FN.replace(OLDPAT,NEWPAT)
        os.rename(FN,NewFN)  

#======================== MAIN PROCEDURE ==========================
def main():
    """
        Called when not in documentation mode.
        """

    # Read parameters from command line
    P = Parameters()
                             
    ReName(P.TASK,P.OLDPAT,P.NEWPAT,P.FILES)      

if ("-test" in sys.argv):
    pass
else:
    main()


