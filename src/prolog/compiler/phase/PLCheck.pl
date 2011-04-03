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
:- use_module('../Utils.pl').

% TODO: add a check if a variable is bounded
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

/*
	calls for all user predicate the internal check routine
*/
check_predicates(DebugConfig,Program,State) :- 
	catch(
		foreach_user_predicate(Program, check_predicate(DebugConfig,State, Program)),
		_,
		fail),
		!,
	debug_message(DebugConfig,on_exit, write('\n[Debug] PLCheck: no errors found '))
		. 
		
		
check_predicates(DebugConfig,_Program,_State) :- 
	debug_message(DebugConfig,on_exit, write('\n\n[Debug] PLCheck:  ###################### ERRORS FOUND ######################')),
	!,
	fail.	
check_predicate(DebugConfig, State, Program, Predicate) :- 
	Predicate = pred(PredicateIdentifier,_,_),
	debug_message(DebugConfig,processing_predicate, write_list(['\n[Debug] Processing Predicate: ', PredicateIdentifier])),
	predicate_clauses(Predicate, Clauses), 
	catch(
		foreach_clause(Clauses,check_clause(DebugConfig, State, Program)),
		_,
		fail ).
	

/*
check_clause(DebugConfig, _State, _Program, Clause):- 
	is_rule_without_body(Clause); %CHECK: scheint nicht zu funktionieren
	clause_implementation(Clause,Impl),
	is_rule(Impl),
	rule(_Head,Body,_Meta,Impl),
	is_string_atom(Body),
	debug_message(DebugConfig,memberchk(processing_clause), write_list( ['\n[Debug] \tClause is a string atom: (',Body,')']) ).		
*/
check_clause(DebugConfig, State, Program, Clause):- 
	debug_message(DebugConfig,memberchk(processing_clause), write('\n[Debug] Processing one Clause: ') ),
	clause_implementation(Clause,Impl),
	is_rule(Impl),
	rule(_Head,Body,_Meta,Impl),
	check_term(DebugConfig, State, Program, Body).

check_term(_DebugConfig, _State, _Program, Term) :-
	% a anonymous vaiable is no valid predicate
	% ( p(x) :- _. )
	is_anonymous_variable(Term),
	pl_check_error(Term, 'a anonymous variable is no valid predicate'),
	!,
	fail.

/*	
% check_term for String atoms like !, fail, ...
check_term(DebugConfig, _State, _Program, Term) :- 
	%a atom make no sense as a predicate?
	%aber brauch es da is_rule_without_body nicht so will wie ich möchte :)
	is_string_atom(Term),  									
	string_atom(Term,Value),
	internal_allowed_string_atoms(Value),
	!,
	debug_message(DebugConfig,memberchk(processing_clause), write( '\n[Debug] \Term a allowed string atom (fail, !, ...)') ).
*/

% check_term for predicates p\0	
check_term(DebugConfig, _State, Program, Term) :- 
	%a atom make no sense as a predicate?
	%aber brauch es da is_rule_without_body nicht so will wie ich möchte :)
	is_string_atom(Term),  									
	string_atom(Term,Value),
	lookup_predicate(Value/0, Program, Predicate),
	Predicate = pred(Value/0,_,_),
	!,
	debug_message(DebugConfig,memberchk(processing_clause), write( '\n[Debug] \tCurrent Term is a predicate\0') ).	

	
%check_term for complex terms	
check_term(DebugConfig, State,Program, Term) :- 
	is_compound_term(Term), %compound term == compley term
	!,
	compound_term_identifier(Term,FunctorArity),
	compound_term_args(Term,Args),
	debug_message(DebugConfig,memberchk(processing_clause), write_list( (['\n[Debug] \tCurrent Term is a compound term with the functor: ',FunctorArity ])) ),
	!,
	check_complex_term(DebugConfig, State, Program, Term, FunctorArity, Args).

	%check_term failed
check_term(_DebugConfig, _State, _Program, Term) :- 
	%no vaild Term so fail
	%State = error,
	!,
	%State = error_unknown_term,
	pl_check_error(Term, 'Unknown term'),
	fail.	

check_term(_DebugConfig, _State, _Program, Term) :-
	pl_check_error(Term, 'Error during PLCheck'),
	!,
	fail.	
%----

check_complex_term(DebugConfig, State, Program, _Term,  FunctorArity, Args) :- 
	% check if the term is a control flow term
	internal_control_flow_term(FunctorArity),
	!,
	debug_message(DebugConfig,memberchk(processing_clause), write_list( (['\n[Debug] \tCurrent term iscontrol flow term (',FunctorArity,')' ])) ),
	check_args_of_a_complex_term(DebugConfig, State, Program, Args).
	
	

