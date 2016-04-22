package sae.test.code.innerclass;

/**
 * Author: Ralf Mitschke
 * Created: 08.06.11 14:16
  * Notice, we test on the anonymous inner classes, thus do NOT change the order of the defined methods!
 */
public class MyRootClass
{

    public static void main(String[] args)
    {
        MyRootClass outer = new MyRootClass();

        MyRootClass.InnerPrinterOfX printer = outer.new InnerPrinterOfX();
        printer.displayInt();
        printer.displayIntReallyNice();
        outer.printXWithInnerClass();
        outer.anonymousClassField.displayInt();
        outer.anonymousClassField.displayIntReallyNice();
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

        public void displayIntReallyNice()
        {
            System.out.println(
                    new Formater()
                    {
                        public String format(final int theX)
                        {

                            InnerPrinterOfX printer = new InnerPrinterOfX()
                            {
                                public void displayInt()
                                {
                                    this.prettyPrinter.format(theX);
                                }
                            };
                            String prefix  = "this is the x in a really nice anonymous presentation: ";

                            printer.prettyPrinter = this;
                            return prefix + theX;
                        }
                    }.format(x)
            );
        }
    };

    interface Formater
    {
        public String format(int theX);
    }

    class InnerPrinterOfX
    {
        public Formater prettyPrinter;

        public InnerPrinterOfX()
        {
            this.prettyPrinter = new Formater()
            {
                public String format(int theX)
                {
                    return "this is the x in a really nice presentation:" + theX;
                }
            };
        }

        class InnerPrettyPrinter implements Formater
        {
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

        public void displayIntReallyNice()
        {
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
