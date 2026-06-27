package Utils;

import annotation.UrlMapping;
import java.io.File;
import java.lang.annotation.*;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Utilitaire {

    public List<Method> getAllMethodeAnnote(Class<?> classe, Class<? extends Annotation> annotationClass) {
        List<Method> listeMethode = new ArrayList<>();
        Method[] methods = classe.getDeclaredMethods();
        for (Method methode : methods) {
            if (methode.isAnnotationPresent(annotationClass)) {
                listeMethode.add(methode);
            }
        }
        return listeMethode;
    }

    public List<Class<?>> getClassesWithAnnotation(String NomPackage, Class<? extends Annotation> annotationClass) {
        List<Class<?>> listeClasse = getAllClasse(NomPackage);
        List<Class<?>> classWithAnnotation = new ArrayList<>();
        for (Class<?> classe : listeClasse) {
            if (classe.isAnnotationPresent(annotationClass)) {
                classWithAnnotation.add(classe);
            }
        }
        return classWithAnnotation;
    }

    public List<Class<?>> getAllClasse(String NomPackage) {
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
