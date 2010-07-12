/* BENCHMARK HARNESS
initialize(triple(18, 12, 6)).
slow_initialize(triple(18, 5, 5)).

benchmark(triple(X, Y, Z), Out) :-
	tak(X, Y, Z, _),
	tak(X, Y, Z, _),
	tak(X, Y, Z, _),
	tak(X, Y, Z, _),
	tak(X, Y, Z, Out).
*/

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

