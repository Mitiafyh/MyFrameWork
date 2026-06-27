package Presentation;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import Utils.*;
import annotation.UrlMapping;

public class FrontControllerServlet extends HttpServlet {

    private Map<String, Mapping> mappingUrls = new HashMap<>();

    Utilitaire utilitaire = new Utilitaire();

    @Override
    public void init() throws ServletException {
        String packageCible = getInitParameter("packageToScan");

        if (packageCible == null || packageCible.isEmpty()) {
            System.out.println("Erreur : Le paramètre 'packageToScan' n'est pas configuré dans le web.xml !");
            return;
        }
        List<Class<?>> classesControllers = utilitaire.getClassesWithAnnotation(packageCible, annotation.Controller.class);
        if (classesControllers != null) {
            for (Class<?> classe : classesControllers) {
                try {
                    List<Method> methodes = utilitaire.getAllMethodeAnnote(classe, annotation.UrlMapping.class);
                    if (methodes != null && !methodes.isEmpty()) {
                        Object instanceUnique = classe.getDeclaredConstructor().newInstance();
                        for (Method methode : methodes) {
                            UrlMapping urlMapping = methode.getAnnotation(UrlMapping.class);
                            Mapping mapping = new Mapping(instanceUnique, methode);
                            this.mappingUrls.put(urlMapping.value(), mapping);
                        }
                    }
                } catch (Exception e) {
                    System.out.println("Erreur lors du pré-chargement du Singleton pour la classe " + classe.getName());
                    e.printStackTrace();
                }
            }
        }
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        processRequest(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        processRequest(request, response);
    }

    protected void processRequest(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String uri = request.getRequestURI();
        String path = request.getPathInfo();

        response.setContentType("text/html");
        PrintWriter out = response.getWriter();

        if (this.mappingUrls.containsKey(path)) {
            Mapping cible = this.mappingUrls.get(path);

            out.println("<h3>Route trouvée !</h3>");
            out.println("URL  : " + path + "<br>");
            out.println("Classe : " + cible.getControllerInstance().getClass().getName() + "<br>");
            out.println("Méthode associée : " + cible.getMethode().getName() + "()<br>");
        } else {
            out.println("<h3> Aucune méthode ne correspond à l'URL : " + path + "</h3>");
            out.println("<h3>Liste des routes disponibles :</h3>");
            for (Map.Entry<String, Mapping> exist : this.mappingUrls.entrySet()) {
                String url = exist.getKey();
                Mapping mapping = exist.getValue();
                out.println("URL  : " + url + "<br>");
                out.println("Classe : " + mapping.getControllerInstance().getClass().getName() + "<br>");
                out.println("Méthode associée : " + mapping.getMethode().getName() + "()<br>");
            }
        }

    }
}
