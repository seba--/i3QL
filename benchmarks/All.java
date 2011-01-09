import java.io.DataOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Method;
import java.text.DateFormat;
import java.util.Date;

public class All {

	public static void writeToPerformanceLog(String s) {

		try {
			FileOutputStream fos = new FileOutputStream("PerformanceLog.txt", true);
			DataOutputStream dos = new DataOutputStream(fos);
			if (s.equals("\n")) {
				dos.writeChars("\n");
			} else {
				dos.writeChars(DateFormat.getDateTimeInstance().format(new Date()));
				dos.writeChars("   =>   ");
				dos.writeChars(s);
			}
			dos.close();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public static void main(String[] args) throws Throwable {
		writeToPerformanceLog("\n");
		writeToPerformanceLog("\n");
		writeToPerformanceLog("LONG RUN\n");
		for (int i = 0; i < 10; i++) {
			runAllMains();
		}
		writeToPerformanceLog("LONG RUN FINISHED\n");
	}

	public static void runAllMains() throws Throwable {
		System.out.println("RUNS ALL TESTS");
		writeToPerformanceLog("\n");
		writeToPerformanceLog("EXECUTING ALL TESTS - " + System.getProperty("java.vm.name") + "\n");
		long startTime = System.nanoTime();
		runMain(MainEinsteinsRiddle.class);
		runMain(MainQueens.class);
		runMain(MainChatParser.class);
		runMain(MainQSort.class);
		runMain(MainPrimes.class);
		runMain(MainHanoi.class);
		runMain(MainTak.class);
		runMain(MainZebra.class);
		long duration = System.nanoTime() - startTime;
		Double time = new Double(duration / 1000.0 / 1000.0 / 1000.0);
		All.writeToPerformanceLog("EXECUTING ALL TESTS FINISHED IN: " + time + "\n");
	}

	public static void runMain(Class<?> clazz) throws Throwable {
		Method m = clazz.getMethod("main", String[].class);
		m.invoke(null, new Object[] { null });
	}
}
