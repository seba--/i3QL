% Autor:
% Datum: 14.11.2010
:- module(
        'SAEProlog:Compiler:FormatterOld',
        [format_file/1]
        ).
        

operator('=',infix).
operator(':-',infix).
operator(',',infix).
operator('-->',infix).
operator(';',infix).
operator('=',infix).
operator('-',infix).
operator('+',infix).
operator('->',infix).
operator('*',infix).
operator('/',infix).
operator('\\=',infix).
operator('is',infix).
operator('<',infix).
operator('>',infix).
operator('=<',infix).
operator('>=',infix).
operator('=:=',infix).
operator('=\\=',infix).
operator('=..',infix).
operator('==',infix).
operator('\\==',infix).
operator('@<',infix).
operator('@=<',infix).
operator('@>',infix).
operator('@>=',infix).
operator('\\/',infix).
operator('/\\',infix).
operator('|',infix). % also defined by SWI Prolog
operator('//',infix). % X // Y Division mit Ganzzahlergebnis
operator('mod',infix).
operator('<<',infix).
operator('>>',infix).
operator('**',infix).
operator('^',infix).

format_file([]).
format_file([H|T]) :- nl,write_clause(H),format_file(T).



write_clause(v(_Pos,Name)) :- write(Name).
write_clause(av(_Pos,Name)) :- write(Name).
write_clause(a(_Pos,Atom)) :-  write('\''),write(Atom),write('\'').
write_clause(i(_Pos,Atom)) :- write(Atom).
write_clause(r(_Pos,Atom)) :- write(Atom).
write_clause(ct(_Pos,Functor,ClauseList)) :-
    write_functors(Functor,ClauseList).%,write('('),write_term_list(ClauseList),write(')').

write_clause(X) :- throw(internal_error('the given term has an unexpected type',X)).

write_functors(Functor,ClauseList) :- is_infix(Functor,ClauseList), write_functor_infix(Functor,ClauseList).
%write_functors(Functor,ClauseList) :- is_prefix(ClauseList), write_functor_prefix(Functor,ClauseList).
write_functors(Functor,ClauseList) :- write(Functor),write('('),write_term_list(ClauseList),write(')').

write_functor_infix(Functor,[H|T]) :-  write_clause(H),write(Functor),write_rest_clause(T).
%write_functor_prefix(Functor,ClauseList) :-  write(Functor),write('('),write_term_list(ClauseList),write(')').

write_rest_clause([]):-!.
write_rest_clause([H|T]):-write_clause(H),write_rest_clause(T).

is_infix(Functor,[_,_|_]) :- operator(Functor,infix).


write_term_list([H|T]) :-
        write_clause(H),
        write_term_list_rest(T).

write_term_list_rest([]) :- !.
write_term_list_rest([Clause|Clauses]) :-
        write(','),write_clause(Clause),
        write_term_list_rest(Clauses).