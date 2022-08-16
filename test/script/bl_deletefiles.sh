#!/bin/bash

# For each file in FILELIST, compress the file with the appropriate
# compression tool, based on the file extension EXTENSION.

# Synopsis bl_deletefiles.sh filelist

case $# in 
    0) 
       echo 'Usage: bl_deletefiles.sh filelist'
       exit
       ;;
    1) 
       FILELIST=$1
       ;;
esac

# Don't delete any files if none are selected. The problem is that the
# BioLegato Table canvas selects all files if none are selected. This is a feature.
# We can circumvent that by checking for a pseudo-comment beginning '#' on the first line.
# Files are not deleted if this is found.
FILES=(`cat $FILELIST`)
FIRSTFILE=${FILES[0]}
if [[ ${FIRSTFILE:0:1} = "#" ]]
then
    MESSAGE='No files selected. Delete canceled.'
    java -jar $BIRCH/script/OkayBox2.jar "$MESSAGE"
else
    MESSAGE='Do you wish to delete PERMANENTLY the following files or directories? '
    FSTR=`cat $FILELIST | sed -e 's/$//' `
    MESSAGE=$MESSAGE$'\n'$FSTR
    PROCEED=`java -jar $BIRCH/script/ConfirmBox2.jar "$MESSAGE"`
    if [ $PROCEED == "Yes" ]
    then
        for FILE in ${FILES[@]}
        do
            if [[ ${FILE:0:4} = '.nfs' ]] || [ ${FILE} = '.' ] || [ ${FILE} = '..' ] 
            then # don't delete NFS file handles, the current directory or the parent directory
                echo Skipping $FILE
            else
                rm -rf $FILE
            fi
        done
    else
        MESSAGE='Delete canceled.'
        java -jar $BIRCH/script/OkayBox2.jar "$MESSAGE"
    fi

fi   
