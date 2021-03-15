package org.spiderland.Psh;

import java.util.Arrays;
import java.util.Random;

import optimisation.PopulationOptimisation;

/**
 * Dummy class, so that I can keep all the vector stack
 * instructions together in one file.
 */
public class VectorStackInstructions {

}

/**
 * Gets value of a specified component from the vector
 * at the top of the vector stack. Does not pop the vector.
 * 
 * intStack - specifies component index
 */
class VectorGetComponent extends Instruction {
	private static final long serialVersionUID = 1L;
	
	@Override
	public void Execute(Interpreter inI) {
		vectorStack vstack = inI.vectorStack();
		floatStack fstack = inI.floatStack();
		intStack istack = inI.intStack();
		
		if(vstack.size() > 0 && istack.size() > 0) {
			int dim = istack.pop() % vstack._dims;
			if(dim>=0 && dim<vstack._dims) {
				fstack.push(vstack.top()[dim]);
			}
		}
	}
}

/**
 * Sets the value of a specified component in the vector
 * at the top of the vector stack.
 * 
 * floatStack - specified value
 * intStack - specifies component index
 */
class VectorSetComponent extends Instruction {
	private static final long serialVersionUID = 1L;
	
	@Override
	public void Execute(Interpreter inI) {
		vectorStack vstack = inI.vectorStack();
		floatStack fstack = inI.floatStack();
		intStack istack = inI.intStack();
		
		if(fstack.size() > 0 && istack.size() > 0 && vstack.size() > 0) {
			float[] v = vstack.pop();
			int dim = istack.pop() % v.length;
			if(dim>=0 && dim<v.length) {
				float[] result = Arrays.copyOf(v, v.length);
				result[dim] = fstack.pop();
				vstack.push(result);
			}
		}
	}
}

/**
 * Creates a new vector by popping values from the float stack.
 * Does nothing if there are insufficient values on the float stack.
 */
class VectorFromFloats extends Instruction {
	private static final long serialVersionUID = 1L;
	
	@Override
	public void Execute(Interpreter inI) {
		vectorStack vstack = inI.vectorStack();
		floatStack fstack = inI.floatStack();
		
		if(fstack.size() >= vstack._dims) {
			float[] v = new float[vstack._dims];
			// fill in reverse, consistent with VectorToFloats
			for(int i=v.length-1; i>=0; i--) {
				v[i] = fstack.pop();
			}
			vstack.push(v);
		}
	}
}

/**
 * Pushes all the components of the vector at the top
 * of the vector stack on to the float stack.
 * Pops the vector stack.
 */
class VectorToFloats extends Instruction {
	private static final long serialVersionUID = 1L;
	
	@Override
	public void Execute(Interpreter inI) {
		vectorStack vstack = inI.vectorStack();
		floatStack fstack = inI.floatStack();
		
		// only if there are enough floats on the float stack
		if(vstack.size() > 0) {
			float[] v = vstack.pop();
			for(int i=0; i<v.length; i++) {
				fstack.push(v[i]);
			}
		}
	}
}

/**
 * Pushes a random vector to the vector stack.
 * Component values are sampled uniformly from the
 * full float range.
 */
class VectorRand extends Instruction {
	private static final long serialVersionUID = 1L;
	
	Random _RNG;

	@Override
	public void Execute(Interpreter inI) {
		if(_RNG==null) _RNG=inI._RNG;
		
		vectorStack vstack = inI.vectorStack();
		float[] rnd = new float[vstack._dims];
		float range = (inI._maxRandomFloat - inI._minRandomFloat)
				/ inI._randomFloatResolution;
		for(int i=0; i<rnd.length; i++) {
			rnd[i] = (_RNG.nextFloat() * range * inI._randomFloatResolution)
					+ inI._minRandomFloat;
		}
		inI.vectorStack().push(rnd);
	}
}

/**
 * Pushes a random vector to the vector stack.
 * Component values are sampled uniformly from a
 * pre-specified float range.
 */
