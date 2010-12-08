package saere.database.util;

import java.io.FileOutputStream;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.nio.charset.Charset;
import java.util.Iterator;

import saere.database.Utils;
import saere.database.index.TermList;
import saere.database.index.Trie;
import saere.database.index.TrieBuilder;

/**
 * This class gathers some information about a {@link Trie}.
 * 
 * @author David Sullivan
 * @version 0.2, 9/21/2010
 */
@Deprecated
public class TrieInspector {
	
	private static final MathContext MC = new MathContext(10, RoundingMode.HALF_UP);
	
	// we actually don't need BigDecimals...
	private BigDecimal avgNumChild;
	private BigDecimal fracTerms;
	private double numLists;
	private double numTries;
	private double numTerms;
	
	public  void inspect(Trie trie, TrieBuilder builder) {
		avgNumChild = new BigDecimal(0, MC);
		fracTerms = new BigDecimal(0, MC);
		numTries = numTerms = numLists = 0;
		
		Iterator<Trie> iterator = builder.nodeIterator(trie);
		while (iterator.hasNext()) {
			collectNodeStats(iterator.next());
		}
		
		BigDecimal bigNumTries = new BigDecimal(numTries, MC);
		avgNumChild = bigNumTries.divide(avgNumChild, MC);
		fracTerms = new BigDecimal(numTerms, MC).divide(bigNumTries, MC).multiply(new BigDecimal(100, MC));
	}
	
	public  void print(Trie trie, TrieBuilder builder, String filename, boolean small) {
		try {
			Charset charset = Charset.forName("ISO-8859-1");
			OutputStream out = new FileOutputStream(filename);
			String shape = small ? "point" : "box";
			out.write(("digraph \"trie\" {\nnode [ shape = " + shape + ", fontname = \"Verdana\" ];\n").getBytes());
			
			Iterator<Trie> iterator = builder.nodeIterator(trie);
			while (iterator.hasNext()) {
				trie = iterator.next();
				String trieName = makeTrieName(trie, true);
				
				// edges to children (of which actually only the one to the first children exists)
				Trie child = trie.getFirstChild();
				while (child != null) {
					out.write((trieName + " -> " + makeTrieName(child, true) + ";\n").getBytes(charset));
					child = child.getNextSibling();
				}
				
				// edges to terms
				TermList list = trie.getTerms();
				while (list != null) {
					out.write((trieName + " -> \"" + /*shorten(*/escape(Utils.termToString(list.term()))/*, 16)*/ + "\";\n").getBytes(charset));
					list = list.next();
				}
			}
		
			out.write("}".getBytes());
			out.close();
		} catch (Exception e) {
			System.err.println(e);
		}
	}
	
	private  String makeTrieName(Trie trie, boolean longName) {
		String name = null;
		
		if (longName) {
			return "\"" + trie.hashCode() + "/" + trie.toString() + "\"";
		} else {
			name = trie.toString();
		}
		
		return "\"" + trie.hashCode() + "/" + name + "\"";
	}
	
	private String escape(String s) {
		String r = new String(s);
		r = r.replace('\n', ' ');
		r = r.replace('\r', ' ');
		r = r.replace('"', '\'');
		r = r.replace('\\', '/');
		return r;
	}
	
	private  void collectNodeStats(Trie trie) {
		numTries++;
		
		// count terms...
		TermList list = trie.getTerms();
		if (list != null) {
			numLists++;
			while (list != null) {
				numTerms++;
				list = list.next();
			}
		}
		
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
		System.out.println("Number of term lists:\t\t" + numLists);
		System.out.println("Number of terms:\t\t" + numTerms);
		System.out.println("Average term list length:\t" + (numTerms / numLists));
		System.out.println("Number of collisions:\t\t" + (numTerms - numLists));
		System.out.println("Fraction of terms:\t\t" + fracTerms + "%");
		System.out.println("Average number of childs:\t" + avgNumChild);
		System.out.println();
	}
}
