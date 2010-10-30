package saere.database.predicate;

import java.util.Iterator;

import saere.Solutions;
import saere.State;
import saere.StringAtom;
import saere.Term;
import saere.database.Database;
import saere.database.ListDatabase;
import saere.database.TrieDatabase;

/**
 * Represents a database procedure and provides an array-based (i.e., <b>rather 
 * slow</b>) <i>default implementation</i>.
 * 
 * @author David Sullivan
 * @version 0.3, 10/14/2010
 */
public class DatabasePredicate {
	
	protected static Database database;
	
	protected final StringAtom functor;
	protected final int arity;
	
	protected DatabasePredicate(String functor, int arity) {
		this.functor = StringAtom.StringAtom(functor);
		this.arity = arity;
		database = ListDatabase.getInstance(); // use lists by default
	}

	/**
	 * Attemps to unify with this predicate. To be used only if the number of
	 * arguments can not be determined. Any subclass extending this abstract
	 * class should offer a unify method with a fixed number (arity) of term
	 * arguments.
	 * 
	 * @param terms The predicate arguments.
	 * @return The {@link Solutions} of this unification.
	 */
	public Solutions unify(Term... terms) {
		return new DatabaseSolutions(terms);
	}

	@Override
	public String toString() {
		return functor + "/" + arity;
	}

	public StringAtom functor() {
		return functor;
	}

	public int arity() {
		return arity;
	}
	
	public static void useTries() {
		database = TrieDatabase.getInstance();
	}
	
	public static void useLists() {
		database = ListDatabase.getInstance();
	}
	
	/**
	 * A generic solution class for database. Only for demonstration purposes 
	 * and not actual use.
	 * 
	 * @author David Sullivan
	 * @version 0.2, 9/21/2010
	 */
	protected class DatabaseSolutions implements Solutions {
		
		private Term[] args;
		private State[] states;
		private Iterator<Term> iterator;
		
		protected DatabaseSolutions(Term[] args) {
			this.args = args;
			save();
			Term[] argsWithFunctor = new Term[args.length + 1];
			argsWithFunctor[0] = functor;
			System.arraycopy(args, 0, argsWithFunctor, 1, args.length);
			iterator = database.getCandidates(argsWithFunctor);
		}

		public boolean next() {			
			
			// reset to original states (not necessary for first call)
			load();
			
			// as long as we have potentially matching facts...
			while (iterator.hasNext()) {
				Term fact = iterator.next();
				
				if (unifies(fact)) {
					return true; // unification succeded
				} else {
					load(); // unifiation failed, reset states
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
		 * Saves the states of the goal.
		 */
		private void save() {
			states = new State[args.length];
			for (int i = 0; i < args.length; i++) {
				states[i] = args[i].manifestState();
			}
		}
		
		/**
		 * Restores the states of the goal.
		 */
		private void load() {
			for (int i = 0; i < args.length; i++) {
				args[i].setState(states[i]);
			}
		}
	}
}
