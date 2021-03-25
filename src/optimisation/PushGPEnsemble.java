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
