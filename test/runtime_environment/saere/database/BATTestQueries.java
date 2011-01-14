package saere.database;

import static saere.database.DatabaseTermFactory.*;
import saere.Term;

/**
 * Manually composed queries for BAT facts.
 * 
 * @author David Sullivah
 * @version 0.3, 11/29/2010
 */
public final class BATTestQueries {
	
	private BATTestQueries() { /* empty */ }
	
	
	
	// instr/3
	
	/** All instructions: <tt>instr(X,Y,Z).</tt> */
	public static final Term IQ0 = ct("instr", v(), v(), v());
	
	/** All instructions of the first method: <tt>instr(m_1,Y,Z).</tt> */
	public static final Term IQ1 = ct("instr", sa("m_1"), v(), v());
	
	/** All instructions of the 200th method: <tt>instr(m_200,Y,Z).</tt> */
	public static final Term IQ2 = ct("instr", sa("m_200"), v(), v());
	
	/** All instructions of the 5000th metho: <tt>instr(m_5000,Y,Z).</tt> */
	public static final Term IQ3 = ct("instr", sa("m_5000"), v(), v());
	
	/** Every second instruction of all methods: <tt>instr(X,1,Z).</tt> */
	public static final Term IQ4 = ct("instr", v(), ia(1), v());
	
	/** Every 10th instruction of all methods: <tt>instr(X,19,Z).</tt> */
	public static final Term IQ5 = ct("instr", v(), ia(9), v());
	
	/** Every 20th instruction of all methods: <tt>instr(X,19,Z).</tt> */
	public static final Term IQ6 = ct("instr", v(), ia(19), v());
	
	/** Every invoke instruction: <tt>instr(X,Y,invoke(Z,A,B,C)).</tt> */
	public static final Term IQ7 = ct("instr", v(), v(), ct("invoke", v(), v(),v(),v()));
	
	/** Every invoke interface instruction: <tt>instr(X,Y,invoke(interface,A,B,C)).</tt> */
	public static final Term IQ8 = ct("instr", v(), v(), ct("invoke", sa("interface"), v(),v(),v()));
	
	/** Every invoke static instruction: <tt>instr(X,Y,invoke(static,A,B,C)).</tt> */
	public static final Term IQ9 = ct("instr", v(), v(), ct("invoke", sa("static"), v(),v(),v()));
	
	/** Every invoke virtual instruction: <tt>instr(X,Y,invoke(virtual,A,B,C)).</tt> */
	public static final Term IQ10 = ct("instr", v(), v(), ct("invoke", sa("virtual"), v(),v(),v()));
	
	/** Every return instruction: <tt>instr(X,Y,return(Z)).</tt> */
	public static final Term IQ11 = ct("instr", v(), v(), ct("return", v()));
	
	/** Every return(void) instruction: <tt>instr(X,Y,return(void)).</tt> */
	public static final Term IQ12 = ct("instr", v(), v(), ct("return", sa("void")));
	
	/** Every load instruction: <tt>instr(X,Y,load(Z,A)).</tt> */
	public static final Term IQ13 = ct("instr", v(), v(), ct("load", v(), v()));
	
	/** Every reference load instruction: <tt>instr(X,Y,load(reference,Z)).</tt> */
	public static final Term IQ14 = ct("instr", v(), v(), ct("load", sa("reference"), v()));
	
	/** Every int load instruction: <tt>instr(X,Y,load(int,Z)).</tt> */
	public static final Term IQ15 = ct("instr", v(), v(), ct("load", sa("int"),v()));
	
	/** A query with no expected result (invalid first argument): <tt>instr(abc_0,Y,Z)</tt>. */
	public static final Term IQ16 = ct("instr", sa("abc_0"), v(), v());
	
	/** A query with no expected result (invalid second argument): <tt>instr(abc_0,Y,Z)</tt>. */
	public static final Term IQ17 = ct("instr", v(), ia(-1), v());
	
