/* 
	Example Usage: 
	sum(X, int(R)), R is X * 10 + 10 * 8 + 100.
*/
sum(A, X) :-
    osum([int(6), int(2), int(1), int(0), int(-1), int(A),
          int(6), int(2), int(1), int(0), int(-1), int(A),
          int(6), int(2), int(1), int(0), int(-1), int(A),
          int(6), int(2), int(1), int(0), int(-1), int(A),
          int(6), int(2), int(1), int(0), int(-1), int(A),
          int(6), int(2), int(1), int(0), int(-1), int(A),
          int(6), int(2), int(1), int(0), int(-1), int(A),
          int(6), int(2), int(1), int(0), int(-1), int(A),
          int(6), int(2), int(1), int(0), int(-1), int(A),
          int(6), int(2), int(1), int(0), int(-1), int(A)], int(100), X).


oadd(int(A), int(B), int(C)) :-
    C is A + B.
oadd(atom(A), atom(B), atom(C)) :-
    atom_concat(A, B, C).

osum([], Initial, Initial).
osum([H|T], Initial, Result) :-
	oadd(H, Initial, Intermediate),
	osum(T, Intermediate, Result).