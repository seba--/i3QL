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
	Definition of predicates that help to understand and debug the inner 
	workings of the lexer.
	
	@author Michael Eichberg
*/
:- module(
		'SAEProlog:Compiler:Debug:Lexer',
		[
			tokenize_string/2,
			echo_file/1,
			write_token/1,
			write_escaped_atoms/1,
			write_sc_token/1
		]
	).



:- use_module('../Lexer.pl').
:- use_module(library(charsio)). 






/**
	Tokenizes a given string (S) and returns the list of tokens (Ts).
	<p>
	<b>Example</b>
	<pre>
	?- tokenize_string("a(b,C is b + 1)",Ts),write(Ts).
	[t(a, 1, 0), ((1, 1), t(b, 1, 2), t(,, 1, 3), v(C, 1, 4), t(is, 1, 6), t(b, 1, 9), t(+, 1, 11), i(1, 1, 13), )(1, 14)]
	
	</pre>
	</p>
*/
tokenize_string(S,Ts) :- 
	string_to_list(S,Cs),
	open_chars_stream(Cs,Stream),
	tokenize(Stream,Ts),
	close(Stream).



echo_file(File) :-
   tokenize_file(File,Tokens,[comments(retain_all)]),
   member(Token,Tokens),	
      write_token(Token),
   fail.
echo_file(_).


/* Using the following two predicates may lead to false conclusions. 
	The given term is already parsed by (SWI)Prolog and normalized
	before the term is actually passed to the lexer...
	
tokenize_term(T,Ts) :- 
	write_to_chars(T,Cs),
	open_chars_stream(Cs,S),
	tokenize(S,Ts),
	close(S).

echo_term(Term) :-
	tokenize_term(Term,Ts),
	echo(Ts).
*/



write_token(a(T,_)) :- !,
	write_term(T,[quoted(true),character_escapes(true),max_depth(0)]).
write_token(f(T,_)) :- !,
	write_term(T,[quoted(true),character_escapes(true),max_depth(0)]).
write_token(i(I,_)) :- !, write(I).
write_token(r(F,_)) :- !, write(F).
write_token(v(V,_)) :- !, write(V).
write_token(av(AV,_)) :- !, write(AV).
write_token('('(_)) :- !, write('(').
write_token(')'(_)) :- !, write(')').
write_token('{'(_)) :- !, write('{').
write_token('}'(_)) :- !, write('}').
write_token('['(_)) :- !, write('[').
write_token(']'(_)) :- !, write(']').
write_token(chars(S,_)) :- !, 
	write('"'),
	write_escaped_atoms(S),
	write('"').

write_token(eolc(C,_)) :- write('%'),write(C),!.
write_token(mlc(C,_)) :- write('/*'),write(C),write('*/'),!.
write_token(sc(SC,_)) :- % TODO Should we fail of reconstruct the structured comment? 
	!,
	write('/**'),
	write(SC),
	write('*/').
	
write_token(T):- write('ERROR: UNKNOWN TOKEN: '),write(T),nl.



write_escaped_atoms([Atom|Atoms]) :-
	(	Atom = '\n',!,write('\\n')
	;	Atom = '\t',!,write('\\t')
	;	Atom = '\\',!,write('\\\\')
	;	write(Atom)
	),
	write_escaped_atoms(Atoms).
write_escaped_atoms([]).



write_sc_token(tf(TF,_)) :- write(TF),!.
write_sc_token(SC) :- SC =..[F|_],write(F).
