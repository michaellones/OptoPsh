package optimisation;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;
import java.util.StringTokenizer;

import org.spiderland.Psh.GAIndividual;
import org.spiderland.Psh.GATestCase;
import org.spiderland.Psh.InspectorInput;
import org.spiderland.Psh.Interpreter;
import org.spiderland.Psh.ObjectStack;
import org.spiderland.Psh.Params;
import org.spiderland.Psh.Program;
import org.spiderland.Psh.PushGP;
import org.spiderland.Psh.PushGPIndividual;
import org.spiderland.Psh.booleanStack;
import org.spiderland.Psh.floatStack;
import org.spiderland.Psh.intStack;
import org.spiderland.Psh.vectorStack;

import optimisation.coco.Benchmark;
import optimisation.coco.CocoProblem;
import optimisation.coco.Observer;
import optimisation.coco.Suite;
import optimisation.coco.Timing;
import optimisation.problems.ModifiedOriginalCEC2005Problem;
import optimisation.problems.OriginalCEC2005Problem;
import optimisation.problems.Problem;

public class PopulationOptimisation extends PushGPWithMoreStandardEAs {

	Problem[] _problems;	// Optimisation problems used for evaluation
	boolean modifyProblems;	// Whether to add transformations to the optimisation problems
	int maxMoves;			// Maximum number of moves per optimisation run
	int maxEvaluations;		// Maximum solution evaluations per run
	int numRuns;			// Number of optimisation runs to average over
	int popSize;			// Swarm size used by evolved optimisers
	
	Process[] population;	// Swarm
	int currPopSize;		// Swarm size
	boolean evolvePopSize;	// Whether to evolve the swarm size (in development)
	int maxPopSize;			// Maximum swarm size
	
	boolean fixedStarts = true;	// Whether optimisers always start from the same point(s)
	float[][][][] _starts;		// Stating points, if using fixed starts

	String _report = "";
	
	@Override
	protected void InitFromParameters() throws Exception {
		// needs to be before super is called
		evolvePopSize = this.GetBooleanParamWithDefault("optimisation.evolvepopsize", false);
		if(evolvePopSize) {
			maxPopSize = (int) GetFloatParam("optimisation.maxpopsize");
			System.out.println(maxPopSize);
			maxEvaluations = (int) GetFloatParam("optimisation.maxevaluations");
		}
		_parameters.put("individual-class", "optimisation.PopulationOptimiser");
		
		super.InitFromParameters();
		init();
	}
	
