/* License (BSD Style License):
 * Copyright (c) 2010
 * Department of Computer Science
 * Technische Universität Darmstadt
 * All rights reserved.
 *
 *  Redistribution and use in source and binary forms, with or without
 *  modification, are permitted provided that the following conditions are met:
 *
 *  - Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 *  - Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *  - Neither the name of the Software Technology Group or Technische 
 *    Universität Darmstadt nor the names of its contributors may be used to 
 *    endorse or promote products derived from this software without specific 
 *    prior written permission.
 *
 *  THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 *  AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 *  IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 *  ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
 *  LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 *  CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 *  SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 *  INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 *  CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 *  ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 *  POSSIBILITY OF SUCH DAMAGE.
 */
package saere.term;

import static saere.PredicateRegistry.predicateRegistry;
import saere.CompoundTerm;
import saere.FloatValue;
import saere.Goal;
import saere.IntValue;
import saere.PredicateFactory;
import saere.PredicateIdentifier;
import saere.StringAtom;
import saere.Term;
import saere.Variable;

/**
 * Central class to create terms.
 * 
 * @author Michael Eichberg
 */
public class Terms {

	private Terms() {
		// nothing to do...
	}

	public static StringAtom atomic(String value) {
		return StringAtom.get(value);
	}

	public static IntValue atomic(long value) {
		return IntValue.get(value);
	}

	public static FloatValue atomic(double value) {
		return FloatValue.get(value);
	}

	public static Variable variable() {
		return new Variable();
	}

	public static CompoundTerm compoundTerm(StringAtom functor, Term t) {
		switch (functor.rawValue()[0]) {
		case '-':
			return new Minus1(t);
		default:
			return new CompoundTermWithOneArg(functor, t);
		}
	}

	public static CompoundTerm compoundTerm(StringAtom functor, Term t1, Term t2) {
		switch (functor.rawValue()[0]) {
		// control-flow related terms
		case ',':
			return new And2(t1, t2);
		case ';':
			return new Or2(t1, t2);

			// arithmetic terms
		case '-':
			return new Minus2(t1, t2);
		case '+':
			return new Plus2(t1, t2);

			// other commonly used terms
		case '.':
			return new ListElement2(t1, t2);

		default:
			return new CompoundTermWithTwoArgs(functor, t1, t2);
		}
	}

	public static CompoundTerm compoundTerm(StringAtom functor, Term t0, Term t1, Term t2) {
		return new CompoundTermWithThreeArgs(functor, t0, t1, t2);
	}

	public static CompoundTerm compoundTerm(StringAtom functor, Term t0, Term t1, Term t2, Term t3) {
		return new CompoundTermWithFourArgs(functor, t0, t1, t2, t3);
	}

	public static CompoundTerm compoundTerm(StringAtom functor, Term t0, Term t1, Term t2, Term t3,
			Term t4) {
		return new CompoundTermWithFiveArgs(functor, t0, t1, t2, t3, t4);
	}

	public static CompoundTerm compoundTerm(StringAtom functor, Term t0, Term t1, Term t2, Term t3,
			Term t4, Term t5) {
		return new CompoundTermWithSixArgs(functor, t0, t1, t2, t3, t4, t5);
	}

	public static CompoundTerm compoundTerm(StringAtom functor, Term t0, Term t1, Term t2, Term t3,
			Term t4, Term t5, Term t6) {
		return new CompoundTermWithSevenArgs(functor, t0, t1, t2, t3, t4, t5, t6);
	}

	public static CompoundTerm compoundTerm(StringAtom functor, Term t0, Term t1, Term t2, Term t3,
			Term t4, Term t5, Term t6, Term t7) {
		return new CompoundTermWithEightArgs(functor, t0, t1, t2, t3, t4, t5, t6, t7);
	}

	public static CompoundTerm compoundTerm(StringAtom functor, Term... args) {
		if (args.length < 9)
			throw new UnsupportedOperationException("the number of args must be larger");

		System.out.println("GenericCompoundTerm created - #args: " + args.length);

		return new GenericCompoundTerm(functor, args);
	}

