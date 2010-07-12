%   qsort
%
%   David H. D. Warren
%
%   quicksort a list of 50 integers

/* BENCHMARK HARNESS
initialize(500).
benchmark(X, _) :- call_num(X).
call_num(0).
call_num(X) :- X > 0, \+(\+ main), write(X), write(' '), X0 is X - 1, call_num(X0).
*/

main :-
        list(Data),
	qsort(Data, _, []),
	qsort(Data, _, []),
	qsort(Data, _, []),
	qsort(Data, _, []),
	qsort(Data, _, []),
	qsort(Data, _, []),
	qsort(Data, _, []),
	qsort(Data, _, []),
	qsort(Data, _, []),
	qsort(Data, _, []),
	qsort(Data, _, []).

list([27,74,17,33,94,18,46,83,65, 2,
                32,53,28,85,99,47,28,82, 6,11,
                55,29,39,81,90,37,10, 0,66,51,
                 7,21,85,27,31,63,75, 4,95,99,
                11,28,61,74,18,92,40,53,59, 8]).

slow_data([27,74,17,5]).

qsort([], R, R).
qsort([X|L], R, R0) :-
	partition(L, X, L1, L2),
	qsort(L2, R1, R0),
	qsort(L1, R, [X|R1]).

partition([],_,[],[]).
partition([X|L],Y,[X|L1],L2) :-
	X =< Y, !,
	partition(L,Y,L1,L2).
partition([X|L],Y,L1,[X|L2]) :-
	partition(L,Y,L1,L2).
