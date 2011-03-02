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
	Tests the lexer. 
*/
:- ensure_loaded('src/prolog/compiler/Lexer.pl').


do_tokenization(SimpleFileName,Ts,Options) :-
	atomic_list_concat(['test/compiler/lexer/data/',SimpleFileName],File),
	tokenize_file(File,Ts,Options).


:- begin_tests(lexer).

test(	string_atoms,
		[true( 
			Ts = [
				a('IllegalCharacters:()[]{},;.:|',pos('test/compiler/lexer/data/StringAtoms.pl',1,0)),
				a('a',pos('test/compiler/lexer/data/StringAtoms.pl',2,0)),
				a('aaaaaaaaaaaaaaaaaaaaaaaaVVVVVVVVVVEEEEEEERRRRRRRRRYYYYYY_LLLLLLLLLLLLLLOOOOOOOOOOOOOOOOGGGGGGGGGGGGnnnnnnnnnnnaaaaaaaaaaaammmmmmmmmmmeeeeeeee',pos('test/compiler/lexer/data/StringAtoms.pl',3,0)),
				a(abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ,pos('test/compiler/lexer/data/StringAtoms.pl',4,0))
			]
		)]
	) :- do_tokenization('StringAtoms.pl',Ts,[]).


test(	empty_file, 
		[true(Ts=[])]
	) :- do_tokenization('Empty.pl',Ts,[]).


test(	numbers, 
		[true(Ts=[
			i(0, pos(_,1, 0)),
			i(1, pos(_,3, 0)),
			i(123456789, pos(_,5, 0)),
			r(1.2e+11, pos(_,7, 0)),
			r(1.2e+11, pos(_,9, 0)),
			r(1.2e-09, pos(_,11, 0)),
			r(12.34, pos(_,13, 0)),
			r(1234.0, pos(_,15, 0)),
			r(1234.0, pos(_,17, 0)),
			r(12.34e-2, pos(_,19, 0))
			]
		)]
	) :- do_tokenization('Numbers.pl',Ts,[]).


test(	ignore_comments_while_parsing,
		[true(Ts=[
			f(a, pos(_,3, 0)),
			'('(pos(_,3,1)),
			a(b, pos(_,3, 25)),
			')'(pos(_,3,26)),
			a('.', pos(_,3, 27))])
		]
	) :- do_tokenization('Comments.pl',Ts,[]).


test(	report_comments,
		[true(Ts=[
			eolc(' an end of line comment', pos(_,1, 0)), 
			f(a, pos(_,3, 0)),
			'('(pos(_,3,1)),
			mlc(' an inline comment ', pos(_,3, 2)),
			a(b, pos(_,3, 25)),
			')'(pos(_,3,26)),
			a('.', pos(_,3, 27)),
			mlc('\na multi-line comment\n', pos(_,5, 0))
			])
		]
	) :- do_tokenization('Comments.pl',Ts,[comments(retain_all)]).


test(	report_sc_comments,
		[true(Ts=[
			sc(['\n', '\t', 'A', ' ', s, t, r|_], 
				pos(_, 2, 0))
			])
		]
	) :- do_tokenization('StructuredComments.pl',Ts,[comments(retain_sc)]).


