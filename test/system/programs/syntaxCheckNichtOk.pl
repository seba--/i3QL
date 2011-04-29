p(X) :- call(p2,X).
p1(X). p(X) :- call(p1,X,Y).
a(1). p1(X) :- a(X). p(X) :- call(p1).
p(X) :- g(X).
b(1). p(X) :- g(X).
b(1). a(2). p(X) :- a(X) , b(X) , c(X).
b(1). a(2). p(X) :- a(X) , b(X) , c(X), _.
b(1). a(2). p(X) :- a(X) -> b(X) ; c(X).
b(1). a(2). p(X) :- a(X) *-> b(X) ; c(X).
b(1). a(2). p(X) :- a(X) *-> c(X).
b(1). a(2). p(X) :- a(X) -> c(X).
b(1). a(2). p(X) :- a(X) ; b(X) ; c(X).
p(X) :- findall(X, p1, Xs).
p1(X):- g(x). p(X) :- findall(X, p1(X), Xs).
p1(X). p(X) :- findall(X, p1, Xs).
p(X) :- findall(X, findall(X1, p5(X), X1s), Xs).
p(X) :- findall(X, findall(X1, p, X1s), Xs).
p(X) :- _.
p(X) :- true, p3(_).
p(X) :- not(p2(X)).
p(X) :- \+(p2(X)).
