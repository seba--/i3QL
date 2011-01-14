/*
	This program computes the list of moves to solve the Hanoi's towers.
	
 	Example:
 	?- hanoi(5, a,b,c, Moves).
*/



/* IF REQUIRED
append([],Ys,Ys).
append([X|Xs],Ys,[X|Zs]) :- append(Xs,Ys,Zs).
*/



hanoi(1, A,B,C,[[A,B]]):-!.
hanoi(N, A,B,C,Moves):-
	N > 1,
	N1 is N - 1,
	hanoi(N1, A,C,B,Ms1),
	hanoi(N1, C,B,A,Ms2),
	append(Ms1, [[A,B]|Ms2], Moves),
	!.



/* "hanoi1" is an alternative implementation that uses Prolog's database.
hanoi1(1, A,B,C,[[A,B]]).
hanoi1(N, A,B,C,Moves):-
	N > 1,
	N1 is N - 1,
	hanoi1(N1, A,C,B,Ms1),
	asserta((hanoi1(N1, A,C,B,Ms1):-!)),
	hanoi1(N1, C,B,A,Ms2),
	append(Ms1, [[A,B]|Ms2], Moves), 
	!.
*/