package saere.database.index.full;

import static saere.database.TestFacts.ALL_TERMS;

import org.junit.Test;

import saere.Term;

public class VariableIteratorTest {
	
	@Test
	public void test1() {
		FullTrie root = new FullTrie();
		FullTrieBuilder builder = new FullTrieBuilder();
		
		for (Term term : ALL_TERMS) {
			builder.insert(term, root);
		}
		
		FullTrie a = root.firstChild.firstChild.firstChild;
		VariableIterator iter = new VariableIterator(a);
		while (iter.hasNext()) {
			FullTrie node = iter.next();
			System.out.println("Node " + node.hashCode() + "/" + node);
		}
	}
}
