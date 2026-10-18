package Utils;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;
import java.lang.annotation.*;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import annotation.UrlMapping;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.Part;
import java.nio.file.*;
import java.util.Properties;

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

    public static List<Class<?>> getClassesWithAnnotation(String NomPackage,
            Class<? extends Annotation> annotationClass) {
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

    public Object ConvertType(String param, Class<?> type) throws ServletException {
        if (type == String.class) {
            return param;
        } else if (type == int.class || type == Integer.class) {
            return Integer.parseInt(param);
        } else if (type == double.class || type == Double.class) {
            return Double.parseDouble(param);
        } else if (type == boolean.class || type == Boolean.class) {
            return Boolean.parseBoolean(param);
        }
        return null;
    }

    public MapFile extraireMapFile(HttpServletRequest request, String nomChampCherche) {
        try {
            Part file = null;

            try {
                file = request.getPart(nomChampCherche);
            } catch (Exception ignored) {
            }

            if (file != null && file.getSize() > 0) {
                MapFile mapFileInstance = new MapFile();

                String nomFile = file.getSubmittedFileName();
                mapFileInstance.setNom(nomFile);

                String extension = "";
                if (nomFile != null && nomFile.contains(".")) {
                    extension = nomFile.substring(nomFile.lastIndexOf("."));
                }
                mapFileInstance.setExtension(extension);

                byte[] bytesPrim = file.getInputStream().readAllBytes();
                Byte[] bytesObj = new Byte[bytesPrim.length];
                for (int k = 0; k < bytesPrim.length; k++) {
                    bytesObj[k] = bytesPrim[k];
                }
                mapFileInstance.setBytes(bytesObj);

                return mapFileInstance;
            }
        } catch (Exception e) {
            System.out.println("Erreur lors de la lecture du fichier pour MapFile : " + e.getMessage());
        }
        return null;
    }

    public void sauvegarderMapFile(MapFile mapFile) {
        String nomFichierProperties = "upload.properties";
        if (mapFile == null || mapFile.getBytes() == null) {
            System.out.println("Aucun fichier à sauvegarder.");
            return;
        }

        try {
            Properties props = new Properties();
            try (InputStream input = getClass().getClassLoader().getResourceAsStream(nomFichierProperties)) {
                if (input == null) {
                    throw new RuntimeException("Fichier properties introuvable : " + nomFichierProperties);
                }
                props.load(input);
            }

            String dossierCible = props.getProperty("upload.path");
            if (dossierCible == null || dossierCible.isEmpty()) {
                throw new RuntimeException("La propriété 'upload.path' est absente de " + nomFichierProperties);
            }

            File dossier = new File(dossierCible);
            if (!dossier.exists()) {
                dossier.mkdirs();
            }

            Byte[] bytesObj = mapFile.getBytes();
            byte[] bytesPrim = new byte[bytesObj.length];
            for (int i = 0; i < bytesObj.length; i++) {
                bytesPrim[i] = bytesObj[i];
            }

            Path cheminDestination = Paths.get(dossierCible, mapFile.getNom());

            try (ByteArrayInputStream inputStream = new ByteArrayInputStream(bytesPrim)) {
                Files.copy(
                        inputStream,
                        cheminDestination,
                        StandardCopyOption.REPLACE_EXISTING);
            }

            System.out.println(
                    "Fichier sauvegardé avec succès via Files.copy dans : " + cheminDestination.toAbsolutePath());

        } catch (Exception e) {
            System.out.println("Erreur lors de la sauvegarde du fichier : " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void remplirObjet(Object instance, Class<?> clazz, String basePrefix, HttpServletRequest request)
            throws Exception {
        Field[] attributs = clazz.getDeclaredFields();

        for (Field attribut : attributs) {
            String nomChamp = attribut.getName();
            Class<?> typeChamp = attribut.getType();
            attribut.setAccessible(true);

            boolean isSimple = typeChamp == String.class || typeChamp == Integer.class
                    || typeChamp == int.class || typeChamp == double.class
                    || typeChamp == Double.class || typeChamp == Boolean.class
                    || typeChamp == boolean.class;

            boolean isMapFile = typeChamp == MapFile.class;

            if (isMapFile) {
                String cle1 = basePrefix + "." + nomChamp;
                String cle2 = basePrefix + "_" + nomChamp;
                String cle3 = nomChamp;

                MapFile mapFile = extraireMapFile(request, cle1);
                if (mapFile == null)
                    mapFile = extraireMapFile(request, cle2);
                if (mapFile == null)
                    mapFile = extraireMapFile(request, cle3);

                if (mapFile != null) {
                    attribut.set(instance, mapFile);
                }

            } else if (!isSimple && !List.class.isAssignableFrom(typeChamp)) {
                Object sousObjet = typeChamp.getDeclaredConstructor().newInstance();
                Field[] sousAttributs = typeChamp.getDeclaredFields();

                for (Field sousAttribut : sousAttributs) {
                    String nomSousChamp = sousAttribut.getName();
                    sousAttribut.setAccessible(true);

                    String cle1 = basePrefix + "." + nomChamp + "." + nomSousChamp;
                    String cle2 = basePrefix + "_" + nomChamp + "_" + nomSousChamp;
                    String cle3 = basePrefix + "[" + nomChamp + "][" + nomSousChamp + "]";

                    String valeur = null;
                    if (request.getParameterMap().containsKey(cle1)) {
                        valeur = request.getParameter(cle1);
                    } else if (request.getParameterMap().containsKey(cle2)) {
                        valeur = request.getParameter(cle2);
                    } else if (request.getParameterMap().containsKey(cle3)) {
                        valeur = request.getParameter(cle3);
                    }

                    if (valeur != null) {
                        if (valeur.isEmpty()) {
                            if (!sousAttribut.getType().isPrimitive()) {
                                sousAttribut.set(sousObjet, null);
                            }
                        } else {
                            Object convert = ConvertType(valeur, sousAttribut.getType());
                            sousAttribut.set(sousObjet, convert);
                        }
                    }
                }
                attribut.set(instance, sousObjet);

            } else {
                String cle1 = basePrefix + "." + nomChamp;
                String cle2 = basePrefix + "_" + nomChamp;
                String cle3 = nomChamp;

                String valeur = null;
                if (request.getParameterMap().containsKey(cle1)) {
                    valeur = request.getParameter(cle1);
                } else if (request.getParameterMap().containsKey(cle2)) {
                    valeur = request.getParameter(cle2);
                } else if (request.getParameterMap().containsKey(cle3)) {
                    valeur = request.getParameter(cle3);
                }

                if (valeur != null) {
                    if (valeur.isEmpty()) {
                        if (!typeChamp.isPrimitive()) {
                            attribut.set(instance, null);
                        }
                    } else {
                        Object convert = ConvertType(valeur, typeChamp);
                        attribut.set(instance, convert);
                    }
                }
            }
        }
    }

}
