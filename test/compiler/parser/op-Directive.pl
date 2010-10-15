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
	Tests related to parsing lists.

   @author Michael Eichberg (mail@michael-eichberg.de)
*/
:- ensure_loaded('src/compiler/Lexer.pl').
:- ensure_loaded('src/compiler/Parser.pl').


:- begin_tests(parser_op_directive).

		
test(yf_successful) :- 
	tokenize_string(":- op(300,yf,$).X = (a $ $ + b ).",Ts),
	program(
		Ts,
		[ct(pos([], 1, 18), =, [
			v(pos([], 1, 16), 'X'), 
			ct(pos([], 1, 27), +, [
				ct(pos([], 1, 25), $, [
					ct(pos([], 1, 23), $, [
						a(pos([], 1, 21), a)])]), 
				a(pos([], 1, 29), b)])])]).
	
test(xf_successful) :- 
	tokenize_string(":- op(300,xf,$).X = (a $ + b ).",Ts),
	program(
		Ts,
		[ct(pos([], 1, 18), =, [
			v(pos([], 1, 16), 'X'), 
			ct(pos([], 1, 25), +, [
				ct(pos([], 1, 23), $, [a(pos([], 1, 21), a)]), 
				a(pos([], 1, 27), b)])])]).

test(xf_fail,[fail]) :- 
	tokenize_string(":- op(300,xf,$).X = (a $ $ + b ).",Ts),
	program(Ts,_).

			
:- end_tests(parser_op_directive).