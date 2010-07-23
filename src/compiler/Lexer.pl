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
	A lexer to tokenize standard (ISO/SAE) Prolog files.
	<p>
	The result of tokenizing a file is a list of tokens representing all
	parts of the code (including comments).<br/>
	The types of tokens recognized by this lexer is shown in the following. 
	<code>LN</code> and <code>CN</code> always identify the line and
	column where the token was found.
	<ul>
	<li>s(S,LN,CN) :- S is a list of char atoms; i.e., it is a "string".
	<li>sa(S,LN,CN) :- S is a string atom; recall that <i> the cut ("!") is also an
	 	atom.</i></li>
	<li>i(N,LN,CN) :- N is an integer value.</li>		
	<li>f(N,LN,CN) :- N is a floating point value.</li>		
	<li>o(Op,LN,CN) :- Op is a sequence of operator characters (e.g., ':-').</li>
	<li>o(Op,LN,CN) :- Op is an operator that cannot appear as part of an operator
	 	sequence (';',',','|').</li>
	<li>av(V,LN,CN) :- V is the name of an anonymous variable (av).</li>
	<li>v(V,LN,CN) :- V is the name of a concrete variable (v).</li>
	<li>&lt;Special Characters&gt; a paranthesis (e.g., one of "(,),{,},[,]"), 
		special characters are included in the token list as is.</li>		
	</ul>
	The following tokens are only relevant for a pretty printer / java doc
	generator. The lexer has a parameter to specify whether white space
	information should be generated or not.
	<ul>	
	<li>sc(Tokens,LN,CN) :- Tokens of a structured comment (/ * * .. * /).</li>
	<li>eolc(Token,LN,CN) :- Token is an end-of-line comment (% ...).</li>
	<li>mlc(Token,LN,CN) :- Token is multi-line / inline comment (using / * .. * /)</li>	
	<li>ws(Token,LN,CN) :- Token is a white space character</li>			
	</ul>
	</p>
	<p>
	<b>Example</b>
	Given a file that starts with:<br/>
	<code><br />
	:- module('Lexer',[tokenize_file/2]).<br />
	<code><br />
	then the result of running this lexer is:
	<pre>
		tokenize_file('Lexer.pl',false,Ts)
		Ts = [
			o(:-, 32, 1),
			sa(module, 32, 4),
			'(', 
			sa('Lexer', 32, 11), 
			o(',', 32, 18),
			'[', 
			sa(tokenize_file, 32, 20),
			o(/, 32, 33),
			n(..., ..., ...)|...].
	</pre>
	</p>	
	<p>
	<b>Implementation Note</b>
	This is a hand written lexer that delivers sufficient performance for
	lexing real world (ISO) Prolog programs. The lexer provides information
	about a token's position to enable subsequent phases to give precise error
	messages. If the lexer does not recognize a special character / symbol it 
	prints an error or warning message, but otherwise just ignores the character
	and continues with the next character.
	</p>
	
	@author Michael Eichberg (mail@michael-eichberg.de)
	@version 0.9 - July, 23th 2010 (The lexer works, but is not yet fully tested.)
*/
:- module(
	'SAEProlog:Lexer',
	[
		tokenize_file/3,
		tokenize/3,
		white_space/1,
		name_atom/1,
		variable/1
	]
).




/**
	Tokenizes a file.
*/
tokenize_file(File,KeepWhiteSpace,Tokens) :-
	open(File,read,Stream),
	tokenize(Stream,KeepWhiteSpace,Tokens),
	close(Stream).



/**
	Tokenizes a stream of characters.
	
	@signature tokenize(Stream,Tokens)
	@arg(in) Stream a stream that supports reading characters
	@arg(in) KeepWhiteSpace if <code>true</code> Tokens will include white space
		information
	@arg(out) Tokens the list of recognized tokens
*/
tokenize(Stream,_KeepWhiteSpace,[]) :- at_end_of_stream(Stream),!.
tokenize(Stream,KeepWhiteSpace,Tokens) :-
% TODO Just peek..
	get_char(Stream,C),
	read_token(C,Stream,Token),
	(	% if...
		( Token = none ; \+ KeepWhiteSpace,	white_space(Token) ), !,
		Tokens = Ts
	;	% else...
% TODO add line number information here except for atoms or white_space
		Tokens = [Token|Ts]
	),
	tokenize(Stream,KeepWhiteSpace,Ts).




