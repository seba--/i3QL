package saere.database.profiling;

import java.io.IOException;
import java.io.OutputStream;

import saere.Atom;
import saere.database.DatabaseTermFactory;

public aspect KeyWriterAspect {
	
	public static boolean ACTIVE = false;
	
	private pointcut keyAtom(String prefix) : execution(public Atom DatabaseTermFactory.KeyAtom(String)) && args(prefix) && if(ACTIVE);
	
	private static OutputStream out = System.out;
	
	public static void setOutputStream(OutputStream out) {
		KeyWriterAspect.out = out;
	}
	
	Object around(String prefix) : keyAtom(prefix) {
		Object obj = proceed(prefix); // should be a StringAtom
		try {
			out.write((obj.toString() + "\n").getBytes());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return obj;
	}
}
