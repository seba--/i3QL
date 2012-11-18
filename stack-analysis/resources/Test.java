public class Test {
	public void testSeq() {
		int a = 1;
		int b = -1;
		int c = a + b;
	}
	
	public void testIf() {
		int a = 1;
		int b = 0;
		
		if(b == a)
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
		
	public void testString() {
	
		String s = "Test1";
		String t = "Test2";
		boolean b;
		
		if(s == t)
			b = true;
		else
			b = false;
	
	}
	
	public void testLong() {
		long l = 5;
		int i = 0;
		
		if(l == i) {
			l = 0;
		} else {
			l = i;
		}		
	}
	
	public int testParam(int a , String b) {
		return a + 5;
	}
	
	public void testInvoke() {
		int a = 0;
		
		if(testParam(a,"Hallo Welt!") > 10)
			a = 1;
	}
	
}