package saere.database.index.full;

import java.util.ArrayList;
import java.util.List;

import saere.Atom;
import saere.StringAtom;
import saere.Term;
import saere.Variable;
import saere.database.index.TermFlattener;

public class FullTermFlattener implements TermFlattener {
	
	// XXX Actually, we need something that makes these *really* unique...
	public static final StringAtom OPEN = StringAtom.StringAtom("_(");
	public static final StringAtom CLOSE = StringAtom.StringAtom("_)");
	//public static final StringAtom COMMA = StringAtom.StringAtom("__;");

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
				flattened.add(OPEN);
				for (int i = 0; i < term.arity(); i++) {
					//if (i > 0)
						//flattened.add(COMMA);
					flattened.addAll(flattenNoVars(term.arg(i)));
				}
				flattened.add(CLOSE);
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
				flattened.add(OPEN);
				for (int i = 0; i < term.arity(); i++) {
					//if (i > 0)
						//flattened.add(COMMA);
					flattened.addAll(flattenWithVars(term.arg(i)));
				}
				flattened.add(CLOSE);
			}
		}
		
		return flattened;
	}
	
	@Override
	public String toString() {
		return "full";
	}
}
