package saere.database;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import saere.CompoundTerm;
import scala.Function0;
import scala.collection.immutable.List;
import de.tud.cs.st.bat.ClassFile;
import de.tud.cs.st.bat.reader.Java6Framework;

/**
 * Reads Java class files or JAR and ZIP files containing them and stores their 
 * Prolog representatiosn in the {@link Factbase}. The transformation of the 
 * bytecode to Prolog is done with BAT.
 * 
 * @author David Sullivan
 * @version 0.1, 9/22/2010
 */
public class FactbaseBytecodeReader {
	
	private final Factbase factbase = Factbase.getInstance();
	private final DatabaseTermFactory factory = DatabaseTermFactory.getInstance();

	/**
	 * Processes the contents of a ZIP or JAR file and stores the Prolog 
	 * representations in the {@link Factbase}.
	 * 
	 * @param filename The JAR or ZIP file.
	 */
	public void processFile(String filename) {
		try {
			if (filename.endsWith(".zip") || filename.endsWith(".jar")) {
				ZipFile zipFile = new ZipFile(new File(filename));
				Enumeration<? extends ZipEntry> zipEntries = zipFile.entries();
				while (zipEntries.hasMoreElements()) {
					ZipEntry zipEntry = zipEntries.nextElement();
					if (!zipEntry.isDirectory() && zipEntry.getName().endsWith(".class")) {
						processFunc0(function0(zipFile.getInputStream(zipEntry)));
					}
				}
			} else {
				processFunc0(function0(new FileInputStream(filename)));
			}
		} catch (IOException e) {
			System.err.println(e);
		}
	}

	/**
	 * Processes the contents of a Java class file and stores the Prolog 
	 * representations in the {@link Factbase}.
	 * 
	 * @param filename The JAR or ZIP file.
	 */
	public void processClass(String filename) {		
		try {
			processFunc0(function0(new FileInputStream(filename)));
		} catch (FileNotFoundException e) {
			System.err.println(e);
		}
	}
	
	/**
	 * Does the actual processing with an input stream.
	 * 
	 * @param func0 The function object that wraps the input stream.
	 */
	private void processFunc0(Function0<InputStream> func0) {
		ClassFile classFile = (ClassFile) Java6Framework.ClassFile(func0);
		List<CompoundTerm> facts = classFile.toProlog(factory);
		for (int i = 0; i < facts.size(); i++) {
			CompoundTerm fact = (CompoundTerm) facts.apply(i);
			factbase.add(fact);
		}	
	}

	/**
	 * Convenience method that wraps an input stream into a zero parameter 
	 * function object.
	 * 
	 * @param in The input stream.
	 * @return A zero parameter function object.
	 */
	private Function0<InputStream> function0(InputStream in) {
		return new Function0<InputStream>() {
	
			private InputStream in;
	
			// Well...
			public Function0<InputStream> bounce(InputStream in) {
				this.in = in;
				return this;
			}
			
			public InputStream apply() {
				return in;
			}
			
			public byte apply$mcB$sp() {
				return 0;
			}
	
			public char apply$mcC$sp() {
				return 0;
			}
	
			public double apply$mcD$sp() {
				return 0;
			}
	
			public float apply$mcF$sp() {
				return 0;
			}
	
			public int apply$mcI$sp() {
				return 0;
			}
	
			public long apply$mcL$sp() {
				return 0;
			}
	
			public short apply$mcS$sp() {
				return 0;
			}
	
			public void apply$mcV$sp() {}
	
			public boolean apply$mcZ$sp() {
				return false;
			}
		}.bounce(in);
	}
}
