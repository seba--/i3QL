package saere.database.profiling;


import saere.Atom;
import saere.StringAtom;
import saere.database.DatabaseTermFactory;

// This aspect does not steal passwords.
public aspect KeyLogger {
	
	private static boolean ACTIVE = true;
	private static final Keys KEYS = Keys.getInstance();
	
	private pointcut keyAtom(String prefix) : execution(public Atom DatabaseTermFactory.KeyAtom(String)) && args(prefix) && if(ACTIVE);
	
	Object around(String prefix) : keyAtom(prefix) {
		Object obj = proceed(prefix); // should be a StringAtom
		KEYS.addKey(((StringAtom) obj).toString());
		return obj;
	}
}
