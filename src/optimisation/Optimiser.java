package optimisation;

import org.spiderland.Psh.GAIndividual;
import org.spiderland.Psh.Program;
import org.spiderland.Psh.PushGPIndividual;

public class Optimiser extends PushGPIndividual {
	
	public Optimiser(Program _program) {
		super(_program);
	}

	public Optimiser() {
		super();
	}

	public GAIndividual clone() {
		Optimiser copy = new Optimiser(_program);
		copy.SetFitness(this.GetFitness());
		return copy;
	}
	
}
