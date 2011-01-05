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
package saere.predicate;

import static saere.StringAtom.EMPTY_LIST;
import static saere.term.Terms.variable;
import saere.CompoundTerm;
import saere.Goal;
import saere.PredicateIdentifier;
import saere.PredicateRegistry;
import saere.State;
import saere.StringAtom;
import saere.Term;
import saere.PredicateFactoryThreeArgs;
import saere.Variable;
import saere.term.ListElement2;

/**
 * Implementation of ISO Prolog's append/3 predicate.
 * 
 * <pre>
 * <code>
 * append([],Ys,Ys).
 * append([X|Xs],Ys,[X|Zs]) :- append(Xs,Ys,Zs).
 * </code>
 * </pre>
 * 
 * @author Michael Eichberg (mail@michael-eichberg.de)
 */
public final class Append3 implements Goal {

	public final static PredicateIdentifier IDENTIFIER = new PredicateIdentifier(
			StringAtom.get("append"), 3);

	public final static PredicateFactoryThreeArgs FACTORY = new PredicateFactoryThreeArgs() {

		@Override
		public Goal createInstance(Term Xs, Term Ys, Term Zs) {
			return new Append3(Xs, Ys, Zs);
		}

	};

	public static void registerWithPredicateRegistry(PredicateRegistry registry) {
		registry.register(IDENTIFIER, FACTORY);
	}

	// variables to control/manage the execution this predicate
	private int goalToExecute = 1;
	// variables related to the predicate's state
	private Term Xs;
	private Term Ys;
	private Term Zs;
	private State XsState;
	private State YsState;
	private State ZsState;
	final private State rootXsState; // REQUIRED BY TAIL-CALL OPTIMIZATION ...
	final private State rootYsState;
	final private State rootZsState;
	// variables to store clause local information
	private Term clv0;
	private Term clv1;
	private Term clv2;

	public Append3(final Term Xs, final Term Ys, final Term Zs) {
		// the implementation depends on the property of Xs...Zs being
		// "unwrapped"
		this.Xs = Xs.unwrap();
		this.Ys = Ys.unwrap();
		this.Zs = Zs.unwrap();

		// REQUIRED BY TAIL-CALL OPTIMIZATION ...
		this.rootXsState = Xs.manifestState();
		this.rootYsState = Ys.manifestState();
		this.rootZsState = Zs.manifestState();
	}

	public void abort() {
		reset();
	}

	private void reset() {
		if (rootXsState != null)
			rootXsState.reincarnate();
		if (rootZsState != null)
			rootZsState.reincarnate();
		if (rootYsState != null)
			rootYsState.reincarnate();
	}

	public boolean choiceCommitted() {
		return false;
	}

	public boolean next() {
		do {
			if (this.clause1()) {
				return true;
			}

			this.goalToExecute = 1;
			if (!this.clause2()) {
				reset();
				return false;
			}
			// continue;
		} while (true);
	}

	private boolean clause1() {
		if (goalToExecute == 1) {
			XsState = Xs.manifestState();
			if (!Xs.unify(EMPTY_LIST)) {
				if (XsState != null)
					XsState.reincarnate();
				return false;
			}

			ZsState = Zs.manifestState();
			YsState = Ys.manifestState();
			if (Zs.unify(Ys)) {
				goalToExecute = 2;
				return true;
			}
		}

		if (XsState != null)
			XsState.reincarnate();
		if (ZsState != null)
			ZsState.reincarnate();
		if (YsState != null)
			YsState.reincarnate();
		return false;
	}

	// append([X|Xs],Ys,[X|Zs]) :- append(Xs,Ys,Zs).
	private boolean clause2() {
		if (goalToExecute == 1) {
			// Xs's state is already manifested... (as part of the first goal of the first clause)
			if (Xs.isVariable()) {
				// Xs has to be free..., because Xs was explicitly
				// "unwrapped"
				this.clv0 = new Variable();
				this.clv1 = new Variable();
				Xs.asVariable().setValue(new ListElement2(clv0, clv1));
			} else if (!(Xs.arity() == 2 && Xs.functor().sameAs(StringAtom.LIST))) {
				return false;
			} else {
				CompoundTerm ctXs = Xs.asCompoundTerm();
				this.clv0 = ctXs.firstArg();
				this.clv1 = ctXs.secondArg();
			}

			// this.ZsState = Zs.manifestState(); (not required...)
			this.clv2 = variable();
			if (Zs.unify(new ListElement2(clv0, clv2))) {
				this.Xs = clv1.unwrap();
				// this.Ys = Ys.unwrapped();
				this.Zs = clv2.unwrap();
				goalToExecute = 1;
				return true;
			}
		}

		// The following is not required, because this claus is tail recursive (and subject to last
		// call optimization):
		// if (XsState != null)
		// XsState.reincarnate();
		// // if (YsState != null)
		// // YsState.reincarnate();
		// if (ZsState != null)
		// ZsState.reincarnate();
		return false;
	}
}
