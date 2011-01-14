/* License (BSD Style License):
 * Copyright (c) 2010
 * Department of Computer Science
 * Technische Universität Darmstadt
 * All rights reserved.
 *
 *  Redistribution and use in source and binary forms, with or without
 *  modification, are permitted provided that the following conditions are met:
 *
 *  - Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 *  - Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *  - Neither the name of the Software Technology Group or Technische 
 *    Universität Darmstadt nor the names of its contributors may be used to 
 *    endorse or promote products derived from this software without specific 
 *    prior written permission.
 *
 *  THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 *  AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 *  IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 *  ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
 *  LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 *  CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 *  SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 *  INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 *  CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 *  ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 *  POSSIBILITY OF SUCH DAMAGE.
 */
package saere.utils;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Method;
import java.text.DateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import org.odftoolkit.odfdom.type.Color;
import org.odftoolkit.simple.SpreadsheetDocument;
import org.odftoolkit.simple.table.Cell;
import org.odftoolkit.simple.table.Row;
import org.odftoolkit.simple.table.Table;

public class Performance {

	public static void main(String[] args) throws Throwable {
		if (args.length == 0) {
			System.err.println("The program that should be measured needs to be specified.");
			System.exit(1);
		}

		String className = "harness." + args[0];
		System.out.print("Loading: " + className);
		if (args.length == 1) {
			System.out.print(".");
			Method method = Class.forName(className).getMethod("measure", new Class<?>[] {});
			System.out.println("Starting.");
			method.invoke(null, new Object[] {});
			System.out.println("Finished.");
		} else {
			String[] prg_args = Arrays.copyOfRange(args, 1, args.length);
			System.out.print(": " + Arrays.toString(prg_args) + ".");
			Method method = Class.forName(className).getMethod("measure",
					new Class<?>[] { String[].class });
			System.out.println("Starting.");
			method.invoke(null, new Object[] { prg_args });
			System.out.println("Finished.");
		}
	}

	public static void writeToPerformanceLog(String benchmark, long timeInNanoSecs) {
		writeToPerformanceLog(benchmark, 1, timeInNanoSecs);
	}

	public static void writeToPerformanceLog(String benchmark, int run, long timeInNanoSecs) {
		try {
			SpreadsheetDocument doc;
			File file = new File("PerformanceLog.ods");
			if (file.exists())
				doc = SpreadsheetDocument.loadDocument(file);
			else
				doc = SpreadsheetDocument.newSpreadsheetDocument();

			Table table = doc.getTableByName(benchmark);
			if (table == null) {
				table = Table.newTable(doc);
				table.setTableName(benchmark);
			}
			Row row = table.appendRow();

			Calendar date = Calendar.getInstance();

			Cell dateCell = row.getCellByIndex(0);
			dateCell.setValueType("date");
			dateCell.setDateValue(new GregorianCalendar(date.get(Calendar.YEAR), date
					.get(Calendar.MONTH), date.get(Calendar.DAY_OF_MONTH)));
			dateCell.setFormatString("yyyy-MM-dd");

			Cell timeCell = row.getCellByIndex(1);
			timeCell.setValueType("time");
			timeCell.setTimeValue(date);

			Cell runCell = row.getCellByIndex(2);
			runCell.setStringValue(String.valueOf(run));

			if (timeInNanoSecs >= 0) {

				Cell performanceCell = row.getCellByIndex(3);
				Double timeInSecs = new Double(timeInNanoSecs / 1000.0d / 1000.0d / 1000.0d);
				performanceCell.setDoubleValue(timeInSecs);
				performanceCell.setFormatString("0.0000");
			} else {
				Cell performanceCell = row.getCellByIndex(3);
				performanceCell.setStringValue("measurement failed");
				performanceCell.setCellBackgroundColor(Color.RED);
			}

			doc.save(file);
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}
	}

	@Deprecated
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
