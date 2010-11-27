% Autor:
% Datum: 18.11.2010

% Autor:
% Datum: 14.11.2010
:- module(
        'SAEProlog:Compiler:Formatter',
        [format_file/3,write_clause/2]
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


format_file([],L,L).
format_file([H|T],In,OutputList) :- write_clause(H,Out),format_file(T,[In,Out,'\n'],OutputList).%nl,write_clause(H),write('.'),format_file(T).

write_clause(v(_Pos,Name),Name) :- !.%, write(Name).
write_clause(av(_Pos,Name),Name) :- !.%,write(Name).
write_clause(a(_Pos,Atom),Atom) :- !.%,        write('\''),write(Atom),write('\'').
write_clause(i(_Pos,Atom),Atom) :- !.%,        write(Atom).
write_clause(r(_Pos,Atom),Atom) :- !.%,        write(Atom).
%write_clause(ct(_Pos,Functor,ClauseList),[Functor,'(',Out,')']) :- write_functor(ClauseList,Out),!.%,write_term_list(ClauseList,T).
%write_clause(ct(_Pos,'.',ClauseList),Out) :- write_List(ClauseList,Out),!.%,write_term_list(ClauseList,T).
write_clause(ct(_Pos,Functor,ClauseList),Out) :- write_functors(Functor,ClauseList,Out),!.%,write_term_list(ClauseList,T).
        
write_clause(X,_) :- throw(internal_error('the given term has an unexpected type',X)).
write_clause(X) :- throw(internal_error('the given term has an unexpected type',X)).


write_functors('.',ClauseList,Out) :- write_List(ClauseList,Out).
write_functors(Functor,ClauseList,Out) :- is_infix(Functor,ClauseList), write_functor_infix(Functor,ClauseList,Out).

write_functors(Functor,ClauseList,[Functor,'(',Out,')']) :- write_term_list(ClauseList,Out).

write_functor_infix(Functor,[H|T],[First,Functor,Rest]) :-  write_clause(H,First),write_rest_clause(T,Rest).

write_rest_clause([],[]):-!.
write_rest_clause([H|T],[Out,Rest]):-write_clause(H,Out),write_rest_clause(T,Rest).

is_infix(Functor,[_,_|_]) :- operator(Functor,infix).


write_List([H|T],['[',First,Rest,']']) :- write_clause(H,First),write_rest_clause(T,Rest).



write_term_list([Arg|Args],[H|T]) :-
        write_clause(Arg,H),
        write_term_list_rest(Args,T).

write_term_list_rest([],[]) :- !.
write_term_list_rest([Clause|Clauses],[',',H|T]) :-
        write_clause(Clause,H),
        write_term_list_rest(Clauses,T).