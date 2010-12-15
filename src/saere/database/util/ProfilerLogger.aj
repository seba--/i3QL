package saere.database.util;

import saere.Term;
import saere.*;
import saere.database.*;
import saere.database.index.*;
import saere.database.profiling.*;
import saere.database.predicate.*;

/**
 * Writes calls to {@link Profiler#profile(saere.Term)} to a database.
 * 
 * @author David Sullivan
 * @version 0.1, 12/10/2010
 */
public aspect ProfilerLogger {
	
	// To (de-)activate the whole aspect (w.r.t. pointcuts/weaving)
	private static final boolean ACTIVE = false;
	
	private final PostgreSQL database;
	private final Stopwatch sw;
	
	private int profileCounter;
	private int reorderCounter;
	
	public ProfilerLogger() {
		database = new PostgreSQL();
		database.connect();
		sw = new Stopwatch();
	}
	
	// Don't log recursive calls (because DB I/O would be counted to direct calls)
	private pointcut profile(Term query) :
		call(public void Profiler.profile(Term)) && !withincode(public void PredicateProfiler.profile(Term)) && args(query) && if(ACTIVE);	
	
	Object around(Term query) : profile(query) {
		sw.reset();
		Object obj = proceed(query);
		long time = sw.reset();
		
		insertProfile(query, time);
		
		return obj;
	}
	
	private void insertProfile(Term query, long time) {
		
		// Compose values
		String prof_number = String.valueOf(profileCounter++);
		String prof_time = String.valueOf(time);
		String query_functor = query.functor().toString();
		String query_full = Utils.termToString(query); // XXX *Should* be no real escape problems with queries...
		
		// Escape values
		query_functor = query_functor.replace('\'', '"');
		query_full = query_full.replace('\'', '"');
		
		database.modify("INSERT INTO profiles(prof_number, prof_time, query_functor, query_full) " +
			"VALUES (" + prof_number + ", " + prof_time +  ", '" + query_functor + "', '" + query_full + "')");
	}
	
	private pointcut reorderArgs(Term query) :
		call(protected Term[] TermFlattener.getArgs(Term)) && args(query) 
		&& cflow(execution(public Solutions DatabasePredicate.unify(Term))) && if(ACTIVE);
	
	Object around(Term query) : reorderArgs(query) {
		sw.reset();
		Object obj = proceed(query);
		long time = sw.reset();
		
		insertReordering(query, time);
		
		return obj;
	}
	
	private void insertReordering(Term query, long time) {
		if (Profiler.getInstance().mode() != Profiler.Mode.USE)
			return;
		
		// Compose values
		String ro_number = String.valueOf(reorderCounter++);
		String ro_time = String.valueOf(time);
		String query_functor = query.functor().toString();
		String query_full = Utils.termToString(query); // XXX *Should* be no real escape problems with queries...
		
		// Escape values
		query_functor = query_functor.replace('\'', '"');
		query_full = query_full.replace('\'', '"');
		
		database.modify("INSERT INTO reorders(ro_number, ro_time, query_functor, query_full) " +
			"VALUES (" + ro_number + ", " + ro_time +  ", '" + query_functor + "', '" + query_full + "')");
	}
	
	@Override
	protected void finalize() throws Throwable {
		if (database != null)
			database.disconnect();
	}
}
