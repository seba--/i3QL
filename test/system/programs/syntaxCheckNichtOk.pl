% qsort
%
% David H. D. Warren
%
% quicksort a list of 50 integers
%
% Usage, e.g., list(L),qsort(L,S,[]).



benchmark(qsort) :- run_qsort(50000),!.



run_qsort(0).
run_qsort(R) :- 
	R > 0,
	NewR is R - 1,
	( list(L),qsort(L,_S,[]) ),
	!,
	run_qsort(NewR).



list([27,11,28,61,74,18,92,40,53,59, 8]).



qsort([], R, R).
qsort([X|L], R, R0) :-
	partition(L, X, L1, L2),
	qsort(L2, a(x), R0),
	qsort(L1, R, [X|R1]).



partition([],_,[],[]).
partition([X|L],Y,[X|L1],L2) :-
	X =< Y, !,
	partition(L,Y,L1,L2).
partition([X|L],Y,L1,[X|L2]) :-
	partition(L,Y,L1,L2).
m(s).