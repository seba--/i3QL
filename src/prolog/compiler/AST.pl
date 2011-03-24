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
	This module provides the predicates to create, traverse and manipulate a 
	program's AST. <i>Code that wants to add, remove, manipulate or traverse the
	AST is not allowed to directly operate on the AST; the datastructures used
	to store the AST may change without notice.</i>
	
	<p><b>The AST</b><br/>
	<i>Conceptually</i>, the AST has the following structure:
	<pre><code>
	[										% the list of all predicates...
		pred(								% a predicate
			a/1, 							% the identifier of the predicate
			[								% the clauses defining the predicate... 
											% (built-in predicates do not have clauses)
				(	C, 					% a clause
					[det, type=I]		% the properties of this clause
				)
			],												
			[type=...]					% the properties of this predicate (e.g.,
											% whether this predicate – as a whole – is
											% (semi-)deterministic, uses the cut operator,
											% ...).
		),...
	],
	[
											% the list of directives...
	]
	</code></pre>
	The precise data structures that are used to store the AST and its nodes
	are an implementation detail of this module. This enables the
	exchange/evolution of the underlying data structure(s) whenever necessary.
	In particular, code that uses the AST must not make assumptions about
	the order and the way the predicates are stored, how clauses are stored, and
	in which way the properties of a clause or predicate are handled. <i>The 
	code must use the appropriate abstractions provided by this module</i>.
	</p>
	
	<p><b>Used Terminology</b><br/>
	To refer to the different parts of a prolog program, we use the terminology
	as defined in the document "Structure of Prolog Programs."
	</p>

	@author Michael Eichberg
*/
:- module(
	sae_ast,
	[	
		empty_ast/1,
		variable/2,
		variable/3,
		variable/4,
		anonymous_variable/2,
		anonymous_variable/3,
		integer_value/2,	
		integer_value/3,
		float_atom/2,		
		float_atom/3,
		string_atom/2,
		string_atom/3,
		compound_term/3,		
		compound_term/4,
		compound_term/5,
		compound_term_args/2,
		compound_term_functor/2,	
		compound_term_identifier/2,	
		directive_goal/2,
		rule/4,
		rule_head/2,
		rule_body/2,
		
		cut_analysis/2,
		disjunction_of_cut_behaviors/3,
		conjunction_of_cut_behaviors/3,
		names_of_variables_of_term/2,
		named_variables_of_term/3,
		
		clone_meta/2,
		pos_meta/2,
		predicate_meta/2,
		add_to_predicate_meta/2,
		lookup_in_meta/2,
		lookup_in_term_meta/2,
		add_to_meta/2,
		add_to_term_meta/2,
		add_to_each_term_meta/2,
		term_meta/2,
		term_pos/2,
		term_pos/4,
		add_clause/3,
		add_predicates/3,
		is_ground_term/1,
		is_rule/1,
		is_rule_with_body/1,
		is_rule_without_body/1,
		is_directive/1,
		is_compound_term/1,
		is_variable/1,
		is_anonymous_variable/1,
		is_string_atom/1,
		is_integer_value/1,
		is_float_atom/1,
		is_numeric_atom/1,
		
		lookup_predicate/3,
		lookup_in_predicate_meta/2,
		add_flag_to_predicate_meta/2,
		user_predicate/2,
		predicate_identifier/2, % TODO rename to predicate_indicator (as used by ISO Prolog)
		predicate_clauses/2,
		foreach_user_predicate/2,
		foreach_clause/3,
		foreach_clause/2,
		single_clause/1,
		two_clauses/1,
		clause_definition/2,
		clause_implementation/2,
		clause_meta/2,
		lookup_in_clause_meta/2,
		add_to_clause_meta/2,
		
		transform_term/4,
		
		write_ast/2,
		write_clauses/1,
		write_ast_node/1
	]
).
:- meta_predicate(foreach_user_predicate(+,1)).
:- meta_predicate(foreach_user_predicate_impl(+,1)).
:- meta_predicate(foreach_clause(+,1)).
:- meta_predicate(foreach_clause(+,4,-)).
:- meta_predicate(foreach_clause_impl(+,+,4,-)).
:- meta_predicate(transform_term(+,2,2,-)).

:- use_module('Utils.pl').




/* DEVELOPER
	Internal Structure of the AST:
	(
		[										% The open list of all predicates
			pred(								% A predicate.
				a/1, 							% The identifier of the predicate.
				[								% An open list of ...
					(							% ... the clauses defining the predicate 
						C, 
						[det,_]				% An open list of this clause's properties.
					),
					_
				],			
											
				[type=...,_]				% An open list of this predicate's properties.
			),
			...
		],
		[
			Directives (as is)
		]
	)

*/



/**
	Unifies a given AST with the empty AST.
	
	@signature empty_ast(AST)
	@arg AST An empty AST.
*/
empty_ast(([]/*Rules*/,[]/*Directives*/)).



