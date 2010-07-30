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

/**
	A parser that can parse full (ISO) Prolog.
   
   @author Michael Eichberg (mail@michael-eichberg.de)
   @version 0.9 - July, 28th 2010 
		The parser works reasonably well for the tested examples.
*/
:- module(
   	'SAEProlog:Compiler:Parser',
   	[
      	program/2
   	]
	).


/*
	In general, to parse prolog terms we would like to define a rule 
	"term ::= term | term op term". But, such left-recursive rules are not supported by
	DCGs and, hence, we had to transform the grammar to get rid of the left 
	recursion. Furthermore, to make the parser efficient, we tried to avoid
	any backtracking; the first – naive – version was ~1000 times slower than
	this "optimized" variant.
*/


/**
   Parses a list of tokens (<code>Ts</code>) and generates the AST of the 
   program (<code>P</code>). At the top-level the AST is a list of statements.
*/
program(Ts,P) :-
	default_op_table(Ops),
	program(Ops,P,Ts,X),
	(	
		X=[],! % we could parse all input
	;
		X=[T|_], % rest of the ...
		write('ERROR: could not parse clause starting with '),write(T),nl,
		fail 
	).
		

program(Ops,[S|SRs]) --> % this is actually a program/4 predicate...
	stmt(Ops,S),
	program(Ops,SRs),{!}. % ... add code to update the operator table!		 	
program(_Ops,[]) --> {true}.



% ... add code to check that the term is valid top-level term.... i.e., 
% it is an atom, a compound term, a directive or a clause definition
stmt(Ops,T) --> term(Ops,T),[a('.',_Pos)],{/*write(T),nl,*/!}. 



atom(a(A,Pos)) --> [a(A,Pos)],{!}. % 
atom(i(I,Pos)) --> [i(I,Pos)],{!}.
atom(r(F,Pos)) --> [r(F,Pos)],{!}.


var(v(V,Pos)) --> [v(V,Pos)],{!}.
var(av(V,Pos)) --> [av(V,Pos)],{!}.


primitive_term(_Ops,V) --> var(V),{!}.
primitive_term(_Ops,A) --> atom(A),{!}.
primitive_term(Ops,T) --> ['('(_OPos)],term(Ops,T),[')'(_CPos)],{!}.
primitive_term(Ops,CT) --> compound_term(Ops,CT),{!}.
primitive_term(Ops,LT) --> list(Ops,LT),{!}.
primitive_term(Ops,te(T)) --> ['{'(_OPos)],term(Ops,T),['}'(_CPos)],{!}. % a term expression


primary_term(Ops,pre(Op,Pos,PT)) --> 
	[a(Op,Pos)], % a prefix operator in a funtor position is not a prefix operator....
	{once(is_prefix(Op,Ops))},
	primary_term(Ops,PT).
primary_term(Ops,PT) --> primitive_term(Ops,PT).
primary_term(Ops,post(Op,Pos,PT)) --> 
	primitive_term(Ops,PT),
	{once(is_postfix(Op,Ops))},
	[a(Op,Pos)].



/*
term(Ops,infix(Op,Pos,PT1,T2)) -->
	primary_term(Ops,PT1),[a(Op,Pos)],{is_infix(Op,Ops)},term(Ops,T2).
term(Ops,PT) --> primary_term(Ops,PT).
*/
term(Ops,T) --> primary_term(Ops,PT),term_2(Ops,PT,T).
term_2(Ops,LT,infix(Op,Pos,LT,RT)) --> 
	[T],
	{( T = a(Op,Pos) ; T = f(Op,Pos) ),is_infix(Op,Ops),!}, % TODO add error handling
	primary_term(Ops,IRT),
	term_2(Ops,IRT,RT).
term_2(_Ops,T,T) --> {!}.



list(Ops,T) --> ['['(Pos)],list_2(Ops,Pos,T).
list_2(_Ops,Pos,a('[]',Pos))--> [']'(_Pos)],{!}.
list_2(Ops,_FEPos,Es)--> elements(Ops,Es),[']'(_CPos)].

elements(Ops,.(E,Es)) --> element(Ops,E),elements_2(Ops,Es).
elements_2(Ops,.(E,Es)) --> [a(',',_)],{!},element(Ops,E),elements_2(Ops,Es).
elements_2(Ops,E) --> [a('|',_)],{!},term(Ops,E).
elements_2(_Ops,[]) --> {!}.

