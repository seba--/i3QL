/**

	Typical usage:
	?- run_nrev(1000,R).
*/
run_nrev(R,Result) :- nrev_range(R,Range),nrev(Range,Result).

nrev([],[]).
nrev([X|Y],Z) :- nrev(Y,Z1), myappend(Z1,[X],Z).

myappend([],L,L).
myappend([X|Y],L,[X|Z]) :- myappend(Y,L,Z).

nrev_range(X, L) :- Y is X - 1, nrev_range(Y, [], L).

nrev_range(0, Acc, [0|Acc]) :- !.
nrev_range(X, Acc, Result) :- Y is X - 1, nrev_range(Y, [X|Acc], Result).


benchmark(nrev) :- nrev_range(1500,Range), nrev(Range,Result).