/**
	Adds a clause that has to be a valid, normalized rule or a directive to
	the given AST.

	@signature add_clause(AST,Clause,NewAST)
	@arg(in) AST The AST to which the given term is added.
	@arg(in) Clause Clause has to be a valid, normalized rule or a directive.
	@arg(out) NewAST The new AST which contains the new term (predicate).
*/
add_clause(AST,Rule,NewAST) :- 
	Rule = ct(_Meta,':-',[LeftTerm,_RightTerm]),
	(	% e.g., terms such as "do :- write(...)".
		LeftTerm = a(_,Functor),
		Arity = 0
	;	% e.g., terms such as "do(Foo,Bar) :- write(Foo),write(Bar)".
		LeftTerm = ct(_,Functor,Args),
		length(Args,Arity)
	),!,
	ID = Functor/Arity,
	AST=(Rules,Directives),
	(	memberchk(pred(ID,Clauses,_PredProps1),Rules) ->
		append_ol((Rule,_ClauseProps1),Clauses),
		NewAST=AST
	;
		NewAST=([pred(ID,[(Rule,_ClauseProps2)|_],_PredProps2)|Rules],Directives)
	).
add_clause((Rules,Directives),Directive,NewAST) :-
	Directive = ct(_Meta,':-',[_TheDirective]),!,
	NewAST=(Rules,[Directive|Directives]).
add_clause(_AST,Term,_NewAST) :- 
	throw(internal_error('the term is not a rule or directive',Term)).



/**
	Adds the information about (built-in) predicates to the AST.

	@signature add_predicates(AST,Predicates,NewAST)
	@arg(in) AST is the current AST.
	@arg(in) Predicates is the list of all built-in predicates. The information 
		about the predicates has to be structured as follows:<br/>
		<pre><code>
		[									% List of predicates
			pred(							% A predicate
				Functor/Arity,			% The predicate's identifier
				[							% List of properties
					&lt;Property&gt;	% A property
				]
			)
		]
		</code></pre>
	@arg NewAST is the AST that contains the information about the 
		specified predicates.
*/
add_predicates((Rules,Directives),[pred(ID,Properties)|Preds],NewAST) :- !,
	IntermediateAST = ([pred(ID,[],Properties)|Rules],Directives),
	add_predicates(IntermediateAST,Preds,NewAST).
add_predicates(AST,[],AST).



/**
	@signature user_defined_predicates(AST,UserPredicateASTNode)
*/
user_predicate((Predicates,_Directives),UserPredicate) :-
	user_predicate_impl(Predicates,UserPredicate).
user_predicate_impl([Predicate|Predicates],UserPredicate) :-
	(
		Predicate = pred(_Identifier,[_|_],_PredicateProperties),
		UserPredicate = Predicate
	;
		user_predicate_impl(Predicates,UserPredicate)
	).



foreach_user_predicate((Predicates,_Directives),Goal) :-
	foreach_user_predicate_impl(Predicates,Goal).
foreach_user_predicate_impl([Predicate|Predicates],Goal) :-
	% user defined predicates are those predicates with at least one clause: [_|_]
	Predicate = pred(PredFunctor/PredArity,[_|_],_PredicateProperties), !,
	(	call(Goal,Predicate) ->
		foreach_user_predicate_impl(Predicates,Goal)
	;
		functor(Goal,Functor,Arity),
		atomic_list_concat(['calling the goal (',Functor,'/',Arity,') for the predicate (',PredFunctor,'/',PredArity,') failed'],Message),
		throw(internal_error(foreach_user_predicate/2,Message))
	).
foreach_user_predicate_impl([_Predicate|Predicates],Goal) :- !,
	foreach_user_predicate_impl(Predicates,Goal).
foreach_user_predicate_impl([],_Goal).



/**
	@signature predicate_clauses(Predicate_ASTNode,Clauses)
	@arg Clauses the clauses of the predicate.
*/
predicate_clauses(pred(_ID,Clauses,_Properties),Clauses).



lookup_in_predicate_meta(Property,pred(_ID,_Clauses,Properties)) :-
	memberchk_ol(Property,Properties).



add_flag_to_predicate_meta(Flag,pred(_ID,_Clauses,Properties)) :-
	\+ memberchk_ol(Flag,Properties) ->
		add_to_meta(Flag,Properties)
	;
		true.



add_to_predicate_meta(Property,Predicate) :-
	predicate_meta(Predicate,Meta),add_to_meta(Property,Meta).



/**
	@signature lookup_predicate(PredicateIdentifier,Program,Predicate)
*/
lookup_predicate(PredicateIdentifier,(Predicates,_Directives),Predicate) :-
	lookup_predicate_in_predicates(PredicateIdentifier,Predicates,Predicate).



%INTERNAL
lookup_predicate_in_predicates(
		PredicateIdentifier,
		[Predicate|_Predicates],
		Predicate
	) :-
	Predicate = pred(PredicateIdentifier,_,_),!.
lookup_predicate_in_predicates(
		PredicateIdentifier,
		[_Predicate|Predicates],
		Predicate
	) :-
	lookup_predicate_in_predicates(PredicateIdentifier,Predicates,Predicate).



