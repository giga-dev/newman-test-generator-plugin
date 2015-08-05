package com.gigaspaces.newman.testgenerator.scanner;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.reflections.Reflections;
import org.reflections.scanners.MethodAnnotationsScanner;
import org.reflections.scanners.ResourcesScanner;
import org.reflections.scanners.SubTypesScanner;
import org.reflections.util.ConfigurationBuilder;
import org.reflections.util.FilterBuilder;

import java.lang.reflect.Method;
import java.net.URL;
import java.util.HashSet;
import java.util.Set;

/**
 * @author Yohana Khoury
 * @since 10.2
 */
public class JUnitScanner extends AbstractNewmanScanner {


    @Override
    public boolean isFilteredByAnnotations(JSONObject annotations) {
        return false;
    }

    @Override
    public Class getTestAnnotationClass() {
        return org.junit.Test.class;
    }

    @Override
    public JSONObject scanAndGet(String type, FilterBuilder filter, Set<URL> urls) throws Exception {
        Reflections reflections = new Reflections(
                new ConfigurationBuilder().
                        setScanners(new SubTypesScanner(false), new MethodAnnotationsScanner(), new ResourcesScanner()).
                        filterInputsBy(filter).
                        setUrls(urls));

        Set<Method> methods = reflections.getMethodsAnnotatedWith(getTestAnnotationClass());

        JSONArray testsJSON = scanMethods(methods);
        System.out.println("Type: " + type+", Total methods: "+methods.size());
        JSONObject object = new JSONObject();
        object.put("type", type);
        object.put("tests", testsJSON);
        return object;
    }
}
