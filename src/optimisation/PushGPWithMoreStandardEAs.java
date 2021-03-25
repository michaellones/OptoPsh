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

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.spiderland.Psh.GAIndividual;
import org.spiderland.Psh.Interpreter;
import org.spiderland.Psh.PushGP;
import org.spiderland.Psh.PushGPIndividual;

/**
 * Provides some alternative EA implementations.
 * Set the "reproduction" parameter to use these; values are "standard" and "crowding".
 * Otherwise the default Psh EA will be used. 
 */
public abstract class PushGPWithMoreStandardEAs extends PushGP {

	protected String reproductionType;
	
	
	@Override
	protected void InitFromParameters() throws Exception {
		super.InitFromParameters();
		
		reproductionType = this.GetParam("reproduction", true);
		if(reproductionType==null) reproductionType="pushgp";
	}
	
	
	@Override
	protected void Evaluate() {
		// fitness evaluation is mostly done within the crowding routine, if it's in use
		if(reproductionType.contains("crowding") && _generationCount>0) {
			
			// copied from Evaluate, but with EvaluateIndividual removed
			float totalFitness = 0;
			_bestMeanFitness = Float.MAX_VALUE;

			for (int n = 0; n < _populations[_currentPopulation].length; n++) {
				GAIndividual i = _populations[_currentPopulation][n];

				totalFitness += i.GetFitness();

				if (i.GetFitness() < _bestMeanFitness) {
					_bestMeanFitness = i.GetFitness();
					_bestIndividual = n;
					_bestSize = ((PushGPIndividual) i)._program.programsize();
					_bestErrors = i.GetErrors();
				}
			}

			_populationMeanFitness = totalFitness
					/ _populations[_currentPopulation].length;
		}
		super.Evaluate();
	}


