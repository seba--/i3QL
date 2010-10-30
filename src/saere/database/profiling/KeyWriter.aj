package saere.database.profiling;

import java.io.OutputStream;

import saere.Atom;
import saere.database.DatabaseTermFactory;

public aspect KeyWriter {
	
	public static boolean active = true;
	
	private pointcut keyAtom() : execution(public Atom DatabaseTermFactory.KeyAtom(String)) && args(prefix) && if(active);
	
	private static OutputStream out = System.out;
	
	public static void setOutputStream(OutputStream out) {
		KeyWriter.out = out;
	}
	
	Object around(String prefix) : keyAtom(prefix) {
		Object obj = proceed(); // should be a StringAtom
		out.write((obj.toString() + "\n").getBytes());
		return obj;
	}
}
