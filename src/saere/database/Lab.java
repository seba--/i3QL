package saere.database;

import static saere.database.Utils.queryNoPrint;
import static saere.database.Utils.query;
import saere.Term;
import saere.Variable;

public class Lab {
	
	private static final boolean PRINT = false;
	
	private static final Database TDB = Database_Trie.getInstance();
	private static final Database SDB = Database_Default.getInstance();
	
	public static void main(String[] args) {
		
		// Medium:		"../test/classfiles/Tomcat-6.0.20.zip"
		// Small:		"../bat/build/opal-0.5.0.jar"
		// Small:		"../test/classfiles/shiftone-jrat.jar"
		// Very small:	"../test/classfiles/MMC.jar"
		// Tiny:		"../test/classfiles/HelloWorld.class"
		Factbase.read("../bat/build/opal-0.5.0.jar");
		TDB.fill();
		SDB.fill();
		
		Instr3_Trie instr3p_Trie = new Instr3_Trie(); // with trie
		Instr3_Default instr3p_Default = new Instr3_Default(); // with list
		
		Variable x = new Variable();
		Variable y = new Variable();
		Variable z = new Variable();
		
		// instr(X, Y, Z).
		System.out.print("With Tries: ");
		q(instr3p_Trie, x, y, z);

		System.out.print("Without Tries: ");
		q(instr3p_Default, x, y, z);
		
		// instr(X, 1, Z).
		System.out.print("With Tries: ");
		q(instr3p_Trie, x, DatabaseTermFactory.makeIntegerAtom(1), z);
		
		System.out.print("Without Tries: ");
		q(instr3p_Default, x, DatabaseTermFactory.makeIntegerAtom(1), z);
		
		// instr(m_1, Y, Z).
		System.out.print("With Tries: ");
		q(instr3p_Trie, DatabaseTermFactory.makeStringAtom("m_20"), y, z);
		
		System.out.print("Without Tries: ");
		q(instr3p_Default, DatabaseTermFactory.makeStringAtom("m_20"), y, z);
	}
	
	private static void q(DatabasePredicate p, Term ... terms) {
		if (PRINT) {
			query(p, terms);
		} else {
			queryNoPrint(p, terms);
		}	
	}
}
