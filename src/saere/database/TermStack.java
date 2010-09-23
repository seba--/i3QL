package saere.database;

import saere.Term;
import scala.actors.threadpool.Arrays;

/**
 * Mimics a (read-only) stack by wrapping a {@link Term} array. It's actually 
 * nothing more than a state, i.e., an integer position, of an array.<br />
 * <br />
 * <b>Note that a push operation is not supported.</b>
 * 
 * @author David Sullivan
 * @version $Id$
 */
public class TermStack {
	
	private final Term[] terms;
	
	private int position;
	
	public static void main(String[] args) {
		Term a = DatabaseTermFactory.makeStringAtom("a");
		Term b = DatabaseTermFactory.makeStringAtom("b");
		Term c = DatabaseTermFactory.makeStringAtom("c");
		Term i1 = DatabaseTermFactory.makeIntegerAtom(1);
		Term i2 = DatabaseTermFactory.makeIntegerAtom(2);
		Term i3 = DatabaseTermFactory.makeIntegerAtom(3);
		
		TermStack ts = new TermStack(new Term[] { a, b, c, i1, i2, i3 });
		System.out.println(ts);
		System.out.println("pop 2");
		ts.pop(2);
		System.out.println(ts);
		System.out.println("peek 2: " + ts.peek(2));
		System.out.println("pop 2");
		ts.pop(2);
		System.out.println(ts);
		System.out.println("back 3");
		ts.back(3);
		System.out.println(ts);
		System.out.println("back 10");
		ts.back(10);
		System.out.println(ts);
		System.out.println("pop 10");
		ts.pop(10);
		System.out.println(ts);
		System.out.println("back 6");
		ts.back(10);
		System.out.println(ts);
		System.out.println("peek 7: " + ts.peek(7));
	}
		
	/**
	 * Creates a new <tt>TermDeque</tt> with its first element at the first 
	 * index of the specified array.
	 * 
	 * @param terms The underlying array for this <tt>TermDeque</tt>.
	 */
	public TermStack (Term[] terms) {
		this(terms, 0);
	}
	
	/**
	 * Creates a new <tt>TermStack</tt> with its first element at the specified 
	 * <tt>position</tt> index of the specified array.
	 * 
	 * @param terms The underlying array for this <tt>TermStack</tt>.
	 * @param position The first element's index in <tt>terms</tt>.
	 */
	private TermStack(Term[] terms, int position) {
		this.terms = terms;
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
	public Term pop() {
		Term term = peek();
		position++;
		return term;
	}
	
	public Term pop(int number) {
		assert number > 0 && position + number < terms.length : "illegal number";
		
		Term term = pop();
		if (number > 1) {
			if (number > 2)
				position += (number - 2); // jump
			term = pop();
		}
		
		return term;
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
	public Term peek() {
		if (position < terms.length) {
			assert terms[position] != null : "Term stack element is null";
			return terms[position];
		} else {
			return null;
		}
	}
	
	public Term peek(int number) {
		assert number > 0 && position + number < terms.length : "illegal number";
		
		if (number == 1) {
			return peek();
		} else {
			int peekPosition = position + number;
			if (peekPosition < terms.length) {
				return terms[peekPosition - 1];
			} else {
				return null;
			}
		}
	}
	
	// XXX Time travel...
	public void back() {
		position = (position > 0) ? (position - 1) : 0;
	}
	
	public void back(int number) {
		position = (position > (number - 1)) ? (position - number) : 0;
	}
	
	/**
	 * The size of the <tt>TermStack</tt>.
	 * 
	 * @return The size of the <tt>TermStack</tt>.
	 */
	public int size() {
		int size = terms.length - position;
		return size > 0 ? size : 0;
	}
	
	/**
	 * Returns a copy of this <tt>TermStack</tt>, i.e., its current state.
	 * 
	 * @return A copy of this <tt>TermStack</tt>.
	 */
	public TermStack copy() {
		return new TermStack(terms, position);
	}
	
	public int length() {
		return terms.length;
	}
	
	/**
	 * Returns the stack in its <b>current state</b> as array.
	 * 
	 * @return The stack in its current state as array.
	 */
	public Term[] asArray() {
		Term[] array = new Term[size()];
		if (array.length > 0) {
			System.arraycopy(terms, position, array, 0, array.length);
		}
		return array;
	}
	
	// XXX Only for debugging!
	@Override
	public String toString() {
		if (size() > 0) {
			Term[] ts = new Term[size()];
			System.arraycopy(terms, position, ts, 0, size());
			return Arrays.toString(ts);
		} else {
			return "[]";
		}
	}
}
