% Code to test the handling of unification

:- discontiguous(test/2).

/** 
	These predicates just succeed a given number of times. They are guaranteed
	to not be optimized away by the compiler.
*/
'$SAE$succeed_once'. 

'$SAE$succeed_twice'.
'$SAE$succeed_twice'.



/* ************************************************************************** *\
 *                                                                            *
 *              TESTS THAT UNIFICATIONS ARE COMPILED CORRECTLY                *
 *                                                                            *
\* ************************************************************************** */



bin_or(true,true,true).
bin_or(true,false,true).
bin_or(false,true,true).
bin_or(false,false,false).
test(bin_or/3,args(in(true),in(false),out([true]))).

unify(T,T).
test(unify/2,args(in(true),out([true]))).
test(unify/2,args(in(false),out([false]))).

deep_unify(c(b(T)),x(T)).
test(deep_unify/2,args(in(true),out([true]))).
test(deep_unify/2,args(in(c(b(true))),out([x(true)]))).

body_unify1(T) :- T = true.
test(body_unify1/1,args(out([true]))).

body_unify2(I,T) :- T = I.
test(body_unify2/2,args(in(true),out([true]))).
test(body_unify2/2,args(in(false),out([false]))).

/*
body_unify3(T) :- X = _, X = T, T = X, _ = T, T = T, T = true, X = true.
test(body_unify3/1,args(out([true]))).
*/


modifier(final, F) :- unify(final(yes),F).
modifier(abstract, F) :- unify(abstract(yes),F).
modifier(synthetic, F) :- unify(synthetic(yes),F).
modifier(deprecated, F) :- unify(deprecated(yes),F).
test(modifier/2,args(in(final),out([final(yes)]))).
