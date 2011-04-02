a(1).
a(2).
a(3).
b(1).
b(2).
b(3).

p1(X) :- a(X) ; b(X).
p2(X) :- a(X) -> b(X) ; b(X).
p3(X) :- a(X) -> b(x).
p4(X) :- a(X) *-> b(X) ; b(X).
p5(X) :- a(X) *-> b(x).
p6(X,Y) :- a(X) , b(Y).