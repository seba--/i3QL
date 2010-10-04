package saere.database.profiling;

import saere.Term;

public class Insertion {
	
	/** The number of this insertion (this insertion was the <i>number-th</i> one). */
	private final int number;
	
	/** The time this insertion took in nano seconds. */
	private final int time;
	
	/** The term that was inserted. */
	private final Term term;
	
	public Insertion(int number, int time, Term term) {
		this.number = number;
		this.time = time;
		this.term = term;
	}
	
	public int getNumber() {
		return number;
	}
	
	public int getTime() {
		return time;
	}
	
	public Term getTerm() {
		return term;
	}
}
