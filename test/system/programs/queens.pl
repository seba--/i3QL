% Usage, e.g, ?- queens(8, Qs).

benchmark(queens) :- queens(22,_).

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


% Architecture:
% 	Model Name:	MacBook Pro
% 	Model Identifier:	MacBookPro5,2
% 	Processor Name:	Intel Core 2 Duo
% 	Processor Speed:	3,06 GHz
% 	Number Of Processors:	1
% 	Total Number Of Cores:	2
% 	L2 Cache:	6 MB
% 	Memory:	4 GB
% 	Bus Speed:	1,07 GHz
% 
% Michael$ swipl --nodebug -O -f Queens.pl
% Welcome to SWI-Prolog (Multi-threaded, 64 bits, Version 5.8.3)
% ?- time(queens(22,R)).
% 115,761,936 inferences, 18.400 CPU in 18.961 seconds (97% CPU, 6291410 Lips)
% R = [15, 11, 7, 16, 6, 9, 12, 21, 8|...].