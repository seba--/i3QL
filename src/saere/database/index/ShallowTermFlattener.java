package saere.database.index;

import java.util.ArrayList;
import java.util.List;

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
 * @version 0.1, 9/22/2010
 */
public class ShallowTermFlattener extends AbstractTermFlattener {

	@Override
	public Term[] flatten(Term term) {
		assert term != null : "term is null";
		
		// term is a variable
		if (term.isVariable()) {
			Variable var = term.asVariable();
			
			if (var.isInstantiated()) {
				return flatten(term.asVariable().binding());
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
				terms.add(atomize(term.arg(i))); // add arguments as terms
			}
		}
		
		if (maxLength > 0 && terms.size() > maxLength) {
			terms = terms.subList(0, maxLength + 1); // it may actually a little late and costly to prune now...
		}
		
		return terms.toArray(new Term[0]);
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
	private Term atomize(Term term) {
		if (term.isIntegerAtom() || term.isStringAtom()) {
			return term;
		} else if (term.isCompoundTerm()) {
			return term.functor();
		} else {
			Variable var = term.asVariable();
			if (!var.isInstantiated()) {
				return term;
			} else {
				term = var.asVariable().binding();
				return atomize(term);
			}
		}
	}

	@Override
	public Term[] flattenQuery(Term... terms) {
		assert terms.length > 0 : "Invalid query length 0";
		
		Term[] flattened = new Term[terms.length];
		for (int i = 0; i < flattened.length; i++) {
			flattened[i] = atomize(terms[i]);
		}
		
		return flattened;
	}

}
