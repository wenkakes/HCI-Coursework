#!/bin/bash

ant -buildfile hci/build.xml > /dev/null
java -cp $CLASSPATH:hci/bin src.ImageLabeller hci/images/U1003_0000.jpg
