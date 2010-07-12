/* Implementation of predicates related to debugging the compiler.

	@author Michael Eichberg
*/
:- module(
	'Compiler:Debug',
	[debug_message/3]
).




/* debug_message(Debug,Type,Message) :- prints a debug Message to standard
	out if the developer is interested in the message.<br/>
	This predicate will always succeed.
	
	@param Debug the list of debug information that should be printed out. 
		E.g., [on_entry,ast]
	@param Type identifies the current type of the debug information. E.g.,
		'on_entry'.
	@param Message the message that will be displayed if the developer is
		currently interested in the debug message (i.e, Type is a member of Debug).
*/
debug_message(Debug,Type,Message) :-
	atom(Message),!,
	ignore(
		(member(Type,Debug),write(Message),nl)
	).
debug_message(Debug,Type,MessageFragments) :-
	Type \== ast,!,
	atomic_list_concat(MessageFragments,Message),
	ignore( 
		(member(Type,Debug),write(Message),nl)
	).
debug_message(Debug,ast,Program) :- 
	!,
	ignore( 
		(member(ast,Debug),debug_message_ast(Program))
	).	

debug_message_ast([]).	
debug_message_ast([pred(Predicate,Clauses,PredicateProperties)|Predicates]) :-	
	write('\nPREDICATE: '),write(Predicate),nl,
	write('Properties: '),write(PredicateProperties),nl,
	write_clauses(Clauses),
	debug_message_ast(Predicates).


write_clauses(Clauses) :- 
	ignore((
		Clauses \= [],
		write('Clauses: '),nl,
		write_clauses(1,Clauses)
	)).

write_clauses(_,[]).
write_clauses(Id,[(Clause,ClauseProperties)|Clauses]) :-
	write(Id),write(':\t'),
	write_term(Clause,[quoted(true)]),nl,
	write(Id),write(':\tProperties: '),write(ClauseProperties),nl,
	NewId is Id + 1,
	write_clauses(NewId, Clauses).