white_space(sc(_,_,_)).
white_space(eolc(_,_,_)).	
white_space(mlc(_,_,_)).
white_space(ws(_)).		



name_atom(o(_,_,_)).
name_atom(sa(_,_,_)).



variable(v(_,_,_)).
variable(av(_,_,_)).



/**
	Reads in a token of a specific type.</br >
	Based on the previously read character Char and at most one further character 
	(using peek_char), the type of the token is determined and reading the rest 
	of the token's chars is delegated to the corresponding "read_*" predicates.
	
	@signature read_token(Char,Stream,Token)
	@param Char the last read token. This character is used to identify the 
		type of the current token and determines how the following characters
		are interpreted.
	@param Token the read token. An atom in case of a special character (e.g., all 
		forms of parantheses); 'none' if an 
		errror is detected; a compound term describing the token and its position
		in all other cases.)
*/
read_token('%',Stream,eolc(Token,LN,CN)) :- 
	!,
	stream_position(Stream,LN,CN),
	read_eol_comment(Stream,Cs),
	atom_chars(Token,Cs).

read_token('!',Stream,sa('!',LN,CN)) :- !, stream_position(Stream,LN,CN).

% "and (,)", "or (;)", etc. are special operators because they cannot
% contribute to an operator name (e.g. ':-')
read_token(',',Stream,o(',',LN,CN)) :- !, stream_position(Stream,LN,CN).	
read_token(';',Stream,o(';',LN,CN)) :- !, stream_position(Stream,LN,CN).	
read_token('|',Stream,o('|',LN,CN)) :- !, stream_position(Stream,LN,CN).	

% parantheses are "returned" as is 
read_token('(',_Stream,'(') :- !.	
read_token(')',_Stream,')') :- !.	
read_token('{',_Stream,'{') :- !.	
read_token('}',_Stream,'}') :- !.	
read_token('[',_Stream,'[') :- !.	
read_token(']',_Stream,']') :- !.	

% an anonymous variable
read_token('_',Stream,av(AID,LN,CN)) :- 
	!,
	stream_position(Stream,LN,CN),
	read_identifier(Stream,Cs),
	ID = ['_'|Cs],
	atom_chars(AID,ID).

% a quoted atom
read_token('\'',Stream,sa(AID,LN,CN)) :- 
	!,
	stream_position(Stream,LN,CN),
	read_string_with_quotations(Stream,'\'',Cs),
	atom_chars(AID,Cs).
	
% a string	
read_token('"',Stream,s(S,LN,CN)) :- 
	!,
	stream_position(Stream,LN,CN),
	read_string_with_quotations(Stream,'"',S).

% white space
read_token(W,_Stream,ws(W)) :- char_type(W,space), !.

% a string atom
read_token(C,Stream,sa(AID,LN,CN)) :- 
	char_type(C,lower),
	!,
	stream_position(Stream,LN,CN),
	read_identifier(Stream,Cs),
	ID = [C|Cs],
	atom_chars(AID,ID).

% a variable name
read_token(C,Stream,v(AID,LN,CN)) :- 
	char_type(C,upper),
	!,
	stream_position(Stream,LN,CN),
	read_identifier(Stream,Cs),
	ID = [C|Cs],
	atom_chars(AID,ID).	

% a number
read_token(I,Stream,n(N,LN,CN)) :-
	char_type(I,digit),
	!,
	stream_position(Stream,LN,CN),
	read_number(Stream,Is),
	NCs = [I|Is],
	number_chars(N,NCs).