class VectorRandWithLimits extends Instruction {
	private static final long serialVersionUID = 1L;
	
	Random _RNG;
	static float _lowerLimit;
	static float _upperLimit;
	
	// TODO: find a better solution than using statics
	public static void setLimits(float lower, float upper) {
		_lowerLimit = lower;
		_upperLimit = upper;
	}

	@Override
	public void Execute(Interpreter inI) {
		if(_RNG==null) _RNG=inI._RNG;
		
		vectorStack vstack = inI.vectorStack();
		float[] rnd = new float[vstack._dims];
		float range = (_upperLimit - _lowerLimit);
		for(int i=0; i<rnd.length; i++) {
			rnd[i] = (_RNG.nextFloat() * range)
					+ _lowerLimit;
		}
		inI.vectorStack().push(rnd);
	}
}

/**
 * Pushes a random vector to the vector stack.
 * Component values are sampled uniformly from a
 * specified float range.
 * 
 * floatStack - contains absolute limit used to
 * bound positive and negative values
 */
class VectorRandWithWidth extends Instruction {
	private static final long serialVersionUID = 1L;
	
	Random _RNG;

	@Override
	public void Execute(Interpreter inI) {
		if(_RNG==null) _RNG=inI._RNG;
		
		vectorStack vstack = inI.vectorStack();
		floatStack fstack = inI.floatStack();
		float[] rnd = new float[vstack._dims];
		float range = fstack.pop();
		for(int i=0; i<rnd.length; i++) {
			rnd[i] = (_RNG.nextFloat() * 2.0f * range)
					- range;
			if(Float.isInfinite(rnd[i]) && rnd[i] > 0){
				rnd[i] = Float.MAX_VALUE;
			}
			if(Float.isInfinite(rnd[i]) && rnd[i] < 0){
				rnd[i] = (1.0f - Float.MAX_VALUE);
			}
			if(Float.isNaN(rnd[i])){
				rnd[i] = 0.0f;
			}
		}
		inI.vectorStack().push(rnd);
	}
}

/**
 * Pushes a random unit vector to the vector stack.
 * Component values are sampled uniformly.
 */
class UnitVectorRand extends Instruction {
	private static final long serialVersionUID = 1L;
	
	Random _RNG;

	@Override
	public void Execute(Interpreter inI) {
		
		if(_RNG==null) _RNG=inI._RNG;
		
		vectorStack vstack = inI.vectorStack();
		float[] rnd = new float[vstack._dims];
		float range = (inI._maxRandomFloat - inI._minRandomFloat)
				/ inI._randomFloatResolution;
		for(int i=0; i<rnd.length; i++) {
			rnd[i] = (_RNG.nextFloat() * range * inI._randomFloatResolution)
					+ inI._minRandomFloat;
		}
		double mag = 0;
		for(int i=0; i<rnd.length; i++) {
			mag += rnd[i] * rnd[i];
		}
		mag = Math.sqrt(mag);
		for(int i=0; i<rnd.length; i++) {
			rnd[i] /= mag;
		}
		inI.vectorStack().push(rnd);
	}
}

/**
 * Base class for instructions that take one vector
 * and one scalar as arguments and return a vector.
 */
abstract class BinaryVectorScalarInstruction extends Instruction {
	private static final long serialVersionUID = 1L;
	
	abstract float[] BinaryOperator(float[] inA, float inB);

	@Override
	public void Execute(Interpreter inI) {
		vectorStack astack = inI.vectorStack();
		floatStack sstack = inI.floatStack();

		if (astack.size() > 0 && sstack.size() > 0) {
			float[] a = astack.pop();
			float b = sstack.pop();
			astack.push(BinaryOperator(a, b));
		}
	}
}

/**
 * Base class for instructions that take two vectors
 * as arguments and return a vector.
 */
abstract class BinaryVectorInstruction extends Instruction {
	private static final long serialVersionUID = 1L;
	
