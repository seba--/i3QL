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
	A parser to parse (ISO) Prolog.</br>
	
	<p>
	<b>Prolog Grammar Specification</b><br/>
	<i>Notes</i><br/>
	As the argument of a compound term only operators with priority less than 1000
	are allowed.<br/>
	Special cases that are not reflected by the grammar are:
	<ul>
	<li> "\+ (a,b)" and "\+(a,b)" are different statements; the first term
	is actually equivalent to: "'\+'(','(a,b))"</li>
	<li> "V=(a,b)" is equivalent to: "'='(V,(a,b))" ("'='(V,','(a,b))")
	</ul>
	<blockquote>
	<code><pre>
	clause ::= term{&lt;=1200} '.'

	term{MAX_PRIORITY} ::= prefix_op{MAX_PRIORITY}, term
	term{MAX_PRIORITY} ::= term, infix_op{MAX_PRIORITY}, term
	term{MAX_PRIORITY} ::= term, postfix_op{MAX_PRIORITY}
	term{MAX_PRIORITY} ::= elementary_term

	elementary_term ::= 
		stringatom | floatatom | integeratom | 
		variable | 
		compound_term | 
		list | nested_term | dcg_expr
	compound_term ::= functor'(' term{&lt;=999} (',' term{&lt;=999})* ')'	% NOTE no whitespace is allowed between the functor and the opening bracket 
	list ::= '[' list_elems? ']'
	list_elems ::= term{&lt;=999} (',' term{&lt;=999})*  ('|' term{&lt;=999})? 
	nested_term ::= '(' term{&lt;=1200} ')'
	dcg_expr ::= '{' term{&lt;=1200} '}'

	prefix_op{MAX_PRIORITY} ::= stringatom
	postfix_op{MAX_PRIORITY} ::= stringatom
	infix_op{MAX_PRIORITY} ::= 
		stringatom | 
		functor % EXAMPLE "V=(A,B)"; here "=" is indeed an infix operator in functor postion

	integeratom ::= &lt;an integer value&gt;
	floatatom ::= &lt;a floating point value&gt;
	functor ::= &lt;stringatom in functor position&gt;
	stringatom ::= &lt;either a sequence of characters enclosed in "'"'s, an operator (sequence) or a "plain" 
			name starting with a lower-case letter&gt;
	variable ::= &lt;a "plain" name starting with an upper-case letter or "_"&gt;
	</pre></code>
	</blockquote>
	</p>
   
   @author Michael Eichberg (mail@michael-eichberg.de)
   @version 0.9 - Sep., 1st 2010 
		The parser works reasonably well for the tested examples.
*/
:- module(
   	'SAEProlog:Compiler:Parser',
   	[
      	program/2,
			default_op_table/1
   	]
	).




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
		X=[T|_], % the parser failed while parsing the statement beginnig with T
		write('\nERROR: could not parse clause starting with '),write(T),
		fail 
	).
		

