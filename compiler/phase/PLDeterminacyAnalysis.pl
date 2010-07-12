/*
	Implements a very simple analysis to determine if a predicate is 
	guaranteed to be deterministic. 
	
	@version $Date$ $Rev$
	@author Michael Eichberg
*/
:- module(
	'Compiler:Phase:Analysis:Determinacy',
	[
		pl_determinacy_analysis/4,
		cuts/2
	]).

:- use_module('../Predef.pl').
:- use_module('../Utils.pl').
:- use_module('../Debug.pl').




/* Implements a very simple analysis to determine if a predicate is 
	guaranteed to be deterministic. A predicate is deterministic if
	it succeeds at most once.  
	<p>
	A predicate's overall determinancy property is determined by its clauses.
	A necessary precondition for a predicate to be deterministic is that 
	all its defining clauses are deterministic. Additionally, always only at most 
	one clause must succeed (i.e., the choice must be deterministic.)
	</p>
	<p>
	A clause is deterministic iff every goal of the clause that appears after a
 	cut (if any) is deterministic.
	</p>
	If the clause is tail recursive, i.e., the
	last goal of the clause is a call of the clause's predicate, then 
	this goal (the recursive call) is deterministic if always at most one clause 
	succeeds. This is trivially the case if the clauses 1..(N-1) of a predicate
	consisting of N clauses contain a cut.
	
	To determine the determinancy of predicates we do a bottom-up analysis of the
	call graph.
	
	@param Program is the program's AST of a normalized program. 
		<pre>
		[	pred(
				a/1, 							% The predicate (Functor/Arity)
				[(C, [det, type=int])],	% A list of clauses defining the 
												% predicate. C is one of these clauses.
												% In case of built-in propeties, this list
												% is empty.<br/>
												% C is always of the form:
												% <code> term(A1,...AN) :- Body. </code>
												% where each Ai is a unique variable. 
				[type=...]					% List of properties of the predicate
			),...
		]
		</pre>	
*/
pl_determinacy_analysis(Debug,Program,_OutputFolder,NProgram) :-
	debug_message(
		Debug,on_entry,
		'\nPhase: Determinacy Analysis_________________________________________'),

	process_predicates(Program,NProgram),

	debug_message(Debug,ast,NProgram).




process_predicates([],[]) :- !.
process_predicates([Predicate|Predicates],NProgram) :- 
	Predicate = pred(ID,Clauses,PredicateProperties),
	( 	Clauses = [] -> % built-in predicate
		NPredicate = Predicate
	;
		analyze_cut_behavior(Clauses,NClauses),
		NPredicate = pred(ID,NClauses,PredicateProperties)
	),
	NProgram = [NPredicate|OtherNPredicates],
	process_predicates(Predicates,OtherNPredicates).
	



analyze_cut_behavior([],[]) :- !.
analyze_cut_behavior([(Clause,Properties)|Clauses],NClauses) :-
	analyze_cut_behavior(Clause,Properties,NProperties),
	NClauses=[(Clause,NProperties)|OtherNClauses],
	analyze_cut_behavior(Clauses,OtherNClauses).




/*	analyze_cut_behavior(Clause,Properties,NProperties) :- analyzes a clause w.r.t.
	its use of the cut operator. When this analysis has finished, the clause's set of 
	properties (NProperties) contains an entry that specifies if a cut operation is
   "always", "never" or "sometimes" executed when the clause succeeds.
	
	<p> This property helps to decide whether the choice of a predicate's clause
	is deterministic; i.e., if always at most one clause succeeds. 
	</p>
*/
analyze_cut_behavior(':-'(_H,B),Properties,NProperties) :-
	cuts(B,R), % R is either "always","sometimes" (can happen if ";" is used), or "never"
	NProperties = [(cut=R)|Properties].

/* <b>Semantics of "->"</b>
	<pre>
	If -&gt; Then; _Else :- If, !, Then.                                 
	If -&gt; _Then; Else :- !, Else.                                      
	
	If_-&gt;_Then_:-_If,_!,_Then.
	</pre>

	?- (write(s1);write(s2)),A=true,B=true,C=true,((A;B;C) -> write('R') ; write('No R')).
	s1R
	A = true,B = true,C = true ;
	s2R
	A = true,B = true,C = true.

	Operator priority
	?- (A -> B ; C) = ((LL -> LR); R).
	A = LL,
	B = LR,
	C = R.


	<p>
	<b>Semantics of "*->"</b>
	?- (write(s1);write(s2)),A=true,B=true,C=true,((A;B;C) *-> write('R') ; write('No R')).
	s1R
	A = true,B = true,C = true ;
	R
	A = true,B = true,C = true ;
	R
	A = true,B = true,C = true ;
	s2R
	A = true,B = true,C = true ;
	R
	A = true,B = true,C = true ;
	R
	A = true,B = true,C = true ;
	
	Operator priority

	?- (A *-> B ; C) = ((LL *-> LR); R).
	A = LL,
	B = LR,
	C = R.	
	</p>
*/
/* cuts(T,B) :- analyzes the goal term T w.r.t. its cut behavior B. B is 
	either "always", "sometimes" or "never".
*/
cuts(T,B) :-
	T = '!' -> B = always ;
	(T = (L,R) -> (
		cuts(L,LCB),
		cuts(R,RCB),
		(
			(LCB = always ; RCB = always),!, B = always
		;
			(LCB = sometimes ; RCB = sometimes),!, B = sometimes 
		;
			B = never
		)	
	);
	(T = (L;R) -> (
		cuts(L,LCB),
		cuts(R,RCB),
		(
			LCB = always, 
			RCB = always,
			!,
			B = always
		;
			LCB = never, 
			RCB = never, 
			!, 
			B = never
		;
			B = sometimes
		)
	);
	B = never
	)).


	







