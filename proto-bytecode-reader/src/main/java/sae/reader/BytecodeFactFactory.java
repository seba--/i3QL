package sae.reader;

import de.tud.cs.st.bat.ClassFile;

public interface BytecodeFactFactory extends FactFactory {

	/**
	 * Adds a classfile to the potential fact base. The elements of the
	 * classfile are added to the database upon calling addAllFacts()
	 * 
	 * @param classFile
	 */
	void addClassFile(ClassFile classFile);
}
