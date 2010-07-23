/* Tests the lexer. */

:- ensure_loaded('src/compiler/Lexer.pl').


do_tokenization(SimpleFileName,KeepWhiteSpace,Ts) :-
	atomic_list_concat(['test/compiler/lexer/data/',SimpleFileName],File),
	tokenize_file(File,KeepWhiteSpace,Ts).


:- begin_tests(lexer).

test(	string_atoms,
		[true( 
			Ts = [
				sa('IllegalCharacters:()[]{},;.:|',1,1),
				sa('a',2,1),
				sa('aaaaaaaaaaaaaaaaaaaaaaaaVVVVVVVVVVEEEEEEERRRRRRRRRYYYYYY_LLLLLLLLLLLLLLOOOOOOOOOOOOOOOOGGGGGGGGGGGGnnnnnnnnnnnaaaaaaaaaaaammmmmmmmmmmeeeeeeee',3,1),
				sa(abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ,4,1)
			]
		)]
	) :- do_tokenization('StringAtoms.pl',false,Ts).


test(	empty_file, 
		[true(Ts=[])]
	) :- do_tokenization('Empty.pl',false,Ts).


test(	numbers, 
		[true(Ts=[
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
	) :- do_tokenization('Numbers.pl',false,Ts).


test(	ignore_comments_while_parsing,
		[true(Ts=[
			sa(a, 3, 1),
			'(',
			sa(b, 3, 26),
			')',
			o('.', 3, 28)])
		]
	) :- do_tokenization('Comments.pl',false,Ts).


test(	report_comments,
		[true(Ts=[
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
	) :- do_tokenization('Comments.pl',true,Ts).


test(	operators,
		[true(Ts=[
			o(-->, 1, 1),
			o(:-, 2, 1),
			o(:-, 3, 1), 
			o(?-, 4, 1),
			o(;, 5, 1),
			o(,, 6, 1),
			o(|, 7, 1), 
			o(->, 8, 1), 
			o(\+, 9, 1), 
			o(~, 10, 1), 
			o(<, 11, 1), 
			o(=, 12, 1), 
			o(=.., 13, 1), 
			o(=@=, 14, 1), 
			o(=:=, 15, 1), 
			o(=<, 16, 1), 
			o(==, 17, 1), 
			o(=\=, 18, 1), 
			o(>, 19, 1), 
			o(>=, 20, 1), 
			o(@<, 21, 1), 
			o(@=<, 22, 1), 
			o(@>, 23, 1), 
			o(@>=, 24, 1), 
			o(\=, 25, 1), 
			o(\==, 26, 1), 
			sa(is, 27, 1), 
			o(:, 28, 1),
			o(+, 29, 1),
			o(-, 30, 1), 
			o(/\, 31, 1), 
			o(\/, 32, 1), 
			sa(xor, 33, 1), 
			o(><, 34, 1), 
			o(?, 35, 1), 
			o(*, 36, 1), 
			o(/, 37, 1), 
			o(//, 38, 1), 
			sa(rdiv, 39, 1),
			o(<<, 40, 1), 
			o(>>, 41, 1), 
			sa(mod, 42, 1), 
			sa(rem, 43, 1),
			o(**, 44, 1), 
			o(^, 45, 1), 
			o(+, 46, 1)])
		]
	) :- do_tokenization('Operators.pl',false,Ts).


:- end_tests(lexer).