test(	operators,
		[true(Ts=[
			a(-->, pos(_, 1, 0)),
			a(:-, pos(_, 2, 0)),
			a(:-, pos(_, 3, 0)), 
			a(?-, pos(_, 4, 0)),
			a(;, pos(_, 5, 0)),
			a(,, pos(_, 6, 0)),
			a(|, pos(_, 7, 0)), 
			a(->, pos(_, 8, 0)), 
			a(\+, pos(_, 9, 0)), 
			a(~, pos(_, 10, 0)), 
			a(<, pos(_, 11, 0)), 
			a(=, pos(_, 12, 0)), 
			a(=.., pos(_, 13, 0)), 
			a(=@=, pos(_, 14, 0)), 
			a(=:=, pos(_, 15, 0)), 
			a(=<, pos(_, 16, 0)), 
			a(==, pos(_, 17, 0)), 
			a(=\=, pos(_, 18, 0)), 
			a(>, pos(_, 19, 0)), 
			a(>=, pos(_, 20, 0)), 
			a(@<, pos(_, 21, 0)), 
			a(@=<, pos(_, 22, 0)), 
			a(@>, pos(_, 23, 0)), 
			a(@>=, pos(_, 24, 0)), 
			a(\=, pos(_, 25, 0)), 
			a(\==, pos(_, 26, 0)), 
			a(is, pos(_, 27, 0)), 
			a(:, pos(_, 28, 0)),
			a(+, pos(_, 29, 0)),
			a(-, pos(_, 30, 0)), 
			a(/\, pos(_, 31, 0)), 
			a(\/, pos(_, 32, 0)), 
			a(xor, pos(_, 33, 0)), 
			a(><, pos(_, 34, 0)), 
			a(?, pos(_, 35, 0)), 
			a(*, pos(_, 36, 0)), 
			a(/, pos(_, 37, 0)), 
			a(//, pos(_, 38, 0)), 
			a(rdiv, pos(_, 39, 0)),
			a(<<, pos(_, 40, 0)), 
			a(>>, pos(_, 41, 0)), 
			a(mod, pos(_, 42, 0)), 
			a(rem, pos(_, 43, 0)),
			a(**, pos(_, 44, 0)), 
			a(^, pos(_, 45, 0)), 
			a(+, pos(_, 46, 0))])
		]
	) :- do_tokenization('Operators.pl',Ts,[]).


test(	operators_in_context,
		[true(Ts=[
			f(o, pos(_, 1, 0)), '('(pos(_, 1, 1)), a(-->, pos(_, 1, 2)), a(,, pos(_, 1, 5)), i(1, pos(_, 1, 7)), ')'(pos(_,_,_)), 
			f(o, pos(_, 2, 0)), '('(pos(_, 2, 1)), a(:-, pos(_, 2, 2)), a(,, pos(_, 2, 4)), i(2, pos(_, 2, 6)), ')'(pos(_,_,_)), 
			f(o, pos(_, 3, 0)), '('(pos(_, 3, 1)), a(:-, pos(_, 3, 2)), a(,, pos(_, 3, 4)), i(3, pos(_, 3, 6)), ')'(pos(_,_,_)), 
			f(o, pos(_, 4, 0)), '('(pos(_, 4, 1)), a(?-, pos(_, 4, 2)), a(,, pos(_, 4, 4)), i(4, pos(_, 4, 6)), ')'(pos(_,_,_)), 
			f(o, pos(_, 5, 0)), '('(pos(_, 5, 1)), a(;, pos(_, 5, 2)), a(,, pos(_, 5, 3)), i(5, pos(_, 5, 5)), ')'(pos(_,_,_)), 
			f(o, pos(_, 6, 0)), '('(pos(_, 6, 1)), a(,, pos(_, 6, 2)), a(,, pos(_, 6, 3)), i(6, pos(_, 6, 5)), ')'(pos(_,_,_)), 
			f(o, pos(_, 7, 0)), '('(pos(_, 7, 1)), a(|, pos(_, 7, 2)), a(,, pos(_, 7, 3)), i(7, pos(_, 7, 5)), ')'(pos(_,_,_)),
			f(o, pos(_, 8, 0)), '('(pos(_, 8, 1)), a(->, pos(_, 8, 2)), a(,, pos(_, 8, 4)), i(8, pos(_, 8, 6)), ')'(pos(_,_,_)), 
			f(o, pos(_, 9, 0)), '('(pos(_, 9, 1)), a(\+, pos(_, 9, 2)), a(,, pos(_, 9, 4)), i(9, pos(_, 9, 6)), ')'(pos(_,_,_)), 
			f(o, pos(_, 10, 0)), '('(pos(_, 10, 1)), a(~, pos(_, 10, 2)), a(,, pos(_, 10, 3)), i(10, pos(_, 10, 5)), ')'(pos(_,_,_)), 
			f(o, pos(_, 11, 0)), '('(pos(_, 11, 1)), a(<, pos(_, 11, 2)), a(,, pos(_, 11, 3)), i(11, pos(_, 11, 5)), ')'(pos(_,_,_)), 
			f(o, pos(_, 12, 0)), '('(pos(_, 12, 1)), a(=, pos(_, 12, 2)), a(,, pos(_, 12, 3)), i(12, pos(_, 12, 5)), ')'(pos(_,_,_)),
			f(o, pos(_, 13, 0)), '('(pos(_, 13, 1)), a(=.., pos(_, 13, 2)), a(,, pos(_, 13, 5)), i(13, pos(_, 13, 7)), ')'(pos(_,_,_)),
			f(o, pos(_, 14, 0)), '('(pos(_, 14, 1)), a(=@=, pos(_, 14, 2)), a(,, pos(_, 14, 5)), i(14, pos(_, 14, 7)), ')'(pos(_,_,_)),
			f(o, pos(_, 15, 0)), '('(pos(_, 15, 1)), a(=:=, pos(_, 15, 2)), a(,, pos(_, 15, 5)), i(15, pos(_, 15, 7)), ')'(pos(_,_,_)), 
			f(o, pos(_, 16, 0)), '('(pos(_, 16, 1)), a(=<, pos(_, 16, 2)), a(,, pos(_, 16, 4)), i(16, pos(_, 16, 6)), ')'(pos(_,_,_)),
			f(o, pos(_, 17, 0)), '('(pos(_, 17, 1)), a(==, pos(_, 17, 2)), a(,, pos(_, 17, 4)), i(17, pos(_, 17, 6)), ')'(pos(_,_,_)), 
			f(o, pos(_, 18, 0)), '('(pos(_, 18, 1)), a(=\=, pos(_, 18, 2)), a(,, pos(_, 18, 5)), i(18, pos(_, 18, 7)), ')'(pos(_,_,_)), 
			f(o, pos(_, 19, 0)), '('(pos(_, 19, 1)), a(>, pos(_, 19, 2)), a(,, pos(_, 19, 3)), i(19, pos(_, 19, 5)), ')'(pos(_,_,_)), 
			f(o, pos(_, 20, 0)), '('(pos(_, 20, 1)), a(>=, pos(_, 20, 2)), a(,, pos(_, 20, 4)), i(20, pos(_, 20, 6)), ')'(pos(_,_,_)), 
			f(o, pos(_, 21, 0)), '('(pos(_, 21, 1)), a(@<, pos(_, 21, 2)), a(,, pos(_, 21, 4)), i(21, pos(_, 21, 6)), ')'(pos(_,_,_)), 
			f(o, pos(_, 22, 0)), '('(pos(_, 22, 1)), a(@=<, pos(_, 22, 2)), a(,, pos(_, 22, 5)), i(22, pos(_, 22, 7)), ')'(pos(_,_,_)), 
			f(o, pos(_, 23, 0)), '('(pos(_, 23, 1)), a(@>, pos(_, 23, 2)), a(,, pos(_, 23, 4)), i(23, pos(_, 23, 6)), ')'(pos(_,_,_)), 
			f(o, pos(_, 24, 0)), '('(pos(_, 24, 1)), a(@>=, pos(_, 24, 2)), a(,, pos(_, 24, 5)), i(24, pos(_, 24, 7)), ')'(pos(_,_,_)), 
			f(o, pos(_, 25, 0)), '('(pos(_, 25, 1)), a(\=, pos(_, 25, 2)), a(,, pos(_, 25, 4)), i(25, pos(_, 25, 6)), ')'(pos(_,_,_)), 
			f(o, pos(_, 26, 0)), '('(pos(_, 26, 1)), a(\==, pos(_, 26, 2)), a(,, pos(_, 26, 5)), i(26, pos(_, 26, 7)), ')'(pos(_,_,_)),
			f(o, pos(_, 27, 0)), '('(pos(_, 27, 1)), a(:, pos(_, 27, 2)), a(,, pos(_, 27, 3)), i(28, pos(_, 27, 5)), ')'(pos(_,_,_)),
			f(o, pos(_, 28, 0)), '('(pos(_, 28, 1)), a(+, pos(_, 28, 2)), a(,, pos(_, 28, 3)), i(29, pos(_, 28, 5)), ')'(pos(_,_,_)), 
			f(o, pos(_, 29, 0)), '('(pos(_, 29, 1)), a(-, pos(_, 29, 2)), a(,, pos(_, 29, 3)), i(30, pos(_, 29, 5)), ')'(pos(_,_,_)),
			f(o, pos(_, 30, 0)), '('(pos(_, 30, 1)), a(/\, pos(_, 30, 2)), a(,, pos(_, 30, 4)), i(31, pos(_, 30, 6)), ')'(pos(_,_,_)), 
			f(o, pos(_, 31, 0)), '('(pos(_, 31, 1)), a(\/, pos(_, 31, 2)), a(,, pos(_, 31, 4)), i(32, pos(_, 31, 6)), ')'(pos(_,_,_)), 
			f(o, pos(_, 32, 0)), '('(pos(_, 32, 1)), a(><, pos(_, 32, 2)), a(,, pos(_, 32, 4)), i(34, pos(_, 32, 6)), ')'(pos(_,_,_)), 
			f(o, pos(_, 33, 0)), '('(pos(_, 33, 1)), a(?, pos(_, 33, 2)), a(,, pos(_, 33, 3)), i(35, pos(_, 33, 5)), ')'(pos(_,_,_)), 
			f(o, pos(_, 34, 0)), '('(pos(_, 34, 1)), a(*, pos(_, 34, 2)), a(,, pos(_, 34, 3)), i(36, pos(_, 34, 5)), ')'(pos(_,_,_)), 
			f(o, pos(_, 35, 0)), '('(pos(_, 35, 1)), a(/, pos(_, 35, 2)), a(,, pos(_, 35, 3)), i(37, pos(_, 35, 5)), ')'(pos(_,_,_)), 
			f(o, pos(_, 36, 0)), '('(pos(_, 36, 1)), a(//, pos(_, 36, 2)), a(,, pos(_, 36, 4)), i(38, pos(_, 36, 6)), ')'(pos(_,_,_)), 
			f(o, pos(_, 37, 0)), '('(pos(_, 37, 1)), a(<<, pos(_, 37, 2)), a(,, pos(_, 37, 4)), i(40, pos(_, 37, 6)), ')'(pos(_,_,_)), 
			f(o, pos(_, 38, 0)), '('(pos(_, 38, 1)), a(>>, pos(_, 38, 2)), a(,, pos(_, 38, 4)), i(41, pos(_, 38, 6)), ')'(pos(_,_,_)), 
			f(o, pos(_, 39, 0)), '('(pos(_, 39, 1)), a(**, pos(_, 39, 2)), a(,, pos(_, 39, 4)), i(44, pos(_, 39, 6)), ')'(pos(_,_,_)), 
			f(o, pos(_, 40, 0)), '('(pos(_, 40, 1)), a(^, pos(_, 40, 2)), a(,, pos(_, 40, 3)), i(45, pos(_, 40, 5)), ')'(pos(_,_,_)), 
			f(o, pos(_, 41, 0)), '('(pos(_, 41, 1)), a(+, pos(_, 41, 2)), a(,, pos(_, 41, 3)), i(46, pos(_, 41, 5)), ')'(pos(_,_,_))
			])
		]
	) :- do_tokenization('OperatorsInContext.pl',Ts,[]).


test(rg_test_Example_pl) :- do_tokenization('Example.pl',_Ts,[]).


test(rg_test_Test_pl) :- tokenize_file('test/compiler/lexer/Test.pl',_Ts).


:- end_tests(lexer).