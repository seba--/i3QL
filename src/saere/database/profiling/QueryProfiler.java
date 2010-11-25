package saere.database.profiling;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.Map.Entry;

import saere.StringAtom;
import saere.Term;
import saere.database.index.FunctorLabel;

public class QueryProfiler {
	
	private static final QueryProfiler INSTANCE = new QueryProfiler();
	
	private IdentityHashMap<FunctorLabel, PredicateProfiler> profiledPredicates;
	private IdentityHashMap<FunctorLabel, int[]> orders;
	
	private HashMap<FunctorKey, int[]> serializableOrders;
	
	public static void main(String[] args) {
		Date time = Calendar.getInstance().getTime();
		System.out.println(time);
	}
	
	private QueryProfiler() {
		profiledPredicates = new IdentityHashMap<FunctorLabel, PredicateProfiler>();
		orders = new IdentityHashMap<FunctorLabel, int[]>();
		serializableOrders = new HashMap<FunctorKey, int[]>();
	}
	
	public static QueryProfiler getInstance() {
		return INSTANCE;
	}
	
	@SuppressWarnings("unchecked")
	public void loadOrders(String filename) {
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
	
	public void saveOrders() {
		// XXX Should work for Unix and Windows systems in practice
		saveOrders("src/saere/database/profiling/Profiling-" + timestamp() + ".ser");
	}
	
	public void saveOrders(String filename) {
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
		FunctorLabel functorLabel = FunctorLabel.FunctorLabel(query.functor(), query.arity());
		PredicateProfiler predicateProfiler = profiledPredicates.get(functorLabel);
		if (predicateProfiler == null) {
			predicateProfiler = new PredicateProfiler(this, functorLabel);
			profiledPredicates.put(functorLabel, predicateProfiler);
		}
		predicateProfiler.profile(query);
	}
	
	// Fills the serializable orders map will all profiled orders
	private void fillOrdersMapBeforeSerialization() {
		for (Entry<FunctorLabel, PredicateProfiler> entry : profiledPredicates.entrySet()) {
			FunctorLabel functorLabel = entry.getKey();
			serializableOrders.put(new FunctorKey(functorLabel.atom().toString(), functorLabel.arity()), entry.getValue().order());
		}
	}
	
	// Fills the (non-serializable) orders map from the serializable orders map
	private void fillOrdersMapAfterDeserialization() {
		for (Entry<FunctorKey, int[]> entry : serializableOrders.entrySet()) {
			FunctorKey functorKey = entry.getKey();
			orders.put(FunctorLabel.FunctorLabel(StringAtom.StringAtom(functorKey.functor()), functorKey.arity()), entry.getValue());
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
		FunctorLabel functorLabel = FunctorLabel.FunctorLabel(term.functor(), term.arity());
		
		// Check wether an order is already cached in orders
		int[] order = orders.get(functorLabel);
		if (order != null) {
			return getOrder(term, order);
		} else {
			
			// Check wether we have a order profiled in this run or not
			PredicateProfiler predicateProfiler = profiledPredicates.get(functorLabel);
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
			args[order[i]] = term.arg(i);
		}
		return args;
	}
	
	private String timestamp() {
		Calendar c = Calendar.getInstance();
		StringBuilder sb = new StringBuilder();
		
		sb.append(c.get(Calendar.YEAR));
		sb.append("-");
		sb.append(c.get(Calendar.MONTH) + 1);
		sb.append("-");
		sb.append(c.get(Calendar.DATE));
		sb.append("_");
		sb.append(c.get(Calendar.HOUR_OF_DAY));
		sb.append("-");
		sb.append(c.get(Calendar.MINUTE));
		sb.append("-");
		sb.append(c.get(Calendar.SECOND));
		
		return sb.toString();
	}
}
