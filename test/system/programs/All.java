import java.lang.reflect.Method;

public class All {

	public static void main(String[] args) throws Throwable {
		Utils.writeToPerformanceLog("\n");
		Utils.writeToPerformanceLog("\n");
		Utils.writeToPerformanceLog("LONG RUN\n");
		for (int i = 0; i < 10; i++) {
			runAllMains();
		}
		Utils.writeToPerformanceLog("LONG RUN FINISHED\n");
	}

	public static void runAllMains() throws Throwable {
		System.out.println("RUNS ALL TESTS");
		Utils.writeToPerformanceLog("\n");
		Utils.writeToPerformanceLog("EXECUTING ALL TESTS - " + System.getProperty("java.vm.name")
				+ "\n");
		long startTime = System.nanoTime();
		runMain(einsteins_riddle.class);
		runMain(queens.class);
		runMain(chat_parser.class);
		runMain(qsort.class);
		runMain(primes.class);
		runMain(hanoi.class);
		runMain(tak.class);
		runMain(zebra.class);
		runMain(arithmetic.class);
		runMain(nrev.class);
		runMain(crypt.class);

		long duration = System.nanoTime() - startTime;
		Double time = new Double(duration / 1000.0 / 1000.0 / 1000.0);
		Utils.writeToPerformanceLog("EXECUTING ALL TESTS FINISHED IN: " + time + "\n");
	}

	public static void runMain(Class<?> clazz) throws Throwable {
		Method m = clazz.getMethod("main", String[].class);
		m.invoke(null, new Object[] { null });
	}
}
