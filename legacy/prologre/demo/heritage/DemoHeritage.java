package prologre.demo.heritage;

import static prologre.Atom.Atom;

import java.text.DateFormat;
import java.util.Date;

import prologre.Atom;
import prologre.GoalIterator;
import prologre.Variable;

public class DemoHeritage {
    public static void main(String[] args) {
	System.out.println("SAE Demo - " + DateFormat.getDateTimeInstance().format(new Date()));

	{
	    System.out.println("\nIs ... father of ...: ");
	    Atom reinhard = Atom.Atom("Reinhard");
	    Atom alice = Atom.Atom("Alice");
	    GoalIterator bi1 = Father_2.getInstance().unify(reinhard, alice);
	    System.out.println("Reinhard is father of Alice: " + bi1.next());
	    GoalIterator bi2 = Father_2.getInstance().unify(alice, reinhard);
	    System.out.println("Alice is father of Reinhard: " + bi2.next());
	}

	{
	    System.out.println("\nAll father relations: ");
	    Variable X = new Variable();
	    Variable Y = new Variable();
	    GoalIterator bi = Father_2.getInstance().unify(X, Y);
	    while (bi.next()) {
		System.out.println(X.getBinding().toString() + " is father of "
			+ Y.getBinding().toString());
	    }
	}
	{
	    System.out.println("\nFather of Alice: ");
	    Variable X = new Variable();
	    GoalIterator bi = Father_2.getInstance().unify(X, Atom("Alice"));
	    while (bi.next()) {
		System.out.println(X.getBinding().toString());
	    }
	}
	{
	    System.out.println("\nReinhard is father of: ");
	    Variable X = new Variable();
	    GoalIterator bi = Father_2.getInstance().unify(Atom("Reinhard"), X);
	    while (bi.next()) {
		System.out.println(X.getBinding().toString());
	    }
	}
	{
	    System.out.println("\nAll parent relations: ");
	    Variable X = new Variable();
	    Variable Y = new Variable();
	    GoalIterator bi = Parent_2.getInstance().unify(X, Y);
	    while (bi.next()) {
		System.out.println(X.getBinding().toString() + " is parent of "
			+ Y.getBinding().toString());

	    }
	}
	{
	    System.out.println("\nAll grandparent relations: ");
	    Variable X = new Variable();
	    Variable Y = new Variable();
	    GoalIterator bi = Grandparent_2.getInstance().unify(X, Y);
	    while (bi.next()) {
		System.out.println(X.getBinding().toString() + " is grandparent of "
			+ Y.getBinding().toString());

	    }
	}
	{
	    System.out.println("\nAll sibling relations: ");
	    Variable X = new Variable();
	    Variable Y = new Variable();
	    GoalIterator bi = Sibling_2.getInstance().unify(X, Y);
	    while (bi.next()) {
		System.out.println(X.getBinding().toString() + " is sibling of "
			+ Y.getBinding().toString());

	    }
	}
	{
	    System.out.println("\nAll ancestor relations: ");
	    Variable X = new Variable();
	    Variable Y = new Variable();
	    GoalIterator bi = Ancestor_2.getInstance().unify(X, Y);
	    while (bi.next()) {
		System.out.println(X.getBinding().toString() + " is ancestor of "
			+ Y.getBinding().toString());

	    }
	}
    }
}
