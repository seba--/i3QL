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


/*	Implementation of the compiler's infrastructure.

	<p><i><b>General Implementation Notes</b><br /> 
	The compiler does not make use of exceptions due to several reasons. First of 
	all, we want to avoid that the compiler immediately terminates when we encounter 
	an error. We want to be able to report multiple errors.<br /> However, if 
	an error is detected, the phase as a whole is expected to fail. The
	compiler will automatically abort after calling the phase.<br />
	Error messages are to be formatted as described here:
	<a href="http://www.gnu.org/prep/standards/html_node/Errors.html">
	http://www.gnu.org/prep/standards/html_node/Errors.html</a>
	</i></p>	

	@author Michael Eichberg
*/
:- module('Compiler',[phase/3,compile/2]).

:- use_module('phase/PLLoad.pl',[pl_load/4]).
:- use_module('phase/PLCheck.pl',[pl_check/4]).
:- use_module('phase/PLNormalize.pl',[pl_normalize/4]).
:- use_module('phase/PLCallGraph.pl',[pl_call_graph/4]).
:- use_module('phase/PLDeterminacyAnalysis.pl',[pl_determinacy_analysis/4]).
:- use_module('phase/PLLastCallOptimizationAnalysis.pl',[pl_last_call_optimization_analysis/4]).
:- use_module('phase/PLNameVariables.pl',[pl_name_variables/4]).
:- use_module('phase/PLToOO.pl',[pl_to_oo/4]).
:- use_module('phase/OOToScala.pl',[oo_to_scala/4]).




/*	The phase/3 predicate identifies the different phases of the compiler and
	also specifies the debug information that will be produced when the respective
	compiler phase is executed.</br>
	The order in which the phases are specified determines the order in which
	the phases are executed; if a phase is to be executed at all.
	<p>
	The entry predicate of each phase has to have the following signature:
	<pre>
	&lt;name_of_the_phase&gt;(
		&lt;debug_flag ()&gt;,
		&lt;input (result of the previous phase; the input of the first phase is
			a list of file names.)&gt;,
		&lt;OutputFolder&gt;(the folder where to store the generated artifacts),
		Output (always a free variable, becomes the input of the next phase))
	</pre>
	</p>

	@signature phase(Name,ExecutionFlag,Debug)
	@param Name is the name of the phase.
	@param ExecutionFlag specifies if the phase should be executed. The phase
			is executed if the value is: "execute". (The value "omit" is expected
			to be used to define that the phase should not be executed.)
	@param Debug is a list that identifies which debug information should be
			produced. E.g., <code>[on_entry,ast]</code><br />
			If the execution flag is not "execute", than the
			value of Debug is meaningless. <br />
			Legal values of the debug argument are defined by the respective phase.
			Most phases define "ast" and "on_entry" to show the program's ast after
			execution of the phase and "on_entry" to signal that the phase is entered.
*/
%%%% 1. LOADING AND CHECKING
phase(pl_load,execute,[on_entry,reading_file,ast]). %Debug flags: ast, on_entry, reading_file
phase(pl_normalize,execute,[on_entry,ast]) :- phase(pl_load,execute,_).
phase(pl_check,execute,[on_entry]) :- phase(pl_normalize,execute,_).
	
%%%% 2. ANALYSES
phase(pl_call_graph,execute,[on_entry,ast]) :- phase(pl_check,execute,_).
phase(pl_determinacy_analysis,execute,[on_entry,result]) :- 
	phase(pl_normalize,execute,_).
%phase(inline,omit,ast) :- phase(pl_normalize_ast,execute,_).
phase(pl_last_call_optimization_analysis,execute,[on_entry,ast(user)]) :-
	phase(pl_determinacy_analysis,execute,_).
	
%%%% 3. TRANSFORMATION TO OO	
phase(pl_name_variables,execute,[on_entry,ast(user)]) :- % Debug flags: on_entry, ast
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


	% 2. find all executable phases
	findall(
		executable_phase(X,D),
		phase(X,execute,D),
		ExecutablePhases),

	% 3. do execute all phases
	execute_phases(ExecutablePhases,AFS,OutputFolder).




/*	execute_phases(Phases,Input,OutputFolder) :- executes all phases unless 
	a phase fails. <br />
	A phase's Input is the Output of the previous phase. OutputFolder defines
	the place where the generated artifacts have to be stored.
*/
execute_phases([],_,_) :- !.
execute_phases([executable_phase(Phase,Debug)|Phases],Input,OutputFolder) :-
	% apply(length([a,b,c]), [L]).
	apply(Phase, [Debug,Input,OutputFolder,Output]),
	execute_phases(Phases,Output,OutputFolder).









