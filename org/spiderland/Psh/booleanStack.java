//
// Hooray for lame Java generics!
// 

package org.spiderland.Psh;

/**
 * The Push stack type for booleans.
 */

public class booleanStack extends Stack {
	private static final long serialVersionUID = 1L;

	protected boolean _stack[];

	@Override
	public boolean equals(Object obj) {
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		final booleanStack other = (booleanStack) obj;
		if (_size != other._size)
			return false;
		for (int i = 0; i < _size; i++)
			if (_stack[i] != other._stack[i])
				return false;
		return true;
	}

	@Override
	public int hashCode() {
		int hash = 7;
		for (int i = 0; i < _size; i++)
			hash = 41 * hash + Boolean.valueOf(_stack[i]).hashCode();
		return hash;
	}

	void resize(int inSize) {
		boolean newstack[] = new boolean[inSize];

		if (_stack != null)
			System.arraycopy(_stack, 0, newstack, 0, _size);

		_stack = newstack;
		_maxsize = inSize;
	}

	public boolean top() {
		if (_size > 0)
			return peek(_size - 1);

		return false;
	}

	public boolean peek(int inIndex) {
		if (inIndex >= 0 && inIndex < _size)
			return _stack[inIndex];

		return false;
	}

	public boolean pop() {
		boolean result = false;

		if (_size > 0) {
			result = _stack[_size - 1];
			_size--;
		}

		return result;
	}

	public void push(boolean inValue) {
		_stack[_size] = inValue;
		_size++;

		if (_size >= _maxsize)
			resize(_maxsize * 2);
	}

	public void dup() {
		if (_size > 0)
			push(_stack[_size - 1]);
	}

	public void swap() {
		if (_size > 1) {
			boolean tmp = _stack[_size - 1];
			_stack[_size - 1] = _stack[_size - 2];
			_stack[_size - 2] = tmp;
		}
	}

	public void rot() {
		if (_size > 2) {
			boolean tmp = _stack[_size - 3];
			_stack[_size - 3] = _stack[_size - 2];
			_stack[_size - 2] = _stack[_size - 1];
			_stack[_size - 1] = tmp;
		}
	}

	public String toString() {
		String result = "[";

		for (int n = _size - 1; n >= 0; n--) {
			if (n == _size - 1)
				result += _stack[n];
			else
				result += " " + _stack[n];
		}
		result += "]";

		return result;
	}
}
