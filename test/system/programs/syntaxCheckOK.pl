a(b).
a(c).
p1(X) :- a(X), a(X).
p2(X,Y).
p3(X,Y) :- p2(X,Y).
p4(X) :- call(p2, a, d).