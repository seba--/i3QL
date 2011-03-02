/* License (BSD Style License):
   Copyright (c) 2010
   Department of Computer Science
   Technische Universität Darmstadt
   All rights reserved.

   Redistribution and use in source and binary forms, with or without
   modification, are permitted provided that the following conditions are met:

    - Redistributions of source code must retain the above copyright notice,
      this list of conditions and the following disclaimer.
    - Redistributions in binary form must reproduce the above copyright notice,
      this list of conditions and the following disclaimer in the documentation
      and/or other materials provided with the distribution.
    - Neither the name of the Software Technology Group or Technische
      Universität Darmstadt nor the names of its contributors may be used to
      endorse or promote products derived from this software without specific
      prior written permission.

   THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
   AND ANY EXPRESSED OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
   IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
   ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
   LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
   CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
   SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
   INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
   CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
   ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
   POSSIBILITY OF SUCH DAMAGE.
*/

% Autor: Dennis Siebert
% Datum: 18.11.2010

:- module(
   sae_formatter,
   [format_clauses/2,
   format_clauses/3,
   getTabs/3,
   replace_characer/4,build_string_sequence/4]
).

:- use_module('AST.pl').

format_clauses(Cs,Fs) :-  membercheck(X,[],80),format_file(Cs,'',X,Fs).

format_clauses(Cs,Fs,Options) :- membercheck(X,Options,80),format_file(Cs,'',X,Fs).

   
count_clauses([],0).
count_clauses([H|T],Length):- count_clause(H,AtomLength),count_clauses(T,RestLength), Length is AtomLength + RestLength,!.

count_clause(ASTNode,Length) :-
   (
      variable(ASTNode,Value)
   ;
      anonymous_variable(ASTNode,Value)
   ;
      string_atom(ASTNode,Value)
   ;
      integer_atom(ASTNode,Value)
   ;
      float_atom(ASTNode,Value)
   ), term_to_atom(Value,Atom),atom_length(Atom,Length),write(Value),write(' = '),write(Length),nl,!.

count_clause(ASTNode,Length) :-
    complex_term(ASTNode,Functor,Args),count_clauses(Args,Restlength),term_to_atom(Functor,Atom),atom_length(Atom,FunctorLength), Length is FunctorLength + Restlength,write(Functor),write(' = '),write(FunctorLength),nl.

format_file([],L,_,L).
format_file([H|T],In,Linewidth,OutputList) :-
% 
%    nl,write('LineWidth = '),write(Linewidth),
%    count_clause(H,Length),
%    nl,write('Length = '),write(Length),
%    (                                            c
%       Length =< Linewidth
%       %line short enought
%       %format_file(Cs,'',Fs)
%    ;
%       Length > Linewidth
%       %line to long, must be split
%       %format_file(Cs,'',Fs)
%    ),
   (
   (
      % if
      complex_term(H,':-',[First|_]),
      complex_term(First,CurrentName,CurrentArity),
      length(CurrentArity,ListLenght)
   ;
      complex_term(H,CurrentName,CurrentArity),
      length(CurrentArity,ListLenght)
   ),!,format_file([H|T],In,[CurrentName,ListLenght],Linewidth,OutputList)
   ;
   write_clause(H,1200,0,Out), atomic_list_concat([In,Out,'.\n'],Concated_List),format_file(T,Concated_List,Linewidth,OutputList)).

format_file([],L,_,_,L).
format_file([H|T],In,[ClauseName,ClauseArity],Linewidth,OutputList) :-
%   nl,write('LineWidth = '),write(Linewidth),
%    count_clause(H,Length),
%    nl,write('Length = '),write(Length),
%    (
%       Length =< Linewidth
%       %line short enought
%       %format_file(Cs,'',Fs)
%    ;
%       Length > Linewidth
%       %line to long, must be split
%       %format_file(Cs,'',Fs)
%    ),
   (
      complex_term(H,':-',[First|_]),
      complex_term(First,CurrentName,CurrentArity),
      length(CurrentArity,ListLenght)
   ;
      complex_term(H,CurrentName,CurrentArity),
      length(CurrentArity,ListLenght)
   ),
   (
      CurrentName = ClauseName, ListLenght = ClauseArity,
      write_clause(H,1200,0,Out),
      atomic_list_concat([In,Out,'.\n'],Concated_List)
   ;
      CurrentName = ClauseName,
      write_clause(H,1200,0,Out),
      atomic_list_concat([In,'\n',Out,'.\n'],Concated_List)
   ;
      write_clause(H,1200,0,Out),
      atomic_list_concat([In,'\n\n',Out,'.\n'],Concated_List)
   ),!,format_file(T,Concated_List,[CurrentName,ListLenght],Linewidth,OutputList).
   

