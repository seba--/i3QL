package saere.database;

import java.util.Iterator;

import saere.Solutions;
import saere.State;
import saere.StringAtom;
import saere.Term;

/**
 * Represents a database procedure and provides an array-based (<b>slow</b>) 
 * <i>default implementation</i> that uses the {@link Database_Default}.
 * 
 * @author David Sullivan
 * @version $Id$
 */
public class DatabasePredicate {
	
	protected final StringAtom functor;
	protected final int arity;
	
	protected DatabasePredicate(String functor, int arity) {
		this.functor = DatabaseTermFactory.makeStringAtom(functor);
		this.arity = arity;
	}

	/**
	 * Attemps to unify with this predicate. To be used only if the number of
	 * arguments can not be determined. Any subclass extending this abstract
	 * class should offer a unify method with a fixed number (arity) of term
	 * arguments.
	 * 
	 * @param terms
	 * @return
	 */
	public Solutions unify(Term... terms) {
		return new DatabaseSolutions(terms);
	}

	@Override
	public String toString() {
		return functor.toString() + "/" + arity;
	}

	public StringAtom functor() {
		return functor;
	}

	public int arity() {
		return arity;
	}
	
	/**
	 * A generic solution class for database. Only for demonstration purposes 
	 * and not actual use.
	 * 
	 * @author David Sullivan
	 * @version $Id$
	 */
	protected class DatabaseSolutions implements Solutions {
		
		private Term[] args;
		private State[] states;
		private Iterator<Term> iterator;
		
		protected DatabaseSolutions(Term[] args) {
			this.args = args;
			save();
			iterator = Database_Default.getInstance().getFacts(functor).iterator();
		}

		public boolean next() {			
			// reset to original states (unnecessary for first call)
			load();
			
			// as long as we have potentially matching facts...
			while (iterator.hasNext()) {
				Term fact = iterator.next();
				
				if (unifies(fact)) {
					// unification succeded
					return true;
				} else {
					// unifiation failed, reset states
					load();
				}
			}
			
			return false;
		}
		
		/**
		 * Checks wether the specified fact unifies with the goal.
		 * 
		 * @param fact The fact.
		 * @return <tt>true</tt> if so.
		 */
		private boolean unifies(Term fact) {
			assert fact.functor().sameAs(functor) : "Cannot unify";
			
			if (fact.arity() != arity)
				return false;
			
			boolean unifies = true;
			for (int i = 0; i < args.length; i++) {
				if (!args[i].unify(fact.arg(i))) {
					unifies = false;
					break;
				}	
			}
			
			return unifies;
		}
		
		/**
		 * Save the states of the goal.
		 */
		private void save() {
			states = new State[args.length];
			for (int i = 0; i < args.length; i++) {
				states[i] = args[i].manifestState();
			}
		}
		
		/**
		 * Restore the states of the goal.
		 */
		private void load() {
			for (int i = 0; i < args.length; i++) {
				args[i].setState(states[i]);
			}
		}
	}
}
