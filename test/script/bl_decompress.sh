#!/bin/bash

# For each file in FILELIST, decompress the file with the appropriate
# decompression tool, based on the file extension EXTENSION.

# Synopsis decompress.sh filelist

# Testing for existence of an executable is far more complicated 
# than one would think. Many sources consider which unportable.
# See https://stackoverflow.com/questions/592620/how-can-i-check-if-a-program-exists-from-a-bash-script
exists()
{
  command -v "$1" >/dev/null 2>&1
}

case $# in 
    0) 
       echo 'Usage: decompress.sh filelist'
       exit
       ;;
    1) 
       FILELIST=$1
       ;;
esac


for FILE in `cat $FILELIST`
   do
      echo 'FILE: '$FILE
      EXT=${FILE##*.}
      echo 'EXTENSION: '$EXT

      case "$EXT" in 
          gz)
              if  -f `which unpigz`  
              then 
                  unpigz $FILE
              else
                  gunzip $FILE
              fi          
              ;;
           zip)
             unzip -o $FILE
             if [ $? -eq 0 ]
             then
                rm -f $FILE
             fi
             ;;
          bz2)
              if  exists bunzip2 
              then 
                  bunzip2 $FILE
              else
                  echo "bunzip2 not installed on this system"
              fi          
             ;;
          7z)
              if  exists p7zip
              then 
                  p7zip -d $FILE
              else
                  echo "p7zip not installed on this system"
              fi           
             ;;
          *)
             echo 'File: '$FILE 'Unknown file extension: '$EXT
             ;;
      esac

done
