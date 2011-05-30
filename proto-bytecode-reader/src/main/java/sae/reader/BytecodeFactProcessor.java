package sae.reader;

import de.tud.cs.st.bat.ClassFile;

public interface BytecodeFactProcessor {

	/**
	 * Adds a classfile to the potential fact base. The elements of the
	 * classfile are added to the database upon calling processAllFacts()
	 * 
	 * @param classFile
	 */
	void processClassFile(ClassFile classFile);


	void processAllFacts();
}
