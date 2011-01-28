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
	sae_predef,
	[	add_predefined_predicates_to_ast/2,
	
		default_op_table/1,
		
		predefined_functor/1,
		
		is_arithmetic_comparison_operator/1
	]
).

:- use_module('AST.pl').


/* 
(
	Excerpts of the following text are taken 
	from the online documentation of LogTalk
)
<h1>Modes</h1>
Many predicates cannot be called with arbitrary arguments with arbitrary
instantiation status. The valid arguments and instantiation modes can be
documented by using the mode/2 directive. (In general, SAE Prolog does not
use user-specified mode annotations, but calculates them for its own.) For
instance:
<code><pre>
    :- mode(member(?term, +list), zero_or_more).
</pre></code>
The first argument describes a valid calling mode. The minimum information will
be the instantiation mode of each argument. There are four possible values: 
<ul>
<li>"+" Argument must be instantiated.</li>
<li>"-" Argument must be a free (non-instantiated) variable.</li>
<li>"?" Argument can either be instantiated or free.</li>
<li>"@" Argument will not be modified.</li>
</ul>
These four mode atoms are also declared as prefix operators. This makes it
possible to include type information for each argument like in the example
above. Some of the possible type values are: callable, term, nonvar, var,
atomic, atom, number, integer, float, compound, and list. It is also possible to
use your own types. They can be either atoms or compound terms.

The second argument documents the number of proofs (or solutions) for the 
specified mode ([LowerBound,UpperBound]). The possible values are:

<ul>
<li>[0,0] Predicate always fails.</li>
<li>[1,1] Predicate always succeeds once.</li>
<li>[0,1] Predicate either fails or succeeds.</li>
<li>[0,*] Predicate has zero or more solutions.</li>
<li>[1,*] Predicate has one or more solutions.</li>
<li>error Predicate will throw an error (see below).</li>
</ul>

Note that most predicates have more than one valid mode implying several mode
directives. For example, to document the possible use modes of the atom_concat/3
ISO built-in predicate we would write:

    :- mode(atom_concat(?atom, ?atom, +atom), [1,*]).
    :- mode(atom_concat(+atom, +atom, -atom), [0,1]).

<h1>Metapredicate directive</h1>
Some predicates may have arguments that will be called as goals. To ensure that
these calls will be executed in the correct scope we need to use the
metapredicate/1 directive. For example:

    :- metapredicate(findall(*, :, *)).
The predicate arguments in this directive have the following meaning:

":" Meta-argument that will be called as a goal.
"*" Normal argument.

<h1>Dynamic directive</h1>
A predicate can be static or dynamic. By default, all predicates are static. To
declare a dynamic predicate we use the dynamic/1 directive:
<code><pre>
    :- dynamic(foo/1).
</pre></code>
Because each SAE Prolog file is compiled independently from other files, this
directive must be included in every file that uses or defines the described
predicate. If the dynamic declaration is omitted then the predicate definition
will be compiled to static code and predicate calls will statically be resolved.
Note that any static object may declare and define dynamic predicates.
*/


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
		% Extra-logical Predicates
		pred(nonvar/1,[solutions([0,1])]),
		pred(var/1,[solutions([0,1])]),
		pred(atomic/1,[solutions([0,1])]),
		pred(integer/1,[solutions([0,1])]),
		pred(float/1,[solutions([0,1])]),
		pred(atom/1,[solutions([0,1])]),
		pred(compound/1,[solutions([0,1])]),
		pred(functor/3,[solutions([0,1])]),

		% "Other" Predicates
		pred('='/2,[solutions([0,1])]), % unify
		pred('\\='/2,[solutions([0,1])]), % does not unify
		pred('/'('\\+',1),[solutions([0,1])]), % not
		pred('=='/2,[solutions([0,1])]), % term equality
		pred('\\=='/2,[solutions([0,1])]), % term inequality

		pred('is'/2,[solutions([0,1])]), % "arithmetic" evaluation
		pred(true/0,[solutions([1,1])]), % always succeeds
		pred(false/0,[solutions([0,0])]), % always fails
		pred(fail/0,[solutions([0,0])]), % always fails
		pred('!'/0,[solutions([1,1])]), % cut

		pred('=:='/2,[solutions([0,1]),mode(+number,+number)]), % arithmetic comparison; requires both terms to be instantiated
		pred('=\\='/2,[solutions([0,1]),mode(+,+)]), % arithmetic comparison; requires both terms to be instantiated
		pred('<'/2,[solutions([0,1]),mode(+number,+number)]), % arithmetic comparison; requires both terms to be instantiated
		pred('=<'/2,[solutions([0,1]),mode(+,+)]), % arithmetic comparison; requires both terms to be instantiated
		pred('>'/2,[solutions([0,1]),mode(+,+)]), % arithmetic comparison; requires both terms to be instantiated
		pred('>='/2,[solutions([0,1]),mode(+,+)]) % arithmetic comparison; requires both terms to be instantiated
		% The "other" arithmetic operators ( +, -, *,...) are not top-level predicates!
		],
		Program
	).



/**
	The list of (SAE) Prolog's default operators. 
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
			op(1200,fx,':-'),
			op(1200,fx,'?-'),
			op(900,fy,'\\+'),
			op(200,fy,'\\'), % bitwise complement
			op(200,fy,'-'), % arithmetic "minus"
			op(200,fx,'+'), % for mode declarations
			op(200,fx,'@'),  % for mode declarations
			op(200,fx,'?')  % for mode declarations
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



predefined_functor('=').
predefined_functor('\\=').
predefined_functor(',').
predefined_functor(';').
predefined_functor('!').
predefined_functor('*->').
predefined_functor('->').
predefined_functor('true').
predefined_functor('false').
predefined_functor('fail').
predefined_functor('not').
predefined_functor('\\+').
predefined_functor('is').
predefined_functor('*').
predefined_functor('-').
predefined_functor('+').
predefined_functor('<').
predefined_functor('=<').
predefined_functor('>').
predefined_functor('>=').
predefined_functor('=:=').
predefined_functor('=\\=').
predefined_functor('[]').
predefined_functor('.').



is_arithmetic_comparison_operator('=:=').
is_arithmetic_comparison_operator('=\\=').
is_arithmetic_comparison_operator('<').
is_arithmetic_comparison_operator('=<').
is_arithmetic_comparison_operator('>').
is_arithmetic_comparison_operator('>=').