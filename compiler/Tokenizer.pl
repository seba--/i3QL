/*	
tokenize(Chars,Tokens) :-
	tokenize(Chars,1,0,Tokens).
*/
	

/*	Input from the tokenizer.
	s(S) - s is a string atom and the value is S
	i(I) - i is an integer atom and the value is I
	v(V) - is a variable with the name V
*/

stmt(S) --> term([S]),['.'].

% In general, to parse prolog terms we would like to define a rule 
% "term ::= term op term". But, such left-recursive rules are not supported by
% DCGs and hence we had to transform the grammar to get rid of the left recursion.

term([A]) --> atom(A). 
term([A,Op1|T1]) --> atom(A), term1(Op1,T1).

%term([T]) --> ['('],term(T),[')']. % nested terms
%term([T,Op1|T1]) --> ['('],term(T),[')'],term1(Op1,T1).

term([CT]) --> compound_term(CT).
term([CT,Op1|T1]) --> compound_term(CT), term1(Op1,T1).

%term([OpT]) --> term1(Op1,T1),{ OpT =.. [Op1,T1] }.
term([OpT]) --> [s(Op)],term(T),{ OpT =.. [Op,T] }. % prefix operators
term([OpT,Op1|T1]) --> [s(Op)],term(T),term1(Op1,T1),{ OpT =.. [Op,T] }.


term1(op(Op1),T1) --> [s(Op1)],term(T1).



atom(A) --> [s(A)]. % handles vars, string atoms and integer atoms
atom(A) --> [i(A)].
atom(A) --> [v(A)].



compound_term(CT) --> [s(F)],args(As),{ CT =.. [F|As] }.
args([T]) --> ['('],term(T),[')'].%,term_to_term_list(T,Ts).




default_op_table([
		op(1200,xfx,':-'),
		op(1200,xfx,'-->'),
		op(1200,fx,':-'),
		op(1200,fx,'?-'),		

		op(1100,xfy,';'),

		op(1050,xfy,'->'),

%		op(1000,xfy,','), Redefining "and" is NOT supported!

		op(900,xfy,'\\+'),

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
	
		op(400,yfx,*),
		op(400,yfx,/),
		op(400,yfx,mod),

		op(200,fy,'-')		
	]).