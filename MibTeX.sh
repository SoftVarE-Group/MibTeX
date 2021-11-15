#!/bin/bash

BASEDIR=$(dirname "$0")

if [ ! -f $BASEDIR/MibTeX/target/MibTeX-1.0-SNAPSHOT.jar ]; then
    mvn -v > /dev/null 2>&1
    if [ $? -ne 0 ]; then
        echo Maven not installed.
        exit
    fi
    mvn install -f $BASEDIR/MibTeX/pom.xml
fi

(cd $BASEDIR/MibTeX; java -jar target/MibTeX-1.0-SNAPSHOT.jar ../options.ini)