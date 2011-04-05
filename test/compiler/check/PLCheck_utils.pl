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
	construct_initial_ast([Clause],AST),
	pl_check([],AST,_,AST).
test_not_valid_prolog_clause(Clause) :-
	not(test_valid_prolog_clause(Clause)).
