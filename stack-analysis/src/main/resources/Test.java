public class Test {
	public void test0() {
		int a = 1;
		int b = -1;
		int c = a + b;
	}
	
	public void test1() {
		int a = 1;
		int b = 0;
		
		if(b == a)
			a = 0;
		else
			a = 1;
			
		a = 2;
	}
	
	public void test2() {
		int a = 0;

		while (a < 10)
			a++;
	}
	
	public void test3() {
		int a = 0;
		
		while (a < 100) {
			if(a == 0)
				a = a + 5;
			else
				a = a + 1;
			
			while(a < 40)
				a = a + a;
			
			int b = 0;
			if(a < 50)
				b = 1;
			else
				while(b < a)
					b = b + 1;
		}
	}
	
	public int test4() {
		return 1 + -1;
	}
	
	public void test5() {
		int a = test4() + test4();
	}
	
	public void test6() {
		int a = 0;
		
		while (a < 100) {		
			a = a + 5;			
		}
	}
	
	
}