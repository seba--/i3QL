/*
	Constructs call graph information for a given SAE Prolog program.
	
	@author Michael Eichberg
*/
:- module('Compiler:Phase:CallGraph',[pl_call_graph/4]).

:- use_module('../Predef.pl').
:- use_module('../Utils.pl').
:- use_module('../Debug.pl').



/*
	@param Program
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
pl_call_graph(Debug,Program,_OutputFolder,NProgram) :-
	debug_message(
		Debug,on_entry,
		'\nPhase: Call Graph Construction______________________________________'),

	call_graph(Program,NProgram,State),
	State = constructed,
	
	debug_message(Debug,ast,Program).




call_graph(Program,NProgram,State) :- call_graph(Program,Program,NProgram,State).
	
	
	
	
/* call_graph(Predicates,Program,NProgram,State) :- makes the called-by 
	information readily available.
	
	@param Predicates the set of remaining predicates that has to be processed.
	@param Program the current program information
	@param NProgram the new program information (with the called by 
		information.)
	@param State the state of the call graph construction.
*/
call_graph([],Program,Program,_) :- !.
call_graph([Predicate|Predicates],Program,NProgram,State) :- 
	Predicate=pred(F/A,Clauses,_),
	(	Clauses = [] -> % pred is a built-in predicate
		call_graph(Predicates,Program,NProgram,State)
	;
		process_clauses(F/A,Clauses,Program,IProgram,State),
		call_graph(Predicates,IProgram,NProgram,State)
	).
	
	
process_clauses(_CurrentPredicate,[],Program,Program,_State).
process_clauses(CurrentPredicate,[(Clause,_ClauseProperties)|Clauses],Program,NProgram,State) :-
	process_clause(CurrentPredicate,Clause,Program,IProgram,State),
	process_clauses(CurrentPredicate,Clauses,IProgram,NProgram,State).




/*
	Analyzes a normalized claused.
*/
process_clause(CurrentPredicate,:-(_,B),Program,NProgram,State) :-
write(CurrentPredicate),write(' - processing clause: '),write(B),nl,
	process_term(CurrentPredicate,B,Program,NProgram,State).


process_term(_CurrentPredicate,V,_Program,_NProgram,_State) :- var(V),!.		




process_term(CurrentPredicate,Term,Program,NProgram,State) :- 
	functor(Term,Functor,Arity),
	partition(
		'='(pred(Functor/Arity,_Clauses,_PredicateProperties)),
		Program,
		CalledPredicate,
		OtherPredicates
	),
	( 	CalledPredicate = [] -> (
			atomic_list_concat(['[Error] the goal: ',Functor,'/',Arity,' is not defined.\n'],M),
			write(M),
			State = undefined_goal,
			IProgram = Program %... just to be able to report further errors
		) ; (	
			CalledPredicate = [pred(P,Clauses,PredicateProperties)],
			add_called_by_information(
				CurrentPredicate,
				PredicateProperties,
				NPredicateProperties
			),
			IProgram = [pred(P,Clauses,NPredicateProperties)|OtherPredicates]
		)
	),
	( (Arity =:= 2, (Functor=';' ; Functor=',')) -> (
			arg(1,Term,L),process_term(CurrentPredicate,L,IProgram,LProgram,State),
			arg(2,Term,R),process_term(CurrentPredicate,R,LProgram,NProgram,State)	
		);
			NProgram = IProgram
	).




add_called_by_information(P,PredicateProperties,NPredicateProperties) :-
	partition(
		'='(called_by(_)),
		PredicateProperties,
		CalledByPredicateProperty,
		OtherPredicateProperties
	),
	(	CalledByPredicateProperty = [] ->	
			NPredicateProperties = [called_by([P])|PredicateProperties]
		; 
		(
			CalledByPredicateProperty = [called_by(Ps)],
			list_set_prepend(P,Ps,NPs),			
			NPredicateProperties = [called_by(NPs)|OtherPredicateProperties]
		)
	).




list_set_prepend(E,ListSet,NewListSet) :-
	member(E,ListSet) ->
		NewListSet = ListSet
	;
		NewListSet = [E|ListSet].