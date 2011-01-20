% Usage, e.g, ?- queens(8, Qs).

benchmark(queens) :- queens(22,_).

benchmark(queens_8_findall) :- run_queens_8_findall(1000).

run_queens_8_findall(0).
run_queens_8_findall(R) :- 
	R > 0,
	NewR is R - 1,
	queens_8_findall,
	!,
	run_queens_8_findall(NewR).

queens_8_findall :- queens(8,_),fail.
queens_8_findall.



queens(N,Qs) :-
	range(1,N,Ns),
	queens(Ns,[],Qs).


queens([],Qs,Qs).
queens(UnplacedQs,SafeQs,Qs) :-
	select(UnplacedQs,UnplacedQs1,Q),
	not_attack(SafeQs,Q),
	queens(UnplacedQs1,[Q|SafeQs],Qs).


range(N,N,[N]) :- !.
range(M,N,[M|Ns]) :-
	M < N,
	is(M1,M+1),
	range(M1,N,Ns).


select([X|Xs],Xs,X). %select is predefined by gprolog...
% In case of the following clause, we can do last-call
% optimization, because	the following select clause is 
% the last clause, and "unifications with the head" never
% generates choice points. 
select([Y|Ys],[Y|Zs],X) :- select(Ys,Zs,X).


not_attack(Xs,X) :-
	not_attack(Xs,X,1).

not_attack([],_,_) :- !.
not_attack([Y|Ys],X,N) :-
	X =\= Y+N,
	X =\= Y-N,
	N1 is N+1,
	not_attack(Ys,X,N1).
