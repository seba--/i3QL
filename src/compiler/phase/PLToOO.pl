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

/*
	Generates the code for the SAE program.

	@author Michael Eichberg
*/
:- module('SAEProlog:Compiler:Phase:PhasePLtoOO',[pl_to_oo/4]).

:- use_module('../AST.pl').
:- use_module('../Predef.pl').
:- use_module('../Utils.pl').
:- use_module('../Debug.pl').
:- use_module('../Analyses.pl').


/**
	Encodes an SAE Prolog program using a small object-oriented language. The 
	AST of the target language is created.
	
	<h1>AST NODES</h1>
	<h2>TOP LEVEL NODES</h2>
	class_decl(PredicateIdentifier,ExtendedClasses,ImplementedInterfaces,ClassMembers) - ClassMembers is a list of SAEOO AST nodes 
	predicate_registration(PredicateIdentifier) - PredicateIdentifier = Functor/Arity

	<h2>CLASS MEMBERS</h2>
	eol_comment(Comment)
	field_decl(goal_stack) - create a field to manage the clause local goal stack(s) (a goal stack is not always required...)
	field_decl(Modifiers,Type,Name)
	field_decl(Modifiers,Type,Name,Expression) - Modifiers is a list of modifiers. Currently, the only allowed/supported modifier is final.)
	constructor_decl(PredicateIdentifier,ParameterDecls,Statements) 
	method_decl(Visibility,ReturnType,Identifier,ParameterDecls,Statements) - Visibility is either public or private

	<h2>STATEMENTS</h2>
	create_undo_goal_and_put_on_goal_stack(TermExpressions) - TermExpressions is a list of expressions where each expression has type term
	clear_goal_stack
	abort_pending_goals_and_clear_goal_stack
	push_onto_goal_stack(GoalExpression)
	remove_top_level_goal_from_goal_stack
	abort_and_remove_top_level_goal_from_goal_stack
	forever(Label,Statements)
	continue(Label)
	eol_comment(Comment)
	switch(SwitchContext,Expression,CaseStatements) - SwitchContext is either "top_level" or "inside_forever"
	expression_statement(Expression)
	manifest_state(ReceiverExpression,Assignee) - ReceiverExpression must be of type "term"
	reincarnate_state(ReceiverExpression) - ReceiverExpression must be of type "state"
	return(Expression)
	local_variable_decl(Type,Name,Expression)
	if(Condition,Statements)
	if(Condition,ThenStatements,ElseStatements)
	error(ErrorDescription) - to signal an programmer's error (e.g., if the developer tries to evaluate a non-arithmetic term.)
	
	<h2>EXPRESSIONS</h2>
	get_top_element_from_goal_stack 
	assignment(LValue,Expression)
	method_call(ReceiverExpression,Identifier,Expressions)
	new_object(Type,Expressions) - Expressions is a list of expressions; for each constructor argument an expression has to be given.
	field_ref(Receiver,Identifier)
	local_variable_ref(Identifier)
	reference_comparison(Expression,Expression)
	null
	self - the self reference ("this" in Java)
	string(Value) - a string value in the target language (not a string atom)
	int(Value) - an int value in the target language (not a Prolog int value)
	boolean(Value) - a boolean value in the target language (not a Prolog boolean value)
	unify(Term1Expression,Term2Expression) - base unification without state manifestation
	call_term(TermExpression)
	static_predicate_call(complex_term(Functor,Terms))
	predicate_lookup(Functor,Arity,TermExpressions)
	arithmetic_comparison(Operator,LeftArithmeticTerm,RightArithmeticTerm)
	arithmetic_evaluation(ArithmeticTerm)
	
	<h2>TERM EXPRESSION</H2>
	string_atom(Value)
	int_value(Value)
	float_value(Value)
	variable
	complex_term(Functor,Terms) - Functor is a string atom and Terms is a list of term expressions (string atoms, int values, float values, variables of compound terms)
	

	<h2>LValue</h2>
	field_ref(Receiver,Identifier)
	local_variable_ref(Identifier)	

	<h2>OTHER</h2>
	param_decl(Type,Name) - Parameters are always considered to be final
	case(ConstantExpression,Statements)
	eol_comment(Comment)
	multiline_comment(Comment)
	
	<h1>TYPES</h1>
	type(void)
	type(int)
	type(boolean)
	type(goal)
	type(goal(PredicateIdentifier))
	type(term)
	type(complex_term)
	type(complex_term(TermIdentifier)) - TermIdentifier = Functor,Arity
	type(atomic(string_atom))
	type(atomic(int_value))
	type(atomic(float_value))
	type(variable) - the type used by Prolog variables
	@param Debug the list of debug information that should be printed.	
*/
pl_to_oo(DebugConfig,Program,_OutputFolder,Program) :-
	debug_message(DebugConfig,on_entry,write('\n[Debug] Phase: Generate the OO Representation_______________________________\n')),
	foreach_user_predicate(Program,process_predicate(DebugConfig,Program)).



