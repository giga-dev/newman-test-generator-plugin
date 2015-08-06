package com.gigaspaces.newman.testgenerator;

import com.gigaspaces.newman.testgenerator.scanner.AbstractNewmanScanner;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.project.MavenProject;
import org.json.simple.JSONObject;
import org.reflections.util.ClasspathHelper;
import org.reflections.util.FilterBuilder;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.*;

/**
 * @author Yohana Khoury
 * @goal generate
 * @requiresProject true
 * @requiresDependencyResolution compile
 */
public class NewmanTestGeneratorMojo extends AbstractMojo {

    private Log logger;

    /**
     * @parameter expression="${project.build.directory}/test-metadata.json"
     * @required
     */
    private File outputFile;

    /**
     * @parameter
     * @required
     */
    protected String type;

    /**
     * @parameter
     * @required
     */
    private List<String> packages;

    /**
     * The scanner
     *
     * @parameter expression="${scanner}" default-value="com.gigaspaces.newman.testgenerator.scanner.JUnitScanner"
     */
    private String scanner;

    /**
     * The maven project.
     *
     * @parameter expression="${project}"
     * @readonly
     */
    private MavenProject project;

    /**
     * @parameter expression="${project.build.outputDirectory}"
     * @required
     */
    protected File classesDirectory;

    /**
     * @parameter expression="${project.build.testOutputDirectory}"
     * @required
     */
    protected File testClassesDirectory;

    /**
     * @parameter expression="${includeSources}" default-value=true
     */
    protected boolean includeSources;

    /**
     * @parameter expression="${includeTests}" default-value=false
     */
    protected boolean includeTests;


    public void execute() throws MojoExecutionException {
        logger = getLog();

        try {
            List<String> runtimeClasspathElements = project.getRuntimeClasspathElements();

            URL[] runtimeClasspathElementsUrls = new URL[runtimeClasspathElements.size()];
            for (int i = 0; i < runtimeClasspathElementsUrls.length; i++) {
                runtimeClasspathElementsUrls[i] = new File(runtimeClasspathElements.get(i)).toURI().toURL();
            }

            Set<URL> urls = new HashSet<URL>();
            FilterBuilder filter = new FilterBuilder();


            logger.info("Found " + packages.size() + " package(s) to scan...");
            for (String pkg : packages) {
                urls.addAll(ClasspathHelper.forPackage(pkg));
                filter.includePackage(pkg);
            }

            ArrayList<URL> runtimeClasspathElementsUrlsList = new ArrayList<URL>();
            runtimeClasspathElementsUrlsList.addAll(Arrays.asList(runtimeClasspathElementsUrls));


            if (includeSources) {
                Classpath classpath = new Classpath(Arrays.asList(classesDirectory.getAbsolutePath()));
                ClassLoader cl = classpath.createClassLoader();
                Collection<URL> classPathUrls = ClasspathHelper.forClassLoader(cl);
                urls.addAll(classPathUrls);
                runtimeClasspathElementsUrlsList.addAll(classPathUrls);
            }


            if (includeTests) {
                Classpath classpath = new Classpath(Arrays.asList(testClassesDirectory.getAbsolutePath()));
                ClassLoader cl = classpath.createClassLoader();
                Collection<URL> classPathUrls = ClasspathHelper.forClassLoader(cl);
                urls.addAll(classPathUrls);
                runtimeClasspathElementsUrlsList.addAll(classPathUrls);
            }

            {
                Set<Artifact> classpathArtifacts = project.getArtifacts();
                for (Artifact artifact : classpathArtifacts) {
                    if (artifact.getArtifactHandler().isAddedToClasspath()) {
                        File file = artifact.getFile();
                        if (file != null) {
                            Classpath classpath = new Classpath(Arrays.asList(file.getPath()));
                            ClassLoader cl = classpath.createClassLoader();
                            Collection<URL> classPathUrls = ClasspathHelper.forClassLoader(cl);
                            urls.addAll(classPathUrls);
                            runtimeClasspathElementsUrlsList.addAll(classPathUrls);
                        }
                    }
                }
            }

            URLClassLoader urlClassLoader = new URLClassLoader(runtimeClasspathElementsUrlsList.toArray(new URL[0]), Thread.currentThread().getContextClassLoader());
            Thread.currentThread().setContextClassLoader(urlClassLoader);

            AbstractNewmanScanner newmanScanner = (AbstractNewmanScanner) Class.forName(scanner).newInstance();
            JSONObject object = newmanScanner.scanAndGet(type, filter, urls);

            writeTestsToFile(object, outputFile);

        } catch (Exception e) {
            logger.error(e);
        }
    }


    public void writeTestsToFile(JSONObject object, File permutationFile) throws IOException {
        FileWriter file = new FileWriter(permutationFile);
        try {
            file.write(object.toJSONString());
        } finally {
            file.flush();
            file.close();
        }
    }

}
