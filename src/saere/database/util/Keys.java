package saere.database.util;

import java.util.HashMap;
import java.util.HashSet;

/**
 * Utility class since keys for a prefix are not assigned continuously.
 * 
 * @author David Sullivan
 * @version 0.1, 11/1/2010
 */
public class Keys {
	
	private static final Keys INSTANCE = new Keys();
	private static final String KEY_SEPARATOR = "_"; // NOT part of a prefix!
	
	// Maps a prefix to its set of integers
	private HashMap<String, HashSet<Integer>> keys;
	private HashMap<String, Integer> mins;
	private HashMap<String, Integer> maxs;
	
	private Keys() {
		keys = new HashMap<String, HashSet<Integer>>();
		mins = new HashMap<String, Integer>();
		maxs = new HashMap<String, Integer>();
	}
	
	public static Keys getInstance() {
		return INSTANCE;
	}
	
	public void addKey(String key) {
		try {
			String[] split = key.split(KEY_SEPARATOR);
			addKey(split[0], Integer.parseInt(split[1]));
		} catch (Exception e) {
			System.err.println("Invalid key: " + key);
		}
	}
	
	private void addKey(String prefix, int number) {
		assert !prefix.endsWith("_") : "Prefix mustn't end with _";
		HashSet<Integer> set = keys.get(prefix);
		if (set == null) {
			set = new HashSet<Integer>();
			keys.put(prefix, set);
			
			mins.put(prefix, number);
			maxs.put(prefix, number);
		} else {
			if (number < mins.get(prefix))
				mins.put(prefix, number);
			if (number > maxs.get(prefix))
				maxs.put(prefix, number);
		}
		set.add(number);
	}
	
	public Integer[] getKeys(String prefix) {
		assert !prefix.endsWith("_") : "Prefix mustn't end with _";
		return keys.get(prefix).toArray(new Integer[0]);
	}
	
	public int getHighestKey(String prefix) {
		assert !prefix.endsWith("_") : "Prefix mustn't end with _";
		return maxs.get(prefix);
	}
	
	public int getLowestKey(String prefix) {
		assert !prefix.endsWith("_") : "Prefix mustn't end with _";
		return mins.get(prefix);
	}
	
	public boolean hasKeys(String prefix) {
		assert !prefix.endsWith("_") : "Prefix mustn't end with _";
		return keys.get(prefix) != null;
	}
}
