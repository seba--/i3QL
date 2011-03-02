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
:- ensure_loaded('src/prolog/compiler/Lexer.pl').
:- ensure_loaded('src/prolog/compiler/Parser.pl').


:- begin_tests(parser_lists).


test(
	empty_list,
	[	true(P=[a([pos(_,1,0)|_],[])])	]
) :- tokenize_string("[].",Ts),clauses(Ts,P).


test(
	list_with_one_element,
	[	true(P=[ct(_,'.',[av([pos(_,1,1)|_],'_'),a(_,[])])]) ]
) :- tokenize_string("[_].",Ts),clauses(Ts,P).


test(
	list_with_multiple_elements,
	[	true(P=[ct([pos([], 1, 0)|_], ., [a([pos([], 1, 1)|_], a), ct([pos([], 1, 2)|_], ., [a([pos([], 1, 3)|_], b), ct([pos([], 1, 4)|_], ., [a([pos([], 1, 5)|_], c), ct([pos([], 1, 6)|_], ., [ct([pos([], 1, 9)|_], ;, [a([pos([], 1, 8)|_], d), a([pos([], 1, 10)|_], e)]), a([pos([], 1, 12)|_], [])])])])])])]
) :- tokenize_string("[a,b,c,(d;e)].",Ts),clauses(Ts,P).


test(
	list_with_a_open_tail,
	[	true(P = [ct([pos([], 1, 0)|_], '.', [ct([pos([], 1, 3)|_], ;, [a([pos([], 1, 2)|_], a), a([pos([], 1, 4)|_], e)]), v([pos([], 1, 7)|_], 'T')])])]
) :- tokenize_string("[(a;e)|T].",Ts),clauses(Ts,P).


test(
	list_with_a_compound_term_as_a_tail,
	[	
		true(P=[
			ct([pos([], 1, 0)|_], ., [
				ct([pos([], 1, 3)|_], ;, [a([pos([], 1, 2)|_], a), a([pos([], 1, 4)|_], e)]),
				ct([pos([], 1, 6)|_], ., [
					a([pos([], 1, 7)|_], b), 
					ct([pos([], 1, 10)|_], ;, [
						a([pos([], 1, 9)|_], a), 
						ct([pos([], 1, 12)|_], ,, [
							a([pos([], 1, 11)|_], e), 
							a([pos([], 1, 13)|_], b)])])])])])
	]
) :- tokenize_string("[(a;e),b|a;e,b].",Ts),clauses(Ts,P).
	
	
test(empty_list_as_an_argument) :- 
	tokenize_string("f([]).",Ts),
	clauses(Ts,[ct([pos([], 1, 0)|_], f, [a([pos([], 1, 2)|_], [])])]).		


test(normalization_of_lists) :- 
		tokenize_string("[a,b].",T1s),
		tokenize_string(".(a,.(b,[])).",T2s),
		clauses(T1s,[ct(_, '.', [a(_, a), ct(_, '.', [a(_, b), a(_, [])])])]),
		clauses(T2s,[ct(_, '.', [a(_, a), ct(_, '.', [a(_, b), a(_, [])])])]).


test(normalization_of_lists_with_tail) :- 
		tokenize_string("[a,b|T].",T1s),
		tokenize_string(".(a,.(b,T)).",T2s),
		clauses(T1s,[ct(_, '.', [a(_, a), ct(_, '.', [a(_, b), v(_, 'T')])])]),
		clauses(T2s,[ct(_, '.', [a(_, a), ct(_, '.', [a(_, b), v(_, 'T')])])]).
		
		
:- end_tests(parser_lists).