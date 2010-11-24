package saere.database.index;

import static saere.database.DatabaseTermFactory.ct;
import static saere.database.DatabaseTermFactory.sa;
import static saere.database.DatabaseTest.*;

import java.io.File;
import java.util.Iterator;
import java.util.LinkedList;

import org.junit.Test;

import saere.Term;
import saere.database.Factbase;
import saere.database.Stopwatch;
import saere.database.index.multi.MultiTrieBuilder;

public final class MultiTrieBuilderTest {
	
	public static void main(String[] args) {
		MultiTrieBuilderTest test = new MultiTrieBuilderTest();
		test.test2();
	}
	
	@Test
	public void test1() {
		TrieBuilder builder = new MultiTrieBuilder(50);
		InnerNode multiRoot = InnerNode.newRoot();
		
		for (Term fact : Facts.ALL) {
			builder.insert(fact, multiRoot);
		}
		
		Iterator<Term> iter = builder.iterator(multiRoot);
		while (iter.hasNext()) {
			System.out.println(iter.next());
		}
		
		System.out.println("DONE");
	}
	
	@Test
	public void test2() {
		
		TrieBuilder builder = new MultiTrieBuilder(50);
		
		//String filename = DATA_PATH + File.separator + "HelloWorld.class";
		String filename = DATA_PATH + File.separator + "opal-0.5.0.jar";
		//String filename = "../test/classfiles/MMC.jar";
		Factbase.getInstance().read(filename);
		
		for (int i = 0; i < 5; i++) {
			System.out.println("\nRun " + (i + 1));
			
			InnerNode multiRoot = InnerNode.newRoot();
			fill(builder, multiRoot);
			
			Stopwatch sw = new Stopwatch();
			Iterator<Term> iter = builder.iterator(multiRoot);
			int c = 0;
			while (iter.hasNext()) {
				iter.next();
				c++;
			}
			sw.printElapsed("Iterating over a trie with " + c + " terms");
			
			
			
			LinkedList<Term> list = new LinkedList<Term>();
			iter = builder.iterator(multiRoot);
			while (iter.hasNext()) {
				list.push(iter.next());
			}
			
			sw = new Stopwatch();
			c = 0;
			iter = list.iterator();
			while (iter.hasNext()) {
				iter.next();
				c++;
			}
			sw.printElapsed("Iterating over a list with " + c + " terms");
		}
	}
	
	public static class Facts {
		
		/** f(a(d,e),b). */
		public static final Term T0 = ct("f", ct("a", sa("d"), sa("e")), sa("b"));
		
		/** f(a,b). */
		public static final Term T1 = ct("f", sa("a"), sa("b"));
		
		/** f(a(d),b). */
		public static final Term T2 = ct("f", ct("a", sa("d")), sa("b"));
		
		/** f(a(d,g),b). */
		public static final Term T3 = ct("f", ct("a", sa("d"), sa("g")), sa("b"));
		
		/** f(x(y)). */
		public static final Term T4 = ct("f", ct("x", sa("y")));
		
		public static final Term[] ALL = { T0, T1, T2, T3, T4 }; 
	}
}
