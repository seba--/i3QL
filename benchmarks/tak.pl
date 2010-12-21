/* 
	tak is an artificial benchmark, originally written in Lisp; it is heavily 
	recursive and does lots of simple integer arithmetic.
*/

/*
	data(triple(18, 12, 6)).

	benchmark(triple(X, Y, Z), Out) :-
		tak(X, Y, Z, Out).
		
		I am looking for a nice benchmark for the cutter. 
		It should work in the sense of the tak/4 which 
		puts the memory management of a Prolog interpreter 
		to stress: 
		tak(X,Y,Z,A) :- 
		        X =< Y, !, 
		        Z = A. 
		tak(X,Y,Z,A) :- 
		        X1 is X - 1, 
		        tak(X1,Y,Z,A1), 
		        Y1 is Y - 1, 
		        tak(Y1,Z,X,A2), 
		        Z1 is Z - 1, 
		        tak(Z1,X,Y,A3), 
		        tak(A1,A2,A3,A). 
		test :- 
		        tak(18, 12, 6, _). 
		The test should have a non-trivial invokation 
		of the cut. In the both example with have the 
		cut after the built-in =</2. What I would like 
		to see is a clause like this in the benchmark: 
		     q(...) :- p(...), !, r(...). 
		The q should build up a lot of choice points, 
		so that the cutter has really something to do, 
		and the performance of the cutter can be tested. 
		Also it would be nice if the r would also 
		built up some choice points, so that the interaction 
		of the cutter with other optinizations can be seen. 
		I was thinking about Tic Tac Toe as a benchmark. 
		But maybe people use other typicall benchmarks 
		for the cutter. 
		Bye
		
*/

tak(X,Y,Z,A) :-
	X =< Y, !,
	Z = A.
tak(X,Y,Z,A) :-
	% X > Y,
	X1 is X - 1,
	tak(X1,Y,Z,A1),
	Y1 is Y - 1,
	tak(Y1,Z,X,A2),
	Z1 is Z - 1,
	tak(Z1,X,Y,A3),
	tak(A1,A2,A3,A).

