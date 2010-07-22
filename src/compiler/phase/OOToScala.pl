/*
	Generates the code for the SAE program.

	@author Michael Eichberg
*/
:- module('Compiler:Phase:Encode',[oo_to_scala/4]).

:- use_module('../Predef.pl').
:- use_module('../Utils.pl').
:- use_module('../Debug.pl').



/* Encoding of the SAE Prolog program in Scala.

	@param Debug the list of debug information that should be printed.
	@param Program is a program in AOOL.		
*/
oo_to_scala(Debug,OOProgram,OutputFolder,OOProgram) :-
	debug_message(
		Debug,on_entry,
		'\nPhase: Encoding Scala Program_______________________________________'),

	forall(
		member(pred(Functor/Arity,AST),OOProgram),
		(
			debug_message(Debug,processing_predicate,['Processing: ',Functor,'/',Arity]),
			encode_predicate(Functor/Arity,AST,OutputFolder)
		)
	).

	

encode_predicate(Functor/Arity,AST,OutputFolder) :-
	
	encode_as_scala(AST,Scala),
	% Write the Scala sourcecode:
	atomic_list_concat([OutputFolder,Functor,Arity,'.scala'],OutputFile),
	open(OutputFile,write,Stream),
	write(Stream,Scala),
	close(Stream).



encode_as_scala(TopLevelStatements,Scala) :-
	encode_tlstatements(TopLevelStatements,Encoding - []),
	atomic_list_concat(Encoding,Scala).


encode_tlstatements([],X-X).
encode_tlstatements([Statement|Statements],Sx-Sz) :-
	encode_tlstatement(Statement,Sx-Sy),	
	encode_tlstatements(Statements,Sy-Sz).
	

encode_tlstatement(comment(C),[Comment|Ss]-Ss) :-
	atomic_list_concat(['/*\n',C,'\n*/\n'],Comment).
encode_tlstatement(namespace(NS,NSMembers),[Declaration|Ss]-Ss) :-
	encode_nsmembers(NSMembers,NS,EncodedMembers-[]),
	atomic_list_concat(EncodedMembers,Encoding),
	atomic_list_concat(
		[
			'package ',NS,'\n',
			'import saere._\n',
			'import saere.term._\n',
			'import saere.predicate._\n\n',
			Encoding
		],
		Declaration
	).
	


encode_nsmembers([],_NS,X-X).
encode_nsmembers([Member|Members],NS,['\n'|Sx]-Sz) :-
	encode_nsmember(Member,NS,Sx-Sy),	
	encode_nsmembers(Members,NS,Sy-Sz).


encode_nsmember(class(final(Final),Name,SuperType,Members),NS,Ss) :-
	encode_class(class,Final,Name,SuperType,Members,NS,Ss).
	
encode_nsmember(singleton(final(Final),Name,SuperType,Members),NS,Ss) :-
	encode_class(object,Final,Name,SuperType,Members,NS,Ss).


encode_class(ClassType,Final,Name,SuperType,Members,_NS,Ss) :-
	(	Final = yes -> F='final ' ; F =''),
	(	SuperType = none -> 
		ST='' 
	; 
		( encode_type(SuperType,QN), atomic_concat(' extends ',QN,ST) )
	),
	encode_class_members(Members,EncodedMembers-[]),	
	atomic_list_concat(EncodedMembers,Encoding),
	Ss = [F,ClassType,' ',Name,ST,' {\n'|S1]-Sz,
	S1 = [Encoding,'\n'|S2],
	S2 = ['}\n'|Sz].


encode_class_members([],X-X).
encode_class_members([Member|Members],['\n'|Sx]-Sz) :-
	encode_class_member(Member,Sx-Sy),	
	encode_class_members(Members,Sy-Sz).


encode_class_member(field_declaration(final(Final),Name,Type,Expression),Ss) :-
	% Fields are always private
	(Final = yes -> F='val '; F='var '),
	encode_type(Type,T),
	encode_expression(Expression,EncodedExpression),
	Ss = ['private ',F,Name,' : ',T,' = ',EncodedExpression|Sx]-Sx.
	

