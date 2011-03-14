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
   format_clauses/3]
   ).


:- use_module('AST.pl').
:- use_module('Utils.pl').

/*
   format_clauses formats a clause or clauses in the ISO-Prlog standard and returns a char_sequence
   format_clauses(+Clauses,-FormattedClauses).
   It is possibel to configure the linewidth manually with a list of options like [linewidth(Value)]
*/
format_clauses(Clauses,FClauses) :-  membercheck(X,[],30),format_terms(Clauses,'',X,FClauses).

format_clauses(Clauses,FClauses,Options) :- membercheck(X,Options,30),format_terms(Clauses,'',X,FClauses).


/*
   format_term/4 checks first the clauses name and arity to able to compare this with the next clause
   is there isn't a clauses at all, the term will be simply formatted.
   format_terms(+Clauses,+EmptyCharSequence,+Linewidth,-Formatted).
*/
format_terms([],L,_,L).
format_terms([H|T],In,Linewidth,OutputList) :-
   (
      (
         % if first term is a :- then we have to check the head for clause name and arity
         compound_term(H,':-',[First|_]),
         compound_term(First,CurrentName,CurrentArity),
         length(CurrentArity,ListLenght)
      ;
         % else the first term ist already a clause
         compound_term(H,CurrentName,CurrentArity),
         length(CurrentArity,ListLenght)
      ),!,format_terms([H|T],In,[CurrentName,ListLenght],Linewidth,OutputList)
   ;
      construct_clause(H,1200,Linewidth,Out), atomic_list_concat([In,Out,'.\n'],Concated_List),format_terms(T,Concated_List,Linewidth,OutputList)
   ).

/*
   format_term/5 builds up on format_terms/4 and has the name and arity of the last clauses as arguments as well.
   It then compares name and arity with the current one to set the empty lines betweet for example different clauses.
   format_terms(+Clauses,+EmptyCharSequence,+[ClauseName,ClauseArity],+Linewidth,-Formatted).
*/
format_terms([],L,_,_,L).
format_terms([H|T],In,[ClauseName,ClauseArity],Linewidth,OutputList) :-
   (  %look up current name and arty
      compound_term(H,':-',[First|_]),
      compound_term(First,CurrentName,CurrentArity),
      length(CurrentArity,ListLenght)
   ;
      compound_term(H,CurrentName,CurrentArity),
      length(CurrentArity,ListLenght)
   ),!,
   (  %compares current term and last term
      CurrentName = ClauseName, ListLenght = ClauseArity,
      construct_clause(H,1200,Linewidth,Out),
      atomic_list_concat([In,Out,'.\n'],Concated_List)
   ;
      CurrentName = ClauseName,
      construct_clause(H,1200,Linewidth,Out),
      atomic_list_concat([In,'\n',Out,'.\n'],Concated_List)
   ;
      construct_clause(H,1200,Linewidth,Out),
      atomic_list_concat([In,'\n\n',Out,'.\n'],Concated_List)
   ),!,format_terms(T,Concated_List,[CurrentName,ListLenght],Linewidth,OutputList).

/*
   construct_clause compares the default or manually set linewidth with the actuall lenght of the construced clause. At the moment three different line formatings are possible.
   First, a clause fits completely into a line.
   In the second case there will be a line break after :- and in the last case there will be a line break after :- and every following AND (,). After a line break, the following line
   will be shifted two spaces to the right.
   construct_clause(+Clause,+Priority,+Linewidth,-Out).
*/
construct_clause(Clause,Priority,Linewidth,Out) :- write_clause(Clause,Priority,[0,none],Output), string_length(Output,Length),Length =< Linewidth, atomic_list_concat([Output],Out).
construct_clause(Clause,Priority,Linewidth,Out) :-
   compound_term(Clause,':-',[First|[Rest|_]]),
   write_clause(First,Priority,[0,none],FirstOut),
   write_clause(Rest,Priority,[0,none],RestOut),
   getShift(2,RestOut,RestTabbed),
   atomic_list_concat([FirstOut,' :-\n',RestTabbed],Out),
   atom_codes(RestTabbed,InputList),
   split(InputList,"\n",Split),
   checkLength(Split,Linewidth).
