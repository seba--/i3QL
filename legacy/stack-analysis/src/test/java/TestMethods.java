import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class TestMethods {

    private TestMethods(String s) {
        name = s;
    }

    public TestMethods() {
        this("TestMethods");
    }

    public void testSeq() {
        int a = 1;
        int b = -1;
        int c = a + b;
    }

    public void testIf() {
        int a = 1;
        int b = 0;

        if (b == a)
            a = 0;
        else
            a = 1;

        a = 2;
    }

    public void testWhile() {
        int a = 0;

        while (a < 10)
            a++;
    }

    public void testWhile2() {
        int a = 0;

        while (a < 100) {
            if (a == 0)
                a = a + 5;
            else
                a = a + 1;

            while (a < 40)
                a = a + a;

            int b = 0;
            a = b;
            if (a < 50) {
                b = a;
            } else
                while (b < a)
                    b = b + 1;

        }
    }

    public void testString() {

        String s = "Test1";
        String t = "Test2";
        boolean b;

        if (s == t)
            b = true;
        else
            b = false;

    }

    public void testLong() {
        long l = 5;
        int i = 0;

        if (l == i) {
            l = 0;
        } else {
            l = i;
        }
    }

    public int testParam(int a, String b) {
        return a + 5;
    }

    public void testInvoke() {
        int a = 0;

        if (testParam(a, "Hallo Welt!") > 10)
            a = 1;
    }

    public int testExceptions(String s) {
        int a = 0;

        try {
            a = s.length();
        } catch (NullPointerException e) {
            a = -1;
        } finally {
            System.out.println(a);
        }

        return a;

    }

    public void testNull(int b) {
        String a = null;

        if (b == 0)
            a = "Hallo Welt!";

        b = a.length();
    }

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

    private String name = "";

    public void testSelfAssignment(int a) {
        String name = this.name;

        if (a < 5)
            name = name;
        else
            this.name = name;

        String s = name;
        name = s;
    }

    private Long l = 0l;

    public void testSelfComparison(int a) {
        String s = name;

        if (s == name) {
            while (a < 0) {
                if (s == name)
                    System.out.println("OK!");
                s = "";
                a++;
            }
        }

        if (name.equals(name))
            System.out.println("OKOK!");

        if (l.compareTo(l) == -1)
            System.out.println("OKOKOK!");
    }

    public void testArrayToString() {
        int[] a = new int[3];

        System.out.println(a);

        String s = a.toString();

        StringBuilder builder = new StringBuilder();
        builder.append(a).append("test1");
        System.out.println(builder);

        StringBuffer buffer = new StringBuffer();
        buffer.append(a).append("test2");
        System.out.println(buffer);
    }

    public void testBadSQLAccess() {
        Connection con = null;
        int i = 0;
        try {
            PreparedStatement ps = con.prepareStatement("UPDATE EMPLOYEES SET SALARY = ? WHERE ID = ?");
            ps.setInt(0, 2);
            ps.setLong(i, 7000l);
            ps.setString(2, "testID");
            while (i < 10) {
                ps.setBoolean(i, true);
                i++;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void testReturnValue(int a) {
        int b = 0;

        new String("test");

        if (a < 5)
            testExceptions("hallo");
        else
            b = testExceptions("fertig");


        System.out.println(b);
    }

    public static Integer count = 0;

    public static void testSynchronized() {
        synchronized (count) {
            count++;
        }
    }

}