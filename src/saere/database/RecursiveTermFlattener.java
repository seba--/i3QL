package saere.database;

import java.util.ArrayList;
import java.util.List;

import saere.Term;
import saere.Variable;

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
