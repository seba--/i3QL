package saere.database;

import java.io.FileOutputStream;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.nio.charset.Charset;
import java.util.Iterator;
import java.util.NoSuchElementException;

import saere.Term;

// FIXME A node can store more than one terms. As of now, only the first is accounted for.
public class TrieInspector {
	
	private static final MathContext MC = new MathContext(10, RoundingMode.HALF_UP);
	
	private BigDecimal avgNumChild;
	private BigDecimal fracTerms;
	private double numTries;
	private double numTerms;
	
	public TrieInspector() {
		// TODO Auto-generated constructor stub
	}
	
	public void inspect(Trie trie) {
		avgNumChild = new BigDecimal(0, MC);
		fracTerms = new BigDecimal(0, MC);
		numTries = numTerms = 0;
		
		Iterator<Trie> iterator = new TrieNodeIterator(trie);
		while (iterator.hasNext()) {
			collectNodeStats(iterator.next());
		}
		
		BigDecimal bigNumTries = new BigDecimal(numTries, MC);
		avgNumChild = bigNumTries.divide(avgNumChild, MC);
		fracTerms = bigNumTries.divide(new BigDecimal(numTerms, MC), MC);
	}
	
	public void print(Trie trie, String filename, boolean small) {
		try {
			Charset charset = Charset.forName("ISO-8859-1");
			OutputStream out = new FileOutputStream(filename);
			String shape = small ? "point" : "box";
			out.write(("digraph \"trie\" {\nnode [ shape = " + shape + ", fontname = \"Verdana\" ];").getBytes());
			
			Iterator<Trie> iterator = new TrieNodeIterator(trie);
			while (iterator.hasNext()) {
				trie = iterator.next();
				String trieName = makeTrieName(trie);
				
				// edges to children (of which actually only the one to the first children exists)
				Trie child = trie.getFirstChild();
				while (child != null) {
					out.write((trieName + " -> " + makeTrieName(child) + ";\n").getBytes(charset));
					child = child.getNextSibling();
				}
			}
		
			out.write("}".getBytes());
			out.close();
		} catch (Exception e) {
			System.err.println(e);
		}
	}
	
	private String makeTrieName(Trie trie) {
		Term label = trie.getLabel();
		String labelStr = "";
		if (label != null) {
			labelStr = label.toString();
			labelStr = labelStr.replace('\n', ' ');
			labelStr = labelStr.replace('\r', ' ');
			labelStr = labelStr.replace('"', '\'');
			labelStr = labelStr.replace('\\', '/');
		}
		return "\"" + trie.hashCode() + "/" + labelStr + "\"";
	}
	
	private void collectNodeStats(Trie trie) {
		numTries++;
		if (trie.getTerm() != null)
			numTerms++;
		
		// count children...
		int numChildren = 0;
		Trie child = trie.getFirstChild();
		if (child != null) {
			numChildren++;
			child = child.getNextSibling();
		}
		avgNumChild = avgNumChild.add(new BigDecimal(numChildren, MC), MC);
	}
	
	public void printStats() {
		System.out.println();
		System.out.println("Number of tries:\t\t" + numTries);
		System.out.println("Number of terms:\t\t" + numTerms);
		System.out.println("Fraction of terms:\t\t" + fracTerms + "%");
		System.out.println("Average number of childs:\t" + avgNumChild);
		System.out.println();
	}
	
	// XXX yes, some code overlap with the Trie iterators...
	private class TrieNodeIterator implements Iterator<Trie> {

		Trie start;
		Trie current;
		Trie next;
		
		private TrieNodeIterator(Trie start) {
			this.start = start;
			current = start;
			next = start;
		}
		
		@Override
		public boolean hasNext() {
			return next != null;
		}

		@Override
		public Trie next() {
			if (hasNext()) {
				Trie ret = next;
				next = nextNode();
				return ret;
			} else {
				throw new NoSuchElementException();
			}
		}

		@Override
		public void remove() {
			throw new UnsupportedOperationException();
		}
		
		private Trie nextNode() {
			if (current.getFirstChild() != null) { // if we can go deeper, we do this
				goDeeper();
			} else {
				goRight();
			}
			
			if (current == start) {
				current = null;
				return null;
			} else {
				return current;
			}
		}
		
		private void goDeeper() {
			current = current.getFirstChild();
		}
		
		private void goRight() {
			while (current != start && current.getNextSibling() == null) {
				current = current.getParent();
			}
			
			if (current != start) // treat start as root (and a root has no siblings)
				current = current.getNextSibling();
		}
	}
}
