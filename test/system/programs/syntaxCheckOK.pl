father('Michael', 'Wilhelm').
mother('Heidi','Vincent').
parent(X,Y) :- mother(X,Y).
parent(X,Y) :- father(X,Y), mother(X,Y).
parent(X,Y).
member(a,[a,2,b3]).
call(mother(X,Y)).
d(X) :- call(parent(X,_)).
findall(X,parent(X,Y), Z).
c(X) :- findall(X,parent(X,Y), Z).

a(b).
a(c).
p1(X,Y) :- a(X), a(Y).
p2(X,Y).
p3(X) :- p2(X,_).
call(p2).
call(p1, 2).
p4(X) :- call(p2).
p5(X,Y) :- call(p2, b, c).