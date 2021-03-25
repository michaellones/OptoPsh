/*
 * Copyright 2021 Michael Lones
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
