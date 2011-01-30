/* 
	"tak" is an artificial benchmark, originally written in Lisp; it is heavily 
	recursive and does lots of simple integer arithmetic.

	Example Usage
	?- time(tak(32,14,6,R)).
	R = 7.
*/


benchmark(tak) :- tak(32,14,6,_R).


tak(X,Y,Z,A) :-
	X =< Y, !,
	Z = A.
tak(X,Y,Z,A) :-
	% X > Y,
	X1 is X - 1,
	tak(X1,Y,Z,A1),
	Y1 is Y - 1,
	tak(Y1,Z,X,A2),
	Z1 is Z - 1,
	tak(Z1,X,Y,A3),
	tak(A1,A2,A3,A).
