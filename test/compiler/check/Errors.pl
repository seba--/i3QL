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

test('predicate') :- test_not_valid_prolog_clause('p(X) :- a(X).', "\n[]:1:8 : error: Lookup of the predicate failed (Termtype: compound_term Termname: a)").
test('predicate2') :- test_not_valid_prolog_clause('b(1). p(X) :- a(X).', "\n[]:1:14 : error: Lookup of the predicate failed (Termtype: compound_term Termname: a)").
test('predicate2') :- test_not_valid_prolog_clause('b(1). a(2). p(X) :- a(X) , b(X) , c(X).', "\n[]:1:34 : error: Lookup of the predicate failed (Termtype: compound_term Termname: c)").
test('Var: _') :- test_not_valid_prolog_clause('b(1). a(2). p(X) :- a(X) , b(X) , c(X), _.', "\n[]:1:34 : error: Lookup of the predicate failed (Termtype: compound_term Termname: c)\n[]:1:40 : error: an anonymous variable is no valid predicate (Termtype: term_anonymous_variable Termname: _)").
test('control flow1') :- test_not_valid_prolog_clause('b(1). a(2). p(X) :- a(X) -> b(X) ; c(X).', "\n[]:1:35 : error: Lookup of the predicate failed (Termtype: compound_term Termname: c)").
test('control flow2') :- test_not_valid_prolog_clause('b(1). a(2). p(X) :- a(X) *-> b(X) ; c(X).',"\n[]:1:36 : error: Lookup of the predicate failed (Termtype: compound_term Termname: c)").
test('control flow3') :- test_not_valid_prolog_clause('b(1). a(2). p(X) :- a(X) *-> c(X).',"\n[]:1:29 : error: Lookup of the predicate failed (Termtype: compound_term Termname: c)").
test('control flow4') :- test_not_valid_prolog_clause('b(1). a(2). p(X) :- a(X) -> c(X).',"\n[]:1:28 : error: Lookup of the predicate failed (Termtype: compound_term Termname: c)").
test('control flow5') :- test_not_valid_prolog_clause('b(1). a(2). p(X) :- a(X) ; b(X) ; c(X).',"\n[]:1:34 : error: Lookup of the predicate failed (Termtype: compound_term Termname: c)").
test('findall1') :- test_not_valid_prolog_clause('p(X) :- findall(X, p1, Xs).',"\n[]:1:19 : error: Unknown term (Termtype: term_atom Termname: p1)").
test('findall2') :- test_not_valid_prolog_clause('p1(X):- a(x). p(X) :- findall(X, p1(X), Xs).',"\n[]:1:8 : error: Lookup of the predicate failed (Termtype: compound_term Termname: a)").
test('findall3') :- test_not_valid_prolog_clause('p1(X). p(X) :- findall(X, p1, Xs).',"\n[]:1:26 : error: Unknown term (Termtype: term_atom Termname: p1)").
test('findall4') :- test_not_valid_prolog_clause('p(X) :- findall(X, findall(X1, p1(X), X1s), Xs).',"\n[]:1:31 : error: Lookup of the predicate failed (Termtype: compound_term Termname: p1)").
test('findall5') :- test_not_valid_prolog_clause('p(X) :- findall(X, findall(X1, p, X1s), Xs).',"\n[]:1:31 : error: Unknown term (Termtype: term_atom Termname: p)").
test('call') :- test_not_valid_prolog_clause('p(X) :- call(p2,X).',"\n[]:1:13 : error: Lookup of callable sub term failed (Termtype: term_atom Termname: p2)").
test('call2') :- test_not_valid_prolog_clause('p1(X). p(X) :- call(p1,X,Y).',"\n[]:1:20 : error: Lookup of callable sub term failed (Termtype: term_atom Termname: p1)").
test('call3') :- test_not_valid_prolog_clause('a(1). p1(X) :- a(X). p(X) :- call(p1).',"\n[]:1:34 : error: Lookup of callable sub term failed (Termtype: term_atom Termname: p1)").
test('_') :- test_not_valid_prolog_clause('p(X) :- _.',"\n[]:1:8 : error: an anonymous variable is no valid predicate (Termtype: term_anonymous_variable Termname: _)").
test('missing predicate') :- test_not_valid_prolog_clause('p(X) :- true, p3(_).',"\n[]:1:14 : error: Lookup of the predicate failed (Termtype: compound_term Termname: p3)").
test('not1') :- test_not_valid_prolog_clause('p(X) :- not(p2(X)).',"\n[]:1:12 : error: Lookup of the predicate failed (Termtype: compound_term Termname: p2)").

:- end_tests(errors).