	protected void init() throws Exception {

		_interpreter.setPopulation(this);
		
		int dims = (int) GetFloatParam("optimisation.dimensions");
		maxMoves = (int) GetFloatParam("optimisation.moves");
		numRuns = (int) GetFloatParam("optimisation.runs");
		popSize = (int) GetFloatParam("optimisation.popsize");
		
		String problemstring = GetParam("optimisation.problems");
		
		// Initialise problems
		if(!problemstring.equalsIgnoreCase("COCO")) {
			String problems[] = problemstring.split(",");
			int[] ps = new int[problems.length];
			for(int p=0; p<problems.length; p++) {
				ps[p] = Integer.parseInt(problems[p]);
			}
			_problems = new Problem[ps.length];
			modifyProblems = this.GetBooleanParamWithDefault("optimisation.modifyproblems", false);
		
			_interpreter.vectorStack().setDimensionality(dims);
			
			for(int p=0; p<ps.length; p++) {
				
				// Create problem object for CEC 2005 function
				_problems[p] = new OriginalCEC2005Problem(ps[p], dims);
				
				// Apply transformations, if required
				if(modifyProblems) {
					float lowerScale = this.GetFloatParam("problem.lowerScale", true);
					if(Float.isNaN(lowerScale)) lowerScale = 0.5f;
					float upperScale = this.GetFloatParam("problem.upperScale", true);
					if(Float.isNaN(upperScale)) upperScale = 2.0f;
					float lowerTranslate = this.GetFloatParam("problem.lowerTranslate", true);
					if(Float.isNaN(lowerTranslate)) lowerTranslate = -0.5f;
					float upperTranslate = this.GetFloatParam("problem.upperTranslate", true);
					if(Float.isNaN(upperTranslate)) upperTranslate = 0.5f;
					_problems[p] = new ModifiedOriginalCEC2005Problem(
							(OriginalCEC2005Problem) _problems[p], this._RNG,
							lowerScale, upperScale, lowerTranslate, upperTranslate);
				}
			}
			
			// if required, the same starting points can be used over multiple runs
			// TODO: make this play nicely with modified problems (though unlikely to use both)			
			String fstarts = this.GetParam("optimisation.fixedstarts");
			if(fstarts.equalsIgnoreCase("true") || fstarts.equalsIgnoreCase("share")) {
				
				boolean generate = true;
				
				// re-load an existing set of starting points
				if(fstarts.equalsIgnoreCase("share")) {
					// look for saved list in working directory
					File saved = new File("starts.txt");
					if(saved.exists()) {
						generate = false;
						BufferedReader reader = new BufferedReader(new FileReader(saved));
						StringTokenizer line;
						_starts = new float[ps.length][numRuns][popSize][];
						for(int p=0; p<ps.length; p++) {
							for(int s=0; s<numRuns; s++) {
								for(int n=0; n<popSize; n++) {
									_starts[p][s][n] = new float[dims];
									line = new StringTokenizer(reader.readLine());
									for(int i=0; i<dims; i++) {
										_starts[p][s][n][i] = Float.parseFloat(line.nextToken());
									}
								}
							}
						}
					}
					else
						generate = true;
				}
				
				// or generate fixed starting points randomly within bounds
				if(generate) {
					_starts = new float[ps.length][numRuns][popSize][];
					Random rand = new Random();
					for(int p=0; p<ps.length; p++) {
						for(int s=0; s<numRuns; s++) {
							for(int n=0; n<popSize; n++) {
								_starts[p][s][n] = new float[dims];
								for(int i=0; i<dims; i++) {
									_starts[p][s][n][i] = rand.nextFloat() * 
											(_problems[p].getUpperBound(i)-_problems[p].getLowerBound(i))
											+ _problems[p].getLowerBound(i);
								}
							}
						}
					}
					
					// and save them to a file, if required
					if(fstarts.equalsIgnoreCase("share")) {
						PrintWriter writer = new PrintWriter("starts.txt");
						for(int p=0; p<ps.length; p++) {
							for(int s=0; s<numRuns; s++) {
								for(int n=0; n<popSize; n++) {
									for(int i=0; i<dims; i++) {
										writer.print(""+_starts[p][s][n][i]+" ");
									}
									writer.println();
								}
							}
						}
						writer.flush();
						writer.close();
					}
				}
			}
			
			// or load the starting points from the parameters file
			else if(fstarts.equalsIgnoreCase("defined")) {
				fixedStarts = true;
				float[][][] defined = new float[ps.length][popSize][dims];
				int sn = 0;
				for(int p=0; p<ps.length; p++) {
					String strings[] = this.GetParam("starts"+"."+(_problems[p].getName())).split(",");
					for(int n=0; n<popSize; n++) {
						for(int i=0; i<dims; i++) {
							defined[p][n][i] = Float.parseFloat(strings[sn++]);
						}
					}
				}
				
				_starts = new float[ps.length][numRuns][popSize][];
				for(int p=0; p<ps.length; p++) {
					for(int s=0; s<numRuns; s++) {
						for(int n=0; n<popSize; n++) {
							_starts[p][s][n] = new float[dims];
							for(int i=0; i<dims; i++) {
								_starts[p][s][n][i] = defined[p][n][i];
							}
						}
					}
				}
			}
			else {
				// generate random starting points at each evaluation
				fixedStarts = false;
			}
		}
		else {
			// for COCO benchmarking
			modifyProblems = false;
			fixedStarts = false;
			numRuns = 1;
		}
		
	}

	@Override
	protected void EvaluateIndividual(GAIndividual inIndividual,
			boolean duringSimplify) {
		_report = "";
		EvaluateIndividual(inIndividual, duringSimplify, false, false);
	}
	
	protected void EvaluateIndividual(GAIndividual inIndividual,
			boolean duringSimplify, boolean describe, boolean verbose) {
		ArrayList<Float> errors = new ArrayList<Float>();
		
		if (!duringSimplify && !(inIndividual instanceof PushGPEnsemble))
			_averageSize += ((PushGPIndividual) inIndividual)._program
					.programsize();

		long t = System.currentTimeMillis();

		for (int n = 0; n < _problems.length; n++) {
			float e = evaluate((PushGPIndividual) inIndividual, errors, _problems[n], n, describe, verbose);
		}
		t = System.currentTimeMillis() - t;

		float fitness = 0;
		for(float error : errors)
			fitness += error;
		fitness /= errors.size();
		
		inIndividual.SetFitness(fitness);
		inIndividual.SetErrors(errors);
	}
	
