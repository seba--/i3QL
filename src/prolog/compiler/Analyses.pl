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
	Definition of some general purpose analyses.
	
	@author Michael Eichberg
*/
:- module(
	sae_analyses,
	[	first_primitive_goal/2,
		last_primitive_goals_if_true/3,
		last_primitive_goal_if_false/2,

		number_of_solutions_of_goal/4,		
		number_of_solutions_of_goal/5,
		conjunction_of_number_of_solutions_of_goals/5,
		disjunction_of_number_of_solutions_of_goals/5
	]
).

:- use_module('AST.pl').
:- use_module('Predef.pl').
:- use_module('Utils.pl').



/**
	Given some (compound) goal, the first primitive goal that would be called,
	if this (compound) goal as a whole is called is returned.
	
	@signature first_primitive_goal(ASTNode,FirstGoal_ASTNode)
*/	
first_primitive_goal(ASTNode,FirstGoal) :-
	compound_term(ASTNode,Functor,[LASTNode,_RASTNode]),
	(	
		Functor = ','
	; 
		Functor = ';' 
	;
		Functor = '->'
	; 
		Functor = '*->' 		
	),
	!,
	first_primitive_goal(LASTNode,FirstGoal).
first_primitive_goal(ASTNode,ASTNode).



last_primitive_goals_if_true(ASTNode,SGoals) :-
	last_primitive_goals_if_true(ASTNode,SGoals,[]).

last_primitive_goals_if_true(ASTNode,SGoals,SRest) :-
	compound_term(ASTNode,Functor,[_LASTNode,RASTNode]),
	(
		Functor = ','
	;
		Functor = '->'
	;
		Functor = '*->'
	),
	!,
	last_primitive_goals_if_true(RASTNode,SGoals,SRest).
last_primitive_goals_if_true(ASTNode,SGoals,SRest) :-
	compound_term(ASTNode,';',[LASTNode,RASTNode]),!,
	last_primitive_goals_if_true(LASTNode,SGoals,SFurtherGoals),
	last_primitive_goals_if_true(RASTNode,SFurtherGoals,SRest).	
last_primitive_goals_if_true(ASTNode,[ASTNode|SRest],SRest).



last_primitive_goal_if_false(ASTNode,LastGoal) :-
	compound_term(ASTNode,Functor,[LASTNode,RASTNode]),
	(	
		Functor = ',',
		last_primitive_goal_if_false(LASTNode,LastGoal)
	; 
		Functor = ';',
		last_primitive_goal_if_false(RASTNode,LastGoal)
	),
	!.
last_primitive_goal_if_false(ASTNode,ASTNode).



number_of_solutions_of_goal(Program,ASTNode,Solutions,DidCut) :-
	number_of_solutions_of_goal(Program,none,ASTNode,Solutions,DidCut).

number_of_solutions_of_goal(
		Program,
		PredicateSolutionsAssumption,
		ASTNode,
		Solutions,
		_DidCut
	) :-
	compound_term(ASTNode,',',[LASTNode,RASTNode]),!,
	conjunction_of_number_of_solutions_of_goals(
			Program,PredicateSolutionsAssumption,LASTNode,RASTNode,
			Solutions).
	
number_of_solutions_of_goal(Program,PredicateSolutionsAssumption,ASTNode,Solutions,_DidCut) :-
	compound_term(ASTNode,';',[LASTNode,RASTNode]),!,
	(
		compound_term(LASTNode,'->',[_IFASTNode,ThenASTNode]),!,
		disjunction_of_number_of_solutions_of_goals(Program,PredicateSolutionsAssumption,ThenASTNode,RASTNode,Solutions)
	;
		% the case: "compound_term(LASTNode,'*->',[IFASTNode,ThenASTNode]),!," 
		% is appropriately handled by disjunction_of_number_of_solutions_of_goals...
		disjunction_of_number_of_solutions_of_goals(Program,PredicateSolutionsAssumption,LASTNode,RASTNode,Solutions)
	).
		
number_of_solutions_of_goal(Program,PredicateSolutionsAssumption,ASTNode,[0,UB],_DidCut) :-
	compound_term(ASTNode,'->',[_LASTNode,RASTNode]),!,
	number_of_solutions_of_goal(Program,PredicateSolutionsAssumption,RASTNode,[_LB,UB],_).
	
number_of_solutions_of_goal(Program,PredicateSolutionsAssumption,ASTNode,Solutions,_DidCut) :-
	compound_term(ASTNode,'*->',[LASTNode,RASTNode]),!,
	conjunction_of_number_of_solutions_of_goals(Program,PredicateSolutionsAssumption,LASTNode,RASTNode,Solutions).

number_of_solutions_of_goal(Program,PredicateSolutionsAssumption,ASTNode,Solutions,DidCut) :-
	(
		compound_term_identifier(ASTNode,Functor/Arity),!
	;
		string_atom(ASTNode,Functor),Arity = 0
	),
	(
		PredicateSolutionsAssumption = assumption(Functor/Arity,solutions(Solutions)),
		!
	;	
		Functor = '!',
		Arity == 0, 
		Solutions = [1,1],
		DidCut = yes,
		!
	;	
		lookup_predicate(Functor/Arity,Program,Predicate), % lookup may fail
		lookup_in_predicate_meta(solutions(Solutions),Predicate), % lookup may fail
		!
	;
		Solutions = [0,'*']
	).



conjunction_of_number_of_solutions_of_goals(
		Program,PredicateSolutionsAssumption,LASTNode,RASTNode,
		[LB,UB]
	) :-	
	number_of_solutions_of_goal(
			Program,PredicateSolutionsAssumption,LASTNode,
			[LLB,LUB],_DidCut), % [Left Lower Bound, Left Upper Bound]
	number_of_solutions_of_goal(
			Program,PredicateSolutionsAssumption,RASTNode,
			[RLB,RUB],RDidCut),
	(
		RDidCut == yes ->
		LB is min(LLB,RLB),
		UB = 1
	;	
		LB is min(LLB,RLB),
		(	
			(RUB == 0 ; LUB == 0),!,UB = 0
		;
			(LUB == '*' ; RUB == '*'),!,UB = '*'
		;
			UB = 1
		)
	).



disjunction_of_number_of_solutions_of_goals(
		Program,PredicateSolutionsAssumption,LASTNode,RASTNode, % <= In
		[LB,UB] % <= Out
	) :-	
	number_of_solutions_of_goal(
			Program,PredicateSolutionsAssumption,LASTNode,
			[LLB,LUB],_LDidCut), % [Left Lower Bound, Left Upper Bound]
	number_of_solutions_of_goal(
			Program,PredicateSolutionsAssumption,RASTNode,
			[RLB,RUB],_RDidCut),
	LB is max(LLB,RLB),
	(	(LUB == '*' ; RUB == '*') ->
		UB = '*'
	;
		UB is max(LUB,RUB)
	).
