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
	Loads an SAE Prolog program's source files and creates the overall AST.
	
	@author Michael Eichberg
*/
:- module(sae_load_program,[pl_load/4]).

:- use_module('../AST.pl').
:- use_module('../Debug.pl').
:- use_module('../Utils.pl').
:- use_module('../Lexer.pl',[tokenize_file/2]).
:- use_module('../Parser.pl',[clauses/2]).
:- use_module('../Predef.pl',[add_predefined_predicates_to_ast/2]).



/**
	Loads an SAE Prolog program's source files and creates the overall AST. 
	The AST consists only of directives and rule definitions.
	
	@arg(in) DebugConfig is the list of debug information that should be emitted.
		Possible values are: 'on_entry', 'ast(user)','ast(built_in)' and 'reading_file'.
	@arg(in) Files is the list of (all) files of the SAE Prolog program. All 
		files will be loaded. The file names have to be expanded and absolute.
	@arg(unused) _OutputFolders 
	@arg(out) AST is the AST of the program including all
		predefined predicates.
*/
pl_load(DebugConfig,Files,_OutputFolders,AST) :-
	debug_message(
			DebugConfig,
			on_entry,
			write('[Debug] Phase: Loading source files___________________________________\n')),
	findall(
		Clauses,
		(
			member(File,Files),
			debug_message(DebugConfig,memberchk(reading_file),write_atomic_list(['[Debug] Reading file: ',File,'\n'])),
			tokenize_file(File,Tokens),
			clauses(Tokens,Clauses)
		),
		ListOfListOfClauses
	),
	% iterate over all terms of all ASTs and create a global ast
	build_ast(ListOfListOfClauses,AST0),
	add_predefined_predicates_to_ast(AST0,AST),
	
	debug_message(DebugConfig,ast(user),write_ast(user,AST)),
	debug_message(DebugConfig,ast(built_in),write_ast(built_in,AST)).



/* ************************************************************************** *\
 *                P R I V A T E     I M P L E M E N T A T I O N               *
\* ************************************************************************** */



build_ast(ListOfListOfClauses,TheAST) :-
	empty_ast(EmptyAST),
	process_list_of_list_of_clauses(ListOfListOfClauses,EmptyAST,TheAST).



process_list_of_list_of_clauses(
	[ListOfClauses|FurtherListsOfListsOfClauses],CurrentAST,FinalAST
) :- 
	!,
	process_clauses(ListOfClauses,CurrentAST,IntermediateAST),
	process_list_of_list_of_clauses(
		FurtherListsOfListsOfClauses,IntermediateAST,FinalAST
	).
process_list_of_list_of_clauses([],AST,AST).	



process_clauses([Clause|Clauses],CurrentAST,FinalAST) :- 
	is_directive(Clause),!, 
	add_clause(CurrentAST,Clause,IntermediateAST),
	process_clauses(Clauses,IntermediateAST,FinalAST).
process_clauses([Clause|Clauses],CurrentAST,FinalAST) :- 
	/* assert is_rule(Clause) */ !,
	normalize_rule(Clause,NormalizedClause),
	add_clause(CurrentAST,NormalizedClause,IntermediateAST),
	process_clauses(Clauses,IntermediateAST,FinalAST).
process_clauses([],AST,AST).



/* ************************************************************************** *\
 *                                                                            *
 *              -----------------------------------------------               *   
 *              N O R M A L I Z I N G   P R O L O G   T E R M S               *   
 *              -----------------------------------------------               *   
 *                                                                            *
\* ************************************************************************** */



/**
	Normalizes a rule. <br />
	In a normalized rule the head's arguments are all variables where no
	variable occurs more than once. Further, every rule is transformed into
	"clausal form"; i.e., it is always a right side which may just be <code>true</code>.
	
	@signature normalize_rule(Rule,NormalizedRule)
	@arg Rule The old rule.
	@arg NormalizedRule The normalized variant of the rule.
*/
normalize_rule(Rule0,NormalizedRule) :-
	term_meta(Rule0,Meta0),
	rule_with_head_and_body(Rule0,Rule1),
	remove_head_unification(Rule1,Rule2),
	rule_head(Rule2,Head2),
	rule_body(Rule2,Body2),
	left_descending_goal_sequence(Body2,LeftDescendingBody),
	no_trailing_true_goals(LeftDescendingBody,OptimizedBody),
	rule(Head2,OptimizedBody,Meta0,NormalizedRule).



