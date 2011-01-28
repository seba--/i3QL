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
	Associates each (named) Prolog variable - i.e., variables that are not 
	anonymous -	with a unique id.<br />
	In case of head variables (variables defined in a clause's	head), the ids
	are "arg(X)" where X identifies the argument's postion (0 based). In case of
	local variables, the ids are "clv(Y)". <br />
	The variable id is added to the variable's meta information 
	(mapped_variable_id(Name)).
	<p>
	Further, the information about the number of body variables is added to the
	clauses' meta information.
	</p>
	
	<p>
	<b>Implementation Note</b><br/>
	If we use Variable (or VariableId) we refer to the unique id associated with 
	each variable. We use VariableNode to refer to the AST node that represents
	the variable.
	</p>
	
	@author Michael Eichberg
*/
:- module(
		sae_analyze_variables,
		[
			pl_analyze_variables/4,
			mapped_variable_ids/2
		]
	).

:- use_module('../AST.pl').
:- use_module('../Debug.pl').
:- use_module('../Predef.pl').
:- use_module('../Utils.pl').



pl_analyze_variables(DebugConfig,Program,_OutputFolder,Program) :-
	debug_message(
			DebugConfig,
			on_entry,
			write('\n[Debug] Phase: Variable Usage Analysis______________________________________\n')),
	foreach_user_predicate(Program,process_predicate(DebugConfig)),
	!/* we are finished... */.



process_predicate(DebugConfig,Predicate) :-
	predicate_identifier(Predicate,Functor/Arity),
	debug_message(
		DebugConfig,
		processing_predicate,
		write_atomic_list(['[Debug] Processing Predicate: ',Functor,'/',Arity,'\n'])),
	
	predicate_clauses(Predicate,Clauses),	
	foreach_clause(Clauses,analyze_variable_usage,NumberOfLocalVariablesOfClauses),
	max_list(NumberOfLocalVariablesOfClauses,Max),
	add_to_predicate_meta(maximum_number_of_clause_local_variables(Max),Predicate).



analyze_variable_usage(_ClauseId,Clause,_RelativeClausePosition,ClauseLocalVariablesCount) :-
	clause_implementation(Clause,Implementation),
	
	rule_head(Implementation,HeadASTNode),
	(	is_string_atom(HeadASTNode) ->
		HeadVariablesCount = 0
		% VariableIds remains free...
	;
		compound_term_args(HeadASTNode,AllHeadVariablesNodes), % since the clause is normalized, the head only contains (anonymous) variable declarations
		map_names_of_head_variables(0,AllHeadVariablesNodes,HeadVariablesCountExpr,VariableNamesToIdsMap),
		HeadVariablesCount is HeadVariablesCountExpr,
		dictionary_values(VariableNamesToIdsMap,HeadVariables)
	),
	add_to_clause_meta(used_head_variables_count(HeadVariablesCount),Clause),
	
	rule_body(Implementation,BodyASTNode),
	named_variables_of_term(BodyASTNode,AllBodyVariablesNodes,[]),
	map_names_of_body_variables(0,AllBodyVariablesNodes,ClauseLocalVariablesCount,VariableNamesToIdsMap),
	add_to_clause_meta(clause_local_variables_count(ClauseLocalVariablesCount),Clause),
	
	intra_clause_variable_usage(
			BodyASTNode,
			HeadVariables,[],HeadVariables,
			_UsedVariables,_PotentiallyUsedVariables,VariablesUsedOnlyOnce),
			
	write_warning_variables_used_only_once(Clause,VariablesUsedOnlyOnce,VariableNamesToIdsMap).



write_warning_variables_used_only_once(_Clause,[],_VariableNamesToIdsMappping).
write_warning_variables_used_only_once(
		Clause,
		[VariableIdUsedOnlyOnce|VariableIdsUsedOnlyOnce],
		VariableNamesToIdsMappping
	) :-
	clause_implementation(Clause,ASTNode),
	term_pos(ASTNode,File,LN,_CN),
	dictionary_identity_lookup_key(
			VariableIdUsedOnlyOnce,
			VariableNamesToIdsMappping,
			VariableName),!,
	atomic_list_concat(
			[	File,':',LN,
				': warning: the variable ',VariableName,' is only used once\n'
			],
			MSG),% GCC compliant
   write(MSG),
	write_warning_variables_used_only_once(
			Clause,
			VariableIdsUsedOnlyOnce,
			VariableNamesToIdsMappping).



map_names_of_head_variables(_Id,[],0,_VariableNamesToIdsMap).
map_names_of_head_variables(Id,[HeadVariable|HeadVariables],FinalHeadVariablesCount,VariableNamesToIdsMap) :-
	( 	variable(HeadVariable,HeadVariableName) ->
		VariableId = arg(Id),
		lookup(HeadVariableName,VariableNamesToIdsMap,VariableId),
		add_to_term_meta(variable_id(VariableId),HeadVariable),
		FinalHeadVariablesCount = HeadVariablesCount + 1
	;
		FinalHeadVariablesCount = HeadVariablesCount
	),
	NewId is Id + 1,
	map_names_of_head_variables(NewId,HeadVariables,HeadVariablesCount,VariableNamesToIdsMap).



/** 
	@signature map_names_of_body_variables(Id,AllBodyVariablesNodes,ClauseLocalVariablesCount,VariableNamesToIdsMap)
*/
map_names_of_body_variables(Id,[],Id,_VariableNamesToIdsMap).	
map_names_of_body_variables(
		Id,
		[VariableNode|VariablesNodes],
		ClauseLocalVariablesCount,VariableNamesToIdsMap
	) :-
	variable(VariableNode,VariableName),
	lookup(VariableName,VariableNamesToIdsMap,VariableId),
	(	var(VariableId) ->
		VariableId = clv(Id),
		NewId is Id + 1
	;
		NewId = Id
	),
	add_to_term_meta(variable_id(VariableId),VariableNode),
	map_names_of_body_variables(NewId,VariablesNodes,ClauseLocalVariablesCount,VariableNamesToIdsMap).



intra_clause_variable_usage(
		ASTNode,
		UsedVariables,
		PotentiallyUsedVariables,
		VariablesUsedOnlyOnce,
		NewUsedVariables,
		NewPotentiallyUsedVariables,
		NewVariablesUsedOnlyOnce
	) :-
	compound_term(ASTNode,',',[LASTNode,RASTNode]),!,
	intra_clause_variable_usage(
			LASTNode,
			UsedVariables,PotentiallyUsedVariables,VariablesUsedOnlyOnce,
			UV1,PUV1,UOV1),
	intra_clause_variable_usage(
			RASTNode,
			UV1,PUV1,UOV1,
			NewUsedVariables,NewPotentiallyUsedVariables,NewVariablesUsedOnlyOnce).

intra_clause_variable_usage(
		ASTNode,
		UsedVariables,
		PotentiallyUsedVariables,
		VariablesUsedOnlyOnce,
		NewUsedVariables,
		NewPotentiallyUsedVariables,
		NewVariablesUsedOnlyOnce
	) :-
	compound_term(ASTNode,';',[LASTNode,RASTNode]),!,
	(
		(compound_term(LASTNode,Functor,[_,_]) ;
		compound_term(RASTNode,Functor,[_,_]) ) ,
		(Functor == '->' ; Functor == '*->') ->
		throw(to_be_implemented)
	;
		true
	),
	intra_clause_variable_usage(
			LASTNode,
			UsedVariables,PotentiallyUsedVariables,VariablesUsedOnlyOnce,
			UV1,PUV1,UOV1),
	intra_clause_variable_usage(
			RASTNode,
			UsedVariables,PotentiallyUsedVariables,VariablesUsedOnlyOnce,
			UV2,PUV2,UOV2),
	intersect_sets(UV1,UV2,NewUsedVariables),
	merge_sets(PUV1,PUV2,MPUV_1_2),
	% add those variables that are only used on one branch
	set_subtract(UV1,NewUsedVariables,LPUV),
	set_subtract(UV2,NewUsedVariables,RPUV),
	merge_sets(LPUV,RPUV,LRPUV),
	merge_sets(MPUV_1_2,LRPUV,NewPotentiallyUsedVariables),
	% update the set of variables that are used only once
	merge_sets(UOV1,UOV2,NewVariablesUsedOnlyOnce).

intra_clause_variable_usage(
		ASTNode,
		PreviouslyUsedVariables,
		PotentiallyUsedVariables,
		VariablesUsedOnlyOnce,
		NewUsedVariables,
		NewPotentiallyUsedVariables,
		NewVariablesUsedOnlyOnce
	) :-
	is_compound_term(ASTNode),!, 
	named_variables_of_term(ASTNode,VariableNodes,[]),
	mapped_variable_ids(VariableNodes,UsedVariables,VariableIdsUsedMoreThanOnce),
	% determine the variables that are used for the first time by this goal...
	set_subtract(UsedVariables,PreviouslyUsedVariables,IVariables),
	set_subtract(IVariables,PotentiallyUsedVariables,FirstTimeUsedVariables),
	add_to_term_meta(variables_used_for_the_first_time(FirstTimeUsedVariables),ASTNode),
	add_to_term_meta(variables_that_may_have_been_used(PotentiallyUsedVariables),ASTNode),
	% update the set of definitively used variables
	merge_sets(PreviouslyUsedVariables,UsedVariables,NewUsedVariables),
	% remove from the set of "potentially used" variables those that are "used"
	set_subtract(PotentiallyUsedVariables,UsedVariables,NewPotentiallyUsedVariables),
	% update the set of variables that are used only once
	set_subtract(VariablesUsedOnlyOnce,UsedVariables,IUOV),
	set_subtract(FirstTimeUsedVariables,VariableIdsUsedMoreThanOnce,IFTUV),
	merge_sets(IUOV,IFTUV,NewVariablesUsedOnlyOnce).	

intra_clause_variable_usage(
		_ASTNode, % if we reach this clause, ASTNode has to be an atomic ..
		UsedVariables,
		PotentiallyUsedVariables,
		VariablesUsedOnlyOnce,		
		UsedVariables,
		PotentiallyUsedVariables,
		VariablesUsedOnlyOnce
	).



mapped_variable_ids(VariableNodes,VariableIds) :-
	mapped_variable_ids(VariableNodes,[],VariableIds,_VariableIdsUsedMoreThanOnce).



mapped_variable_ids(VariableNodes,VariableIds,VariableIdsUsedMoreThanOnce) :-
	mapped_variable_ids(VariableNodes,[],VariableIds,VariableIdsUsedMoreThanOnce).



mapped_variable_ids([],VariableIds,VariableIds,[]).
mapped_variable_ids(
		[VariableNode|VariableNodes],
		VariableIds,
		NewVariableIds,
		VariableIdsUsedMoreThanOnce) :-
	lookup_in_term_meta(variable_id(VariableId),VariableNode),
	add_to_set(VariableId,VariableIds,IVariableIds,VariableIdWasInSet),
	(	VariableIdWasInSet ->
		VariableIdsUsedMoreThanOnce = [VariableId|MoreVariableIdsUsedMoreThanOnce]
	;
		VariableIdsUsedMoreThanOnce = MoreVariableIdsUsedMoreThanOnce
	),
	mapped_variable_ids(VariableNodes,IVariableIds,NewVariableIds,MoreVariableIdsUsedMoreThanOnce).
	
	
	
	
	
	
	
	