	/**
	 * Evaluate optimiser on COCO benchmark suite (experimental)
	 * @param inIndividual
	 * @param suiteName bbob or bbob-largescale
	 * @param maxRuns maximum number of attempts before giving up
	 */
	protected void EvaluateIndividualUsingCoco(GAIndividual inIndividual, String suiteName,
			int maxRuns, String name) {
		try {
			optimisation.coco.Problem rawproblem;
			
			System.out.println("Running COCO experiment...");
			System.out.flush();
			
			/* Set some options for the observer. See documentation for other options. */
			final String observerOptions = 
					  "result_folder: "+name+"_on_" + suiteName + " " 
					+ "algorithm_name: RS "
					+ "algorithm_info: \"An evolved optimiser\"";
	
			/* Initialize the suite and observer.
	         * For more details on how to change the default options, see
	         * http://numbbo.github.io/coco-doc/C/#suite-parameters and
	         * http://numbbo.github.io/coco-doc/C/#observer-parameters. */
			Suite suite = new Suite(suiteName, "", "");
			Observer observer = new Observer(suiteName, observerOptions);
			Benchmark benchmark = new Benchmark(suite, observer);
	
			/* Initialize timing */
			Timing timing = new Timing();
			
			/* Iterate over all problems in the suite */
			while ((rawproblem = benchmark.getNextProblem()) != null) {
				
				int dimension = rawproblem.getDimension();
				_interpreter.vectorStack().setDimensionality(dimension);
	
				CocoProblem problem = new CocoProblem(rawproblem);
				System.out.println("\n"+problem.getName()+"["+problem.getDimensionality()+"]");
				
				/* Run the algorithm at least once */
				for (int run = 1; run <= 1 + maxRuns; run++) {
	
					/* Break the loop if the target was hit */
					if (rawproblem.isFinalTargetHit())
						break;
					
					float e = evaluate((PushGPIndividual) inIndividual, null, problem, 0, true, false);
					System.out.println(""+e+" ["+rawproblem.isFinalTargetHit()+"]");
					
					//System.exit(1);
					
					// TODO: handle budget scaling with dimensionality 
				}
	
				/* Keep track of time */
				timing.timeProblem(rawproblem);
			}
	
			/* Output the timing data */
			timing.output();
	
			benchmark.finalizeBenchmark();
		} catch (Exception e) {
			System.err.println(e.toString());
		}
	}
	
	public void InitIndividual(GAIndividual inIndividual) {
		super.InitIndividual(inIndividual);
		PopulationOptimiser i = (PopulationOptimiser) inIndividual;

		if(this.evolvePopSize)
			i.popSize = _RNG.nextInt(maxPopSize-1)+1;
	}
	
	/**
	 * Contains the state for a particular swarm member.
	 */
	class Process implements Comparable {
		float[] bestPoint;				// personal best search point
		float[] currentPoint;			// current search point
		float bestValue;				// objective value of personal best
		float currentValue;				// objective value of current search point
		Interpreter.StackFrame stacks;	// stack states
		
		Process(float[] initPoint, float value, Interpreter ip) {
			this.bestPoint = initPoint;
			this.currentPoint = Arrays.copyOf(initPoint, initPoint.length);
			this.bestValue = value;
			this.currentValue = value;
			this.stacks = ip.new StackFrame();
			this.stacks.init(ip);
		}

		@Override
		public int compareTo(Object o) {
			return Float.compare(bestValue, ((Process)o).bestValue);
		}
	}
	
	/**
	 * Finds the swarm member with the best personal best.
	 * @return index of best swarm member
	 */
	private int getBest() {
		double bestValue = Float.POSITIVE_INFINITY;
		int best = Integer.MAX_VALUE;
		for(int p=0; p<currPopSize; p++) {
			if(population[p].bestValue<bestValue) {
				bestValue = population[p].bestValue;
				best = p;
			}
		}
		return best;
	}

	/**
	 * Sorts the swarm members by the objective value of their personal best.
	 */
	private void rank(Process[] population, Interpreter ip) {
		Arrays.sort(population);
		ip.inputStack().clear();
		for(int p=population.length-1; p>=0; p--) {
			ip.inputStack().push(population[p].bestPoint);
		}
	}
	