process_predicate(DebugConfig,Program,Predicate) :-
	predicate_identifier(Predicate,PredicateIdentifier),
	term_to_atom(PredicateIdentifier,PredicateIdentifierAtom),
	debug_message(DebugConfig,processing_predicate,write_atomic_list(['[Debug] Processing Predicate: ',PredicateIdentifierAtom,'\n'])),
	% build the OO AST
	% METHODS
	SMethods = [SConstructor,SAbortMethod,SChoiceCommittedMethod,SClauseSelectorMethod|S4],
	gen_predicate_constructor(Program,Predicate,SConstructor),
	gen_abort_method(Program,Predicate,SAbortMethod),
	gen_choice_committed_method(Program,Predicate,SChoiceCommittedMethod),
	gen_clause_selector_method(Program,Predicate,SClauseSelectorMethod),
	gen_clause_impl_methods(DeferredActions,Program,Predicate,S4),
	% FIELDS
	gen_fields_for_the_control_flow_and_evaluation_state(DeferredActions,Program,Predicate,S1,S2),
	gen_fields_for_predicate_arguments(Program,Predicate,S2,S3),
	gen_fields_for_clause_local_variables(Predicate,S3,SMethods),
	OOAST = oo_ast([
		class_decl(PredicateIdentifier,type(goal),S1),
		predicate_registration(PredicateIdentifier)
		]),
	predicate_meta(Predicate,Meta),
	add_to_meta(OOAST,Meta).	



/*

	F I E L D S

*/

gen_fields_for_the_control_flow_and_evaluation_state(DeferredActions,_Program,Predicate,SFieldDecls,SR) :-
	predicate_meta(Predicate,PredicateMeta),
	predicate_clauses(Predicate,Clauses),
	SFieldDecls = [
		eol_comment('variables to control/manage the execution this predicate')|
		SClauseToExecute
	],
	(	single_clause(Clauses) ->
		SClauseToExecute = SCutEvaluation
	;
		SClauseToExecute = [field_decl([],type(int),'clauseToExecute',int(0))|SCutEvaluation]
	),	
	(	lookup_in_meta(cut(never),PredicateMeta) ->
		SCutEvaluation = SGoalsEvaluation
	;
		SCutEvaluation = [field_decl([],type(boolean),'cutEvaluation',boolean('false'))|SGoalsEvaluation]
	),
	SGoalsEvaluation = [
		field_decl([],type(int),'goalToExecute',int(0)),
		field_decl(goal_stack) |
		SGoalPredescessors
	],
	% For each goal, that is the successor of some or goal, we have to create a 
	% a variable that stores which goal was executed previously.
	findall(
		SGoalPredescessor,
		(
			member_ol(create_field_to_store_predecessor_goal(GoalNumber),DeferredActions),
			GoalCaseId is GoalNumber * 2 - 1,
			atomic_list_concat(['goal',GoalCaseId,'PredecessorGoal'],PredecessorGoalFieldIdentifier),
			SGoalPredescessor = field_decl([],type(int),PredecessorGoalFieldIdentifier,int(0))
		),
		SGoalPredescessors,
		SR
	).



gen_fields_for_predicate_arguments(
		_Program,
		Predicate,
		[eol_comment('variables related to the predicate\'s state')|SFieldForArgDecls],
		SR
	) :-
	predicate_identifier(Predicate,PredicateIdentifier),
	PredicateIdentifier = _Functor/Arity,
	(
		lookup_in_predicate_meta(has_clauses_where_last_call_optimization_is_possible,Predicate) ->
		call_foreach_i_in_0_to_u(Arity,field_decl_for_pred_arg_i([]),SFieldForArgDecls,SFieldForArgStateDecls),		
		call_foreach_i_in_0_to_u(Arity,field_decl_for_initial_pred_arg_state_i,SFieldForArgStateDecls,SR)
	;
		call_foreach_i_in_0_to_u(Arity,field_decl_for_pred_arg_i([final]),SFieldForArgDecls,SR)		
	).



field_decl_for_pred_arg_i(Modifiers,I,field_decl(Modifiers,type(term),FieldName)) :-
	atom_concat('arg',I,FieldName).



field_decl_for_initial_pred_arg_state_i(I,field_decl([final],type(state),FieldName)) :-
	atomic_list_concat(['initialArg',I,'state'],FieldName).



gen_fields_for_clause_local_variables(
		Predicate,
		[eol_comment('variables to store clause local information')|SFieldDecls],
		SR
	) :- 
	predicate_meta(Predicate,Meta),
	lookup_in_meta(maximum_number_of_clause_local_variables(Max),Meta),
	call_foreach_i_in_0_to_u(Max,field_decl_for_clause_local_variable,SFieldDecls,SR).



field_decl_for_clause_local_variable(I,field_decl([],type(term),FieldName)) :-
	atom_concat(clv,I,FieldName).



/*

	C O N S T R U C T O R
	
*/
gen_predicate_constructor(_Program,Predicate,constructor_decl(PredicateIdentifier,ParamDecls,SInitFieldsForArgsStmts)) :-
	predicate_identifier(Predicate,PredicateIdentifier),
	PredicateIdentifier = _Functor/Arity,
	call_foreach_i_in_0_to_u(Arity,constructor_param_decl_for_arg_i,ParamDecls),
	call_foreach_i_in_0_to_u(Arity,init_field_of_arg_i,SInitFieldsForArgsStmts,SInitFieldsForInitialArgStatesStmts),
	(	lookup_in_predicate_meta(has_clauses_where_last_call_optimization_is_possible,Predicate) ->
		call_foreach_i_in_0_to_u(Arity,init_field_of_initial_arg_state_i,SInitFieldsForInitialArgStatesStmts)	
	;
		SInitFieldsForInitialArgStatesStmts = []
	).
	

