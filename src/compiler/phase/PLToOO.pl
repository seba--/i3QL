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
:- module('SAEProlog:Compiler:Phase:PhaseLtoOO',[pl_to_oo/4]).

:- use_module('../AST.pl').
:- use_module('../Predef.pl').
:- use_module('../Utils.pl').
:- use_module('../Debug.pl').


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
	abort_pending_goals_and_clear_goal_stack
	push_onto_goal_stack(GoalExpression)
	remove_top_element_from_goal_stack
	forever(Statements)
	eol_comment(Comment)
	switch(Expression,CaseStatements) 
	expression_statement(Expression)
	return(Expression)
	local_variable_decl(Type,Name,Expression)
	if(Condition,Statements)
	if(Condition,TrueStatements,FalseStatements)
	error(ErrorDescription) - to signal an programmer's error (e.g., if the developer tries to evaluate a non-arithmetic term.)
	
	<h2>EXPRESSIONS</h2>
	get_top_element_from_goal_stack 
	assignment(LValue,Expression)
	method_call(ReceiverExpression,Identifier,Expressions)
	new_object(Type,Expressions) - Expressions is a list of expressions; for each constructor argument an expression has to be given.
	field_ref(Receiver,Identifier)
	local_variable_ref(Identifier)
	null
	self - the self reference ("this" in Java)
	string(Value) - a string value in the target language (not a string atom)
	int(Value) - an int value in the target language (not a Prolog int value)
	boolean(Value) - a boolean value in the target language (not a Prolog boolean value)
	call_term(TermExpression)
	predicate_lookup(Functor,Arity,TermExpressions)
	
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
	% FIELDS
	gen_fields_for_the_control_flow_and_evaluation_state(Program,Predicate,S1,S2),
	gen_fields_for_predicate_arguments(Program,Predicate,S2,S3),
	% METHODS
	S3 = [SConstructor,SAbortMethod,SChoiceCommittedMethod,SClauseSelectorMethod|S4],
	gen_predicate_constructor(Program,Predicate,SConstructor),
	gen_abort_method(Program,Predicate,SAbortMethod),
	gen_choice_committed_method(Program,Predicate,SChoiceCommittedMethod),
	gen_clause_selector_method(Program,Predicate,SClauseSelectorMethod),
	gen_clause_impl_methods(Program,Predicate,S4),
	OOAST = oo_ast([
		class_decl(PredicateIdentifier,type(goal),S1),
		predicate_registration(PredicateIdentifier)
		]),
	predicate_meta(Predicate,Meta),
	add_to_meta(OOAST,Meta).	



/*

	F I E L D S

*/

gen_fields_for_the_control_flow_and_evaluation_state(_Program,Predicate,SFieldDecls,SR) :-
	predicate_clauses(Predicate,Clauses),
	(	single_clause(Clauses) ->
		ClauseToExecute = eol_comment('this predicate is implemented by a single clause')
	;
		ClauseToExecute = field_decl([],type(int),'clauseToExecute',int(1))
	),	
	SFieldDecls = [
		ClauseToExecute,
		field_decl(goal_stack),
		field_decl([],type(int),'goalToExecute',int(1)),
		field_decl([],type(boolean),'cutEvaluation',boolean('false')) |
		SR
	].


gen_fields_for_predicate_arguments(_Program,Predicate,SFieldDecls,SR) :-
	predicate_identifier(Predicate,PredicateIdentifier),
	PredicateIdentifier = _Functor/Arity,
	call_foreach_i_in_0_to_u(Arity,field_decl_for_pred_arg_i,SFieldDecls,SR).


field_decl_for_pred_arg_i(I,field_decl([final],type(term),FieldName)) :-
	atom_concat(arg,I,FieldName).



/*

	C O N S T R U C T O R
	
*/
gen_predicate_constructor(_Program,Predicate,constructor_decl(PredicateIdentifier,ParamDecls,Stmts)) :-
	predicate_identifier(Predicate,PredicateIdentifier),
	PredicateIdentifier = _Functor/Arity,
	call_foreach_i_in_0_to_u(Arity,constructor_param_decl_for_arg_i,ParamDecls),
	call_foreach_i_in_0_to_u(Arity,init_field_of_arg_i,Stmts).
	

constructor_param_decl_for_arg_i(I,param_decl(type(term),ParamName)) :- 
	atom_concat(arg,I,ParamName).
	
	
init_field_of_arg_i(
		I,
		expression_statement(
			assignment(
				field_ref(self,ArgName),
				local_variable_ref(ArgName)))) :-
	atom_concat(arg,I,ArgName).



/*

	"void abort()" M E T H O D

*/	
gen_abort_method(_Program,_Predicate,AbortMethod) :-
	AbortMethod = 
		method_decl(
			public,
			type(void),
			'abort',
			[],
			[
				abort_pending_goals_and_clear_goal_stack
			]).



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
	(	single_clause(Clauses) ->
		ClauseSelectorMethod = 
			method_decl(
				public,
				type(boolean),
				'next',
				[],
				[ return(method_call(self,'clause1',[])) ])
	;
		ClauseSelectorMethod = 
			method_decl(
				public,
				type(boolean),
				'next',
				[],
				[
					switch(field_ref(self,'clauseToExecute'),CaseStmts)
				]),
		foreach_clause(Clauses,selector_for_clause_i,CaseStmts)
	).
	


selector_for_clause_i(I,_Clause,last,case(int(I),Stmts)) :- !,
	% if this is the last clause, we don't care if the evaluation was "cutted" or not
	atom_concat('clause',I,ClauseIdentifier),
	Stmts = [return(method_call(self,ClauseIdentifier,[]))].
