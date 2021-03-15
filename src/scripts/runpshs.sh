#!/bin/bash

# Runs runpsh.sh for multiple parameters files, i.e. multiple batches of runs.
# The invocations of runpsh.sh are run in parallel. If max-number-of-parallel-jobs,
# the maximum number of parallel processes will be equal to the number of cores.
# Command line arguments: number-of-repeats directory [max-number-of-parallel-jobs]

# Recommended to reduce priority, e.g. nice -10 runpshs.sh ...

pushd $2
rm -f listofjobs

if [ "$#" -gt 2 ]; then
  nprocesses=$3
else
  nprocesses=$(nproc --all)
fi

echo "Running $nprocesses parallel jobs"

for f in *.pushgp; do

  # create a job for each parameters file
  cline="$1 $f"
  echo $cline >> listofjobs

done

# run jobs, maximum of parallel-jobs at once
cat listofjobs | xargs -P $nprocesses -n2 sh -c 'runpsh.sh $1 $2' sh

rm listofjobs
popd
