/*******************************************
* This program compute the list of moves to solve the Hanoi's towers
* The defined predicates are:
* hanoi(Height, Tower1, Tower2, Tower3, Moves)
* hanoi1(Height, Tower1, Tower2, Tower3, Moves)
* 
* Example:
* JIP:-hanoi(5, a,b,c, Moves).
* (hanoi1 is faster then hanoi).
*******************************************/

hanoi(1, A,B,C,[[A,B]]):-!.

hanoi(N, A,B,C,Moves):-
	N > 1,
	N1 is N - 1,
	hanoi(N1, A,C,B,Ms1),
	hanoi(N1, C,B,A,Ms2),
	append(Ms1, [[A,B]|Ms2], Moves),
	!.

/**********************************/
	
hanoi1(1, A,B,C,[[A,B]]).

hanoi1(N, A,B,C,Moves):-
	N > 1,
	N1 is N - 1,
	hanoi1(N1, A,C,B,Ms1),
	asserta((hanoi1(N1, A,C,B,Ms1):-!)),
	hanoi1(N1, C,B,A,Ms2),
	append(Ms1, [[A,B]|Ms2], Moves), 
	!.
