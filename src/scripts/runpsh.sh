#!/bin/bash

# Runs Psh multiple times on the same parameters file
# and stores the consecutively numbered output files
# in a new directory.
# Command line arguments: number-of-repeats parameters-file

# trap ctrl-c
trap 'finish=y' INT

# create directory
today=`date +"%d%m%Y"`
paramsbase=$(basename -- "$2")
runsdir="${paramsbase%%.*}$today"
mkdir $runsdir

# store a copy of the parameters file
cp $2 "$runsdir/_params.pushgp"

# do runs
for i in `seq 1 $1`; do
	runout="$runsdir/output"$i".txt"

	# only run if not already completed
	if [ ! -f $runout ]; then
		date > $runout
		# -Xmx8000M if running out of memory
		java PshGP $2 >> $runout
		date >> $runout

		# exit early if run was interrupted
		if [ x"$finish" = xy ]; then
			echo "$paramsbase run $i cancelled"
			break
		else
			echo "$paramsbase run $i completed"
		fi
	else
		echo echo "$paramsbase run $i already completed"
	fi
done
