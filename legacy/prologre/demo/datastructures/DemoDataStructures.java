package prologre.demo.datastructures;

import java.text.DateFormat;
import java.util.Date;

import prologre.Atom;
import prologre.GoalIterator;
import prologre.Variable;
import prologre.demo.datastructures.Tree_3.Tree_3Term;

public class DemoDataStructures {
    public static void main(String[] args) {
	System.out.println("SAE Demo - " + DateFormat.getDateTimeInstance().format(new Date()));

	{
	    System.out.println("\ntree(A,B,C):");

	    Variable A = new Variable();
	    Variable B = new Variable();
	    Variable C = new Variable();

	    Tree_3Term tree_3Term = new Tree_3.Tree_3Term(A, B, C);

	    GoalIterator bi = Tree_3.getInstance().unify(tree_3Term);
	    int solution = 1;
	    while (bi.next()) {
		System.out.println("Solution: " + (solution++));
		System.out.println("A=" + A.toString());
		System.out.println("B=" + B.toString());
		System.out.println("C=" + C.toString());
	    }
	}

	{
	    System.out.println("\ntree(a2,A,B):");
	    Variable A = new Variable();
	    Variable B = new Variable();
	    Tree_3Term tree_3Term = new Tree_3.Tree_3Term(Atom.Atom("a2"), A, B);

	    GoalIterator bi = Tree_3.getInstance().unify(tree_3Term);
	    while (bi.next()) {
		System.out.println("A=" + A.toString());
		System.out.println("B=" + B.toString());
	    }
	}

	{
	    System.out.println("\ntree(a2,tree(b2,A,null),B) : ");
	    Variable A = new Variable();
	    Variable B = new Variable();
	    Tree_3Term tree_3Term = new Tree_3.Tree_3Term(Atom.Atom("a2"), new Tree_3.Tree_3Term(
		    Atom.Atom("b2"), A, null), B);

	    GoalIterator bi = Tree_3.getInstance().unify(tree_3Term);
	    int solution = 1;
	    while (bi.next()) {
		System.out.println("Solution: " + (solution++));
		System.out.println("A=" + A.toString());
		System.out.println("B=" + B.toString());

	    }
	}

	{
	    System.out.println("\ntree(x,tree(Y,tree(Z,null,null),null),tree(Z,null,null) : ");
	    Variable Y = new Variable();
	    Variable Z = new Variable();
	    Tree_3Term tree_3Term = new Tree_3.Tree_3Term(Atom.Atom("x"), new Tree_3.Tree_3Term(Y,
		    new Tree_3.Tree_3Term(Z, null, null), null), new Tree_3.Tree_3Term(Z, null,
		    null));

	    GoalIterator bi = Tree_3.getInstance().unify(tree_3Term);
	    int solution = 1;
	    while (bi.next()) {
		System.out.println("Solution: " + (solution++));
		System.out.println("Y=" + Y.toString());
		System.out.println("Z=" + Z.toString());

	    }
	}

	{
	    System.out.println("\ntree(a2,A,null) : (no result expected!)");
	    Variable A = new Variable();
	    Tree_3Term tree_3Term = new Tree_3.Tree_3Term(Atom.Atom("a2"), A, null);

	    GoalIterator bi = Tree_3.getInstance().unify(tree_3Term);
	    int solution = 1;
	    while (bi.next()) {
		System.out.println("Solution: " + (solution++));
		System.out.println("A=" + A.toString());
	    }
	}
    }
}
