package sae.benchmark;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * @author Ralf Mitschke
 */
public class Harness {

    private static final String BAT_TIME_PROFILER = "sae.bytecode.analyses.profiler.BATAnalysesTimeProfiler";

    private static final String SAE_OO_TIME_PROFILER = "sae.bytecode.analyses.profiler.SAEAnalysesOOTimeProfiler";

    private static final String SAE_OO_MEMORY_PROFILER = "sae.bytecode.analyses.profiler.SAEAnalysesOOMemoryProfiler";

    private static final String SAE_Rel_TIME_PROFILER = "sae.bytecode.analyses.profiler.SAEAnalysesRelTimeProfiler";

    private static final String SAE_Rel_MEMORY_PROFILER = "sae.bytecode.analyses.profiler.SAEAnalysesRelMemoryProfiler";

    private static final String SAE_OO_REPLAY_TIME_PROFILER = "sae.bytecode.analyses.profiler.SAEAnalysesOOReplayTimeProfiler";


    public static void main(String[] args) throws IOException, InterruptedException, URISyntaxException {
        String cmd = createCommandLine();
        Runtime runtime = Runtime.getRuntime();
        String definitionsDir = "sae/benchmark/definitions";
        String reReadJars = "false";
        if (args.length >= 1) {
            definitionsDir = args[0];
        }
        if(args.length >= 2) {
            reReadJars = args[1];
        }

        System.out.println("Harness started");
        System.out.println("re read jars = " + reReadJars);
        System.out.println("reading profiles from: " + definitionsDir);
        System.out.println(System.getProperty("user.dir"));

        String[] benchmarks = getBenchmarkCommands(definitionsDir);

        for (String benchmark : benchmarks) {
            String exec = addArguments(cmd, new String[]{benchmark, reReadJars});
            System.out.println(exec);
            Process process = runtime.exec(exec);

            int returnCode = process.waitFor();
            System.out.println("Exited with error code " + returnCode);
        }
    }

    public static String[] getBenchmarkCommands(String definitionsDirectory) throws IOException, URISyntaxException {
        String[] resources = getRecursiveResourceListing(Harness.class, definitionsDirectory);

        List<String> benchmarks = new ArrayList<String>();
        for (String s : resources) {
            if (s.endsWith(".properties")) {
                benchmarks.add(s);
            }
        }

        List<String> commands = new ArrayList<String>();

        for (String benchmark : benchmarks) {
            Properties properties = new Properties();
            properties.load(Harness.class.getClassLoader().getResource(benchmark).openStream());

            String benchmarkType = properties.getProperty("sae.benchmark.type", "SAEOO");
            if (benchmarkType.equals("SAEOO")) {
                commands.add(SAE_OO_TIME_PROFILER + " " + benchmark);
                commands.add(SAE_OO_MEMORY_PROFILER + " " + benchmark);
            }
            if (benchmarkType.equals("SAERel")) {
                commands.add(SAE_Rel_TIME_PROFILER + " " + benchmark);
                commands.add(SAE_Rel_MEMORY_PROFILER + " " + benchmark);
            }
            if (benchmarkType.equals("BAT")) {
                commands.add(BAT_TIME_PROFILER + " " + benchmark);
            }
            if (benchmarkType.equals("SAEOOReplay")) {
                commands.add(SAE_OO_REPLAY_TIME_PROFILER + " " + benchmark);
            }
        }

        return commands.toArray(new String[commands.size()]);
    }

    /**
     * List directory contents for a resource folder. Not recursive.
     * This is basically a brute-force implementation.
     * Works for regular files and also JARs.
     *
     * @param clazz Any java class that lives in the same place as the resources you want.
     * @param path  Should end with "/", but not start with one.
     * @return Just the name of each member item, not the full paths.
     * @throws URISyntaxException
     * @throws IOException
     * @author Greg Briggs
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
            String me = clazz.getName().replace(".", "/") + ".class";
            dirURL = clazz.getClassLoader().getResource(me);
        }

        if (dirURL.getProtocol().equals("jar")) {
            /* A JAR path */
            String jarPath = dirURL.getPath().substring(5, dirURL.getPath().indexOf("!")); //strip out only the JAR file
            JarFile jar = new JarFile(URLDecoder.decode(jarPath, "UTF-8"));
            Enumeration<JarEntry> entries = jar.entries(); //gives ALL entries in jar
            Set<String> result = new HashSet<String>(); //avoid duplicates in case it is a subdirectory
            while (entries.hasMoreElements()) {
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

        throw new UnsupportedOperationException("Cannot list files for URL " + dirURL);
    }


    /**
     * List directory contents for a resource folder. !!Recursive!!.
     * This is basically a brute-force implementation.
     * Works for regular files and also JARs.
     *
     * @param clazz Any java class that lives in the same place as the resources you want.
     * @param path  Should end with "/", but not start with one.
     * @return Just the name of each member item, not the full paths.
     * @throws URISyntaxException
     * @throws IOException
     * @author Greg Briggs
     * @author Ralf Mitschke
     */
    private static String[] getRecursiveResourceListing(Class clazz, String path) throws URISyntaxException, IOException {
        URL dirURL = clazz.getClassLoader().getResource(path);
        if (dirURL != null && dirURL.getProtocol().equals("file")) {
            /* A file path: easy enough */
            String[] entries = new File(dirURL.toURI()).getAbsoluteFile().list();
            String[] results = new String[0];
            if (entries == null)
                return results;
            else {
                for (int i = 0; i < entries.length; i++) {
                    entries[i] = path + "/" + entries[i];
                }

                results = entries;
            }

            for (String entry : entries) {
                String[] childEntries = getRecursiveResourceListing(clazz, entry);
                int oldLength = results.length;
                results = Arrays.copyOf(results, results.length + childEntries.length);
                for (int i = oldLength; i < results.length; i++) {
                    results[i] = childEntries[i - oldLength];
                }
            }
            return results;
        }

        if (dirURL == null) {
            /*
            * In case of a jar file, we can't actually find a directory.
            * Have to assume the same jar as clazz.
            */
            String me = clazz.getName().replace(".", "/") + ".class";
            dirURL = clazz.getClassLoader().getResource(me);
        }

        if (dirURL.getProtocol().equals("jar")) {
            /* A JAR path */
            String jarPath = dirURL.getPath().substring(5, dirURL.getPath().indexOf("!")); //strip out only the JAR file
            JarFile jar = new JarFile(URLDecoder.decode(jarPath, "UTF-8"));
            Enumeration<JarEntry> entries = jar.entries(); //gives ALL entries in jar
            Set<String> result = new HashSet<String>(); //avoid duplicates in case it is a subdirectory
            while (entries.hasMoreElements()) {
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

        throw new UnsupportedOperationException("Cannot list files for URL " + dirURL);
    }

    private static String createCommandLine() {
        String commandLine = getJavaExecutable();

        commandLine = addArguments(commandLine, new String[]{
                "-classpath" + " " + getRuntimeClasspath() 
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
