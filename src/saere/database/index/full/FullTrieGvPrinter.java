package saere.database.index.full;

import java.io.FileOutputStream;
import java.nio.charset.Charset;
import java.util.IdentityHashMap;
import java.util.Iterator;

import saere.database.Utils;

// XXX Actually, we must make term nodes unique...
public class FullTrieGvPrinter {
	
	private static final Charset ENCODING = Charset.forName("UTF-8");
	private static final String NEW_LINE = System.getProperty("line.separator");
	
	private FullTrieBuilder builder;
	private FullTrie root;
	private IdentityHashMap<FullTrie, Integer> cache;
	private int number;
	
	public FullTrieGvPrinter(FullTrie root, FullTrieBuilder builder) {
		cache = new IdentityHashMap<FullTrie, Integer>();
		number = 0;
		this.root = root;
		this.builder = builder;
	}
	
	public void print(String filename, Mode mode) {
		FileOutputStream out = null;
		try {
			out = new FileOutputStream(filename);
			out.write(("digraph \"trie\" {\nnode [ shape = " + shape(mode) + ", fontname = \"Verdana\" ];" + NEW_LINE).getBytes(ENCODING));
			
			Iterator<FullTrie> nodeIter = builder.nodeIterator(root);
			while (nodeIter.hasNext()) {
				FullTrie node = nodeIter.next();
				String nodeName = nodeName(node);
				
				// Edges to children
				FullTrie child = node.firstChild;
				while (child != null) {
					out.write((nodeName + " -> " + nodeName(child) + ";" + NEW_LINE).getBytes(ENCODING));
					child = child.nextSibling;
				}
				
				// Edge to term
				if (node.term != null) {
					out.write((nodeName + " -> \"" + escape(Utils.termToString(node.term)) + "\";" + NEW_LINE).getBytes(ENCODING));
				}
			}
			
			out.write(("}" + NEW_LINE).getBytes(ENCODING));
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (out != null)
				try { out.close(); } catch (Exception e) { /* ignored */ }
		}
	}
	
	private String shape(Mode mode) {
		switch (mode) {
			case POINT: return "point";
			case BOX: return "box";
			default: return "box";
		}
	}
	
	private String nodeName(FullTrie trie) {
		return "\"" + getNumber(trie) + "/" + trie + "\"";
	}
	
	private String escape(String s) {
		String r = new String(s);
		r = r.replace('\n', ' ');
		r = r.replace('\r', ' ');
		r = r.replace('"', '\'');
		r = r.replace('\\', '/');
		return r;
	}
	
	private int getNumber(FullTrie trie) {
		if (cache.containsKey(trie)) {
			return cache.get(trie); // XXX integer == null?!
		} else {
			cache.put(trie, ++number);
			return number;
		}
	}
	
	public enum Mode {
		POINT,
		BOX
	}
}
