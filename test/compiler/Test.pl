:- ensure_loaded('test/compiler/lexer/Test.pl').
:- ensure_loaded('test/compiler/parser/Test.pl').


run_all_compiler_tests :-
	run_tests(lexer),
	run_tests(parser).
	
	
:- run_all_compiler_tests.
	