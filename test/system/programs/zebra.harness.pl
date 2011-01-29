:- ensure_loaded('zebra.pl').

benchmark :- run(1000).

run(0).
run(R) :- 
	R > 0,
	\+ \+ zebra(_Houses),
	NewR is R - 1,
	run(NewR).