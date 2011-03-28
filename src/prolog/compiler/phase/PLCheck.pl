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


pl_check(DebugConfig,Program,_OutputFolder,Program) :-
	debug_message(
			DebugConfig,
			on_entry,
			write('\n[Debug] Phase: Check Program________________________________________________\n')),
	check_predicates(DebugConfig,Program,_).
	% The following unification (and subsequently the pl_check predicate as a 
	% whole) fails if an error was found.
	%State = no_errors. 


/**
	Validates the SAE program.
*/

check_predicates(DebugConfig,Program,State) :- 
	write('check predicates'),
	foreach_user_predicate(Program, check_predicate(DebugConfig,State, Program)).

check_predicate(DebugConfig, State, Program, Predicate) :- 
	write('check predicate'),
	Predicate = pred(PredicateIdentifier,_,_),
	debug_message(
			DebugConfig,
			on_entry,
			write( ('\n[Debug] Processing Predicate: ' , PredicateIdentifier , '\n')) ),
	predicate_clauses(Predicate, Clauses), 
	!,
	catch(
		foreach_clause(Clauses,check_clause(State, Program)),
		_,
		pl_check_failed(DebugConfig) ).

		
%CHECK: müssen fact und directive hier betrachtet werden?
check_clause(State, Program, Clause):- 
	write('\n\t Processing one Clause'), 
	clause_implementation(Clause,Impl),
	is_rule(Impl),
	rule(_Head,Body,_Meta,Impl),
	!,
	check_term(State, Program, Body).
	
check_term(State, Program, Term) :- 
	is_atom(Term),  %a atom is always a valid term
	write('\n\t\tCurrent Clause is a atom -> Lookup was successful\n'),
	!.
check_term(State, Program, Term) :- 
	 is_compound_term(Term), %compound term == compley term
	 !,
	 compound_term_identifier(Term,FunctorArity),
	 compound_term(Term,Functor,Args),
	 write('\n\t\tCurrent Clause is a compound_term'),
	 write('\n\t\t\tFunktor:'),
	 write(FunctorArity),
	 %write('\n\t\t\tArgs:'),
	 %write(Args),
	 write('\n\t\t\tLookup the Predicate of the compound_term: '),
	 write(FunctorArity),
	 check_complex_term(State, Program, Functor, FunctorArity),
	 !,
	 check_args_of_a_complex_term(State, Program, Args).
check_term(State, Program, Term) :- 
	%no vaild Term so fail
	write('\n\t\t\t\t\t\tUnknown term'),
	write(Term),
	%State = error,
	!,
	fail.			

check_complex_term(State, Program, Functor, FunctorArity) :- 
	lookup_predicate(FunctorArity,Program, Predicate), 
	write('\n\t\t\t\tlookup of the functor was successful.'),
	!.
check_complex_term(State, Program, Functor, FunctorArity) :- 
	FunctorArity = Functor/Arity,
	predefined_functor(Functor),
	Arity =:= 2,
	write('\n\t\t\t\tlookup of the functor was successful.'),
	!.
check_complex_term(State, Program, Functor, FunctorArity) :- 
	write('\n\t\t\t\t lookup failed'),
	!,fail.

check_args_of_a_complex_term(State, Program, [] ) .
check_args_of_a_complex_term(State, Program, [Head | Tail ]  ) :- 
	%write('\n'),
	%write(Head),
	%write('\n'),
	!,
	check_term(State, Program, Head),
	!,
	check_args_of_a_complex_term(State, Program, Tail ).
	%check_term(State, Program, Head).
	
	
is_atom(Term) :-
	is_variable(Term);
	is_string_atom(Term) ;
	is_integer_value(Term) ;
	is_float_atom(Term) ;
	is_numeric_atom(Term) ;
	is_anonymous_variable(Term).
%check_predicates(_,_State). % :- !.
% TODO implement a check for multiple occurences of the same "named" anonymous variable
% TODO implement a check that all gooals exist (unresolved references)
% TODO check that no "default" operators are overridden


pl_check_failed(DebugConfig) :-
	debug_message(
		DebugConfig,
		on_entry,
		write('\n[Debug] Phase: Check Program ---------------- FAILED --------------------------\n')).