constructor_param_decl_for_arg_i(I,param_decl(type(term),ParamName)) :- 
	atom_concat('arg',I,ParamName).
	
	
init_field_of_arg_i(
		I,
		expression_statement(
			assignment(
				field_ref(self,ArgName),
				local_variable_ref(ArgName)))) :-
	atom_concat('arg',I,ArgName).



init_field_of_initial_arg_state_i(
		I,
		manifest_state(
			local_variable_ref(ArgName),
			field_ref(self,FieldName))) :-
	atom_concat('arg',I,ArgName),
	atomic_list_concat(['initialArg',I,'state'],FieldName).



/*

	"void abort()" M E T H O D

*/	
gen_abort_method(_Program,Predicate,AbortMethod) :-
	predicate_identifier(Predicate,_Functor/Arity),
	(	lookup_in_predicate_meta(has_clauses_where_last_call_optimization_is_possible,Predicate) ->
		call_foreach_i_in_0_to_u(Arity,reincarnate_initial_arg_state_i,Stmts,SGoalStack)	
	;
		Stmts = SGoalStack
 	),
	SGoalStack = [abort_pending_goals_and_clear_goal_stack],
	AbortMethod = 
		method_decl(
			public,
			type(void),
			'abort',
			[],
			Stmts).



reincarnate_initial_arg_state_i(
		I,
		reincarnate_state(field_ref(self,FieldName))) :-
	atomic_list_concat(['initialArg',I,'state'],FieldName).



/*

	"boolean choiceCommitted()" M E T H O D

*/	
gen_choice_committed_method(_Program,_Predicate,ChoiceCommittedMethod) :-
	ChoiceCommittedMethod = 
		method_decl(
			public,
			type(boolean),
			'choiceCommitted',
			[],
			[return(boolean(false))]).



/*

	"boolean next()" M E T H O D      ( T H E   C L A U S E   S E L E C T O R )

*/	
gen_clause_selector_method(_Program,Predicate,ClauseSelectorMethod) :-
	predicate_clauses(Predicate,Clauses),
	(	lookup_in_predicate_meta(has_clauses_where_last_call_optimization_is_possible,Predicate) ->
		SMain = [forever('eval_clauses',SEvalGoals)],
		SwitchContext = inside_forever
	;
		SMain = SEvalGoals,
		SwitchContext = top_level
	),
	(	single_clause(Clauses) ->
		SEvalGoals = [return(method_call(self,'clause0',[]))],
		ClauseSelectorMethod = 
			method_decl(
				public,
				type(boolean),
				'next',
				[],
				SMain)
	;
		SEvalGoals = [switch(SwitchContext,field_ref(self,'clauseToExecute'),CaseStmts)],
		foreach_clause(Clauses,selector_for_clause_i(Predicate),CaseStmts),
		ClauseSelectorMethod = 
			method_decl(
				public,
				type(boolean),
				'next',
				[],
				SMain)
	).



selector_for_clause_i(_Predicate,I,Clause,last,case(int(I),Stmts)) :- !,
	atom_concat('clause',I,ClauseIdentifier),
	(	lookup_in_clause_meta(last_call_optimization_is_possible,Clause) ->
		reset_clause_local_variables(Clause,SCleanup,SPrepareForNextClause),
		SPrepareForNextClause = [
			expression_statement(assignment(field_ref(self,'goalToExecute'),int(0))),
			expression_statement(assignment(field_ref(self,'clauseToExecute'),int(0))),
			continue('eval_clauses')
		],
		Stmts = [
			eol_comment('tail recursive clause with last call optimization'),
			if(
				method_call(self,ClauseIdentifier,[]),
				SCleanup
			),
			expression_statement(method_call(self,'abort',[])),
			return(boolean(false))
		]
	;
		% if this is the last clause, we don't care if the evaluation was "cutted" or not
		Stmts = [return(method_call(self,ClauseIdentifier,[]))]
	).
	
selector_for_clause_i(Predicate,I,Clause,_ClausePosition,case(int(I),Stmts)) :-
	atom_concat('clause',I,ClauseIdentifier),
	NextClauseId is I + 1,
	
	reset_clause_local_variables(Clause,SCleanup,SPrepareForNextClause),
	(
		lookup_in_clause_meta(cut(never),Clause) ->
		ClauseFailed = [eol_comment('this clause contains no "cut"') |SCleanup],
		SPrepareForNextClause = [
			eol_comment('prepare the execution of the next clause'),
			expression_statement(assignment(field_ref(self,'goalToExecute'),int(0))),
			expression_statement(assignment(field_ref(self,'clauseToExecute'),int(NextClauseId)))
		]
	;
		(	lookup_in_predicate_meta(has_clauses_where_last_call_optimization_is_possible,Predicate) ->
			Return = [ 
				expression_statement(method_call(self,'abort',[])),
				return(boolean(false))
			]
		;
			Return = [return(boolean(false))]
		),
		SPrepareForNextClause = [
			eol_comment('prepare the execution of the next clause'),
			expression_statement(assignment(field_ref(self,'goalToExecute'),int(0))),
			expression_statement(assignment(field_ref(self,'clauseToExecute'),int(NextClauseId)))
		],
		ClauseFailed = [
			if(field_ref(self,'cutEvaluation'),
				Return
			) | 
			SCleanup
		]
	),
	(	lookup_in_clause_meta(last_call_optimization_is_possible,Clause) ->
		SPrepareForNextClause = [
			expression_statement(assignment(field_ref(self,'goalToExecute'),int(0))),
			expression_statement(assignment(field_ref(self,'clauseToExecute'),int(0))),
			continue('eval_clauses')
		],
		Stmts = [
			eol_comment('tail recursive clause with last call optimization'),
			if(
				method_call(self,ClauseIdentifier,[]),
				SCleanup
			),
			ClauseFailed
		]
	;	
		Stmts = [
			if(method_call(self,ClauseIdentifier,[]),
				[return(boolean(true))]
			) |
			ClauseFailed
		]
	).



