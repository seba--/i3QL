/*
	Implements a very simple analysis to determine if a predicate is 
	guaranteed to be deterministic. 
	
	@author Michael Eichberg
*/
:- module(
	'Compiler:Phase:Analysis:Determinacy',
	[
		pl_determinacy_analysis/4,
		calls_cut/2
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
	one clause must succeed (currently we call this property: "clause_selection")
	</p>
	<p>
	A clause is deterministic iff every goal of the clause that appears after a
 	cut (if any) is deterministic.
	</p>
	If the clause is tail recursive, i.e., the
	last goal of the clause is a call of the clause's predicate, then 
	this goal (the recursive call) is deterministic if always at most one clause 
	succeeds. This is trivially the case if all clauses of a predicate
	contain a cut.
	<p>
	To determine the determinancy of predicates we do a bottom-up analysis of the
	call graph.
	</p>
	
	@param Program is the AST of a normalized program {@link pl_normalize/4}. 
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
		analyze_clause_selection(NClauses,PredicateProperties,NPredicateProperties),		
		NPredicate = pred(ID,NClauses,NPredicateProperties)
	),
	NProgram = [NPredicate|OtherNPredicates],
	process_predicates(Predicates,OtherNPredicates).
	



analyze_cut_behavior([],[]) :- !.
analyze_cut_behavior([(Clause,Properties)|Clauses],NClauses) :-
	analyze_cut_behavior(Clause,Properties,NProperties),
	NClauses=[(Clause,NProperties)|OtherNClauses],
	analyze_cut_behavior(Clauses,OtherNClauses).


/**
	Analyze the behavior w.r.t. to clause selection...
**/



/*	analyze_cut_behavior(Clause,Properties,NProperties) :- analyzes a clause w.r.t.
	its use of the cut operator. When this analysis has finished, the clause's set of 
	properties (NProperties) contains an entry that specifies if a cut operation is
   "always", "never" or "sometimes" executed when the clause succeeds.
	
	<p> This property helps to decide whether the choice of a predicate's clause
	is deterministic; i.e., if always at most one clause succeeds. 
	</p>
*/
analyze_cut_behavior(':-'(_H,B),Properties,NProperties) :-
	calls_cut(B,R), % R is either "always","sometimes" (can happen if ";" is used), or "never"
	NProperties = [cut(R)|Properties].

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


/* calls_cut(G,C) :- analyzes if the goal G uses the cut operator ('!') on all
	execution paths (C=always), on some execution paths (C=sometimes) or
	never (C=never).
*/
calls_cut(G,B) :-
	G = '!' -> B = always ;
	(G = (L,R) -> (
		calls_cut(L,LCB),
		calls_cut(R,RCB),
		(
			(LCB = always ; RCB = always),!, B = always
		;
			(LCB = sometimes ; RCB = sometimes),!, B = sometimes 
		;
			B = never
		)	
	);
	(G = (L;R) -> (
		calls_cut(L,LCB),
		calls_cut(R,RCB),
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



analyze_clause_selection([_Clause],PredicateProperties,NPredicateProperties) :- 
	!, % green cut
	NPredicateProperties = [clause_selection(deterministic)|PredicateProperties].
analyze_clause_selection([(_Clause,Properties)|Clauses],PredicateProperties,NPredicateProperties) :-
	Clauses \= [], %... not strictly required
	(	Properties=[cut(always)|_] ->
		analyze_clause_selection(Clauses,PredicateProperties,NPredicateProperties)
	;
		NPredicateProperties = PredicateProperties
	).
	
	

	







