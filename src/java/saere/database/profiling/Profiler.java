package saere.database.profiling;

import static saere.database.Utils.timestamp;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.Map.Entry;

import saere.StringAtom;
import saere.Term;
import saere.database.index.FunctorLabel;
import scala.actors.threadpool.Arrays;

public class Profiler {
	
	private static final Profiler INSTANCE = new Profiler();
	private static final String DEFAULT_STORAGE_PATH = "src" + File.separator + "saere" + File.separator + "database" + File.separator + "profiling";
	
	private final IdentityHashMap<FunctorLabel, PredicateProfiler> predicates;
	private final IdentityHashMap<FunctorLabel, int[]> orders;
	private final QueryRater rater;
	
	private HashMap<FunctorKey, int[]> serializableOrders;
	private Mode mode;	
	
	private Profiler() {
		predicates = new IdentityHashMap<FunctorLabel, PredicateProfiler>();
		orders = new IdentityHashMap<FunctorLabel, int[]>();
		serializableOrders = new HashMap<FunctorKey, int[]>();
		rater = new QueryRater(orders);
		mode = Mode.OFF;
	}
	
	public static Profiler getInstance() {
		return INSTANCE;
	}
	
	@SuppressWarnings("unchecked")
	public void loadProfiles(String filename) {
		orders.clear();
		
		FileInputStream fis = null;
		ObjectInputStream ois = null;
		try {
			fis = new FileInputStream(filename);
			ois = new ObjectInputStream(fis);
			serializableOrders = (HashMap<FunctorKey, int[]>) ois.readObject();
			
			fillOrdersMapAfterDeserialization();
		} catch (Exception e) {
			System.err.println("Unable to load serialization from file " + filename);
			e.printStackTrace();
		} finally {
			if (fis != null) try { fis.close(); } catch (Exception e) { /* ignored */ }
			if (ois != null) try { ois.close(); } catch (Exception e) { /* ignored */ }
		}
	}
	
	public void saveProfiles() {
		saveProfiles(DEFAULT_STORAGE_PATH + File.separator + "Profiling-" + timestamp() + ".ser");
	}
	
	public void saveProfiles(String filename) {
		fillOrdersMapBeforeSerialization();
		
		FileOutputStream fos = null;
		ObjectOutputStream oos = null;
		try {
			fos = new FileOutputStream(filename);
			oos = new ObjectOutputStream(fos);
			oos.writeObject(serializableOrders);
		} catch (Exception e) {
			System.err.println("Unable to serialize to file " + filename);
			e.printStackTrace();
		} finally {
			if (fos != null) try { fos.close(); } catch (Exception e) { /* ignored */ }
			if (oos != null) try { oos.close(); } catch (Exception e) { /* ignored */ }
		}
	}
	
	// Delegate the query to the responsible predicate profiler
	public void profile(Term query) {
		assert mode == Mode.PROFILE : "Clients should call this method only if the mode is " + Mode.PROFILE;
		
		FunctorLabel functorLabel = FunctorLabel.FunctorLabel(query.functor(), query.arity());
		PredicateProfiler predicateProfiler = predicates.get(functorLabel);
		if (predicateProfiler == null) {
			predicateProfiler = new PredicateProfiler(this, functorLabel);
			predicates.put(functorLabel, predicateProfiler);
		}
		predicateProfiler.profile(query);
	}
	
	// Fills the serializable orders map will all profiled orders
	private void fillOrdersMapBeforeSerialization() {
		for (Entry<FunctorLabel, PredicateProfiler> entry : predicates.entrySet()) {
			FunctorLabel functorLabel = entry.getKey();
			serializableOrders.put(new FunctorKey(functorLabel.atom().toString(), functorLabel.arity()), entry.getValue().order());
		}
	}
	
	// Fills the (non-serializable) orders map from the serializable orders map
	private void fillOrdersMapAfterDeserialization() {
		for (Entry<FunctorKey, int[]> entry : serializableOrders.entrySet()) {
			FunctorKey functorKey = entry.getKey();
			orders.put(FunctorLabel.FunctorLabel(StringAtom.instance(functorKey.functor()), functorKey.arity()), entry.getValue());
		}
	}
	
	/**
	 * Gets a (new) order to the arguments of a fact to be inserted or a 
	 * query.
	 * The goal of this is that for frequent queries free variables are 
	 * positioned at the end of the flattened term representation.
	 * 
	 * @param term A fact or a query.
	 * @return An array that contains the arguments of the specified term in a new order.
	 */
	public Term[] getOrderedArgs(Term term) {
		assert mode == Mode.USE : "Clients should call this method only if the mode is " + Mode.USE;
		
		FunctorLabel functorLabel = FunctorLabel.FunctorLabel(term.functor(), term.arity());
		
		// Check wether an order is already cached in orders
		int[] order = orders.get(functorLabel);
		if (order != null) {
			return getOrder(term, order);
		} else {
			// Should not happen...
			//assert false : "No";
			
			// Check wether we have a order profiled in this run or not
			PredicateProfiler predicateProfiler = predicates.get(functorLabel);
			if (predicateProfiler != null) {
				order = predicateProfiler.order();
				orders.put(functorLabel, order);
				return getOrder(term, order);
			} else {
				Term[] args = new Term[term.arity()];
				for (int i = 0; i < args.length; i++) {
					args[i] = term.arg(i);
				}
				return args;
			}
		}		
	}
	
	private Term[] getOrder(Term term, int[] order) {
		Term[] args = new Term[order.length];
		for (int i = 0; i < order.length; i++) {
			args[i] = term.arg(order[i]);
		}
		return args;
	}
	
	/**
	 * Rates how &quot;good&quot; a trie will be for answering the query based 
	 * on a simple heuristic.
	 * 
	 * @param query The query to rate.
	 * @return A value between 0.0 (worst) and 1.0 (best).
	 */
	public float rate(Term query) {
		assert mode == Mode.USE : "Clients should call this method only if the mode is " + Mode.USE;
		return rater.rate(query);
	}
	
	public Mode mode() {
		return mode;
	}
	
	public void setMode(Mode mode) {
		this.mode = mode;
	}
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		for (Entry<FunctorLabel, int[]> order : orders.entrySet()) {
			sb.append(order.getKey() + " " + Arrays.toString(order.getValue()) + "\n");
		}
		return sb.toString();
	}
	
	/**
	 * The profiling mode of the query profiler.
	 */
	public enum Mode {
		
		/** Do the profiling (slows down the system). */
		PROFILE,
		
		/** Make use of profilings (may enhance queries). */
		USE,
		
		/** Completely disable profiling. */
		OFF
	}
}
