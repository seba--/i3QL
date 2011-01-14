package saere.database.index;

import java.util.LinkedList;
import java.util.List;

import saere.CompoundTerm;
import saere.StringAtom;
import saere.Term;

/**
 * The {@link FullFlattener} recursively flattens terms. That is, 
 * arguments of {@link CompoundTerm}s are also flattend (and if these are 
 * arguments are {@link CompoundTerm}s they are flattened to and so on).<br/>
 * <br/>
 * For example the terms <code>f(a, b, c)</code> and <code>f(a, b(c))</code> 
 * are both flattend to the array <code>[f, a, b, c]</code>.<br/>
 * <br/>
 * Flattened term representations created by the {@link FullFlattener} 
 * tend to be longer than the ones created with the 
 * {@link ShallowFlattener}.
 * 
 * @author David Sullivan
 * @version 0.1, 9/22/2010
 * @see ShallowFlattener
 */
public final class FullFlattener extends TermFlattener {
	
	@Override
	public LabelStack flatten(Term term) {
		return new LabelStack(flattenTerm(term).toArray(new Label[0]));
	}

	private List<Label> flattenTerm(Term term) {		
		List<Label> flattened = new LinkedList<Label>();
		
		if (term.isStringAtom()) {
			flattened.add(AtomLabel.AtomLabel(term.functor()));
		} else if (term.isCompoundTerm()) {
			flattened.add(FunctorLabel.FunctorLabel(term.functor(), term.arity()));
			
			// Reorder according to profilings here (if we use profilings)!
			Term[] args = getArgs(term);
			
			for (int i = 0; i < args.length; i++) {		
				flattened.addAll(flattenTerm(args[i]));
			}
		} else if (term.isIntValue()) {
			flattened.add(AtomLabel.AtomLabel(term.asIntValue()));
		} else if (term.isFloatValue()) {
			flattened.add(AtomLabel.AtomLabel(term.asFloatValue()));
		} else if (term.isVariable()) {
			Term binding = term.asVariable().binding();
			if (binding != null) {
				flattened.addAll(flattenTerm(binding));
			} else {
				flattened.add(VariableLabel.VariableLabel());
			}
		} else { // 'term.isList()'
			if (term.functor().sameAs(StringAtom.EMPTY_LIST_FUNCTOR)) {
				flattened.add(AtomLabel.AtomLabel(term.functor()));
			} else {
				flattened.add(FunctorLabel.FunctorLabel(term.functor(), term.arity()));
				flattened.addAll(flattenTerm(term.arg(0)));
				flattened.addAll(flattenTerm(term.arg(1)));
			}
		}
		
		return flattened;
	}

	@Override
	public String toString() {
		return "full";
	}
}
