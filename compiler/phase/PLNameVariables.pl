/*
	Iterates over all clauses and associates free variables with names. 
	
	@author Michael Eichberg
*/
:- module('Compiler:Phase:NameVariables',[pl_name_variables/4]).

:- use_module('../Utils.pl').
:- use_module('../Debug.pl').


/*	Associates names with an SAE Prolog program's (free) variables.
	
	@param Program is the program in normalized form. The structure is:
		<pre>
		[	pred(
				a/1, 							% The identifier of the predicate
				[(C, [det, type=int])],	% A list of clauses defining the 
												% predicate. C is one of these clauses.
												% In case of built-in propeties, this list
												% is empty.
				[type=...]					% List of properties of the predicate
			),...
		]
		</pre>
*/
pl_name_variables(Debug,Program,_OutputFolder,Program) :-
	debug_message(
		Debug,on_entry,
		'\nPhase: Name Variables_______________________________________________'),
		
	process_predicates(Program),
	
	debug_message(Debug,ast,Program).
	


process_predicates([]).
process_predicates([pred(_Predicate,Clauses,_PredicateProperties)|Predicates]) :-
	process_clauses(Clauses),
	process_predicates(Predicates).
	
	


/*	Traverses a list of clauses and unifies each (free) variable with a uniqe 
	term 'SAE':var(<VarName>,State) which associates the variable with a name 
	and additional information about the variable. 
	If a variable is used only once, State remains free, otherwise State is 
	bound to 'not_dead'.
*/
process_clauses([]) :- !. %green
process_clauses([(:-(H,B),_ClauseProperties)|Clauses]) :- 
write('Processing: '),write(:-(H,B)),nl,
	process_term(H,arg,1,_AID),
	process_term(B,local,1,_VID),
	process_clauses(Clauses).	

process_term(Term,Type,ID,NewID) :-
	var(Term),
	!,
	Term = 'SAE':var(Type,ID,_Free),
	NewID is ID + 1.
process_term(Term,_Type,ID,ID) :- 
	Term = 'SAE':var(_,_,'not_dead'),
	!.
process_term([Term|Terms],Prefix,ID,NewID) :-
	% This special case is required to not run in 
	% endless recursion (cf. next clause).
	process_term(Term,Prefix,ID,NID),
	process_term(Terms,Prefix,NID,NewID),
	!.	
process_term(Term,Type,ID,NewID) :-
	compound(Term),!,
	Term =.. [_|Args],
	process_term(Args,Type,ID,NewID).
% The base case (i.e., _Term is an atom):
process_term(_Term,_Type,ID,ID) :- 
	true.






