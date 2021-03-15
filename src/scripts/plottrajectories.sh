#!/bin/bash

# Plots trajectory for final program from each run's output file in a directory
# Creates a montage in montage.pdf
# Setting the "repeats" argument generates multiple runs/plots for the same program
# Command line arguments: directory function_number moves|-1 local|pop [repeats] [maxpop]

pushd $1

# find parameters file used for runs and indicate 2 dimensions
paramsfile=`find . -maxdepth 1 -iname '*.pushgp'`
paramsfile=$(basename $paramsfile)
tmppfile="temp_${paramsfile}"

if [ "$#" -gt 4 ]; then
  repeats=$5
else
  repeats=1
fi

cp $paramsfile $tmppfile
echo "optimisation.problems=${2}" >> $tmppfile
echo "optimisation.dimensions=2" >> $tmppfile
echo "optimisation.runs=$repeats" >> $tmppfile
echo "optimisation.modifyproblems=false" >> $tmppfile

# change number of moves if required
if [ $3 -ne "-1" ]; then
  echo "optimisation.moves=$3" >> $tmppfile
fi

# loop through each evolutionary run's output file
# note: will process all txt files - replace with output* to limit
for f in *.txt; do
  echo "***** ${f}"

  if [[ $(basename -- $f) != *all* ]]; then
    # extract evolved program from output file
    program=`tail -4 $f | head -1`
    program=${program:16}
    echo $program > "program"
    echo 100 >> "program"
  else
    cp $f "program"
  fi

  # execute program
  if [ $4 == "pop" ]; then
    # change popsize parameter if required
    if [ "$#" -gt 5 ]; then
      psize=`grep popsize $tmppfile | tr -dc '0-9'`
      if [ "$psize" -gt $6 ]; then
        echo "optimisation.popsize=$5" >> $tmppfile
      fi
    fi
    java optimisation.PopulationOptimisation $tmppfile program
  else
    java optimisation.Optimisation $tmppfile program
  fi

  # plot trajectory from each repeat
  for (( r=0; r<$repeats; r++ ))
  do
    trajfile=`find . -maxdepth 1 -iname "*run${r}.txt"`
    outputname=$(basename $f)
    trajfile=$(basename $trajfile)
    newtrajfile="${outputname}_${trajfile}"
    mv $trajfile $newtrajfile
    echo $newtrajfile
    #program=`echo $program | tr " " _`
    plottrajectory.R $2 $newtrajfile $4 $3 $outputname
    #break;
  done
  if [ $repeats -eq "1" ]; then
    rm *run*txt
  fi
  rm program

done

rm $tmppfile

# make a montage of all the images
montage *.png -tile 3x4 -mode concatenate montage.pdf

popd
