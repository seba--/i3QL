package saere.database;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import saere.StringAtom;
import saere.Term;

public class Database_Default extends Database {

	private final static Database INSTANCE = new Database_Default();
	
	private final Map<StringAtom, List<Term>> predicates;
	
	private Database_Default() {
		predicates = new HashMap<StringAtom, List<Term>>();
	}
	
	public static Database getInstance() {
		return INSTANCE;
	}
	
	@Override
	public void add(Term fact) {
		if (!predicates.containsKey(fact.functor())) {
			predicates.put(fact.functor(), new LinkedList<Term>());
		}
		predicates.get(fact.functor()).add(fact);
	}

	@Override
	public List<Term> getFacts() {
		List<Term> facts = new LinkedList<Term>();
		for (Entry<StringAtom, List<Term>> entry : predicates.entrySet()) {
			facts.addAll(entry.getValue());
		}
		return facts;
	}

	@Override
	public List<Term> getFacts(StringAtom functor) {
		return predicates.get(functor);
	}
}
