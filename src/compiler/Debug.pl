/* Predicates to debugg the compiler.

	@author Michael Eichberg
*/
:- module(
	'SAEProlog:Compiler:Debug',
	[debug_message/3]
).




/* debug_message(Debug,Type,Message) :- prints the debug Message to standard
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
		(
			(
				(member(ast,Debug),ShowPredicates = all)
			;
				member(ast(ShowPredicates),Debug)
			),
			debug_message_ast(ShowPredicates,Program)
		)
	).	

debug_message_ast(_ShowPredicates,[]).	
debug_message_ast(ShowPredicates,[pred(Predicate,Clauses,PredicateProperties)|Predicates]) :-	
	(
		(	ShowPredicates = all 
		; 
			(ShowPredicates = built_in, Clauses=[])
		;
			(ShowPredicates = user, Clauses=[_|_])
		) -> (
			write('\nPREDICATE: '),write(Predicate),nl,
			write('Properties: '),write(PredicateProperties),nl,
			write_clauses(Clauses)
		) 
	; 
		true
	),
	debug_message_ast(ShowPredicates,Predicates).


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



