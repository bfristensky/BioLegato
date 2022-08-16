#!/usr/bin/env python3

'''
bltsv2name.py - strip out the filenames from a TSV file generated
by BioLegato. The filename is presumed to be the first field.
BioLegato adds double quotes to names, so we also strip those off.

Synopsis: bltsv2name.py infile outfile
 
'''

import os
import sys

#======================== MAIN PROCEDURE ==========================

IFN=sys.argv[1]
OFN=sys.argv[2]

INFILE = open(IFN,'r')
OUTFILE = open(OFN,'w')
TAB = '\t'
NL = '\n'
for line in INFILE.readlines() :
    if not line.startswith('#') : #eliminate comments
        outstr=line.strip()
        if len(outstr) > 0 : #eliminate empty lines
            outstr=outstr.split(TAB)[0].replace('"','')
            OUTFILE.write(outstr + NL)
INFILE.close()  
OUTFILE.close()  

  
    
      


