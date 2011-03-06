test(a,b,c).
test(a,b,c) :- a = b.
test(a,b,c) :-
  a = b, b = c, c = a, c is a.
test(a,b,c) :-
  a = b,
  b = c,
  c = a,
  a is b,
  b is c,
  c is a,
  a is very_complicated.
