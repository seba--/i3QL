package saere.database.index;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import saere.Atom;
import saere.CompoundTerm;
import saere.Term;
import saere.Variable;

/**
 * The {@link RecursiveTermFlattener} recursively flattens terms. That is, 
 * arguments of {@link CompoundTerm}s are also flattend (and if these are 
 * arguments are {@link CompoundTerm}s they are flattened to and so on).<br/>
 * <br/>
 * For example the terms <code>f(a, b, c)</code> and <code>f(a, b(c))</code> 
 * are both flattend to the array <code>[f, a, b, c]</code>.<br/>
 * <br/>
 * Flattened term representations created by the {@link RecursiveTermFlattener} 
 * tend to be longer than the ones created with the 
 * {@link ShallowTermFlattener}.
 * 
 * @author David Sullivan
 * @version 0.1, 9/22/2010
 * @see ShallowTermFlattener
 */
public final class RecursiveTermFlattener extends AbstractTermFlattener {

	@Override
	public Atom[] flattenForInsertion(Term term) {
		assert term != null : "term is null";
		
		// We assume that we get mostly compound terms (actually only(?)).
		if (term.isCompoundTerm()) {
			List<Atom> terms = new ArrayList<Atom>();
			terms.add(term.functor()); // Add functor as first term
			int min = maxLength > 0 ? Math.min(term.arity(), maxLength - 1) : term.arity();
			for (int i = 0; i < min; i++) {
				Atom[] argTerms = flattenForInsertion(term.arg(i));
				for (Atom argTerm : argTerms) {
					terms.add(argTerm); // Add arguments as terms
				}
			}
			return terms.toArray(new Atom[0]);
		} else if (term.isIntegerAtom()) {
			return new Atom[] { term.asIntegerAtom() };
		} else if (term.isStringAtom()) {
			return new Atom[] { term.asStringAtom() };
		} else {
			return new Atom[] { term.functor() }; // XXX Must suffice for lists
		}
	}

	@Override
	public Term[] flattenForQuery(Term... terms) {
		assert terms.length > 0 : "Invalid query length 0";
		
		List<Term> flattened = new LinkedList<Term>();
		for (int i = 0; i < terms.length; i++) {
			Term[] argTerms = flattenForQuery(terms[i]);
			for (Term argTerm : argTerms) {
				flattened.add(argTerm);
			}
		}
		
		return flattened.toArray(new Term[0]);
	}
	
	public Term[] flattenForQuery(Term term) {
		assert term != null : "term is null";
		
		// term is a variable
		if (term.isVariable()) {
			Variable var = term.asVariable();
			
			if (var.isInstantiated()) {
				return flattenForQuery(term.asVariable().binding());
			} else {
				return new Term[] { var };
			}	
		}
		
		if (term.isIntegerAtom()) {
			return new Term[] { term }; // don't make StringAtoms out of IntegerAtoms with functor()!
		}
		
		// term is not a variable ...
		List<Term> terms = new ArrayList<Term>();
		terms.add(term.functor()); // add functor as first term
		if (term.isCompoundTerm()) {
			for (int i = 0; i < term.arity(); i++) {
				Term[] argTerms = flattenForQuery(term.arg(i));  // recursive term flattening
				for (Term argTerm : argTerms) {
					terms.add(argTerm);
				}
			}
		}
		
		if (maxLength > 0 && terms.size() > maxLength) {
			terms = terms.subList(0, maxLength + 1);
		}
		
		return terms.toArray(new Term[0]);
	}

	@Override
	public String toString() {
		return "recursive";
	}
}
