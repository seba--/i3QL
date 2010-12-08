/*
	This file is meant to test, that the comments remain where they are...
	(C) 2010
*/



sex(X,male) :- male(X),!.
sex(X,female) :-  female(X),!.



/**
	This is a structured comment directly in front of a predicate.
*/
male(michael) :- !.



female(alice).
female(andrea) :- !. % this cut is not strictly required... isn't it?
female(sabine).