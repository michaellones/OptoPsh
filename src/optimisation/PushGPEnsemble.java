package optimisation;

import org.spiderland.Psh.Program;
import org.spiderland.Psh.PushGPIndividual;

/**
 * A hybrid optimiser.
 */
public class PushGPEnsemble extends PopulationOptimiser {

	public Program[] _programs; // component optimisers
	
	public PushGPEnsemble(Program _program) {
		super(_program);
	}

	public PushGPEnsemble() {
		super();
	}
	
}