reset_clause_local_variables(
		Clause,
		SCLVReset,
		SR
	) :- 
	clause_meta(Clause,Meta),
	lookup_in_meta(clause_local_variables_count(CLVCount),Meta),
	call_foreach_i_in_0_to_u(CLVCount,reset_clause_local_variable,SCLVReset,SR).



reset_clause_local_variable(
		I,
		expression_statement(assignment(field_ref(self,FieldName),null))
	) :-
	atom_concat(clv,I,FieldName).



/*

	"boolean clauseX()" M E T H O D S      ( T H E   C L A U S E  I M P L E M E N T A T I O N S )

*/
gen_clause_impl_methods(DeferredActions,_Program,Predicate,ClauseImpls) :-
	predicate_clauses(Predicate,Clauses),
	foreach_clause(Clauses,implementation_for_clause_i(DeferredActions),ClauseImpls).
	
implementation_for_clause_i(DeferredActions,I,Clause,_ClausePosition,ClauseMethod) :-
	atom_concat('clause',I,ClauseIdentifier),
	clause_definition(Clause,ClauseDefinition),
	rule_body(ClauseDefinition,Body),
	number_primitive_goals(Body,0,LastId),
	set_primitive_goals_successors(DeferredActions,Body),
	primitive_goals_list(Body,PrimitiveGoalsList,[]),
	translate_goals(PrimitiveGoalsList,Cases,[]),
	(	LastId == 1 ->
		MethodBody = [switch(top_level,field_ref(self,'goalToExecute'),Cases)]
	;
		MethodBody = [ 
			forever(
				'eval_goals',
				[switch(inside_forever,field_ref(self,'goalToExecute'),Cases)]
			) 
		]
	),
	ClauseMethod = method_decl(
			private,
			type(boolean),
			ClauseIdentifier,
			[],
			MethodBody
		).



translate_goals([PrimitiveGoal|PrimitiveGoals],SGoalCases,SRest) :-
	translate_goal(PrimitiveGoal,SGoalCases,SOtherGoalCases),
	translate_goals(PrimitiveGoals,SOtherGoalCases,SRest).
translate_goals([],SCases,SCases).



% To translate a goal, we use the following meta information:
% next_goal_if_fails(Action,GoalNumber) - Action is either "redo" (in case of backtracking) or "call" (if we try an new alternative)
% next_goal_if_fails(Action,multiple) - Action must be "redo"
% not_unique_predecessor_goal
% next_goal_if_succeeds(GoalNumber)
translate_goal(PrimitiveGoal,[SCall,SRedo|SCases],SCases) :-
	string_atom(PrimitiveGoal,'!'),!,
	term_meta(PrimitiveGoal,Meta),
	lookup_in_meta(goal_number(GoalNumber),Meta),
	% Handle the case if the cut is called the first time
	goal_call_case_id(GoalNumber,CallCaseId),
	select_and_jump_to_next_goal_after_succeed(Meta,force_jump,JumpToNextGoalAfterSucceed),
	SCall = case(
		int(CallCaseId),
		[
			eol_comment('cut...'),
			expression_statement(assignment(field_ref(self,'cutEvaluation'),boolean(true))) |
			JumpToNextGoalAfterSucceed
		]
	),
	% Handle the case if the cut is called the second time (redo-case)
	goal_redo_case_id(GoalNumber,RedoCaseId),
	SRedo = case(
		int(RedoCaseId),
		[
			abort_pending_goals_and_clear_goal_stack,
			return(boolean(false))
		]
	).

