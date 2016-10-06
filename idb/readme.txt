Syntactical Oddities:

toString(): Never use this method without parenthesis.
The execution order of AND, OR can differ from the order in the code.
LMS can not catch this application and then the generated code will
contain toString representations of your programs AST

Optimizations:
Currently Existential queries produce large ASTs if used in the middle of boolean conditions.
Use them at the end to obtain better queries.