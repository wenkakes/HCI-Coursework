#!/bin/bash

echo "Compiling..."
ant -buildfile hci/build.xml > /dev/null && echo "Done."