/**
	@deprecated Use {@link clause_implementation/2} instead.
 */
clause_definition((Clause,_Meta),Clause).



/**
	@signature clause_implementation(Clause,Implementation)
 */
clause_implementation((Implementation,_Meta),Implementation).



/**
	Succeeds if the given clauses AST contains a single clause; i.e., if a
	predicate is defined by a single clause.
	
	@signature single_clause(Clauses_ASTNode)
 */
single_clause(Clauses) :- % Clauses is an open list...
	nonvar(Clauses),Clauses=[_|T],var(T).



two_clauses(Clauses):-
	nonvar(Clauses),Clauses=[_|FCs],nonvar(FCs),FCs=[_|R],var(R).



/**
	Calls the given goal for each clause with the respective clause as the last
	parameter.
	
	@signature foreach_clause(Clauses_ASTNode,Goal)
*/
foreach_clause(Var,_Goal) :- var(Var),!. % Clauses is an open list...
foreach_clause([Clause|Clauses],Goal) :- 
	call(Goal,Clause), !,
	foreach_clause(Clauses,Goal).
foreach_clause([Clause|_Clauses],Goal) :-
 	throw(
		internal_error(
			foreach_clause/2,
			['calling the goal: ',Goal,' with ',Clause,'failed'])).



/**
	For each clause the given goal is called with the following information:<br/>
	<ol>
	<li>N - the number of the clause</li>
	<li>Clause - the clause</li>
	<li>Last - indicates if the given clause is the last clause. The value is either: 
		<code>last</code> or <code>not_last</code>.
	<li>Result - a free variable where the called goal can store its result.
	</ol>

	@signature foreach_clause(Clauses_ASTNode,Goal,ListOfResults)
*/
foreach_clause(Clauses,Goal,Os) :- 
	foreach_clause_impl(Clauses,0,Goal,Os).
foreach_clause_impl(Var,_,_,[]) :- var(Var),!. % Clauses is an open list...
foreach_clause_impl([Clause|Clauses],N,F,[O|Os]) :- 
	(	var(Clauses) -> Last = last ; Last = not_last ),
	call(F,N,Clause,Last,O),!,
	NewN is N + 1,
	foreach_clause_impl(Clauses,NewN,F,Os).
foreach_clause_impl([Clause|_Clauses],_N,F,_) :- 
 	throw(
		internal_error(
			foreach_clause/2,
			['calling the goal: ',F,' with ',Clause,'failed'])).



/**
	@signature clause_meta(Clause_ASTNode,MetaInformation)
*/
clause_meta((_Clause,Meta),Meta).



add_to_clause_meta(Information,Clause) :-
	clause_meta(Clause,Meta),add_to_meta(Information,Meta).



lookup_in_clause_meta(Information,Clause) :-
	clause_meta(Clause,Meta),lookup_in_meta(Information,Meta).



/**
	@signature predicate_meta(Predicate_ASTNode,MetaInformation)
	@arg MetaInformation is the information about the predicate as a whole.
*/
predicate_meta(pred(_ID,_Clauses,Meta),Meta).



/**
 @signature predicate_identifier(Predicate_ASTNode,Functor/Arity)
*/
predicate_identifier(pred(ID,_Clauses,_Properties),ID).



/**
	Succeeds if the given ASTNode represents a directive.<br /> 
	(A directive is a compound term, with the functor 
	":-" and exactly one argument.)
	<p>
	A directive is never a rule(/fact) and vice versa.
	</p>
	
	@signature is_directive(ASTNode)
*/
is_directive(ct(_Meta,':-',[_Directive])) :- true. 



/**
	Goal is a directive's goal. For example, given the directive:
	<code><pre>
	:- use_module('Utils.pl').
	</pre></code>
	then Goal is the AST node that represents the compound term 
	<code>use_module</code>.
	<p>
	This predicate can either be used to extract a directive's goal or
	to construct a new AST node that represents a directive and which is not
	associated with any meta information.
	</p>
	
	@signature directive_goal(Directive_ASTNode,Goal_ASTNode)
	@arg Directive_ASTNode An AST node that represents a directive definition.
	@arg Goal_ASTNode The directive's goal
*/
directive_goal(ct(_Meta,':-',[Goal]),Goal) :- true.



/**
	(De-)constructs a rule.
	
	@signature rule(Head_ASTNode,Body_ASTNode,Meta,Rule_ASTNode)
	@arg Head_ASTNode The rule's head.
	@arg Body_ASTNode The rule's body.
	@arg Meta Meta-information about the rule.
	@arg(out) Rule_ASTNode
*/
rule(Head,Body,Meta,ct(Meta,':-',[Head,Body])) :- true.