	abstract float[] BinaryOperator(float[] inA, float[] inB);

	@Override
	public void Execute(Interpreter inI) {
		vectorStack stack = inI.vectorStack();

		if (stack.size() > 1) {
			float[] a, b;
			a = stack.pop();
			b = stack.pop();
			stack.push(BinaryOperator(b, a));
		}
	}
}

/**
 * Base class for instructions that take two vectors
 * as arguments and return a scalar.
 */
abstract class BinaryVectorToScalarInstruction extends Instruction {
	private static final long serialVersionUID = 1L;
	
	abstract float BinaryOperator(float[] inA, float[] inB);

	@Override
	public void Execute(Interpreter inI) {
		vectorStack vstack = inI.vectorStack();
		floatStack fstack = inI.floatStack();

		if (vstack.size() > 1) {
			float[] a, b;
			a = vstack.pop();
			b = vstack.pop();
			fstack.push(BinaryOperator(b, a));
		}
	}
}

/**
 * Base class for instructions that takes one vector
 * as an argument and returns a scalar.
 */
abstract class UnaryVectorScalarInstruction extends Instruction {
	private static final long serialVersionUID = 1L;
	
	abstract float UnaryOperator(float[] inValue);

	@Override
	public void Execute(Interpreter inI) {
		vectorStack astack = inI.vectorStack();
		floatStack sstack = inI.floatStack();

		if (astack.size() > 0)
			sstack.push(UnaryOperator(astack.pop()));
	}
}

/**
 * Base class for instructions that take a vector,
 * and two scalars as arguments and return a vector.
 */
abstract class TernaryVectorInstruction extends Instruction {
	private static final long serialVersionUID = 1L;
	
	abstract float[] TernaryOperator(float[] inA, int inB, float inC);

	@Override
	public void Execute(Interpreter inI) {
		vectorStack vstack = inI.vectorStack();
		floatStack fstack = inI.floatStack();
		intStack istack = inI.intStack();

		if (vstack.size() > 0 && fstack.size() > 0 && istack.size() > 0) {
			float[] a = vstack.top();
			float b = fstack.pop();
			int c = istack.pop();
			vstack.push(TernaryOperator(a, c, b));
		}
	}
}


/**
 * Vector sum.
 */
class VectorAdd extends BinaryVectorInstruction {
	private static final long serialVersionUID = 1L;
	
	@Override
	float[] BinaryOperator(float[] inA, float[] inB) {
		float[] result = new float[inA.length];
		float temp;
		for(int i=0; i<inA.length; i++) {
			temp = inA[i]+inB[i];
			if(Float.isInfinite(temp) && temp > 0){
				temp = Float.MAX_VALUE;
			}
			if(Float.isInfinite(temp) && temp < 0){
				temp = (1.0f - Float.MAX_VALUE);
			}
			result[i] = temp;
		}
		return result;
	}
}

/**
 * Vector difference.
 */
class VectorSub extends BinaryVectorInstruction {
	private static final long serialVersionUID = 1L;
	
	@Override
	float[] BinaryOperator(float[] inA, float[] inB) {
		float[] result = new float[inA.length];
		float temp;
		for(int i=0; i<inA.length; i++) {
			temp = inA[i]-inB[i];
			if(Float.isInfinite(temp) && temp > 0){
				temp = Float.MAX_VALUE;
			}
			if(Float.isInfinite(temp) && temp < 0){
				temp = (1.0f - Float.MAX_VALUE);
			}
			result[i] = temp;
		}
		return result;
	}
}

/**
 * Vector multiplication.
 */
class VectorMul extends BinaryVectorInstruction {
	private static final long serialVersionUID = 1L;
	
