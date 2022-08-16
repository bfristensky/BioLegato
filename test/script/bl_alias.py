#!/usr/bin/env python3

'''
bl_alias.py - Given a series of paired-end read filenames, replace the leftmost common string with a specified string. 

Synopsis: bl_alias.py filename target [newname] 

    For each file whose name begins with target, create a symbolic link to target whose
    name begins with newname

    eg.if the target file is HI.4183.005.Index_12.450-cont-2_R1.fastq.gz

    then  bl_alias.py HI.4183.005.Index_12.450-cont-2_R1.fastq.gz HI.4183.005.Index_12.450 Zm450 

    will create a link named Zm405-cont-2_R1.fastq.gz, pointing to the target file. 

    If newname is not provided, the target string will simply be omitted from the name of the link.

    eg. bl_alias.py HI.4183.005.Index_12.450-cont-2_R1.fastq.gz HI.4183.005.Index_12.450

    would create a link named 450-cont-2_R1.fastq.gz
   

@modified: June 7, 2018
@author: Brian Fristensky
@contact: Brian.Fristensky@umanitoba.ca  
'''

"""
optparse is deprecated in favor of argparse as of Python 2.7. However,
 since 2.7 is not always present on many systems, at this writing,
 it is safer to stick with optparse for now. It should be easy
 to change later, since the syntax is very similar between argparse and optparse.
 from optparse import OptionParser
"""
from optparse import OptionParser

import os
import re
import stat
import subprocess
import sys

PROGRAM = "bl_alias.py : "
USAGE = "\n\tUSAGE: bl_alias.py target [newname]"

DEBUG = False
if DEBUG :
    print('bl_alias.py: Debugging mode on')

# - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
class Parameters:
    """
      	Wrapper class for command line parameters
      	"""
    def __init__(self):
        """
     	  Initializes arguments:
                IFN = ""
                TARGET = ""
                NEWNAME = ""

     	  Then calls read_args() to fill in their values from command line
          """
        self.IFN = "" 
        self.TARGET = ""
        self.NEWNAME = ""           
        self.read_args()


        if DEBUG :
            print('------------ Parameters from command line ------') 
            print('    IFN: ' + self.IFN)
            print('    TARGET: ' + self.TARGET)
            print('    NEWNAME: ' + self.NEWNAME)
            print()  

    def read_args(self):
        """
        	Read command line arguments into a Parameter object
    	"""                  
        self.IFN = sys.argv[1]
        self.TARGET = sys.argv[2]
        if len(sys.argv) > 3 :
            self.NEWNAME = sys.argv[3]


# - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
def MakeLink(IFN,TARGET,NEWNAME) :

    BN = os.path.basename(IFN)
    LinkName=BN.replace(TARGET,NEWNAME)
    if (not LinkName == BN) and (not os.path.exists(LinkName)) :
        os.symlink(BN,LinkName)
    

#======================== MAIN PROCEDURE ==========================
def main():
    """
        Called when not in documentation mode.
        """
	
    # Read parameters from command line
    P = Parameters()
                             
    MakeLink(P.IFN,P.TARGET,P.NEWNAME)      


if __name__ == "__main__":
    main()
#else:
    #used to generate documentation
#    import doctest
#    doctest.testmod()

#if (BM.documentor() or "-test" in sys.argv):
#    pass
#else:
#    main()