/**
	Extracts a rule's head.
	<p>
	<font color="red">
	This predicate must not/can not be used to create a partial rule. The ASTNode
	will be invalid.
	</font>
	</p>
	
	@signature rule_head(ASTNode,Head_ASTNode)
	@arg(in) ASTNode 
	@arg(out) Head_ASTNode A rule's head. 
*/
rule_head(ct(_Meta,':-',[Head,_]),Head) :- !.
rule_head(Head,Head) :- \+ is_directive(Head).



/**
	Extracts a rule's body.
	<p><font color="red">
	This predicate must not/can not be used to create a partial rule. The ASTNode
	will be invalid.
	</font></p>

	@signature rule_body(Rule_ASTNode,RuleBody_ASTNode)
*/
rule_body(ct(_Meta,':-',[_Head,Body]),Body) :- true.



/**
	Succeeds if the given clause is a rule definition; i.e., if the clause is not
	a directive.

	@signature is_rule(Clause_ASTNode) 
*/
is_rule(Clause) :- is_rule_with_body(Clause),!.
is_rule(Clause) :- is_rule_without_body(Clause),!.



/**
	Succeeds if the given AST node represents a rule with a head and a body.
	
	@signature is_rule_with_body(ASTNode)
	@arg(in) ASTNode
*/
is_rule_with_body(ct(_Meta,':-',[_Head,_Body])) :- true.



/**
	Succeeds if the given AST node represents a rule definition without a body;
	e.g., rules such as:
	<code><pre>
	true.
	loves(X,Y).
	</pre></code>
	
	@signature is_rule_without_body(ASTNode)
	@arg(in) ASTNode
*/
is_rule_without_body(Clause) :- 
	\+ is_directive(Clause),
	(
		Clause = ct(_CTMeta,Functor,_Args),
		Functor \= (':-')
	;
		Clause = a(_AMeta,_Value) % e.g., "true."
	).



is_compound_term(ct(_,_,_)).



/**
	Succeeds if ASTNode represents the declaration of a named variable; i.e.,
	a variable that is not anonymous.
	
	@signature is_variable(ASTNode)
	@arg(in) ASTNode
*/
is_variable(v(_Meta,_Name)).



/**
	Succeeds if ASTNode represents the declaration of an anonymous variable.
	
	@signature is_anonymous_variable(ASTNode)
	@arg(in) ASTNode	
*/
is_anonymous_variable(av(_Meta,_Name)).



/**
	Succeeds if ASTNode represents a string atom.
	
	@signature is_integer_value(ASTNode)
	@arg(in) ASTNode	
*/
is_string_atom(a(_Meta,_Value)).



/**
	Succeeds if ASTNode represents a numeric value.
	
	@signature is_numeric_atom(ASTNode)
	@arg(in) ASTNode	
*/
is_numeric_atom(ASTNode) :- 
	once((is_integer_value(ASTNode) ; is_float_atom(ASTNode))).



/**
	Succeeds if ASTNode represents an integer value.
	
	@signature is_integer_value(ASTNode)
	@arg(in) ASTNode	
*/
is_integer_value(i(_Meta,_Value)).



/**
	Succeeds if ASTNode represents a floating point value.
	
	@signature is_float_atom(ASTNode)
	@arg(in) ASTNode	
*/
is_float_atom(r(_Meta,_Value)).



/**
	(De)constructs an AST node that represents a variable definition.<br /> 
	If this predicate is used to construct an AST node, the node will not be 
	associated with any meta information.
	
	@signature variable(Variable_ASTNode,VariableName)
*/
variable(v(_Meta,VariableName),VariableName).



/**
	(De)constructs an AST node that represents a variable definition.<br />
	If this predicate is used to construct an AST node, the only meta information 
	will be the position information.
	
	@signature variable(VariableName,Pos,Variable_ASTNode)
*/
variable(Name,Pos,v([Pos|_],Name)).



/**
	ASTNode is a term that represents a variable definition. 
	<p>
	The name of the variable is determined by concatenating the BaseQualifier and 
	the ID. The associated meta information (e.g., the position of the variable
	in the source code) is determined by Meta.<br />
	This predicate can only be used to create new variable nodes.
	</p>
	
	@signature variable_node(BaseQualifier,Id,Meta,ASTNode)
	@arg(atom) BaseQualifier Some atom.
	@arg(atom) Id Some atom.
	@arg Meta Meta-information associated with the variable.	
	@arg ASTNode An AST node representing the definition of a non-anonymous 
		variable.
*/
variable(BaseQualifier,Id,Meta,v(Meta,VariableName)) :-
	atom_concat(BaseQualifier,Id,VariableName).	



/**
	(De)constructs an AST node that represents the definition of an anonymous 
	variable.<br/>
	If this 
	predicate is used to construct an AST node, the node will not be associated
	with any meta information.

	@signature anonymous_variable(AnonymousVariable_ASTNode,Name)
*/
anonymous_variable(av(_Meta,Name),Name).



/**
	(De)constructs an AST node that represents the definition of an anonymous 
	variable.<br />
	If this 
	predicate is used to construct an AST node, the only meta information will
	be the position information.
	
	@signature anonymous_variable(Name,Pos,AnonymousVariable_ASTNode)
*/
anonymous_variable(Name,Pos,av([Pos|_],Name)).



