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
	Generates the code for the SAE Prolog program.

	@author Michael Eichberg
*/
:- module(sae_pl_to_oo,[pl_to_oo/4]).

:- use_module('../AST.pl').
:- use_module('../Predef.pl').
:- use_module('../Utils.pl').
:- use_module('../Debug.pl').
:- use_module('../Analyses.pl').

:- use_module('PLAnalyzeVariables.pl',[mapped_variable_ids/2]).


/**
	Encodes an SAE Prolog program using a small object-oriented language (SAE-OO).
*/
pl_to_oo(DebugConfig,Program,_OutputFolder,Program) :-
	debug_message(
			DebugConfig,
			on_entry,
			write('\n[Debug] Phase: Generate the OO Representation_______________________________\n')),
	foreach_user_predicate(Program,encode_predicate(DebugConfig,Program)).



encode_predicate(DebugConfig,Program,Predicate) :-
	predicate_identifier(Predicate,PredicateIdentifier),
	PredicateIdentifier = Functor/Arity,
	debug_message(
		DebugConfig,
		processing_predicate,
		write_atomic_list(['[Debug] Processing Predicate: ',Functor,'/',Arity,'\n'])),
	% build the OO AST
	% ------- METHODS -------
	gen_predicate_constructor(Program,Predicate,SConstructor),
	gen_abort_method(Program,Predicate,SAbortMethod),
	gen_clause_impl_methods(DeferredActions,Program,Predicate,SClausesMethods),
	% If the predicate is defined by a single clause, we simple copy the 
	% the clause implementation in the "next()" method...
	(	predicate_clauses(Predicate,Clauses),single_clause(Clauses) ->
		SClausesMethods = [
			method_decl(
				private,type(boolean),_ClauseIdentifier,[],
				SClauseImplementation
			)
		],
		SImplementation = method_decl(
				public,type(boolean),'next',[],
				SClauseImplementation),
		SMethods = [SConstructor,SAbortMethod,SImplementation]
	;
		gen_clause_selector_method(Program,Predicate,SClauseSelectorMethod),
		SMethods = [
			SConstructor,
			SAbortMethod,
			SClauseSelectorMethod |
			SClausesMethods
		]
	),
	% ------- FIELDS -------
	gen_fields_for_the_control_flow_and_evaluation_state(DeferredActions,Program,Predicate,SFields,S2),
	gen_fields_for_predicate_arguments(Program,Predicate,S2,S3),
	gen_fields_for_clause_local_variables(Predicate,S3,SMethods),
	OOAST = oo_ast([
		predicate_decl(PredicateIdentifier,SFields),
		predicate_registration(PredicateIdentifier)
		]),
	add_to_predicate_meta(OOAST,Predicate).	



/*

	F I E L D S

*/

gen_fields_for_the_control_flow_and_evaluation_state(DeferredActions,_Program,Predicate,SFieldDecls,SR) :-
	predicate_meta(Predicate,PredicateMeta),
	predicate_clauses(Predicate,Clauses),
	% Code to create fields for pre created terms.
	assign_ids_to_pre_created_terms(1,DeferredActions),
	field_decls_for_pre_created_terms(DeferredActions,SFieldDecls,SControlFlow),
	% Standard (instance-related) variables
	SControlFlow = [
		eol_comment('variables to control/manage the execution of this predicate')|
		SClauseToExecute
	],
	(	single_clause(Clauses) ->
		SClauseToExecute = SCutEvaluation
	;
		SClauseToExecute = [field_decl([],type(int),'clauseToExecute',int(0))|SCutEvaluation]
	),	
	(	( lookup_in_meta(uses_cut(no),PredicateMeta) ; single_clause(Clauses) ) ->
		SCutEvaluation = SGoalsEvaluation
	;
		SCutEvaluation = [field_decl([],type(boolean),'cutEvaluation',boolean('false'))|SGoalsEvaluation]
	),
	SGoalsEvaluation = [
		field_decl([],type(int),'goalToExecute',int(0)),
		goal_stack |
		SGoalPredescessors
	],
	% For each goal, that is the successor of an "or" goal, we have to create a 
	% a variable that stores which goal was executed previously, to go back to
	% the correct goal in case of backtracking.
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
	atom_concat('arg',I,FieldName). % TODO remove the string concatenation here......just pass the field to the next phase



field_decl_for_initial_pred_arg_state_i(I,field_decl([final],type(state),FieldName)) :-
	atomic_list_concat(['initialArg',I,'state'],FieldName). % TODO remove the string concatenation here...just pass the field to the next phase



gen_fields_for_clause_local_variables(
		Predicate,
		[eol_comment('variables to store intermediate, clause local information')|SFieldDecls],
		SR
	) :- 
	predicate_meta(Predicate,Meta),
	lookup_in_meta(maximum_number_of_clause_local_variables(Max),Meta),
	call_foreach_i_in_0_to_u(Max,field_decl_for_clause_local_variable,SFieldDecls,SR).



field_decl_for_clause_local_variable(I,field_decl([],type(term),FieldName)) :-
	atom_concat(clv,I,FieldName). % TODO remove the string concatenation here......just pass the field to the next phase



field_decls_for_pre_created_terms(Tail,SRest,SRest) :- var(Tail),!.
field_decls_for_pre_created_terms(
		[create_field_for_pre_created_term(PCTId,Expr)|Actions],
		SFieldDecl,
		SRest
	) :- !,
	SFieldDecl = [pre_created_term(PCTId,Expr) | SNextFieldDecl],
	field_decls_for_pre_created_terms(Actions,SNextFieldDecl,SRest).
field_decls_for_pre_created_terms([_Action|Actions],SFieldDecl,SRest) :- 
	field_decls_for_pre_created_terms(Actions,SFieldDecl,SRest).



assign_ids_to_pre_created_terms(_Id,Tail) :- var(Tail),!.
assign_ids_to_pre_created_terms(
		Id,
		[create_field_for_pre_created_term(PCTId,_)|Actions]
	) :-
	(	var(PCTId) ->
		PCTId = Id
	;
		true
	),
	NextId is Id + 1,
	assign_ids_to_pre_created_terms(NextId,Actions).
assign_ids_to_pre_created_terms(Id,[_|Actions]) :-
	assign_ids_to_pre_created_terms(Id,Actions).



