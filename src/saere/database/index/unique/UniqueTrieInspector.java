package saere.database.index.unique;

import java.util.Iterator;

import saere.database.Stopwatch;
import saere.database.profiling.PostgreSQL;

public class UniqueTrieInspector {

	private static final PostgreSQL DB = new PostgreSQL();
	
	public UniqueTrieInspector() {
		DB.connect();
	}
	
	public void inspect(UniqueTrie root, UniqueTrieBuilder builder) {
		System.out.println("Deleting old results from DB...");
		DB.modify("DELETE FROM trie_nodes");

		Iterator<UniqueTrie> iter = builder.nodeIterator(root);
		Stopwatch sw = new Stopwatch();
		while (iter.hasNext()) {
			collectData(iter.next());
		}
		sw.printElapsed("Inspecting the trie (and filling the database)");
	}
	
	private void collectData(UniqueTrie node) {
		
		// Compose the various values...
		String id = String.valueOf(node.hashCode());
		String label = node.label != null ? node.label.toString() : "";
		String num_children = String.valueOf(node.childrenNumber);
		
		String num_terms = "1"; // ALWAYS
		
		String sibling_index = String.valueOf(node.siblingIndex);
		String uses_map = node.childrenMap != null ? "TRUE" : "FALSE";
		String parent_id = node.parent != null ? String.valueOf(node.parent.hashCode()) : "-1";
		String first_child_id = node.firstChild != null ? String.valueOf(node.firstChild.hashCode()) : "-1";
		String next_sibling_id = node.nextSibling != null ? String.valueOf(node.nextSibling.hashCode()) : "-1";
		
		DB.insert("trie_nodes",
			new String[] {"id", "label", "num_children", "num_terms", "sibling_index", "uses_map", "parent_id", "first_child_id", "next_sibling_id"},
			new String[] {id, label, num_children, num_terms, sibling_index, uses_map, parent_id, first_child_id, next_sibling_id}
		);
	}
	
	@Override
	protected void finalize() throws Throwable {
		DB.disconnect();
	}
}
