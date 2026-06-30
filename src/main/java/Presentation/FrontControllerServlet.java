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
import java.rmi.server.ServerCloneException;

public class FrontControllerServlet extends HttpServlet {

    private Map<UrlMethod, Mapping> mappingUrls = new HashMap<>();

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
<<<<<<< HEAD
               
=======
>>>>>>> Sprint3_BIs_execution
                    List<Method> methodes = utilitaire.getAllMethodeAnnote(classe, annotation.UrlMapping.class);
                    if (methodes != null && !methodes.isEmpty()) {
                        for (Method methode : methodes) {
                            UrlMapping urlMapping = methode.getAnnotation(UrlMapping.class);
                            String url = urlMapping.value();
                            String httpMethod = urlMapping.method().toUpperCase();
                            HttpMethod method = HttpMethod.valueOf(httpMethod);
                            UrlMethod urlMethod = new UrlMethod(url, method);

                            if (this.mappingUrls.containsKey(urlMethod)) {
                                throw new ServletException("Erreur : Conflit de mapping pour l'URL " 
                                + url + " avec la méthode HTTP " + httpMethod 
                                + ". Deux méthodes annotées avec @UrlMapping ont le même chemin et la même méthode HTTP.");
                            }

                            Mapping mapping = new Mapping(classe, methode);
                            this.mappingUrls.put(urlMethod, mapping);
                        }
                    }
<<<<<<< HEAD
               
=======
                
>>>>>>> Sprint3_BIs_execution
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
        if (path == null || path.equals("/")) {
            path = request.getServletPath();
        }

        HttpMethod requestMethod = HttpMethod.valueOf(request.getMethod().toUpperCase());
        UrlMethod urlMethod = new UrlMethod(path, requestMethod);

        response.setContentType("text/html");
        PrintWriter out = response.getWriter();

        if (this.mappingUrls.containsKey(urlMethod)) {
            Mapping cible = this.mappingUrls.get(urlMethod);

            out.println("<h3>Route trouvée !</h3>");
            out.println("URL  : " + urlMethod.getUrl() + ",methode " + urlMethod.getMethod() + "<br>");
            out.println("Classe : " + cible.getControllerInstance().getName() + "<br>");
            out.println("Méthode associée : " + cible.getMethode().getName() + "()<br>");
            try {
                Class<?> classeDuControleur = cible.getControllerInstance();
                Object instanceControleur = classeDuControleur.getDeclaredConstructor().newInstance();
                Method methodeAExecuter = cible.getMethode();
                methodeAExecuter.invoke(instanceControleur);

            } catch (Exception e) {
                e.printStackTrace(out);
                out.println("<h3>Erreur lors de l'exécution de la méthode : " + e.getMessage() + "</h3>");
            }
        } else {
            out.println("<h3> Aucune méthode ne correspond à l'URL : " + path + ",methode " + requestMethod + "</h3>");
            out.println("<h3>Liste des routes disponibles :</h3>");
            for (Map.Entry<UrlMethod, Mapping> exist : this.mappingUrls.entrySet()) {
                UrlMethod methode = exist.getKey();
                Mapping mapping = exist.getValue();
                out.println("URL  : " + methode.getUrl() + ", Méthode : " + methode.getMethod() + "<br>");
                out.println("Classe : " + mapping.getControllerInstance().getName() + "<br>");
                out.println("Méthode associée : " + mapping.getMethode().getName() + "()<br>");
            }
        }

    }
}