	/**
	 * Gets the search point associated with a specified swarm member.
	 * @param i		the swarm member's index
	 * @param best	if true, returns personal best rather than current search point
	 * @return
	 */
	public float[] getPosition(int i, boolean best) {
		int pos = i;
		float[] point;
		if(pos<0) // this can happen if i was Integer.MIN_VALUE due to overflow
			pos = currPopSize - 1;
		else {
			pos = i % currPopSize;
		}
		if(best)
			point = population[pos].bestPoint;
		else
			point = population[pos].currentPoint;
		return point;
	}
	
	/**
	 * Evaluate an optimiser on one or more runs of a single problem.
	 */
	private float evaluate(PushGPIndividual inIndividual, ArrayList<Float> errors, 
			Problem problem, int problemid, boolean describe, boolean verbose) {
		
		PopulationOptimiser optimiser = (PopulationOptimiser) inIndividual;
		Problem currentproblem;
		
		float[] current;
		float mean = 0;

		int popBest = -1;
		float popBestValue = Float.MAX_VALUE;
		float[][][] trajectories = null;
		
		PrintWriter out = null;
		Random rand = new Random();
		
		currPopSize = evolvePopSize ? optimiser.popSize : popSize;
		int moves = this.evolvePopSize ? Math.floorDiv(this.maxEvaluations,currPopSize) : maxMoves;
	
		if(describe) {
			_report += "\n"+problem.getName()+": ";
		}
		
		for(int run=0; run<numRuns; run++) {
			
			// when doing crowding, the trajectories  
			// for the first run need to be saved
			if(reproductionType!=null) {
				if(run==0 && reproductionType.contains("crowding"))
					trajectories = new float[popSize][moves][problem.getDimensionality()];
				else
					trajectories = null;
			}
			else
				trajectories = null;
			
			popBestValue = Float.MAX_VALUE;
			
			if(describe) {
				try {
					out = new PrintWriter(problem.getName()+"_run"+run+".txt");
				} catch (FileNotFoundException e) {
					e.printStackTrace();
				}
			}
			
			if(verbose) {
				System.out.println("\n\n### "+problem.getName()+" run "+run);
			}
			
			_interpreter.ClearStacks(); // probably redundant
			
			population = new Process[currPopSize];
			
			// set limits
			float lower = problem.getLowerBound(0);
			float upper = problem.getUpperBound(0);
			_interpreter.setVectorLimits(lower, upper); // needed for random vector instructions
			
			currentproblem = problem;
			
			// iterate through swarm
			for(int i=0; i< currPopSize; i++) {
				float[] point = new float[problem.getDimensionality()];
				
				// set starting point for swarm member
				if(fixedStarts) {
					point = Arrays.copyOf(_starts[problemid][run][i], currentproblem.getDimensionality());
				}
				else {
					for(int d=0; d<currentproblem.getDimensionality(); d++) {
						point[d] = rand.nextFloat() * 
									(currentproblem.getUpperBound(d)-currentproblem.getLowerBound(d))
									+ currentproblem.getLowerBound(d);
					}
				}
				
				float value = currentproblem.getError(point, false);
				if(value<popBestValue)
					popBestValue = value;
				
				population[i] = new Process(point, value, _interpreter);
				
				// push the current search point and its value onto the interpreter's stacks
				population[i].stacks._vectorStack.push(population[i].currentPoint);
				population[i].stacks._floatStack.push(value);
				population[i].stacks._boolStack.push(true);
				
				if(describe) {
					for(int j=0; j<point.length; j++)
						out.print(point[j]+"\t");
					out.print(value+"\t");
				}
				
				if(trajectories!=null) {
					for(int j=0; j<point.length; j++)
						trajectories[i][0][j] = point[j];
				}
			}
			
			if(describe) {
				out.println(popBestValue);
			}
			
			// is this a hybrid optimiser?
			boolean isEnsemble = inIndividual instanceof PushGPEnsemble;
			PushGPEnsemble inEnsemble = isEnsemble ? (PushGPEnsemble) inIndividual : null;
			
			// generate a trajectory for each swarm member
			for(int move=0; move<moves; move++) {
				
				if(verbose) {
					System.out.println("\n# Move "+move);
				}
				
				int bestIndividual = getBest();
				
				Process ind;
				for(int p=0; p<currPopSize; p++) {
					
					if(verbose) {
						System.out.println("\n--- Individual "+p);
					}
					
					ind = population[p];
					
					// switch swarm member's stack states into interpreter
					population[p].stacks.switchIn(_interpreter);
					_interpreter.setCurrent(p);
					
					// make bounds available on input stack
					_interpreter.inputStack().clear();
					_interpreter.inputStack().push(lower);
					_interpreter.inputStack().push(upper);
					
					// place move number, current index and population best index on int stack
					_interpreter.intStack().push(move);
					_interpreter.intStack().push(p);
					_interpreter.intStack().push(bestIndividual);
					
					// execute push program
					
					if(isEnsemble) {
						// if it's a hybrid, choose a program randomly from the pool
						_interpreter.Execute(
								inEnsemble._programs[rand.nextInt(inEnsemble._programs.length)],
								_executionLimit, verbose);
					}
					else {
						_interpreter.Execute(inIndividual._program,
								_executionLimit, verbose);
					}
					
					// retrieve next search point from the top of the vector stack
					// no need to clone, since vectors are invariant
					
					current = _interpreter.vectorStack().top();
					if(current != null)
						ind.currentPoint = current;
					
					_interpreter.inputStack().clear();
					_interpreter.inputStack().push(lower);
					_interpreter.inputStack().push(upper);
					
					if(currentproblem.isWithinBounds(ind.currentPoint)) {
						
						// if a valid point, add its objective value to the float stack
						ind.currentValue = currentproblem.getError(ind.currentPoint, false);
						
						if(ind.currentValue <= ind.bestValue) {
							// indicate it was an improving move
							_interpreter.floatStack().push(ind.currentValue);
							_interpreter.boolStack().push(true);
							ind.bestPoint = ind.currentPoint;
							ind.bestValue = ind.currentValue;
						}
						else {
							// indicate it wasn't an improving move
							_interpreter.floatStack().push(ind.currentValue);
							_interpreter.boolStack().push(false);
							// and put its previous best position back on the vector stack
							_interpreter.vectorStack().push(Arrays.copyOf(ind.bestPoint, ind.bestPoint.length));
						}
						
						if(ind.currentValue < popBestValue) {
							popBestValue = ind.currentValue;
							popBest = p;
						}
					}
					else {
						// indicate that the search point was out of bounds
						_interpreter.floatStack().push(Float.MAX_VALUE);
						_interpreter.boolStack().push(false);
						// and put its previous best position back on the vector stack
						_interpreter.vectorStack().push(Arrays.copyOf(ind.bestPoint, ind.bestPoint.length));
					}
					
					_interpreter.inputStack().push(p);
					
					if(describe) {
						for(int i=0; i<ind.currentPoint.length; i++)
							out.print(ind.currentPoint[i]+"\t");
						out.print(ind.currentValue+"\t");
					}
					
					if(trajectories!=null) {
						for(int i=0; i<ind.currentPoint.length; i++)
							trajectories[p][move][i] = ind.currentPoint[i];
					}
				}
				
				if(describe) {
					out.println(popBestValue);
				}
				
			}
			if(describe) {
				_report += popBestValue + " ";
				out.close();
			}
			
			if(trajectories != null) {
				((PopulationOptimiser)inIndividual)
					.trajectories = trajectories;
			}
			
			if(verbose) System.out.println();
			
			if(verbose)
				System.out.println(popBestValue);
			mean += popBestValue;
			
		}
		
		mean /= numRuns; // mean error of the optimisation runs
		if(errors != null)
			errors.add(mean); // adds each problem as a fitness case
		
		if(describe) {
			_report += "(" + mean + ")";
		}
		
		return mean;
	}
	
	
	@Override
	protected String Report() {
		String report = super.Report();
		
		PushGPIndividual best = (PushGPIndividual)_populations[_currentPopulation][_bestIndividual];
		EvaluateIndividual(best, false, true, false);
		report += _report;
		
		return report;
	}
	
	
	/**
	 * Reads in an existing Push program, or pool of Push programs.
	 * For a pool of programs, it expects the file to contain one program per line,
	 * preceded by its fitness value.
	 */
	public PushGPIndividual loadProgram(String path) throws Exception {
		long lines = Files.lines(Paths.get(path)).count();
		
		PopulationOptimiser ind;
		if(lines>2) { // this is a pool of programs
			PushGPEnsemble inde = new PushGPEnsemble();
			ind = inde;
			BufferedReader reader = new BufferedReader(new FileReader(path));
			File tempfile = new File("program_temp.txt");
			String maxRead = this.GetParam("ensembleSize", true);
			int numToRead = (int) lines;
			if(maxRead!=null)
				numToRead = Integer.parseInt(maxRead);
			inde._programs = new Program[(int) numToRead];
			System.out.println("Ensemble size: "+numToRead);
			
			String line;
			for(int i=0; i<numToRead; i++) {
				PrintWriter writer = new PrintWriter(tempfile);
				line = reader.readLine();
				line = line.substring(line.indexOf(' ')+1); // remove fitness
				writer.println(line);
				writer.println(100);
				writer.flush();
				writer.close();
				InspectorInput input = new InspectorInput(tempfile);
				Program program = input.getProgram();
				inde._programs[i] = program;
				tempfile.delete();
				_interpreter = input.getInterpreter();
			}
		} else {
			// load a single program
			InspectorInput input = new InspectorInput(path);
			_interpreter = input.getInterpreter();
			Program program = input.getProgram();
			ind = new PopulationOptimiser();
			ind._program = program;
			if(this.evolvePopSize)
				ind.popSize = Integer.parseInt(input.getExtras().get("p"));
		}
		
		return ind;
	}
	
