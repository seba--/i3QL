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
:- module('Lexer',[tokenize_file/2]).



/**
	A lexer to tokenize standard (ISO/SAE) Prolog files.
	<p>
	The result of tokenizing a file is a list of tokens excluding insignificant
	white space.<br/>
	The types of tokens recognized by this lexer is shown in the following. 
	<code>LN</code> and <code>CN</code> always identify the line and
	column where the token was found.
	<ul>
	<li>s(S,LN,CN) :- S is a list of char atoms.
	<li>sa(S,LN,CN) :- S is a string atom; <i> the cut ("!") is also an atom.</i></li>
	<li>i(N,LN,CN) :- N is an integer value.</li>		
	<li>f(N,LN,CN) :- N is a floating point value.</li>		
	<li>o(Op,LN,CN) :- Op is a sequence of operator characters (e.g., ':-').</li>
	<li>o(Op,LN,CN) :- Op is an operator that cannot appear as part of an operator
	 	sequence (';',',','|').</li>
	<li>av(V,LN,CN) :- V is the name of an anonymous(a) variable.</li>
	<li>v(V,LN,CN) :- V is the name of a concrete(c) variable.</li>
	<li>&lt;Special Characters&gt; is every parantheses ("(,),{,},[,]"), special 
		character is returned as is.</li>			
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
		tokenize_file('Lexer.pl',Ts)
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
	message.
	</p>
	
	@author Michael Eichberg (mail@michael-eichberg.de)
	@version 1.0 - July, 22th 2010
*/



tokenize_file(File,Tokens) :-
	open(File,read,Stream),
	(	% if...
		peek_char(Stream,C),\+ char_type(C,end_of_file),!, % the stream is not empty
		tokenize(Stream,1,0,Tokens)
	;	% else
		Tokens = []
	),
	close(Stream).



/**
	@signature tokenize(Stream,LN,CN,Tokens)
	@param LN the current line number (starts with 1)
	@param CN the number of the current column (starts with 1)
	@param Tokens is the list of tokens (output argument)
*/
tokenize(Stream,LN,CN,Tokens) :-
	get_char(Stream,C),
	ICN is CN + 1,
	token(C,LN,ICN,Stream,Token,NLN,NCN),
	(	% if...
		Token = none,!,
		Tokens = Ts
	;	% else...
		Tokens = [Token|Ts]
	),
	(
		at_end_of_stream(Stream),!,
		Ts = []
	;
		tokenize(Stream,NLN,NCN,Ts)
	).



/**
	Based on Char and at most one further character (using peek_char), the type
	of the token is determined. Reading the rest of the chars of the token
	is delegated to the corresponding "read_*" predicates.
	
	@signature token(Char,LN,CN,Stream,Token,NLN,NCN)
	@param Char the last read token, based on which the type of the token will 
		be decided.
	@param LN the current line number (starts with 1)
	@param CN the number of the current column (starts with 1)
	@param Token the read token (either an atom or a compound term; token is
		unified with 'none' in case of insignificant white space.
*/

token('%',LN,_CN,Stream,none,NLN,0) :- !,
	read_eol_comment(Stream),NLN is LN +1.

% "and (,)", "or (;)", etc. are special operators because they cannot
% contribute to an operator name (e.g. ':-')
token(',',LN,CN,_Stream,o(',',LN,CN),LN,CN) :- !.	
token(';',LN,CN,_Stream,o(';',LN,CN),LN,CN) :- !.	
token('|',LN,CN,_Stream,o('|',LN,CN),LN,CN) :- !.	

% parantheses are "returned" as is 
token('(',LN,CN,_Stream,'(',LN,CN) :- !.	
token(')',LN,CN,_Stream,')',LN,CN) :- !.	
token('{',LN,CN,_Stream,'{',LN,CN) :- !.	
token('}',LN,CN,_Stream,'}',LN,CN) :- !.	
token('[',LN,CN,_Stream,'[',LN,CN) :- !.	
token(']',LN,CN,_Stream,']',LN,CN) :- !.	

