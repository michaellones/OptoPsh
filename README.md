> Copyright 2021 Michael Lones
>
> Licensed under the Apache License, Version 2.0 (the "License");
> you may not use this file except in compliance with the License.
> You may obtain a copy of the License at
>
>    http://www.apache.org/licenses/LICENSE-2.0
>
> Unless required by applicable law or agreed to in writing, software
> distributed under the License is distributed on an "AS IS" BASIS,
> WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
> See the License for the specific language governing permissions and
> limitations under the License.

OptoPsh
=======

OptoPsh is a version of [Psh](http://spiderland.org/Psh/) that is used to evolve optimisers. [Psh](http://spiderland.org/Psh/) is a Java implementation of [Push and PushGP](http://hampshire.edu/lspector/push.html).

For more information about OptoPsh, see:

> Michael Lones, Evolving Continuous Optimisers from Scratch, arXiv:2103.11746 [cs.NE], 2021
> [https://arxiv.org/abs/2103.11746](https://arxiv.org/abs/2103.11746)

Building OptoPsh
================
The easiest way for now is to import the source into an IDE such as Eclipse and let it do the compiling for you. Just make sure to set your system's Java CLASSPATH before trying to run anything from the command line.

Note: there is an experimental integration with [COCO](https://coco.gforge.inria.fr), but this has only been compiled for macOS. Check out the readme file in the optimisation.coco package for details of how to build the shared library for other systems.

Evolving Optimisers
===================
You will first need a parameters file. There's an example of one of these in optimisation/sample. This specifies the optimisation functions, the optimiser configuration, and the Push GP settings.

Once you have this, you can use PshGP to evolve an optimiser, e.g.

    $ java PshGP optimisation/sample/params.pushgp

For each generation, this will output the best optimiser program found so far, in addition to various statistics. This includes the errors on each evaluation of each training function, which appear as a list, with the mean in parentheses at the end. At the end of the run, it will output the best optimiser found.

Batches of Runs
===============
Two bash scripts are provided for carrying out multiple runs of PshGP on specified parameter files. The script runpsh.sh carries out multiple runs on the same parameters file, and runpshs.sh executes runpsh.sh on multiple parameter files in parallel. Both will save the output of PshGP for each run, so that you can analyse these afterwards.

Processing Batches of Runs
--------------------------
The script processruns.sh reevaluates all the best-of-run optimisers in one or more batches of runs. Assuming the runs are in directory1, directory2 etc, use:

    $ processruns.sh <directory1> <directory2> ...

This will create a number of files:
- directory_name_reevaluated.txt, for each directory, listing the reevaluation errors in the form: output_file mean_error standard_deviation list_of_errors
- means.txt containing the mean errors for each best-of-run optimiser in each batch, with one column for each batch. This is useful for producing distribution plots.
- allprogramssorted_reevaluated, within each directory, containing all the best-of-run programs for that batch, with the mean error in the first column and the program in the second. This file can be used as a hybrid optimiser pool.

In addition, the script gatherprograms.sh can be used to extract all the best-of-run programs from a single batch, without reevaluating or generating statistics, and gatherprograms_atgen.sh does the same, but gathers the best-of-run programs at a specified generation.

Evaluating Individual Optimisers
================================

The bash script evaluateoptimiser.sh will re-evaluate an evolved optimiser using a specified set of parameters.

    $ evaluateoptimiser.sh <params-file> <optimiser-file> [repeats moves problems dims modify-problems] [ensemble-size]

The optional arguments will override any settings in params-file (use -1 to use the current setting). If optimiser-file is an output file, then it will use the best-of-run optimiser. If optimiser-file is a program pool file, then it will form a hybrid optimiser. You can set the parameter ensembleSize in params-file to limit the number of programs to be used in the hybrid, or use the argument ensemble-size if you are also specifying the other optional parameters.

Visualising Optimisers
======================

There are several R and bash scripts for visualising optimiser trajectories.

Plotting a trajectory
---------------------

The bash script plottrajectories.sh generates trajectory plots for a set of optimisers, overlaying each on a contour plot for a specified CEC 2005 function.

    $ plottrajectories.sh <directory> <CEC-function-number> <moves|-1> <local|pop> [repeats]

Where "directory" is usually a batch directory. The fourth argument should be 'pop'; 'local' is obsolete. A value other than -1 for moves will cause the trajectory to be truncated at the specified iteration. Specifying a value for 'repeats' will generate multiple trajectory plots for each optimiser, which is useful due to stochasticity. Note that there is also an R script, plottrajectory.R, which generates a single plot from an existing trajectory file.

Trajectory movies
-----------------

If [ffmpeg](https://www.ffmpeg.org) is installed, the bash script trajectorymovie.sh can be used to generate a trajectory movie, with a frame for each move.

    $ trajectorymovie.sh <optimiser-file> <params-file> <CEC-function_number> <moves|-1> <local|pop> [max-pop]
