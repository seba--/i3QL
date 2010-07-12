/*
	Implementation of the compiler's infrastructure.

	<p><i><b>General Implementation Notes</b><br /> 
	We do not use exceptions to avoid that the compiler immediately terminates 
	when we encounter an error. The compiler has to able to report multiple 
	errors. However, if an error is detected, the corresponding predicate is
	expected to fail and the compiler aborts.
	</i></p>	

	@version $Date$ $Rev$
	@author Michael Eichberg
*/
:- module('Compiler',[phase/3,compile/2]).

:- use_module('phase/PLLoad.pl',[pl_load/4]).
:- use_module('phase/PLCheck.pl',[pl_check/4]).
:- use_module('phase/PLNormalize.pl',[pl_normalize/4]).
%:- use_module('phase/CallGraph.pl',[pl_call_graph/4]).
:- use_module('phase/PLDeterminacyAnalysis.pl',[pl_determinacy_analysis/4]).
:- use_module('phase/PLLastCallOptimizationAnalysis.pl',[pl_last_call_optimization_analysis/4]).
:- use_module('phase/PLNameVariables.pl',[pl_name_variables/4]).
:- use_module('phase/PLToOO.pl',[pl_to_oo/4]).
:- use_module('phase/OOToScala.pl',[oo_to_scala/4]).

:- use_module('ast.pl').



/*	<p>
	The phase/3 predicate identifies the different phases of the compiler and
	also specifies the debug information that will be shown by the respective
	compiler phase.</br>
	The order in which the phases are specified determines the order in which
	the phases are executed.
	</p>
	The entry predicate of each phase has to have the following signature:
	<pre>
	&lt;name_of_the_phase&gt;(
		&lt;debug_flag ()&gt;,
		&lt;input (result of the previous phase; the input of the first phase is
			a list of file names.)&gt;,
		OutputFolder (the folder where to store the compiler's results),
		Output (always a free variable, becomes the input of the next phase))
	</pre>

	@signature phase(Name,ExecutionFlag,Debug)
	@param Name is the name of the phase.
	@param ExecutionFlag specifies if the phase should be executed or not. The
			value is either: "execute" or "omit".
	@param Debug is a value that identifies which debug information should be
			shown. If the execution flag is omit (i.e., not "execute"), than the
			value of Debug is meaningless. The structure and legal values of the
			debug argument are defined by the respective phase. Most phases define
			"ast" and "on_entry" to show the program's ast after execution of the
			phase and "on_entry" to signal that the phase is entered.
*/
%%%% 1. LOADING AND CHECKING
phase(pl_load,execute,[on_entry,reading_file,ast]). %Debug flags: ast, on_entry, reading_file
phase(pl_normalize,execute,[on_entry,ast]) :- phase(pl_load,execute,_).
phase(pl_check,execute,[on_entry]) :- phase(pl_normalize,execute,_).
	
%%%% 2. ANALYSES
phase(pl_determinacy_analysis,execute,[on_entry,ast]) :- 
	phase(pl_normalize,execute,_).
%phase(pl_call_graph,omit,[on_entry,ast]) :- % Debug flags: on_entry,ast
%	phase(pl_check,execute,_).
%phase(inline,omit,ast) :- phase(pl_normalize_ast,execute,_).
phase(pl_last_call_optimization_analysis,omit,[on_entry,ast]) :-
	phase(pl_determinacy_analysis,execute,_).
	
%%%% 3. TRANSFORMATION TO OO	
phase(pl_name_variables,execute,[on_entry,ast]) :- % Debug flags: on_entry, ast
	phase(pl_normalize,execute,_).
phase(pl_to_oo,execute,[on_entry,processing_predicate]) :-
	phase(pl_determinacy_analysis,execute,_),
	phase(pl_name_variables,execute,_).
	
%%%% 4. CODE GENERATION
phase(oo_to_scala,omit,[on_entry,processing_predicate]) :-
	phase(pl_to_oo,execute,_).





/* The predicate compile/2 (compile(FilePattern(s),OutputFolder)) compiles all
	files matching the given FilePattern(s). The result of the compilation is
	stored in the OutputFolder. OutputFolder has to end with a "/" otherwise the
	last segment of outputfolder is prepended to the generated files. E.g., if
	OutputFolder is "/User/Michael/SAE" then all files will be stored in the
	folder "/User/Michael" and the names of all generated files will begin with
	SAE.

	@signature compile(FilePatterns,OutputFolder)
	@param FilePatterns is a list of filename patterns.
	@param OutputFolder is the folder where the compiled code will be stored.
*/
compile(FilePattern,OutputFolder) :-
	atom(FilePattern),!,% green cut
	compile([FilePattern],OutputFolder).
compile(FilePatterns,OutputFolder) :-
	FilePatterns = [_],

	% 1. Generate the list of source files
	findall( % We always load all files to do whole program analyses.
		AF,
		(
			member(FP,FilePatterns),
			expand_file_name(FP,EFs), % Handles wildcards and also the "~".
			member(EF,EFs),
			absolute_file_name(EF,AF)
		),
		AFS), % AFS is the list of all absolute file names.


	% 2. Find all executable phases
	findall(
		executable_phase(X,D),
		phase(X,execute,D),
		ExecutablePhases),

	% 3. Do execute all phases
	execute_phases(ExecutablePhases,AFS,OutputFolder).




/*	execute(Phases,Input,OutputFolder) :- executes all phases in order.
	A phase's Input is the Output of the previous phase. OutputFolder defines
	the place where the generate code and other artifacts are stored.
	
	<p>
	After the load phase the input / output is an AST with the following structure:
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
	</p>
*/
execute_phases([],_,_) :- !.
execute_phases([executable_phase(Phase,Debug)|Phases],Input,OutputFolder) :-
	% apply(length([a,b,c]), [L]).
	apply(Phase, [Debug,Input,OutputFolder,Output]),
	execute_phases(Phases,Output,OutputFolder).









