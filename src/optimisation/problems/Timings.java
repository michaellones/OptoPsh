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

package optimisation.problems;

import java.time.Duration;
import java.time.Instant;
import java.util.Random;

public class Timings {

	/**
	 * Measures the average evaluation time of the CEC 2005 problems.
	 */
	public static void main(String[] args) {
		
		int dims = 10;
		int repeats = 10000;
		OriginalCEC2005Problem problem;
		float[] inputs;
		
		Instant start, finish;
		for(int p=1; p<=25; p++) {
			problem = new OriginalCEC2005Problem(p, dims);
			start = Instant.now();
			for(int r=0; r<repeats; r++) {
				inputs = randomChromosome((float)problem.getLowerBound(0), (float)problem.getLowerBound(0), dims);
				float response = problem.evaluate(inputs, false);
			}
			finish = Instant.now();
			long timeElapsed = Duration.between(start, finish).toMillis();
			System.out.println("F"+p+": "+timeElapsed);
		}
		
	}
	
	/**
	 * Makes a random list of floating point numbers, each within a specific range.
	 */
	private static float[] randomChromosome(float lowerLimit, float upperLimit, int length) {
		float[] chromosome = new float[length];
		Random rnd = new Random();
		for(int i=0; i<length; i++)
			chromosome[i] = (upperLimit-lowerLimit)*rnd.nextFloat() + lowerLimit;
		return chromosome;
	}

}
