package saere.database.profiling;

import java.io.FileOutputStream;
import java.nio.charset.Charset;
import java.util.IdentityHashMap;
import java.util.Iterator;

import saere.database.Utils;
import saere.database.index.TermList;
import saere.database.index.Trie;
import saere.database.index.TrieBuilder;

/**
 * A printer for creating Graphviz files for {@link Trie}s.
 * 
 * @author David Sullivan
 * @version 0.1, 11/11/2010
 */
public final class TriePrinter {
	
	private static final Charset ENCODING = Charset.forName("UTF-8");
	private static final String NEW_LINE = System.getProperty("line.separator");
	
	private TriePrinter() { /* empty */ }
	
	public static void print(Trie root, TrieBuilder builder, String filename, Mode mode) {
		FileOutputStream out = null;
		try {
			out = new FileOutputStream(filename);
			out.write(("digraph \"trie\" {\nnode [ shape = " + shape(mode) + ", fontname = \"Verdana\" ];" + NEW_LINE).getBytes(ENCODING));
			
			Iterator<Trie> nodeIter = builder.nodeIterator(root);
			IdentityHashMap<Trie, Integer> cache = new IdentityHashMap<Trie, Integer>();
			int number = 0;
			while (nodeIter.hasNext()) {
				Trie node = nodeIter.next();
				number = getId(node, cache, number);
				String nodeName = nodeName(node, number);
				
				// Edges to children
				Trie child = node.getFirstChild();
				while (child != null) {
					number = getId(child, cache, number);
					out.write((nodeName + " -> " + nodeName(child, number) + ";" + NEW_LINE).getBytes(ENCODING));
					child = child.getNextSibling();
				}
				
				// Edge(s) to term(s)
				if (node.isSingleStorageLeaf()) {
					TermList terms = node.getTerms();
					while (terms != null) {
						number = getId(node, cache, number);
						out.write((nodeName + " -> \"" + escape(Utils.termToString(terms.term())) + "\";" + NEW_LINE).getBytes(ENCODING));
						terms = terms.next();
					}
				}
			}
			
			out.write(("}" + NEW_LINE).getBytes(ENCODING));
		} catch (Exception e) {
			System.err.println("Unable to print trie");
			e.printStackTrace();
		} finally {
			if (out != null)
				try { out.close(); } catch (Exception e) { /* ignored */ }
		}
	}
	
	private static String shape(Mode mode) {
		switch (mode) {
			case POINT: return "point";
			case BOX: return "box";
			default: return "box";
		}
	}
	
	private static String nodeName(Trie trie, int trieId) {
		String s = trie.toString();
		return "\"" + trieId + ":" + s.substring(s.indexOf(':') + 1) + "\"";
	}
	
	private static int getId(Trie trie, IdentityHashMap<Trie, Integer> cache, int number) {
		return trie.hashCode();
		/*
		if (cache.containsKey(trie)) {
			return cache.get(trie);
		} else {
			cache.put(trie, number + 1);
			return number + 1;
		}*/
	}
	
	private static String escape(String s) {
		String r = new String(s);
		r = r.replace('\n', ' ');
		r = r.replace('\r', ' ');
		r = r.replace('"', '\'');
		r = r.replace('\\', '/');
		return r;
	}
	
	/**
	 * The vertex mode of the GV-file.
	 */
	public enum Mode {
		
		/** Displays vertexes as simple black dot. */
		POINT,
		
		/** Displays vertexes as a box with text. */
		BOX
	}
}