	// Special methods for the most common Prolog terms

	public static And2 and(Term t1, Term t2) {
		return new And2(t1, t2);
	}

	public static Or2 or(Term t1, Term t2) {
		return new Or2(t1, t2);
	}

	public static CompoundTerm unify(Term t1, Term t2) {
		return new Equals2(t1, t2);
	}

	public static ListElement2 list(Term head, Term tail) {
		return new ListElement2(head, tail);
	}

	public static Term delimitedList(Term... terms) {
		Term rest = StringAtom.EMPTY_LIST;

		for (int i = terms.length - 1; i >= 0; i--) {
			rest = new ListElement2(terms[i], rest);
		}

		return rest;
	}

	//
	//
	// C O M P O U N D T E R M S   S P E C I A L I Z A T I O N S
	//
	// Primary Reason
	// The instantiation time is significantly reduced when compared to having a single generic
	// class which uses an array.
	//
	// Other Advantages:
	// Minimally reduced memory overhead (not significant).
	//

	private static final class CompoundTermWithOneArg extends CompoundTerm {

		private final StringAtom functor;
		private final Term arg;

		CompoundTermWithOneArg(StringAtom functor, Term arg) {
			this.functor = functor;
			this.arg = arg;
		}

		@Override
		public Term arg(int i) throws IndexOutOfBoundsException {
			return arg;
		}

		@Override
		public Term firstArg() {
			return arg;
		}

		@Override
		public Term secondArg() throws IndexOutOfBoundsException {
			throw new IndexOutOfBoundsException();
		}

		@Override
		public int arity() {
			return 1;
		}

		@Override
		public StringAtom functor() {
			return functor;
		}

		@Override
		public Goal call() {
			PredicateIdentifier pi = new PredicateIdentifier(this.functor, 1);
			PredicateFactory pf = predicateRegistry().getPredicateFactory(pi);
			return pf.createInstance(new Term[] { arg });
		}

	}

	private static final class CompoundTermWithTwoArgs extends CompoundTerm {

		private final StringAtom functor;
		private final Term t0;
		private final Term t1;

		CompoundTermWithTwoArgs(StringAtom functor, Term t0, Term t1) {
			this.functor = functor;
			this.t0 = t0;
			this.t1 = t1;
		}

		@Override
		public Term arg(int i) throws IndexOutOfBoundsException {
			return i == 0 ? t0 : t1;
		}

		@Override
		public Term firstArg() {
			return t0;
		}

		@Override
		public Term secondArg() throws IndexOutOfBoundsException {
			return t1;
		}

		@Override
		public int arity() {
			return 2;
		}

		@Override
		public StringAtom functor() {
			return functor;
		}

		@Override
		public Goal call() {
			PredicateIdentifier pi = new PredicateIdentifier(this.functor, 2);
			PredicateFactory pf = predicateRegistry().getPredicateFactory(pi);
			return pf.createInstance(new Term[] { t0, t1 });
		}

	}

	private static final class CompoundTermWithThreeArgs extends CompoundTerm {

		private final StringAtom functor;
		private final Term t0;
		private final Term t1;
		private final Term t2;

		CompoundTermWithThreeArgs(StringAtom functor, Term t0, Term t1, Term t2) {
			this.functor = functor;
			this.t0 = t0;
			this.t1 = t1;
			this.t2 = t2;
		}

		@Override
		public Term arg(int i) throws IndexOutOfBoundsException {
			switch (i) {
			case 0:
				return t0;
			case 1:
				return t1;
			case 2:
				return t2;
			default:
				throw new IndexOutOfBoundsException();
			}
		}

		@Override
		public Term firstArg() {
			return t0;
		}

		@Override
		public Term secondArg() throws IndexOutOfBoundsException {
			return t1;
		}

		@Override
		public int arity() {
			return 3;
		}