construct_clause(Clause,Priority,_,Out) :-
   compound_term(Clause,':-',[First|Rest]),
   write_clause(First,Priority,[2,'and'],FirstOut),
   build_body(Rest,Priority,[2,'and'],RestOut),
   atomic_list_concat([FirstOut,' :-\n',RestOut],Out).
%    atom_codes(RestOut,InputList),
%    split(InputList,"\n",Split),
%    checkLength(Split,Linewidth).
   
% construct_clause(Clause,Priority,_,Out) :-
%    compound_term(Clause,':-',[First|Rest]),
%    write_clause(First,Priority,[2,'or'],FirstOut),
%    build_body(Rest,Priority,[2,'or'],RestOut),
%    atomic_list_concat([FirstOut,' :-\n',RestOut],Out).

/*
   build_body constructs the body part and goes through every term if the third case of construc clause is needed and there has to be a line break every and.
   build_body(+Clause,+Priority,+[Depth,Mode],-Out).
*/
build_body([],_,_,'').
build_body([H|T],Priority,[Depth,Mode],Out) :-
   write_clause(H,Priority,[Depth,Mode],HeadOut),
   build_body(T,Priority,[Depth,Mode],RestOut),
   getShift(Depth,HeadOut,Shifted),
   atomic_list_concat([Shifted,RestOut],Out).

/*
   checks the length of lists of lists, e.g. [[1,2,3],[4,5,6,7]] = 3 % 4
   and compares it to a given linewidth. It is used to check the length of lists, after a char sequeces is be split at '\n' into several subsequences.
   Finally it determines whether or not the next case of contruct_clauses has to be used.
   checkLength(+List of Lists,+Linewidth).
*/
checkLength([],_).
checkLength([H|T],Linewidth) :- length(H,Length), Length =< Linewidth,checkLength(T,Linewidth).

/*
   membercheck/3 sets either the default value for the linewidth or the user one when set as an option.
   membercheck(-Value,+Options,+DefaultValue)
*/
membercheck(Value,Options,DefaultValue) :-
 (memberchk(linewidth(Value),Options) ; Value is DefaultValue),!.

/*
   build_string_sequence constructs an atom / char sequence and is able to replace chars in this atom
   build_string_sequence(+Term,+OldC,+NewC,-Sequence)
*/
build_string_sequence(Term,OldC,NewC,Sequence) :-
   term_to_atom(Term,Atom), atom_codes(Atom,InputList) ,replace_char_with_string(InputList,OldC,NewC,OutputList),atom_codes(Sequence,OutputList).

/*
  write_clause formates the different kinds of terms, e.g. variables, string-atoms, interger-atoms, lists, and compound terms.
  While handeling a compound term, we distingush between operators (prefix,infix,postfix) in the op table and user-created terms like test(a,b,c).
  write_clause(+ASTNode,+Priority,+Depth,-Out)
*/
write_clause(ASTNode,_,_,Value) :-
   (
      variable(ASTNode,Value)
   ;
      anonymous_variable(ASTNode,Value)
   ;
      string_atom(ASTNode,QValue),build_string_sequence(QValue,"\n","\\n",Value)
   ;
      integer_value(ASTNode,Value)
   ;
      float_atom(ASTNode,Value)
   ),!.
write_clause(ASTNode,Priority,Depth,Out) :-
   compound_term(ASTNode,'.',Args),
   write_List(Args,Priority,Depth,Out),!.
