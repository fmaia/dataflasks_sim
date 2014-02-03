set term postscript color eps enhanced 22
set output "convergenceAll.eps"


#set style data histogram
#set style histogram rowstacked
#set style fill solid border -1
#set boxwidth 1


set xrange [0:2000]
set yrange [0:100]
set xlabel "Time (s)"
set ylabel "Number of nodes"
#set title "Group construction convergence."
#set key outside 
#unset key

plot for [col=2:int(coluna)] 'convergenceAll.txt' using 1:col with lines lw 3 title columnheader

!epstopdf convergenceAll.eps
quit

