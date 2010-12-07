package saere.database.profiling;

import static saere.database.Utils.isFreeVariable;

import java.util.IdentityHashMap;

import saere.Term;
import saere.database.index.FunctorLabel;

/**
 * This class implements a (very) simple heuristic to estimate wether a trie 
 * based on the orders (from the {@link Profiler}) given the constructor is fit 
 * for a query. The value 0.0 is the worst and the value 1.0 the best possible 
 * result.<br>
 * <br>
 * A query is rated ({@link #rate(Term)}) based on the number of ground 
 * arguments (or instantiated variable arguments) and their reordered 
 * positions.<br>
 * <br>
 * The heuristic is limited because nothing is known about the selectivity of 
 * queries and their (ground) arguments.<br>
 * 
 * @author David Sullivan
 * @version 0.1, 12/7/2010
 */
public class QueryRater {
	
	// Only read access...
	private final IdentityHashMap<FunctorLabel, int[]> orders;
	
	public QueryRater(IdentityHashMap<FunctorLabel, int[]> orders) {
		this.orders = orders;
	}
	
	public float rate(Term term) {
		float termRatings = termRatings(term) * term.arity();
		int divisor = term.arity();
		
		// Check for nested compound terms (arguments)
		for (int i = 0; i < term.arity(); i++) {
			if (term.arg(i).isCompoundTerm()) {
				Term compoundTermArg = term.arg(i);
				termRatings += (termRatings(compoundTermArg) * compoundTermArg.arity());
				divisor += compoundTermArg.arity();
			}
		}
		
		// Return weighted average...
		return termRatings / divisor;
	}
	
	private float termRatings(Term term) {
		final int[] order = orders.get(FunctorLabel.FunctorLabel(term.functor(), term.arity()));
		assert order != null : "No order for " + term;
		
		float argRatings = 0F;
		float maxRatings = 0F; // or cache?
		
		for (int i = 0; i < term.arity(); i++) {
			final float argRating = 1F / (order[i] + 1F);
			maxRatings += argRating;
			
			if (!isFreeVariable(term.arg(i)))
				argRatings += argRating;
		}
		
		return argRatings / maxRatings;
	}
}
