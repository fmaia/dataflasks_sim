#!/bin/bash

rm -rf src/*.txt
cd src
javac core/*.java
java core.Main 16384 5000 16 100
cd ..
./parserAll.py 1024 src/*.txt
gnuplot -e "coluna=12" plotconvergenceAll.gp
