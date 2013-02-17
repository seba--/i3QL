/**
 * Created with IntelliJ IDEA.
 * User: Mirko
 * Date: 16.12.12
 * Time: 15:08
 * To change this template use File | Settings | File Templates.
 */
public class TestTransitive {

    private String name = "";
    private Long l = 0l;

    public void testRefComparison() {
        String a = "Hallo Welt!";
        String b = null;

        if (a == b)
            System.out.println(a);
        else
            System.out.println(b);

        b = "Hallo Welt!";

        if (a != b)
            System.out.println(a);
        else
            System.out.println(b);

        Boolean b1 = Boolean.TRUE;
        Boolean b2 = Boolean.FALSE;

        if (b1 == b2)
            b1 = b2;
        else if (b1.equals(b2))
            b2 = b1;
    }
}
