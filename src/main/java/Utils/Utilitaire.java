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
    // public static void afficher(List<Class<?>> listeclasse,Class<? extends Annotation> annotationClass, String url){
    //     boolean find = false;
    //     String affiche = "";
    //     for (Class classe : listeclasse){
    //         for(Method methode : getAllMethodeAnnote(classe,annotationClass)){
    //             if(methode.getAnnotation(annotationClass).equals(url)){
    //                 find = true;
    //                  affiche = url +"   " + classe.getName()+"   "+methode.getName();
    //                break;
    //             }
    //         }
    //     }
    //     if(find==true){
    //         System.out.println(affiche);
    //     }else{
    //         System.out.println("les methode existant: ");
    //         for (Class<?> classe : listeclasse){
    //         for(Method methode : getAllMethodeAnnote(classe,annotationClass)){
    //             System.out.println(methode.getAnnotation(annotationClass)+"   "+  classe.getName()+"   "+methode.getName());
    //         }
    //     }
    //     }
    // }

    public List<Map<String, String>> GetAllUrl(List<Class<?>> listeclasse, Class<? extends Annotation> annotationClass) {
        List<Map<String, String>> methode = new ArrayList<>();
        if (listeclasse == null) {
            return methode;
        }

        for (Class<?> classe : listeclasse) {
            for (Method method : getAllMethodeAnnote(classe, annotationClass)) {
                UrlMapping urlMapping = method.getAnnotation(UrlMapping.class);
                String url = urlMapping.value();
                Map<String, String> methodel = new HashMap<>();
                methodel.put("className", classe.getSimpleName());
                methodel.put("NomMethode", method.getName());
                methodel.put("URL", url);
                methode.add(methodel);
            }
        }
        return methode;
    }

    public Map<String, String> GetUrl(List<Class<?>> listeclasse, Class<? extends Annotation> annotationClass, String url) {
        if (listeclasse == null || url == null) {
            return null;
        }

        for (Class<?> classe : listeclasse) {
            for (Method method : getAllMethodeAnnote(classe, annotationClass)) {
                UrlMapping urlMapping = method.getAnnotation(UrlMapping.class);
                String urlAnnotation = urlMapping.value();
                if (urlAnnotation.equals(url)) {
                    Map<String, String> methodel = new HashMap<>();
                    methodel.put("className", classe.getSimpleName());
                    methodel.put("NomMethode", method.getName());
                    methodel.put("URL", urlAnnotation);
                    return methodel;
                }
            }
        }
        return null;
    }

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
