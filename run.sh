#!/bin/bash

echo "Compiling..."
ant -buildfile hci/build.xml > /dev/null
echo "Running..."
java -cp $CLASSPATH:hci/bin src.ImageLabeller hci/images/U1003_0000.jpg
