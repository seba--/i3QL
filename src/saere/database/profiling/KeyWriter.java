package saere.database.profiling;

import java.io.IOException;
import java.io.OutputStream;

// Since AspectJ causes trouble...
public class KeyWriter {
	
	private static final KeyWriter INSTANCE = new KeyWriter();
	
	private OutputStream out = null;
	
	private KeyWriter() {
		
	}
	
	public static KeyWriter getInstance() {
		return INSTANCE;
	}
	
	public void setOutputStream(OutputStream out) {
		this.out = out;
	}
	
	public void write(String s) {
		if (out != null) {
			try {
				out.write(s.getBytes());
			} catch (IOException e) {
				// ignore
			}
		}	
	}
}
