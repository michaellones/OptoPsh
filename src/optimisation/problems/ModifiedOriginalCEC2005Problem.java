package optimisation.problems;

import java.util.Random;

import org.spiderland.Psh.PushGP;

/**
 * Wrapper for transformed CEC 2005 functions.
 * 
 * @author michaellones
 */
public class ModifiedOriginalCEC2005Problem implements Problem {

	OriginalCEC2005Problem original;
	float[] lowerBounds;	// new lower bound, taking into account scaling and translations
	float[] upperBounds;	// new upper bound, taking into account scaling and translations
	float[] offsets;		// offsets for all dimensions (proportional to range)
	float[] scales;			// scaling factors for all dimensions
	boolean[] flips;		// whether to flip each dimension
	
	public ModifiedOriginalCEC2005Problem() {
		
	}
	
	public ModifiedOriginalCEC2005Problem(OriginalCEC2005Problem original, Random rnd) {
		this(original, rnd, 0.5f, 2.0f, -0.5f, 0.5f);
	}
	
	public ModifiedOriginalCEC2005Problem(OriginalCEC2005Problem original, 
			Random rnd, float lowerScale, float upperScale, 
			float lowerTranslate, float upperTranslate) {
		
		this.original = original;
		
		// create scaling for each dimension
		scales = new float[original.getDimensionality()];
		for(int i=0; i<scales.length; i++) {
			scales[i] = rnd.nextFloat() * 
					(upperScale-lowerScale) + lowerScale;
		}
		// work out new bounds for each dimension
		lowerBounds = new float[original.getDimensionality()];
		upperBounds = new float[original.getDimensionality()];
		for(int i=0; i<lowerBounds.length; i++) {
			lowerBounds[i] = original.getLowerBound(i) * scales[i];
			upperBounds[i] = original.getUpperBound(i) * scales[i];
		}
		
		// create offset for each dimension
		offsets = new float[original.getDimensionality()];
		for(int i=0; i<offsets.length; i++) {
			float range = original.getUpperBound(i)-original.getLowerBound(i);
			offsets[i] = rnd.nextFloat() * 
					(upperTranslate*range - lowerTranslate*range) 
					+ lowerTranslate*range;
		}
		// work out new bounds for each dimension
		for(int i=0; i<lowerBounds.length; i++) {
			lowerBounds[i] += offsets[i];
			upperBounds[i] += offsets[i];
		}
		
		// create flips for each dimension
		flips = new boolean[original.getDimensionality()];
		for(int i=0; i<flips.length; i++) {
			flips[i] = rnd.nextBoolean();
		}
	}

	@Override
	public String getName() {
		return original.getName();
	}

	@Override
	public int getDimensionality() {
		return original.getDimensionality();
	}

	@Override
	public float evaluate(float[] inputs, boolean rescale) {
		float[] modified = new float[inputs.length];
		for(int i=0; i<modified.length; i++) {
			if(flips[i]) {
				modified[i] = lowerBounds[i] + (upperBounds[i] - modified[i]);
			}
			modified[i] = (inputs[i] - offsets[i]) / scales[i];
		}
		return original.evaluate(modified, rescale);
	}

	@Override
	public float getLowerBound(int dim) {
		return lowerBounds[dim];
	}

	@Override
	public float getUpperBound(int dim) {
		return upperBounds[dim];
	}

	@Override
	public boolean isWithinBounds(float[] inputs) {
		for(int i=0; i<inputs.length; i++) {
			if(inputs[i] < lowerBounds[i]) return false;
			if(inputs[i] > upperBounds[i])  return false;
		}
		return true;
	}

	@Override
	public float getError(float[] inputs, boolean rescale) {
		return evaluate(inputs, rescale) - original._optimum;
	}

}
