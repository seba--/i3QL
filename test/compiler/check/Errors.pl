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
:- begin_tests(errors).

test('predicate') :- test_not_valid_prolog_clause('p(X) :- a(X).').
test('predicate2') :- test_not_valid_prolog_clause('b(1). p(X) :- a(X).').
test('predicate2') :- test_not_valid_prolog_clause('b(1). a(2). p(X) :- a(X) , b(X) , c(X).').
test('Var: _') :- test_not_valid_prolog_clause('b(1). a(2). p(X) :- a(X) , b(X) , c(X), _').
test('control flow1') :- test_not_valid_prolog_clause('b(1). a(2). p(X) :- a(X) -> b(X) ; c(X).').
test('control flow2') :- test_not_valid_prolog_clause('b(1). a(2). p(X) :- a(X) *-> b(X) ; c(X).').
test('control flow3') :- test_not_valid_prolog_clause('b(1). a(2). p(X) :- a(X) *-> c(X).').
test('control flow4') :- test_not_valid_prolog_clause('b(1). a(2). p(X) :- a(X) -> c(X).').
test('control flow5') :- test_not_valid_prolog_clause('b(1). a(2). p(X) :- a(X) ; b(X) ; c(X).').
test('findall1') :- test_not_valid_prolog_clause('p(X) :- findall(X, p1, Xs).').
test('findall2') :- test_not_valid_prolog_clause('p1(X):- a(x). p(X) :- findall(X, p1(X), Xs).').
test('findall3') :- test_not_valid_prolog_clause('p1(X). p(X) :- findall(X, p1, Xs).').
test('findall4') :- test_not_valid_prolog_clause('p(X) :- findall(X, findall(X1, p1(X), X1s), Xs).').
test('findall5') :- test_not_valid_prolog_clause('p(X) :- findall(X, findall(X1, p, X1s), Xs).').
test('call') :- test_not_valid_prolog_clause('p(X) :- call(p2,X).').
test('call2') :- test_not_valid_prolog_clause('p1(X). p(X) :- call(p1,X,Y).').
test('call3') :- test_not_valid_prolog_clause('a(1). p1(X) :- a(X). p(X) :- call(p1).').
test('_') :- test_not_valid_prolog_clause('p(X) :- _.').
test('missing predicate') :- test_not_valid_prolog_clause('p(X) :- true, p3(_).').
test('not1') :- test_not_valid_prolog_clause('p(X) :- not(p2(X)).').
test('not2') :- test_not_valid_prolog_clause('p(X) :- \+(p2(X)).').

:- end_tests(errors).

