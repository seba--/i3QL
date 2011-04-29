/* License (BSD Style License):
   Copyright (c) 2011
   Department of Computer Science
   Technische Universit�t Darmstadt
   All rights reserved.

   Redistribution and use in source and binary forms, with or without
   modification, are permitted provided that the following conditions are met:

    - Redistributions of source code must retain the above copyright notice,
      this list of conditions and the following disclaimer.
    - Redistributions in binary form must reproduce the above copyright notice,
      this list of conditions and the following disclaimer in the documentation
      and/or other materials provided with the distribution.
    - Neither the name of the Software Technology Group or Technische 
      Universit�t Darmstadt nor the names of its contributors may be used to 
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
	Tests for PLCheck. 
	
   @author Malte Viering
*/
:- ensure_loaded('src/prolog/compiler/Lexer.pl').
:- ensure_loaded('src/prolog/compiler/Parser.pl').
:- ensure_loaded('src/prolog/compiler/AST.pl').
:- ensure_loaded('src/prolog/compiler/Utils.pl').
:- ensure_loaded('src/prolog/compiler/phase/PLCheck.pl').
:- ensure_loaded('src/prolog/compiler/phase/PLLoad.pl').
:- use_module('PLCheck_utils.pl').
:- begin_tests(valid_prolog_clauses).

%test_valid_prolog_clause doesnt work for that clause
%but test_valid_prolog_file works
%test('valid_prolog_clauses: not2') :- test_valid_prolog_clause('a(1). p(X) :- a(X). p2(X) :- \+(p(X)).').
test('valid_prolog_clauses: cut') :- test_valid_prolog_clause('p(X) :- !.').
test('valid_prolog_clauses: cut') :- test_valid_prolog_clause('a(1). p(X) :- !. p(X) :- a(X), p(X).').
test('valid_prolog_clauses: fact') :- test_valid_prolog_clause('a(1).').
test('valid_prolog_clauses: fact+rule') :- test_valid_prolog_clause('a(1). p(X) :- a(X).').
test('valid_prolog_clauses: fact+rule2') :- test_valid_prolog_clause('a(1). p(X) :- a(X). p2(X) :- p(X), a(_).').
test('valid_prolog_clauses: fact+rule+call') :- test_valid_prolog_clause('a(1). p(X) :- a(X). p2(X) :- call(p, A).').
test('valid_prolog_clauses: not') :- test_valid_prolog_clause('a(1). p(X) :- a(X). p2(X) :- not(p(X)).').


:- end_tests(valid_prolog_clauses).