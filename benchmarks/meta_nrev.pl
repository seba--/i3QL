
interpret([]).
interpret([A | T]) :-
    my_clause(A, T, R),
    interpret(R).

my_clause(app([], X, X), T, T).
my_clause(app([H | T1], T2, [H | T3]), Rest, [app(T1, T2, T3) | Rest]).
my_clause(nrev([], []), T, T).
my_clause(nrev([X | Y], Z), Rest, [nrev(Y, Z1), app(Z1, [X], Z) | Rest]).

meta_nrev_range(0, Acc, [0|Acc]) :- !.
meta_nrev_range(X, Acc, Result) :- Y is X - 1, meta_nrev_range(Y, [X|Acc], Result).
meta_nrev_range(X, L) :- Y is X - 1, meta_nrev_range(Y, [], L).

/* BENCHMARK HARNESS
initialize(D) :- range(1700, D).
slow_initialize(D) :- range(10, D).
benchmark(D, Out) :- interpret([nrev(D, Out)]).
*/