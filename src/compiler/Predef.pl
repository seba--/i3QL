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


/* Definition of all operators and predicates that are either directly
	understood by SAE Prolog - i.e., that are taken care of during compilation -
	or which are pre-imlemented as part of SAE Prolog's library.
	
	The redefinition of built-in predicates (operators) is not supported. In 
	general, these predicates have the standard ISO semantics.

	@author Michael Eichberg
*/
:- module(
	'Compiler:Predef',
	[	predefined_predicates/2,
		predefined_operator/1,
		predefined_term_operator/1,
		predefined_arithmetic_operator/1,
		
		built_in_term/1]
).




/* Definition of all predicates directly suppported by SAE Prolog including all
	relevant properties.

	@signature predefined_predicates(UserDefinedProgram,CompleteProgram)
	@param UserDefinedProgram is the AST of the loaded SAE Prolog program.
	@param CompleteProgram is the merge of the AST of the loaded program and the 
		built-in predicates.
*/
predefined_predicates(UserDefinedProgram,CompleteProgram) :-
	CompleteProgram = [
	
		% Primary Predicates - we need to analyze the sub
		pred(';'/2,[],[deterministic(no)]), % or
		%(not yet supported:) ('->'/2,[],[deterministic(dependent)]), % If->Then(;Else)
		%(not yet supported:) ('*->'/2,[],[deterministic(dependent)]), % Soft Cut
		pred(','/2,[],[deterministic(dependent)]), % and
	
		% Extra-logical Predicates
		pred(nonvar/1,[],[deterministic(yes)]),
		pred(var/1,[],[deterministic(yes)]),
		pred(integer/1,[],[deterministic(yes)]),
		pred(atom/1,[],[deterministic(yes)]),
		pred(compound/1,[],[deterministic(yes)]),
		pred(functor/3,[],[deterministic(yes)]),

		% "Other" Predicates
		pred('='/2,[],[deterministic(yes)]), % unify
		pred('\\='/2,[],[deterministic(yes)]), % does not unify
		pred('/'('\\+',1),[],[deterministic(yes)]), % not
		pred('=='/2,[],[deterministic(yes)]), % term equality
		pred('\\=='/2,[],[deterministic(yes)]), % term inequality

		pred('is'/2,[],[deterministic(yes),mode(-,+)]), % "arithmetic" assignment (comparison)

		pred(true/0,[],[deterministic(yes)]), % always succeeds
		pred(false/0,[],[deterministic(yes)]), % always fails
		pred(fail/0,[],[deterministic(yes)]), % always fails
		pred('!'/0,[],[deterministic(yes)]), % cut

		pred('<'/2,[],[deterministic(yes),mode(+,+)]), % arithmetic comparison; requires both terms to be instantiated
		pred('=:='/2,[],[deterministic(yes),mode(+,+)]), % arithmetic comparison; requires both terms to be instantiated
		pred('=<'/2,[],[deterministic(yes),mode(+,+)]), % arithmetic comparison; requires both terms to be instantiated
		pred('=\\='/2,[],[deterministic(yes),mode(+,+)]), % arithmetic comparison; requires both terms to be instantiated
		pred('>'/2,[],[deterministic(yes),mode(+,+)]), % arithmetic comparison; requires both terms to be instantiated
		pred('>='/2,[],[deterministic(yes),mode(+,+)]) % arithmetic comparison; requires both terms to be instantiated
		% The "other" arithmetic operators ( +, -, *,...) are not top-level predicates!
		
		| UserDefinedProgram].



/*	predefined_operator(Operator) :- succeeds if Operator is an Operator that is
	defined by SAE prolog.
*/
predefined_operator(Operator) :-
	predefined_term_operator(Operator);
	predefined_arithmetic_operator(Operator).



predefined_term_operator('=').
predefined_term_operator('\\=').
predefined_term_operator('==').
predefined_term_operator('\\==').



predefined_arithmetic_operator('<').
predefined_arithmetic_operator('=:=').
predefined_arithmetic_operator('=<').
predefined_arithmetic_operator('=\\=').
predefined_arithmetic_operator('>').
predefined_arithmetic_operator('>=').