% a multi-line comment; it is either a structured or an unstructured comment
read_token('/',Stream,Token) :- 
	peek_char(Stream,'*'),
	!, 
	get_char(Stream,_), % /*/ is not considered to be a valid comment...
	read_ml_comment(Stream,Token).

% a sequence of operator characters 
% (This clause has to come AFTER handling of multi-line comments!)
read_token(Op,Stream,o(AOPs,LN,CN)) :- 
	operator_char(Op),
	!,
	stream_position(Stream,LN,CN),
	read_operators(Stream,OPs),
	O = [Op|OPs],
	atom_chars(AOPs,O).
	
% fallback case to enable the lexer to continue if an error is found...
read_token(C,Stream,none) :- lexer_error(Stream,['unrecognized symbol (',C,')']).




% an identifier is a sequence of upper or lower case letters, digits and "_".
read_identifier(Stream,[]) :- at_end_of_stream(Stream),!.
read_identifier(Stream,[C|Cs]) :-
	peek_char(Stream,C),
	( 	% if...
		char_type(C,alnum) 
	;	% or...
		C = '_' 
	),
	!, % then ...
	get_char(Stream,_),
	read_identifier(Stream,Cs).	
read_identifier(_Stream,[]).	



read_string_with_quotations(Stream,Delimiter,[]) :- 
	at_end_of_stream(Stream,['missing delimiter: ',Delimiter]),
	!.
read_string_with_quotations(Stream,Delimiter,R) :-
	get_char(Stream,C),!,
	(	% C is the delimiter...
		C = Delimiter,!,
		R = []
	;	% C is the start of an escape sequence...
		C = '\\',!,
		(	% but we are at the end of the stream...
			at_end_of_stream(Stream,['unfinished escape sequence (\\)']),
			!,
			R = []
		;	
			get_char(Stream,NC),
			(
				NC = '\\',!,
				R = ['\\'|Cs]
			;
				NC = 't',!,
				R = ['\t'|Cs]
			;
				NC = 'n',!,
				R = ['\n'|Cs]
			;
				NC = '\'',!,
				R = ['\''|Cs]
			;
				lexer_error(Stream,['unsupported escape sequence (\\',NC,')']),
				R = [NC|Cs]
			),
			read_string_with_quotations(Stream,Delimiter,Cs)
		)
	;	% C is an "ordinary" character...
		R = [C|Cs],
		read_string_with_quotations(Stream,Delimiter,Cs)
	).




% a squence of "operator" signs
read_operators(Stream,[]) :- at_end_of_stream(Stream),!.
read_operators(Stream,[C|Cs]) :-
	peek_char(Stream,C),
	operator_char(C),!,
	get_char(Stream,_),
	read_operators(Stream,Cs).	
read_operators(_Stream,[]).


% the list of all operators that are allowed to be combined to form new
% operator names, such as, :-
operator_char('='). % "=" is not mentioned in the ISO Prolog book
operator_char('+').
operator_char('-').
operator_char('*').
operator_char('/').
operator_char('\\').
operator_char('~').
operator_char('^').
operator_char('<').
operator_char('>').
operator_char(':').
operator_char('.').
operator_char('?').
operator_char('@').
operator_char('#').
operator_char('$').
operator_char('&').




% an EOL comment can contain arbitrary chars and extends until the end of the line
read_eol_comment(Stream,[]) :- at_end_of_stream(Stream),!.
read_eol_comment(Stream,Cs) :-
	get_char(Stream,C),
	process_eol_comment_char(Stream,C,Cs).
process_eol_comment_char(_Stream,'\n',[]) :- !.
process_eol_comment_char(Stream,C,[C|Cs]) :- read_eol_comment(Stream,Cs),!.



% an integer or floating point value
read_number(Stream,S1) :-
	read_int_part(Stream,S1,S2),
	read_fp_part(Stream,S2,[]).
	
	
read_fp_part(Stream,[],[]) :- at_end_of_stream(Stream),!.	
read_fp_part(Stream,S1,SZ) :-
	peek_char(Stream,C),
	read_fp_part1(C,Stream,S1,SZ).
	
read_fp_part1(C,_Stream,[],[]) :- char_type(C,space),!.	
read_fp_part1('.',Stream,S1,SZ) :-
	!,
	stream_property(Stream,position(BeforeDot)),
	get_char(Stream,_),
	read_int_part(Stream,S2,S3),
	(														% if ...
		S2 = [],											% no floating point segment
		!,													% is found
		set_stream_position(Stream,BeforeDot),  % then the "." is an operator
		S1 = SZ, SZ = []
	;	% succeeded reading floating point segment
		S1 = ['.'|S2],
		(
			at_end_of_stream(Stream),
			!,
			S3 = [],
			SZ = []
		;
			peek_char(Stream,C),
			read_fp_part2(C,Stream,S3,SZ)
		)
	).
read_fp_part1(C,Stream,S1,SZ) :-  read_fp_part2(C,Stream,S1,SZ).

read_fp_part2(C,_Stream,[],[]) :- char_type(C,space),!.
read_fp_part2('e',Stream,S1,SZ) :- 
	!,
	get_char(Stream,_),
	S1 = ['e'|S2],
	read_exponent(Stream,S2,SZ).
read_fp_part2(C,Stream,[],[]) :-
	char_type(C,alpha),
	lexer_error(Stream,['unexpected symbol (',C,')']).
read_fp_part2(_C,_Stream,SZ,SZ). 

read_exponent(Stream,[],[]) :- at_end_of_stream(Stream,['exponent expected']),!.
read_exponent(Stream,S1,SZ) :-
	peek_char(Stream,C),
	(
		( C = '-' ; C = '+' ),
		!,
		get_char(Stream,_),
		S1 = [C|SX]
	;
		S1 = SX
	),
	read_int_part(Stream,SX,SZ).
	


% reads the characters of an integer value using a difference list
read_int_part(Stream,SZ,SZ) :- at_end_of_stream(Stream),!.
read_int_part(Stream,[C|SY],SZ) :-
	peek_char(Stream,C),
	char_type(C,digit),
	!,
	get_char(Stream,_),
	read_int_part(Stream,SY,SZ).
read_int_part(_Stream,SZ,SZ).

	
	

% a multi-line comment extends until "*/" is found
read_ml_comment(Stream,_) :- at_end_of_stream(Stream,['expected */']),!.
read_ml_comment(Stream,Token) :-
	peek_char(Stream,C),
	(
		C = '*',!,
		get_char(Stream,_),
		fail/*,
		stream_position(Stream,LN,CN),
		read_unstructured_ml_comment(Stream,Cs)*/
	;
		stream_position(Stream,LN,CN),
		read_unstructured_ml_comment(Stream,Cs),
		atom_chars(ACs,Cs),
		ICN is CN - 1,
		Token=mlc(ACs,LN,ICN)
	).
	