% an anonymous variable
token('_',LN,CN,Stream,Token,LN,NCN) :- !,
	read_identifier(Stream,X,CN,NCN),
	ID = ['_'|X],
	atom_chars(AID,ID),
	Token = av(AID,LN,CN).

% a quoted atom
token('\'',LN,CN,Stream,Token,NLN,NCN) :- 
	read_string(Stream,'\'',X,LN,CN,NLN,NCN),
	atom_chars(AID,X),
	Token = sa(AID,LN,CN).
	
% a string	
token('"',LN,CN,Stream,s(X,LN,CN),NLN,NCN) :- 
	read_string(Stream,'"',X,LN,CN,NLN,NCN).

% white space
token(W,LN,CN,_Stream,none,NLN,NCN) :- 
	char_type(W,space),!,
	(
		W = '\n',!,
		NLN is LN + 1, NCN = 0
	;
		NLN = LN, NCN = CN
	).

% a string atom
token(C,LN,CN,Stream,Token,LN,NCN) :- 
	char_type(C,lower),!,
	read_identifier(Stream,X,CN,NCN),
	ID = [C|X],
	atom_chars(AID,ID),
	Token = sa(AID,LN,CN).
token('!',LN,CN,_Stream,sa('!',LN,CN),LN,CN) :- !.	

% a variable name
token(C,LN,CN,Stream,Token,LN,NCN) :- 
	char_type(C,upper),!,
	read_identifier(Stream,X,CN,NCN),
	ID = [C|X],
	atom_chars(AID,ID),
	Token = v(AID,LN,CN).	

% a number
token(I,LN,CN,Stream,Token,LN,NCN) :-
	char_type(I,digit),!,
	read_number(Stream,X,CN,NCN),
	NCs = [I|X],
	number_chars(N,NCs),
	Token = n(N,LN,CN).

