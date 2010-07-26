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
	In general, to parse prolog terms we would like to define a rule 
	"term ::= term op term". But, such left-recursive rules are not supported by
	DCGs and, hence, we had to transform the grammar to get rid of the left recursion.
*/


/**
   Parses a list of tokens (<code>Ts</code>) and generates the AST of the 
   program (<code>P</code>). At the top-level the AST is a list of statements.
*/
program(P,Ts) :-
	default_op_table(Ops),
	program(Ops,P,Ts,[]).
		

program(Ops,[S|RS]) --> 
	stmt(Ops,S),
	program(Ops,RS). % ... add code to update the operator table!		
program(Ops,[S]) --> stmt(Ops,S).



% ... add code to check that the term is valid top-level term.... i.e., 
% it is an atom, a compound term, a directive or a clause definition
stmt(Ops,T) --> term(Ops,T),[o('.',_LN,_CN)],{write(T),nl,!}. 



primitive_term(Ops,CT) --> compound_term(Ops,CT).
primitive_term(_Ops,V) --> var(V).
primitive_term(_Ops,A) --> atom(A).



primary_term(Ops,PT) --> primitive_term(Ops,PT).
primary_term(Ops,pre(Op,PT,LN,CN)) --> [Op],primitive_term(Ops,PT),{is_prefix(Op,Ops,LN,CN)}.
primary_term(Ops,post(Op,PT,LN,CN)) --> primitive_term(Ops,PT),[Op],{is_postfix(Op,Ops,LN,CN)}.



term(Ops,PT) --> primary_term(Ops,PT).
term(Ops,infix(PT1,Op,T2,LN,CN)) --> primary_term(Ops,PT1),[Op],term(Ops,T2),{is_infix(Op,Ops,LN,CN)}.



compound_term(Ops,ct(F,Args,LN,CN)) --> [sa(F,LN,CN)],['('],term_list(Ops,Args),[')'].


term_list(Ops,[T]) --> comma_delimited_term(Ops,T).
term_list(Ops,[T|TRs]) --> comma_delimited_term(Ops,T),[o(',',_LN,_CN)],term_list(Ops,TRs).
term_list(Ops,[T]) --> ['('],term(Ops,T),[')'].
term_list(Ops,[T|TRs]) --> ['('],term(Ops,T),[')'],[o(',',_LN,_CN)],term_list(Ops,TRs).



comma_delimited_term(Ops,PT) --> primary_term(Ops,PT).
comma_delimited_term(Ops,infix(PT1,Op,T2,LN,CN)) --> primary_term(Ops,PT1),[Op],comma_delimited_term(Ops,T2),{Op \= o(',',_,_),is_infix(Op,Ops,LN,CN)}.



atom(o(Op,LN,CN)) --> [o(Op,LN,CN)]. 
atom(sa(A,LN,CN)) --> [sa(A,LN,CN)]. % handles vars, string atoms and integer atoms
atom(i(I,LN,CN)) --> [i(I,LN,CN)].



var(v(V,LN,CN)) --> [v(V,LN,CN)].
var(av(V,LN,CN)) --> [av(V,LN,CN)].



is_infix(o(Op,LN,CN),Ops,LN,CN) :- is_infix(Op,Ops).
is_infix(sa(Op,LN,CN),Ops,LN,CN) :- is_infix(Op,Ops).
is_infix(Op,Ops) :-
	member(op(_,Mode,Op),Ops),
	(	
		Mode = xfx;
		Mode = xfy;
		Mode = yfx;
		Mode = yfy
	), write('is_infix: '),write(Op),nl.

is_postfix(o(Op,LN,CN),Ops,LN,CN) :- is_postfix(Op,Ops).
is_postfix(sa(Op,LN,CN),Ops,LN,CN) :- is_postfix(Op,Ops).
is_postfix(Op,Ops) :-
	member(op(_,Mode,Op),Ops),
	(
		Mode = xf;
		Mode = yf
	),write('is_postfix: '),write(Op),nl.

is_prefix(o(Op,LN,CN),Ops,LN,CN) :- is_prefix(Op,Ops).
is_prefix(sa(Op,LN,CN),Ops,LN,CN) :- is_prefix(Op,Ops).
is_prefix(Op,Ops) :-
	member(op(_,Mode,Op),Ops),
	(
		Mode = fx;
		Mode = fy
	),	write('is_prefix: '),write(Op),nl.



default_op_table([
		op(1200,xfx,':-'),
		op(1200,xfx,'-->'),
		op(1200,fx,':-'),
		op(1200,fx,'?-'),		

		op(1100,xfy,';'),

		op(1050,xfy,'->'),

		op(1000,xfy,','), % Redefining "and" is NOT supported!

		op(900,fy,'\\+'),

		op(700,xfx,'='),
		op(700,xfx,'\\='),
		op(700,xfx,'is'),
		op(700,xfx,'<'),
		op(700,xfx,'>'),
		op(700,xfx,'=<'),
		op(700,xfx,'>='),
		op(700,xfx,'=='),
		op(700,xfx,'\\=='),
		op(700,xfx,'=:='),
		op(700,xfx,'=..'),
		op(700,xfx,'@<'),
		op(700,xfx,'@=<'),
		op(700,xfx,'@>'),
		op(700,xfx,'@>='),		

		op(500,yfx,'+'),
		op(500,yfx,'-'),
	
		op(400,yfx,'*'),
		op(400,yfx,'/'),
		op(400,yfx,'mod'),

		op(200,fy,'-')		
	]).