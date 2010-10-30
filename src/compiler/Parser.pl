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
	A parser for (ISO) Prolog.</br>
	
	<p>
	<b>Prolog Grammar Specification</b><br/>
	<i>Notes</i><br/>
	The operators of a term used as an argument of a compound term must have a 
	priority less than 1000.<br/>
	Special cases that are not directly reflected by the grammar, but which are
	handled by the parser, are:
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
   program (<code>P</code>). P is a list of terms (Facts, Directives and Rule 
	definitions).
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
			op(1200,xfx,'-->'),
			op(1100,xfy,';'),
			op(1000,xfy,','), % Redefining "and" is NOT supported!
			op(700,xfx,'='),
			op(500,yfx,'-'),
			op(500,yfx,'+'),
			op(1050,xfy,'->'),
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





/******************************************************************************\
 *                                                                            *
 *                         I M P L E M E N T A T I O N                        *
 *                                                                            *
\******************************************************************************/

/*
	The following pseudocode shows the underlying approach to parse Prolog terms 
	using Definite Clause Grammars (DCGs). 

	---- 1. Step - NAIVE GRAMMAR - NOT WORKING... (left recursive)
	term --> prefix_op, term.
	term --> [a(_)]
	term --> term, infix_op, term.
	term --> term, postfix_op.
	prefix_op --> [...].
	postfix_op --> [...].
	infix_op --> [...].

	---- 2. Step - AFTER LEFT RECURSION REMOVAL...
	term --> prefix_op, term, term_r.
	term --> [a(_)], term_r.

	term_r --> infix_op, term, term_r.
	term_r --> postfix_op, term_r.
	term_r --> [].

	prefix_op --> [...].
	postfix_op --> [...].
	infix_op --> [...].
	
	----- Remarks
	The operator table is made an argument of each rule and the default operator 
	table is defined by default_op_table.
*/
		

program(Ops,R) --> 
	clause(Ops,S),
	{	!,
		(	
			S = ct(_DPos, ':-',[_Directive]),!, % a directive always has one argument
			R = SRs,
			process_directive(Ops,S,NewOps)
		;	
			\+ validate_clause(S),!,
			R = SRs,
			Ops = NewOps
		;
			R = [S|SRs],
			Ops = NewOps
		)
	},
	program(NewOps,SRs).
program(_Ops,[]) --> {true}.


% TODO add code to check that the operator definition is valid 
% TODO support the removal / redefinition of operators
process_directive(
		ops(PrefixOps,InfixOps,PostfixOps),
		ct(Pos,':-',[ct(_,op,[i(_,Priority), a(_, Specifier), a(_, Op)])]),
		ops(NewPrefixOps,NewInfixOps,NewPostfixOps)
) :- !,
	write('Operator definition: '),write(Op),nl,
	(  (Specifier = fx ; Specifier = fy),!,
		NewPrefixOps = [op(Priority,Specifier,Op)|PrefixOps],
		NewInfixOps = InfixOps,
		NewPostfixOps = PostfixOps
	;	(Specifier = xfx ; Specifier = xfy ; Specifier = yfx),!,
		NewPrefixOps = PrefixOps,
		NewInfixOps = [op(Priority,Specifier,Op)|InfixOps],
		NewPostfixOps = PostfixOps
	;	(Specifier = xf ; Specifier = yf),!,
		NewPrefixOps = PrefixOps,
		NewInfixOps = InfixOps,
		NewPostfixOps = [op(Priority,Specifier,Op)|PostfixOps]
	;
		% IMPROVE error message
		write('Illegal specifier: '),write(Pos),write(Specifier),nl,
		NewPrefixOps = PrefixOps,
		NewInfixOps = InfixOps,
		NewPostfixOps = PostfixOps
	).
process_directive(Ops,ct(Pos,':-',[Directive]),Ops) :- !,
	% IMPROVE error message
	write('Unknown/unsupported directive: '),write(Pos),write(Directive),nl.



validate_clause(_S). % TODO check that a clause definition is valid (e.g. that it is not a simple variable.)




/******************************************************************************\
 *                                                                            *
 *                       T H E   C O R E   P A R S E R                        *
 *                                                                            *
\******************************************************************************/




clause(Ops,Term) --> 
	term(Ops,1200,Term,_TermPriority),
	[a('.',_Pos)],
	{!}. 


/* @signature term{_r}(Ops,MaxPriority{,LeftTerm,LeftTermPriority},Term,TermPriority)
 	@arg(in) Ops table of operators
	@arg(in) MaxPriority the maximum allowed priority of a term
	@arg(in) LeftTerm the left term
	@arg(in) LeftTermPriority the priority of the left term
	@arg(out) Term the accepted term
	@arg(out) TermPriority the priority of the accepted term	
*/
term(Ops,MaxPriority,Term,TermPriority) --> 
	elementary_term(Ops,IntermediateTerm), 
	term_r(Ops,MaxPriority,IntermediateTerm,0,Term,TermPriority).
