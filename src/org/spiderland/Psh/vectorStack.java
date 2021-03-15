package org.spiderland.Psh;

/**
 * Fixed-length vector stack for storing and manipulating search points.
 * 
 * @author michaellones
 */
public class vectorStack extends Stack {
	private static final long serialVersionUID = 1L;

	protected float _stack[][];
	
	protected static int _dims = 3; // made static due to ordering of initialisation
	
	public static void setDimensionality(int dimensions) {
		_dims = dimensions;
	}
	
	@Override
	void resize(int inSize) {
		float newstack[][] = new float[inSize][];

		if (_stack != null) {
			for(int i=0; i<_size; i++) {
				newstack[i] = _stack[i];
			}
		}

		_stack = newstack;
		_maxsize = inSize;
	}
	
	public void push(float[] inValue) {
		_stack[_size] = inValue;
		_size++;

		if (_size >= _maxsize)
			resize(_maxsize * 2);
	}
	
	public float[] pop() {
		float[] result = null;

		if (_size > 0) {
			result = _stack[_size - 1];
			_size--;
		}

		return result;
	}
	
	public float[] peek(int inIndex) {
		if (inIndex >= 0 && inIndex < _size)
			return _stack[inIndex];

		return null;
	}
	
	public float[] top() {
		return peek(_size - 1);
	}

	@Override
	public void dup() {
		if (_size > 0)
			push(_stack[_size - 1]);
	}

	@Override
	public void rot() {
		if (_size > 2) {
			float tmp[] = _stack[_size - 3];
			_stack[_size - 3] = _stack[_size - 2];
			_stack[_size - 2] = _stack[_size - 1];
			_stack[_size - 1] = tmp;
		}
	}

	@Override
	public void shove(int inIndex) {
		if (_size > 0) {
			if(inIndex < 0){
				inIndex = 0;
			}
			if(inIndex > _size - 1){
				inIndex = _size - 1;
			}
			
			float toShove[] = top();
			int shovedIndex = _size - inIndex - 1;

			for (int i = _size - 1; i > shovedIndex; i--) {
				_stack[i] = _stack[i - 1];
			}
			_stack[shovedIndex] = toShove;
		}
	}

	@Override
	public void swap() {
		if (_size > 1) {
			float tmp[] = _stack[_size - 1];
			_stack[_size - 1] = _stack[_size - 2];
			_stack[_size - 2] = tmp;
		}
	}

	@Override
	public void yank(int inIndex) {
		if (_size > 0) {
			if(inIndex < 0){
				inIndex = 0;
			}
			if(inIndex > _size - 1){
				inIndex = _size - 1;
			}

			int yankedIndex = _size - inIndex - 1;
			float toYank[] = peek(yankedIndex);

			for (int i = yankedIndex; i < _size - 1; i++) {
				_stack[i] = _stack[i + 1];
			}
			_stack[_size - 1] = toYank;
		}
	}

	@Override
	public void yankdup(int inIndex) {
		if (_size > 0) {
			if(inIndex < 0){
				inIndex = 0;
			}
			if(inIndex > _size - 1){
				inIndex = _size - 1;
			}

			int yankedIndex = _size - inIndex - 1;
			push(peek(yankedIndex));
		}
	}

	public String toString() {
		String result = "[";
		String element;
		
		for (int n = _size - 1; n >= 0; n--) {
			element = "<";
			for(int i=0; i<_stack[n].length; i++) {
				element += _stack[n][i];
				if(i<_stack[n].length-1)
					element += ",";
			}
			element += ">";
			if (n == _size - 1)
				result += element;
			else
				result += " " + element;
		}
		result += "]";

		return result;
	}
}