translate_goal(PrimitiveGoal,[SCallCase,SRedoCase|SCases],SCases) :-
	complex_term(PrimitiveGoal,'=',[LASTNode,RASTNode]),!,
	term_meta(PrimitiveGoal,Meta),
	lookup_in_meta(goal_number(GoalNumber),Meta),
	% Handle the case if the comparison is called the first time
	%%%%%%%%%%% akjdfkjasdföklajsdfölkasdjfölkasdjfölkasdjfölkasdjfölkasdjfölkasdjfölksdjfölksdjfaölksdfjöalskdjf
	goal_call_case_id(GoalNumber,CallCaseId),
	select_and_jump_to_next_goal_after_succeed(Meta,force_jump,JumpToNextGoalAfterSucceed),
	create_term(LASTNode,LTermConstructor,LMappedVariableNames),
	create_term(RASTNode,RTermConstructor,RMappedVariableNames),
	merge_sets(LMappedVariableNames,RMappedVariableNames,MappedVariableNames),
	SEval = [
		if(unify(LTermConstructor,RTermConstructor),
			JumpToNextGoalAfterSucceed
		)
	],
	(
		lookup_in_meta(variables_used_for_the_first_time(VariablesUsedForTheFirstTime),Meta),
		lookup_in_meta(potentially_used_variables(VariablesPotentiallyPreviouslyUsed),Meta) ->
		init_clause_local_variables(
			VariablesUsedForTheFirstTime,
			VariablesPotentiallyPreviouslyUsed,
			MappedVariableNames,
			SInitCLVs,
			SSaveState
		),
% write(used),write(MappedVariableNames),nl,
% write(used_for_the_first_time),write(VariablesUsedForTheFirstTime),nl,
% write(potentially_used_variables),write(VariablesPotentiallyPreviouslyUsed),nl,
		(	remove_from_set(arg(_),VariablesUsedForTheFirstTime,CLVariablesUsedForTheFirstTime),
			set_subtract(MappedVariableNames,CLVariablesUsedForTheFirstTime,VariablesThatNeedToBeSaved),
			not_empty(VariablesThatNeedToBeSaved) ->
			save_state_in_undo_goal(VariablesThatNeedToBeSaved,SSaveState,SEval),
			RedoAction = [
				abort_and_remove_top_level_goal_from_goal_stack |
				JumpToNextGoalAfterFail
			]
		;
			SSaveState = SEval,
			RedoAction = JumpToNextGoalAfterFail
		)
	;
		%SInitCLVs = SEval
		throw(internal_error(translate_goal/3))
	),
	SCallCase = case(
		int(CallCaseId),
		SInitCLVs
	),
	% (redo-case)
	goal_redo_case_id(GoalNumber,RedoCaseId),
	select_and_jump_to_next_goal_after_fail(Meta,JumpToNextGoalAfterFail,[]),
	SRedoCase = case(
		int(RedoCaseId),
		RedoAction
	).


translate_goal(PrimitiveGoal,[SCall,SRedo|SCases],SCases) :-
	complex_term(PrimitiveGoal,Operator,[LASTNode,RASTNode]),
	is_arithmetic_comparison_operator(Operator),
	!,
	term_meta(PrimitiveGoal,Meta),
	lookup_in_meta(goal_number(GoalNumber),Meta),
	% Handle the case if the comparison is called the first time
	goal_call_case_id(GoalNumber,CallCaseId),
	select_and_jump_to_next_goal_after_succeed(Meta,force_jump,JumpToNextGoalAfterSucceed),
	create_term(LASTNode,LTermConstructor,_LMappedVariableNames),
	create_term(RASTNode,RTermConstructor,_RMappedVariableNames),
	SCall = case(
		int(CallCaseId),
		[
			if(arithmetic_comparison(Operator,LTermConstructor,RTermConstructor),
				JumpToNextGoalAfterSucceed
			)
		]
	),
	% (redo-case)
	goal_redo_case_id(GoalNumber,RedoCaseId),
	select_and_jump_to_next_goal_after_fail(Meta,JumpToNextGoalAfterFail,[]),
	SRedo = case(
		int(RedoCaseId),
		JumpToNextGoalAfterFail
	).

translate_goal(PrimitiveGoal,[SCall,SRedo|SCases],SCases) :-
	% implement the case that the result of is is assigned to a new variable..
	complex_term(PrimitiveGoal,'is',[LASTNode,RASTNode]),
	term_meta(PrimitiveGoal,Meta),
	lookup_in_meta(variables_used_for_the_first_time(NewVariables),Meta),
	lookup_in_term_meta(mapped_variable_name(Name),LASTNode),
	memberchk(Name,NewVariables),!,
	% Handle the case if "is" is called the first time
	lookup_in_meta(goal_number(GoalNumber),Meta),
	mapped_variable_name_to_variable_identifier(Name,VariableIdentifier),
%write(VariableIdentifier),nl,	
	goal_call_case_id(GoalNumber,CallCaseId),
	select_and_jump_to_next_goal_after_succeed(Meta,force_jump,JumpToNextGoalAfterSucceed),
	create_term(RASTNode,RTermConstructor,_RMappedVariableNames),
	SCall = case(
		int(CallCaseId),
		[
			expression_statement(
				assignment(
					local_variable_ref(VariableIdentifier),
					int_value_expr(arithmetic_evluation(RTermConstructor)))) |
			JumpToNextGoalAfterSucceed
		]
	),
	% (redo-case)
	goal_redo_case_id(GoalNumber,RedoCaseId),
	select_and_jump_to_next_goal_after_fail(Meta,JumpToNextGoalAfterFail,[]),
	SRedo = case(
		int(RedoCaseId),
		JumpToNextGoalAfterFail
	).	

% tail recursive call
translate_goal(PrimitiveGoal,[SGoalCall|SCases],SCases) :-
	term_meta(PrimitiveGoal,Meta),
	lookup_in_meta(last_call_optimization_is_possible,Meta),!,
	lookup_in_meta(goal_number(GoalNumber),Meta),
	goal_call_case_id(GoalNumber,CallCaseId),
	SCaseHead = [eol_comment('tail call with last call optimization')|SUpdatePredArgs],
	(	complex_term_args(PrimitiveGoal,Args) ->
		update_predicate_arguments(0,Args,SUpdatePredArgs,SRecursiveCall)
	;	% if the predicate does not have arguments... (e.g., repeat)	
		SUpdatePredArgs = SRecursiveCall
	),
	SRecursiveCall = [
		clear_goal_stack,
		return(boolean(true))
	],
	SGoalCall = case(
		int(CallCaseId),
		SCaseHead
	).

