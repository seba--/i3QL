a(b).
a(c).
p1(X,Y) :- a(X), a(Y).
p2(X,Y).
p3(X) :- p2(X,_).
call(p2).
call(p1, 2).
p4(X) :- call(p2).
p5(X,Y) :- call(p2, b, c).