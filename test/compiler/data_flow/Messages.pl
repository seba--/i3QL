/* License (BSD Style License):
   Copyright (c) 2011
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
	Tests related to the compiler's data flow analysis. This test suite is
	intended to check the compiler generated warnings and errors.

   @author Michael Eichberg (mail@michael-eichberg.de)
*/
:- ensure_loaded('src/prolog/compiler/Lexer.pl').
:- ensure_loaded('src/prolog/compiler/Parser.pl').
:- ensure_loaded('src/prolog/compiler/AST.pl').
:- ensure_loaded('src/prolog/compiler/Utils.pl').
:- ensure_loaded('src/prolog/compiler/phase/PLAnalyzeVariables.pl').


:- begin_tests(data_flow_analyses).

test(variable_used_multiple_times_by_one_term) :-
	assert_standard_output(
		(	% the clause has to be normalized!
			tokenize_string("do(A,X) :- A = X,true.",Ts),
			clauses(Ts,[Clause]),
			analyze_variable_usage(_Meta,Clause,_CLVsCount) 
		),
		"" %nothing to warn about
	).
	
test(variable_used_multiple_times_by_one_term_in_body) :-
	assert_standard_output(
		(	
			tokenize_string("do :- b(A,_B,_C,A). ",Ts),
			clauses(Ts,[Clause]),
			analyze_variable_usage(_Meta,Clause,_CLVsCount)
		),
		"" %nothing to warn about
	).	

test(all_variables_are_used) :-
	assert_standard_output(
		(	
			tokenize_string("do(A,B) :- call(A) ; call(B). ",Ts),
			clauses(Ts,[Clause]),
			analyze_variable_usage(_Meta,Clause,_CLVsCount)
		),
		"" %nothing to warn about
	).

test(one_body_variable_is_used_only_once_1) :-
	assert_standard_output(
		(	
			tokenize_string("do(A,B) :- (call(A) ; call(B)),call(C). ",Ts),
			clauses(Ts,[Clause]),
			analyze_variable_usage(_Meta,Clause,_CLVsCount)
		),
		"Warning: the variable C is only used once (per possible path)\n"
	).

test(one_body_variable_is_used_only_once_2) :-
	assert_standard_output(
		(	
			tokenize_string("do(A,B) :- call(C), (call(A) ; call(B)). ",Ts),
			clauses(Ts,[Clause]),
			analyze_variable_usage(_Meta,Clause,_CLVsCount)
		),
		"Warning: the variable C is only used once (per possible path)\n"
	).

test(one_head_variable_is_used_only_once) :-
	assert_standard_output(
		(	
			tokenize_string("do(A,B,C) :- call(A) ; call(B). ",Ts),
			clauses(Ts,[Clause]),
			analyze_variable_usage(_Meta,Clause,_CLVsCount)
		),
		"Warning: the variable C is only used once (per possible path)\n"
	).

test(all_variables_are_used_only_once) :-
	assert_standard_output(
		(	
			tokenize_string("do :- call(A) ; call(B). ",Ts),
			clauses(Ts,[Clause]),
			analyze_variable_usage(_Meta,Clause,_CLVsCount)
		),
		"Warning: the variable B is only used once (per possible path)\nWarning: the variable A is only used once (per possible path)\n"
	).

			
:- end_tests(data_flow_analyses).