check_complex_term(DebugConfig, _State, Program, _Term,  FunctorArity, _Args) :- 
	lookup_predicate(FunctorArity, Program, Predicate),
	Predicate = pred(FunctorArity,_,_),
	not(lookup_in_predicate_meta(mode(_X),Predicate)),
	debug_message(DebugConfig,memberchk(processing_clause), write( ('\n[Debug] \tLookup predicate succesful, predicate has no mode'))),
	!.

check_complex_term(DebugConfig, State, Program, _Term,  FunctorArity, Args) :- 
	lookup_predicate(FunctorArity, Program, Predicate),
	Predicate = pred(FunctorArity,_,_),
	lookup_in_predicate_meta(mode(X),Predicate),
	!,
	debug_message(DebugConfig,memberchk(processing_clause), write( ('\n[Debug] \tLookup predicate succesful, predicate has mode -> look if some sub terms a callable'))),
	!,
	check_all_callable_sub_terms(DebugConfig, State,Program, Predicate, Args, X)
	.
	
%----
	


check_complex_term(_DebugConfig, _State, _Program, Term,  _FunctorArity, _Args) :- 
	%State = error_lookup_failed,
	pl_check_error(Term, 'Lookup failed'),
	!,fail.
	

check_all_callable_sub_terms(_DebugConfig, _State, _Program, _Predicate, [], [] ).
check_all_callable_sub_terms(DebugConfig, State, Program,  Predicate, [Arg|Args], [_;callable | Xs] ) :-
	!,
	debug_message(DebugConfig,memberchk(processing_clause), write( '\n[Debug] \tFound callable sub term')),
	%write(Arg),
	!,
	check_term(DebugConfig, State, Program, Arg),
	!,
	check_all_callable_sub_terms(DebugConfig, State, Program,  Predicate, Args,Xs ).
check_all_callable_sub_terms(DebugConfig, State, Program,  Predicate, [Arg|Args], [_;callable/A | Xs] ) :-
	!,
	debug_message(DebugConfig,memberchk(processing_clause), write( '\n[Debug] \tFound callable/A sub term')),
	term_name(Arg, Term_name),
	%!,
	%term_type(Arg, Term_type),
	!,
	lookup_predicate(Term_name/A, Program, Pred),
	Pred = pred(Term_name/A,_,_),
	!,
	check_all_callable_sub_terms(DebugConfig, State, Program,  Predicate, Args,Xs ).	
check_all_callable_sub_terms(DebugConfig, State, Program,  Predicate, [_Arg|Args], [ _X | Xs] ) :-
	%!,
	%Arg \= _;callable_,
	%Arg \= _;callable,
	!,
	debug_message(DebugConfig,memberchk(processing_clause), write( '\n[Debug] \tNot callable sub term')),
	!,
	check_all_callable_sub_terms(DebugConfig, State, Program,  Predicate, Args, Xs ).
check_all_callable_sub_terms(_, _, _,  _, _, _ ) :- !, fail.


check_args_of_a_complex_term(_DebugConfig, _State, _Program, [] ) :- !.
check_args_of_a_complex_term(DebugConfig, State, Program, [Head | Tail ]  ) :- 
	!,
	check_term(DebugConfig, State, Program, Head),
	!,
	check_args_of_a_complex_term(DebugConfig, State, Program, Tail ).


%control flow terms	
internal_control_flow_term((,)/2).
internal_control_flow_term((;)/2).
internal_control_flow_term((->)/2).
internal_control_flow_term((*->)/2).
internal_control_flow_term(not/1).
internal_control_flow_term((\+)/1).


internal_allowed_string_atoms(!).
internal_allowed_string_atoms(fail).
internal_allowed_string_atoms(true).
internal_allowed_string_atoms(false).

% TODO implement a check for multiple occurences of the same "name" anonymous variable
% TODO check that no "default" operators are overridden


pl_check_failed(_DebugConfig) :-
	write('fail'),
	fail.
		
pl_check_error(Term, MSG) :- 
	term_pos(Term, File, LineNumber, CN),
	%TODO: put Term =.. [_,_, TermName|_] in the AST 
	Term =.. [TermType,_, TermName|_],
	atomic_list_concat(['\n',File, ':',LineNumber,':' , CN ,' : error: ',MSG, ' (Termtype: ', TermType, ' Termname: ' ,TermName ,')'], ErrorMSG),
	write(ErrorMSG).

%TODO: put in the ast
term_name(Term, Name) :- Term =.. [_,_, Name|_].
term_type(Term, Type) :- Term =.. [Type,_,_|_].