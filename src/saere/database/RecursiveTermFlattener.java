package saere.database;

import java.util.ArrayList;
import java.util.List;

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
public class RecursiveTermFlattener extends AbstractTermFlattener {

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
				Term[] argTerms = flatten(term.arg(i));  // recursive term flattening
				for (Term argTerm : argTerms) {
					terms.add(argTerm);
				}
			}
		}
		
		if (maxLength > 0 && terms.size() > maxLength) {
			terms = terms.subList(0, maxLength + 1); // it may actually a little late and costly to prune now...
		}
		
		return terms.toArray(new Term[0]);
	}

}
