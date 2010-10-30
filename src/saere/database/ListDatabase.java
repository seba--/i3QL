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
import saere.database.predicate.DatabasePredicate;

/**
 * The {@link ListDatabase} serves as default implementation and is used by the 
 * default (list-based) unification.
 *
 * @author David Sullivan
 * @version 0.22, 10/14/2010
 * @see DatabasePredicate#useLists()
 */
public class ListDatabase extends Database {

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
		if (!predicates.containsKey(fact.functor())) {
			predicates.put(fact.functor(), new LinkedList<Term>());
		}
		predicates.get(fact.functor()).push(fact); // this reverses order (and actually restores the original order)
	}

	@Override
	protected void fillProcessComplete() {
		
	}

	@Override
	public void drop() {
		predicates.clear();
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
	public Iterator<Term> getFacts(StringAtom functor) {
		return predicates.get(functor).iterator();
	}
	
	@Override
	public Iterator<Term> getCandidates(Term[] terms) {
		assert terms != null && terms.length > 1 && terms[0].isStringAtom() : "Invalid terms specified";
		
		// get the first term (functor) and ignore the rest
		return getFacts(terms[0].asStringAtom());
	}
}