		@Override
		public StringAtom functor() {
			return functor;
		}

		@Override
		public Goal call() {
			PredicateIdentifier pi = new PredicateIdentifier(this.functor, 3);
			PredicateFactory pf = predicateRegistry().getPredicateFactory(pi);
			return pf.createInstance(new Term[] { t0, t1, t2 });
		}
	}

	private static final class CompoundTermWithFourArgs extends CompoundTerm {

		private final StringAtom functor;
		private final Term t0;
		private final Term t1;
		private final Term t2;
		private final Term t3;

		CompoundTermWithFourArgs(StringAtom functor, Term t0, Term t1, Term t2, Term t3) {
			this.functor = functor;
			this.t0 = t0;
			this.t1 = t1;
			this.t2 = t2;
			this.t3 = t3;
		}

		@Override
		public Term arg(int i) throws IndexOutOfBoundsException {
			switch (i) {
			case 0:
				return t0;
			case 1:
				return t1;
			case 2:
				return t2;
			case 3:
				return t3;
			default:
				throw new IndexOutOfBoundsException();
			}
		}

		@Override
		public Term firstArg() {
			return t0;
		}

		@Override
		public Term secondArg() throws IndexOutOfBoundsException {
			return t1;
		}

		@Override
		public int arity() {
			return 4;
		}

		@Override
		public StringAtom functor() {
			return functor;
		}

		@Override
		public Goal call() {
			PredicateIdentifier pi = new PredicateIdentifier(this.functor, 4);
			PredicateFactory pf = predicateRegistry().getPredicateFactory(pi);
			return pf.createInstance(new Term[] { t0, t1, t2, t3 });
		}
	}

	private static final class CompoundTermWithFiveArgs extends CompoundTerm {

		private final StringAtom functor;
		private final Term t0;
		private final Term t1;
		private final Term t2;
		private final Term t3;
		private final Term t4;

		CompoundTermWithFiveArgs(StringAtom functor, Term t0, Term t1, Term t2, Term t3, Term t4) {
			this.functor = functor;
			this.t0 = t0;
			this.t1 = t1;
			this.t2 = t2;
			this.t3 = t3;
			this.t4 = t4;
		}

		@Override
		public Term arg(int i) throws IndexOutOfBoundsException {
			switch (i) {
			case 0:
				return t0;
			case 1:
				return t1;
			case 2:
				return t2;
			case 3:
				return t3;
			case 4:
				return t4;
			default:
				throw new IndexOutOfBoundsException();
			}
		}

		@Override
		public Term firstArg() {
			return t0;
		}

		@Override
		public Term secondArg() throws IndexOutOfBoundsException {
			return t1;
		}

		@Override
		public int arity() {
			return 5;
		}

		@Override
		public StringAtom functor() {
			return functor;
		}

		@Override
		public Goal call() {
			PredicateIdentifier pi = new PredicateIdentifier(this.functor, 5);
			PredicateFactory pf = predicateRegistry().getPredicateFactory(pi);
			return pf.createInstance(new Term[] { t0, t1, t2, t3, t4 });
		}
	}

	private static final class CompoundTermWithSixArgs extends CompoundTerm {

		private final StringAtom functor;
		private final Term t0;
		private final Term t1;
		private final Term t2;
		private final Term t3;
		private final Term t4;
		private final Term t5;

		CompoundTermWithSixArgs(StringAtom functor, Term t0, Term t1, Term t2, Term t3, Term t4,
				Term t5) {
			this.functor = functor;
			this.t0 = t0;
			this.t1 = t1;
			this.t2 = t2;
			this.t3 = t3;
			this.t4 = t4;
			this.t5 = t5;
		}

		@Override
		public Term arg(int i) throws IndexOutOfBoundsException {
			switch (i) {
			case 0:
				return t0;
			case 1:
				return t1;
			case 2:
				return t2;
			case 3:
				return t3;
			case 4:
				return t4;
			case 5:
				return t5;
			default:
				throw new IndexOutOfBoundsException();
			}
		}

