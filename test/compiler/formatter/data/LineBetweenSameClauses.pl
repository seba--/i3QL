attack(X,Xs) :- attack(X, 1, Xs).

attack(X,N,[Y|_]) :-
   X =:= Y + N;
   X =:= Y - N.
   
attack(X,N,[_|Ys]) :-
   N1 is N + 1,
   attack(X,N1,Ys).