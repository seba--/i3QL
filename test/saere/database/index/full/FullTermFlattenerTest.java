package saere.database.index.full;

import org.junit.Test;

import saere.IntegerAtom;
import saere.StringAtom;
import saere.Term;
import saere.database.index.TermFlattener;
import saere.meta.GenericCompoundTerm;

public class FullTermFlattenerTest {
	
	private static final StringAtom F = StringAtom.StringAtom("f");
	private static final StringAtom A = StringAtom.StringAtom("a");
	private static final IntegerAtom I1 = IntegerAtom.IntegerAtom(1);
	private static final StringAtom C = StringAtom.StringAtom("c");
	
	// a(1)
	private static final Term A1 = new GenericCompoundTerm(A, new Term [] { I1 });
	
	// f(a, 1, c)
	private static final Term FA1C_FLAT = new GenericCompoundTerm(F, new Term[] { A, I1, C});
	
	// f(a(1), c)
	private static final Term FA1C_COMP = new GenericCompoundTerm(F, new Term[] { A1, C });
	
	private static final TermFlattener FLATTENER = new FullTermFlattener();
	
	@Test
	public void test1() {
		System.out.println(flattenedToString(FLATTENER.flatten(FA1C_FLAT)));
		System.out.println(flattenedToString(FLATTENER.flatten(FA1C_COMP)));
		
		System.out.println(flattenedToString(FLATTENER.flattenForQuery(FA1C_FLAT)));
		System.out.println(flattenedToString(FLATTENER.flattenForQuery(FA1C_COMP)));
	}
	
	private String flattenedToString(Term[] flattened) {
		StringBuilder sb = new StringBuilder();
		//sb.append('[');
		for (Term atom : flattened) {
			if (atom == FullTermFlattener.OPEN) {
				sb.append('<');
			} /*else if (atom == FullTermFlattener.COMMA) {
				sb.append(";");
			} */else if (atom == FullTermFlattener.CLOSE) {
				sb.append(">");
			} else {
				sb.append(atom.toString());
			}
		}
		return sb.toString();
		//return sb.append(']').toString();
	}
}
