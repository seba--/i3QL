package sae.test.code.innerclass;

/**
 * Author: Ralf Mitschke
 * Created: 08.06.11 14:16
 *
 * Notice, we test on the anonymous inner classes, thus do NOT change the order of the defined methods!
 */
public class MyRootClass
{

    public static void main(String[] args)
    {
        MyRootClass outer = new MyRootClass();

        MyRootClass.InnerPrinterOfX printer = outer.new InnerPrinterOfX();
        printer.displayInt();
        outer.printXWithInnerClass();
        outer.anonymousClassField.displayInt();
        outer.printXWithAnonymousInnerClass();
    }

    private int x = 3;

    private InnerPrinterOfX anonymousClassField = new InnerPrinterOfX()
    {
        class InnerPrinterOfAnonymousClass
        {
            public void displayInt()
            {
                System.out.println("anonymous inner declared" + x);
            }
        }

        public void displayInt()
        {
            new InnerPrinterOfAnonymousClass().displayInt();
        }
    };

    class InnerPrinterOfX
    {
        class InnerPrettyPrinter{
            public String format(int theX)
            {
                return "this is the x:" + theX;
            }
        }

        public void displayInt()
        {
            InnerPrettyPrinter prettyPrinter = new InnerPrettyPrinter();
            System.out.println(prettyPrinter.format(x));
        }
    }


    private void printXWithInnerClass()
    {
        class MyInnerPrinter
        {
            public void displayInt()
            {
                System.out.println(x);
            }
        }
        // MyInnerPrinter can only be instantiated here
        MyInnerPrinter printer = new MyInnerPrinter();
        printer.displayInt();
    }


    private void printXWithAnonymousInnerClass()
    {
        InnerPrinterOfX printer = new InnerPrinterOfX()
        {
            public void displayInt()
            {
                System.out.println("anonymous " + x);
            }
        };
        printer.displayInt();
    }
}
