/* License (BSD Style License):
   Copyright (c) 2010
   Department of Computer Science
   Technische Universität Darmstadt
   All rights reserved.

    Redistribution and use in source and binary forms, with or without
    modification, are permitted provided that the following conditions are met:

    - Redistributions of source code must retain the above copyright notice,
      this list of conditions and the following disclaimer.
    - Redistributions in binary form must reproduce the above copyright notice,
      this list of conditions and the following disclaimer in the documentation
      and/or other materials provided with the distribution.
    - Neither the name of the Software Technology Group or Technische 
      Universität Darmstadt nor the names of its contributors may be used to 
      endorse or promote products derived from this software without specific 
      prior written permission.

    THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
    AND ANY EXPRESSED OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
    IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
    ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
    LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
    CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
    SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
    INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
    CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
    ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
    POSSIBILITY OF SUCH DAMAGE.
*/


/*	Checks that the SAE Prolog program is valid. I.e., that the program does not 
	redefine a built-in predicate or uses a datastructure that is used by the 
	compiler	itself.
	
	@author Michael Eichberg
*/
:- module('Compiler:Phase:Check',[pl_check/4]).

:- use_module('../Debug.pl').
:- use_module('../Predef.pl',[built_in_term/1]).



pl_check(Debug,Program,_OutputFolder,Program) :-
	debug_message(
		Debug,on_entry,
		'\nPhase: Check Program________________________________________________'),

	check_predicates(Program,Program,State),
	% The following unification (and subsequently the check predicate) 
	% fails if an error was found:
	State = no_errors. 



/* check_predicates(PredicatesToCheck,Program,State) :- checks that the 
	SAE Prolog progam does not (try to) redefine built-in predicates and 
	that no (sub)terms conflict with terms used by SAE prolog.

	@param PredicatesToCheck is the list of all predicates of the SAE Prolog 
		program. Initially, PredicatesToCheck and Program are considered to 
		identical.
	@param Program is a SAE Prolog program in normalized form. 
		<p>
		The expected structure is:
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
		<p/>
	@param State is the output variable. State is free unless an error is 
		detected, in this case State is unified with 'error_found'.
*/
check_predicates([],_,_) :- !.
check_predicates([pred(Predicate,Clauses,_Properties)|Predicates],Program,State) :-
	ignore((
		%	IMPROVE For each predicate we iterates over all predicates (O(n^2)).
		\+ is_predicate_unique(Predicate,Program),
		State = error_found,
		write('[Error] Multiple definitions of the predicate: '),
		write(Predicate),
		write(' exists.'),nl
	)),
	check_clauses(Clauses,State),
	check_predicates(Predicates,Program,State).



/*	is_predicate_unique(Predicate,Program)	:- tests that a predicate does not
	have multiple definitions. 
	
	@param Program a complete SAE Prolog program consisting of the user and 
		built in predicates.
*/
is_predicate_unique(Predicate,Program)	:-
	count_predicate_definitions(Predicate,Program,0,Count),
	Count =:= 1.



% Helper predicate; see {@link is_predicate_unique/2}.
count_predicate_definitions(Predicate,[pred(Predicate,_,_)|Predicates],Count,Result) :-
	!,
	NewCount is Count + 1,
	count_predicate_definitions(Predicate,Predicates,NewCount,Result).
count_predicate_definitions(Predicate,[pred(APredicate,_,_)|Predicates],Count,Result) :-
	Predicate \= APredicate,!,
	count_predicate_definitions(Predicate,Predicates,Count,Result).
count_predicate_definitions(_Predicate,[],Count,Count).



% just iterates over all clauses
check_clauses([],_State).
check_clauses([(Clause,_Properties)|Clauses],State) :-
	check_clause(Clause,State),
	check_clauses(Clauses,State).



check_clause(Clause,State) :-
	ignore((
		\+ does_not_use_sae_namespace(Clause),
		State=error_found,
		write('[Error] The clause: "'),
		write(Clause),
		write('" uses the reserved namespace \'$SAE\'.'),nl
	)).



does_not_use_sae_namespace(V) :- var(V),!.
does_not_use_sae_namespace(T) :-
	\+ built_in_term(T),
	T =.. [_Functor|Args],
	forall(member(Arg,Args),does_not_use_sae_namespace(Arg)).

	