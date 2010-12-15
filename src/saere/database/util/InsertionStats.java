package saere.database.util;

import saere.Term;
import saere.database.Utils;

public final class InsertionStats {
	
	private static final InsertionStats INSTANCE = new InsertionStats();
	
	private long minInsTime = Long.MAX_VALUE;
	private long maxInsTime = 0;
	private double totalInsTime;
	private long termNum;
	private Term minInsTimeTerm;
	private Term maxInsTimeTerm;
	private long maxNum;
	private long minNum;
	
	private InsertionStats() { /* emtpy */ }
	
	public static InsertionStats getInstance() {
		return INSTANCE;
	}
	
	public long getMinInsTime() {
		return minInsTime;
	}
	public void setMinInsTime(long minInsTime) {
		this.minInsTime = minInsTime;
	}
	public long getMaxInsTime() {
		return maxInsTime;
	}
	public void setMaxInsTime(long maxInsTime) {
		this.maxInsTime = maxInsTime;
	}
	public double getTotalInsTime() {
		return totalInsTime;
	}
	public void increaseAvgInsTime(double avgInsTime) {
		this.totalInsTime += avgInsTime;
	}
	public long getTermNum() {
		return termNum;
	}
	public void increaseTermNum() {
		termNum++;
	}
	public Term getMinInsTimeTerm() {
		return minInsTimeTerm;
	}
	public void setMinInsTimeTerm(Term minInsTimeTerm) {
		this.minInsTimeTerm = minInsTimeTerm;
	}
	public Term getMaxInsTimeTerm() {
		return maxInsTimeTerm;
	}
	public void setMaxInsTimeTerm(Term maxInsTimeTerm) {
		this.maxInsTimeTerm = maxInsTimeTerm;
	}
	
	public void reset() {
		minInsTime = Long.MAX_VALUE;
		maxInsTime = 0;
		totalInsTime = 0;
		termNum = 0;
		minInsTimeTerm = null;
		maxInsTimeTerm = null;
		setMinNum(0);
		setMaxNum(0);
	}
	
	public void print() {
		System.out.println("minInsTime: " + minInsTime);
		System.out.println("minInsTimeTerm: " + Utils.termToString(minInsTimeTerm));
		System.out.println("maxInsTime: " + maxInsTime);
		System.out.println("maxInsTimeTerm: " + Utils.termToString(maxInsTimeTerm));
		System.out.println("avgInsTime: " + (totalInsTime / termNum));
		System.out.println("termNum: " + termNum);
	}

	/**
	 * @param minNum the minNum to set
	 */
	public void setMinNum(long minNum) {
		this.minNum = minNum;
	}

	/**
	 * @return the minNum
	 */
	public long getMinNum() {
		return minNum;
	}

	/**
	 * @param maxNum the maxNum to set
	 */
	public void setMaxNum(long maxNum) {
		this.maxNum = maxNum;
	}

	/**
	 * @return the maxNum
	 */
	public long getMaxNum() {
		return maxNum;
	}
}
