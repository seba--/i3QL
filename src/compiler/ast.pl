:- module(ast,
 [
 ]).


empty_ast([]).

predicate(AST, Predicate) :-
	Predicate = _/_,
	member(pred(Predicate, _, _), AST).

clause(AST, Predicate, H:-B) :-
	member(pred(Predicate, Clauses, _), AST),
	is_list(Clauses),
	member(H:-B, Clauses).

add_predicate(AST, Predicate, Clauses, NewAST) :-
	NewAST = [pred(Predicate, Clauses, []) |  AST].

add_clause(AST, Predicate, Clauses, NewAST) :- fail.



