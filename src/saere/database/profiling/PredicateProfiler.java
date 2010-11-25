package saere.database.profiling;

import static saere.database.Utils.*;

import saere.Term;
import saere.database.index.FunctorLabel;
import scala.actors.threadpool.Arrays;

/**
 * Profiles queries for predicates.
 * 
 * @author David Sullivan
 * @version 0.1, 11/25/2010
 */
public class PredicateProfiler {
	
	private final QueryProfiler profiler;
	private final FunctorLabel functorLabel;
	
	public static void main(String[] args) {
		int iargs[] = { 7, 3, 11 };
		
		OrderTuple[] newOrder = new OrderTuple[iargs.length];
		for (int i = 0; i < iargs.length; i++) {
			newOrder[i] = new OrderTuple(i, iargs[i]);
		}
		Arrays.sort(newOrder);
		
		System.out.println(Arrays.deepToString(newOrder));
	}
	
	// The number of queries for this predicate
	private int queryNum;
	
	// Counts the 
	private int[] instantiatedArgs;
	
	public PredicateProfiler(QueryProfiler profiler, FunctorLabel functorLabel) {
		this.profiler = profiler;
		this.functorLabel = functorLabel;
		instantiatedArgs = new int[functorLabel.arity()];
	}
	
	public void profile(Term query) {
		assert query.isCompoundTerm() : "Query is not a compound term";
		assert FunctorLabel.FunctorLabel(query.functor(), query.arity()) == functorLabel : "Wrong predicate " + query.functor() + "/" + query.arity() + " for predicate profiler " + toString();
		
		queryNum++;
		
		for (int i = 0; i < query.arity(); i++) {
			Term arg = query.arg(i);
			
			// Check wether the argument is not a free variable, if not increase the respective counter
			if (!isFreeVariable(arg)) {
				instantiatedArgs[i]++;
				
				// Recursively profile if the argument is a compound term but not a free variable
				if (arg.isCompoundTerm()) {
					profiler.profile(arg);
				}
			}
		}
	}
	
	public int[] order() {
		assert queryNum > 0  : "Cannot get order for a predicate that was not queried";
		
		/*
		// Default if nothing was profiled
		if (queryNum == 0) {
			return new int[instantiatedArgs.length];
		}
		*/
		
		OrderTuple[] orderTuples = new OrderTuple[instantiatedArgs.length];
		for (int i = 0; i < instantiatedArgs.length; i++) {
			orderTuples[i] = new OrderTuple(i, instantiatedArgs[i]);
		}
		Arrays.sort(orderTuples);
		int order[] = new int[instantiatedArgs.length];
		for (int i = 0; i < instantiatedArgs.length; i++) {
			order[i] = orderTuples[i].oldIndex();
		}
		
		return order;
	}
	
	@Override
	public String toString() {
		return functorLabel + "(" + queryNum + ")" + Arrays.toString(instantiatedArgs);
	}
}
