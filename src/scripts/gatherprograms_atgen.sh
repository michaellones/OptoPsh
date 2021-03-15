#!/bin/bash

# Gathers together the all the best programs at the specified generation
# from a directory containing a batch of runs
# Command line arguments: directory generation

gen=$(($2+1))

pushd $1
rm "g${2}_allprograms.txt"

for f in output*.txt; do

  # extract generation-th program from output file
  #program=`grep -A1 "Best Program:" "$f" | grep -v "Best Program:" | head -$gen | tail -1`
  program=`grep -A1 "Partial" "$f"  | grep -v "Partial" | grep -v "\-\-" | grep -v -e '^[[:space:]]*$' | sed "${gen}q;d"`

  # extract its fitness
  fitness=`grep "Best Program Fitness" "$f" | sed "${gen}q;d" | rev | cut -d" " -f1 | rev`

  echo "${fitness} ${program}" | grep -v -e '^[[:space:]]*$' >> g${2}_allprograms.txt
done

sort -k1 -n "g${2}_allprograms.txt" > "g${2}_allprogramssorted.txt"

#rm temp.txt
popd