encode_class_member(method_declaration(Visibility,Name,Signature,Statements),Ss) :-
	(Visibility = private -> V='private '; V=''),
	encode_signature(Signature,EncodedSignature),
	encode_statements(Statements,EncodedStatements),
	atomic_list_concat(EncodedStatements,ESs),
	Ss = [V,'def ',Name,EncodedSignature,'{\n',ESs,'\n}'|Sx]-Sx.


encode_class_member(constructor_declaration(Parameters,Statements),Ss) :-
	encode_parameters(Parameters,EncodedParameters),
	atomic_list_concat(EncodedParameters,EPs),
	encode_statements(Statements,EncodedStatements),
	atomic_list_concat(EncodedStatements,ESs),
	Ss = ['def this(',EPs,'){\n',ESs,'\n}'|Sx]-Sx.


encode_signature(signature(Parameters,ReturnType),E) :-
	encode_parameters(Parameters,EncodedParameters),
	atomic_list_concat(EncodedParameters,EPs),
	encode_type(ReturnType,ERT),
	atomic_list_concat(['(',EPs,') : ',ERT,' = '],E).
	

encode_parameters([],[]).
encode_parameters([parameter(Name,Type)|Parameters],EP) :-
	(	Parameters = [] ->
		SequenzOperator = ''
	;
		SequenzOperator = ', '
	),
	encode_type(Type,EncodedType),
	EP = [Name,' : ',EncodedType,SequenzOperator|OtherEPs],
	encode_parameters(Parameters,OtherEPs).


encode_statements([],[]).
encode_statements([Statement | Statements],ES) :- 
	encode_statement(Statement,EncodedStatement),
	ES = [EncodedStatement|OtherESs],
	encode_statements(Statements,OtherESs).


encode_statement(return(Expression),S) :-
	encode_expression(Expression,E),
	atomic_list_concat(['return ',E,'\n'],S).
encode_statement(set_field(ReceiverExpression,FieldName,ValueExpression),S) :-
	encode_expression(ReceiverExpression,RE),
	encode_expression(ValueExpression,VE),
	atomic_list_concat([RE,'.',FieldName,' = ',VE,'\n'],S).


encode_type(btype(void),'Unit').
encode_type(ptype(int),'Int').
encode_type(ptype(boolean),'Boolean').
encode_type(btype(term),'Term').
encode_type(btype(stringAtom),'StringAtom').
encode_type(btype(goalStack),'List[Solutions]').
encode_type(btype(solutions),'Solutions').
encode_type(btype(array(T)),E) :- 
	encode_type(T,ET),
	atomic_list_concat(['Array[',ET,']'],E).
encode_type(utype(Name),Name).	


encode_expression(this,this).
encode_expression(get_lv(LocalVariableName),LocalVariableName).
encode_expression(get_field(ReceiverExpression,FieldName),GF) :- 
	encode_expression(ReceiverExpression,RE),
	atomic_list_concat([RE,'.',FieldName],GF).
encode_expression(constant_int(I),I).
encode_expression(constant_boolean(B),B).
encode_expression(string_atom(S),SA) :- atomic_list_concat(['StringAtom("',S,'")'],SA).
encode_expression(empty_goal_stack,E) :- E = 'Nil'.
encode_expression(load_from_array(Array,Index),AL) :- 
	encode_expression(Array,AE),
	encode_expression(Index,IE),
	atomic_list_concat([AE,'(',IE,')'],AL).
encode_expression(new(Type,Arguments),NE) :-
	encode_type(Type,ET),
	encode_expressions(Arguments,',',EAs),
	atomic_list_concat(['new ',ET,'(',EAs,')'],NE).
	
	
encode_expressions(Expressions,Separator,E) :-
	expressions_concat(Expressions,Separator,L),
	atomic_list_concat(L,E).

	
expressions_concat([],_Separator,[]).
expressions_concat([Expression|Expressions],Separator,CEs):-	
	encode_expression(Expression,E),
	(
		Expressions = [],
		CEs = [E]
	;
		Expressions = [_|_],
		CEs = [E,Separator|R],
		expressions_concat(Expressions,Separator,R)
	).

	