write_clause(ASTNode,Priority,[Depth,Mode],Out) :-
   compound_term(ASTNode,Functor,Args),
   (
      term_meta(ASTNode,Meta),
      lookup_in_meta(ops(FirstPrefixOps,FirstInfixOps,FirstPostfixOps),Meta),
      (  % CompoundTerm has a prefix functor
         memberchk(op(Func_Priority,_,Functor),FirstPrefixOps),
         Args = [_],
         (
            Func_Priority > Priority,
            write_functors(Args,Priority,[Depth,Mode],Output),
            atomic_list_concat([Functor,'(',Output,')'],Out)
         ;
            write_functors(Args,Func_Priority,[Depth,Mode],Output),
            atomic_list_concat([Functor,' ',Output],Out)
         )
      ;  % CompoundTerm has an infix functor
         memberchk(op(Func_Priority,_,Functor),FirstInfixOps),Args = [_,_],
         (
         Func_Priority > Priority,
            (
               Mode = 'or', Functor =';',
               write_functor_infix(Functor,Args,Func_Priority,[Depth,Mode],Output),
               getShift(Depth,')',ShiftedBracket),
               getShift(Depth,Output,ShiftedOutput),
               atomic_list_concat(['(\n',ShiftedOutput,'\n',ShiftedBracket],Out)
            ;
               write_functor_infix(Functor,Args,Func_Priority,[Depth,Mode],Output),
               atomic_list_concat(['(',Output,')'],Out)
            )
         ;
         (
             Functor =';',
            write_functor_infix(Functor,Args,Func_Priority,[Depth,Mode],Output),
            atomic_list_concat([Output],Out)
            ;
            write_functor_infix(Functor,Args,Func_Priority,[Depth,Mode],Output),
            atomic_list_concat([Output],Out)
         )
         )
      ;  %CompoundTerm has a postfix functor
         memberchk(op(_,_,Functor),FirstPostfixOps),Args=[_],write_functors(Args,Priority,[Depth,Mode],Out),atomic_list_concat(['(',Output,')',Functor,' '],Out)
      )
      ;
      %CompoundTerm has an unknown functor
      write_functors(Args,Priority,[Depth,Mode],Output),atomic_list_concat([Functor,'(',Output,')'],Out)
   ),!.
write_clause(ASTNode,_,_,_) :- throw(internal_error('[Formatter] the given term has an unexpected type',ASTNode)).

/*
   wite_functors simply handles the parameter list of a functor and every following term in this list
   write_functors(+ParameterList,+Priority,+Depth,-RestList)
*/
write_functors(ParameterList,Priority,Depth,RestList) :- write_term_list(ParameterList,Priority,Depth,RestList).

/*
   write_functor_infix concentrates on infix functors like AND OR and any other type of infix functor.
   AND functors and handled differently in the case of construc_clause three to insert a line break after every AND functor
   write_functor_infix(+Functor,+Args,+Priority,+Depth,-Concated_List).
*/
write_functor_infix(',',[H|T],Priority,[Depth,Mode],Concated_List) :-
   write_clause(H,Priority,[Depth,Mode],First),
   write_rest_clause(T,Priority,[Depth,Mode],Rest),
   (
      Depth > 0,Mode = 'and',
      getShift(Depth,Rest,Shifted),
      atomic_list_concat([First,',','\n',Shifted],Concated_List)
   ;
      atomic_list_concat([First,',',' ',Rest],Concated_List)
   ),!.
write_functor_infix(';',[H|T],Priority,[Depth,Mode],Concated_List) :-
   write_clause(H,Priority,[Depth,Mode],First),
   write_rest_clause(T,Priority,[Depth,Mode],Rest),
   atomic_list_concat([First,' ; ',Rest],Concated_List).
write_functor_infix(Functor,[H|T],Priority,Depth,Concated_List) :-
   write_clause(H,Priority,Depth,First),
   write_rest_clause(T,Priority,Depth,Rest),
   atomic_list_concat([First,' ',Functor,' ',Rest],Concated_List).


/*
   write_rest_clause goes straight through al remaining terms of a clause
   write_rest_clause(+Terms,+Priority,+Depth,-Concated_List).
*/
write_rest_clause([],_,_,'').
write_rest_clause([H|T],Priority,Depth,Concated_List) :-
   write_clause(H,Priority,Depth,First),
   write_rest_clause(T,Priority,Depth,Rest),
   atomic_list_concat([First,Rest],Concated_List).

/*
   write_List is the basic case when the formatter just entered a new list. It now assumes that it is in a list and writes in a list.
   write_List(+Terms,+Priority,+Depth,-Concated_List).
*/
write_List([],_,_,'').
write_List([H|T],Priority,Depth,Concated_List) :-
   (
      compound_term(H,'.',Args),write_List(Args,Priority,Depth,First),write_In_List(T,'false',Priority,Depth,Rest),atomic_list_concat([First,Rest],Output)
   ;
      write_clause(H,Priority,Depth,First),write_In_List(T,'false',Priority,Depth,Rest),atomic_list_concat([First,Rest],Output)
   ),!,atomic_list_concat(['[',Output,']'],Concated_List).

