package sae.util;

import java.io.File;
import java.io.IOException;
import java.net.JarURLConnection;
import java.net.URL;
import java.util.Collections;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * Author: Ralf Mitschke
 * Created: 29.06.11 13:19
 */
public class JarUtil
{

    private static final String JAR = "jar";

    private static final String CLASS = "class";

    public static URL[] listDirectoryFromClasspath(String directory) throws IOException
    {
        String searchDir = directory;
        if (!directory.endsWith("/"))
            searchDir += "/";
        URL url = JarUtil.class.getClassLoader().getResource(searchDir);
        if (url.getProtocol().equals(JAR)) {
            return findDirectoryEntries(searchDir, ((JarURLConnection) url.openConnection()));
        }
        return new URL[0];
    }

    public static URL getURLFromClasspath(String fileUri)
    {
        return JarUtil.class.getClassLoader().getResource(fileUri);
    }

    public static URL[] resolveDirectoryAndJarUrisFromClasspath(String[] uris) throws IOException
    {
        List<URL> urls = new LinkedList<URL>();
        for (String uri : uris) {
            if (uri.endsWith(JAR)) {
                urls.add(getURLFromClasspath(uri));
                continue;
            }
            if (uri.endsWith(CLASS)) {
                urls.add(getURLFromClasspath(uri));
                continue;
            }


            // check whether this is a directory
            if (uri.endsWith("/") || getURLFromClasspath(uri + "/") != null) {
                URL[] listedURLs = listDirectoryFromClasspath(uri);
                Collections.addAll(urls, listedURLs);
            }
        }
        URL[] result = new URL[urls.size()];
        return urls.toArray(result);
    }

    private static URL[] findDirectoryEntries(String directory, JarURLConnection connection) throws IOException
    {
        List<URL> urls = new LinkedList<URL>();
        JarFile jar = connection.getJarFile();
        String baseUrl = connection.getURL().getFile().substring(
                0,
                connection.getURL().getFile().length() - directory.length()
        );
        for (Enumeration<JarEntry> it = jar.entries(); it.hasMoreElements(); ) {
            JarEntry entry = it.nextElement();
            if (entry.getName().startsWith(directory)) {

                URL u = new URL(connection.getURL().getProtocol(), "", baseUrl + entry.getName());
                urls.add(u);
            }
        }
        URL[] result = new URL[urls.size()];
        return urls.toArray(result);
    }


    public static URL[] resolveDirectoryAndJarUrisFromFilesystem(String[] uris) throws IOException
    {
        List<URL> urls = new LinkedList<URL>();
        for (String uri : uris) {


            if (uri.endsWith(JAR)) {
                urls.add(new File(uri).toURI().toURL());
                continue;
            }
            if (uri.endsWith(CLASS)) {
                urls.add(new File(uri).toURI().toURL());
                continue;
            }

            if (!uri.endsWith("/") &&  !new File(uri + "/").exists())
                continue;
            File file = new File(uri);
            List<File> files = getFilesRecursively(file);
            for (File f : files) {
                urls.add(f.toURI().toURL());
            }
        }
        URL[] result = new URL[urls.size()];
        return urls.toArray(result);
    }


    private static List<File> getFilesRecursively(File dir)
    {
        List<File> files = new LinkedList<File>();
        if (dir.isDirectory()) {
            String[] children = dir.list();
            for (String child : children) {
                File file = new File(dir.getAbsolutePath() + File.separator + child);
                if (file.isDirectory()) {
                    files.addAll(getFilesRecursively(file));
                } else {
                    files.add(file);
                }

            }
        }
        return files;
    }
}