% This implements the base case... if no special support for a given predicate
% is provided by the compiler
translate_goal(PrimitiveGoal,[SCallCase,SRedoCase|SCases],SCases) :-
	term_meta(PrimitiveGoal,Meta),
	lookup_in_meta(goal_number(GoalNumber),Meta),
	
	% GOAL "call-case"
	goal_call_case_id(GoalNumber,CallCaseId),
	create_term(PrimitiveGoal,TermConstructor,MappedVariableNames),
	(
		lookup_in_meta(variables_used_for_the_first_time(NewVariables),Meta),
		lookup_in_meta(potentially_used_variables(PotentiallyUsedVariables),Meta) ->
		init_clause_local_variables(
			NewVariables,
			PotentiallyUsedVariables,
			MappedVariableNames,
			SInitCLVs,
			[ push_onto_goal_stack(static_predicate_call(TermConstructor)) ]
		)
	;
		SInitCLVs = [ push_onto_goal_stack(static_predicate_call(TermConstructor)) ]
	),
	SCallCase = case(
		int(CallCaseId),
		SInitCLVs
	),
	
	% GOAL "redo-case"
	CallGoal = [
		local_variable_decl(type(boolean),'succeeded',
			method_call(get_top_element_from_goal_stack,'next',[])
		),
		if(not(local_variable_ref('succeeded')),[
			remove_top_level_goal_from_goal_stack |
			JumpToNextGoalAfterFail
		])|
		JumpToNextGoalAfterSucceed
	],
	select_and_jump_to_next_goal_after_fail(Meta,JumpToNextGoalAfterFail,[]),
	select_and_jump_to_next_goal_after_succeed(Meta,may_fall_through,JumpToNextGoalAfterSucceed),
	goal_redo_case_id(GoalNumber,RedoCaseId),
	SRedoCase = case(
		int(RedoCaseId),
		CallGoal
	).



/**
	Generates the code that selects and executes the next goal if this goal
	has failed.
*/
select_and_jump_to_next_goal_after_fail(Meta,SStmts,SRest) :-
	lookup_in_meta(next_goal_if_fails(redo,multiple),Meta),!,
	lookup_in_meta(goal_number(GoalNumber),Meta),
	goal_call_case_id(GoalNumber,CallCaseId),
	atomic_list_concat(['goal',CallCaseId,'PredecessorGoal'],PredecessorGoal),
	SStmts = [
		expression_statement(assignment(field_ref(self,'goalToExecute'),field_ref(self,PredecessorGoal))),
		continue('eval_goals')
		|SRest
	].
select_and_jump_to_next_goal_after_fail(Meta,SStmts,SRest) :-
	lookup_in_meta(next_goal_if_fails(Action,TargetGoalNumber),Meta),!,
	(	Action == redo ->
		goal_redo_case_id(TargetGoalNumber,TargetCaseId)
	;	% Action == call
		goal_call_case_id(TargetGoalNumber,TargetCaseId)
	),
	SStmts = [
		expression_statement(assignment(field_ref(self,'goalToExecute'),int(TargetCaseId))),
		continue('eval_goals')
		|SRest
	].
% ... if this goal fails, the goal as a whole fails	
select_and_jump_to_next_goal_after_fail(_Meta,[return(boolean(false))|SRest],SRest).



select_and_jump_to_next_goal_after_succeed(Meta,ForceJump,JumpToNextGoalAfterSucceed) :-
	lookup_in_meta(goal_number(GoalNumber),Meta),
	(	lookup_in_meta(next_goal_if_succeeds(TargetGoalNumber),Meta) ->
		goal_call_case_id(TargetGoalNumber,TargetCallCaseId),
		(
			lookup_in_meta(not_unique_predecessor_goal,Meta) ->
			goal_redo_case_id(GoalNumber,ThisGoalRedoCaseId),
			atomic_list_concat(['goal',TargetCallCaseId,'PredecessorGoal'],PredecessorGoal),
			JumpToNextGoalAfterSucceed = [
				expression_statement(assignment(field_ref(self,PredecessorGoal),int(ThisGoalRedoCaseId))) | 
				SelectAndJump
			]
		;
			JumpToNextGoalAfterSucceed = SelectAndJump
		),
		(	TargetGoalNumber =:= GoalNumber + 1, ForceJump \= force_jump  ->
			atom_concat('fall through ... ',TargetCallCaseId,NextGoalIfSucceedComment),
			SelectAndJump = [eol_comment(NextGoalIfSucceedComment)]
		;
			SelectAndJump = [
				expression_statement(assignment(field_ref(self,'goalToExecute'),int(TargetCallCaseId))),
				continue('eval_goals')
			]
		)
	;
		% this is (a) last goal of the clause
		goal_redo_case_id(GoalNumber,ThisGoalRedoCaseId),
		JumpToNextGoalAfterSucceed = [
			expression_statement(assignment(field_ref(self,'goalToExecute'),int(ThisGoalRedoCaseId))),
			return(boolean(true))
		]
	).



