package saere.database.profiling;


public class OrderTuple implements Comparable<OrderTuple> {
	private final int oldIndex;
	private final int frequency;
	
	public OrderTuple(int oldIndex, int frequency) {
		this.oldIndex = oldIndex;
		this.frequency = frequency;
	}
	
	public int oldIndex() {
		return oldIndex;
	}
	
	@Override
	public int compareTo(OrderTuple o) {
		
		// This enables reverse ordering as the largest element will be the first
		if (frequency > o.frequency) {
			return -1;
		} else if (frequency < o.frequency) {
			return 1;
		} else {
			return 0;
		}
	}
	
	@Override
	public String toString() {
		return oldIndex + "(" + frequency + ")";
	}
}
