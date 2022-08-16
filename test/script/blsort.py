#!/usr/bin/env python3

import os
import sys
import argparse
import os.path
import re
import shutil

'''
blsort.py - Sort a table from a csv or tsv file by columns
Synopsis: blsort.py infile outfile  [-cols comma-separated-list] [-descending] [-sep seperator]

  infile -  input file
  outfile - output file
  -cols <integer>[,<integer>] 
  -descending
  -sep - separator character to use when input is a csv file eg tab, comma
  
@modified: July 2, 2022
@author: Brian Fristensky
@contact: brian.fristensky@umanitoba.ca  
'''


PROGRAM = "blsort.py : "
USAGE = "\n    USAGE: blsort.py infile outfile [-cols <integer>[,<integer>]] [-descending] [-sep <seperator>]"

DEBUG = True

#--------------------------- Parameters -----------------------------
class Parameters:
    """
          Wrapper class for command line parameters
          """
    def __init__(self):
        """
           Initializes arguments:
             IFN=""
             COLS=[1]
                DESCENDING = False
                SEPERATOR = "TAB"
             OFN=""

           Then calls read_args() to fill in their values from command line
          """
        self.IFN = ""
        self.COLS = [1]
        self.DESCENDING = False
        self.SEPERATOR = "\t"
        self.OFN = ""
        self.read_args()

    def read_args(self):
        """
        Read command line arguments into a Parameter object

        """
        parser = argparse.ArgumentParser()
        parser.add_argument('infile', type=str, action="store", default="",
                          help="input file") 
        parser.add_argument('outfile', type=str,  action="store", default="",
                          help="output file") 
        parser.add_argument("-cols", action="store", default="",
                          help="comma-separated list of integers, listing columns")   
        parser.add_argument("-descending", action="store_true", default=False,
                          help="sort list indescending order")
        parser.add_argument('-sep', type=str, action="store", default="\t",
                          help="field seperator, eg. TAB or comma")        
        args = parser.parse_args()

        self.IFN = args.infile
        self.OFN = args.outfile
        colstring = args.cols
        tempcols = colstring.split(',')
        # cast list of columns into int list
        self.COLS = []
        for field in tempcols : 
            self.COLS.append(int(field))          
        self.DESCENDING = args.descending
        self.SEPERATOR = args.sep

        if DEBUG :
            print('INFILE: ' + self.IFN)
            print('OUTFILE: ' + self.OFN)
            print('COLS: ' + str(self.COLS))
            print('DESCENDING: ' + str(self.DESCENDING))
            print('SEPERATOR: ' + self.SEPERATOR)