	/** All queries for <tt>instr/3</tt> facts. */
	public static final Term[] ALL_INSTR3_QUERIES = new Term[] { IQ0, IQ1, IQ2, IQ3, IQ4, IQ5, IQ6, IQ7, IQ8, IQ9, IQ10, IQ11, IQ12, IQ13, IQ14, IQ15, IQ16, IQ17 };
	
	/** All queries for <tt>instr/3</tt> with approximated frequencies. */
	public static final Term[] ALL_INSTR3_FREQ_QUERIES = new Term[] { IQ0, IQ1, IQ5, IQ10, IQ10, IQ7, IQ7, IQ8, IQ8, IQ9, IQ9, IQ10, IQ10, IQ11, IQ11, IQ12, IQ12, IQ13, IQ13, IQ14, IQ14, IQ15, IQ15 };
	
	
	
	// class_file/10
	
	/** All class files: <tt>class_file(A,B,C,D,E,F,G,H,I,J)</tt>. */
	public static final Term CQ0 = ct("class_file", v(), v(), v(), v(), v(), v(), v(), v(), v(), v());
	
	/** The first class file: <tt>class_file(cf_1,B,C,D,E,F,G,H,I,J)</tt>. */
	public static final Term CQ1 = ct("class_file", sa("cf_1"), v(), v(), v(), v(), v(), v(), v(), v(), v());
	
	/** The 20th class file: <tt>class_file(cf_20,B,C,D,E,F,G,H,I,J)</tt>. */
	public static final Term CQ2 = ct("class_file", sa("cf_20"), v(), v(), v(), v(), v(), v(), v(), v(), v());
	
	/** The 100th class file: <tt>class_file(cf_100,B,C,D,E,F,G,H,I,J)</tt>. */
	public static final Term CQ3 = ct("class_file", sa("cf_100"), v(), v(), v(), v(), v(), v(), v(), v(), v());
	
	/** All final class files: <tt>class_file(A,B,C,D,E,final(yes),G,H,I)</tt>. */
	public static final Term CQ4 = ct("class_file", v(), v(), v(), v(), v(), v(), ct("final", sa("yes")), v(), v(), v());
	
	/** All non-final class files: <tt>class_file(A,B,C,D,E,final(no),G,H,I,J)</tt>. */
	public static final Term CQ5 = ct("class_file", v(), v(), v(), v(), v(), v(), ct("final", sa("no")), v(), v(), v());
	
	/** All abstract class files: <tt>class_file(A,B,C,D,E,F,G,abstract(yes),I,J)</tt>. */
	public static final Term CQ6 = ct("class_file", v(), v(), v(), v(), v(), v(), v(), ct("abstract", sa("yes")), v(), v());
	
	/** All non-abstract class files: <tt>class_file(A,B,C,D,E,F,G,abstract(no),I,J)</tt>. */
	public static final Term CQ7 = ct("class_file", v(), v(), v(), v(), v(), v(), v(), ct("abstract", sa("no")), v(), v());
	
	/** All interface declarations: <tt>class_file(A,interface_declaration,C,D,E,F,G,H,I,J)</tt>. */
	public static final Term CQ8 = ct("class_file", v(), sa("interface_declaration"), v(), v(), v(), v(), v(), v(), v(), v());
	
	/** All class declarations: <tt>class_file(A,class_declaration,C,D,E,F,G,H,I,J)</tt>. */
	public static final Term CQ9 = ct("class_file", v(), sa("class_declaration"), v(), v(), v(), v(), v(), v(), v(), v());
	
	/** All abstract class declarations: <tt>class_file(A,class_declaration,C,D,E,F,G,abstract(yes),I,J)</tt>. */
	public static final Term CQ10 = ct("class_file", v(), sa("class_declaration"), v(), v(), v(), v(), v(), ct("abstract", sa("yes")), v(), v());
	
	/** All non-abstract class declarations: <tt>class_file(A,class_declaration,C,D,E,F,G,abstract(no),I,J)</tt>. */
	public static final Term CQ11 = ct("class_file", v(), sa("class_declaration"), v(), v(), v(), v(), v(), ct("abstract", sa("no")), v(), v());
	
