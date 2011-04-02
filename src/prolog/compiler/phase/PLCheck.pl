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


/**
	Checks that the SAE Prolog program is valid.
	
	@author Michael Eichberg
	@author Malte Viering
*/
:- module(sae_check_sae_program,[pl_check/4]).

:- use_module('../Debug.pl').
:- use_module('../Predef.pl').
:- use_module('../AST.pl').

% TODO: add a check if a variable is bounded
pl_check(DebugConfig,Program,_OutputFolder,Program) :-
	debug_message(
			DebugConfig,
			on_entry,
			write('\n[Debug] Phase: Check Program________________________________________________\n')),
	check_predicates(DebugConfig,Program,_),
	debug_message(
			DebugConfig,
			on_exit,
			write('\n[Debug] Phase: Check Program_______________________END______________________\n')).
	% The following unification (and subsequently the pl_check predicate as a 
	% whole) fails if an error was found.
	%State = no_errors. 
pl_check(_,_,_,_).

/**
	Validates the SAE program.
*/
check_predicates(DebugConfig,Program,State) :- 
	catch(
		foreach_user_predicate(Program, check_predicate(DebugConfig,State, Program)),
		_,
		pl_check_failed(DebugConfig) ).

check_predicate(DebugConfig, State, Program, Predicate) :- 
	Predicate = pred(PredicateIdentifier,_,_),
	debug_message(
			DebugConfig,
			on_entry, %flag
			write( ('\n[Debug] Processing Predicate: ' , PredicateIdentifier , '\n')) ),
	predicate_clauses(Predicate, Clauses), 
	catch(
		foreach_clause(Clauses,check_clause(DebugConfig, State, Program)),
		_,
		fail ).

		
check_clause(DebugConfig, State, Program, Clause):- 
	is_rule_without_body(Clause). %CHECK: scheint nicht zu funktionieren
		
check_clause(DebugConfig, State, Program, Clause):- 
	write('\n\t Processing one Clause'), 
	clause_implementation(Clause,Impl),
	is_rule(Impl),
	rule(_Head,Body,_Meta,Impl),
	check_term(DebugConfig, State, Program, Body).

check_term(DebugConfig, _State, _Program, Term) :-
	% a anonymous vaiable is no valid predicate
	% ( p(x) :- _. )
	is_anonymous_variable(Term),
	pl_check_error(Term, 'a anonymous variable is no valid predicate'),
	!,
	fail.
check_term(DebugConfig, _State, _Program, Term) :- 
	%a atom make no sense as a predicate?
	%aber brauch es da is_rule_without_body nicht so will wie ich möchte :)
	is_atom(Term),  									
	!,
	write(Term),
	write('\n\t\tCurrent Clause is a atom -> Lookup was successful\n').

check_term(DebugConfig, State,Program, Term) :- 
	 is_compound_term(Term), %compound term == compley term
	 !,
	 compound_term_identifier(Term,FunctorArity),
	 compound_term_args(Term,Args),
	 write('\n\t\tCurrent Clause is a compound_term'),
	 write('\n\t\t\tFunktor:'),
	 write(FunctorArity),
	 %write('\n\t\t\tArgs:'),
	 %write(Args),
	 write('\n\t\t\tLookup the Predicate of the compound_term: '),
	 write(FunctorArity),
	 write('\n'),
	check_complex_term(DebugConfig, State, Program, FunctorArity, Args).

check_term(DebugConfig, _State, _Program, Term) :- 
	%no vaild Term so fail
	write('\n\t\t\t\t\t\tUnknown term'),
	write(Term),
	%State = error,
	!,
	pl_check_error(Term, 'Unknown term'),
	fail.	

%----
check_complex_term(DebugConfig, State, Program,  FunctorArity, Args) :- 
	FunctorArity == (=)/2.

check_complex_term(DebugConfig, State, Program,  FunctorArity, Args) :- 
	write('\nPredicate in ast?'),
	lookup_predicate(FunctorArity, Program, Predicate),
	Predicate = pred(FunctorArity,_,_),
	write('ja'),
	not(lookup_in_predicate_meta(mode(X),Predicate)),
	!,
	write('\n\that kein mode').

check_complex_term(DebugConfig, State, Program,  FunctorArity, Args) :- 
	write('\nPredicate in ast?'),
	lookup_predicate(FunctorArity, Program, Predicate),
	Predicate = pred(FunctorArity,_,_),
	write('ja'),
	lookup_in_predicate_meta(mode(X),Predicate),
	!,
	check_all_callable_sub_terms(DebugConfig, State,Program, Predicate, Args, X),
	
	write('\n\that mode: '),
	write(X).

%----
	
check_complex_term(DebugConfig, State, Program,  FunctorArity, Args) :- 
	% check if the term is a control flow term
	internal_control_flow_term(FunctorArity),
	!,
	write(FunctorArity),
	write('\nIs control flow term\n\n'),
	check_args_of_a_complex_term(DebugConfig, State, Program, Args).
	
	

check_complex_term(DebugConfig, _State, _Program,  _FunctorArity, Args) :- 
	write('\n\t\t\t\tlookup failed'),
	!,fail.
	

check_all_callable_sub_terms(DebugConfig, State, Program,  Predicate, Args, [] ).
check_all_callable_sub_terms(DebugConfig, State, Program,  Predicate, [Arg|Args], [+;callable | Xs] ) :-
	!,
	write('\nCheck sub terms Arg: \n'),
	write(Arg),
	check_term(DebugConfig, State, Program, Arg),
	!,
	check_all_callable_sub_terms(DebugConfig, State, Program,  Predicate, Args,Xs ).
check_all_callable_sub_terms(DebugConfig, State, Program,  Predicate, [Arg|Args], [ X | Xs] ) :-
	!,
	write('\nCheck sub terms Mode X: \n'),
	write(X),
	check_all_callable_sub_terms(DebugConfig, State, Program,  Predicate, Args, Xs ).

check_args_of_a_complex_term(DebugConfig, _State, _Program, [] ) :- !, write('\n\t\t\t\t\tlookup of the clause was successful.').
check_args_of_a_complex_term(DebugConfig, State, Program, [Head | Tail ]  ) :- 
	check_term(DebugConfig, State, Program, Head),
	!,
	check_args_of_a_complex_term(DebugConfig, State, Program, Tail ).

% call x  gleich x
is_atom(Term) :-

	%is_variable(Term)  %vaiable mussen mussen gecheck werden das sie gebunden sind
	is_string_atom(Term) 
	%is_integer_value(Term) 
	%is_float_atom(Term) ;
	%is_numeric_atom(Term) 
	. %hier muss ein fehelr passieren bei anonzmous vaiable

%control flow terms	
internal_control_flow_term((,)/2).
internal_control_flow_term((;)/2).
internal_control_flow_term((->)/2).
internal_control_flow_term((*->)/2).


% TODO implement a check for multiple occurences of the same "named" anonymous variable
% TODO implement a check that all gooals exist (unresolved references)
% TODO check that no "default" operators are overridden


pl_check_failed(DebugConfig) :-
	debug_message(
		DebugConfig,
		on_entry,
		write('\n[Debug] Phase: Check Program ---------------- PCCheck-FAILED --------------------------\n'))
		,fail.
		
pl_check_error(Term, MSG).