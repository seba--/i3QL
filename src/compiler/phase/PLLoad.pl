/*
	Loads and creates the AST of an SAE Prolog program.
	
	@author Michael Eichberg
*/
:- module('SAEProlog:Compiler:Phase:Load',[pl_load/4]).

:- use_module('../Lexer.pl').
:- use_module('../Parser.pl').
:- use_module('../Predef.pl').
:- use_module('../Debug.pl').


/*	Loads and creates the AST of a SAE Prolog program. 
	
	@param Debug is the list of debug information that should be printed out.
		Possible values are: 'on_entry', 'ast' and 'reading_file'.
	@param Files is the list of files that contain the SAE Prolog program. All 
		files will be loaded.
	@param _OutputFolders 
	@param Program is the created AST. Program has the following structure:
		<pre>
		[	pred(
				a/1, 							% The identifier of the predicate
				[(C, [det, type=int])],	% A list of clauses defining the 
												% predicate. C is one of these clauses.
												% In case of built-in propeties, this list
												% is empty.
				[type=...]					% List of properties of the predicate
			),...
		]
		</pre>
		The list of clauses appear in source code order. The structure of a 
		property is not predefined. <br/>
		The list of user defined predicates is merged with the list of pre-
		defined predicates (see Predef.pl).
*/
pl_load(Debug,Files,_OutputFolders,Program) :-
	debug_message(
		Debug,on_entry,
		'\nPhase: Loading______________________________________________________'),

	findall( % Since we do whole program analyses, we always load all files first.
		P, 
		(
			member(F,Files),
			debug_message(Debug,reading_file,['Reading file: ',F]),
			consult(F), % we use SWI Prolog's built in parser to parse SAE Prolog :-)
			predicate_property(P,file(F))
		),
		Ps), % Ps is the set of all user defined predicates / facts.
	findall(
		pred(F/A,Ps1,[]), % A predicate and the list of its defining clauses.
		(
			member(P1,Ps),
			findall((:-(P1,B),[]),clause(P1,B),Ps1),
			functor(P1,F,A)
		),
		UserDefinedPredicates 
	),
	predefined_predicates(UserDefinedPredicates,Program),
	
	debug_message(Debug,ast,Program).
	



 