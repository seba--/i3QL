package sae.benchmark;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLDecoder;
import java.nio.file.DirectoryStream;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * @author Ralf Mitschke
 */
public class Harness {

    public static void main(String[] args) throws IOException, InterruptedException, URISyntaxException {
        String cmd = createCommandLine();
        Runtime runtime = Runtime.getRuntime();

        String[] benchmarks = getBenchmarks();
        new Properties();

        System.out.println("Starting Benchmark: Harness");
        Process process = runtime.exec(cmd);

        int returnCode = process.waitFor();
        System.out.println("Exited with error code " + returnCode);
    }

    public static String[] getBenchmarks() throws IOException, URISyntaxException {
        return getResourceListing(Harness.class, "sae/benchmark/definitions");
        //Enumeration<URL> resources = Harness.class.getClassLoader().getResources("sae.benchmark.definitions");
               /*
        while (resources.hasMoreElements()) {
            URL resource = resources.nextElement();
            System.out.println(resource);
        }
        return null;
        */
    }

    /**
     * List directory contents for a resource folder. Not recursive.
     * This is basically a brute-force implementation.
     * Works for regular files and also JARs.
     *
     * @author Greg Briggs
     * @param clazz Any java class that lives in the same place as the resources you want.
     * @param path Should end with "/", but not start with one.
     * @return Just the name of each member item, not the full paths.
     * @throws URISyntaxException
     * @throws IOException
     */
    private static String[] getResourceListing(Class clazz, String path) throws URISyntaxException, IOException {
        URL dirURL = clazz.getClassLoader().getResource(path);
        if (dirURL != null && dirURL.getProtocol().equals("file")) {
            /* A file path: easy enough */
            return new File(dirURL.toURI()).list();
        }

        if (dirURL == null) {
            /*
            * In case of a jar file, we can't actually find a directory.
            * Have to assume the same jar as clazz.
            */
            String me = clazz.getName().replace(".", "/")+".class";
            dirURL = clazz.getClassLoader().getResource(me);
        }

        if (dirURL.getProtocol().equals("jar")) {
            /* A JAR path */
            String jarPath = dirURL.getPath().substring(5, dirURL.getPath().indexOf("!")); //strip out only the JAR file
            JarFile jar = new JarFile(URLDecoder.decode(jarPath, "UTF-8"));
            Enumeration<JarEntry> entries = jar.entries(); //gives ALL entries in jar
            Set<String> result = new HashSet<String>(); //avoid duplicates in case it is a subdirectory
            while(entries.hasMoreElements()) {
                String name = entries.nextElement().getName();
                if (name.startsWith(path)) { //filter according to the path
                    String entry = name.substring(path.length());
                    int checkSubdir = entry.indexOf("/");
                    if (checkSubdir >= 0) {
                        // if it is a subdirectory, we just return the directory name
                        entry = entry.substring(0, checkSubdir);
                    }
                    result.add(entry);
                }
            }
            return result.toArray(new String[result.size()]);
        }

        throw new UnsupportedOperationException("Cannot list files for URL "+dirURL);
    }


    private static String createCommandLine() {
        final String commandLine = getJavaExecutable();

        addArguments(commandLine, new String[]{
                "-classpath" + " " + getRuntimeClasspath(),
                "Harness"
        });

        return commandLine;
    }

    private static String addArguments(String command, String[] args) {
        StringBuilder stringBuilder = new StringBuilder(command);
        for (String arg : args) {
            stringBuilder.append(" ");
            stringBuilder.append(arg);
        }
        return stringBuilder.toString();
    }

    private static String getDefaultJavaPath() {
        final String javaHome = System.getProperty("java.home");
        return javaHome == null ? null : javaHome + File.separator + "bin" + File.separator + "java";
    }

    private static String getJavaExecutable() {
        String javaExecutable;

        if ((javaExecutable = getDefaultJavaPath()) != null) {
            return javaExecutable;
        } else {
            throw new IllegalStateException("Unable to locate java (JDK)");
        }
    }

    private static String getRuntimeClasspath() {
        return System.getProperty("java.class.path");
    }
}
