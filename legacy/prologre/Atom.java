package prologre;

import java.nio.charset.Charset;
import java.util.Arrays;

public final class Atom implements GroundTerm {

    public static Charset UTF8Charset = Charset.forName("UTF-8");

    private final byte[] title;

    private final int hashCode;

    public static Atom Atom(final String title) {
	// TODO Use the flyweight pattern to share instances....
	return new Atom(title.getBytes(UTF8Charset));
    }

    public Atom(byte[] title) {
	this.title = title;
	this.hashCode = Arrays.hashCode(this.title);
    }

    public boolean unify(Term term) {

	if (term.isVariable()) {
	    return term.asVariable().unify(this);
	} else {
	    return term.asAtom().equals(this);
	}

    }

    public State manifestState() {
	return null;
    }

    public void setState(State parameter0State) {
	// nothing to do
    };

    @Override
    public boolean equals(Object other) {
	return other instanceof Atom && equals((Atom) other);
    }

    public boolean equals(Atom other) {
	return this == other || Arrays.equals(other.title, this.title);
    }

    @Override
    public int hashCode() {
	return this.hashCode;
    }

    @Override
    public String toString() {
	return new String(this.title, UTF8Charset);
    }

    @Override
    public Atom asAtom() {
	return this;
    }

    @Override
    public Variable asVariable() throws UnsupportedOperationException {
	throw new UnsupportedOperationException();
    }

    @Override
    public boolean isAtom() {
	return true;
    }

    @Override
    public boolean isVariable() {
	return false;
    }

    @Override
    public CompoundTerm asCompoundTerm() throws UnsupportedOperationException {
	throw new UnsupportedOperationException();
    }

    @Override
    public boolean isCompoundTerm() {
	return false;
    }
}
