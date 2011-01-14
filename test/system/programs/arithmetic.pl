/* TYPICAL USAGE
	?- num_to_binary(14, D),factorial(D, O).
*/
bit_xor(0, 0, 0).
bit_xor(0, 1, 1).
bit_xor(1, 0, 1).
bit_xor(1, 1, 0).

bit_and(0, 0, 0).
bit_and(0, 1, 0).
bit_and(1, 0, 0).
bit_and(1, 1, 1).

half_adder(X, Y, R, C) :-
    bit_xor(X, Y, R),
    bit_and(X, Y, C).

full_adder(B, X, Y, R, C) :-
    half_adder(X, Y, W, XY),
    half_adder(W, B, R, WZ),
    bit_xor(XY, WZ, C).

num_to_binary(0, []).
num_to_binary(Num, Result) :-
    Num =\= 0,
    Parity is Num mod 2,
    num_to_binary(Num, Parity, Result).

num_to_binary(Num, 0, Result) :-
    Half is Num // 2,
    num_to_binary(Half, HalfResult),
    Result = [0 | HalfResult].
num_to_binary(Num, 1, Result) :-
    Half is (Num - 1) // 2,
    num_to_binary(Half, HalfResult),
    Result = [1 | HalfResult].

binary_to_num([], 0).
binary_to_num([X | Y], Result) :-
    binary_to_num(Y, R),
    Result is R * 2 + X.

pos([_ | _]).

greater_one([_, _| _]).

adder(0, N, [], N).
adder(0, [], M, M) :-
    pos(M).
adder(1, N, [], R) :-
    adder(0, N, [1], R).
adder(1, [], M, R) :-
    pos(M),
    adder(0, [1], M, R).
adder(D, [1], [1], R) :-
    R = [A, C],
    full_adder(D, 1, 1, A, C).
adder(D, [1], M, R) :-
    gen_adder(D, [1], M, R).
adder(D, N, [1], R) :-
    greater_one(N),
    greater_one(R),
    adder(D, [1], N, R).
adder(D, N, M, R) :-
    greater_one(N),
    gen_adder(D, N, M, R).

gen_adder(D, [A | X], [B | Y], [C | Z]) :-
    pos(Y),
    pos(Z),
    full_adder(D, A, B, C, E),
    adder(E, X, Y, Z).

bin_add(X, Y, Z) :-
    adder(0, X, Y, Z).

bin_sub(X, Y, Z) :-
    adder(0, Y, Z, X).

bin_mul([], M, []).
bin_mul(N, [], []) :-
    pos(N).
bin_mul([1], M, M) :-
    pos(M).
bin_mul(N, [1], N) :-
    greater_one(N).
bin_mul([0 | X], M, [0 | Z]) :-
    pos(X),
    pos(Z),
    greater_one(M),
    bin_mul(X, M, Z).
bin_mul([1 | X], [0 | Y], P) :-
    pos(X),
    pos(Z),
    bin_mul([0 | Y], [1 | X], P).
bin_mul([1 | X], [1 | Y], P) :-
    pos(X),
    pos(Y),
    odd_mul(X, [1 | X], [1 | Y], P).

odd_mul(X, N, M, P) :-
    bound_mul(Q, P, N, M),
    bin_mul(X, M, Q),
    bin_add([0 | Q], M, P).

bound_mul([], [_|_], N, M).

bound_mul([_ | X], [_ | Y], [], [_ | Z]) :-
    bound_mul(X, Y, Z, []).
    
bound_mul([_ | X], [_ | Y], [_ | Z], M) :-
    bound_mul(X, Y, Z, M).

bin_st_length([], [_ | _]).
bin_st_length([1], Y) :-
    greater_one(Y).
bin_st_length([_ | X], [_ | Y]) :-
    pos(X),
    pos(Y),
    bin_st_length(X, Y).

bin_eq_length([], []).
bin_eq_length([1], [1]).
bin_eq_length([_ | X], [_ | Y]) :-
    pos(X),
    pos(Y),
    bin_eq_length(X, Y).

bin_st(X, Y) :-
    bin_st_length(X, Y).
bin_st(X, Y) :-
    bin_eq_length(X, Y),
    pos(Z),
    bin_add(X, Z, Y).

factorial([], [1]).
factorial([1], [1]).
factorial([0, 1], [0, 1]).
factorial([1, 1], [0, 1, 1]).

factorial(X, Y) :-
    pos(X),
    pos(Xmin1),
    bin_sub(X, [1], Xmin1),
    factorial(Xmin1, R),
    bin_st(X, Y),
    bin_mul(X, R, Y).


