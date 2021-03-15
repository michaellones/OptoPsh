package optimisation.problems;

public interface Problem {

	public String getName();
	
	public int getDimensionality();
	
	public float evaluate(float[] inputs, boolean rescale);
	
	public float getLowerBound(int dim);
	
	public float getUpperBound(int dim);
	
	public boolean isWithinBounds(float[] inputs);
	
	public float getError(float[] inputs, boolean rescale);
}
