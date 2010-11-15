package saere.database.index;

import java.lang.ref.WeakReference;

import saere.Atom;
import scala.actors.threadpool.Arrays;

public final class ComplexLabel extends Label {

	private Label[] labels;
	
	private ComplexLabel(Label[] labels) {
		this.labels = labels;
	}
	
	@Override
	public int arity() {
		throw new UnsupportedOperationException();
	}

	@Override
	public Atom atom() {
		throw new UnsupportedOperationException();
	}

	@Override
	public int length() {
		return labels.length;
	}

	@Override
	public Label[] labels() {
		return labels;
	}
	
	@Override
	public ComplexLabel split(int index) {
		assert index >= 0 && index < labels.length - 1 : "Illegal split index " + index + "(length " + labels.length + ")";
		
		SimpleLabel[] prefix = new SimpleLabel[index + 1];
		SimpleLabel[] suffix = new SimpleLabel[labels.length - (index + 1)];
		
		System.arraycopy(labels, 0, prefix, 0, prefix.length);
		System.arraycopy(labels, index + 1, suffix, 0, suffix.length);
		
		labels = prefix;
		return new ComplexLabel(suffix);
	}
	
	@Override
	public int hashCode() {
		// Even after splits, the first element should stay the same but 
		// actually a pretty weak hash code...
		return labels[0].hashCode();
	}
	
	@Override
	public boolean equals(Object obj) {
		// ... and an expensive equals too
		if (!(obj instanceof ComplexLabel)) {
			return false;
		}
		
		ComplexLabel other = (ComplexLabel) obj;
		if (labels.length != other.length()) {
			return false;
		}
		
		for (int i = 0; i < labels.length; i++) {
			if (labels[i] != other.labels[i]) {
				return false;
			}
		}
		
		return true;
	}
	
	//@SuppressWarnings("constructorName")
	@SuppressWarnings("all")
	public static ComplexLabel ComplexLabel(Label[] labels) {
		final Label candidate = new ComplexLabel(labels);
		synchronized (CACHE) {
			WeakReference<Label> cached = CACHE.get(candidate);
			if (cached == null) {
				cached = new WeakReference<Label>(candidate);
				CACHE.put(candidate, cached);
			}
			return (ComplexLabel) cached.get();
		}
	}

	@Override
	public int match(Label other) {
		assert other instanceof ComplexLabel : "The specified other label is not a complex label";
		
		if (this == other) {
			return labels.length;
		} else {
			int min = Math.min(labels.length, other.length());
			Label[] others = other.labels();
			int i;
			for (i = 0; i < min; i++) {
				if (!labels[i].sameAs(others[i]))
					break;
			}
			
			return i;
		}
	}
	
	@Override
	public String toString() {
		return Arrays.toString(labels);
	}
}
