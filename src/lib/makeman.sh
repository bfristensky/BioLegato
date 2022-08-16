#!/bin/bash

for file in `ls *.java`; do write=${file%.java}; cat $file | sed -e '/\/\*\*/,/\*\// !d' | grep -v "     \*" | sed -e 's&[/*]&&g' -e 's&^  &&' | tail +2 > $write'.man' ; done
