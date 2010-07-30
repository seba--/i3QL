/* Tests the parser. */


:- ensure_loaded('src/compiler/Lexer.pl').
:- ensure_loaded('src/compiler/debug/Lexer.pl').
:- ensure_loaded('src/compiler/Parser.pl').



:- begin_tests(parser).



test(
	stress_test_1,
	[	true(P=[infix(=, pos(_,1,1), v('X', pos(_,1,0)), infix(*, pos(_,1,3), i(1, pos(_,1,2)), infix(/, pos(_,1,6), i(2, pos(_,1,5)), infix(-, pos(_,1,9), i(3, pos(_,1,8)), i(4, pos(_,1,10))))))])
	]
) :- tokenize_string("X=1*(2/(3-4)).",Ts),program(Ts,P).


test(
	stress_test_2,
	[	true(P=[infix(=, pos(_,1,1), v('X', pos(_,1,0)), infix(',', pos(_,1,4), a(a, pos(_,1,3)), ct(b, [infix(;, pos(_,1,8), a(c, pos(_,1,7)), a(d, pos(_,1,9))), a(',', pos(_,1,11))], pos(_,1,5))))])
	]
) :- tokenize_string("X=(a,b(c;d,,)).",Ts),program(Ts,P).


test(
	stress_test_3,
	[	true(P=[pre(\+, pos(_,1,0), ct(\+, [a(a, pos(_,1,6)), ct(b, [infix(;, pos(_,1,11), a(c, pos(_,1,10)), a(d, pos(_,1,12))), a(',', pos(_,1,14))], pos(_,1,8))], pos(_,1,3)))])
	]
) :- tokenize_string("\\+ \\+(a,b(c;d,,)).",Ts),program(Ts,P).


test(
	lists_1,
	[	true(P=[a([], pos(_,1,0))])
	]
) :- tokenize_string("[].",Ts),program(Ts,P).


test(
	lists_2,
	[	true(P=[[av('_', pos(_,1,1))]])
	]
) :- tokenize_string("[_].",Ts),program(Ts,P).


test(
	lists_3,
	[	true(P=[[a(a, pos(_,1,1)), a(b, pos(_,1,3)), a(c, pos(_,1,5)), infix(;, pos(_,1,8), a(d, pos(_,1,7)), a(e, pos(_,1,9)))]]) ]
) :- tokenize_string("[a,b,c,d;e].",Ts),program(Ts,P).


test(
	lists_4,
	[	true(P=[[infix(;, pos(_,1,2), a(a, pos(_,1,1)), a(e, pos(_,1,3)))|av('_', pos(_,1,5))]]) ]
) :- tokenize_string("[a;e|_].",Ts),program(Ts,P).


test(
	lists_5,
	[	true(P=[[infix(;, pos(_,1,2), a(a, pos(_,1,1)), a(e, pos(_,1,3))), a(b, pos(_,1,5))|infix(;, pos(_,1,8), a(a, pos(_,1,7)), infix(',', pos(_,1,10), a(e, pos(_,1,9)), a(b, pos(_,1,11))))]])
	]
) :- tokenize_string("[a;e,b|a;e,b].",Ts),program(Ts,P).



test('rg_test:benchmarks/arithmetic.pl') :-
	tokenize_file('benchmarks/arithmetic.pl',Ts),program(Ts,_P).


test('rg_test:benchmarks/boyer.pl') :-
	tokenize_file('benchmarks/boyer.pl',Ts),program(Ts,_P).


test('rg_test:benchmarks/chat_parser.pl') :-
	tokenize_file('benchmarks/chat_parser.pl',Ts),program(Ts,_P).


test('rg_test:benchmarks/crypt.pl') :-
	tokenize_file('benchmarks/crypt.pl',Ts),program(Ts,_P).


test('rg_test:benchmarks/deriv.pl') :-
	tokenize_file('benchmarks/deriv.pl',Ts),program(Ts,_P).


test('rg_test:benchmarks/meta_nrev.pl') :-
	tokenize_file('benchmarks/meta_nrev.pl',Ts),program(Ts,_P).


test('rg_test:benchmarks/mu.pl') :-
	tokenize_file('benchmarks/mu.pl',Ts),program(Ts,_P).


test('rg_test:benchmarks/nrev.pl') :-
	tokenize_file('benchmarks/nrev.pl',Ts),program(Ts,_P).


test('rg_test:benchmarks/poly.pl') :-
	tokenize_file('benchmarks/poly.pl',Ts),program(Ts,_P).


test('rg_test:benchmarks/primes.pl') :-
	tokenize_file('benchmarks/primes.pl',Ts),program(Ts,_P).


test('rg_test:benchmarks/qsort.pl') :-
	tokenize_file('benchmarks/qsort.pl',Ts),program(Ts,_P).


test('rg_test:benchmarks/queens.pl') :-
	tokenize_file('benchmarks/queens.pl',Ts),program(Ts,_P).


test('rg_test:benchmarks/reducer.pl') :-
	tokenize_file('benchmarks/reducer.pl',Ts),program(Ts,_P).


test('rg_test:benchmarks/sum.pl') :-
	tokenize_file('benchmarks/sum.pl',Ts),program(Ts,_P).


test('rg_test:benchmarks/tak.pl') :-
	tokenize_file('benchmarks/tak.pl',Ts),program(Ts,_P).


test('rg_test:benchmarks/zebra.pl') :-
	tokenize_file('benchmarks/zebra.pl',Ts),program(Ts,_P).

		
:- end_tests(parser).