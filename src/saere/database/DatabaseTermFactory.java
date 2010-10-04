package saere.database;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import saere.Atom;
import saere.CompoundTerm;
import saere.IntegerAtom;
import saere.StringAtom;
import saere.Term;
import saere.Variable;
import saere.database.index.TermStack;
import saere.meta.GenericCompoundTerm;
import saere.term.ListElement2;
import scala.Function1;
import scala.collection.Seq;
import scala.collection.mutable.ArraySeq;
import de.tud.cs.st.bat.PrologTermFactory;

/**
 * The Database Term Factory.
 * 
 * @author David Sullivan
 * @version 0.1, 9/22/2010
 */
public class DatabaseTermFactory extends PrologTermFactory<CompoundTerm, Term, Atom> {
	
	// Singleton, because there should be only one factory that assigns unique IDs.
	private static final DatabaseTermFactory INSTANCE = new DatabaseTermFactory();
	
	private final AtomicInteger counter; // well...
	private final StringAtom noneAtom;
	private final StringAtom yesAtom;
	private final StringAtom noAtom;

	private DatabaseTermFactory() {
		counter = new AtomicInteger(0);
		noneAtom = makeStringAtom("none");
		yesAtom = makeStringAtom("yes");
		noAtom = makeStringAtom("no");
	}
	
	/**
	 * Gets the {@link DatabaseTermFactory} singleton.
	 * 
	 * @return The {@link DatabaseTermFactory} singleton.
	 */
	public static DatabaseTermFactory getInstance() {
		return INSTANCE;
	}

	/**
	 * Makes an integer atom.
	 * 
	 * @param value The value for the integer atom.
	 * @return An integer atom.
	 */
	public static IntegerAtom makeIntegerAtom(int value) {
		return IntegerAtom.IntegerAtom(value);
	}

	/**
	 * Makes a string atom.
	 * 
	 * @param value The value for the string atom.
	 * @return A string atom.
	 */
	public static StringAtom makeStringAtom(String value) {
		return StringAtom.StringAtom(value);
	}

	/**
	 * Makes a compound term.
	 * 
	 * @param functor The functor for the compound term.
	 * @param args The array of arguments for the compound term.
	 * @return A compound term.
	 */
	public static CompoundTerm makeCompoundTerm(String functor, Term[] args) {
		return new GenericCompoundTerm(makeStringAtom(functor), args);
	}

	public CompoundTerm Fact(String functor, Seq<Term> args) {
		return makeCompoundTerm(functor, array(args));
	}

	public Atom FloatAtom(double value) {
		return makeIntegerAtom((int) value); // FIXME Real float atoms required
	}

	public Atom IntegerAtom(long value) {
		// XXX Is cast to integer okay?
		return makeIntegerAtom((int) value);
	}

	public Atom StringAtom(String value) {
		return makeStringAtom(value);
	}

	public Term Term(String functor, Seq<Term> args) {
		return makeCompoundTerm(functor, array(args));
	}

	public Atom TextAtom(String value) {
		return makeStringAtom(value);
	}

	@Override
	public Atom KeyAtom(String value) {
		return makeStringAtom(value + counter.getAndIncrement());
	}

	@Override
	public Atom NoneAtom() {
		return noneAtom;
	}
	
	@Override
	public Atom YesAtom() {
		return yesAtom;
	}
	
	@Override
	public Atom NoAtom() {
		return noAtom;
	}
	
	public <T> Term Terms(Seq<T> ts, Function1<T, Term> func1) {
		Term[] terms = new Term[ts.size()];
		for (int i = 0; i < ts.size(); i++) {
			terms[i] = func1.apply(ts.apply(i));
		}
		return list(new TermStack(terms));
	}

	/*
	 * FIXME Use for TermTrie?
	 * (non-Javadoc)
	 * @see de.tud.cs.st.prolog.PrologTermFactory#Univ(java.lang.Object)
	 */
	public Seq<Term> Univ(Term term) {
		if (term.isVariable() && ((Variable) term).isInstantiated()) {
			// term is a variable and bound
			return Univ(((Variable) term).binding());
		} else {
			List<Term> terms = new LinkedList<Term>();
			
			// add functor
			terms.add(term.functor());
			
			// add args if compound term
			if (term.isCompoundTerm()) {
				CompoundTerm ct = (CompoundTerm) term;
				for (int i = 0; i < ct.arity(); i++) {
					terms.add(ct.arg(i)); // do not add arguments recursively if they are compound terms?
				}
			}
			
			return seq(terms.toArray(new Term[0]));
		}
	}

	// XXX Workaround for ClassManifest
	private Term[] array(Seq<Term> seq) {
		Term[] array = new Term[seq.size()];
		for (int i = 0; i < seq.size(); i++) {
			array[i] = seq.apply(i);
		}
		return array;
	}
	
	// XXX Workaround...
	private Seq<Term> seq(Term[] array) {
		Seq<Term> seq = new ArraySeq<Term>(array.length);
		for (int i = 0; i < array.length; i++) {
			((ArraySeq<Term>) seq).update(i, array[i]);
		}
		return seq;
	}
	
	private Term list(TermStack ts) {
		if (ts.size() > 1) {
			return new ListElement2(ts.pop(), list(ts));
		} else if (ts.size() > 0) {
			return ts.pop();
		} else {
			return makeCompoundTerm(".", new Term[] {}); // XXX EmptyList0.apply();
		}
	}
	
	public void resetIdCounter() {
		counter.set(0);
	}
}