/**
	The list of the default operators. 
	<p>
	This list can be maintained using.
	<code>:- op(Priority, Op_Specifier, Operator) </code> directives.
	op(Priority, Op_Specifier, Operator) is true, with the side effect that
	<ul>
	<li>if Priority is 0 then Operator is removed from the operator table, else</li>
	<li>Operator is added to the Operator table, with the specified Priority (lower binds 
		tighter) and Associativity (determined by Op_Specifier according 
		to the rules):
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
	same type and name.</br>
	<br />
	The initial operator table is given by:
	<pre>
	Priority	Specifier	Operator(s)
	1200		xfx			:- -->
	1200		fx				:- ?-
	1100		xfy			;
	1050		xfy			->
	1000		xfy			','
	900		fy				\+
	700		xfx			= \=
	700		xfx			== \== @< @=< @> @>=
	700		xfx			=..
	700		xfx			is =:= =\= < =< > >=
	500		yfx			+ - /\ \/
	400		yfx			* / // rem mod << >>
	200		xfx			**
	200		xfy			^
	200		fy				- \
	</pre>
	Parts of this text are taken from: 
	<a href="http://pauillac.inria.fr/~deransar/prolog/bips.html">
	http://pauillac.inria.fr/~deransar/prolog/bips.html
	</a>.
	</p>

	@signature default_op_table(ops(PrefixOperators,InfixOperators,PostfixOperators))
	@arg(out) PrefixOperators is the list of all predefined prefix operators.
	@arg(out) InfixOperators is the list of all predefined infix Operators.
	@arg(out) PostfixOperators is the list of all predefined postfix operators.
*/
default_op_table(
	ops(
		[  % PREFIX...
			op(900,fy,'\\+'),
			op(200,fy,'-'),	
			op(200,fy,'\\'), % bitwise complement
			op(1200,fx,':-'),
			op(1200,fx,'?-')			
		],
		[	% INFIX...
			op(1200,xfx,':-'),
			op(700,xfx,'='),
			op(1100,xfy,';'),
			op(1000,xfy,','), % Redefining "and" is NOT supported!
			op(500,yfx,'-'),
			op(500,yfx,'+'),
			op(1050,xfy,'->'),
			op(1200,xfx,'-->'),
			op(400,yfx,'*'),
			op(400,yfx,'/'),
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
			op(1100,xfy,'|'), % also defined by SWI Prolog
			op(400,yfx,'//'), % X // Y Division mit Ganzzahlergebnis
			op(400,yfx,'mod'),
			op(400,yfx,'<<'),
			op(400,yfx,'>>'),
			op(200,xfx,'**'),		
			op(200,xfy,'^')				

		],
		[	% POSTFIX...		
		]		
	)
).
/*
default_op_table(ops(PrefixOps,InfixOps,PostfixOps)) :-
	list_to_assoc(
		[
			-(':-',op(1200,fx,':-')),
			-('?-',op(1200,fx,'?-')),		

			-('\\+',op(900,fy,'\\+')),

			-('-',op(200,fy,'-')),	
			-('\\',op(200,fy,'\\')) % bitwise complement
		],
		PrefixOps
	),
	list_to_assoc(
		[
			-(':-',op(1200,xfx,':-')),
			-('-->',op(1200,xfx,'-->')),

			-(';',op(1100,xfy,';')),
			-('|',op(1100,xfy,'|')), % also defined by SWI Prolog

			-('->',op(1050,xfy,'->')),

			-(',',op(1000,xfy,',')), % Redefining "and" is NOT supported!

			-('=',op(700,xfx,'=')),
			-('\\=',op(700,xfx,'\\=')),
			-('is',op(700,xfx,'is')),
			-('<',op(700,xfx,'<')),
			-('>',op(700,xfx,'>')),
			-('=<',op(700,xfx,'=<')),
			-('>=',op(700,xfx,'>=')),
			-('=:=',op(700,xfx,'=:=')),
			-('=\\=',op(700,xfx,'=\\=')),	
			-('=..',op(700,xfx,'=..')),
			-('==',op(700,xfx,'==')),
			-('\\==',op(700,xfx,'\\==')),
			-('@<',op(700,xfx,'@<')),
			-('@=<',op(700,xfx,'@=<')),
			-('@>',op(700,xfx,'@>')),
			-('@>=',op(700,xfx,'@>=')),		

			-('\\/',op(500,yfx,'\\/')),
			-('/\\',op(500,yfx,'/\\')),	
			-('+',op(500,yfx,'+')),
			-('-',op(500,yfx,'-')),

			-('*',op(400,yfx,'*')),
			-('/',op(400,yfx,'/')),
			-('//',op(400,yfx,'//')), % X // Y Division mit Ganzzahlergebnis
			-('mod',op(400,yfx,'mod')),
			-('<<',op(400,yfx,'<<')),
			-('>>',op(400,yfx,'>>')),

			-('**',op(200,xfx,'**')),		
			-('^',op(200,xfy,'^'))				
		],
		InfixOps
	),
	empty_assoc(PostfixOps).
*/	



