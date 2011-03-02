:- ensure_loaded('sum.pl').

benchmark :- call_num(100000).

call_num(0).
call_num(X) :-
	X > 0, 
	\+(\+ (sum(X, int(R)), R is X * 10 + 10 * 8 + 100)), 
	X0 is X - 1, 
	call_num(X0).
