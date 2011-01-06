import java.io.DataOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Method;
import java.text.DateFormat;
import java.util.Date;

public class Utils {

	public static void writeToPerformanceLog(String s) {

		try {
			FileOutputStream fos = new FileOutputStream("PerformanceLog.txt", true);
			DataOutputStream dos = new DataOutputStream(fos);
			dos.writeChars(DateFormat.getDateTimeInstance().format(new Date()));
			dos.writeChars("   =>   ");
			dos.writeChars(s);
			dos.close();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public static void main(String[] args) throws Throwable {
		System.out.println("RUNS ALL TESTS");
		writeToPerformanceLog("EXECUTING ALL TESTS\n\n");
		runMain(MainEinsteinsRiddle.class);
		runMain(MainQueens.class);
		runMain(MainChatParser.class);
		runMain(MainQSort.class);
		runMain(MainPrimes.class);
		runMain(MainHanoi.class);
		runMain(MainTak.class);

	}

	public static void runMain(Class<?> clazz) throws Throwable {
		Method m = clazz.getMethod("main", String[].class);
		m.invoke(null, new Object[] { null });
	}
}
