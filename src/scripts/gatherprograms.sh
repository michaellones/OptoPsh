#!/bin/bash

# Gathers together either:
#    the best-of-run programs in a directory containing a batch of runs
#    all programs from an OptoPsh output file (i.e. the best of each generation)
# Command line arguments: directory|output_file

if [ -f $1 ]
then
  # extract all programs from output file
  grep -A1 "Partial" "$1"  | grep -v "Partial" | grep -v "\-\-" | grep -v -e '^[[:space:]]*$' | nl -nrz > "$1_allprograms.txt"

else
  # extract best programs from output files in directory
  pushd $1
  rm allprograms.txt

  for f in output*.txt; do
    fitness=`cat "$f" | grep "Best Program Fitness"| tail -1 | rev | cut -d" " -f1 | rev`
    program=`tail -4 $f | head -1`
    program=${program:16}
    echo "${fitness} ${program}" >> allprograms.txt
  done

  sort -k1 -n allprograms.txt > allprogramssorted.txt

  popd
fi