/******************************************************************************\
 *                                                                            *
 *                         I M P L E M E N T A T I O N                        *
 *                                                                            *
\******************************************************************************/

/*
The following pseudocode shows the underlying approach to parse Prolog terms using 
Definite Clause Grammars (DCGs). The operator table is an argument of 
each rule and the default operator table is defined by default_op_table.

---- 1. Step - NAIVE GRAMMAR - NOT WORKING... (left recursive)
term --> prefix_op, term.
term --> [a(_)]
term --> term, infix_op, term.
term --> term, postfix_op.
prefix_op --> [...].
postfix_op --> [...].
infix_op --> [...].

---- 2. Step - AFTER LEFT RECURSION REMOVAL...
term_r --> infix_op, term, term_r.
term_r --> postfix_op, term_r.
term_r --> [].

term --> prefix_op, term, term_r.
term --> [a(_)], term_r.

prefix_op --> [...].
postfix_op --> [...].
infix_op --> [...].
*/
		

program(Ops,[S|SRs]) --> 
	clause(Ops,S),
	program(Ops,SRs),{!}. % TODO Add code to update the operator table...		 	
program(_Ops,[]) --> {true}.



% TODO Add code to check that the term is a valid top-level term....
clause(Ops,Term) --> 
	% Check that term is valid top-level term; i.e., it is an atom, a compound 
	% term, a directive or a clause definition.
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
	@arg(in) MaxPriority the maximum allowed priority of a term
	@arg(in) LeftTerm the left term
	@arg(in) LeftTermPriority the priority of the left term
	@arg(out) Term the accepted term
	@arg(out) TermPriority the priority of the accepted term	
*/
term_r(Ops,MaxPriority,LeftTerm,LeftTermPriority,Term,TermPriority) --> 
	infix_op(Ops,op(Priority,Associativity,Op),Pos),
	{ MaxPriority >= Priority },  
	term(Ops,MaxPriority,RightTerm,RightTermPriority),
	{
		(
			(Associativity = xfx, Priority > LeftTermPriority, Priority > RightTermPriority) 
		; 
			(Associativity = yfx, Priority >= LeftTermPriority, Priority > RightTermPriority)
		; 
			(Associativity = xfy, Priority > LeftTermPriority, Priority >= RightTermPriority)
		), 
		IntermediateTerm = ct(Pos,Op,[LeftTerm,RightTerm])
	},
	term_r(Ops,MaxPriority,IntermediateTerm,Priority,Term,TermPriority).
term_r(Ops,MaxPriority,LeftTerm,LeftTermPriority,Term,TermPriority) --> 
	postfix_op(Ops,op(Priority,Associativity,Op),Pos), 
	{ MaxPriority >= Priority }, 
	{
		(	Associativity = xf -> 
			Priority > LeftTermPriority 
		; 
 			Priority >= LeftTermPriority 
		),
		/*(
			( Associativity = xf, Priority > LeftTermPriority ) 
		; 
 			( Associativity = yf, Priority >= LeftTermPriority )
		),
		*/
		IntermediateTerm = ct(Pos,Op,[LeftTerm])
	},
	term_r(Ops,MaxPriority,IntermediateTerm,Priority,Term,TermPriority).
term_r(_Ops,_MaxPriority,T,TP,T,TP) --> [].

term(Ops,MaxPriority,Term,TermPriority) --> 
	prefix_op(Ops,op(Priority,Associativity,Op),Pos),
	{ MaxPriority >= Priority }, 
	term(Ops,MaxPriority,RightTerm,RightTermPriority), 
	{
		(	Associativity = fx -> 
			Priority > RightTermPriority 
		; 
			Priority >= RightTermPriority
		),
		/*
		(
			(Associativity = fx, Priority > RightTermPriority) 
		; 
			(Associativity = fy, Priority >= RightTermPriority)
		),
		*/
		IntermediateTerm = ct(Pos,Op,[RightTerm])
	},
	term_r(Ops,MaxPriority,IntermediateTerm,Priority,Term,TermPriority).
