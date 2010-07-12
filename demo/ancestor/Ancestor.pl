father('Michael', 'Wilhelm').
father('Michael', 'Vincent').
father('Michael', 'Leonie').
father('Michael', 'Valerie').
father('Reinhard','Alice').		
father('Reinhard', 'Thilo'). 
father('Werner', 'Michael').
mother('Gertrud', 'Wilhelm').
mother('Heidi','Leonie').
mother('Heidi','Vincent').
mother('Heidi','Valerie').
mother('Christel','Michael').
mother('Gertrud','Christel').
mother('Magdalena','Alice').
mother('Magdalena','Thilo').
male('Thilo').
male('Michael').
male('Werner').
male('Reinhard').
male('Vincent').
male('Wilhelm').
female('Leonie').
female('Valerie').
female('Alice').
female('Gertrud').
female('Magdalena').
female('Heidi').
parent(X,Y) :- mother(X,Y).
parent(X,Y) :- father(X,Y).
grandparent(X,Z) :- parent(X,Y) , parent(Y,Z).
ancestor(X,Z) :- parent(X,Z).
ancestor(X,Z) :- parent(X,Y), ancestor(Y,Z).
%[Inefficient] sibling(X,Y) :- mother(M,X) , mother(M,Y), father(F,X), father(F,Y), X \= Y.
sibling(X,Y) :- mother(M,X) , mother(M,Y),  X \= Y, father(F,X), father(F,Y).
brother(X,Y) :- sibling(X,Y), male(X).
sister(X,Y) :- sibling(X,Y), female(X).

% half_sister :- a sister with whom one has only one parent in common.
% Two definitions (the first one (using two rules) is more efficient!)
% [1]
half_sister(X,Y) :- female(X), mother(M,X), mother(M,Y), X \= Y, father(F1,X), father(F2,Y), F1 \= F2.
half_sister(X,Y) :- female(X), father(F,X), father(F,Y), X \= Y, mother(M1,X) , mother(M2,Y),  M1 \= M2.
% [2]
% half_sister(X,Y) :- female(X), mother(M1,X), mother(M2,Y), father(F1,X), father(F2,Y), X \= Y, ((M1 = M2, F1 \= F2) ; (M1 \= M2, F1 = F2)).

lookup(Key,[(Key,Value)|Dict],Value).
lookup(Key,[(Key1,Value1)|Dict],Value) :-
	Key \= Key1, lookup(Key,Dict,Value).