selector_for_clause_i(I,Clause,_ClausePosition,case(int(I),Stmts)) :-
	atom_concat('clause',I,ClauseIdentifier),
	NextClauseId is I + 1,
	PrepareForNextClause = [
		expression_statement(assignment(field_ref(self,'clauseToExecute'),int(NextClauseId))),
		expression_statement(assignment(field_ref(self,'goalToExecute'),int(1)))
	],
	(
		clause_meta(Clause,Meta) , lookup_in_meta(cut(never),Meta) ->
		ClauseFailed = [eol_comment('this clause contains no "cut"') |PrepareForNextClause]
	;
		ClauseFailed = [
			if(field_ref(self,'cutEvaluation'),
				[return(boolean(false))],
				PrepareForNextClause
			)
		]
	),
	Stmts = [
		if(method_call(self,ClauseIdentifier,[]),
			[return(boolean(true))],
			ClauseFailed
		)
	].




/*

	"boolean clauseX()" M E T H O D S      ( T H E   C L A U S E  I M P L E M E N T A T I O N S )

*/
gen_clause_impl_methods(_Program,Predicate,ClauseImpls) :-
	predicate_clauses(Predicate,Clauses),
	foreach_clause(Clauses,implementation_for_clause_i,ClauseImpls).
	
implementation_for_clause_i(I,Clause,ClausePosition,ClauseMethod) :-
	atom_concat('clause',I,ClauseIdentifier),
	ClauseMethod = 
		method_decl(
			private,
			type(boolean),
			ClauseIdentifier,
			[],
			[ forever([switch(field_ref(self,'goalToExecute'),Cases)]) ]),
			
	clause_definition(Clause,ClauseDefinition),
	rule_body(ClauseDefinition,Body),		
(ClauseIdentifier = 'clause1' -> write(Body),nl ; true),	
	create_term(Body,TermConstructor,MappedVariableNames),
(ClauseIdentifier = 'clause1' -> write(TermConstructor),nl ; true),
write(MappedVariableNames),	nl,
	create_clause_variables(MappedVariableNames,ClauseVariableDecls,SR),	
write(SR),write('  ~  '),write(ClauseVariableDecls),nl,
	SR =
	[
		push_onto_goal_stack(call_term(TermConstructor)),
		expression_statement(
			assignment(field_ref(self,'goalToExecute'),int(2)))
	],
	(	ClausePosition = last -> 
		CutAnalysis = empty
	;
		CutAnalysis = expression_statement(
			assignment(
				field_ref(self,'cutEvaluation'),
				method_call(get_top_element_from_goal_stack,'choiceCommitted',[])))
	),
	Cases=[
		case(
			int(1), % GOAL PREPARATION
			ClauseVariableDecls
		),
		case(
			int(2), % GOAL EXECUTION
			[
				local_variable_decl(
					type(boolean),
					'succeeded',
					method_call(get_top_element_from_goal_stack,'next',[])),
				if(local_variable_ref('succeeded'),[return(boolean(true))]),
				CutAnalysis,
				remove_top_element_from_goal_stack,
				return(boolean(false))
			]
		)
	].
	


/**
	@signature create_clause_variables(MappedBodyVariableNames,SClauseLocalVariables,SZ).
*/
create_clause_variables([MappedBodyVariableName|MappedBodyVariableNames],SClauseLocalVariables,SZ) :-
	(	MappedBodyVariableName = clv(_I),
		mapped_variable_name_to_variable_identifier(MappedBodyVariableName,VI),
		SClauseLocalVariables = [local_variable_decl(type(variable),VI,variable)|SI]
	;
		SClauseLocalVariables = SI
	),!,
	create_clause_variables(MappedBodyVariableNames,SI,SZ).
create_clause_variables([],SZ,SZ).


%	clause_local_variables(NamedHeadVariables,BodyVariables,SClauseLocalVariables,SR).

/*

clause_local_variables(HeadVariables,[BodyVariable|BodyVariables],SClauseVariables,SR) :-
	(
		\+ memberchk(BodyVariable,HeadVariables),
		ClauseVariable = local_variable_decl(type(variable),BodyVariable,variable),
		SClauseVariables = [ClauseVariable|SX],
		clause_local_variables(HeadVariables,BodyVariables,SX,SR)
	;
		clause_local_variables(HeadVariables,BodyVariables,SClauseVariables,SR)
	),!.
clause_local_variables(_,[],SR,SR).
*/


/*
predicate_args_variables_mapping(Id,[HeadVariable|HeadVariables],[SHeadVariableMapping|SX],SR) :- !,
	(	variable(HeadVariable,HeadVariableName), % fails if a head variable is anonymous
		atom_concat('arg',Id,ArgName),
		SHeadVariableMapping=local_variable_decl(type(term),HeadVariableName,field_ref(self,ArgName))
	;
		atomic_list_concat(['arg',Id,' is not used'],Comment),
		SHeadVariableMapping = eol_comment(Comment)
	),!,
	NewId is Id + 1,
	predicate_args_variables_mapping(NewId,HeadVariables,SX,SR).
predicate_args_variables_mapping(_Id,[],SR,SR).
*/



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
	
	
	
mapped_variable_name_to_variable_identifier(arg(I),VariableName) :- !,
	atom_concat(arg,I,VariableName).
mapped_variable_name_to_variable_identifier(clv(I),VariableName) :- !,
	atom_concat(clv,I,VariableName).
mapped_variable_name_to_variable_identifier(X,_) :-
	throw(internal_error(['unsupported mapped variable name: ',X])).