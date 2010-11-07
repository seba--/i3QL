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
:- module('SAEProlog:Compiler:Phase:Load',[pl_load/4]).

:- use_module('../Debug.pl').
:- use_module('../Utils.pl').
:- use_module('../AST.pl').
:- use_module('../Lexer.pl',[tokenize_file/2]).
:- use_module('../Parser.pl',[program/2]).
:- use_module('../Predef.pl',[add_predefined_predicates_to_ast/2]).



/**
	Loads an SAE Prolog program's source files and creates the overall AST. 
	The AST consists only of normalized top-level terms.
	
	@arg(in) Debug is the list of debug information that should be emitted.
		Possible values are: 'on_entry', 'ast(user)','ast(built_in)' and 'reading_file'.
	@arg(in) Files is the list of (all) files of the SAE Prolog program. All 
		files will be loaded. The file names have to be expanded and absolute.
	@arg(unused) _OutputFolders 
	@arg(out) Program is the AST of the program including all
		predefined predicates.
*/
pl_load(DebugConfig,Files,_OutputFolders,Program) :-
	debug_message(DebugConfig,on_entry,write('[Debug] Phase: Loading source files___________________________________\n')),
	findall(
		AST,
		(
			member(File,Files),
			debug_message(DebugConfig,memberchk(reading_file),write_atomic_list(['[Debug] Reading file: ',File,'\n'])),
			tokenize_file(File,Tokens),
			program(Tokens,AST)
		),
		ASTs
	),
	% iterate over all terms of all ASTs and create a global ast
	global_ast(ASTs,GlobalAST),
	add_predefined_predicates_to_ast(GlobalAST,Program),
	debug_message(DebugConfig,ast(user),write_ast(user,Program)),
	debug_message(DebugConfig,ast(built_in),write_ast(built_in,Program)).



/* ************************************************************************** *\
 *                P R I V A T E     I M P L E M E N T A T I O N               *
\* ************************************************************************** */



global_ast(ASTs,TheAST) :-
	empty_ast(EmptyAST),
	process_asts(ASTs,EmptyAST,TheAST).



process_asts([AnAST|ASTs],CurrentAST,FinalAST) :- !,
	process_terms(AnAST,CurrentAST,IntermediateAST),
	process_asts(ASTs,IntermediateAST,FinalAST).
process_asts([],AST,AST).	



process_terms([Term|Terms],CurrentAST,FinalAST) :- !,
	normalize_term(Term,NormalizedTerm),
	add_term_to_ast(CurrentAST,NormalizedTerm,IntermediateAST),
	process_terms(Terms,IntermediateAST,FinalAST).
process_terms([],AST,AST).



/* ************************************************************************** *\
 *                                                                            *
 *              -----------------------------------------------               *   
 *              N O R M A L I Z I N G   P R O L O G   T E R M S               *   
 *              -----------------------------------------------               *   
 *                                                                            *
\* ************************************************************************** */



/**
	Normalizes a (top-level) term. <br />
	In a normalized term the header's arguments are all variables where no
	variable occurs more than once. Further, every term is transformed into
	"clausal form"; i.e., it is always an implication with a left and a right 
	side.
	
	@signature normalize_term(Term,NormalizedTerm)
	@arg Term The old term.
	@arg NormalizedTerm The normalized variant of term.
*/
normalize_term(Term0,ct(Pos,':-',[Head,OptimizedBody])) :-
	as_clause(Term0,Term1),
	remove_head_unification(Term1,ct(Pos,':-',[Head,Body])),
	left_descending_goal_sequence(Body,LeftDescendingBody),
	no_trailing_true_goals(LeftDescendingBody,OptimizedBody).



/**
	If the term is not an implication, it is transformed into one where the right
	side (the body) is <code>true</code>.

	@signature as_clause(Term,Clause)
	@arg(in) Term A valid top-level term.
	@arg(out) Clause A valid top-level clause.
*/
as_clause(
		ct(Pos,Functor,Args),
		ct(Pos,':-',[ct(Pos,Functor,Args),a(Pos,'true')])
) :- Functor \= (':-'),!.
as_clause(
		a(Pos,Functor),
		ct(Pos,':-',[a(Pos,Functor),a(Pos,'true')])
) :- !.
as_clause(Term,Term).



remove_head_unification(
		ct(IPos,':-',[ct(HPos,Functor,HeadArgs),Body]),
		ct(IPos,':-',[ct(HPos,Functor,NewHeadArgs),NewBody])
) :- 	
	/* not_empty(HeadArgs), */ !,
	normalize_arguments(HeadArgs,1,HeadArgs,NewHeadArgs,Body,NewBody).	
remove_head_unification(TermNode,TermNode).



/**
 	@signature normalize_arguments(HeadArgs,HeadArgId,RemainingHeadArgs,NewHeadArgs,Body,NewBody) 
*/
normalize_arguments(_,_,[],[],Body,Body) :- !.
normalize_arguments(AllHeadArgs,Id,[HArg|HArgs],NewHeadArgs,Body,NewBody) :-
	(
		(	
			HArg \= v(_Pos,_)
		;
			HArg = v(_VPos,VariableName),
			\+ is_first_occurence_of_variable(VariableName,AllHeadArgs,1,Id)
		)	->  
			term_pos(HArg,Pos),
			variable_node(Pos,'&H',Id,NewVariableNode),
			NewHeadArgs = [NewVariableNode|FurtherNewHeadArgs],
			NewBody = ct(Pos,',',[ct(Pos,'=',[NewVariableNode,HArg]),RestOfBody])
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
	is a complex term where the variable is used as an argument.
	</p>

	@signature is_first_occurence_of_variable(VariableName,Args,Id,MaxID)
	@arg VariableName The name of a variable for which it is checked if it is
		already used by a previous argument.
	@arg(in) Args the list of (all) arguments. 
	@arg(int) ID The id (1-based) of the argument which is checked.
	@arg(int) MaxID The id of the last argument which is checked for the 
		definition of a variable.
*/
is_first_occurence_of_variable(_,_,ID,MaxID) :- ID >= MaxID,!.
is_first_occurence_of_variable(VariableName,[Arg|Args],ID,MaxID) :- 
	Arg \= v(_,VariableName),
	NewID is ID + 1,
	is_first_occurence_of_variable(VariableName,Args,NewID,MaxID).



% IMPROVE document... we are preforming a tree rotation to facilitate several analyses
% TODO also make all or paths left_descending and remove trailing true goals
left_descending_goal_sequence(ct(TPos,',',[LNode,ct(RPos,',',[RLNode,RRNode])]),NewNode) :- !,
	left_descending_goal_sequence(ct(RPos,',',[ct(TPos,',',[LNode,RLNode]),RRNode]),NewNode).
left_descending_goal_sequence(Node,Node).



/**
	Removes all trailing true goals. Requires that the goal node is left descending.
*/
no_trailing_true_goals(ct(_Pos,',',[Goal,a(_APos,'true')]),GoalSeqWithoutTrailingTrueGoals) :- !,
	no_trailing_true_goals(Goal,GoalSeqWithoutTrailingTrueGoals).
no_trailing_true_goals(GoalSeqWithoutTrailingTrueGoals,GoalSeqWithoutTrailingTrueGoals).
	
	
	