/**
	If the rule does not (yet) have a body, a body is created.

	@signature rule_with_head_and_body(Rule,RuleWithHeadAndBody)
	@arg(in) Rule A valid rule.
	@arg(out) RuleWithHeadAndBody A rule with a head and a body.
*/
rule_with_head_and_body(Rule,Rule) :-
	is_rule_with_body(Rule),!.
rule_with_head_and_body(Clause,RuleHB) :-
	is_rule_without_body(Clause),!,
	term_pos(Clause,Pos),
	string_atom('true',Pos,Body),
	pos_meta(Pos,Meta),
	rule(Clause,Body,Meta,RuleHB).
rule_with_head_and_body(Clause,_) :- % to catch errors early on...
	throw(internal_error('the clause is not a rule',Clause)).



remove_head_unification(
		ct(IMI,':-',[ct(HMI,Functor,HeadArgs),Body]),
		ct(IMI,':-',[ct(HMI,Functor,NewHeadArgs),NewBody])
) :- 	
	/* not_empty(HeadArgs), */ !,
	normalize_arguments(HeadArgs,1,HeadArgs,NewHeadArgs,Body,NewBody).	
remove_head_unification(ASTNode,ASTNode).



/**
 	@signature normalize_arguments(HeadArgs,HeadArgId,RemainingHeadArgs,NewHeadArgs,Body,NewBody) 
*/
normalize_arguments(_,_,[],[],Body,Body) :- !.
normalize_arguments(AllHeadArgs,Id,[HArg|HArgs],NewHeadArgs,Body,NewBody) :-
	(
		(	
			\+ ( is_variable(HArg) ; is_anonymous_variable(HArg) )
		;
			variable(HArg,VariableName),
			\+ is_first_occurence_of_variable_in_head(VariableName,AllHeadArgs,1,Id)
		)	->  
			term_meta(HArg,Meta),
			clone_meta(Meta,HeadVariableMeta), 
			clone_meta(Meta,BodyVariableMeta), 
			variable('$H',Id,HeadVariableMeta,NewHeadVariableNode),
			variable('$H',Id,BodyVariableMeta,NewBodyVariableNode),
			NewHeadArgs = [NewHeadVariableNode|FurtherNewHeadArgs],
			clone_meta(Meta,UnifyMeta),
			clone_meta(Meta,AndMeta),
			NewBody = ct(AndMeta,',',[ct(UnifyMeta,'=',[NewBodyVariableNode,HArg]),RestOfBody])
		;	
			NewHeadArgs=[HArg|FurtherNewHeadArgs],
			NewBody = RestOfBody
	),
	NewId is Id + 1,
	normalize_arguments(AllHeadArgs,NewId,HArgs,FurtherNewHeadArgs,Body,RestOfBody).



/**
	Succeeds if a variable is not used by an argument with index [1..MaxId).
	<p>
	Requires that all arguments with index [1..MaxId) are variables. The behavior
	of this predicate is not defined if a previous argument
	is a compound term where the variable is used as an argument.
	</p>

	@signature is_first_occurence_of_variable(VariableName,Args,Id,MaxID)
	@arg VariableName The name of a variable for which it is checked if it is
		already used by a previous argument.
	@arg(in) Args the list of (all) arguments. 
	@arg(int) ID The id (1-based) of the argument which is checked.
	@arg(int) MaxID The id of the last argument which is checked for the 
		definition of a variable.
*/
is_first_occurence_of_variable_in_head(_,_,ID,MaxID) :- ID >= MaxID,!.
is_first_occurence_of_variable_in_head(VariableName,[Arg|Args],ID,MaxID) :- 
	Arg \= v(_,VariableName),
	NewID is ID + 1,
	is_first_occurence_of_variable_in_head(VariableName,Args,NewID,MaxID).



/**
	We are performing a tree rotation of "and-goals" to facilitate some analyses
	(e.g., tail call related analyses).
*/
left_descending_goal_sequence(ct(TMI,',',[LNode,ct(RMI,',',[RLNode,RRNode])]),NewNode) :- !,
	left_descending_goal_sequence(ct(RMI,',',[ct(TMI,',',[LNode,RLNode]),RRNode]),NewNode).
left_descending_goal_sequence(Node,Node).



/**
	Removes all trailing true goals. Requires that the goal sequence is left descending.
*/
no_trailing_true_goals(ct(_MI,',',[Goal,a(_AMI,'true')]),GoalSeqWithoutTrailingTrueGoals) :- !,
	no_trailing_true_goals(Goal,GoalSeqWithoutTrailingTrueGoals).
no_trailing_true_goals(GoalSeqWithoutTrailingTrueGoals,GoalSeqWithoutTrailingTrueGoals).
	
	
	