#!/bin/bash

# Reevaluates either the best program from an OptoPsh run's output file
# or a hybrid opitmiser if passed a program pool file.
# Command line arguments: params_file output_file|ensemble_file [repeats moves problems dims modifyproblems] [ensemble_size]

# make changes to parameters file
tmppfile="temp.params"
cp $1 $tmppfile
echo "optimisation.modifyproblems=false" >> $tmppfile

# check for optional parameters
if [ "$#" -gt 2 ]; then
  if [ $3 -ne "-1" ]; then
    echo "optimisation.runs=$3" >> $tmppfile
  fi
  if [ $4 -ne "-1" ]; then
    echo "optimisation.moves=$4" >> $tmppfile
  fi
  if [ $5 != "-1" ]; then
    echo "optimisation.problems=$5" >> $tmppfile
  fi
  if [ $6 -ne "-1" ]; then
    echo "optimisation.dimensions=$6" >> $tmppfile
  fi
  if [ $7 != "-1" ]; then
    echo "optimisation.modifyproblems=$7" >> $tmppfile
  fi
fi

if [[ $2 != *all* ]]; then
  # extract program from output file
  program=`tail -4 $2 | head -1`
  program=${program:16}
  echo $program > "program"
  echo 100 >> "program"
else
  cp $2 "program"
  if [ "$#" -gt 7 ]; then
    echo "ensembleSize=$8" >> $tmppfile
  fi
fi

java optimisation.PopulationOptimisation $tmppfile program > output.txt
grep "Fitness" output.txt
