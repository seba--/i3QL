package saere.database.profiling;

import java.util.IdentityHashMap;

import saere.Atom;
import saere.Term;
import saere.database.Utils;
import saere.database.index.ComplexTrieBuilder;
import saere.database.index.SimpleTrieBuilder;
import saere.database.index.Trie;
import saere.database.index.TrieBuilder;

/**
 * Logger for insertions that writes to a PostgreSQL database.
 * 
 * @author David Sullivan
 * @version 0.3, 11/1/2010
 */
public aspect InsertionLogger  {
	
	// To (de-)activate the whole aspect (w.r.t. pointcuts/weaving)
	private static final boolean ACTIVE = false;
	
	// To (de-)activate the whole aspect while runtime (w.r.t. to advice)
	private static boolean active;
	
	private PostgreSQL database;
	private Stopwatch sw;
	private IdentityHashMap<Object, Integer> builderCounter;
	
	// Constructor
	public InsertionLogger() {
		database = new PostgreSQL();
		database.connect();
		sw = new Stopwatch();
		builderCounter = new IdentityHashMap<Object, Integer>();
		active = false;
	}
	
	/**
	 * Activates or deactivates the logging into the PostgreSQL DB. The aspect 
	 * is inactive by default.<br>
	 * <br>(Note that this aspect is still active and weaved into the 
	 * application.)
	 * 
	 * @param active Set to <tt>true</tt> to activate.
	 */
	public static void setActive(boolean active) {
		InsertionLogger.active = active;
	}
	
	// Pointcut for simple insertions by the 'saere.database.index' package
	private pointcut simpleInsertion(Term term, Trie start, SimpleTrieBuilder builder) :
		execution(public Trie TrieBuilder.insert(Term, Trie)) &&
		args(term, start) && target(builder) && if(ACTIVE);
	
	// Pointcut for complex insertions by the 'saere.database.index' package
	private pointcut complexInsertion(Term term, Trie start, ComplexTrieBuilder builder) :
		execution(public Trie TrieBuilder.insert(Term, Trie)) &&
		args(term, start) && target(builder) && if(ACTIVE);
	
	// Advice for simple insertions by the 'saere.database.index' package
	Object around(Term term, Trie start, SimpleTrieBuilder builder) : simpleInsertion(term, start, builder) {
		sw.start();
		Object obj = proceed(term, start, builder);
		long time = sw.stop();
		
		insertIntoDatabase(term, builder, time);
		
		return obj;
	}
	
	// Advice for simple insertions by the 'saere.database.index' package
	Object around(Term term, Trie start, ComplexTrieBuilder builder) : complexInsertion(term, start, builder) {
		sw.start();
		Object obj = proceed(term, start, builder);
		long time = sw.stop();
		
		insertIntoDatabase(term, builder, time);
		
		return obj;
	}
	
	private void insertIntoDatabase(Term term, Object builder, long time) {
		if (!active)
			return;
		
		// Compose values
		String ins_number = String.valueOf(getAndIncrement(builder));
		String ins_time = String.valueOf(time);
		String term_functor = term.functor().toString();
		String term_arg0 = term.arity() > 0 ? Utils.termToString(term.arg(0)) : "";
		String term_arg1 = term.arity() > 1 ? Utils.termToString(term.arg(1)) : "";
		String term_full = ""; // FIXME Use actual string representation of term (encoding issues as of now).
		String ins_mode = builder.toString();
		String triebuilder_oid = String.valueOf(builder.hashCode());
		
		// Escape values
		term_functor = term_functor.replace('\'', '"');
		term_arg0 = term_arg0.replace('\'', '"');
		term_arg1 = term_arg1.replace('\'', '"');
		term_full = term_full.replace('\'', '"');
		
		database.modify("INSERT INTO insertions(ins_number, ins_time, term_functor, term_arg0, term_arg1, term_full, ins_mode, triebuilder_oid) " +
			"VALUES (" + ins_number + ", " + ins_time +  ", '" + term_functor + "', '" + term_arg0 + "', '" + term_arg1 + "', '" + term_full + "', '" + ins_mode + "', " + triebuilder_oid + ")");
	}
	
	private int getAndIncrement(Object obj) {
		Integer counter = builderCounter.get(obj);
		if (counter == null) {
			counter = 1;
		}
		builderCounter.put(obj, counter + 1);
		return counter;
	}
	
	@Override
	protected void finalize() throws Throwable {
		if (database != null) {
			database.disconnect();
		}
	};
}