	/** A query with no expected result (invalid first argument): <tt>class_file(abc_0,B,C,D,E,F,G,H,I,J)</tt>. */
	public static final Term CQ12 = ct("class_file", sa("abc_0"), v(), v(), v(), v(), v(), v(), v(), v(), v());
	
	/** A query with no expected result (invalid second argument): <tt>class_file(A,metaclazz_declaration,C,D,E,F,G,H,I,J)</tt>. */
	public static final Term CQ13 = ct("class_file", v(), sa("metaclazz_declaration"), v(), v(), v(), v(), v(), v(), v(), v());
	
	/** A query with very few free variables. */
	public static final Term CQ14 = ct("class_file", v(), sa("class_declaration"), v(), ct("class", sa("java/lang"), sa("Object")), v(), sa("public"), ct("final", sa("no")), ct("abstract", sa("no")), ct("synthetic", sa("no")), ct("deprecated", sa("no")));
	
	/** All queries for <tt>class_file/10</tt> facts. */
	public static final Term[] ALL_CLASSFILE10_QUERIES = new Term[] { CQ0, CQ1, CQ2, CQ3, CQ4, CQ5, CQ6, CQ7, CQ8, CQ9, CQ10, CQ11, CQ12, CQ13, CQ14 };
	
	/** All queries for <tt>class_file/10</tt> with approximated frequencies. */
	public static final Term[] ALL_CLASSFILE10_FREQ_QUERIES = new Term[] { CQ0, CQ1, CQ2, CQ4, CQ4, CQ4, CQ7, CQ7, CQ7, CQ8, CQ8, CQ8, CQ8, CQ9, CQ9, CQ10, CQ11, CQ11, CQ11, CQ14, CQ14, CQ14, CQ14, CQ14 };
	
	
	
	// method/15
	
	/** All methods: <tt>method(A,B,C,D,E,F,G,H,I,J,K,L,M,N,O)</tt>. */
	public static final Term MQ0 = ct("method", v(), v(), v(), v(), v(), v(), v(), v(), v(), v(), v(), v(), v(), v(), v());
	
	/** All methods of the first class: <tt>method(cf_1,B,C,D,E,F,G,H,I,J,K,L,M,N,O)</tt>. */
	public static final Term MQ1 = ct("method", sa("cf_1"), v(), v(), v(), v(), v(), v(), v(), v(), v(), v(), v(), v(), v(), v());
	
	/** All methods of the 20th class: <tt>method(cf_20,B,C,D,E,F,G,H,I,J,K,L,M,N,O)</tt>. */
	public static final Term MQ2 = ct("method", sa("cf_20"), v(), v(), v(), v(), v(), v(), v(), v(), v(), v(), v(), v(), v(), v());
	
	/** All methods of the 100th class: <tt>method(cf_100,B,C,D,E,F,G,H,I,J,K,L,M,N,O)</tt>. */
	public static final Term MQ3 = ct("method", sa("cf_100"), v(), v(), v(), v(), v(), v(), v(), v(), v(), v(), v(), v(), v(), v());
	
	/** The first method: <tt>method(A,m_1,C,D,E,F,G,H,I,J,K,L,M,N,O)</tt>. */
	public static final Term MQ4 = ct("method", v(), sa("m_1"), v(), v(), v(), v(), v(), v(), v(), v(), v(), v(), v(), v(), v());
	
	/** The 20th method: <tt>method(A,m_20,C,D,E,F,G,H,I,J,K,L,M,N,O)</tt>. */
	public static final Term MQ5 = ct("method", v(), sa("m_20"), v(), v(), v(), v(), v(), v(), v(), v(), v(), v(), v(), v(), v());
	
	/** The 100th method: <tt>method(A,m_100,C,D,E,F,G,H,I,J,K,L,M,N,O)</tt>. */
	public static final Term MQ6 = ct("method", v(), sa("m_100"), v(), v(), v(), v(), v(), v(), v(), v(), v(), v(), v(), v(), v());
	
	/** All public methods: <tt>method(A,B,C,D,public,E,G,H,I,J,K,L,M,N,O)</tt>. */
	public static final Term MQ7 = ct("method", v(), v(), v(), v(), sa("public"), v(), v(), v(), v(), v(), v(), v(), v(), v(), v());
	
