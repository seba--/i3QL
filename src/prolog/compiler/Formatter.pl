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
   [format_file/3]
).

:- use_module('AST.pl').



format_file([],L,L).
format_file([H|T],In,OutputList) :- 
   write_clause(H,Out),
   atomic_list_concat([In,Out,'.\n'],Concated_List),
   format_file(T,Concated_List,OutputList).
   
   
write_clause(ASTNode,Value) :-
   (
      variable(ASTNode,Value);
      anonymous_variable(ASTNode,Value);
      string_atom(ASTNode,Value);
      integer_atom(ASTNode,Value);
      float_atom(ASTNode,Value)
   ),!.
   
write_clause(ASTNode,Out) :-
   compound_term(ASTNode,'.',Args),
   write_List(Args,Out),!.
   
write_clause(ASTNode,Out) :-
   compound_term(ASTNode,Functor,Args),
   (
      term_meta(ASTNode,Meta),
      lookup_in_meta(ops(FirstPrefixOps,FirstInfixOps,FirstPostfixOps),Meta),
      (
         memberchk(op(_,_,Functor),FirstPrefixOps),Args = [_],write_functors(Functor,Args,Out)
         ;
         memberchk(op(_,_,Functor),FirstInfixOps),Args = [_,_],write_functor_infix(Functor,Args,Out)
         ;
         memberchk(op(_,_,Functor),FirstPostfixOps),Args=[_],write_functors(Functor,Args,Out)
      )
      ;
      write_functors(Functor,Args,Out)
   ),!.
   
write_clause(ASTNode,_) :- throw(internal_error('[Formatter] the given term has an unexpected type',ASTNode)).


write_functors(Functor,ClauseList,Concated_List) :-
   write_term_list(ClauseList,RestList),
   atomic_list_concat([Functor,'(',RestList,')'],Concated_List).


write_functor_infix(',',[H|T],Concated_List) :-
   write_clause(H,First),
   write_rest_clause(T,Rest),
   atomic_list_concat([First,',',' ',Rest],Concated_List).

write_functor_infix(Functor,[H|T],Concated_List) :-
   write_clause(H,First),
   write_rest_clause(T,Rest),
   atomic_list_concat([First,' ',Functor,' ',Rest],Concated_List).


write_rest_clause([],'').

write_rest_clause([H|T],Concated_List) :-
   write_clause(H,First),
   write_rest_clause(T,Rest),
   atomic_list_concat([First,Rest],Concated_List).


write_List([],'').

write_List([H|T],Concated_List) :-
   (
      compound_term(H,'.',Args),write_List(Args,First),write_In_List(T,'false',Rest),atomic_list_concat([First,Rest],Output)
      ;
      write_clause(H,First),write_In_List(T,'false',Rest),atomic_list_concat([First,Rest],Output)
   ),!
   ,atomic_list_concat(['[',Output,']'],Concated_List).


write_In_List([],_,'').

write_In_List([H|T],Dotted,Output) :-
   (
      Dotted = 'false',compound_term(H,'.',Args),write_In_List(Args,'true',Output)
      ;
      Dotted = 'true',compound_term(H,'.',Args),write_List(Args,Out),atomic_list_concat([',',Out],Output)
      ;
      string_atom(H,'[]'),write_In_List(T,'false',Rest),atomic_list_concat([Rest],Output)
      ;
      T = [],write_clause(H,First),write_In_List(T,'false',Rest),atomic_list_concat(['|',First,Rest],Output)
      ;
      write_clause(H,First),write_In_List(T,'false',Rest),atomic_list_concat([',',First,Rest],Output)
   ),!.


write_term_list([Arg|Args],Concated_List) :-
   write_clause(Arg,H),
   write_term_list_rest(Args,T),
   atomic_list_concat([H,T],Concated_List).


write_term_list_rest([],'').

write_term_list_rest([Clause|Clauses],Concated_List) :-
   write_clause(Clause,H),
   write_term_list_rest(Clauses,T),
   atomic_list_concat([',',H,T],Concated_List).