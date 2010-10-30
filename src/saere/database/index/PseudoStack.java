package saere.database.index;

import saere.Term;
import scala.actors.threadpool.Arrays;

/**
 * Mimics a (read-only) stack by wrapping an array. It's actually 
 * nothing more than a state, i.e., an integer position, of an array.<br />
 * <br />
 * <b>Note that a push operation is not supported.</b>
 * 
 * @author David Sullivan
 * @version 0.7, 10/17/2010
 */
public abstract class PseudoStack<T> {
	
	protected T[] values;
	protected int position;
		
	public PseudoStack (T[] values) {
		this(values, 0);
	}
	
	/**
	 * Creates a new <tt>InsertStack</tt> with its first element at the specified 
	 * <tt>position</tt> index of the specified array.
	 * 
	 * @param terms The underlying array for this <tt>InsertStack</tt>.
	 * @param position The first element's index in <tt>terms</tt>.
	 */
	private PseudoStack(T[] values, int position) {
		this.values = values;
		this.position = position;
	}
	
	/**
	 * Retrieves the first element and moves the position forward. That 
	 * is, multiple calls will always return the (new) first element until no 
	 * element is left.<br/>
	 * <br/>
	 * <b>Warning: Doesn't differ between an out of bound call and a null 
	 * element.</b>
	 * 
	 * @return The first element.
	 */
	public T pop() {
		T value = peek();
		position++;
		return value;
	}
	
	public T pop(int number) {
		assert number > 0 && position + number < values.length : "Illegal number " + number;
		
		T value = pop();
		if (number > 1) {
			if (number > 2)
				position += (number - 2); // jump
			value = pop();
		}
		
		return value;
	}
	
	/**
	 * Retrieves the first element but does not move the position forward. That 
	 * is, multiple calls will always return the same first element.<br/>
	 * <br/>
	 * <b>Warning: Doesn't differ between an out of bound call and a null 
	 * element.</b>
	 * 
	 * @return The first element.
	 */
	public T peek() {
		if (position < values.length) {
			assert values[position] != null : "Term stack element is null";
			return values[position];
		} else {
			return null;
		}
	}
	
	public T peek(int number) {
		assert number > 0 && position + number < values.length : "illegal number";
		
		if (number == 1) {
			return peek();
		} else {
			int peekPosition = position + number;
			if (peekPosition < values.length) {
				return values[peekPosition - 1];
			} else {
				return null;
			}
		}
	}
	
	public void back() {
		position = (position > 0) ? (position - 1) : 0;
	}
	
	public void back(int number) {
		position = (position > (number - 1)) ? (position - number) : 0;
	}
	
	/**
	 * The size of the <tt>InsertStack</tt>.
	 * 
	 * @return The size of the <tt>InsertStack</tt>.
	 */
	public int size() {
		int size = values.length - position;
		return size > 0 ? size : 0;
	}
	
	public int length() {
		return values.length;
	}
	
	/**
	 * Returns the stack in its <b>current state</b> as array.
	 * 
	 * @return The stack in its current state as array.
	 */
	// As we cannot create generic arrays...
	public abstract T[] asArray();
	
	// XXX Only for debugging!
	@Override
	public String toString() {
		if (size() > 0) {
			Term[] ts = new Term[size()];
			System.arraycopy(values, position, ts, 0, size());
			return Arrays.toString(ts);
		} else {
			return "[]";
		}
	}
}
