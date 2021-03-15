#!/usr/bin/env Rscript

# Overlays a trajectory on a contour plot of a CEC 2005 function
# The trajectory file should have 3 columns for each trajectory (x, y and z),
# with each iteration on a subsequent row. There should also be an extra column
# at the end with the current best z for that iteration.

library(cec2005benchmark)
library(gplots)
library(functional)

# the standard axis limits for the CEC benchmark functions
ulimit = c(100,100,100,100,100,100,100,32,5,5,0.5,pi,2.5,100,5,5,5,5,5,5,5,5,5,5,100)
llimit = c(-100,-100,-100,-100,-100,-100,-100,-32,-5,-5,-0.5,-pi,-2.5,-100,-5,-5,-5,-5,-5,-5,-5,-5,-5,-5,-100)

# locations of global minima for functions used in study
# TODO: fill in the missing ones
#       1               2                           9                   12    13          14              15                     18
ox = c(-3.93119e+001, 3.56267e+001,0,0,0,0,0,0, 1.9005e+000,0,0,-2.0280e+000, 2.471e-001,-7.3602900e+001)
oy = c( 5.88999e+001,-8.29123e+001,0,0,0,0,0,0,-1.5644e+000,0,0,-1.5589e+000,-8.497e-001,-2.3549700e+001)

# This should contain the code for the function to which the
# optimiser is being applied (in this case a call to a predefined
# function in the CEC 2005 benchmarks library). It will be used
# to draw the fitness contours in the background.
cecfnn = function(fnum, x, y) {
  return (cec2005benchmark(fnum, c(x,y)))
}

bestpoints = function(data, fn) {
  fnc = Curry(bestiniteration, fn=fn)
  b = t(apply(data, 1, fnc))
  b = subset(b, b[,2]!=-10000) # messy work-around
  print(b)
  return (b)
}

bestiniteration = function(iteration, fn) {
  zs = seq(3, length(iteration), by=3) # these columns contain the population's objective values
  mm = which.min(iteration[zs]) # identify individual that found the best point
  mm = (mm-1)*3+1 # first column number of this individual
  if(iteration[mm+2]==iteration[length(iteration)]) {
    xy = c( iteration[mm] , iteration[mm+1] ) # x, y
    if(fn!=0) {
      if(xy[1]<llimit[fn] || xy[1]>ulimit[fn] || xy[2]<llimit[fn] || xy[2]>ulimit[fn]) {
        return( c(-10000, -10000) )
      }
      else {
        return (xy)
      }
    }
    else {
      if(xy[1]< -5 || xy[1]>5 || xy[2]< -5 || xy[2]>5) {
        return( c(-10000, -10000) )
      }
      else {
        return (xy)
      }
    }
  }
  else {
    return( c(-10000, -10000) )
  }
}

# Creates the plot.
# Arguments:
#    CEC 2005 function number
#    trajectory file
#    maximum iterations (-1 to use all iterations in the trajectory file)
#    plot type: use "pdf" or "png" to save a file, "plot" to plot in R
#    plot title
createplot = function(fn, src, maxit=-1, savetype="plot", title=NULL, showlines=T) {

  # use currying to fix the fnum argument of cecfnn
  # and then vectorise the resulting function object
  if(fn!=0) {
    fnc = Curry(Vectorize(cecfnn), fnum=fn)
  }

  levels = 25
  plevels = 35
  samples = 20

  # tweak the display parameters where required
  if(fn==13) {
    levels = 50
    plevels = 53
    samples = 50
  }
  if(fn==9) {
    samples = 50
  }
  if(fn==14) {
    samples = 80
  }
  if(fn>14) {
    samples = 80
  }
  if(fn==24) {
    samples = 80
    levels = 50
    plevels = 53
  }

  # this creates a matrix of objective values which will be
  # used to draw the contour plot in the background
  if(fn!=0) {
    xmin = llimit[fn] # x axis range
    xmax = ulimit[fn]
    ymin = llimit[fn] # y axis range
    ymax = ulimit[fn]
  }
  else {
    # assume coco
    xmin = -5
    xmax = 5
    ymin = -5
    ymax = 5
  }
  x <- seq(xmin, xmax, length=samples) # x samples
  y <- seq(ymin, ymax, length=samples) # y samples
  if(fn!=0) {
    z <- outer(x, y, FUN=fnc) # matrix of x,y objective values
  }
  else {
    z <- matrix(0, nrow = length(x), ncol = length(y)) # matrix of zeros
  }

  # Read in the search trajectory coordinates from the specified text file
  # if maxit is set, only the trajectory up to that iteration will be read.
  out = read.table(src, nrows=maxit)
  lst = nrow(out) # last row number
  best = which.min(out[,ncol(out)]) # row with best objective value

  # save plot to a file if specified; otherwise plot onscreen if running interactively
  if(savetype=="pdf") pdf(paste(src,"pdf",sep="."), width=7.75, height=7)
  if(savetype=="png") png(paste(src,"png",sep="."), width=650, height=600)

  popsize = (ncol(out)-1)/3 # work out population size from number of columns
  lcols = rainbow(popsize) # choose colours for each population member
  if(popsize==1) lcols = c('#8a2e04') # use brown if only one population member

  zs = seq(3, ncol(out), by=3) # these columns contain the population's objective values
  mm = which.min(out[best,zs]) # identify individual that found the best point
  mm = (mm-1)*3+1 # first column number of this individual

  bestpath = bestpoints(out, fn)

  # make a gray colour palette for the contour plot
  # need to fiddle with the number to get the right contrast
  cols = rev(colorRampPalette(c('black','white'))(plevels))

  # F13 looks best with a logarithmic scale
  if(fn==13) {
    z = log(z+130)
  }

  # draw the contour plot in the background
  # then add x and y axis
  # then draw the search trajectories
  # then add an "O" to indicate the start of each trajectory
  # and add "X"s to indicate the optimum and best objective value found
  filled.contour(x, y, z, col=cols, nlevels=levels,
                plot.axes={axis(1); axis(2);
                   for (p in 1:popsize) {
                     c1 = 3*(p-1)+1
                     c2 = c1+1
                     if(showlines) {
                      lines(x=out[,c1], y=out[,c2], col=lcols[p], lw=0.5)
                     }
                     points(x=out[,c1], y=out[,c2], pch=20, col=lcols[p], lw=1, cex=1.0)
                     points(x=out[1,c1], y=out[1,c2], pch="O", col=lcols[p], lw=4)
                   };

                   points(x=ox[fn], y=oy[fn], pch=4, col="red", lw=6, cex=2.0)
                   points(x=out[best,mm], y=out[best,mm+1], pch=4, col="black", lw=4, cex=2.0)
                   points(x=bestpath[1,1], y=bestpath[1,2], pch=1, col="black", lw=3, cex=2.0)

                   lines(bestpath, lw=3)
                },
                plot.title = title(main = title))

  if(!(savetype=="plot")) dev.off() # close output file
}
