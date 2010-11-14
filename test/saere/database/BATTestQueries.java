package saere.database;

import static saere.database.DatabaseTermFactory.*;
import saere.Term;

public final class BATTestQueries {
	
	private BATTestQueries() { /* empty */ }
	
	/** All instructions: <tt>instr(X,Y,Z).</tt> */
	public static final Term Q0 = ct("instr", v(), v(), v());
	
	/** All instructions of the first method: <tt>instr(m_1,Y,Z).</tt> */
	public static final Term Q1 = ct("instr", sa("m_1"), v(), v());
	
	/** All instructions of the 200th method: <tt>instr(m_200,Y,Z).</tt> */
	public static final Term Q2 = ct("instr", sa("m_200"), v(), v());
	
	/** All instructions of the 5000th metho: <tt>instr(m_5000,Y,Z).</tt> */
	public static final Term Q3 = ct("instr", sa("m_5000"), v(), v());
	
	/** Every second instruction of all methods: <tt>instr(X,1,Z).</tt> */
	public static final Term Q4 = ct("instr", v(), ia(1), v());
	
	/** Every 20th instruction of all methods: <tt>instr(X,19,Z).</tt> */
	public static final Term Q5 = ct("instr", v(), ia(19), v());
	
	/** Every invoke instruction: <tt>instr(X,Y,invoke(Z,A)).</tt> */
	public static final Term Q6 = ct("instr", v(), v(), ct("invoke", v(), v()));
	
	/** Every return instruction: <tt>instr(X,Y,return(Z)).</tt> */
	public static final Term Q7 = ct("instr", v(), v(), ct("return", v()));
	
	/** All queries for <tt>instr/3</tt> facts. */
	public static final Term[] ALL_INSTR_QUERIES = new Term[] { Q0, Q1, Q2, Q3, Q4, Q5, Q6, Q7 };
	
	/** All queries for <tt>class_file/10</tt> facts. */
	public static final Term[] ALL_CLASSFILE_QUERIES = new Term[] { };
	
	/** All queries for <tt>method/15</tt> facts. */
	public static final Term[] ALL_METHOD_QUERIES = new Term[] { };
	
	/** All queries. */
	public static final Term[] ALL_QUERIES = new Term[] { Q0, Q1, Q2, Q3, Q4, Q5, Q6, Q7 };
}
