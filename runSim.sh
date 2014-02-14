#!/bin/bash

rm -rf *.txt
javac core/*.java
java core.Main 16384 5000 16 100
./parserAll.py 1024 ./*.txt
#gnuplot -e "coluna=12" plotconvergenceAll.gp
rm -rf ./*.txt
