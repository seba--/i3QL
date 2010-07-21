package prologre;

public interface Term {

    boolean unify(Term term);

    // The return value State can be null when the corresponding setState method also accepts a
    // null value
    State manifestState();

    void setState(State state);

    boolean isAtom();

    Atom asAtom() throws UnsupportedOperationException;

    boolean isVariable();

    Variable asVariable() throws UnsupportedOperationException;

    boolean isCompoundTerm();

    CompoundTerm asCompoundTerm() throws UnsupportedOperationException;

}
