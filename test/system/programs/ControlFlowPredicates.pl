% Code to test the compilation of the built-in control flow predicates.

:- discontiguous(test/2).

/** 
	These predicates just succeed a given number of times. They are guaranteed
	to not be optimized away by the compiler.
*/
'$SAE$succeed_once'. 

'$SAE$succeed_twice'.
'$SAE$succeed_twice'.



/* ************************************************************************** *\
 *                                                                            *
 *                  TESTS THAT OR(;/2) IS COMPILED CORRECTLY                  *
 *                                                                            *
\* ************************************************************************** */
or_0(X) :- (X = 1 ; X = 2).
test(or_0/1,args(out([1,2]))). % the last argument has to be the out argument..

or_1(X) :- (X = 1 ; X = 2), '$SAE$succeed_once'.
test(or_1/1,args(out([1,2]))).

or_2(X) :- '$SAE$succeed_once', (X = 1 ; X = 2).
test(or_2/1,args(out([1,2]))).

or_3(X) :- '$SAE$succeed_once', (X = 1 ; X = 2), '$SAE$succeed_once'.
test(or_3/1,args(out([1,2]))).

or_cut_0(X) :- (X = 1,! ; X = 2). 
test(or_cut_0/1,args(out([1]))).

or_cut_1(X) :- '$SAE$succeed_once',(X = 1,! ; X = 2). 
test(or_cut_1/1,args(out([1]))).

or_cut_2(X) :- (X = 1,! ; X = 2), '$SAE$succeed_once'.
test(or_cut_2/1,args(out([1]))).

or_cut_3(X) :- (X = 1 ; X = 2),!,'$SAE$succeed_once'.
test(or_cut_3/1,args(out([1]))).

or_modeling_if(T,X) :- T = true,!, X = 1 ; X = 2.
test(or_modeling_if/2,args(in(true),out([1]))).
test(or_modeling_if/2,args(in(false),out([2]))).

multiple_ors_1(X) :- X = 1; X = 2 ; X = 3; X = 4.
test(multiple_ors_1/1,args(out([1,2,3,4]))).

multiple_ors_2(X) :- '$SAE$succeed_twice', (X = 1; X = 2 ; X = 3; X = 4).
test(multiple_ors_2/1,args(out([1,2,3,4,1,2,3,4]))).

multiple_ors_3(X) :- ('$SAE$succeed_twice', X = 1); X = 2 ; X = 3; X = 4.
test(multiple_ors_3/1,args(out([1,1,2,3,4]))).

multiple_ors_4(X) :- 
	(	('$SAE$succeed_twice', X = 1)
	; 	X = 2 
	; 	X = 3
	; 	X = 4
	),
	'$SAE$succeed_once'.
test(multiple_ors_4/1,args(out([1,1,2,3,4]))).

multiple_ors_5(X) :- 
	(	('$SAE$succeed_twice', X = 1)
	;	X = 2
	;	('$SAE$succeed_twice', X = 3)
	;	X = 4
	),
	'$SAE$succeed_once'.
test(multiple_ors_5/1,args(out([1,1,2,3,3,4]))).

multiple_ors_and_cuts_1(X) :- 
	'$SAE$succeed_twice',
	!,
	(	X = 1
	; 	X = 2
	;	('$SAE$succeed_twice', X = 3)
	;	X = 4
	),
	'$SAE$succeed_once'.
test(multiple_ors_and_cuts_1/1,args(out([1,2,3,3,4]))).

multiple_ors_and_cuts_2(X) :- 
	'$SAE$succeed_twice',
	!,
	(	X = 1
	;	X = 2
	;	('$SAE$succeed_twice',!, X = 3)
	;	X = 4
	),
	'$SAE$succeed_once'.
test(multiple_ors_and_cuts_2/1,args(out([1,2,3]))).

multiple_ors_and_cuts_3(X) :- 
	'$SAE$succeed_twice',
	!,
	(	X = 1
	;	X = 2
	;	('$SAE$succeed_twice',!, X = 3)
	;	X = 4
	),
	'$SAE$succeed_twice'.
test(multiple_ors_and_cuts_3/1,args(out([1,1,2,2,3,3]))).
/*
multiple_succeeding_ors(R) :- 
	(	X = 1
	;	X = 2
	;	X = 3
	),
	(	Y = 1
	;	Y = 2
	;	Y = 3
	),
	R = p(X,Y).
test(
		multiple_succeeding_ors/1,
		args(out([
			p(1,1),p(1,2),p(1,3),
			p(2,1),p(2,2),p(2,3),
			p(3,1),p(3,2),p(3,3)
		]))
).
*/