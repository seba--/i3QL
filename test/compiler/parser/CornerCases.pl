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
	Tests related to corner cases.

   @author Michael Eichberg (mail@michael-eichberg.de)
*/
:- ensure_loaded('src/compiler/Lexer.pl').
:- ensure_loaded('src/compiler/Parser.pl').


:- begin_tests(parser_corner_cases).

test('V=(a,b)') :- % (a,b) is an anonymous tuple
	tokenize_string("V=(a,b).",Ts),
	clauses(Ts,[ct([pos([], 1, 1)|_], =, [v([pos([], 1, 0)|_], 'V'), ct([pos([], 1, 4)|_], ',', [a([pos([], 1, 3)|_], a), a([pos([], 1, 5)|_], b)])])]).

test(', as an atom') :- 
	tokenize_string("X=a(,,,).",Ts),
	clauses(
		Ts,
		[ct([pos([], 1, 1)|_], =, [
			v([pos([], 1, 0)|_], 'X'),
			ct([pos([], 1, 2)|_], a, [
				a([pos([], 1, 4)|_], ','), 
				a([pos([], 1, 6)|_], ',')])])]).

test('operator as functor') :- 
	tokenize_string("\\+ \\+(a,b).",Ts),
	clauses(Ts,[ct([pos([], 1, 0)|_], \+, [ct([pos([], 1, 3)|_], \+, [a([pos([], 1, 6)|_], a), a([pos([], 1, 8)|_], b)])])]).

test('escaped operators') :- 
	tokenize_string("'-' \\+(a,b).",Ts),
	clauses(Ts,[ct([pos([], 1, 0)|_], -, [ct([pos([], 1, 4)|_], \+, [a([pos([], 1, 7)|_], a), a([pos([], 1, 9)|_], b)])])]).
		
:- end_tests(parser_corner_cases).