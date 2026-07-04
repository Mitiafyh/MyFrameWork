package Presentation;

import jakarta.servlet.RequestDispatcher;
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

public class FrontControllerServlet extends HttpServlet {

    private Map<UrlMethod, Mapping> mappingUrls;
    private String prefix;
    private String suffix;

    @Override
    public void init() throws ServletException {
        this.mappingUrls = (Map<UrlMethod, Mapping>) getServletContext().getAttribute("mappingUrls");
        if (this.mappingUrls == null) {
            throw new ServletException("Le mapping des URL n'a pas été initialisé. Assurez-vous que le RequestControllerListener est correctement configuré.");
        }
        this.prefix = getServletContext().getInitParameter("viewPrefix");
        this.suffix = getServletContext().getInitParameter("viewSuffix");

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
                Object resultat = methodeAExecuter.invoke(instanceControleur);
                if(resultat instanceof ModelAndView){
                    ModelAndView mv = (ModelAndView) resultat;

                    for(Map.Entry<String, Object> attribut : mv.getAttribut().entrySet()){
                        request.setAttribute(attribut.getKey(), attribut.getValue());
                    }
                    String prochaineVue = mv.getViewName();
                    String cheminComplet = this.prefix + prochaineVue + this.suffix;
                    RequestDispatcher dispatcher = request.getRequestDispatcher(cheminComplet);
                    dispatcher.forward(request, response);

                }else{
                    out.println("<h3>Route trouvée mais aucun ModelView renvoyé.</h3>");
                }

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