replace_characer([],_,_,'').
replace_characer([Char|Chars],OldChar,NewChar,Output) :-
   (
      variable(ASTNode,Value);
      anonymous_variable(ASTNode,Value);
      string_atom(ASTNode,Value);
      integer_value(ASTNode,Value);
      Char = OldChar,
      replace_characer(Chars,OldChar,NewChar,Out),
     atomic_list_concat([NewChar,Out],Output)
   ;
     replace_characer(Chars,OldChar,NewChar,Out),
     atomic_list_concat([Char,Out],Output)
   ),!.
   
membercheck(Value,Options,DefaultValue) :-
   (
      memberchk(linewidth(Value),Options)
   ;
      Value is DefaultValue
   ),!.
   
build_string_sequence(String,OldC,NewC,Sequence) :- term_to_atom(String,Atom),atom_chars(Atom,AtomList),replace_characer(AtomList,OldC,NewC,Sequence).

write_clause(ASTNode,_,_,Value) :-
   (
      variable(ASTNode,Value)
   ;
      anonymous_variable(ASTNode,Value)
   ;
      string_atom(ASTNode,QValue),build_string_sequence(QValue,'\n','\\n',Value)
   ;
      integer_atom(ASTNode,Value)
   ;
      float_atom(ASTNode,Value)
   ),!.
   
write_clause(ASTNode,Priority,Depth,Out) :-
   compound_term(ASTNode,'.',Args),
   write_List(Args,Priority,Depth,Out),!.
   
write_clause(ASTNode,Priority,Depth,Out) :-
   compound_term(ASTNode,Functor,Args),
   (
      term_meta(ASTNode,Meta),
      lookup_in_meta(ops(FirstPrefixOps,FirstInfixOps,FirstPostfixOps),Meta),
      (  % ComplexTerm has a prefix functor
         memberchk(op(Func_Priority,_,Functor),FirstPrefixOps),
         Args = [_],
         (
            Func_Priority > Priority,
            write_functors(_,Args,Priority,Depth,Output),
            atomic_list_concat([Functor,'(',Output,')'],Out)
         ;
            write_functors(_,Args,Func_Priority,Depth,Output),
            atomic_list_concat([Functor,' ',Output],Out)
         )
      ;  % ComplexTerm has an infix functor
         memberchk(op(Func_Priority,_,Functor),FirstInfixOps),Args = [_,_],
         (
            Func_Priority > Priority,

            (
               Functor = ';',
               write_functor_infix(Functor,Args,Func_Priority,Depth+1,Output),
               atomic_list_concat(['\n(\n',Output,'\n)'],Out)
            ;
             write_functor_infix(Functor,Args,Func_Priority,Depth,Output),
               atomic_list_concat(['(',Output,')'],Out)
            )
         ;
         (
            Functor = ';',
            write_functor_infix(Functor,Args,Func_Priority,Depth,Output),
            atomic_list_concat([Output],Out)
            ;
            write_functor_infix(Functor,Args,Func_Priority,Depth,Output),
            atomic_list_concat([Output],Out)
         )
         )
      ;  %ComplexTerm has a postfix functor
         memberchk(op(_,_,Functor),FirstPostfixOps),Args=[_],write_functors(Functor,Args,Priority,Depth,Out),atomic_list_concat(['(',Output,')',Functor,' '],Out)
      )
      ;
      %ComplexTerm has an unknown functor
      write_functors(Functor,Args,Priority,Depth,Output),atomic_list_concat([Functor,'(',Output,')'],Out)
   ),!.
   
write_clause(ASTNode,_,_) :- throw(internal_error('[Formatter] the given term has an unexpected type',ASTNode)).


