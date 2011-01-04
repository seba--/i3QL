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
	predicate_identifier(Predicate,PredicateIdentifier),
	foreach_clause(Clauses,check_for_tail_recursion(Program,Predicate),_).
	
	
	
check_for_tail_recursion(Program,Predicate,Id,Clause,RelativePosition,_) :-	
	predicate_identifier(Predicate,Functor/Arity),
	clause_implementation(Clause,Implementation),
	rule_body(Implementation,BodyASTNode),
	(
		last_primitive_goals_if_true(BodyASTNode,[LastGoal],[]),
		complex_term(LastGoal,Functor,Args),
		length(Args,Arity), % ... we have found a recursive call
		(	% is it a candidate for a last call optimization?
			RelativePosition == last ; 
			clause_meta(Clause,Meta), lookup_in_meta(cut(always),Meta)
		)
		->
		write_atomic_list(['the ',Id,'. clause of ',Functor,'/',Arity,' is tail recursive\n'])
	;
		% the clause does not have a unique last goal or 
		% the unique last goal is not a recursive call or
		% a successor clause exists (hence, we still have a choice point...)
		true 
	).



number_of_potential_choice_points_of_clause(ASTNode,ChoicePoints) :-
	complex_term(ASTNode,Functor,[]).
