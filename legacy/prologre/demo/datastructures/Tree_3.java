package prologre.demo.datastructures;

import static prologre.Atom.Atom;
import prologre.Atom;
import prologre.CompoundGroundTerm;
import prologre.CompoundTerm;
import prologre.GoalIterator;
import prologre.State;
import prologre.Term;
import prologre.Variable;

// This class will be "generated" from either a prolog file or some meta-information...
// meta-information tree(Atom,>tree/3,>tree/3)
@SuppressWarnings("all")
public class Tree_3 {

    private abstract static class _Tree_3 implements CompoundTerm {

	_Tree_3() {
	}

	abstract Term first();

	abstract Term second();

	abstract Term third();

	@Override
	public final boolean isCompoundTerm() {
	    return true;
	}

	@Override
	public CompoundTerm asCompoundTerm() throws UnsupportedOperationException {
	    return this;
	}

	@Override
	public final boolean isAtom() {
	    return false;
	}

	@Override
	public final Atom asAtom() throws UnsupportedOperationException {
	    throw new UnsupportedOperationException();
	}

	@Override
	public final boolean isVariable() {
	    return false;
	}

	@Override
	public final Variable asVariable() throws UnsupportedOperationException {
	    throw new UnsupportedOperationException();
	}

	@Override
	public final String functor() {
	    return "tree/3";
	}

	@Override
	public String toString() {
	    return "tree(" + first() + "," + second() + "," + third() + ")";
	}

    }

    private static class Tree_3Fact extends _Tree_3 implements CompoundGroundTerm {

	final Atom element;

	final Tree_3Fact left;

	final Tree_3Fact right;

	public Tree_3Fact(Atom element, Tree_3Fact left, Tree_3Fact right) {
	    super();
	    this.element = element;
	    this.left = left;
	    this.right = right;
	}

	@Override
	public boolean unify(Term term) {
	    // we need to support matching of (Compound)GroundTerms...
	    throw new UnsupportedOperationException();
	}

	@Override
	public State manifestState() {
	    return null;
	}

	@Override
	public void setState(State state) {
	    // nothing to do...
	}

	@Override
	public Term first() {
	    return this.element;
	}

	@Override
	public Term second() {
	    return this.left;
	}

	@Override
	public Term third() {
	    return this.right;
	}

    }

    public final static class Tree_3Term extends _Tree_3 {

	private static class Tree_3TermState implements State {
	    final State elementState;

	    final State leftState;
	    final State rightState;

	    public Tree_3TermState(Tree_3Term t) {
		super();
		this.elementState = t.element.manifestState();
		this.leftState = t.left == null ? null : t.left.manifestState();
		this.rightState = t.right == null ? null : t.right.manifestState();
	    }

	    public void resetState(Tree_3Term t) {
		t.element.setState(this.elementState);
		if (t.left != null)
		    t.left.setState(this.leftState);
		if (t.right != null)
		    t.right.setState(this.rightState);
	    }

	}

	final Term element;

	final Term left;

	final Term right;

	/**
	 * @param element
	 *            either a Variable or an atom.
	 * @param left
	 *            either a Variable or a Tree_3Term (including null).
	 * @param right
	 *            either a Variable or a Tree_3Term (including null).
	 */
	public Tree_3Term(Term element, Term left, Term right) {
	    super();
	    this.element = element;
	    this.left = left;
	    this.right = right;
	}

	@Override
	public State manifestState() {
	    return new Tree_3TermState(this);
	}

	@Override
	public void setState(State state) {
	    ((Tree_3TermState) state).resetState(this);
	}

	public boolean unify(Tree_3Fact fact) {
	    return element.unify(fact.element) && unify(left, fact.left)
		    && unify(right, fact.right);
	}

	private static boolean unify(Term t, Tree_3Fact f) {
	    if (t == null) {
		if (f == null)
		    return true;
		else
		    return false;
	    } else { // t != null
		if (t.isVariable()) {
		    return t.unify(f);
		} else {
		    if (f == null || t.isAtom()) {
			return false;
		    } else { // t is not null and f is not null

			return t.asCompoundTerm().functor() == f.functor()
				&& ((Tree_3Term) t).unify(f);
		    }
		}
	    }
	}

	@Override
	public boolean unify(Term term) {
	    return term.isCompoundTerm() && term.asCompoundTerm().functor() == this.functor()
		    && unify((Tree_3Term) term);
	}

	public boolean unify(Tree_3Term term) {
	    throw new UnknownError("Needs to be implemented...");
	}

	@Override
	public Term first() {
	    return element;
	}

	@Override
	public Term second() {
	    return left;
	}

	@Override
	public Term third() {
	    return right;
	}

    }

    private static Tree_3 instance = new Tree_3();

    public static Tree_3 getInstance() {
	return instance;
    }

    private static final Tree_3Fact[] data = new Tree_3Fact[] {
	    new Tree_3Fact(Atom("a1"), new Tree_3Fact(Atom("b1"), null, null), new Tree_3Fact(
		    Atom("c1"), null, null)),
	    new Tree_3Fact(Atom("x"), new Tree_3Fact(Atom("y"), new Tree_3Fact(Atom("z"), null,
		    null), null), new Tree_3Fact(Atom("z"), null, null)),
	    new Tree_3Fact(Atom("x"), new Tree_3Fact(Atom("y"), new Tree_3Fact(Atom("z"), null,
		    null), null), new Tree_3Fact(Atom("y"), null, null)),
	    new Tree_3Fact(Atom("a2"), new Tree_3Fact(Atom("b2"), null, null), new Tree_3Fact(
		    Atom("c2"), new Tree_3Fact(Atom("d2"), new Tree_3Fact(Atom("e2"), null, null),
			    new Tree_3Fact(Atom("f2"), null, null)), null)) };

    public GoalIterator unify(final Tree_3Term p) {

	// TODO Do we need to check for p == null?

	return new GoalIterator() {

	    private final State pState = p.manifestState();

	    private int index = 0;

	    // The binding is implicitly stored in the variables (if any)
	    public boolean next() {

		p.setState(this.pState);

		while (this.index < data.length) {
		    int currentIndex = this.index++;
		    Tree_3Fact t = data[currentIndex];
		    if (p.unify(t)) {
			return true;
		    }
		}
		return false;
	    }
	};
    }
}
