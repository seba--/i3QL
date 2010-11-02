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


:- begin_tests(parser_lists).


test(
	empty_list,
	[	true(P=[a(pos(_,1,0),[])])	]
) :- tokenize_string("[].",Ts),program(Ts,P).


test(
	list_with_one_element,
	[	true(P=[ct(_,'.',[av(pos(_,1,1),'_'),a(_,[])])]) ]
) :- tokenize_string("[_].",Ts),program(Ts,P).


test(
	list_with_multiple_elements,
	[	true(P=[ct(pos([], 1, 0), ., [a(pos([], 1, 1), a), ct(pos([], 1, 2), ., [a(pos([], 1, 3), b), ct(pos([], 1, 4), ., [a(pos([], 1, 5), c), ct(pos([], 1, 6), ., [ct(pos([], 1, 9), ;, [a(pos([], 1, 8), d), a(pos([], 1, 10), e)]), a(pos([], 1, 12), [])])])])])])]
) :- tokenize_string("[a,b,c,(d;e)].",Ts),program(Ts,P).


test(
	list_with_a_open_tail,
	[	true(P = [ct(pos([], 1, 0), '.', [ct(pos([], 1, 3), ;, [a(pos([], 1, 2), a), a(pos([], 1, 4), e)]), v(pos([], 1, 7), 'T')])])]
) :- tokenize_string("[(a;e)|T].",Ts),program(Ts,P).


test(
	list_with_a_complex_term_as_a_tail,
	[	
		true(P=[
			ct(pos([], 1, 0), ., [
				ct(pos([], 1, 3), ;, [a(pos([], 1, 2), a), a(pos([], 1, 4), e)]),
				ct(pos([], 1, 6), ., [
					a(pos([], 1, 7), b), 
					ct(pos([], 1, 10), ;, [
						a(pos([], 1, 9), a), 
						ct(pos([], 1, 12), ,, [
							a(pos([], 1, 11), e), 
							a(pos([], 1, 13), b)])])])])])
	]
) :- tokenize_string("[(a;e),b|a;e,b].",Ts),program(Ts,P).
	
	
test(empty_list_as_an_argument) :- 
	tokenize_string("f([]).",Ts),
	program(Ts,[ct(pos([], 1, 0), f, [a(pos([], 1, 2), [])])]).		


test(normalization_of_lists) :- 
		tokenize_string("[a,b].",T1s),
		tokenize_string(".(a,.(b,[])).",T2s),
		program(T1s,[ct(_, '.', [a(_, a), ct(_, '.', [a(_, b), a(_, [])])])]),
		program(T2s,[ct(_, '.', [a(_, a), ct(_, '.', [a(_, b), a(_, [])])])]).


test(normalization_of_lists_with_tail) :- 
		tokenize_string("[a,b|T].",T1s),
		tokenize_string(".(a,.(b,T)).",T2s),
		program(T1s,[ct(_, '.', [a(_, a), ct(_, '.', [a(_, b), v(_, 'T')])])]),
		program(T2s,[ct(_, '.', [a(_, a), ct(_, '.', [a(_, b), v(_, 'T')])])]).
		
		
:- end_tests(parser_lists).