	protected GAIndividual ReproduceByMutation(GAIndividual p) {
		PopulationOptimiser o = (PopulationOptimiser) super.ReproduceByMutation(p);
		
		// mutate population size
		if(this.evolvePopSize)
			if(_RNG.nextDouble()<0.05) // TODO: Add parameter
					o.popSize = _RNG.nextInt(maxPopSize-1)+1;
		
		return o;
	}
	
	
	/**
	 * Reevaluates an existing Push program (or pool of programs)
	 * @param args parametersfile inputfile [coconame]
	 */
	public static void main(String args[]) throws Exception {		

		// adapted from PshInspector
		
		if (args.length < 2) {
			System.out.println("Usage: PopulationOptimisation parametersfile inputfile [coconame]");
			System.exit(0);
		}
		
		PopulationOptimisation opt = new PopulationOptimisation();
		opt._executionLimit = 100; // TODO: handle this more elegantly
		opt.SetParams(Params.ReadFromFile(new File(args[0])));
		
		PushGPIndividual ind = opt.loadProgram(args[1]);
		Interpreter interpreter = opt._interpreter;
		opt.init();
		
		if(opt.GetBooleanParamWithDefault("use-random-seed", false)) {
			long seed = Long.parseLong(opt.GetParam("random-seed"));
			interpreter.setSeed(seed);
		}
		
		String problemstring = opt.GetParam("optimisation.problems");
		if(!problemstring.equalsIgnoreCase("COCO")) {
			// get fitness info
			opt.EvaluateIndividual(ind, false, true, false);
			System.out.print("Fitness: "+ind.GetFitness()+ "( ");
			for(float f: ind.GetErrors())
				System.out.print(""+f+" ");
			System.out.println(")");
			System.out.println(opt._report);
		}
		else {
			// COCO (experimental!)
			String name;
			if(args.length>2)
				name = args[2];
			else
				name = new File(args[0]).getName(); // program file name
			
			// change third parameter for multiple restarts (0 = just run once for each problem)
			opt.EvaluateIndividualUsingCoco(ind, "bbob", 0, name);
		}
		
	}

	
	/**
	 * A simple distance metric for use with crowding (experimental).
	 */
	@Override
	protected double phenotypicDistance(GAIndividual s1, GAIndividual s2) {
		float[][][] t1 = ((PopulationOptimiser) s1).trajectories;
		float[][][] t2 = ((PopulationOptimiser) s2).trajectories;
		
		double totaldistance = 0;
		double pdistance; 
		double mdistance; // distance between two d-dimensional points
		double sep;
		
		// calculate sum of distances over all trajectories
		for(int p=0; p<t1.length; p++) {
			pdistance = 0;
			
			// calculate sum of distances over all moves
			for(int m=0; m<t1[0].length; m++) {
				
				// Euclidean distance between two d-dimensional points
				mdistance = 0;
				for(int d=0; d<t1[0][0].length; d++) {
					sep = t2[p][m][d] - t1[p][m][d];
					mdistance += sep * sep;
				}
				pdistance += Math.sqrt(mdistance);
			}
			
			totaldistance += pdistance;
		}
		
		return totaldistance;
	}
	
	
	// not used
	@Override
	public float EvaluateTestCase(GAIndividual inIndividual, Object inInput, Object inOutput) {
		return 0;
	}
	
	// not required
	@Override
	protected void InitInterpreter(Interpreter inInterpreter) throws Exception {
	}
	
}