/**
	@signature init_clause_local_variables(VariablesUsedForTheFirstTime,VariablesPotentiallyUsedBefore,BodyVariableNames,SInitClauseLocalVariables,SZ).
*/
init_clause_local_variables(
		NewVariables,
		PotentiallyUsedVariables,
		[MappedBodyVariableName|MappedBodyVariableNames],
		SInitCLV,
		SZ
	) :-
	(	MappedBodyVariableName = clv(_I), % may fail
		mapped_variable_name_to_variable_identifier(MappedBodyVariableName,VI),
		(
			memberchk(MappedBodyVariableName,NewVariables),
			term_to_atom(NewVariables,NewVariablesAtom),
			SInitCLV = [
				eol_comment(NewVariablesAtom),
				expression_statement(assignment(field_ref(self,VI),variable))
				|SI
			]
		;	
			memberchk(MappedBodyVariableName,PotentiallyUsedVariables),
			SInitCLV = [
				if(
					reference_comparison(field_ref(self,VI),null),
					[
						expression_statement(assignment(field_ref(self,VI),variable))
					]
				)
				|SI
			]	
		)
	;
		SInitCLV = SI
	),!,
	init_clause_local_variables(NewVariables,PotentiallyUsedVariables,MappedBodyVariableNames,SI,SZ).
init_clause_local_variables(_NewVariables,_PotentiallyUsedVariables,[],SZ,SZ).



save_state_in_undo_goal(MappedVariableNames,SSaveState,SEval) :- 
	SSaveState =[
		create_undo_goal_and_put_on_goal_stack(MappedVariableNames)
		|SEval
	].
	



update_predicate_arguments(_,[],SR,SR).
update_predicate_arguments(ArgId,[Arg|Args],SUpdatePredArg,SRest) :-
	atom_concat('arg',ArgId,ArgName),
	create_term(Arg,TermConstructor,_),
	SUpdatePredArg = [
		expression_statement(assignment(
			field_ref(self,ArgName),TermConstructor)) | 
		SNextUpdatePredArg
	],
	NextArgId is ArgId + 1,
	update_predicate_arguments(NextArgId,Args,SNextUpdatePredArg,SRest).	



/**
	@signature create_term(ASTNode,TermConstructor,MappedVariableNames)
	@arg(out) MappedVariableNames - the names of the variables used by this term
*/
create_term(ASTNode,_TermConstructor,_MappedVariableNames) :- % let's catch some programming errors early on...
	var(ASTNode),!,throw(internal_error('cannot create term for nothing')).
create_term(ASTNode,TermConstructor,MappedVariableNames) :-
	create_term(ASTNode,TermConstructor,[],MappedVariableNames).
/**
	@signature create_term(ASTNode,TermConstructor,OldVariables,NewVariables)
*/
create_term(ASTNode,int_value(Value),MVNs,MVNs) :-	
	integer_atom(ASTNode,Value),!.
create_term(ASTNode,float_value(Value),MVNs,MVNs) :-
	float_atom(ASTNode,Value),!.
create_term(ASTNode,string_atom(Value),MVNs,MVNs) :-	
	string_atom(ASTNode,Value),!.
create_term(ASTNode,anonymous_variable,MVNs,MVNs) :- 
	anonymous_variable(ASTNode,_VariableName),!.	
create_term(ASTNode,complex_term(string_atom(Functor),ArgsConstructors),OldMVNs,NewMVNs) :-
	complex_term(ASTNode,Functor,Args),
	create_terms(Args,ArgsConstructors,OldMVNs,NewMVNs),!.
create_term(ASTNode,local_variable_ref(VariableIdentifier),OldMVNs,NewMVNs) :- 
	is_variable(ASTNode),!,
	term_meta(ASTNode,Meta),
	lookup_in_meta(mapped_variable_name(MVN),Meta),
	mapped_variable_name_to_variable_identifier(MVN,VariableIdentifier),
	add_to_set(MVN,OldMVNs,NewMVNs).	



create_terms([Arg|Args],[TermConstructor|TermConstructors],OldMVNs,NewMVNs) :- !,
	create_term(Arg,TermConstructor,OldMVNs,IMVNs),
	create_terms(Args,TermConstructors,IMVNs,NewMVNs).
create_terms([],[],MVNs,MVNs).



%mapped_variable_names_to_variable_identifiers([],[]) :- !.
%mapped_variable_names_to_variable_identifiers([VariableName|VariableNames],[VariableIdentifier|VariableIdentifiers]) :- !,
%	mapped_variable_name_to_variable_identifier(VariableName,VariableIdentifier),
%	mapped_variable_names_to_variable_identifiers(VariableNames,VariableIdentifiers).



mapped_variable_name_to_variable_identifier(arg(I),VariableName) :- !,
	atom_concat(arg,I,VariableName).
mapped_variable_name_to_variable_identifier(clv(I),VariableName) :- !,
	atom_concat(clv,I,VariableName).
mapped_variable_name_to_variable_identifier(X,_) :-
	throw(internal_error(['unsupported mapped variable name: ',X])).



/* *************************************************************************** *\

	HELPER METHODS TO DETERMINE THE CONTROL FLOW

\* *************************************************************************** */



