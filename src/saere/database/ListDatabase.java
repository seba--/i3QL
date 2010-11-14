package saere.database;

import java.util.Deque;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import saere.StringAtom;
import saere.Term;
import saere.database.index.EmptyTermIterator;
import saere.database.predicate.DatabasePredicate;

/**
 * The {@link ListDatabase} serves as default implementation and is used by the 
 * default (list-based) unification.
 *
 * @author David Sullivan
 * @version 0.22, 10/14/2010
 * @see DatabasePredicate#useLists()
 */
// TODO Add hash maps for IDs...
public final class ListDatabase extends Database {

	private final static ListDatabase INSTANCE = new ListDatabase();
	
	private final Map<StringAtom, Deque<Term>> predicates;
	
	private ListDatabase() {
		predicates = new HashMap<StringAtom, Deque<Term>>();
	}
	
	/**
	 * Gets the {@link ListDatabase} singleton.
	 * 
	 * @return The {@link ListDatabase} singleton.
	 */
	public static ListDatabase getInstance() {
		return INSTANCE;
	}
	
	@Override
	public void add(Term fact) {
		Deque<Term> predicateList = predicates.get(fact.functor());
		if (predicateList == null) {
			predicateList = new LinkedList<Term>();
			predicates.put(fact.functor(), predicateList);
		}
		predicateList.push(fact); // This reverses order (and actually restores the original order)
	}

	@Override
	public void drop() {
		predicates.clear();
		System.gc();
	}

	@Override
	public Iterator<Term> getFacts() {
		List<Term> facts = new LinkedList<Term>(); // XXX Maybe not the best way to get an iterator for all sub-lists together...
		for (Entry<StringAtom, Deque<Term>> entry : predicates.entrySet()) {
			facts.addAll(entry.getValue());
		}
		return facts.iterator();
	}
	
	@Override
	public Iterator<Term> query(Term query) {
		
		// Get the first term (functor) and ignore the rest, i.e., the 
		// candidate lists are generally much larger than those composed with 
		// tries.
		Deque<Term> predicateList = predicates.get(query.functor());
		if (predicateList != null) {
			return predicateList.iterator();
		} else {
			return EmptyTermIterator.getInstance();
		}
	}
}
