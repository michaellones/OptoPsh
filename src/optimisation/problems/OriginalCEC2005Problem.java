package optimisation.problems;

import optimisation.problems.cec2005.*;

/**
 * Wrapper for existing CEC2005 benchmark code.
 *
 * @author michaellones
 */
public class OriginalCEC2005Problem implements Problem {

	test_func _testFunction;
	int _dims;
	float _ulimit;
    float _llimit;
    float _optimum;
    String _name;
    static final float[] optima = new float[] {-450f, -450f, -450f, -450f, -310f, 390f, -180f, -140f, -330f, -330f, 90f, -460f, -130f, -300f, 120f, 120f, 120f, 10f, 10f, 10f, 360f, 360f, 360f, 260f, 260f};
	
	public OriginalCEC2005Problem(int problemnumber, int dimensionality) {
		_name = "cecF"+problemnumber;
		_dims = dimensionality;
		_optimum = optima[problemnumber-1];
		benchmark cec2005ProblemFactory = new benchmark();
		_testFunction = cec2005ProblemFactory.testFunctionFactory(
				problemnumber, dimensionality);
		
	    switch (problemnumber) {
	      case 1:
	      case 2:
	      case 3:
	      case 4:
	      case 5:
	      case 6:
	      case 14:
	        _ulimit = 100;
	        _llimit = -100;
	        break;
	      case 7:
	      case 25:
	        _ulimit = Float.MAX_VALUE;
	        _llimit = Float.MIN_VALUE;
	        break;
	      case 8:
	        _ulimit = 32;
	        _llimit = -32;
	        break;
	      case 9:
	      case 10:
	      case 13:
	      case 15:
	      case 16:
	      case 17:
	      case 18:
	      case 19:
	      case 20:
	      case 21:
	      case 22:
	      case 23:
	      case 24:
	        _ulimit = 5;
	        _llimit = -5;
	        break;
	      case 11:
	        _ulimit = 0.5f;
	        _llimit = -0.5f;
	        break;
	      case 12:
	        _ulimit = (float) Math.PI;
	        _llimit = (float) -Math.PI;
	        break;
	      default:
	        _ulimit = 100;
	        _llimit = 100;
	    }
	}
	
	@Override
	public int getDimensionality() {
		return _dims;
	}

	@Override
	public float evaluate(float[] inputs, boolean rescale) {
		double[] x = new double[inputs.length];
		for(int i=0; i<inputs.length; i++) {
			x[i] = inputs[i];
			if(rescale) x[i] = (x[i] * (getUpperBound(i)-getLowerBound(i)))
					+ getLowerBound(i);
		}
		float y = (float) _testFunction.f(x);
		if(Float.isInfinite(y) && y > 0){
			y = Float.MAX_VALUE;
		}
		if(Float.isInfinite(y) && y < 0){
			y = (1.0f - Float.MAX_VALUE);
		}
		return y;
	}
	
	public float getError(float[] inputs, boolean rescale) {
		return evaluate(inputs, rescale) - _optimum;
	}

	@Override
	public boolean isWithinBounds(float[] inputs) {
		for(int i=0; i<_dims; i++) {
			if(inputs[i] < _llimit) return false;
			if(inputs[i] > _ulimit)  return false;
		}
		return true;
	}

	@Override
	public float getLowerBound(int dim) {
		return _llimit;
	}

	@Override
	public float getUpperBound(int dim) {
		return _ulimit;
	}

	@Override
	public String getName() {
		return _name;
	}
}
