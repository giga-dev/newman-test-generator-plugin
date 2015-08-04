package com.gigaspaces.newman.testgenerator;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.*;

/**
 * Created by Barak Bar Orion
 * 30/10/14.
 */
public class Classpath implements Iterable<String> {

    private final List<String> unmodifiableElements;

    @SuppressWarnings("UnusedDeclaration")
    public static Classpath join(Classpath firstClasspath, Classpath secondClasspath) {
        LinkedHashSet<String> accumulated = new LinkedHashSet<String>();
        if (firstClasspath != null) {
            firstClasspath.addTo(accumulated);
        }
        if (secondClasspath != null) {
            secondClasspath.addTo(accumulated);
        }
        return new Classpath(accumulated);
    }


    private void addTo(Collection<String> c) {
        c.addAll(unmodifiableElements);
    }

    private Classpath() {
        this.unmodifiableElements = Collections.emptyList();
    }


    public Classpath(Classpath other, String additionalElement) {
        ArrayList<String> elems = new ArrayList<String>(other.unmodifiableElements);
        elems.add(additionalElement);
        this.unmodifiableElements = Collections.unmodifiableList(elems);
    }

    public Classpath(Iterable<String> elements) {
        List<String> newCp = new ArrayList<String>();
        for (String element : elements) {
            newCp.add(element);
        }
        this.unmodifiableElements = Collections.unmodifiableList(newCp);
    }

    @SuppressWarnings("UnusedDeclaration")
    public static Classpath emptyClasspath() {
        return new Classpath();
    }

    @SuppressWarnings("UnusedDeclaration")
    public Classpath addClassPathElementUrl(String path) {
        if (path == null) {
            throw new IllegalArgumentException("Null is not a valid class path element url.");
        }
        return !unmodifiableElements.contains(path) ? new Classpath(this, path) : this;
    }

    @SuppressWarnings("UnusedDeclaration")
    public List<String> getClassPath() {
        return unmodifiableElements;
    }

    public List<URL> getAsUrlList() throws MalformedURLException {
        List<URL> urls = new ArrayList<URL>();
        for (String url : unmodifiableElements) {
            File f = new File(url);
            urls.add(UrlUtils.getURL(f));
        }
        return urls;
    }


    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        Classpath classpath = (Classpath) o;

        return !(unmodifiableElements != null
                ? !unmodifiableElements.equals(classpath.unmodifiableElements)
                : classpath.unmodifiableElements != null);

    }

    public ClassLoader createClassLoader() throws MalformedURLException {
        List<URL> urls = getAsUrlList();
        return new URLClassLoader(urls.toArray(new URL[urls.size()]));
    }


    public int hashCode() {
        return unmodifiableElements != null ? unmodifiableElements.hashCode() : 0;
    }

    public Iterator<String> iterator() {
        return unmodifiableElements.iterator();
    }
}