/*
   write_In_List assumes that the formatter is already in list. Now it has to handle different cases:
   - If we are just in one list
   - If we are entering a new list
   - If we reached the end of a list(different endings of a list are possible)
   write_In_List(+Terms,+Dotted,+Priority,+Depth,-Output).
*/
write_In_List([],_,_,_,'').
write_In_List([H|T],Dotted,Priority,Depth,Output) :-
   (
      Dotted = 'false',compound_term(H,'.',Args),write_In_List(Args,'true',Priority,Depth,Output)
   ;
      Dotted = 'true',compound_term(H,'.',Args),write_List(Args,Priority,Depth,Out),atomic_list_concat([',',Out],Output)
   ;
      string_atom(H,'[]'),write_In_List(T,'false',Priority,Depth,Rest),atomic_list_concat([Rest],Output)
   ;
      T = [],write_clause(H,Priority,Depth,First),write_In_List(T,'false',Priority,Depth,Rest),atomic_list_concat(['|',First,Rest],Output)
   ;
      compound_term(H,',',_),write_clause(H,0,Depth,First),write_In_List(T,'false',Priority,Depth,Rest),atomic_list_concat([',',First,Rest],Output)
   ;
      write_clause(H,Priority,Depth,First),write_In_List(T,'false',Priority,Depth,Rest),atomic_list_concat([',',First,Rest],Output)
   ),!.

/*
   write_term_list handles the basic case to write the parameters of an function
   If a parameter is an AND term, the priority is changed accordingly
   write_term_list(+Terms,+Priority,+Depth,-Concated_List).
*/
write_term_list([Arg|Args],Priority,Depth,Concated_List) :-
   (
      compound_term(Arg,',',_),
      write_clause(Arg,0,Depth,H)
   ;
      write_clause(Arg,Priority,Depth,H)
   ),
   write_term_list_rest(Args,Priority,Depth,T),
   atomic_list_concat([H,T],Concated_List),!.

/*
   write_term_list_rest handles the rest of the parameters, where the difference can be found in the concatination.
   If a parameter is an AND term, the priority is changed accordingly
   write_term_list_rest(+Terms,+Priority,+Depth,-Concated_List).
*/
write_term_list_rest([],_,_,'').
write_term_list_rest([Arg|Args],Priority,Depth,Concated_List) :-
(
   compound_term(Arg,',',_),
   write_clause(Arg,0,Depth,H)
   ;
   write_clause(Arg,Priority,Depth,H)
   ),
   write_term_list_rest(Args,Priority,Depth,T),
   atomic_list_concat([',',H,T],Concated_List),!.

/*
   getShift shifts a char sequence for specific amount to the right or rather inserts a specific amount of spaces in front of the sequence.
   getShift(+Amount,+OldTab,-NewTab).
*/
getShift(Amount,OldTab,NewTab) :-
   (
      Amount > 0,
      NewAmount is Amount - 1,
      getShift(NewAmount,OldTab,Tab),
      atomic_list_concat([' ',Tab],NewTab)
   ;
      atomic_list_concat([OldTab],NewTab)
   ),!.
   
/*
  split split a string at the corresponding delimiter and returns a list of lists/strings
  split(+String,+Delimiters,-Result).
*/
split(String,"",Result) :- !, characters_to_strings(String, Result).
split(String,Delimiters,Result) :- real_split(String, Delimiters, Result).

/*
   Simple case when just splitting
   characters_to_strings(+String, -Result).
*/
characters_to_strings([],[]).
characters_to_strings([C|Cs],[[C]|Ss]) :-
   characters_to_strings(Cs,Ss).

/*
   Complex case when using a delimiter
   real_split(+String,+Delimiters,-Result).
*/
real_split(String,Delimiters,Result) :-
   (
      append(Substring,[Delimiter|Rest],String),
      memberchk(Delimiter,Delimiters)
      ->  Result = [Substring|Substrings],
      real_split(Rest,Delimiters,Substrings)
   ;
      Result = [String]
   ).

 