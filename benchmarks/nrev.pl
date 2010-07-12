nrev([],[]).

nrev([X|Y],Z) :- nrev(Y,Z1), myappend(Z1,[X],Z).

myappend([],L,L).

myappend([X|Y],L,[X|Z]) :- myappend(Y,L,Z).

range(0, Acc, [0|Acc]) :- !.
range(X, Acc, Result) :- Y is X - 1, range(Y, [X|Acc], Result).
range(X, L) :- Y is X - 1, range(Y, [], L).

/* BENCHMARK HARNESS
initialize(D) :- range(1700, D).
slow_initialize(D) :- range(10, D).
benchmark(D, Out) :- nrev(D, Out).
*/