package saere.database.profiling;

import static saere.database.Utils.isFreeVariable;

import java.io.FileInputStream;
import java.io.ObjectInputStream;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.Map.Entry;

import saere.StringAtom;
import saere.Term;
import saere.database.BATTestQueries;
import saere.database.index.FunctorLabel;
import scala.actors.threadpool.Arrays;

// Should be fast (approach limited because nothing about selectivity is known)
public class QueryRater {
	
	public static void main(String[] args) {
		FileInputStream fis = null;
		ObjectInputStream ois = null;
		IdentityHashMap<FunctorLabel, int[]> orders = new IdentityHashMap<FunctorLabel, int[]>();
		HashMap<FunctorKey, int[]> serializableOrders;
		String filename = "src/saere/database/profiling/1291041307202.ser";
		try {
			fis = new FileInputStream(filename);
			ois = new ObjectInputStream(fis);
			serializableOrders = (HashMap<FunctorKey, int[]>) ois.readObject();
			
			for (Entry<FunctorKey, int[]> entry : serializableOrders.entrySet()) {
				FunctorKey functorKey = entry.getKey();
				orders.put(FunctorLabel.FunctorLabel(StringAtom.StringAtom(functorKey.functor()), functorKey.arity()), entry.getValue());
			}
		} catch (Exception e) {
			System.err.println("Unable to load serialization from file " + filename);
			e.printStackTrace();
		} finally {
			if (fis != null) try { fis.close(); } catch (Exception e) { /* ignored */ }
			if (ois != null) try { ois.close(); } catch (Exception e) { /* ignored */ }
		}
		
		QueryRater rater = new QueryRater(orders);
		for (Term query : BATTestQueries.ALL_QUERIES) {
			FunctorLabel functorLabel = FunctorLabel.FunctorLabel(query.functor(), query.arity());
			System.out.println("Query " + query + " has a rating of " + rater.rate(query) + " (order: " + Arrays.toString(orders.get(functorLabel)) + ")");
		}
	}
	
	// Only read access...
	private final IdentityHashMap<FunctorLabel, int[]> orders;
	
	public QueryRater(IdentityHashMap<FunctorLabel, int[]> orders) {
		this.orders = orders;
	}
	
	public float rate(Term term) {
		int[] order = orders.get(FunctorLabel.FunctorLabel(term.functor(), term.arity()));
		AbsoluteRatingResult result = absoluteRating(term, order);
		
		float rating = result.rating();
		return rating / result.maxRating();
	}
	
	private AbsoluteRatingResult absoluteRating(Term term, int[] order) {
		assert term.arity() == order.length : "Order doesn't match term length";
		
		boolean argIsNotVar[] = new boolean[order.length];
		for (int i = 0; i < order.length; i++) {
			argIsNotVar[i] = isFreeVariable(term.arg(i)) ? false : true;
		}
		
		float rating = 0;
		float maxRating = 0;
		int arity = term.arity();
		for (int i = 0; i < order.length; i++) {
			
			maxRating += ((i + 1F) / (order[i] + 1F));
			
			if (argIsNotVar[i]) {
				Term arg = term.arg(i);
				if (!arg.isCompoundTerm()) {
					rating += ((i + 1F) / (order[i] + 1F));
				} else {
					rating += 0.0;
					
					/*
					int[] orderForArg = orders.get(FunctorLabel.FunctorLabel(arg.functor(), arg.arity()));
					if (orderForArg != null) {
						AbsoluteRatingResult resultForArg = absoluteRating(arg, orderForArg);
						rating += resultForArg.rating();
						arity += resultForArg.length() - 1;
					} else {
						rating += absoluteDefaultRating(arg);
						arity += arg.arity() - 1;
					}
					*/
				}
			}
			
		}
		
		return new AbsoluteRatingResult(rating, maxRating, arity);
	}
	
	private int absoluteDefaultRating(Term term) {
		return (term.arity() % 2 == 0) ? term.arity() / 2 : (term.arity() + 1) / 2;
	}
	
	// Would be nice if Java had real tuples...
	private class AbsoluteRatingResult {
		private final float rating;
		private final int length;
		private final float maxRating;
		
		public AbsoluteRatingResult(float rating, float maxRating, int length) {
			this.rating = rating;
			this.length = length;
			this.maxRating = maxRating;
		}
		
		public float rating() {
			return rating;
		}
		
		public int length() {
			return length;
		}
		
		public float maxRating() {
			return maxRating;
		}
	}
}