	@Override
	float[] BinaryOperator(float[] inA, float[] inB) {
		float[] result = new float[inA.length];
		float temp;
		for(int i=0; i<inA.length; i++) {
			temp = inA[i]*inB[i];
			if(Float.isInfinite(temp) && temp > 0){
				temp = Float.MAX_VALUE;
			}
			if(Float.isInfinite(temp) && temp < 0){
				temp = (1.0f - Float.MAX_VALUE);
			}
			if(Float.isNaN(temp)){
				temp = 0.0f;
			}
			result[i] = temp;
		}
		return result;
	}
}

/**
 * Vector division.
 */
class VectorDiv extends BinaryVectorInstruction {
	private static final long serialVersionUID = 1L;
	
	@Override
	float[] BinaryOperator(float[] inA, float[] inB) {
		float[] result = new float[inA.length];
		float temp;
		for(int i=0; i<inA.length; i++) {
			temp = inA[i]/inB[i];
			if(Float.isInfinite(temp) && temp > 0){
				temp = Float.MAX_VALUE;
			}
			if(Float.isInfinite(temp) && temp < 0){
				temp = (1.0f - Float.MAX_VALUE);
			}
			if(Float.isNaN(temp)){
				temp = 0.0f;
			}
			result[i] = temp;
		}
		return result;
	}
}

/**
 * Dot product.
 */
class VectorDotProduct extends BinaryVectorToScalarInstruction {
	private static final long serialVersionUID = 1L;
	
	@Override
	float BinaryOperator(float[] inA, float[] inB) {
		float result = 0;
		for(int i=0; i<inA.length; i++) {
			result += inA[i]*inB[i];
		}
		if(Float.isInfinite(result) && result > 0){
			result = Float.MAX_VALUE;
		}
		if(Float.isInfinite(result) && result < 0){
			result = (1.0f - Float.MAX_VALUE);
		}
		return result;
	}
}

/**
 * Add value to each component of a vector.
 */
class VectorAddScalar extends BinaryVectorScalarInstruction {
	private static final long serialVersionUID = 1L;
	
	@Override
	float[] BinaryOperator(float[] inA, float inB) {
		float[] result = new float[inA.length];
		float temp;
		for(int i=0; i<inA.length; i++) {
			temp = inA[i]+inB;
			if(Float.isInfinite(temp) && temp > 0){
				temp = Float.MAX_VALUE;
			}
			if(Float.isInfinite(temp) && temp < 0){
				temp = (1.0f - Float.MAX_VALUE);
			}
			result[i] = temp;
		}
		return result;
	}
}

/**
 * Subtract value from each component of a vector.
 */
class VectorSubScalar extends BinaryVectorScalarInstruction {
	private static final long serialVersionUID = 1L;
	
	@Override
	float[] BinaryOperator(float[] inA, float inB) {
		float[] result = new float[inA.length];
		float temp;
		for(int i=0; i<inA.length; i++) {
			temp = inA[i]-inB;
			if(Float.isInfinite(temp) && temp > 0){
				temp = Float.MAX_VALUE;
			}
			if(Float.isInfinite(temp) && temp < 0){
				temp = (1.0f - Float.MAX_VALUE);
			}
			result[i] = temp;
		}
		return result;
	}
}

/**
 * Multiply each component of a vector by a value.
 */
class VectorMulScalar extends BinaryVectorScalarInstruction {
	private static final long serialVersionUID = 1L;
	
	@Override
	float[] BinaryOperator(float[] inA, float inB) {
		float[] result = new float[inA.length];
		float temp;
		for(int i=0; i<inA.length; i++) {
			temp = inA[i]*inB;
			if(Float.isInfinite(temp) && temp > 0){
				temp = Float.MAX_VALUE;
			}
			if(Float.isInfinite(temp) && temp < 0){
				temp = (1.0f - Float.MAX_VALUE);
			}
			if(Float.isNaN(temp)){
				temp = 0.0f;
			}
			result[i] = temp;
		}
		return result;
	}
}

/**
 * Divide each component of a vector by a value.
 */
class VectorDivScalar extends BinaryVectorScalarInstruction {
	private static final long serialVersionUID = 1L;
	