/**
	(De)constructs an AST node that represents the definition of an integer 
	value.<br/>
	If this predicate is used to construct an AST node, the node will not be
	associated with any meta information.
	
	@signature integer_value(IntegerAtom_ASTNode,Value)
*/
integer_value(i(_Meta,Value),Value). % TODO rename to integer_value


/**
	(De)constructs an AST node that represents the definition of an integer 
	value.<br />
	If this predicate is used to construct an AST node, the only meta information
	will be the position information.
	
	@signature integer_value(Value,Pos,IntegerAtom_ASTNode)
*/
integer_value(Value,Pos,i([Pos|_],Value)). % TODO rename to integer_value


/**
	(De)constructs an AST node that represents the definition of a float 
	value.<br/>
	If this predicate is used to construct an AST node, the node will not be
	associated with any meta information.
	
	@signature float_atom(FloatAtom_ASTNode,Value)
*/
float_atom(r(_Meta,Value),Value). % TODO rename to float_value

/**
	(De)constructs an AST node that represents the definition of a float atom.<br />
	If this 
	predicate is used to construct an AST node, the only meta information will
	be the position information.
	
	@signature float_atom(Value,Pos,FloatAtom_ASTNode)
*/
float_atom(Value,Pos,r([Pos|_],Value)). % TODO rename to float_value


/**
	(De)constructs an AST node that represents the definition of a string 
	atom.<br/>
	If this predicate is used to construct an AST node, the node will not be
	associated with any meta information.
	
	@signature string_atom(StringAtom_ASTNode,Value)
*/
string_atom(a(_Meta,Value),Value).



/**
	(De)constructs an AST node that represents the definition of a string atom.<br />
	If this 
	predicate is used to construct an AST node, the only meta information will
	be the position information.

	@signature string_atom(Value,Pos,StringAtom_ASTNode)
*/
string_atom(Value,Pos,a([Pos|_],Value)).



/**
	(De)constructs an AST node that represents the definition of a compound term.<br/>
	If this predicate is used to construct an AST node, the node will not be
	associated with any meta information.
	
	@signature compound_term(CompoundTerm_ASTNode,Functor,Args)
	@arg CompoundTerm_ASTNode
	@arg Functor A string atom representing the compound term's functor.
	@arg Args the arguments of a compound term. Each argument is an AST node.
*/
compound_term(ct(_Meta,Functor,Args),Functor,Args).



/**
	Constructs a new compound term.
	
	@signature compound_term(Functor,Args,Pos,ASTNode)
	@arg Args are the arguments of a compound term. They are AST nodes.
*/
compound_term(Functor,Args,Pos,ct([Pos|_],Functor,Args)).



/**
	Constructs a new compound term.
	
	@signature compound_term(Functor,Args,Pos,OperatorTable,ASTNode)
	@arg Args are the arguments of a compound term. They are AST nodes.
*/
compound_term(Functor,Args,Pos,OperatorTable,ct([Pos,OperatorTable|_],Functor,Args)).



/**
	@signature compound_term_args(CompoundTerm_ASTNode,Args)
	@arg(in) CompoundTerm_ASTNode is an AST node that represents a compound term.
	@arg(out) Args are the arguments of a compound term. They are AST nodes.
*/
compound_term_args(ct(_Meta,_Functor,Args),Args).



/**
	@signature compound_term_functor(CompoundTerm_ASTNode,Functor)
*/
compound_term_functor(ct(_Meta,Functor,_Args),Functor).



/**
	@signature compound_term_identifier(CompoundTerm_ASTNode,Functor/Arity)
*/
compound_term_identifier(ct(_Meta,Functor,Args),Functor/Arity) :-
	length(Args,Arity).



/**
	Meta is a term that represents the term's meta information. For example, the
	source code position, the current operator table, associated comments.
	@arg(in) ASTNode
	@arg(out) Meta
*/
term_meta(ASTNode,Meta) :- ASTNode =.. [_,Meta|_].



add_to_each_term_meta(_,[]).
add_to_each_term_meta(Flag,[ASTNode|ASTNodes]) :-
	term_meta(ASTNode,Meta),
	add_to_meta(Flag,Meta),
	add_to_each_term_meta(Flag,ASTNodes).



add_to_term_meta(Information,ASTNode) :-
	term_meta(ASTNode,Meta),add_to_meta(Information,Meta).



lookup_in_term_meta(Information,ASTNode) :-
	term_meta(ASTNode,Meta),lookup_in_meta(Information,Meta).



lookup_in_meta(Information,Meta) :- memberchk_ol(Information,Meta).



add_to_meta(Information,Meta) :- append_ol(Information,Meta).



clone_meta(Meta,NewMeta) :- clone_ol(Meta,NewMeta).



/**
	@signature pos_meta(Position,MetaInformation_ASTNode)
*/
pos_meta(Pos,[Pos|_]) :- Pos = pos(_,_,_).



