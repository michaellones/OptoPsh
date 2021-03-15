#!/bin/bash

# Command line arguments: output_file params_file function_number moves|-1 local|pop [max_pop]

# trap ctrl-c
trap 'finish=y' INT

base=$(basename -- $1)
paramsfile=$(basename -- $2)
filename="${base%.*}"
trajdir=$(dirname $1)
moviedir="${trajdir}/${filename}_movie"
mkdir $moviedir
cp $1 $moviedir/$base
cp $2 $moviedir/$paramsfile
pushd $moviedir

# make changes to parameters file
tmppfile="trajectorymovie_temp.params"
cp $paramsfile $tmppfile
echo "optimisation.problems=${3}" >> $tmppfile
echo "optimisation.dimensions=2" >> $tmppfile
echo "optimisation.runs=1" >> $tmppfile
echo "use-random-seed=true" >> $tmppfile
echo "random-seed=100" >> $tmppfile

#echo "optimisation.fixedstarts=defined" >> $tmppfile
echo "optimisation.fixedstarts=false" >> $tmppfile
echo "optimisation.modifyproblems=false" >> $tmppfile

# change number of moves if required
if [ $4 -ne "-1" ]; then
  echo "optimisation.moves=$4" >> $tmppfile
fi

if [[ $base != all* ]]; then
  # extract program from output file
  program=`tail -4 $1 | head -1`
  program=${program:16}
  echo $program > "program"
  echo 100 >> "program"
else
  cp $1 "program"
fi

# execute program
if [ $5 == "pop" ]; then
  # change popsize parameter if required
  if [ "$#" -gt 5 ]; then
    psize=`grep popsize $tmppfile | tr -dc '0-9'`
    if [ "$psize" -gt $6 ]; then
      echo "optimisation.popsize=$6" >> $tmppfile
    fi
  fi
  java optimisation.PopulationOptimisation $tmppfile program
else
  java optimisation.Optimisation $tmppfile program
fi

# plot trajectory from first run
trajfile=`find . -maxdepth 1 -iname '*run0.txt'`
trajfile=$(basename $trajfile)

lines=`wc -l < $trajfile`
for i in `seq 1 $lines`; do
	plottrajectory.R $3 $trajfile $5 $i Iteration\ $i
	mv "${trajfile}.png" "${base}$i.png"
	
	if [ x"$finish" = xy ]; then
		echo " cancelled"
		break
	fi
done

ffmpeg -framerate 2 -i "$base%d.png" -pix_fmt yuv420p "$base.mp4"
mv starts.txt oldstarts.txt

popd