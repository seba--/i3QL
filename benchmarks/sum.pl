oadd(int(A), int(B), int(C)) :-
    C is A + B.
oadd(atom(A), atom(B), atom(C)) :-
    atom_concat(A, B, C).

osum([], Initial, Initial).
osum([H|T], Initial, Result) :-
    oadd(H, Initial, Intermediate),
    osum(T, Intermediate, Result).


/* BENCHMARK HARNESS
initialize(50000).
benchmark(X, _) :- call_num(X).
call_num(0).
call_num(X) :- X > 0, \+(\+ (sum(X, int(R)), R is X * 10 + 10 * 8 + 100)), X0 is X - 1, call_num(X0).
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