/**
	Pos is the position of a term in the original source file.
	
	@arg(in) ASTNode is an AST node representing a term in a source file.
	@arg(out) Pos is the position object {@file SAEProlog:Compiler:Parser} 
		identifying the position of the term in the source file.
*/
term_pos(ASTNode,Pos) :- 
	ASTNode =.. [_,Meta|_], 
	Meta = [Pos|_], 
	Pos = pos(_File,_LN,_CN).



/**
	Returns the position of the term in the source file. Succeeds iff position
	information is available.
	
	@arg(in) ASTNode An AST node representing a term in a source file.
	@arg(out) File 
	@arg(out) LN
	@arg(out) CN 	 
*/ % TODO deep documentation: make sure that the in annotation is enforced (=.. requires AST to be a compound term or an atom)
term_pos(ASTNode,File,LN,CN) :- 
	ASTNode =.. [_,Meta|_],
	Meta = [Pos|_],
	Pos = pos(File,LN,CN).



/**
	@signature names_of_variables_of_term(ASTNode,SetOfVariableNames)
*/
names_of_variables_of_term(ASTNode,Vs) :- 
	names_of_variables_of_term(ASTNode,[],Vs).


names_of_variables_of_term(v(_,VariableName),Vs,NewVs) :- !,
	add_to_set(VariableName,Vs,NewVs).
names_of_variables_of_term(ct(_,_,Args),Vs,NewVs) :- !,
	names_of_variables_of_terms(Args,Vs,NewVs).
names_of_variables_of_term(_,Vs,Vs).	


names_of_variables_of_terms([Term|Terms],Vs,NewVs) :-
	names_of_variables_of_term(Term,Vs,IVs),
	names_of_variables_of_terms(Terms,IVs,NewVs).
names_of_variables_of_terms([],Vs,Vs).



/**
	@signature named_variables_of_term(ASTNode,[Variables_ASTNodes|T],T)
*/
named_variables_of_term(v(Meta,Name),[v(Meta,Name)|SZ],SZ) :- !.
named_variables_of_term(ct(_,_,Args),S1,SZ) :- !,
	named_variables_of_terms(Args,S1,SZ).
named_variables_of_term(_,SZ,SZ).	


named_variables_of_terms([Term|Terms],S1,SZ) :-
	named_variables_of_term(Term,S1,S2),
	named_variables_of_terms(Terms,S2,SZ).
named_variables_of_terms([],SZ,SZ).



/**	
	Determines how the given term(goal) uses the cut operator.
	<p>
	The cut analysis supports all standard control-flow constructs:<br/>
	 ",",";","!","->","*->","-> ; ","*-> ; ".
	</p>
	Recall, a cut in the condition part of a soft-cut or if-then-else is local
	to the condition part.<br />
	Example, for if-then-else:<br />
	<code><pre>
	?- member(X,[a,b,a,c]),
		(	(	write(try_a_),X == a ; 
				write(try_b_),!, X == b; % this cut prevents that the next goals is ever called
				write(try_c_),X == c) 
		->	write(yes),nl 
		; 	write(fail),nl
		).
	try_a_yes
	X = a ;
	try_a_try_b_yes
	X = b ;
	try_a_yes
	X = a ;
	try_a_try_b_fail
	X = c ;
	false.

	</pre></code>
	<pre><code>
	?- member(X,[a,b,a,c]),
		(	(write(try_),X == a ; write(try_), X == b),
			! 
		*-> 
			write(yes),nl 
		; 
			write(fail),nl
		).
	try_a_yes
	X = a ;
	try_b_try_a_try_b_yes
	X = b ;
	try_a_yes
	X = a ;
	try_b_try_a_try_b_fail
	X = c ;
	false.
	</pre></code>
	
	@signature cut_analysis(ASTNode,CutBehavior)
	@arg(out) CutBehavior is either "always", "maybe", or "never".
*/
% TODO write a test to check that the cut behavior analysis is correct for "->" and "*->"
cut_analysis(a(_,'!'),always):- !. % TODO rename cut_analysis to cut_usage_analysis

cut_analysis(ct(_Meta,',',[LASTNode,RASTNode]),CutBehavior) :- !,
	cut_analysis_of_and_goal(LASTNode,RASTNode,CutBehavior).

cut_analysis(ct(_Meta,';',[LASTNode,RASTNode]),CutBehavior) :- !,
	(
		(	LASTNode = ct(_,'->',[ConditionASTNode,ThenASTNode]) ;
	 		LASTNode = ct(_,'*->',[ConditionASTNode,ThenASTNode]) ) ->
		cut_analysis_of_or_goal(ThenASTNode,RASTNode,CutBehavior)
	;
		cut_analysis_of_or_goal(LASTNode,RASTNode,CutBehavior)
	).
		
cut_analysis(ct(_Meta,'->',[_ConditionASTNode,BodyASTNode]),CutBehavior) :- !,
	cut_analysis(BodyASTNode,CutBehavior).	
	
