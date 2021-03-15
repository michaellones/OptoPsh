package optimisation.coco;

import optimisation.problems.Problem;

/**
 * Wrapper, translates COCO problem into OptoPsh optimisation problem
 */
public class CocoProblem implements Problem {

	private optimisation.coco.Problem problem;
	
	public CocoProblem(optimisation.coco.Problem problem) {
		this.problem = problem;
	}
	
	@Override
	public String getName() {
		return problem.getName();
	}

	@Override
	public int getDimensionality() {
		return problem.getDimension();
	}

	@Override
	public float evaluate(float[] inputs, boolean rescale) {
		return getError(inputs, rescale);
	}

	@Override
	public float getLowerBound(int dim) {
		return (float) problem.getSmallestValueOfInterest(dim);
	}

	@Override
	public float getUpperBound(int dim) {
		return (float) problem.getLargestValueOfInterest(dim);
	}

	@Override
	public boolean isWithinBounds(float[] inputs) {
		for(int i=0; i<inputs.length; i++) {
			if(inputs[i] < getLowerBound(i)) return false;
			if(inputs[i] > getUpperBound(i)) return false;
		}
		return true;
	}

	@Override
	public float getError(float[] inputs, boolean rescale) {
		double[] dinputs = new double[inputs.length];
		for(int i=0; i<inputs.length; i++) {
			dinputs[i] = inputs[i];
		}
		double[] val = problem.evaluateFunction(dinputs);
		return (float) val[0];
	}

}