% a multi-line comments	
token('/',LN,CN,Stream,Token,NLN,NCN) :- 
	peek_char(Stream,'*'),!, 
	get_char(Stream,_), % /*/ is not considered to be a valid comment...
	ICN is CN + 1,
	read_ml_comment(Stream,LN,ICN,Token,NLN,NCN).

% a sequence of operator characters 
% (This clause has to come AFTER handling of multi-line comments!)
token(C,LN,CN,Stream,Token,LN,NCN) :- 
	operator_char(C),!,
	read_operators(Stream,X,CN,NCN),
	ID = [C|X],
	atom_chars(AID,ID),
	Token = o(AID,LN,CN).
	
% fallback case to enable the lexer to continue if an error is found...
token(C,LN,CN,Stream,none,LN,CN) :-
	current_stream(File,read,Stream),
	atomic_list_concat(['ERROR:',File,':',LN,':',CN,': unrecognized symbol (',C,')\n'],MSG),
	write(MSG).



% an identifier is a squence of upper or lower case letters, digits and "_".
read_identifier(Stream,[C|X],CN,NCN) :-
	peek_char(Stream,C),
	( 
		char_type(C,alnum) 
	; 
		C = '_' 
	),!,
	get_char(Stream,_),
	ICN is CN + 1,
	read_identifier(Stream,X,ICN,NCN).	
read_identifier(_Stream,[],CN,CN).	



read_string(Stream,Delimiter,R,LN,CN,NLN,NCN) :-
	get_char(Stream,C),!,
	(	% if C is a newline character ...
		char_type(C,newline),!,
		ILN is LN + 1,	ICN = 0
	;	% C is not a newline character ...
		ILN = LN, ICN is CN + 1
	),
	(	% C is the delimiter...
		C = Delimiter,!,
		NLN = ILN, NCN = ICN, R = []
	;	% C is the start of an escape sequence...
		C = '\\',!,
		(	% but we are at the end of the stream...
			at_end_of_stream(Stream),!,
			current_stream(File,read,Stream),
			atomic_list_concat(['ERROR:',File,': unexpected end of file while parsing escape sequence (\\)'],MSG),
			write(MSG),
			NLN = ILN, NCN = ICN, R = []
		;	
			get_char(Stream,NC),
			(
				NC = '\\',!,
				R = ['\\'|X]
			;
				NC = 't',!,
				R = ['\t'|X]
			;
				NC = 'n',!,
				R = ['\n'|X]
			;
				NC = '\'',!,
				R = ['\''|X]
			;
				current_stream(File,read,Stream),
				atomic_list_concat(['WARNING:',File,':',LN,':',CN,': unsupported escape character (',NC,'); escape symbol (\\) ignored'],MSG),
				write(MSG),
				R = [NC|X]
			),
			IILN is LN + 1,IICN is CN +1,
			read_string(Stream,Delimiter,X,IILN,IICN,NLN,NCN)
		)
	;	% C is an "ordinary" character...
		(
			at_end_of_stream(Stream),!,
			current_stream(File,read,Stream),
			atomic_list_concat(['ERROR:',File,': unexpected end of file while parsing (a closing \' is missing)'],MSG),
			write(MSG),
			NLN = ILN,
			NCN = ICN,
			R = []
		;	% the base case
			R = [C|X],
			read_string(Stream,Delimiter,X,ILN,ICN,NLN,NCN)
		)
	).
read_string(_Stream,_Delimiter,[],LN,CN,LN,CN).



% a squence of "operator" signs
read_operators(Stream,[C|X],CN,NCN) :-
	peek_char(Stream,C),
	operator_char(C),!,
	get_char(Stream,_),
	ICN is CN + 1,
	read_operators(Stream,X,ICN,NCN).	
read_operators(_Stream,[],CN,CN).



% an EOL comment can contain arbitrary chars and extends until the newline character 
read_eol_comment(Stream) :-
	get_char(Stream,C),
	(
		C \= '\n',!,
		\+ at_end_of_stream(Stream),
		read_eol_comment(Stream)
	;
		true % C = '\n'
	).


% an integer or floating point value
read_number(Stream,X,CN,NCN) :-
	read_int(Stream,SX,SZ,CN,NCN),
	
	SZ = [],X=SX.
	
	

% reads the characters of an integer value using a difference list
read_int(Stream,[C|SY],SZ,CN,NCN) :-
	peek_char(Stream,C),
	char_type(C,digit),
	!,
	get_char(Stream,_),
	ICN is CN + 1,
	read_int(Stream,SY,SZ,ICN,NCN).
read_int(_Stream,SZ,SZ,CN,CN).

	

% a multi-line comment extends until "*/" is found
read_ml_comment(Stream,LN,CN,Token,NLN,NCN) :-
	peek_char(Stream,C),
	(
		C = '*',!,
		get_char(Stream,_),
		ICN is CN + 1,
		read_structured_ml_comment(Stream,LN,ICN,Token,NLN,NCN)
	;
		Token = none,
		read_unstructured_ml_comment(Stream,LN,CN,NLN,NCN)
	).
	
read_structured_ml_comment(Stream,LN,CN,none,NLN,NCN) :-
	read_unstructured_ml_comment(Stream,LN,CN,NLN,NCN). % TODO Implement lexing of structured comments...
	
read_unstructured_ml_comment(Stream,LN,CN,NLN,NCN) :-
	get_char(Stream,C),
	(
		C = '*',!,
		ILN = LN,
		ICN is CN + 1,
		(
			peek_char(Stream,NC),NC='/',!,
			get_char(Stream,_),
			NLN is ILN, NCN is ICN + 1
		;
			read_unstructured_ml_comment(Stream,ILN,ICN,NLN,NCN)				
		)
	;
		( 	
			C = '\n',!,
			ILN is LN +1, ICN = 0 
		; 	
			ILN = LN, ICN is CN +1 
		),
		read_unstructured_ml_comment(Stream,ILN,ICN,NLN,NCN)
	).



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