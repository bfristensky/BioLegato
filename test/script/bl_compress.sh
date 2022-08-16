#!/bin/bash

# For each file in FILELIST, compress the file with the appropriate
# compression tool, based on the file extension EXTENSION.

# Synopsis compress.sh filelist extension

# Testing for existence of an executable is far more complicated 
# than one would think. Many sources consider which unportable.
# See https://stackoverflow.com/questions/592620/how-can-i-check-if-a-program-exists-from-a-bash-script
exists()
{
  command -v "$1" >/dev/null 2>&1
}

case $# in 
    0|1) 
       echo 'Usage: compress.sh filelist extension'
       exit
       ;;
    2) 
       FILELIST=$1
       EXT=$2
       ;;
esac


for FILE in `cat $FILELIST`
do
       FEXT=${FILE##*.}
       if [ ${EXT} == ${FEXT} ]
       then
           echo 'Skipping '$FILE
       else
           case "$EXT" in 
               gz)
                  if  exists pigz  
                  then 
                      pigz $FILE
                  else
                      gzip $FILE
                  fi    
                  ;;
               zip)
                  zip $FILE.zip $FILE
                  if [ $? -eq 0 ]
                  then
                     rm -f $FILE
                  fi
                  ;;
               bz2)
                  if exists bzip2  
                  then 
                      bzip2 $FILE
                  else
                      echo "bzip2 not installed on this system"
                  fi                
                  ;;
               7z)
                  if  exists p7zip 
                  then 
                      p7zip $FILE
                  else
                      echo "p7zip not installed on this system"
                  fi                    
                  ;;
               *)
                  echo 'Unknown file extension: '$EXT
                  ;;
           esac
       fi
   done

