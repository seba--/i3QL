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
      	program/2,
			default_op_table/1
   	]
	).


/* <b>Grammar Specification</b><br/>
	As the argument of a compound term only operators with priority less than 1000
	are allowed.
	<blockquote>
	<code>
	clause ::= term{<=1200} '.'
	
	term{PRIORITY} ::= prefix_op{PRIORITY}, term.
	term{PRIORITY} ::= primitive_term{PRIORITY=0}
	term{PRIORITY} ::= term, infix_op{PRIORITY}, term.
	term{PRIORITY} ::= term, postfix_op{PRIORITY}.
	
	primitive_term ::= atom | float | integer | variable | compound_term | list | nested_term | term_expr
	compound_term ::= functor'(' term{<=999} (',' term{<=999})* ')'	% NOTE no whitespace is allowed between the functor and the opening bracket 
	list ::= '[' list_elems? ']'
	list_elems ::= term{<=999} (',' term{<=999})*  ('|' term{<=999})? 
	nested_term ::= '(' term{<=1200} ')'
	term_expr ::= '{' term{<=1200} '}'
	
	prefix_op ::= atom
	postfix_op ::= atom
	infix_op ::= atom | functor % EXAMPLE "V=(A,B)"; here "=" is indeed an infix operator in functor postion
	
	integer ::= <an integer value>
	float ::= <a floating point value>
	atom ::= <either a string atom, an operator sequence or a "plain" name starting with a lower-case letter>
	variable ::= <a "plain" name starting with an upper-case letter or "_">
	</code>
	</blockquote>
*/


/**
   Parses a list of tokens (<code>Ts</code>) and generates the AST of the 
   program (<code>P</code>). At the top-level the AST is a list of statements.
*/
program(Ts,P) :-
	default_op_table(Ops),
	program(Ops,P,Ts,X),
	(	
		X=[],! % the parser succeeded (all tokens were accepted).
	;
		X=[T|_], % the parser failed while paring the statement beginnig with T
		write('\nERROR: could not parse clause starting with '),write(T),
		fail 
	).
		