/*

	C O N S T R U C T O R
	
*/
gen_predicate_constructor(
		_Program,
		Predicate,
		constructor_decl(PredicateIdentifier,ParamDecls,SInitFieldsForArgsStmts)
	) :-
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
	atom_concat('arg',I,ParamName). % REFACTOR use "arg(I)"

	
	
init_field_of_arg_i(
		I,
		expression_statement(
			assignment(
				arg(I),reveal(
				local_variable_ref(ArgName))))) :-
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
	(	single_clause(Clauses),!,
		SEvalGoals = [return(method_call(self,'clause0',[]))],
		ClauseSelectorMethod = 
			method_decl(
				public,
				type(boolean),
				'next',
				[],
				SMain)
	;	/*
		If we have only two cases it is sufficient to use an "if" statement to 
		branch between these two statements.
		*/
		two_clauses(Clauses),!,
		foreach_clause(
			Clauses,
			selector_for_clause_i(Predicate),
			[
				case(int(0),FirstClauseStmts),
				case(int(1),SecondClauseStmts)
			]
		),
		SEvalGoals = [
			if(
				value_comparison('==',field_ref(self,'clauseToExecute'),int(0)),
				FirstClauseStmts
			)|
			SecondClauseStmts
		],
		ClauseSelectorMethod = 
			method_decl(
				public,
				type(boolean),
				'next',
				[],
				SMain)
	;	/*
		This is the base case to handle arbitrary numbers of clauses.
		*/
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

selector_for_clause_i(_Predicate,I,Clause,last,case(int(I),Stmts)) :- 
	atom_concat('clause',I,ClauseIdentifier),
	lookup_in_clause_meta(last_call_optimization_is_possible,Clause),!,
	reset_clause_local_variables(Clause,SCleanup,SPrepareForNextClause),
	(	lookup_in_clause_meta(cut(never),Clause) ->
		SCutReset = eol_comment('no cut...')
	;
		SCutReset = expression_statement(
			assignment(field_ref(self,'cutEvaluation'),boolean(false))
		)
	),
	SPrepareForNextClause = [
		SCutReset,
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
	].

selector_for_clause_i(_Predicate,I,Clause,_ClausePosition,case(int(I),Stmts)) :-
	lookup_in_clause_meta(last_call_optimization_is_possible,Clause),!,
	atom_concat('clause',I,ClauseIdentifier),
	NextClauseId is I + 1,
	(	lookup_in_clause_meta(cut(never),Clause) ->
		SCutReset = eol_comment('no cut...')
	;
		SCutReset = expression_statement(assignment(field_ref(self,'cutEvaluation'),boolean(false)))
	),
	reset_clause_local_variables(
		Clause,
		SResetCLVs,
		[  % after reseting the CLVs...
			SCutReset,		
			expression_statement(assignment(field_ref(self,'goalToExecute'),int(0))),
			expression_statement(assignment(field_ref(self,'clauseToExecute'),int(0))), % unless this is the first clause....
			continue('eval_clauses')
		] 
	),
	(	lookup_in_clause_meta(cut(never),Clause) ->
		SCut = eol_comment('this clause contains no "cut"')
	;
		SCut = if(field_ref(self,'cutEvaluation'),
			[
				expression_statement(method_call(self,'abort',[])),
				return(boolean(false))
			]
		)
	),
	Stmts = [
		if(
			method_call(self,ClauseIdentifier,[]),
			[	eol_comment('tail recursive clause with last call optimization')|
				SResetCLVs
			]
		),		
		SCut,
		eol_comment('prepare the execution of the next clause'),
		expression_statement(assignment(field_ref(self,'goalToExecute'),int(0))),
		expression_statement(assignment(field_ref(self,'clauseToExecute'),int(NextClauseId)))
	].
	

selector_for_clause_i(Predicate,I,_Clause,last,case(int(I),Stmts)) :- !,
	atom_concat('clause',I,ClauseIdentifier),
	% if this is the last clause, we don't care if the evaluation was "cutted" or not
	( lookup_in_predicate_meta(has_clauses_where_last_call_optimization_is_possible,Predicate) ->
		Stmts = [
			if(method_call(self,ClauseIdentifier,[]),
				[return(boolean(true))]
			),
			expression_statement(method_call(self,'abort',[])),
			return(boolean(false))
		]
	;
		Stmts = [return(method_call(self,ClauseIdentifier,[]))]
	).
		
	
selector_for_clause_i(Predicate,I,Clause,_ClausePosition,case(int(I),Stmts)) :-
	atom_concat('clause',I,ClauseIdentifier),
	NextClauseId is I + 1,
	
	reset_clause_local_variables(Clause,SCleanup,SPrepareForNextClause),
	SPrepareForNextClause = [
		eol_comment('prepare the execution of the next clause'),
		expression_statement(assignment(field_ref(self,'goalToExecute'),int(0))),
		expression_statement(assignment(field_ref(self,'clauseToExecute'),int(NextClauseId)))
	],
	(
		lookup_in_clause_meta(cut(never),Clause) ->
		ClauseFailed = [eol_comment('this clause contains no "cut"') |SCleanup]
	;
		(	lookup_in_predicate_meta(has_clauses_where_last_call_optimization_is_possible,Predicate) ->
			Return = [ 
				expression_statement(method_call(self,'abort',[])),
				return(boolean(false))
			]
		;
			Return = [return(boolean(false))]
		),
		ClauseFailed = [
			if(field_ref(self,'cutEvaluation'),
				Return
			) | 
			SCleanup
		]
	),
	Stmts = [
		if(method_call(self,ClauseIdentifier,[]),
			[return(boolean(true))]
		) |
		ClauseFailed
	].



reset_clause_local_variables(
		Clause,
		SCLVReset,
		SR
	) :- 
	lookup_in_clause_meta(clause_local_variables_count(CLVCount),Clause),
	call_foreach_i_in_0_to_u(CLVCount,reset_clause_local_variable,SCLVReset,SR).



reset_clause_local_variable(
		I,
		expression_statement(assignment(field_ref(self,FieldName),null))
	) :-
	atom_concat(clv,I,FieldName). % TODO let the OOto... layer rewrite the names..



/*

	T H E   C L A U S E  I M P L E M E N T A T I O N S
	
	"boolean clauseX()" M E T H O D S      

*/
gen_clause_impl_methods(DeferredActions,_Program,Predicate,SClauseImpls) :-
	predicate_clauses(Predicate,Clauses),
	foreach_clause(
		Clauses,
		implementation_for_clause_i(Predicate,DeferredActions),
		SClauseImpls).
	
implementation_for_clause_i(
		Predicate,
		DeferredActions,
		I,
		Clause,
		_ClausePosition,
		SClauseMethod
	) :-
	atom_concat('clause',I,ClauseIdentifier),
	clause_definition(Clause,ClauseDefinition),
	rule_body(ClauseDefinition,BodyASTNode),
	number_primitive_goals(BodyASTNode,0,LastId),	
	set_primitive_goals_successors(DeferredActions,BodyASTNode),
	primitive_goals_list(BodyASTNode,PrimitiveGoalsList,[]),
	translate_goals(Predicate,Clause,PrimitiveGoalsList,DeferredActions,SCases,[]),
	(	LastId == 1 ->
		MethodBody = [switch(top_level,field_ref(self,'goalToExecute'),SCases)]
	;
		MethodBody = [ 
			forever(
				'eval_goals',
				[switch(inside_forever,field_ref(self,'goalToExecute'),SCases)]
			) 
		]
	),
	SClauseMethod = method_decl(
			private,
			type(boolean),
			ClauseIdentifier,
			[],
			MethodBody
		).



translate_goals(
		Predicate,Clause,[PrimitiveGoal|PrimitiveGoals],
		DeferredActions,SGoalCases,SRest
	) :-
	translate_goal(
		PrimitiveGoal,Clause,Predicate,
		DeferredActions,SGoalCases,SOtherGoalCases
	),
	translate_goals(
		Predicate,Clause,PrimitiveGoals,
		DeferredActions,SOtherGoalCases,SRest
	).
translate_goals(_Predicate,_Clause,[],_DeferredActions,SCases,SCases).



/**
	To translate a goal, the following information have to be available:
	<ul>
	<li>next_goal_if_fails(Action,GoalNumber)<br />
		Action is either "redo" (in case of backtracking) or "call" (if we try an 
		new alternative)</li>
	<li>next_goal_if_fails(Action,multiple)<br />
		Action must be "redo"</li>
	<li>not_unique_predecessor_goal</li>
	<li>next_goal_if_succeeds(GoalNumber)</li>
	</ul>
*/
translate_goal(
		PrimitiveGoal,_Clause,Predicate,
		_DeferredActions,[SCallCase,SRedoCase|SCases],SCases
	) :-
	string_atom(PrimitiveGoal,'!'),!,
	term_meta(PrimitiveGoal,CutMeta),
	lookup_in_meta(goal_number(GoalNumber),CutMeta),
	% Handle the case if the cut is called the first time
	goal_call_case_id(GoalNumber,CallCaseId),
	select_and_jump_to_next_goal_after_succeed(CutMeta,force_jump,JumpToNextGoalAfterSucceed),
	(	predicate_clauses(Predicate,Clauses), single_clause(Clauses) ->
		SetCutEvaluation = nop
	;
		SetCutEvaluation = expression_statement(
			assignment(field_ref(self,'cutEvaluation'),boolean(true))
		)
	),
	SCallCase = case(
		int(CallCaseId),
		[
			eol_comment('cut...'),
			SetCutEvaluation |
			JumpToNextGoalAfterSucceed
		]
	),
	% Handle the case if the cut is called the second time (redo-case)
	goal_redo_case_id(GoalNumber,RedoCaseId),
	SRedoCase = case(
		int(RedoCaseId),
		[
			abort_pending_goals_and_clear_goal_stack,
			return(boolean(false))
		]
	).

/*
	Translates the "unification"
*/
translate_goal(
		PrimitiveGoal,_Clause,_Predicate,
		DeferredActions,[SCallCase,SRedoCase|SCases],SCases
	) :-
	compound_term(PrimitiveGoal,'=',[LASTNode,RASTNode]),
	(	is_variable(LASTNode), 
		\+ is_variable(RASTNode),
		VarASTNode = LASTNode,
		TermASTNode = RASTNode
	;
		is_variable(RASTNode), 
		\+ is_variable(LASTNode),
		VarASTNode = RASTNode,
		TermASTNode = LASTNode
	),!,
	term_meta(PrimitiveGoal,UnifyMeta),
	lookup_in_meta(goal_number(GoalNumber),UnifyMeta),
	select_and_jump_to_next_goal_after_succeed(UnifyMeta,force_jump,JumpToNextGoalAfterSucceed),
	select_and_jump_to_next_goal_after_fail(UnifyMeta,JumpToNextGoalAfterFail,[]),
	% call-case
	SHead = [
		locally_scoped_states_list,
		local_variable_decl(type(boolean),'succeeded',boolean(false))
		|
		SUnification
	],
	unfold_unification(DeferredActions,UnifyMeta,VarASTNode,TermASTNode,SUnification,STail),	
	STail = [
		if(local_variable_ref('succeeded'),
			[
				create_undo_goal_for_locally_scoped_states_list_and_put_on_goal_stack |
				JumpToNextGoalAfterSucceed
			],
			[
				locally_scoped_states_list_reincarnate_states |
				JumpToNextGoalAfterFail
			]
		)
	],
	goal_call_case_id(GoalNumber,CallCaseId),
	SCallCase = case(
		int(CallCaseId),
		SHead
	),	
	% redo-case
	goal_redo_case_id(GoalNumber,RedoCaseId),
	SRedoCase = case(
		int(RedoCaseId),
		[
			abort_and_remove_top_level_goal_from_goal_stack |
			JumpToNextGoalAfterFail
		]
	).



% Handles all other cases of unification 
% IMPROVE unfold the unification in more complex cases such as "a(b,X) = a(_,Y)"...
translate_goal(
		PrimitiveGoal,_Clause,_Predicate,
		DeferredActions,[SCallCase,SRedoCase|SCases],SCases
	) :-
	compound_term(PrimitiveGoal,'=',[LASTNode,RASTNode]),!,
	term_meta(PrimitiveGoal,Meta),
	lookup_in_meta(goal_number(GoalNumber),Meta),
	% call-case
	goal_call_case_id(GoalNumber,CallCaseId),
	select_and_jump_to_next_goal_after_succeed(Meta,force_jump,JumpToNextGoalAfterSucceed),
	create_term(LASTNode,cache,LTermConstructor,LVariableIds,DeferredActions),
	create_term(RASTNode,cache,RTermConstructor,RVariableIds,DeferredActions),
	merge_sets(LVariableIds,RVariableIds,VariableIds),
	SEval = [
		if(unify(LTermConstructor,RTermConstructor),
			JumpToNextGoalAfterSucceed
		)
	],
	(
		lookup_in_meta(variables_used_for_the_first_time(VariablesUsedForTheFirstTime),Meta),
		lookup_in_meta(variables_that_may_have_been_used(VariablesPotentiallyPreviouslyUsed),Meta) ->
		init_clause_local_variables(
			VariablesUsedForTheFirstTime,
			VariablesPotentiallyPreviouslyUsed,
			VariableIds,
			SInitCLVs,
			SSaveState
		),
		(	remove_from_set(arg(_),VariablesUsedForTheFirstTime,CLVariablesUsedForTheFirstTime),
			set_subtract(VariableIds,CLVariablesUsedForTheFirstTime,VariablesThatNeedToBeSaved),
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
	% redo-case
	goal_redo_case_id(GoalNumber,RedoCaseId),
	select_and_jump_to_next_goal_after_fail(Meta,JumpToNextGoalAfterFail,[]),
	SRedoCase = case(
		int(RedoCaseId),
		RedoAction
	).

/*
	Translates arithmetic comparisons. (e.g., =:=, =<, <, >, ... )
*/
translate_goal(
		PrimitiveGoal,_Clause,_Predicate,
		DeferredActions,[SCallCase,SRedoCase|SCases],SCases
	) :-
	compound_term(PrimitiveGoal,Operator,[LASTNode,RASTNode]),
	is_arithmetic_comparison_operator(Operator),
	!,
	term_meta(PrimitiveGoal,Meta),% REFACTOR Meta -> PrimitiveGoalMeta
	lookup_in_meta(goal_number(GoalNumber),Meta),
	% call-case...
	goal_call_case_id(GoalNumber,CallCaseId),
	select_and_jump_to_next_goal_after_succeed(
		Meta,
		force_jump,
		JumpToNextGoalAfterSucceed),
	create_term(LASTNode,do_not_cache,LTermConstructor,_LMappedVarIds,DeferredActions),
	create_term(RASTNode,do_not_cache,RTermConstructor,_RMappedVarIds,DeferredActions),
	SCallCase = case(
		int(CallCaseId),
		[
			if(arithmetic_comparison(Operator,LTermConstructor,RTermConstructor),
				JumpToNextGoalAfterSucceed
			)
		]
	),
	% redo-case...
	goal_redo_case_id(GoalNumber,RedoCaseId),
	select_and_jump_to_next_goal_after_fail(Meta,JumpToNextGoalAfterFail,[]),
	SRedoCase = case(
		int(RedoCaseId),
		JumpToNextGoalAfterFail
	).

/*
	Translates the arithmetic evaluation operator "is".
*/
translate_goal(
		PrimitiveGoal,_Clause,_Predicate,
		DeferredActions,[SCallCase,SRedoCase|SCases],SCases
	) :-
	% implements the case that the result of "is" is assigned to a new variable..
	% IMPROVE If the left side of "is" is an int_value or a variable(containing) an int_value, we currently just create an instance of the "is" Predicate, which ist grossly inefficient
	compound_term(PrimitiveGoal,'is',[LASTNode,RASTNode]),
	term_meta(PrimitiveGoal,Meta),
	lookup_in_meta(variables_used_for_the_first_time(NewVariables),Meta),
	lookup_in_term_meta(variable_id(VarId),LASTNode),
	memberchk_ol(VarId,NewVariables),VarId \= arg(_),!,
	% Handle the case if "is" is called the first time
	lookup_in_meta(goal_number(GoalNumber),Meta),
	goal_call_case_id(GoalNumber,CallCaseId),
	select_and_jump_to_next_goal_after_succeed(Meta,force_jump,JumpToNextGoalAfterSucceed),
	create_term(RASTNode,do_not_cache,RTermConstructor,_RVariableIds,DeferredActions),
	SCallCase = case(
		int(CallCaseId),
		[
			expression_statement(
				assignment(
					VarId,
					int_value_expr(arithmetic_evluation(RTermConstructor)))) |
			JumpToNextGoalAfterSucceed
		]
	),
	% (redo-case)
	goal_redo_case_id(GoalNumber,RedoCaseId),
	select_and_jump_to_next_goal_after_fail(Meta,JumpToNextGoalAfterFail,[]),
	SRedoCase = case(
		int(RedoCaseId),
		JumpToNextGoalAfterFail
	).	

/*
	Translates tail recursive calls. (None of the previous goals can be tail-recursive!)
*/
translate_goal(
		PrimitiveGoal,_Clause,_Predicate,
		_DeferredActions,[SCallCase|SCases],SCases
	) :-
	term_meta(PrimitiveGoal,Meta),
	lookup_in_meta(last_call_optimization_is_possible,Meta),!,
	lookup_in_meta(goal_number(GoalNumber),Meta),
	goal_call_case_id(GoalNumber,CallCaseId),
	SCaseHead = [eol_comment('tail call with last call optimization')|SLSTVs],
	(	compound_term_args(PrimitiveGoal,Args) ->
		% IMPROVE Identify those variables that were not yet subject to unification..., because for these variables, we can omit calling "reveal" (which is nice for all variables that just deal with numbers...)
		lookup_in_meta(variables_used_for_the_first_time(NewVariables),Meta),
		update_predicate_arguments(NewVariables,0,Args,SUpdatePredArgs,SRecursiveCall,IdsOfArgsThatNeedToBeTemporarilySaved),
		findall(
			locally_scoped_term_variable(Id,reveal(arg(Id))),
			member_ol(Id,IdsOfArgsThatNeedToBeTemporarilySaved),
			SLSTVs,
			SUpdatePredArgs
		)
	;	% if the predicate does not have arguments... (e.g., repeat)	
		SLSTVs = SRecursiveCall
	),
	SRecursiveCall = [
		clear_goal_stack,
		return(boolean(true))
	],
	SCallCase = case(
		int(CallCaseId),
		SCaseHead
	).
	
/*
	Translates goals that are neither tail-recursive and subject to last-call
	optimization nor built-ins of the SAE Prolog compiler.
*/
translate_goal(
		PrimitiveGoal,_Clause,_Predicate,
		DeferredActions,[SCallCase,SRedoCase|SCases],SCases
	) :-
	term_meta(PrimitiveGoal,Meta),
	lookup_in_meta(goal_number(GoalNumber),Meta),
	
	% "call-case"
	goal_call_case_id(GoalNumber,CallCaseId),
	create_term(PrimitiveGoal,do_not_cache_root,TermConstructor,VariableIds,DeferredActions),
	(
		lookup_in_meta(variables_used_for_the_first_time(FTUVars),Meta),
		lookup_in_meta(variables_that_may_have_been_used(PotentiallyUsedVariables),Meta) ->
		remove_from_set(arg(_),FTUVars,CLFTUVars),
		replace_vars_in_term_constructor(TermConstructor,CLFTUVars,replace_by_lstv_var,NewTermConstructor),
		init_clause_local_variables(
			FTUVars,
			PotentiallyUsedVariables,
			VariableIds,
			SInitCLVs,
			[ push_onto_goal_stack(static_predicate_call(NewTermConstructor)) ]
		)
	;	% handles the case that the goal is a string-atom (not a compound term)
		SInitCLVs = [ push_onto_goal_stack(static_predicate_call(TermConstructor)) ]
	),
	SCallCase = case(
		int(CallCaseId),
		SInitCLVs
	),
	
	% "redo-case"
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
select_and_jump_to_next_goal_after_fail(Meta/*REFACTOR GoalMeta*/,SStmts,SRest) :-
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
	@signature init_clause_local_variables(FTUVars,VariablesPotentiallyUsedBefore,BodyVariableIds,SInitClauseLocalVariables,SZ).
*/
init_clause_local_variables(
		FTUVars, % FTU = First Time Used...
		PotentiallyUsedVariables,
		[VariableId|VariableIds],
		SInitCLV,
		SZ
	) :-
	(	VariableId = clv(_I), % may fail
		(
			memberchk(VariableId,FTUVars),
			SInitCLV = [
				locally_scoped_term_variable(VariableId,assignment(VariableId,variable))
				%expression_statement(assignment(VariableId,variable))
				|SI
			]
		;	
			memberchk(VariableId,PotentiallyUsedVariables),
			SInitCLV = [
				if(
					reference_comparison(VariableId,null),
					[
						expression_statement(assignment(VariableId,variable))
					]
				)
				|SI
			]	
		)
	;
		SInitCLV = SI
	),!,
	init_clause_local_variables(FTUVars,PotentiallyUsedVariables,VariableIds,SI,SZ).
init_clause_local_variables(_FTUVars,_PotentiallyUsedVariables,[],SZ,SZ).



save_state_in_undo_goal(VariableIds,SSaveState,SEval) :- 
	SSaveState =[
		create_undo_goal_and_put_on_goal_stack(VariableIds)
		|SEval
	].




/*
	Updates the predicate arguments before the next round (tail recursive)...
	A predicate such as swap(X,Y) :- swap(Y,X) requires that we sometime have to 
	store the values of the args into temporary variables.
*/
update_predicate_arguments(_NewVariables,_,[],SR,SR,_) :- !.
update_predicate_arguments(
		NewVariables,
		ArgId,
		[Arg|Args],
		SUpdatePredArg,
		SRest,
		IdsOfArgsThatNeedToBeTemprarilySaved
	) :- % LSTVs are local scoped term variables...
	create_term(Arg,cache,TermConstructor,_,_DeferredActions),
	replace_ids_of_args_lower_than(ArgId,TermConstructor,NewTermConstructor,IdsOfArgsThatNeedToBeTemprarilySaved),
	(	
		NewTermConstructor = reveal(arg(ArgId)), memberchk_ol(arg(ArgId),NewVariables),
		!,
		atomic_list_concat(['arg',ArgId,' is not used in the body...'],Comment),
		SUpdatePredArg = [
			eol_comment(Comment)  | 
			SNextUpdatePredArg
		]
	;	
		( NewTermConstructor = arg(_ArgId) ; NewTermConstructor = clv(_CLVId) ),
		!,
		SUpdatePredArg = [
			expression_statement(assignment(arg(ArgId),reveal(NewTermConstructor)))  | 
			SNextUpdatePredArg
		]
	;
		SUpdatePredArg = [
			expression_statement(assignment(arg(ArgId),NewTermConstructor)) | 
			SNextUpdatePredArg
		]
	),
	NextArgId is ArgId + 1,
	update_predicate_arguments(NewVariables,NextArgId,Args,SNextUpdatePredArg,SRest,IdsOfArgsThatNeedToBeTemprarilySaved).	



replace_ids_of_args_lower_than(Id,arg(ArgId),NewTermConstructor,Ids) :- 
	!,
	(	ArgId < Id ->
		add_to_set_ol(ArgId,Ids),
		NewTermConstructor = lstv(ArgId)
	;
		NewTermConstructor = reveal(arg(ArgId))
	).
replace_ids_of_args_lower_than(_Id,clv(CLVId),NewTermConstructor,_Ids) :- 
	!,
	NewTermConstructor = reveal(clv(CLVId)).	
replace_ids_of_args_lower_than(
		Id,
		compound_term(Functor,ArgsConstructors),
		compound_term(Functor,NewArgsConstructors),
		Ids
	) :- !,
	replace_ids_of_args_of_list_lower_than(Id,ArgsConstructors,NewArgsConstructors,Ids).
replace_ids_of_args_lower_than(_Id,TermConstructor,TermConstructor,_).



replace_ids_of_args_of_list_lower_than(_,[],[],_Ids).
replace_ids_of_args_of_list_lower_than(
		Id,
		[ArgConstructor|ArgsConstructors],
		[NewArgConstructor|NewArgsConstructors],
		Ids) :-
	replace_ids_of_args_lower_than(Id,ArgConstructor,NewArgConstructor,Ids),
	replace_ids_of_args_of_list_lower_than(Id,ArgsConstructors,NewArgsConstructors,Ids).



unfold_unification(
		DeferredActions,
		UnifyMeta,
		VarASTNode,
		TermASTNode,
		SUnification,STail
	) :-
	create_term(VarASTNode,do_not_cache,VarNodeTermConstructor,[VarId],DeferredActions),
	create_term(TermASTNode,cache,CachedTermNodeTermConstructor,_,DeferredActions),
	lookup_in_meta(variables_used_for_the_first_time(FTUVarsIds),UnifyMeta), % FTUVar.. = First Time Used Variables
	lookup_in_meta(variables_that_may_have_been_used(MUVarsIds),UnifyMeta), % MUVar.. = May have been Used Variables
	named_variables_of_term(TermASTNode,NamedVariablesASTNodes,[]),
	mapped_variable_ids(NamedVariablesASTNodes,VariablesIds),
	% if the variable is free... and guaranteed to be reveald 
	% (currently, only args are guaranteed to be reveald)
	(	VarId = arg(_), memberchk_ol(VarId,FTUVarsIds) ->
		IsExposed = reveald
	;
		IsExposed = unknown
	),
	init_clause_local_variables(FTUVarsIds,MUVarsIds,VariablesIds,SInitCLVs,SSaveStates),
	remove_from_set(arg(_),FTUVarsIds,CLFTUVarsIds),
	replace_vars_in_term_constructor(
		CachedTermNodeTermConstructor,
		CLFTUVarsIds,
		replace_by_lstv_var,
		NewCachedTermNodeTermConstructor),
	SSaveStates = [
		manifest_state_and_add_to_locally_scoped_states_list([VarId]) |
		SSucceeded
	],		
	SSucceeded = [
		bind_variable(VarId,NewCachedTermNodeTermConstructor),
		expression_statement(assignment(local_variable_ref('succeeded'),boolean(true)))
	],
	% if we can "unfold"...
	create_term(TermASTNode,do_not_cache_root,TermNodeTermConstructor,_,DeferredActions),
	(
		TermNodeTermConstructor = int_value(_IntValue),!,
		SMatchTerm = [
			if(
				boolean_and(
					test_term_is_integer_value(VarNodeTermConstructor),
					arithmetic_comparison(
						'=:=',
						VarNodeTermConstructor,
						TermNodeTermConstructor)
				),
				[
					expression_statement(assignment(local_variable_ref('succeeded'),boolean(true)))
				]
			)
		]
	;
		TermNodeTermConstructor = float_value(_FloatValue),!,
		SMatchTerm = [
			if(
				boolean_and(
					test_term_is_float_value(VarNodeTermConstructor),
					arithmetic_comparison(
						'=:=',
						VarNodeTermConstructor,
						TermNodeTermConstructor)
				),
				[
					expression_statement(assignment(local_variable_ref('succeeded'),boolean(true)))
				]
			)
		]
	;
		TermNodeTermConstructor = string_atom(_StringAtom),!,
		SMatchTerm = [
			if(
				boolean_and(
					% IMPROVE.. have something like a combined test and compare expression to avoid repetitive to reveal the same term multiple times
					test_term_is_string_atom(VarNodeTermConstructor),
					string_atom_comparison(reveal(VarNodeTermConstructor),TermNodeTermConstructor)
				),
				[
					expression_statement(assignment(local_variable_ref('succeeded'),boolean(true)))
				]
			)
		]
	;
		TermNodeTermConstructor = compound_term(FunctorConstructor,ArgsConstructors),!,
		FunctorConstructor = string_atom(StringValue),
		create_term_for_cacheable_string_atom(
				StringValue,
				CachedFunctorConstructor,
				DeferredActions),
		length(ArgsConstructors,ArgsConstructorsCount),
		remove_from_set(arg(_),FTUVarsIds,FTUCLVarsIds),
		test_and_unify_args(
			ArgsConstructors,
			VarId,
			0,
			[],FTUCLVarsIds,
			STest,
			[	% if all tests succeed, we set the "succeeded" variable to true 
				expression_statement(assignment(local_variable_ref('succeeded'),boolean(true))) 
			],
			DeferredActions),
		SMatchTerm = [
			if(
				boolean_and(
					% TODO Refactor use as the operator the standard prolog operators
					value_comparison('==',term_arity(VarNodeTermConstructor),int(ArgsConstructorsCount)),					
					functor_comparison(term_functor(VarNodeTermConstructor),CachedFunctorConstructor)
				),
				STest
			)		
		]
	),
	SUnification = [
		if(test_rttype_of_term_is_free_variable(IsExposed,VarNodeTermConstructor),
			SInitCLVs,
			SMatchTerm
		) |
		STail
	].


% GOAL Locally used variables..
test_and_unify_args(
		[], % We are finished; there are no more arguments.
		_BaseVariable,_ArgId,_VarsWithSavedState,_VarsThatAreFree,
		SRest,SRest,
		_DeferredActions) :- !.
test_and_unify_args(
		[CLV|ArgsConstructors],
		BaseVariable,
		ArgId,
		VarsWithSavedState,VarsThatAreFree,
		STaU,SRest,
		DeferredActions
	) :- 
	CLV = clv(_),
	memberchk(CLV,VarsThatAreFree),!,
	STaU = [
		expression_statement(assignment(CLV,term_arg(BaseVariable,int(ArgId))))
		| SFurtherTaU
	],	
	remove_from_set(CLV,VarsThatAreFree,NewVarsThatAreFree),
	NewArgId is ArgId + 1,
	test_and_unify_args(
		ArgsConstructors,
		BaseVariable,
		NewArgId,
		VarsWithSavedState,NewVarsThatAreFree,
		SFurtherTaU,SRest,
		DeferredActions). 
test_and_unify_args(
		[ArgsConstructor|ArgsConstructors],
		BaseVariable,
		ArgId,
		VarsWithSavedState,VarsThatAreFree,
		STaU,SRest,
		DeferredActions	
	) :- 
	ArgsConstructor = anonymous_variable,!,
	NewArgId is ArgId + 1,
	test_and_unify_args(
		ArgsConstructors,
		BaseVariable,
		NewArgId,
		VarsWithSavedState,VarsThatAreFree,
		STaU,SRest,
		DeferredActions).
% the base case... 
test_and_unify_args(
		[ArgsConstructor|ArgsConstructors],
		BaseVariable,
		ArgId,
		VarsWithSavedState,
		VarsThatAreFree,
		STaU,SRest,
		DeferredActions) :- 
	term_constructor_variables(ArgsConstructor,UsedVariables,[]),	
	set_subtract(UsedVariables,VarsWithSavedState,UsedVarsWithNoSavedState),
	set_subtract(UsedVarsWithNoSavedState,VarsThatAreFree,VarsThatNeedToBeSaved),
	% init goal local variables that are not yet initialized
	intersect_sets(VarsThatAreFree,UsedVariables,VarsThatNeedToBeInitialized),
	findall(
		InitStmt,
		(
			member(Var,VarsThatNeedToBeInitialized),
			InitStmt = if(
				reference_comparison(Var,null),
				[
					expression_statement(assignment(Var,variable))
				]
			)
		),
		STaU,
		SSaveStatesAndUnify
	),
	SSaveStatesAndUnify = [
		% save the state of the nth argument of the base variable and the vars that need to be saved
		manifest_state_and_add_to_locally_scoped_states_list(
			[term_arg(BaseVariable,int(ArgId))|VarsThatNeedToBeSaved]
		),
		if(unify(term_arg(BaseVariable,int(ArgId)),ArgsConstructor),
		 	SFurtherTaU
		)
	],
	merge_sets(VarsWithSavedState,VarsThatNeedToBeSaved,NewVarsWithSavedState),
	set_subtract(VarsThatAreFree,VarsThatNeedToBeInitialized,NewVarsThatAreFree),
	NewArgId is ArgId + 1,
	test_and_unify_args(
		ArgsConstructors,
		BaseVariable,
		NewArgId,
		NewVarsWithSavedState,
		NewVarsThatAreFree,
		SFurtherTaU,SRest,
		DeferredActions).



term_constructor_variables(int_value(_),SRest,SRest) :- !.
term_constructor_variables(float_value(_),SRest,SRest) :- !.
term_constructor_variables(string_atom(_),SRest,SRest) :- !.
term_constructor_variables(pre_created_term(_),SRest,SRest) :- !.
term_constructor_variables(anonymous_variable,SRest,SRest) :- !.
term_constructor_variables(compound_term(_,Args),SMappedVariables,SRest) :- !,
	term_constructors_variables(Args,SMappedVariables,SRest).
term_constructor_variables(Variable,[Variable|SRest],SRest) :- !.



term_constructors_variables([],SRest,SRest).
term_constructors_variables([Arg|Args],SMappedVariables,SRest) :-
	term_constructor_variables(Arg,SMappedVariables,SIMappedVariables),
	term_constructors_variables(Args,SIMappedVariables,SRest).



/**
	replace_vars_in_term_constructor(TermConstructor,FTUVars,Goal,NewTermConstructor)
*/
replace_vars_in_term_constructor(TermConstructor,_FTUVars,_,TermConstructor) :-
	TermConstructor = int_value(_),!.
replace_vars_in_term_constructor(TermConstructor,_FTUVars,_,TermConstructor) :-
	TermConstructor = float_value(_),!.
replace_vars_in_term_constructor(TermConstructor,_FTUVars,_,TermConstructor) :-
	TermConstructor = string_atom(_),!.
replace_vars_in_term_constructor(TermConstructor,_FTUVars,_,TermConstructor) :-
	TermConstructor = pre_created_term(_),!.
replace_vars_in_term_constructor(TermConstructor,_FTUVars,_,TermConstructor) :-
	TermConstructor = anonymous_variable,!.	
replace_vars_in_term_constructor(TermConstructor,FTUVars,Goal,compound_term(Functor,NewArgs)) :-
	TermConstructor = compound_term(Functor,Args),!,
	replace_vars_in_term_constructors(Args,FTUVars,Goal,NewArgs).
replace_vars_in_term_constructor(Var,FTUVars,Goal,NewVar) :-
	memberchk_ol(Var,FTUVars),!,
	call(Goal,Var,NewVar).
replace_vars_in_term_constructor(Var,_FTUVars,_Goal,Var).


replace_vars_in_term_constructors([],_FTUVars,_Goal,[]) :- !.
replace_vars_in_term_constructors([TermConstructor|TermConstructors],FTUVars,Goal,[NewTermConstructor|NewTermConstructors]) :-
	replace_vars_in_term_constructor(TermConstructor,FTUVars,Goal,NewTermConstructor),
	replace_vars_in_term_constructors(TermConstructors,FTUVars,Goal,NewTermConstructors).



replace_by_lstv_var(Var,lstv(Var)).



/**
	@signature create_term(ASTNode,Type,TermConstructor,VariableIds,DeferredActions)
	@arg(in) Type is either "cache", "do_not_cache_root" or "do_not_cache"
	@arg(out) VariableIds - the names of the variables used by this term
*/
create_term(
		ASTNode,
		_Type,_TermConstructor,_VariableIds,_DeferredActions
	) :- % let's catch some programming errors early on...
	var(ASTNode),!,
	throw(internal_error(
		create_term/5,'ASTNode needs to be instantiated')).
create_term(ASTNode,Type,TermConstructor,VariableIds,DeferredActions) :-
	create_term(ASTNode,Type,TermConstructor,[],VariableIds,DeferredActions).

/**
	@signature create_term(ASTNode,Type,TermConstructor,OldVariables,NewVariables,DeferredActions)
*/
create_term(ASTNode,_Type,int_value(Value),VarIds,VarIds,_DeferredActions) :-	
	integer_value(ASTNode,Value),!.
create_term(ASTNode,_Type,float_value(Value),VarIds,VarIds,_DeferredActions) :-
	float_atom(ASTNode,Value),!.

create_term(ASTNode,do_not_cache,string_atom(Value),VarIds,VarIds,_DeferredActions) :- 	
	string_atom(ASTNode,Value),!.
create_term(ASTNode,do_not_cache_root,string_atom(Value),VarIds,VarIds,_DeferredActions) :- 	
	string_atom(ASTNode,Value),!.	
create_term(ASTNode,cache,TermConstructor,VarIds,VarIds,DeferredActions) :- 	
	string_atom(ASTNode,Value),!,
	create_term_for_cacheable_string_atom(Value,TermConstructor,DeferredActions).

create_term(
		ASTNode,
		do_not_cache,
		compound_term(string_atom(Functor),ArgsConstructors),
		OldVarIds,NewVarIds,
		DeferredActions
	) :-
	compound_term(ASTNode,Functor,Args),
	create_terms(Args,do_not_cache,ArgsConstructors,OldVarIds,NewVarIds,DeferredActions),!.
create_term(
		ASTNode,
		do_not_cache_root,
		compound_term(string_atom(Functor),ArgsConstructors),
		OldVarIds,NewVarIds,
		DeferredActions) :-
	compound_term(ASTNode,Functor,Args),
	create_terms(Args,cache,ArgsConstructors,OldVarIds,NewVarIds,DeferredActions),!.
create_term(ASTNode,cache,TermConstructor,OldVarIds,NewVarIds,DeferredActions) :-
	compound_term(ASTNode,Functor,Args),
	create_term_for_cacheable_string_atom(
		Functor,
		FunctorConstructor,
		DeferredActions),
	TC = compound_term(FunctorConstructor,ArgsConstructors),
	(	is_ground_term(ASTNode) ->
		create_terms(
			Args,
			do_not_cache,
			ArgsConstructors,
			OldVarIds,NewVarIds,
			DeferredActions),
		TermConstructor = pre_created_term(CTId),
		add_to_set_ol(create_field_for_pre_created_term(CTId,TC),DeferredActions)
	;
		create_terms(
			Args,
			cache,
			ArgsConstructors,
			OldVarIds,NewVarIds,
			DeferredActions),
		TermConstructor = TC
	),!.

create_term(ASTNode,_,anonymous_variable,VarIds,VarIds,_DeferredActions) :- 
	anonymous_variable(ASTNode,_VariableName),!.	

create_term(ASTNode,_Type,VariableId,OldVarIds,NewVarIds,_DeferredActions) :- 
	is_variable(ASTNode),!,
	lookup_in_term_meta(variable_id(VariableId),ASTNode),
	add_to_set(VariableId,OldVarIds,NewVarIds).	

create_term(ASTNode,Type,_,_,_,_) :-
	throw(internal_error(
		create_term/6,
		['the type (',Type,') of the ASTNode (',ASTNode,') is unknown'])).



create_terms(
		[Arg|Args],
		Type,
		[TermConstructor|TermConstructors],
		OldVarIds,NewVarIds,
		DeferredActions
	) :- !,
	create_term(Arg,Type,TermConstructor,OldVarIds,IVarIds,DeferredActions),
	create_terms(Args,Type,TermConstructors,IVarIds,NewVarIds,DeferredActions).
create_terms([],_Type,[],VarIds,VarIds,_DeferredActions).



create_term_for_cacheable_string_atom(
		StringValue,
		TermConstructor,
		DeferredActions
	) :- 
	(
		predefined_functor(StringValue) ->
		TermConstructor = string_atom(StringValue)
	;	
		TermConstructor = pre_created_term(Id),
		add_to_set_ol(
			create_field_for_pre_created_term(Id,string_atom(StringValue)),
			DeferredActions)
	).



/* *************************************************************************** *\

	HELPER METHODS RELATED TO CONTROLLING AND DETERMING A CLAUSE'S CONTROL FLOW

\* *************************************************************************** */



goal_call_case_id(GoalNumber,CallCaseId) :- CallCaseId is GoalNumber * 2.



goal_redo_case_id(GoalNumber,RedoCaseId) :- RedoCaseId is GoalNumber * 2 + 1.



/**
	Returns the list of all (primitive) goals of the given term.<br />
	The first element of the list is always the primitive goal that would be 
	executed first, if we would call the term as a whole.

	@signature primitive_goals_list(ASTNode,SGoals_HeadOfListOfASTNodes,SRest_TailOfThePreviousList)
*/
primitive_goals_list(ASTNode,SGoal,SRest) :- 
	compound_term(ASTNode,Functor,[LASTNode,RASTNode]),
	(	
		Functor = ',' 
	; 
		Functor = ';' 
	),
	!,
	primitive_goals_list(LASTNode,SGoal,SFurtherGoals),
	primitive_goals_list(RASTNode,SFurtherGoals,SRest).
primitive_goals_list(ASTNode,[ASTNode|SRest],SRest).



/**
	Associates each primitive goal with a unique number; the information
	is added to a goal's meta information.

	Typical usage: <code>number_primitive_goals(ASTNode,0,L)</code>
*/
number_primitive_goals(ASTNode,First,Last) :-
	compound_term(ASTNode,Functor,[LASTNode,RASTNode]),
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
	compound_term(ASTNode,',',[LASTNode,RASTNode]),!,
	first_primitive_goal(RASTNode,FR),
	last_primitive_goals_if_true(LASTNode,LeftLPGTs,[]),
	last_primitive_goal_if_false(RASTNode,RightLPGF),
	set_successors(DeferredActions,LeftLPGTs,succeeds,[FR]),
	set_successors(DeferredActions,[RightLPGF],fails(redo),LeftLPGTs),
	set_primitive_goals_successors(DeferredActions,LASTNode),
	set_primitive_goals_successors(DeferredActions,RASTNode).
set_primitive_goals_successors(DeferredActions,ASTNode) :-
	compound_term(ASTNode,';',[LASTNode,RASTNode]),!,
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
	lookup_in_term_meta(goal_number(GoalNumber),SuccessorASTNode),
	add_to_term_meta(next_goal_if_succeeds(GoalNumber),ASTNode),
	set_successor(DeferredActions,ASTNode,succeeds,SuccessorASTNodes).
set_successor(_DeferredActions,_ASTNode,succeeds,[]).
% Action is either "call" or "redo"
set_successor(DeferredActions,ASTNode,fails(Action),SuccessorASTNodes) :-
	term_meta(ASTNode,Meta),
	(	SuccessorASTNodes = [SuccessorASTNode], % ... may fail
		term_meta(SuccessorASTNode,SuccessorMeta),
		lookup_in_meta(goal_number(GoalNumber),SuccessorMeta),
		add_to_meta(next_goal_if_fails(Action,GoalNumber),Meta)
	;
		lookup_in_meta(goal_number(GoalNumber),Meta),
		add_to_set_ol(create_field_to_store_predecessor_goal(GoalNumber),DeferredActions),
		add_to_meta(next_goal_if_fails(Action,multiple),Meta),
		add_to_each_term_meta(not_unique_predecessor_goal,SuccessorASTNodes)
	),!.