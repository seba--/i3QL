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
	This analysis identifies clauses for which we can apply simple last call 
	optimizations. I.e., this analysis identifies predicates that are tail 
	recursive, and where we can statically decide that there a no more choice 
	points – when the tail recursive goal is reached. 
	<br />
	Additionally, this analysis makes a sound approximation of the number of 
	solutions of each predicate.
		
	@author Michael Eichberg
*/
:- module(
	sae_analyze_tail_calls,
	[pl_analyze_tail_calls/4]
).

:- use_module('../AST.pl').
:- use_module('../Debug.pl').
:- use_module('../Predef.pl').
:- use_module('../Utils.pl').
:- use_module('../Analyses.pl').



pl_analyze_tail_calls(DebugConfig,Program,_OutputFolder,Program) :-
	debug_message(DebugConfig,on_entry,
	write('\n[Debug] Phase: Analyzing Tail Calls ________________________________________\n')),
	% IMPROVE Implement a true fixed-point analysis for estimating the number of solutions and identifying tail calls etc.
	foreach_user_predicate(
		Program,
		analyze_number_of_potential_solutions_of_predicate(DebugConfig,Program)),
	foreach_user_predicate(
		Program,
		analyze_tail_calls(DebugConfig,Program)),
	foreach_user_predicate(
		Program,
		analyze_number_of_potential_solutions_of_predicate(DebugConfig,Program)).



analyze_number_of_potential_solutions_of_predicate(_DebugConfig,_Program,Predicate) :-
	lookup_in_predicate_meta(solutions(_),Predicate),!. % we have already analyzed the predicate
analyze_number_of_potential_solutions_of_predicate(DebugConfig,Program,Predicate) :-
	predicate_identifier(Predicate,Functor/Arity),
	(	lookup_in_predicate_meta(deterministic_clause_selection(yes),Predicate)
	->	
		predicate_clauses(Predicate,Clauses),
		foreach_clause(
			Clauses,
			analyze_number_of_potential_solutions_of_clause(Program),
			SolutionsList),
		(	forall(member(Solution,SolutionsList),Solution = [_,1]) 
		->
			add_flag_to_predicate_meta(solutions([0,1]),Predicate),
			% logging...
			debug_message(DebugConfig,memberchk(solutions),write_atomic_list(['[Debug] Potential number of solutions of: ',Functor,'/',Arity,' - [0,1]\n']))
		;	% We know nothing...
			true
		)	
	;	% IMPROVE We could improve the lower bound estimation, by checking that all clauses have at least one solution.
		add_flag_to_predicate_meta(solutions([0,*]),Predicate),
		% logging...
		debug_message(DebugConfig,memberchk(solutions),write_atomic_list(['[Debug] Potential number of solutions of: ',Functor,'/',Arity,' - [0,*]\n']))
	).



analyze_number_of_potential_solutions_of_clause(
		Program,
		_ClauseId,
		Clause,
		_RelativePosition,
		Solutions
	) :-
	clause_implementation(Clause,Implementation),
	rule_body(Implementation,BodyASTNode),
	number_of_solutions_of_goal(Program,BodyASTNode,Solutions,_),
	(	Solutions = [_,1] ->
		add_to_clause_meta(solutions(Solutions),Clause)
	;
		% we know nothing... 
		% IMPROVE Store the clause for later (re-analysis).
		true
	).



analyze_tail_calls(DebugConfig,Program,Predicate) :-
	predicate_clauses(Predicate,Clauses),
	foreach_clause(
		Clauses,
		check_clause_for_tail_recursion(DebugConfig,Program,Predicate,HasTailCalls,NaiveLastCallOptimizationIsPossible),
		_
	),
	% So far we have identified all "trivially" tail recursive calls where we
	% can apply the last call optimization.
	% However, predicates (clauses) that call themselve - before the 
	% tail-recursive call - are not yet detected. For example, given the 
	% following predicate:
	% 	tak(X,Y,Z,A) :-
	%		X =< Y, !,
	%		Z = A.
	%	tak(X,Y,Z,A) :-
	%		% X > Y,
	%		X1 is X - 1,
	%		tak(X1,Y,Z,A1),
	%		Y1 is Y - 1,
	%		tak(Y1,Z,X,A2),
	%		Z1 is Z - 1,
	%		tak(Z1,X,Y,A3),
	%		tak(A1,A2,A3,A).
	% doing a last call optimization is possible.
	% The number of solutions of this predicate is always at most one (and if
	% it succeeds, no other clauses will be evaluated).
	% The number of solutions of the first clause is at most one and the second 
	% clause is tail recursive; i.e., it does not produce solutions on its own.
	%
	% To detect such cases, we check that all non-recursive clauses have at
	% most one solution and are always cut and that a tail-recursive clause
	% is either always cut or that it is the last clause and that the number
	% of solutions is at most one under the assumption that the number of solutions
	% of a recursive call is at most one.
	(	NaiveLastCallOptimizationIsPossible = false,
		HasTailCalls == true,
		foreach_clause(
			Clauses,
			check_predicate_for_tail_recursion(
				Program,
				Predicate,
				PredicateSupportsLastCallOptimization),
			_
		),
		PredicateSupportsLastCallOptimization = true, % the hypothesis was not rejected
		!,
		predicate_identifier(Predicate,Functor/Arity),
		debug_message(DebugConfig,memberchk(report_clauses_where_lco_is_possible),write_atomic_list(['[Debug] Last call optimization is possible for all tail-recursive clauses of: ',Functor,'/',Arity,'\n'])),
		add_flag_to_predicate_meta(has_clauses_where_last_call_optimization_is_possible,Predicate),
		add_flag_to_predicate_meta(solutions([0,1]),Predicate),
		foreach_clause(
			Clauses,
			add_information_that_last_call_optimization_is_possible_for_tail_calls_with(Functor/Arity)
		)
	;
		true % no further analysis is necessary
	).
	
	