	@Override
	float[] BinaryOperator(float[] inA, float inB) {
		float[] result = new float[inA.length];
		float temp;
		for(int i=0; i<inA.length; i++) {
			temp = inA[i]/inB;
			if(Float.isInfinite(temp) && temp > 0){
				temp = Float.MAX_VALUE;
			}
			if(Float.isInfinite(temp) && temp < 0){
				temp = (1.0f - Float.MAX_VALUE);
			}
			if(Float.isNaN(temp)){
				temp = 0.0f;
			}
			result[i] = temp;
		}
		return result;
	}
}

/**
 * Vector magnitude.
 */
class VectorMag extends UnaryVectorScalarInstruction {
	private static final long serialVersionUID = 1L;
	
	@Override
	float UnaryOperator(float[] inValue) {
		double result = 0;
		for(int i=0; i<inValue.length; i++) {
			result += inValue[i] * inValue[i];
		}
		result = Math.sqrt(result);
		if(Double.isInfinite(result) && result > 0){
			result = Float.MAX_VALUE;
		}
		if(Double.isInfinite(result) && result < 0){
			result = (1.0f - Float.MAX_VALUE);
		}
		return (float)result;
	}
}

/**
 * Adds a value to the specified vector component.
 */
class VectorDimAdd extends TernaryVectorInstruction {
	private static final long serialVersionUID = 1L;
	
	@Override
	float[] TernaryOperator(float[] inA, int inB, float inC) {
		if(inB<0) inB = -inB;
		int dim = inB % inA.length;
		float[] result = Arrays.copyOf(inA, inA.length);
		if(dim>=0 && dim<inA.length) { // may not be the case with extreme values
			float temp = inA[inB % inA.length] + inC;
			if(Float.isInfinite(temp) && temp > 0){
				temp = Float.MAX_VALUE;
			}
			if(Float.isInfinite(temp) && temp < 0){
				temp = (1.0f - Float.MAX_VALUE);
			}
			result[dim] = temp;
		}
		return result;
	}
}

/**
 * Subtracts a value from the specified vector component.
 */
class VectorDimMul extends TernaryVectorInstruction {
	private static final long serialVersionUID = 1L;
	
	@Override
	float[] TernaryOperator(float[] inA, int inB, float inC) {
		if(inB<0) inB = -inB;
		int dim = inB % inA.length;
		float[] result = Arrays.copyOf(inA, inA.length);
		if(dim>=0 && dim<inA.length) { // may not be the case with extreme values
			float temp = inA[inB % inA.length] * inC;
			if(Float.isInfinite(temp) && temp > 0){
				temp = Float.MAX_VALUE;
			}
			if(Float.isInfinite(temp) && temp < 0){
				temp = (1.0f - Float.MAX_VALUE);
			}
			result[dim] = temp;
		}
		return result;
	}
}

/**
 * Applies code to pairwise components of two vectors.
 */
class VectorZip extends Instruction {
	private static final long serialVersionUID = 1L;

	@Override
	public void Execute(Interpreter inI) {
		floatStack fstack = inI.floatStack();
		vectorStack vstack = inI.vectorStack();
		ObjectStack estack = inI.execStack();
		ObjectStack cstack = inI.codeStack();
		
		if (vstack.size() > 1 && estack.size() > 0) {
			float[] v1 = vstack.pop();
			float[] v2 = vstack.pop();
			float[] v3 = new float[v1.length];
			Object ins = estack.pop();
			inI._execStack = new ObjectStack();
			inI._codeStack = new ObjectStack();
			for(int i=0; i<v1.length; i++) {
				fstack.push(v1[i]);
				fstack.push(v2[i]);
				if(ins instanceof Program)
					inI.Execute((Program)ins, 100);
				if(ins instanceof Instruction)
					inI.ExecuteInstruction(ins);
				if(fstack.size() > 0) {
					v3[i] = fstack.pop();
				}
				inI._execStack.clear();
				inI._codeStack.clear();
			}
			inI._execStack = estack;
			inI._codeStack = cstack;
			vstack.push(v3);
		}
	}
}

