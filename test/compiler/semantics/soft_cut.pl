% Just some code to demonstrate the meaning of soft-cuts
test(a).
test(b).

main_sc :- test(X) *-> write(X). % Solutions: 'a' and 'b' 
main_ife :- test(X) -> write(X). % Only one solution: 'a'