		@Override
		public Term firstArg() {
			return t0;
		}

		@Override
		public Term secondArg() throws IndexOutOfBoundsException {
			return t1;
		}

		@Override
		public int arity() {
			return 6;
		}

		@Override
		public StringAtom functor() {
			return functor;
		}

		@Override
		public Goal call() {
			PredicateIdentifier pi = new PredicateIdentifier(this.functor, 6);
			PredicateFactory pf = predicateRegistry().getPredicateFactory(pi);
			return pf.createInstance(new Term[] { t0, t1, t2, t3, t4, t5 });
		}
	}

	private static final class CompoundTermWithSevenArgs extends CompoundTerm {

		private final StringAtom functor;
		private final Term t0;
		private final Term t1;
		private final Term t2;
		private final Term t3;
		private final Term t4;
		private final Term t5;
		private final Term t6;

		CompoundTermWithSevenArgs(StringAtom functor, Term t0, Term t1, Term t2, Term t3, Term t4,
				Term t5, Term t6) {
			this.functor = functor;
			this.t0 = t0;
			this.t1 = t1;
			this.t2 = t2;
			this.t3 = t3;
			this.t4 = t4;
			this.t5 = t5;
			this.t6 = t6;
		}

		@Override
		public Term arg(int i) throws IndexOutOfBoundsException {
			switch (i) {
			case 0:
				return t0;
			case 1:
				return t1;
			case 2:
				return t2;
			case 3:
				return t3;
			case 4:
				return t4;
			case 5:
				return t5;
			case 6:
				return t6;
			default:
				throw new IndexOutOfBoundsException();
			}
		}

		@Override
		public Term firstArg() {
			return t0;
		}

		@Override
		public Term secondArg() throws IndexOutOfBoundsException {
			return t1;
		}

		@Override
		public int arity() {
			return 7;
		}

		@Override
		public StringAtom functor() {
			return functor;
		}

		@Override
		public Goal call() {
			PredicateIdentifier pi = new PredicateIdentifier(this.functor, 7);
			PredicateFactory pf = predicateRegistry().getPredicateFactory(pi);
			return pf.createInstance(new Term[] { t0, t1, t2, t3, t4, t5, t6 });
		}
	}

	private static final class CompoundTermWithEightArgs extends CompoundTerm {

		private final StringAtom functor;
		private final Term t0;
		private final Term t1;
		private final Term t2;
		private final Term t3;
		private final Term t4;
		private final Term t5;
		private final Term t6;
		private final Term t7;

		CompoundTermWithEightArgs(StringAtom functor, Term t0, Term t1, Term t2, Term t3, Term t4,
				Term t5, Term t6, Term t7) {
			this.functor = functor;
			this.t0 = t0;
			this.t1 = t1;
			this.t2 = t2;
			this.t3 = t3;
			this.t4 = t4;
			this.t5 = t5;
			this.t6 = t6;
			this.t7 = t7;
		}

		@Override
		public Term arg(int i) throws IndexOutOfBoundsException {
			switch (i) {
			case 0:
				return t0;
			case 1:
				return t1;
			case 2:
				return t2;
			case 3:
				return t3;
			case 4:
				return t4;
			case 5:
				return t5;
			case 6:
				return t6;
			case 7:
				return t7;
			default:
				throw new IndexOutOfBoundsException();
			}
		}

		@Override
		public Term firstArg() {
			return t0;
		}

		@Override
		public Term secondArg() throws IndexOutOfBoundsException {
			return t1;
		}

		@Override
		public int arity() {
			return 8;
		}

		@Override
		public StringAtom functor() {
			return functor;
		}

		@Override
		public Goal call() {
			PredicateIdentifier pi = new PredicateIdentifier(this.functor, 8);
			PredicateFactory pf = predicateRegistry().getPredicateFactory(pi);
			return pf.createInstance(new Term[] { t0, t1, t2, t3, t4, t5, t6, t7 });
		}
	}
}