/*
element(Ops,infix(Op,Pos,PT1,T2)) -->
	primary_term(Ops,PT1),[a(Op,Pos)],element(Ops,T2),
	{Op \= ',',Op \= '|',is_infix(Op,Ops),!}.
element(Ops,PT) --> primary_term(Ops,PT).
*/
element(Ops,T) --> primary_term(Ops,PT),element_2(Ops,PT,T).
element_2(Ops,LT,infix(Op,Pos,LT,RT)) --> 
	[T],
	{( T = a(Op,Pos) ; T = f(Op,Pos) ),Op \= ',',Op \= '|',is_infix(Op,Ops),!}, % TODO add error handling
	primary_term(Ops,IRT),
	element_2(Ops,IRT,RT).
element_2(_Ops,T,T) --> {!}.



compound_term(Ops,ct(F,Args,Pos)) --> 
	[f(F,Pos)],
	['('(_)],
	arguments(Ops,Args),
	[')'(_)].


arguments(Ops,[T|TRs]) --> argument(Ops,T),arguments_2(Ops,TRs).
arguments_2(Ops,[T|TRs]) --> [a(',',_Pos)],{!},argument(Ops,T),arguments_2(Ops,TRs).
arguments_2(_Ops,[]) --> {true}.


/*
argument(Ops,infix(PT1,Op,T2,Pos)) -->
	primary_term(Ops,PT1),[a(Op,Pos)],argument(Ops,T2),
	{Op \= ',',is_infix(Op,Ops),!}.
argument(Ops,PT) --> primary_term(Ops,PT).
*/
argument(Ops,T) --> primary_term(Ops,PT),argument_2(Ops,PT,T).
argument_2(Ops,LT,infix(Op,Pos,LT,RT)) --> 
	[T],
	{( T = a(Op,Pos) ; T = f(Op,Pos) ),Op \= ',', is_infix(Op,Ops),!}, % TODO add error handling
	primary_term(Ops,IRT),
	argument_2(Ops,IRT,RT).
argument_2(_Ops,T,T) --> {!}.



is_infix(Op,Ops) :-
	member(op(_,Mode,Op),Ops),
	(	
		Mode = xfx;
		Mode = xfy;
		Mode = yfx;
		Mode = yfy
	). %, write('is_infix: '),write(Op),nl.


is_postfix(Op,Ops) :-
	member(op(_,Mode,Op),Ops),
	(
		Mode = xf;
		Mode = yf
	). %,write('is_postfix: '),write(Op),nl.


is_prefix(Op,Ops) :-
	member(op(_,Mode,Op),Ops),
	(
		Mode = fx;
		Mode = fy
	). %,	write('is_prefix: '),write(Op),nl.



% For further details go to: http://pauillac.inria.fr/~deransar/prolog/bips.html
default_op_table([
		op(1200,xfx,':-'),
		op(1200,xfx,'-->'),
		op(1200,fx,':-'),
		op(1200,fx,'?-'),		

		op(1100,xfy,';'),

		op(1050,xfy,'->'),

		op(1000,xfy,','), % Redefining "and" is NOT supported!

		op(900,fy,'\\+'),

		op(700,xfx,'='),
		op(700,xfx,'\\='),
		op(700,xfx,'is'),
		op(700,xfx,'<'),
		op(700,xfx,'>'),
		op(700,xfx,'=<'),
		op(700,xfx,'>='),
		op(700,xfx,'=:='),
		op(700,xfx,'=\\='),	
		op(700,xfx,'=..'),
		op(700,xfx,'=='),
		op(700,xfx,'\\=='),
		op(700,xfx,'@<'),
		op(700,xfx,'@=<'),
		op(700,xfx,'@>'),
		op(700,xfx,'@>='),		

		op(500,yfx,'\\/'),
		op(500,yfx,'/\\'),	
		op(500,yfx,'+'),
		op(500,yfx,'-'),
	
		op(400,yfx,'*'),
		op(400,yfx,'/'),
		op(400,yfx,'//'), % X // Y Division mit Ganzzahlergebnis
		op(400,yfx,'mod'),
		op(400,yfx,'<<'),
		op(400,yfx,'>>'),

		op(200,xfx,'**'),		
		op(200,xfy,'^'),				
		op(200,fy,'-'),	
		op(200,fy,'\\') % bitwise complement	
			
	]).