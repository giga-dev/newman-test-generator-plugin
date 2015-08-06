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
import java.lang.reflect.Modifier;
import java.net.URL;
import java.util.HashSet;
import java.util.Set;

/**
 * @author Kobi Kisos, Yohana Khoury
 * @since 10.2
 */
public class TestNGScanner extends AbstractNewmanScanner {
    @Override
    public boolean isFilteredByAnnotations(JSONObject annotations) {
        return annotations.get("org.testng.annotations.Test.enabled").equals("false");
    }

    @Override
    public Class getTestAnnotationClass() {
        return org.testng.annotations.Test.class;
    }

    @Override
    public JSONObject scanAndGet(String type, FilterBuilder filter, Set<URL> urls) throws Exception {


        Reflections reflections = new Reflections(new ConfigurationBuilder()
                .setScanners(new SubTypesScanner(false), new MethodAnnotationsScanner(), new ResourcesScanner())
                .setUrls(urls)
                .filterInputsBy(filter));

        Set<Method> methods = new HashSet<Method>();
        Set<Class<?>> classes = reflections.getSubTypesOf(Object.class);
        for (Class clazz : classes) {
            if (Modifier.isAbstract(clazz.getModifiers())) {
                continue;
            }

            Method[] methods1 = clazz.getDeclaredMethods();
            for (Method method : methods1) {
                Class klass = clazz;
                while (klass != Object.class && klass != null) {
                    try {
                        Method klassMethod = klass.getDeclaredMethod(method.getName(), method.getParameterTypes());
                        if (klassMethod.isAnnotationPresent(getTestAnnotationClass())) {
                            methods.add(method);
                            break;
                        }
                    } catch (NoSuchMethodException e) {
                        break;
                    }
                    klass = klass.getSuperclass();
                }
            }
        }

        System.out.println("Total methods: " + methods.size());
        JSONArray testsJSON = scanMethods(methods);
        System.out.println("Type: " + type);
        JSONObject object = new JSONObject();
        object.put("type", type);
        object.put("tests", testsJSON);
        return object;
    }
}
