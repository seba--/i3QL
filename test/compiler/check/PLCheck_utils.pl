:- module(sae_check_test_utils,
	[test_valid_prolog_file/1,
	test_not_valid_prolog_file/1,
	test_valid_prolog_sys_programs/1,
	test_valid_prolog_clause/1,
	test_not_valid_prolog_clause/1]).

:- ensure_loaded('src/prolog/compiler/Lexer.pl').
:- ensure_loaded('src/prolog/compiler/Parser.pl').
:- ensure_loaded('src/prolog/compiler/AST.pl').
:- ensure_loaded('src/prolog/compiler/Utils.pl').
:- ensure_loaded('src/prolog/compiler/phase/PLCheck.pl').
:- ensure_loaded('src/prolog/compiler/phase/PLLoad.pl').
:- ensure_loaded('src/prolog/compiler/Predef.pl').

        	 
test_valid_prolog_file(File) :-
	atomic_list_concat(['test/compiler/check/data/',File,'.pl'], CompletFile),
	test_valid_prolog_files([CompletFile]).
test_valid_prolog_files(Files) :-
	pl_load([],Files,_,AST),
	pl_check([],AST,_OutputFolder,AST).
test_not_valid_prolog_file(File) :-
	not(test_valid_prolog_file(File)).

test_valid_prolog_sys_programs(File):-
	atomic_list_concat(['test/system/programs/',File,'.pl'], CompletFile),
	test_valid_prolog_files([CompletFile]).

%CHECK: not working for all PrologClauses!	
test_valid_prolog_clause(PrologClause) :-
	tokenize_string(PrologClause,Ts),
	clauses(Ts,Clause),
	build_ast([Clause],AST0),
	add_predefined_predicates_to_ast(AST0,AST),
	pl_check([],AST,_,AST).
test_not_valid_prolog_clause(Clause) :-
	not(test_valid_prolog_clause(Clause)).
	
	
/*
copyed predicats from PLLoad 
*/
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
	"clausal form"; i.e., each clause always has a right side which may just
	be <code>true</code>.
	
	@signature normalize_rule(Rule,NormalizedRule)
	@arg(in,+,ASTNode) Rule The old rule.
	@arg(out,?,ASTNode) NormalizedRule The normalized variant of the rule.
*/
normalize_rule(Rule0,NormalizedRule) :-
	term_meta(Rule0,Meta0),
	rule_with_head_and_body(Rule0,Rule1),
	remove_head_unification(Rule1,Rule2),
	rule_head(Rule2,Head2),
	rule_body(Rule2,Body2),
	transform_term(
		Body2,
		replace_unification_with_anonymous_variable_by_true_goal,
		remove_superfluous_true_and_fail_goals,
		Body3),
	left_descending_goal_sequence(Body3,LeftDescendingBody),
	rule(Head2,LeftDescendingBody,Meta0,NormalizedRule).



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
	@arg(in,+) Args the list of (all) arguments. 
	@arg(in,+,int) ID The id (1-based) of the argument which is checked.
	@arg(in,+,int) MaxID The id of the last argument which is checked for the 
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
% TODO reevaluate if this analysis/transformation is still necessary (useful)
left_descending_goal_sequence(ct(TMI,',',[LNode,ct(RMI,',',[RLNode,RRNode])]),NewNode) :- !,
	left_descending_goal_sequence(ct(RMI,',',[ct(TMI,',',[LNode,RLNode]),RRNode]),NewNode).
left_descending_goal_sequence(Node,Node).



remove_superfluous_true_and_fail_goals(ASTNode,NewASTNode) :-
	compound_term(ASTNode,',',[LASTNode,NewASTNode]),
	string_atom(LASTNode,'true'),
	!.
remove_superfluous_true_and_fail_goals(ASTNode,NewASTNode) :-
	compound_term(ASTNode,',',[NewASTNode,RASTNode]),
	string_atom(RASTNode,'true'),
	!.
remove_superfluous_true_and_fail_goals(ASTNode,NewASTNode) :-
	compound_term(ASTNode,';',[LASTNode,NewASTNode]),
	(string_atom(LASTNode,'false') ;  string_atom(LASTNode,'fail')),
	!.
remove_superfluous_true_and_fail_goals(ASTNode,NewASTNode) :-
	compound_term(ASTNode,';',[NewASTNode,RASTNode]),
	(string_atom(RASTNode,'false') ;  string_atom(RASTNode,'fail')),
	!.		
remove_superfluous_true_and_fail_goals(ASTNode,ASTNode).
	
	
replace_unification_with_anonymous_variable_by_true_goal(ASTNode,NewASTNode) :-
	compound_term(ASTNode,'=',[LASTNode,RASTNode]),
	( is_anonymous_variable(LASTNode) ; is_anonymous_variable(RASTNode) ),
	!, % TODO replace using "if -> then" predicate (verify that it works)
	term_meta(ASTNode,Meta),
	string_atom(NewASTNode,'true'),
	term_meta(NewASTNode,Meta),
	% Generate warning message...
	term_pos(ASTNode,File,LN,_CN),	
	% TODO extract predicate "print_warning(POS,MSG)"		
	(	nonvar(File),File \== [],nonvar(LN) ->	
		atomic_list_concat(
				[	File,':',LN,
					': warning: useless unification with an anonymous variable\n'
				],
				MSG)% GCC compliant
	;
		atomic_list_concat(
				[	'Warning: useless unification with an anonymous variable\n' ],
				MSG)% GCC compliant
	),
   write(MSG)
	.
replace_unification_with_anonymous_variable_by_true_goal(ASTNode,ASTNode).