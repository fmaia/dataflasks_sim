#!/usr/bin/python

import sys

farnodes = {}


def parseFile(pathIn,desired) :
    global farnodes
    level = 1
    fIn = open(pathIn,'r')
    time = pathIn.split(".")[0].split("/")[1]
    lines = fIn.readlines()
    nlines = len(lines)

    while(level<=desired):
        if(not level in farnodes.keys()):
               farnodes[level] = {}
        lid = 0
        counter = 0
        total = 0
        while(lid<nlines):
        	line1 = lines[lid]
        	total = total + 1
        	lid = lid + 1
        	info = line1.strip("\n").split(" ")
        	if(level!=int(info[2])):
            		counter = counter + 1
    	farnodes[level][time] = ((total-counter)*100)/total
        level = level * 2



if __name__ == '__main__' :

    if len(sys.argv) < 3 :
        print './logParser <input files>'
        sys.exit()

    nbInputs = len(sys.argv) -1
    pathOut = "convergenceAll.txt"
    print nbInputs
    for x in xrange(2,nbInputs+1):
        print 'Parsing: ', sys.argv[x]
        parseFile(sys.argv[x],int(sys.argv[1]))
        print 'Parsing: ', sys.argv[x], ' done'

    fOut = open(pathOut,'w')
    levels = farnodes.keys()
    print farnodes
    intlevels = []
    for strlevtemp in levels:
        intlevels.append(int(strlevtemp))
    intlevels = sorted(intlevels)
    fOut.write("Tempo(s) ")
    for lev in intlevels:
	fOut.write(str(lev)+" ")
    fOut.write("\n")
    tempos = farnodes[lev].keys()
    inttempos = []
    for strtmp in tempos:
      	inttempos.append(int(strtmp))
    tempos = sorted(inttempos)
    for tmp in tempos:
	fOut.write(str(tmp)+" ")
	for lev in intlevels:
       		fOut.write(str(farnodes[lev][str(tmp)])+" ")
	fOut.write("\n")
    fOut.close()
    #print 'maxnpartitions: ' + str(maxp)
    print "That's all folks"
