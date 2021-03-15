#!/bin/bash

# Command line arguments: trajectory_file function_number [local|pop]

# trap ctrl-c
trap 'finish=y' INT

export CLASSPATH=/Users/michaellones/git/Psh

base=$(basename -- $1)
filename="${base%.*}"
trajdir=$(dirname $1)
maxit=$(wc -l $1)
maxit=$(echo $maxit | cut -d" " -f1)

moviedir="$trajdir/$filename"

mkdir $moviedir
cp $1 $moviedir/$base
cd $moviedir

for i in `seq 1 $maxit`; do
	plottrajectory.R $2 $1 $3 $i
	mv "${base}.png" "${base}$i.png"
	
	if [ x"$finish" = xy ]; then
		echo " cancelled"
		break
	fi
done

ffmpeg -framerate 5 -i "$base%d.png" -pix_fmt yuv420p "$base.mp4"

cd ..