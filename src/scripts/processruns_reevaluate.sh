#!/bin/bash

# Used for analysing batches of evolved optimisers.
# Reevaluates run bests from each run in each directory.
# Saves results to files <directory>_reevaluated.txt, sorted by mean
# in the form: file mean stddev repeats
# (use processruns.sh to also extract programs)
# To augment or override settings in the parameters files, place the new
# settings in a file called additions.pushgp
# Command line arguments: directories

dir=$(pwd)
numdirs=$#

# for each results directory
for d in "$@";
do
  echo dir $d
  pushd $d

  tmppfile="temp.params"
  cp "_params.pushgp" $tmppfile
  dirresults="$dir/$(basename -- $d)_reevaluated.txt"
  rm $dirresults

  # make default changes to parameters file
  echo "optimisation.modifyproblems=false" >> $tmppfile
  echo "optimisation.fixedstarts=false" >> $tmppfile
  echo "optimisation.runs=25" >> $tmppfile

  # add any additional changes
  if [ -f "$dir/additions.pushgp" ]; then
    cat "$dir/additions.pushgp" >> $tmppfile
  fi

  # loop through each run's output file
  for f in output*.txt; do

    echo file $f

    # output file name (no newline, interpret escaped chars)
    echo -n -e $(basename $f) "\t" >> $dirresults

    # extract evolved program from output file
    program=`tail -4 $f | head -1`
    program=${program:16}
    echo $program > "program"
    echo 100 >> "program"

    # reevaluate it
    java optimisation.PopulationOptimisation $tmppfile program > temp.txt
    mean=`grep "Fitness" temp.txt | tr -d "(" | cut -d " " -f2`
    echo -n -e $mean "\t" >> $dirresults
    output=`tail -1 temp.txt`
    output=${output#*:} # remove "cecFN:"
    output=${output%(*} # remove "(average)"
    stddev=$(
      echo $output | tr " " "\n" | awk '{sum+=$1; array[NR]=$1} END {for(x=1;x<=NR;x++){sumsq+=((array[x]-(sum/NR))**2);}print sqrt(sumsq/NR)}'
    )
    echo -n -e $stddev "\t" >> $dirresults
    echo $output >> $dirresults

  done

  # uncomment these two lines for sorted means file
  #mv $dirresults temp.txt
  #sort -k2 -g temp.txt > $dirresults

  rm $tmppfile
  rm temp.txt
  rm cec*
  rm program

  popd

done

# paste the means together by column
files=""
for d in "$@";
do
  echo $d > "${d}/means.txt"
  cat "${d}_reevaluated.txt" | cut -f2 -d" " >> "${d}/means.txt"
  files="$files ${d}/means.txt"
done
paste $files > means.txt
