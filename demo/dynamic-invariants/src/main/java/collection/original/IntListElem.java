package collection.original;

/**
 * @author Ralf Mitschke
 */
public class IntListElem {
    public int value;
    public IntListElem next;

    IntListElem(int a) {
        value = a;
    }

    public String toString() {
        return value + "";
    }
}
