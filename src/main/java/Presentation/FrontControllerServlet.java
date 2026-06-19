package Presentation;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import java.util.Map;

import Utils.*;

public class FrontControllerServlet extends HttpServlet {

    private List<Class<?>> classesControllers;

        Utilitaire utilitaire = new Utilitaire();

    @Override
    public void init() throws ServletException {
        String packageCible = getInitParameter("packageToScan");

        if (packageCible == null || packageCible.isEmpty()) {
            System.out.println("Erreur : Le paramètre 'packageToScan' n'est pas configuré dans le web.xml !");
            return;
        }

        this.classesControllers = utilitaire.getClassesWithAnnotation(packageCible, annotation.Controller.class);

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
        out.println(uri + "<br>");

        out.println("\n");
        out.println("\n");

        out.println("Classes avec annotation @Controller : " + "<br>");

        if (this.classesControllers != null) {
            for (Class<?> classe : this.classesControllers) {
                out.println(" - " + classe.getName() + "<br><br>");

            }
        } else {
            out.println(" Pas de classes avec l'annotation ");

        }

        Map<String, String> url = utilitaire.GetUrl(this.classesControllers, annotation.UrlMapping.class, path);
        if (url != null && !url.isEmpty()) {
            out.println("Cet URL existe, voici les informations de l'URL : " + "<br>");
            out.println("URL: " + url.get("URL") + "  Class: " + url.get("className") + "  Method: " + url.get("NomMethode") + "<br>");
        } else {

            out.println("Cet URL n'existe pas, voici les URL existant");
            List<Map<String, String>> url1 = utilitaire.GetAllUrl(this.classesControllers, annotation.UrlMapping.class);


            for (Map<String, String> url2 : url1) {
                out.println("URL: " + url2.get("URL") + "  Class: " + url2.get("className") + "  Method: " + url2.get("NomMethode") + "<br>");
            }

        }

    }
}
