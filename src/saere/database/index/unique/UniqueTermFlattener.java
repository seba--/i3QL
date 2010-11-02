package saere.database.index.unique;

import java.util.ArrayList;
import java.util.List;

import saere.Atom;
import saere.StringAtom;
import saere.Term;
import saere.Variable;
import saere.database.index.TermFlattener;

public class UniqueTermFlattener implements TermFlattener {
	
	// XXX Actually, we need something that makes the separator *really* unique...
	private static final byte[] SEPARATOR_BYTES = new byte[0];
	
	public static final StringAtom SEPARATOR = StringAtom.StringAtom(SEPARATOR_BYTES);

	@Override
	public Atom[] flattenForInsertion(Term term) {
		return flattenNoVars(term).toArray(new Atom[0]);
	}
	
	// Term is not allowed to have variables (or be one)!
	private List<Atom> flattenNoVars(Term term) {
		List<Atom> flattened = new ArrayList<Atom>();
		
		// Either an integer atom or an string atom / compound term
		if (term.isIntegerAtom()) {
			flattened.add(term.asIntegerAtom());
		} else {
			flattened.add(term.functor());
			
			if (term.isCompoundTerm()) {
				flattened.add(SEPARATOR);
				for (int i = 0; i < term.arity(); i++) {
					flattened.addAll(flattenNoVars(term.arg(i)));
				}
				flattened.add(SEPARATOR);
			}
		}
		
		return flattened;
	}
	
	@Override
	public Term[] flattenForQuery(Term... terms) {
		List<Term> flattened = new ArrayList<Term>();
		for (Term term : terms) {
			flattened.addAll(flattenWithVars(term));
		}
		return flattened.toArray(new Term[0]);
	}

	private List<Term> flattenWithVars(Term term) {
		List<Term> flattened = new ArrayList<Term>();
		
		// Either integer atom, variable, or string atom / compound term
		if (term.isIntegerAtom()) {
			flattened.add(term.asIntegerAtom());
		} else if (term.isVariable()) {
			Variable var = term.asVariable();
			
			if (!var.isInstantiated()) {
				flattened.add(var);
			} else {
				term = var.binding();
				flattened.addAll(flattenWithVars(term));
			}
		} else {
			flattened.add(term.functor());
			
			if (term.isCompoundTerm()) {
				flattened.add(SEPARATOR);
				for (int i = 0; i < term.arity(); i++) {
					flattened.addAll(flattenWithVars(term.arg(i)));
				}
				flattened.add(SEPARATOR);
			}
		}
		
		return flattened;
	}

}