	/**
	 * psh's default EA behaviour uses every solution during breeding
	 * by stepping through the population and mutating (etc.) each one;
	 * tournaments are only used to select a mating partner.
	 * This version also implements a more standard EA in which
	 * tournament selection is used to determine all parents, or
	 * alternatively crowding can be used.
	 * @see org.spiderland.Psh.PushGP#Reproduce()
	 */
	@Override
	protected void Reproduce() {
		
		// Use crowding to promote a diverse population
		if(reproductionType.contains("crowding")) {
			ReproduceWithCrowding();
		}
		
		// A more standard EA with tournament selection
		else if(reproductionType.equals("standard")) {
			int nextPopulation = _currentPopulation == 0 ? 1 : 0;
			float psum = _crossoverPercent + _mutationPercent;
			
			// copy the best and store in first child population slot
			GAIndividual elite = _populations[_currentPopulation][_bestIndividual].clone();
			if(_RNG.nextFloat() < _simplificationPercent/100f) {
				elite = ReproduceBySimplification(elite);
			}
			_populations[nextPopulation][0] = elite;
			
			// for each remaining child population slot
			for (int n = 1; n < _populations[_currentPopulation].length; n++) {
				float method = _RNG.nextFloat();
				GAIndividual next;
	
				// either mutate or recombine an existing solution
				if (method < _mutationPercent/psum) {
					PushGPIndividual i = (PushGPIndividual) 
							Select(_tournamentSize, n).clone();
					next = ReproduceByMutation(i);
				} else {
					PushGPIndividual a = (PushGPIndividual) 
							Select(_tournamentSize, n);
					PushGPIndividual b = (PushGPIndividual) 
							Select(_tournamentSize, n);
					next = ReproduceByCrossover(a, b);
				}
				
				// and then maybe simplify the child 
				if(_RNG.nextFloat() < _simplificationPercent/100f) {
					next = ReproduceBySimplification(next);
				}
	
				_populations[nextPopulation][n] = next;
			}
		}
		
		// psh's EA
		else {
			super.Reproduce();
			return;
		}
	}

	
	protected GAIndividual ReproduceByCrossover(GAIndividual p1, GAIndividual p2) {
		PushGPIndividual a = (PushGPIndividual) p1;
		PushGPIndividual b = (PushGPIndividual) p2;

		if (a._program.programsize() <= 0) {
			return b;
		}
		if (b._program.programsize() <= 0) {
			return a;
		}
		
		int aindex = ReproductionNodeSelection(a);
		int bindex = ReproductionNodeSelection(b);
		
		if (a._program.programsize() + b._program.SubtreeSize(bindex)
				- a._program.SubtreeSize(aindex) <= _maxPointsInProgram)
			a._program.ReplaceSubtree(aindex, b._program.Subtree(bindex));

		return a;
	}
	
	
	protected GAIndividual[] ReproduceTwoChildrenByCrossover(GAIndividual p1, GAIndividual p2) {
		PushGPIndividual a = (PushGPIndividual) p1;
		PushGPIndividual b = (PushGPIndividual) p2;

		if (a._program.programsize() <= 0) {
			return new GAIndividual[] {a.clone(), b.clone()};
		}
		if (b._program.programsize() <= 0) {
			return new GAIndividual[] {a.clone(), b.clone()};
		}
		
		PushGPIndividual[] children = new PushGPIndividual[2];
		
		int aindex = ReproductionNodeSelection(a);
		int bindex = ReproductionNodeSelection(b);
		
		if (a._program.programsize() + b._program.SubtreeSize(bindex)
				- a._program.SubtreeSize(aindex) <= _maxPointsInProgram) {
			children[0] = (PushGPIndividual) a.clone();
			children[0]._program.ReplaceSubtree(aindex, b._program.Subtree(bindex));
		}
		
		if (b._program.programsize() + a._program.SubtreeSize(aindex)
				- b._program.SubtreeSize(bindex) <= _maxPointsInProgram) {
			children[1] = (PushGPIndividual) b.clone();
			children[1]._program.ReplaceSubtree(bindex, a._program.Subtree(aindex));
		}

		return children;
	}

	
	protected GAIndividual ReproduceByMutation(GAIndividual p) {
		PushGPIndividual i = (PushGPIndividual) p.clone(); // clones first

		int totalsize = i._program.programsize();
		int which = ReproductionNodeSelection(i);
		
		int oldsize = i._program.SubtreeSize(which);
		int newsize = 0;

		if (_useFairMutation) {
			int range = (int) Math.max(1, _fairMutationRange * oldsize);
			newsize = Math.max(1, oldsize + _RNG.nextInt(2 * range) - range);
		} else {
			newsize = _RNG.nextInt(_maxRandomCodeSize);
		}

		Object newtree;

		if (newsize == 1)
			newtree = _interpreter.RandomAtom();
		else
			newtree = _interpreter.RandomCode(newsize);

		if (newsize + totalsize - oldsize <= _maxPointsInProgram)
			i._program.ReplaceSubtree(which, newtree);

		return i;
	}
	
	
	protected GAIndividual ReproduceBySimplification(GAIndividual a) {
		PushGPIndividual i = (PushGPIndividual) a.clone();

		i = Autosimplify(i, _reproductionSimplifications);

		return i;
	}

	
	/**
	 * Based on psudeocode in Mengshoel and Goldberg,
	 * "The Crowding Approach to Niching in Genetic Algorithms"
	 * (experimental!)
	 */
	protected void ReproduceWithCrowding() {
		int nextPopulation = _currentPopulation == 0 ? 1 : 0;
		int popsize = _populations[_currentPopulation].length;
		
		int S; // parent set size, should be a multiple of 2
		try {
			S = (int) this.GetFloatParam("crowding.S", true);
		} catch (Exception e) {
			S = 2;
		}
		GAIndividual[] parent = new GAIndividual[S]; 
		GAIndividual[] child = new GAIndividual[S];
		double[][] distance = new double[S][S];
		
		// make a list containing all population indices
		List<Integer> indexPool = IntStream.rangeClosed(0, popsize-1)
			    .boxed().collect(Collectors.toList());
		
		int k = 0;
		while(!indexPool.isEmpty()) {
			
			// make a parent set
			for(int i=0; i<S; i++) {
				int j = indexPool.remove(this._RNG.nextInt(indexPool.size()));
				parent[i] = _populations[_currentPopulation][j];
			}
			
			// apply variation operators to create children
			for(int i=0; i<S; i+=2) { // same operator for each pair
				float method = _RNG.nextInt(100);

				if (method < _mutationPercent/(_crossoverPercent+_mutationPercent)) {
					child[i] = ReproduceByMutation(parent[i]);
					child[i+1] = ReproduceByMutation(parent[i+1]);
				} else {
					GAIndividual[] xchild = 
							ReproduceTwoChildrenByCrossover(parent[i], parent[i+1]);
					child[i] = xchild[0];
					child[i+1] = xchild[1];
				}
				
				EvaluateIndividual(child[i]);
				EvaluateIndividual(child[i+1]);
			}
			
			// calculate distance matrix between parents and children
			for(int i=0; i<S; i++) {
				for(int j=0; j<S; j++) {
					distance[i][j] = phenotypicDistance(parent[i],child[j]);
					if(Double.isInfinite(distance[i][j])) {
						distance[i][j] = 1e99; // arbitrary large number
					}
				}
			}
			
			// match children to most similar parents
			HungarianMatching matcher = new HungarianMatching(distance);
			int[] matches = matcher.execute();
			
			// keep either child or parent in each case
			for(int i=0; i<S; i++) {
				if(replacementRule(parent[i].GetFitness(), child[matches[i]].GetFitness())) {
					_populations[nextPopulation][k] = child[matches[i]];
				}
				else {
					_populations[nextPopulation][k] = parent[i];
				}
				
				// maybe apply some simplification
				if(_RNG.nextDouble() < _simplificationPercent) {
					_populations[nextPopulation][k] = 
							ReproduceBySimplification(
									_populations[nextPopulation][k]);
				}
				
				k++;
			}
			
		}

	}
	
	/**
	 * Deterministic replacement is the default rule for crowding
	 */
	protected boolean replacementRule(float pfitness, float cfitness) {
		if(cfitness > pfitness)
			return true;
		else if(cfitness==pfitness)
			return _RNG.nextBoolean();
		else
			return false;
	}
	
	protected abstract double phenotypicDistance(GAIndividual s1, GAIndividual s2);
	
}
