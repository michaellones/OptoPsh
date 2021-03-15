#!/usr/bin/env Rscript

# Plots an existing trajectory file
# Note: "local" is deprecated; aways use "pop"
# args:  function_num  traj_file  pop|local  [max_it  title]
args = commandArgs(trailingOnly=TRUE)

# find directory name of this script
library(getopt)
sourceDir = dirname(get_Rscript_filename())

if(args[3]=="pop") {
  suppressMessages(source(paste(sourceDir, "/contourp.R", sep="")))
  #source(paste(sourceDir, "/contourp.R", sep=""))
} else {
  suppressMessages(source(paste(sourceDir, "/contour.R", sep="")))
}

fnum = as.integer(args[1])
file = args[2]
maxit = -1
if(length(args)>3) {
  maxit = as.integer(args[4])
}
print(maxit)

title = NULL
if(length(args)>4) {
  title = args[5]
}
print(title)

createplot(fnum, file, maxit, savetype="png", title=title)
