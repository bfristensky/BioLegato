#!/usr/bin/env python3

'''
bldirtab.py - generate a listing of the current working directory as TSV file

Synopsis: bldirtab.py outfile [pattern]

     outfile - output file in tsv format
     pattern - If specified, only files matching the pattern will be in the output.
         Regular expressions must be quoted or escaped at the command line when using this option.
 
'''
import datetime
import fnmatch
import os
import sys

# - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
class FileMetaData:
    """
      	Holds metadata for a file.
      	"""
    def __init__(self):
        """
     	  Initializes arguments:
                Name = ""
                Size = 0
                MDate = datetime.time()
          """
        self.Name = ""
        self.Size = 0
        now = datetime.datetime.now()
        self.CurrentYear = now.year
        self.Mtime = now
        self.Ftype = "f"

    def getLocalMetaData(self,FN) :
        """
        Retrieve metadata for a local file.
        """
        if os.path.islink(FN) :
            finfo = os.lstat(FN)
            self.Ftype="l"
        else:
            finfo = os.stat(FN)
        if os.path.isdir(FN) :
            self.Ftype="d"
        self.Name = FN
        self.Size = int(finfo.st_size)
        timestamp = finfo.st_mtime
        self.Mtime = datetime.datetime.fromtimestamp(timestamp)
        #print(self.Name + ' ' + str(self.Size) + ' ' + str(self.Mtime) )

#======================== MAIN PROCEDURE ==========================

CWD=os.getcwd()
namelist = os.listdir(CWD)
namelist.sort()
TAB = '\t'
OFN=sys.argv[1]
if len(sys.argv) > 2 :
    PATTERN=sys.argv[2]
else :
    PATTERN="*"
OUTFILE = open(OFN,'w')
OUTFILE.write('#' + CWD + TAB + ' ' + TAB + ' ' + '\n')
OUTFILE.write('# Filename' + TAB + 'Size' + TAB + 'Date/Time' + TAB + 'Type' + '\n')
for filename in namelist :
    if fnmatch.fnmatch(filename,PATTERN) :
        F = FileMetaData()
        F.getLocalMetaData(filename)
        DateStr = F.Mtime.strftime("%Y-%m-%d %H:%M")
        OUTSTR = F.Name + TAB + str(F.Size) + TAB + DateStr + TAB + F.Ftype
        OUTFILE.write(OUTSTR + '\n')
OUTFILE.close()    
    
      



