





encode_entry_method(Arity,Method),
	encode_entry_method_parameters(Arity,Parameters),
	atomic_list_concat([
		'\tdef apply(',Parameters,') : Solutions = {\n',
		'}\n'
	]).

/*	encode_entry_method_parameters(Arity,Parameters) :-
		Parameters is a (difference) list of atoms that
		encodes a comma separated sequence of method parameters (terms)
		with the names "pX : Term", where X is an int value in the range (0,Arity].
*/
encode_entry_method_parameters(0,_Ps).
encode_entry_method_parameters(Arity,Parameters) :- 
	Arity > 0,
	encode_entry_method_parameters(1,Arity,Ps-[]),
	atomic_list_concat(Ps,Parameters).
	
encode_entry_method_parameters(I,Arity,[P|X]-X) :-
	I =:= Arity,
	!,% Green Cut
	atomic_list_concat([a,I,' : Term'],P).
encode_entry_method_parameters(I,Arity,[P|E]-Z) :-
	I < Arity,
	atomic_list_concat([a,I,' : Term, '],P),
	NewI is I + 1,
	encode_entry_method_parameters(NewI,Arity,E-Z).










encode_arithmetic_expression('SAE@VAR'(VarName,_),[S|SZ]-SZ) :-
	atomic_list_concat([VarName,'.eval'],S),
	!.
encode_arithmetic_expression(IntegerValue,[IntegerValue|SZ]-SZ) :-
	integer(IntegerValue),
	!.
encode_arithmetic_expression(BinaryExpression,SS) :-
	functor(BinaryExpression,Functor,2),
	(
		Functor = '+';
		Functor = '-';
		Functor = '*';
		Functor = '/';
		Functor = 'rem'
	),
	arg(1,BinaryExpression,A),
	arg(2,BinaryExpression,B),
	encode_arithmetic_operator(Functor,Operator),
	SS = ['( '|S2]-SZ,
	encode_arithmetic_expression(A,S2-S3),
	S3 = [' ',Operator,' '|S4],
	encode_arithmetic_expression(B,S4-S5),
	S5 = [' )'|SZ],
	!.
encode_arithmetic_expression(UnaryExpression,SS) :-
	functor(UnaryExpression,Functor,1),
	(
		Functor = '+';
		Functor = '-'
	),
	arg(1,UnaryExpression,A),
	SS = ['( ',Functor|S2]-SZ,
	encode_arithmetic_expression(A,S2-S3),
	S3 = [' )'|SZ],
	!.	
% THIS LAST CLAUSE CATCHES POTENTIAL "PROGRAMMING" ERRORS	
encode_arithmetic_expression(A,X-X) :-	
	write('[Error] Unknown arithmetic expression: '),write(A),write('.'),nl,
	assert(terminate),
	!.
	
% encode_operator(POperator,TOperator)	:-
%	POperator is an  arithmetic Prolog operator.
%	TOperator is the operator used by teh target language (i.e., Scala).
encode_arithmetic_operator('+','+').
encode_arithmetic_operator('-','-').		
encode_arithmetic_operator('*','*').		
encode_arithmetic_operator('/','/').		
encode_arithmetic_operator('rem','%').	






encode_predicate_entry_point(Predicate,OutputFolder) :-
writeln(Predicate),
	Predicate = (Functor/Arity,_),
	predicate_identifier(Functor,Arity,PI),
	
	S1 = ['package sae\n',
			'import saere._\n',
			'import saere.Predef._\n',
			'object ',PI,'{\n',
			'def arity = ',Arity,'\n',
			'def functor = "',Functor,'"\n',
			'/* Entry point. */\ndef unify('|S2]-SZ,
	encode_parameter_list(Arity,S2-S3),
	S3 = [') : Solutions = {\n'|S4],
	encode_predicate_instance_creation(PI,S4-S5),
	S5 = ['}\n}'|SZ],
	
	% Assemble the source code:
	S1 = Ss-[],
	atomic_list_concat(Ss,Scala),
	
	% Write the Scala sourcecode:
	atomic_list_concat([OutputFolder,PI,'.scala'],OutputFile),
	write('Writing: '),write(OutputFile),nl,
	open(OutputFile,write,S),
	write(S,Scala),
	close(S).
	
	
	