read_unstructured_ml_comment(Stream,[]) :- 
	at_end_of_stream(Stream,['expected */']),
	!.
read_unstructured_ml_comment(Stream,R) :-
	get_char(Stream,C),
	(
		C = '*',!,
		(
			peek_char(Stream,NC),NC='/',!,
			get_char(Stream,_),
			R = []
		;
			R = [C|Cs],
			read_unstructured_ml_comment(Stream,Cs)				
		)
	;
		R = [C|Cs],
		read_unstructured_ml_comment(Stream,Cs)
	).


read_structured_ml_comment(Stream,Token) :-
	read_unstructured_ml_comment(Stream,Token).
/*
read_structured_ml_comment(Stream,Token) :-
	line_count(Stream,LN),line_position(Stream,CN),
	tokenize_sc(Stream,Tokens),
	Token = sc(Tokens,LN,CN).


tokenize_sc(Stream,[]) :- 
	at_end_of_stream(Stream),!,
	current_stream(File,read,Stream),
	atomic_list_concat(['ERROR:',File,': unexpected end of file while parsing structured comment'],MSG),
	write(MSG).
tokenize_sc(Stream,Tokens) :-
	get_char(Stream,C),
	( 
		C = '*', peek_char(Stream,'/'), !, get_char(Stream,_), Tokens = []
	;
		read_token_sc(C,Stream,Token),
		(	% if...
			Token = none,!,
			Tokens = Ts
		;	% else...
			Tokens = [Token|Ts]
		),
		tokenize_sc(Stream,Ts)
	).
	

read_token_sc('@',Stream,'@'(T,LN,CN)) :- 
	!,
	line_count(Stream,LN),line_position(Stream,CN),
	read_identifier(Stream,T).
% TODO read_token_sc('{',Stream,Token) :- ...

read_token_sc(C,Stream,tf(TF,LN,CN)) :- 
	!,
	line_count(Stream,LN),
	line_position(Stream,CN),
	*/



stream_position(Stream,LN,CN) :-
	line_count(Stream,LN),
	line_position(Stream,CN).



at_end_of_stream(Stream,ErrorMessage) :-
	at_end_of_stream(Stream),
	!,
	atomic_list_concat(ErrorMessage,EM),
	lexer_error(Stream,['unexpected end of file (',EM,')']).



lexer_error(Stream,ErrorMessage) :-
	atomic_list_concat(ErrorMessage,EM),
	stream_position(Stream,LN,CN),
	current_stream(File,read,Stream),
	atomic_list_concat(['ERROR:',File,':',LN,':',CN,': ',EM,'\n'],MSG),
	write(MSG).
	
	
