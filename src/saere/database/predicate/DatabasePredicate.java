package saere.database.predicate;

import static saere.database.Utils.hasFreeVariable;

import java.util.Iterator;

import saere.Solutions;
import saere.State;
import saere.StringAtom;
import saere.Term;
import saere.database.Database;
import saere.database.DatabaseAdapter;
import saere.database.profiling.Profiler;
import saere.database.profiling.Profiler.Mode;

/**
 * Represents a database procedure and provides an array-based (i.e., <b>rather 
 * slow</b>) <i>default implementation</i>.
 * 
 * @author David Sullivan
 * @version 0.4, 11/20/2010
 */
public class DatabasePredicate {
	
	private static final Profiler PROFILER = Profiler.getInstance();
	
	protected final boolean noCollision;
	protected final StringAtom functor;
	protected final int arity;
	protected final DatabaseAdapter adapter;
	
	public DatabasePredicate(String functor, int arity, Database database) {
		this.functor = StringAtom.instance(functor);
		this.arity = arity;
		this.adapter = database.getAdapter(this.functor, arity); // XXX Assumes that entries already exist
		this.noCollision = database.noCollision();
	}

	/**
	 * Attemps to unify with this predicate. To be used only if the number of
	 * arguments can not be determined. Any subclass extending this abstract
	 * class should offer a unify method with a fixed number (arity) of term
	 * arguments.
	 * 
	 * @param query The query.
	 * @return The {@link Solutions} of this unification.
	 */
	public Solutions unify(Term query) {		
		if (arity == query.arity() && functor.sameAs(query.functor())) {
			
			// Profile only if this query is for this predicate
			if (PROFILER.mode() == Mode.PROFILE) {
				PROFILER.profile(query);
			}
			
			if (!noCollision) {
				return new DatabaseSolutions(query);
				//return GenericSolutions.forArity(arity, adapter, query);
			} else {
				
				return new DatabaseSolutionsNoCollision(query);
				//return GenericSolutions.forArityNoCollision(arity, adapter, query);
			}
		} else {
			return EmptySolutions.getInstance();
		}
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
	
	/**
	 * A generic solutions implementation for database predicates. It expects 
	 * candidate sets.
	 * 
	 * @author David Sullivan
	 * @version 0.1, 12/5/2010
	 */
	private class DatabaseSolutions implements Solutions {
		
		private final Term[] args;
		private final State[] states;
		private final Iterator<Term> iterator;
		
		private int progress;
		
		private DatabaseSolutions(Term query) {
			
			// Get arguments
			args = new Term[query.arity()]; // arity() should be inlined...
			for (int i = 0; i < query.arity(); i++) {
				args[i] = query.arg(i);
			}
			
			// Save states
			states = new State[query.arity()];
			for (int i = 0; i < query.arity(); i++) {
				states[i] = args[i].manifestState();
			}
			
			iterator = adapter.query(query);
		}
		
		@Override
		public boolean next() {			
			
			// As long as we have potentially matching facts...
			while (iterator.hasNext()) {
				resetStates();
				
				Term fact = iterator.next();
				progress = 0;
				for (int i = 0; i < args.length; i++) {
					if (args[i].unify(fact.arg(i))) progress++;
					else break;
				}
				
				if (progress == args.length)
					return true;
			}
			
			resetStates();
			return false;
		}
		
		/**
		 * Restores the states of the goal with regard to progress.
		 */
		private void resetStates() {
			for (int i = 0; i < progress; i++) {
				if (states[i] != null) states[i].reset();
			}
		}

		@Override
		public boolean choiceCommitted() {
			throw new UnsupportedOperationException();
		}

		@Override
		public void abort() {
			throw new UnsupportedOperationException();
		}
	}
	
	/**
	 * A generic solutions implementation for database predicates. It expects 
	 * exact result sets.
	 * 
	 * @author David Sullivan
	 * @version 0.1, 12/5/2010
	 */
	private class DatabaseSolutionsNoCollision implements Solutions {
		
		private final Term[] args;
		private final State[] states;
		private final boolean[] vars;
		private final Iterator<Term> iterator;
		
		private DatabaseSolutionsNoCollision(Term query) {	
			assert noCollision : "No collisions expected";
		
			// Save positions of arguments with free variables
			vars = new boolean[query.arity()];
			for (int i = 0; i < query.arity(); i++) {
				vars[i] = hasFreeVariable(query.arg(i));
			}
			
			// Get arguments (only for arguments with free variables)
			args = new Term[query.arity()]; // arity() should be inlined...
			for (int i = 0; i < query.arity(); i++) {
				if (vars[i]) args[i] = query.arg(i);
			}
			
			// Save states (only for arguments with free variables)
			states = new State[query.arity()];
			for (int i = 0; i < query.arity(); i++) {
				if (vars[i]) states[i] = args[i].manifestState();
			}
			
			iterator = adapter.query(query);
		}

		@Override
		public boolean next() {
			
			// We get only terms that'll unify (no set of 'maybe' candidates)
			if (iterator.hasNext()) {
				resetStates();
				
				Term fact = iterator.next();
				for (int i = 0; i < args.length; i++) {
					if (vars[i]) args[i].unify(fact.arg(i));
				}
				
				return true;
			} else {
				//System.out.print("[" + counter + " candidates iterated]");
				resetStates();
				return false;
			}
		}
		
		/**
		 * Restores the states of the goal with regard to free variable 
		 * arguments in the query.
		 */
		private void resetStates() {
			for (int i = 0; i < args.length; i++) {
				if (vars[i] && states[i] != null) states[i].reset();
			}
		}

		@Override
		public boolean choiceCommitted() {
			throw new UnsupportedOperationException();
		}

		@Override
		public void abort() {
			throw new UnsupportedOperationException();
		}
	}
}
