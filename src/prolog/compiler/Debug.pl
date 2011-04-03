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
	Predicates to facilitate tracing and debugging the compiler.

	@author Michael Eichberg
*/
:- module(
	sae_debug,
	[
		debug_message/3,
		
		processing_predicate/1,
		on_entry/1,
		on_exit/1,
		ast/2 %,
		
%		write_list/1
	]
).
:- meta_predicate debug_message(+,1,0).
%:- use_module('../Utils.pl').
/*	debug_message(DebugConfiguration,GenerateMessageGoal,MessageGoal) :-
	is used to print debug messages.<br/>
	The MessagePredicate is called if the GenerateMessageGoal succeeds. <code>
	This predicate is deterministic and always succeeds.
	
	@arg(in) DebugConfiguration is the list of debug information that the compiler
	 		should emit. E.g., [on_entry,ast,on_exit]
	@arg(in) TestGenerateMessageGoal the goal that is called to determine if the
			the debug message should be emitted. The DebugConfiguration
	 		is passed in as the last argument.
	@arg(in) MessageGoal this goal is called if GenerateMessageGoal succeeds.
*/
debug_message(DebugConfiguration,TestGenerateMessageGoal,MessageGoal) :-
	call(TestGenerateMessageGoal,DebugConfiguration),
	call(MessageGoal),
	!.
debug_message(_DebugConfiguration,_TestGenerateMessageGoal,_MessageGoal).



on_entry(DebugConfig) :- memberchk(on_entry,DebugConfig)	.


on_exit(DebugConfig) :- memberchk(on_exit,DebugConfig).	


ast(Type,DebugConfig) :- memberchk(ast(Type),DebugConfig).


processing_predicate(DebugConfig) :- memberchk(processing_predicate,DebugConfig).	


/* TODO test if we can remove this predicate... if not rename!
write_list(X) :- var(X),!.
write_list([]).
write_list([X|Xs]) :- write(X),nl,write_list(Xs).
*/


