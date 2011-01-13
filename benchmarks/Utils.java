import java.io.DataOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DateFormat;
import java.util.Date;

public class Utils {

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
}
