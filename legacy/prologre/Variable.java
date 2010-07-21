package prologre;

public final class Variable implements Term {

    private static class VariableState implements State {
	final Term term;
	final boolean bound;

	VariableState(boolean bound, Term term) {
	    this.term = term;
	    this.bound = bound;
	}
    };

    private final static VariableState NO_STATE = new VariableState(false, null);

    private Term boundTerm;

    private boolean bound = false;

    public void bind(Term term) {

	this.boundTerm = term;
	this.bound = true;
    }

    public Term getBinding() {
	return this.boundTerm;
    }

    // A variable
    public boolean isFree() {
	return !this.bound;
    }

    public void clear() {
	this.boundTerm = null;
	this.bound = false;
    }

    @Override
    public boolean unify(Term term) {
	if (this == term) {
	    return true;
	} else {
	    if (isFree()) {
		bind(term);
		return true;
	    } else {
		return this.boundTerm.unify(term);
	    }
	}
    }

    @Override
    public State manifestState() {

	if (this.boundTerm == null && !this.bound)
	    return NO_STATE;
	else
	    return new VariableState(this.bound, this.boundTerm);
    }

    @Override
    public void setState(State state) {
	VariableState vState = ((VariableState) state);

	this.bound = vState.bound;
	this.boundTerm = vState.term;
    }

    @Override
    public Atom asAtom() throws UnsupportedOperationException {
	throw new UnsupportedOperationException();
    }

    @Override
    public Variable asVariable() {
	return this;
    }

    @Override
    public boolean isAtom() {
	return false;
    }

    @Override
    public boolean isVariable() {
	return true;
    }

    @Override
    public CompoundTerm asCompoundTerm() throws UnsupportedOperationException {
	throw new UnsupportedOperationException();
    }

    @Override
    public boolean isCompoundTerm() {
	return false;
    }

    @Override
    public String toString() {
	return this.bound ? "" + this.boundTerm : "<FREE>";
    }
}
