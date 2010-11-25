package saere.database.index.reference;

import static saere.database.Utils.*;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map.Entry;

import saere.StringAtom;
import saere.Term;
import saere.database.Database;
import saere.database.DatabaseAdapter;
import saere.database.ReferenceAdapter;
import saere.database.index.EmptyTermIterator;

/**
 * A reference database based on hash maps (basically first argument indexing). 
 * 
 * @author David Sullivan
 * @version 0.2, 11/22/2010
 */
public final class ReferenceDatabase extends Database {
	
	/** The map of indexes. For each functor there is an index. Each index entry is a list for terms with the same first argument (or functor in the case of atoms). */
	private final HashMap<StringAtom, HashMap<StringAtom, LinkedList<Term>>> indexes = new HashMap<StringAtom, HashMap<StringAtom, LinkedList<Term>>>();
	
	/** The map of lists. For reach functor there is a list. */
	// Instead of list concat hash map contents?
	private final HashMap<StringAtom, LinkedList<Term>> lists = new HashMap<StringAtom, LinkedList<Term>>();
	
	private boolean allowDuplicates;
	
	@Override
	public void add(Term fact) {
		assert isFact(fact) : "The specified term is not a fact";
		
		// Add to list (redundant but this list is build only once)
		LinkedList<Term> list = lists.get(fact.functor());
		if (list == null) {
			list = new LinkedList<Term>();
			lists.put(fact.functor(), list);
		}
		addFactToList(list, fact);
		
		// Put into index...
		HashMap<StringAtom, LinkedList<Term>> index = indexes.get(fact.functor());
		if (index == null) {
			index = new HashMap<StringAtom, LinkedList<Term>>();
			indexes.put(fact.functor(), index);
		}
		
		// Index on the first argument for compound terms or on the functor for atoms
		StringAtom key = null;
		if (fact.isCompoundTerm()) {
			key = fact.arg(0).functor();
		} else {
			key = fact.functor();
		}
		
		LinkedList<Term> bucket = index.get(key);
		if (bucket == null) {
			bucket = new LinkedList<Term>();
			index.put(key, bucket);
		}
		addFactToList(bucket, fact);
	}

	@Override
	public void drop() {
		indexes.clear();
		lists.clear();
		System.gc();
	}

	@Override
	public Iterator<Term> terms() {
		LinkedList<Term> allFacts = new LinkedList<Term>();
		
		// Add the indexes
		for (Entry<StringAtom, HashMap<StringAtom, LinkedList<Term>>> entry : indexes.entrySet()) {
			HashMap<StringAtom, LinkedList<Term>> index = entry.getValue();
			
			// Add the lists
			for (LinkedList<Term> bucket : index.values()) {
				allFacts.addAll(bucket);
			}
		}
		
		return allFacts.iterator();
	}

	@Override
	public Iterator<Term> query(Term query) {
		StringAtom key = null;
		if (query.isCompoundTerm()) {
			if (!hasFreeVariable(query.arg(0))) {
				key = query.arg(0).functor();
			}
		} else {
			key = query.functor();
		}
		
		if (key != null) {
			HashMap<StringAtom, LinkedList<Term>> index = indexes.get(query.functor());
			if (index == null) {
				return EmptyTermIterator.getInstance();
			}
			
			LinkedList<Term> bucket = index.get(key);
			if (bucket == null) {
				return EmptyTermIterator.getInstance();
			}
			
			return bucket.iterator();
		} else {
			LinkedList<Term> list = lists.get(query.functor());
			if (list != null) {
				return list.iterator();
			} else {
				return EmptyTermIterator.getInstance();
			}
		}
		
	}
	
	public HashMap<StringAtom, LinkedList<Term>> getIndex(StringAtom functor) {
		return indexes.get(functor);
	}
	
	public LinkedList<Term> getList(StringAtom functor) {
		return lists.get(functor);
	}
	
	private boolean addFactToList(LinkedList<Term> list, Term fact) {
		if (list.size() == 0 || allowDuplicates) {
			list.push(fact);
			return true;
		} else {
			for (Term listFact : list) {
				// Unification with facts doesn't change anything
				if (listFact.unify(fact)) {
					return false;
				}
			}
			list.push(fact);
			return true;
		}
	}
	
	public void allowDuplicates(boolean allowDuplicates) {
		this.allowDuplicates = allowDuplicates;
	}

	@Override
	public DatabaseAdapter getAdapter(StringAtom functor, int arity) {
		
		// Create an empty list and index if none exist
		LinkedList<Term> list = lists.get(functor);
		if (list == null) {
			list = new LinkedList<Term>();
			lists.put(functor, list);
		}
		HashMap<StringAtom, LinkedList<Term>> index = indexes.get(functor);
		if (index == null) {
			index = new HashMap<StringAtom, LinkedList<Term>>();
			indexes.put(functor, index);
		}
		
		return new ReferenceAdapter(this, functor);
	}
}
