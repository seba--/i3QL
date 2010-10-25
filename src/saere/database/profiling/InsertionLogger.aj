package saere.database.profiling;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Deque;
import java.util.IdentityHashMap;
import java.util.LinkedList;

import saere.Atom;
import saere.Term;
import saere.database.Utils;
import saere.database.index.ComplexTrieBuilder;
import saere.database.index.SimpleTrieBuilder;
import saere.database.index.Trie;
import saere.database.index.TrieBuilder;
import saere.database.profiling.Insertion.InsertionMode;

/**
 * Very simple logger for insertions (can't actually distinguish between 
 * differenct tries etc.).
 * 
 * @author David Sullivan
 * @version 0.1, 10/23/2010
 */
public aspect InsertionLogger {
	
	private static final LinkedList<Insertion> INSERTIONS = new LinkedList<Insertion>();
	
	private final Stopwatch sw = new Stopwatch();
	
	private static PostgreSQL database;
	
	private OutputStream out = System.out;
	private int number = 0;
	private IdentityHashMap<TrieBuilder<?>, InsertionMode> builderModeCache = new IdentityHashMap<TrieBuilder<?>, InsertionMode>();
	
	public InsertionLogger() {
		
	}
	
	// For insertions in the 'seare.database.simple' package
	private pointcut verySimpleInsertion(Term term, saere.database.index.simple.Trie start, saere.database.index.simple.SimpleTrieBuilder builder) :
		execution(public saere.database.index.simple.Trie saere.database.index.simple.SimpleTrieBuilder.insert(Term, saere.database.index.simple.Trie)) &&
		args(term, start) && target(builder);
	
	// For simple insertions
	Object around(Term term, saere.database.index.simple.Trie start, saere.database.index.simple.SimpleTrieBuilder builder) : verySimpleInsertion(term, start, builder) {
		sw.start();
		Object obj = proceed(term, start, builder);
		long time = sw.stop();
		
		String ins_number = String.valueOf(number++);
		String ins_time = String.valueOf(time);
		String term_functor = term.functor().toString();
		String term_arg0 = term.arity() > 0 ? Utils.termToString(term.arg(0)) : "";
		String term_arg1 = term.arity() > 1 ? Utils.termToString(term.arg(1)) : "";
		String term_full = "<the-term>"; //Utils.termToString(term);
		String ins_mode = "shallow-simple";
		
		// XXX Actually better create one large query...
		
		// Escape...
		term_functor = term_functor.replace('\'', '"');
		term_arg0 = term_arg0.replace('\'', '"');
		term_arg1 = term_arg1.replace('\'', '"');
		term_full = term_full.replace('\'', '"');
		
		// Also remove string values
		//term_full = term_full.replaceAll("string\\(.*\\)", "string()");
		
		database.modify("INSERT INTO insertions(ins_number, ins_time, term_functor, term_arg0, term_arg1, term_full, ins_mode) " +
				"VALUES (" + ins_number + ", " + ins_time +  ", '" + term_functor + "', '" + term_arg0 + "', '" + term_arg1 + "', '" + term_full + "', '" + ins_mode + "')");
		
		return obj;
	}
	
	private pointcut simpleInsertion(Term term, Trie<Atom> start, SimpleTrieBuilder builder) :
		execution(public Trie<Atom> saere.database.index.TrieBuilder.insert(Term, Trie<Atom>)) &&
		args(term, start) && target(builder);
	
	private pointcut complexInsertion(Term term, Trie<Atom[]> start, ComplexTrieBuilder builder) :
		execution(public Trie<Atom[]> saere.database.index.TrieBuilder.insert(Term, Trie<Atom[]>)) &&
		args(term, start) && target(builder);
	
	// For simple insertions
	Object around(Term term, Trie<Atom> start, SimpleTrieBuilder builder) : simpleInsertion(term, start, builder) {
		sw.start();
		Object obj = proceed(term, start, builder);
		long time = sw.stop();
		InsertionMode mode = getInsertionMode(builder);
		
		Insertion insertion = new Insertion(number++, time, term, mode);
		//INSERTIONS.push(insertion);
		try {
			out.write(insertion.toString().getBytes());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return obj;
	}
	
	// For complex insertions
	Object around(Term term, Trie<Atom[]> start, ComplexTrieBuilder builder) : complexInsertion(term, start, builder) {
		sw.start();
		Object obj = proceed(term, start, builder);
		long time = sw.stop();
		InsertionMode mode = getInsertionMode(builder);
		
		Insertion insertion = new Insertion(number++, time, term, mode);
		//INSERTIONS.push(insertion);
		try {
			out.write(insertion.toString().getBytes());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return obj;
	}
	
	private InsertionMode getInsertionMode(TrieBuilder<?> builder) {
		InsertionMode mode = builderModeCache.get(builder);
		if (mode == null) {
			// Awkward checking the string representation, but well...
			String builderAsString = builder.toString();
			if (builderAsString.equals("shallow-simple")) {
				mode = builderModeCache.put(builder, InsertionMode.SHALLOW_SIMPLE);
			} else if (builderAsString.equals("recursive-simple")) {
				mode = builderModeCache.put(builder, InsertionMode.RECURSIVE_SIMPLE);
			} else if (builderAsString.equals("shallow-complex")) {
				mode = builderModeCache.put(builder, InsertionMode.SHALLOW_COMPLEX);
			} else if (builderAsString.equals("recursive-complex")) {
				mode = builderModeCache.put(builder, InsertionMode.RECURSIVE_COMPLEX);
			}
		}
		return mode;	
	}
	
	public static void setDatabase(PostgreSQL database) {
		InsertionLogger.database = database;
	}

}