cut_analysis(ct(_Meta,'*->',[_ConditionASTNode,BodyASTNode]),CutBehavior) :- !,
	cut_analysis(BodyASTNode,CutBehavior).	
		
cut_analysis(_,never).



cut_analysis_of_or_goal(LASTNode,RASTNode,CutBehavior) :- 
	cut_analysis(LASTNode,LCutBehavior),
	cut_analysis(RASTNode,RCutBehavior),
	disjunction_of_cut_behaviors(LCutBehavior,RCutBehavior,CutBehavior).



disjunction_of_cut_behaviors(LCutBehavior,RCutBehavior,CutBehavior) :-	
	(	( LCutBehavior == always , RCutBehavior == always ),
		CutBehavior = always
	;	( LCutBehavior == never , RCutBehavior == never ),
		CutBehavior = never
	;
		CutBehavior = maybe
	),!.



cut_analysis_of_and_goal(LASTNode,RASTNode,CutBehavior) :-
	cut_analysis(LASTNode,LCutBehavior),
	cut_analysis(RASTNode,RCutBehavior),
	conjunction_of_cut_behaviors(LCutBehavior,RCutBehavior,CutBehavior).



conjunction_of_cut_behaviors(LCutBehavior,RCutBehavior,CutBehavior) :-	
	(	( LCutBehavior == always ; RCutBehavior == always ),
		CutBehavior = always
	;	( LCutBehavior == maybe ; RCutBehavior == maybe ),
		CutBehavior = maybe
	;
		CutBehavior = never
	),!.



/**
	True if the given ASTNode represents a ground term. If the ASTNode itself or
	some sub node of it represents a variable or an anonymous variable then this
	predicate fails. 
	
	@signature is_ground(ASTNode) is semidet
	@arg(in,+) ASTNode an ASTNode; if the term is not an ASTNode the result is 
		undefined.
*/
% TODO rename: "is_ground"
is_ground_term(V) :- 
	var(V),
	throw(internal_error(
			'AST.pl',
			is_ground_term/1,
			'the argument must be instantiated')
	).
is_ground_term(ct(_Meta,_Functor,ASTNodes)) :- !,
	forall(member(ASTNode,ASTNodes),is_ground_term(ASTNode)).
is_ground_term(r(_,_)) :- !.
is_ground_term(i(_,_)) :- !.
is_ground_term(a(_,_)) :- !.
is_ground_term(av(_Meta,_Name)) :- !,false.	 
is_ground_term(v(_Meta,_Name)) :- !,false.



/**
	Performs a depth-first, bottom-up traversal of the term (ASTNode) and calls 
	for each
	primitive goal and each control flow goal (<i>"COND -&gt; THEN ; ELSE",
	"COND *-&gt; THEN ; ELSE", ",", ";", "-&gt;", "*-&gt;"</i>) 
	the corresponding goal (<code>PrimitiveGoalTransformer</code>/<code>
	ContolFlowGoalTransformer</code>) to transform the goal.
	
	@signature transform_term(
			+ASTNode:ASTNode,
			+PrimitiveGoalTransformer:callable,
			+ContolFlowGoalTransformer:callable,
			-NewASTNode:ASTNode
		) is det
	@arg PrimitiveGoalTransformer a callable term that is passed in the current
		primitive goal (ASTNode) and a free variable which has to be bound to the 
		transformed ASTNode. If primitive goals are not to be transformed, it is
		sufficient to pass in '=' to unify the input and the output.
*/
transform_term(
		ct(OrMeta,';',[ct(CondMeta,CFFunctor,[CondASTNode,LASTNode]),RASTNode]),
		PrimitiveGoalTransformer,
		ContolFlowGoalTransformer,
		NewASTNode
	) :-
	(	CFFunctor == '->' ;	CFFunctor == '*->' ),	!,
	transform_term(CondASTNode,PrimitiveGoalTransformer,ContolFlowGoalTransformer,NewCondASTNode),
	transform_term(LASTNode,PrimitiveGoalTransformer,ContolFlowGoalTransformer,NewLASTNode),
	transform_term(RASTNode,PrimitiveGoalTransformer,ContolFlowGoalTransformer,NewRASTNode),
	call(
		ContolFlowGoalTransformer,
		ct(OrMeta,';',[
				ct(CondMeta,CFFunctor,[NewCondASTNode,NewLASTNode]),
				NewRASTNode
			]
		),
		NewASTNode).

transform_term(
		ct(Meta,CFFunctor,[LASTNode,RASTNode]),
		PrimitiveGoalTransformer,
		ContolFlowGoalTransformer,
		NewASTNode
	) :-
	(	CFFunctor == ';' 
	;	CFFunctor == ',' 
	;	CFFunctor == '->' 
	;	CFFunctor == '*->'
	),
	!,
	transform_term(LASTNode,PrimitiveGoalTransformer,ContolFlowGoalTransformer,NewLASTNode),
	transform_term(RASTNode,PrimitiveGoalTransformer,ContolFlowGoalTransformer,NewRASTNode),
	call(ContolFlowGoalTransformer,ct(Meta,CFFunctor,[NewLASTNode,NewRASTNode]),NewASTNode).

