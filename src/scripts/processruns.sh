#!/bin/bash

# Used for analysing batches of evolved optimisers.
# Reevaluates run bests from each run in each directory.
# Saves results to files <directory>_reevaluated.txt, sorted by mean
# in the form: file mean stddev repeats
# Also creates allprogramssorted_reevaluated.txt based on reevaluated ordering
# (use processruns_reevaluate.sh to just generate results files)
# To augment or override settings in the parameters files, place the new
# settings in a file called additions.pushgp
# Command line arguments: directories

dir=$(pwd)
numdirs=$#

# reevaluate all best of runs
processruns_reevaluate.sh "$@"

# create list of programs, ordered by reevaluated fitness
for d in "$@";
do
  pushd $d
  rm allprogramssorted_reevaluated.txt

  while IFS= read -r line
  do
    outputfile=`echo $line | cut -f1 -d" "`
    fitness=`echo $line | cut -f2 -d" "`
    program=`tail -4 $outputfile | head -1`
    program=${program:16}
    echo -e $fitness "\t" $program >> allprogramssorted_reevaluated.txt

  done < "$dir/${d}_reevaluated.txt"

  popd
done
