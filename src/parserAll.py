'''
Copyright (c) 2014.

Universidade do Minho
Francisco Maia

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

   http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and limitations under the License.
'''

#!/usr/bin/python

import sys

farnodes = {}


def parseFile(pathIn,desired) :
    global farnodes
    level = 1
    fIn = open(pathIn,'r')
    time = pathIn.split(".")[1].split("/")[1]
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
    pathOut = "convergenceAll.log"
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
