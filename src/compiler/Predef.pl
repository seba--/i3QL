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


/* Definition of the operators and predicates that are either directly
	understood by SAE Prolog - i.e., that are taken care of during compilation -
	or which are pre-implemented as part of SAE Prolog's library.
	
	The redefinition of built-in predicates (operators) is not supported. In 
	general, these predicates have the standard ISO semantics.

	@author Michael Eichberg
*/
:- module(
	'SAEProlog:Compiler:Predef',
	[	add_predefined_predicates_to_ast/2,
	
		default_op_table/1
	]
).

:- use_module('AST.pl').


/* Definition of all predicates directly suppported by SAE Prolog including all
	relevant properties.

	@signature append_predefined_predicates(UserDefinedProgram,CompleteProgram)
	@param UserDefinedProgram is the AST of the SAE Prolog program.
	@param CompleteProgram is the merge of the AST of the loaded program and the 
		built-in predicates.
*/
add_predefined_predicates_to_ast(AST,Program) :-
	add_predicates( 
		AST,
		[
	
		% Primary Predicates - we need to analyze the sub
		pred(';'/2,[deterministic(no)]), % or
		%'->'/2, and '*->'/2 are sub-predicates of "or"
		pred(','/2,[deterministic(dependent)]), % and
	
		% Extra-logical Predicates
		pred(nonvar/1,[deterministic(yes)]),
		pred(var/1,[deterministic(yes)]),
		pred(integer/1,[deterministic(yes)]),
		pred(atom/1,[deterministic(yes)]),
		pred(compound/1,[deterministic(yes)]),
		pred(functor/3,[deterministic(yes)]),

		% "Other" Predicates
		pred('='/2,[deterministic(yes)]), % unify
		pred('\\='/2,[deterministic(yes)]), % does not unify
		pred('/'('\\+',1),[deterministic(yes)]), % not
		pred('=='/2,[deterministic(yes)]), % term equality
		pred('\\=='/2,[deterministic(yes)]), % term inequality

		pred('is'/2,[deterministic(yes),mode(-,+)]), % "arithmetic" assignment (comparison)

		pred(true/0,[deterministic(yes)]), % always succeeds
		pred(false/0,[deterministic(yes)]), % always fails
		pred(fail/0,[deterministic(yes)]), % always fails
		pred('!'/0,[deterministic(yes)]), % cut

		pred('<'/2,[deterministic(yes),mode(+,+)]), % arithmetic comparison; requires both terms to be instantiated
		pred('=:='/2,[deterministic(yes),mode(+,+)]), % arithmetic comparison; requires both terms to be instantiated
		pred('=<'/2,[deterministic(yes),mode(+,+)]), % arithmetic comparison; requires both terms to be instantiated
		pred('=\\='/2,[deterministic(yes),mode(+,+)]), % arithmetic comparison; requires both terms to be instantiated
		pred('>'/2,[deterministic(yes),mode(+,+)]), % arithmetic comparison; requires both terms to be instantiated
		pred('>='/2,[deterministic(yes),mode(+,+)]) % arithmetic comparison; requires both terms to be instantiated
		% The "other" arithmetic operators ( +, -, *,...) are not top-level predicates!
		],
		Program
	).



/**
	The list of the default operators. 
	<p>
	This list is maintained using.
	<code>:- op(Priority, Op_Specifier, Operator) </code> directives which is
	always immediately evaluated by the parser.
	<code>op(Priority, Op_Specifier, Operator)</code> succeeds, with the side 
	effect that...
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
	1000		xfy			,
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

	@signature default_op_table(Ops)
	@mode det()
	@arg(out) Ops Ops has the following structure:<br/>
		<code>ops(PrefixOperators,InfixOperators,PostfixOperators)</code> where
		PrefixOperators is the list of all predefined prefix operators, 
		InfixOperators is the list of all predefined infix Operators, and
		PostfixOperators is the list of all predefined postfix operators.
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
			op(1050,xfy,'*->'),			
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
