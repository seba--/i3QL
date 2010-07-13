/*
	Checks if the SAE Prolog program is valid. I.e., the user does not redefine 
	a built-in predicate or use a datastructure that is used by the Compiler
	itself.
	
	@author Michael Eichberg
*/
:- module('Compiler:Phase:Check',[pl_check/4]).

%:- use_module('../Predef.pl').
%:- use_module('../Utils.pl').
:- use_module('../Debug.pl').




pl_check(Debug,Program,_OutputFolder,Program) :-
	debug_message(
		Debug,on_entry,
		'\nPhase: Check Program________________________________________________'),

	check_predicates(Program,Program,State),
	% The following unification (and subsequently the check predicate) 
	% fails if an error was found:
	State = no_errors. 




/* check_predicates(PredicatesToCheck,Program,State) :- checks an SAE Prolog program.

	@param PredicatesToCheck is the list of all predicates of the SAE Prolog 
		program. Initially, PredicatesToCheck and Program are identical.
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
	@param State remains a free variable unless an error is detected, in this case
		State is unified with 'error_found'.
*/
check_predicates([],_,_) :- !.
check_predicates([pred(Predicate,_Clauses,_Properties)|Predicates],Program,State) :-
	ignore((
		\+ is_predicate_unique(Predicate,Program),
		State = predicate_is_not_unique,
		write('[Error] Multiple definitions of the predicate: '),
		write(Predicate),
		write(' exists.'),nl
	)),
	check_predicates(Predicates,Program,State) .
	
	
	
is_predicate_unique(Predicate,Program)	:-
	count_predicate_definitions(Predicate,Program,0,Count),
	Count =:= 1.
	

count_predicate_definitions(_Predicate,[],Count,Count).
count_predicate_definitions(Predicate,[pred(Predicate,_,_)|Predicates],Count,Result) :-
	NewCount is Count + 1,
	count_predicate_definitions(Predicate,Predicates,NewCount,Result).
count_predicate_definitions(Predicate,[pred(APredicate,_,_)|Predicates],Count,Result) :-
	Predicate \= APredicate,
	count_predicate_definitions(Predicate,Predicates,Count,Result).
	
	
	