transform_term(
		ASTNode,
		PrimitiveGoalTransformer,
		_ContolFlowGoalTransformer,
		NewASTNode
	) :-
	call(PrimitiveGoalTransformer,ASTNode,NewASTNode).
	

/* ************************************************************************** *\
 *                                                                            *
 *            P R E D I C A T E S   T O   D E B U G   T H E   A S T           *
 *                                                                            *
\* ************************************************************************** */

write_ast(ShowPredicates,(Rules,Directives)) :- 
	write_rules(ShowPredicates,Rules),
	write_directives(Directives).	

	
write_directives([Directive|Directives])	:-
	write_ast_node(Directive),nl,
	write_directives(Directives).
write_directives([]).


/**
	Writes the complete AST - including all meta-information - to standard out.

	@signature write_rules(ShowPredicates,AST) 
	@arg(in) ShowPredicates The kind of predicates that should be printed out.
		ShowPredicates has to be <code>built_in</code> or <code>user</code>.
	@arg(in) AST the ast.
*/	
write_rules(_ShowPredicates,[]) :- !. % green cut	
write_rules(ShowPredicates,[pred(Identifier,Clauses,PredicateProperties)|Predicates]) :-	
	(
		(	(ShowPredicates = built_in, Clauses = [])
		;
			(ShowPredicates = user, Clauses = [_|_]/* at least one element */)
		) -> (
			nl,write('[Debug] '),write(Identifier),nl,
			write('[Debug] Properties: '),
			( 	var(PredicateProperties) ->
				write('None')
			;
				write(PredicateProperties)
			),
			nl,
			write_clauses(Clauses)
		) 
	; 
		true
	),
	write_rules(ShowPredicates,Predicates).	
write_rules(_ShowPredicates,AST) :- 
	% IMRPOVE throw exception in case of internal programming errors
	write('[Internal Error:AST:write_rules/2] The AST: '),
	write(AST),
	write(' is invalid.\nl').
	
	
/**
	Writes all information about the clauses of a predicate to standard out.

	@signature write_clauses(Clauses) 
	@arg(in) Clauses The (open) list of clauses defining a predicate.
*/	
write_clauses(Clauses) :- 
	ignore(
		(
		Clauses \= [],
		write('[Debug] Clauses: '),nl,
		write_clauses(1,Clauses)
		)
	).
	
% Implementation of write_clauses/1
write_clauses(_,Clauses) :- 
	var(Clauses)/* Clauses is an open list and a test such as Clauses = [] would
						lead to accidental unification. */,
	!.
write_clauses(Id,[(Clause,ClauseProperties)|Clauses]) :-
	write('[Debug]   '), write(Id), write(':\t'), write_ast_node(Clause),nl,
	write('[Debug]   '), write(Id), write(':\tProperties: '),
	( 	var(ClauseProperties) ->
		write('None')
	;
		write(ClauseProperties)
	), nl,
	NewId is Id + 1,
	write_clauses(NewId, Clauses).	
	
	

/**
	Writes a text representation of a given AST node to standard out.<br />
	The generated representation is meant to facilitate debugging the compiler
	and not as a representation of the AST.
	
	@signature write_ast_node(Term)
	@arg(in) Term A node in the AST representing a term in the source file.
		{@link Parser#:-(module)}
*/
%write_ast_node(v(_Meta,Name)) :- !, write('v('),write(Name),write(')').
write_ast_node(v(_Meta,Name)) :- !, write(Name).
%write_ast_node(av(_Meta,Name)) :- !, write('av('),write(Name),write(')').	
write_ast_node(av(_Meta,Name)) :- !,write(Name).	
write_ast_node(a(_Meta,Atom)) :- !,	write('\''),write(Atom),write('\'').	
write_ast_node(i(_Meta,Atom)) :- !,	write(Atom).	
write_ast_node(r(_Meta,Atom)) :- !,	write(Atom).	
write_ast_node(ct(_Meta,Functor,ClauseList)) :- !,
	write(' \''),write(Functor),write('\''),
	write('[ '),write_term_list(ClauseList),write(' ]').
write_ast_node(X) :- throw(internal_error('[AST] the given term has an unexpected type',X)).
	
	
/**
	Writes a text representation of each AST Node, which represents a term in 
	the source file, to standard out.<br />
	Position information are dropped to make the output easier to read.

	@signature write_term_list(TermList)
	@arg(in) TermList A non-empty list of AST nodes representing source-level
		terms.
*/	
write_term_list([ASTNode|ASTNodes]) :-
	write_ast_node(ASTNode),
	write_term_list_rest(ASTNodes).	
	
write_term_list_rest([]) :- !.
write_term_list_rest([ASTNode|ASTNodes]) :-
	write(','),write_ast_node(ASTNode),
	write_term_list_rest(ASTNodes).