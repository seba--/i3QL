package collection.incremental;

/**
 * @author Ralf Mitschke
 */
public class IntListElem {
    public int value;
    public IntListElem next;

    public IntListElem(int a) {
        value = a;
    }

    public IntListElem(int a, IntListElem next) {
        value = a;
        this.next = next;
    }


    public IntListElem(IntListElem origin) {
        this.value = origin.value;
        this.next = origin.next;
    }

    public String toString() {
        return value + " -> " + (next == null ? "null" : next.value);
    }
}
