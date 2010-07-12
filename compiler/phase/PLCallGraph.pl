/*
	Constructs call graph information for a given SAE Prolog program.
	
	@version $Date$ $Rev$
	@author Michael Eichberg
*/
:- module('Compiler:Phase:CallGraph',[pl_call_graph/4]).

:- use_module('../Predef.pl').
:- use_module('../Utils.pl').
:- use_module('../Debug.pl').




pl_call_graph(Debug,Program,_OutputFolder,Program) :-
	debug_message(
		Debug,on_entry,
		'\nPhase: Call Graph Construction______________________________________'),

	call_graph(Program,State),
	State = constructed,
	
	debug_message(Debug,ast,Program).


call_graph(Program,State) :- process_predicates(Program,Program,State).
	

process_predicates([],_,_State) :- !. % green cut
process_predicates([(_Predicate,[defined(sae)|_]-_)|Predicates],Program,State):- 
	!, % green cut
	process_predicates(Predicates,Program,State).
process_predicates([(Predicate,[defined(user)|OtherProperties]-_)|Predicates],Program,State):-
%write('Processing predicate: '),write(Predicate),write(OtherProperties),nl,
	once(member(clauses(Clauses),OtherProperties)),
	process_clauses(Clauses,Program,State),
	process_predicates(Predicates,Program,State).
	
	
process_clauses([],_,_).
process_clauses([[NClause|_]|Clauses],Program,State):-
%write('Processing clause: '),write(NClause),nl,
	process_nclause(NClause,Program,State),
	process_clauses(Clauses,Program,State).


/*
	Processes a normalized claused.
*/
process_nclause(:-(_,B),Program,State) :-
	process_term(B,Program,State).

		
process_term(V,_Program,_State) :- var(V),!.		

process_term((L,R),Program,State) :- !,
	process_term(L,Program,State),
	process_term(R,Program,State).

process_term((L;R),Program,State) :- !,
	process_term(L,Program,State),
	process_term(R,Program,State).

/* If one of the predefined operators is used, no call is actually executed. */
process_term(Operator,_Program,_State) :- 
	functor(Operator,Functor,Arity),
	Arity =:= 2,
	predefined_operator(Functor),
	!.
	
process_term(A,Program,State) :- 
	atom(A),!,
	( 	member((A/0,Properties),Program) -> 
			write('call '),write(A),nl
		;
			atomic_list_concat(['[Error] the goal: ',A,'/0 is not defined.\n'],M),
			write(M),
			State = undefined_goal
	).	
	
process_term(CT,Program,State) :- 
	functor(CT,Functor,Arity),
	(	member((Functor/Arity,Properties),Program) ->
			write('call '),write(Functor),write('/'),write(Arity),nl
		;
			atomic_list_concat(['[Error] the goal: ',Functor,'/',Arity,' is not defined.\n'],M),
			write(M),
			State = undefined_goal
	).
	
	
	
	








