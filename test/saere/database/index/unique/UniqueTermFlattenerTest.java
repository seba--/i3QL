package saere.database.index.unique;

import org.junit.Test;

import saere.IntegerAtom;
import saere.StringAtom;
import saere.Term;
import saere.database.index.TermFlattener;
import saere.meta.GenericCompoundTerm;

public class UniqueTermFlattenerTest {
	
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
	
	private static final TermFlattener FLATTENER = new UniqueTermFlattener();
	
	@Test
	public void test1() {
		System.out.println(flattenedToString(FLATTENER.flattenForInsertion(FA1C_FLAT)));
		System.out.println(flattenedToString(FLATTENER.flattenForInsertion(FA1C_COMP)));
		
		System.out.println(flattenedToString(FLATTENER.flattenForQuery(FA1C_FLAT)));
		System.out.println(flattenedToString(FLATTENER.flattenForQuery(FA1C_COMP)));
	}
	
	private String flattenedToString(Term[] flattened) {
		StringBuilder sb = new StringBuilder();
		sb.append('[');
		boolean first = true;
		for (Term atom : flattened) {
			if (atom == UniqueTermFlattener.SEPARATOR) {
				sb.append('|');
				first = true;
			} else {
				if (first) {
					first = false;
				} else {
					sb.append(',');
				}
				sb.append(atom.toString());
			}
		}
		return sb.append(']').toString();
	}
}
