/* Tests the parser. */


:- ensure_loaded('src/compiler/Lexer.pl').
:- ensure_loaded('src/compiler/debug/Lexer.pl').
:- ensure_loaded('src/compiler/Parser.pl').



:- begin_tests(parser).



test(
	stress_test_1,
	[	true(P=[ct(pos([], 1, 1), =, [v(pos([], 1, 0), 'X'), ct(pos([], 1, 3), *, [i(pos([], 1, 2), 1), ct(pos([], 1, 6), /, [i(pos([], 1, 5), 2), ct(pos([], 1, 9), -, [i(pos([], 1, 8), 3), i(pos([], 1, 10), 4)])])])])])
	]
) :- tokenize_string("X=1*(2/(3-4)).",Ts),program(Ts,P).


test(
	stress_test_2,
	[	true(P=[ct(pos([], 1, 1), =, [v(pos([], 1, 0), 'X'), ct(pos([], 1, 4), ,, [a(pos([], 1, 3), a), ct(pos([], 1, 5), b, [ct(pos([], 1, 9), ;, [a(pos([], 1, 8), c), a(pos([], 1, 10), d)]), a(pos([], 1, 13), ,)])])])])
	]
) :- tokenize_string("X=(a,b((c;d),,)).",Ts),program(Ts,P).


test(
	stress_test_3,
	[	true(P=[ct(pos([], 1, 1), =, [v(pos([], 1, 0), 'X'), ct(pos([], 1, 2), a, [a(pos([], 1, 4), ,), a(pos([], 1, 6), ,)])])])
	]
) :- tokenize_string("X=a(,,,).",Ts),program(Ts,P).

test(
	stress_test_4,
	[	true(P=[ct(pos([], 1, 0), \+, [ct(pos([], 1, 3), \+, [a(pos([], 1, 6), a), ct(pos([], 1, 8), b, [ct(pos([], 1, 12), ;, [a(pos([], 1, 11), c), a(pos([], 1, 13), d)]), a(pos([], 1, 16), ,)])])])])
	]
) :- tokenize_string("\\+ \\+(a,b((c;d),,)).",Ts),program(Ts,P).


/* Stress Test Example 
?- op(300,yf,$).
true.

?- X = (a $ $ $ + b ).
X = $ ($ ($a))+b.

?- X = (+ a = 1 ),X = '='('+'(a),1).
X = (+a=1).

?- X = (\+ a = 1 ),X = '\\+'('='(a,1)).
X = (\+a=1).

?- X = [a,b;c| a,b;c], X = [a,(b;c)|(a,b);c].
X = [a, (b;c)| (a, b;c)].


?- X = (\+ - 3*1+2 =.. L).
X = (\+ -(3)*1+2=..L).

?- X = (a+b+c+d), X = ((a+b)+c)+d.
X = a+b+c+d.

?- X = (a,b,c), X = (a,(b,c)).
X = (a, b, c).

?- X = (0+a+b*c), X = ((0+a)+(b*c)).
X = 0+a+b*c.


A prefix operator that is used as a functor does not affect the compound term's
priority (a compound term's priority is 0)
?- X = (- \+ a).
ERROR: Syntax error: Operator priority clash
ERROR: X = (-
ERROR: ** here **
ERROR:  \+ a) . 
?- X = (- \+(a)).
X = - (\+a).


*/



test(
	lists_1,
	[	true(P=[a(pos(_,1,0),[])])
	]
) :- tokenize_string("[].",Ts),program(Ts,P).


test(
	lists_2,
	[	true(P=[ct(_,'.',[av(pos(_,1,1),'_'),a(_,[])])]) ]
) :- tokenize_string("[_].",Ts),program(Ts,P).


test(
	lists_3,
	[	true(P=[ct(pos([], 1, 0), ., [a(pos([], 1, 1), a), ct(pos([], 1, 2), ., [a(pos([], 1, 3), b), ct(pos([], 1, 4), ., [a(pos([], 1, 5), c), ct(pos([], 1, 6), ., [ct(pos([], 1, 9), ;, [a(pos([], 1, 8), d), a(pos([], 1, 10), e)]), a(pos([], 1, 12), [])])])])])])]
) :- tokenize_string("[a,b,c,(d;e)].",Ts),program(Ts,P).


test(
	lists_4,
	[	true(P = [ct(pos([], 1, 0), '.', [ct(pos([], 1, 3), ;, [a(pos([], 1, 2), a), a(pos([], 1, 4), e)]), av(pos([], 1, 7), '_')])])]
) :- tokenize_string("[(a;e)|_].",Ts),program(Ts,P).


test(
	lists_5,
	[	true(P=[
			ct(pos([], 1, 0), ., [
				ct(pos([], 1, 3), ;, [a(pos([], 1, 2), a), a(pos([], 1, 4), e)]),
				ct(pos([], 1, 6), ., [
					a(pos([], 1, 7), b), 
					ct(pos([], 1, 10), ;, [
						a(pos([], 1, 9), a), 
						ct(pos([], 1, 12), ,, [
							a(pos([], 1, 11), e), 
							a(pos([], 1, 13), b)])])])])])]
) :- tokenize_string("[(a;e),b|a;e,b].",Ts),program(Ts,P).


/*
 *	The following tests just load a large number of different prolog files
 * to make sure that our parser can handle reasonable, real world Prolog 
 * programs.
 */

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