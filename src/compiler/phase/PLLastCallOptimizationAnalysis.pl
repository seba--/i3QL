/*
	This analysis identifies clauses for which we can apply last call 
	optimizations. I.e., this analysis identifies predicates that are tail 
	recursive, and where we can statically decide that – when the tail recursive
	goal is reached – there a no more choice points. 
		
	@author Michael Eichberg
*/
:- module(
	'Compiler:Phase:Analysis:LastCallOptimizationAnalysis',
	[pl_last_call_optimization_analysis/4]
).

:- use_module('../Utils.pl').
:- use_module('../Debug.pl').




pl_last_call_optimization_analysis(Debug,Program,_OutputFolder,NProgram) :-
	debug_message(
		Debug,on_entry,
		'\nPhase: Last Call Optimization Analysis______________________________'),
		
	process_predicates(Program,Program,NProgram),

	debug_message(Debug,ast,NProgram).
	


process_predicates([],_Program,[]).
process_predicates([pred(P,Clauses,PredicateProperties)|Predicates],Program,NProgram) :- 
	( 	Clauses = [] ->
		NPredicate = pred(P,Clauses,PredicateProperties)
	;
		last_call_optimization_analysis(P,Clauses,NClauses,Program),
		NPredicate = pred(P,NClauses,PredicateProperties)
	),
	NProgram = [NPredicate|OtherNPredicates],
	process_predicates(Predicates,Program,OtherNPredicates).

	
	

last_call_optimization_analysis(_P,[],[],_Program).
last_call_optimization_analysis(P,[Clause|Clauses],NClauses,Program) :-
write(P),write(Clause),nl,write(Clauses),nl,nl,
	(	(
			Clause = (':-'(Head,Body),Properties),
			last_goal(Body,goal(LastGoal)),
			is_predicate_call(P,LastGoal), 		% this is a tail recursive call
			Body=(MainBody,_TailRecursiveCall),	
			is_deterministic(MainBody,Program),
			( % test if the clause choice is fixed
				Clauses = [] % <=> the current clause is the last clause
			;	
write(memberchk),write(Properties),nl,			
				memberchk(cut(always),Properties) % <=> a cut is used
			)
		) -> 
			NClauses=[(':-'(Head,Body),[tail_recursion_optimisation(possible)|Properties])|RNClauses]
		;
			NClauses=[Clause|RNClauses]
	),
	last_call_optimization_analysis(P,Clauses,RNClauses,Program).
	
	
	
	
	
	
	
	
/* last_goal(Term,Goal) :- Goal is the last sub goal that will be called when 
	Term is called. If no unique last goal exists; e.g., if "or" (";")
	is used, Goal is bound to the atom 'none' otherwise it is a compound term 
	<code>goal(G)</code> where G is bound to the last Goal of Term.
*/
last_goal(Term,G) :-
 	var(Term)		-> G = goal(call(Term));
	Term = (L,R)	-> last_goal(R,G);
	Term = (L;R)	-> G = none;
	/*else*/				G = goal(Term). 



	
/* is_predicate_call(P,Goal) :- succeeds if Goal is a call of the predicate P 
	(P = Functor/Arity).	
*/
is_predicate_call(Functor/Arity,Goal) :- 
	functor(Goal,Functor,Arity).
	
	

	
/* is_deterministic(Term,Program) :- succeeds if the execution of Term is 
	deterministic; i.e, if a call to Term always succeeds at most once.
*/
is_deterministic(Term,Program) :-
		Term = (L,R) -> (
				R = ! -> true %... this models the cutting of choice points 
			;
				(is_deterministic(L,Program), is_deterministic(R,Program))
		)
	;
		(
			functor(Term,Functor,Arity),
			has_property(Functor/Arity,deterministic(yes),Program)
		)
	.




/* has_property(P,Property,Program) :- succeeds if the predicate P 
	(P=Functor/Arity) of the Program has the given Property.
*/
has_property(P,Property,Program) :- 
	member(pred(P,_Clauses,Properties),Program),
	memberchk(Property,Properties).


	