	/** All protected methods: <tt>method(A,B,C,D,protected,E,G,H,I,J,K,L,M,N,O)</tt>. */
	public static final Term MQ8 = ct("method", v(), v(), v(), v(), sa("protected"), v(), v(), v(), v(), v(), v(), v(), v(), v(), v());
	
	/** All private methods: <tt>method(A,B,C,D,private,E,G,H,I,J,K,L,M,N,O)</tt>. */
	public static final Term MQ9 = ct("method", v(), v(), v(), v(), sa("private"), v(), v(), v(), v(), v(), v(), v(), v(), v(), v());
	
	/** All abstract methods:<tt>method(A,B,C,D,E,abstract(yes),G,H,I,J,K,L,M,N,O)</tt>. */
	public static final Term MQ10 = ct("method", v(), v(), v(), v(), v(), ct("abstract", sa("yes")), v(), v(), v(), v(), v(), v(), v(), v(), v());
	
	/** All non-abstract methods: <tt>method(A,B,C,D,E,abstract(no),G,H,I,J,K,L,M,N,O)</tt>. */
	public static final Term MQ11 = ct("method", v(), v(), v(), v(), v(), ct("abstract", sa("no")), v(), v(), v(), v(), v(), v(), v(), v(), v());
	
	/** All deprecated methods: <tt>method(A,B,C,D,E,F,G,H,I,J,K,L,M,N,deprecated(yes)</tt>. */
	public static final Term MQ12 = ct("method", v(), v(), v(), v(), v(), v(), v(), v(), v(), v(), v(), v(), v(), v(), ct("deprecated", sa("yes")));
	
	/** All non-deprecated methods: <tt>method(A,B,C,D,E,F,G,H,I,J,K,L,M,N,deprecated(no)</tt>. */
	public static final Term MQ13 = ct("method", v(), v(), v(), v(), v(), v(), v(), v(), v(), v(), v(), v(), v(), v(), ct("deprecated", sa("no")));
	
	/** A query with no expected result (invalid first argument): <tt>method(abc_0,B,C,D,E,F,G,H,I,J,K,L,M,N,O)</tt>. */
	public static final Term MQ14 = ct("method", sa("abc_0"), v(), v(), v(), v(), v(), v(), v(), v(), v(), v(), v(), v(), v(), v());
	
	/** A query with no expected result (invalid second argument): <tt>method(A,abc_0,C,D,E,F,G,H,I,J,K,L,M,N,O)</tt>. */
	public static final Term MQ15 = ct("method", v(), sa("abc_0"), v(), v(), v(), v(), v(), v(), v(), v(), v(), v(), v(), v(), v());
	
	/** All queries for <tt>method/15</tt> facts. */
	public static final Term[] ALL_METHOD15_QUERIES = new Term[] { MQ0, MQ1, MQ2, MQ3, MQ4, MQ5, MQ6, MQ7, MQ8, MQ9, MQ10, MQ11, MQ12, MQ13, MQ14, MQ15 };
	
	public static final Term[] ALL_METHOD15_FREQ_QUERIES = new Term[] { MQ0, MQ2, MQ2, MQ5, MQ7, MQ7, MQ9, MQ9, MQ10, MQ11, MQ11, MQ12, MQ12 };
	
	
	
	// Everything
	
	/** All queries. */
	public static final Term[] ALL_QUERIES = new Term[] {
		IQ0, IQ1, IQ2, IQ3, IQ4, IQ5, IQ6, IQ7, IQ8, IQ9, IQ10, IQ11, IQ12, IQ13, IQ14, IQ15, IQ16, IQ17,
		CQ0, CQ1, CQ2, CQ3, CQ4, CQ5, CQ6, CQ7, CQ8, CQ9, CQ10, CQ11, CQ12, CQ13, CQ14,
		MQ0, MQ1, MQ2, MQ3, MQ4, MQ5, MQ6, MQ7, MQ8, MQ9, MQ10, MQ11, MQ12, MQ13, MQ14, MQ15
	};
}
