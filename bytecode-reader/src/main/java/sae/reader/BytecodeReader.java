package sae.reader;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;

import de.tud.cs.st.bat.ClassFile;
import de.tud.cs.st.bat.reader.Java6Reader;

/**
 * Reads Java class files or JAR and ZIP files containing them and stores their
 * Prolog representations in a {@link FactListDatabase}. The transformation of
 * the bytecode to Prolog is done with the Bytecode Analysis Toolkit (BAT).
 *
 * @author Ralf Mitschke
 * @author David Sullivan
 * @version $Date: 2009-08-12 11:24:16 +0200 (Wed, 12 Aug 2009) $ $Rev: 298 $
 */
public class BytecodeReader {

	private final BytecodeFactProcessor factProcessor;

	public BytecodeReader(BytecodeFactProcessor factProcessor) {
		this.factProcessor = factProcessor;
	}

	/**
	 * Processes the contents of a ZIP or JAR file and stores the Prolog
	 * representations in the {@link Database}.
	 *
	 * @param filename
	 *            The JAR or ZIP file.
	 */
	public void readBytecodeFile(String filename) throws IOException {
		readArchive(new File(filename));
	}

	/**
	 * Processes the contents of a ZIP or JAR file and stores the Prolog
	 * representations in the {@link Database}.
	 *
	 * @param file
	 *            The JAR or ZIP file.
	 */
	public void readArchive(File file) throws IOException {
		if (file.getName().endsWith(".zip") || file.getName().endsWith(".jar")) {
			ZipFile zipFile = new ZipFile(file);
			Enumeration<? extends ZipEntry> zipEntries = zipFile.entries();
			while (zipEntries.hasMoreElements()) {
				ZipEntry zipEntry = zipEntries.nextElement();
				if (!zipEntry.isDirectory()
						&& zipEntry.getName().endsWith(".class")) {
					readClassFile(zipFile.getInputStream(zipEntry));

				}
			}
		} else {
			readClassFile(file);
		}
		//factProcessor.processAllFacts();
	}

	/**
	 * Processes the contents of a ZIP or JAR file and stores the Prolog
	 * representations in the {@link Database}.
	 *
	 * @param stream
	 *            The JAR or ZIP file.
	 */
	public void readArchive(InputStream stream) throws IOException {
		ZipInputStream zipStream = new ZipInputStream(stream);
		ZipEntry zipEntry = null;
		while ((zipEntry = zipStream.getNextEntry()) != null) {
			if (!zipEntry.isDirectory()
					&& zipEntry.getName().endsWith(".class")) {
				//System.out.println("reading ... " + zipEntry.getName());
				readClassFile(new ZipStreamEntryWrapper(zipStream, zipEntry));
			}
		}
		//factProcessor.processAllFacts();
	}

	/**
	 * Processes the contents of a Java class file and stores the Prolog
	 * representations in the {@link Database}.
	 *
	 * @param filename
	 *            The name class file.
	 */
	public void readClassFile(File file) throws IOException {
		FileInputStream fileInputStream = new FileInputStream(file);
		readClassFile(fileInputStream);
		//factProcessor.processAllFacts();
	}

	public void readClassFile(InputStream stream) throws IOException {
		DataInputStream dataInputStream = new DataInputStream(stream);
		ClassFile classFile = (ClassFile) Java6Reader.ClassFile(dataInputStream);
		factProcessor.processClassFile(classFile);
	}

	private static class ZipStreamEntryWrapper extends InputStream {
		private final ZipInputStream stream;
		private final ZipEntry entry;

		public ZipStreamEntryWrapper(ZipInputStream stream, ZipEntry entry) {
				this.stream = stream;
				this.entry = entry;
		}

		/* (non-Javadoc)
		 * @see java.io.InputStream#close()
		 */
		@Override
		public void close() throws IOException {
			stream.closeEntry();
		}

		@Override
		public int read() throws IOException {
			return stream.read();
		}

	}
}
