import predicates.father2;
import predicates.half_sister2;
import predicates.male1;
import predicates.parent2;
import predicates.sibling2;
import saere.Goal;
import saere.StringAtom;
import saere.Variable;

public class MainAncestor {

	public static void print(Object o) {
		System.out.print(o);
	}

	public static void print(boolean b) {
		System.out.print(b);
	}

	public static void println(boolean b) {
		System.out.println(b);
	}

	public static void println(Object o) {
		System.out.println(o);
	}

	public static void println() {
		System.out.println();
	}

	public static void main(String[] args) {

		// MALE/1
		{
			Goal goal = new male1(StringAtom.get("Thilo"));
			print("\"true\" expected - result ");
			println(goal.next());
			print("\"false\" expected - result ");
			println(goal.next());
		}

		print("\"false\" expected - result ");
		println(new male1(StringAtom.get("Maria")).next());

		{
			final Variable X = new Variable();
			final Goal solutions = new male1(X);
			while (solutions.next()) {
				print(X.toProlog());
				print(" ");
			}
			println("");
		}

		// FATHER/2
		{
			Goal solutions = new father2(StringAtom.get("Reinhard"), StringAtom.get("Werner"));
			print("\"false\" expected - result ");
			print(solutions.next());
			println();
		}
		{
			Goal solutions = new father2(StringAtom.get("Werner"), StringAtom.get("Michael"));
			print("\"true\" expected - result ");
			println(solutions.next());
			println();
		}

		{
			Variable X = new Variable();
			Variable Y = new Variable();
			Goal solutions = new father2(X, Y);
			while (solutions.next()) {
				print("(" + X.toProlog() + "," + Y.toProlog() + ") ");
			}
			println();
		}

		// PARENT/2
		{
			Variable X = new Variable();
			Variable Y = new Variable();
			Goal solutions = new parent2(X, Y);
			while (solutions.next()) {
				print("(" + X.toProlog() + "," + Y.toProlog() + ") ");
			}
			println();
		}
		{
			Variable X = new Variable();
			Goal solutions = new parent2(X, StringAtom.get("Michael"));
			while (solutions.next()) {
				print(X.toProlog() + " ");
			}
			println();
		}

		// SIBLING/2
		{
			Variable X = new Variable();
			Variable Y = new Variable();
			Goal solutions = new sibling2(X, Y);
			while (solutions.next()) {
				print("(" + X.toProlog() + "," + Y.toProlog() + ") ");
			}
			println();
		}

		// SIBLING/2
		{
			Variable X = new Variable();
			Variable Y = new Variable();
			Goal solutions = new sibling2(X, Y);
			while (solutions.next()) {
				print("(" + X.toProlog() + "," + Y.toProlog() + ") ");
			}
			println();
		}

		// HALF_SISTER/2
		{
			Variable X = new Variable();
			Variable Y = new Variable();
			Goal solutions = new half_sister2(X, Y);
			while (solutions.next()) {
				print("(" + X.toProlog() + "," + Y.toProlog() + ") ");
			}
			println();
		}
	}
}
