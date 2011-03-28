father('Michael', 'Wilhelm').
mother('Heidi','Vincent').
parent(X,Y) :- mother(X,Y).
parent(X,Y) :- father(X,Y), mother(X,Y).
parent(X,Y).
member(a,[a,2,b3]).
a(X) :- (mother(X,_) -> fail ; father(X,_)).
b(Y) :- (a(x) -> parent(X,_)).
a(X) :- (mother(X,_) *-> fail ; father(X,_)).
b(Y) :- (a(x) *-> parent(X,_)).
c(B) :- call(B,_).
d(_) :- findall(_,_,_,_).