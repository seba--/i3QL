package saere.database.index.reference;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.NoSuchElementException;
import java.util.Map.Entry;

import saere.StringAtom;
import saere.Term;
import saere.database.Database;
import saere.database.index.EmptyTermIterator;

/**
 * A reference database based on hash maps (basically single argument indexing). 
 * The decision on what and how is indexed is hardcoded 
 * (see {@link #generateKey(Term)}) and only BAT facts can be indexed<br>
 * <br>
 * If a key for a query can be derived an iterator for the appropriate 
 * predicate is returned.
 * 
 * @author David Sullivan
 * @version 0.1, 11/16/2010
 */
public final class ReferenceDatabase extends Database {
	
	/** The map of indexes. For each functor there is an index. */
	private final HashMap<StringAtom, HashMap<Key, Term>> indexes = new HashMap<StringAtom, HashMap<Key, Term>>();
	
	/** The map of lists. For each functor there is a list. */
	private final HashMap<StringAtom, LinkedList<Term>> lists = new HashMap<StringAtom, LinkedList<Term>>();
	
	// The functors of indexed predicates
	private static final StringAtom __class_file$10 = StringAtom.StringAtom("class_file");
	private static final StringAtom __class_file_source$2 = StringAtom.StringAtom("class_file_source");
	private static final StringAtom __enclosing_method$4 = StringAtom.StringAtom("enclosing_method");
	private static final StringAtom __annotation$4 = StringAtom.StringAtom("annotation");
	private static final StringAtom __annotation_default$2 = StringAtom.StringAtom("annotation_default");
	private static final StringAtom __parameter_annotations$3 = StringAtom.StringAtom("parameter_annotations");
	private static final StringAtom __field$11 = StringAtom.StringAtom("field");
	private static final StringAtom __field_value$2 = StringAtom.StringAtom("field_value");
	private static final StringAtom __method$15 = StringAtom.StringAtom("method");
	private static final StringAtom __method_exceptions$2 = StringAtom.StringAtom("__method_exceptions");
	private static final StringAtom __method_line_number_table$2 = StringAtom.StringAtom("method_line_number_table");
	private static final StringAtom __method_local_variable_table$2 = StringAtom.StringAtom("method_local_variable_table");
	private static final StringAtom __method_exceptions_table$2 = StringAtom.StringAtom("method_exceptions_table");
	private static final StringAtom __instr$3 = StringAtom.StringAtom("instr");
	private static final StringAtom __inner_classes$2 = StringAtom.StringAtom("inner_classes");
	
	@Override
	public void add(Term fact) {
		Key key = generateKey(fact);
		if (key != null) {
			getOrCreateIndex(fact).put(key, fact);
		} else {
			getOrCreateList(fact).push(fact);
		}
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
		
		// Add the lists
		for (Entry<StringAtom, LinkedList<Term>> entry : lists.entrySet()) {
			for (Term term : entry.getValue()) {
				allFacts.push(term);
			}
		}
		
		// Add the indexes
		for (Entry<StringAtom, HashMap<Key, Term>> entry : indexes.entrySet()) {
			for (Term term : entry.getValue().values()) {
				allFacts.push(term);
			}
		}
		
		return allFacts.iterator();
	}

	@Override
	public Iterator<Term> query(Term query) {
		// Try indexes first
		HashMap<Key, Term> index = indexes.get(query.functor());
		if (index != null) {
			Key key = generateKey(query);
			if (key != null) {
				// The best case: We have an index and a key
				return new SingleResult(index.get(key));
			} else {
				return index.values().iterator();
			}
		} else {
			LinkedList<Term> list = lists.get(query.functor());
			if (list != null) {
				return list.iterator();
			} else {
				// The term is not stored at all
				return EmptyTermIterator.getInstance();
			}
		}
	}
	
	/**
	 * Gets or creates the index for the specified term based on its functor.
	 * 
	 * @param term The term for which a index is requested.
	 * @return The index for the term.
	 */
	private HashMap<Key, Term> getOrCreateIndex(Term term) {
		HashMap<Key, Term> index = indexes.get(term.functor());
		if (index == null) {
			index = new HashMap<Key, Term>();
			indexes.put(term.functor(), index);
		}
		
		return index;
	}
	
	/**
	 * Gets or creates the list for the specified term based on its functor.
	 * 
	 * @param term The term for which a list is requested
	 * @return The list for the term.
	 */
	private LinkedList<Term> getOrCreateList(Term term) {
		LinkedList<Term> list = lists.get(term.functor());
		if (list == null) {
			list = new LinkedList<Term>();
			lists.put(term.functor(), list);
		}
		
		return list;
	}
	
