/* Tests the lexer. */

:- ensure_loaded('src/compiler/Lexer.pl').


do_tokenization(SimpleFileName,KeepWhiteSpace,Ts) :-
	atomic_list_concat(['test/compiler/lexer/data/',SimpleFileName],File),
	tokenize_file(File,KeepWhiteSpace,Ts).


:- begin_tests(lexer).

test(	string_atoms,
		[true( 
			TS = [
				sa('IllegalCharacters:()[]{},;.:|',1,1),
				sa('a',2,1),
				sa('aaaaaaaaaaaaaaaaaaaaaaaaVVVVVVVVVVEEEEEEERRRRRRRRRYYYYYY_LLLLLLLLLLLLLLOOOOOOOOOOOOOOOOGGGGGGGGGGGGnnnnnnnnnnnaaaaaaaaaaaammmmmmmmmmmeeeeeeee',3,1),
				sa(abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ,4,1)
			]
		)]
	) :- do_tokenization('StringAtoms.pl',false,TS).


test(	empty_file, 
		[true(TS=[])]
	) :- do_tokenization('Empty.pl',false,TS).


test(	numbers, 
		[true(TS=[
			n(0, 1, 1),
			n(1, 3, 1),
			n(123456789, 5, 1),
			n(1.2e+11, 7, 1),
			n(1.2e+11, 9, 1),
			n(1.2e-09, 11, 1),
			n(12.34, 13, 1),
			n(1234.0, 15, 1),
			n(1234.0, 17, 1),
			n(12.34e-2, 19, 1)
			]
		)]
	) :- do_tokenization('Numbers.pl',false,TS).


test(	no_comments,
		[true(TS=[
			sa(a, 3, 1),
			'(',
			sa(b, 3, 26),
			')',
			o('.', 3, 28)])
		]
	) :- do_tokenization('Comments.pl',false,TS).


test(	with_comments,
		[true(TS=[
			eolc(' an end of line comment', 1, 1), 
			ws('\n'), 
			sa(a, 3, 1),
			'(',
			mlc(' an inline comment ', 3, 3),
			sa(b, 3, 26),
			')',
			o('.', 3, 28),
			ws('\n'),
			ws('\n'),
			mlc('\na multi-line comment\n', 5, 1)
			])
		]
	) :- do_tokenization('Comments.pl',true,TS).


:- end_tests(lexer).