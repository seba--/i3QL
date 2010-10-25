package saere.database.profiling;

import saere.Term;
import saere.database.Utils;

public class Insertion {
	
	/** The number of this insertion (this insertion was the <i>number-th</i> one). */
	private final int number;
	
	/** The time this insertion took in nano seconds. */
	private final long time;
	
	/** The term that was inserted. */
	private final Term term;
	
	/** Insertion mode */
	private final InsertionMode mode;
	
	public Insertion(int number, long time, Term term, InsertionMode mode) {
		this.number = number;
		this.time = time;
		this.term = term;
		this.mode = mode;
	}
	
	public int getNumber() {
		return number;
	}
	
	public long getTime() {
		return time;
	}
	
	public Term getTerm() {
		return term;
	}
	
	public enum InsertionMode {
		SHALLOW_SIMPLE,
		RECURSIVE_SIMPLE,
		SHALLOW_COMPLEX,
		RECURSIVE_COMPLEX
	}
	
	@Override
	public String toString() {
		return number + " " + time + " " + Utils.termToString(term) + " " + mode;
	}
	
	public String toCSV() {
		return number + "," + time + ",\"" + Utils.termToString(term).replace('\n', ' ').replace('\r', ' ') + "\"," + mode;
	}
}
