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

/*
	Generates the code for the SAE program.

	@author Michael Eichberg
*/
:- module('SAEProlog:Compiler:Phase:PLtoOO',[pl_to_java/4]).

:- use_module('../AST.pl').
:- use_module('../Predef.pl').
:- use_module('../Utils.pl').
:- use_module('../Debug.pl').



/**
	Encoding of the SAE Prolog program in Java.

	@param Debug the list of debug information that should be printed.	
*/
pl_to_java(DebugConfig,Program,OutputFolder,Program) :-
	debug_message(DebugConfig,on_entry,write('\n[Debug] Phase: Generate the Java Program________________________________________________\n')),
	( exists_directory(OutputFolder) ; make_directory(OutputFolder) ),
	working_directory(Old,OutputFolder),
	( exists_directory(predicates) ; make_directory(predicates) ),
	working_directory(_,predicates),!,
	(	/* 
			Loop over all user defined predicates and generate the code for each 
		 	predicate; user_predicate is the loop anchor, it succeeds for each 
		 	user_predicate.
		*/
		user_predicate(Program,Predicate),
		predicate_identifier(Predicate,PredicateIdentifier),
		term_to_atom(PredicateIdentifier,PredicateIdentifierAtom),
		debug_message(DebugConfig,processing_predicate,write_atomic_list(['[Debug] Processing Predicate: ',PredicateIdentifierAtom,'\n'])),		
		process_predicate(Predicate),
		fail % i.e., continue with the next predicate
	;	
		true
	),
	working_directory(_,Old).



process_predicate(Predicate) :-
	predicate_identifier(Predicate,Functor/Arity),
	main_template(Functor,Arity,Template,ClauseBodies,Methods), %IMPROVE ClauseBodies and Methods don't quite describe it...

	predicate_clauses(Predicate,Clauses),

	create_terms_for_clause_bodies(Clauses,ClauseBodies),
	Methods = 'public boolean next(){return false;}\n',
	
	% concatenate all parts and write out the Java file
	atomic_list_concat(Template,TheCode),
	atomic_list_concat([Functor,Arity,'.java'],FileName),
	open(FileName,write,Stream),
	write(Stream,TheCode),
	close(Stream).



main_template(Functor,Arity,Template,ClauseBodies,Methods) :-
	call_foreach_i_in_0_to_u(Arity,variable_for_term_i,TermVariables),
	atomic_list_concat(TermVariables,';\n\t',ConcatenatedTermVariables),
	
	call_foreach_i_in_0_to_u(Arity,variable_initialization_for_term_i,TermVariablesInitializations),
	atomic_list_concat(TermVariablesInitializations,';\n\t\t',ConcatenatedTermVariablesInitializations),	
	
	call_foreach_i_in_0_to_u(Arity,args_i_access,Args),
	atomic_list_concat(Args,',',ConcatenatedArgs),
	
	call_foreach_i_in_0_to_u(Arity,constructor_arg_for_term_i,ConstructorArgs),
	atomic_list_concat(ConstructorArgs,',',ConcatenatedConstructorArgs),
	
	% IMPROVE split up the template: Header, Class (PredicateRegistration,MethodBody)
	Template = [
		'package predicates;\n\n',
		'import saere.*;\n',
		'import saere.term.*;\n\n',
		'public final class ',Functor,Arity,' implements Solutions {\n\n',
		'\n\t',ClauseBodies,'\n',
		'\n',
		'	public static void registerWithPredicateRegistry(PredicateRegistry registry) {\n',
		'		registry.registerPredicate(\n',
		'			StringAtom.instancepublic("',Functor , '"),\n',
		'			',Arity,',\n',
		'			new PredicateInstanceFactory() {\n',
		'				@Override\n',
		'				public Solutions createPredicateInstance(Term[] args) {\n',
		'					return new ',Functor,Arity,'(',ConcatenatedArgs,');\n',
		'				}\n',
		'			}\n',
		'		);\n',
		'	}\n\n\n',
		'	',ConcatenatedTermVariables,';\n\n',
		'	public ',Functor,Arity,'(',ConcatenatedConstructorArgs,'){\n',
		'		',ConcatenatedTermVariablesInitializations,';\n',
		'	}\n\n',
		Methods,'\n',	
		'	@Override\n',
		'	public boolean choiceCommitted() {\n',
		'		return false;\n',
		'	}\n',
		'}\n'
	].
	
	
	
args_i_access(N,Args)	 :- atomic_list_concat(['args[',N,']'],Args).

variable_for_term_i(N,TermVariable)	 :- atomic_list_concat(['private final Term t',N],TermVariable).

variable_initialization_for_term_i(N,TermVariableInitialization)	 :- atomic_list_concat(['this.t',N,' = t',N],TermVariableInitialization).

constructor_arg_for_term_i(I,ConstructorArg) :- atom_concat('final Term t',I,ConstructorArg).


create_terms_for_clause_bodies(Clauses,ClauseBodies) :- 
	foreach_clause(Clauses,'SAEProlog:Compiler:Phase:PLtoOO':variable_for_clause_body,ClauseVariables),
	atomic_list_concat(ClauseVariables,';\n\t',ConcatenatedClauseVariables),
	
	foreach_clause(Clauses,'SAEProlog:Compiler:Phase:PLtoOO':variable_initialization_for_clause_body,ClauseVariablesInitialization),
	atomic_list_concat(ClauseVariablesInitialization,'\n\t',ConcatenatedClauseVariablesInitialization),
	
	atomic_list_concat([ConcatenatedClauseVariables,';\n\t',ConcatenatedClauseVariablesInitialization],ClauseBodies).


variable_for_clause_body(N,_Clause,ClauseVariable) :-
	atomic_list_concat(['private static final Term CLAUSE_',N,' = createClause',N,'()'],ClauseVariable).


variable_initialization_for_clause_body(N,_Clause,ClauseVariableInitialization) :-
	atomic_list_concat(['private static Term createClause',N,'(){','}'],ClauseVariableInitialization).


%create_term(ASTNode,TermConstructor) :-
	
	
	