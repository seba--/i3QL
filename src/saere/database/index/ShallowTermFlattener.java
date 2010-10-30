package saere.database.index;

import java.util.ArrayList;
import java.util.List;

import saere.Atom;
import saere.CompoundTerm;
import saere.Term;
import saere.Variable;

/**
 * The {@link ShallowTermFlattener} flattens terms using their functor and 
 * arguments. If the argument is a {@link CompoundTerm} its functor is 
 * used.<br/>
 * <br/>
 * For example the terms <code>f(a, b, c)</code> and <code>f(a, b(c))</code> 
 * are flattend to the arrays <code>[f, a, b, c]</code> and 
 * <code>[f, a, b]</code>, respectively.<br/>
 * <br/>
 * Flattened term representations created by the {@link ShallowTermFlattener} 
 * tend to be shorter than the ones created with the 
 * {@link RecursiveTermFlattener}.
 * 
 * @author David Sullivan
 * @version 0.2, 10/14/2010
 */
public final class ShallowTermFlattener extends AbstractTermFlattener {

	@Override
	public Atom[] flattenInsertion(Term term) {
		assert term != null : "term is null";
		
		// We assume that we get mostly compound terms (actually only(?)).
		if (term.isCompoundTerm()) {
			List<Atom> terms = new ArrayList<Atom>();
			terms.add(term.functor()); // Add functor as first term
			int min = maxLength > 0 ? Math.min(term.arity(), maxLength - 1) : term.arity();
			for (int i = 0; i < min; i++) {
				terms.add(atomizeForInsertion(term.arg(i))); // Add arguments as terms
			}
			return terms.toArray(new Atom[0]);
		} else if (term.isIntegerAtom()) {
			return new Atom[] { term.asIntegerAtom() };
		} else if (term.isStringAtom()) {
			return new Atom[] { term.asStringAtom() };
		} else {
			return new Atom[] { term.functor() }; // Should suffice for lists
		}
	}
	
	private Atom atomizeForInsertion(Term term) {
		if (term.isIntegerAtom()) {
			return term.asIntegerAtom();
		} else { // string atom, compound term
			return term.functor();
		}
	}
	
	/**
	 * Gets an atom/variable repesentation of a term. If the specified term is 
	 * an integer or string atom, the term itself is returned. It it is a compound 
	 * term, the functor is returned. Unbound variables are returned unmodified 
	 * while bound variables are processed depending on their binding.
	 * 
	 * @param term The term to atomize.
	 * @return An atom/variable representation of the specified term.
	 */
	private Term atomizeForQuery(Term term) {
		if (term.isIntegerAtom() || term.isStringAtom()) {
			return term;
		} else if (term.isVariable()) { 
			Variable var = term.asVariable();
			if (!var.isInstantiated()) {
				return term;
			} else {
				term = var.asVariable().binding();
				return atomizeForQuery(term);
			}
		} else { // CompoundTerm or EmptyList
			return term.functor();		
		}
	}

	@Override
	public Term[] flattenQuery(Term... terms) {
		assert terms.length > 0 : "Invalid query length 0";
		
		Term[] flattened = new Term[terms.length];
		for (int i = 0; i < flattened.length; i++) {
			flattened[i] = atomizeForQuery(terms[i]);
		}
		
		return flattened;
	}

}