/**
 * Applies code to each component of a vector.
 */
class VectorApply extends Instruction {
	private static final long serialVersionUID = 1L;

	@Override
	public void Execute(Interpreter inI) {
		floatStack fstack = inI.floatStack();
		vectorStack vstack = inI.vectorStack();
		ObjectStack estack = inI.execStack();
		ObjectStack cstack = inI.codeStack();
		
		if (vstack.size() > 1 && estack.size() > 0) {
			float[] v1 = vstack.pop();
			float[] v3 = new float[v1.length];
			Object ins = estack.pop();
			inI._execStack = new ObjectStack();
			inI._codeStack = new ObjectStack();
			for(int i=0; i<v1.length; i++) {
				fstack.push(v1[i]);
				if(ins instanceof Program)
					inI.Execute((Program)ins, 100);
				if(ins instanceof Instruction)
					inI.ExecuteInstruction(ins);
				if(fstack.size() > 0) {
					v3[i] = fstack.pop();
				}
				inI._execStack.clear();
				inI._codeStack.clear();
			}
			inI._execStack = estack;
			inI._codeStack = cstack;
			vstack.push(v3);
		}
	}
}

/**
 * If [0,1], returns a point on the line between two vectors.
 * Otherwise extends proportionally beyond the end of the line.
 */
class VectorBetween extends Instruction {
	private static final long serialVersionUID = 1L;

	@Override
	public void Execute(Interpreter inI) {
		floatStack fstack = inI.floatStack();
		vectorStack vstack = inI.vectorStack();
		
		if (vstack.size() > 1 && fstack.size() > 0) {
			float[] v2 = vstack.pop();
			float[] v1 = vstack.pop();
			float[] v3 = new float[v1.length];
			float f = fstack.pop();
			
			for(int i=0; i<v1.length; i++) {
				v3[i] = v1[i] + (f * (v2[i] - v1[i]));
				if(Float.isInfinite(v3[i]) && v3[i] > 0){
					v3[i] = Float.MAX_VALUE;
				}
				if(Float.isInfinite(v3[i]) && v3[i] < 0){
					v3[i] = (1.0f - Float.MAX_VALUE);
				}
				if(Float.isNaN(v3[i])){
					v3[i] = v1[i];
				}
			}
			
			vstack.push(v3);
		}
	}
}

/**
 * Returns the personal best of the swarm member whose index is
 * specified on the integer stack (modulus the swarm size).
 */
class VectorBest extends Instruction {
	private static final long serialVersionUID = 1L;
	private static PopulationOptimisation _pop;
	
	public static void setPopulation(PopulationOptimisation pop) {
		_pop = pop;
	}
		
	@Override
	public void Execute(Interpreter inI) {
		intStack istack = inI.intStack();
		int i;
		if(istack.size()<1)
			i = inI.current; // current population member
		else
			i = istack.pop();
		if(i<0)
			i = inI.current;
		float[] best = _pop.getPosition(i, true);
		inI.vectorStack().push(Arrays.copyOf(best, best.length));
	}
}

/**
 * Returns the current search point of the swarm member whose index
 * is specified on the integer stack (modulus the swarm size).
 */
class VectorCurrent extends Instruction {
	private static final long serialVersionUID = 1L;
	private static PopulationOptimisation _pop;
	
	public static void setPopulation(PopulationOptimisation pop) {
		_pop = pop;
	}
		
	@Override
	public void Execute(Interpreter inI) {
		intStack istack = inI.intStack();
		int i;
		if(istack.size()<1)
			i = inI.current; // current population member
		else
			i = istack.pop();
		if(i<0)
			i = inI.current;
		float[] current = _pop.getPosition(i, false);
		inI.vectorStack().push(Arrays.copyOf(current, current.length));
	}
}