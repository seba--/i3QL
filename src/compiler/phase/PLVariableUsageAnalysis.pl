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
	Maps the names of the variables of a normalized clause to a set of unified 
	names (arg(0)...arg(N) and clv(0)...clv(M)). Adds the new name to the variables
	meta information (mapped_variable_name(Name)). Further, the information about
	the number of body variables is added to the clauses' meta information.
	
	@author Michael Eichberg
*/
:- module('Compiler:Phase:PLVariableUsageAnalysis',[pl_variable_usage_analysis/4]).

:- use_module('../AST.pl').
:- use_module('../Debug.pl').
:- use_module('../Predef.pl').
:- use_module('../Utils.pl').



pl_variable_usage_analysis(DebugConfig,Program,_OutputFolder,Program) :-
	debug_message(DebugConfig,on_entry,write('\n[Debug] Phase: Variable Usage Analysis______________________________________\n')),
	foreach_user_predicate(Program,process_predicate(DebugConfig)),
	!/* we are finished... */.



process_predicate(DebugConfig,Predicate) :-
	predicate_identifier(Predicate,PredicateIdentifier),
	term_to_atom(PredicateIdentifier,PredicateIdentifierAtom),
	debug_message(DebugConfig,processing_predicate,write_atomic_list(['[Debug] Processing Predicate: ',PredicateIdentifierAtom,'\n'])),
	
	predicate_clauses(Predicate,Clauses),	
	foreach_clause(Clauses,analyze_variable_usage,NumberOfLocalVariablesOfClauses),
	max_list(NumberOfLocalVariablesOfClauses,Max),
	predicate_meta(Predicate,Meta),
	add_to_meta(maximum_number_of_clause_local_variables(Max),Meta).



analyze_variable_usage(_ClauseId,Clause,_RelativeClausePosition,ClauseLocalVariablesCount) :-
	clause_implementation(Clause,Implementation),
	clause_meta(Clause,Meta),	
	
	rule_head(Implementation,HeadASTNode),
	complex_term_args(HeadASTNode,AllHeadVariables), % since the clause is normalized, the head only contains (anonymous) variable declarations
	map_names_of_head_variables(0,AllHeadVariables,HeadVariablesCountExpr,VariableNamesMapping),
	HeadVariablesCount is HeadVariablesCountExpr,
	add_to_meta(used_head_variables_count(HeadVariablesCount),Meta),
	
	rule_body(Implementation,BodyASTNode),
	named_variables_of_term(BodyASTNode,AllBodyVariables,[]),
	map_names_of_body_variables(0,AllBodyVariables,ClauseLocalVariablesCount,VariableNamesMapping),

	add_to_meta(clause_local_variables_count(ClauseLocalVariablesCount),Meta).



/**
	@arg FinalHeadVariablesCount is a (complex) term that can be evaluated
*/
map_names_of_head_variables(_Id,[],0,_VariableNamesMapping).
map_names_of_head_variables(Id,[HeadVariable|HeadVariables],FinalHeadVariablesCount,VariableNamesMapping) :-
	( 	variable(HeadVariable,HeadVariableName) ->
		MappedVariableName = mapped_variable_name(arg(Id)),
		lookup(HeadVariableName,VariableNamesMapping,MappedVariableName),
		term_meta(HeadVariable,Meta),
		add_to_meta(MappedVariableName,Meta),
		FinalHeadVariablesCount = HeadVariablesCount + 1
	;
		FinalHeadVariablesCount = HeadVariablesCount
	),
	NewId is Id + 1,
	map_names_of_head_variables(NewId,HeadVariables,HeadVariablesCount,VariableNamesMapping).



/** 
	@signature map_names_of_body_variables(0,AllBodyVariables,ClauseLocalVariablesCount,VariableNamesMapping)
*/
map_names_of_body_variables(Id,[],Id,_VariableNamesMapping).	
map_names_of_body_variables(Id,[Variable|Variables],ClauseLocalVariablesCount,VariableNamesMapping) :-
	variable(Variable,VariableName),
	lookup(VariableName,VariableNamesMapping,MappedVariableName),
	(	var(MappedVariableName) ->
		MappedVariableName = mapped_variable_name(clv(Id)),
		NewId is Id + 1
	;
		NewId = Id
	),
	term_meta(Variable,Meta),
	add_to_meta(MappedVariableName,Meta),
	map_names_of_body_variables(NewId,Variables,ClauseLocalVariablesCount,VariableNamesMapping).