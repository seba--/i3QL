package saere.database;

import static saere.database.DatabaseTermFactory.ct;
import static saere.database.DatabaseTermFactory.ia;
import static saere.database.DatabaseTermFactory.sa;
import static saere.database.DatabaseTermFactory.v;
import saere.Term;

public final class BATInstr3Queries {
	
	private BATInstr3Queries() { /* empty */ }
	
	public static final Term[] QUERIES = {
		
			// instr(X,Y,invoke(interface,Z,A,B))
			ct("instr", v(), v(), ct("invoke", sa("interface"), v(),v(),v())),
			
			// instr(X,Y,invoke(Z,A,B,C))
			ct("instr", v(), v(), ct("invoke", v(), v(),v(),v())),
			
			/** All instructions: <tt>instr(X,Y,Z).</tt> */
			ct("instr", v(), v(), v()),
			
			/** All instructions of the first method: <tt>instr(m_1,Y,Z).</tt> */
			ct("instr", sa("m_1"), v(), v()),
			
			/** All instructions of the 200th method: <tt>instr(m_200,Y,Z).</tt> */
			ct("instr", sa("m_200"), v(), v()),
			
			/** All instructions of the 5000th metho: <tt>instr(m_5000,Y,Z).</tt> */
			ct("instr", sa("m_5000"), v(), v()),
			
			/** Every second instruction of all methods: <tt>instr(X,1,Z).</tt> */
			ct("instr", v(), ia(1), v()),
			
			/** Every 10th instruction of all methods: <tt>instr(X,19,Z).</tt> */
			ct("instr", v(), ia(9), v()),
			
			/** Every 20th instruction of all methods: <tt>instr(X,19,Z).</tt> */
			ct("instr", v(), ia(19), v()),
			
			/** Every invoke instruction: <tt>instr(X,Y,invoke(interface,A,B,C)).</tt> */
			ct("instr", v(), v(), ct("invoke", sa("interface"), v(),v(),v())),
			
			ct("instr", v(), v(), ct("invoke", v(), v(),v(),v())),
			
			/** Every return instruction: <tt>instr(X,Y,return(Z)).</tt> */
			ct("instr", v(), v(), ct("return", v())),
			
			ct("instr", v(), v(), ct("load", v(), v())),
			
			ct("instr", v(), v(), ct("load", sa("reference"), v())),
			
			ct("instr", v(), v(), ct("load", sa("int"),v()))
	};
}
