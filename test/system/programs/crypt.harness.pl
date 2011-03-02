:- ensure_loaded('crypt.pl').


benchmark :- run_crypt(1000).


/* 
	run_crypt(X) executes "crypt" X times.
*/
run_crypt(0).
run_crypt(X) :- X > 0, \+(\+ crypt(_)), X0 is X - 1, run_crypt(X0).

