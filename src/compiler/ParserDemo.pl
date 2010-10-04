/*
The following code demonstrates how to parse Prolog terms using 
Definite Clause Grammars (DCGs). The operator table is an argument of 
each rule and the default operator table is defined by default_op_table.
*/


/* 1. Step - NAIVE GRAMMAR - NOT WORKING... (left recursive)
term --> prefix_op, term.
term --> [a(_)]
term --> term, infix_op, term.
term --> term, postfix_op.
prefix_op --> [...].
postfix_op --> [...].
infix_op --> [...].
*/


/* 2. Step - AFTER LEFT RECURSION REMOVAL...
term_r --> infix_op, term, term_r.
term_r --> postfix_op, term_r.
term_r --> [].

term --> prefix_op, term, term_r.
term --> [a(_)], term_r.

prefix_op --> [...].
postfix_op --> [...].
infix_op --> [...].
*/

seq(Ops,[FT,ST]) --> term(Ops,999,FT,_TP), [,], term(Ops,999,ST,_TP).

/* Example usage: 
 	default_op_table(Ops), term(Ops,T,TP,['-','-',t(x),'yDEL','*',t(y)],[]),write(T).

	@signature (x_)term(Ops,T,TP)
 	@arg(in) Ops table of operators
	@arg(in) MaxPriority the maximum priority of a term
	@arg(in) LeftTerm the left term
	@arg(in) LeftTermPriority
	@arg(out) Term the accepted term
	@arg(out) TermPriority the priority of the accepted term	
*/
term_r(Ops,MaxPriority,LeftTerm,LeftTermPriority,Term,TermPriority) --> 
	infix_op(Ops,op(Priority,Associativity,Op)),
	{ MaxPriority >= Priority },  
	term(Ops,MaxPriority,RightTerm,RightTermPriority),
	{
		(
			(Associativity = xfx, Priority > LeftTermPriority, Priority > RightTermPriority) 
		; 
			(Associativity = yfx, Priority >= LeftTermPriority, Priority > RightTermPriority)
		; 
			(Associativity = xfy, Priority > LeftTermPriority, Priority >= RightTermPriority)
		),
		IntermediateTerm =.. [Op,LeftTerm,RightTerm]
	},
	term_r(Ops,MaxPriority,IntermediateTerm,Priority,Term,TermPriority).
term_r(Ops,MaxPriority,LeftTerm,LeftTermPriority,Term,TermPriority) --> 
	postfix_op(Ops,op(Priority,Associativity,Op)), 
	{ MaxPriority >= Priority }, 
	{
		(
			( Associativity = xf, Priority > LeftTermPriority ) 
		; 
 			( Associativity = yf, Priority >= LeftTermPriority )
		),
		IntermediateTerm =.. [Op,LeftTerm]
	},
	term_r(Ops,MaxPriority,IntermediateTerm,Priority,Term,TermPriority).
term_r(_Ops,_MaxPriority,T,TP,T,TP) --> [].

term(Ops,MaxPriority,Term,TermPriority) --> 
	prefix_op(Ops,op(Priority,Associativity,Op)),
	{ MaxPriority >= Priority }, 
	term(Ops,MaxPriority,RightTerm,RightTermPriority), 
	{
		(
			(Associativity = fx, Priority > RightTermPriority) 
		; 
			(Associativity = fy, Priority >= RightTermPriority)
		),
		IntermediateTerm =.. [Op,RightTerm]
	},
	term_r(Ops,MaxPriority,IntermediateTerm,Priority,Term,TermPriority).
term(Ops,MaxPriority,Term,TermPriority) --> 
	[Terminal],{Terminal=t(_)}, 
	term_r(Ops,MaxPriority,Terminal,0,Term,TermPriority).

prefix_op(Ops,Op) --> [X],{Op=op(_,C,X),member(Op,Ops),(C=fx;C=fy)}.
postfix_op(Ops,Op) --> [X],{Op=op(_,C,X),member(Op,Ops),(C=xf;C=yf)}.
infix_op(Ops,Op) --> [X],{Op=op(_,C,X),member(Op,Ops),(C=xfx;C=yfx;C=xfy)}.


default_op_table([
		op(1200,xfx,':-'),
		op(1200,xfx,'-->'),
		op(1200,fx,':-'),
		op(1200,fx,'?-'),		

		op(1100,xfy,';'),
		op(1100,xfy,'|'), % also defined by SWI Prolog

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
		op(700,xfx,'=:='),
		op(700,xfx,'=\\='),	
		op(700,xfx,'=..'),
		op(700,xfx,'=='),
		op(700,xfx,'\\=='),
		op(700,xfx,'@<'),
		op(700,xfx,'@=<'),
		op(700,xfx,'@>'),
		op(700,xfx,'@>='),		

		op(500,yfx,'\\/'),
		op(500,yfx,'/\\'),	
		op(500,yfx,'+'),
		op(500,yfx,'-'),

		op(400,yf,'yDEL'),	%%%%% JUST FOR TESTING PURPOSES!!!!!		

		op(400,yfx,'*'),
		op(400,yfx,'/'),
		op(400,yfx,'//'), % X // Y Division mit Ganzzahlergebnis
		op(400,yfx,'mod'),
		op(400,yfx,'<<'),
		op(400,yfx,'>>'),

		op(300,xf,'xDEL'),	%%%%% JUST FOR TESTING PURPOSES!!!!!				

		op(200,xfx,'**'),		
		op(200,xfy,'^'),				
		op(200,fy,'-'),	
		op(200,fy,'\\') % bitwise complement	

	]).