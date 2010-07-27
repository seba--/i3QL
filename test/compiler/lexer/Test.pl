/* Tests the lexer. */

:- ensure_loaded('src/compiler/Lexer.pl').


do_tokenization(SimpleFileName,Ts,Options) :-
	atomic_list_concat(['test/compiler/lexer/data/',SimpleFileName],File),
	tokenize_file(File,Ts,Options).


:- begin_tests(lexer).

test(	string_atoms,
		[true( 
			Ts = [
				sa('IllegalCharacters:()[]{},;.:|',1,0),
				sa('a',2,0),
				sa('aaaaaaaaaaaaaaaaaaaaaaaaVVVVVVVVVVEEEEEEERRRRRRRRRYYYYYY_LLLLLLLLLLLLLLOOOOOOOOOOOOOOOOGGGGGGGGGGGGnnnnnnnnnnnaaaaaaaaaaaammmmmmmmmmmeeeeeeee',3,0),
				sa(abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ,4,0)
			]
		)]
	) :- do_tokenization('StringAtoms.pl',Ts,[]).


test(	empty_file, 
		[true(Ts=[])]
	) :- do_tokenization('Empty.pl',Ts,[]).


test(	numbers, 
		[true(Ts=[
			i(0, 1, 0),
			i(1, 3, 0),
			i(123456789, 5, 0),
			f(1.2e+11, 7, 0),
			f(1.2e+11, 9, 0),
			f(1.2e-09, 11, 0),
			f(12.34, 13, 0),
			f(1234.0, 15, 0),
			f(1234.0, 17, 0),
			f(12.34e-2, 19, 0)
			]
		)]
	) :- do_tokenization('Numbers.pl',Ts,[]).


test(	ignore_comments_while_parsing,
		[true(Ts=[
			sa(a, 3, 0),
			'(',
			sa(b, 3, 25),
			')',
			o('.', 3, 27)])
		]
	) :- do_tokenization('Comments.pl',Ts,[]).


test(	report_comments,
		[true(Ts=[
			eolc(' an end of line comment', 1, 0), 
			ws('\n', 2, 0), 
			sa(a, 3, 0),
			'(',
			mlc(' an inline comment ', 3, 2),
			sa(b, 3, 25),
			')',
			o('.', 3, 27),
			ws('\n', 3, 28),
			ws('\n',4,0),
			mlc('\na multi-line comment\n', 5, 0)
			])
		]
	) :- do_tokenization('Comments.pl',Ts,[white_space(retain_all)]).


test(	report_sc_comments,
		[true(Ts=[
			sc([
				ws('\n', 2, 3), ws('\t', 3, 0), tf('A', 3, 8), ws(' ', 3, 9), 
				tf(structured, 3, 10), ws(' ', 3, 20), tf('comment.', 3, 21), 3<29,
				tf(br, 3, 30), ws(' ', 3, 32), 3/33, 3>34, ws('\n', 3, 35),
				ws('\t', 4, 0), tf('A', 4, 8), ws(' ', 4, 9), tf('link:', 4, 10), 
				ws(' ', 4, 15), '{'(4, 16), @(4, 17), tf(link, 4, 18), 
				ws(' ', 4, 22), tf(tokenize_with_sc, 4, 23), 4/39, tf('2', 4, 40), 
				'}'(4, 41), ws('\n', 4, 42), ws('\t', 5, 0), ws('\n', 5, 8), 
				ws('\t', 6, 0), @(6, 8), tf(signature, 6, 9), ws(' ', 6, 18), 
				tf(tokenize, 6, 19), '('(6, 27), tf('Stream', 6, 28), (6, 34), 
				tf('Tokens', 6, 35), (6, 41), tf('No', 6, 42), ')'(6, 44), 
				ws('\n', 6, 45), ws('\t', 7, 0), @(7, 8), tf(arg, 7, 9), 
				ws(' ', 7, 12), tf('No', 7, 13), ws(' ', 7, 15), tf(a, 7, 16), 
				ws(' ', 7, 17), tf(number, 7, 18), ws('\n', 7, 24), ws('\t', 8, 0), 
				@(8, 8), tf(arg, 8, 9), '('(8, 12), tf(in, 8, 13), ')'(8, 15), 
				ws(' ', 8, 16), tf('Stream', 8, 17), ws(' ', 8, 23), tf(a, 8, 24), 
				ws(' ', 8, 25), tf(stream, 8, 26), ws('\n', 8, 32), ws('\t', 9, 0), 
				@(9, 8), tf(arg, 9, 9), '('(9, 12), tf(out, 9, 13), ')'(9, 16), 
				ws(' ', 9, 17), tf('Tokens', 9, 18), ws(' ', 9, 24), tf(the, 9, 25), 
				ws(' ', 9, 28), tf(tokens, 9, 29), ws('\n', 9, 35)], 2, 0)
			])
		]
	) :- do_tokenization('StructuredComments.pl',Ts,[white_space(retain_sc)]).


