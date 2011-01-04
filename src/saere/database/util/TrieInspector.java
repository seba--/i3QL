package saere.database.util;

import java.util.Iterator;

import saere.database.index.TermList;
import saere.database.index.Trie;
import saere.database.index.TrieBuilder;

/**
 * This class gathers some information about a {@link Trie}.
 * 
 * @author David Sullivan
 * @version 0.3, 12/10/2010
 */
public class TrieInspector {
	
	// Average and total children number(NOT counting leafs) as well as nodes with only a single child
	private double avgChildNum;
	private double totalChildNum;
	private double nodeNumOneChild;
	
	// Average and total term number (only for leafs)
	private double avgTermNum;
	private double totalTermNum;
	
	// Total number of (all) nodes, inner nodes, inner hash nodes, and leafs
	private double totalNodes;
	private double totalInnerNodes;
	private double totalHashNodes;
	private double totalLeafs;
	
	public void inspect(Trie trie, TrieBuilder builder) {
		
		// Reset all
		avgChildNum = totalChildNum = avgTermNum = totalTermNum = totalNodes = totalInnerNodes = totalHashNodes = totalLeafs = nodeNumOneChild = 0;
		Iterator<Trie> iterator = builder.nodeIterator(trie);
		while (iterator.hasNext()) {
			collectNodeStats(iterator.next());
		}
		
		Iterator<saere.Term> termIter = builder.iterator(trie);
		int termNum = 0;
		while (termIter.hasNext()) {
			termIter.next();
			termNum++;
		}
		
		System.out.println(termNum);
		
		printStats();
	}
	
	private void collectNodeStats(Trie trie) {
		totalNodes++;
		
		// Count terms...
		if (trie.isSingleStorageLeaf()) {
			totalLeafs++;
			totalTermNum++;
		} else if (trie.isMultiStorageLeaf()) {
			totalLeafs++;
			
			int listLength = 0;
			TermList list = trie.getTerms();
			while (list != null) {
				listLength++;
				list = list.next();
			}
			
			totalTermNum += listLength;
		} else {
			// Inner node or inner hash node (or root which is counted either as inner node or inner hash node)
			
			// Count children
			int childNum = 0;
			if (trie.getMap() != null) {
				totalHashNodes++;
				childNum = trie.getMap().size();
			} else {
				totalInnerNodes++;
				Trie child = trie.getFirstChild();
				while (child != null) {
					childNum++;
					child = child.getNextSibling();
				}
			}
			
			if (childNum == 1)
				nodeNumOneChild++;
			
			totalChildNum += childNum;
		}
	}
	
	private void printStats() {
		
		// Compute averages now
		avgChildNum = totalChildNum / (totalInnerNodes + totalHashNodes);
		avgTermNum = totalTermNum / totalLeafs;
		
		// Some numbers are rendundant but nice to check...
		System.out.println("Average children number for non-leafs: " + avgChildNum);
		System.out.println("Total children number for non-leafs: " + totalChildNum);
		System.out.println("Number of non-leaf nodes with a single child: " + nodeNumOneChild);
		System.out.println("Average number of terms stored at leafs: " + avgTermNum);
		System.out.println("Total number of terms stored at leafs:" + totalTermNum);
		System.out.println("Total number of nodes: " + totalNodes);
		System.out.println("Total number of inner nodes: " + totalInnerNodes);
		System.out.println("Total number of inner hash nodes: " + totalHashNodes);
		System.out.println("Total number of leafs: " + totalLeafs);
	}
}
