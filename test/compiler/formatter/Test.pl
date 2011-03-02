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
	Tests the formatter. 
	
	Autor Dennis Siebert
	Datum: 08.11.2010
*/

:- ensure_loaded('src/prolog/compiler/Lexer.pl').
:- ensure_loaded('src/prolog/compiler/Parser.pl').
:- ensure_loaded('src/prolog/compiler/Formatter.pl').


do_formatting(FileName,Solution,Options) :-
   atomic_list_concat(['test/compiler/formatter/data/',FileName],File),
   atomic_list_concat(['test/compiler/formatter/data/',Solution],FileSolution),

   tokenize_file(File,Ts,Options), clauses(Ts,Cs), format_clauses(Cs,Formatted),
   nl,write('###FORMATTED###\n'),write(Formatted),
   load_solution(FileSolution,Formatted).

load_solution(Solution,Lines) :-
   open(Solution,read,Stream),
   readLines(Stream,Out),
   close(Stream),
   atomic_list_concat(Out,Lines).
   %nl,write('###SOLUTION###\n'),write(Lines).



readLines(Stream,[X|T]):-
   \+ at_end_of_stream(Stream),!,
   get_char(Stream,X),
   readLines(Stream,T).
readLines(_Stream,[]).


:- begin_tests(formatter).

test(emptyFormatter) :- do_formatting('EmptyTest.pl','EmptyTest_formatted.pl',[]),!.

test(whitespaces_surrounding_operators) :- do_formatting('OperatorsWhitespace.pl','OperatorsWhitespace_formatted.pl',[]),!.

test(whitespace_in_compound_terms) :- do_formatting('Whitespace_in_compound_terms.pl','Whitespace_in_compound_terms_formatted.pl',[]),!.

test(lists) :- do_formatting('Lists.pl','Lists_formatted.pl',[]),!.

test(whitespace_in_lists) :- do_formatting('Whitespace_in_lists.pl','Whitespace_in_lists_formatted.pl',[]),!.

test(comma_as_functor) :- do_formatting('Comma_as_functor.pl','Comma_as_functor_formatted.pl',[]),!.

%test(comments) :- do_formatting('Comments.pl','Comments_formatted.pl',[]),!.

test('no_empty_line_between_same_clauses\\arity') :- do_formatting('NoEmptyLines.pl','NoEmptyLines_formatted.pl',[]),!.

test('lines between different arity') :- do_formatting('LineBetweenSameClauses.pl','LineBetweenSameClauses_formatted.pl',[]),!.

test('lines between different clauses') :- do_formatting('TwoLineBetweenDiffClauses.pl','TwoLineBetweenDiffClauses_formatted.pl',[]),!.

test(correct_functor_brackets) :- do_formatting('OperatorBrackets.pl','OperatorBrackets_formatted.pl',[]),!.

test(if_then_else) :- do_formatting('If_then_else.pl','If_then_else_formatted.pl',[]),!.

:- end_tests(formatter).