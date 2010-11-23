package saere.database;

import static saere.database.DatabaseTermFactory.*;
import saere.Term;

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
	
	/** Every invoke instruction: <tt>instr(X,Y,invoke(interface,A,B,C)).</tt> */
	public static final Term IQ7 = ct("instr", v(), v(), ct("invoke", sa("interface"), v(),v(),v()));
	
	public static final Term IQ8 = ct("instr", v(), v(), ct("invoke", v(), v(),v(),v()));
	
	/** Every return instruction: <tt>instr(X,Y,return(Z)).</tt> */
	public static final Term IQ9 = ct("instr", v(), v(), ct("return", v()));
						  	//FIXME instr(m_4641,13,invoke(static,class(scala/runtime,BoxesRunTime), boxToInteger, signature([int], class(java/lang, Integer))))
	/** A specific instruction: <tt>instr(m_4641,13,invoke(static,class('scala/runtime','BoxesRunTime'),'boxToInteger',signature([int],class('java/lang','Integer')))).</tt> */
	public static final Term IQ10 = ct("instr", v(), v(), ct("invoke", sa("static"), ct("class", sa("scala/runtime"), sa("BoxesRunTime")), sa("boxToInteger"), ct("signature", sa("[int]"), ct("class", sa("java/lang"), sa("Integer")))));
	
	public static final Term IQ11 = ct("instr", v(), v(), ct("load", v(), v()));
	
	public static final Term IQ12 = ct("instr", v(), v(), ct("load", sa("reference"), v()));
	
	public static final Term IQ13 = ct("instr", v(), v(), ct("load", sa("int"),v()));
	
	/** All queries for <tt>instr/3</tt> facts. */
	public static final Term[] ALL_INSTR_QUERIES = new Term[] { IQ0, IQ1, IQ2, IQ3, IQ4, IQ5, IQ6, IQ7, IQ8, IQ9, IQ10, IQ11, IQ12, IQ13 };
	
	// class_file/10
	
	/** All class files: <tt>class_files(A,B,C,D,E,F,G,H,I,J)</tt>. */
	public static final Term CQ1 = ct("class_file", v(), v(), v(), v(), v(), v(), v(), v(), v(), v());
	
	/** The first class file: <tt>class_files(cf_1,B,C,D,E,F,G,H,I,J)</tt>. */
	public static final Term CQ2 = ct("class_file", sa("cf_1"), v(), v(), v(), v(), v(), v(), v(), v(), v());
	
	/** The 20th class file: <tt>class_files(cf_20,B,C,D,E,F,G,H,I,J)</tt>. */
	public static final Term CQ3 = ct("class_file", sa("cf_20"), v(), v(), v(), v(), v(), v(), v(), v(), v());
	
	/** The 100th class file: <tt>class_files(cf_100,B,C,D,E,F,G,H,I,J)</tt>. */
	public static final Term CQ4 = ct("class_file", sa("cf_100"), v(), v(), v(), v(), v(), v(), v(), v(), v());
	
	/** All final class file: <tt>class_files(A,B,C,D,E,final(yes),G,H,I,J)</tt>. */
	public static final Term CQ5 = ct("class_file", v(), v(), v(), v(), v(), v(), ct("final", sa("yes")), v(), v(), v());
	
	/** All abstract class file: <tt>class_files(A,B,C,D,E,final(yes),G,H,I,J)</tt>. */
	public static final Term CQ6 = ct("class_file", v(), v(), v(), v(), v(), v(), v(), ct("abstract", sa("yes")), v(), v());
	
	public static final Term CQ7 = ct("class_file", v(), sa("interface_declaration"), v(), v(), v(), v(), v(), v(), v(), v());
	
	public static final Term CQ8 = ct("class_file", v(), sa("class_declaration"), v(), v(), v(), v(), v(), ct("abstract", sa("yes")), v(), v());
	
	/** All queries for <tt>class_file/10</tt> facts. */
	public static final Term[] ALL_CLASSFILE_QUERIES = new Term[] { CQ1, CQ2, CQ3, CQ4, CQ5, CQ6, CQ7, CQ8 };
	
	// method/15
	
	/** All methods: <tt>method(A,B,C,D,E,F,G,H,I,J,K,L,M,N,O)</tt>. */
	public static final Term MQ1 = ct("method", v(), v(), v(), v(), v(), v(), v(), v(), v(), v(), v(), v(), v(), v(), v());
	
	/** All methods of the first class: <tt>method(cf_1,B,C,D,E,F,G,H,I,J,K,L,M,N,O)</tt>. */
	public static final Term MQ2 = ct("method", sa("cf_1"), v(), v(), v(), v(), v(), v(), v(), v(), v(), v(), v(), v(), v(), v());
	
	/** All methods of the 20th class: <tt>method(cf_20,B,C,D,E,F,G,H,I,J,K,L,M,N,O)</tt>. */
	public static final Term MQ3 = ct("method", sa("cf_20"), v(), v(), v(), v(), v(), v(), v(), v(), v(), v(), v(), v(), v(), v());
	
	/** All methods of the 100th class: <tt>method(cf_100,B,C,D,E,F,G,H,I,J,K,L,M,N,O)</tt>. */
	public static final Term MQ4 = ct("method", sa("cf_100"), v(), v(), v(), v(), v(), v(), v(), v(), v(), v(), v(), v(), v(), v());
	
	/** All methods: <tt>method(A,B,C,D,E,F,G,H,I,J,K,L,M,N,O)</tt>. */
	public static final Term MQ5 = ct("method", v(), v(), v(), v(), v(), v(), v(), v(), v(), v(), v(), v(), v(), v(), v());
	
	/** The first method: <tt>method(A,m_1,C,D,E,F,G,H,I,J,K,L,M,N,O)</tt>. */
	public static final Term MQ6 = ct("method", v(), sa("m_1"), v(), v(), v(), v(), v(), v(), v(), v(), v(), v(), v(), v(), v());
	
	/** The 20th method: <tt>method(A,m_20,C,D,E,F,G,H,I,J,K,L,M,N,O)</tt>. */
	public static final Term MQ7 = ct("method", v(), sa("m_20"), v(), v(), v(), v(), v(), v(), v(), v(), v(), v(), v(), v(), v());
	
	/** The 100th method: <tt>method(A,m_100,C,D,E,F,G,H,I,J,K,L,M,N,O)</tt>. */
	public static final Term MQ8 = ct("method", v(), sa("m_100"), v(), v(), v(), v(), v(), v(), v(), v(), v(), v(), v(), v(), v());
	
	/** All public methods: <tt>method(A,B,C,D,public,E,G,H,I,J,K,L,M,N,O)</tt>. */
	public static final Term MQ9 = ct("method", v(), v(), v(), v(), sa("public"), v(), v(), v(), v(), v(), v(), v(), v(), v(), v());
	
	/** All deprecated methods: <tt>method(A,B,C,D,E,F,G,H,I,J,K,L,M,N,deprecated(yes)</tt>. */
	public static final Term MQ10 = ct("method", v(), v(), v(), v(), v(), v(), v(), v(), v(), v(), v(), v(), v(), v(), ct("deprecated", sa("yes")));
	
	/** All queries for <tt>method/15</tt> facts. */
	public static final Term[] ALL_METHOD_QUERIES = new Term[] { MQ1, MQ2, MQ3, MQ4, MQ5, MQ6, MQ7, MQ8, MQ9, MQ10 };
	
	// Everything
	
	/** All queries. */
	public static final Term[] ALL_QUERIES = new Term[] {
		IQ0, IQ1, IQ2, IQ3, IQ4, IQ5, IQ6, IQ7, IQ8, IQ9, IQ10,
		CQ1, CQ2, CQ3, CQ4, CQ5, CQ6, CQ7, CQ8,
		MQ1, MQ2, MQ3, MQ4, MQ5, MQ6, MQ7, MQ8, MQ9, MQ10
	};
}
