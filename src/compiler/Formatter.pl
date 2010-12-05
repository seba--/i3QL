% Autor:
% Datum: 18.11.2010

% Autor:
% Datum: 14.11.2010
:- module(
        'SAEProlog:Compiler:Formatter',
        [format_file/3]
        ).
:- use_module('AST.pl').



format_file([],L,L).
format_file([H|T],In,OutputList) :- write_clause(H,Out),atomic_list_concat([In,Out,'.\n'],Concated_List),format_file(T,Concated_List,OutputList).%nl,write_clause(H),write('.'),format_file(T).


write_clause(X,Value) :- variable(X,Value);
  anonymous_variable(X,Value);
  string_atom(X,Value);
  integer_atom(X,Value);
  float_atom(X,Value).


%    [  % PREFIX...
%     op(900,fy,'\\+'),
write_clause(X,Out) :-complex_term(X,'.',Args),write_List(Args,Out).
write_clause(X,Out) :-complex_term(X,Functor,Args),
        (
        term_meta(X,Meta),
        lookup_in_meta(ops(FirstPrefixOps,FirstInfixOps,FirstPostfixOps),Meta),
           (
           memberchk(op(_,_,Functor),FirstPrefixOps),is_prefix(Args),write_functors(Functor,Args,Out)
           ;
           memberchk(op(_,_,Functor),FirstInfixOps),is_infix(Args),write_functor_infix(Functor,Args,Out)
           ;
           memberchk(op(_,_,Functor),FirstPostfixOps),write_functors(Functor,Args,Out)
           )
        ;
        write_functors(Functor,Args,Out)
        ).


write_clause(X,_) :- throw(internal_error('the given term has an unexpected type',X)).

is_prefix([_|[]]).
is_infix([_,_|[]]).

%write_functors(Functor,ClauseList,Out) :- is_infix(Functor,ClauseList), write_functor_infix(Functor,ClauseList,Out).

write_functors(Functor,ClauseList,Out) :- write_term_list(ClauseList,RestList),atomic_list_concat([Functor,'(',RestList,')'],Out).

write_functor_infix(Functor,[H|T],Out) :-  write_clause(H,First),write_rest_clause(T,Rest),atomic_list_concat([First,' ',Functor,' ',Rest],Out).

write_rest_clause([],''):-!.
write_rest_clause([H|T],Out):-write_clause(H,First),write_rest_clause(T,Rest),atomic_list_concat([First,Rest],Out).

write_List([],''):-!.
write_List([H|T],Out) :-
  (
  complex_term(H,'.',Args),write_List(Args,First),write_In_List(T,'false',Rest),atomic_list_concat([First,Rest],Output)
  ;
  write_clause(H,First),write_In_List(T,'false',Rest),atomic_list_concat([First,Rest],Output)
  ),!
,atomic_list_concat(['[',Output,']'],Out).

write_In_List([],_,'').
write_In_List([H|T],Dotted,Output) :-
(
Dotted = 'false',complex_term(H,'.',Args),write_In_List(Args,'true',Output)
;
Dotted = 'true',complex_term(H,'.',Args),write_List(Args,Out),atomic_list_concat([',',Out],Output)
;
string_atom(H,'[]'),write_In_List(T,'false',Rest),atomic_list_concat([Rest],Output)
;
T = [],write_clause(H,First),write_In_List(T,'false',Rest),atomic_list_concat(['|',First,Rest],Output)
;
write_clause(H,First),write_In_List(T,'false',Rest),atomic_list_concat([',',First,Rest],Output)
).

write_term_list([Arg|Args],Out) :-
        write_clause(Arg,H),
        write_term_list_rest(Args,T),
        atomic_list_concat([H,T],Out).

write_term_list_rest([],'') :- !.
write_term_list_rest([Clause|Clauses],Out) :-
        write_clause(Clause,H),
        write_term_list_rest(Clauses,T),
        atomic_list_concat([',',H,T],Out).