term(Ops,MaxPriority,Term,TermPriority) --> 
	elementary_term(Ops,IntermediateTerm), 
	term_r(Ops,MaxPriority,IntermediateTerm,0,Term,TermPriority).

prefix_op(ops(PrefixOps,_,_),Op,Pos) --> 
	[a(X,Pos)],
	{ Op=op(_,_,X), memberchk(Op,PrefixOps) }.
	% { get_assoc(X,PrefixOps,Op) }.
	
infix_op(ops(_,InfixOps,_),Op,Pos) --> 
	(
		[a(X,Pos)]
	;
		[f(X,Pos)]
	),
	{ !, Op=op(_,_,X), memberchk(Op,InfixOps) }.
	% { get_assoc(X,InfixOps,Op) }.

postfix_op(ops(_,_,PostfixOps),Op,Pos) --> 
	[a(X,Pos)],
	{ Op=op(_,_,X), memberchk(Op,PostfixOps) }.
	% { get_assoc(X,PostfixOps,Op) }.


elementary_term(_Ops,V) --> var(V),{!}.
elementary_term(_Ops,A) --> atom(A),{!}.
elementary_term(Ops,CT) --> compound_term(Ops,CT),{!}.
elementary_term(Ops,LT) --> list(Ops,LT),{!}.
elementary_term(Ops,T) --> 
	['('(_OPos)],
	term(Ops,1200,T,_TermPriority),
	[')'(_CPos)],
	{!}.
elementary_term(_Ops,chars(Pos,C)) --> [chars(C,Pos)],{!}.
elementary_term(Ops,te(OPos,T)) --> 
	['{'(OPos)],
	term(Ops,1200,T,_TermPriority),
	['}'(_CPos)],
	{!}. % a term expression (used in combination with DCGs)


atom(a(Pos,A)) --> [a(A,Pos)],{!}. 
atom(i(Pos,I)) --> [i(I,Pos)],{!}.
atom(r(Pos,F)) --> [r(F,Pos)].%,{!}.


var(v(Pos,V)) --> [v(V,Pos)],{!}.
var(av(Pos,V)) --> [av(V,Pos)].%,{!}.



/*
 *  
 *			HANDLING LISTS
 *
 *			Lists are always represented using their canonic form: ".(H,T)" where
 *			T(ail) is either again a list element or the empty list atom "[]".
 *			
 *			Examples:
 *			?- [a,b|[]] = .(a,b).
 *			false.
 *			
 *			?- [a,b,c] = .(a,.(b,.(c,[]))).
 *			true.
 *			
 *			?- [a,b,c|d] = .(a,.(b,.(c,d))).
 *			true.
 */
list(Ops,T) --> 
	['['(Pos)],
	(
		[']'(_Pos)],{!,T=a(Pos,'[]')}
	;
		list_elements(Ops,Pos,T)		
	).

list_elements(Ops,Pos,ct(Pos,'.',[E,Es])) --> 
	term(Ops,999,E,_TermPriority),
	list_elements_2(Ops,Es).
	
list_elements_2(Ops,ct(Pos,'.',[E,Es])) --> 
	[a(',',Pos)],{!},
	term(Ops,999,E,_TermPriority),
	list_elements_2(Ops,Es).
list_elements_2(Ops,E) --> 
	[a('|',_)],{!},
	term(Ops,1200,E,_TermPriority),
	[']'(_Pos)].
list_elements_2(_Ops,a(Pos,'[]')) --> [']'(Pos)].% {!}.
/* OLD (uses the term "list" to represent lists)
list(Ops,list(Pos,T)) --> 
	['['(Pos)],
	list_2(Ops,T).

list_2(_Ops,[]) --> 
	[']'(_Pos)],{!}.
list_2(Ops,Es)--> 
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
	term(Ops,1200,E,_TermPriority).
list_elements_2(_Ops,[]) --> [].% {!}.	
*/



/*
 *  
 *			HANDLING OF COMPOUND TERMS
 *
 */

compound_term(Ops,ct(Pos,F,Args)) --> 
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
arguments_2(_Ops,[]) --> [].% {!}.

