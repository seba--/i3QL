/*	
tokenize(Chars,Tokens) :-
	tokenize(Chars,1,0,Tokens).
*/
	

/*	Input from the tokenizer.
	s(S) - s is a string atom and the value is S
	i(I) - i is an integer atom and the value is I
	v(V) - is a variable with the name V
*/


%%%% ______________________________ THE PARSER _____________________________%%%%


% In general, to parse prolog terms we would like to define a rule 
% "term ::= term op term". But, such left-recursive rules are not supported by
% DCGs and, hence, we had to transform the grammar to get rid of the left recursion.


program(P,Ts) :-
	default_op_table(Ops),
	program(Ops,P,Ts,[]).
		

program(Ops,[S|RS]) --> 
	stmt(Ops,S),
	program(Ops,RS). % ... add code to update the operator table!		
program(Ops,[S]) --> stmt(Ops,S).



stmt(Ops,T) --> term(Ops,T),['.']. % ... add code to check that the term is valid... it is an atom, a compound term, a directive or a clause definition



primitive_term(Ops,CT) --> compound_term(Ops,CT).
primitive_term(_Ops,V) --> var(V).
primitive_term(_Ops,A) --> atom(A).


primary_term(Ops,PT) --> primitive_term(Ops,PT).
primary_term(Ops,pre(Op,PT)) --> [Op],primitive_term(Ops,PT),{is_prefix(Op,Ops)}.
primary_term(Ops,post(Op,PT)) --> primitive_term(Ops,PT),[Op],{is_postfix(Op,Ops)}.



term(Ops,PT) --> primary_term(Ops,PT).
term(Ops,infix(PT1,Op,T2)) --> primary_term(Ops,PT1),[Op],term(Ops,T2),{is_infix(Op,Ops)}.


comma_delimited_term(Ops,PT) --> primary_term(Ops,PT).
comma_delimited_term(Ops,infix(PT1,Op,T2)) --> primary_term(Ops,PT1),[Op],comma_delimited_term(Ops,T2),{Op \= ',',is_infix(Op,Ops)}.


compound_term(Ops,ct(F,Args)) --> [s(F)],['('],term_list(Ops,Args),[')'].


term_list(Ops,[T]) --> comma_delimited_term(Ops,T).
term_list(Ops,[T|TRs]) --> comma_delimited_term(Ops,T),[','],term_list(Ops,TRs).
term_list(Ops,[T]) --> ['('],term(Ops,T),[')'].
term_list(Ops,[T|TRs]) --> ['('],term(Ops,T),[')'],[','],term_list(Ops,TRs).




atom(s(A)) --> [s(A)]. % handles vars, string atoms and integer atoms
atom(i(I)) --> [i(I)].


var(v(V)) --> [v(V)].






is_infix(Op,Ops) :-
	member(op(_,Mode,Op),Ops),
	(	
		Mode == xfx;
		Mode == xfy;
		Mode == yfx;
		Mode == yfy
	).

is_postfix(Op,Ops) :-
	member(op(_,Mode,Op),Ops),
	(
		Mode == xf;
		Mode == yf
	).

is_prefix(Op,Ops) :-
	member(op(_,Mode,Op),Ops),
	(
		Mode == fx;
		Mode == fy
	).


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