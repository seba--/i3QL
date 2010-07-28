/* Tests the parser. */


:- ensure_loaded('src/compiler/Lexer.pl').
:- ensure_loaded('src/compiler/debug/Lexer.pl').
:- ensure_loaded('src/compiler/Parser.pl').



:- begin_tests(parser).

test(
	stress_test_1,
	[	true(P=[pre(\+, 1, 0, ct(\+, [a(a, 1, 6), ct(b, [infix(;, 1, 11, a(c, 1, 10), a(d, 1, 12)), a(',', 1, 14)], 1, 8)], 1, 3))])
	]
) :- tokenize_string("\\+ \\+(a,b(c;d,,)).",Ts),program(Ts,P).


test(
	lists_1,
	[	true(P=[a([], 1, 0)])
	]
) :- tokenize_string("[].",Ts),program(Ts,P).


test(
	lists_2,
	[	true(P=[[av('_', 1, 1)]])
	]
) :- tokenize_string("[_].",Ts),program(Ts,P).


test(
	lists_3,
	[	true(P=[[a(a, 1, 1), a(b, 1, 3), a(c, 1, 5), infix(;, 1, 8, a(d, 1, 7), a(e, 1, 9))]]) ]
) :- tokenize_string("[a,b,c,d;e].",Ts),program(Ts,P).


test(
	lists_4,
	[	true(P=[[infix(;, 1, 2, a(a, 1, 1), a(e, 1, 3))|av('_', 1, 5)]]) ]
) :- tokenize_string("[a;e|_].",Ts),program(Ts,P).


test(
	lists_5,
	[	true(P=[[infix(;, 1, 2, a(a, 1, 1), a(e, 1, 3)), a(b, 1, 5)|infix(;, 1, 8, a(a, 1, 7), infix(',', 1, 10, a(e, 1, 9), a(b, 1, 11)))]])
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