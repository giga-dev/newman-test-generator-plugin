package com.gigaspaces.newman.testgenerator.scanner;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.reflections.util.FilterBuilder;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

/**
 * @author Yohana Khoury
 * @since 10.2
 */
public abstract class AbstractNewmanScanner {

    public abstract JSONObject scanAndGet(String type, FilterBuilder filter, Set<URL> urls);

    public JSONArray scanMethods(Set<Method> methods) {
        JSONArray testsArray = new JSONArray();
        int count=0;
        for (Method testMethod : methods) {
            String testClassName = testMethod.getDeclaringClass().getName();
            String testMethodName = testMethod.getName();
            JSONObject test = new JSONObject();
            test.put("name", testClassName + "#" + testMethodName);
            List<String> testPermutations = new ArrayList<String>();
            testPermutations.add(testClassName + "#" + testMethodName);
            test.put("arguments", testPermutations);

            try {
                JSONObject annotations = getTestMethodAnnotations(testMethod);
                test.put("annotations", annotations);
            } catch (Exception e) {
                System.out.println("Failed to load method annotations " + e);
            }
            testsArray.add(test);

        }
        System.out.println("Total enabled: "+count);
        return testsArray;
    }

    private JSONObject getTestMethodAnnotations(Method m) throws InvocationTargetException, IllegalAccessException {
        JSONObject annotations = new JSONObject();
        for (Annotation a : m.getDeclaredAnnotations()) {
            Method[] declaredMethods = a.annotationType().getDeclaredMethods();
            JSONObject annotation = new JSONObject();
            for (Method declaredMethod : declaredMethods) {
                try {
                    Object value = declaredMethod.invoke(a);
                    Object toPut = annotationValueToString(value);
                    annotation.put(declaredMethod.getName(), toPut);
                } catch (Exception e) {
                    e.printStackTrace();
                    return annotations;
                }
            }
            annotations.put(a.annotationType().getName(), annotation);
        }
        return annotations;
    }

    private String annotationValueToString(Object value) {
        String toPut;
        if (value instanceof String[]){
            toPut = Arrays.toString((String[]) value);
        }
        else if (value instanceof String) {
            toPut = (String) value;
        }
        else {
            toPut = value.toString();
        }
        return toPut;
    }
    /*private JSONObject getTestMethodAnnotations2(Method m) throws InvocationTargetException, IllegalAccessException {
        JSONObject annotations = new JSONObject();
        Class klass = m.getDeclaringClass();
        while (klass != null && klass != Object.class) {
            try {
                Method method = klass.getMethod(m.getName());
                if (!method.isAnnotationPresent(org.testng.annotations.Test.class)) {
                    klass = klass.getSuperclass();
                    continue;
                }
                for (Annotation a : method.getDeclaredAnnotations()) {
                    Method[] declaredMethods = a.annotationType().getDeclaredMethods();
                    JSONObject annotation = new JSONObject();
                    for (Method declaredMethod : declaredMethods) {
                        try {
                            Object value = declaredMethod.invoke(a);
                            Object toPut = annotationValueToString(value);
                            annotation.put(declaredMethod.getName(), toPut);
                        } catch (Exception e) {
                            e.printStackTrace();
                            return annotations;
                        }
                    }
                    annotations.put(a.annotationType().getName(), annotation);
                }
            } catch (Exception e) {
                break;
            }
            klass = klass.getSuperclass();
        }
        return annotations;
    }*/

/*    private Object annotationValueToString(Object value) {
        if (value instanceof Class) {
            return ((Class) value).toString();
        } else if (value instanceof String[]) {
            return Arrays.toString((String[]) value);
        } else if (value instanceof String) {
            return (String) value;
        } else if (value instanceof Number) {
            return value.toString();
        } else {
            return value.toString();
        }
    }*/
}
