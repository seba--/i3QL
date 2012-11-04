public class Test {
	public void test0() {
		int a = 1;
		int b = -1;
		int c = a + b;
	}
	
	public void test1() {
		int a = 1;
		int b = -1;

		if(b == a)
			b = a + b;
		else
			a = a + b;
	}
	
	public void test2() {
		int a = 0;

		while (a < 10)
			a++;
	}
	
	public int test4() {
		return 1 + -1;
	}
	
	public void test5() {
		int a = test4() + test4();
	}
	
	
}