echo(File) :-
	open(File,read,Stream),
	repeat,
		get_char(Stream,C),
		write(C),
	at_end_of_stream(Stream),!,
	close(Stream).



tokenize_file(File,Tokens) :-
	open(File,read,Stream),
	tokenize(Stream,1,0,Tokens),
	close(Stream).



/**
	@signature tokenize(Stream,LN,CN,NLN,NCN,Tokens)
	@param LN the current line number (starts with 1)
	@param CN the number of the current column (starts with 1)
*/
tokenize(Stream,LN,CN,Tokens) :-
	get_char(Stream,C),
	ICN is CN + 1,
	token(C,LN,ICN,Stream,Token,NLN,NCN),
	(
		Token = none,!,
		Tokens = Ts
	;
		Tokens = [Token|Ts]
	),
	(
		at_end_of_stream(Stream),!,
		Ts = []
	;
		tokenize(Stream,NLN,NCN,Ts)
	).


/**
	Reads in a single token.
	
	@signature token(Char,LN,CN,Stream,Token,NLN,NCN)
	@param LN the current line number (starts with 1)
	@param CN the number of the current column (starts with 1)
*/
token('_',LN,CN,Stream,Token,LN,NCN) :- !,
	read_identifier(Stream,X,CN,NCN),
	ID = ['_'|X],
	atom_chars(AID,ID),
	Token = av(AID,LN,CN).	
%token('\'',LN,CN,Stream,Token,NLN,NCN) :- read_escapable_identifier(Stream,Token).
token('%',LN,_CN,Stream,none,NLN,0) :- read_eol_comment(Stream),NLN is LN +1.
token('/',LN,CN,Stream,none,NLN,NCN) :- 
	peek_char(Stream,'*'), % /*/ is not considered to be a valid comment...
	get_char(Stream,_),
	ICN is CN + 1,
	read_ml_comment(Stream,LN,ICN,NLN,NCN).
%token(I,LN,CN,Stream,Token,NLN,NCN) :- char_type(I,digit),read_number(I,Stream,Token).
token(C,LN,CN,Stream,Token,LN,NCN) :- 
	char_type(C,lower),!,
	read_identifier(Stream,X,CN,NCN),
	ID = [C|X],
	atom_chars(AID,ID),
	Token = s(AID,LN,CN).
token(C,LN,CN,Stream,Token,LN,NCN) :- 
	char_type(C,upper),!,
	read_identifier(Stream,X,CN,NCN),
	ID = [C|X],
	atom_chars(AID,ID),
	Token = v(AID,LN,CN).	
token(W,LN,CN,_Stream,none,NLN,NCN) :- 
	char_type(W,space),!,
	(
		W = '\n',!,
		NLN is LN + 1, NCN = 0
	;
		NLN = LN, NCN = CN
	).
token(SC,LN,CN,_Stream,SC,LN,CN) :- special_char(SC),!.	

% Fallback case to enable the lexer to continue if an error is found...
token(C,LN,CN,Stream,none,LN,CN) :-
	current_stream(File,read,Stream),
	atomic_list_concat(['ERROR:',File,':',LN,':',CN,': unrecognized symbol (',C,')\n'],MSG),
	write(MSG).



% An identifier is a squence of upper or lower case letters, digits and "_".
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



% An EOL comment can contain arbitrary chars and extends until the newline character 
read_eol_comment(Stream) :-
	get_char(Stream,C),
	(
		C \= '\n',!,
		read_eol_comment(Stream)
	;
		true % C = '\n'
	).


% A multi-line comment extends until "*/" is found
read_ml_comment(Stream,LN,CN,NLN,NCN) :-
	get_char(Stream,C),
	(
		C = '*',!,
		ILN = LN,
		ICN is CN + 1,
		(
			peek_char(Stream,NC),NC='/',!,
			get_char(Stream,_),
			NLN is ILN,
			NCN is ICN + 1
		;
			read_ml_comment(Stream,ILN,ICN,NLN,NCN)				
		)
	;
		( 	
			C = '\n',!,
			ILN is LN +1,
			ICN = 0 
		; 	
			ILN = LN,
			ICN is CN +1 
		),
		read_ml_comment(Stream,ILN,ICN,NLN,NCN)
	).


special_char('}').
special_char('{').
special_char('(').
special_char(')').
special_char('[').
special_char(']').



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