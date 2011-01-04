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
	This analysis identifies clauses for which we can apply last call 
	optimizations. I.e., this analysis identifies predicates that are tail 
	recursive, and where we can statically decide that – when the tail recursive
	goal is reached – there a no more choice points. 
		
	@author Michael Eichberg
*/
:- module(
	'SAEProlog:Compiler:Phase:Analysis:LastCallOptimizationAnalysis',
	[pl_last_call_optimization_analysis/4]
).

:- use_module('../AST.pl').
:- use_module('../Debug.pl').
:- use_module('../Predef.pl').
:- use_module('../Utils.pl').
:- use_module('../Analyses.pl').



pl_last_call_optimization_analysis(DebugConfig,Program,_OutputFolder,Program) :-
	debug_message(DebugConfig,on_entry,
	write('\n[Debug] Phase: Last Call Optimization Analysis______________________________\n')),
	foreach_user_predicate(Program,process_predicate(Program)).



process_predicate(Program,Predicate) :-
	predicate_clauses(Predicate,Clauses),
	foreach_clause(Clauses,check_clause_for_tail_recursion(Program,Predicate),_).
	
	
% Currently, we only identify very simple cases where we have a unique last
% goal and which is a conjunction with its previous goals (we rely on the
% property that the goal sequence is left descending (done during loading)!) 
% x(...) :- a,b,c,x(...)
check_clause_for_tail_recursion(Program,Predicate,_ClauseId,Clause,RelativePosition,_) :-	
	predicate_identifier(Predicate,PredicateIdentifier),
	clause_implementation(Clause,Implementation),
	rule_body(Implementation,BodyASTNode),
	(
		% TODO support last call optimization for cases such as: 
		% x(...) :- a,!,x(...);b,x(...)... 
		% [this is the way to go for a more decent analysis:] 
		% last_primitive_goals_if_true(BodyASTNode,[LastGoal],[]),...
		complex_term(BodyASTNode,',',[PreviousGoal,LastGoal]),
		complex_term_identifier(LastGoal,PredicateIdentifier), % ... we have found a recursive call
		(	% ... is it a candidate for a last call optimization?
			RelativePosition == last ; 
			lookup_in_meta(cut(always),Clause)
		),
		number_of_solutions_of_goal(Program,PreviousGoal,[_LB,1])
		->
		add_to_clause_meta(last_call_optimization_is_possible,Clause),
		add_flag_to_predicate_meta(has_clauses_where_last_call_optimization_is_possible,Predicate),
		add_to_term_meta(last_call_optimization_is_possible,LastGoal)
	;
		% the clause does not have a unique last goal or 
		% the unique last goal is not a recursive call or
		% a successor clause exists (hence, we still have a choice point...)
		true 
	).



number_of_solutions_of_goal(Program,ASTNode,Solutions) :-
	complex_term(ASTNode,',',[LASTNode,RASTNode]),!,
	conjunction_of_number_of_solutions_of_goals(Program,LASTNode,RASTNode,Solutions).
	
number_of_solutions_of_goal(Program,ASTNode,Solutions) :-
	complex_term(ASTNode,';',[LASTNode,RASTNode]),!,
	(
		complex_term(LASTNode,'->',[_IFASTNode,ThenASTNode]),!,
		disjunction_of_number_of_solutions_of_goals(Program,ThenASTNode,RASTNode,Solutions)
	;
		% the case: "complex_term(LASTNode,'*->',[IFASTNode,ThenASTNode]),!," 
		% is appropriately handled by disjunction_of_number_of_solutions_of_goals...
		disjunction_of_number_of_solutions_of_goals(Program,LASTNode,RASTNode,Solutions)
	).
		
number_of_solutions_of_goal(Program,ASTNode,[0,UB]) :-
	complex_term(ASTNode,'->',[_LASTNode,RASTNode]),!,
	number_of_solutions_of_goal(Program,RASTNode,[_LB,UB]).
	
number_of_solutions_of_goal(Program,ASTNode,Solutions) :-
	complex_term(ASTNode,'*->',[LASTNode,RASTNode]),!,
	conjunction_of_number_of_solutions_of_goals(Program,LASTNode,RASTNode,Solutions).

number_of_solutions_of_goal(Program,ASTNode,Solutions) :-
	complex_term(ASTNode,'*->',[LASTNode,RASTNode]),!,
	conjunction_of_number_of_solutions_of_goals(Program,LASTNode,RASTNode,Solutions).

number_of_solutions_of_goal(Program,ASTNode,Solutions) :-
	(
		complex_term_identifier(ASTNode,Functor/Arity),!
	;
		string_atom(ASTNode,Functor),Arity = 0
	),
	(
		lookup_predicate(Functor/Arity,Program,Predicate), % lookup may fail
		lookup_in_predicate_meta(solutions(Solutions),Predicate),! % lookup may fail
	;
		Solutions = [0,'*']
	).



conjunction_of_number_of_solutions_of_goals(Program,LASTNode,RASTNode,[LB,UB]) :-	
	number_of_solutions_of_goal(Program,LASTNode,[LLB,LUB]), % [Left Lower Bound, Left Upper Bound]
	number_of_solutions_of_goal(Program,RASTNode,[RLB,RUB]),
	LB is min(LLB,RLB),
	(	
		(RUB == 0 ; LUB == 0),!,UB = 0
	;
		(LUB == '*' ; RUB == '*'),!,UB = '*'
	;
		UB = 1
	).



disjunction_of_number_of_solutions_of_goals(Program,LASTNode,RASTNode,[LB,UB]) :-	
	number_of_solutions_of_goal(Program,LASTNode,[LLB,LUB]), % [Left Lower Bound, Left Upper Bound]
	number_of_solutions_of_goal(Program,RASTNode,[RLB,RUB]),
	LB is max(LLB,RLB),
	(	(LUB == '*' ; RUB == '*') ->
		UB = '*'
	;
		UB is max(LUB,RUB)
	).