% Currently, we only identify very simple cases where we have a unique last
% goal and which is a conjunction with its previous goals (we rely on the
% property that the goal sequence is left descending (done during loading)!) 
% x(...) :- a,b,c,x(...)
check_clause_for_tail_recursion(
		DebugConfig,
		Program,
		Predicate,
		HasTailCalls, % only to be instantiated to "true"!
		LastCallOptimizationIsPossible, % only to be instantiated to "true"!
		ClauseId,
		Clause,
		RelativePosition,
		_Output
	) :-	
	predicate_identifier(Predicate,Functor/Arity),
	clause_implementation(Clause,Implementation),
	rule_body(Implementation,BodyASTNode),
	(
		% TODO support last call optimization for cases such as: 
		% x(...) :- a,!,x(...);b,x(...). 
		% [this is the way to go for a more decent analysis:] 
		% last_primitive_goals_if_true(BodyASTNode,[LastGoal],[]),...
		compound_term(BodyASTNode,',',[PreviousGoal,LastGoal]),
		compound_term_identifier(LastGoal,Functor/Arity),% ... we have found a recursive call
		HasTailCalls = true, %... this result is reported 
		!,
		(
			(	% ... is it a candidate for a last call optimization?
				RelativePosition == last ; 
				lookup_in_clause_meta(cut(always),Clause)
			),
			number_of_solutions_of_goal(Program,PreviousGoal,[_LB,1],_),
			!,
			LastCallOptimizationIsPossible = true,
			debug_message(DebugConfig,memberchk(report_clauses_where_lco_is_possible),write_atomic_list(['[Debug] Last call optimization possible: ',Functor,'/',Arity,' - Clause: ',ClauseId,' (zero based)\n'])),
			add_to_clause_meta(last_call_optimization_is_possible,Clause),
			add_flag_to_predicate_meta(has_clauses_where_last_call_optimization_is_possible,Predicate),
			add_to_term_meta(last_call_optimization_is_possible,LastGoal)
		;
			true
		)
	;
		% the clause does not have a unique last goal or 
		% the unique last goal is not a recursive call or
		% a successor clause exists (hence, we still have a choice point...)
		true 
	).



% TODO merge the above analysis and this one...
check_predicate_for_tail_recursion(
		Program,
		Predicate,
		LastCallOptimizationIsPossible, % only to be instantiated to "true"!
		_ClauseId,
		Clause,
		RelativePosition,
		_Output
	) :-	
	predicate_identifier(Predicate,Functor/Arity),
	clause_implementation(Clause,Implementation),
	rule_body(Implementation,BodyASTNode),
	(
		compound_term(BodyASTNode,',',[PreviousGoal,LastGoal]),
		compound_term_identifier(LastGoal,Functor/Arity),
		( lookup_in_clause_meta(cut(always),Clause) ; RelativePosition == last ),
		number_of_solutions_of_goal(
			Program,
			assumption(Functor/Arity,solutions([0,1])),
			PreviousGoal,
			[_,1]
			,_
		),
		!
	;
		( lookup_in_clause_meta(cut(always),Clause) ; RelativePosition == last ),
		number_of_solutions_of_goal(Program,BodyASTNode,[_,1],_),
		!
	;
		LastCallOptimizationIsPossible = false
	).



add_information_that_last_call_optimization_is_possible_for_tail_calls_with(PredicateIdentifier,Clause) :-
	clause_implementation(Clause,Implementation),
	rule_body(Implementation,BodyASTNode),
	compound_term(BodyASTNode,',',[_PreviousGoal,LastGoal]),
	compound_term_identifier(LastGoal,PredicateIdentifier),
	!,
	add_to_clause_meta(last_call_optimization_is_possible,Clause),
	add_to_term_meta(last_call_optimization_is_possible,LastGoal).
add_information_that_last_call_optimization_is_possible_for_tail_calls_with(_PredicateIdentifier,_Clause).
		
	


