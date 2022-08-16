#!/bin/bash

# For each file in SOURCEDIR, create a symbolic
# link in the current directory that points to that file

# Synopsis makelinks.sh sourcedir [pattern]

#Note: for most purposes, it is best to specify sourcedir as a
# relative path, rather than as a fully-qualified path.
# That way, when a source directory and the current directory are
# part of a relocatable file tree, the entire tree can be
# copied to a new system without changing the links.

#If the symbolic link already exists, it is removed, and a new link
# created in its place.

#Pattern is a regular expression. It is NOT evaluated by globbing.
# Thus, if you want to include characters that have a meaning in
# regular expressions, you must escape them.
# example: makelinks.sh ../..  \.param will match all files ending in 
# '.param', whereas ../..  .param will match all files whose names are
# as single character, followed by 'param'.

case "$#" in 
    0) echo 'Usage: makelinks.sh sourcedir [pattern]'
       ;;
    1) SOURCEDIR=$1
       PATTERN=""
       ;;
    *) SOURCEDIR=$1
       PATTERN=$2
       ;;
esac

echo 'PATTERN: ' $PATTERN

if [ "${PATTERN}" = "" ]
then
    FILELIST=`ls -1 "$SOURCEDIR"`    
else
    FILELIST=`ls -1 "$SOURCEDIR"/$PATTERN`
fi

for FILE in $FILELIST
   do
      FN=`basename $FILE`
      echo $FN
      if [ -L $FN ]
      then
          rm $FN
      fi
      ln -s "$SOURCEDIR/$FN"
done
