a(1).
a(2).
a(3).
b(1).
b(2).
b(3).

p1(X) :- a(X).
p2(X) :- a(X), b(X).
p2(X) :- a(X), Y \= X, b(Y).

findall(X,p2,Z).
p3(X) :- findall(X,p2,Z).
p4(X) :- findall(X,p3,Z).
p5(X) :- findall(X,findall(X,p3,Z),Z).