/**
	Returns the list of all primitive goals of the given term.<br />
	The first element of the list is always the primitive goal that would be 
	executed first, if we would call the term as a whole.

	@signature primitive_goals_list(ASTNode,SGoals_HeadOfListOfASTNodes,SRest_TailOfThePreviousList)
*/
primitive_goals_list(ASTNode,SGoal,SGoals) :- 
	complex_term(ASTNode,Functor,[LASTNode,RASTNode]),
	(	
		Functor = ',' 
	; 
		Functor = ';' 
	),
	!,
	primitive_goals_list(LASTNode,SGoal,SFurtherGoals),
	primitive_goals_list(RASTNode,SFurtherGoals,SGoals).
primitive_goals_list(ASTNode,[ASTNode|SGoals],SGoals).



/**
	Associates each primitive goal with a unique number; the information
	is added to a goal's meta information.

	Typical usage: <code>number_primitive_goals(ASTNode,0,L)</code>
*/
number_primitive_goals(ASTNode,First,Last) :-
	complex_term(ASTNode,Functor,[LASTNode,RASTNode]),
	(	
		Functor = ','
	; 
		Functor = ';' 
	),
	!,
	number_primitive_goals(LASTNode,First,Intermediate),
	number_primitive_goals(RASTNode,Intermediate,Last).
number_primitive_goals(ASTNode,First,Last) :-
	term_meta(ASTNode,Meta),
	add_to_meta(goal_number(First),Meta),
	Last is First + 1.



/**
	Adds the following meta-information to each (primitive) goal:
	<ul>
	<li>next_goal_if_fails(GoalNumber)</li>
	<li>next_goal_if_fails(multiple)</li>
	<li>not_unique_predecessor_goal</li>
	<li>next_goal_if_succeeds(GoalNumber)</li>
	</ul>
	Prerequisite: all primitive goals must be numbered.<br />
*/
set_primitive_goals_successors(DeferredActions,ASTNode) :-
	complex_term(ASTNode,',',[LASTNode,RASTNode]),!,
	first_primitive_goal(RASTNode,FR),
	last_primitive_goals_if_true(LASTNode,LeftLPGTs,[]),
	last_primitive_goal_if_false(RASTNode,RightLPGF),
	set_successors(DeferredActions,LeftLPGTs,succeeds,[FR]),
	set_successors(DeferredActions,[RightLPGF],fails(redo),LeftLPGTs),
	set_primitive_goals_successors(DeferredActions,LASTNode),
	set_primitive_goals_successors(DeferredActions,RASTNode).
set_primitive_goals_successors(DeferredActions,ASTNode) :-
	complex_term(ASTNode,';',[LASTNode,RASTNode]),!,
	first_primitive_goal(RASTNode,FR),
	first_primitive_goal(LASTNode,FL),
	set_successors(DeferredActions,[FL],fails(call),[FR]),
	set_primitive_goals_successors(DeferredActions,LASTNode),
	set_primitive_goals_successors(DeferredActions,RASTNode).	
set_primitive_goals_successors(_DeferredActions,_ASTNode).



set_successors(DeferredActions,[ASTNode|ASTNodes],Type,SuccessorASTNodes) :-
	set_successor(DeferredActions,ASTNode,Type,SuccessorASTNodes),
	set_successors(DeferredActions,ASTNodes,Type,SuccessorASTNodes).
set_successors(_DeferredActions,[],_Type,_SuccessorASTNodes).



set_successor(DeferredActions,ASTNode,succeeds,[SuccessorASTNode|SuccessorASTNodes]) :-
	set_succeeds_successor(ASTNode,SuccessorASTNode),
	set_successor(DeferredActions,ASTNode,succeeds,SuccessorASTNodes).
set_successor(_DeferredActions,_ASTNode,succeeds,[]).
% Action is either "call" or "redo"
set_successor(DeferredActions,ASTNode,fails(Action),SuccessorASTNodes) :-
	term_meta(ASTNode,Meta),
	(
		SuccessorASTNodes = [SuccessorASTNode], % ... may fail
		term_meta(SuccessorASTNode,SuccessorMeta),
		lookup_in_meta(goal_number(GoalNumber),SuccessorMeta),
		add_to_meta(next_goal_if_fails(Action,GoalNumber),Meta)
	;
		lookup_in_meta(goal_number(GoalNumber),Meta),
		add_to_set_ol(create_field_to_store_predecessor_goal(GoalNumber),DeferredActions),
		add_to_meta(next_goal_if_fails(Action,multiple),Meta),
		set_flag(SuccessorASTNodes,not_unique_predecessor_goal)
	),!.



set_succeeds_successor(ASTNode,SuccessorASTNode) :-
	term_meta(ASTNode,Meta),
	term_meta(SuccessorASTNode,SuccessorMeta),
	lookup_in_meta(goal_number(GoalNumber),SuccessorMeta),
	add_to_meta(next_goal_if_succeeds(GoalNumber),Meta).



set_flag([],_).
set_flag([ASTNode|ASTNodes],Flag) :-
	term_meta(ASTNode,Meta),
	add_to_meta(Flag,Meta),
	set_flag(ASTNodes,Flag).



goal_call_case_id(GoalNumber,CallCaseId) :-
	CallCaseId is GoalNumber * 2.



goal_redo_case_id(GoalNumber,RedoCaseId) :-
	RedoCaseId is GoalNumber * 2 + 1.



