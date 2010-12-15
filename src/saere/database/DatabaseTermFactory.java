package saere.database;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import saere.Atom;
import saere.CompoundTerm;
import saere.IntegerAtom;
import saere.StringAtom;
import saere.Term;
import saere.Variable;
import saere.GenericCompoundTerm;
import saere.term.ListElement2;
import scala.Function1;
import scala.collection.Seq;
import scala.collection.mutable.ArraySeq;
import de.tud.cs.st.bat.PrologTermFactory;

/**
 * The Database Term Factory.
 * 
 * @author David Sullivan
 * @version 0.3, 11/11/2010
 */
public class DatabaseTermFactory extends PrologTermFactory<CompoundTerm, Term, Atom> {
	
	// Singleton, because there should be only one factory that assigns unique IDs.
	private static final DatabaseTermFactory INSTANCE = new DatabaseTermFactory();
	
	private final HashMap<String, AtomicInteger> keyCounters;
	private final StringAtom noneAtom;
	private final StringAtom yesAtom;
	private final StringAtom noAtom;

	private DatabaseTermFactory() {
		keyCounters = new HashMap<String, AtomicInteger>();
		noneAtom = StringAtom.StringAtom("none");
		yesAtom = StringAtom.StringAtom("yes");
		noAtom = StringAtom.StringAtom("no");
	}
	
	/**
	 * Gets the {@link DatabaseTermFactory} singleton.
	 * 
	 * @return The {@link DatabaseTermFactory} singleton.
	 */
	public static DatabaseTermFactory getInstance() {
		return INSTANCE;
	}

	@Override
	public CompoundTerm Fact(String functor, Seq<Term> args) {
		return new GenericCompoundTerm(StringAtom.StringAtom(functor), array(args));
	}

	@Override
	public Atom FloatAtom(double value) {
		return IntegerAtom.IntegerAtom((int) value); // FIXME Real float atoms required
	}

	@Override
	public Atom IntegerAtom(long value) {
		// XXX Is cast to integer okay?
		return IntegerAtom.IntegerAtom((int) value);
	}

	@Override
	public Atom StringAtom(String value) {
		return StringAtom.StringAtom(value);
	}

	@Override
	public Term Term(String functor, Seq<Term> args) {
		return new GenericCompoundTerm(StringAtom.StringAtom(functor), array(args));
	}

	// XXX Add quotation marks?
	@Override
	public Atom TextAtom(String value) {
		return StringAtom.StringAtom(value);
	}

	@Override
	public Atom KeyAtom(String prefix) {
		AtomicInteger counter = keyCounters.get(prefix);
		if (counter == null) {
			counter = new AtomicInteger(1);
			keyCounters.put(prefix, counter);
		}
		
		return StringAtom.StringAtom(prefix + counter.getAndIncrement());
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
	
	@Override
	public <T> Term Terms(Seq<T> ts, Function1<T, Term> func1) {
		Deque<Term> terms = new ArrayDeque<Term>();
		for (int i = 0; i < ts.size(); i++) {
			terms.add(func1.apply(ts.apply(i)));
		}
		return list(terms);
	}
	
	@Override
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

	/**
	 * Turns the specified Scala {@link Seq} for {@link Term}s into a Java 
	 * array for {@link Term}s.
	 * 
	 * @param seq The Scala {@link Seq}.
	 * @return An appropriate Java array.
	 */
	// XXX Workaround for ClassManifest
	private Term[] array(Seq<Term> seq) {
		Term[] array = new Term[seq.size()];
		for (int i = 0; i < seq.size(); i++) {
			array[i] = seq.apply(i);
		}
		return array;
	}
	
	/**
	 * Turns the specified Java array for {@link Term}s into a Scala 
	 * {@link Seq} for {@link Term}s.
	 * 
	 * @param array The Java array.
	 * @return An appropriate Scala {@link Seq}.
	 */
	// XXX Workaround...
	private Seq<Term> seq(Term[] array) {
		Seq<Term> seq = new ArraySeq<Term>(array.length);
		for (int i = 0; i < array.length; i++) {
			((ArraySeq<Term>) seq).update(i, array[i]);
		}
		return seq;
	}
	
	/**
	 * Creates a Prolog list from the specified stack of terms.
	 * 
	 * @param ts The stack of terms.
	 * @return A Prolog list with the terms.
	 */
	private Term list(Deque<Term> ts) {		
		if (ts.size() > 1) {
			return new ListElement2(ts.pop(), list(ts));
		} else {
			return StringAtom.EMPTY_LIST_FUNCTOR;
		}
	}
	
	/**
	 * Resets the ID cache.
	 */
	public void reset() {
		keyCounters.clear();
	}
	
	// Convenience methods...
	
	/**
	 * Makes an integer atom.
	 * 
	 * @param value The value for the integer atom.
	 * @return An integer atom.
	 */
	public static IntegerAtom ia(int value) {
		return IntegerAtom.IntegerAtom(value);
	}

	/**
	 * Makes a string atom.
	 * 
	 * @param value The value for the string atom.
	 * @return A string atom.
	 */
	public static StringAtom sa(String value) {
		return StringAtom.StringAtom(value);
	}

	/**
	 * Makes a compound term.
	 * 
	 * @param functor The functor for the compound term.
	 * @param args The array of arguments for the compound term.
	 * @return A compound term.
	 */
	public static CompoundTerm ct(String functor, Term ... args) {
		return new GenericCompoundTerm(sa(functor), args);
	}
	
	public static Variable v() {
		return new Variable();
	}
}
