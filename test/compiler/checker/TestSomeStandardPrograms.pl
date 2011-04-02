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
	all tests in that file must succed

   @author Malte Viering
*/
:- ensure_loaded('src/prolog/compiler/Lexer.pl').
:- ensure_loaded('src/prolog/compiler/Parser.pl').
:- ensure_loaded('src/prolog/compiler/AST.pl').
:- ensure_loaded('src/prolog/compiler/Utils.pl').
:- ensure_loaded('src/prolog/compiler/phase/PLCheck.pl').
:- ensure_loaded('src/prolog/compiler/phase/PLLoad.pl').
:- use_module('PLCheck_utils.pl').
:- begin_tests(test_some_standard_programs).

test(ancestor) :- test_valid_prolog_sys_programs('ancestor').
test(arithmetic) :- test_valid_prolog_sys_programs('arithmetic').
test(zebra) :- test_valid_prolog_sys_programs('zebra').
test(einsteins_riddle) :- test_valid_prolog_sys_programs('einsteins_riddle').
test(is_prime) :- test_valid_prolog_sys_programs('is_prime').
%%@< is not supported
%test(poly) :- test_valid_prolog_sys_programs('poly').
test(qsort) :- test_valid_prolog_sys_programs('qsort').
test(queens) :- test_valid_prolog_sys_programs('queens').

:- end_tests(test_some_standard_programs).