/**
	op(Priority, Op_Specifier, Operator) is true, with the side effect that
	<ul>
	<li>if Priority is 0 then Operator is removed from the operator table, else</li>
	<li>Operator is added to the Operator table, with priority (lower binds 
		tighter) Priority and associativity determined by Op_Specifier according 
		to the rules:
		<pre>
			Specifier	Type		Associativity
			fx				prefix	no
			fy				prefix	yes
			xf				postfix	no
			yf				postfix	yes
			xfx			infix		no
			yfx			infix		left
			xfy			infix		right
		</pre>
	</li>
	</ul>
	It is forbidden to alter the priority or type of ','. It is forbidden to have
	an infix and a postfix operator with the same name, or two operators with the 
	same associativity and name.</br>
	<br />
	The initial operator table is given by:
	<pre>
	Priority	Specifier	 Operator(s)
	1200	 xfx	 :- -->
	1200	fx	 :- ?-
	1100	 xfy	 ;
	1050	 xfy	 ->
	1000	 xfy	 ','
	900	 fy	 \+
	700	 xfx	 = \=
	700	 xfx	 == \== @< @=< @> @>=
	700	 xfx	 =..
	700	 xfx	 is =:= =\= < =< > >=
	500	 yfx	 + - /\ \/
	400	 yfx	 * / // rem mod << >>
	200	 xfx	 **
	200	 xfy	 ^
	200	 fy	 - \
	</pre>
	Parts of this text are taken from: 
	<a href="http://pauillac.inria.fr/~deransar/prolog/bips.html">
	http://pauillac.inria.fr/~deransar/prolog/bips.html
	</a>.
*/
default_op_table([
		op(1200,xfx,':-'),
		op(1200,xfx,'-->'),
		op(1200,fx,':-'),
		op(1200,fx,'?-'),		

		op(1100,xfy,';'),
		op(1100,xfy,'|'), % also defined by SWI Prolog

		op(1050,xfy,'->'),

		op(1000,xfy,','), % Redefining "and" is NOT supported!

		op(950,yf,'yDEL'),	%%%%% JUST FOR TESTING PURPOSES!!!!!		

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



/******************************************************************************\
 *                                                                            *
 *                         I M P L E M E N T A T I O N                        *
 *                                                                            *
\******************************************************************************/

		

program(Ops,[S|SRs]) --> 
	clause(Ops,S),
	program(Ops,SRs),{!}. % TODO Add code to update the operator table...		 	
program(_Ops,[]) --> {true}.



% TODO Add code to check that the term is valid top-level term....
clause(Ops,Term) --> 
	% Check that term is valid top-level term; i.e., it is an atom, a compound 
	% term, a directive or a clause definition
	term(Ops,1200,Term,_TermPriority),
	[a('.',_Pos)],
	{
		/*write(T),nl,*/
		!
	}. 



/* Example usage: 
 	default_op_table(Ops), term(Ops,T,TP,['-','-',a(x),'yDEL','*',a(y)],[]),write(T).

	@signature (x_)term(Ops,T,TP)
 	@arg(in) Ops table of operators
	@arg(in) MaxPriority the maximum priority of a term
	@arg(in) LeftTerm the left term
	@arg(in) LeftTermPriority
	@arg(out) Term the accepted term
	@arg(out) TermPriority the priority of the accepted term	
*/
term_r(Ops,MaxPriority,LeftTerm,LeftTermPriority,Term,TermPriority) --> 
%{write('Testing infix...\n')},
	infix_op(Ops,op(Priority,Associativity,Op)),
%{write('succeed(1)...\n')},	
	{ MaxPriority >= Priority },  
%{write('succeed(2)...\n')},		
	term(Ops,MaxPriority,RightTerm,RightTermPriority),
%{write('succeed(3)...\n')},		
	{
		(
			(Associativity = xfx, Priority > LeftTermPriority, Priority > RightTermPriority) 
		; 
			(Associativity = yfx, Priority >= LeftTermPriority, Priority > RightTermPriority)
		; 
			(Associativity = xfy, Priority > LeftTermPriority, Priority >= RightTermPriority)
		),
		IntermediateTerm =.. [Op,LeftTerm,RightTerm]
	},
%{write('succeed(4)...'),write([MaxPriority,IntermediateTerm,Priority,Term,TermPriority]),nl},			
	term_r(Ops,MaxPriority,IntermediateTerm,Priority,Term,TermPriority).
term_r(Ops,MaxPriority,LeftTerm,LeftTermPriority,Term,TermPriority) --> 
	postfix_op(Ops,op(Priority,Associativity,Op)), 
	{ MaxPriority >= Priority }, 
	{
		(
			( Associativity = xf, Priority > LeftTermPriority ) 
		; 
 			( Associativity = yf, Priority >= LeftTermPriority )
		),
		IntermediateTerm =.. [Op,LeftTerm]
	},
	term_r(Ops,MaxPriority,IntermediateTerm,Priority,Term,TermPriority).
term_r(_Ops,_MaxPriority,T,TP,T,TP) --> [].

term(Ops,MaxPriority,Term,TermPriority) --> 
	prefix_op(Ops,op(Priority,Associativity,Op)),
	{ MaxPriority >= Priority }, 
	term(Ops,MaxPriority,RightTerm,RightTermPriority), 
	{
		(
			(Associativity = fx, Priority > RightTermPriority) 
		; 
			(Associativity = fy, Priority >= RightTermPriority)
		),
		IntermediateTerm =.. [Op,RightTerm]
	},
	term_r(Ops,MaxPriority,IntermediateTerm,Priority,Term,TermPriority).
term(Ops,MaxPriority,Term,TermPriority) --> 
	primitive_term(Ops,IntermediateTerm), 
	term_r(Ops,MaxPriority,IntermediateTerm,0,Term,TermPriority).

prefix_op(Ops,Op) --> [a(X,Pos)],{Op=op(_,C,X),member(Op,Ops),(C=fx;C=fy)}.
postfix_op(Ops,Op) --> [a(X,Pos)],{Op=op(_,C,X),member(Op,Ops),(C=xf;C=yf)}.
infix_op(Ops,Op) --> [a(X,Pos)],{Op=op(_,C,X),member(Op,Ops),(C=xfx;C=yfx;C=xfy)}.



primitive_term(_Ops,V) --> var(V),{!}.
primitive_term(_Ops,A) --> atom(A),{!}.
primitive_term(Ops,T) --> ['('(_OPos)],term(Ops,1200,T,_TermPriority),[')'(_CPos)],{!}.
primitive_term(Ops,CT) --> compound_term(Ops,CT),{!}.
primitive_term(Ops,LT) --> list(Ops,LT),{!}.
primitive_term(Ops,te(T)) --> ['{'(_OPos)],term(Ops,1200,T,_TermPriority),['}'(_CPos)],{!}. % a term expression


atom(a(A,Pos)) --> [a(A,Pos)],{!}. % 
atom(i(I,Pos)) --> [i(I,Pos)],{!}.
atom(r(F,Pos)) --> [r(F,Pos)],{!}.


var(v(V,Pos)) --> [v(V,Pos)],{!}.
var(av(V,Pos)) --> [av(V,Pos)],{!}.

% an atom has "priority 0"


/*
 *  
 *			HANDLING LISTS
 *
 */


list(Ops,T) --> 
	['['(Pos)],
	list_2(Ops,Pos,T).
list_2(_Ops,Pos,a('[]',Pos)) --> 
	[']'(_Pos)],{!}.
list_2(Ops,_FEPos,Es)--> 
	list_elements(Ops,Es),[']'(_CPos)].

list_elements(Ops,.(E,Es)) --> 
	term(Ops,999,E,_TermPriority),
	list_elements_2(Ops,Es).
list_elements_2(Ops,.(E,Es)) --> 
	[a(',',_)],{!},
	term(Ops,999,E,_TermPriority),
	list_elements_2(Ops,Es).
list_elements_2(Ops,E) --> 
	[a('|',_)],{!},
	term(Ops,999,E,_TermPriority).
list_elements_2(_Ops,[]) --> {!}.




/*
 *  
 *			HANDLING OF COMPOUND TERMS
 *
 */


compound_term(Ops,ct(F,Args,Pos)) --> 
	[f(F,Pos)],
	['('(_)],
	arguments(Ops,Args),
	[')'(_)].


arguments(Ops,[T|TRs]) --> 
	term(Ops,999,T,_TermPriority),
	arguments_2(Ops,TRs).
arguments_2(Ops,[T|TRs]) --> 
	[a(',',_Pos)],{!},
	term(Ops,999,T,_TermPriority),
	arguments_2(Ops,TRs).
arguments_2(_Ops,[]) --> {!}.

