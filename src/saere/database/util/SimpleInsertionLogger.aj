package saere.database.util;

import saere.*;
import saere.database.*;
import saere.database.index.*;
import saere.database.util.Stopwatch.Unit;


public aspect SimpleInsertionLogger {
	
	// To (de-)activate the whole aspect (w.r.t. pointcuts/weaving)
	private static final boolean ACTIVE = false;
	
	private final InsertionStats stats = InsertionStats.getInstance();
	private final Stopwatch sw;
	
	public SimpleInsertionLogger() {
		sw = new Stopwatch(Unit.NANOSECONDS);
	}
	
	private pointcut insertion(Term term, Database database) :
		execution(public void Database.add(Term)) &&
		args(term) && target(database) && if(ACTIVE);
	
	Object around(Term term, Database database) : insertion(term, database) {
		sw.reset();
		Object obj = proceed(term, database);
		long time = sw.reset();
		
		// Gather stats...
		stats.increaseTermNum();
		stats.increaseAvgInsTime(time);
		if (stats.getMinInsTime() > time) {
			stats.setMinInsTime(time);
			stats.setMinInsTimeTerm(term);
			stats.setMinNum(stats.getTermNum());
		} else if (stats.getMaxInsTime() < time) {
			stats.setMaxInsTime(time);
			stats.setMaxInsTimeTerm(term);
			stats.setMaxNum(stats.getTermNum());
		}
		
		return obj;
	}
}
