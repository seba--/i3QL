queens(N,Qs) :- rangeList(1,N,Ns), queens(Ns,[],Qs).

queens(UnplacedQs, SafeQs, Qs) :-
  selectq(Q, UnplacedQs, UnplacedQs1),
  \+ attack(Q,SafeQs),
  queens(UnplacedQs1,[Q|SafeQs],Qs).
queens([],Qs,Qs).

attack(X,Xs) :- attack(X, 1, Xs).

attack(X,N,[Y|_]) :- X =:= Y+N ; X =:= Y-N.
attack(X,N,[_|Ys]) :- N1 is N+1, attack(X,N1,Ys).

rangeList(M,N,[M]) :- N < M, !.
rangeList(M,N,[M|Tail]) :- M1 is M+1, rangeList(M1,N,Tail).

selectq(X,[X|Xs],Xs).  
selectq(X,[Y|Ys],[Y|Zs]) :- selectq(X,Ys,Zs). 

/* BENCHMARK HARNESS
initialize(11).
benchmark(X, Y) :- \+ (queens(X, Y), fail).
*/