write_functors(_,ClauseList,Priority,Depth,RestList) :-
   write_term_list(ClauseList,Priority,Depth,RestList).
   %atomic_list_concat([Functor,'(',RestList,')'],Concated_List).


write_functor_infix(',',[H|T],Priority,Depth,Concated_List) :-
   write_clause(H,Priority,Depth,First),
   write_rest_clause(T,Priority,Depth,Rest),
   atomic_list_concat([First,',',' ',Rest],Concated_List).


write_functor_infix(';',[H|T],Priority,Depth,Concated_List) :-
   write_clause(H,Priority,Depth,First),
   write_rest_clause(T,Priority,Depth,Rest),
   getTabs(Depth,';',Functor),
   getTabs(Depth,First,TabbedFirst),
   getTabs(Depth,Rest,TabedRest),
   atomic_list_concat([TabbedFirst,'\n',Functor,'\n',TabedRest],Concated_List).



write_functor_infix(Functor,[H|T],Priority,Depth,Concated_List) :-
   write_clause(H,Priority,Depth,First),
   write_rest_clause(T,Priority,Depth,Rest),
   atomic_list_concat([First,' ',Functor,' ',Rest],Concated_List).


write_rest_clause([],_,_,'').

write_rest_clause([H|T],Priority,Depth,Concated_List) :-
   write_clause(H,Priority,Depth,First),
   write_rest_clause(T,Priority,Depth,Rest),
   atomic_list_concat([First,Rest],Concated_List).


write_List([],_,_,'').

write_List([H|T],Priority,Depth,Concated_List) :-
   (
      complex_term(H,'.',Args),write_List(Args,Priority,Depth,First),write_In_List(T,'false',Priority,Depth,Rest),atomic_list_concat([First,Rest],Output)
      compound_term(H,'.',Args),write_List(Args,First),write_In_List(T,'false',Rest),atomic_list_concat([First,Rest],Output)
      ;
      write_clause(H,Priority,Depth,First),write_In_List(T,'false',Priority,Depth,Rest),atomic_list_concat([First,Rest],Output)
   ),!
   ,atomic_list_concat(['[',Output,']'],Concated_List).


write_In_List([],_,_,_,'').

write_In_List([H|T],Dotted,Priority,Depth,Output) :-
   (
      Dotted = 'false',complex_term(H,'.',Args),write_In_List(Args,'true',Priority,Depth,Output)
      Dotted = 'false',compound_term(H,'.',Args),write_In_List(Args,'true',Output)
      ;
      Dotted = 'true',complex_term(H,'.',Args),write_List(Args,Priority,Depth,Out),atomic_list_concat([',',Out],Output)
      Dotted = 'true',compound_term(H,'.',Args),write_List(Args,Out),atomic_list_concat([',',Out],Output)
      ;
      string_atom(H,'[]'),write_In_List(T,'false',Priority,Depth,Rest),atomic_list_concat([Rest],Output)
      ;
      T = [],write_clause(H,Priority,Depth,First),write_In_List(T,'false',Priority,Depth,Rest),atomic_list_concat(['|',First,Rest],Output)
      ;
      write_clause(H,Priority,Depth,First),write_In_List(T,'false',Priority,Depth,Rest),atomic_list_concat([',',First,Rest],Output)
   ),!.


write_term_list([Arg|Args],Priority,Depth,Concated_List) :-
   write_clause(Arg,Priority,Depth,H),
   write_term_list_rest(Args,Priority,Depth,T),
   atomic_list_concat([H,T],Concated_List).


write_term_list_rest([],_,_,'').

write_term_list_rest([Clause|Clauses],Priority,Depth,Concated_List) :-
   write_clause(Clause,Priority,Depth,H),
   write_term_list_rest(Clauses,Priority,Depth,T),
   atomic_list_concat([',',H,T],Concated_List).


getTabs(Amount,OldTab,NewTab) :-
   (
      Amount > 0,
      NewAmount is Amount - 1,
      getTabs(NewAmount,OldTab,Tab),
      atomic_list_concat(['\t',Tab],NewTab)
   ;
      atomic_list_concat([OldTab],NewTab)
   ),!.
 