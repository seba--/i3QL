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
	The following tests just load a large number of different prolog files
	to make sure that the parser does not crash for a wide variety of Prolog 
	programs.

   @author Michael Eichberg (mail@michael-eichberg.de)
*/
:- ensure_loaded('src/prolog/compiler/Lexer.pl').
:- ensure_loaded('src/prolog/compiler/Parser.pl').
:- ensure_loaded('src/prolog/compiler/Utils.pl').


:- begin_tests(parser_overall).

% Let's test if we can load / parse some standard Prolog programs

test('test/system/programs/ancestor.pl') :-
	tokenize_file('test/system/programs/ancestor.pl',Ts),clauses(Ts,_P).


test('test/system/programs/arithmetic.pl') :-
	tokenize_file('test/system/programs/arithmetic.pl',Ts),clauses(Ts,_P).


test('test/system/programs/boyer.pl') :-
	tokenize_file('test/system/programs/boyer.pl',Ts),clauses(Ts,_P).


test('test/system/programs/chat_parser.pl') :-
	tokenize_file('test/system/programs/chat_parser.pl',Ts),clauses(Ts,_P).


test('test/system/programs/crypt.pl') :-
	tokenize_file('test/system/programs/crypt.pl',Ts),clauses(Ts,_P).


test('test/system/programs/deriv.pl') :-
	tokenize_file('test/system/programs/deriv.pl',Ts),clauses(Ts,_P).


test('test/system/programs/meta_nrev.pl') :-
	tokenize_file('test/system/programs/meta_nrev.pl',Ts),clauses(Ts,_P).


test('test/system/programs/mu.pl') :-
	tokenize_file('test/system/programs/mu.pl',Ts),clauses(Ts,_P).


test('test/system/programs/nrev.pl') :-
	tokenize_file('test/system/programs/nrev.pl',Ts),clauses(Ts,_P).


test('test/system/programs/poly.pl') :-
	tokenize_file('test/system/programs/poly.pl',Ts),clauses(Ts,_P).


test('test/system/programs/primes.pl') :-
	tokenize_file('test/system/programs/primes.pl',Ts),clauses(Ts,_P).


test('test/system/programs/qsort.pl') :-
	tokenize_file('test/system/programs/qsort.pl',Ts),clauses(Ts,_P).


test('test/system/programs/queens.pl') :-
	tokenize_file('test/system/programs/queens.pl',Ts),clauses(Ts,_P).


test('test/system/programs/reducer.pl') :-
	tokenize_file('test/system/programs/reducer.pl',Ts),clauses(Ts,_P).


test('test/system/programs/sum.pl') :-
	tokenize_file('test/system/programs/sum.pl',Ts),clauses(Ts,_P).


test('test/system/programs/tak.pl') :-
	tokenize_file('test/system/programs/tak.pl',Ts),clauses(Ts,_P).


test('test/system/programs/zebra.pl') :-
	tokenize_file('test/system/programs/zebra.pl',Ts),clauses(Ts,_P).


test('test/compiler/parser/data/NumberAtEnd.pl') :-
	tokenize_file('test/compiler/parser/data/NumberAtEnd.pl',Ts),clauses(Ts,_).

% Let's test if we can parse our own code

test('src/prolog/compiler/Lexer.pl',[setup(redirect_stdout_to_null(S)),cleanup(reset_stdout_redirect(S))]) :-
	tokenize_file('src/prolog/compiler/Lexer.pl',Ts),clauses(Ts,_P).

test('src/prolog/compiler/Parser.pl',[setup(redirect_stdout_to_null(S)),cleanup(reset_stdout_redirect(S))]) :-
	tokenize_file('src/prolog/compiler/Parser.pl',Ts),clauses(Ts,_P).


test('src/prolog/compiler/AST.pl',[setup(redirect_stdout_to_null(S)),cleanup(reset_stdout_redirect(S))]) :-
	tokenize_file('src/prolog/compiler/AST.pl',Ts),clauses(Ts,_P).


test('src/prolog/compiler/Utils.pl',[setup(redirect_stdout_to_null(S)),cleanup(reset_stdout_redirect(S))]) :-
	tokenize_file('src/prolog/compiler/Utils.pl',Ts),clauses(Ts,_P).


test('src/prolog/compiler/Predef.pl',[setup(redirect_stdout_to_null(S)),cleanup(reset_stdout_redirect(S))]) :-
	tokenize_file('src/prolog/compiler/Predef.pl',Ts),clauses(Ts,_P).
		
		
% Let's test if we can load a (very) large fact database

test('test/compiler/parser/data/Flashcards0.6.pl',[setup(redirect_stdout_to_null(S)),cleanup(reset_stdout_redirect(S))]) :-
	tokenize_file('test/compiler/parser/data/Flashcards0.6.pl',Ts),clauses(Ts,_P).
		
:- end_tests(parser_overall).