term(Ops,MaxPriority,Term,TermPriority) --> 
	prefix_op(Ops,op(Priority,Associativity,Op),Pos),
	{ 	MaxPriority >= Priority,
		(	Associativity = fx -> 
			MaxSubTermPriority is Priority - 1
		; %Associativity = fy 
			MaxSubTermPriority = Priority
		)
	}, 
	term(Ops,MaxSubTermPriority,SubTerm,_SubTermPriority), 
	term_r(Ops,MaxPriority,ct(Pos,Op,[SubTerm]),Priority,Term,TermPriority).
	
term_r(Ops,MaxPriority,LeftTerm,LeftTermPriority,Term,TermPriority) --> 
	infix_op(Ops,op(Priority,Associativity,Op),Pos),
	{	MaxPriority >= Priority,
		(	Associativity = yfx -> 
			Priority >= LeftTermPriority
		; %(Associativity = xfx, Associativity = xfy),
			Priority > LeftTermPriority
		)		
	},  
	term(Ops,Priority,RightTerm,RightTermPriority),
	{
		(	Associativity = xfy ->
			Priority >= RightTermPriority
		;% (Associativity = xfx; Associativity = yfx),
			Priority > RightTermPriority
		),
		IntermediateTerm = ct(Pos,Op,[LeftTerm,RightTerm])
	},
	term_r(Ops,MaxPriority,IntermediateTerm,Priority,Term,TermPriority).
term_r(Ops,MaxPriority,LeftTerm,LeftTermPriority,Term,TermPriority) --> 
	postfix_op(Ops,op(Priority,Associativity,Op),Pos), 
	{	MaxPriority >= Priority, 
		(	Associativity = xf -> 
			Priority > LeftTermPriority 
		; %Associativity = yf 
 			Priority >= LeftTermPriority 
		)
	},
	term_r(Ops,MaxPriority,ct(Pos,Op,[LeftTerm]),Priority,Term,TermPriority).
term_r(_Ops,_MaxPriority,T,TP,T,TP) --> [].



prefix_op(ops(PrefixOps,_,_),Op,Pos) --> 
	[a(X,Pos)],
	{ Op=op(_,_,X), memberchk(Op,PrefixOps) }.
	
	
infix_op(ops(_,InfixOps,_),Op,Pos) --> 
	(
		[a(X,Pos)]
	;
		[f(X,Pos)] % ... an infix operator in functor postion; e.g., V=(A,B)
	),
	{ !, Op=op(_,_,X), memberchk(Op,InfixOps) }.


postfix_op(ops(_,_,PostfixOps),Op,Pos) --> 
	[a(X,Pos)],
	{ Op=op(_,_,X), memberchk(Op,PostfixOps) }.



elementary_term(_Ops,V) --> var(V),{!}.
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
elementary_term(_Ops,A) --> atom(A),{!}.


atom(a(Pos,A)) --> [a(A,Pos)],{!}. 
atom(i(Pos,I)) --> [i(I,Pos)],{!}.
atom(r(Pos,F)) --> [r(F,Pos)].%,{!}.


var(v(Pos,V)) --> [v(V,Pos)],{!}.
var(av(Pos,V)) --> [av(V,Pos)].%,{!}.



/*
 *			HANDLING LISTS
 *
 *			Lists are represented using their canonic form: ".(Head,Tail)" where
 *			the Tail is typically either again a list element or the empty list 
 *			(atom) "[]".
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
list(Ops,T) --> ['['(Pos)], list_2(Ops,Pos,T), {!}.


list_2(_Ops,Pos,a(Pos,'[]')) --> [']'(_Pos)], {!}.
list_2(Ops,Pos,ct(Pos,'.',[E,Es])) --> 
	term(Ops,999,E,_TermPriority),
	list_elements_2(Ops,Es),
	{!}. % we can commit to the current term (E)


list_elements_2(_Ops,LE) --> [']'(Pos)], {!,LE=a(Pos,'[]')}.	
list_elements_2(Ops,LE) --> 
	[a('|',_)], {!},
	term(Ops,1200,LE,_TermPriority),
	[']'(_Pos)],
	{!}. % we can commit to the current term (LE)
list_elements_2(Ops,LE) --> 
	[a(',',Pos)], {!},
	term(Ops,999,E,_TermPriority),
	list_elements_2(Ops,Es),
	{!,LE=ct(Pos,'.',[E,Es])}. % we can commit to the current term (E)




/*
 *			HANDLING OF COMPOUND TERMS
 */
compound_term(Ops,ct(Pos,F,[T|TRs])) --> 
	[f(F,Pos)],
	['('(_)],
	term(Ops,999,T,_TermPriority), % a compound term has at least one argument
	arguments_2(Ops,TRs),
	{!}. % we can commit to the current term (T) and the other arguments (TRs)


arguments_2(_Ops,[]) --> [')'(_)],{!}.
arguments_2(Ops,[T|TRs]) --> 
	[a(',',_Pos)],
	term(Ops,999,T,_TermPriority), 
	arguments_2(Ops,TRs),{!}. % we can commit to the term (T) and the arguments (TRs)
	