	/**
	 * Attempts to create a key for the specified term. It key generation fails 
	 * <tt>null</tt> is returned.
	 * 
	 * @param fact The fact for which a key should be generated.
	 * @return The key for the fact or <tt>null</tt>.
	 */
	/*
	 * XXX This implementation depends heavily on BAT facts.
	 * We chose the one or two arguments which are seem best for IDs in general 
	 * and not based on actual queries.
	 */
	private Key generateKey(Term fact) {
		StringAtom functor = fact.functor();
		
		if (functor.sameAs(__class_file$10) && fact.arity() == 10 && !fact.arg(0).isVariable()) {
			return new SimpleKey(fact.arg(0).asStringAtom());
		} else if (functor.sameAs(__class_file_source$2) && fact.arity() == 2 && !fact.arg(0).isVariable()) {
			return new SimpleKey(fact.arg(0).asStringAtom());
		} if (functor.sameAs(__enclosing_method$4) && fact.arity() == 4 && !fact.arg(0).isVariable()) {
			// TODO Validate this!
			return new SimpleKey(fact.arg(0).asStringAtom());
		} else if (functor.sameAs(__annotation$4) && fact.arity() == 4 && !fact.arg(0).isVariable()) {
			// TODO Validate this!
			return new SimpleKey(fact.arg(0).asStringAtom());
		} else if (functor.sameAs(__annotation_default$2) && fact.arity() == 2 && !fact.arg(0).isVariable()) {
			// TODO Validate this!
			return new SimpleKey(fact.arg(0).asStringAtom());
		} else if (functor.sameAs(__parameter_annotations$3) && fact.arity() == 3 && !fact.arg(0).isVariable()) {
			// TODO Validate this!
			return new SimpleKey(fact.arg(0).asStringAtom());
		} else if (functor.sameAs(__field$11) && fact.arity() == 11 && !fact.arg(0).isVariable()) {
			// TODO Validate this!
			return new SimpleKey(fact.arg(0).asStringAtom());
		} else if (functor.sameAs(__field_value$2) && fact.arity() == 2 && !fact.arg(0).isVariable()) {
			// TODO Validate this!
			return new SimpleKey(fact.arg(0).asStringAtom());
		} else if (functor.sameAs(__method$15) && fact.arity() == 15 && !fact.arg(1).isVariable()) {
			return new SimpleKey(fact.arg(1).asStringAtom());
		} else if (functor.sameAs(__method_exceptions$2) && fact.arity() == 2 && !fact.arg(0).isVariable()) {
			// TODO Validate this!
			return new SimpleKey(fact.arg(0).asStringAtom());
		} else if (functor.sameAs(__method_line_number_table$2) && fact.arity() == 2 && !fact.arg(0).isVariable()) {
			// TODO Validate this!
			return new SimpleKey(fact.arg(0).asStringAtom());
		} else if (functor.sameAs(__method_local_variable_table$2) && fact.arity() == 2 && !fact.arg(0).isVariable()) {
			// TODO Validate this!
			return new SimpleKey(fact.arg(0).asStringAtom());
		} else if (functor.sameAs(__method_exceptions_table$2) && fact.arity() == 2 && !fact.arg(0).isVariable()) {
			// TODO Validate this!
			return new SimpleKey(fact.arg(0).asStringAtom());
		} else if (functor.sameAs(__instr$3) && fact.arity() == 3 && !fact.arg(0).isVariable() && !fact.arg(1).isVariable()) {
			return new CompositeKey(fact.arg(0).asStringAtom(), fact.arg(1).asIntegerAtom());
		} else if (functor.sameAs(__inner_classes$2) && fact.arity() == 2 && !fact.arg(0).isVariable()) {
			// TODO Validate this!
			return new SimpleKey(fact.arg(0).asStringAtom());
		}
		
		return null;
	}
	
	/**
	 * An iterator for a single result (overhead of course).
	 */
	private class SingleResult implements Iterator<Term> {

		private final Term term;
		private boolean iterated;
		
		public SingleResult(Term term) {
			this.term = term;
		}
		
		@Override
		public boolean hasNext() {
			return !iterated;
		}

		@Override
		public Term next() {
			if (hasNext()) {
				iterated = true;
				return term;
			} else {
				throw new NoSuchElementException();
			}
		}

		@Override
		public void remove() {
			throw new UnsupportedOperationException();
		}
	}
}