% encode_binding_iterator(Predicate,Ss) :- 
%	Predicate is a pair (Functor/Artiy,Clauses) that provides information about
%		the predicate.
% 	Ss is the difference list (A-B) based encoding of the binding_iterator.
encode_binding_iterator((Functor/Arity,Clauses),Ss) :-
	
	Ss = ['new BindingIterator(){\n'|S2]-SZ, %S1 is the first segment, SZ is the last segment
	% Create the (Scala) variables to store the current state of the arguments
	% and the (Scala) variables that represent Prolog variables.
	manifest_state(Arity,S2-S3),
	local_variables(Clauses,S3-S4),
	S4 = ['def next() : Boolean = {\n'|SY],
	%...
	SY = ['}\n}\n'|SZ].
	/*,
	(	(\+
			not_ground_predicate(Cs),
			write('Ground: '),write(Cs)
		)
		;
		write(Cs)
	).*/


% 

manifest_state(0,X-X).
manifest_state(Arity,Ss) :- Arity > 0, manifest_state(1,Arity,Ss).
manifest_state(I,Arity,[P|S2]-SZ) :-
	I =< Arity,
	atomic_list_concat(['val p',I,'State : State = ',p,I,'.manifestState\n'],P),
	NewI is I + 1,
	manifest_state(NewI,Arity,S2-SZ).
manifest_state(I,Arity,X-X) :- I > Arity.


% For every local variable create a Scala Variable
local_variables(Cs,[P|X]-X):-
	number_of_local_variables(Cs,LVsCount).

	/*	forall(
	       member(H :- B,Cs),
	       (   write(H),write(' :- '),write(B),nl)).*/


number_of_local_variables(Clauses,LVsCount) :- number_of_local_variables(Clauses,0,LVsCount).
number_of_local_variables([],LVsCount,LVsCount).
number_of_local_variables([Clause|Clauses],Max,LVsCount) :-
	term_variables(Clause,L),
	length(L,N),
	NewMax is max(N,Max),
	number_of_local_variables(Clauses,NewMax,LVsCount).









predicate_identifier(Functor,Arity,I) :-
   atomic_list_concat([Functor,Arity],I).


encode_predicate_instance_creation(PI,Ss) :- 
	Ss = ['new ',PI,'p('|S2]-SZ,
	S2 = [')\n'|SZ].
	


/*
constructor(Term,Constructor) :-
	term_constructor(Term,Cs-[]),
	atomic_list_concat(Cs,Constructor).
*/
% To get the constructor C (which is an atom),
% call this predicate as follows.
%    term_constructor(Term+,C-[]).
term_constructor(variable(VarName),[VarName|X]-X) :- !.
term_constructor([],['StringAtom.emptyList'|X]-X) :- !. % RED CUT (Required, because an empty list - in particular the empty string: "" - is an atom.)
term_constructor(Term,['IntegerAtom(',Term,')'|X]-X) :- integer(Term),!.
%term_constructor(Term,['FloatAtom(',Term,')'|X]-X) :- float(Term),!.
term_constructor(Term,['StringAtom("',Term,'")'|X]-X) :- atom(Term),!.
term_constructor([H|T],['StringAtom("',Term,'")'|X]-X) :- atom_codes(Term,[H|T]),!.
term_constructor(Term,C) :-
	compound(Term), % Rember: There is no such thing as an empty term: "a()".
	Term =.. [Functor|Args],
	C = ['Term("',Functor,'"'|TCs]-E,
	term_constructors(Args,TCs-R),
	R = [')'|E],
	!.
	
% To make sure that we catch errors early on.
term_constructor(Term,Cs) :- throw((Term,Cs)).

% term_constructors(Ts,Cs) :-
%  Ts is a list of terms,
%  Cs is a comma seperated list of constructors for
%     this term in the target programming language
%     (e.g. Scala).
term_constructors([],X-X).
term_constructors([T|Ts],[','|TC]-RestTCs) :-
	term_constructor(T,TC-TCs),
	term_constructors(Ts,TCs-RestTCs).
	
	
	


