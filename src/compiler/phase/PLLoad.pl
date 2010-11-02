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
	Loads and creates the AST of an SAE Prolog program.
	
	@author Michael Eichberg
*/
:- module('SAEProlog:Compiler:Phase:Load',[pl_load/4]).

:- use_module('../Debug.pl').
:- use_module('../AST.pl',[new_ast/1,add_term_to_ast/3,write_ast/2]).
:- use_module('../Lexer.pl',[tokenize_file/2]).
:- use_module('../Parser.pl',[program/2]).
:- use_module('../Predef.pl',[add_predefined_predicates_to_ast/2]).



/**
	Loads and creates the AST of a SAE Prolog program. 
	
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
	% iterate over all terms of all asts and create a global ast
	create_ast(ASTs,GlobalAST),
	add_predefined_predicates_to_ast(GlobalAST,Program),
	debug_message(DebugConfig,ast(user),write_ast(user,Program)),
	debug_message(DebugConfig,ast(built_in),write_ast(built_in,Program)).



/* ************************************************************************** *\
 *                P R I V A T E     I M P L E M E N T A T I O N               *
\* ************************************************************************** */



create_ast(ASTs,TheAST) :-
	new_ast(EmptyAST),
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


normalize_term(Term0,NormalizedTerm) :-
	as_clause(Term0,NormalizedTerm).


/** Private
	If it is a fact, make it a clause, where the right side is just 'true'.

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

	



/* normalize_clause(OldClause,NewClause) :- normalizes the representation of a
 	clause. <br />
	In a normalized clause the header's arguments are all variables where no
	variable occurs more than once. 
	
	@param OldClause the old clause.
	@param NewClause is the normalized representation of OldClause.
* /
normalize_clause(((H :- B),ClauseProperties),NewClause) :- 
	functor(H,Functor,Arity),
	functor(NewH,Functor,Arity),
	normalize_arguments(H,NewH,B,NewB),
	normalize_goal_sequence_order(NewB,NormalizedGoalOrderB),
	remove_trailing_true_calls(NormalizedGoalOrderB,MinimizedB),
	NewClause = ':-'(NewH  MinimizedB),ClauseProperties).
	
	
	

normalize_arguments(H,NewH,B,NewB) :- 
	functor(H,_,AID),
	normalize_arguments(AID,H,NewH,B,NewB).

normalize_arguments(0,_,_,B,B) :- !.
normalize_arguments(AID,H,NewH,B,FinalB) :-
	AID > 0,	NewAID is AID - 1,
	arg(AID,H,Arg),
	(	(var(Arg),is_first_occurence_of_variable_as_term_argument(NewAID,H,Arg)) ->
		(
			arg(AID,NewH,Arg),
			NewB=B
		);(
			arg(AID,NewH,X),
			NewB = ((X = Arg),B)
		)
	),
	normalize_arguments(NewAID,H,NewH,NewB,FinalB).




/ * is_first_occurence_of_variable_as_term_argument(+AID,+T,+Var) :- tests if no 
	argument of a given term T in the range [1..AID] is the variable Var.
	
 	@param AID is the upper bound of the argument index.
	@param T a term.
	@param Var an (unbound) variable.
* /
is_first_occurence_of_variable_as_term_argument(0,_,_) :- !.
is_first_occurence_of_variable_as_term_argument(AID,T,Var) :-
	arg(AID,T,Arg),
	Arg \== Var,
	NewAID is AID - 1,
	is_first_occurence_of_variable_as_term_argument(NewAID,T,Var).	




/ * normalize_goal_sequence_order(Body,NewBody) :- performs a tree rotation to 
	make sure that	a clause's last goal – if the last goal is unique – is the
	immediate right child element of top-level "and" node.
	
	<p>
	<b>Example</b><br />
	E.g., the goal sequence (A,B,C,D,E) <=>
	(A,(B,(C,(D,E))) is transformed to ((((A, B), C), D), E). This representation
	is advantegeous because some analyses do need to analyze a goal sequence
	except of the "last goal". Hence, after this transformation the goal
	sequence G can be unified with G=(PreviousGoals,LastGoal) to get the sequence
	of all goals except of the last goal. 
	</p>
* /
normalize_goal_sequence_order(B,NewB) :- 
		var(B) -> NewB = B
	;
		(B = (L,R), nonvar(R), R=(RL,RR)) -> ( % we have to avoid accidental bindings
			normalize_goal_sequence_order((L,RL),NewL),
			IB = (NewL,RR),
			normalize_goal_sequence_order(IB,NewB)
		)
	;
		B = (L;R) -> (
			normalize_goal_sequence_order(L,NewLB),
			normalize_goal_sequence_order(R,NewRB),
			NewB = (NewLB;NewRB)
		)
	;
		NewB = B.

	


remove_trailing_true_calls(Term,NewTerm) :-
		var(Term) -> NewTerm = Term 
	;
		Term = (T,true) -> (ITerm = T, remove_trailing_true_calls(ITerm,NewTerm))
	;
		NewTerm = Term.
*/