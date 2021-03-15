package optimisation;

import org.spiderland.Psh.GAIndividual;
import org.spiderland.Psh.Program;
import org.spiderland.Psh.PushGPIndividual;

/**
 * A population-based (or swarm) optimiser.
 */
public class PopulationOptimiser extends Optimiser {

	public int popSize; // population size, if being evolved (experimental)
	
	public PopulationOptimiser(Program _program) {
		super(_program);
	}

	public PopulationOptimiser() {
		super();
	}

	public GAIndividual clone() {
		PopulationOptimiser copy = new PopulationOptimiser(_program);
		copy.SetFitness(this.GetFitness());
		copy.trajectories = this.trajectories; // sharing is fine
		copy.popSize = this.popSize;
		return copy;
	}
	
	public String toString() {
		return super.toString() + ' ' + "{p="+popSize+"}";
	}
	
	float[][][] trajectories; // used for crowding
	
}