#--------------------------- Table -----------------------------
class Table:
    """
          Implements a table as a list of lists. self.T is a list of rows.
          Each row is a list of fields (columns).
          """
    def __init__(self):
        """
        Has methods for reading, writing, and sorting a table
          """
        self.Header = []
        self.TUnsorted = [] # Original unsorted rows
        self.TCantSort = [] # Rows with empty fields in sort columns
        self.TSorted = [] # Rows after sorting
        self.TWidth = 0
        self.numrows = 0

    # - - - - - - - - - - - - - - - - -
    def read_table(self,IFN,COLS,SEP):
        """
        Read a table as a list of lines

        Convert each column to a type that can be sorted
        as the user might reasonably expect. 

        currency - not yet implemented
        date - not yet implemented  
        
        """

        # If a row has missing fields, or null fields, it is unsortable.
        def Sortable(temp) :
            Okay = True
            for c in COLS :
                if c > len(temp) :
                    Okay = False
                else :
                    if temp[c-1] == "" :
                        Okay = False
            return Okay       

        def RemoveUnsortable():
            while len(self.TUnsorted) > 0 :
                temp = self.TUnsorted.pop(0)
                if Sortable(temp) :
                    self.TSorted.append(temp)
                else :
                    self.TCantSort.append(temp)                    

        def ParseColumns() :

           # return true if column is float
           def testfloat(value) :
               result = False
               try :
                   F = float(value)
                   result = True
               except ValueError as ve :
                   pass
               return result

           # return true if column is int
           def testint(value) :
               result = False
               try :
                   F = int(value)
                   result = True
               except ValueError as ve :
                   pass
               return result

           # - - - - - - - - ParseColumns main - - - - - - - - -
           # For each column x, cast values into one of float, integer, or string 
           for x in range(0, self.TWidth) :

               # First, make sure that  ALL items in the column are integer
               # Test all rows in the column until something cannot be parsed as an integer
               ColumnType="integer"
               y = 0
               sortrows = len(self.TSorted)
               while (y < sortrows) and (ColumnType=="integer") :
                   if not testint(self.TSorted[y][x]) :
                       ColumnType = "other"
                   y += 1

               if ColumnType == "other" :
                   # See if ALL items in the column are float
                   ColumnType="float"
                   y = 0
                   while (y < sortrows) and (ColumnType=="float") :
                       if not testfloat(self.TSorted[y][x]) :
                           ColumnType = "other"
                       y += 1

               if ColumnType == "other" :
                   ColumnType = "string"  
  
               if DEBUG :
                   print('Column ' + str(x) + ': ' + ColumnType)

               #if not a string, convert all cells in the column into the appropriate type
               if ColumnType == "float" :
                   y = 0
                   for row in self.TSorted :
                       self.TSorted[y][x] = float(self.TSorted[y][x])
                       y += 1
               if ColumnType == "integer" :
                   y = 0
                   for row in self.TSorted :
                       self.TSorted[y][x] = int(self.TSorted[y][x])
                       y += 1

        # . . . . . . . . read_table main . . . . . . . 
        h_infile = open(IFN,'r')
        self.TWidth = 0
        for Line in h_infile:

            # Remove double quotes that enclose fields, if any
            # Also remove leading and trailing whitespace
            templine = Line.replace('"','').strip()

            # If a comment, add the original line to the header list.
            # Otherwise, add the parsed row to the table.
            # Empty lines in the input are ignored.
            if len(templine) > 0 :
                if templine.startswith('#') :
                    self.Header.append(templine)
                else:
                    row = templine.split(SEP)
                    self.TUnsorted.append(row)   
                    if len(row) > self.TWidth :
                        self.TWidth = len(row)  
        h_infile.close()
        self.numrows = len(self.TUnsorted)

        # Pad any rows that are less than the table width with empty fields.
        for i in range(0,self.numrows) :
            while len(self.TUnsorted[i]) < self.TWidth :
                self.TUnsorted[i].append("")

        RemoveUnsortable()
        #print(len(self.TUnsorted),len(self.TCantSort),len(self.TSorted))
        ParseColumns()


    # - - - - - - - - - - - - - - - - - - - - - -
    def sort_table(self,COLS,DESCENDING):
        """
        Sort a table using COLS as keys, in ascending or descending order

        """ 
        from operator import itemgetter  
     
        colitems = []
        for item in COLS :
            # list indices begin with 0, so we have to subtract 1
            colitems.append(item-1)

        self.TSorted = sorted(self.TSorted, key=itemgetter(*tuple(colitems)), reverse=DESCENDING)

    # - - - - - - - - - - - - - - - - - -
    def write_table(self,OFN,SEP,DESCENDING):
        """
        Write a table 

        """

        # Write the rows of a table to a file
        def write_rows(F,TBL,SEP) :
            for row in TBL :
                n = 0
                LENGTH = len(row)
                if n < LENGTH :
                    F.write(str(row[n]))
                    n = n + 1
                while n < LENGTH :
                    F.write(SEP + str(row[n]))
                    n = n + 1
                F.write('\n')   

        h_outfile = open(OFN,'w')

        # write out header lines
        for line in self.Header :
            h_outfile.write(line + '\n')

        # write out table lines
        if DESCENDING :
            write_rows(h_outfile,self.TSorted,SEP)
            write_rows(h_outfile,self.TCantSort,SEP)
        else :
            write_rows(h_outfile,self.TCantSort,SEP)
            write_rows(h_outfile,self.TSorted,SEP)
                    
        h_outfile.close()

#======================== MAIN PROCEDURE ==========================
def main():
    """
        Called when not in documentation mode.
        """
    # Read parameters from input file
    print("getting parameters")
    P = Parameters()    

    # Read table from infile
    TBL = Table()
    TBL.read_table(P.IFN,P.COLS,P.SEPERATOR) 

    # Sort table
    TBL.sort_table(P.COLS,P.DESCENDING)
    
    # Write sorted table to output file
    TBL.write_table(P.OFN,P.SEPERATOR,P.DESCENDING)  

if __name__ == "__main__":
    main()

