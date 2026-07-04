package Utils;

import java.io.File;
import java.lang.annotation.*;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import annotation.UrlMapping;
import jakarta.servlet.ServletException;

public class Utilitaire {

    public static Map<UrlMethod, Mapping> getMappings(String packageCible) throws Exception {
        Map<UrlMethod, Mapping> mappingUrls = new HashMap<>();
        List<Class<?>> classesControllers = getClassesWithAnnotation(packageCible, annotation.Controller.class);
        if (classesControllers != null) {
            for (Class<?> classe : classesControllers) {
                List<Method> methodes = getAllMethodeAnnote(classe, annotation.UrlMapping.class);
                if (methodes != null && !methodes.isEmpty()) {
                    for (Method methode : methodes) {
                        UrlMapping urlMapping = methode.getAnnotation(UrlMapping.class);
                        String url = urlMapping.value();
                        String httpMethod = urlMapping.method().toUpperCase();
                        HttpMethod method = HttpMethod.valueOf(httpMethod);
                        UrlMethod urlMethod = new UrlMethod(url, method);

                        if (mappingUrls.containsKey(urlMethod)) {
                            throw new Exception("Mapping dupliqué : " + url + " " + httpMethod);
                        }

                        Mapping mapping = new Mapping(classe, methode);
                        mappingUrls.put(urlMethod, mapping);
                    }
                }
            }
        }
        return mappingUrls;
    }

    public static List<Method> getAllMethodeAnnote(Class<?> classe, Class<? extends Annotation> annotationClass) {
        List<Method> listeMethode = new ArrayList<>();
        Method[] methods = classe.getDeclaredMethods();
        for (Method methode : methods) {
            if (methode.isAnnotationPresent(annotationClass)) {
                listeMethode.add(methode);
            }
        }
        return listeMethode;
    }

    public static List<Class<?>> getClassesWithAnnotation(String NomPackage, Class<? extends Annotation> annotationClass) {
        List<Class<?>> listeClasse = getAllClasse(NomPackage);
        List<Class<?>> classWithAnnotation = new ArrayList<>();
        for (Class<?> classe : listeClasse) {
            if (classe.isAnnotationPresent(annotationClass)) {
                classWithAnnotation.add(classe);
            }
        }
        return classWithAnnotation;
    }

    public static List<Class<?>> getAllClasse(String NomPackage) {
        List<Class<?>> listClass = new ArrayList<>();
        String path = NomPackage.replace(".", "/");
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        URL resource = classLoader.getResource(path);
        if (resource == null) {
            return listClass;
        }
        try {
            File directory = new File(resource.toURI());
            File[] files = directory.listFiles();

            if (files != null) {
                for (File file : files) {
                    String classNameWithoutEnd = file.getName().replace(".class", "");
                    String fullClassName = NomPackage + "." + classNameWithoutEnd;
                    Class<?> classe = Class.forName(fullClassName);
                    listClass.add(classe);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return listClass;
    }

}