test(	operators,
		[true(Ts=[
			o(-->, 1, 0),
			o(:-, 2, 0),
			o(:-, 3, 0), 
			o(?-, 4, 0),
			o(;, 5, 0),
			o(,, 6, 0),
			o(|, 7, 0), 
			o(->, 8, 0), 
			o(\+, 9, 0), 
			o(~, 10, 0), 
			o(<, 11, 0), 
			o(=, 12, 0), 
			o(=.., 13, 0), 
			o(=@=, 14, 0), 
			o(=:=, 15, 0), 
			o(=<, 16, 0), 
			o(==, 17, 0), 
			o(=\=, 18, 0), 
			o(>, 19, 0), 
			o(>=, 20, 0), 
			o(@<, 21, 0), 
			o(@=<, 22, 0), 
			o(@>, 23, 0), 
			o(@>=, 24, 0), 
			o(\=, 25, 0), 
			o(\==, 26, 0), 
			sa(is, 27, 0), 
			o(:, 28, 0),
			o(+, 29, 0),
			o(-, 30, 0), 
			o(/\, 31, 0), 
			o(\/, 32, 0), 
			sa(xor, 33, 0), 
			o(><, 34, 0), 
			o(?, 35, 0), 
			o(*, 36, 0), 
			o(/, 37, 0), 
			o(//, 38, 0), 
			sa(rdiv, 39, 0),
			o(<<, 40, 0), 
			o(>>, 41, 0), 
			sa(mod, 42, 0), 
			sa(rem, 43, 0),
			o(**, 44, 0), 
			o(^, 45, 0), 
			o(+, 46, 0)])
		]
	) :- do_tokenization('Operators.pl',Ts,[]).


test(	operators_in_context,
		[true(Ts=[
			sa(o, 1, 0), '(', o(-->, 1, 2), o(,, 1, 5), i(1, 1, 7), ')', 
			sa(o, 2, 0), '(', o(:-, 2, 2), o(,, 2, 4), i(2, 2, 6), ')', 
			sa(o, 3, 0), '(', o(:-, 3, 2), o(,, 3, 4), i(3, 3, 6), ')', 
			sa(o, 4, 0), '(', o(?-, 4, 2), o(,, 4, 4), i(4, 4, 6), ')', 
			sa(o, 5, 0), '(', o(;, 5, 2), o(,, 5, 3), i(5, 5, 5), ')', 
			sa(o, 6, 0), '(', o(,, 6, 2), o(,, 6, 3), i(6, 6, 5), ')', 
			sa(o, 7, 0), '(', o(|, 7, 2), o(,, 7, 3), i(7, 7, 5), ')',
			sa(o, 8, 0), '(', o(->, 8, 2), o(,, 8, 4), i(8, 8, 6), ')', 
			sa(o, 9, 0), '(', o(\+, 9, 2), o(,, 9, 4), i(9, 9, 6), ')', 
			sa(o, 10, 0), '(', o(~, 10, 2), o(,, 10, 3), i(10, 10, 5), ')', 
			sa(o, 11, 0), '(', o(<, 11, 2), o(,, 11, 3), i(11, 11, 5), ')', 
			sa(o, 12, 0), '(', o(=, 12, 2), o(,, 12, 3), i(12, 12, 5), ')',
			sa(o, 13, 0), '(', o(=.., 13, 2), o(,, 13, 5), i(13, 13, 7), ')',
			sa(o, 14, 0), '(', o(=@=, 14, 2), o(,, 14, 5), i(14, 14, 7), ')',
			sa(o, 15, 0), '(', o(=:=, 15, 2), o(,, 15, 5), i(15, 15, 7), ')', 
			sa(o, 16, 0), '(', o(=<, 16, 2), o(,, 16, 4), i(16, 16, 6), ')',
			sa(o, 17, 0), '(', o(==, 17, 2), o(,, 17, 4), i(17, 17, 6), ')', 
			sa(o, 18, 0), '(', o(=\=, 18, 2), o(,, 18, 5), i(18, 18, 7), ')', 
			sa(o, 19, 0), '(', o(>, 19, 2), o(,, 19, 3), i(19, 19, 5), ')', 
			sa(o, 20, 0), '(', o(>=, 20, 2), o(,, 20, 4), i(20, 20, 6), ')', 
			sa(o, 21, 0), '(', o(@<, 21, 2), o(,, 21, 4), i(21, 21, 6), ')', 
			sa(o, 22, 0), '(', o(@=<, 22, 2), o(,, 22, 5), i(22, 22, 7), ')', 
			sa(o, 23, 0), '(', o(@>, 23, 2), o(,, 23, 4), i(23, 23, 6), ')', 
			sa(o, 24, 0), '(', o(@>=, 24, 2), o(,, 24, 5), i(24, 24, 7), ')', 
			sa(o, 25, 0), '(', o(\=, 25, 2), o(,, 25, 4), i(25, 25, 6), ')', 
			sa(o, 26, 0), '(', o(\==, 26, 2), o(,, 26, 5), i(26, 26, 7), ')',
			sa(o, 27, 0), '(', o(:, 27, 2), o(,, 27, 3), i(28, 27, 5), ')',
			sa(o, 28, 0), '(', o(+, 28, 2), o(,, 28, 3), i(29, 28, 5), ')', 
			sa(o, 29, 0), '(', o(-, 29, 2), o(,, 29, 3), i(30, 29, 5), ')',
			sa(o, 30, 0), '(', o(/\, 30, 2), o(,, 30, 4), i(31, 30, 6), ')', 
			sa(o, 31, 0), '(', o(\/, 31, 2), o(,, 31, 4), i(32, 31, 6), ')', 
			sa(o, 32, 0), '(', o(><, 32, 2), o(,, 32, 4), i(34, 32, 6), ')', 
			sa(o, 33, 0), '(', o(?, 33, 2), o(,, 33, 3), i(35, 33, 5), ')', 
			sa(o, 34, 0), '(', o(*, 34, 2), o(,, 34, 3), i(36, 34, 5), ')', 
			sa(o, 35, 0), '(', o(/, 35, 2), o(,, 35, 3), i(37, 35, 5), ')', 
			sa(o, 36, 0), '(', o(//, 36, 2), o(,, 36, 4), i(38, 36, 6), ')', 
			sa(o, 37, 0), '(', o(<<, 37, 2), o(,, 37, 4), i(40, 37, 6), ')', 
			sa(o, 38, 0), '(', o(>>, 38, 2), o(,, 38, 4), i(41, 38, 6), ')', 
			sa(o, 39, 0), '(', o(**, 39, 2), o(,, 39, 4), i(44, 39, 6), ')', 
			sa(o, 40, 0), '(', o(^, 40, 2), o(,, 40, 3), i(45, 40, 5), ')', 
			sa(o, 41, 0), '(', o(+, 41, 2), o(,, 41, 3), i(46, 41, 5), ')'
			])
		]
	) :- do_tokenization('OperatorsInContext.pl',Ts,[]).


test(rg_test_Example_pl) :- do_tokenization('Example.pl',_Ts,[]).


test(rg_test_Test_pl) :- tokenize_file('test/compiler/lexer/Test.pl',_Ts).


:- end_tests(lexer).